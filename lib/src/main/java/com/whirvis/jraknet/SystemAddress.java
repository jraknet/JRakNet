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

import java.math.BigInteger;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Utilities for working with IP addresses.
 *
 * @see #fromString(String, char, int, InetFamily)
 * @see #toString(InetSocketAddress, char)
 */
public final class SystemAddress {

    public static final @NotNull InetSocketAddress
            ZERO = new InetSocketAddress("0.0.0.0", 0);

    public static final @NotNull Comparator<InetSocketAddress>
            COMPARATOR = SystemAddress::compare;

    public static final @NotNull List<InetAddress>
            LOOPBACK_ADDRESSES = getLoopbackAddresses(),
            IPV4_LOOPBACK_ADDRESSES = getLoopbackAddresses(InetFamily.INET4),
            IPV6_LOOPBACK_ADDRESSES = getLoopbackAddresses(InetFamily.INET6);

    public static final @NotNull InetAddress
            PREFERRED_IPV4_LOOPBACK = resolve("127.0.0.1"),
            PREFERRED_IPV6_LOOPBACK = resolve("::1");

    private static final @NotNull Map<Character, Pattern>
            BRACKETED_REGEXES = new HashMap<>(),
            UNBRACKETED_REGEXES = new HashMap<>();

    private static @NotNull BigInteger
    addressToUnsignedInt(@NotNull InetAddress address) {
        byte[] addressBytes = address.getAddress();

        /*
         * By allocating an array with one extra byte (and appending the
         * address to the end), we ensure that the value is interpreted as
         * an unsigned value.
         *
         * TODO: Find a more efficient way to implement this.
         */
        byte[] unsignedAddressBytes = new byte[addressBytes.length + 1];
        System.arraycopy(
                addressBytes, 0,
                unsignedAddressBytes, 1,
                addressBytes.length
        );

        return new BigInteger(unsignedAddressBytes);
    }

    private static @NotNull List<InetAddress>
    getLoopbackAddresses() {
        List<InetAddress> loopbackAddresses = new ArrayList<>();

        Enumeration<NetworkInterface> nits;
        try {
            nits = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            /* TODO: log this error to the console? */
            /* it's possible this PC just has no network cards */
            return Collections.unmodifiableList(loopbackAddresses);
        }

        while (nits.hasMoreElements()) {
            NetworkInterface nit = nits.nextElement();

            Enumeration<InetAddress> addresses = nit.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                loopbackAddresses.add(address);
            }
        }

        /*
         * Not sure why, but it seems that "0.0.0.0" isn't returned as one
         * of the addresses for the network cards. Perhaps it's considered
         * universal so it just isn't listed here?
         */
        InetAddress zeroAddress = ZERO.getAddress();
        if (!loopbackAddresses.contains(zeroAddress)) {
            loopbackAddresses.add(zeroAddress);
        }

        loopbackAddresses.sort((o1, o2) -> {
            BigInteger o1Address = addressToUnsignedInt(o1);
            BigInteger o2Address = addressToUnsignedInt(o2);
            return o1Address.compareTo(o2Address);
        });

