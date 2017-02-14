package com.zmtmt.zhibohao;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class GuideActivity extends Activity {

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent=new Intent(GuideActivity.this,IndexActivity.class);
            startActivity(intent);
            finish();
        }
    };
    Message msg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
            msg=Message.obtain();
            mHandler.sendMessageDelayed(msg,3000);
    }
}