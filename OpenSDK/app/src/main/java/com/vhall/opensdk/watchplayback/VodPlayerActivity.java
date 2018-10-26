package com.vhall.opensdk.watchplayback;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.framework.logger.L;
import com.vhall.lss.play.IVHLivePlayer;
import com.vhall.opensdk.R;
import com.vhall.ops.VHDocument;
import com.vhall.ops.VHDocumentContainer;
import com.vhall.ops.VHOPS;
import com.vhall.ops.VHOPSView;
import com.vhall.vod.VHPlayerListener;
import com.vhall.vod.VHVodPlayer;
import com.vhall.vod.player.VHExoPlayer;

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
    ImageView mPlayBtn;
    ProgressBar mLoadingPB;
    SeekBar mSeekbar;
    TextView mPosView, mMaxView;
    RadioGroup mDPIGroup;
    //data
    String currentDPI = "";
    VHOPS mDocument;
    VHOPSView mDocView;

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
        mDocument.setEventListener(new VHOPS.OnEventListener() {
            @Override
            public void onEvent(JSONObject object) {
                Log.i(TAG, "object:" + object.toString());
//                mPageView.setText("页数：" + (object.optInt("currentPage") + 1) + "/" + object.optInt("page"));
//                mStepView.setText("步数：" + (object.optInt("currentStep") + 1) + "/" + object.optInt("step"));
            }
        });
        mDocument.join();
        mPlayer = new VHVodPlayer(this, mSurfaceView);
        mPlayer.addListener(new MyPlayer());
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
        mSeekbar.setOnSeekBarChangeListener(new MySeekbarListener());
        mSeekbar.setEnabled(false);
        mDPIGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                L.e(TAG, String.valueOf(checkedId));
                String dpi = ((RadioButton) mDPIGroup.getChildAt(checkedId)).getText().toString();
                if (!dpi.equals(currentDPI))
                    mPlayer.setDPI(dpi);
            }
        });
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mPlayer != null && mPlayer.resumeAble()) {
                    mPlayer.setBackground(false);
                    mPlayer.resume();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mPlayer != null) {
                    mPlayer.pause();
                    mPlayer.setBackground(true);
                }

            }
        });

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

    public void play(View view) {
        if (mPlaying) {
            mPlayer.pause();
        } else {
            if (mPlayer.resumeAble()) {
                if (mPlayer.getPlayerState() == VHExoPlayer.STATE_ENDED)
                    mPlayer.seek(0);
                else
                    mPlayer.resume();
            } else
                mPlayer.start(recordId, accessToken);
        }
    }

    class MyPlayer implements VHPlayerListener {

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case VHVodPlayer.EVENT_STATUS_STARTING:
                    mLoadingPB.setVisibility(View.VISIBLE);
                    break;
                case VHVodPlayer.EVENT_STATUS_STARTED:
                    int max = (int) mPlayer.getDuration();
                    mSeekbar.setMax(max);
                    mSeekbar.setEnabled(true);
                    mMaxView.setText(converLongTimeToStr(max));
                    mLoadingPB.setVisibility(View.GONE);
                    mPlaying = true;
                    mPlayBtn.setImageResource(R.mipmap.icon_pause_bro);
                    break;
                case VHVodPlayer.EVENT_STATUS_STOPED:
                    mPlaying = false;
                    mPlayBtn.setImageResource(R.mipmap.icon_start_bro);
                    mLoadingPB.setVisibility(View.GONE);
                    break;
                case VHVodPlayer.EVENT_STATUS_END:
                    mPlaying = false;
                    mPlayBtn.setImageResource(R.mipmap.icon_start_bro);
                    mLoadingPB.setVisibility(View.GONE);
                    break;
                case VHVodPlayer.EVENT_VIDEO_SIZE_CHANGED:

                    break;
                case IVHLivePlayer.EVENT_DPI_LIST:
                    try {
                        JSONArray array = new JSONArray(msg);
                        if (array != null && array.length() > 0) {
                            mDPIGroup.removeAllViews();
                            for (int i = 0; i < array.length(); i++) {
                                String dpi = (String) array.opt(i);
                                RadioButton rb = new RadioButton(VodPlayerActivity.this);
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
        public void onError(int errorCode, String msg) {
            mPlaying = false;
            mPlayBtn.setImageResource(R.mipmap.icon_start_bro);
            mLoadingPB.setVisibility(View.GONE);
            Toast.makeText(VodPlayerActivity.this, msg, Toast.LENGTH_SHORT).show();
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
            if (mPlayer.resumeAble())
                mPlayer.seek(seekBar.getProgress());
        }
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
