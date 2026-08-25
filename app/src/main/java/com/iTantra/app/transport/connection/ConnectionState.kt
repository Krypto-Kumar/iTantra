package com.iTantra.app.transport.connection

/**
 * Represents the lifecycle state of the Bluetooth Classic RFCOMM connection.
 */
enum class ConnectionState {
    /** Bluetooth is unsupported or disabled on the local device */
    UNAVAILABLE,

    /** Server is listening for an incoming connection request */
    WAITING,

    /** Client is actively attempting to establish an RFCOMM socket connection */
    CONNECTING,

    /** RFCOMM connection is established and streams are open for bidirectional transmission */
    CONNECTED,

    /** Connection was cleanly closed or lost */
    DISCONNECTED,

    /** An error occurred during connection initialization or runtime execution */
    ERROR
}
