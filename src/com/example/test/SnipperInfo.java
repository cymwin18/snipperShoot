package com.example.test;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.graphics.Color;
import java.util.*;

enum PLAYER_TYPE {
	COM, HUM
};

enum FIELD_TYPE {
	NORMAL, BULLET, WATER
}

class PositionInfo {
	public PositionInfo(int x, int y) {
		super();
		X = x;
		Y = y;
	}
	
	public String toString() {
		return "Pos: (" + X + "," + Y + ")";
	}

	public boolean isNextTo(PositionInfo pi) {
		if ((Math.abs(pi.X - X) == 1 && Math.abs(pi.Y - Y) == 0)
				|| (Math.abs(pi.X - X) == 0 && Math.abs(pi.Y - Y) == 1)) {
			return true;
		}

		return false;
	}

	public int getX() {
		return X;
	}

	public void setX(int x) {
		X = x;
	}

	public int getY() {
		return Y;
	}

	public void setY(int y) {
		Y = y;
	}

	public boolean equal(PositionInfo pi) {
		if (X == pi.X && Y == pi.Y) {
			return true;
		}
		return false;
	}

	public boolean equal(int x, int y) {
		if (X == x && Y == y) {
			return true;
		}
		return false;
	}

	int X;
	int Y;
}

class SnipperInfo {
	public SnipperInfo(int bulletNum, PositionInfo pos, String name,
			PLAYER_TYPE _playerType) {
		super();
		this.bulletNum = bulletNum;
		this.pos = pos;
		this.name = name;
		this.playerType = _playerType;
	}

	int bulletNum = 0;
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
		Log.i("Yangming", "POS is X:" + pos.getX()+ " Y: " + pos.getY());
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
		PositionInfo comPi = PlayerInfo.comPlay.getPos();
		Random r = new Random();
		int n = r.nextInt(4);
		Log.i("Yangming", "n:" + n + " POS: " + comPi.toString());
		
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
		PositionInfo ret = new PositionInfo(r.nextInt(3) + 4, r.nextInt(5));
		
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
		MapInfo.updateField(PLAYER_TYPE.HUM, false);
		// 3. show me
		showMe();
	}
	
	public boolean shoot(PositionInfo _pos) {
		if (PLAYER_TYPE.COM == playerType && PlayerInfo.humPlay.getPos().equal(_pos)) {
			return true;
		}
		
		if (PLAYER_TYPE.HUM == playerType && PlayerInfo.comPlay.getPos().equal(_pos)) {
			return true;
		}
		
		// Disable the shot field.
		MapInfo.battleMap[_pos.getX()][_pos.getY()].setEnabled(false);
		MapInfo.battleMap[_pos.getX()][_pos.getY()].setText("X");
		MapInfo.battleMap[_pos.getX()][_pos.getY()].setShoot(true);
		return false;
	}

	public void showMe() {
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
}

class FieldInfo extends Button {
	public FieldInfo(Context context) {
		super(context);
	}

	public FieldInfo(Context context, PositionInfo pos) {
		super(context);
		this.pos = pos;
		this.setText(getTextFromPos(pos));
		this.setTextColor(getColorFromPos(pos));
	}

	public FIELD_TYPE getType() {
		return type;
	}

	public void setType(FIELD_TYPE type) {
		this.type = type;
	}

	public PositionInfo getPos() {
		return pos;
	}

	public void setPos(PositionInfo pos) {
		this.pos = pos;
	}

	public void resetField(PositionInfo _pos) {
		this.setText(getTextFromPos(_pos));
		this.setTextColor(getColorFromPos(_pos));
	}

	public static String getTextFromPos(int _x, int _y) {
		String ret = " ";
		switch (MapInfo.map[_x][_y]) {
		case 0:
			// ret = _x + ":" + _y;
			break;
		case 2:
			ret = "B";
			break;
		case -1:
			ret = "#";
			break;
		}
		return ret;
	}

	public static String getTextFromPos(PositionInfo pi) {
		return getTextFromPos(pi.X, pi.Y);
	}

	public static int getColorFromPos(PositionInfo pi) {
		int ret = 0;
		switch (MapInfo.map[pi.X][pi.Y]) {
		case 0:
			ret = Color.parseColor("black");
			break;
		case 2:
			ret = Color.parseColor("red");
			break;
		case -1:
			ret = Color.parseColor("yellow");
			break;
		}
		return ret;
	}

	FIELD_TYPE type;
	PositionInfo pos;
	boolean isShoot = false;
	
	public boolean isShoot() {
		return isShoot;
	}

	public void setShoot(boolean isShoot) {
		this.isShoot = isShoot;
	}
}

class MapInfo {
	static int[][] map = new int[][] { { 0, 0, 0, 0, 0 }, { 2, 0, 0, 0, 2 },
			{ 0, 0, 2, 0, 0 }, { -1, -1, -1, -1, -1 }, { 0, 0, 2, 0, 0 },
			{ 2, 0, 0, 0, 2 }, { 0, 0, 0, 0, 0 } };

	static FieldInfo[][] battleMap = new FieldInfo[map.length][map[0].length];

	void init() {
	}

	static void refreshPosition(PositionInfo pi) {
		battleMap[pi.X][pi.Y].resetField(pi);
	}

	static void updateField(PLAYER_TYPE userType, boolean enabled) {
		int start = 0, end = battleMap.length;
		if (userType == PLAYER_TYPE.COM) { // COM
			end = (end - start) / 2;
		} else {
			start = (end - start) / 2;
		}

		for (int i = start; i < end; i++) {
			for (int j = 0; j < battleMap[i].length; j++) {
				battleMap[i][j].setEnabled(enabled);
				// battleMap[i][j].setBackgroundColor(Color.parseColor("#CCCCCC"));
			}
		}
		if (userType == PLAYER_TYPE.COM){
			PlayerInfo.comPlay.showMe();
		} else {
			PlayerInfo.humPlay.showMe();
		}
	}

	static void setupPlayerField() {
		// Disable HUM field, enable COM field.
		// updateField(PLAYER_TYPE.COM, true);
		updateField(PLAYER_TYPE.HUM, false);

		// Enable buttons where user can go.
		int x = PlayerInfo.humPlay.pos.X;
		int y = PlayerInfo.humPlay.pos.Y;
		battleMap[x][y].setEnabled(false);

		if (x + 1 < map.length && !battleMap[x + 1][y].isShoot()) {
			battleMap[x + 1][y].setEnabled(true);
			// battleMap[x+1][y].setBackgroundColor(Color.parseColor("blue"));
		}
		if (x > 1 && x - 1 > (map.length / 2) && !battleMap[x - 1][y].isShoot()) {
			battleMap[x - 1][y].setEnabled(true);
			// battleMap[x-1][y].setBackgroundColor(Color.parseColor("blue"));
		}
		if (y + 1 < map[0].length && !battleMap[x][y + 1].isShoot()) {
			battleMap[x][y + 1].setEnabled(true);
			// battleMap[x][y+1].setBackgroundColor(Color.parseColor("blue"));
		}
		if (y - 1 >= 0 && !battleMap[x][y - 1].isShoot()) {
			battleMap[x][y - 1].setEnabled(true);
			// battleMap[x][y-1].setBackgroundColor(Color.parseColor("blue"));
		}
	}
}

class PlayerInfo {
	public static SnipperInfo comPlay;
	public static SnipperInfo humPlay;
}
