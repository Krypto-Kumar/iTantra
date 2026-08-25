package com.iTantra.app.transport.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

/**
 * Manages RFCOMM client connections to a specified paired Bluetooth device.
 */
class BluetoothClient(
    private val serviceUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
) {

    /**
     * Attempts to connect to the target [device] asynchronously on [Dispatchers.IO].
     * Returns connected [BluetoothSocket] or throws IOException / returns null on failure.
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(device: BluetoothDevice): BluetoothSocket? = withContext(Dispatchers.IO) {
        val adapter = BluetoothAdapter.getDefaultAdapter()

        // Cancel discovery to avoid slowing down connection attempt
        try {
            adapter?.cancelDiscovery()
        } catch (e: Exception) {
            // Ignore
        }

        var socket: BluetoothSocket? = null
        try {
            socket = device.createRfcommSocketToServiceRecord(serviceUuid)
            socket.connect() // Blocking RFCOMM connection on Dispatchers.IO
            return@withContext socket
        } catch (e: IOException) {
            try {
                socket?.close()
            } catch (closeException: IOException) {
                // Ignore
            }
            return@withContext null
        }
    }
}
