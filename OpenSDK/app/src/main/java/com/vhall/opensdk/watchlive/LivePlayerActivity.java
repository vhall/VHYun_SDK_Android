package com.vhall.opensdk.watchlive;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.logmanager.L;
import com.vhall.lss.play.VHLivePlayer;
import com.vhall.opensdk.R;
import com.vhall.opensdk.watchplayback.VodPlayerActivity;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.player.stream.play.IVHVideoPlayer;
import com.vhall.player.stream.play.impl.VHVideoPlayerView;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Hank on 2017/11/23.
 */
public class LivePlayerActivity extends Activity {

    private static final String TAG = "LivePlayerActivity";

    VHLivePlayer mPlayer;
    VHVideoPlayerView mVideoPlayer;
    private String roomId = "";
    private String accessToken = "";
    //data
    String currentDPI = "";
    int drawmode = IVHVideoPlayer.DRAW_MODE_NONE;
    //view
    ImageView mPlayBtn;
    ProgressBar mLoadingPB;
    TextView mSpeedTV;
    //    private DPIPopu popu;
    RadioGroup mDPIGroup;
    //TODO delete
    LinearLayout ll_urls;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomId = getIntent().getStringExtra("channelid");
        accessToken = getIntent().getStringExtra("token");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.player_layout);
        ll_urls = this.findViewById(R.id.ll_urls);
        mDPIGroup = this.findViewById(R.id.rg_dpi);
        mVideoPlayer = this.findViewById(R.id.player);
        mPlayBtn = this.findViewById(R.id.btn_play);
        mLoadingPB = this.findViewById(R.id.pb_loading);
        mSpeedTV = this.findViewById(R.id.tv_speed);
        mDPIGroup.setOnCheckedChangeListener(new OnCheckedChange());


        mPlayer = new VHLivePlayer.Builder()
                .videoPlayer(mVideoPlayer)
                .listener(new MyListener())
                .build();
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
                    Toast.makeText(LivePlayerActivity.this, "Error message:" + msg, Toast.LENGTH_SHORT).show();
                    break;

            }
        }

    }
}
