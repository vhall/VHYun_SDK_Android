package com.vhall.opensdk.watchplayback;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vhall.document.DocumentView;
import com.vhall.opensdk.WatermarkConfigActivity;
import com.vhall.ops.VHOPS;
import com.vhall.ops.WatermarkOption;

/**
 * @author hkl
 */
public class VodPlayerDocActivity extends VodPlayerActivity {

    private DocumentView mDocView;
    private WatermarkOption mWatermarkOption;
    private SharedPreferences mSp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rlToolContainer.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initDocument() {
        initWatermarkOption();
        mDocument = new VHOPS(accessToken, this, recordId, null);
        if (null != mWatermarkOption) {
            mDocument.setWatermark(mWatermarkOption);
        }
        mDocument.setListener(opsCallback);
        mDocument.join();
    }

    private void initWatermarkOption() {
        if (null == mSp) {
            mSp = getSharedPreferences(WatermarkConfigActivity.SP_NAME, MODE_PRIVATE);
        }
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rlToolContainer.setVisibility(View.GONE);
        } else {
            rlToolContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mDocument) {
            mDocument.leave();
        }
    }

    private VHOPS.EventListener opsCallback = new VHOPS.EventListener() {

        @Override
        public void onEvent(String event, String type, String cid) {
            if (event.equals(VHOPS.KEY_OPERATE)) {
                if (type.equals(VHOPS.TYPE_ACTIVE)) {
                    mDocView = mDocument.getActiveView();
                    rlContainer.removeAllViews();
                    if (mDocView != null) {
                        rlContainer.addView(mDocView);
                        setLayout();
                    }
                } else if (type.equals(VHOPS.TYPE_CREATE)) {
                    //创建文档
                    Log.e(TAG, "onEvent: create:" + cid);

                } else if (type.equals(VHOPS.TYPE_DESTROY)) {
                    //删除编号 cid的文档
                    Log.e(TAG, "onEvent: destroy:" + cid);
                } else if (type.equals(VHOPS.TYPE_SWITCHOFF)) {
                    //关闭文档演示
                    rlContainer.setVisibility(View.GONE);
                    Log.e(TAG, "onEvent: switchoff:" + cid);
                } else if (type.equals(VHOPS.TYPE_SWITCHON)) {
                    //打开文档演示
                    Log.e(TAG, "onEvent: switchon:" + cid);
                    rlContainer.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onError(int errorCode, int innerError, String errorMsg) {

        }
    };

    private void setLayout() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mDocView.setLayoutParams(params);
    }
}