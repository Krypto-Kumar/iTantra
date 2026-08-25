package com.iTantra.app.transport.connection

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.iTantra.app.transport.bluetooth.BluetoothClient
import com.iTantra.app.transport.bluetooth.BluetoothPermissionHelper
import com.iTantra.app.transport.bluetooth.BluetoothServer
import com.iTantra.app.transport.protocol.StreamFramingBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

/**
 * High-level connection manager coordinating Bluetooth Classic RFCOMM server/client sockets,
 * managing connection states, reading incoming streams into framed lines, and queuing writes.
 */
class BluetoothManager(
    private val scope: CoroutineScope
) {
    private val _connectionState = MutableStateFlow(
        if (BluetoothPermissionHelper.isBluetoothSupported()) ConnectionState.DISCONNECTED else ConnectionState.UNAVAILABLE
    )
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingFrames = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingFrames: SharedFlow<String> = _incomingFrames.asSharedFlow()

    private var activeSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private val sendQueue = SendQueue()
    private val framingBuffer = StreamFramingBuffer()

    private var server: BluetoothServer? = null
    private var client: BluetoothClient? = null

    private var listenerJob: Job? = null
    private var readJob: Job? = null

    /**
     * Starts listening for incoming RFCOMM connections as a Server.
     */
    fun startListeningServer() {
        if (_connectionState.value == ConnectionState.CONNECTED) return

        disconnect()
        _connectionState.value = ConnectionState.WAITING

        listenerJob = scope.launch(Dispatchers.IO) {
            val serverInstance = BluetoothServer()
            server = serverInstance
            val socket = serverInstance.listenForConnection()

            if (socket != null) {
                onSocketConnected(socket)
            } else if (_connectionState.value == ConnectionState.WAITING) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        }
    }

    /**
     * Connects to a target paired [BluetoothDevice] as a Client.
     */
    fun connectToDevice(device: BluetoothDevice) {
        disconnect()
        _connectionState.value = ConnectionState.CONNECTING

        listenerJob = scope.launch(Dispatchers.IO) {
            val clientInstance = BluetoothClient()
            client = clientInstance
            val socket = clientInstance.connect(device)

            if (socket != null) {
                onSocketConnected(socket)
            } else {
                _connectionState.value = ConnectionState.ERROR
            }
        }
    }

    /**
     * Called upon successful socket establishment. Initializes IO streams and starts reader loop.
     */
    private fun onSocketConnected(socket: BluetoothSocket) {
        try {
            activeSocket = socket
            inputStream = socket.inputStream
            outputStream = socket.outputStream
            framingBuffer.reset()

            _connectionState.value = ConnectionState.CONNECTED
            startReaderLoop()
        } catch (e: Exception) {
            disconnect()
            _connectionState.value = ConnectionState.ERROR
        }
    }

    /**
     * Asynchronously reads bytes from the input stream, passing them through [StreamFramingBuffer].
     */
    private fun startReaderLoop() {
        readJob = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(2048)
            val stream = inputStream ?: return@launch

            try {
                while (_connectionState.value == ConnectionState.CONNECTED) {
                    val bytesRead = stream.read(buffer) // Blocking read on Dispatchers.IO
                    if (bytesRead == -1) {
                        break
                    }

                    val frames = framingBuffer.appendAndExtractFrames(buffer, bytesRead)
                    for (frame in frames) {
                        _incomingFrames.emit(frame)
                    }
                }
            } catch (e: Exception) {
                // Connection closed or lost
            } finally {
                onConnectionLost()
            }
        }
    }

    /**
     * Sends raw bytes to the connected socket via [SendQueue].
     */
    suspend fun sendRawBytes(bytes: ByteArray): Boolean {
        if (_connectionState.value != ConnectionState.CONNECTED) return false
        return sendQueue.enqueueAndWrite(outputStream, bytes)
    }

    /**
     * Handles unexpected connection loss.
     */
    private fun onConnectionLost() {
        if (_connectionState.value == ConnectionState.CONNECTED) {
            disconnect()
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    /**
     * Cleanly closes sockets, streams, and cancels running background jobs.
     */
    fun disconnect() {
        try {
            server?.stopListening()
        } catch (e: Exception) {
            // Ignore
        }
        server = null
        client = null

        listenerJob?.cancel()
        readJob?.cancel()
        listenerJob = null
        readJob = null

        try {
            inputStream?.close()
        } catch (e: Exception) {
            // Ignore
        }
        try {
            outputStream?.close()
        } catch (e: Exception) {
            // Ignore
        }
        try {
            activeSocket?.close()
        } catch (e: Exception) {
            // Ignore
        }

        inputStream = null
        outputStream = null
        activeSocket = null

        framingBuffer.reset()
        if (_connectionState.value != ConnectionState.UNAVAILABLE) {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
}
