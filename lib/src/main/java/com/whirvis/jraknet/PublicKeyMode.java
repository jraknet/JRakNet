package com.whirvis.jraknet;

/**
 * Used with the PublicKey structure.
 */
public enum PublicKeyMode {

    /**
     * The connection is insecure. You can also just pass 0 for the
     * pointer to PublicKey in RakPeerInterface::Connect().
     */
    PKM_INSECURE_CONNECTION,

    /**
     * Accept whatever public key the server gives us. This is vulnerable
     * to man in the middle, but does not require distribution of the public
     * key in advance of connecting.
     */
    PKM_ACCEPT_ANY_PUBLIC_KEY,

    /**
     * Use a known remote server public key. PublicKey::remoteServerPublicKey
     * must be non-zero. This is the recommended mode for secure connections.
     */
    PKM_USE_KNOWN_PUBLIC_KEY,

    /**
     * Use a known remote server public key AND provide a public key for
     * the connecting client. PublicKey::remoteServerPublicKey, myPublicKey
     * and myPrivateKey must be all be non-zero. The server must cooperate
     * for this mode to work.
     * <p>
     * I don't recommend this mode except for server-to-server communication
     * as it significantly increases the CPU requirements during connections
     * for both sides. When it is used, a connection password should be used
     * as well to avoid DoS attacks.
     */
    PKM_USE_TWO_WAY_AUTHENTICATION;

}
