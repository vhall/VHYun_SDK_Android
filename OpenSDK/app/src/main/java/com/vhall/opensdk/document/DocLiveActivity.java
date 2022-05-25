package com.vhall.opensdk.document;

import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

/**
 * Created by Hank on 2017/12/18.
 *
 * 文档 + 视频直播
 */
public class DocLiveActivity extends DocActivity {
    private static final String TAG = "DocActivity";
    private VHVideoCaptureView videoCapture;
    private IVHCapture audioCapture;
    private VHLivePusher pusher;
    private VHLivePushConfig config;
    private ImageView btnPlay;
    private ProgressBar mLoadingView;
    private RelativeLayout rlVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();

        //配置发直播系列参数
        config = new VHLivePushConfig(VHLivePushFormat.PUSH_MODE_HD);//Android 仅支持PUSH_MODE_HD(480p)  PUSH_MODE_XXHD(720p)
        config.screenOri = VHLivePushFormat.SCREEN_ORI_PORTRAIT;//横竖屏设置 重要
        //发起流类型设置   STREAM_TYPE_A 音频，STREAM_TYPE_V 视频  STREAM_TYPE_AV 音视频
        config.streamType = VHLivePushFormat.STREAM_TYPE_AV;
        //初始化音视频采集器
        videoCapture = this.findViewById(R.id.video_view);
//        videoCapture.setGestureEnable(false);
        audioCapture = new VHAudioCapture();
        //初始化直播器
        pusher = new VHLivePusher(videoCapture, audioCapture, config);//纯音频推流，视频渲染器传null
        pusher.setListener(new MyListener());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pusher != null && pusher.getState() == Constants.State.START) {
            pusher.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pusher != null) {
            pusher.resume();
        }
    }

    public void push(View view) {
        if (pusher.getState() == Constants.State.START) {
            pusher.pause();
        } else {
            if (pusher.resumeAble())
                pusher.resume();
            else
                pusher.start(mRoomId, mAccessToken);
        }
    }

    public void onVideoClick(View view) {
        if (btnPlay.getVisibility() == VISIBLE) {
            btnPlay.setVisibility(View.GONE);
        } else {
            btnPlay.setVisibility(VISIBLE);
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnPlay.setVisibility(View.GONE);
                }
            }, 3000);
        }
    }

    class MyListener implements VHPlayerListener {

        @Override
        public void onError(int errorCode, int innerErrorCode, String msg) {
            mLoadingView.setVisibility(View.GONE);
            btnPlay.setSelected(false);
            switch (errorCode) {
                case Constants.ErrorCode.ERROR_PUSH://推送过程出错
                    break;
                case Constants.ErrorCode.ERROR_AUDIO_CAPTURE://音频采集过程出错
                    break;
                case Constants.ErrorCode.ERROR_VIDEO_CAPTURE://视频采集过程出错
                    break;
            }
            Toast.makeText(DocLiveActivity.this, "push error,errorCode:" + errorCode + ",innerCode:" + innerErrorCode + ",msg:" + msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case START:
                    mLoadingView.setVisibility(View.GONE);
                    btnPlay.setSelected(true);
                    /**
                     * 重要
                     * 为了保证生成的回放文档播放正常，每次开始推流必需调用下面接口
                     */
                    mDocument.sendSpecial();
                    break;
                case BUFFER:
                    mLoadingView.setVisibility(View.VISIBLE);
                    break;
                case STOP:
                    mLoadingView.setVisibility(View.GONE);
                    btnPlay.setSelected(false);
                    break;
            }
        }

        @Override
        public void onEvent(int eventCode, String eventMsg) {
            switch (eventCode) {
                case Constants.Event.EVENT_UPLOAD_SPEED:
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

    private void initView() {
        rlVideo = findViewById(R.id.rl_video);
        rlVideo.setVisibility(VISIBLE);
        btnPlay = findViewById(R.id.btn_push);
        btnPlay.setVisibility(VISIBLE);
        mLoadingView = findViewById(R.id.pb_loading);
        if (TextUtils.isEmpty(mRoomId)) {
            rlVideo.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pusher.release();
    }
}