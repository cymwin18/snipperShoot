package com.example.test;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window; 
import android.view.WindowManager;
import android.view.View;
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
	};
	
	private LinearLayout mOutputlist;
	
	private void outputLog(String msg, LOG_LEVEL level) {
		TextView outputlog = new TextView(this);
		outputlog.setText(msg);
		switch (level) {
		case USER:
			outputlog.setTextColor(Color.parseColor("black"));
			break;
		case SYS:
			outputlog.setTextColor(Color.parseColor("blue"));
			break;
		case IMPORTANT:
			outputlog.setTextColor(Color.parseColor("red"));
			break;
		}
		mOutputlist.addView(outputlog);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,  
                WindowManager.LayoutParams. FLAG_FULLSCREEN); 
		setContentView(R.layout.activity_main);
		
		TableLayout table = (TableLayout) findViewById(R.id.maptable);
		table.setStretchAllColumns(true); 
		
		 mOutputlist = (LinearLayout) findViewById(R.id.outputlist);
		
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
				        		Toast.makeText(getApplicationContext(), "Please User Shoot ~", Toast.LENGTH_SHORT).show();
				        		MapInfo.updateField(PLAYER_TYPE.COM, true);
				        		outputLog("Please User Shoot ~~", LOG_LEVEL.SYS);
				        	} else {
				        		outputLog("User Shoot " + fi.getPos().toString(), LOG_LEVEL.USER);
				        		if (PlayerInfo.humPlay.shoot(fi.getPos())) {
				        			PlayerInfo.comPlay.setAlive(false);
				        			Toast.makeText(getApplicationContext(), "COM DIED ~_~", Toast.LENGTH_SHORT).show();
				        			outputLog("GAME OVER: YOU WIN!!! COM DIED!", LOG_LEVEL.IMPORTANT);
				        		} else {
				        			outputLog("COM still Alive.", LOG_LEVEL.SYS);
				        			
				        			// COM move
				        			Toast.makeText(getApplicationContext(), "COM play time ~", Toast.LENGTH_SHORT).show();
				        			if (!PlayerInfo.comPlay.canMove()) {
				        				outputLog("GAME OVER: YOU WIN!!! COM CAN'T MOVE!", LOG_LEVEL.IMPORTANT);
				        			} else {
				        				PositionInfo tmp = PlayerInfo.comPlay.calcNextMove(); // TODO move logic.
				        			    PlayerInfo.comPlay.moveTo(tmp);
				        			    outputLog("COM: move to " + tmp.toString(), LOG_LEVEL.SYS);
				        			
				        			    // COM shoot
				        			    PositionInfo shootPos = PlayerInfo.comPlay.aim(); // TODO shoot logic.
				        			    outputLog("COM Shoot : " + shootPos.toString(), LOG_LEVEL.SYS);
				        			    if (PlayerInfo.comPlay.shoot(shootPos)) {
				        				    outputLog("GAME OVER: YOU DIE!!! COM WIN!", LOG_LEVEL.IMPORTANT);
				        			    } 
				        			
				        			    outputLog("Please User Move.", LOG_LEVEL.SYS);
				        			    Toast.makeText(getApplicationContext(), "Please User Move ~", Toast.LENGTH_SHORT).show();

				        			    // Enable User chess.
				        			    MapInfo.setupPlayerField();
				        			    
				        			    if (!PlayerInfo.humPlay.canMove()) {
				        			    	outputLog("GAME OVER: COM WIN!!! YOU CAN'T MOVE!", LOG_LEVEL.IMPORTANT);
				        			    }
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
		
		// 1. Set up User.
		PlayerInfo.comPlay = new SnipperInfo(3, new PositionInfo(0, 0), "COM", PLAYER_TYPE.COM);
		PlayerInfo.humPlay = new SnipperInfo(3, new PositionInfo(6, 2), "YOU", PLAYER_TYPE.HUM);
		
		// User move/shoot first.
		MapInfo.setupPlayerField();
		// COM field disabled.
		MapInfo.updateField(PLAYER_TYPE.COM, false);
		
		Toast.makeText(getApplicationContext(), "Game Start!!", Toast.LENGTH_SHORT).show();
		Toast.makeText(getApplicationContext(), "Please User Move ~", Toast.LENGTH_SHORT).show();
		
		outputLog("Game Start!!", LOG_LEVEL.SYS);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
