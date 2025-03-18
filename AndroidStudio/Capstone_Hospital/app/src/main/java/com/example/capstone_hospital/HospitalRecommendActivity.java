package com.example.capstone_hospital;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HospitalRecommendActivity extends AppCompatActivity implements OnMapReadyCallback {

    Button btnBack, btnHome;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    ArrayList<String> subjectList = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private NaverMap mNaverMap;
    private FusedLocationSource locationSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_recommend);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnHome = (Button) findViewById(R.id.btnHome);

        String medicalSubject = getIntent().getStringExtra("medicalSubject");

        String[] symptomSplit = medicalSubject.split(",");
        subjectList = new ArrayList<String>(Arrays.asList(symptomSplit));

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("isLoggedIn", true);
                startActivity(intent);
                finish();
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.mapView);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.mapView, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d("Fragment3", "onMapReady");

        ArrayList<Integer> numberList = new ArrayList<>();
        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<String> addressList = new ArrayList<>();

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        for(int i=0; i<subjectList.size(); i++) {
            String sql = "select * from hospital where medicalSubject like '%" + subjectList.get(i) + "%'";
            Log.d("SQL", sql);
            Cursor cursor = sqlDB.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                int no = cursor.getInt(0);
                String hospitalName = cursor.getString(1);
                String address = cursor.getString(3);

                numberList.add(no);
                nameList.add(hospitalName);
                addressList.add(address);
                Log.d("address", address);
            }
            cursor.close();
        }
        sqlDB.close();

        // 데이터가 없을 경우 기본 병원 추가
        if (numberList.isEmpty() && nameList.isEmpty() && addressList.isEmpty()) {
            numberList.add(1);
            nameList.add("동군산병원");
            addressList.add("군산시 조촌로 149 (조촌동)");

            numberList.add(2);
            nameList.add("군산의료원");
            addressList.add("군산시 의료원로 27 (지곡동,(지곡동, 군산의료원))");

            numberList.add(9);
            nameList.add("차병원");
            addressList.add("군산시 수송로 8 (나운동)");
        }

        mNaverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        Context context = getApplicationContext();

        Location lastLocation = locationSource.getLastLocation();
        if (lastLocation != null) {
            LatLng initialLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            CameraPosition initialCameraPosition = new CameraPosition(initialLocation, 15);
            naverMap.setCameraPosition(initialCameraPosition);
        } else {
            LatLng defaultLocation = new LatLng(35.945209, 126.682838);
            CameraPosition defaultCameraPosition = new CameraPosition(defaultLocation, 15);
            naverMap.setCameraPosition(defaultCameraPosition);
        }

        ArrayList<Location> locationList = new ArrayList<>();

        for(int i=0; i<addressList.size(); i++) {
            Location location = addToPoint(context, addressList.get(i));
            locationList.add(i, location);
        }

        List<LatLng> locations = new ArrayList<>();
        if (locationList != null) {
            for(int i=0; i<addressList.size(); i++) {
                locations.add(new LatLng(locationList.get(i).getLatitude(), locationList.get(i).getLongitude()));
            }
        }

        for(int i = 0; i < locations.size(); i++) {
            Marker marker = new Marker();
            marker.setPosition(locations.get(i));
            marker.setCaptionText(nameList.get(i));
            marker.setWidth(100);
            marker.setHeight(100);
            marker.setIcon(OverlayImage.fromResource(R.drawable.hospital_marker));
            marker.setMap(naverMap);

            int number = numberList.get(i);

            marker.setOnClickListener(new Overlay.OnClickListener() {
                @Override
                public boolean onClick(@NonNull Overlay overlay) {
                    Intent intent = new Intent(getApplicationContext(), MapInfoActivity.class);
                    intent.putExtra("number", number);
                    startActivity(intent);
                    return false;
                }
            });
        }

        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    // 주소를 위도와 경도로 변환
    public static Location addToPoint(Context context, String addressName) {
        Location location = new Location("");
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocationName(addressName, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null) {
            for (int i = 0; i < addresses.size(); i++) {
                Address latLng = addresses.get(i);
                location.setLatitude(latLng.getLatitude());
                location.setLongitude(latLng.getLongitude());
            }
        }
        return location;
    }

    // 위치 권한 설정
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
                if (!locationSource.isActivated()) {
                    mNaverMap.setLocationTrackingMode(LocationTrackingMode.None);
                }
            }
        }
    }
}