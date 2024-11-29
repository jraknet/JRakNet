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

import com.whirvis.jraknet.dummy.AddressOrGUID;
import org.jetbrains.annotations.NotNull;

/**
 * Determines how packets are delivered.
 *
 * @see PacketPriority
 */
public enum PacketReliability {

    /**
     * Same as regular UDP, except that duplicates will be discarded.
     * <p>
     * <b>Overhead:</b> RakNet adds {@code (6 to 17) + 21} bits of
     * overhead. Sixteen of these are used to detect duplicate packets,
     * while six to seventeen are used for the message length.
     */
    UNRELIABLE(0, false, false, false, false),

    /**
     * Same as {@link #UNRELIABLE} but with a sequence counter.
     * <p>
     * Out of order messages will be discarded. Sequenced and ordered
     * messages sent on the same channel will arrive in the order they
     * were sent.
     */
    UNRELIABLE_SEQUENCED(1, false, false, true, false),

    /**
     * The is guaranteed to arrive, but not necessarily in any order.
     * <p>
     * <b>Overhead:</b> Same as {@link #UNRELIABLE}.
     */
    RELIABLE(2, true, false, false, false),

    /**
     * This message is guaranteed to arrive and be in order.
     * <p>
     * Messages will be delayed while waiting for out of order messages.
     * Sequenced and ordered messages sent on the same channel will arrive
     * in the order they were sent.
     * <p>
     * <b>Overhead:</b> Same as {@link #UNRELIABLE_SEQUENCED}.
     */
    RELIABLE_ORDERED(3, true, true, false, false),

    /**
     * This message is guaranteed to arrive and be in sequence.
     * <p>
     * Out or order messages will be dropped. Sequenced and ordered messages
     * sent on the same channel will arrive in the order they were sent.
     * <p>
     * <b>Overhead:</b> Same as {@link #UNRELIABLE_SEQUENCED}.
     */
    RELIABLE_SEQUENCED(4, true, false, true, false),

    /**
     * TODO: document
     */
    UNRELIABLE_WITH_ACK_RECEIPT(5, false, false, false, true),

    /**
     * TODO: document
     */
    RELIABLE_WITH_ACK_RECEIPT(6, true, false, false, true),

    /**
     * TODO: document
     */
    RELIABLE_ORDERED_WITH_ACK_RECEIPT(7, true, true, false, true);

    private final byte id;

    private final boolean reliable;
    private final boolean ordered;
    private final boolean sequenced;
    private final boolean requiresAck;

    PacketReliability(int id, boolean reliable, boolean ordered, boolean sequenced, boolean requiresAck) {
        if (id < 0b000 || id > 0b111) {
            String msg = "id must fit inside an unsigned 3-bit value";
            throw new IllegalArgumentException(msg);
        } else if (ordered && sequenced) {
            String msg = "reliability cannot be ordered and sequenced";
            throw new IllegalArgumentException(msg);
        }

        this.id = (byte) id;

        this.reliable = reliable;
        this.ordered = ordered;
        this.sequenced = sequenced;
        this.requiresAck = requiresAck;
    }

    public boolean isUnreliable() {
        return !this.reliable;
    }

    public boolean isReliable() {
        return this.reliable;
    }

    public boolean isOrdered() {
        return this.ordered;
    }

    public boolean isSequenced() {
        return this.sequenced;
    }

    public boolean hasAckReceipt() {
        return this.requiresAck;
    }

    public static @NotNull PacketReliability valueOf(int id) {
        PacketReliability located = null;

        for (PacketReliability reliability : values()) {
            if (reliability.id == id) {
                located = reliability;
                break;
            }
        }

        if (located == null) {
            String msg = "no such reliability (id: " + id + ")";
            throw new IllegalArgumentException(msg);
        }

        return located;
    }

}
