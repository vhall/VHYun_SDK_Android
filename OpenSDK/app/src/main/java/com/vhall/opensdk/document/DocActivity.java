package com.vhall.opensdk.document;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.vhall.opensdk.R;
import com.vhall.opensdk.util.SpUtils;

/**
 * Created by Hank on 2017/12/18.
 * <p>
 * 纯文档直播
 */
public class DocActivity extends AppCompatActivity {
    private static final String TAG = "DocActivity";
    private String mChannelId = "";
    protected String mRoomId = "";
    protected String mAccessToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRoomId = SpUtils.share().getBroadcastId();
        mChannelId = SpUtils.share().getChatId();
        mAccessToken = SpUtils.share().getToken();

        if (TextUtils.isEmpty(mRoomId)) {
            mRoomId = mChannelId;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.doc_layout);
        initView();
    }

    private DocFragment mDocFragment;

    public void hideDocFragment() {
        if (null != mDocFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(mDocFragment);
            transaction.commit();
        }
    }

    public void showDocFragment() {
        if (null != mDocFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (null == getSupportFragmentManager().findFragmentById(R.id.container_doc)) {
                transaction.add(R.id.container_doc, mDocFragment);
            } else {
                transaction.show(mDocFragment);
            }
            transaction.commit();
        }
    }

    private void initView() {
        if (null == mDocFragment) {
            mDocFragment = DocFragment.getInstance(false);
        }
        showDocFragment();
    }

    /**
     * 重要
     * 为了保证生成的回放文档播放正常，每次开始推流必需调用下面接口
     */
    protected void sendSpecial() {
        if (null != mDocFragment) {
            mDocFragment.sendSpecial();
        }
    }
}