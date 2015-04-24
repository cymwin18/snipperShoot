package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.test.MainActivity;
import com.example.test.R;

import java.net.UnknownHostException;

/**
 * Created by cymwin18 on 4/22/15.
 */

public class StartPage extends Activity {

    public static String ipAddr_local = null;

    public static VS_MODE vsMode = VS_MODE.COMVSHUM;

    public static final String[] connMode = {"Create New Game","Connect to Game"};

    void sendVsMode(VS_MODE vsMode) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("VSMODE", vsMode == VS_MODE.HUMVSHUM ? 1 : 0);
        startActivity(intent);
    }

    enum GAME_TYPE {
        SERVER,
        CLIENT
    }

    GAME_TYPE mGameType = GAME_TYPE.SERVER;

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.startpage);

        final RadioButton btnVsCom = (RadioButton) findViewById(R.id.vsCom_btn);
        final RadioButton btnVsHum = (RadioButton) findViewById(R.id.vsHum_btn);

        final EditText ipAddr_et = (EditText) findViewById(R.id.ipAddr_et);
        final TextView ip_tv = (TextView) findViewById(R.id.ip_textview);

        final Spinner connMode_spin = (Spinner) findViewById(R.id.spinner_connMode);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,connMode);
        connMode_spin.setAdapter(adapter);

        connMode_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View view, int i, long l) {
                if (i == 0) {
                    // Create new game.
                    mGameType = GAME_TYPE.SERVER;
                    if (ipAddr_local == null) {
                        ipAddr_local = Utils.getWIFILocalIpAdress(getApplicationContext());
                    }
                    ipAddr_et.setText(ipAddr_local);
                    ipAddr_et.setEnabled(false);
                } else {
                    // Join an existed game.
                    ipAddr_et.setText("");
                    ipAddr_et.setEnabled(true);
                    mGameType = GAME_TYPE.CLIENT;
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ipAddr_et.setVisibility(View.GONE);
        ip_tv.setVisibility(View.GONE);
        connMode_spin.setVisibility(View.GONE);

        btnVsCom.setChecked(true);

        // PLAY VS COM
        btnVsCom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btnVsCom.setChecked(true);
                btnVsHum.setChecked(false);
                ipAddr_et.setVisibility(View.GONE);
                ip_tv.setVisibility(View.GONE);
                connMode_spin.setVisibility(View.GONE);

                vsMode = VS_MODE.COMVSHUM;
            }
        });

        // PLAY VS HUM
        btnVsHum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btnVsCom.setChecked(false);
                btnVsHum.setChecked(true);

                java.net.InetAddress test = null;
                if (ipAddr_local == null) {
                    ipAddr_local = Utils.getWIFILocalIpAdress(getApplicationContext());
                }

                ipAddr_et.setText(ipAddr_local);
                ipAddr_et.setVisibility(View.VISIBLE);
                ip_tv.setVisibility(View.VISIBLE);
                connMode_spin.setVisibility(View.VISIBLE);

                vsMode = VS_MODE.HUMVSHUM;
            }
        });

        Button btnStart = (Button) findViewById(R.id.Start_btn);
        btnStart.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.i("Yangming", "mode is " + vsMode);
                if (vsMode == VS_MODE.HUMVSHUM) {
                    if (mGameType == GAME_TYPE.SERVER) {
                        // TODO: Create the socket server.
                    } else {
                        // TODO: Join an exist server.
                    }
                }
                sendVsMode(vsMode);
            }
        });
    }
}


