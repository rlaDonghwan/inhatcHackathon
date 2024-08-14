package com.example.hackathonproject.Seminar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.Chat.ChatListActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.hackathonproject.Education.EducationActivity;
import com.example.hackathonproject.R;
import com.example.hackathonproject.Setting.SettingsActivity;
import com.example.hackathonproject.db.SeminarDAO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SeminarActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SeminarAdapter adapter;
    private List<SeminarPost> seminarPostList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private SeminarManager seminarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seminor);

        seminarManager = new SeminarManager(new SeminarDAO());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        adapter = new SeminarAdapter(seminarPostList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadSeminarPosts);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(SeminarActivity.this, SeminarWriteActivity.class);
            startActivity(intent);
        });

        loadSeminarPosts();

        LinearLayout firstMenuItem = findViewById(R.id.first_menu_item);
        firstMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(SeminarActivity.this, EducationActivity.class);
            startActivity(intent);
        });

        LinearLayout secondMenuItem = findViewById(R.id.second_menu_item);
        secondMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(SeminarActivity.this, SeminarActivity.class);
            startActivity(intent);
        });

        LinearLayout thirdMenuItem = findViewById(R.id.third_menu_item);
        thirdMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(SeminarActivity.this, ChatListActivity.class);
            startActivity(intent);
        });

        LinearLayout fourthMenuItem = findViewById(R.id.fourth_menu_item);
        fourthMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(SeminarActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // SeminarPost 클릭 시 이벤트 처리
        adapter.setOnItemClickListener(new SeminarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                SeminarPost post = seminarPostList.get(position);
                Intent intent = new Intent(SeminarActivity.this, SeminarContentView.class); // SeminarContentView로 이동
                intent.putExtra("lectureId", post.getLectureId()); // 게시글 ID 전달
                startActivity(intent); // 액티비티 시작
            }
        });
    }

    private void loadSeminarPosts() {
        new LoadPostsTask().execute();
    }

    private class LoadPostsTask extends AsyncTask<Void, Void, List<SeminarPost>> {
        @Override
        protected List<SeminarPost> doInBackground(Void... voids) {
            return seminarManager.getAllSeminarPosts();
        }

        @Override
        protected void onPostExecute(List<SeminarPost> posts) {
            seminarPostList = posts;
            adapter.updateData(seminarPostList);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
