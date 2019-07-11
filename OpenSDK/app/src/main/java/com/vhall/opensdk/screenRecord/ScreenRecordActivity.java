package com.vhall.opensdk.screenRecord;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.slss.VHScreenRecordService;
import com.vhall.slss.VHScreenRecorder;
import com.vhall.opensdk.R;
import com.vhall.push.VHLivePushConfig;
import com.vhall.push.VHLivePushFormat;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zwp on 2019/5/29
 */
public class ScreenRecordActivity extends Activity {
    private static final String TAG = "ScreenRecordActivity";

    boolean isRecording = false;
    ScreenRecordReceiver recordReceiver;

    public String token;
    public String roomId;
    private MediaProjectionManager mMediaProjectionManager;
    private VHLivePushConfig config = null;
    ScreenService screenRecordService;
    private static final int REQUEST_CODE = 1;


    Button btnRecord;
    TextView tvSpeed;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            VHScreenRecordService.ScreenRecordServiceBinder binder = (VHScreenRecordService.ScreenRecordServiceBinder) service;
            screenRecordService = (ScreenService) binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_record_layout);
        Log.e(TAG, "onCreate: ");
        roomId = getIntent().getStringExtra("channelid");
        token = getIntent().getStringExtra("token");

        btnRecord = findViewById(R.id.btn_record);
        tvSpeed = findViewById(R.id.tv_speed);

        Intent serviceIntent = new Intent(this, ScreenService.class);
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

        config = new VHLivePushConfig(VHLivePushFormat.PUSH_MODE_XXHD);
        config.screenOri = VHLivePushFormat.SCREEN_ORI_LANDSPACE;
        config.videoBitrate = 800*1000;
        config.pushReconnectTimes = 15;

        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        registerReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
        setBtnText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        unbindService(connection);
        unregisterReceiver();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult: requestCode=" + requestCode);
        MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection == null) {
            Log.e(TAG, "onActivityResult: media projection is null");
            return;
        }
        //初始化SDK录屏操作
        VHScreenRecorder mScreenRecorder = new VHScreenRecorder(config.videoWidth, config.videoHeight, 1, mediaProjection, config.videoFrameRate, null);
        screenRecordService.startConnection(roomId, token, config, mScreenRecorder);

//        moveTaskToBack(true);
    }

    public void onScreenRecordPush(View view) {
        if (isRecording) {
            screenRecordService.stopConnection();
        } else {
            Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_CODE);
        }
    }

    class ScreenRecordReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case 0://开始推流
                    isRecording = true;
                    Toast.makeText(context, "Screen recorder is running...", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onReceive: Screen recorder is running..." );
                    break;
                case 1://结束推流
                    isRecording = false;
                    Toast.makeText(context, "Screen recorder is stop", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onReceive: Screen recorder is stop" );
                    break;
                case 2://推流速率
                    String content = intent.getStringExtra("msg");
                    tvSpeed.setText(content + "kbps");
                    Log.d(TAG, "onReceive: 上传速度：" + content + "kbps");
                    break;
                case 3://错误信息反馈
                    isRecording = false;
                    String msg = intent.getStringExtra("msg");
                    try {
                        JSONObject obj = new JSONObject(msg);
                        int errorCode = obj.optInt("errorCode", -1);
                        String errorMsg = obj.optString("errorMsg", "");
                        Toast.makeText(context, "errorCode=" + errorCode + "--->errorMsg:" + errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onReceive: errorCode="+errorCode +"errorMsg="+errorMsg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            setBtnText();
        }
    }

    private void registerReceiver() {
        recordReceiver = new ScreenRecordReceiver();
        IntentFilter filter = new IntentFilter(VHScreenRecordService.BROADCAST_ACTION);
        registerReceiver(recordReceiver, filter);
    }

    private void unregisterReceiver() {
        unregisterReceiver(recordReceiver);
    }

    private void setBtnText() {
        if (isRecording) {
            btnRecord.setText("结束录屏直播");
        } else {
            btnRecord.setText("开始录屏直播");
        }
    }
}