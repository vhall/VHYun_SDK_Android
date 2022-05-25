package com.vhall.opensdk.watchlive;

import static com.vhall.ops.VHOPS.ERROR_CONNECT;
import static com.vhall.ops.VHOPS.ERROR_DOC_INFO;
import static com.vhall.ops.VHOPS.ERROR_SEND;
import static com.vhall.ops.VHOPS.KEY_OPERATE;
import static com.vhall.ops.VHOPS.TYPE_ACTIVE;
import static com.vhall.ops.VHOPS.TYPE_CREATE;
import static com.vhall.ops.VHOPS.TYPE_DESTROY;
import static com.vhall.ops.VHOPS.TYPE_RESET;
import static com.vhall.ops.VHOPS.TYPE_SWITCHOFF;
import static com.vhall.ops.VHOPS.TYPE_SWITCHON;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.document.DocumentView;
import com.vhall.opensdk.R;
import com.vhall.opensdk.WatermarkConfigActivity;
import com.vhall.ops.VHOPS;
import com.vhall.ops.WatermarkOption;

import org.json.JSONException;
import org.json.JSONObject;

public class DocPlayerOnlyActivity extends Activity {

    private static final String TAG = "DocPlayerOnlyActivity";

    protected String roomId = "";
    protected String channelId = "";
    protected String accessToken = "";
    protected VHOPS mDocument;
    protected RelativeLayout rlOpsContainer, rlPlayerContent;
    private CheckBox cbFullScreen;
    private TextView tvResize;
    private EditText edtWidth, edtHeight;
    private WatermarkOption mWatermarkOption;
    private SharedPreferences mSp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        roomId = getIntent().getStringExtra("roomId");
        channelId = getIntent().getStringExtra("channelId");

        if (TextUtils.isEmpty(roomId)) {
            roomId = channelId;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        accessToken = getIntent().getStringExtra("token");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.player_layout);

        rlPlayerContent = findViewById(R.id.rl_player_content);
        rlOpsContainer = findViewById(R.id.rl_ops_container);
        cbFullScreen = findViewById(R.id.cb_fullscreen);
        tvResize = findViewById(R.id.tv_resize);
        edtWidth = findViewById(R.id.edt_doc_width);
        edtHeight = findViewById(R.id.edt_doc_height);

        tvResize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDocument.getActiveView().getLayoutParams().width = Integer.valueOf(edtWidth.getText().toString());
                mDocument.getActiveView().getLayoutParams().height = Integer.valueOf(edtHeight.getText().toString());

                mDocument.getActiveView().setLayoutParams(mDocument.getActiveView().getLayoutParams());

            }
        });

        cbFullScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });

        mSp = getSharedPreferences(WatermarkConfigActivity.SP_NAME, MODE_PRIVATE);
        initWatermarkOption();

        joinDoc();
    }

    private void initWatermarkOption() {
        String markStr = mSp.getString(WatermarkConfigActivity.KEY_TEXT, "");
        if (TextUtils.isEmpty(markStr)) {
            mWatermarkOption = null;
            return;
        }
        mWatermarkOption = new WatermarkOption(
                markStr,
                mSp.getString(WatermarkConfigActivity.KEY_COLOR, ""),
                Integer.valueOf(mSp.getString(WatermarkConfigActivity.KEY_ANGLE, "")),
                Float.valueOf(mSp.getString(WatermarkConfigActivity.KEY_OPACITY, "")),
                Integer.valueOf(mSp.getString(WatermarkConfigActivity.KEY_FONTSIZE, ""))
        );
    }

    private void joinDoc() {
        mDocument = new VHOPS(this, channelId, roomId, accessToken, true);
        if (null != mWatermarkOption) {
            mDocument.setWatermark(mWatermarkOption);
        }
        mDocument.setListener(opsListener);
        mDocument.join();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            rlPlayerContent.setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            rlPlayerContent.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private VHOPS.EventListener opsListener = new VHOPS.EventListener() {
        @Override
        public void onEvent(String event, String type, String cid) {
            if (event.equals(KEY_OPERATE)) {
                if (type.equals(TYPE_RESET)) {
                    rlOpsContainer.removeAllViews();
                } else if (type.equals(TYPE_ACTIVE)) {
                    if (rlOpsContainer != null) {
                        rlOpsContainer.removeAllViews();
                        DocumentView mDocView = mDocument.getActiveView();
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        params.addRule(RelativeLayout.CENTER_IN_PARENT);
                        rlOpsContainer.addView(mDocView, params);
                    }
                } else if (type.equals(TYPE_CREATE)) {
                    //创建文档

                } else if (type.equals(TYPE_DESTROY)) {
                    //删除编号 cid的文档

                } else if (type.equals(TYPE_SWITCHOFF)) {
                    //关闭文档演示
                    rlOpsContainer.setVisibility(View.INVISIBLE);
                } else if (type.equals(TYPE_SWITCHON)) {
                    //打开文档演示
                    rlOpsContainer.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onError(int errorCode, int innerError, String errorMsg) {
            switch (errorCode) {
                case ERROR_CONNECT:
                case ERROR_SEND:
                    Toast.makeText(DocPlayerOnlyActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_DOC_INFO:
                    try {
                        JSONObject obj = new JSONObject(errorMsg);
                        String msg = obj.optString("msg");
                        String cid = obj.optString("cid");
                        Toast.makeText(DocPlayerOnlyActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDocument.leave();
    }

    public void wmark_setting(View view) {
        startActivity(new Intent(this, WatermarkConfigActivity.class));
    }

    public void wmark_reload(View view) {
        initWatermarkOption();
        if (null != mWatermarkOption) {
            mDocument.leave();
            joinDoc();
        } else {
            Toast.makeText(this, "水印为空", Toast.LENGTH_SHORT).show();
        }
    }
}