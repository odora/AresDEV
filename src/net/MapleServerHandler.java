/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation version 3 as published by
the Free Software Foundation. You may not use, modify or distribute
this program under any other version of the GNU Affero General Public
License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.server.Server;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import tools.FilePrinter;
import tools.MapleAESOFB;
import tools.MaplePacketCreator;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;
import constants.ServerConstants;

public class MapleServerHandler extends IoHandlerAdapter {

	private final PacketProcessor processor;
	private int world = -1, channel = -1;
	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"dd-MM-yyyy HH:mm");

	public MapleServerHandler() {
		this.processor = PacketProcessor.getProcessor(-1, -1);
	}

	public MapleServerHandler(int world, int channel) {
		this.processor = PacketProcessor.getProcessor(world, channel);
		this.world = world;
		this.channel = channel;
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		if ((cause instanceof IOException)
				|| (cause instanceof ClassCastException)) {
			return;
		}
		final MapleClient mc = (MapleClient) session
				.getAttribute(MapleClient.CLIENT_KEY);
		if ((mc != null) && (mc.getPlayer() != null)) {
			FilePrinter.printError(FilePrinter.EXCEPTION_CAUGHT, cause,
					"Exception caught by: " + mc.getPlayer());
		}
	}

	@Override
	public void sessionOpened(IoSession session) {
		if (!Server.getInstance().isOnline()) {
			session.close(true);
			return;
		}
		if ((this.channel > -1) && (this.world > -1)) {
			if (Server.getInstance().getChannel(this.world, this.channel) == null) {
				session.close(true);
				return;
			}
		} else {
			FilePrinter.print(
					FilePrinter.SESSION,
					"IoSession with " + session.getRemoteAddress()
							+ " opened on "
							+ sdf.format(Calendar.getInstance().getTime()),
					false);
		}

		final byte key[] = { 0x13, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00,
				0x06, 0x00, 0x00, 0x00, (byte) 0xB4, 0x00, 0x00, 0x00, 0x1B,
				0x00, 0x00, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x33, 0x00, 0x00,
				0x00, 0x52, 0x00, 0x00, 0x00 };
		final byte ivRecv[] = { 70, 114, 122, 82 };
		final byte ivSend[] = { 82, 48, 120, 115 };
		ivRecv[3] = (byte) (Math.random() * 255);
		ivSend[3] = (byte) (Math.random() * 255);
		final MapleAESOFB sendCypher = new MapleAESOFB(key, ivSend,
				(short) (0xFFFF - ServerConstants.VERSION));
		final MapleAESOFB recvCypher = new MapleAESOFB(key, ivRecv,
				ServerConstants.VERSION);
		final MapleClient client = new MapleClient(sendCypher, recvCypher,
				session);
		client.setWorld(this.world);
		client.setChannel(this.channel);
		session.write(MaplePacketCreator.getHello(ServerConstants.VERSION,
				ivSend, ivRecv));
		session.setAttribute(MapleClient.CLIENT_KEY, client);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		final MapleClient client = (MapleClient) session
				.getAttribute(MapleClient.CLIENT_KEY);
		if (client != null) {
			try {
				boolean inCashShop = false;
				if (client.getPlayer() != null) {
					inCashShop = client.getPlayer().getCashShop().isOpened();
				}
				client.disconnect(false, inCashShop);
			} catch (final Throwable t) {
				FilePrinter.printError(FilePrinter.ACCOUNT_STUCK, t);
			} finally {
				session.close();
				session.removeAttribute(MapleClient.CLIENT_KEY);
				// client.empty();
			}
		}
		super.sessionClosed(session);
	}

	@Override
	public void messageReceived(IoSession session, Object message) {
		final byte[] content = (byte[]) message;
		final SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(
				new ByteArrayByteStream(content));
		final short packetId = slea.readShort();
		final MapleClient client = (MapleClient) session
				.getAttribute(MapleClient.CLIENT_KEY);

		final MaplePacketHandler packetHandler = this.processor
				.getHandler(packetId);
		if ((packetHandler != null) && packetHandler.validateState(client)) {
			try {
				packetHandler.handlePacket(slea, client);
			} catch (final Throwable t) {
				FilePrinter.printError(
						FilePrinter.PACKET_HANDLER
								+ packetHandler.getClass().getName() + ".txt",
						t,
						"Error for "
								+ (client.getPlayer() == null ? ""
										: "player ; " + client.getPlayer()
												+ " on map ; "
												+ client.getPlayer().getMapId()
												+ " - ") + "account ; "
								+ client.getAccountName() + "\r\n"
								+ slea.toString());
				// client.announce(MaplePacketCreator.enableActions());//bugs
				// sometimes
			}
		}
	}

	@Override
	public void sessionIdle(final IoSession session, final IdleStatus status)
			throws Exception {
		final MapleClient client = (MapleClient) session
				.getAttribute(MapleClient.CLIENT_KEY);
		if (client != null) {
			client.sendPing();
		}
		super.sessionIdle(session, status);
	}
}
