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

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * TODO: docs
 */
public abstract class RakNetSocket implements Closeable {

    public interface EventHandler {

        /**
         * Called when a socket is bound.
         *
         * @param address the address the socket is now bound to.
         * @param socket  the socket which was bound.
         */
        default void onBind(
                @NotNull InetSocketAddress address,
                @NotNull RakNetSocket socket
        ) {
            /* no-op */
        }

        default void onSend(
                byte @NotNull [] data,
                int off,
                int len,
                @NotNull InetSocketAddress address,
                int ttl,
                @NotNull RakNetSocket socket
        ) {
            /* no-op */
        }

        /**
         * Called when a socket is closed.
         *
         * @param pollThread the thread which polled the socket, if any.
         * @param cause      the error which caused the socket to close, if
         *                   any. This will only not be {@code null} if the
         *                   socket used a poll thread and an exception was
         *                   thrown there.
         * @param socket     the socket which closed.
         */
        default void onClose(
                @Nullable Thread pollThread,
                @Nullable Throwable cause,
                @NotNull RakNetSocket socket
        ) {
            /* no-op */
        }

        /**
         * Called when a socket receives data.
         *
         * @param data    the data buffer.
         * @param length  the number of bytes received.
         * @param address the source of the message.
         * @param timeMs  the time of reception.
         * @param socket  the socket which received the data.
         */
        default void onReceive(
                byte @NotNull [] data,
                int length,
                @NotNull InetSocketAddress address,
                long timeMs,
                @NotNull RakNetSocket socket
        ) {
            /* no-op */
        }

    }

    /**
     * A {@link RakNetSocket} which is polled automatically via a
     * dedicated polling thread.
     */
    public static abstract class Polled extends RakNetSocket {

        private static final Duration
                DEFAULT_POLL_FREQUENCY = Duration.ofMillis(100);

        private Duration pollTime;
        private long pollTimeMs;

        public Polled() {
            this.setPollTime(DEFAULT_POLL_FREQUENCY);
        }

        /**
         * Returns the time between each call to {@link #pollSocket()}.
         *
         * @return the time between each call to {@link #pollSocket()}.
         */
        public final @NotNull Duration getPollTime() {
            return this.pollTime;
        }

        /**
         * Sets how often {@link #pollSocket()} is called each second.
         *
         * @param pollTime the time between each poll. A {@code null}
         *                 value is permitted, and will have a default
         *                 value used instead.
         */
        public final void setPollTime(@Nullable Duration pollTime) {
            if (pollTime == null) {
                this.setPollTime(DEFAULT_POLL_FREQUENCY);
            } else if (pollTime.toMillis() <= 0) {
                String msg = "pollTime must be positive";
                throw new IllegalArgumentException(msg);
            } else {
                this.pollTime = pollTime;
                this.pollTimeMs = pollTime.toMillis();
            }
        }

        /**
         * Polls the socket on the polling thread.
         * <p>
         * This method is called in a set interval, depending on the last
         * value passed to {@link #setPollTime(Duration)}.
         */
        protected abstract void pollSocket();

    }

    private static class PollThread extends Thread {

        private final @NotNull Polled polledSocket;

        public PollThread(@NotNull Polled polled) {
            this.polledSocket = polled;
        }

