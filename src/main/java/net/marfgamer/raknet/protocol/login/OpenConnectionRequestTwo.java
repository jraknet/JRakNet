/*
 *       _   _____            _      _   _          _   
 *      | | |  __ \          | |    | \ | |        | |  
 *      | | | |__) |   __ _  | | __ |  \| |   ___  | |_ 
 *  _   | | |  _  /   / _` | | |/ / | . ` |  / _ \ | __|
 * | |__| | | | \ \  | (_| | |   <  | |\  | |  __/ | |_ 
 *  \____/  |_|  \_\  \__,_| |_|\_\ |_| \_|  \___|  \__|
 *                                                  
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 MarfGamer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
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
package net.marfgamer.raknet.protocol.login;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import net.marfgamer.raknet.Packet;
import net.marfgamer.raknet.RakNetPacket;
import net.marfgamer.raknet.protocol.MessageIdentifier;

public class OpenConnectionRequestTwo extends RakNetPacket {

	public boolean magic;
	public InetSocketAddress address;
	public int maximumTransferUnit;
	public long clientGuid;

	public OpenConnectionRequestTwo(Packet packet) {
		super(packet);
	}

	public OpenConnectionRequestTwo() {
		super(MessageIdentifier.ID_OPEN_CONNECTION_REQUEST_2);
	}

	@Override
	public void encode() {
		try {
			this.writeMagic();
			this.writeAddress(address);
			this.writeShort(maximumTransferUnit);
			this.writeLong(clientGuid);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void decode() {
		try {
			this.magic = this.checkMagic();
			this.address = this.readAddress();
			this.maximumTransferUnit = this.readUShort();
			this.clientGuid = this.readLong();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
