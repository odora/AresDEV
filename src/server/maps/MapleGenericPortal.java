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
package server.maps;

import java.awt.Point;

import scripting.portal.PortalScriptManager;
import server.MaplePortal;
import tools.MaplePacketCreator;
import client.MapleClient;

public class MapleGenericPortal implements MaplePortal {

	private String name;
	private String target;
	private Point position;
	private int targetmap;
	private final int type;
	private boolean status = true;
	private int id;
	private String scriptName;
	private boolean portalState;

	public MapleGenericPortal(int type) {
		this.type = type;
	}

	@Override
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Point getPosition() {
		return this.position;
	}

	@Override
	public String getTarget() {
		return this.target;
	}

	@Override
	public void setPortalStatus(boolean newStatus) {
		this.status = newStatus;
	}

	@Override
	public boolean getPortalStatus() {
		return this.status;
	}

	@Override
	public int getTargetMapId() {
		return this.targetmap;
	}

	@Override
	public int getType() {
		return this.type;
	}

	@Override
	public String getScriptName() {
		return this.scriptName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPosition(Point position) {
		this.position = position;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setTargetMapId(int targetmapid) {
		this.targetmap = targetmapid;
	}

	@Override
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	@Override
	public void enterPortal(MapleClient c) {
		boolean changed = false;
		if (this.getScriptName() != null) {
			changed = PortalScriptManager.getInstance().executePortalScript(
					this, c);
		} else if (this.getTargetMapId() != 999999999) {
			final MapleMap to = c.getPlayer().getEventInstance() == null ? c
					.getChannelServer().getMapFactory()
					.getMap(this.getTargetMapId()) : c.getPlayer()
					.getEventInstance().getMapInstance(this.getTargetMapId());
			MaplePortal pto = to.getPortal(this.getTarget());
			if (pto == null) {// fallback for missing portals - no real life
								// case anymore - intresting for not implemented
								// areas
				pto = to.getPortal(0);
			}
			c.getPlayer().changeMap(to, pto); // late resolving makes this
												// harder but prevents us from
												// loading the whole world at
												// once
			changed = true;
		}
		if (!changed) {
			c.announce(MaplePacketCreator.enableActions());
		}
	}

	@Override
	public void setPortalState(boolean state) {
		this.portalState = state;
	}

	@Override
	public boolean getPortalState() {
		return this.portalState;
	}
}
