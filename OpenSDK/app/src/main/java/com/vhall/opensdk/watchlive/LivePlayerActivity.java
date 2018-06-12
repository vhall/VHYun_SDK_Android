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

import com.vhall.lss.play.IVHLivePlayer;
import com.vhall.lss.play.IVHVideoPlayer;
import com.vhall.lss.play.VHLivePlayer;
import com.vhall.lss.play.impl.VHVideoPlayerView;
import com.vhall.lss.play.VHPlayerListener;
import com.vhall.opensdk.R;

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

        mDPIGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String dpi = ((RadioButton) mDPIGroup.getChildAt(checkedId)).getText().toString();
                if (!dpi.equals(currentDPI))
                    mPlayer.setDPI(dpi);
            }
        });

        mPlayer = new VHLivePlayer.Builder().videoPlayer(mVideoPlayer).listener(new MyListener()).build();
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
        public void onEvent(int event, String msg) {

            switch (event) {
                case IVHLivePlayer.EVENT_STATUS_STARTING:
                    break;
                case IVHLivePlayer.EVENT_STATUS_STARTED:
                    mPlayBtn.setImageResource(R.mipmap.icon_pause_bro);
                    break;
                case IVHLivePlayer.EVENT_STATUS_STOPED:
                    mPlayBtn.setImageResource(R.mipmap.icon_start_bro);
                    break;
                case IVHLivePlayer.EVENT_DPI_LIST:
                    Log.i(TAG, "DPILIST:" + msg);
                    try {
                        JSONArray array = new JSONArray(msg);
                        if (array != null && array.length() > 0) {
                            mDPIGroup.removeAllViews();
                            for (int i = 0; i < array.length(); i++) {
                                String dpi = (String) array.opt(i);
                                RadioButton rb = new RadioButton(LivePlayerActivity.this);
                                rb.setId(i);
                                rb.setText(dpi);
                                rb.setTextColor(Color.WHITE);
                                mDPIGroup.addView(rb);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    popu.notifyDataSetChanged(currentDPI, dipList);
                    break;
                case IVHLivePlayer.EVENT_DPI_CHANGED:
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
                case IVHLivePlayer.EVENT_URL:
                    TextView textView = new TextView(LivePlayerActivity.this);
                    textView.setText(currentDPI + ":" + msg);
                    ll_urls.addView(textView);
                    break;
                case IVHLivePlayer.EVENT_DOWNLOAD_SPEED:
                    mSpeedTV.setText(msg + "kb/s");
                    break;
                case IVHLivePlayer.EVENT_START_BUFFER:
                    mLoadingPB.setVisibility(View.VISIBLE);
                    break;
                case IVHLivePlayer.EVENT_STOP_BUFFER:
                    mLoadingPB.setVisibility(View.GONE);
                    break;
                case IVHLivePlayer.EVENT_VIDEO_SIZE_CHANGED:
                    Log.i(TAG, msg);
                    break;
                case IVHLivePlayer.EVENT_STREAM_START://发起端发起
                    if (mPlayer.isPlaying()) {
                        return;
                    }
                    Toast.makeText(LivePlayerActivity.this, "主播开始推流", Toast.LENGTH_SHORT).show();
                    if (mPlayer.resumeAble())
                        mPlayer.resume();
                    break;
                case IVHLivePlayer.EVENT_STREAM_STOP://发起端停止
                    if (mPlayer.isPlaying()) {
                        return;
                    }
                    Toast.makeText(LivePlayerActivity.this, "主播停止推流", Toast.LENGTH_SHORT).show();
                    mPlayer.pause();
                    break;
            }


        }

        @Override
        public void onError(int errorCode, String msg) {
            mLoadingPB.setVisibility(View.GONE);
            Log.i(TAG, "errorCode:" + errorCode + ",msg:" + msg);
            switch (errorCode) {
                case IVHLivePlayer.ERROR_CONNECT:
                    break;
            }

        }
    }
}
