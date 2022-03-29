package com.vhall.opensdk;

import android.app.Application;

import com.vhall.httpclient.api.VHNetApi;
import com.vhall.httpclient.core.IVHNetLogCallback;
import com.vhall.httpclient.core.VHGlobalConfig;
import com.vhall.logmanager.L;
import com.vhall.opensdk.util.SpUtils;

/**
 * author: caoyanglong
 * date: 2020/9/21
 */
public class VhallApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VHGlobalConfig config = new VHGlobalConfig.Builder()
                .setEnableLog(true)
                .setVHNetLogCallback(new IVHNetLogCallback() {
                    @Override
                    public void log(String url, String message) {
                        L.e("vhallapplication",message);
                    }
                }).build();
        VHNetApi.getNetApi().setGlobalConfig(config);
        SpUtils.init(this);

        SpUtils.share().clear();
    }
}
