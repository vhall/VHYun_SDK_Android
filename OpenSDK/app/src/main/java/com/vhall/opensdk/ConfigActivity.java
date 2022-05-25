package com.vhall.opensdk;

import static android.Manifest.permission.CAMERA;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


import com.vhall.push.VHLivePushFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigActivity extends Activity {

    public static final String KEY_APP_ID = "app_id";
    public static final String KEY_BROADCAST_ID = "broid";
    public static final String KEY_PIX_TYPE = "pix_type";
    public static final String KEY_LSS_ID = "lss_id";
    public static final String KEY_VOD_ID = "vod_id";
    public static final String KEY_CHAT_ID = "chat_id";
    public static final String KEY_INAV_ID = "inav_id";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_DOC_ID = "doc_id";
    public static final String KEY_DOC_WIDTH_PRO="width_pro";
    public static final String KEY_DOC_HEIGHT_PRO="height_pro";
    public static final String KEY_DOC_WIDTH_LAN="width_lan";
    public static final String KEY_DOC_HEIGHT_LAN="height_lan";
    private ArrayAdapter<String> mAdapter;

    EditText etBroid, etLss, etVod, etInav, etChat, etDoc, etToken,appId;
    SharedPreferences sp;
    Button mBtnSave;
    LinearLayout llCamera;
    private Spinner mSpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_layout);
        etBroid = this.findViewById(R.id.et_broid);
        mBtnSave = this.findViewById(R.id.btn_save);
        llCamera = this.findViewById(R.id.ll_camera);
        etLss = findViewById(R.id.edt_lss_room_id);
        etVod = findViewById(R.id.edt_vod_room_id);
        etInav = findViewById(R.id.edt_inav_room_id);
        etChat = findViewById(R.id.edt_ch_room_id);
        etDoc = findViewById(R.id.edt_doc_id);
        etToken = findViewById(R.id.edt_token);
        appId = findViewById(R.id.appId);

        sp = this.getSharedPreferences("config", MODE_PRIVATE);

        mSpinner = findViewById(R.id.config_defination);

        prepareDefinationList();

        mAdapter = new ArrayAdapter(this, R.layout.item_defination, parseDefinationKeySet());
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSpinner.setSelection(mAdapter.getPosition(findKeyByValue(sp.getInt(KEY_PIX_TYPE, VHLivePushFormat.PUSH_MODE_HD))));

        etBroid.setText(sp.getString(KEY_BROADCAST_ID, ""));
        etToken.setText(sp.getString(KEY_TOKEN, ""));
        etDoc.setText(sp.getString(KEY_DOC_ID, ""));
        etChat.setText(sp.getString(KEY_CHAT_ID, ""));
        etLss.setText(sp.getString(KEY_LSS_ID, ""));
        etVod.setText(sp.getString(KEY_VOD_ID, ""));
        etInav.setText(sp.getString(KEY_INAV_ID, ""));
        appId.setText(sp.getString(KEY_APP_ID, ""));

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(KEY_BROADCAST_ID, etBroid.getText().toString());
                editor.putInt(KEY_PIX_TYPE, mDefinationMapping.get(mSpinner.getSelectedItem().toString()));
                editor.putString(KEY_TOKEN, etToken.getText().toString().trim());
                editor.putString(KEY_LSS_ID, etLss.getText().toString().trim());
                editor.putString(KEY_VOD_ID, etVod.getText().toString().trim());
                editor.putString(KEY_INAV_ID, etInav.getText().toString().trim());
                editor.putString(KEY_CHAT_ID, etChat.getText().toString().trim());
                editor.putString(KEY_DOC_ID, etDoc.getText().toString().trim());
                editor.putString(KEY_APP_ID, appId.getText().toString().trim());

                editor.commit();
                finish();
            }
        });
        openCamera();
    }

    private String findKeyByValue(int value) {
        if(null != mDefinationMapping){
            for(String key: mDefinationMapping.keySet()){
                if(mDefinationMapping.get(key).equals(value)){
                    return key;
                }
            }
        }
        return "";
    }

    private List<String> parseDefinationKeySet() {
        return new ArrayList<>(mDefinationMapping.keySet());
    }

    private HashMap<String, Integer> mDefinationMapping;

    private void prepareDefinationList() {
        mDefinationMapping = new HashMap<>();
        mDefinationMapping.put("640*480", VHLivePushFormat.PUSH_MODE_HD);
        mDefinationMapping.put("640*480-25fps", VHLivePushFormat.PUSH_MODE_HD_25);
        mDefinationMapping.put("1280*720", VHLivePushFormat.PUSH_MODE_XHD);
        mDefinationMapping.put("1280*720-25fps", VHLivePushFormat.PUSH_MODE_XHD_25);
        mDefinationMapping.put("1920*1080", VHLivePushFormat.PUSH_MODE_XXHD);
        mDefinationMapping.put("1920*1080-25fps", VHLivePushFormat.PUSH_MODE_XXHD_25);
    }

    private void openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) {
                open();
            } else {
                requestPermissions(new String[]{CAMERA}, 1);
            }
        } else {
            open();
        }
    }

    public void open() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int count = Camera.getNumberOfCameras();
        TextView tvNo = new TextView(this);
        tvNo.setText("摄像头数：" + count);
        llCamera.addView(tvNo);
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            TextView tvCamera = new TextView(this);
            StringBuilder text = new StringBuilder();
            text.append("camera" + cameraId + ":");
            Camera.getCameraInfo(cameraId, cameraInfo);
            Camera camera = Camera.open(cameraId);
            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> size = params.getSupportedPreviewSizes();
            for (int i = 0; i < size.size(); i++) {
                Camera.Size s = size.get(i);
                text.append("[").append(s.width).append(",").append(s.height).append("],");
            }
            tvCamera.setText(text.toString());
            llCamera.addView(tvCamera);
            camera.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            open();
        }
    }
}
