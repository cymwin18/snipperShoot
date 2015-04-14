package com.example.test;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window; 
import android.view.WindowManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.graphics.Color;

public class MainActivity extends Activity {
	public enum LOG_LEVEL {
		USER,
		SYS,
		IMPORTANT
	}


    private static PLAY_TYPE mPlayType = PLAY_TYPE.COMVSHUM;

    private PLAYER_TURN mPlayTurn =  PLAYER_TURN.PLAYER_0;

	private void outputLog(String msg, LOG_LEVEL level) {
        LinearLayout outList = (LinearLayout) findViewById(R.id.outputlist);
        TextView infoText = (TextView) findViewById(R.id.info_text);

        int color = 0;
		TextView outLog = new TextView(this);
		outLog.setText(msg);
		switch (level) {
		case USER:
            color = Color.parseColor("black");
			break;
		case SYS:
            color = Color.parseColor("blue");
			break;
		case IMPORTANT:
            color = Color.parseColor("red");
			break;
		}
        outLog.setTextColor(color);
		outList.addView(outLog);

        if (level == LOG_LEVEL.SYS || level == LOG_LEVEL.IMPORTANT) {
            if (level == LOG_LEVEL.IMPORTANT) {
                infoText.setTextColor(color);
            }
            infoText.setText(msg);
        }
	}

    private void resetLog() {
        LinearLayout outList = (LinearLayout) findViewById(R.id.outputlist);
        outList.removeAllViews();
    }

    private void initGameMap() {
        TableLayout table = (TableLayout) findViewById(R.id.maptable);
        table.setStretchAllColumns(true);
        table.removeAllViews();

        // 1. Set up chess table.
        for (int i = 0; i < MapInfo.map.length; i++) {
            TableRow tablerow = new TableRow(this);
            for (int j = 0; j < MapInfo.map[i].length; j++) {
                MapInfo.battleMap[i][j] = new FieldInfo(this, new PositionInfo(i, j));

                switch (MapInfo.map[i][j]) {
                    case -1:
                        MapInfo.battleMap[i][j].setEnabled(false);
                        break;
                    case 2:
                    default:
                        MapInfo.battleMap[i][j].setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                FieldInfo fi = (FieldInfo) v;
                                if (fi.getPos().X > (MapInfo.battleMap.length / 2)) {
                                    if (mPlayTurn == PLAYER_TURN.PLAYER_0) {
                                        // 1. hum player 0 move.
                                        PlayerInfo.player_0.moveTo(fi.getPos());
                                        outputLog("User move to " + fi.getPos().toString(), LOG_LEVEL.USER);
                                        // After user move, enable the shoot field.
                                        //Toast.makeText(getApplicationContext(), "Please User Shoot ~", Toast.LENGTH_SHORT).show();
                                        MapInfo.updateField(mPlayTurn, false);
                                        outputLog("Please User Shoot ~~", LOG_LEVEL.SYS);
                                    }
                                    if (mPlayTurn == PLAYER_TURN.PLAYER_1) {
                                        outputLog("Player 1 Shoot", LOG_LEVEL.SYS);
                                        // hum player 1 shoot.
                                        if (PlayerInfo.player_1.shoot(fi.getPos())) {
                                            PlayerInfo.player_0.setAlive(false);
                                            outputLog("GAME OVER: PLAYER 1 WIN!!! PLAYER 0 DIED!", LOG_LEVEL.IMPORTANT);
                                            MapInfo.updateField(mPlayTurn, false);
                                            gameOver();
                                        } else {
                                            MapInfo.updateField(mPlayTurn, false);
                                        }
                                    }

                                } else {
                                    if (mPlayTurn == PLAYER_TURN.PLAYER_0) {
                                        outputLog("User Shoot " + fi.getPos().toString(), LOG_LEVEL.USER);
                                        if (PlayerInfo.player_0.shoot(fi.getPos())) {
                                            PlayerInfo.player_1.setAlive(false);
                                            //Toast.makeText(getApplicationContext(), "COM DIED ~_~", Toast.LENGTH_SHORT).show();
                                            outputLog("GAME OVER: YOU WIN!!! COM DIED!", LOG_LEVEL.IMPORTANT);
                                            MapInfo.updateField(mPlayTurn, false);
                                            gameOver();
                                        } else {
                                            outputLog("PLAYER 2 still Alive.", LOG_LEVEL.SYS);
                                            MapInfo.updateField(mPlayTurn, false);
                                        }
                                    }

                                    if (mPlayTurn == PLAYER_TURN.PLAYER_1) {
                                        PlayerInfo.player_1.moveTo(fi.getPos());
                                        outputLog("User move to " + fi.getPos().toString(), LOG_LEVEL.USER);
                                        MapInfo.updateField(mPlayTurn, false);
                                    }
                                }
                            }
                        });
                        break;
                }

