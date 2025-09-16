package com.zybooks.projecttwonickburnetteweightlossoption;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder> {
    private List<WeightEntry> weightList;

    public WeightAdapter(List<WeightEntry> weightList) {
        this.weightList = weightList;
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_weight, parent, false);
        return new WeightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        WeightEntry entry = weightList.get(position);
        holder.weightTextView.setText("Weight: " + entry.getWeight() + " lbs");
        holder.dateTextView.setText("Date: " + entry.getDate());
    }

    @Override
    public int getItemCount() {
        return weightList.size();
    }

    public static class WeightViewHolder extends RecyclerView.ViewHolder {
        TextView weightTextView, dateTextView;

        public WeightViewHolder(@NonNull View itemView) {
            super(itemView);
            weightTextView = itemView.findViewById(R.id.weightTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
