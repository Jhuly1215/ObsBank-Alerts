package com.obsbank.alerts;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "obsbank_alerts";
    private static final String TAG = "OBS_BANK_FCM";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token: " + token);

        SharedPreferences prefs = getSharedPreferences("obsbank_prefs", MODE_PRIVATE);
        prefs.edit().putString("last_token", token).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.d(TAG, "onMessageReceived ejecutado");

        String title = "Alert";
        String body = "New alert";

        if (message.getNotification() != null) {
            Log.d(TAG, "Notification payload presente");

            if (message.getNotification().getTitle() != null) {
                title = message.getNotification().getTitle();
            }
            if (message.getNotification().getBody() != null) {
                body = message.getNotification().getBody();
            }
        }

        Map<String, String> data = message.getData();
        if (data != null && !data.isEmpty()) {
            Log.d(TAG, "Data payload: " + data.toString());

            if (data.containsKey("title")) {
                title = data.get("title");
            }
            if (data.containsKey("body")) {
                body = data.get("body");
            }
        }

        saveLastMessage(title, body);
        showNotification(title, body);
    }

    private void saveLastMessage(String title, String body) {
        SharedPreferences prefs = getSharedPreferences("obsbank_prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("last_title", title)
                .putString("last_body", body)
                .apply();

        Log.d(TAG, "Mensaje guardado: " + title + " / " + body);
    }

    private void showNotification(String title, String body) {
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "ObsBank Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}