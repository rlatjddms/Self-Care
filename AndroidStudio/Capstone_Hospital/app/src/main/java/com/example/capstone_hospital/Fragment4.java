package com.example.capstone_hospital;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Fragment4 extends Fragment {

    LinearLayout loginLayout;
    TextView memberName, tvEditProfile, tvMedicalHistory, tvBookmark;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    String name = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_4, container, false);

        loginLayout = (LinearLayout) view.findViewById(R.id.loginLayout);
        tvEditProfile = (TextView) view.findViewById(R.id.tvEditProfile);
        memberName = (TextView) view.findViewById(R.id.name);
        tvMedicalHistory = (TextView) view.findViewById(R.id.tvMedicalHistory);
        tvBookmark = (TextView) view.findViewById(R.id.tvBookmark);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        String userId = sharedPref.getString("userId", "");
        Log.d("userId", userId);

        myHelper = new MainActivity.MyDBHelper(getContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        String sql = "select * from member where id='" + userId + "'";
        Log.d("SQL", sql);
        Cursor cursor = sqlDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            name = cursor.getString(2);
        }
        cursor.close();
        sqlDB.close();

        memberName.setText(name);

        tvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        tvMedicalHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MedicalHistoryActivity.class);
                startActivity(intent);
            }
        });

        tvBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BookmarkActivity.class);
                startActivity(intent);
            }
        });

        loadUserData();
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            loadUserData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        String userId = sharedPref.getString("userId", "");

        myHelper = new MainActivity.MyDBHelper(getContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        String sql = "select * from member where id=?";
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{userId});
        if (cursor.moveToNext()) {
            name = cursor.getString(2);
            memberName.setText(name);
        }
        cursor.close();
        sqlDB.close();
    }
}