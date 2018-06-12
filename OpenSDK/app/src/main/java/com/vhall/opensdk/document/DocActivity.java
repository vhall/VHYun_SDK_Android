package com.vhall.opensdk.document;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.vhall.framework.VhallConnectService;
import com.vhall.opensdk.R;
import com.vhall.ops.VHDocument;
import com.vhall.ops.VHDocumentContainer;

/**
 * Created by Hank on 2017/12/18.
 */
public class DocActivity extends Activity {

    VHDocument document;
    VHDocumentContainer view;
    private String mChannelId = "";
    private String mAccessToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChannelId = getIntent().getStringExtra("channelid");
        mAccessToken = getIntent().getStringExtra("token");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.doc_layout);
        view = this.findViewById(R.id.doc);
        document = new VHDocument(mChannelId, mAccessToken);
        document.setDocumentView(view);
        document.setOnConnectChangedListener(new VhallConnectService.OnConnectChangedListener() {
            @Override
            public void onConnectChanged(int code) {
                String text = code == 1 ? "连接成功！" : "连接失败";
                Toast.makeText(DocActivity.this, "网络：" + text, Toast.LENGTH_SHORT).show();
            }
        });
        document.join();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        document.leave();
    }
}