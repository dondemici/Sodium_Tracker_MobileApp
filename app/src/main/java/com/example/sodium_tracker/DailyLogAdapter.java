package com.example.sodium_tracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DailyLogAdapter extends RecyclerView.Adapter<DailyLogAdapter.LogViewHolder> {

    public interface OnLogActionListener {
        void onEditClick(DailyLogEntry entry);
        void onDeleteClick(DailyLogEntry entry);
    }

    private List<DailyLogEntry> logList;
    private OnLogActionListener listener;

    public DailyLogAdapter(List<DailyLogEntry> logList, OnLogActionListener listener) {
        this.logList = logList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        DailyLogEntry entry = logList.get(position);
        holder.dateText.setText(entry.getDate());
        holder.recipeText.setText(entry.getRecipeName());
        holder.mealText.setText(entry.getMealType());
        holder.sodiumText.setText(String.format("%.2f mg", entry.getSodium()));

        holder.editButton.setOnClickListener(v -> listener.onEditClick(entry));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(entry));
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, recipeText, mealText, sodiumText;
        Button editButton, deleteButton;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.logDate);
            recipeText = itemView.findViewById(R.id.logRecipe);
            mealText = itemView.findViewById(R.id.logMeal);
            sodiumText = itemView.findViewById(R.id.logSodium);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
