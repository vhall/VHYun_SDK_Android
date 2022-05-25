package com.vhall.opensdk.watchlive;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business_support.dlna.DMCControl;
import com.vhall.business_support.dlna.DeviceDisplay;
import com.vhall.document.DocumentView;
import com.vhall.logmanager.L;
import com.vhall.lss.play.VHLivePlayer;
import com.vhall.lss.play.VHTimeShiftPlayer;
import com.vhall.opensdk.R;
import com.vhall.opensdk.watchplayback.DevicePopu;
import com.vhall.ops.VHOPS;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.player.stream.play.IVHVideoPlayer;
import com.vhall.player.stream.play.impl.VHVideoPlayerView;
import com.vhall.player.vod.VodPlayerView;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.android.FixedAndroidLogHandler;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
public class TimeShiftPlayerActivity extends Activity {

    private static final String TAG = "TimeShiftPlayerActivity";

    VHTimeShiftPlayer mPlayer;
    VodPlayerView mVideoPlayer;
    private String roomId = "";
    private String channelId = "";
    private String accessToken = "";
    //data
    String currentDPI = "";
    int drawmode = IVHVideoPlayer.DRAW_MODE_NONE;
    //view
    ImageView mPlayBtn, ivScreen;
    ProgressBar mLoadingPB;
    RadioGroup mDPIGroup;

    private CheckBox cbFllScreen;
    private SeekBar seekBar;
    private TextView time;
    private int live_duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomId = getIntent().getStringExtra("roomId");
        channelId = getIntent().getStringExtra("channelId");

        if (TextUtils.isEmpty(roomId)) {
            roomId = channelId;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        accessToken = getIntent().getStringExtra("token");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.player_time_shift_layout);
        mDPIGroup = this.findViewById(R.id.rg_dpi);
        mVideoPlayer = this.findViewById(R.id.player);
        mPlayBtn = this.findViewById(R.id.btn_play);
        mLoadingPB = this.findViewById(R.id.pb_loading);
        time = this.findViewById(R.id.tv_time);
        seekBar = this.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mLoadingPB.setVisibility(View.VISIBLE);
                nowProgress = live_duration-seekBar.getProgress();
                mPlayer.start(roomId, accessToken, nowProgress);
            }
        });


        mDPIGroup.setOnCheckedChangeListener(new OnCheckedChange());

        mPlayer = new VHTimeShiftPlayer.Builder(TimeShiftPlayerActivity.this)
                .videoPlayer(mVideoPlayer)
                .listener(new MyListener())
                .build(TimeShiftPlayerActivity.this);
        mPlayer.start(roomId, accessToken);

    }


    public void screenImageOnClick(View view) {
        ivScreen.setVisibility(View.GONE);
    }

    public void screenShot(View view) {
        String dpi = ((RadioButton) mDPIGroup.getChildAt(mDPIGroup.getCheckedRadioButtonId())).getText().toString();
        if (mPlayer.isPlaying() && !dpi.equals("a")) {//正在播放，且非音频模式截屏；

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
    }

    public void play(View view) {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        } else {
            mLoadingPB.setVisibility(View.VISIBLE);
            nowProgress = live_duration-90;
            mPlayer.start(roomId, accessToken);
        }
    }

    public void live(View view) {
        mLoadingPB.setVisibility(View.VISIBLE);
        nowProgress = live_duration;
        mPlayer.start(roomId, accessToken, 0);
    }

    public void changeMode(View view) {
        if (mPlayer != null && mPlayer.resumeAble()) {
            mPlayer.setDrawMode(++drawmode % 3);
        }

    }

    class MyListener implements VHPlayerListener {

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case START:
                    live_duration = mPlayer.getLive_duration();
                    nowProgress = live_duration - 90;
                    handlePosition();
                    mLoadingPB.setVisibility(View.GONE);
                    mPlayBtn.setImageResource(R.drawable.svg_iv_playing);
                    break;
                case BUFFER:
                    mLoadingPB.setVisibility(View.VISIBLE);
                    mPlayBtn.setImageResource(R.drawable.svg_iv_play_stop);
                    break;
                case STOP:
                    mLoadingPB.setVisibility(View.GONE);
                    mPlayBtn.setImageResource(R.drawable.svg_iv_play_stop);
                    break;
                case END:
                    mLoadingPB.setVisibility(View.GONE);
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
                                RadioButton rb = new RadioButton(TimeShiftPlayerActivity.this);
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
                    TextView textView = new TextView(TimeShiftPlayerActivity.this);
                    textView.setText(currentDPI + ":" + msg);
                    break;
                case Constants.Event.EVENT_VIDEO_SIZE_CHANGED:
                    Log.i(TAG, msg);
                    break;
                case Constants.Event.EVENT_STREAM_START://发起端发起
                    if (mPlayer.isPlaying()) {
                        return;
                    }
                    Toast.makeText(TimeShiftPlayerActivity.this, "主播开始推流", Toast.LENGTH_SHORT).show();
                    if (mPlayer.resumeAble())
                        mPlayer.resume();
                    break;
                case Constants.Event.EVENT_STREAM_STOP://发起端停止
//                    if (!mPlayer.isPlaying()) {
//                        return;
//                    }
                    Toast.makeText(TimeShiftPlayerActivity.this, "主播停止推流", Toast.LENGTH_SHORT).show();
                    mPlayer.pause();
                    finish();
                    break;
                case Constants.Event.EVENT_NO_STREAM:
                    Toast.makeText(TimeShiftPlayerActivity.this, "主播端暂未推流", Toast.LENGTH_SHORT).show();
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
                    mPlayBtn.setImageResource(R.drawable.svg_iv_play_stop);
                    Toast.makeText(TimeShiftPlayerActivity.this, "Error message:error connect " + msg, Toast.LENGTH_SHORT).show();
                    break;

            }
        }

    }

    private ScheduledThreadPoolExecutor service;

    private int nowProgress;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (mPlayer.isPlaying()) {
                mLoadingPB.setVisibility(View.GONE);
                live_duration++;
                time.setText(converLongTimeToStr(live_duration));
                seekBar.setMax(live_duration);
                nowProgress++;
                seekBar.setProgress(nowProgress);
            }
            return false;
        }
    });

    private void handlePosition() {
        if (service != null) {
            return;
        }
        service = new ScheduledThreadPoolExecutor(1);
        service.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public static String converLongTimeToStr(long time) {
        int ss = 1;
        int mi = ss * 60;
        int hh = mi * 60;

        long hour = (time) / hh;
        long minute = (time - hour * hh) / mi;
        long second = (time - hour * hh - minute * mi) / ss;

        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;
        if (hour > 0) {
            return strHour + ":" + strMinute + ":" + strSecond;
        } else {
            return "00:" + strMinute + ":" + strSecond;
        }
    }
}
