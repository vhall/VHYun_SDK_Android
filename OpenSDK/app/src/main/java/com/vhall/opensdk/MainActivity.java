package com.vhall.opensdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.vhall.framework.VhallSDK;

/**
 * Created by Hank on 2017/12/8.
 */
public class MainActivity extends Activity {

    EditText et_channelid, et_token;
    TextView tv_appid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        et_channelid = (EditText) this.findViewById(R.id.et_channelid);
        et_token = (EditText) this.findViewById(R.id.et_token);
        tv_appid = (TextView) this.findViewById(R.id.tv_appid);
        tv_appid.setText(VhallSDK.getInstance().getAPP_ID());
    }

    public void push(View view) {
        Intent intent = new Intent(this, PushActivity.class);
        startAct(intent);
    }

    public void playlive(View view) {
        Intent intent = new Intent(this, LivePlayerActivity.class);
        startAct(intent);
    }

    public void playvod(View view) {
        Intent intent = new Intent(this, VodPlayerActivity.class);
        startAct(intent);
    }

    public void showDoc(View view) {
        Intent intent = new Intent(this, DocActivity.class);
        startAct(intent);
    }

    public void showIM(View view) {
        Intent intent = new Intent(this, IMActivity.class);
        startAct(intent);
    }

    private void startAct(Intent intent) {
        String roomid = et_channelid.getText().toString();
        String token = et_token.getText().toString();
        if (TextUtils.isEmpty(roomid) || TextUtils.isEmpty(token))
            return;
        intent.putExtra("channelid", roomid);
        intent.putExtra("token", token);
        startActivity(intent);
    }
}
