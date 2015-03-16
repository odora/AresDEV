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
package scripting.quest;

import scripting.npc.NPCConversationManager;
import server.quest.MapleQuest;
import client.MapleClient;

/**
 *
 * @author RMZero213
 */
public class QuestActionManager extends NPCConversationManager {
	private final boolean start; // this is if the script in question is start
									// or end
	private final int quest;

	public QuestActionManager(MapleClient c, int quest, int npc, boolean start) {
		super(c, npc);
		this.quest = quest;
		this.start = start;
	}

	public int getQuest() {
		return this.quest;
	}

	public boolean isStart() {
		return this.start;
	}

	@Override
	public void dispose() {
		QuestScriptManager.getInstance().dispose(this, this.getClient());
	}

	public boolean forceStartQuest() {
		return this.forceStartQuest(this.quest);
	}

	public boolean forceStartQuest(int id) {
		return MapleQuest.getInstance(id).forceStart(this.getPlayer(),
				this.getNpc());
	}

	public boolean forceCompleteQuest() {
		return this.forceCompleteQuest(this.quest);
	}

	public boolean forceCompleteQuest(int id) {
		return MapleQuest.getInstance(id).forceComplete(this.getPlayer(),
				this.getNpc());
	}
}
