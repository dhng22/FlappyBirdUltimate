package edu.hanu.flappybird;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReminderReceiver extends BroadcastReceiver {
    public static final int reminderId = 221;
    public static final String actionRemind = "actionRemind";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(actionRemind)) {
            Notification notificationCompat = new NotificationCompat
                    .Builder(context, MyApplication.channel_reminder)
                    .setContentTitle("PLAYING TIMEEEE!")
                    .setContentText("Good morning, play some game with music on might boost your mood :DD")
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.wojak))
                    .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(context.getResources(), R.drawable.wojak)).bigLargeIcon(null))
                    .setSmallIcon(R.drawable.goldenbird_downflap)
                    .build();
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
            managerCompat.notify(reminderId, notificationCompat);
        }
    }
}
