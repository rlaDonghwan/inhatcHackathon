package com.example.hackathonproject.Education;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathonproject.R;

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
        // 현재 위치의 게시글 데이터를 가져와 ViewHolder에 바인딩
        EducationPost post = educationPostList.get(position);
        holder.postTitle.setText(post.getTitle()); // 제목 설정
        holder.postDetails.setText(post.getCreatedAt());  // 게시글 작성 시간 등 추가 정보 설정
        holder.postLocation.setText(post.getLocation()); // 위치 설정
        holder.postViews.setText("조회수: " + post.getViews());  // 조회수 설정
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
        public TextView postTitle; // 제목을 표시할 TextView
        public TextView postDetails; // 게시글 작성 시간 등을 표시할 TextView
        public TextView postLocation; // 위치를 표시할 TextView
        public TextView postViews; // 조회수를 표시할 TextView

        public EducationViewHolder(View itemView) {
            super(itemView);
            // 각 뷰 요소를 아이템 뷰에서 초기화
            postTitle = itemView.findViewById(R.id.postTitle);
            postDetails = itemView.findViewById(R.id.postDetails);
            postLocation = itemView.findViewById(R.id.postLocation);
            postViews = itemView.findViewById(R.id.postViews);

            // 아이템 뷰 클릭 시 리스너 호출
            itemView.setOnClickListener(v -> {
                if (listener != null) { // 리스너가 설정되어 있는지 확인
                    int position = getAdapterPosition(); // 현재 클릭된 위치를 가져옴
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(v, position); // 클릭 이벤트 처리
                    }
                }
            });
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
