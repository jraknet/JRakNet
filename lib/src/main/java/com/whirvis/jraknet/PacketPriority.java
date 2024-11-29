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

/**
 * Determines when packets are delivered.
 *
 * @see PacketReliability
 */
public enum PacketPriority {

    /**
     * The highest possible priority.
     * <p>
     * These messages trigger sends immediately, and are generally not
     * buffered or aggregated into a single datagram.
     */
    IMMEDIATE_PRIORITY(0),

    /**
     * For every two {@code IMMEDIATE_PRIORITY} messages, one
     * {@code HIGH_PRIORITY} message will be sent.
     * <p>
     * Messages at this priority and lower are buffered to be sent in groups
     * at 10 millisecond intervals to reduce UDP overhead and better measure
     * congestion control.
     */
    HIGH_PRIORITY(1),

    /**
     * For every two {@code HIGH_PRIORITY} messages, one
     * {@code MEDIUM_PRIORITY} will be sent.
     * <p>
     * Messages at this priority and lower are buffered to be sent in groups
     * at 10 millisecond intervals to reduce UDP overhead and better measure
     * congestion control.
     */
    MEDIUM_PRIORITY(2),

    /**
     * For every two {@code MEDIUM_PRIORITY} messages, one
     * {@code LOW_PRIORITY} will be sent.
     * <p>
     * Messages at this priority and lower are buffered to be sent in groups
     * at 10 millisecond intervals to reduce UDP overhead and better measure
     * congestion control.
     */
    LOW_PRIORITY(3);

    public final int level;

    PacketPriority(int level) {
        this.level = level;
    }

    public static @NotNull PacketPriority valueOf(int level) {
        PacketPriority located = null;

        for (PacketPriority priority : values()) {
            if (priority.level == level) {
                located = priority;
                break;
            }
        }

        if (located == null) {
            String msg = "no such priority (level: " + level + ")";
            throw new IllegalArgumentException(msg);
        }

        return located;
    }

}
