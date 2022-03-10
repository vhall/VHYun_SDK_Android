package com.vhall.opensdk.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.vhall.opensdk.ConfigActivity;

import java.security.spec.PSSParameterSpec;

public class SpUtils {
    static SpUtils instance;
    public static SpUtils share(){
        if(instance == null){
            instance = new SpUtils();
        }
        return instance;
    }

    private static Application app;
    public static void init(Application app){
        SpUtils.app = app;
    }
    private SharedPreferences sp;
    private SpUtils(){
        sp = app.getSharedPreferences("config", Context.MODE_PRIVATE);
    }

    public String getAppId(){
        return sp.getString(ConfigActivity.KEY_APP_ID,"");
    }


    public int getDefinition(){
        return sp.getInt(ConfigActivity.KEY_PIX_TYPE,0);
    }

    public String getBroadcastId(){
        return sp.getString(ConfigActivity.KEY_BROADCAST_ID,"");
    }

    public String getUserId(){
        return sp.getString("userId", Build.MODEL);
    }

    public void commitStr(String key,String txt){
        sp.edit().putString(key,txt).apply();
    }

    //获取聊天channel id
    public String getChatId(){
        return sp.getString(ConfigActivity.KEY_CHAT_ID,"");
    }
}
