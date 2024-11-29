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
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.Objects;

/**
 * Contains a {@link InetSocketAddress} or a {@link RakNetGUID}, but not both.
 */
public final class AddressOrGUID implements Comparable<AddressOrGUID> {

    public static final @NotNull Comparator<AddressOrGUID>
            COMPARATOR = AddressOrGUID::compareTo;

    private final @Nullable RakNetGUID guid;
    private final @Nullable InetSocketAddress address;

    /**
     * Constructs a new {@code AddressOrGUID}.
     *
     * @param input the address to contain.
     * @throws NullPointerException if {@code input} is {@code null}.
     */
    public AddressOrGUID(@NotNull InetSocketAddress input) {
        Objects.requireNonNull(input, "input cannot be null");
        this.guid = null;
        this.address = input;
    }

    /**
     * Constructs a new {@code AddressOrGUID}.
     *
     * @param input the GUID to contain.
     * @throws NullPointerException if {@code input} is {@code null}.
     */
    public AddressOrGUID(@NotNull RakNetGUID input) {
        Objects.requireNonNull(input, "input cannot be null");
        this.guid = input;
        this.address = null;
    }

    /* TODO: constructor for Packet */

    /**
     * Constructs a new {@code AddressOrGUID} from another.
     *
     * @param input the instance to copy.
     * @throws NullPointerException if {@code input} is {@code null}.
     */
    public AddressOrGUID(@NotNull AddressOrGUID input) {
        Objects.requireNonNull(input, "input cannot be null");
        this.guid = input.guid;
        this.address = input.address;
    }

    /**
     * Returns a string representation of the address or GUID, whichever
     * one this object contains.
     *
     * @param portSeparator the port separator to use (assuming this object
     *                      contains an address). Use {@code '\0'} to discard
     *                      the port from the string.
     * @return a string representation of the address or GUID.
     */
    public @NotNull String toString(char portSeparator) {
        if (guid != null) {
            return guid.toString();
        } else if (address != null) {
            return SystemAddress.toString(address, portSeparator);
        }
        throw new RuntimeException("unreachable");
    }

    @Override
    public int compareTo(@NotNull AddressOrGUID other) {
        RakNetGUID ourGuid = RakNetGUID.notNullOrZero(this.guid);
        RakNetGUID theirGuid = RakNetGUID.notNullOrZero(other.guid);

        int guidCompare = ourGuid.compareTo(theirGuid);
        if (guidCompare != 0) {
            return guidCompare;
        }

        InetSocketAddress ourAddress = SystemAddress.notNullOrZero(this.address);
        InetSocketAddress theirAddress = SystemAddress.notNullOrZero(other.address);
        return SystemAddress.compare(ourAddress, theirAddress);
    }

    @Override
    public int hashCode() {
        if (guid != null) {
            return guid.hashCode();
        } else if (address != null) {
            return address.hashCode();
        }
        throw new RuntimeException("unreachable");
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof AddressOrGUID) {
            AddressOrGUID aog = (AddressOrGUID) obj;
            return (guid != null && guid == aog.guid)
                    || (address != null && address == aog.address);
        }
        return false;
    }

    @Override
    public @NotNull String toString() {
        return this.toString('|');
    }

}
