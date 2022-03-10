package com.vhall.opensdk.interactive.screenrecord;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.vhall.vhallrtc.common.LogManager;
//前台服务用于保活
public class ScreenRecordService extends Service {

    public static final String BROADCAST_ACTION = "com.vhallrtc.sdk.status";

    public class ScreenRecordServiceBinder extends Binder {
        public ScreenRecordService getService() {
            return ScreenRecordService.this;
        }
    }

    public ScreenRecordService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        String CHANNEL_ONE_ID = "vhall_screen_record1";
        String CHANNEL_ONE_NAME = "vhall screenRecord1";
        NotificationChannel notificationChannel = null;
        Notification.Builder builder = new Notification.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogManager.d("onBind---->");
        return new ScreenRecordServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogManager.d("onUnbind---->");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogManager.d("onStartCommand---->");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogManager.d("onConfigurationChanged---->");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        LogManager.d("onLowMemory---->");
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        LogManager.d("onDestroy---->");
        super.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
        LogManager.d("onTrimMemory---->");
        super.onTrimMemory(level);
    }

}
