package com.olcow.shiniu.until;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.olcow.shiniu.R;

public class NotificationUntil extends ContextWrapper {

    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "channel_1";
    private static final String CHANNEL_NAME = "channel_name_1";

    public NotificationUntil(Context context){
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(){
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH);
        getManager().createNotificationChannel(channel);
    }

    private NotificationManager getManager(){
        if (notificationManager==null){
            notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }
    private NotificationCompat.Builder getChannelNotification(String title, String content, PendingIntent pendingIntent){
        return new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_olcowlog_50dp)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
    }
    private NotificationCompat.Builder getNotification_25(String title, String content, PendingIntent pendingIntent){
        return new NotificationCompat.Builder(getApplicationContext(),"default")
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_olcowlog_50dp)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
    }
    public void sendNotification(String title, String content,PendingIntent pendingIntent){
        if(Build.VERSION.SDK_INT>26){
            createNotificationChannel();
            getManager().notify(1,getChannelNotification(title,content,pendingIntent).build());
        } else {
            getManager().notify(1,getNotification_25(title,content,pendingIntent).build());
        }
    }
}
