package com.vhall.opensdk.watchlive;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.document.DocumentView;
import com.vhall.logmanager.L;
import com.vhall.lss.play.VHLivePlayer;
import com.vhall.opensdk.R;
import com.vhall.opensdk.document.DocActivity;
import com.vhall.ops.VHOPS;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.player.stream.LivePlayer;
import com.vhall.player.stream.play.IVHVideoPlayer;
import com.vhall.player.stream.play.impl.VHVideoPlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.vhall.ops.VHOPS.ERROR_CONNECT;
import static com.vhall.ops.VHOPS.ERROR_DOC_INFO;
import static com.vhall.ops.VHOPS.ERROR_SEND;
import static com.vhall.ops.VHOPS.KEY_OPERATE;
import static com.vhall.ops.VHOPS.TYPE_ACTIVE;
import static com.vhall.ops.VHOPS.TYPE_CREATE;
import static com.vhall.ops.VHOPS.TYPE_DESTROY;
import static com.vhall.ops.VHOPS.TYPE_RESET;
import static com.vhall.ops.VHOPS.TYPE_SWITCHOFF;
import static com.vhall.ops.VHOPS.TYPE_SWITCHON;

/**
 * Created by Hank on 2017/11/23.
 */
public class LivePlayerActivity extends Activity {

    private static final String TAG = "LivePlayerActivity";

