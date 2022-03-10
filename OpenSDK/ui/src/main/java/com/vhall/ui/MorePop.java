package com.vhall.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Switch;

/**
 * @author hkl
 * Date: 2019-07-24 16:58
 */
public class MorePop extends PopupWindow {
    private OnClickListener onClickListener;
    private boolean isVod;
    private Context mContext;
    private Activity activity;

    public MorePop(Context context, boolean isVod) {
        super(context);
        activity = (Activity) context;
        mContext = context;
        this.isVod = isVod;
        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int x = point.x;
        setWidth(x / 3);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
        setBackgroundDrawable(dw);
        setFocusable(true);
        View root = View.inflate(context, R.layout.pop_more_layout, null);
        setContentView(root);
        initView(root, activity);
        myRegisterReceiver();
    }

    public void setCirculation(boolean isCirculation) {
        if (circulation != null) {
            circulation.setChecked(isCirculation);
        }
    }

    private AudioManager mAudioManager;
    private WindowManager.LayoutParams win;
    private Switch circulation;
    private SeekBar light;
    private SeekBar voice;

    private void initView(View root, final Activity activity) {
        mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        assert mAudioManager != null;
        light = root.findViewById(R.id.seek_bar_light);
        voice = root.findViewById(R.id.seek_bar_voice);
        voice.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        voice.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        win = activity.getWindow().getAttributes();
        light.setMax(255);
        light.setProgress(getSystemBrightness(activity));
        if (!isVod) {
            root.findViewById(R.id.ll_circulation).setVisibility(View.GONE);
        }
        circulation = root.findViewById(R.id.circulation);
        circulation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (onClickListener != null) {
                    onClickListener.onClick(isChecked);
                }
            }
        });
        light.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int arg1, boolean b) {
                Log.e("mm", "arg1" + arg1);
                win.screenBrightness = (float) arg1 / 255;
                activity.getWindow().setAttributes(win);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        voice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, AudioManager.FLAG_PLAY_SOUND);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * 注册当音量发生变化时接收的广播
     */
    private void myRegisterReceiver() {
        MyVolumeReceiver mVolumeReceiver = new MyVolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        mContext.registerReceiver(mVolumeReceiver, filter);
    }

    /**
     * 处理音量变化时的界面显示
     *
     * @author long
     */
    private class MyVolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 当前的媒体音量
                voice.setProgress(currVolume);
            }
        }
    }

    private int getSystemBrightness(Activity activity) {
        int systemBrightness = 0;
        try {
            systemBrightness = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(boolean isCirculation);

        void dismiss();

    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (onClickListener != null) {
            onClickListener.dismiss();
        }
    }
}
