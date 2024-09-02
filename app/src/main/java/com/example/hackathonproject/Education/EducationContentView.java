package com.example.hackathonproject.Education;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.example.hackathonproject.Chat.ChatActivity;
import com.example.hackathonproject.Education.EducationPost;
import com.example.hackathonproject.db.ChatDAO;
import com.example.hackathonproject.db.DatabaseConnection;
import com.example.hackathonproject.R;
import com.example.hackathonproject.db.EducationDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class EducationContentView extends AppCompatActivity {
    private int educationId;
    private TextView contentTextView, titleTextView, teacherNameTextView, dateTextView, feeTextView, locationTextView, workName;
    private ImageButton menuButton;
    private EducationDAO educationDAO;
    private EducationPost currentPost;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView contentImageView;
    private ImageView profileImageView;

    private int getLoggedInUserId() {
        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        return pref.getInt("UserID", -1);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education_content_view);

        educationDAO = new EducationDAO();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        titleTextView = findViewById(R.id.content_title);
        contentTextView = findViewById(R.id.content_text);
        teacherNameTextView = findViewById(R.id.name);
        dateTextView = findViewById(R.id.upload_date);
        menuButton = findViewById(R.id.menu_button);
        feeTextView = findViewById(R.id.work_price);
        locationTextView = findViewById(R.id.location);
        contentImageView = findViewById(R.id.content_image);
        profileImageView = findViewById(R.id.profile_image);
        workName = findViewById(R.id.work_name);

        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);
        int LocalFontSize = 17;

        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize + 5);
        contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        teacherNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize - 3);
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize - 5);
        feeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        workName.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        locationTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, LocalFontSize);

        menuButton.setOnClickListener(this::showPopupMenu);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshContent);

        educationId = getIntent().getIntExtra("educationId", -1);
        Log.d("EducationContentView", "Received educationId: " + educationId);

        if (educationId == -1) {
            Toast.makeText(this, "유효하지 않은 게시글 ID입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadPostContent();

        Button btnApply = findViewById(R.id.btnApply);
        btnApply.setOnClickListener(v -> {
            int loggedInUserId = getLoggedInUserId();
            int otherUserId = loggedInUserId;
            int educationId = currentPost.getEducationId();

            if (educationId <= 0) {
                Toast.makeText(EducationContentView.this, "잘못된 게시글 ID입니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show();
                return;
            }

            // Use the singleton instance here
            DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
            databaseConnection.connectAsync(new DatabaseConnection.DatabaseCallback() {
                @Override
                public void onSuccess(Connection connection) {
                    ChatDAO chatDAO = new ChatDAO(connection);
                    chatDAO.getOrCreateChatRoom(loggedInUserId, otherUserId, educationId, null, new ChatDAO.ChatRoomCallback() {
                        @Override
                        public void onSuccess(int chatId) {
                            runOnUiThread(() -> {
                                if (chatId != -1) {
                                    Intent intent = new Intent(EducationContentView.this, ChatActivity.class);
                                    intent.putExtra("chatId", chatId);
                                    intent.putExtra("otherUserId", otherUserId);
                                    intent.putExtra("educationId", educationId);
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

    private void refreshContent() {
        loadPostContent();
    }

    private void loadPostContent() {
        new LoadPostTask().execute(educationId);
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadPostTask extends AsyncTask<Integer, Void, EducationPost> {
        @Override
        protected EducationPost doInBackground(Integer... params) {
            int educationId = params[0];
            try {
                EducationPost post = educationDAO.getEducationPostById(educationId);
                if (post != null) {
                    educationDAO.incrementPostViews(educationId);
                }
                return post;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(EducationPost post) {
            Log.d("EducationContentView", "Loaded post: " + post);
            swipeRefreshLayout.setRefreshing(false);

            if (post != null) {
                currentPost = post;
                titleTextView.setText(post.getTitle());
                contentTextView.setText(post.getContent());
                teacherNameTextView.setText(post.getUserName());
                String formattedTime = formatTimeAgo(post.getCreatedAt());
                dateTextView.setText(formattedTime);

                DecimalFormat df = new DecimalFormat("#,###");
                feeTextView.setText(df.format(post.getFee()));

                locationTextView.setText("위치: " + post.getLocation());

                byte[] profileImageData = post.getUserProfileImage();
                if (profileImageData != null && profileImageData.length > 0) {
                    Glide.with(EducationContentView.this)
                            .asBitmap()
                            .load(profileImageData)
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.default_profile_image);
                }

                byte[] imageData = post.getImageData();
                if (imageData != null && imageData.length > 0) {
                    Log.d("EducationContentView", "Image data size: " + imageData.length);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    contentImageView.setImageBitmap(bitmap);
                    contentImageView.setVisibility(View.VISIBLE);
                } else {
                    Log.d("EducationContentView", "No image data found, hiding image view.");
                    contentImageView.setVisibility(View.GONE);
                }

                int loggedInUserId = getLoggedInUserId();
                Button btnApply = findViewById(R.id.btnApply);
                ImageButton menuButton = findViewById(R.id.menu_button);

                if (loggedInUserId == post.getUserId()) {
                    btnApply.setText("교육 수정");
                    menuButton.setVisibility(View.VISIBLE);
                    btnApply.setOnClickListener(v -> {
                        Intent intent = new Intent(EducationContentView.this, EducationWriteActivity.class);
                        intent.putExtra("educationId", currentPost.getEducationId());
                        intent.putExtra("title", currentPost.getTitle());
                        intent.putExtra("content", currentPost.getContent());
                        intent.putExtra("location", currentPost.getLocation());
                        intent.putExtra("category", currentPost.getCategory());
                        intent.putExtra("fee", currentPost.getFee());
                        startActivity(intent);
                    });
                } else {
                    btnApply.setText("신청하기");
                    menuButton.setVisibility(View.GONE);
                    btnApply.setOnClickListener(v -> {
                        Intent intent = new Intent(EducationContentView.this, ChatActivity.class);
                        intent.putExtra("otherUserId", post.getUserId());
                        intent.putExtra("educationId", educationId);
                        startActivity(intent);
                    });
                }

                menuButton.setOnClickListener(v -> showPopupMenu(v));

            } else {
                Toast.makeText(EducationContentView.this, "게시글을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String formatTimeAgo(String createdAt) {
        if (createdAt != null && createdAt.endsWith(".0")) {
            createdAt = createdAt.substring(0, createdAt.length() - 2);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime postTime = LocalDateTime.parse(createdAt, formatter);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

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

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add(0, 0, 0, "게시글 수정");
        popup.getMenu().add(0, 1, 1, "삭제").setTitleCondensed("삭제");
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    if (currentPost != null) {
                        Intent intent = new Intent(EducationContentView.this, EducationWriteActivity.class);
                        intent.putExtra("educationId", currentPost.getEducationId());
                        intent.putExtra("title", currentPost.getTitle());
                        intent.putExtra("content", currentPost.getContent());
                        intent.putExtra("location", currentPost.getLocation());
                        intent.putExtra("category", currentPost.getCategory());
                        intent.putExtra("fee", currentPost.getFee());
                        intent.putExtra("imageData", currentPost.getImageData());
                        startActivity(intent);
                    }
                    return true;
                case 1:
                    confirmDeletePost();
                    return true;
                default:
                    return false;
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

    @SuppressLint("StaticFieldLeak")
    private void deletePost() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return educationDAO.deleteEducationPost(educationId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(EducationContentView.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EducationContentView.this, "게시글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
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