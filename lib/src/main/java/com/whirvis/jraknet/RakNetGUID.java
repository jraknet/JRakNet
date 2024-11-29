package com.whirvis.jraknet;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class RakNetGUID implements Comparable<RakNetGUID> {

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
