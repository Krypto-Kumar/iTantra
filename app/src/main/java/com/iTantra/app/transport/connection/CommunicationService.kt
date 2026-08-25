package com.iTantra.app.transport.connection

import android.bluetooth.BluetoothDevice
import com.iTantra.app.transport.protocol.Message
import com.iTantra.app.transport.protocol.MessageSerializer
import com.iTantra.app.transport.protocol.ProtocolEnvelope
import com.iTantra.app.transport.protocol.ProtocolMessageType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Main application entry point for iTantra transport layer.
 * Provides clean boundary for future STT (sendMessage) and TTS (incomingMessages).
 *
 * Features:
 * - Clean STT/TTS isolation
 * - Automated protocol ACKs
 * - Bounded retry mechanism (MAX_RETRIES = 3)
 * - In-memory duplicate filtering by message ID
 * - Ordered stream processing
 */
class CommunicationService(
    private val scope: CoroutineScope,
    private val ackTimeoutMs: Long = 2000L,
    private val maxRetries: Int = 3
) {
    private val bluetoothManager = BluetoothManager(scope)

    val connectionState: StateFlow<ConnectionState> = bluetoothManager.connectionState

    private val _incomingMessages = MutableSharedFlow<Message>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<Message> = _incomingMessages.asSharedFlow()

    private val _sendFailures = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val sendFailures: SharedFlow<String> = _sendFailures.asSharedFlow()

    // Map of messageId -> CompletableDeferred<Unit> waiting for ACK
    private val pendingAcks = ConcurrentHashMap<String, CompletableDeferred<Unit>>()

    // Set of processed message IDs to filter duplicates
    private val processedMessageIds = ConcurrentHashMap.newKeySet<String>()

    private var processJob: Job? = null

    init {
        startIncomingFrameProcessor()
    }

    /**
     * Starts listening for incoming Bluetooth RFCOMM server connections.
     */
    fun startServer() {
        bluetoothManager.startListeningServer()
    }

    /**
     * Connects to a target paired [BluetoothDevice].
     */
    fun connectToDevice(device: BluetoothDevice) {
        bluetoothManager.connectToDevice(device)
    }

    /**
     * Closes connections cleanly.
     */
    fun disconnect() {
        bluetoothManager.disconnect()
    }

    /**
     * Listens for raw incoming framed lines from [BluetoothManager], deserializes envelopes,
     * handles ACKs, filters duplicates, and emits new valid messages.
     */
    private fun startIncomingFrameProcessor() {
        processJob = scope.launch(Dispatchers.Default) {
            bluetoothManager.incomingFrames.collect { rawFrame ->
                val envelope = MessageSerializer.deserializeEnvelope(rawFrame) ?: return@collect

                when (envelope.type) {
                    ProtocolMessageType.ACK -> {
                        envelope.ackMessageId?.let { ackId ->
                            pendingAcks[ackId]?.complete(Unit)
                        }
                    }
                    ProtocolMessageType.DATA -> {
                        val message = envelope.message ?: return@collect

                        // Automatically send ACK back to sender
                        sendAck(message.id)

                        // Check for duplicate message ID
                        val isNewMessage = processedMessageIds.add(message.id)
                        if (isNewMessage) {
                            _incomingMessages.emit(message)
                        }
                    }
                }
            }
        }
    }

    /**
     * Sends an ACK protocol envelope for the target [messageId].
     */
    private fun sendAck(messageId: String) {
        scope.launch(Dispatchers.IO) {
            val ackEnvelope = ProtocolEnvelope(
                envelopeId = "env-ack-$messageId",
                type = ProtocolMessageType.ACK,
                ackMessageId = messageId
            )
            val bytes = MessageSerializer.serializeEnvelopeToBytes(ackEnvelope)
            bluetoothManager.sendRawBytes(bytes)
        }
    }

    /**
     * Transmits a logical [Message] over RFCOMM with bounded retries and ACK wait.
     * Called by STT or typed user message interface.
     *
     * @param message Logical data model containing id, language, timestamp, and text
     * @return Boolean true if message was acknowledged, false if failed after max retries
     */
    suspend fun sendMessage(message: Message): Boolean = withContext(Dispatchers.IO) {
        if (connectionState.value != ConnectionState.CONNECTED) {
            return@withContext false
        }

        val envelope = ProtocolEnvelope(
            envelopeId = "env-${message.id}",
            type = ProtocolMessageType.DATA,
            message = message
        )
        val payloadBytes = MessageSerializer.serializeEnvelopeToBytes(envelope)

        var attempts = 0
        var ackReceived = false

        while (attempts < maxRetries && !ackReceived) {
            attempts++
            val ackDeferred = CompletableDeferred<Unit>()
            pendingAcks[message.id] = ackDeferred

            val sendSuccess = bluetoothManager.sendRawBytes(payloadBytes)
            if (sendSuccess) {
                try {
                    // Wait for ACK with timeout
                    kotlinx.coroutines.withTimeout(ackTimeoutMs) {
                        ackDeferred.await()
                    }
                    ackReceived = true
                } catch (e: Exception) {
                    // ACK timed out, loop will retry
                } finally {
                    pendingAcks.remove(message.id)
                }
            } else {
                pendingAcks.remove(message.id)
                break
            }
        }

        if (!ackReceived) {
            _sendFailures.emit(message.id)
        }

        return@withContext ackReceived
    }

    /**
     * Clears processed duplicate ID history.
     */
    fun clearDuplicateHistory() {
        processedMessageIds.clear()
    }
}
