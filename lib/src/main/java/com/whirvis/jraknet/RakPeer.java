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

import com.whirvis.jraknet.dummy.BitStream;
import com.whirvis.jraknet.dummy.PluginInterface;
import com.whirvis.jraknet.dummy.RakNetSocket;
import com.whirvis.jraknet.dummy.RakNetStatistics;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.List;

@SuppressWarnings("unused")
public interface RakPeer extends Closeable {

    StartupResult startup(
            int maxConnections,
            List<InetSocketAddress> socketDescriptors,
            int threadPriority
    );

    boolean initializeSecurity(
            KeyPair key,
            boolean requireClientKey
    );

    void disableSecurity();

    void addToSecurityExceptionList(
            InetAddress ip
    );

    void removeFromSecurityExceptionList(
            InetAddress ip
    );

    boolean isInSecurityExceptionList(
            InetAddress ip
    );

    void setMaximumIncomingConnections(short numberAllowed);

    int getMaximumIncomingConnections();

    short getNumberOfConnections();

    void setIncomingPassword(String password);

    String getIncomingPassword();

    ConnectionAttemptResult connect(
            InetSocketAddress address,
            String password,
            PublicKey key,
            int socketIndex,
            int attemptCount,
            long timeBetweenAttempts,
            long timeoutTime
    );

    ConnectionAttemptResult connect(
            InetSocketAddress address,
            String password,
            PublicKey key,
            RakNetSocket socket,
            int attemptCount,
            long timeBetweenAttempts,
            long timeout
    );

    void shutdown(
            long blockDuration,
            byte orderingChannel,
            PacketPriority disconnectionNotificationPriority
    );

    boolean isActive();

    List<InetSocketAddress> getConnectionList();

    int getNextSendReceipt();

    int incrementNextSendReceipt();

    int send(
            byte[] data,
            PacketPriority priority,
            PacketReliability reliability,
            byte orderingChannel,
            AddressOrGUID systemIdentifier,
            boolean broadcast,
            int forceReceiptNumber
    );

    int send(
            BitStream bits,
            PacketPriority priority,
            PacketReliability reliability,
            byte orderingChannel,
            AddressOrGUID systemIdentifier,
            boolean broadcast,
            int forceReceiptNumber
    );

    void sendLoopback(
            byte[] data
    );

    void sendLoopback(
            BitStream bits
    );

    int sendList(
            List<byte[]> data,
            int numParameters,
            PacketPriority priority,
            PacketReliability reliability,
            byte orderingChannel,
            AddressOrGUID systemIdentifier,
            boolean broadcast,
            int forceReceiptNumber
    );

    Packet receive();

    int getMaximumNumberOfPeers();

    void closeConnection(
            AddressOrGUID target,
            boolean sendDisconnectionNotification,
            byte orderingChannel,
            PacketPriority disconnectionNotificationPriority
    );

    ConnectionState getConnectionState(
            AddressOrGUID systemIdentifier
    );

    void cancelConnectionAttempt(
            InetSocketAddress target
    );

    int getIndexFromInetSocketAddress(
            InetSocketAddress systemAddress
    );

    InetSocketAddress getInetSocketAddressFromIndex(
            int index
    );



    RakNetGUID getGUIDFromIndex(
            int index
    );

    void getSystemList(
            List<InetSocketAddress> addresses,
            List<RakNetGUID> guids
    );

    void addToBanList(
            InetAddress ip,
            long banDuration
    );

    void removeFromBanList(InetAddress ip);

    void clearBanList();

    boolean isBanned(InetAddress ip);

    void setLimitIPConnectionFrequency(
            boolean limit
    );

    void ping(
            InetSocketAddress target
    );

    boolean ping(
            InetSocketAddress target,
            boolean onlyReplyOnAcceptingConnections,
            int connectionSocketIndex
    );

    int getAveragePing(
            AddressOrGUID systemIdentifier
    );

    int getLastPing(
            AddressOrGUID systemIdentifier
    );

    int getLowestPing(
            AddressOrGUID systemIdentifier
    );

    void setOccasionalPing(
            boolean enable
    );

    long getClockDifferential(
            AddressOrGUID systemIdentifier
    );

    void setOfflinePingResponse(
            byte[] data
    );

    byte[] getOfflinePingResponse();

    InetSocketAddress getInternalID(
            @Nullable InetSocketAddress systemAddress,
            int index
    );

    void setInternalID(
            InetSocketAddress systemAddress,
            int index
    );

    InetSocketAddress getExternalID(
            InetSocketAddress target
    );

    RakNetGUID getMyGUID();

    InetSocketAddress getMyBoundAddress(
            int socketIndex
    );

    RakNetGUID getGuidFromInetSocketAddress(
            InetSocketAddress input
    );

    InetSocketAddress getSystemAddressFromGuid(
            RakNetGUID input
    );

    boolean getClientPublicKeyFromInetSocketAddress(
            InetSocketAddress input,
            PublicKey clientPublicKey
    );

    void setTimeoutTime(
            long timeMs,
            InetSocketAddress target
    );

    long getTimeoutTime(
            InetSocketAddress target
    );

    int getMTUSize(
            InetSocketAddress target
    );

    int getNumberOfAddresses();

    InetSocketAddress getLocalIP(
            int index
    );

    boolean isLocalIP(
            String ip
    );

    void allowConnectionResponseIPMigration(
            boolean allow
    );

    boolean advertiseSystem(
            InetSocketAddress target,
            byte[] data,
            int connectionSocketIndex
    );

    void setSplitMessageProgressInterval(
            int interval
    );

    int getSplitMessageProgressInterval();

    void setUnreliableTimeout(
            long timeoutMS
    );

    void sendTTL(
            InetSocketAddress target,
            long ttl,
            int connectionSocketIndex
    );

    void attachPlugin(
            PluginInterface plugin
    );

    void detachPlugin(
            PluginInterface plugin
    );

    void pushBackPacket(
            Packet packet,
            boolean pushAtHead
    );

    void changeSystemAddress(
            RakNetGUID guid,
            InetSocketAddress systemAddress
    );

    RakNetSocket getSocket(
            InetSocketAddress target
    );

    List<RakNetSocket> getSockets();

    void releaseSockets(
            List<RakNetSocket> sockets
    );

    void writeOutOfBandHeader(
            BitStream bits
    );

    void applyNetworkSimulator(
            float packetLoss,
            short minExtraPing,
            short extraPingVariance
    );

    void setPerConnectionOutgoingBandwidthLimit(
            int maxBitsPerSecond
    );

    boolean isNetworkSimulatorActive();

    RakNetStatistics getStatistics(
            InetSocketAddress systemAddress
    );

    RakNetStatistics getStatistics(
            int index
    );

    void getStatisticsList(
            List<InetSocketAddress> addresses,
            List<RakNetGUID> guids,
            List<RakNetStatistics> statistics
    );

    int getReceiveBufferSize();

    boolean runUpdateCycle(
            BitStream updateBitStream
    );

    boolean sendOutOfBand(
            InetSocketAddress target,
            byte[] data,
            int connectionSocketIndex
    );

}
