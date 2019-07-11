package com.vhall.opensdk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vhall.framework.VhallSDK;
import com.vhall.opensdk.document.DocActivity;
import com.vhall.opensdk.document.UploadDocumentActivity;
import com.vhall.opensdk.im.IMActivity;
import com.vhall.opensdk.interactive.InteractiveActivity;
import com.vhall.opensdk.push.PushActivity;
import com.vhall.opensdk.screenRecord.ScreenRecordActivity;
import com.vhall.opensdk.watchlive.LivePlayerActivity;
import com.vhall.opensdk.watchplayback.VodPlayerActivity;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.vhall.opensdk.ConfigActivity.KEY_CHAT_ID;
import static com.vhall.opensdk.ConfigActivity.KEY_INAV_ID;
import static com.vhall.opensdk.ConfigActivity.KEY_LSS_ID;
import static com.vhall.opensdk.ConfigActivity.KEY_TOKEN;
import static com.vhall.opensdk.ConfigActivity.KEY_VOD_ID;

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
    SharedPreferences sp;
    private String token;
    private String roomid;


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
    }

    public void push(View view) {
        if (getPushPermission(REQUEST_PUSH)) {
            roomid = sp.getString(KEY_LSS_ID,"");
            Intent intent = new Intent(this, PushActivity.class);
            startAct(intent);
        }
    }

    public void playlive(View view) {
        roomid = sp.getString(KEY_LSS_ID,"");
        Intent intent = new Intent(this, LivePlayerActivity.class);
        startAct(intent);
    }

    //观看回放需要下载、保存和读取文档信息
    public void playvod(View view) {
        roomid = sp.getString(KEY_VOD_ID,"");
        if (getStoragePermission()) {
            Intent intent = new Intent(this, VodPlayerActivity.class);
            startAct(intent);
        }

    }

    //需要文件读取权限
    public void uploadDocument(View view) {
        if (getUploadPermission()) {
            Intent intent = new Intent(this, UploadDocumentActivity.class);
            startAct(intent);
        }
    }

    public void showDoc(View view) {
        roomid = sp.getString(KEY_CHAT_ID,"");
        Intent intent = new Intent(this, DocActivity.class);
        startAct(intent);
    }

    public void showIM(View view) {
        roomid = sp.getString(KEY_CHAT_ID,"");
        Intent intent = new Intent(this, IMActivity.class);
        startAct(intent);
    }

    public void showInteractive(View view) {
        roomid = sp.getString(KEY_INAV_ID,"");
        if (getPushPermission(REQUEST_INTERACTIVE)) {
            Intent intent = new Intent(this, InteractiveActivity.class);
            startAct(intent);
        }
    }

    public void showScreenRecord(View view) {
        roomid = sp.getString(KEY_LSS_ID,"");
        if (getAudioRecordPermission()) {
            Intent intent = new Intent(this, ScreenRecordActivity.class);
            startAct(intent);
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

    private boolean getAudioRecordPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        requestPermissions(new String[]{RECORD_AUDIO}, REQUEST_AUDIO_RECORD);
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
        }
    }

    private void startAct(Intent intent) {
        token = sp.getString(KEY_TOKEN, "");
        if (TextUtils.isEmpty(roomid) || TextUtils.isEmpty(token))
            return;
        if (roomid.contains(",")) {
            String[] data = roomid.split(",");
            intent.putExtra("roomid", data[0]);
            intent.putExtra("channelid", data[1]);
            intent.putExtra("token", token);
        } else {
            intent.putExtra("channelid", roomid);
            intent.putExtra("token", token);
        }
        startActivity(intent);
    }
}
