package com.garciaapps.botoesdememe;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.Console;
import java.util.Map;
import java.util.Random;

public class FCMInstanceIDService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN",s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getData().isEmpty()) {
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());

        }else{
            showNotification(remoteMessage.getData());
        }
    }

    private void showNotification(Map<String, String> data) {
        String title = data.get("title");
        String body = data.get("body");

        Intent it = null;
        PendingIntent contentIntent = null;
        int requestID = (int) System.currentTimeMillis();
        if(!data.get("link").isEmpty()){
            it = new Intent(Intent.ACTION_VIEW, Uri.parse(data.get("link")));
        }else{
            it = new Intent(getApplicationContext(), TabActivity.class);
        }
        contentIntent = PendingIntent.getActivity(this, requestID, it, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "com.garciaapps.botoesdememe";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notificação", notificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Botões de Memes");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentTitle(title)
                .setContentText(body)
                .setVibrate(new long[]{0L})
                .setContentIntent(contentIntent);

        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }

    private void showNotification(String title, String body) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "com.garciaapps.botoesdememe";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notificação", notificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Botões de Memes");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentTitle(title)
                .setContentText(body)
                .setVibrate(new long[]{0L});

        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());

    }
}
