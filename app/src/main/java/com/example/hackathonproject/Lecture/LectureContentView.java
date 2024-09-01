package com.example.hackathonproject.Lecture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hackathonproject.Chat.ChatActivity;
import com.example.hackathonproject.R;
import com.example.hackathonproject.db.ChatDAO;
import com.example.hackathonproject.db.DatabaseConnection;
import com.example.hackathonproject.db.LectureDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class LectureContentView extends AppCompatActivity {
    private int lectureId;  // 강연 ID를 저장할 변수
    private TextView contentTextView, titleTextView, lecturerNameTextView, dateTextView, feeTextView, locationTextView, workName;  // UI 요소들
    private ImageButton menuButton;  // 메뉴 버튼
    private ImageView contentImageView;  // 추가된 이미지뷰 참조
    private LectureDAO lectureDAO;  // 데이터베이스 접근 객체
    private LecturePost currentPost;  // 현재 게시글 객체 (수정 시 사용)
    private SwipeRefreshLayout swipeRefreshLayout;  // 새로고침 레이아웃

    // 로그인한 사용자의 ID를 가져오는 메서드
    private int getLoggedInUserId() {
        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        return pref.getInt("UserID", -1); // 로그인하지 않은 경우 -1 반환
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_content_view);

        lectureDAO = new LectureDAO(); // DAO 객체 초기화

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 기본 제목 숨기기
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 기본 백 버튼을 제거하고, XML에서 만든 버튼을 사용
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // back_button 클릭 시 뒤로 가기
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // UI 요소 초기화
        titleTextView = findViewById(R.id.content_title);  // 제목 텍스트뷰
        contentTextView = findViewById(R.id.content_text);  // 내용 텍스트뷰
        lecturerNameTextView = findViewById(R.id.name);  // 강연자 이름 텍스트뷰
        dateTextView = findViewById(R.id.upload_date);  // 날짜 텍스트뷰
        feeTextView = findViewById(R.id.work_price);  // 강연료 텍스트뷰
        locationTextView = findViewById(R.id.location);  // 위치 텍스트뷰
        menuButton = findViewById(R.id.menu_button);  // 메뉴 버튼
        contentImageView = findViewById(R.id.content_image);  // 이미지뷰 참조
        workName = findViewById(R.id.work_name);  // 강연명 텍스트뷰

        // SharedPreferences에서 폰트 크기 설정 불러오기
        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);
        int LocalFontSize = 17;

        // 텍스트뷰의 폰트 크기 적용
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize + 5);  // 제목은 크게
        contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);  // 내용
        lecturerNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize - 3);  // 강연자 이름
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize - 5);  // 작성일자 더 작게
        feeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);  // 강연료
        workName.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);  // 강연명
        locationTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, LocalFontSize);  // 위치

        // 메뉴 버튼 클릭 시 팝업 메뉴 표시
        menuButton.setOnClickListener(this::showPopupMenu);

        // SwipeRefreshLayout 초기화 및 새로고침 리스너 설정
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshContent);

        // Intent로부터 강연 ID 가져오기
        lectureId = getIntent().getIntExtra("lectureId", -1);
        loadLectureContent();  // 강연 내용 로드 및 조회수 증가

        // "신청하기" 버튼 클릭 시 채팅방 생성 및 ChatActivity로 이동
        Button btnApply = findViewById(R.id.btnApply);
        btnApply.setOnClickListener(v -> {
            int loggedInUserId = getLoggedInUserId();
            int otherUserId = currentPost.getUserId();  // 강연 작성자의 ID
            int lectureId = currentPost.getLectureId();  // 현재 강연 ID

            if (lectureId <= 0) {
                Toast.makeText(LectureContentView.this, "잘못된 강연 ID입니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show();
                return;
            }

            // 데이터베이스 연결 비동기 처리
            DatabaseConnection databaseConnection = new DatabaseConnection();
            databaseConnection.connectAsync(new DatabaseConnection.DatabaseCallback() {
                @Override
                public void onSuccess(Connection connection) {
                    ChatDAO chatDAO = new ChatDAO(connection);
                    chatDAO.getOrCreateChatRoom(loggedInUserId, otherUserId, null, lectureId, new ChatDAO.ChatRoomCallback() {
                        @Override
                        public void onSuccess(int chatId) {
                            runOnUiThread(() -> {
                                if (chatId != -1) {
                                    // 채팅 화면으로 이동하면서 채팅방에 대한 정보를 전달
                                    Intent intent = new Intent(LectureContentView.this, ChatActivity.class);
                                    intent.putExtra("chatId", chatId);
                                    intent.putExtra("otherUserId", otherUserId);
                                    intent.putExtra("lectureId", lectureId);
                                    intent.putExtra("currentUserId", loggedInUserId);  // 현재 사용자 ID 전달
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(LectureContentView.this, "채팅방을 생성할 수 없습니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(LectureContentView.this, "채팅방을 생성할 수 없습니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                }

                @Override
                public void onError(SQLException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(LectureContentView.this, "데이터베이스 연결에 실패했습니다.", Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }

    // 새로고침 메서드
    private void refreshContent() {
        loadLectureContent();  // 강연 내용 로드
    }

    // 강연 내용을 로드하는 메서드
    private void loadLectureContent() {
        new LoadLectureTask().execute(lectureId); // 비동기 작업으로 데이터베이스에서 강연 내용 불러오기
    }

    // 비동기 작업으로 강연 내용을 로드하는 클래스
    @SuppressLint("StaticFieldLeak")
    private class LoadLectureTask extends AsyncTask<Integer, Void, LecturePost> {
        @Override
        protected LecturePost doInBackground(Integer... params) {
            int lectureId = params[0];
            LecturePost post = lectureDAO.getLecturePostById(lectureId);  // 강연 ID로 강연 가져오기
            if (post != null) {
                lectureDAO.incrementLecturePostViews(lectureId); // 조회수 증가
            }
            return post;
        }

        @Override
        protected void onPostExecute(LecturePost post) {
            swipeRefreshLayout.setRefreshing(false);  // 새로고침 완료
            if (post != null) {
                // 강연 내용 UI에 설정
                currentPost = post;  // 현재 강연 객체 저장
                titleTextView.setText(post.getTitle());  // 제목 설정
                contentTextView.setText(post.getContent());  // 내용 설정
                lecturerNameTextView.setText(post.getUserName());  // 사용자 이름 설정
                String formattedTime = formatTimeAgo(post.getCreatedAt());
                dateTextView.setText(formattedTime);  // 작성 날짜 설정

                DecimalFormat df = new DecimalFormat("#,###");  // 소수점 없이 강연료 포맷 설정
                feeTextView.setText(df.format(post.getFee()));  // 강연료 설정

                locationTextView.setText("위치: " + post.getLocation());  // 위치 설정

                // 이미지 설정
                byte[] imageData = post.getImageData();
                if (imageData != null && imageData.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    contentImageView.setImageBitmap(bitmap);  // 이미지 설정
                } else {
                    contentImageView.setVisibility(View.GONE);  // 이미지가 없을 경우 ImageView 숨기기
                }

                int loggedInUserId = getLoggedInUserId();
                Button btnApply = findViewById(R.id.btnApply);
                if (loggedInUserId == post.getUserId()) {
                    // 사용자가 강연 작성자인 경우
                    btnApply.setText("강연 수정");
                    btnApply.setOnClickListener(v -> {
                        Intent intent = new Intent(LectureContentView.this, LectureWriteActivity.class);
                        intent.putExtra("lectureId", currentPost.getLectureId());
                        intent.putExtra("title", currentPost.getTitle());
                        intent.putExtra("content", currentPost.getContent());
                        intent.putExtra("fee", currentPost.getFee());  // 금액 전달
                        intent.putExtra("location", currentPost.getLocation());
                        intent.putExtra("isYouthAudienceAllowed", currentPost.isYouthAudienceAllowed()); // 청년 참관 가능 상태 전달
                        intent.putExtra("imageData", currentPost.getImageData()); // 이미지 데이터 전달
                        startActivity(intent);
                    });

                    // 메뉴 버튼을 표시
                    menuButton.setVisibility(View.VISIBLE);

                } else {
                    // 사용자가 다른 사람의 강연을 본 경우
                    btnApply.setText("신청하기");
                    btnApply.setOnClickListener(v -> {
                        Intent intent = new Intent(LectureContentView.this, ChatActivity.class);
                        intent.putExtra("otherUserId", post.getUserId()); // 강연 작성자의 ID를 채팅 화면으로 전달
                        intent.putExtra("lectureId", lectureId); // 현재 강연 ID를 전달
                        startActivity(intent);
                    });

                    // 메뉴 버튼을 숨김
                    menuButton.setVisibility(View.GONE);
                }
            } else {
                // 강연 로드 실패 시 메시지 표시
                Toast.makeText(LectureContentView.this, "강연을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 시간을 "몇 분 전" 등의 형식으로 변환하는 메서드
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
        // 팝업 메뉴에 항목 추가
        popup.getMenu().add(0, 0, 0, "강연 수정");  // 수정 메뉴 추가
        popup.getMenu().add(0, 1, 1, "삭제").setTitleCondensed("삭제");  // 삭제 메뉴 추가

        // 팝업 메뉴 항목 클릭 시 처리
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    // 강연 수정 선택 시, LectureWriteActivity로 이동
                    if (currentPost != null) {
                        Intent intent = new Intent(LectureContentView.this, LectureWriteActivity.class);
                        intent.putExtra("lectureId", currentPost.getLectureId());  // 강연 ID 전달
                        intent.putExtra("title", currentPost.getTitle());  // 제목 전달
                        intent.putExtra("content", currentPost.getContent());  // 내용 전달
                        intent.putExtra("location", currentPost.getLocation());  // 위치 전달
                        intent.putExtra("fee", currentPost.getFee());  // 강연료 전달
                        intent.putExtra("isYouthAudienceAllowed", currentPost.isYouthAudienceAllowed());  // 청년 참관 가능 여부 전달
                        intent.putExtra("imageData", currentPost.getImageData());  // 이미지 데이터 전달
                        startActivity(intent);  // WriteActivity 시작
                    }
                    return true;
                case 1:
                    // 삭제 확인 다이얼로그 표시
                    confirmDeleteLecture();
                    return true;
                default:
                    return false;
            }
        });
        popup.show();  // 팝업 메뉴 표시
    }

    // 강연 삭제를 확인하는 메서드
    private void confirmDeleteLecture() {
        new AlertDialog.Builder(this).setTitle("강연 삭제")  // 다이얼로그 제목 설정
                .setMessage("정말로 이 게시글을 삭제하시겠습니까?")  // 다이얼로그 메시지 설정
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {  // 삭제 버튼 설정
                    public void onClick(DialogInterface dialog, int which) {
                        deleteLecture();  // 강연 삭제 메서드 호출
                    }
                }).setNegativeButton("취소", null)  // 취소 버튼 설정
                .show();  // 다이얼로그 표시
    }

    // 강연 삭제 메서드
    @SuppressLint("StaticFieldLeak")
    private void deleteLecture() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return lectureDAO.deleteLecturePost(lectureId);  // 데이터베이스에서 강연 삭제
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(LectureContentView.this, "강연이 삭제되었습니다.", Toast.LENGTH_SHORT).show();  // 성공 메시지 표시
                    finish();  // 액티비티 종료
                } else {
                    Toast.makeText(LectureContentView.this, "강연 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();  // 실패 메시지 표시
                }
            }
        }.execute();  // 비동기 작업 실행
    }

    // 상단 바의 뒤로가기 버튼을 클릭 시 호출
    @Override
    public boolean onSupportNavigateUp() {
        navigateBackToLectureActivity();
        return true;
    }

    // 디바이스의 뒤로가기 버튼을 클릭 시 호출
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateBackToLectureActivity();
    }

    // LectureActivity로 돌아가는 메서드
    private void navigateBackToLectureActivity() {
        Intent intent = new Intent(this, LectureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);  // 기존의 LectureActivity를 재사용
        startActivity(intent);  // LectureActivity 시작
        finish();  // 현재 액티비티 종료
    }
}