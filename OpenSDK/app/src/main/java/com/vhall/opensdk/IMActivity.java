package com.vhall.opensdk;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.framework.VhallSDK;
import com.vhall.ims.VHIM;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by Hank on 2017/12/20.
 */
public class IMActivity extends Activity {

    private String mChannelId = "";
    private String mAccessToken = "";
    private LinearLayout ll_content;
    private EditText et;
    VHIM im;
    private OkHttpClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChannelId = getIntent().getStringExtra("channelid");
        mAccessToken = getIntent().getStringExtra("token");
        setContentView(R.layout.im_layout);
        ll_content = (LinearLayout) this.findViewById(R.id.ll_content);
        et = (EditText) this.findViewById(R.id.et);
        et.setOnEditorActionListener(new EditListener());
        im = new VHIM(mChannelId, mAccessToken);
        im.setOnMessageListener(new MsgListener());
        im.join();
        if (!VhallSDK.getInstance().isEnable())
            Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        im.leave();
    }

    class EditListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                im.sendMsg(v.getText().toString(), new VHIM.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg) {
                        Log.e("imact", "errorCode:" + errorCode + "&errorMsg:" + errorMsg);
                    }
                });
            }
            return true;
        }
    }

    class MsgListener implements VHIM.OnMessageListener {

        @Override
        public void onMessage(String msg) {
            try {
                JSONObject obj = new JSONObject(msg);
                JSONObject text = new JSONObject(URLDecoder.decode(obj.optString("text")));
                String third_party_user_id = text.optString("third_party_user_id");
                String nick_name = text.optString("nick_name");
                String avatar = text.optString("avatar");

                String time = text.optString("date_time");
                String data = text.optString("data");
                View view = View.inflate(IMActivity.this, R.layout.im_item_layout, null);
                ImageView v = (ImageView) view.findViewById(R.id.avatar);
                if (!TextUtils.isEmpty(avatar) && !avatar.equals("null")) {
                    requestAvatar(avatar, v);
                }
                TextView c = (TextView) view.findViewById(R.id.content);
                TextView t = (TextView) view.findViewById(R.id.time);
                c.setText(nick_name + ": " + data);
                t.setText(time);
                ll_content.addView(view);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestAvatar(String url, final ImageView view) {
        Request request = new Request.Builder().url(url).build();
        if (mClient == null)
            mClient = new OkHttpClient();
        Call call = mClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final byte[] picture = response.body().bytes();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setImageBitmap(BitmapFactory.decodeByteArray(picture, 0, picture.length));
                    }
                });
            }
        });
    }
}
