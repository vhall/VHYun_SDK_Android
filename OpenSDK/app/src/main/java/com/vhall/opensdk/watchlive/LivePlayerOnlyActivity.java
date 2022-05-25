package com.vhall.opensdk.watchlive;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business_support.dlna.DMCControl;
import com.vhall.business_support.dlna.DeviceDisplay;
import com.vhall.logmanager.L;
import com.vhall.lss.play.VHLivePlayer;
import com.vhall.opensdk.R;
import com.vhall.opensdk.watchplayback.DevicePopu;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.player.stream.play.IVHVideoPlayer;
import com.vhall.player.stream.play.impl.VHVideoPlayerView;

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

import java.util.Collection;

/**
 * Created by Hank on 2017/12/20.
 */
public class LivePlayerOnlyActivity extends Activity {

    private static final String TAG = "LivePlayerOnlyActivity";
    VHLivePlayer mPlayer;
    VHVideoPlayerView mVideoPlayer;
    private String roomId = "";
    private String accessToken = "";
    String currentDPI = "";
    int drawmode = IVHVideoPlayer.DRAW_MODE_NONE;
    ImageView mPlayBtn, ivScreen;
    ProgressBar mLoadingPB;
    TextView mSpeedTV;
    RadioGroup mDPIGroup;
    RelativeLayout rlPlayerContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomId = getIntent().getStringExtra("roomId");
        accessToken = getIntent().getStringExtra("token");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.layout_liveonly);
        mDPIGroup = this.findViewById(R.id.rg_dpi);
        mVideoPlayer = this.findViewById(R.id.player);
        mPlayBtn = this.findViewById(R.id.btn_play);
        mLoadingPB = this.findViewById(R.id.pb_loading);
        mSpeedTV = this.findViewById(R.id.tv_speed);
        ivScreen = findViewById(R.id.iv_screen_show);

        rlPlayerContent = findViewById(R.id.rl_player_content);

        mDPIGroup.setOnCheckedChangeListener(new OnCheckedChange());
        mVideoPlayer.setDrawMode(Constants.VideoMode.DRAW_MODE_ASPECTFIT);
        mPlayer = new VHLivePlayer.Builder()
                .videoPlayer(mVideoPlayer)
                .bufferSeconds(4)
                .listener(new MyListener())
                .build();
        mPlayer.start(roomId, accessToken);

        org.seamless.util.logging.LoggingUtil.resetRootHandler(
                new FixedAndroidLogHandler()
        );
        this.bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            rlPlayerContent.setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            rlPlayerContent.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }


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

    public void onProjectionScreen(View view) {
        showDevices();
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
                                RadioButton rb = new RadioButton(LivePlayerOnlyActivity.this);
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
                    TextView textView = new TextView(LivePlayerOnlyActivity.this);
                    textView.setText(currentDPI + ":" + msg);
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
                    Toast.makeText(LivePlayerOnlyActivity.this, "主播开始推流", Toast.LENGTH_SHORT).show();
                    if (mPlayer.resumeAble())
                        mPlayer.resume();
                    break;
                case Constants.Event.EVENT_STREAM_STOP://发起端停止
                    if (!mPlayer.isPlaying()) {
                        return;
                    }
                    Toast.makeText(LivePlayerOnlyActivity.this, "主播停止推流", Toast.LENGTH_SHORT).show();
                    mPlayer.pause();
                    break;
                case Constants.Event.EVENT_NO_STREAM:
                    Toast.makeText(LivePlayerOnlyActivity.this, "主播端暂未推流", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(LivePlayerOnlyActivity.this, "Error message:error connect " + msg, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }


//    ------------------------------------------------------投屏相关--------------------------------------------------
    private BrowseRegistryListener registryListener = new BrowseRegistryListener();
    private DevicePopu devicePopu;
    private AndroidUpnpService upnpService;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e("Service ", "mUpnpServiceConnection onServiceConnected");
            upnpService = (AndroidUpnpService) service;
            // Clear the list
            if (devicePopu != null) {
                devicePopu.clear();
            }
            // Get ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);
            // Now add all devices to the list we already know about
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }
            // Search asynchronously for all devices, they will respond soon
            upnpService.getControlPoint().search(); // 搜索设备
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    protected class BrowseRegistryListener extends DefaultRegistryListener {

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
//            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {

        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            if (device.getType().getNamespace().equals("schemas-upnp-org") && device.getType().getType().equals("MediaRenderer")) {
                deviceAdded(device);
            }

        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
//            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
//            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (devicePopu == null) {
                        devicePopu = new DevicePopu(LivePlayerOnlyActivity.this);
                        devicePopu.setOnItemClickListener(new OnItemClick());
                    }
                    devicePopu.deviceAdded(device);
                }
            });
        }

        public void deviceRemoved(final Device device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (devicePopu == null) {
                        devicePopu = new DevicePopu(LivePlayerOnlyActivity.this);
                        devicePopu.setOnItemClickListener(new OnItemClick());
                    }
                    devicePopu.deviceRemoved(device);
                }
            });
        }
    }

    class OnItemClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final DeviceDisplay deviceDisplay = (DeviceDisplay) parent.getItemAtPosition(position);
            DMCControl dmcControl = new DMCControl(deviceDisplay, upnpService, mPlayer.getOriginalUrl(), mPlayer.getProjectionScreen());
            devicePopu.setDmcControl(dmcControl);
        }
    }

    public static final DeviceType DMR_DEVICE_TYPE = new UDADeviceType("MediaRenderer");

    public Collection<Device> getDmrDevices() {
        if (upnpService == null) {
            return null;
        }
        Collection<Device> devices = upnpService.getRegistry().getDevices(DMR_DEVICE_TYPE);
        return devices;
    }

    public void showDevices() {
        if (devicePopu == null) {
            devicePopu = new DevicePopu(this);
            devicePopu.setOnItemClickListener(new OnItemClick());
        }
        devicePopu.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    public void dismissDevices() {
        if (devicePopu != null) {
            devicePopu.dismiss();
        }
    }
}