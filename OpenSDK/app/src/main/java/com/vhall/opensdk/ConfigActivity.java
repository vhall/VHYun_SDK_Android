package com.vhall.opensdk;

import android.app.Activity;
import android.content.Intent;
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

    public static final String KEY_BROCASTID = "broid";
    public static final String KEY_PIX_TYPE = "pix_type";

    EditText et_broid;
    RadioGroup rg;
    RadioButton rb_sd, rb_hd, rb_uhd;
    TextView et_pix;
    SharedPreferences sp;
    Button mBtnSave;
    int i = 0;
    LinearLayout ll_camera;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_layout);
        et_broid = this.findViewById(R.id.et_broid);
        rg = this.findViewById(R.id.rg);
        et_pix = this.findViewById(R.id.et_pix);
        rb_sd = this.findViewById(R.id.rb_sd);
        rb_hd = this.findViewById(R.id.rb_hd);
        rb_uhd = this.findViewById(R.id.rb_uhd);
        mBtnSave = this.findViewById(R.id.btn_save);
        ll_camera = this.findViewById(R.id.ll_camera);
        sp = this.getSharedPreferences("config", MODE_PRIVATE);
        et_broid.setText(sp.getString(KEY_BROCASTID, ""));
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
                editor.putString(KEY_BROCASTID, et_broid.getText().toString());
                editor.putInt(KEY_PIX_TYPE, i);
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
                rb_sd.setChecked(true);
                et_pix.setText("176*144");
                break;
            case 1:
                rb_hd.setChecked(true);
                et_pix.setText("320*240");
                break;
            case 2:
                rb_uhd.setChecked(true);
                et_pix.setText("480*360");
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
        ll_camera.addView(tvNo);
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            TextView tvCamera = new TextView(this);
            StringBuilder text = new StringBuilder();
            text.append("cammera" + cameraId + ":");
            Camera.getCameraInfo(cameraId, cameraInfo);
            Camera camera = Camera.open(cameraId);
            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> size = params.getSupportedPreviewSizes();

            for (int i = 0; i < size.size(); i++) {
                Camera.Size s = size.get(i);
                text.append("[").append(s.width).append(",").append(s.height).append("],");
            }
            tvCamera.setText(text.toString());
            ll_camera.addView(tvCamera);
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
