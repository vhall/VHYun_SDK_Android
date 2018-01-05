package com.vhall.opensdk;

import android.app.Application;

import com.vhall.framework.VhallSDK;
import com.vhall.framework.logger.LogLevel;

/**
 * Created by Hank on 2017/11/27.
 */
public class VhallApp extends Application {
    private String userId = "test1";//客户用户ID，

    @Override
    public void onCreate() {
        super.onCreate();
        VhallSDK.getInstance().init(this, getResources().getString(R.string.appid), userId);
    }
}
