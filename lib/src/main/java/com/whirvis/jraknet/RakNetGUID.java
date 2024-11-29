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

import java.util.Comparator;

public final class RakNetGUID implements Comparable<RakNetGUID> {

    public static final @NotNull Comparator<RakNetGUID>
            COMPARATOR = RakNetGUID::compareTo;

    private static final @NotNull RakNetGUID
            ZERO_GUID = new RakNetGUID(0L);

    static @NotNull RakNetGUID
    notNullOrZero(@Nullable RakNetGUID guid) {
        if (guid == null) {
            return ZERO_GUID;
        }
        return guid;
    }

    private final long guid;

    public RakNetGUID(long guid) {
        String guidStr = Long.toUnsignedString(guid);
        this.guid = guid;
    }

    @Override
    public int compareTo(@NotNull RakNetGUID guid) {
        return Long.compareUnsigned(this.guid, guid.guid);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(guid);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof RakNetGUID) {
            RakNetGUID that = (RakNetGUID) obj;
            return this.guid == that.guid;
        }
        return false;
    }

    @Override
    public String toString() {
        return Long.toString(guid);
    }

}
