package com.obsbank.alerts;

import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    private List<Alert> alertList;

    public AlertsAdapter(List<Alert> alertList) {
        this.alertList = alertList;
    }

    public void updateAlerts(List<Alert> newAlerts) {
        this.alertList = newAlerts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alertList.get(position);

        holder.tvAlertTitle.setText(alert.getTitle());
        holder.tvAlertBody.setText(alert.getBody());
        
        // Formato relativo de tiempo (Ej: "Hace 5 min")
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                alert.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
        );
        holder.tvAlertTime.setText(timeAgo);

        // Coloreado según severidad
        String severity = alert.getSeverity() != null ? alert.getSeverity().toLowerCase() : "info";
        switch (severity) {
            case "critical":
                holder.colorIndicator.setBackgroundColor(Color.parseColor("#E53935")); // Rojo
                break;
            case "warning":
                holder.colorIndicator.setBackgroundColor(Color.parseColor("#FFB300")); // Naranja
                break;
            default:
                holder.colorIndicator.setBackgroundColor(Color.parseColor("#1E88E5")); // Azul
                break;
        }

        // Lógica de expansión
        boolean isExpanded = alert.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.ivExpandIcon.setRotation(isExpanded ? 180f : 0f); // Girar la flechita

        // Click en la tarjeta principal para expandir/colapsar
        holder.itemView.setOnClickListener(v -> {
            alert.setExpanded(!alert.isExpanded());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        View colorIndicator;
        TextView tvAlertTitle;
        TextView tvAlertTime;
        ImageView ivExpandIcon;
        LinearLayout expandableLayout;
        TextView tvAlertBody;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            tvAlertTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvAlertTime = itemView.findViewById(R.id.tvAlertTime);
            ivExpandIcon = itemView.findViewById(R.id.ivExpandIcon);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            tvAlertBody = itemView.findViewById(R.id.tvAlertBody);
        }
    }
}
