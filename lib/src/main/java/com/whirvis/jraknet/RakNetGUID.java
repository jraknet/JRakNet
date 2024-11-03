package com.whirvis.jraknet;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class RakNetGUID implements Comparable<RakNetGUID> {

    private static final @NotNull BigInteger UNSIGNED_INT_MAX_VALUE = new BigInteger("FFFFFFFF", 16);

    public static final @NotNull RakNetGUID UNASSIGNED = new RakNetGUID(0xFFFFFFFFFFFFFFFFL);

    public static @NotNull RakNetGUID fromString(@NotNull String str, int radix) {
        BigInteger guid = new BigInteger(str, radix);
        return new RakNetGUID(guid);
    }

    public static @NotNull RakNetGUID fromString(@NotNull String str) {
        return fromString(str, 10);
    }

    public static int toUnsignedInt(@NotNull RakNetGUID guid) {
        /*
         * See the following link for the original implementation of this method:
         *  https://github.com/facebookarchive/RakNet/blob/master/Source/RakNetTypes.cpp#L811
         */
        BigInteger u32 = guid.guidExact.shiftRight(Integer.SIZE);
        BigInteger mask = guid.guidExact.and(UNSIGNED_INT_MAX_VALUE);
        return u32.xor(mask).intValue();
    }

    /*
     * The "guid" field is used for fast comparison in the equals()
     * method in addition to generating the value for hashCode(). The
     * "guidExact" field is used for compareTo() to ensure accuracy.
     */
    private final long guid;
    private final @NotNull BigInteger guidExact;

    public RakNetGUID() {
        this.guid = 0L;
        this.guidExact = BigInteger.ZERO;
    }

    /**
     * Constructs a new <code>RakNetGUID</code>.
     *
     * @param guid The internal GUID.
     */
    public RakNetGUID(long guid) {
        String guidStr = Long.toUnsignedString(guid);
        this.guid = guid;
        this.guidExact = new BigInteger(guidStr);
    }

    /**
     * Constructs a new <code>RakNetGUID</code>.
     *
     * @param guid The internal GUID.
     * @throws IllegalArgumentException If <code>guid</code> does not fit
     *                                  within a long (i.e., takes up more
     *                                  than 64-bits).
     */
    public RakNetGUID(@NotNull BigInteger guid) {
        if (guid.bitLength() > Long.SIZE) {
            throw new IllegalArgumentException("guid must fit inside a long");
        }
        this.guid = guid.longValue();
        this.guidExact = guid;
    }

    @Override
    public int compareTo(@NotNull RakNetGUID guid) {
        return guidExact.compareTo(guid.guidExact);
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
        return guidExact.toString();
    }

}
