package com.example.hackathonproject.Edu;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathonproject.R;

import java.util.List;

public class EducationAdapter extends RecyclerView.Adapter<EducationAdapter.ViewHolder> {
    private List<EducationPost> educationPostList;
    private OnItemClickListener listener;

    public EducationAdapter(List<EducationPost> educationPostList) {
        this.educationPostList = educationPostList;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_education_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EducationPost post = educationPostList.get(position);
        holder.bind(post, listener);
    }

    @Override
    public int getItemCount() {
        return educationPostList.size();
    }

    public void updateData(List<EducationPost> newPosts) {
        this.educationPostList = newPosts;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView postTitle;
        TextView postDetails;
        TextView postLocation;
        TextView postViews;

        public ViewHolder(View itemView) {
            super(itemView);
            postTitle = itemView.findViewById(R.id.postTitle);
            postDetails = itemView.findViewById(R.id.postDetails);
            postLocation = itemView.findViewById(R.id.postLocation);
            postViews = itemView.findViewById(R.id.postViews);
        }

        public void bind(final EducationPost post, final OnItemClickListener listener) {
            postTitle.setText(post.getTitle());
            postDetails.setText(post.getUserName() + " | " + post.getCreatedAt().toString().substring(0, 16)); // 작성자 이름과 게시시간 설정
            postLocation.setText("위치 | " + post.getLocation());
            postViews.setText("조회수 | " + post.getViews());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(v, getAdapterPosition());
                }

                // 클릭 시 조회수 증가 및 콘텐츠 보기로 이동
                Intent intent = new Intent(itemView.getContext(), EducationContentView.class);
                intent.putExtra("postId", post.getPostId());
                itemView.getContext().startActivity(intent);
            });
        }
    }

}
