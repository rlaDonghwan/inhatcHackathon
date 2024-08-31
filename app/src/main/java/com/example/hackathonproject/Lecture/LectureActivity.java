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
import com.example.hackathonproject.db.LectureDAO;
import com.example.hackathonproject.Login.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LectureActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LectureAdapter adapter;
    private List<LecturePost> lecturePostList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private LectureManager lectureManager;
    private SessionManager sessionManager;

    //-----------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture);

        sessionManager = new SessionManager(this);
        lectureManager = new LectureManager(new LectureDAO());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        adapter = new LectureAdapter(lecturePostList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadLecturePosts);

        FloatingActionButton fab = findViewById(R.id.fab);
        if (sessionManager.isUserOrganization() || "학교".equals(sessionManager.getUserRole())) {
            fab.setVisibility(View.VISIBLE);  // 기관 또는 학교인 경우 버튼을 보이게 설정
        } else {
            fab.setVisibility(View.GONE);  // 기관도 학교도 아닌 경우 버튼을 숨김
        }

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(LectureActivity.this, LectureWriteActivity.class);
            startActivity(intent);
        });

        // 메뉴 아이템 클릭 리스너 설정
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

        adapter.setOnItemClickListener((view, position) -> {
            LecturePost post = lecturePostList.get(position);
            Intent intent = new Intent(LectureActivity.this, LectureContentView.class);
            intent.putExtra("lectureId", post.getLectureId());
            startActivity(intent);
        });

        loadLecturePosts(); // 화면을 로딩할 때도 데이터 새로 고침
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true); // 새로고침 UI 표시
        loadLecturePosts();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    private void loadLecturePosts() {
        new LoadPostsTask().execute();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    private class LoadPostsTask extends AsyncTask<Void, Void, List<LecturePost>> {
        @Override
        protected List<LecturePost> doInBackground(Void... voids) {
            return lectureManager.getAllLecturePosts();
        }

        @Override
        protected void onPostExecute(List<LecturePost> posts) {
            if (posts != null && !posts.isEmpty()) {
                Collections.sort(posts, Comparator.comparing(LecturePost::getCreatedAt).reversed());
            }

            lecturePostList.clear(); // 기존 데이터 삭제
            lecturePostList.addAll(posts); // 새 데이터 추가
            adapter.notifyDataSetChanged(); // 어댑터에 변경 사항 알림
            swipeRefreshLayout.setRefreshing(false); // 새로 고침 상태 해제
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

}