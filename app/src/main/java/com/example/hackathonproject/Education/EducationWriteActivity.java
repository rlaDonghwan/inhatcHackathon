package com.example.hackathonproject.Education;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.Login.SessionManager;
import com.example.hackathonproject.R;
import com.example.hackathonproject.db.EducationDAO;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

public class EducationWriteActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationProviderClient fusedLocationClient;
    private String currentLocation = "서울"; // 기본 위치는 서울로 설정

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText priceEditText;
    private Button submitButton;
    private CheckBox checkBoxBuy;
    private CheckBox checkBoxSell;
    private EducationDAO educationDAO;
    private SessionManager sessionManager;
    private int educationId = -1; // 수정 시 사용할 교육 게시글 ID
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education_write);

        educationDAO = new EducationDAO(); // DAO 객체 초기화
        sessionManager = new SessionManager(this); // SessionManager 객체 초기화

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        titleEditText = findViewById(R.id.title_edit_text);
        descriptionEditText = findViewById(R.id.content_edit_text);
        submitButton = findViewById(R.id.btnSummit);
        checkBoxBuy = findViewById(R.id.checkbox_buy);
        checkBoxSell = findViewById(R.id.checkbox_sell);
        priceEditText = findViewById(R.id.price_edit_text);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);

        checkBoxBuy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBoxSell.setChecked(false);
            }
        });

        checkBoxSell.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBoxBuy.setChecked(false);
            }
        });

        // 위치 권한 요청
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 위치 정보 가져오기
            getLastKnownLocation();
        }

        Intent intent = getIntent();
        if (intent.hasExtra("educationId")) {
            educationId = intent.getIntExtra("educationId", -1);
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            String category = intent.getStringExtra("category");
            int fee = intent.getIntExtra("fee", 0);

            titleEditText.setText(title);
            descriptionEditText.setText(content);
            priceEditText.setText(String.valueOf(fee));

            if ("구해요".equals(category)) {
                checkBoxBuy.setChecked(true);
            } else if ("할게요".equals(category)) {
                checkBoxSell.setChecked(true);
            }

            toolbarTitle.setText("교육 수정");
            submitButton.setOnClickListener(v -> updateEducationPost());
        } else {
            toolbarTitle.setText("교육 신청");
            submitButton.setOnClickListener(v -> submitEducation());
        }

        Button imageButton = findViewById(R.id.image_button);
        imageButton.setOnClickListener(v -> openImagePicker());

        submitButton.setOnClickListener(v -> {
            if (educationId != -1) {
                updateEducationPost();
            } else {
                submitEducation();
            }
        });
    }

    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            currentLocation = getAddressFromLocation(location); // 좌표를 주소로 변환하여 저장
                        } else {
                            // 위치를 가져올 수 없을 경우 IP 주소 기반 위치로 대체
                            new GetLocationFromIPTask().execute();
                        }
                    }
                });
    }

    // 좌표를 주소로 변환하는 메서드
    private String getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            } else {
                return "주소를 찾을 수 없습니다.";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "주소를 찾을 수 없습니다.";
        }
    }

    // IP 주소를 통해 위치를 가져오는 비동기 작업
    private class GetLocationFromIPTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String location = "서울";
            try {
                URL url = new URL("https://ipinfo.io/json");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                InputStream inputStream = urlConnection.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int read;
                byte[] buffer = new byte[1024];
                while ((read = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, read);
                }
                String jsonResponse = byteArrayOutputStream.toString();

                JSONObject jsonObject = new JSONObject(jsonResponse);
                String loc = jsonObject.getString("loc");
                String[] latLng = loc.split(",");
                double latitude = Double.parseDouble(latLng[0]);
                double longitude = Double.parseDouble(latLng[1]);

                return getAddressFromLocation(new Location("IP") {{
                    setLatitude(latitude);
                    setLongitude(longitude);
                }});
            } catch (Exception e) {
                Log.e("IP Location Error", "Error getting location from IP", e);
            }
            return location;
        }

        @Override
        protected void onPostExecute(String location) {
            currentLocation = location;
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ImageView imagePreview = findViewById(R.id.image_preview);
            imagePreview.setVisibility(View.VISIBLE);
            imagePreview.setImageURI(imageUri);
        }
    }

    private void submitEducation() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String category = checkBoxBuy.isChecked() ? "구해요" : checkBoxSell.isChecked() ? "할게요" : "";
        String feeStr = priceEditText.getText().toString().trim();
        int fee = feeStr.isEmpty() ? 0 : Integer.parseInt(feeStr);

        if (title.isEmpty() || description.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "제목, 내용, 카테고리를 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new SubmitEducationTask().execute(title, category, description, currentLocation, fee, imageUri);
    }

    private void updateEducationPost() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String category = checkBoxBuy.isChecked() ? "구해요" : checkBoxSell.isChecked() ? "할게요" : "";
        String feeStr = priceEditText.getText().toString().trim();
        int fee = feeStr.isEmpty() ? 0 : Integer.parseInt(feeStr);

        if (title.isEmpty() || description.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "제목, 내용, 카테고리를 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new UpdateEducationTask().execute(educationId, title, category, description, currentLocation, fee);
    }

    private class SubmitEducationTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            String title = (String) params[0];
            String category = (String) params[1];
            String description = (String) params[2];
            String location = (String) params[3];
            int fee = (int) params[4];
            int userId = sessionManager.getUserId();
            Uri imageUri = params.length > 5 ? (Uri) params[5] : null;

            byte[] imageBytes = null;
            if (imageUri != null) {
                imageBytes = getImageBytes(imageUri);
            }

            return educationDAO.submitEducationWithImage(title, category, description, location, fee, userId, ZonedDateTime.now(ZoneId.of("Asia/Seoul")), imageBytes);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(EducationWriteActivity.this, "게시글이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(EducationWriteActivity.this, "게시글 등록에 실패하였습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            }
        }

        private byte[] getImageBytes(Uri uri) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];

                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                return byteBuffer.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private class UpdateEducationTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            int educationId = (int) params[0];
            String title = (String) params[1];
            String category = (String) params[2];
            String description = (String) params[3];
            String location = (String) params[4];
            int fee = (int) params[5];
            int userId = sessionManager.getUserId();

            try {
                return educationDAO.updateEducationPost(educationId, title, category, description, location, fee, userId);
            } catch (Exception e) {
                Log.e("UpdateEducationTask", "Error updating post", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(EducationWriteActivity.this, "게시글이 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(EducationWriteActivity.this, "게시글 수정에 실패하였습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}