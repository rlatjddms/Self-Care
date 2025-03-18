package com.example.capstone_hospital;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class DiseaseActivity extends AppCompatActivity {

    Button btnBack, btnHospital;
    CheckBox bookmark;
    TextView tvDiseaseName, tvDefinition, tvSymptom, tvCause, tvManagement, tvMedicalSubject;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    String userId = "";
    String diseaseName = "";
    String definition = "";
    String symptom = "";
    String cause = "";
    String management = "";
    String medicalSubject = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnHospital = (Button) findViewById(R.id.btnHospital);
        bookmark = (CheckBox) findViewById(R.id.bookmark);
        tvDiseaseName = (TextView) findViewById(R.id.tvDiseaseName);
        tvDefinition = (TextView) findViewById(R.id.tvDefinition);
        tvSymptom = (TextView) findViewById(R.id.tvSymptom);
        tvCause = (TextView) findViewById(R.id.tvCause);
        tvManagement = (TextView) findViewById(R.id.tvManagement);
        tvMedicalSubject = (TextView) findViewById(R.id.tvMedicalSubject);

        if(getIntent().getStringExtra("selectedBookmark") != null) {
            String[] itemSplit = getIntent().getStringExtra("selectedBookmark").split("\\|");
            diseaseName = itemSplit[1];
        }
        else {
            diseaseName = getIntent().getStringExtra("diseaseName");
        }

        tvDiseaseName.setText(diseaseName);

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        userId = sharedPref.getString("userId", "");

        String sql = "select * from disease where diseaseName = ?";
        Log.d("SQL", sql);
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{diseaseName});

        while (cursor.moveToNext()) {
            definition = cursor.getString(2);
            symptom = cursor.getString(3);
            cause = cursor.getString(4);
            management = cursor.getString(5);
            medicalSubject = cursor.getString(6);
        }

        tvDefinition.setText(definition);
        tvSymptom.setText(symptom);
        tvCause.setText(cause);
        tvManagement.setText(management);
        tvMedicalSubject.setText(medicalSubject);

        sql = "select bookmark from member where id ='" + userId + "'";
        Log.d("SQL", sql);
        cursor = sqlDB.rawQuery(sql, null);

        String memberBookmark = "";
        if (cursor.moveToFirst()) {
            memberBookmark = cursor.getString(0);
        }
        cursor.close();

        if (memberBookmark != null && memberBookmark.contains("0|" + diseaseName)) {
            bookmark.setChecked(true);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HospitalRecommendActivity.class);
                intent.putExtra("medicalSubject", medicalSubject);
                startActivity(intent);
            }
        });

        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bookmark.isChecked()) {
                    Toast.makeText(getApplicationContext(), "즐겨찾기에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                    updateBookmark();
                }
                else {
                    Toast.makeText(getApplicationContext(), "즐겨찾기에서 제거되었습니다.", Toast.LENGTH_SHORT).show();
                    removeBookmark();
                }
            }
        });
    }

    private void updateBookmark() {
        String bookmark = 0 + "|" + diseaseName;

        Cursor cursor = sqlDB.rawQuery("SELECT bookmark FROM member WHERE id = ?", new String[]{userId});
        String currentBookmark = "";
        if (cursor.moveToFirst()) {
            currentBookmark = cursor.getString(0);
            if (currentBookmark == null) {
                currentBookmark = "";
            }
        }
        cursor.close();

        String updatedBookmark = currentBookmark.isEmpty() ? bookmark : bookmark + "~" + currentBookmark;

        sqlDB.execSQL("UPDATE member SET bookmark = ? WHERE id = ?", new Object[]{updatedBookmark, userId});

        // 서버에 POST 요청 보내기
        new Thread(() -> {
            try {
                URL url = new URL("https://kse.calab.myds.me/update_member.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String columnName = "bookmark";
                String postData = "columnName=" + URLEncoder.encode(columnName, "UTF-8") +
                        "&userId=" + URLEncoder.encode(userId, "UTF-8") +
                        "&item=" + URLEncoder.encode(updatedBookmark, "UTF-8");

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    Log.d("SERVER_RESPONSE", response.toString());
                } else {
                    Log.e("SERVER_ERROR", "Response code: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e("HTTP_ERROR", e.getMessage());
            }
        }).start();
    }

    private void removeBookmark() {
        String bookmarkToRemove = 0 + "|" + diseaseName;

        Cursor cursor = sqlDB.rawQuery("SELECT bookmark FROM member WHERE id = ?", new String[]{userId});
        String currentBookmark = "";
        if (cursor.moveToFirst()) {
            currentBookmark = cursor.getString(0);
        }
        cursor.close();

        String removedBookmark = currentBookmark.replace(bookmarkToRemove, "").replaceAll("~~", "~").trim();

        if (removedBookmark.startsWith("~")) {
            removedBookmark = removedBookmark.substring(1);
        }
        if (removedBookmark.endsWith("~")) {
            removedBookmark = removedBookmark.substring(0, removedBookmark.length() - 1);
        }

        if (removedBookmark == null || removedBookmark.isEmpty()) {
            removedBookmark = "";
        }

        sqlDB.execSQL("UPDATE member SET bookmark = ? WHERE id = ?", new Object[]{removedBookmark, userId});

        // 서버에 POST 요청 보내기
        String finalRemovedBookmark = removedBookmark;
        new Thread(() -> {
            try {
                URL url = new URL("https://kse.calab.myds.me/update_member.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String columnName = "bookmark";
                String postData = "columnName=" + URLEncoder.encode(columnName, "UTF-8") +
                        "&userId=" + URLEncoder.encode(userId, "UTF-8") +
                        "&item=" + URLEncoder.encode(finalRemovedBookmark, "UTF-8");

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    Log.d("SERVER_RESPONSE", response.toString());
                } else {
                    Log.e("SERVER_ERROR", "Response code: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e("HTTP_ERROR", e.getMessage());
            }
        }).start();
    }
}