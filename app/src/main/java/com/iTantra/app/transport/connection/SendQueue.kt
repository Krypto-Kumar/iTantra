package com.iTantra.app.transport.connection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.OutputStream

/**
 * Thread-safe sequential send queue ensuring concurrent write attempts
 * to the Bluetooth socket [OutputStream] do not interleave or corrupt RFCOMM byte streams.
 */
class SendQueue {
    private val writeMutex = Mutex()

    /**
     * Writes [bytes] sequentially to the target [outputStream] under a mutex lock.
     *
     * @param outputStream Connected socket output stream
     * @param bytes ByteArray to write
     * @return Boolean true if write succeeded, false on IO failure
     */
    suspend fun enqueueAndWrite(outputStream: OutputStream?, bytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        if (outputStream == null) return@withContext false

        writeMutex.withLock {
            return@withContext try {
                outputStream.write(bytes)
                outputStream.flush()
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
