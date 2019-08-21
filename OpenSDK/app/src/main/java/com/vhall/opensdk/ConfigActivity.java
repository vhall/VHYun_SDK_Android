package com.vhall.opensdk;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

import static android.Manifest.permission.CAMERA;

public class ConfigActivity extends Activity {

    public static final String KEY_BROADCAST_ID = "broid";
    public static final String KEY_PIX_TYPE = "pix_type";
    public static final String KEY_LSS_ID = "lss_id";
    public static final String KEY_VOD_ID = "vod_id";
    public static final String KEY_CHAT_ID = "chat_id";
    public static final String KEY_INAV_ID = "inav_id";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_DOC_ID ="doc_id";

    EditText etBroid, etLss, etVod, etInav, etChat,etDoc,etToken;
    RadioGroup rg;
    RadioButton rbSd, rbHd, rbUhd;
    TextView etPix;
    SharedPreferences sp;
    Button mBtnSave;
    int i = 0;
    LinearLayout llCamera;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_layout);
        etBroid = this.findViewById(R.id.et_broid);
        rg = this.findViewById(R.id.rg);
        etPix = this.findViewById(R.id.et_pix);
        rbSd = this.findViewById(R.id.rb_sd);
        rbHd = this.findViewById(R.id.rb_hd);
        rbUhd = this.findViewById(R.id.rb_uhd);
        mBtnSave = this.findViewById(R.id.btn_save);
        llCamera = this.findViewById(R.id.ll_camera);
        etLss = findViewById(R.id.edt_lss_room_id);
        etVod = findViewById(R.id.edt_vod_room_id);
        etInav = findViewById(R.id.edt_inav_room_id);
        etChat = findViewById(R.id.edt_ch_room_id);
        etDoc = findViewById(R.id.edt_doc_id);
        etToken = findViewById(R.id.edt_token);

        sp = this.getSharedPreferences("config", MODE_PRIVATE);
        etBroid.setText(sp.getString(KEY_BROADCAST_ID, ""));
        etToken.setText(sp.getString(KEY_TOKEN,""));
        etDoc.setText(sp.getString(KEY_DOC_ID,""));
        etChat.setText(sp.getString(KEY_CHAT_ID,""));
        etLss.setText(sp.getString(KEY_LSS_ID,""));
        etVod.setText(sp.getString(KEY_VOD_ID,""));
        etInav.setText(sp.getString(KEY_INAV_ID,""));

        int pix = sp.getInt(KEY_PIX_TYPE, 0);//0sd 1hd 2uhd
        showParams(pix);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_sd:
                        showParams(0);
                        break;
                    case R.id.rb_hd:
                        showParams(1);
                        break;
                    case R.id.rb_uhd:
                        showParams(2);
                        break;
                }
            }
        });
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(KEY_BROADCAST_ID, etBroid.getText().toString());
                editor.putInt(KEY_PIX_TYPE, i);
                editor.putString(KEY_TOKEN,etToken.getText().toString().trim());
                editor.putString(KEY_LSS_ID,etLss.getText().toString().trim());
                editor.putString(KEY_VOD_ID,etVod.getText().toString().trim());
                editor.putString(KEY_INAV_ID,etInav.getText().toString().trim());
                editor.putString(KEY_CHAT_ID,etChat.getText().toString().trim());
                editor.putString(KEY_DOC_ID,etDoc.getText().toString().trim());

                editor.commit();
                finish();
            }
        });
        openCamera();
    }

    private void showParams(int type) {
        i = type;
        switch (type) {
            case 0:
                rbSd.setChecked(true);
                etPix.setText("176*144");
                break;
            case 1:
                rbHd.setChecked(true);
                etPix.setText("320*240");
                break;
            case 2:
                rbUhd.setChecked(true);
                etPix.setText("480*360");
                break;
        }
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
