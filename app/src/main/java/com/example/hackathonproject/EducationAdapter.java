package com.example.hackathonproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EducationAdapter extends RecyclerView.Adapter<EducationAdapter.ViewHolder> {
    private List<Education> educationList;

    public EducationAdapter(List<Education> educationList) {
        this.educationList = educationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_education, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Education education = educationList.get(position);
        holder.postTitle.setText(education.getTitle());
        holder.postDetails.setText(education.getDetails());
        holder.postLocation.setText("위치 | " + education.getLocation());
        holder.postViews.setText("조회수 | " + education.getViews());
    }

    @Override
    public int getItemCount() {
        return educationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView postTitle, postDetails, postLocation, postViews;

        public ViewHolder(View itemView) {
            super(itemView);
            postTitle = itemView.findViewById(R.id.postTitle);
            postDetails = itemView.findViewById(R.id.postDetails);
            postLocation = itemView.findViewById(R.id.postLocation);
            postViews = itemView.findViewById(R.id.postViews);
        }
    }
}
