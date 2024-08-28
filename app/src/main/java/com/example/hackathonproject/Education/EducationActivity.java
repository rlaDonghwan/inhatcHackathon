package com.example.hackathonproject.Education;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

        loadEducationPosts(); // 게시글 로드

        // 교육 받기 탭 클릭 시 EducationActivity로 이동
        LinearLayout firstMenuItem = findViewById(R.id.first_menu_item);
        firstMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(EducationActivity.this, EducationActivity.class);
            startActivity(intent);
        });

        // 강연자 신청 탭 클릭 시 SeminarActivity로 이동
        LinearLayout secondMenuItem = findViewById(R.id.second_menu_item);
        secondMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(EducationActivity.this, LectureActivity.class);
            startActivity(intent);
        });

        // 채팅 탭 클릭 시 ChatActivity로 이동
        LinearLayout thirdMenuItem = findViewById(R.id.third_menu_item);
        thirdMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(EducationActivity.this, ChatListActivity.class);
            startActivity(intent);
        });

        // 설정 탭 클릭 시 SettingsActivity로 이동
        LinearLayout fourthMenuItem = findViewById(R.id.fourth_menu_item);
        fourthMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(EducationActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

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
            return educationManager.getAllEducationPosts();
        }

        @Override
        protected void onPostExecute(List<EducationPost> posts) {
            // 게시글 로드 완료 후 UI 갱신
            educationPostList = posts;
            adapter.updateData(educationPostList); // 어댑터에 데이터 업데이트
            swipeRefreshLayout.setRefreshing(false); // 새로고침 중지

            // 각 게시글 클릭 시 이벤트 처리
            adapter.setOnItemClickListener(new EducationAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    EducationPost post = educationPostList.get(position);
                    Intent intent = new Intent(EducationActivity.this, EducationContentView.class); // 게시글 내용 보기 액티비티로 이동
                    intent.putExtra("educationId", post.getEducationId()); // 게시글 ID 전달
                    startActivity(intent); // 액티비티 시작
                }
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
