package com.vhall.opensdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.vhall.framework.VhallSDK;
import com.vhall.framework.logger.LogLevel;

/**
 * Created by Hank on 2018/3/9.
 */
public class SplashActivity extends Activity {

    EditText mEditAppid;
    EditText mEditUserid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        mEditAppid = (EditText) this.findViewById(R.id.et_appid);
        mEditUserid = (EditText) this.findViewById(R.id.et_userid);
    }

    public void enter(View view) {

        String appid = mEditAppid.getText().toString();
        String userid = mEditUserid.getText().toString();

        if (!TextUtils.isEmpty(appid)) {
            VhallSDK.getInstance().setLogLevel(LogLevel.FULL);
            VhallSDK.getInstance().nativeLog(true);
            VhallSDK.getInstance().init(getApplicationContext(), appid, userid, new VhallSDK.InitCallback() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onFailure(String msg) {

                }
            });

        }
    }
}
