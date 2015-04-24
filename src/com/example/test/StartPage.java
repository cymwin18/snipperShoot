package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.example.test.MainActivity;
import com.example.test.R;

import java.net.UnknownHostException;

/**
 * Created by cymwin18 on 4/22/15.
 */
public class StartPage extends Activity {
    void sendVsMode(String vsMode) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("VSMODE", vsMode);
        startActivity(intent);
    }

    // TODO: for Hum vs hum mode, need to create a socket server.

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.startpage);

        final RadioButton btnVsCom = (RadioButton) findViewById(R.id.vsCom_btn);
        final RadioButton btnVsHum = (RadioButton) findViewById(R.id.vsHum_btn);

        final EditText ipAddr_et = (EditText) findViewById(R.id.ipAddr_et);
        ipAddr_et.setVisibility(View.INVISIBLE);

        btnVsCom.setChecked(true);

        btnVsCom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btnVsCom.setChecked(true);
                btnVsHum.setChecked(false);
            }
        });

        btnVsHum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btnVsCom.setChecked(false);
                btnVsHum.setChecked(true);

                java.net.InetAddress test = null;
                String ipAddr = Utils.getWIFILocalIpAdress(getApplicationContext());
                ipAddr_et.setText(ipAddr);
                ipAddr_et.setVisibility(View.VISIBLE);
            }
        });

        Button btnStart = (Button) findViewById(R.id.Start_btn);
        btnStart.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (btnVsCom.isChecked()) {
                    sendVsMode("HUMVSCOM");
                } else {
                    sendVsMode("HUMVSHUM");
                }
            }
        });
    }
}

