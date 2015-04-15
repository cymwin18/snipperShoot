package com.example.test;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.graphics.Color;
import java.util.*;

enum PLAYER_TYPE {
	COM, HUM
};

enum PLAYER_TURN {
    PLAYER_0, // User down
    PLAYER_1  // User up
}

enum FIELD_TYPE {
	NORMAL, BULLET, WATER
}

enum PLAY_TYPE {
    COMVSHUM,
    HUMVSHUM
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
    private int mShotEffect = 0;

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

	public void resetField() {
        if (isShoot()) {
            setEnabled(false);
        }

		this.setText(getTextFromPos(pos));
		this.setTextColor(getColorFromPos(pos));
	}

	public String getTextFromPos(int _x, int _y) {
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

        if (mShotEffect != 0) {
            ret = "" + mShotEffect;
        }

		return ret;
	}

	public String getTextFromPos(PositionInfo pi) {
		return getTextFromPos(pi.X, pi.Y);
	}

    public String getTextFromPos() { return getTextFromPos(pos); }

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

	
	public boolean isShoot() {
        return (mShotEffect != 0);
    }

	public void setShoot(int shotEffect) {
		this.mShotEffect = shotEffect;
	}

    public void reduceShotEffect() {
        if (mShotEffect != 0) {
            this.mShotEffect--;
        }
    }
}

class MapInfo {
	static int[][] map = new int[][] { { 0, 0, 0, 0, 0 }, { 2, 0, 0, 0, 2 },
            { 0, 0, 2, 0, 0 }, { -1, -1, -1, -1, -1 }, { 0, 0, 2, 0, 0 },
            { 2, 0, 0, 0, 2 }, { 0, 0, 0, 0, 0 } };

        static FieldInfo[][] battleMap = new FieldInfo[map.length][map[0].length];

    void init() {
    }

    static void lockDownMap() {
        for (int i = 0; i < battleMap.length; i++) {
            for (int j = 0; j < battleMap[i].length; j++) {
                battleMap[i][j].setEnabled(false);
            }
        }
    }

    static void refreshPosition(PositionInfo pi) {
        battleMap[pi.X][pi.Y].resetField();
    }

    static void updateField(PLAYER_TURN playerTurn, boolean updateShotEffect) {
        int start = 0, end = battleMap.length, mid = (end - start) / 2;
        if (playerTurn == PLAYER_TURN.PLAYER_0) { // COM
            end = (end - start) / 2;
        } else {
            start = (end - start) / 2;
        }

        for (int i = 0; i < battleMap.length; i++) {
            for (int j = 0; j < battleMap[i].length; j++) {
                battleMap[i][j].resetField();

                if (playerTurn == PLAYER_TURN.PLAYER_0) {
                    if (i >= 0 && i < mid) {
                        // Attack area
                        if (PlayerInfo.player_0.isShootDone()) {
                            battleMap[i][j].setEnabled(false);
                        } else {
                            battleMap[i][j].setEnabled(true);

                            if (i >= start && i < end) {
                                if (Math.abs(j - PlayerInfo.player_0.getPos().getY()) > 1) {
                                    battleMap[i][j].setEnabled(false);
                                }
                            }
                        }
                    } else if (i > mid && i < battleMap.length) {
                        // Move Area
                        battleMap[i][j].setEnabled(false);
                        if (updateShotEffect) {
                            battleMap[i][j].reduceShotEffect();
                            battleMap[i][j].resetField();
                        }
                        if (!PlayerInfo.player_0.isMoveDone()) {
                            setupPlayerField(PlayerInfo.player_0);
                        }
                    }
                }

                if (playerTurn == PLAYER_TURN.PLAYER_1) {
                    if (i >= 0 && i < mid) {
                        battleMap[i][j].setEnabled(false);
                        if (updateShotEffect) {
                            battleMap[i][j].reduceShotEffect();
                            battleMap[i][j].resetField();
                        }
                        if (!PlayerInfo.player_1.isMoveDone()) {
                            setupPlayerField(PlayerInfo.player_1);
                        }
                    } else if (i > mid && i < battleMap.length) {
                        // Attack area
                        if (PlayerInfo.player_1.isShootDone()) {
                            battleMap[i][j].setEnabled(false);
                        } else {
                            battleMap[i][j].setEnabled(true);

                            if (i >= start && i < end) {
                                if (Math.abs(j - PlayerInfo.player_1.getPos().getY()) > 1) {
                                    battleMap[i][j].setEnabled(false);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (playerTurn == PLAYER_TURN.PLAYER_0) {
            PlayerInfo.player_0.showMe();
        } else {
            PlayerInfo.player_1.showMe();
        }
    }

	static void setupPlayerField(SnipperInfo player) {
		// Disable HUM field, enable COM field.
		//updateField(PLAYER_TYPE.HUM, false, false);

		// Enable buttons where user can go.
		int x = player.pos.X;
		int y = player.pos.Y;
		battleMap[x][y].setEnabled(false);

        if (x >  map.length / 2) {
            // Player 0.
            if (x + 1 < map.length && !battleMap[x + 1][y].isShoot()) {
                battleMap[x + 1][y].setEnabled(true);
                // battleMap[x+1][y].setBackgroundColor(Color.parseColor("blue"));
            }

            if (x > 1 && x - 1 > (map.length / 2) && !battleMap[x - 1][y].isShoot()) {
                battleMap[x - 1][y].setEnabled(true);
                // battleMap[x-1][y].setBackgroundColor(Color.parseColor("blue"));
            }
        } else {
            // Player 1.
            if (x + 1 < map.length / 2 - 1 && !battleMap[x + 1][y].isShoot()) {
                battleMap[x + 1][y].setEnabled(true);
                // battleMap[x+1][y].setBackgroundColor(Color.parseColor("blue"));
            }

            if (x > 0 && !battleMap[x - 1][y].isShoot()) {
                battleMap[x - 1][y].setEnabled(true);
                // battleMap[x-1][y].setBackgroundColor(Color.parseColor("blue"));
            }
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
	public static SnipperInfo player_0; // DOWN
    public static SnipperInfo player_1; // UP

    public static void initPlayer(PLAY_TYPE type) {
        if (type == PLAY_TYPE.COMVSHUM) {
            Random r = new Random();
            PlayerInfo.player_1 = new SnipperInfo(3, new PositionInfo(r.nextInt(3), r.nextInt(5)), "COM", PLAYER_TYPE.COM); // Random COM init pos.
        } else {
            PlayerInfo.player_1 = new SnipperInfo(3, new PositionInfo(0, 2), "HUM_1", PLAYER_TYPE.HUM); // User can choose where to born.
        }

		PlayerInfo.player_0 = new SnipperInfo(3, new PositionInfo(6, 2), "HUM_0", PLAYER_TYPE.HUM); // User can choose where to born.
    }
}
