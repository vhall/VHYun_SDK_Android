package com.vhall.ui;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.player.vod.VodPlayer;
import com.vhall.player.vod.VodPlayerView;
import com.vhall.vod.VHVodPlayer;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author hkl
 * Date: 2019-07-23 16:24
 */
public class VHVodPlayUiView extends FrameLayout implements View.OnClickListener {
    private Context context;
    private SeekBar sbFull, sbSmall;
    private TextView tvCurrentTimeFull, tvCurrentSpeed, tvCurrentDpi, tvCurrentTime, tvEndTime;
    private ImageView ivPlayFull, ivPlaySmall, ivFullscreen, ivBack, ivMore;
    private VodPlayerView playerView;
    private ProgressBar progressBar;
    private RelativeLayout rlFull, rlFullTop;
    private LinearLayout llSmall;
    private VHVodPlayer mPlayer;
    private boolean mPlaying = false;
    private String recordId, token;
    private boolean isInit = false;
    private int height = -1, width;
    private ScheduledThreadPoolExecutor service;
    private VHUiPlayerLister uiPlayerLister;
    private List<String> dpiData = new ArrayList<>();
    private List<String> speedData = new ArrayList<>();
    private List<Float> speed = new ArrayList<>();
    private boolean isCirculation = false;
    private int minSize;

