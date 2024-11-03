package com.whirvis.jraknet;

public enum ConnectionState {

    /**
     * TODO: Reference an actual method for "Connect()".
     * <p>
     * Connect() was called, but the process hasn't started yet.
     */
    IS_PENDING,

    /**
     * Processing the connection attempt.
     */
    IS_CONNECTING,

    /**
     * Is connected and able to communicate.
     */
    IS_CONNECTED,

    /**
     * Was connected, but will disconnect as soon as the remaining
     * messages are delivered.
     */
    IS_DISCONNECTING,

    /**
     * A connection attempt failed and will be aborted.
     */
    IS_SILENTLY_DISCONNECTING,

    /**
     * No longer connected.
     */
    IS_DISCONNECTED,

    /**
     * Was never connected, or else was disconnected long enough
     * ago that the entry has been discarded.
     */
    IS_NOT_CONNECTED;

}
