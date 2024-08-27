package com.example.hackathonproject.Education;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hackathonproject.Chat.ChatActivity;
import com.example.hackathonproject.db.ChatDAO;
import com.example.hackathonproject.db.DatabaseConnection;
import com.example.hackathonproject.R;
import com.example.hackathonproject.db.EducationDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class EducationContentView extends AppCompatActivity {
    private int postId;  // 게시글 ID를 저장할 변수
    private TextView contentTextView, titleTextView, teacherNameTextView, dateTextView;  // UI 요소들
    private ImageButton menuButton;  // 메뉴 버튼
    private EducationDAO educationDAO;  // 데이터베이스 접근 객체
    private EducationPost currentPost;  // 현재 게시글 객체 (수정 시 사용)
    private SwipeRefreshLayout swipeRefreshLayout;  // 새로고침 레이아웃

    // 로그인한 사용자의 ID를 가져오는 메서드
    private int getLoggedInUserId() {
        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        return pref.getInt("UserID", -1); // 로그인하지 않은 경우 -1 반환
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education_content_view);

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
        if (postId == -1) {
            Toast.makeText(this, "유효하지 않은 게시글 ID입니다.", Toast.LENGTH_SHORT).show();
            finish(); // 유효하지 않은 postId로 인해 액티비티를 종료
            return;
        }
        loadPostContent();  // 게시글 내용 로드 및 조회수 증가

        // "신청하기" 버튼 클릭 시 채팅방 생성 및 ChatActivity로 이동
        Button btnApply = findViewById(R.id.btnApply);
        btnApply.setOnClickListener(v -> {
            int loggedInUserId = getLoggedInUserId();
            int otherUserId = currentPost.getUserId();  // 게시글 작성자의 ID
            int postId = currentPost.getPostId();  // 현재 게시글 ID

            if (postId <= 0) {
                Toast.makeText(EducationContentView.this, "잘못된 게시글 ID입니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show();
                return;
            }
            // 데이터베이스 연결 비동기 처리
            DatabaseConnection databaseConnection = new DatabaseConnection();
            databaseConnection.connectAsync(new DatabaseConnection.DatabaseCallback() {
                @Override
                public void onSuccess(Connection connection) {
                    ChatDAO chatDAO = new ChatDAO(connection);
                    chatDAO.getOrCreateChatRoom(loggedInUserId, otherUserId, postId, null, new ChatDAO.ChatRoomCallback() {
                        @Override
                        public void onSuccess(int chatId) {
                            runOnUiThread(() -> {
                                if (chatId != -1) {
                                    // 채팅 화면으로 이동하면서 채팅방에 대한 정보를 전달
                                    Intent intent = new Intent(EducationContentView.this, ChatActivity.class);
                                    intent.putExtra("chatId", chatId);
                                    intent.putExtra("otherUserId", otherUserId);
                                    intent.putExtra("postId", postId);
                                    intent.putExtra("currentUserId", loggedInUserId);  // 현재 사용자 ID 전달
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(EducationContentView.this, "채팅방을 생성할 수 없습니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(EducationContentView.this, "채팅방을 생성할 수 없습니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                }

                @Override
                public void onError(SQLException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(EducationContentView.this, "데이터베이스 연결에 실패했습니다.", Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // 새로고침 메서드
    private void refreshContent() {
        loadPostContent();  // 게시글 내용 로드
    }

    // 게시글 내용을 로드하는 메서드
    private void loadPostContent() {
        new LoadPostTask().execute(postId); // 비동기 작업으로 데이터베이스에서 게시글 내용 불러오기
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadPostTask extends AsyncTask<Integer, Void, EducationPost> {
        @Override
        protected EducationPost doInBackground(Integer... params) {
            int postId = params[0];
            try {
                EducationPost post = educationDAO.getEducationPostById(postId);  // 게시글 ID로 게시글 가져오기
                if (post != null) {
                    educationDAO.incrementPostViews(postId); // 조회수 증가
                }
                return post;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        @Override
        protected void onPostExecute(EducationPost post) {
            swipeRefreshLayout.setRefreshing(false);  // 새로고침 완료
            if (post != null) {
                currentPost = post;  // 현재 게시글 객체 저장
                titleTextView.setText(post.getTitle());  // 제목 설정
                contentTextView.setText(post.getContent());  // 내용 설정
                teacherNameTextView.setText("작성자 | " + post.getUserName());  // 작성자 설정
                String formattedTime = formatTimeAgo(post.getCreatedAt());
                dateTextView.setText(formattedTime);  // 작성 날짜 설정

                int loggedInUserId = getLoggedInUserId();
                Button btnApply = findViewById(R.id.btnApply);

                if (loggedInUserId == post.getUserId()) {
                    // 사용자가 게시글 작성자인 경우
                    btnApply.setText("글 수정하기");
                    btnApply.setOnClickListener(v -> {
                        Intent intent = new Intent(EducationContentView.this, WriteActivity.class);
                        intent.putExtra("postId", currentPost.getPostId());
                        intent.putExtra("title", currentPost.getTitle());
                        intent.putExtra("content", currentPost.getContent());
                        intent.putExtra("location", currentPost.getLocation());
                        intent.putExtra("category", currentPost.getCategory());
                        startActivity(intent);
                    });
                } else {
                    // 사용자가 다른 사람의 글을 본 경우
                    btnApply.setText("신청하기");
                    btnApply.setOnClickListener(v -> {
                        Intent intent = new Intent(EducationContentView.this, ChatActivity.class);
                        intent.putExtra("otherUserId", post.getUserId()); // 게시글 작성자의 ID를 채팅 화면으로 전달
                        intent.putExtra("postId", postId); // 현재 게시글 ID를 전달
                        startActivity(intent);
                    });
                }
            } else {
                Toast.makeText(EducationContentView.this, "게시글을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();  // 오류 메시지 표시
            }
        }
    }

    private String formatTimeAgo(String createdAt) {
        // "2024-08-19 18:03:19.0"에서 ".0" 제거
        if (createdAt != null && createdAt.endsWith(".0")) {
            createdAt = createdAt.substring(0, createdAt.length() - 2);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime postTime = LocalDateTime.parse(createdAt, formatter);

        // 현재 시간을 KST로 가져옴
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // 작성 시간과 현재 시간의 차이 계산
        long minutes = ChronoUnit.MINUTES.between(postTime, now);
        long hours = ChronoUnit.HOURS.between(postTime, now);
        long days = ChronoUnit.DAYS.between(postTime, now);

        if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else {
            return days + "일 전";
        }
    }

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

    // 게시글 삭제 메서드
    @SuppressLint("StaticFieldLeak")
    private void deletePost() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return educationDAO.deleteEducationPost(postId);  // 데이터베이스에서 게시글 삭제
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
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

    @Override
    public boolean onSupportNavigateUp() {
        navigateBackToEducationActivity();  // 상단 바의 뒤로가기 버튼을 클릭 시 처리
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateBackToEducationActivity();  // 디바이스의 뒤로가기 버튼을 클릭 시 처리
    }

    private void navigateBackToEducationActivity() {
        Intent intent = new Intent(this, EducationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);  // 기존의 EducationActivity를 재사용
        startActivity(intent);  // EducationActivity 시작
        finish();  // 현재 액티비티 종료
    }
}
