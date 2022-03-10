package com.vhall.opensdk.interactive;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.WindowManager;

import com.vhall.opensdk.R;
import com.vhall.opensdk.interactive.screenrecord.ScreenRecordInteractiveFragment;

public class InteractiveActivity extends FragmentActivity {
    private static final String TAG = "InteractiveActivity";
    String mRoomId;
    String mAccessToken;
    Fragment mInteractiveFrag;
    //相机直播
    public final static String CAMERA_LIVE = "camera_live";
    //录屏直播
    public final static String SCREEN_RECORD_LIVE = "screen_record_live";
    //无延迟直播
    public final static String NODELAY_LIVE = "ondelay_live";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRoomId = getIntent().getStringExtra("roomId");
        mAccessToken = getIntent().getStringExtra("token");
        String type = getIntent().getStringExtra("type");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.interactive_layout);
        if(TextUtils.equals(type,CAMERA_LIVE)){
            mInteractiveFrag = InteractiveFragment.getInstance(mRoomId, mAccessToken);
        }else if(TextUtils.equals(type,NODELAY_LIVE)){
            mInteractiveFrag = InteractiveFragment.getInstance(mRoomId, mAccessToken,true);
        }else if(TextUtils.equals(type,SCREEN_RECORD_LIVE)){
            mInteractiveFrag = ScreenRecordInteractiveFragment.getInstance(mRoomId, mAccessToken);
        }else{
            mInteractiveFrag = InteractiveFragment.getInstance(mRoomId, mAccessToken);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.videoFrame, mInteractiveFrag);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
