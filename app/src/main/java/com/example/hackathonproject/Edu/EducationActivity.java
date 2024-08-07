package com.example.hackathonproject.Edu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hackathonproject.DatabaseHelper;
import com.example.hackathonproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class EducationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EducationAdapter adapter;
    private List<EducationPost> educationPostList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edu);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        adapter = new EducationAdapter(educationPostList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadEducationPosts);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(EducationActivity.this, WriteActivity.class);
            startActivityForResult(intent, 100);
        });

        loadEducationPosts();
    }

    private void loadEducationPosts() {
        DatabaseHelper dbHelper = new DatabaseHelper();
        dbHelper.getAllEducationPostsAsync(new DatabaseHelper.DatabaseCallback() {
            @Override
            public void onQueryComplete(Object result) {
                educationPostList = (List<EducationPost>) result;
                adapter.updateData(educationPostList);
                swipeRefreshLayout.setRefreshing(false);

                adapter.setOnItemClickListener(new EducationAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        EducationPost post = educationPostList.get(position);
                        Intent intent = new Intent(EducationActivity.this, EducationContentView.class);
                        intent.putExtra("postId", post.getPostId());
                        startActivity(intent);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadEducationPosts();
        }
    }
}
