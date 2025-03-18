package com.example.capstone_hospital;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    Button btnBack, btnSignIn, btnCheck;
    EditText etID, etPW, etPWCheck, etName, etGender, etBirth, etPhone, etEmail, etAddress;
    TextView tvCheckId, tvCheckId2, tvPwType, tvCheckPw, tvCheckPw2;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnCheck = (Button) findViewById(R.id.btnCheck);
        etName = (EditText) findViewById(R.id.editText1);
        etID = (EditText) findViewById(R.id.etCurrentPw);
        etPW = (EditText) findViewById(R.id.editText3);
        etPWCheck = (EditText) findViewById(R.id.etNewPw);
        etBirth = (EditText) findViewById(R.id.editText5);
        etGender = (EditText) findViewById(R.id.editText6);
        etPhone = (EditText) findViewById(R.id.etNewPwCheck);
        etAddress = (EditText) findViewById(R.id.editText10);
        etEmail = (EditText) findViewById(R.id.editText11);
        tvCheckId = (TextView) findViewById(R.id.tvCheckId);
        tvCheckId2 = (TextView) findViewById(R.id.tvCheckId2);
        tvPwType = (TextView) findViewById(R.id.tvPwType);
        tvCheckPw = (TextView) findViewById(R.id.tvCheckPw);
        tvCheckPw2 = (TextView) findViewById(R.id.tvCheckPw2);

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvCheckId.setVisibility(View.GONE);
        tvCheckId2.setVisibility(View.GONE);
        etID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    tvCheckId.setVisibility(View.VISIBLE);
                    tvCheckId2.setVisibility(View.GONE);
                } else {
                    tvCheckId.setVisibility(View.GONE);
                    tvCheckId2.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tvPwType.setVisibility(View.GONE);
        tvCheckPw.setVisibility(View.GONE);
        tvCheckPw2.setVisibility(View.GONE);
        etPW.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                boolean isValidPassword = password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,}$");

                if (isValidPassword) {
                    tvPwType.setVisibility(View.GONE);
                } else {
                    tvPwType.setVisibility(View.VISIBLE);
                }

                if (s.toString().trim().isEmpty() || etPWCheck.getText().toString().trim().isEmpty()) {
                    tvCheckPw.setVisibility(View.GONE);
                    tvCheckPw2.setVisibility(View.GONE);
                } else if (!s.toString().equals(etPWCheck.getText().toString()) && tvPwType.getVisibility() == View.GONE) {
                    tvCheckPw.setVisibility(View.VISIBLE);
                    tvCheckPw2.setVisibility(View.INVISIBLE);
                } else {
                    if(tvPwType.getVisibility() == View.GONE) {
                        tvCheckPw.setVisibility(View.INVISIBLE);
                        tvCheckPw2.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etPWCheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty() || etPW.getText().toString().trim().isEmpty()) {
                    tvCheckPw.setVisibility(View.GONE);
                    tvCheckPw2.setVisibility(View.GONE);
                } else if (!s.toString().equals(etPW.getText().toString()) && tvPwType.getVisibility() == View.GONE) {
                    tvCheckPw.setVisibility(View.VISIBLE);
                    tvCheckPw2.setVisibility(View.INVISIBLE);
                } else {
                    if(tvPwType.getVisibility() == View.GONE) {
                        tvCheckPw.setVisibility(View.INVISIBLE);
                        tvCheckPw2.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> idList = new ArrayList<>();

                String sql = "select id from member";
                Log.d("SQL", sql);
                Cursor cursor = sqlDB.rawQuery(sql, null);
                while (cursor.moveToNext()) {
                    String id = cursor.getString(0);
                    idList.add(id);
                }
                cursor.close();

                if(etID.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else if(!idList.contains(etID.getText().toString())) {
                    tvCheckId.setVisibility(View.GONE);
                    tvCheckId2.setVisibility(View.VISIBLE);
                }
                else {
                    etID.setText("");
                    Toast.makeText(getApplicationContext(), "중복되는 아이디가 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String genderNum = "";
                if(!etGender.getText().toString().trim().isEmpty()) {
                    int gender_id = Integer.parseInt(etGender.getText().toString());

                    if(gender_id % 2 == 1) {
                        genderNum = "남성";
                    } else {
                        genderNum = "여성";
                    }
                }

                final String id = etID.getText().toString();
                final String pw = etPW.getText().toString();
                final String pwCheck = etPWCheck.getText().toString();
                final String name = etName.getText().toString();
                final String birth = etBirth.getText().toString();
                final String phone = etPhone.getText().toString();
                final String email = etEmail.getText().toString();
                final String address = etAddress.getText().toString();
                final String gender = genderNum;

                if (id.trim().isEmpty() || pw.trim().isEmpty() || pwCheck.trim().isEmpty() || name.trim().isEmpty() || birth.trim().isEmpty()
                        || phone.trim().isEmpty() || email.trim().isEmpty() || address.trim().isEmpty() || gender.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "빈칸을 모두 채워주세요.", Toast.LENGTH_SHORT).show();
                } else if (tvCheckId.getVisibility() == View.VISIBLE) {
                    Toast.makeText(getApplicationContext(), "아이디 중복 여부를 확인해주세요.", Toast.LENGTH_SHORT).show();
                } else if (tvPwType.getVisibility() == View.VISIBLE) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 알맞은 형식으로 작성해주세요.", Toast.LENGTH_SHORT).show();
                } else if (!pw.equals(pwCheck)) {
                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                } else if (birth.length() < 6) {
                    Toast.makeText(getApplicationContext(), "주민등록번호를 알맞은 형식으로 작성해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    // 모든 조건이 만족되었을 때 회원 등록 요청
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                boolean success = jsonObject.getBoolean("success");
                                if (success) {
                                    Toast.makeText(getApplicationContext(), "회원 등록에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                    insertToSQLite(id, pw, name, gender, birth, phone, email, address);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "회원 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    RegisterRequest registerRequest = new RegisterRequest(id, pw, name, gender, birth, phone, email, address, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                    queue.add(registerRequest);
                }
            }
        });
    }

    private void insertToSQLite(String id, String pw, String name, String gender, String birth, String phone, String email, String address) {
        sqlDB = myHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("pw", pw);
        values.put("name", name);
        values.put("gender", gender);
        values.put("birth", birth);
        values.put("phone", phone);
        values.put("email", email);
        values.put("address", address);
        sqlDB.insert("member", null, values);
        sqlDB.close();
    }
}

class RegisterRequest extends StringRequest {

    final static private String URL = "https://kse.calab.myds.me/register.php";
    private Map<String, String> map;

    public RegisterRequest(String id, String pw, String name, String gender, String birth, String phone, String email, String address, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("id",id);
        map.put("pw", pw);
        map.put("name", name);
        map.put("gender", gender);
        map.put("birth", birth);
        map.put("phone", phone);
        map.put("email", email);
        map.put("address", address);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}