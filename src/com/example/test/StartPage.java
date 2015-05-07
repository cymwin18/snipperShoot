package com.example.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.Toast;

/**
 * Created by cymwin18 on 4/22/15.
 */

public class StartPage extends Activity {
    public class GameServerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.fancyidea.GameServer.ServerCreated")) {
            } else if (action.equals("com.fancyidea.GameServer.ServerConnect")){
            }
        }
    }

    private static final String TAG = "Yangming";
    public static String ipAddr_local = null;
    public static String ipClientAddr_local = null;
    public static VS_MODE vsMode = VS_MODE.COMVSHUM;

    public static final String[] connMode = {"Create New Game","Connect to Game"};

    private IGameServer mGameService;
    enum GAME_TYPE {
        SERVER,
        CLIENT;
    }

    void sendVsMode(VS_MODE vsMode) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("VSMODE", vsMode == VS_MODE.HUMVSHUM ? 1 : 0);
        startActivity(intent);
    }

    GAME_TYPE mGameType = GAME_TYPE.SERVER;

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.startpage);

        Log.i(TAG, "Yangming onCreate");
        // Bind to GameServer
        Intent intent = new Intent(StartPage.this, GameServer.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        // Button init.
        final RadioButton btnVsCom = (RadioButton) findViewById(R.id.vsCom_btn);
        final RadioButton btnVsHum = (RadioButton) findViewById(R.id.vsHum_btn);

        final EditText ipAddr_et = (EditText) findViewById(R.id.ipAddr_et);
        final TextView ip_tv = (TextView) findViewById(R.id.ip_textview);

        final EditText ipclient_Addr_et = (EditText) findViewById(R.id.ipAddr_et_clientip);
        final TextView ipclient_tv = (TextView) findViewById(R.id.ip_textview_client);

        ipclient_Addr_et.setVisibility(View.GONE);
        ipclient_tv.setVisibility(View.GONE);

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

                    ipclient_Addr_et.setVisibility(View.VISIBLE);
                    ipclient_Addr_et.setEnabled(false);
                    ipclient_Addr_et.setText(ipClientAddr_local);
                    ipclient_tv.setVisibility(View.VISIBLE);
                } else {
                    // Join an existed game.
                    ipAddr_et.setText("");
                    ipAddr_et.setEnabled(true);
                    mGameType = GAME_TYPE.CLIENT;


                    ipclient_tv.setVisibility(View.GONE);
                    ipclient_Addr_et.setVisibility(View.GONE);
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ipAddr_et.setVisibility(View.GONE);
        ip_tv.setVisibility(View.GONE);
        ipclient_Addr_et.setVisibility(View.GONE);
        ipclient_tv.setVisibility(View.GONE);
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
                ipclient_Addr_et.setVisibility(View.GONE);
                ipclient_tv.setVisibility(View.GONE);

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
                if (vsMode == VS_MODE.HUMVSHUM) {
                    if (mGameType == GAME_TYPE.SERVER) {
                        // TODO: Create the socket server.
                        if (ipAddr_local != null) {
                            mGameService.startSocketServer(ipAddr_local);
                            Log.i(TAG, "Yangming create");
                        }
                    } else {
                        // TODO: Join an exist server.
                        if (ipAddr_local != null) {
                            mGameService.connSocketServer(ipAddr_local);
                        }
                    }
                }
                sendVsMode(vsMode);
            }
        });
    }

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mGameService = (IGameServer)service;
            Log.i(TAG, "Yangming onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mGameService = null;
            Log.i(TAG, "Yangming onServiceDisconnected");
        }
    };

    protected void onDestory() {
        super.onDestroy();
        unbindService(conn);
        Log.i(TAG, "Yangming onDestory");
    }
}



