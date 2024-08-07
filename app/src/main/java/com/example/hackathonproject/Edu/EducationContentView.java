package com.example.hackathonproject.Edu;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.hackathonproject.DatabaseHelper;
import com.example.hackathonproject.R;

public class EducationContentView extends AppCompatActivity {
    private int postId;
    private TextView contentTextView, titleTextView, teacherNameTextView, dateTextView;
    private ImageButton menuButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        // Toolbar 제목을 빈 문자열로 설정
        getSupportActionBar().setTitle("");

        postId = getIntent().getIntExtra("postId", -1);

        contentTextView = findViewById(R.id.content_text);
        titleTextView = findViewById(R.id.toolbar_title);
        teacherNameTextView = findViewById(R.id.teacher_name);
        dateTextView = findViewById(R.id.date);
        menuButton = findViewById(R.id.menu_button);

        menuButton.setOnClickListener(v -> showPopupMenu(v));

        loadPostContent();
    }

    private void loadPostContent() {
        DatabaseHelper dbHelper = new DatabaseHelper();
        dbHelper.getEducationPostByIdAsync(postId, new DatabaseHelper.DatabaseCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onQueryComplete(Object result) {
                EducationPost post = (EducationPost) result;
                if (post != null) {
                    titleTextView.setText(post.getTitle());
                    contentTextView.setText(post.getContent());
                    teacherNameTextView.setText("작성자 | " + post.getUserName());
                    dateTextView.setText(post.getCreatedAt().toString().substring(0, 16));

                    // 조회수 증가
                    dbHelper.incrementPostViews(postId);
                } else {
                    Toast.makeText(EducationContentView.this, "게시글을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add(0, 0, 0, "게시글 수정");
        popup.getMenu().add(0, 1, 1, "삭제").setTitleCondensed("삭제");

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 0:
                        Intent intent = new Intent(EducationContentView.this, WriteActivity.class);
                        intent.putExtra("postId", postId);
                        startActivity(intent);
                        return true;
                    case 1:
                        confirmDeletePost();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

    private void confirmDeletePost() {
        new AlertDialog.Builder(this)
                .setTitle("게시글 삭제")
                .setMessage("정말로 이 게시글을 삭제하시겠습니까?")
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deletePost();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void deletePost() {
        DatabaseHelper dbHelper = new DatabaseHelper();
        dbHelper.deleteEducationPostAsync(postId, new DatabaseHelper.DatabaseCallback() {
            @Override
            public void onQueryComplete(Object result) {
                boolean success = (boolean) result;
                if (success) {
                    Toast.makeText(EducationContentView.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EducationContentView.this, "게시글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        navigateBackToEducationActivity();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateBackToEducationActivity();
    }

    private void navigateBackToEducationActivity() {
        Intent intent = new Intent(this, EducationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
