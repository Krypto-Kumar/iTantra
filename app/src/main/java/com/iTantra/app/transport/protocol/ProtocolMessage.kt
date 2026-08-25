package com.iTantra.app.transport.protocol

/**
 * Protocol control message types sent over RFCOMM wire stream.
 */
enum class ProtocolMessageType {
    /** Carries an application-level [Message] (speech/text payload) */
    DATA,

    /** Delivery acknowledgement for a received message ID */
    ACK,

    /**
     * Room signalling frame carrying room control messages
     * (JOIN_ROOM, JOIN_ACCEPTED, ROOM_FULL, LEAVE_ROOM, ROOM_KEY_SHARE, etc.)
     * These frames are routed separately from speech DATA payloads.
     */
    ROOM_SIGNAL
}

/**
 * Internal protocol envelope transmitted across the RFCOMM byte stream.
 * Distinguishes user payload messages from transport-level control acknowledgements
 * and room signalling frames.
 *
 * @property envelopeId Unique envelope sequence ID
 * @property type Control type: [ProtocolMessageType.DATA], [ProtocolMessageType.ACK],
 *                or [ProtocolMessageType.ROOM_SIGNAL]
 * @property ackMessageId Target message ID being acknowledged when type == ACK
 * @property message Application message payload when type == DATA
 * @property roomFrame Raw JSON room signal payload when type == ROOM_SIGNAL
 */
data class ProtocolEnvelope(
    val envelopeId: String,
    val type: ProtocolMessageType,
    val ackMessageId: String? = null,
    val message: Message? = null,
    val roomFrame: String? = null
)
