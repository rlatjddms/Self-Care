package com.example.capstone_hospital;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SkinActivity extends AppCompatActivity {

    Button btnBack, btnPic, btnNext;
    ImageView imageView;
    Interpreter tfLite;
    HorizontalBarChart chart;
    ArrayList<Integer> topIndicesList = new ArrayList<>();
    private static final int REQUEST_CODE = 0;
    private static final int IMAGE_SIZE = 224; // 모델에서 사용하는 이미지 크기

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnPic = (Button) findViewById(R.id.btnPic);
        imageView = (ImageView) findViewById(R.id.imageView3);
        chart = (HorizontalBarChart) findViewById(R.id.chart);

        chart.getDescription().setEnabled(false); // 설명 제거
        chart.setFitBars(true); // 막대가 차트 안에 맞게 조정

        try {
            tfLite = new Interpreter(loadModelFile(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 선택한 이미지로 진단 시작
                if (imageView.getDrawable() != null) {
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    classifyImage(bitmap);
                } else {
                    Toast.makeText(getApplicationContext(), "먼저 이미지를 선택하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                // 선택된 인덱스를 가져옴
                int index = (int) e.getX(); // 현재 막대 인덱스

                // topIndicesList에서 인덱스 가져오기
                if (index >= 0 && index < topIndicesList.size()) {
                    int originalIndex = topIndicesList.get(index); // 상위 인덱스를 원본 인덱스로 변환
                    String label = getLabel(originalIndex); // 원본 인덱스를 사용하여 라벨 가져오기

                    Intent intent = new Intent(getApplicationContext(), SkinInfoActivity.class);
                    intent.putExtra("label", label);
                    startActivity(intent);
                }
            }
            @Override
            public void onNothingSelected() {
                // 아무것도 선택되지 않았을 때
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                try {
                    InputStream in = getContentResolver().openInputStream(data.getData());

                    Bitmap img = BitmapFactory.decodeStream(in);
                    in.close();

                    imageView.setImageBitmap(img);
                } catch(Exception e) {

                }
            }
            else if(resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 모델 파일을 로드하는 함수
    private MappedByteBuffer loadModelFile() throws Exception {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // 이미지를 분류하는 함수
    private void classifyImage(Bitmap bitmap) {
        // 상위 인덱스 리스트 초기화
        topIndicesList.clear();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
        // 이미지를 Tensor로 변환
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);
        // 출력 배열 (모델에서 예측된 결과를 저장)
        float[][] output = new float[1][83]; // NUM_CLASSES는 모델의 클래스 수

        // 모델에 입력하여 예측 수행
        tfLite.run(inputBuffer, output);
        // 예측 결과 처리 및 상위 항목 추출
        displayResults(output[0]);
    }

    // 이미지를 ByteBuffer로 변환하는 함수
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3); // 3채널 RGB
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < IMAGE_SIZE; ++i) {
            for (int j = 0; j < IMAGE_SIZE; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.f); // R
                byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.f);  // G
                byteBuffer.putFloat((val & 0xFF) / 255.f);         // B
            }
        }

        return byteBuffer;
    }

    // 예측 결과를 처리하여 상위 N개의 결과를 출력하는 함수
    private void displayResults(float[] probabilities) {
        int[] topIndices = getTopIndices(probabilities, 5);  // 상위 5개 추출
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        float maxConfidence = -1;
        int maxIndex = -1;

        for (int i = 0; i < topIndices.length; i++) {
            int index = topIndices[i];
            String label = getLabel(index);  // 예: "피부염", "건선"
            float confidence = probabilities[index] * 100;  // 확률을 퍼센트로 변환
            entries.add(new BarEntry(i, confidence));  // BarEntry(x, y)
            labels.add(label);  // 질병 라벨 추가
            topIndicesList.add(index); // 상위 인덱스 저장

            if (confidence > maxConfidence) {
                maxConfidence = confidence;
                maxIndex = i;
            }
        }

        ArrayList<BarEntry> maxEntries = new ArrayList<>();
        ArrayList<BarEntry> otherEntries = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            if (i == maxIndex) {
                maxEntries.add(entries.get(i));  // 최대값에 해당하는 항목
            } else {
                otherEntries.add(entries.get(i));  // 기타 항목
            }
        }

        // 최대값에 해당하는 BarDataSet 설정
        BarDataSet maxDataSet = new BarDataSet(maxEntries, "최대값");
        maxDataSet.setColor(getResources().getColor(R.color.colorPrimary));
        maxDataSet.setValueTextSize(10f);

        // 기타 항목에 해당하는 BarDataSet 설정
        BarDataSet otherDataSet = new BarDataSet(otherEntries, "기타값");
        otherDataSet.setColor(getResources().getColor(R.color.customColor));
        otherDataSet.setValueTextSize(10f);

        // BarData에 두 개의 DataSet 추가
        BarData data = new BarData(maxDataSet, otherDataSet);
        chart.setData(data);

        // X축 설정 (라벨)
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // 간격
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // 라벨 설정
        xAxis.setDrawGridLines(false); // 격자선 제거
        xAxis.setTextSize(11f); // 텍스트 크기 조정

        // Y축 설정
        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(false); // 오른쪽 Y축 제거

        // 차트의 왼쪽 마진을 추가
        chart.setExtraLeftOffset(40f); // 적절한 값을 지정
        // 차트 업데이트
        chart.invalidate();  // 차트 다시 그리기
    }

    // 상위 N개의 인덱스를 얻는 함수
    private int[] getTopIndices(float[] probabilities, int numResults) {
        List<Pair<Integer, Float>> indexedProbabilities = new ArrayList<>();
        for (int i = 0; i < probabilities.length; i++) {
            indexedProbabilities.add(new Pair<>(i, probabilities[i]));
        }

        // 확률을 내림차순으로 정렬
        Collections.sort(indexedProbabilities, new Comparator<Pair<Integer, Float>>() {
            @Override
            public int compare(Pair<Integer, Float> o1, Pair<Integer, Float> o2) {
                return Float.compare(o2.second, o1.second); // 높은 확률을 먼저 배치
            }
        });

        int[] topIndices = new int[numResults];
        for (int i = 0; i < numResults; i++) {
            topIndices[i] = indexedProbabilities.get(i).first; // 정렬된 상위 인덱스 저장
        }

        return topIndices; // 상위 인덱스 배열 반환
    }


    // 인덱스에 따른 라벨을 얻는 함수 (모델 클래스에 따라 변경)
    private String getLabel(int index) {
        String[] labels = {
                "가와사키병", "간찰성 홍반", "건선", "곰팡이 감염", "광선 각화증",
                "기미", "기저귀 피부염", "기저세포 암종", "내향성 손발톱", "농가진",
                "단독", "단순 포진", "당뇨병성 족부 질환", "대상포진", "돌발성 발진",
                "동상", "동전모양 습진", "두드러기", "두부 백선", "딸기코",
                "땀띠", "라임병", "만성 단순 태선", "만성 두드러기", "모공성 각화증",
                "모낭염", "모반", "무좀", "백반증", "베체트 병",
                "봉와직염", "비립종", "사마귀", "색소 세포성 모반", "선천성 색소결핍증",
                "섬유종", "소양증", "수부 습진", "스티븐 존슨 증후군", "습진",
                "아토피성 피부염", "악성 흑색종", "안검황색종", "안면 홍조증", "약물 발진",
                "어루러기", "어린선", "여드름", "오타 모반", "옴",
                "옻 중독", "욕창", "접촉 피부염", "조갑 이영양증", "조갑백선",
                "조갑주위염", "주근깨", "쥐젖", "지루성 피부염", "지방종",
                "천포창", "켈로이드", "콜린성 두드러기", "탄저병", "탈락 피부염",
                "탈모증", "태열", "티눈 및 굳은살", "편평 태선", "표피낭종",
                "피부 농양", "피부건조증", "피부근육염", "피부암", "피부염",
                "피부의 양성 종양", "한관종", "한랭 두드러기", "한선염", "한센병",
                "한포진", "화상", "화염상 모반"
            };

        if (index >= 0 && index < labels.length) {
            return labels[index];
        } else {
            return "알 수 없는 질병"; // 예외 처리
        }
    }

}