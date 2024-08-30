package com.example.hackathonproject.Education;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hackathonproject.R;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class EducationAdapter extends RecyclerView.Adapter<EducationAdapter.EducationViewHolder> {

    private List<EducationPost> educationPostList; // 게시글 목록을 저장할 리스트
    private OnItemClickListener listener; // 아이템 클릭 리스너

    // 생성자: 게시글 목록을 초기화
    public EducationAdapter(List<EducationPost> educationPostList) {
        this.educationPostList = educationPostList;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // ViewHolder를 생성하는 메서드
    @NonNull
    @Override
    public EducationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 아이템 뷰를 인플레이트하여 ViewHolder에 전달
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_education_post, parent, false);
        return new EducationViewHolder(view);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // ViewHolder에 데이터를 바인딩하는 메서드
    @Override
    public void onBindViewHolder(@NonNull EducationViewHolder holder, int position) {
        EducationPost post = educationPostList.get(position);
        holder.postTitle.setText(post.getTitle());
        String categoryText = "[" + post.getCategory() + "]";
        holder.postCategory.setText(categoryText);

        holder.postViews.setText("조회수: " + post.getViews());

        String formattedTime = formatTimeAgo(post.getCreatedAt());
        holder.postDetails.setText(post.getLocation() + " - " + formattedTime);

        DecimalFormat df = new DecimalFormat("#,###");
        holder.postFee.setText("강연료: " + df.format(post.getFee()) + "원");

        // 이미지 로드
        byte[] imageBytes = post.getImageData();
        if (imageBytes != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageBytes)
                    .placeholder(R.drawable.placeholder) // 로딩 중에 보여줄 기본 이미지
                    .into(holder.contentImage);
        } else {
            holder.contentImage.setImageResource(R.drawable.placeholder); // 기본 이미지
        }
    }


    private String formatTimeAgo(String createdAt) {
        // "2024-08-19 18:03:19.0"에서 ".0" 제거
        if (createdAt != null && createdAt.endsWith(".0")) {
            createdAt = createdAt.substring(0, createdAt.length() - 2);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime postTime = LocalDateTime.parse(createdAt, formatter);

        // 현재 시간을 KST로 가져옴
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // 작성 시간과 현재 시간의 차이 계산
        long minutes = ChronoUnit.MINUTES.between(postTime, now);
        long hours = ChronoUnit.HOURS.between(postTime, now);
        long days = ChronoUnit.DAYS.between(postTime, now);

        if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else {
            return days + "일 전";
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 어댑터에서 관리하는 아이템의 개수 반환
    @Override
    public int getItemCount() {
        return educationPostList.size();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 데이터를 업데이트하고 RecyclerView를 갱신하는 메서드
    public void updateData(List<EducationPost> posts) {
        this.educationPostList = posts;
        notifyDataSetChanged(); // 데이터 변경을 알림
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 아이템 클릭 리스너 설정
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 아이템 클릭 리스너 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // ViewHolder 클래스: 각 아이템 뷰를 재활용하여 성능을 높임
    public class EducationViewHolder extends RecyclerView.ViewHolder {
        public TextView postTitle, postDetails, postViews, postFee, postCategory;
        public ImageView contentImage; // 추가: 이미지뷰

        public EducationViewHolder(View itemView) {
            super(itemView);
            postTitle = itemView.findViewById(R.id.postTitle);
            postCategory = itemView.findViewById(R.id.post_category);
            postDetails = itemView.findViewById(R.id.postDetails);
            postViews = itemView.findViewById(R.id.postViews);
            postFee = itemView.findViewById(R.id.postFee);
            contentImage = itemView.findViewById(R.id.content_image); // 추가: 이미지뷰 초기화

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(v, position);
                    }
                }
            });
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------
}