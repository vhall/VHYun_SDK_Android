
package com.vhall.opensdk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;

import com.vhall.document.DocumentView;
import com.vhall.framework.VhallSDK;
import com.vhall.jni.VhallLiveApi;
import com.vhall.logmanager.L;
import com.vhall.logmanager.LogReporter;
import com.vhall.opensdk.util.SpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.READ_PHONE_STATE;

/**
 * Created by Hank on 2018/3/9.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int REQUEST_READ_PHONE_STATE = 0;
    EditText mEditAppid;
    EditText mEditUserid;
    CheckBox checkBox;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);

        userId = SpUtils.share().getUserId();
        mEditAppid = this.findViewById(R.id.et_appid);
        mEditUserid = this.findViewById(R.id.et_userid);
        mEditAppid.setText(SpUtils.share().getAppId());
        mEditUserid.setText(userId);
        checkBox = findViewById(R.id.cb_env);
        getPermission();

    }

    //初始化SDK需要读取手机信息做信息统计，如果取不到权限，信息为空，不影响SDK使用
    private void getPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        if (checkSelfPermission(READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
            return;
        requestPermissions(new String[]{READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "get READ_PHONE_STATE permission success");
            }
        }
    }

    public void enter(View view) {

        String appid = mEditAppid.getText().toString();
        String userid = mEditUserid.getText().toString();
        SpUtils.share().commitStr("userId",userid);
        SpUtils.share().commitStr(ConfigActivity.KEY_APP_ID,appid);
        if (!TextUtils.isEmpty(appid)) {
            if (checkBox.isChecked()) {
                //正式环境
                VhallSDK.getInstance().setLogLevel(L.LogLevel.FULL);
//                VhallSDK.getInstance().setPackageCheck("com.vhallsaas.sdk","241A634279A943313DCF69893E5B079A");
                VhallSDK.getInstance().init(getApplicationContext(), appid, userid);//初始化成功会打印日志：初始化成功！，请确保注册的appid与当前应用包名签名一致
//                DocumentView.setHost("https://static.vhallyun.com/jssdk/doc-sdk/dist/release/mobile.html");
            } else {
                VhallSDK.getInstance().setLogLevel(L.LogLevel.FULL);
                //测试环境
//                VhallSDK.getInstance().setPackageCheck("com.vhallsaas.sdk","241A634279A943313DCF69893E5B079A");
                //test-api.vhallyun.com
                //t-open.e.vhall.com
                VhallSDK.getInstance().init(getApplicationContext(), appid, userid, "test-api.vhallyun.com");//初始化成功会打印日志：初始化成功！，请确保注册的appid与当前应用包名签名一致
//                DocumentView.setHost("https://t-static01-open.e.vhall.com/jssdk/doc-sdk/dist/dev/mobile.html");
//                DocumentView.setHost("https://t-static01-open.e.vhall.com/jssdk/doc-sdk/dist/dev/mobile1.1.9.html");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }
                LogReporter.getInstance().setDebug(true);
                VhallLiveApi.EnableDebug(true);

            }
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}

