package com.iTantra.app.transport.protocol

/**
 * Protocol control message types sent over RFCOMM wire stream.
 */
enum class ProtocolMessageType {
    /** Carries an application-level [Message] */
    DATA,
    /** Delivery acknowledgement for a received message ID */
    ACK
}

/**
 * Internal protocol envelope transmitted across the RFCOMM byte stream.
 * Distinguishes user payload messages from transport-level control acknowledgements.
 *
 * @property envelopeId Unique envelope sequence ID
 * @property type Control type: [ProtocolMessageType.DATA] or [ProtocolMessageType.ACK]
 * @property ackMessageId Target message ID being acknowledged when type == ACK
 * @property message Application message payload when type == DATA
 */
data class ProtocolEnvelope(
    val envelopeId: String,
    val type: ProtocolMessageType,
    val ackMessageId: String? = null,
    val message: Message? = null
)
