package com.iTantra.app.transport.protocol

import org.json.JSONObject
import java.nio.charset.StandardCharsets

/**
 * MessageSerializer handles framing-aware JSON serialization and deserialization
 * using strict UTF-8 encoding end-to-end to preserve Indic character sets.
 */
object MessageSerializer {

    const val DELIMITER = "\n"

    /**
     * Serializes a [ProtocolEnvelope] to a newline-terminated JSON string.
     */
    fun serializeEnvelope(envelope: ProtocolEnvelope): String {
        val json = JSONObject()
        json.put("envelopeId", envelope.envelopeId)
        json.put("type", envelope.type.name)

        if (envelope.ackMessageId != null) {
            json.put("ackMessageId", envelope.ackMessageId)
        }

        if (envelope.message != null) {
            val msgJson = serializeMessageToJson(envelope.message)
            json.put("message", msgJson)
        }

        return json.toString() + DELIMITER
    }

    /**
     * Converts a [ProtocolEnvelope] directly to UTF-8 byte array with trailing newline delimiter.
     */
    fun serializeEnvelopeToBytes(envelope: ProtocolEnvelope): ByteArray {
        return serializeEnvelope(envelope).toByteArray(StandardCharsets.UTF_8)
    }

    /**
     * Serializes a logical [Message] into a [JSONObject].
     */
    fun serializeMessageToJson(message: Message): JSONObject {
        val json = JSONObject()
        json.put("id", message.id)
        json.put("type", message.type)
        json.put("language", message.language)
        json.put("timestamp", message.timestamp)
        json.put("text", message.text)
        return json
    }

    /**
     * Serializes a logical [Message] into a standalone newline-terminated JSON string.
     */
    fun serializeMessage(message: Message): String {
        return serializeMessageToJson(message).toString() + DELIMITER
    }

    /**
     * Deserializes a raw JSON string line into a [ProtocolEnvelope].
     * Returns null if parsing fails or payload is malformed.
     */
    fun deserializeEnvelope(jsonString: String): ProtocolEnvelope? {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return null

        return try {
            val json = JSONObject(trimmed)

            // Check if it's a wrapped ProtocolEnvelope
            if (json.has("envelopeId") && json.has("type")) {
                val envelopeId = json.getString("envelopeId")
                val typeStr = json.getString("type")
                val type = ProtocolMessageType.valueOf(typeStr)

                val ackMessageId = if (json.has("ackMessageId") && !json.isNull("ackMessageId")) {
                    json.getString("ackMessageId")
                } else null

                val message = if (json.has("message") && !json.isNull("message")) {
                    deserializeMessageFromJsonObject(json.getJSONObject("message"))
                } else null

                ProtocolEnvelope(
                    envelopeId = envelopeId,
                    type = type,
                    ackMessageId = ackMessageId,
                    message = message
                )
            } else if (json.has("id") && json.has("text")) {
                // Direct Message fallback - wrap in DATA envelope automatically
                val msg = deserializeMessageFromJsonObject(json)
                msg?.let {
                    ProtocolEnvelope(
                        envelopeId = "env-${it.id}",
                        type = ProtocolMessageType.DATA,
                        message = it
                    )
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Deserializes a [JSONObject] into a logical [Message].
     */
    private fun deserializeMessageFromJsonObject(json: JSONObject): Message? {
        return try {
            val id = json.getString("id")
            val text = json.getString("text")
            val type = if (json.has("type")) json.getString("type") else "NORMAL"
            val language = if (json.has("language")) json.getString("language") else "hi"
            val timestamp = if (json.has("timestamp")) json.getLong("timestamp") else System.currentTimeMillis()

            Message(
                id = id,
                type = type,
                language = language,
                timestamp = timestamp,
                text = text
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Deserializes a raw JSON string into a [Message] directly.
     */
    fun deserializeMessage(jsonString: String): Message? {
        val envelope = deserializeEnvelope(jsonString)
        return envelope?.message
    }
}
