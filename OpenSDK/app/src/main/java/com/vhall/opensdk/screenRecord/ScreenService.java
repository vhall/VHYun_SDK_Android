package com.vhall.opensdk.screenRecord;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import com.vhall.slss.VHScreenRecordService;
import com.vhall.opensdk.R;

/**
 * Created by zwp on 2019/5/29
 */
public class ScreenService extends VHScreenRecordService {

    @Override
    public void onCreate() {
        /*VHScreenRecordService 默认添加空白通知以提高Service 存活能力
         *用户可继承重写onCreate方法 自定义实现提高Service存活能力方式
         */
//        super.onCreate();
        String CHANNEL_ONE_ID = "vhall_screen_record";
        String CHANNEL_ONE_NAME = "vhall screenRecord";
        NotificationChannel notificationChannel = null;
        Notification.Builder builder = new Notification.Builder(this);
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }
        Intent notificationIntent = new Intent(this, ScreenRecordActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.addAction(R.drawable.main_button_audio_on, "录屏中...", pendingIntent);
        Notification notification = builder.build();
        startForeground(1, notification);


    }
}
