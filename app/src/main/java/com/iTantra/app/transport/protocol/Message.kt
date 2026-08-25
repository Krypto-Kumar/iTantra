package com.iTantra.app.transport.protocol

/**
 * Logical message data class transferred between iTantra clients.
 * Matches exact structure: id, type, language, timestamp, text.
 *
 * @property id Unique message identifier (e.g. "msg-001")
 * @property type Message category, defaults to "NORMAL"
 * @property language ISO language code (e.g., "hi", "mr", "en", "ta")
 * @property timestamp Unix timestamp in milliseconds
 * @property text The actual speech-to-text / user payload content
 */
data class Message(
    val id: String,
    val type: String = "NORMAL",
    val language: String = "hi",
    val timestamp: Long = System.currentTimeMillis(),
    val text: String
)
