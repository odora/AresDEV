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
package client;

public class BuddyListEntry {
	private final String name;
	private String group;
	private final int cid;
	private int channel;
	private boolean visible;

	/**
	 *
	 * @param name
	 * @param characterId
	 * @param channel
	 *            should be -1 if the buddy is offline
	 * @param visible
	 */
	public BuddyListEntry(String name, String group, int characterId,
			int channel, boolean visible) {
		this.name = name;
		this.group = group;
		this.cid = characterId;
		this.channel = channel;
		this.visible = visible;
	}

	/**
	 * @return the channel the character is on. If the character is offline
	 *         returns -1.
	 */
	public int getChannel() {
		return this.channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public boolean isOnline() {
		return this.channel >= 0;
	}

	public void setOffline() {
		this.channel = -1;
	}

	public String getName() {
		return this.name;
	}

	public String getGroup() {
		return this.group;
	}

	public int getCharacterId() {
		return this.cid;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void changeGroup(String group) {
		this.group = group;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + this.cid;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final BuddyListEntry other = (BuddyListEntry) obj;
		if (this.cid != other.cid) {
			return false;
		}
		return true;
	}
}
