package com.vhall.ui;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vhall.lss.play.VHLivePlayer;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.player.stream.play.impl.VHVideoPlayerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author hkl
 * Date: 2019-07-23 16:24
 */
public class VHLiveUiView extends FrameLayout implements View.OnClickListener {
    private Context context;
    private TextView tvCurrentDpi;
    private ImageView ivPlayFull, ivPlaySmall, ivFullscreen, ivBack, ivMore;
    private VHVideoPlayerView playerView;
    private ProgressBar progressBar;
    private RelativeLayout rlFull, rlFullTop, rlSmall;
    private VHLivePlayer mPlayer;
    private String roomId, token;
    private VHUiPlayerLister uiPlayerLister;
    private int height = -1, width;
    private List<String> dpiData = new ArrayList<>();
    private int minSize;

    public void setMinSize(int minSize) {
        this.minSize = minSize;
        setViewVisibility();
    }

    public VHLivePlayer getPlayerView() {
        return mPlayer;
    }

    public VHLiveUiView(@NonNull Context context) {
        super(context);
        initView(context, null, 0);
    }

    public VHLiveUiView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public VHLiveUiView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;
        Activity activity = (Activity) context;
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        init();
    }

    public void setUiPlayerLister(VHUiPlayerLister uiPlayerLister) {
        this.uiPlayerLister = uiPlayerLister;
    }

    public void release() {
        if (mPlayer != null) {
            mPlayer.release();
        }
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.lss_play, this, true);
        tvCurrentDpi = findViewById(R.id.tv_current_dpi);
        playerView = findViewById(R.id.player);
        playerView.setDrawMode(com.vhall.player.Constants.VideoMode.DRAW_MODE_ASPECTFIT);
        mPlayer = new VHLivePlayer.Builder()
                .videoPlayer(playerView)
                .listener(new MyPlayer())
                .build();
        progressBar = findViewById(R.id.pb);
        rlFullTop = findViewById(R.id.rl_full_top);
        rlFull = findViewById(R.id.rl_full);
        rlSmall = findViewById(R.id.rl_small);

        ivPlayFull = findViewById(R.id.iv_play_full);
        ivPlaySmall = findViewById(R.id.iv_play_small);
        ivFullscreen = findViewById(R.id.iv_fullscreen);
        ivBack = findViewById(R.id.iv_back);
        ivMore = findViewById(R.id.iv_more);

        playerView.setOnClickListener(this);
        tvCurrentDpi.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivMore.setOnClickListener(this);
        ivPlaySmall.setOnClickListener(this);
        ivFullscreen.setOnClickListener(this);
        ivPlayFull.setOnClickListener(this);
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
            rlSmall.setVisibility(GONE);
            timerSeek.cancel();
        } else {
            Activity activity = (Activity) context;
            ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
            if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                this.setLayoutParams(layoutParams);
                rlSmall.setVisibility(View.GONE);
                rlFullTop.setVisibility(View.VISIBLE);
                rlFull.setVisibility(View.VISIBLE);
                timerSeek.cancel();
                timerSeek.start();
            } else {
                layoutParams.height = height;
                layoutParams.width = width;
                this.setLayoutParams(layoutParams);
                rlSmall.setVisibility(View.VISIBLE);
                rlFullTop.setVisibility(View.GONE);
                rlFull.setVisibility(View.GONE);
                timerSeek.cancel();
            }
        }
    }

    public void changeOrientation() {
        Activity activity = (Activity) context;
        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            this.setLayoutParams(layoutParams);
            rlSmall.setVisibility(View.GONE);
            rlFullTop.setVisibility(View.VISIBLE);
            rlFull.setVisibility(View.VISIBLE);
            timerSeek.cancel();
            timerSeek.start();
        } else {
            layoutParams.height = height;
            layoutParams.width = width;
            this.setLayoutParams(layoutParams);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            rlSmall.setVisibility(View.VISIBLE);
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
            } else if (i == R.id.player) {
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

            } else if (i == R.id.iv_fullscreen || i == R.id.iv_back) {
                changeOrientation();
            } else if (i == R.id.tv_current_dpi) {
                rlFull.setVisibility(GONE);
                rlFullTop.setVisibility(GONE);
                timerSeek.cancel();
                ListPop pop = new ListPop(context, dpiData);
                Activity activity = (Activity) context;
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
                MorePop pop = new MorePop(context, false);
                Activity activity = (Activity) context;
                pop.showAtLocation(activity.getWindow().getDecorView().findViewById(android.R.id.content), Gravity.END, 0, 0);
                pop.setOnClickListener(new MorePop.OnClickListener() {
                    @Override
                    public void onClick(boolean isCheck) {
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
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        } else {
            if (mPlayer.resumeAble()) {
                mPlayer.resume();
            } else {
                mPlayer.start(roomId, token);
            }
        }
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

    public void init(String roomId, String token) {
        this.roomId = roomId;
        this.token = token;
        play();
    }

    class MyPlayer implements VHPlayerListener {

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case BUFFER:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case START:
                    progressBar.setVisibility(View.GONE);
                    ivPlayFull.setImageResource(R.drawable.svg_iv_playing);
                    ivPlaySmall.setImageResource(R.drawable.svg_iv_playing);
                    break;
                case STOP:
                case END:
                    ivPlayFull.setImageResource(R.drawable.svg_iv_play_stop);
                    ivPlaySmall.setImageResource(R.drawable.svg_iv_play_stop);
                    progressBar.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case Constants.Event.EVENT_INIT_SUCCESS://初始化成功
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
            ivPlayFull.setImageResource(R.drawable.svg_iv_play_stop);
            ivPlaySmall.setImageResource(R.drawable.svg_iv_play_stop);
            progressBar.setVisibility(View.GONE);
        }

    }
}
