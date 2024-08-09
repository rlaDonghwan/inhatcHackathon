package com.example.hackathonproject.Education;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hackathonproject.db.EducationDAO;
import com.example.hackathonproject.R;

public class EducationContentView extends AppCompatActivity {
    private int postId;  // 게시글 ID를 저장할 변수
    private TextView contentTextView, titleTextView, teacherNameTextView, dateTextView;  // UI 요소들
    private ImageButton menuButton;  // 메뉴 버튼
    private EducationDAO educationDAO;  // 데이터베이스 접근 객체
    private EducationPost currentPost;  // 현재 게시글 객체 (수정 시 사용)
    private SwipeRefreshLayout swipeRefreshLayout;  // 새로고침 레이아웃

    //-----------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_view);

        educationDAO = new EducationDAO(); // DAO 객체 초기화

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 기본 제목 숨기기
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 기본 백 버튼을 제거하고, XML에서 만든 버튼을 사용
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // back_button 클릭 시 뒤로 가기
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        titleTextView = findViewById(R.id.toolbar_title);  // 제목 텍스트뷰
        contentTextView = findViewById(R.id.content_text);  // 내용 텍스트뷰
        teacherNameTextView = findViewById(R.id.teacher_name);  // 작성자 이름 텍스트뷰
        dateTextView = findViewById(R.id.date);  // 날짜 텍스트뷰
        menuButton = findViewById(R.id.menu_button);  // 메뉴 버튼

        menuButton.setOnClickListener(this::showPopupMenu);  // 메뉴 버튼 클릭 시 팝업 메뉴 표시

        // SwipeRefreshLayout 초기화 및 새로고침 리스너 설정
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshContent);

        postId = getIntent().getIntExtra("postId", -1);  // 인텐트로부터 게시글 ID 가져오기
        loadPostContent();  // 게시글 내용 로드 및 조회수 증가
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        // 이제 onResume에서 새로고침을 호출하지 않으므로, 조회수는 onCreate에서만 증가합니다.
        // refreshContent(); 이 호출은 제거되었습니다.
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 새로고침 메서드
    private void refreshContent() {
        loadPostContent();  // 게시글 내용 로드
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 게시글 내용을 로드하는 메서드
    private void loadPostContent() {
        new LoadPostTask().execute(postId); // 비동기 작업으로 데이터베이스에서 게시글 내용 불러오기
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    @SuppressLint("StaticFieldLeak")
    private class LoadPostTask extends AsyncTask<Integer, Void, EducationPost> {
        @Override
        protected EducationPost doInBackground(Integer... params) {
            int postId = params[0];
            EducationPost post = educationDAO.getEducationPostById(postId);  // 게시글 ID로 게시글 가져오기
            if (post != null) {
                educationDAO.incrementPostViews(postId); // 조회수 증가
            }
            return post;
        }

        @Override
        protected void onPostExecute(EducationPost post) {
            swipeRefreshLayout.setRefreshing(false);  // 새로고침 완료
            if (post != null) {
                currentPost = post;  // 현재 게시글 객체 저장
                titleTextView.setText(post.getTitle());  // 제목 설정
                contentTextView.setText(post.getContent());  // 내용 설정
                teacherNameTextView.setText("작성자 | " + post.getUserName());  // 작성자 설정
                dateTextView.setText(post.getCreatedAt().toString().substring(0, 16));  // 작성 날짜 설정
            } else {
                Toast.makeText(EducationContentView.this, "게시글을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();  // 오류 메시지 표시
            }
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 팝업 메뉴를 보여주는 메서드
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add(0, 0, 0, "게시글 수정");  // 수정 메뉴 추가
        popup.getMenu().add(0, 1, 1, "삭제").setTitleCondensed("삭제");  // 삭제 메뉴 추가

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    // 게시글 수정 선택 시 현재 게시글의 데이터를 인텐트에 담아 WriteActivity로 전달
                    if (currentPost != null) {
                        Intent intent = new Intent(EducationContentView.this, WriteActivity.class);
                        intent.putExtra("postId", currentPost.getPostId());  // 게시글 ID 전달
                        intent.putExtra("title", currentPost.getTitle());  // 제목 전달
                        intent.putExtra("content", currentPost.getContent());  // 내용 전달
                        intent.putExtra("location", currentPost.getLocation());  // 위치 전달
                        intent.putExtra("category", currentPost.getCategory());  // 카테고리 전달
                        startActivity(intent);  // WriteActivity 시작
                    }
                    return true;
                case 1:
                    confirmDeletePost();  // 삭제 확인 다이얼로그 표시
                    return true;
                default:
                    return false;
            }
        });
        popup.show();  // 팝업 메뉴 표시
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 게시글 삭제를 확인하는 메서드
    private void confirmDeletePost() {
        new AlertDialog.Builder(this)
                .setTitle("게시글 삭제")  // 다이얼로그 제목 설정
                .setMessage("정말로 이 게시글을 삭제하시겠습니까?")  // 다이얼로그 메시지 설정
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {  // 삭제 버튼 설정
                    public void onClick(DialogInterface dialog, int which) {
                        deletePost();  // 게시글 삭제 메서드 호출
                    }
                })
                .setNegativeButton("취소", null)  // 취소 버튼 설정
                .show();  // 다이얼로그 표시
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 게시글 삭제 메서드
    @SuppressLint("StaticFieldLeak")
    private void deletePost() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return educationDAO.deleteEducationPost(postId);  // 데이터베이스에서 게시글 삭제
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(EducationContentView.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();  // 성공 메시지 표시
                    finish();  // 액티비티 종료
                } else {
                    Toast.makeText(EducationContentView.this, "게시글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();  // 실패 메시지 표시
                }
            }
        }.execute();  // 비동기 작업 실행
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean onSupportNavigateUp() {
        navigateBackToEducationActivity();  // 상단 바의 뒤로가기 버튼을 클릭 시 처리
        return true;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateBackToEducationActivity();  // 디바이스의 뒤로가기 버튼을 클릭 시 처리
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    private void navigateBackToEducationActivity() {
        Intent intent = new Intent(this, EducationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);  // 기존의 EducationActivity를 재사용
        startActivity(intent);  // EducationActivity 시작
        finish();  // 현재 액티비티 종료
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
