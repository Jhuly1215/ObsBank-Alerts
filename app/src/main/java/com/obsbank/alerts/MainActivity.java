package com.obsbank.alerts;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private TextView tokenText;
    private TextView statusText;
    private RecyclerView recyclerViewAlerts;
    private AlertsAdapter alertsAdapter;
    private BroadcastReceiver alertReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //tokenText = findViewById(R.id.tokenText);
        statusText = findViewById(R.id.statusText);
        
        recyclerViewAlerts = findViewById(R.id.recyclerViewAlerts);
        recyclerViewAlerts.setLayoutManager(new LinearLayoutManager(this));
        alertsAdapter = new AlertsAdapter(new ArrayList<>());
        recyclerViewAlerts.setAdapter(alertsAdapter);

        Button btnCriticalSub = findViewById(R.id.btnCriticalSub);
        Button btnCriticalUnsub = findViewById(R.id.btnCriticalUnsub);
        Button btnWarningSub = findViewById(R.id.btnWarningSub);
        Button btnWarningUnsub = findViewById(R.id.btnWarningUnsub);
        Button btnInfoSub = findViewById(R.id.btnInfoSub);
        Button btnInfoUnsub = findViewById(R.id.btnInfoUnsub);

        requestNotificationPermission();
        loadToken();
        loadAlertHistory();

        btnCriticalSub.setOnClickListener(v -> subscribe("obsbank-critical"));
        btnCriticalUnsub.setOnClickListener(v -> unsubscribe("obsbank-critical"));

        btnWarningSub.setOnClickListener(v -> subscribe("obsbank-warning"));
        btnWarningUnsub.setOnClickListener(v -> unsubscribe("obsbank-warning"));

        btnInfoSub.setOnClickListener(v -> subscribe("obsbank-info"));
        btnInfoUnsub.setOnClickListener(v -> unsubscribe("obsbank-info"));

        // Inicializa un receiver para refrescar los datos automáticamente
        alertReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadAlertHistory();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlertHistory();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(alertReceiver, new IntentFilter("com.obsbank.alerts.NEW_ALERT"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(alertReceiver, new IntentFilter("com.obsbank.alerts.NEW_ALERT"));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(alertReceiver);
    }

    private void loadToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        // tokenText.setText("Error obteniendo token");
                        statusText.setText("No se pudo obtener el token");
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM_TOKEN", "Token del dispositivo: " + token);
                    // tokenText.setText(token);
                    statusText.setText("Dispositivo registrado");
                });
    }

    private void subscribe(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        statusText.setText("Suscrito a: " + topic);
                    } else {
                        statusText.setText("Falló la suscripción a: " + topic);
                    }
                });
    }

    private void unsubscribe(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        statusText.setText("Desuscrito de: " + topic);
                    } else {
                        statusText.setText("Falló la desuscripción de: " + topic);
                    }
                });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }
    }

    private void loadAlertHistory() {
        SharedPreferences prefs = getSharedPreferences("obsbank_prefs", MODE_PRIVATE);
        String alertsJson = prefs.getString("alerts_list", "[]");
        
        List<Alert> alertList = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(alertsJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                alertList.add(new Alert(
                        obj.optString("title", "Alerta"),
                        obj.optString("body", ""),
                        obj.optString("severity", "info"),
                        obj.optLong("timestamp", System.currentTimeMillis())
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        alertsAdapter.updateAlerts(alertList);
    }
}