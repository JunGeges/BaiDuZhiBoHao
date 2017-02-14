package com.zmtmt.zhibohao;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zmtmt.zhibohao.entity.ShareInfo;
import com.zmtmt.zhibohao.tools.ShareUtils;

public class endActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView mTextViewTime,mTextViewPersons;
    private Button mButtonConfirm;
    private ImageView mImageViewWX,mImageViewPyq;
    private final int WXSceneTimeline = 1;
    private final int WXSceneSession = 2;
    private ShareInfo mShareInfo;
    private String mLivetime;
    private String mLivepersons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        getData();
        initViews();
    }

    public void getData() {
        Intent intent = getIntent();
        mLivetime = intent.getStringExtra("livetime");
        mLivepersons = intent.getStringExtra("livepersons");
        mShareInfo=intent.getParcelableExtra("shareinfo");
    }

    private void initViews() {
        mTextViewTime=(TextView)findViewById(R.id.end_tv_live_time);
        mTextViewPersons=(TextView)findViewById(R.id.end_tv_persons);
        mButtonConfirm=(Button)findViewById(R.id.end_btn_confirm);
        mButtonConfirm.setOnClickListener(this);
        mImageViewPyq=(ImageView)findViewById(R.id.end_iv_pyq);
        mImageViewWX=(ImageView)findViewById(R.id.end_iv_wx);
        mImageViewPyq.setOnClickListener(this);
        mImageViewWX.setOnClickListener(this);
        mTextViewTime.setText(mLivetime);
        mTextViewPersons.setText(mLivepersons);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.end_btn_confirm:
                finish();
                break;

            case R.id.end_iv_wx:
                ShareUtils.shareToWX(mShareInfo,WXSceneSession);
                break;

            case R.id.end_iv_pyq:
                ShareUtils.shareToWX(mShareInfo,WXSceneTimeline);
                break;
        }
    }
}
