package com.example.test;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;

/**
 * Created by Yangming on 2015/4/16.
 */

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

public class MapInfo {
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

    static void setEnableMap(PLAYER_TYPE player_type, boolean enabled) {
        int start = 0, end = battleMap.length, mid = (end - start) / 2;
        if (player_type == PLAYER_TYPE.COM) { // COM
            end = (end - start) / 2;
        } else {
            start = (end - start) / 2 + 1;
        }

        for (int i = start; i < end; i++) {
            for (int j = 0; j < battleMap[0].length; j++) {
                battleMap[i][j].setEnabled(enabled);
            }
        }
    }
}

