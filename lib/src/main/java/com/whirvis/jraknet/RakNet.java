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

public class RakNet {

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

    public static final int MAX_RPC_MAP_SIZE = 0xFF - 1;
    public static final int UNDEFINED_RPC_INDEX = 0xFF;

    // TODO: /** Index of an unassigned player.*/
    // public static final SystemIndex
    //        UNASSIGNED_PLAYER_INDEX = new SystemIndex(0xFFFF);

    // TODO: /** * Unassigned object ID. */
    // public static final NetworkID
    //        UNASSIGNED_NETWORK_ID = new NetworkID(0xFFFFFFFFFFFFFFFFL);

    public static final int PING_TIMES_ARRAY_SIZE = 5;

    public static boolean nonNumericHostString(String host) {
        for (int i = 0; i < host.length(); i++) {
            char c = host.charAt(i);
            if ((c >= 'g' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                return true;
            }
        }
        return false;
    }

}
