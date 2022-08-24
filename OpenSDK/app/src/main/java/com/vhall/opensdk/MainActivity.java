package com.vhall.opensdk;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.vhall.opensdk.ConfigActivity.KEY_CHAT_ID;
import static com.vhall.opensdk.ConfigActivity.KEY_INAV_ID;
import static com.vhall.opensdk.ConfigActivity.KEY_LSS_ID;
import static com.vhall.opensdk.ConfigActivity.KEY_TOKEN;
import static com.vhall.opensdk.ConfigActivity.KEY_VOD_ID;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.vhall.beautify.IVHBeautifyInitListener;
import com.vhall.beautify.VHBeautifyKit;
import com.vhall.framework.VhallBaseSDK;
import com.vhall.framework.VhallSDK;
import com.vhall.opensdk.document.DocActivity;
import com.vhall.opensdk.document.DocLiveActivity;
import com.vhall.opensdk.document.UploadDocumentActivity;
import com.vhall.opensdk.im.IMActivity;
import com.vhall.opensdk.interactive.InteractiveActivity;
import com.vhall.opensdk.push.PushActivity;
import com.vhall.opensdk.push.PushWithBeautifyActivity;
import com.vhall.opensdk.push.PushWithIMActivity;
import com.vhall.opensdk.screenRecord.ScreenRecordActivity;
import com.vhall.opensdk.upload.UploadActivity;
import com.vhall.opensdk.util.SpUtils;
import com.vhall.opensdk.watchlive.DocPlayerOnlyActivity;
import com.vhall.opensdk.watchlive.FastLivePlayerActivity;
import com.vhall.opensdk.watchlive.LivePlayerActivity;
import com.vhall.opensdk.watchlive.LivePlayerOnlyActivity;
import com.vhall.opensdk.watchlive.LivePlayerUiActivity;
import com.vhall.opensdk.watchlive.TimeShiftPlayerActivity;
import com.vhall.opensdk.watchplayback.VodPlayerActivity;
import com.vhall.opensdk.watchplayback.VodPlayerDocActivity;
import com.vhall.opensdk.watchplayback.VodPlayerUiActivity;

/**
 * Created by Hank on 2017/12/8.
 */
public class MainActivity extends Activity {

    TextView tv_appid;
    Button mBtnConfig;
    private static final String TAG = "VHLivePusher";
    private static final int REQUEST_PUSH = 0;
    private static final int REQUEST_STORAGE = 1;
    private static final int REQUEST_INTERACTIVE = 2;
    private static final int REQUEST_UPLOAD = 3;
    private static final int REQUEST_AUDIO_RECORD = 4;
    private static final int REQUEST_PUSH_WITH_DOC = 5;
    private static final int REQUEST_AUDIO_FLSS = 6;