    public void setMinSize(int minSize) {
        this.minSize = minSize;
        setViewVisibility();
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (mPlaying) {
                int pos = (int) mPlayer.getPosition();
                sbFull.setProgress(pos);
                sbSmall.setProgress(pos);
                tvCurrentTime.setText(converLongTimeToStr(pos));
                int max = (int) mPlayer.getDuration();
                tvCurrentTimeFull.setText(String.format("%s/%s", converLongTimeToStr(pos), converLongTimeToStr(max)));
            }
            sbFull.setSecondaryProgress((int) mPlayer.getBufferedPosition());
            sbSmall.setSecondaryProgress((int) mPlayer.getBufferedPosition());
            return false;
        }
    });

    public VHVodPlayUiView(Context context) {
        super(context);
        initView(context, null, 0);
    }

    public VHVodPlayUiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public VHVodPlayUiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;
        VodPlayer.setBufferDurationsMs(5 * 60 * 1000, 5 * 60 * 1000, 2500, 49 * 6 * 1000);
        init();
    }

    public void setUiPlayerLister(VHUiPlayerLister uiPlayerLister) {
        this.uiPlayerLister = uiPlayerLister;
    }

    public VHVodPlayer getPlayerView() {
        return mPlayer;
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.vod_play_view, this, true);
        sbFull = findViewById(R.id.seek_bar_full);
        sbSmall = findViewById(R.id.seek_bar_small);

        tvCurrentTimeFull = findViewById(R.id.tv_current_time_full);
        tvCurrentSpeed = findViewById(R.id.tv_current_speed);
        tvCurrentDpi = findViewById(R.id.tv_current_dpi);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvEndTime = findViewById(R.id.tv_end_time);

        playerView = findViewById(R.id.vod_player_view);
        mPlayer = new VHVodPlayer(context);
        mPlayer.setListener(new MyPlayer());
        mPlayer.setDisplay(playerView);
        //mPlayer.
        progressBar = findViewById(R.id.pb);
        rlFullTop = findViewById(R.id.rl_full_top);
        rlFull = findViewById(R.id.rl_full);
        llSmall = findViewById(R.id.ll_small);

        ivPlayFull = findViewById(R.id.iv_play_full);
        ivPlaySmall = findViewById(R.id.iv_play_small);
        ivFullscreen = findViewById(R.id.iv_fullscreen);
        ivBack = findViewById(R.id.iv_back);
        ivMore = findViewById(R.id.iv_more);

        playerView.setOnClickListener(this);
        tvCurrentDpi.setOnClickListener(this);
        tvCurrentSpeed.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivMore.setOnClickListener(this);
        ivPlaySmall.setOnClickListener(this);
        ivFullscreen.setOnClickListener(this);
        ivPlayFull.setOnClickListener(this);
        handlePosition();
        sbFull.setOnSeekBarChangeListener(new MySeekBarListener());
        sbFull.setEnabled(false);
        sbSmall.setOnSeekBarChangeListener(new MySeekBarListener());
        sbSmall.setEnabled(false);
        speed.clear();
        speedData.clear();
        speedData.add("2.0X");
        speedData.add("1.75X");
        speedData.add("1.5X");
        speedData.add("正常");
        speedData.add("0.75X");
        speedData.add("0.5X");
        speed.add(2.0f);
        speed.add(1.75f);
        speed.add(1.5f);
        speed.add(1.0f);
        speed.add(0.75f);
        speed.add(0.5f);
        Activity activity = (Activity) context;
        DisplayMetrics outMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        minSize = outMetrics.widthPixels / 2;
        final View view = this;
        view.post(new Runnable() {
            @Override
            public void run() {
                height = view.getHeight();
                width = view.getWidth();
                setViewVisibility();
            }
        });
    }

    private void setViewVisibility() {
        if (height == -1) {
            return;
        }
        if (width < minSize) {
            rlFull.setVisibility(GONE);
            rlFullTop.setVisibility(GONE);
            llSmall.setVisibility(GONE);
            timerSeek.cancel();
        } else {
            Activity activity = (Activity) context;
            ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
            if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                this.setLayoutParams(layoutParams);
                llSmall.setVisibility(View.GONE);
                rlFullTop.setVisibility(View.VISIBLE);
                rlFull.setVisibility(View.VISIBLE);
                timerSeek.cancel();
                timerSeek.start();
            } else {
                layoutParams.height = height;
                layoutParams.width = width;
                this.setLayoutParams(layoutParams);
                llSmall.setVisibility(View.VISIBLE);
                rlFullTop.setVisibility(View.GONE);
                rlFull.setVisibility(View.GONE);
                timerSeek.cancel();
            }
        }
    }

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
        }, 150, 150, TimeUnit.MILLISECONDS);
    }

    CountDownTimer timerSeek = new CountDownTimer(5 * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            if (rlFull.getVisibility() == View.VISIBLE) {
                rlFull.setVisibility(GONE);
                rlFullTop.setVisibility(GONE);
            }
        }
    };

    public void changeOrientation() {
        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
        if (height == -1) {
            height = layoutParams.height;
            width = layoutParams.width;
        }
        Activity activity = (Activity) context;
        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            this.setLayoutParams(layoutParams);
            llSmall.setVisibility(View.GONE);
            rlFullTop.setVisibility(View.VISIBLE);
            rlFull.setVisibility(View.VISIBLE);
            timerSeek.start();
        } else {
            layoutParams.height = height;
            layoutParams.width = width;
            this.setLayoutParams(layoutParams);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            llSmall.setVisibility(View.VISIBLE);
            rlFullTop.setVisibility(View.GONE);
            rlFull.setVisibility(View.GONE);
            timerSeek.cancel();
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (mPlayer != null) {
            if (i == R.id.iv_play_full) {
                timerSeek.cancel();
                play();
                timerSeek.start();
            } else if (i == R.id.iv_play_small) {
                play();
            } else if (i == R.id.iv_fullscreen || i == R.id.iv_back) {
                changeOrientation();
            } else if (i == R.id.vod_player_view) {
                Activity activity = (Activity) context;
                if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    if (rlFull.getVisibility() == View.VISIBLE) {
                        rlFull.setVisibility(GONE);
                        rlFullTop.setVisibility(GONE);
                        timerSeek.cancel();
                    } else {
                        rlFull.setVisibility(VISIBLE);
                        rlFullTop.setVisibility(VISIBLE);
                        timerSeek.cancel();
                        timerSeek.start();
                    }
                }

            } else if (i == R.id.tv_current_speed) {
                rlFull.setVisibility(GONE);
                rlFullTop.setVisibility(GONE);
                timerSeek.cancel();
                ListPop pop = new ListPop(context, speedData);
                Activity activity = (Activity) context;
                pop.showAtLocation(activity.getWindow().getDecorView().findViewById(android.R.id.content), Gravity.END, 0, 0);
                pop.setOnClickListener(new ListPop.OnClickListener() {
                    @Override
                    public void onClick(String data) {
                        if (speedData.contains(data)) {
                            mPlayer.setSpeed(speed.get(speedData.indexOf(data)));
                            tvCurrentSpeed.setText(data);
                        }
                    }

                    @Override
                    public void dismiss() {
                        timerSeek.start();
                    }
                });
            } else if (i == R.id.tv_current_dpi) {
                rlFull.setVisibility(GONE);
                rlFullTop.setVisibility(GONE);
                ListPop pop = new ListPop(context, dpiData);
                Activity activity = (Activity) context;
                timerSeek.cancel();
                pop.showAtLocation(activity.getWindow().getDecorView().findViewById(android.R.id.content), Gravity.END, 0, 0);
                pop.setOnClickListener(new ListPop.OnClickListener() {
                    @Override
                    public void onClick(String data) {
                        if (dpiData.contains(data)) {
                            if ("原画".equals(data)) {
                                mPlayer.setDPI("same");
                            } else if ("音频".equals(data)) {
                                mPlayer.setDPI("a");
                            } else {
                                mPlayer.setDPI(data);
                            }
                        }
                    }

                    @Override
                    public void dismiss() {
                        timerSeek.start();
                    }
                });
            } else if (i == R.id.iv_more) {
                rlFull.setVisibility(GONE);
                rlFullTop.setVisibility(GONE);
                timerSeek.cancel();
                MorePop pop = new MorePop(context, true);
                Activity activity = (Activity) context;
                pop.setCirculation(isCirculation);
                pop.showAtLocation(activity.getWindow().getDecorView().findViewById(android.R.id.content), Gravity.END, 0, 0);
                pop.setOnClickListener(new MorePop.OnClickListener() {
                    @Override
                    public void onClick(boolean isCheck) {
                        isCirculation = isCheck;
                    }

                    @Override
                    public void dismiss() {
                        timerSeek.start();
                    }
                });
            }
        }
    }

    public void play() {
        if (!isInit) {
            mPlayer.init(recordId, token);
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

    public void prepare(){
        if(isInit){
            mPlayer.prepare();
        }
    }

    public void init(String recordId, String token) {
        this.recordId = recordId;
        this.token = token;
        if(!isInit){
            mPlayer.init(recordId,token);
        }
    }

    class MyPlayer implements VHPlayerListener {

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case IDLE:
                    progressBar.setVisibility(View.GONE);
                    mPlaying = false;
                    ivPlayFull.setImageResource(R.drawable.svg_iv_play_stop);
                    ivPlaySmall.setImageResource(R.drawable.svg_iv_play_stop);
                    break;
                case BUFFER:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case START:
                    int max = (int) mPlayer.getDuration();
                    sbFull.setMax(max);
                    sbFull.setEnabled(true);
                    sbSmall.setMax(max);
                    sbSmall.setEnabled(true);
                    tvEndTime.setText(converLongTimeToStr(max));
                    tvCurrentTimeFull.setText(String.format("00:00:00 / %s", converLongTimeToStr(max)));
                    progressBar.setVisibility(View.GONE);
                    mPlaying = true;
                    ivPlayFull.setImageResource(R.drawable.svg_iv_playing);
                    ivPlaySmall.setImageResource(R.drawable.svg_iv_playing);
                    break;
                case STOP:
                    mPlaying = false;
                    ivPlayFull.setImageResource(R.drawable.svg_iv_play_stop);
                    ivPlaySmall.setImageResource(R.drawable.svg_iv_play_stop);
                    progressBar.setVisibility(View.GONE);
                    break;
                case END:
                    mPlaying = false;
                    ivPlayFull.setImageResource(R.drawable.svg_iv_play_stop);
                    ivPlaySmall.setImageResource(R.drawable.svg_iv_play_stop);
                    progressBar.setVisibility(View.GONE);
                    if (isCirculation) {
                        play();
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case Constants.Event.EVENT_INIT_SUCCESS://初始化成功
                    isInit = true;
                    prepare();
                    break;
                case Constants.Event.EVENT_VIDEO_SIZE_CHANGED:

                    break;
                case Constants.Event.EVENT_DPI_LIST:
                    dpiData.clear();
                    try {
                        JSONArray array = new JSONArray(msg);
                        if (array.length() > 0) {
                            for (int i = 0; i < array.length(); i++) {
                                String dpi = (String) array.opt(i);
                                if ("same".equals(dpi)) {
                                    dpiData.add("原画");
                                } else if ("a".equals(dpi)) {
                                    dpiData.add("音频");
                                } else {
                                    dpiData.add(dpi);
                                }
                            }
                            /*
                            * 默认正序：360p,480p,音频、原画
                            * 默认逆序：原画、音频、480p、360p
                            * 目标排序 480p、360p、原画、音频
                            */
                            Collections.reverse(dpiData);
                            if (dpiData.contains("原画")) {
                                dpiData.remove("原画");
                                dpiData.add("原画");
                            }
                            if (dpiData.contains("音频")) {
                                dpiData.remove("音频");
                                dpiData.add("音频");
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.Event.EVENT_DPI_CHANGED:
                    String dpi = msg;
                    if ("same".equals(dpi)) {
                        tvCurrentDpi.setText("原画");
                    } else if ("a".equals(dpi)) {
                        tvCurrentDpi.setText("音频");
                    } else {
                        tvCurrentDpi.setText(dpi);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(int errorCode, int i1, String msg) {
            isInit = false;
            switch (errorCode) {
                case Constants.ErrorCode.ERROR_INIT:
                    if (uiPlayerLister != null) {
                        uiPlayerLister.onError(errorCode, i1, "初始化失败");
                    }
                    break;
                case Constants.ErrorCode.ERROR_INIT_FIRST:
                    if (uiPlayerLister != null) {
                        uiPlayerLister.onError(errorCode, i1, "请先初始化");
                    }
                    break;
                default:
                    if (uiPlayerLister != null) {
                        uiPlayerLister.onError(errorCode, i1, msg);
                    }
                    break;
            }
            mPlaying = false;
            ivPlayFull.setImageResource(R.drawable.svg_iv_play_stop);
            ivPlaySmall.setImageResource(R.drawable.svg_iv_play_stop);
            progressBar.setVisibility(View.GONE);
        }

    }

    class MySeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tvCurrentTime.setText(converLongTimeToStr(progress));
            int max = (int) mPlayer.getDuration();
            tvCurrentTimeFull.setText(String.format("%s/%s", converLongTimeToStr(progress), converLongTimeToStr(max)));

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (isInit) {
                mPlayer.seekto(seekBar.getProgress());
            }
        }

    }

    public void release() {
        if (service != null) {
            service.shutdown();
        }
        if (mPlayer != null) {
            mPlayer.release();
        }
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
}
