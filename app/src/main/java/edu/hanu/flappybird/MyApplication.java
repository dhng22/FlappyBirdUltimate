package edu.hanu.flappybird;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

public class MyApplication extends Application {
    public static final String channel_reminder = "Channel reminder";
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channelReminder = new NotificationChannel(channel_reminder, "channel1", NotificationManager.IMPORTANCE_HIGH);
            channelReminder.setDescription("Remind you to play at 7.am");
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.createNotificationChannel(channelReminder);
        }
    }
}
