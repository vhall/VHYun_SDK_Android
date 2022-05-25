package com.vhall.opensdk.watchlive;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.lss.play.VHFastLivePlayer;
import com.vhall.opensdk.R;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.player.stream.RtcLiveRenderView.VHRtcRenderView;
import com.vhall.player.stream.play.IVHVideoPlayer;

import org.vhwebrtc.RendererCommon;
import org.vhwebrtc.SurfaceViewRenderer;

/**
 * Created by Hank on 2017/11/23.
 */
public class FastLivePlayerActivity extends Activity {

    private static final String TAG = "FastLivePlayerActivity";

    private VHFastLivePlayer mPlayer;
    private VHRtcRenderView mVHRtcRenderView;
    private String roomId = "";
    private String mInavId = "";
    private String accessToken = "";
    int drawmode = IVHVideoPlayer.DRAW_MODE_NONE;
    ImageView mPlayBtn;
    private View mLoadingPB;
    TextView mSpeedTV;
    private TextView mCurStreamTypeView, mTvResolution;
    private final String TYPE_RES_HIGH = "大流";
    private final String TYPE_RES_LOW = "小流";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        roomId = getIntent().getStringExtra("roomId");
        mInavId = getIntent().getStringExtra("inavId");
        accessToken = getIntent().getStringExtra("token");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.page_fastliveplayer);
        mVHRtcRenderView = this.findViewById(R.id.localView);
        mPlayBtn = this.findViewById(R.id.btn_play);
        mLoadingPB = this.findViewById(R.id.pb_loading);
        mSpeedTV = this.findViewById(R.id.tv_speed);
        mCurStreamTypeView = this.findViewById(R.id.btn_changeStream);
        mTvResolution = this.findViewById(R.id.tv_resolution);

        mVHRtcRenderView = findViewById(R.id.localView);
        mVHRtcRenderView.init(null, new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {
            }

            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {
                //LogManager.d("i: "+i+" i1: "+i1+" i2: "+i2);
            }
        });
        mVHRtcRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
        mPlayer = new VHFastLivePlayer.Builder()
                .init(getApplicationContext())
                .connectTimeout(3)
                .reconnectTimes(3)
                .listener(new MyListener())
                .setDisplay(mVHRtcRenderView)
                .build();
        mPlayer.start(roomId, mInavId, accessToken);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer.isPlaying())
            mPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayer.resumeAble())
            mPlayer.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.release();
    }

    private final long INTERVAL_CLICK = 500;
    private long clickTimeMills = 0;
    private long streamTimeMills = 0;

    public void play(View view) {
        long curTime = System.currentTimeMillis();
        if (curTime - clickTimeMills > INTERVAL_CLICK) {
            clickTimeMills = curTime;
            mLoadingPB.setVisibility(View.VISIBLE);
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
                renderStreamType(true, TYPE_RES_HIGH);
            } else {
                if (mPlayer.resumeAble())
                    mPlayer.resume();
                else
                    mPlayer.start(roomId, mInavId, accessToken);
            }
        }
    }

    public void changeStream(View view) {
        long curTime = System.currentTimeMillis();
        if (curTime - streamTimeMills > INTERVAL_CLICK) {
            streamTimeMills = curTime;
            if (mPlayer.isPlaying()) {
                if (mCurStreamTypeView.getText().equals(TYPE_RES_HIGH)) {
                    mPlayer.changeStreamResolution(0);
                    renderStreamType(false, TYPE_RES_LOW);
                } else {
                    mPlayer.changeStreamResolution(1);
                    renderStreamType(false, TYPE_RES_HIGH);
                }
            }
        }
    }

    public void changeMode(View view) {
        if (mPlayer != null && mPlayer.resumeAble()) {
            int mode = ++drawmode % 3;
            mPlayer.setDrawMode(mode);
            toastMode(mode);
        }
    }

    private void toastMode(int mode) {
        String msg = "";
        switch (mode) {
            case 0:
                msg = "双向铺满";
                break;
            case 1:
                msg = "自适应";
                break;
            case 2:
                msg = "高度铺满";
                break;
        }
        Toast.makeText(FastLivePlayerActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private class MyListener implements VHPlayerListener {

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case START:
                    mLoadingPB.setVisibility(View.GONE);
                    showPlayBtnState(true);
                    break;
                case BUFFER:
                    mLoadingPB.setVisibility(View.VISIBLE);
                    break;
                case STOP:
                    mLoadingPB.setVisibility(View.GONE);
                    showPlayBtnState(false);
                    break;
                case END:
                    break;
            }
        }

        private void showPlayBtnState(boolean toPlay) {
            mPlayBtn.setImageResource(toPlay ? R.mipmap.icon_pause_bro : R.mipmap.icon_start_bro);
        }

        @Override
        public void onEvent(int event, String msg) {
            Log.d(TAG, "event: " + event + " - msg: " + msg);
            switch (event) {
                case Constants.Event.EVENT_RTCSTATUS_CONNECTSUCCESS:
                    mCurStreamTypeView.setEnabled(true);
                    break;
                case Constants.Event.EVENT_RTCSTATUS_RECONNECT:
                    if (!TextUtils.isEmpty(msg) && msg.contains("Reconnect： 0")) {
                        mLoadingPB.setVisibility(View.GONE);
                    }
                    break;
                case Constants.Event.EVENT_DPI_LIST:
                    break;
                case Constants.Event.EVENT_DPI_CHANGED:
                    Log.i(TAG, "DPI:" + msg);
                    break;
                case Constants.Event.EVENT_DOWNLOAD_SPEED:
                    break;
                case Constants.Event.EVENT_RTCSTATUS_CHANGERESOLUTION_SUCCESS:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCurStreamTypeView.setEnabled(true);
                            renderResolution();
                        }
                    });
                    break;
                case Constants.Event.EVENT_RTCSTATUS_CHANGERESOLUTION_ERROR:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mCurStreamTypeView.getText().equals(TYPE_RES_HIGH)) {
                                renderStreamType(true, TYPE_RES_LOW);
                            } else {
                                renderStreamType(true, TYPE_RES_HIGH);
                            }
                        }
                    });
                    break;
                case Constants.Event.EVENT_RTCSTATUS_VIDEORESOLUTION_CHANGE:
                    renderResolution();
                    break;
            }

            Toast.makeText(FastLivePlayerActivity.this, msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(int errorCode, int innerCode, String msg) {
            Log.i(TAG, "errorCode:" + errorCode + ",msg:" + msg);

//            mLoadingPB.setVisibility(View.VISIBLE);
            showPlayBtnState(false);

            runOnUiThread(() -> Toast.makeText(FastLivePlayerActivity.this, "Error message:error connect " + msg, Toast.LENGTH_SHORT).show());
            switch (errorCode) {
                case Constants.Event.EVENT_RTCSTATUS_CONNECTSUCCESS:
                    break;
                case Constants.ErrorCode.ERROR_CONNECT:
                case Constants.Event.EVENT_RTCSTATUS_CONNECTERROE:
                case Constants.Event.EVENT_RTCSTATUS_PARMAERROE:
                    renderStreamType(false, TYPE_RES_HIGH);
                    break;
            }
        }
    }

    private void renderStreamType(boolean enable, String type) {
        runOnUiThread(() -> {
            mCurStreamTypeView.setEnabled(enable);
            mCurStreamTypeView.setText(type);
        });
    }

    private void renderResolution() {
        runOnUiThread(() -> {
            if (null != mVHRtcRenderView && null != mVHRtcRenderView.frame) {
                mTvResolution.setText(mVHRtcRenderView.frame.getRotatedWidth() + "X" + mVHRtcRenderView.frame.getRotatedHeight());
            }
        });
    }
}