package com.vhall.opensdk;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

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
        view = (VHDocumentContainer) this.findViewById(R.id.doc);
        document = new VHDocument(mChannelId, mAccessToken);
        document.setDocumentView(view);
        document.join();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        document.leave();
    }
}
