package com.example.hackathonproject.Education;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.TypedValue;
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
        holder.postFee.setText("교육비: " + df.format(post.getFee()) + "원");

        if (post.isInstitutionOrSchool()) {
            holder.certificationMark.setVisibility(View.VISIBLE);
        } else {
            holder.certificationMark.setVisibility(View.GONE);
        }

        byte[] imageBytes = post.getImageData();
        if (imageBytes != null && imageBytes.length > 0) {
            // 이미지가 있는 경우 해당 이미지를 표시
            Glide.with(holder.itemView.getContext())
                    .load(imageBytes)
                    .placeholder(R.drawable.placeholder)  // 로딩 중에는 placeholder를 표시
                    .into(holder.contentImage);
        } else {
            // 이미지가 없는 경우 placeholder2.png를 표시
            holder.contentImage.setImageResource(R.drawable.placeholder2);
        }

        // SharedPreferences에서 폰트 크기 불러오기
        SharedPreferences preferences = holder.itemView.getContext().getSharedPreferences("fontSizePrefs", Context.MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);  // 기본값 25
        int localFontSize = 17;

        // 텍스트 크기 적용
        holder.postTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize - 5);  // 제목은 조금 더 크게
        holder.postCategory.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize - 5);
        holder.postDetails.setTextSize(TypedValue.COMPLEX_UNIT_SP, localFontSize); // 세부사항은 더 작게
        holder.postViews.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize - 11);
        holder.postFee.setTextSize(TypedValue.COMPLEX_UNIT_SP, localFontSize);
    }


    //-----------------------------------------------------------------------------------------------------------------------------------------------

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
        public ImageView certificationMark; // 인증 마크 이미지 뷰 추가
        public ImageView contentImage; // 추가: 이미지뷰

        public EducationViewHolder(View itemView) {
            super(itemView);
            // 각 뷰 요소를 아이템 뷰에서 초기화
            postTitle = itemView.findViewById(R.id.postTitle);
            postCategory = itemView.findViewById(R.id.post_category);
            postDetails = itemView.findViewById(R.id.postDetails);
            postViews = itemView.findViewById(R.id.postViews);
            postFee = itemView.findViewById(R.id.postFee);
            contentImage = itemView.findViewById(R.id.content_image); // 추가: 이미지뷰 초기화
            certificationMark = itemView.findViewById(R.id.certification_mark); // 인증 마크 초기화

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