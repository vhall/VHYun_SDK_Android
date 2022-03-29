
package com.vhall.opensdk;

import static android.Manifest.permission.READ_PHONE_STATE;

import android.content.Intent;
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
import android.widget.Toast;

import com.vhall.framework.VhallSDK;
import com.vhall.jni.VhallLiveApi;
import com.vhall.logmanager.L;
import com.vhall.logmanager.LogReporter;
import com.vhall.opensdk.util.SpUtils;

import org.json.JSONException;
import org.json.JSONObject;

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

    EditText mEditNickName;
    EditText mEditAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);

        userId = SpUtils.share().getUserId();
        mEditAppid = this.findViewById(R.id.et_appid);
        mEditUserid = this.findViewById(R.id.et_userid);
        mEditNickName = this.findViewById(R.id.et_nickname);
        mEditAvatar = this.findViewById(R.id.et_avatar);
        mEditAppid.setText(SpUtils.share().getAppId());
        mEditUserid.setText(userId);
        mEditNickName.setText(SpUtils.share().getNickName());
        mEditAvatar.setText(SpUtils.share().getAvatar());
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
        String nickName = mEditNickName.getText().toString();
        String avatar = mEditAvatar.getText().toString();
        SpUtils.share().commitStr("userId",userid);
        SpUtils.share().commitStr(ConfigActivity.KEY_APP_ID,appid);
        if (!TextUtils.isEmpty(appid)) {
            if (checkBox.isChecked()) {
                VhallSDK.getInstance().setLogLevel(L.LogLevel.FULL);
                VhallSDK.getInstance().init(getApplicationContext(), appid, userid);//初始化成功会打印日志：初始化成功！，请确保注册的appid与当前应用包名签名一致
            } else {
                VhallSDK.getInstance().setLogLevel(L.LogLevel.FULL);
                VhallSDK.getInstance().init(getApplicationContext(), appid, userid, "test-api.vhallyun.com");//初始化成功会打印日志：初始化成功！，请确保注册的appid与当前应用包名签名一致
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }
                LogReporter.getInstance().setDebug(true);
                VhallLiveApi.EnableDebug(true);
            }

            JSONObject obj = new JSONObject();
            try {
                if (!TextUtils.isEmpty(userid)) {
                    obj.put("third_party_user_id", userid);
                }
                if (!TextUtils.isEmpty(nickName)) {
                    SpUtils.share().commitStr("nickname",nickName);
                    obj.put("nickName", nickName);
                    obj.put("nick_name", nickName);
                }
                if (!TextUtils.isEmpty(avatar)) {
                    SpUtils.share().commitStr("avatar",avatar);
                    obj.put("avatar", avatar);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            VhallSDK.getInstance().setUserInfo(obj.toString());

            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra("selfId",mEditUserid.getText().toString());
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "appid不能为空", Toast.LENGTH_LONG).show();
        }
    }
}

