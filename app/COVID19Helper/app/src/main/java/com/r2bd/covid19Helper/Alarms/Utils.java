package com.r2bd.covid19Helper.Alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import static android.content.Context.ALARM_SERVICE;

public class Utils {
    public static final long INTERVAL_TEST = 1 * 60 * 1000;
    public static final long INTERVAL_ONE_HOUR = 60 * 60 * 1000;
    public static final long INTERVAL_TWO_HOUR = 2*INTERVAL_ONE_HOUR;
    public static final long INTERVAL_THREE_HOUR = 3*INTERVAL_ONE_HOUR;

    public static final int ALARM_ID_DAILY_RECORD = 1;
    public static final int ALARM_ID_TEA = 2;
    public static final int ALARM_ID_GARGLE = 3;

    public static void setAlarm(int idAlarm, Long timestamp, Context ctx) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(ctx, AlarmReceiver.class);
        alarmIntent.putExtra("id", String.valueOf(idAlarm));
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(ctx, idAlarm, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
    }

    public static void setDailyAlarmOnceADay(int idAlarm, Long timestamp, Context ctx){
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(ctx, AlarmReceiver.class);
        alarmIntent.putExtra("id", String.valueOf(idAlarm));
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(ctx, idAlarm, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timestamp, AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    public static void setDailyRepeatingAlarmFrequently(int idAlarm, Long timestamp, long frequency, Context ctx){
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(ctx, AlarmReceiver.class);
        alarmIntent.putExtra("id", String.valueOf(idAlarm));
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(ctx, idAlarm, alarmIntent, 0);

        alarmIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timestamp, frequency, pendingIntent);
    }

    public static void stopAlarm(int idAlarm, Context ctx){

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(ctx, AlarmReceiver.class);
        alarmIntent.putExtra("id", String.valueOf(idAlarm));
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(ctx, idAlarm, alarmIntent, 0);
        alarmManager.cancel(pendingIntent);
    }
}
