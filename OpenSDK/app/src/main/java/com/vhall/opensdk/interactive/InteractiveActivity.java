package com.vhall.opensdk.interactive;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.vhall.ilss.VHInteractive;
import com.vhall.opensdk.R;

import java.util.List;

public class InteractiveActivity extends FragmentActivity {
    private static final String TAG = "InteractiveActivity";
    String mRoomId;
    String mAccessToken;
    InteractiveFragment mInteractiveFrag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRoomId = getIntent().getStringExtra("channelid");
        mAccessToken = getIntent().getStringExtra("token");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.interactive_layout);
        mInteractiveFrag = InteractiveFragment.getInstance(mRoomId, mAccessToken);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.videoFrame, mInteractiveFrag);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
