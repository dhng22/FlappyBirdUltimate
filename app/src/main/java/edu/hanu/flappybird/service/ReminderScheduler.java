package edu.hanu.flappybird.service;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import edu.hanu.flappybird.GameActivity;
import edu.hanu.flappybird.MyApplication;
import edu.hanu.flappybird.R;

public class ReminderScheduler extends JobService {
    public static final int reminderId = 221;
    @Override
    public boolean onStartJob(JobParameters params) {
        Intent intent = new Intent(this, GameActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, reminderId, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notificationCompat = new NotificationCompat
                .Builder(getApplicationContext(), MyApplication.channel_reminder)
                .setContentTitle("BONJOUR!")
                .setContentText("Hi, it's 7.am, play some game with music on might boost your mood :DD")
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.wojak))
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.wojak)).bigLargeIcon(null))
                .setSmallIcon(R.drawable.goldenbird_downflap)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
        managerCompat.notify(reminderId, notificationCompat);
        jobFinished(params, true);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
