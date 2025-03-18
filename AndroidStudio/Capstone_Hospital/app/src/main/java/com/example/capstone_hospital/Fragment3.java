package com.example.capstone_hospital;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import net.sourceforge.jtds.jdbc.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class Fragment3 extends Fragment implements OnMapReadyCallback {

    Button btnSearch, btnLocation, btnAll, btnMedicalSubject;
    TextView delete;
    EditText etSearch;
    ListView listView;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    ArrayAdapter<String> listAdapter;
    ArrayList<String> filteredData = new ArrayList<>();
    ArrayList<Integer> numberList = new ArrayList<>();
    ArrayList<String> nameList = new ArrayList<>();
    ArrayList<String> addressList = new ArrayList<>();
    ArrayList<Marker> markersList = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private NaverMap mNaverMap;
    private FusedLocationSource locationSource;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_3, container, false);

        btnSearch = (Button) view.findViewById(R.id.btnSearch);
        btnLocation = (Button) view.findViewById(R.id.btnLocation);
        btnAll = (Button) view.findViewById(R.id.btnAll);
        btnMedicalSubject = (Button) view.findViewById(R.id.btnMedicalSubject);
        delete = (TextView) view.findViewById(R.id.delete);
        etSearch = (EditText) view.findViewById(R.id.etSearch);
        listView = view.findViewById(R.id.listView);

        registerForContextMenu(btnMedicalSubject);

        myHelper = new MainActivity.MyDBHelper(getActivity(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        String sql = "select * from hospital";
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

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = editable.toString();
                filterData(searchText);
            }
        });

        listView.setVisibility(View.INVISIBLE);
        etSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etSearch.getText().toString().isEmpty()) {
                    listView.setVisibility(View.INVISIBLE);
                } else {
                    listView.setVisibility(View.VISIBLE);
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText(null);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.setVisibility(View.INVISIBLE);

                for (Marker marker : markersList) {
                    marker.setMap(null); // 마커를 지도에서 제거
                }
                markersList.clear();

                // 필터링된 병원 이름에 따라 위치 찾기
                ArrayList<Location> filteredLocations = new ArrayList<>();
                for (String hospitalName : filteredData) {
                    int index = nameList.indexOf(hospitalName);
                    Log.d("index", String.valueOf(index));
                    if (index != -1) {
                        String address = addressList.get(index);
                        Location location = addToPoint(getActivity(), address);
                        if (location != null) {
                            filteredLocations.add(location);
                        }
                    }
                }

                // 필터링된 위치에 마커 추가
                List<LatLng> locations = new ArrayList<>();
                for (Location location : filteredLocations) {
                    locations.add(new LatLng(location.getLatitude(), location.getLongitude()));
                }

                addMarkers(mNaverMap, locations);
            }
        });

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Marker marker : markersList) {
                    marker.setMap(null);
                }
                markersList.clear();
                filteredData.clear();

                filteredData.addAll(nameList);

                List<LatLng> allLocations = new ArrayList<>();

                for (String address : addressList) {
                    Location location = addToPoint(getActivity(), address);
                    if (location != null) {
                        allLocations.add(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                }

                // 전체 병원 마커 추가
                addMarkers(mNaverMap, allLocations);
            }
        });

        listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, filteredData);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (Marker marker : markersList) {
                    marker.setMap(null);
                }
                markersList.clear();

                String selectedHospital = (String) parent.getItemAtPosition(position);
                etSearch.setText(selectedHospital);
                listView.setVisibility(View.INVISIBLE);

                int index = nameList.indexOf(selectedHospital);
                if (index != -1) {
                    String address = addressList.get(index);

                    Location location = addToPoint(getActivity(), address);
                    if (location != null) {
                        LatLng hospitalLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        List<LatLng> locations = new ArrayList<>();
                        locations.add(hospitalLocation);

                        CameraPosition cameraPosition = new CameraPosition(hospitalLocation, 15);
                        mNaverMap.setCameraPosition(cameraPosition);

                        // 병원 위치에 마커 추가
                        addMarkers(mNaverMap, locations);
                    }
                }
            }
        });

        btnMedicalSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("진료과목 선택");

                String[] medicalSubjects = {"소아청소년과", "이비인후과", "정형외과", "산부인과", "치과", "안과", "내과", "외과", "피부과",
                        "정신건강의학과", "가정의학과", "성형외과", "한방내과", "비뇨의학과", "신경외과", "영상의학과", "재활의학과"};

                builder.setItems(medicalSubjects, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedSubject = medicalSubjects[which];
                        Toast.makeText(getActivity(), "선택된 진료과목: " + selectedSubject, Toast.LENGTH_SHORT).show();

                        filteredData.clear();

                        String sql = "select * from hospital where medicalSubject like '%" + selectedSubject + "%'";
                        Log.d("SQL", sql);
                        Cursor cursor = sqlDB.rawQuery(sql, null);
                        while (cursor.moveToNext()) {
                            String hospitalName = cursor.getString(1);

                            filteredData.add(hospitalName);
                            Log.d("address", hospitalName);
                        }
                        cursor.close();

                        for (Marker marker : markersList) {
                            marker.setMap(null); // 마커를 지도에서 제거
                        }
                        markersList.clear();

                        // 필터링된 병원 이름에 따라 위치 찾기
                        ArrayList<Location> filteredLocations = new ArrayList<>();
                        for (String hospitalName : filteredData) {
                            int index = nameList.indexOf(hospitalName);
                            Log.d("index", String.valueOf(index));
                            if (index != -1) {
                                String address = addressList.get(index);
                                Location location = addToPoint(getActivity(), address);
                                if (location != null) {
                                    filteredLocations.add(location);
                                }
                            }
                        }

                        // 필터링된 위치에 마커 추가
                        List<LatLng> locations = new ArrayList<>();
                        for (Location location : filteredLocations) {
                            locations.add(new LatLng(location.getLatitude(), location.getLongitude()));
                        }

                        addMarkers(mNaverMap, locations);
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.mapView);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.mapView, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        return view;
    }

    private void filterData(String searchText) {
        filteredData.clear();

        if (searchText.isEmpty()) {
            filteredData.addAll(nameList);
            listView.setVisibility(View.INVISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            for (String item : nameList) {
                if (item.toLowerCase().contains(searchText.toLowerCase())) {
                    filteredData.add(item);
                }
            }
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d("Fragment3", "onMapReady");

        mNaverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        Context context = getActivity();

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

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationSource.getLastLocation() != null) {
                    LatLng currentLocation = new LatLng(locationSource.getLastLocation().getLatitude(), locationSource.getLastLocation().getLongitude());
                    CameraPosition cameraPosition = new CameraPosition(currentLocation, 15);
                    naverMap.setCameraPosition(cameraPosition);
                }
            }
        });

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

        addMarkers(naverMap, locations);
    }

    private void addMarkers(NaverMap naverMap, List<LatLng> locations) {
        if (filteredData.isEmpty()) {
            filteredData.addAll(nameList);
        }

        for (int i = 0; i < locations.size(); i++) {
            Marker marker = new Marker();
            marker.setPosition(locations.get(i));
            marker.setCaptionText(filteredData.get(i));
            marker.setWidth(100);
            marker.setHeight(100);
            marker.setIcon(OverlayImage.fromResource(R.drawable.hospital_marker));
            marker.setMap(naverMap);
            markersList.add(marker);

            String hospitalName = filteredData.get(i);
            int index = nameList.indexOf(hospitalName);
            int number = numberList.get(index);

            marker.setOnClickListener(new Overlay.OnClickListener() {
                @Override
                public boolean onClick(@NonNull Overlay overlay) {
                    Intent intent = new Intent(getActivity(), MapInfoActivity.class);
                    intent.putExtra("number", number);
                    startActivity(intent);
                    return false;
                }
            });
        }
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