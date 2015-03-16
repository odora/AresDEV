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
package server.life;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import tools.ArrayMap;
import tools.Randomizer;
import client.MapleCharacter;
import client.MapleDisease;
import client.status.MonsterStatus;

/**
 *
 * @author Danny (Leifde)
 */
public class MobSkill {
	private final int skillId, skillLevel;
	private int mpCon;
	private final List<Integer> toSummon = new ArrayList<Integer>();
	private int spawnEffect, hp, x, y;
	private long duration, cooltime;
	private float prop;
	private Point lt, rb;
	private int limit;

	public MobSkill(int skillId, int level) {
		this.skillId = skillId;
		this.skillLevel = level;
	}

	public void setMpCon(int mpCon) {
		this.mpCon = mpCon;
	}

	public void addSummons(List<Integer> toSummon) {
		for (final Integer summon : toSummon) {
			this.toSummon.add(summon);
		}
	}

	public void setSpawnEffect(int spawnEffect) {
		this.spawnEffect = spawnEffect;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setCoolTime(long cooltime) {
		this.cooltime = cooltime;
	}

	public void setProp(float prop) {
		this.prop = prop;
	}

	public void setLtRb(Point lt, Point rb) {
		this.lt = lt;
		this.rb = rb;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public void applyEffect(MapleCharacter player, MapleMonster monster,
			boolean skill) {
		MapleDisease disease = null;
		final Map<MonsterStatus, Integer> stats = new ArrayMap<MonsterStatus, Integer>();
		final List<Integer> reflection = new LinkedList<Integer>();
		switch (this.skillId) {
		case 100:
		case 110:
		case 150:
			stats.put(MonsterStatus.WEAPON_ATTACK_UP, Integer.valueOf(this.x));
			break;
		case 101:
		case 111:
		case 151:
			stats.put(MonsterStatus.MAGIC_ATTACK_UP, Integer.valueOf(this.x));
			break;
		case 102:
		case 112:
		case 152:
			stats.put(MonsterStatus.WEAPON_DEFENSE_UP, Integer.valueOf(this.x));
			break;
		case 103:
		case 113:
		case 153:
			stats.put(MonsterStatus.MAGIC_DEFENSE_UP, Integer.valueOf(this.x));
			break;
		case 114:
			if ((this.lt != null) && (this.rb != null) && skill) {
				final List<MapleMapObject> objects = this.getObjectsInRange(
						monster, MapleMapObjectType.MONSTER);
				final int hps = (this.getX() / 1000)
						* (int) (950 + (1050 * Math.random()));
				for (final MapleMapObject mons : objects) {
					((MapleMonster) mons).heal(hps, this.getY());
				}
			} else {
				monster.heal(this.getX(), this.getY());
			}
			break;
		case 120:
			disease = MapleDisease.SEAL;
			break;
		case 121:
			disease = MapleDisease.DARKNESS;
			break;
		case 122:
			disease = MapleDisease.WEAKEN;
			break;
		case 123:
			disease = MapleDisease.STUN;
			break;
		case 124:
			disease = MapleDisease.CURSE;
			break;
		case 125:
			disease = MapleDisease.POISON;
			break;
		case 126: // Slow
			disease = MapleDisease.SLOW;
			break;
		case 127:
			if ((this.lt != null) && (this.rb != null) && skill) {
				for (final MapleCharacter character : this.getPlayersInRange(
						monster, player)) {
					character.dispel();
				}
			} else {
				player.dispel();
			}
			break;
		case 128: // Seduce
			disease = MapleDisease.SEDUCE;
			break;
		case 129: // Banish
			if ((this.lt != null) && (this.rb != null) && skill) {
				for (final MapleCharacter chr : this.getPlayersInRange(monster,
						player)) {
					chr.changeMapBanish(monster.getBanish().getMap(), monster
							.getBanish().getPortal(), monster.getBanish()
							.getMsg());
				}
			} else {
				player.changeMapBanish(monster.getBanish().getMap(), monster
						.getBanish().getPortal(), monster.getBanish().getMsg());
			}
			break;
		case 131: // Mist
			monster.getMap().spawnMist(
					new MapleMist(this.calculateBoundingBox(
							monster.getPosition(), true), monster, this),
					this.x * 10, false, false);
			break;
		case 132:
			disease = MapleDisease.CONFUSE;
			break;
		case 133: // zombify
			break;
		case 140:
			if (this.makeChanceResult()
					&& !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
				stats.put(MonsterStatus.WEAPON_IMMUNITY,
						Integer.valueOf(this.x));
			}
			break;
		case 141:
			if (this.makeChanceResult()
					&& !monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY)) {
				stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(this.x));
			}
			break;
		case 143: // Weapon Reflect
			stats.put(MonsterStatus.WEAPON_REFLECT, Integer.valueOf(this.x));
			stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(this.x));
			reflection.add(this.x);
			break;
		case 144: // Magic Reflect
			stats.put(MonsterStatus.MAGIC_REFLECT, Integer.valueOf(this.x));
			stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(this.x));
			reflection.add(this.x);
			break;
		case 145: // Weapon / Magic reflect
			stats.put(MonsterStatus.WEAPON_REFLECT, Integer.valueOf(this.x));
			stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(this.x));
			stats.put(MonsterStatus.MAGIC_REFLECT, Integer.valueOf(this.x));
			stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(this.x));
			reflection.add(this.x);
			break;
		case 154: // accuracy up
		case 155: // avoid up
		case 156: // speed up
			break;
		case 200:
			if (monster.getMap().getSpawnedMonstersOnMap() < 80) {
				for (final Integer mobId : this.getSummons()) {
					final MapleMonster toSpawn = MapleLifeFactory
							.getMonster(mobId);
					toSpawn.setPosition(monster.getPosition());
					int ypos, xpos;
					xpos = (int) monster.getPosition().getX();
					ypos = (int) monster.getPosition().getY();
					switch (mobId) {
					case 8500003: // Pap bomb high
						toSpawn.setFh((int) Math.ceil(Math.random() * 19.0));
						ypos = -590;
						break;
					case 8500004: // Pap bomb
						xpos = (int) ((monster.getPosition().getX() + Randomizer
								.nextInt(1000)) - 500);
						if (ypos != -590) {
							ypos = (int) monster.getPosition().getY();
						}
						break;
					case 8510100: // Pianus bomb
						if (Math.ceil(Math.random() * 5) == 1) {
							ypos = 78;
							xpos = Randomizer.nextInt(5)
									+ (Randomizer.nextInt(2) == 1 ? 180 : 0);
						} else {
							xpos = (int) ((monster.getPosition().getX() + Randomizer
									.nextInt(1000)) - 500);
						}
						break;
					}
					switch (monster.getMap().getId()) {
					case 220080001: // Pap map
						if (xpos < -890) {
							xpos = (int) (Math.ceil(Math.random() * 150) - 890);
						} else if (xpos > 230) {
							xpos = (int) (230 - Math.ceil(Math.random() * 150));
						}
						break;
					case 230040420: // Pianus map
						if (xpos < -239) {
							xpos = (int) (Math.ceil(Math.random() * 150) - 239);
						} else if (xpos > 371) {
							xpos = (int) (371 - Math.ceil(Math.random() * 150));
						}
						break;
					}
					toSpawn.setPosition(new Point(xpos, ypos));
					monster.getMap().spawnMonsterWithEffect(toSpawn,
							this.getSpawnEffect(), toSpawn.getPosition());
				}
			}
			break;
		}
		if (stats.size() > 0) {
			if ((this.lt != null) && (this.rb != null) && skill) {
				for (final MapleMapObject mons : this.getObjectsInRange(
						monster, MapleMapObjectType.MONSTER)) {
					((MapleMonster) mons).applyMonsterBuff(stats, this.getX(),
							this.getSkillId(), this.getDuration(), this,
							reflection);
				}
			} else {
				monster.applyMonsterBuff(stats, this.getX(), this.getSkillId(),
						this.getDuration(), this, reflection);
			}
		}
		if (disease != null) {
			if ((this.lt != null) && (this.rb != null) && skill) {
				int i = 0;
				for (final MapleCharacter character : this.getPlayersInRange(
						monster, player)) {
					if (!character.isActiveBuffedValue(2321005)) {
						if (disease.equals(MapleDisease.SEDUCE)) {
							if (i < 10) {
								character.giveDebuff(MapleDisease.SEDUCE, this);
								i++;
							}
						} else {
							character.giveDebuff(disease, this);
						}
					}
				}
			} else {
				player.giveDebuff(disease, this);
			}
		}
		monster.usedSkill(this.skillId, this.skillLevel, this.cooltime);
		monster.setMp(monster.getMp() - this.getMpCon());
	}

	private List<MapleCharacter> getPlayersInRange(MapleMonster monster,
			MapleCharacter player) {
		final List<MapleCharacter> players = new ArrayList<MapleCharacter>();
		players.add(player);
		return monster.getMap().getPlayersInRange(
				this.calculateBoundingBox(monster.getPosition(),
						monster.isFacingLeft()), players);
	}

	public int getSkillId() {
		return this.skillId;
	}

	public int getSkillLevel() {
		return this.skillLevel;
	}

	public int getMpCon() {
		return this.mpCon;
	}

	public List<Integer> getSummons() {
		return Collections.unmodifiableList(this.toSummon);
	}

	public int getSpawnEffect() {
		return this.spawnEffect;
	}

	public int getHP() {
		return this.hp;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public long getDuration() {
		return this.duration;
	}

	public long getCoolTime() {
		return this.cooltime;
	}

	public Point getLt() {
		return this.lt;
	}

	public Point getRb() {
		return this.rb;
	}

	public int getLimit() {
		return this.limit;
	}

	public boolean makeChanceResult() {
		return (this.prop == 1.0) || (Math.random() < this.prop);
	}

	private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
		final int multiplier = facingLeft ? 1 : -1;
		final Point mylt = new Point((this.lt.x * multiplier) + posFrom.x,
				this.lt.y + posFrom.y);
		final Point myrb = new Point((this.rb.x * multiplier) + posFrom.x,
				this.rb.y + posFrom.y);
		return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
	}

	private List<MapleMapObject> getObjectsInRange(MapleMonster monster,
			MapleMapObjectType objectType) {
		final List<MapleMapObjectType> objectTypes = new ArrayList<MapleMapObjectType>();
		objectTypes.add(objectType);
		return monster.getMap().getMapObjectsInBox(
				this.calculateBoundingBox(monster.getPosition(),
						monster.isFacingLeft()), objectTypes);
	}
}
