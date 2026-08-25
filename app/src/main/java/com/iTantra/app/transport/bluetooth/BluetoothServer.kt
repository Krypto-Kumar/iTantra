package com.iTantra.app.transport.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

/**
 * Manages RFCOMM server socket listening for incoming Bluetooth connections.
 */
class BluetoothServer(
    private val serviceName: String = "iTantraRFCOMM",
    private val serviceUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
) {
    private var serverSocket: BluetoothServerSocket? = null
    @Volatile private var isListening = false

    /**
     * Listens for an incoming connection asynchronously on [Dispatchers.IO].
     * Returns the connected [BluetoothSocket] or null if listening failed/cancelled.
     */
    @SuppressLint("MissingPermission")
    suspend fun listenForConnection(): BluetoothSocket? = withContext(Dispatchers.IO) {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return@withContext null
        isListening = true

        try {
            serverSocket = adapter.listenUsingRfcommWithServiceRecord(serviceName, serviceUuid)
            val socket = serverSocket?.accept() // Blocking call on Dispatchers.IO
            closeServerSocket()
            return@withContext socket
        } catch (e: IOException) {
            closeServerSocket()
            return@withContext null
        } finally {
            isListening = false
        }
    }

    /**
     * Cancels listening and closes the server socket safely.
     */
    fun stopListening() {
        isListening = false
        closeServerSocket()
    }

    private fun closeServerSocket() {
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            // Ignore close exceptions
        } finally {
            serverSocket = null
        }
    }
}
