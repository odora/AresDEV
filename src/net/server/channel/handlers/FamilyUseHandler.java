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
package net.server.channel.handlers;

import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import client.MapleCharacter;
import client.MapleClient;

/**
 *
 * @author Moogra
 */
public final class FamilyUseHandler extends AbstractMaplePacketHandler {
	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea,
			MapleClient c) {
		final int[] repCost = { 3, 5, 7, 8, 10, 12, 15, 20, 25, 40, 50 };
		final int type = slea.readInt();
		MapleCharacter victim;
		if ((type == 0) || (type == 1)) {
			victim = c.getChannelServer().getPlayerStorage()
					.getCharacterByName(slea.readMapleAsciiString());
			if (victim != null) {
				if (type == 0) {
					c.getPlayer().changeMap(victim.getMap(),
							victim.getMap().getPortal(0));
				} else {
					victim.changeMap(c.getPlayer().getMap(), c.getPlayer()
							.getMap().getPortal(0));
				}
			} else {
				return;
			}
		} else {
			final int erate = type == 3 ? 150 : ((type == 4) || (type == 6)
					|| (type == 8) || (type == 10) ? 200 : 100);
			final int drate = type == 2 ? 150 : ((type == 4) || (type == 5)
					|| (type == 7) || (type == 9) ? 200 : 100);
			if (type > 8) {
			} else {
				c.announce(useRep(drate == 100 ? 2 : (erate == 100 ? 3 : 4),
						type, erate, drate, (((type > 5) || (type == 4)) ? 2
								: 1) * 15 * 60 * 1000));
			}
		}
		c.getPlayer().getFamily().getMember(c.getPlayer().getId())
				.gainReputation(repCost[type]);
	}

	/**
	 * [65 00][02][08 00 00 00][C8 00 00 00][00 00 00 00][00][40 77 1B 00]
	 */
	private static byte[] useRep(int mode, int type, int erate, int drate,
			int time) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(0x60);// noty
		mplew.write(mode);
		mplew.writeInt(type);
		if (mode < 4) {
			mplew.writeInt(erate);
			mplew.writeInt(drate);
		}
		mplew.write(0);
		mplew.writeInt(time);
		return mplew.getPacket();
	}
}
