package com.example.capstone_hospital;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.ArrayList;

public class EditProfileActivity extends AppCompatActivity {

    Button btnBack, btnCheckPw, btnDone, btnLogOut, btnWithdrawal;
    TextView tvCheck, tvCheckPw, tvCheckPw2;
    EditText etName, etCurrentPw, etNewPw, etNewPwCheck, etPhone, etEmail, etAddress;
    LinearLayout pwLayout;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    String userId = "";
    String userName = "";
    String userPw = "";
    String userPhone = "";
    String userEmail = "";
    String userAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnCheckPw = (Button) findViewById(R.id.btnCheckPw);
        btnDone = (Button) findViewById(R.id.btnDone);
        btnLogOut = (Button) findViewById(R.id.btnLogOut);
        btnWithdrawal = (Button) findViewById(R.id.btnWithdrawal);
        tvCheck = (TextView) findViewById(R.id.tvCheck);
        tvCheckPw = (TextView) findViewById(R.id.tvCheckPw);
        tvCheckPw2 = (TextView) findViewById(R.id.tvCheckPw2);
        etName = (EditText) findViewById(R.id.etName);
        etCurrentPw = (EditText) findViewById(R.id.etCurrentPw);
        etNewPw = (EditText) findViewById(R.id.etNewPw);
        etNewPwCheck = (EditText) findViewById(R.id.etNewPwCheck);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etAddress = (EditText) findViewById(R.id.etAddress);
        pwLayout = (LinearLayout) findViewById(R.id.pwLayout);


        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        userId = sharedPref.getString("userId", "");

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getWritableDatabase();

        pwLayout.setVisibility(View.GONE);
        tvCheck.setVisibility(View.GONE);
        tvCheckPw.setVisibility(View.GONE);
        tvCheckPw2.setVisibility(View.GONE);

        String sql = "select * from member where id = ?";
        Log.d("SQL", sql);
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{userId});

        while (cursor.moveToNext()) {
            userName = cursor.getString(2);
            userPw = cursor.getString(1);
            userPhone = cursor.getString(5);
            userEmail = cursor.getString(6);
            userAddress = cursor.getString(7);
        }
        cursor.close();

        etName.setHint(userName);
        etPhone.setHint(userPhone);
        etEmail.setHint(userEmail);
        etAddress.setHint(userAddress);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnCheckPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etCurrentPw.getText().toString().equals(userPw)) {
                    pwLayout.setVisibility(View.VISIBLE);
                    tvCheck.setVisibility(View.GONE);
                } else {
                    tvCheck.setVisibility(View.VISIBLE);
                    pwLayout.setVisibility(View.GONE);
                }
            }
        });

        etNewPw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty() || etNewPwCheck.getText().toString().trim().isEmpty()) {
                    tvCheckPw.setVisibility(View.GONE);
                    tvCheckPw2.setVisibility(View.GONE);
                } else if (!s.toString().equals(etNewPwCheck.getText().toString())) {
                    tvCheckPw.setVisibility(View.VISIBLE);
                    tvCheckPw2.setVisibility(View.INVISIBLE);
                } else {
                    tvCheckPw.setVisibility(View.INVISIBLE);
                    tvCheckPw2.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etNewPwCheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty() || etNewPw.getText().toString().trim().isEmpty()) {
                    tvCheckPw.setVisibility(View.GONE);
                    tvCheckPw2.setVisibility(View.GONE);
                } else if (!s.toString().equals(etNewPw.getText().toString())) {
                    tvCheckPw.setVisibility(View.VISIBLE);
                    tvCheckPw2.setVisibility(View.INVISIBLE);
                } else {
                    tvCheckPw.setVisibility(View.INVISIBLE);
                    tvCheckPw2.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> updateList = new ArrayList<>();

                if (!etName.getText().toString().trim().isEmpty()) {
                    updateList.add("이름: " + etName.getText().toString());
                }
                if (pwLayout.getVisibility() == View.VISIBLE && !etNewPw.getText().toString().trim().isEmpty() && etNewPw.getText().toString().equals(etNewPwCheck.getText().toString())) {
                    updateList.add("비밀번호: " + etNewPw.getText().toString());
                } else if (!etNewPw.getText().toString().equals(etNewPwCheck.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
                if (!etPhone.getText().toString().trim().isEmpty()) {
                    updateList.add("전화번호: " + etPhone.getText().toString());
                }
                if (!etEmail.getText().toString().trim().isEmpty()) {
                    updateList.add("이메일: " + etEmail.getText().toString());
                }
                if (!etAddress.getText().toString().trim().isEmpty()) {
                    updateList.add("주소: " + etAddress.getText().toString());
                }

                if (!updateList.isEmpty() && etNewPw.getText().toString().equals(etNewPwCheck.getText().toString())) {
                    String updateItem = "";
                    for (String item : updateList) {
                        updateItem += item + "\n";
                    }

                    new AlertDialog.Builder(v.getContext())
                            .setTitle("변경 사항이 맞습니까?")
                            .setMessage(updateItem)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!etName.getText().toString().trim().isEmpty()) {
                                        updateProfile("name", etName.getText().toString());
                                    }
                                    if (pwLayout.getVisibility() == View.VISIBLE && !etNewPw.getText().toString().trim().isEmpty() && etNewPw.getText().toString().equals(etNewPwCheck.getText().toString())) {
                                        updateProfile("pw", etNewPw.getText().toString());
                                    }
                                    if (!etPhone.getText().toString().trim().isEmpty()) {
                                        updateProfile("phone", etPhone.getText().toString());
                                    }
                                    if (!etEmail.getText().toString().trim().isEmpty()) {
                                        updateProfile("email", etEmail.getText().toString());
                                    }
                                    if (!etAddress.getText().toString().trim().isEmpty()) {
                                        updateProfile("address", etAddress.getText().toString());
                                    }

                                    Toast.makeText(getApplicationContext(), "변경 사항이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                                    Intent returnIntent = new Intent();
                                    setResult(AppCompatActivity.RESULT_OK, returnIntent);
                                    finish();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "변경 사항이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("userId", "");
                editor.putBoolean("autoLogin", false);
                editor.apply();
                Toast.makeText(getApplicationContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        btnWithdrawal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle("회원탈퇴").setMessage("회원탈퇴를 하시겠습니까?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteMember();
                                SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("userId", "");
                                editor.putBoolean("autoLogin", false);
                                editor.apply();
                                Toast.makeText(getApplicationContext(), "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void updateProfile(String item, String newInfo) {
        sqlDB.execSQL("UPDATE member SET " + item + " = ? WHERE id = ?", new Object[]{newInfo, userId});

        // 서버에 POST 요청 보내기
        new Thread(() -> {
            try {
                URL url = new URL("https://kse.calab.myds.me/update_member.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String columnName = item;
                String postData = "columnName=" + URLEncoder.encode(columnName, "UTF-8") +
                        "&userId=" + URLEncoder.encode(userId, "UTF-8") +
                        "&item=" + URLEncoder.encode(newInfo, "UTF-8");

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

    private void deleteMember() {
        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        String userId = sharedPref.getString("userId", "");
        Log.d("userId", userId);

        sqlDB.execSQL("DELETE FROM member WHERE id = ?", new Object[]{userId});

        // 서버에 POST 요청 보내기
        new Thread(() -> {
            try {
                URL url = new URL("https://kse.calab.myds.me/delete_member.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "userId=" + URLEncoder.encode(userId, "UTF-8");

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                // 서버 응답 처리
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