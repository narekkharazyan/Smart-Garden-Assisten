package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    public void startNewActivity2(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void startNewActivity3(View v) {
        Intent intent = new Intent(this, MainActivity3.class);
        startActivity(intent);
    }
    public void startNewActivity5(View v) {
        Intent intent = new Intent(this, MainActivity4.class);
        startActivity(intent);
    }
}
