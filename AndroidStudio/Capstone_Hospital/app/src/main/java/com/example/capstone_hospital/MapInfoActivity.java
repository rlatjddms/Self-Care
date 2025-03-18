package com.example.capstone_hospital;

import static java.lang.Math.round;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MapInfoActivity extends AppCompatActivity {

    Button btnClose, btnUrl;
    TextView tvHospital, tvSubject, tvAddress, tvPhone;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    String hospitalName = "";
    String medicalSubject = "";
    String address = "";
    String phone = "";
    String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_info);

        btnClose = (Button) findViewById(R.id.btnClose);
        btnUrl = (Button) findViewById(R.id.btnUrl);
        tvHospital = (TextView) findViewById(R.id.tvHospital);
        tvSubject = (TextView) findViewById(R.id.tvSubject);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvPhone = (TextView) findViewById(R.id.tvPhone);

        int number = getIntent().getIntExtra("number", 0);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        String sql = "select * from hospital where no=" + number;
        Log.d("SQL", sql);
        Cursor cursor = sqlDB.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            hospitalName = cursor.getString(1);
            medicalSubject = cursor.getString(2);
            address = cursor.getString(3);
            phone = cursor.getString(4);
            url = cursor.getString(5);
        }
        cursor.close();
        sqlDB.close();

        tvHospital.setText(hospitalName);
        tvSubject.setText("진료과목: " + medicalSubject);
        tvAddress.setText(address);
        tvPhone.setText(phone);

        btnUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HospitalInfoActivity.class);
                intent.putExtra("hospitalName", hospitalName);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        initLayout();
    }

    private void initLayout() {
        int width = 0;
        int height = 0;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
            width = windowMetrics.getBounds().width();
            height = windowMetrics.getBounds().height();
        } else {
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getRealMetrics(displayMetrics);
            width = displayMetrics.widthPixels;
            height = displayMetrics.heightPixels;
        }
        getWindow().setLayout((int)round(width), (int)round(height * 0.84));
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}