package com.vhall.opensdk.im;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.framework.VhallSDK;
import com.vhall.framework.connect.VhallConnectService;
import com.vhall.ims.VHIM;
import com.vhall.lss.play.VHLivePlayer;
import com.vhall.message.ConnectServer;
import com.vhall.opensdk.R;
import com.vhall.opensdk.util.KeyBoardManager;
import com.vhall.opensdk.widget.ChangeUserInfoDialog;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.player.stream.play.impl.VHVideoPlayerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Hank on 2017/12/20.
 */
public class IMActivity extends Activity {

    private String mChannelId = "";
    private String mAccessToken = "";
    private String mRoomId = "";
    private LinearLayout ll_content;
    private EditText et;
    VHIM im;
    private OkHttpClient mClient;
    private VHLivePlayer mPlayer;
    private VHVideoPlayerView mVideoPlayer;
    private ProgressBar mLoadingPB;
    private ImageView mPlayBtn;
    private ChangeUserInfoDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRoomId = getIntent().getStringExtra("roomId");
        mChannelId = getIntent().getStringExtra("channelId");
        mAccessToken = getIntent().getStringExtra("token");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.im_layout);
        ll_content = this.findViewById(R.id.ll_content);
        et = this.findViewById(R.id.et);
        et.setOnEditorActionListener(new EditListener());
        im = new VHIM(mChannelId, mAccessToken);
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
                    Toast.makeText(IMActivity.this, "网络：" + text, Toast.LENGTH_SHORT).show();
                }
            }
        });
        im.join();

        mLoadingPB = findViewById(R.id.pb_loading);
        mPlayBtn = findViewById(R.id.btn_play);
        mVideoPlayer = findViewById(R.id.vh_video_player);
        mPlayer = new VHLivePlayer.Builder()
                .videoPlayer(mVideoPlayer)
                .listener(new MyListener())
                .build();

    }

    public void updateUserInfo(View view) {
        if(dialog == null){
            dialog = new ChangeUserInfoDialog(this);
        }
        dialog.show();
    }

    class MyListener implements VHPlayerListener {

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case START:
                    mLoadingPB.setVisibility(View.GONE);
                    mPlayBtn.setImageResource(R.mipmap.icon_pause_bro);
                    break;
                case BUFFER:
                    mLoadingPB.setVisibility(View.VISIBLE);
                    break;
                case STOP:
                case END:
                    mLoadingPB.setVisibility(View.GONE);
                    mPlayBtn.setImageResource(R.mipmap.icon_start_bro);
                    break;

            }
        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case Constants.Event.EVENT_DPI_LIST:

                    break;
                case Constants.Event.EVENT_DPI_CHANGED:

                    break;
                case Constants.Event.EVENT_URL:

                    break;
                case Constants.Event.EVENT_DOWNLOAD_SPEED:
                    break;
                case Constants.Event.EVENT_VIDEO_SIZE_CHANGED:
                    break;
                case Constants.Event.EVENT_STREAM_START://发起端发起
//                    if (mPlayer.isPlaying()) {
//                        return;
//                    }
//                    Toast.makeText(IMActivity.this, "主播开始推流", Toast.LENGTH_SHORT).show();
//                    if (mPlayer.resumeAble())
//                        mPlayer.resume();
                    break;
                case Constants.Event.EVENT_STREAM_STOP://发起端停止
//                    if (!mPlayer.isPlaying()) {
//                        return;
//                    }
//                    Toast.makeText(IMActivity.this, "主播停止推流", Toast.LENGTH_SHORT).show();
//                    mPlayer.pause();
                    break;
                case Constants.Event.EVENT_NO_STREAM://暂未开始直播
//                    Toast.makeText(IMActivity.this, "主播端暂未推流", Toast.LENGTH_SHORT).show();
                    break;
            }


        }

        @Override
        public void onError(int errorCode, int innerCode, String msg) {
            mLoadingPB.setVisibility(View.GONE);
            switch (errorCode) {
                case Constants.ErrorCode.ERROR_CONNECT:
                    mLoadingPB.setVisibility(View.GONE);
                    mPlayBtn.setImageResource(R.mipmap.icon_start_bro);
                    Toast.makeText(IMActivity.this, "Error message:error connect " + msg, Toast.LENGTH_SHORT).show();
                    break;

            }
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
        im.leave();
        im = null;
        mPlayer.release();
        super.onDestroy();
    }

    public void play(View view) {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        } else {
            if (mPlayer.resumeAble()) {
                mPlayer.resume();
            } else {
                if (!TextUtils.isEmpty(mRoomId)) {
                    mPlayer.start(mRoomId, mAccessToken);
                } else {
                    Toast.makeText(this, "roomId is null", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class EditListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                im.sendMsg(v.getText().toString(),VHIM.TYPE_TEXT,buildContext(), new VHIM.Callback() {
                    @Override
                    public void onSuccess() {
                        et.setText("");
                        KeyBoardManager.closeKeyboard(et,IMActivity.this);
                        Log.i("IMACt", "success");
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg) {
                        KeyBoardManager.closeKeyboard(et,IMActivity.this);
                        Log.e("imact", "errorCode:" + errorCode + "&errorMsg:" + errorMsg);
                        Toast.makeText(IMActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return true;
        }
    }


    private String buildContext(){
        String userInfo = VhallSDK.getInstance().getmUserInfo();
        JSONObject context = new JSONObject();
        try {
            if(!TextUtils.isEmpty(userInfo)){
                context = new JSONObject(userInfo);
            }
            context.put("txt","test");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return context.toString();
    }

    class MsgListener implements VHIM.OnMessageListener {

        @Override
        public void onMessage(String msg) {
            Log.e("vhall_ImA",msg);
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

        @Override
        public void onChannelStatus(String msg) {

        }
    }

    private void addView(String event, String nick_name, String data, String time, String avatar) {
        View view = View.inflate(IMActivity.this, R.layout.im_item_layout, null);
        ImageView v = view.findViewById(R.id.avatar);
        if (ll_content.getChildCount() >= 10) {
            View removeView = ll_content.getChildAt(0);
            ll_content.removeView(removeView);
        }
//        if (!TextUtils.isEmpty(avatar) && !avatar.equals("null")) {
//            requestAvatar(avatar, v);
//        }
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
        ll_content.addView(view);
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
