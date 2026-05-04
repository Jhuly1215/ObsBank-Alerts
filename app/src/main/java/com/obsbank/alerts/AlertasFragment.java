package com.obsbank.alerts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AlertasFragment extends Fragment {

    private TextView tokenText;
    private RecyclerView recyclerViewAlerts;
    private AlertsAdapter alertsAdapter;
    private BroadcastReceiver alertReceiver;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflamos tu layout adaptado al Dark Mode
        return inflater.inflate(R.layout.fragment_alertas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenText = view.findViewById(R.id.tokenText);
        
        // Cargar el token desde las preferencias primero por si ya lo tenemos
        SharedPreferences prefs = requireActivity().getSharedPreferences("obsbank_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("last_token", "Token no disponible aún");
        tokenText.setText(token);

        // Consultar a Firebase directamente para asegurar que se actualice la UI
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && tokenText != null) {
                String currentToken = task.getResult();
                tokenText.setText(currentToken);
                prefs.edit().putString("last_token", currentToken).apply();
            }
        });

        recyclerViewAlerts = view.findViewById(R.id.recyclerViewAlerts);
        recyclerViewAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        alertsAdapter = new AlertsAdapter(new ArrayList<>());
        recyclerViewAlerts.setAdapter(alertsAdapter);

        alertReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadAlertHistory();
            }
        };

        loadAlertHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAlertHistory();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().registerReceiver(alertReceiver, new IntentFilter("com.obsbank.alerts.NEW_ALERT"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireActivity().registerReceiver(alertReceiver, new IntentFilter("com.obsbank.alerts.NEW_ALERT"));
        }
        
        // Actualizar el token si cambió
        SharedPreferences prefs = requireActivity().getSharedPreferences("obsbank_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("last_token", "Token no disponible aún");
        if (tokenText != null) {
            tokenText.setText(token);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(alertReceiver);
    }

    private void loadAlertHistory() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences("obsbank_prefs", Context.MODE_PRIVATE);
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
