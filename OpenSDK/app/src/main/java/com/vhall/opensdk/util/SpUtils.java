package com.vhall.opensdk.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.vhall.opensdk.ConfigActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpUtils {
    public static final String ISpUtils_Groups = "ISpUtils_Groups"; //小组列表

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
        Random ra =new Random();
        return sp.getString("userId", "Android_"+ra.nextInt(10000));
    }
    public String getNickName(){
        return sp.getString("nickname",Build.MODEL);
    }
    public String getAvatar(){
        return sp.getString("avatar","https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png");
    }

    public void commitStr(String key,String txt){
        sp.edit().putString(key,txt).apply();
    }

    public void clear(){
        String groups = SpUtils.share().getGroups();
        if (!TextUtils.isEmpty(groups)){
            if (groups.contains(",")){
                String[] split = groups.split(",");
                for (int i = 0; i < split.length; i++) {
                    removeStr(split[i],split[i]);
                }
            }else {
                removeStr(groups,groups);
            }
            removeStr(ISpUtils_Groups,ISpUtils_Groups);
        }
    }

    public void removeStr(String key,String name){
        String createGroupByName = SpUtils.share().getGroups();
        if (createGroupByName.contains(",")) {
            String[] split = createGroupByName.split(",");
            List<String> list = new ArrayList<>();
            for (int i = 0; i < split.length; i++) {
                if (!name.equals(split[i])) {
                    list.add(split[i]);
                }
            }
            SpUtils.share().commitStr(ISpUtils_Groups, ListUtil.listSplitByChar(list, ','));
        } else {
            //zhi
            SpUtils.share().commitStr(ISpUtils_Groups, "");
        }

        sp.edit().remove(key).apply();
    }

    //获取聊天channel id
    public String getChatId(){
        return sp.getString(ConfigActivity.KEY_CHAT_ID,"");
    }

    // ,号隔开
    public void setGroups(String groups){
        sp.edit().putString(ISpUtils_Groups,groups).apply();
    }
    public String getGroups(){
        return sp.getString(ISpUtils_Groups,"");
    }
    public void setGroupMsg(String groupId, String msg){
        sp.edit().putString(groupId,msg).apply();
    }
    public String getGroupMsg(String groupId){
        return sp.getString(groupId,"");
    }
}
