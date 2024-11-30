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

import java.io.IOException;
import java.net.*;

/**
 * An implementation of {@link RakNetSocket} using Java's built-in
 * {@link DatagramSocket}.
 */
public class RakNetDatagramSocket extends RakNetSocket.Polled {

    private static boolean receive(
            @NotNull DatagramSocket socket,
            @NotNull DatagramPacket packet
    ) {
        try {
            socket.receive(packet);
            return true;
        } catch (SocketTimeoutException e) {
            return false; /* no data received */
        } catch (IOException e) {
            throw new RakNetSocketException(e);
        }
    }

    private static InetSocketAddress getAddress(@NotNull DatagramPacket packet) {
        return new InetSocketAddress(packet.getAddress(), packet.getPort());
    }

    private DatagramSocket socket;
    private DatagramPacket receivePacket;
    private byte[] receiveBuffer;

    @Override
    public void bindSocket(@NotNull InetSocketAddress address) {
        try {
            int mtu = RakNet.getMTU(address);

            this.socket = new DatagramSocket(address);
            this.receiveBuffer = new byte[mtu];
            this.receivePacket = new DatagramPacket(receiveBuffer, mtu);

            socket.setSendBufferSize(mtu);
            socket.setReceiveBufferSize(mtu);
            socket.setSoTimeout(1);
        } catch (SocketException e) {
            throw new RakNetSocketException(e);
        }
    }

    @Override
    public void sendData(
            byte @NotNull [] data,
            int off,
            int len,
            @NotNull InetSocketAddress address,
            int ttl
    ) {
        DatagramPacket packet = new DatagramPacket(data, off, len);
        packet.setSocketAddress(address);
        try {
            socket.send(packet);
            this.callEvent(handler -> handler.onSend(
                    data, off, len, address, ttl, this));
        } catch (IOException e) {
            throw new RakNetSocketException(e);
        }
    }

    @Override
    public void closeSocket() {
        if (socket != null) {
            socket.close();
        }
        this.socket = null;
        this.receivePacket = null;
        this.receiveBuffer = null;
    }

    @Override
    public void pollSocket() {
        if (!receive(socket, receivePacket)) {
            return; /* no data received */
        }

        long currentTime = System.currentTimeMillis();
        InetSocketAddress address = getAddress(receivePacket);
        int length = receivePacket.getLength();

        this.callEvent(handler -> handler.onReceive(
                receiveBuffer, length, address, currentTime, this));
    }

}
