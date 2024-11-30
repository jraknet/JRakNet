/*
 *    __     ______     ______     __  __     __   __     ______     ______
 *   /\ \   /\  == \   /\  __ \   /\ \/ /    /\ "-.\ \   /\  ___\   /\__  _\
 *  _\_\ \  \ \  __<   \ \  __ \  \ \  _"-.  \ \ \-.  \  \ \  __\   \/_/\ \/
 * /\_____\  \ \_\ \_\  \ \_\ \_\  \ \_\ \_\  \ \_\\"\_\  \ \_____\    \ \_\
 * \/_____/   \/_/ /_/   \/_/\/_/   \/_/\/_/   \/_/ \/_/   \/_____/     \/_/
 *
 * the MIT License (MIT)
 *
 * Copyright (c) 2016-2025 "Whirvis" Trent Summerlin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * the above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.whirvis.jraknet;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Objects;

/**
 * The main RakNet component class.
 * <p>
 * This contains protocol information and utility methods.
 */
public final class RakNet {

    /**
     * Returns how many bytes are needed to represent a given number of bits.
     */
    public static int bitsToBytes(int x) {
        return (x + 7) >> 3;
    }

    /**
     * Returns how many bits are needed to represent a given number of bytes.
     */
    public static int bytesToBits(int x) {
        return x << 3;
    }

    /**
     * The minimum value for the maximum transfer unit (MTU) allowed
     * by RakNet.
     * <p>
     * The MTU size determines how many bytes can be sent in a single
     * packet before it is split up when sent over the wire.
     *
     * @see #getMTU(InetAddress)
     */
    public static final int MINIMUM_MTU_SIZE = 400;

    /**
     * The maximum value for the maximum transfer unit (MTU) allowed
     * by RakNet.
     * <p>
     * The MTU size determines how many bytes can be sent in a single
     * packet before it is split up when sent over the wire.
     *
     * @see #getMTU(InetAddress)
     */
    public static final int MAXIMUM_MTU_SIZE = 1492;

    /**
     * Returns the MTU for the network card associated with a given address.
     * <p>
     * The minimum value that can be returned is {@value #MINIMUM_MTU_SIZE}.
     * If the network card has an MTU lower than that, an exception will be
     * thrown.
     * The largest value that can be returned is {@value #MAXIMUM_MTU_SIZE};
     * even if the network card has a higher MTU. This is to remain aligned
     * with the specifications of RakNet.
     *
     * @param address the address bound to the network card.
     * @return the MTU for the network card.
     * @throws NullPointerException  if {@code address} is {@code null}.
     * @throws RakNetSocketException if no network card is bound to the given
     *                               address; if the MTU for the network card
     *                               is less than {@value #MINIMUM_MTU_SIZE}.
     */
    public static int getMTU(@NotNull InetAddress address) {
        Objects.requireNonNull(address, "address cannot be null");

        NetworkInterface nit;
        try {
            nit = NetworkInterface.getByInetAddress(address);
        } catch (SocketException e) {
            throw new RakNetSocketException(e);
        }

        if (nit == null) {
            String msg = "No network card for " + address;
            throw new RakNetSocketException(msg);
        }

        int mtu;
        try {
            mtu = nit.getMTU();
        } catch (SocketException e) {
            throw new RakNetSocketException(e);
        }

        /* probably won't happen, check just to be safe */
        if (mtu < MINIMUM_MTU_SIZE) {
            String msg = "MTU for " + address + " too low (" + mtu + ")";
            throw new RakNetSocketException(msg);
        }

        return Math.min(mtu, MAXIMUM_MTU_SIZE);
    }

    /**
     * Returns the MTU for the network card associated with a given address.
     * <p>
     * The minimum value that can be returned is {@value #MINIMUM_MTU_SIZE}.
     * If the network card has an MTU lower than that, an exception will be
     * thrown.
     * The largest value that can be returned is {@value #MAXIMUM_MTU_SIZE};
     * even if the network card has a higher MTU. This is to remain aligned
     * with the specifications of RakNet.
     *
     * @param address the address bound to the network card.
     * @return the MTU for the network card.
     * @throws NullPointerException  if {@code address} is {@code null}.
     * @throws RakNetSocketException if no network card is bound to the given
     *                               address; if the MTU for the network card
     *                               is less than {@value #MINIMUM_MTU_SIZE}.
     */
    public static int getMTU(@NotNull InetSocketAddress address) {
        Objects.requireNonNull(address, "address cannot be null");
        return getMTU(address.getAddress());
    }

    private RakNet() {
        throw new UnsupportedOperationException();
    }

}