    VHLivePlayer mPlayer;
    VHVideoPlayerView mVideoPlayer;
    private String roomId = "";
    private String channelId = "";
    private String accessToken = "";
    //data
    String currentDPI = "";
    int drawmode = IVHVideoPlayer.DRAW_MODE_NONE;
    //view
    ImageView mPlayBtn, ivScreen;
    ProgressBar mLoadingPB;
    TextView mSpeedTV;
    //    private DPIPopu popu;
    RadioGroup mDPIGroup;
    //TODO delete
    LinearLayout ll_urls;
    Handler handler = new Handler();
    VHOPS mDocument;
    RelativeLayout rlOpsContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomId = getIntent().getStringExtra("roomId");
        channelId = getIntent().getStringExtra("channelId");
        if (TextUtils.isEmpty(roomId)) {
            roomId = channelId;
        }
        accessToken = getIntent().getStringExtra("token");
//        roomId = "lss_772f6eda";
//        accessToken = "vhall";
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.player_layout);
        ll_urls = this.findViewById(R.id.ll_urls);
        mDPIGroup = this.findViewById(R.id.rg_dpi);
        mVideoPlayer = this.findViewById(R.id.player);
        mPlayBtn = this.findViewById(R.id.btn_play);
        mLoadingPB = this.findViewById(R.id.pb_loading);
        mSpeedTV = this.findViewById(R.id.tv_speed);
        ivScreen = findViewById(R.id.iv_screen_show);
        rlOpsContainer = findViewById(R.id.rl_ops_container);
        mDPIGroup.setOnCheckedChangeListener(new OnCheckedChange());
        mVideoPlayer.setDrawMode(Constants.VideoMode.DRAW_MODE_ASPECTFIT);
        mPlayer = new VHLivePlayer.Builder()
                .videoPlayer(mVideoPlayer)
                .listener(new MyListener())
                .build();
        mPlayer.start(roomId, accessToken);

        mDocument = new VHOPS(this, channelId, roomId, accessToken, true);
        mDocument.setListener(opsListener);
        mDocument.join();
    }

    private VHOPS.EventListener opsListener = new VHOPS.EventListener() {
        @Override
        public void onEvent(String event, String type, String cid) {
            if (event.equals(KEY_OPERATE)) {
                if (type.equals(TYPE_RESET)) {
                    rlOpsContainer.removeAllViews();
                } else if (type.equals(TYPE_ACTIVE)) {
                    if (rlOpsContainer != null) {
                        rlOpsContainer.removeAllViews();
                        DocumentView mDocView = mDocument.getActiveView();
                        rlOpsContainer.addView(mDocView);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        mDocView.setLayoutParams(params);
                    }
                } else if (type.equals(TYPE_CREATE)) {
                    //创建文档

                } else if (type.equals(TYPE_DESTROY)) {
                    //删除编号 cid的文档

                } else if (type.equals(TYPE_SWITCHOFF)) {
                    //关闭文档演示
                    rlOpsContainer.setVisibility(View.INVISIBLE);
                } else if (type.equals(TYPE_SWITCHON)) {
                    //打开文档演示
                    rlOpsContainer.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onError(int errorCode, int innerError, String errorMsg) {
            switch (errorCode) {
                case ERROR_CONNECT:
                case ERROR_SEND:
                    Toast.makeText(LivePlayerActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_DOC_INFO:
                    try {
                        JSONObject obj = new JSONObject(errorMsg);
                        String msg = obj.optString("msg");
                        String cid = obj.optString("cid");
                        Toast.makeText(LivePlayerActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void screenImageOnClick(View view) {
        ivScreen.setVisibility(View.GONE);

    }

    public void screenShot(View view) {
        String dpi = ((RadioButton) mDPIGroup.getChildAt(mDPIGroup.getCheckedRadioButtonId())).getText().toString();
        if (mPlayer.isPlaying() && !dpi.equals("a")) {//正在播放，且非音频模式截屏；
            mVideoPlayer.takeVideoScreenshot(new VHVideoPlayerView.ScreenShotCallback() {
                @Override
                public void screenBack(Bitmap bitmap) {
                    ivScreen.setImageBitmap(bitmap);
                    ivScreen.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private class OnCheckedChange implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            L.e(TAG, String.valueOf(checkedId));
            String dpi = ((RadioButton) mDPIGroup.getChildAt(checkedId)).getText().toString();
            if (!dpi.equals(currentDPI))
                mPlayer.setDPI(dpi);
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
        mDocument.leave();

    }

    public void play(View view) {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        } else {
            if (mPlayer.resumeAble())
                mPlayer.resume();
            else
                mPlayer.start(roomId, accessToken);
        }
    }

    public void changeMode(View view) {
        if (mPlayer != null && mPlayer.resumeAble())
            mVideoPlayer.setDrawMode(++drawmode % 3);
    }

    class MyListener implements VHPlayerListener {

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case START:
                    mLoadingPB.setVisibility(View.GONE);
                    mPlayBtn.setImageResource(R.mipmap.icon_pause_bro);
                    if (!TextUtils.isEmpty(channelId) && mDocument != null) {
                        //设置文档延迟时间，以保证与视频操作同步
                        mDocument.setDealTime(mPlayer.getRealityBufferTime() + 3000);
                    }
                    break;
                case BUFFER:
                    mLoadingPB.setVisibility(View.VISIBLE);
                    break;
                case STOP:
                    mLoadingPB.setVisibility(View.GONE);
                    mPlayBtn.setImageResource(R.mipmap.icon_start_bro);
                    break;
                case END:

                    break;

            }
        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case Constants.Event.EVENT_DPI_LIST:
                    Log.i(TAG, "DPILIST:" + msg);
                    try {
                        JSONArray array = new JSONArray(msg);
                        if (array != null && array.length() > 0) {
                            //未取消监听情况下清空选中状态，会造成crash
                            mDPIGroup.setOnCheckedChangeListener(null);
                            //需要清空当前选中状态，下次设置才能生效
                            mDPIGroup.clearCheck();
                            mDPIGroup.removeAllViews();
                            for (int i = 0; i < array.length(); i++) {
                                String dpi = (String) array.opt(i);
                                RadioButton rb = new RadioButton(LivePlayerActivity.this);
                                rb.setId(i);
                                rb.setText(dpi);
                                rb.setTextColor(Color.WHITE);
                                mDPIGroup.addView(rb);
                            }
                            mDPIGroup.setOnCheckedChangeListener(new OnCheckedChange());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    popu.notifyDataSetChanged(currentDPI, dipList);
                    break;
                case Constants.Event.EVENT_DPI_CHANGED:
                    Log.i(TAG, "DPI:" + msg);
                    for (int i = 0; i < mDPIGroup.getChildCount(); i++) {
                        RadioButton button = (RadioButton) mDPIGroup.getChildAt(i);
                        if (button.getText().equals(msg)) {
                            button.setChecked(true);
                            currentDPI = msg;
                            break;
                        }
                    }
                    break;
                case Constants.Event.EVENT_URL:
                    TextView textView = new TextView(LivePlayerActivity.this);
                    textView.setText(currentDPI + ":" + msg);
                    ll_urls.addView(textView);
                    break;
                case Constants.Event.EVENT_DOWNLOAD_SPEED:
                    mSpeedTV.setText(msg + "kb/s");
                    break;
                case Constants.Event.EVENT_VIDEO_SIZE_CHANGED:
                    Log.i(TAG, msg);
                    break;
                case Constants.Event.EVENT_STREAM_START://发起端发起
                    if (mPlayer.isPlaying()) {
                        return;
                    }
                    Toast.makeText(LivePlayerActivity.this, "主播开始推流", Toast.LENGTH_SHORT).show();
                    if (mPlayer.resumeAble())
                        mPlayer.resume();
                    break;
                case Constants.Event.EVENT_STREAM_STOP://发起端停止
                    if (!mPlayer.isPlaying()) {
                        return;
                    }
                    Toast.makeText(LivePlayerActivity.this, "主播停止推流", Toast.LENGTH_SHORT).show();
                    mPlayer.pause();
                    break;
                case Constants.Event.EVENT_NO_STREAM:
                    Toast.makeText(LivePlayerActivity.this, "主播端暂未推流", Toast.LENGTH_SHORT).show();
                    break;
            }


        }

        @Override
        public void onError(int errorCode, int innerCode, String msg) {
            mLoadingPB.setVisibility(View.GONE);
            Log.i(TAG, "errorCode:" + errorCode + ",msg:" + msg);
            switch (errorCode) {
                case Constants.ErrorCode.ERROR_CONNECT:
                    mLoadingPB.setVisibility(View.GONE);
                    mPlayBtn.setImageResource(R.mipmap.icon_start_bro);
                    Toast.makeText(LivePlayerActivity.this, "Error message:error connect " + msg, Toast.LENGTH_SHORT).show();
                    break;

            }
        }

    }
}
