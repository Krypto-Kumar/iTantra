package com.iTantra.app.transport.protocol

import java.nio.charset.StandardCharsets

/**
 * StreamFramingBuffer accumulates incoming byte chunks from an RFCOMM byte stream,
 * decodes UTF-8 text safely, and extracts complete newline-delimited JSON message lines (\n).
 *
 * Handles:
 * - Partial reads
 * - Multiple concatenated messages in a single read buffer
 * - Split messages spanning multiple read calls
 * - UTF-8 Indic character preservation across chunk boundaries
 */
class StreamFramingBuffer {

    private val stringBuffer = StringBuilder()

    /**
     * Appends a chunk of raw bytes to the framing buffer.
     * Decodes bytes as UTF-8 string.
     *
     * @param bytes ByteArray containing incoming data
     * @param length Number of valid bytes in the buffer array
     * @return List of fully framed, complete JSON string lines extracted from the stream
     */
    @Synchronized
    fun appendAndExtractFrames(bytes: ByteArray, length: Int): List<String> {
        if (length <= 0) return emptyList()

        val decodedChunk = String(bytes, 0, length, StandardCharsets.UTF_8)
        stringBuffer.append(decodedChunk)

        val completedFrames = mutableListOf<String>()

        while (true) {
            val newlineIndex = stringBuffer.indexOf(MessageSerializer.DELIMITER)
            if (newlineIndex == -1) {
                break
            }

            val frame = stringBuffer.substring(0, newlineIndex).trim()
            stringBuffer.delete(0, newlineIndex + MessageSerializer.DELIMITER.length)

            if (frame.isNotEmpty()) {
                completedFrames.add(frame)
            }
        }

        return completedFrames
    }

    /**
     * Clears all accumulated data in the buffer.
     */
    @Synchronized
    fun reset() {
        stringBuffer.clear()
    }
}