        @SuppressWarnings("BusyWait")
        @Override
        public void run() {
            try {
                while (!polledSocket.isClosed()) {
                    polledSocket.pollSocket();
                    Thread.sleep(polledSocket.pollTimeMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Throwable e) {
                polledSocket.close(this, e);
            }
        }

    }

    private final @NotNull ReadWriteLock socketLock;
    private final @NotNull ReadWriteLock eventHandlerLock;

    private int index;
    private @Nullable EventHandler eventHandler;
    private @Nullable InetSocketAddress boundAddress;
    private @Nullable Thread pollThread;
    private boolean closed;

    /**
     * Constructs a new {@code RakNetSocket}.
     */
    public RakNetSocket() {
        this.socketLock = new ReentrantReadWriteLock();
        this.eventHandlerLock = new ReentrantReadWriteLock();
        this.index = -1;
    }

    /* called by RakPeer on startup */
    void setIndex(int index) {
        socketLock.writeLock().lock();
        try {
            this.index = index;
        } finally {
            socketLock.writeLock().unlock();
        }
    }

    /**
     * Returns the index of this connection socket for a {@link RakPeer}.
     *
     * @return the index of this connection socket for a {@link RakPeer};
     * {@code -1} if this socket doesn't belong to one.
     */
    public final int getIndex() {
        socketLock.readLock().lock();
        try {
            return this.index;
        } finally {
            socketLock.readLock().unlock();
        }
    }

    /**
     * Sets the event handler.
     *
     * @param handler the event handler.
     */
    public final void setEventHandler(@Nullable EventHandler handler) {
        eventHandlerLock.writeLock().lock();
        try {
            this.eventHandler = handler;
        } finally {
            eventHandlerLock.writeLock().unlock();
        }
    }

    /**
     * Calls an event for the current event handler, if any.
     *
     * @param event the event to call.
     * @throws NullPointerException if {@code event} is {@code null}.
     */
    protected final void callEvent(@NotNull Consumer<EventHandler> event) {
        Objects.requireNonNull(event, "event cannot be null");
        eventHandlerLock.readLock().lock();
        try {
            if (eventHandler != null) {
                event.accept(eventHandler);
            }
        } finally {
            eventHandlerLock.readLock().unlock();
        }
    }

    /**
     * Returns if this socket has been bound to an address.
     *
     * @return {@code true} if this socket has been bound to an address;
     * {@code false} otherwise.
     */
    public final boolean isBound() {
        socketLock.readLock().lock();
        try {
            return this.boundAddress != null;
        } finally {
            socketLock.readLock().unlock();
        }
    }

    /**
     * Returns the address this socket is bound to.
     *
     * @return the address this socket is bound to; {@code null} if this
     * socket has not yet been bound.
     */
    public final @Nullable InetSocketAddress getBoundAddress() {
        socketLock.readLock().lock();
        try {
            return this.boundAddress;
        } finally {
            socketLock.readLock().unlock();
        }
    }

    /**
     * Implementation for {@link #bind(InetSocketAddress)}.
     *
     * @param address the address to bind to.
     */
    protected abstract void bindSocket(@NotNull InetSocketAddress address);

    /**
     * Binds the socket to the given address.
     *
     * @param address the address to bind to.
     * @throws NullPointerException  if {@code address} is {@code null}.
     * @throws IllegalStateException if this socket has already been
     *                               bound to an address.
     */
    public final void bind(@NotNull InetSocketAddress address) {
        Objects.requireNonNull(address, "address cannot be null");
        socketLock.writeLock().lock();
        try {
            if (boundAddress != null) {
                String msg = "socket already bound to" + boundAddress;
                throw new IllegalStateException(msg);
            }

            this.bindSocket(address);
            this.boundAddress = address;

            if (this instanceof Polled) {
                PollThread thread = new PollThread((Polled) this);
                thread.start();
                this.pollThread = thread;
            }

            this.callEvent(handler -> handler.onBind(address, this));
        } finally {
            socketLock.writeLock().unlock();
        }
    }

    /**
     * Implementation for {@link #send(byte[], InetSocketAddress, int)}.
     *
     * @param data    the data to send.
     * @param off     the start offset in {@code data}.
     * @param len     the number of bytes to send.
     * @param address the address to send to.
     * @param ttl     the time-to-live, may be ignored.
     */
    protected abstract void sendData(
            byte @NotNull [] data,
            int off,
            int len,
            @NotNull InetSocketAddress address,
            int ttl
    );

    /**
     * Sends a packet to the specified address.
     *
     * @param data    the data to send.
     * @param off     the start offset in {@code data}.
     * @param len     the number of bytes to send.
     * @param address the address to send to.
     * @param ttl     the time-to-live, may be ignored.
     * @throws NullPointerException     if {@code data} is {@code null};
     *                                  if {@code address} is {@code null}.
     * @throws IllegalArgumentException if {@code ttl} is negative.
     * @throws IllegalStateException    if the socket has not yet been
     *                                  bound to an address.
     */
    public final void send(
            byte @NotNull [] data,
            int off,
            int len,
            @NotNull InetSocketAddress address,
            int ttl
    ) {
        Objects.requireNonNull(data, "data cannot be null");
        Objects.requireNonNull(address, "address cannot be null");
        if (ttl < 0) {
            throw new IllegalArgumentException("ttl cannot be negative");
        }

        /*
         * We use the write lock here instead of the read lock, so only one
         * thread at a time can call sendData() on the underlying socket.
         */
        socketLock.writeLock().lock();
        try {
            if (boundAddress == null) {
                String msg = "Socket must be bound to an address to send";
                throw new IllegalStateException(msg);
            }
            this.sendData(data, off, len, address, ttl);
        } finally {
            socketLock.writeLock().unlock();
        }
    }

    /**
     * Sends a packet to the specified address.
     *
     * @param data    the data to send.
     * @param address the address to send to.
     * @param ttl     the time-to-live, may be ignored.
     * @throws NullPointerException     if {@code data} is {@code null};
     *                                  if {@code address} is {@code null}.
     * @throws IllegalArgumentException if {@code ttl} is negative.
     * @throws IllegalStateException    if the socket has not yet been
     *                                  bound to an address.
     */
    public final void send(
            byte @NotNull [] data,
            @NotNull InetSocketAddress address,
            int ttl
    ) {
        this.send(data, 0, data.length, address, ttl);
    }

    /**
     * Returns if this socket has been closed.
     *
     * @return {@code true} if this socket has been closed; {@code false}
     * otherwise.
     */
    public final boolean isClosed() {
        socketLock.readLock().lock();
        try {
            return this.closed;
        } finally {
            socketLock.readLock().unlock();
        }
    }

    /**
     * Implementation for {@link #close()}.
     */
    protected abstract void closeSocket();

    void close(@Nullable Thread pollThread, @Nullable Throwable cause) {
        socketLock.writeLock().lock();
        try {
            if (closed) {
                return;
            }

            this.closeSocket();

            this.boundAddress = null;
            this.pollThread = null;
            this.closed = true;

            this.callEvent(handler -> handler.onClose(pollThread, cause, this));
        } finally {
            socketLock.writeLock().unlock();
        }
    }

    @Override
    public final void close() {
        this.close(pollThread, null);
    }

}
