package com.example.hackathonproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class EducationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EducationAdapter adapter;
    private List<Education> educationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // 부모 클래스의 onCreate 메서드 호출
        setContentView(R.layout.activity_edu); // 레이아웃 설정

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseHelper dbHelper = new DatabaseHelper();
        dbHelper.getAllEducationsAsync(new DatabaseHelper.DatabaseCallback() {
            @Override
            public void onQueryComplete(Object result) {
                educationList = (List<Education>) result;
                adapter = new EducationAdapter(educationList);
                recyclerView.setAdapter(adapter);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EducationActivity.this, WriteActivity.class);
                startActivity(intent);
            }
        });
    }
}
