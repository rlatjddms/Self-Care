package com.example.capstone_hospital;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.chip.Chip;

public class SymptomActivity extends AppCompatActivity {

    Button btnBack;
    TextView[] symptoms = new TextView[20];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom);

        btnBack = (Button) findViewById(R.id.btnBack);

        symptoms[0] = (TextView) findViewById(R.id.chip1);
        symptoms[1] = (TextView) findViewById(R.id.chip2);
        symptoms[2] = (TextView) findViewById(R.id.chip3);
        symptoms[3] = (TextView) findViewById(R.id.chip4);
        symptoms[4] = (TextView) findViewById(R.id.chip5);
        symptoms[5] = (TextView) findViewById(R.id.chip6);
        symptoms[6] = (TextView) findViewById(R.id.chip7);
        symptoms[7] = (TextView) findViewById(R.id.chip8);
        symptoms[8] = (TextView) findViewById(R.id.chip9);
        symptoms[9] = (TextView) findViewById(R.id.chip10);
        symptoms[10] = (TextView) findViewById(R.id.chip11);
        symptoms[11] = (TextView) findViewById(R.id.chip12);
        symptoms[12] = (TextView) findViewById(R.id.chip13);
        symptoms[13] = (TextView) findViewById(R.id.chip14);
        symptoms[14] = (TextView) findViewById(R.id.chip15);
        symptoms[15] = (TextView) findViewById(R.id.chip16);
        symptoms[16] = (TextView) findViewById(R.id.chip17);
        symptoms[17] = (TextView) findViewById(R.id.chip18);
        symptoms[18] = (TextView) findViewById(R.id.chip19);
        symptoms[19] = (TextView) findViewById(R.id.chip20);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        symptoms[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 1);
                intent.putExtra("symptomName", symptoms[0].getText());
                startActivity(intent);
            }
        });

        symptoms[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 9);
                intent.putExtra("symptomName", symptoms[1].getText());
                startActivity(intent);
            }
        });

        symptoms[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 17);
                intent.putExtra("symptomName", symptoms[2].getText());
                startActivity(intent);
            }
        });

        symptoms[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 25);
                intent.putExtra("symptomName", symptoms[3].getText());
                startActivity(intent);
            }
        });

        symptoms[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 31);
                intent.putExtra("symptomName", symptoms[4].getText());
                startActivity(intent);
            }
        });

        symptoms[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 35);
                intent.putExtra("symptomName", symptoms[5].getText());
                startActivity(intent);
            }
        });

        symptoms[6].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 44);
                intent.putExtra("symptomName", symptoms[6].getText());
                startActivity(intent);
            }
        });

        symptoms[7].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 53);
                intent.putExtra("symptomName", symptoms[7].getText());
                startActivity(intent);
            }
        });

        symptoms[8].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 67);
                intent.putExtra("symptomName", symptoms[8].getText());
                startActivity(intent);
            }
        });

        symptoms[9].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 74);
                intent.putExtra("symptomName", symptoms[9].getText());
                startActivity(intent);
            }
        });

        symptoms[10].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 83);
                intent.putExtra("symptomName", symptoms[10].getText());
                startActivity(intent);
            }
        });

        symptoms[11].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 91);
                intent.putExtra("symptomName", symptoms[11].getText());
                startActivity(intent);
            }
        });

        symptoms[12].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 108);
                intent.putExtra("symptomName", symptoms[12].getText());
                startActivity(intent);
            }
        });

        symptoms[13].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 116);
                intent.putExtra("symptomName", symptoms[13].getText());
                startActivity(intent);
            }
        });

        symptoms[14].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 140);
                intent.putExtra("symptomName", symptoms[14].getText());
                startActivity(intent);
            }
        });

        symptoms[15].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 153);
                intent.putExtra("symptomName", symptoms[15].getText());
                startActivity(intent);
            }
        });

        symptoms[16].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 160);
                intent.putExtra("symptomName", symptoms[16].getText());
                startActivity(intent);
            }
        });

        symptoms[17].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 168);
                intent.putExtra("symptomName", symptoms[17].getText());
                startActivity(intent);
            }
        });

        symptoms[18].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 178);
                intent.putExtra("symptomName", symptoms[18].getText());
                startActivity(intent);
            }
        });

        symptoms[19].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("questionNum", 186);
                intent.putExtra("symptomName", symptoms[19].getText());
                startActivity(intent);
            }
        });
    }
}