package edu.hanu.flappybird.service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import edu.hanu.flappybird.GameActivity;
import edu.hanu.flappybird.utils.DateUtils;

public class AlarmReminder extends BroadcastReceiver {
    ComponentName componentName;
    JobInfo jobInfo;
    JobScheduler scheduler;

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        componentName = new ComponentName(context, ReminderScheduler.class);
        jobInfo = new JobInfo.Builder(GameActivity.JOB_ID_SEVEN, componentName)
                .setPeriodic(calendar.getTimeInMillis()-System.currentTimeMillis())
                .build();

        scheduler.schedule(jobInfo);

    }
}
