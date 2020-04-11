package com.r2bd.covid19Helper.Alarms;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import com.r2bd.covid19Helper.MainActivity;
import com.r2bd.covid19Helper.R;

public class NotificationService extends IntentService {

    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;
    private final static int NOTIFICATION_ID = 0;
    Notification notification;

    public NotificationService(String name) {
        super(name);
    }

    public NotificationService() {
        super("SERVICE");
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onHandleIntent(Intent intent2) {

        String idAlarm = intent2.getStringExtra("id");
        int intIdAlarm = Integer.parseInt(idAlarm);

        String NOTIFICATION_CHANNEL_ID = getApplicationContext().getString(R.string.app_name);

        Context context = this.getApplicationContext();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent mIntent = new Intent(this, MainActivity.class);
        Resources res = this.getResources();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        String message = "";
        int imgID = R.mipmap.cvd19_notification;
        switch (intIdAlarm){
            case Utils.ALARM_ID_DAILY_RECORD:{
                message = getString(R.string.txtNotificationDailyRecord);
                imgID = R.mipmap.cvd19_record;
            }
            break;
            case Utils.ALARM_ID_GARGLE:{
                message = getString(R.string.txtNotificationGarleTime);
                imgID = R.mipmap.cvd19_throat;
            }
            break;
            case Utils.ALARM_ID_TEA:{
                message = getString(R.string.txtNotificationTeaTime);
                imgID = R.mipmap.cvd19_tea;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            final int NOTIFY_ID = 0;
            String id = NOTIFICATION_CHANNEL_ID; // default_channel_id
            String title = NOTIFICATION_CHANNEL_ID; // Default Channel
            NotificationCompat.Builder builder;

            NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notifManager == null) {
                notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }

            builder = new NotificationCompat.Builder(context, id);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //String msg = mIntent.getStringArrayExtra("id").toString() + " --> " + mIntent.getStringArrayExtra("msg").toString();
            pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            int rtr = pendingIntent.getCreatorUid();
            builder.setContentTitle(getString(R.string.app_name)).setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.mipmap.ic_launcher)   // required
                    .setContentText(message)
                    .setLargeIcon(BitmapFactory.decodeResource(res, imgID))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            Notification notification = builder.build();

            notifManager.notify(NOTIFY_ID, notification);

            startForeground(1, notification);

        }
        else {
            pendingIntent = PendingIntent.getActivity(context, 1, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                    .setSound(soundUri)
                    .setAutoCancel(true)
                    .setContentTitle(getString(R.string.app_name)).setCategory(Notification.CATEGORY_SERVICE)
                    .setContentText(message).build();
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }
}