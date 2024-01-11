package com.shreekaram.calllogger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

//https://www.quora.com/How-do-I-add-a-popup-window-over-an-Android-native-incoming-call-screen-like-the-Truecaller-Android-app

public class IncomingCallActivity extends Activity {
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Log.d("IncomingCallActivity: onCreate: ", "flag2");
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            Log.d("IncomingCallActivity: onCreate: ", "flagy");
            setContentView(R.layout.activity_incoming_call);
            Log.d("IncomingCallActivity: onCreate: ", "flagz");
            String number = getIntent().getStringExtra("incomingNumber");
            @SuppressLint({"MissingInflatedId", "LocalSuppress"})
            TextView text = findViewById(R.id.callText);
            text.setText("call with " + number);
        } catch (Exception e) {
            Log.d("Exception", e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}