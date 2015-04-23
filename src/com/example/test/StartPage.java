package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.test.MainActivity;
import com.example.test.R;

/**
 * Created by cymwin18 on 4/22/15.
 */
public class StartPage extends Activity {
    void sendVsMode(String vsMode) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("VSMODE", vsMode);
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.startpage);

        Button btnVsCom = (Button) findViewById(R.id.vsCom_btn);
        btnVsCom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendVsMode("HUMVSCOM");
            }
        });

        Button btnVsHum = (Button) findViewById(R.id.vsHum_btn);
        btnVsHum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendVsMode("HUMVSHUM");
            }
        });
    }
}