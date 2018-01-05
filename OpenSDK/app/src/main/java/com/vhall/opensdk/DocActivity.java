package com.vhall.opensdk;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.vhall.framework.VhallSDK;
import com.vhall.ops.VHDocument;
import com.vhall.ops.VHDocumentView;

/**
 * Created by Hank on 2017/12/18.
 */
public class DocActivity extends Activity {

    VHDocument document;
    VHDocumentView view;
    private String mChannelId = "";
    private String mAccessToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChannelId = getIntent().getStringExtra("channelid");
        mAccessToken = getIntent().getStringExtra("token");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.doc_layout);
        view = (VHDocumentView) this.findViewById(R.id.doc);
        document = new VHDocument(mChannelId, mAccessToken);
        document.setDocumentView(view);
        document.join();
        if (!VhallSDK.getInstance().isEnable()) {
            Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        document.leave();
    }
}
