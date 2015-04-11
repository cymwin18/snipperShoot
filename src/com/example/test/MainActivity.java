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
                                if (fi.getPos().X >= 4) {
                                    // 1. user move.
                                    PlayerInfo.humPlay.moveTo(fi.getPos());
                                    outputLog("User move to " + fi.getPos().toString(), LOG_LEVEL.USER);
                                    // After user move, enable the shoot field.
                                    //Toast.makeText(getApplicationContext(), "Please User Shoot ~", Toast.LENGTH_SHORT).show();
                                    MapInfo.updateField(PLAYER_TYPE.COM, true);
                                    outputLog("Please User Shoot ~~", LOG_LEVEL.SYS);
                                } else {
                                    outputLog("User Shoot " + fi.getPos().toString(), LOG_LEVEL.USER);
                                    if (PlayerInfo.humPlay.shoot(fi.getPos())) {
                                        PlayerInfo.comPlay.setAlive(false);
                                        //Toast.makeText(getApplicationContext(), "COM DIED ~_~", Toast.LENGTH_SHORT).show();
                                        outputLog("GAME OVER: YOU WIN!!! COM DIED!", LOG_LEVEL.IMPORTANT);
                                        gameOver();
                                    } else {
                                        outputLog("COM still Alive.", LOG_LEVEL.SYS);

                                        // COM move
                                        Toast.makeText(getApplicationContext(), "COM play time ~", Toast.LENGTH_SHORT).show();
                                        if (!PlayerInfo.comPlay.canMove()) {
                                            outputLog("GAME OVER: YOU WIN!!! COM CAN'T MOVE!", LOG_LEVEL.IMPORTANT);
                                            gameOver();
                                        } else if (PlayerInfo.comPlay.canMove()) {
                                            PositionInfo tmp = PlayerInfo.comPlay.calcNextMove(); // TODO move logic.
                                            PlayerInfo.comPlay.moveTo(tmp);
                                            outputLog("COM: move to " + tmp.toString(), LOG_LEVEL.SYS);

                                            // COM shoot
                                            PositionInfo shootPos = PlayerInfo.comPlay.aim(); // TODO shoot logic.
                                            outputLog("COM Shoot : " + shootPos.toString(), LOG_LEVEL.SYS);
                                            if (PlayerInfo.comPlay.shoot(shootPos)) {
                                                outputLog("GAME OVER: YOU DIE!!! COM WIN!", LOG_LEVEL.IMPORTANT);
                                                gameOver();
                                            }

                                            outputLog("Please User Move.", LOG_LEVEL.SYS);
                                            Toast.makeText(getApplicationContext(), "Please User Move ~", Toast.LENGTH_SHORT).show();

                                            // Enable User chess.
                                            MapInfo.setupPlayerField();

                                            if (!PlayerInfo.humPlay.canMove()) {
                                                outputLog("GAME OVER: COM WIN!!! YOU CAN'T MOVE!", LOG_LEVEL.IMPORTANT);
                                                gameOver();

                                            }
                                        } else {
                                            outputLog("GAME OVER: YOU WIN!!! COM CAN'T MOVE!", LOG_LEVEL.IMPORTANT);
                                            gameOver();
                                        }
                                    }
                                    MapInfo.updateField(PLAYER_TYPE.COM, false);
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

        // Set up User.
        PlayerInfo.initPlayer();

        // User move/shoot first.
        MapInfo.setupPlayerField();
        // COM field disabled.
        MapInfo.updateField(PLAYER_TYPE.COM, false);

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

        gameStart();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
