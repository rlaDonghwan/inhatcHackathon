package com.example.hackathonproject.Lecture;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hackathonproject.Chat.ChatListActivity;
import com.example.hackathonproject.Education.EducationActivity;
import com.example.hackathonproject.R;
import com.example.hackathonproject.Setting.SettingsActivity;
import com.example.hackathonproject.db.SeminarDAO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class LectureActivity extends AppCompatActivity {
    private RecyclerView recyclerView;  // RecyclerView 변수 선언 (세미나 게시글 리스트를 보여줄 때 사용)
    private LectureAdapter adapter;  // SeminarAdapter 변수 선언 (RecyclerView에 데이터를 연결할 때 사용)
    private List<LecturePost> seminarPostList = new ArrayList<>();  // 세미나 게시글 리스트를 저장할 리스트 변수 선언
    private SwipeRefreshLayout swipeRefreshLayout;  // SwipeRefreshLayout 변수 선언 (새로고침 기능을 구현할 때 사용)
    private LectureManager seminarManager;  // SeminarManager 변수 선언 (세미나 게시글 데이터를 관리할 때 사용)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture);

        seminarManager = new LectureManager(new SeminarDAO());  // SeminarManager 객체 초기화

        recyclerView = findViewById(R.id.recyclerView);  // RecyclerView 레이아웃 요소 초기화
        recyclerView.setLayoutManager(new LinearLayoutManager(this));  // RecyclerView의 레이아웃 매니저 설정 (리스트 형태로 설정)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);  // SwipeRefreshLayout 레이아웃 요소 초기화

        adapter = new LectureAdapter(seminarPostList);  // SeminarAdapter 초기화 (데이터를 연결하기 위해)
        recyclerView.setAdapter(adapter);  // RecyclerView에 어댑터 설정

        swipeRefreshLayout.setOnRefreshListener(this::loadSeminarPosts);  // 새로고침 리스너 설정 (사용자가 새로고침할 때 호출될 메서드)

        FloatingActionButton fab = findViewById(R.id.fab);  // FloatingActionButton 초기화 (새 게시글 작성 버튼)
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(LectureActivity.this, LectureWriteActivity.class);  // 새 게시글 작성 화면으로 이동하는 인텐트 설정
            startActivity(intent);
        });

        loadSeminarPosts();  // 세미나 게시글 로드 메서드 호출

        // 하단 네비게이션 바의 각 메뉴 아이템 클릭 시 각 화면으로 이동하는 코드
        LinearLayout firstMenuItem = findViewById(R.id.first_menu_item);
        firstMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(LectureActivity.this, EducationActivity.class);
            startActivity(intent);
        });

        LinearLayout secondMenuItem = findViewById(R.id.second_menu_item);
        secondMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(LectureActivity.this, LectureActivity.class);
            startActivity(intent);
        });

        LinearLayout thirdMenuItem = findViewById(R.id.third_menu_item);
        thirdMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(LectureActivity.this, ChatListActivity.class);
            startActivity(intent);
        });

        LinearLayout fourthMenuItem = findViewById(R.id.fourth_menu_item);
        fourthMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(LectureActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // SeminarPost(세미나 게시글) 클릭 시 이벤트 처리
        adapter.setOnItemClickListener(new LectureAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                LecturePost post = seminarPostList.get(position);  // 클릭된 게시글의 정보를 가져옴
                Intent intent = new Intent(LectureActivity.this, LectureContentView.class);  // 세미나 상세보기 화면으로 이동하는 인텐트 설정
                intent.putExtra("lectureId", post.getLectureId());  // 게시글 ID를 인텐트에 전달
                startActivity(intent);  // 액티비티 시작
            }
        });
    }

    // 세미나 게시글을 로드하는 메서드
    private void loadSeminarPosts() {
        new LoadPostsTask().execute();  // 비동기 작업으로 게시글 데이터를 로드
    }

    // 비동기 작업을 수행하는 클래스
    private class LoadPostsTask extends AsyncTask<Void, Void, List<LecturePost>> {
        @Override
        protected List<LecturePost> doInBackground(Void... voids) {
            return seminarManager.getAllSeminarPosts();  // SeminarManager를 통해 모든 세미나 게시글 데이터를 가져옴
        }

        @Override
        protected void onPostExecute(List<LecturePost> posts) {
            seminarPostList = posts;  // 가져온 세미나 게시글 데이터를 리스트에 저장
            adapter.updateData(seminarPostList);  // 어댑터에 새로운 데이터 설정
            swipeRefreshLayout.setRefreshing(false);  // 새로고침 완료 표시
        }
    }
}
