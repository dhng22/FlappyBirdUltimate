package edu.hanu.flappybird.service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import edu.hanu.flappybird.MainActivity;
import edu.hanu.flappybird.utils.DateUtils;

public class AlarmReminder extends BroadcastReceiver {
    ComponentName componentName;
    JobInfo jobInfo;
    JobScheduler scheduler;

    @Override
    public void onReceive(Context context, Intent intent) {
        scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        componentName = new ComponentName(context, ReminderScheduler.class);
        jobInfo = new JobInfo.Builder(MainActivity.JOB_ID_SEVEN, componentName)
                .setPeriodic(DateUtils.getTimeToGo())
                .build();

        scheduler.schedule(jobInfo);
    }
}
