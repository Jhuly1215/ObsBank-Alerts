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

import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private TextView tokenText;
    private TextView statusText;
    private TextView lastMessageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenText = findViewById(R.id.tokenText);
        statusText = findViewById(R.id.statusText);
        lastMessageText = findViewById(R.id.lastMessageText);

        Button btnCriticalSub = findViewById(R.id.btnCriticalSub);
        Button btnCriticalUnsub = findViewById(R.id.btnCriticalUnsub);
        Button btnWarningSub = findViewById(R.id.btnWarningSub);
        Button btnWarningUnsub = findViewById(R.id.btnWarningUnsub);
        Button btnInfoSub = findViewById(R.id.btnInfoSub);
        Button btnInfoUnsub = findViewById(R.id.btnInfoUnsub);

        requestNotificationPermission();
        loadToken();
        loadLastMessage();

        btnCriticalSub.setOnClickListener(v -> subscribe("obsbank-critical"));
        btnCriticalUnsub.setOnClickListener(v -> unsubscribe("obsbank-critical"));

        btnWarningSub.setOnClickListener(v -> subscribe("obsbank-warning"));
        btnWarningUnsub.setOnClickListener(v -> unsubscribe("obsbank-warning"));

        btnInfoSub.setOnClickListener(v -> subscribe("obsbank-info"));
        btnInfoUnsub.setOnClickListener(v -> unsubscribe("obsbank-info"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLastMessage();
    }

    private void loadToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        tokenText.setText("Error obteniendo token");
                        statusText.setText("No se pudo obtener el token");
                        return;
                    }

                    String token = task.getResult();
                    tokenText.setText(token);
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

    private void loadLastMessage() {
        SharedPreferences prefs = getSharedPreferences("obsbank_prefs", MODE_PRIVATE);
        String lastTitle = prefs.getString("last_title", "No recibido");
        String lastBody = prefs.getString("last_body", "");

        if (lastBody.isEmpty()) {
            lastMessageText.setText(lastTitle);
        } else {
            lastMessageText.setText(lastTitle + "\n" + lastBody);
        }
    }
}