                MapInfo.battleMap[i][j].setPos(new PositionInfo(i, j));

                tablerow.addView(MapInfo.battleMap[i][j]);
            }
            table.addView(tablerow);
        }
    }

    private void gameStart() {
        // init map.
        initGameMap();

        mPlayTurn =  PLAYER_TURN.PLAYER_0;

        // Set up User.
        PlayerInfo.initPlayer(mPlayType);

        // COM field disabled.
        MapInfo.updateField(PLAYER_TURN.PLAYER_0, false);

        resetLog();

        outputLog("Game Start!!", LOG_LEVEL.SYS);
    }

    private void gameOver() {
        Button btnRestart = (Button) findViewById(R.id.btn_restart);
        MapInfo.lockDownMap();
        btnRestart.setEnabled(true);
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

        final Button btnRestart = (Button) findViewById(R.id.btn_restart);
        // This button should ,
        // be disabled first.
        btnRestart.setEnabled(false);
        btnRestart.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                gameStart();
                btnRestart.setEnabled(false);
            }
        });

        final Button btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setEnabled(true);
        btnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mPlayTurn == PLAYER_TURN.PLAYER_0) {
                    PlayerInfo.player_0.setMoveDone(false);
                    PlayerInfo.player_0.setShootDone(false);
                    mPlayTurn = PLAYER_TURN.PLAYER_1;
                } else {
                    PlayerInfo.player_1.setMoveDone(false);
                    PlayerInfo.player_1.setShootDone(false);
                    mPlayTurn = PLAYER_TURN.PLAYER_0;
                }


                if (mPlayType == PLAY_TYPE.COMVSHUM) {
                    outputLog("COM still Alive.", LOG_LEVEL.SYS);

                    // COM move
                    Toast.makeText(getApplicationContext(), "COM play time ~", Toast.LENGTH_SHORT).show();
                    if (!PlayerInfo.player_1.canMove()) {
                        outputLog("GAME OVER: YOU WIN!!! COM CAN'T MOVE!", LOG_LEVEL.IMPORTANT);
                        gameOver();
                    } else if (PlayerInfo.player_1.canMove()) {
                        PositionInfo tmp = PlayerInfo.player_1.calcNextMove(); // TODO move logic.
                        PlayerInfo.player_1.moveTo(tmp);
                        outputLog("COM: move to " + tmp.toString(), LOG_LEVEL.SYS);

                        // COM shoot
                        PositionInfo shootPos = PlayerInfo.player_1.aim(); // TODO shoot logic.
                        outputLog("COM Shoot : " + shootPos.toString(), LOG_LEVEL.SYS);
                        if (PlayerInfo.player_1.shoot(shootPos)) {
                            outputLog("GAME OVER: YOU DIE!!! COM WIN!", LOG_LEVEL.IMPORTANT);
                            gameOver();
                        }

                        outputLog("Please User Move.", LOG_LEVEL.SYS);
                        Toast.makeText(getApplicationContext(), "Please User Move ~", Toast.LENGTH_SHORT).show();

                        // Enable User chess.
                        if (!PlayerInfo.player_0.canMove()) {
                            outputLog("GAME OVER: COM WIN!!! YOU CAN'T MOVE!", LOG_LEVEL.IMPORTANT);
                            gameOver();
                        }

                    } else {
                        outputLog("GAME OVER: YOU WIN!!! COM CAN'T MOVE!", LOG_LEVEL.IMPORTANT);
                        gameOver();
                    }

                    mPlayTurn = PLAYER_TURN.PLAYER_0;
                }

                MapInfo.updateField(mPlayTurn, true);
            }
        });

        final Button btn_vsmode = (Button) findViewById(R.id.btn_vsmode);
        btn_vsmode.setEnabled(true);
        btn_vsmode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayType = (mPlayType == PLAY_TYPE.COMVSHUM) ? PLAY_TYPE.HUMVSHUM : PLAY_TYPE.COMVSHUM;
                String text = (mPlayType != PLAY_TYPE.COMVSHUM) ? "Num VS Hum" : "Com VS HUM";
                btn_vsmode.setText(text);

                gameStart();
            }
        });

        gameStart();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
