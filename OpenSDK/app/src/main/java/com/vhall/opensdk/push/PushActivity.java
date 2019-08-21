package com.vhall.opensdk.push;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.framework.connect.VhallConnectService;
import com.vhall.ims.VHIM;
import com.vhall.lss.push.VHLivePusher;
import com.vhall.message.ConnectServer;
import com.vhall.opensdk.R;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.push.IVHCapture;
import com.vhall.push.VHAudioCapture;
import com.vhall.push.VHLivePushConfig;
import com.vhall.push.VHLivePushFormat;
import com.vhall.push.VHVideoCaptureView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    ImageView mChangeFilterBtn;
    Switch openNoise;

    private String roomId = "";
    private String accessToken = "";
    private String mChannelId = "";

    private LinearLayout llContent;
    private EditText et;
    VHIM im;
    private OkHttpClient mClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomId = getIntent().getStringExtra("roomId");
        mChannelId = getIntent().getStringExtra("channelId");
        if (TextUtils.isEmpty(roomId)) {
            roomId = mChannelId;
        }
        accessToken = getIntent().getStringExtra("token");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.push_layout);
        //initview
        mSpeedView = this.findViewById(R.id.tv_speed);
        mLoadingView = this.findViewById(R.id.pb_loading);
        mPushBtn = this.findViewById(R.id.btn_push);
        mAudioBtn = this.findViewById(R.id.btn_changeAudio);
        mFlashBtn = this.findViewById(R.id.btn_changeFlash);
        mChangeFilterBtn = this.findViewById(R.id.btn_changeFilter);
        openNoise = findViewById(R.id.switch_open_noise);
        //配置发直播系列参数
        config = new VHLivePushConfig(VHLivePushFormat.PUSH_MODE_XHD);//Android 仅支持PUSH_MODE_HD(480p)  PUSH_MODE_XHD(720p) PUSH_MODE_XXHD(1080p)
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

        llContent = findViewById(R.id.ll_content);
        et = findViewById(R.id.et);
        et.setOnEditorActionListener(new EditListener());
        im = new VHIM(mChannelId, accessToken);
        im.setOnMessageListener(new MsgListener());
        im.setOnConnectChangedListener(new VhallConnectService.OnConnectStateChangedListener() {
            @Override
            public void onStateChanged(ConnectServer.State state, int serverType) {
                if (serverType == VhallConnectService.SERVER_CHAT) {
                    String text = "";
                    switch (state) {
                        case STATE_CONNECTIONG:
                            text = "连接中";
                            break;
                        case STATE_DISCONNECT:
                            text = "连接失败";
                            break;
                        case STATE_CONNECTED:
                            text = "连接成功！";

                            break;
                    }
                    Toast.makeText(PushActivity.this, "网络：" + text, Toast.LENGTH_SHORT).show();
                }
            }
        });
        im.join();
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
        im.leave();
        im = null;
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
        int level = (++mBeautyLevel) % 5;
        if (level == 0) {
            videoCapture.setFilterEnable(false);
        } else {
            videoCapture.setFilterEnable(true);
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

    class EditListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                im.sendMsg(v.getText().toString(), new VHIM.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.i("IMACt", "success");
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg) {
                        Log.e("imact", "errorCode:" + errorCode + "&errorMsg:" + errorMsg);
                        Toast.makeText(PushActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return true;
        }
    }

    class MsgListener implements VHIM.OnMessageListener {

        @Override
        public void onMessage(String msg) {
            try {
                JSONObject text = new JSONObject(msg);
                String service_type = text.optString("service_type");//服务类型
                if (TextUtils.isEmpty(service_type)) return; //
                String sender_id = text.optString("sender_id");
                String time = text.optString("date_time");
                String context = text.optString("context");
                String dataStr = text.optString("data");
                if (service_type.equals(VHIM.TYPE_CUSTOM)) {//自定义消息处理
                    addView(service_type, "", dataStr, time, "");
                } else {
                    JSONObject data = new JSONObject(dataStr);
                    JSONObject contextObj = new JSONObject(context);
                    String nickName = contextObj.optString("nick_name");
                    if (TextUtils.isEmpty(nickName)) {
                        nickName = sender_id;
                    }
                    String avatar = contextObj.optString("avatar");
                    String textContent = data.optString("text_content");
                    String type = data.optString("type");
                    int onlineNum = text.optInt("uv");
//                Toast.makeText(IMActivity.this, " 当前在线人数 ： " + onlineNum, Toast.LENGTH_SHORT).show();
                    if (service_type.equals(VHIM.TYPE_ONLINE)) {
                        addView(type, nickName, textContent, time, avatar);
                    } else {
                        addView(service_type, nickName, textContent, time, avatar);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addView(String event, String nick_name, String data, String time, String avatar) {
        View view = View.inflate(PushActivity.this, R.layout.im_item_layout, null);
        ImageView v = view.findViewById(R.id.avatar);
        if (llContent.getChildCount() >= 10) {
            View removeView = llContent.getChildAt(0);
            llContent.removeView(removeView);
        }
        if (!TextUtils.isEmpty(avatar) && !avatar.equals("null")) {
            requestAvatar(avatar, v);
        }
        TextView c = view.findViewById(R.id.content);
        TextView t = view.findViewById(R.id.time);
        if (event.equals(VHIM.TYPE_CUSTOM)) {
            c.setText("接收的自定义消息" + nick_name + ": " + data);
        } else if (event.equals(VHIM.TYPE_JOIN)) {
            c.setText(nick_name + ": 上线了");
        } else if (event.equals(VHIM.TYPE_LEAVE)) {
            c.setText(nick_name + ": 下线了");
        } else {
            c.setText(nick_name + ": " + data);
        }
        t.setText(time);
        llContent.addView(view);
    }

    private void requestAvatar(String url, final ImageView view) {
        if (url.startsWith("//")) {
            url = "http:" + url;
        }
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            return;
        }
        Request request = new Request.Builder().url(url).build();
        if (mClient == null)
            mClient = new OkHttpClient();
        Call call = mClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final byte[] picture = response.body().bytes();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setImageBitmap(BitmapFactory.decodeByteArray(picture, 0, picture.length));
                    }
                });
            }
        });
    }
}