        return Collections.unmodifiableList(loopbackAddresses);
    }

    private static @NotNull List<InetAddress>
    getLoopbackAddresses(@NotNull InetFamily family) {
        List<InetAddress> loopbackAddresses = new ArrayList<>();
        for (InetAddress address : LOOPBACK_ADDRESSES) {
            if (InetFamily.ofAddress(address) == family) {
                loopbackAddresses.add(address);
            }
        }
        return Collections.unmodifiableList(loopbackAddresses);
    }

    /**
     * Wrapper for {@link InetAddress#getByName(String)}.
     * <p>
     * This wraps the {@link UnknownHostException} into an uncaught runtime
     * exception for convenience. Only use this method if you know for a fact
     * that the host string will be resolved.
     *
     * @param host the specified host, or null.
     * @return an IP address for the given host name.
     * @throws IllegalArgumentException If the host could not be resolved.
     */
    public static @NotNull InetAddress
    resolve(@Nullable String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static @NotNull InetSocketAddress
    notNullOrZero(@Nullable InetSocketAddress address) {
        if (address == null) {
            return ZERO;
        }
        return address;
    }

    static int
    compare(@NotNull InetSocketAddress o1, @NotNull InetSocketAddress o2) {
        int portCompare = Integer.compare(o1.getPort(), o2.getPort());
        if (portCompare != 0) {
            return portCompare;
        }

        BigInteger o1Address = addressToUnsignedInt(o1.getAddress());
        BigInteger o2Address = addressToUnsignedInt(o2.getAddress());
        return o1Address.compareTo(o2Address);
    }

    /* TODO: docs */
    public static boolean
    isLoopback(@Nullable InetAddress address) {
        if (address == null) {
            return false;
        }
        return LOOPBACK_ADDRESSES.contains(address);
    }

    /* TODO: docs */
    public static boolean isLoopback(@Nullable InetSocketAddress address) {
        if (address == null) {
            return false;
        }
        return isLoopback(address.getAddress());
    }

    /* TODO: docs */
    public static boolean isLanAddress(@Nullable InetAddress address) {
        if (address == null) {
            return false;
        } else if (InetFamily.ofAddress(address) != InetFamily.INET4) {
            return false;
        }

        byte[] octets = address.getAddress();
        byte firstOctet = octets[0];
        byte secondOctet = octets[1];

        /*
         * Per RFC 1918, these ranges are reserved for private networks:
         *      10.0.0.0       -    10.255.255.255  (10/8 prefix)
         *      172.16.0.0     -    172.31.255.255  (172.16/12 prefix)
         *      192.168.0.0    -    192.168.255.255 (192.168/16 prefix)
         * See: https://networkengineering.stackexchange.com/a/5830
         */
        if (firstOctet == 10) {
            return true;
        } else if (firstOctet == (byte) 172) {
            return secondOctet >= 16 && secondOctet <= 31;
        } else if (firstOctet == (byte) 192) {
            return secondOctet == (byte) 168;
        }

        return false;
    }

    /* TODO: docs */
    public static boolean isLanAddress(@Nullable InetSocketAddress address) {
        if (address == null) {
            return false;
        }
        return isLanAddress(address.getAddress());
    }

    private static @NotNull InetAddress
    parseHost(
            @NotNull String host,
            @Nullable InetFamily preferredInetFamily
    ) throws UnknownHostException {
        InetAddress hostAddress = InetAddress.getByName(host);

        /*
         * If a loopback address is given, but is of a family which is
         * different from the preferred one, use a loopback address from
         * the preferred family if one is available.
         */
        if (preferredInetFamily == InetFamily.INET4
                && IPV6_LOOPBACK_ADDRESSES.contains(hostAddress)) {
            if (IPV4_LOOPBACK_ADDRESSES.contains(PREFERRED_IPV4_LOOPBACK)) {
                hostAddress = PREFERRED_IPV4_LOOPBACK;
            } else if (!IPV4_LOOPBACK_ADDRESSES.isEmpty()) {
                hostAddress = IPV4_LOOPBACK_ADDRESSES.get(0);
            }
        } else if (preferredInetFamily == InetFamily.INET6
                && IPV4_LOOPBACK_ADDRESSES.contains(hostAddress)) {
            if (IPV6_LOOPBACK_ADDRESSES.contains(PREFERRED_IPV6_LOOPBACK)) {
                hostAddress = PREFERRED_IPV6_LOOPBACK;
            } else if (!IPV6_LOOPBACK_ADDRESSES.isEmpty()) {
                hostAddress = IPV6_LOOPBACK_ADDRESSES.get(0);
            }
        }

        return hostAddress;
    }

    private static int
    parsePort(
            @Nullable String portStr,
            int defaultPort
    ) {
        if (portStr == null) {
            return defaultPort;
        }

        /*
         * If the port is present, but cannot be parsed as a string,
         * treat it as though the entire address is invalid. It would
         * confuse users if they entered an incorrect port value and
         * some other default port was used instead.
         */
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            String msg = "Could not parse port";
            throw new IllegalArgumentException(msg, e);
        }
    }

    /* TODO: docs */
    public static @NotNull InetSocketAddress
    fromString(
            @Nullable String str,
            char portDelimiter,
            int defaultPort,
            @Nullable InetFamily preferredInetFamily
    ) {
        if (defaultPort < 0x0000 || defaultPort > 0xFFFF) {
            String msg = "Port out of range: " + defaultPort;
            throw new IllegalArgumentException(msg);
        }

        if (str == null) {
            return ZERO;
        }

        /*
         * If no port delimiter is specified, assume the caller doesn't
         * expect a port to be specified by the user. In which case, skip
         * the extra parsing and parse the given string as is and use the
         * specified default port.
         */
        if (portDelimiter == '\0') {
            try {
                InetAddress address = parseHost(str, preferredInetFamily);
                return new InetSocketAddress(address, defaultPort);
            } catch (UnknownHostException e) {
                String msg = "Could not resolve address";
                throw new IllegalArgumentException(msg, e);
            }
        }

        Pattern bracketedRegex = BRACKETED_REGEXES.get(portDelimiter);
        if (bracketedRegex == null) {
            bracketedRegex = Pattern.compile("\\[.*]\\Q" + portDelimiter + "\\E.*");
            BRACKETED_REGEXES.put(portDelimiter, bracketedRegex);
        }

        Pattern unbracketedRegex = UNBRACKETED_REGEXES.get(portDelimiter);
        if (unbracketedRegex == null) {
            unbracketedRegex = Pattern.compile("\\Q" + portDelimiter + "\\E");
            UNBRACKETED_REGEXES.put(portDelimiter, unbracketedRegex);
        }

        /*
         * It's possible that the IP address will be surrounded by brackets.
         * This is usually the case for IPv6 addresses which delineate their
         * ports with a colon, for example: [::1]:19132.
         */
        List<String> parts = new ArrayList<>();
        if (!bracketedRegex.matcher(str).matches()) {
            String[] strParts = str.split(unbracketedRegex.pattern());
            parts.addAll(Arrays.asList(strParts));
        } else {
            int openBracketIndex = 1;
            int closeBracketIndex = str.indexOf(']');
            String bracketRange = str.substring(openBracketIndex, closeBracketIndex);

            int portStartIndex = closeBracketIndex + 2;
            int portEndIndex = str.length();
            String portRange = str.substring(portStartIndex, portEndIndex);

            parts.add(bracketRange);
            parts.add(portRange);
        }

        if (parts.size() > 2) {
            String msg = "Expecting only an address and a port";
            throw new IllegalArgumentException(msg);
        }

        String addressStr = parts.get(0);
        String portStr = parts.size() > 1 ? parts.get(1) : null;

        try {
            InetAddress address = parseHost(addressStr, preferredInetFamily);
            int port = parsePort(portStr, defaultPort);
            return new InetSocketAddress(address, port);
        } catch (UnknownHostException e) {
            String msg = "Could not resolve address";
            throw new IllegalArgumentException(msg, e);
        }
    }

    /* TODO: docs */
    public static @NotNull InetSocketAddress
    fromString(
            @Nullable String str,
            char portDelimiter,
            int defaultPort
    ) {
        return fromString(str, portDelimiter, defaultPort, null);
    }

    /**
     * Returns the string representation of an address, in the form of
     * {@code host<separator>port}; or {@code host} if no port separator
     * is specified.
     *
     * @param address       the address. A value of {@code null} may be
     *                      specified, and will have {@link #ZERO} used
     *                      instead.
     * @param portSeparator the port separator to use. Use {@code '\0'}
     *                      to discard the port from the string.
     * @return the string representation of {@code address}.
     */
    public static @NotNull String
    toString(@Nullable InetSocketAddress address, char portSeparator) {
        if (address == null) {
            return toString(ZERO, portSeparator);
        }

        String host = address.getHostString();
        if (portSeparator == '\0') {
            return host;
        }
        return host + portSeparator + address.getPort();
    }

    /**
     * Returns the string representation of an address, in the form of
     * {@code host|port}.
     *
     * @param address the address. A value of {@code null} may be specified,
     *                and will have {@link #ZERO} used instead.
     * @return the string representation of {@code address}.
     */
    public static @NotNull String
    toString(@Nullable InetSocketAddress address) {
        return toString(address, '|');
    }

    private SystemAddress() {
        throw new UnsupportedOperationException();
    }

}
