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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Methods for quick hashing. The original algorithms for this
 * were implemented by Paul Hsieh.
 * <p>
 * These were ported from
 * <a href="https://github.com/facebookarchive/RakNet/blob/master/Source/SuperFastHash.cpp">
 * <code>RakNetTypes.cpp</code></a> to ensure the consistency of
 * hashing functions with the original implementation of RakNet.
 *
 * @see <a href="http://www.azillionmonkeys.com/qed/hash.html">
 * "Hash functions."</a>
 */
public final class SuperFastHash {

    private static final int INCREMENTAL_READ_BLOCK_SIZE = 65536;

    private static int get16bits(byte[] data, int index) {
        return ((data[index + 1] & 0xFF) << 8) | (data[index] & 0xFF);
    }

    private static void readExact(FileInputStream in, byte[] data, int len) throws IOException {
        int bytesRead = in.read(data, 0, len);
        if (bytesRead != len) {
            String msg = "Only got " + bytesRead + " bytes (expected " + len + ")";
            throw new IOException(msg);
        }
    }

    /**
     * <b>Original function:</b> <a href="https://github.com/facebookarchive/RakNet/blob/master/Source/SuperFastHash.cpp#L31">
     * <code>SuperFastHash</code></a>
     * <p>
     * Returns a hash based on the given data.
     *
     * @param data   The data whose hash to compute.
     * @param offset The offset from which to start reading <code>data</code>.
     * @param length The number of bytes to read from <code>data</code>.
     * @return A hash code for the contents of <code>data</code>.
     * @throws IllegalArgumentException If <code>offset</code> is negative.
     */
    public static int forBytes(byte @Nullable [] data, int offset, int length) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        } else if (data == null || length <= 0) {
            return 0; /* following original implementation */
        }

        int bytesRemaining = length;
        int lastHash = length;
        int index = offset;

        while (bytesRemaining >= INCREMENTAL_READ_BLOCK_SIZE) {
            lastHash = incremental(data, index, INCREMENTAL_READ_BLOCK_SIZE, lastHash);
            bytesRemaining -= INCREMENTAL_READ_BLOCK_SIZE;
            index += INCREMENTAL_READ_BLOCK_SIZE;
        }

        if (bytesRemaining > 0) {
            lastHash = incremental(data, index, bytesRemaining, lastHash);
        }

        return lastHash;
    }

    /**
     * <b>Shorthand for</b>: {@link #forBytes(byte[], int, int)}
     * <p>
     * Returns a hash based on the given data.
     *
     * @param data The data whose hash to compute.
     * @return A hash code for the contents of <code>data</code>.
     */
    public static int forBytes(byte @Nullable [] data) {
        if (data == null) {
            return forBytes(null, 0, 0);
        }
        return forBytes(data, 0, data.length);
    }

    /**
     * <b>Original function:</b> <a href="https://github.com/facebookarchive/RakNet/blob/master/Source/SuperFastHash.cpp#L51">
     * <code>SuperFastHashIncremental</code></a>
     * <p>
     * Returns a hash based on the given data and the last hash.
     *
     * @param data     The data whose hash to compute.
     * @param offset   The offset from which to start reading <code>data</code>.
     * @param length   The number of bytes to read from <code>data</code>.
     * @param lastHash The hash code returned by the last invocation of this method.
     * @return A hash code for the contents of <code>data</code> in conjunction
     * with <code>lastHash</code>.
     * @throws IllegalArgumentException If <code>offset</code> is negative.
     */
    public static int incremental(byte @Nullable [] data, int offset, int length, int lastHash) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        } else if (data == null || length <= 0) {
            return 0; /* following original implementation */
        }

        int hash = lastHash;
        int tmp;
        int rem;

        int len = length;
        rem = len & 3;
        len >>= 2;

        /* Main loop */
        int index = offset;
        while (len > 0) {
            hash += get16bits(data, index);
            tmp = (get16bits(data, index + 2) << 11) ^ hash;
            hash = (hash << 16) ^ tmp;
            index += 4;
            hash += hash >>> 11;

            len--;
        }

        /* Handle end cases */
        switch (rem) {
            case 3:
                hash += get16bits(data, index);
                hash ^= hash << 16;
                hash ^= data[index + 2] << 18;
                hash += hash >>> 11;
                break;
            case 2:
                hash += get16bits(data, index);
                hash ^= hash << 11;
                hash += hash >>> 17;
                break;
            case 1:
                hash += data[index];
                hash ^= hash << 10;
                hash += hash >>> 1;
        }

        /* Force "avalanching" of final 127 bits */
        hash ^= hash << 3;
        hash += hash >>> 5;
        hash ^= hash << 4;
        hash += hash >>> 17;
        hash ^= hash << 25;
        hash += hash >>> 6;

        return hash;
    }

    /**
     * <b>Shorthand for:</b> {@link #incremental(byte[], int, int, int)}
     * <p>
     * Returns a hash based on the given data and the last hash.
     *
     * @param data     The data whose hash to compute.
     * @param lastHash The hash code returned by the last invocation of this method.
     * @return A hash code for the contents of <code>data</code> in conjunction
     * with <code>lastHash</code>.
     * @throws IllegalArgumentException If <code>offset</code> is negative.
     */
    public static int incremental(byte @Nullable [] data, int lastHash) {
        if (data == null) {
            return incremental(null, 0, 0, lastHash);
        }
        return incremental(data, 0, data.length, lastHash);
    }

    /**
     * <b>Original function:</b> <a href="https://github.com/facebookarchive/RakNet/blob/master/Source/SuperFastHash.cpp#L99">
     * <code>SuperFastHashFile</code></a>
     * <p>
     * Calculates a hash from the contents of a file at the given path.
     * <p>
     * <b>Note:</b> The original implementation has a bug which causes
     * inaccurate results for any file whose size (in bytes) cannot fit
     * in a positive, signed, 32-bit integer. This method retains that
     * bug for consistency.
     *
     * @param path The path of the file whose contents to hash.
     * @return A hash code for the contents of <code>file</code>.
     * @throws NullPointerException If <code>path</code> is <code>null</code>.
     * @throws IOException          If the file does not exist, is a
     *                              directory rather than a regular file,
     *                              or for some other reason cannot be
     *                              opened for reading.
     * @see #forFile(String, boolean)
     */
    public static int forFile(@NotNull String path) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        File file = new File(path);
        return forFile(file);
    }

    /**
     * <b>Original function:</b> <a href="https://github.com/facebookarchive/RakNet/blob/master/Source/SuperFastHash.cpp#L99">
     * <code>SuperFastHashFile</code></a>
     * <p>
     * Calculates a hash from the contents of a file at the given path.
     * <p>
     * When <code>followOriginal</code> is <code>false</code>, this method
     * will accurately hash the contents of a file regardless of its size.
     * See {@link #forFile(File)} for a proper explanation.
     *
     * @param path           The path of the file whose contents to hash.
     * @param followOriginal <code>true</code> to use the original algorithm
     *                       for this method; <code>false</code> otherwise.
     * @return A hash code for the contents of <code>file</code>.
     * @throws NullPointerException If <code>path</code> is <code>null</code>.
     * @throws IOException          If the file does not exist, is a
     *                              directory rather than a regular file,
     *                              or for some other reason cannot be
     *                              opened for reading.
     */
    public static int forFile(@NotNull String path, boolean followOriginal) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        File file = new File(path);
        return forFile(file, followOriginal);
    }

    /**
     * <b>Original function:</b> <a href="https://github.com/facebookarchive/RakNet/blob/master/Source/SuperFastHash.cpp#L109">
     * <code>SuperFastHashFilePtr</code></a>
     * <p>
     * Calculates a hash from the contents of a given file.
     * <p>
     * <b>Note:</b> The original implementation has a bug which causes
     * inaccurate results for any file whose size (in bytes) cannot fit
     * in a positive, signed, 32-bit integer. This method retains that
     * bug for consistency.
     *
     * @param file The file whose contents to hash.
     * @return A hash code for the contents of <code>file</code>.
     * @throws NullPointerException If <code>file</code> is <code>null</code>.
     * @throws IOException          If the file does not exist, is a
     *                              directory rather than a regular file,
     *                              or for some other reason cannot be
     *                              opened for reading.
     * @see #forFile(File, boolean)
     */
    public static int forFile(@NotNull File file) throws IOException {
        Objects.requireNonNull(file, "file cannot be null");

        int length = (int) file.length();

        int bytesRemaining = length;
        int lastHash = length;
        byte[] readBlock = new byte[INCREMENTAL_READ_BLOCK_SIZE];

        try (FileInputStream in = new FileInputStream(file)) {
            while (bytesRemaining >= readBlock.length) {
                readExact(in, readBlock, readBlock.length);
                lastHash = incremental(readBlock, lastHash);
                bytesRemaining -= readBlock.length;
            }

            if (bytesRemaining > 0) {
                readExact(in, readBlock, bytesRemaining);
                lastHash = incremental(readBlock, 0, bytesRemaining, lastHash);
            }
        }

        return lastHash;
    }

    /**
     * <b>Original function:</b> <a href="https://github.com/facebookarchive/RakNet/blob/master/Source/SuperFastHash.cpp#L109">
     * <code>SuperFastHashFilePtr</code></a>
     * <p>
     * Calculates a hash from the contents of a given file.
     * <p>
     * When <code>followOriginal</code> is <code>false</code>, this method
     * will accurately hash the contents of a file regardless of its size.
     * See the implementation notes of {@link #forFile(File)} for a proper
     * explanation.
     *
     * @param file           The file whose contents to hash.
     * @param followOriginal <code>true</code> to use the original algorithm
     *                       for this method; <code>false</code> otherwise.
     * @return A hash code for the contents of <code>file</code>.
     * @throws NullPointerException If <code>file</code> is <code>null</code>.
     * @throws IOException          If the file does not exist, is a
     *                              directory rather than a regular file,
     *                              or for some other reason cannot be
     *                              opened for reading.
     */
    public static int forFile(@NotNull File file, boolean followOriginal) throws IOException {
        Objects.requireNonNull(file, "file cannot be null");

        if (followOriginal) {
            return forFile(file);
        }

        long length = file.length();

        long bytesRemaining = length;
        int lastHash = (int) length;
        byte[] readBlock = new byte[INCREMENTAL_READ_BLOCK_SIZE];

        try (FileInputStream in = new FileInputStream(file)) {
            while (bytesRemaining >= readBlock.length) {
                readExact(in, readBlock, readBlock.length);
                lastHash = incremental(readBlock, lastHash);
                bytesRemaining -= readBlock.length;
            }

            /*
             * The maximum value for bytesRemaining is readBlock.length-1,
             * so it's safe to cast this value back to an integer for calls
             * to readExact() and incremental().
             */
            int remaining = (int) bytesRemaining;
            if (remaining > 0) {
                readExact(in, readBlock, remaining);
                lastHash = incremental(readBlock, 0, remaining, lastHash);
            }
        }

        return lastHash;
    }

    private SuperFastHash() {
        throw new UnsupportedOperationException();
    }

}
