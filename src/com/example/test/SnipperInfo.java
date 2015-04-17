package com.example.test;

import android.util.Log;
import android.graphics.Color;
import java.util.*;

enum PLAYER_TYPE {
	COM, HUM
};

enum PLAYER_TURN {
    PLAYER_0, // User down
    PLAYER_1  // User up
}

enum VS_MODE {
    COMVSHUM,
    HUMVSHUM
}

class SnipperInfo {
	public SnipperInfo(int bulletNum, PositionInfo pos, String name,
			PLAYER_TYPE _playerType) {
		super();
		this.bulletNum = bulletNum;
		this.pos = pos;
		this.name = name;
		this.playerType = _playerType;
        this.movePoint = 2;
	}

	int bulletNum = 0;

    int movePoint = 2;
    private boolean moveDone = false;
    private boolean shootDone = false;

	// int bulletType; // TODO
	PositionInfo pos;
	String name = "YOU";
	boolean isAlive = true;
	PLAYER_TYPE playerType = PLAYER_TYPE.COM;

	public int getBulletNum() {
		return bulletNum;
	}

	public void setBulletNum(int bulletNum) {
		this.bulletNum = bulletNum;
	}

	public PositionInfo getPos() {
		return pos;
	}

	public void setPos(PositionInfo pos) {
		this.pos = pos;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}
	
	public boolean canMove() {
		// Log.i("Yangming", "POS is X:" + pos.getX()+ " Y: " + pos.getY());
		if ((pos.getX() == MapInfo.map.length - 1 || MapInfo.battleMap[pos.getX() + 1][pos.getY()].isShoot()) &&
				(pos.getX() == 0 || MapInfo.battleMap[pos.getX() - 1][pos.getY()].isShoot()) &&
				(pos.getY() == MapInfo.map[0].length - 1 || MapInfo.battleMap[pos.getX()][pos.getY() + 1].isShoot()) &&
				(pos.getY() == 0 || MapInfo.battleMap[pos.getX()][pos.getY() - 1].isShoot())) {
			return false;
		}
		return true;
	}
	
	// Used for COM to get next move.
	// Return null when not a COM player.
	public PositionInfo calcNextMove() {
		if (playerType != PLAYER_TYPE.COM) {
			return null;
		}
        PositionInfo comPi = PlayerInfo.player_1.getPos();
        Random r = new Random();
        int n = r.nextInt(4);

		PositionInfo ret = new PositionInfo(0, 0);
		if (n == 0) { // left
			if (comPi.getY() == 0) {
				ret = new PositionInfo(comPi.getX(), comPi.getY() + 1); 
			} else {
				ret = new PositionInfo(comPi.getX(), comPi.getY() - 1);
			}
		} else if (n == 1) { // right
			if (comPi.getY() == MapInfo.map[0].length - 1) {
				ret = new PositionInfo(comPi.getX(), comPi.getY() - 1); 
			} else {
				ret = new PositionInfo(comPi.getX(), comPi.getY() + 1);
			}
		} else if (n == 2) { // Up
			if (comPi.getX() == 0) {
				ret = new PositionInfo(comPi.getX()+1, comPi.getY()); 
			} else {
				ret = new PositionInfo(comPi.getX()-1, comPi.getY());
			}
		} else if (n == 3) {
			if (comPi.getX() == (MapInfo.map.length / 2 - 1)) {
				ret = new PositionInfo(comPi.getX()-1, comPi.getY()); 
			} else {
				ret = new PositionInfo(comPi.getX()+1, comPi.getY());
			}
		}
		
		Log.i("Yangming", "here 0" + ret.toString());
		if (MapInfo.battleMap[ret.getX()][ret.getY()].isShoot()) {
			Log.i("Yangming", "here" + ret.toString());
			ret = calcNextMove();
		}
		
		return ret;
	}

	public PositionInfo aim() {
		Random r = new Random();
        int y = pos.getY() - 1 + r.nextInt(3);
        if (y < 0) {
            y = 0;
        }
        if (y >= MapInfo.battleMap[0].length) {
            y = MapInfo.battleMap[0].length - 1;
        }
		PositionInfo ret = new PositionInfo(r.nextInt(3) + 4, y);
		
		if (MapInfo.battleMap[ret.getX()][ret.getY()].isShoot()) {
			return aim();
		}
		
		return ret;
	}
	
	public void moveTo(PositionInfo _pos) {
		if (!_pos.isNextTo(pos)) {
			return;
		}

		// 1. setBack the Field.
		MapInfo.refreshPosition(pos);
		// 2. move to new place.
		setPos(_pos);
		//
        PLAYER_TURN turn = (pos.getX() > MapInfo.battleMap.length / 2) ? PLAYER_TURN.PLAYER_0 : PLAYER_TURN.PLAYER_1;
		MapInfo.updateField(turn, false);
		// 3. show me
		if (playerType == PLAYER_TYPE.HUM) {
			showMe();
		}

        setMoveDone(true);
	}
	
	public boolean shoot(PositionInfo _pos) {
		if (PlayerInfo.player_0.getPos().equal(_pos)) {
			return true;
		}
		
		if (PlayerInfo.player_1.getPos().equal(_pos)) {
			return true;
		}
		
		// Disable the shot field.
		MapInfo.battleMap[_pos.getX()][_pos.getY()].setEnabled(false);
		MapInfo.battleMap[_pos.getX()][_pos.getY()].setText("X");
		MapInfo.battleMap[_pos.getX()][_pos.getY()].setShoot(3); // Normal bullet will last 3 round.

        setShootDone(true);

		return false;
	}

	public void showMe() {
        if (playerType == PLAYER_TYPE.COM) {
            return;
        }

        String me = "";
		int color = Color.parseColor("blue");
		if (isAlive == false) {
			me = "D";
			color = Color.parseColor("red");
		} else {
			if (playerType == PLAYER_TYPE.COM) { // COM
				me = "C";
			} else if (playerType == PLAYER_TYPE.HUM) {
				me = "H";
			}
		}

		MapInfo.battleMap[pos.X][pos.Y].setText(me);
		MapInfo.battleMap[pos.X][pos.Y].setTextColor(color);
	}

    public boolean isMoveDone() {
        return moveDone;
    }

    public void setMoveDone(boolean moveDone) {
        this.moveDone = moveDone;
    }

    public boolean isShootDone() {
        return shootDone;
    }

    public void setShootDone(boolean shootDone) {
        this.shootDone = shootDone;
    }
}

class PlayerInfo {
	public static SnipperInfo player_0; // DOWN
    public static SnipperInfo player_1; // UP

    public static void initPlayer(VS_MODE type) {
        if (type == VS_MODE.COMVSHUM) {
            Random r = new Random();
            PlayerInfo.player_1 = new SnipperInfo(3, new PositionInfo(r.nextInt(3), r.nextInt(5)), "COM", PLAYER_TYPE.COM); // Random COM init pos.
        } else {
            PlayerInfo.player_1 = new SnipperInfo(3, new PositionInfo(0, 2), "HUM_1", PLAYER_TYPE.HUM); // User can choose where to born.
        }

		PlayerInfo.player_0 = new SnipperInfo(3, new PositionInfo(6, 2), "HUM_0", PLAYER_TYPE.HUM); // User can choose where to born.
    }
}
