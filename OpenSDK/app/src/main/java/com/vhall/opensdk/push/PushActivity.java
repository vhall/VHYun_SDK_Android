package com.vhall.opensdk.push;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.lss.push.VHLivePusher;
import com.vhall.opensdk.R;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.push.IVHCapture;
import com.vhall.push.VHAudioCapture;
import com.vhall.push.VHLivePushConfig;
import com.vhall.push.VHLivePushFormat;
import com.vhall.push.VHVideoCaptureView;
import com.vhall.push.renderer.filter.VHBeautyFilter;

/**
 * Created by Hank on 2017/11/13.
 */
public class PushActivity extends Activity {

    private static final String TAG = "PushActivity";

    VHVideoCaptureView videoCapture;
    IVHCapture audioCapture;
    VHLivePusher pusher;
    VHLivePushConfig config;
    //status info
    boolean isFlashOpen = false;
    int mCameraId = 0;
    int mBeautyLevel = 0;
    boolean isAudioEnable = true;
    int mDrawMode = VHLivePushFormat.DRAW_MODE_NONE;
    //view
    TextView mSpeedView;
    ProgressBar mLoadingView;
    ImageView mPushBtn;
    ImageView mAudioBtn;
    ImageView mFlashBtn;

    private String roomId = "";
    private String accessToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomId = getIntent().getStringExtra("channelid");
        accessToken = getIntent().getStringExtra("token");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.push_layout);
        //initview
        mSpeedView = this.findViewById(R.id.tv_speed);
        mLoadingView = this.findViewById(R.id.pb_loading);
        mPushBtn = this.findViewById(R.id.btn_push);
        mAudioBtn = this.findViewById(R.id.btn_changeAudio);
        mFlashBtn = this.findViewById(R.id.btn_changeFlash);
        //配置发直播系列参数
        config = new VHLivePushConfig(VHLivePushFormat.PUSH_MODE_HD);//Android 仅支持PUSH_MODE_HD(480p)  PUSH_MODE_XXHD(720p)
        config.screenOri = VHLivePushFormat.SCREEN_ORI_PORTRAIT;//横竖屏设置 重要
        //发起流类型设置   STREAM_TYPE_A 音频，STREAM_TYPE_V 视频  STREAM_TYPE_AV 音视频
        config.streamType = VHLivePushFormat.STREAM_TYPE_AV;
        //初始化音视频采集器
        videoCapture = this.findViewById(R.id.videoCaptureView);
        audioCapture = new VHAudioCapture();
        //初始化直播器
        pusher = new VHLivePusher(videoCapture, audioCapture, config);//纯音频推流，视频渲染器传null
        pusher.setListener(new MyListener());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pusher.getState() == Constants.State.START) {
            pusher.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        pusher.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pusher.release();
    }

    public void push(View view) {
        if (pusher.getState() == Constants.State.START) {
            pusher.pause();
        } else {
            if (pusher.resumeAble())
                pusher.resume();
            else
                pusher.start(roomId, accessToken);
        }

    }

    public void changeFlash(View view) {
        isFlashOpen = videoCapture.changeFlash(!isFlashOpen);
        if (isFlashOpen) {
            mFlashBtn.setImageResource(R.mipmap.img_round_flash_open);
        } else {
            mFlashBtn.setImageResource(R.mipmap.img_round_flash_close);
        }
    }

    public void changeCamera(View view) {
        mCameraId = videoCapture.switchCamera();
        isFlashOpen = false;
        mFlashBtn.setImageResource(R.mipmap.img_round_audio_close);
    }


    public void changeFilter(View view) {
        int level = (++mBeautyLevel) % 6;
        if (level == 0)
            videoCapture.setFilter(null);
        else {
            videoCapture.setFilter(new VHBeautyFilter());
            videoCapture.setBeautyLevel(level);
        }
        Toast.makeText(this, "level:" + level, Toast.LENGTH_SHORT).show();
    }

    public void switchAudio(View view) {
        isAudioEnable = audioCapture.setEnable(!isAudioEnable);
        if (isAudioEnable) {
            mAudioBtn.setImageResource(R.mipmap.img_round_audio_open);
        } else {
            mAudioBtn.setImageResource(R.mipmap.img_round_audio_close);
        }
    }

    public void changeMode(View view) {
        switch (mDrawMode) {
            case VHLivePushFormat.DRAW_MODE_NONE:
                mDrawMode = VHLivePushFormat.DRAW_MODE_ASPECTFILL;
                break;
            case VHLivePushFormat.DRAW_MODE_ASPECTFILL:
                mDrawMode = VHLivePushFormat.DRAW_MODE_ASPECTFIT;
                break;
            case VHLivePushFormat.DRAW_MODE_ASPECTFIT:
                mDrawMode = VHLivePushFormat.DRAW_MODE_NONE;
                break;
        }
        videoCapture.setCameraDrawMode(mDrawMode);
    }

    class MyListener implements VHPlayerListener {

        @Override
        public void onError(int errorCode, int innerErrorCode, String msg) {
            mLoadingView.setVisibility(View.GONE);
            mPushBtn.setImageResource(R.mipmap.icon_start_bro);
            switch (errorCode) {
                case Constants.ErrorCode.ERROR_PUSH://推送过程出错
                    break;
                case Constants.ErrorCode.ERROR_AUDIO_CAPTURE://音频采集过程出错
                    break;
                case Constants.ErrorCode.ERROR_VIDEO_CAPTURE://视频采集过程出错
                    break;
            }
            Toast.makeText(PushActivity.this, "push error,errorCode:" + errorCode + ",innerCode:" + innerErrorCode + ",msg:" + msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case START:
                    mLoadingView.setVisibility(View.GONE);
                    mPushBtn.setImageResource(R.mipmap.icon_pause_bro);
                    break;
                case STOP:
                    mLoadingView.setVisibility(View.GONE);
                    mPushBtn.setImageResource(R.mipmap.icon_start_bro);
                    break;
            }
        }

        @Override
        public void onEvent(int eventCode, String eventMsg) {
            switch (eventCode) {
                case Constants.Event.EVENT_STATUS_STARTING:
                    mLoadingView.setVisibility(View.VISIBLE);
                    break;
                case Constants.Event.EVENT_UPLOAD_SPEED:
                    mSpeedView.setText(eventMsg + "kbps");
                    break;
            }
        }
    }
}
