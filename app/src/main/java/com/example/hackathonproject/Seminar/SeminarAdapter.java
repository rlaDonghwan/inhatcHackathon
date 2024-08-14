package com.example.hackathonproject.Seminar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hackathonproject.R;

import java.util.List;

public class SeminarAdapter extends RecyclerView.Adapter<SeminarAdapter.SeminarViewHolder> {

    private List<SeminarPost> seminarPosts; // 강연 게시글 목록을 저장할 리스트
    private OnItemClickListener listener; // 아이템 클릭 리스너

    // 생성자: 강연 게시글 목록을 초기화
    public SeminarAdapter(List<SeminarPost> seminarPosts) {
        this.seminarPosts = seminarPosts;
    }

    // ViewHolder를 생성하는 메서드
    @NonNull
    @Override
    public SeminarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 아이템 뷰를 인플레이트하여 ViewHolder에 전달
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seminar_post, parent, false);
        return new SeminarViewHolder(view);
    }

    // ViewHolder에 데이터를 바인딩하는 메서드
    @Override
    public void onBindViewHolder(@NonNull SeminarViewHolder holder, int position) {
        // 현재 위치의 게시글 데이터를 가져와 ViewHolder에 바인딩
        SeminarPost post = seminarPosts.get(position);
        holder.postTitle.setText(post.getTitle()); // 제목 설정
        holder.postDetails.setText("작성자 ID: " + post.getUserId() + " | " + post.getCreatedAt());  // 작성자 ID 및 작성 시간 설정
        holder.postLocation.setText("위치: " + post.getLocation()); // 위치 설정
        holder.postViews.setText("조회수: " + post.getViews());  // 조회수 설정
        holder.postFee.setText("강연료: " + post.getFee() + "원");  // 강연료 설정
    }

    // 어댑터에서 관리하는 아이템의 개수 반환
    @Override
    public int getItemCount() {
        return seminarPosts.size();
    }

    // 데이터를 업데이트하고 RecyclerView를 갱신하는 메서드
    public void updateData(List<SeminarPost> posts) {
        this.seminarPosts = posts;
        notifyDataSetChanged(); // 데이터 변경을 알림
    }

    // 아이템 클릭 리스너 설정
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // 아이템 클릭 리스너 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    // ViewHolder 클래스: 각 아이템 뷰를 재활용하여 성능을 높임
    public class SeminarViewHolder extends RecyclerView.ViewHolder {
        public TextView postTitle, postDetails, postLocation, postViews, postFee; // 뷰 요소들

        public SeminarViewHolder(View itemView) {
            super(itemView);
            // 각 뷰 요소를 아이템 뷰에서 초기화
            postTitle = itemView.findViewById(R.id.postTitle);
            postDetails = itemView.findViewById(R.id.postDetails);
            postLocation = itemView.findViewById(R.id.postLocation);
            postViews = itemView.findViewById(R.id.postViews);
            postFee = itemView.findViewById(R.id.postFee);

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
}
