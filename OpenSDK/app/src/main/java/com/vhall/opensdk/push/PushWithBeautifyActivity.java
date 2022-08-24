package com.vhall.opensdk.push;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.vhall.beautify.VHBeautifyKit;
import com.vhall.beautifykit.control.FaceBeautyControlView;
import com.vhall.lss.push.VHLivePusher;
import com.vhall.opensdk.ConfigActivity;
import com.vhall.opensdk.R;
import com.vhall.opensdk.beautysource.FaceBeautyDataFactory;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.push.IVHCapture;
import com.vhall.push.VHAudioCapture;
import com.vhall.push.VHLivePushConfig;
import com.vhall.push.VHLivePushFormat;
import com.vhall.push.VHVideoCaptureView;

import okhttp3.OkHttpClient;

/**
 * @author：Jooper Email：jooperge@163.com
 * 描述：基于vhall-beautify-kit的美颜交互demo，数据集和配置更新由相芯封装好的FURender处理
 * 修改历史:
 * <p>
 * 创建于： 2021/12/15
 */
public class PushWithBeautifyActivity extends FragmentActivity {

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
    ImageView mChangeFilterBtn;
    Switch openNoise;

    private String roomId = "";
    private String accessToken = "";
    private String mChannelId = "";
    private OkHttpClient mClient;

    private FaceBeautyControlView mFaceBeautyControlView;
    private FaceBeautyDataFactory mFaceBeautyDataFactory;
    public static boolean needBindDataFactory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(getIntent().getIntExtra("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));

        roomId = getIntent().getStringExtra("roomId");
        mChannelId = getIntent().getStringExtra("channelId");
        if (TextUtils.isEmpty(roomId)) {
            roomId = mChannelId;
        }
        accessToken = getIntent().getStringExtra("token");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.push_layout_beautify);
        //initview
        mSpeedView = this.findViewById(R.id.tv_speed);
        mLoadingView = this.findViewById(R.id.pb_loading);
        mPushBtn = this.findViewById(R.id.btn_push);
        mAudioBtn = this.findViewById(R.id.btn_changeAudio);
        mFlashBtn = this.findViewById(R.id.btn_changeFlash);
        mChangeFilterBtn = this.findViewById(R.id.btn_changeFilter);
        openNoise = findViewById(R.id.switch_open_noise);

        SharedPreferences configSp = this.getSharedPreferences("config", MODE_PRIVATE);
        int configDef = configSp.getInt(ConfigActivity.KEY_PIX_TYPE, VHLivePushFormat.PUSH_MODE_XHD);
        Log.e(TAG, ">>>>>> " + configDef);

        //配置发直播系列参数
        config = new VHLivePushConfig(configDef);//Android 仅支持PUSH_MODE_HD(480p)  PUSH_MODE_XHD(720p) PUSH_MODE_XXHD(1080p)
        config.screenOri = VHLivePushFormat.SCREEN_ORI_PORTRAIT;//横竖屏设置 重要
        //发起流类型设置   STREAM_TYPE_A 音频，STREAM_TYPE_V 视频  STREAM_TYPE_AV 音视频
        config.streamType = VHLivePushFormat.STREAM_TYPE_AV;
        //初始化音视频采集器
        videoCapture = this.findViewById(R.id.videoCaptureView);
        videoCapture.setCameraDrawMode(VHLivePushFormat.DRAW_MODE_ASPECTFILL);
        audioCapture = new VHAudioCapture();
        //初始化直播器
        pusher = new VHLivePusher(videoCapture, audioCapture, config);//纯音频推流，视频渲染器传null
        pusher.setListener(new MyListener());

        openNoise.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //开始推流后设置生效
                pusher.openNoiseCancelling(isChecked);
            }
        });

        videoCapture.switchCamera();

        initBeautifyData();

        ((Switch) findViewById(R.id.switch_beautify)).setOnCheckedChangeListener((button, isChecked) -> {
            if (VHBeautifyKit.getInstance().isVHallBeautify()) {
                Toast.makeText(this, "高级美颜未开通或打包未选择美颜flavor", Toast.LENGTH_SHORT).show();
            } else {
                VHBeautifyKit.getInstance().setBeautifyEnable(isChecked);
            }
        });
    }

    private void initBeautifyData() {
        mFaceBeautyControlView = findViewById(R.id.faceBeautyControlView);
        mFaceBeautyControlView.setVisibility(View.VISIBLE);
        mFaceBeautyDataFactory = new FaceBeautyDataFactory(mFaceBeautyListener);
        mFaceBeautyControlView.bindDataFactory(mFaceBeautyDataFactory);
        mFaceBeautyControlView.setOnBottomAnimatorChangeListener(showRate -> {
        });
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

        if (needBindDataFactory) {
            mFaceBeautyControlView.bindDataFactory(mFaceBeautyDataFactory);
            needBindDataFactory = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mFaceBeautyControlView.hideControlView();
        return super.onTouchEvent(event);
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
            Toast.makeText(PushWithBeautifyActivity.this, "push error,errorCode:" + errorCode + ",innerCode:" + innerErrorCode + ",msg:" + msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case START:
                    mLoadingView.setVisibility(View.GONE);
                    mPushBtn.setImageResource(R.mipmap.icon_pause_bro);

                    break;
                case BUFFER:
                    mLoadingView.setVisibility(View.VISIBLE);
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
                case Constants.Event.EVENT_UPLOAD_SPEED:
                    //上传速率kbps
                    mSpeedView.setText(eventMsg + "kbps");
                    break;
                case Constants.Event.EVENT_NETWORK_UNOBS:
                    //网络恢复
                    mLoadingView.setVisibility(View.VISIBLE);
                    break;
                case Constants.Event.EVENT_NETWORK_OBS:
                    //网络阻塞
                    mLoadingView.setVisibility(View.GONE);
                    break;
            }
        }
    }

    FaceBeautyDataFactory.FaceBeautyListener mFaceBeautyListener = new FaceBeautyDataFactory.FaceBeautyListener() {

        @Override
        public void onFilterSelected(int res) {
            Toast.makeText(PushWithBeautifyActivity.this, getString(res), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFaceBeautyEnable(boolean enable) {
            ((Switch) findViewById(R.id.switch_beautify)).setChecked(enable);
            VHBeautifyKit.getInstance().setBeautifyEnable(enable);
        }
    };
}