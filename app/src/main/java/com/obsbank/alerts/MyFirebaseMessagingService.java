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
        String severity = "info";

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
            if (data.containsKey("severity")) {
                severity = data.get("severity");
            }
        }

        saveLastMessage(title, body);
        showNotification(title, body);
    }

    private void saveLastMessage(String title, String body, String severity) {
        SharedPreferences prefs = getSharedPreferences("obsbank_prefs", MODE_PRIVATE);
        String alertsJson = prefs.getString("alerts_list", "[]");
        try {
            org.json.JSONArray array = new org.json.JSONArray(alertsJson);
            org.json.JSONObject newAlert = new org.json.JSONObject();
            newAlert.put("title", title);
            newAlert.put("body", body);
            newAlert.put("severity", severity);
            newAlert.put("timestamp", System.currentTimeMillis());

            org.json.JSONArray newArray = new org.json.JSONArray();
            newArray.put(newAlert);

            for (int i = 0; i < array.length() && i < 49; i++) {
                newArray.put(array.get(i));
            }

            prefs.edit().putString("alerts_list", newArray.toString()).apply();
            
            // Avisar a la MainActivity que hay una nueva alerta (si está abierta)
            sendBroadcast(new android.content.Intent("com.obsbank.alerts.NEW_ALERT"));
            
        } catch (org.json.JSONException e) {
            Log.e(TAG, "Error guardando historial JSON", e);
        }
    }

    private void showNotification(String title, String body, String severity) {
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (manager == null) return;

        String channelId;
        String channelName;
        int importance;
        int color;

        if ("critical".equalsIgnoreCase(severity)) {
            channelId = "obsbank_critical";
            channelName = "Alertas Críticas";
            importance = NotificationManager.IMPORTANCE_HIGH;
            color = android.graphics.Color.RED;
        } else if ("warning".equalsIgnoreCase(severity)) {
            channelId = "obsbank_warning";
            channelName = "Alertas de Advertencia";
            importance = NotificationManager.IMPORTANCE_HIGH;
            color = android.graphics.Color.rgb(255, 165, 0); // Orange
        } else {
            channelId = "obsbank_info";
            channelName = "Alertas de Información";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            color = android.graphics.Color.BLUE;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    importance
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setColor(color)
                        .setPriority(importance == NotificationManager.IMPORTANCE_HIGH ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}