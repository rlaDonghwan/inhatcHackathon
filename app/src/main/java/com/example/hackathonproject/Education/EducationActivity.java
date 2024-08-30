package com.example.hackathonproject.Education;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hackathonproject.Chat.ChatListActivity;
import com.example.hackathonproject.R;
import com.example.hackathonproject.Lecture.LectureActivity;
import com.example.hackathonproject.Setting.SettingsActivity;
import com.example.hackathonproject.db.EducationDAO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
public class EducationActivity extends AppCompatActivity {
    private RecyclerView recyclerView; // 게시글을 보여줄 RecyclerView
    private EducationAdapter adapter; // RecyclerView에 연결할 어댑터
    private List<EducationPost> educationPostList = new ArrayList<>(); // 게시글 목록
    private SwipeRefreshLayout swipeRefreshLayout; // 새로고침을 위한 SwipeRefreshLayout
    private EducationManager educationManager; // 게시글 관리 매니저

    //-----------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education); // 레이아웃 설정

        educationManager = new EducationManager(new EducationDAO()); // EducationManager 초기화

        recyclerView = findViewById(R.id.recyclerView); // RecyclerView 초기화
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // 레이아웃 매니저 설정
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout); // SwipeRefreshLayout 초기화

        adapter = new EducationAdapter(educationPostList); // 어댑터 초기화
        recyclerView.setAdapter(adapter); // RecyclerView에 어댑터 설정

        swipeRefreshLayout.setOnRefreshListener(this::loadEducationPosts); // 새로고침 시 게시글 로드

        FloatingActionButton fab = findViewById(R.id.fab); // 글쓰기 버튼 초기화
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(EducationActivity.this, EducationWriteActivity.class); // 글쓰기 액티비티로 이동
            startActivityForResult(intent, 100); // 결과를 받기 위해 액티비티 시작
        });

        // 메뉴 버튼 설정
        LinearLayout firstMenuItem = findViewById(R.id.first_menu_item);
        firstMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(EducationActivity.this, EducationActivity.class);
            startActivity(intent);
        });

        LinearLayout secondMenuItem = findViewById(R.id.second_menu_item);
        secondMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(EducationActivity.this, LectureActivity.class);
            startActivity(intent);
        });

        LinearLayout thirdMenuItem = findViewById(R.id.third_menu_item);
        thirdMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(EducationActivity.this, ChatListActivity.class);
            startActivity(intent);
        });

        LinearLayout fourthMenuItem = findViewById(R.id.fourth_menu_item);
        fourthMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(EducationActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        // 액티비티가 화면에 보일 때마다 게시글을 새로 로드
        swipeRefreshLayout.setRefreshing(true); // 새로고침 UI 표시
        loadEducationPosts();
    }

    // 게시글을 로드하는 메서드
    private void loadEducationPosts() {
        new LoadPostsTask().execute(); // 비동기 작업으로 게시글 로드
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 게시글 로드를 위한 비동기 작업 클래스
    private class LoadPostsTask extends AsyncTask<Void, Void, List<EducationPost>> {
        @Override
        protected List<EducationPost> doInBackground(Void... voids) {
            // 비동기 작업으로 데이터베이스에서 게시글 불러오기
            List<EducationPost> posts = educationManager.getAllEducationPosts();

            // 각 게시글에 대해 이미지를 로드하여 설정
            for (EducationPost post : posts) {
                int educationId = post.getEducationId();
                byte[] imageData = educationManager.getEducationImage(educationId);

                if (imageData != null && imageData.length > 0) {
                    post.setImageData(imageData);
                    Log.d("LoadPostsTask", "이미지 로드 성공: " + educationId);
                } else {
                    Log.d("LoadPostsTask", "이미지 없음 또는 로드 실패: " + educationId);
                }
            }

            return posts;
        }

        @Override
        protected void onPostExecute(List<EducationPost> posts) {
            // 게시글 로드 완료 후 UI 갱신
            if (posts != null && !posts.isEmpty()) {
                Collections.sort(posts, new Comparator<EducationPost>() {
                    @Override
                    public int compare(EducationPost p1, EducationPost p2) {
                        // createdAt 필드를 기준으로 최신 순으로 정렬
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    }
                });
            }

            educationPostList = posts;
            adapter.updateData(educationPostList); // 어댑터에 데이터 업데이트
            swipeRefreshLayout.setRefreshing(false); // 새로고침 중지

            // 각 게시글 클릭 시 이벤트 처리
            adapter.setOnItemClickListener((view, position) -> {
                EducationPost post = educationPostList.get(position);
                int educationId = post.getEducationId();
                Log.d("EducationActivity", "Selected educationId: " + educationId); // 로그 추가
                Intent intent = new Intent(EducationActivity.this, EducationContentView.class);
                intent.putExtra("educationId", educationId);
                startActivity(intent);
            });
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 글쓰기 액티비티에서 돌아왔을 때 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadEducationPosts(); // 글쓰기 후 게시글 다시 로드
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
