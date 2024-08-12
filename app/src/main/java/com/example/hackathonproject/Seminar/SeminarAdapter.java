package com.example.hackathonproject.Seminar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hackathonproject.R;

import java.util.List;

public class SeminarAdapter extends RecyclerView.Adapter<SeminarAdapter.ViewHolder> {
    private List<SeminarPost> seminarPosts;

    public SeminarAdapter(List<SeminarPost> seminarPosts) {
        this.seminarPosts = seminarPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seminar_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SeminarPost post = seminarPosts.get(position);
        holder.postTitle.setText(post.getTitle());
        holder.postDetails.setText("작성자 ID: " + post.getUserId() + " | " + post.getCreatedAt());
        holder.postLocation.setText("위치: " + post.getLocation());
        holder.postViews.setText("조회수: " + post.getViews());
        holder.postFee.setText("강연료: " + post.getFee() + "원");
    }

    @Override
    public int getItemCount() {
        return seminarPosts.size();
    }

    public void updateData(List<SeminarPost> posts) {
        this.seminarPosts = posts;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView postTitle, postDetails, postLocation, postViews, postFee;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postTitle = itemView.findViewById(R.id.postTitle);
            postDetails = itemView.findViewById(R.id.postDetails);
            postLocation = itemView.findViewById(R.id.postLocation);
            postViews = itemView.findViewById(R.id.postViews);
            postFee = itemView.findViewById(R.id.postFee);
        }
    }
}
