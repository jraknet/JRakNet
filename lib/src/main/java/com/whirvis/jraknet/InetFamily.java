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
import java.util.Objects;

/**
 * Represents a family of the internet protocol.
 */
public enum InetFamily {

    INET4(4, 4),
    INET6(6, 16);

    private final int version;
    private final int addressSize;

    InetFamily(int version, int addressSize) {
        this.version = version;
        this.addressSize = addressSize;
    }

    /**
     * Returns the version as an integer.
     *
     * @return the version as an integer.
     */
    public int getVersion() {
        return this.version;
    }

    /**
     * Returns an {@code InetFamily} by its version number.
     *
     * @param version The version number.
     * @return The {@code InetFamily} of the given version number.
     * @throws IllegalArgumentException If no such {@code InetFamily}
     *                                  exists for the given version.
     */
    public static @NotNull InetFamily fromVersion(int version) {
        InetFamily located = null;

        for (InetFamily family : values()) {
            if (family.version == version) {
                located = family;
                break;
            }
        }

        if (located == null) {
            String msg = "no such family (version: " + version + ")";
            throw new IllegalArgumentException(msg);
        }

        return located;
    }

    /**
     * Returns an {@code InetFamily} by its address size.
     *
     * @param addressSize The address size.
     * @return The {@code InetFamily} with the given address size.
     * @throws IllegalArgumentException If no such {@code InetFamily} with
     *                                  the given address size exists.
     */
    public static @NotNull InetFamily fromSize(int addressSize) {
        InetFamily located = null;

        for (InetFamily family : values()) {
            if (family.addressSize == addressSize) {
                located = family;
                break;
            }
        }

        if (located == null) {
            String msg = "no such family (addressSize: " + addressSize + ")";
            throw new IllegalArgumentException(msg);
        }

        return located;
    }

    /**
     * Returns the {@code InetFamily} for a given address.
     *
     * @param address The address.
     * @return The {@code InetFamily} of the given address.
     * @throws NullPointerException if {@code address} is {@code null}.
     */
    public static @NotNull InetFamily
    ofAddress(@NotNull InetAddress address) {
        Objects.requireNonNull(address, "address cannot be null");
        try {
            return fromSize(address.getAddress().length);
        } catch (IllegalArgumentException e) {
            throw new RakNetException("this is a bug", e);
        }
    }

    /**
     * Returns the {@code InetFamily} for a given address.
     *
     * @param address The address.
     * @return The {@code InetFamily} of the given address.
     * @throws NullPointerException if {@code address} is {@code null}.
     */
    public static @NotNull InetFamily
    ofAddress(@NotNull InetSocketAddress address) {
        Objects.requireNonNull(address, "address cannot be null");
        try {
            return fromSize(address.getAddress().getAddress().length);
        } catch (IllegalArgumentException e) {
            throw new RakNetException("this is a bug", e);
        }
    }

}
