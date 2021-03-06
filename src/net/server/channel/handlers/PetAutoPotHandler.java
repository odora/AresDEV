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
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;

public final class PetAutoPotHandler extends AbstractMaplePacketHandler {
	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea,
			MapleClient c) {
		if (!c.getPlayer().isAlive()) {
			c.announce(MaplePacketCreator.enableActions());
			return;
		}
		slea.readByte();
		slea.readLong();
		slea.readInt();
		final byte slot = (byte) slea.readShort();
		final int itemId = slea.readInt();
		final Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE)
				.getItem(slot);
		if ((toUse != null) && (toUse.getQuantity() > 0)) {
			if (toUse.getItemId() != itemId) {
				c.announce(MaplePacketCreator.enableActions());
				return;
			}
			MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE,
					slot, (short) 1, false);
			final MapleStatEffect stat = MapleItemInformationProvider
					.getInstance().getItemEffect(toUse.getItemId());
			stat.applyTo(c.getPlayer());
			if (stat.getMp() > 0) {
				c.announce(MaplePacketCreator.sendAutoMpPot(itemId));
			}
			if (stat.getHp() > 0) {
				c.announce(MaplePacketCreator.sendAutoHpPot(itemId));
			}
		}
	}
}
