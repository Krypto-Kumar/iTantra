package com.iTantra.app.transport.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Helper utility for handling Android Bluetooth hardware checks, API permissions,
 * and paired device retrieval.
 */
object BluetoothPermissionHelper {

    /**
     * Checks if the device possesses Bluetooth hardware.
     */
    fun isBluetoothSupported(): Boolean {
        return BluetoothAdapter.getDefaultAdapter() != null
    }

    /**
     * Checks if Bluetooth is currently enabled on the device.
     */
    fun isBluetoothEnabled(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        return adapter != null && adapter.isEnabled
    }

    /**
     * Returns the array of required runtime permissions for the current Android API version.
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Checks if all required Bluetooth permissions are granted by the user.
     */
    fun hasRequiredPermissions(context: Context): Boolean {
        for (permission in getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * Returns the set of currently paired Bluetooth devices.
     * Note: Callers must ensure BLUETOOTH_CONNECT permission is granted before invocation on API 31+.
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): Set<BluetoothDevice> {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptySet()
        return try {
            adapter.bondedDevices ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}
