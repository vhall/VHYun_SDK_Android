package com.vhall.opensdk.interactive;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.vhall.opensdk.R;
import com.vhall.opensdk.document.DocFragment;
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

    public final static String NODELAY_ACTION_PUSH = "nodelay_action_push";
    public final static String NODELAY_ACTION_WATCH = "nodelay_action_watch";
    private DocFragment mDocFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRoomId = getIntent().getStringExtra("roomId");
        mAccessToken = getIntent().getStringExtra("token");
        String type = getIntent().getStringExtra("type");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.interactive_layout);
        if (TextUtils.equals(type, CAMERA_LIVE)) {
            mInteractiveFrag = InteractiveFragment.getInstance(mRoomId, mAccessToken);
        } else if (TextUtils.equals(type, NODELAY_LIVE)) {
            mInteractiveFrag = InteractiveFragment.getInstance(mRoomId, mAccessToken, true, getIntent().getStringExtra("action"));
        } else if (TextUtils.equals(type, SCREEN_RECORD_LIVE)) {
            mInteractiveFrag = ScreenRecordInteractiveFragment.getInstance(mRoomId, mAccessToken);
        } else {
            mInteractiveFrag = InteractiveFragment.getInstance(mRoomId, mAccessToken);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.videoFrame, mInteractiveFrag);
        transaction.commit();
    }

    public void showDocFragment() {
        if (null == mDocFragment) {
            mDocFragment = DocFragment.getInstance(true);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.anim_show_b2u, R.anim.anim_exit_u2b);
        if (null == getSupportFragmentManager().findFragmentById(R.id.container_doc)) {
            transaction.add(R.id.container_doc, mDocFragment);
        } else {
            transaction.show(mDocFragment);
        }
        transaction.commit();
    }

    public void hideDocFragment() {
        if (null != mDocFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.anim_show_b2u, R.anim.anim_exit_u2b);
            transaction.hide(mDocFragment);
            transaction.commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}