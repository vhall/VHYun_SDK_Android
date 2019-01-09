package com.vhall.opensdk.watchplayback;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.document.DocumentView;
import com.vhall.logmanager.L;
import com.vhall.opensdk.R;
import com.vhall.ops.VHOPS;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.vod.VHVodPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Hank on 2017/12/11.
 */
public class VodPlayerActivity extends Activity {
    private static final String TAG = "LivePlayerActivity";
    private String recordId = "";
    private String accessToken = "";
    private SurfaceView mSurfaceView;
    private VHVodPlayer mPlayer;
    private boolean mPlaying = false;
    ImageView mPlayBtn, ivAboutSpeed;
    ProgressBar mLoadingPB;
    SeekBar mSeekbar;
    TextView mPosView, mMaxView;
    RadioGroup mDPIGroup;
    TextView mSpeedOneView, mSpeedTwoView, mSpeedThreeView;
    //data
    String currentDPI = "";
    VHOPS mDocument;
    DocumentView mDocView;
    private boolean isInit = false;
    private RadioGroup speedGroup;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mPlaying) {
                int pos = (int) mPlayer.getPosition();
                mSeekbar.setProgress(pos);
                mPosView.setText(converLongTimeToStr(pos));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        recordId = getIntent().getStringExtra("channelid");
        accessToken = getIntent().getStringExtra("token");
        setContentView(R.layout.vod_layout);
        initView();
        mDocument = new VHOPS(recordId, null);
        mDocument.setDocumentView(mDocView);
        mDocument.join();
        mPlayer = new VHVodPlayer(this);
        mPlayer.setDisplay(mSurfaceView);
        mPlayer.setListener(new MyPlayer());
        handlePosition();
    }

    private void initView() {
        mDPIGroup = this.findViewById(R.id.rg_dpi);
        mDocView = this.findViewById(R.id.doc);
        setLayout();
        mPlayBtn = this.findViewById(R.id.btn_play);
        mLoadingPB = this.findViewById(R.id.pb_loading);
        mSeekbar = this.findViewById(R.id.seekbar);
        mSurfaceView = this.findViewById(R.id.surfaceview);
        mPosView = this.findViewById(R.id.tv_pos);
        mMaxView = this.findViewById(R.id.tv_max);
        speedGroup = findViewById(R.id.rg_speed);
        ivAboutSpeed = findViewById(R.id.iv_about_speed);

        speedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                float speed = 1.0f;
                switch (checkedId) {
                    case R.id.rb_speed1:
                        speed = 0.25f;
                        break;
                    case R.id.rb_speed2:
                        speed = 0.5f;
                        break;
                    case R.id.rb_speed3:
                        speed = 0.75f;
                        break;
                    case R.id.rb_speed4:
                        speed = 1.0f;
                        break;
                    case R.id.rb_speed5:
                        speed = 1.25f;
                        break;
                    case R.id.rb_speed6:
                        speed = 1.5f;
                        break;
                    case R.id.rb_speed7:
                        speed = 1.75f;
                        break;
                    case R.id.rb_speed8:
                        speed = 2.0f;
                        break;
                }
                setSpeed(speed);
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new MySeekbarListener());
        mSeekbar.setEnabled(false);
        mDPIGroup.setOnCheckedChangeListener(new OnCheckedChange());
    }


    public void aboutSpeed(View view) {
        if (speedGroup.getVisibility() == View.VISIBLE) {
            TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f
                    , Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
            animation.setDuration(1000);
            speedGroup.startAnimation(animation);
            speedGroup.setVisibility(View.GONE);
            ivAboutSpeed.setImageResource(R.drawable.ic_chevron_left_white_24dp);
        } else {
            speedGroup.setVisibility(View.VISIBLE);
            TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f
                    , Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
            animation.setDuration(1000);
            speedGroup.startAnimation(animation);
            ivAboutSpeed.setImageResource(R.drawable.ic_chevron_right_white_24dp);
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

    private void init() {
        mPlayer.init(recordId, accessToken);
    }


    private void setSpeed(float speed) {
        mPlayer.setSpeed(speed);
    }

    public void play(View view) {
        if (!isInit) {
            init();
        } else {
            if (mPlaying) {
                mPlayer.pause();
            } else {
                if (mPlayer.getState() == Constants.State.END) {
                    mPlayer.seekto(0);
                } else if (mPlayer.getState() == Constants.State.STOP) {
                    mPlayer.resume();
                } else {
                    mPlayer.start();

                }
            }
        }
    }

    class MyPlayer implements VHPlayerListener {

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case BUFFER:
                    mLoadingPB.setVisibility(View.VISIBLE);

                    break;
                case START:
                    int max = (int) mPlayer.getDuration();
                    mSeekbar.setMax(max);
                    mSeekbar.setEnabled(true);
                    mMaxView.setText(converLongTimeToStr(max));
                    mLoadingPB.setVisibility(View.GONE);
                    mPlaying = true;
                    mPlayBtn.setImageResource(R.mipmap.icon_pause_bro);
                    break;
                case STOP:
                case END:
                    mPlaying = false;
                    mPlayBtn.setImageResource(R.mipmap.icon_start_bro);
                    mLoadingPB.setVisibility(View.GONE);
                    break;
            }

        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case Constants.Event.EVENT_INIT_SUCCESS://初始化成功
                    isInit = true;
                    mPlayer.start();
                    break;
                case Constants.Event.EVENT_VIDEO_SIZE_CHANGED:

                    break;
                case Constants.Event.EVENT_DPI_LIST:
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
                                RadioButton rb = new RadioButton(VodPlayerActivity.this);
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
                    for (int i = 0; i < mDPIGroup.getChildCount(); i++) {
                        RadioButton button = (RadioButton) mDPIGroup.getChildAt(i);
                        if (button.getText().equals(msg)) {
                            button.setChecked(true);
                            currentDPI = msg;
                            break;
                        }
                    }
                    break;
            }
        }

        @Override
        public void onError(int errorCode, int i1, String msg) {
            switch (errorCode) {
                case Constants.ErrorCode.ERROR_INIT:
                    isInit = false;
                    Toast.makeText(VodPlayerActivity.this, "初始化失败" + msg, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.ErrorCode.ERROR_INIT_FIRST:
                    Toast.makeText(VodPlayerActivity.this, "请先初始化", Toast.LENGTH_SHORT).show();
                    break;
            }
            mPlaying = false;
            mPlayBtn.setImageResource(R.mipmap.icon_start_bro);
            mLoadingPB.setVisibility(View.GONE);
        }

    }

    class MySeekbarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mPosView.setText(converLongTimeToStr(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (isInit)
                mPlayer.seekto(seekBar.getProgress());
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null)
            mPlayer.release();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        mDocument.leave();
    }

    //每秒获取一下进度
    Timer timer;

    private void handlePosition() {
        if (timer != null)
            return;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 1000, 1000);
    }

    public static String converLongTimeToStr(long time) {
        int ss = 1000;
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

    private void setLayout() {
        WindowManager manager = getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;  //以要素为单位
        int height = width * 9 / 16;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mDocView.getLayoutParams();
        params.width = width;
        params.height = height;
    }
}