    SharedPreferences sp;
    private String token;
    private String roomid;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        tv_appid = this.findViewById(R.id.tv_appid);
        mBtnConfig = this.findViewById(R.id.btn_config);
        tv_appid.setText(VhallSDK.getInstance().getAPP_ID());
        mBtnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                startActivity(intent);
            }
        });
        chatId = SpUtils.share().getChatId();
        token = sp.getString(KEY_TOKEN, "");
        initBeautifySDK();
    }

    private void initBeautifySDK() {
        VhallBaseSDK.getInstance().initBeautify(token, new IVHBeautifyInitListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "美颜sdk:" + VHBeautifyKit.getInstance().sdkModel() + "初始化成功", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(int errCode, String errMsg) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, errCode + " - " + errMsg, Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void pushOnly(View view) {
        chatId = SpUtils.share().getChatId();
        roomid = sp.getString(KEY_LSS_ID, "");
        if (getPushPermission(REQUEST_PUSH)) {
            Intent intent = new Intent(this, PushActivity.class);
            startAct(intent);
        }
    }

    public void pushWithIM(View view) {
        chatId = SpUtils.share().getChatId();
        roomid = sp.getString(KEY_LSS_ID, "");
        if (getPushPermission(REQUEST_PUSH)) {
            Intent intent = new Intent(this, PushWithIMActivity.class);
            startAct(intent);
        }
    }

    public void pushWithBeautify(View view) {
        chatId = SpUtils.share().getChatId();
        roomid = sp.getString(KEY_LSS_ID, "");
        if (getPushPermission(REQUEST_PUSH)) {
            Intent intent = new Intent(this, PushWithBeautifyActivity.class);
            intent.putExtra("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            startAct(intent);
        }
    }

    public void pushWithBeautifyLand(View view) {
        chatId = SpUtils.share().getChatId();
        roomid = sp.getString(KEY_LSS_ID, "");
        if (getPushPermission(REQUEST_PUSH)) {
            Intent intent = new Intent(this, PushWithBeautifyActivity.class);
            intent.putExtra("orientation", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            startAct(intent);
        }
    }

    public void playlive(View view) {
        chatId = SpUtils.share().getChatId();
        roomid = sp.getString(KEY_LSS_ID, "");
        Intent intent = new Intent(this, LivePlayerActivity.class);
        startAct(intent);
    }

    public void playliveOnly(View view) {
        chatId = SpUtils.share().getChatId();
        roomid = sp.getString(KEY_LSS_ID, "");
        Intent intent = new Intent(this, LivePlayerOnlyActivity.class);
        startAct(intent);
    }
    public void playRtcLive(View view) {
        roomid = sp.getString(KEY_LSS_ID, "");
        if (getAudioRecordPermission(REQUEST_AUDIO_FLSS)) {
            startRtcLive();
        }
    }

    private void startRtcLive() {
        Intent intent = new Intent(this, FastLivePlayerActivity.class);
        intent.putExtra("inavId", sp.getString(KEY_INAV_ID, ""));
        startAct(intent);
    }

    public void playvod(View view) {
        chatId = SpUtils.share().getChatId();
        roomid = sp.getString(KEY_VOD_ID, "");
        if (getStoragePermission()) {
            Intent intent = new Intent(this, VodPlayerActivity.class);
            startAct(intent);
        }
    }

    //观看回放需要下载、保存和读取文档信息
    public void playdocvod(View view) {
        chatId = SpUtils.share().getChatId();
        roomid = sp.getString(KEY_VOD_ID, "");
        if (getStoragePermission()) {
            Intent intent = new Intent(this, VodPlayerDocActivity.class);
            startAct(intent);
        }
    }

    public void timeshift(View view) {
        chatId = SpUtils.share().getChatId();
        roomid = sp.getString(KEY_LSS_ID, "");
        Intent intent = new Intent(this, TimeShiftPlayerActivity.class);
        startAct(intent);
    }

    //需要文件读取权限
    public void uploadDocument(View view) {
        roomid = sp.getString(KEY_CHAT_ID, "");
        if (getUploadPermission()) {
            Intent intent = new Intent(this, UploadDocumentActivity.class);
            startAct(intent);
        }
    }

    public void showDoc(View view) {
        roomid = sp.getString(KEY_LSS_ID, "");
        chatId = SpUtils.share().getChatId();
        if (getPushPermission(REQUEST_PUSH_WITH_DOC)) {
            Intent intent = new Intent(this, DocLiveActivity.class);
            startAct(intent);
        }
    }

    public void pushdocOnly(View view) {
        roomid = sp.getString(KEY_LSS_ID, "");
        chatId = SpUtils.share().getChatId();
        if (getPushPermission(REQUEST_PUSH_WITH_DOC)) {
            Intent intent = new Intent(this, DocActivity.class);
            startAct(intent);
        }
    }

    public void showDocOnly(View view) {
        chatId = SpUtils.share().getChatId();
        roomid = sp.getString(KEY_LSS_ID, "");
        Intent intent = new Intent(this, DocPlayerOnlyActivity.class);
        startAct(intent);
    }

    public void docWatermark(View view) {
        startActivity(new Intent(this, WatermarkConfigActivity.class));
    }

    public void showIM(View view) {
        roomid = sp.getString(KEY_LSS_ID, "");
        chatId = SpUtils.share().getChatId();
        Intent intent = new Intent(this, IMActivity.class);
        startAct(intent);
    }

    public void showInteractive(View view) {
        roomid = sp.getString(KEY_INAV_ID, "");
        if (getPushPermission(REQUEST_INTERACTIVE)) {
            Intent intent = new Intent(this, InteractiveActivity.class);
            startAct(intent);
        }
    }

    public void showInteractiveBeautify(View view) {
        roomid = sp.getString(KEY_INAV_ID, "");
        if (getPushPermission(REQUEST_INTERACTIVE)) {
            Intent intent = new Intent(this, InteractiveActivity.class);
            intent.putExtra("beautify", true);
            startAct(intent);
        }
    }

    public void showScreenRecordInteractive(View view) {
        roomid = sp.getString(KEY_INAV_ID, "");
        if (getPushPermission(REQUEST_INTERACTIVE)) {
            Intent intent = new Intent(this, InteractiveActivity.class);
            intent.putExtra("type", InteractiveActivity.SCREEN_RECORD_LIVE);
            startAct(intent);
        }
    }

    //观看 无延时直播 只订阅无推流
    public void showNodelayLive(View view) {
        roomid = sp.getString(KEY_INAV_ID, "");
        Intent intent = new Intent(this, InteractiveActivity.class);
        intent.putExtra("type", InteractiveActivity.NODELAY_LIVE);
        intent.putExtra("action", InteractiveActivity.NODELAY_ACTION_WATCH);
        startAct(intent);
    }

    //发起 无延时直播
    public void pushNodelayLive(View view) {
        roomid = sp.getString(KEY_INAV_ID, "");
        Intent intent = new Intent(this, InteractiveActivity.class);
        intent.putExtra("type", InteractiveActivity.NODELAY_LIVE);
        intent.putExtra("action", InteractiveActivity.NODELAY_ACTION_PUSH);
        startAct(intent);
    }

    public void showScreenRecord(View view) {
        roomid = sp.getString(KEY_LSS_ID, "");
        if (getAudioRecordPermission(REQUEST_AUDIO_RECORD)) {
            Intent intent = new Intent(this, ScreenRecordActivity.class);
            startAct(intent);
        }
    }

    public void showUiVod(View view) {
        roomid = sp.getString(KEY_VOD_ID, "");
        Intent intent = new Intent(this, VodPlayerUiActivity.class);
        startAct(intent);
    }

    public void showUiLss(View view) {
        roomid = sp.getString(KEY_LSS_ID, "");
        Intent intent = new Intent(this, LivePlayerUiActivity.class);
        startAct(intent);
    }

    public void showUpload(View view) {
        if (getStoragePermission()) {
            Intent intent = new Intent(this, UploadActivity.class);
            startActivity(intent);
        }
    }

    private boolean getPushPermission(int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        Log.e(TAG, "CAMERA:" + checkSelfPermission(CAMERA) + " MIC:" + checkSelfPermission(RECORD_AUDIO));
        if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        requestPermissions(new String[]{CAMERA, RECORD_AUDIO}, requestCode);
        return false;
    }

    private boolean getAudioRecordPermission(int reqCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        requestPermissions(new String[]{RECORD_AUDIO}, reqCode);
        return false;
    }

    // 0 vod, 1 upload
    private boolean getStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        return false;
    }

    private boolean getUploadPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, REQUEST_UPLOAD);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PUSH) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "get REQUEST_PUSH permission success");
                Intent intent = new Intent(this, PushActivity.class);
                startAct(intent);
            }
        } else if (requestCode == REQUEST_INTERACTIVE) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "get REQUEST_PUSH permission success");
                Intent intent = new Intent(this, InteractiveActivity.class);
                startAct(intent);
            }
        } else if (requestCode == REQUEST_STORAGE) {
            Log.i(TAG, grantResults.length + ":" + grantResults[0]);
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent;
                intent = new Intent(this, VodPlayerActivity.class);
                startAct(intent);

            }
        } else if (requestCode == REQUEST_UPLOAD) {
            Log.i(TAG, grantResults.length + ":" + grantResults[0]);
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "get REQUEST_PUSH permission success");
                Intent intent = new Intent(this, UploadDocumentActivity.class);
                startAct(intent);
            }

        } else if (requestCode == REQUEST_AUDIO_RECORD) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, ScreenRecordActivity.class);
                startAct(intent);
            }
        } else if (requestCode == REQUEST_PUSH_WITH_DOC) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, DocActivity.class);
                startAct(intent);
            }
        } else if (requestCode == REQUEST_AUDIO_FLSS) {
            startRtcLive();
        }
    }

    private void startAct(Intent intent) {
        if (TextUtils.isEmpty(roomid) || TextUtils.isEmpty(token)) {
            Toast.makeText(getApplicationContext(), "roomid/token为空，", Toast.LENGTH_LONG).show();
//            return;
        }
        if (roomid.contains(",")) {
            String[] data = roomid.split(",");
            intent.putExtra("roomId", data[0]);
            intent.putExtra("channelId", data[1]);
            intent.putExtra("token", token);
        } else {
            intent.putExtra("roomId", roomid);
            intent.putExtra("channelId", chatId);
            intent.putExtra("token", token);
        }
        startActivity(intent);
    }
}
