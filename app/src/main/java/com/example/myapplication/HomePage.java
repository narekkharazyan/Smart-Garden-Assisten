package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home_page);

    }

    public void settingspage(View v) {
        Intent intent = new Intent(this, settings.class);
        startActivity(intent);
    }
    public void menupage(View v) {
        Intent intent = new Intent(this, menu.class);
        startActivity(intent);
    }
    public void myplants(View v) {
        Intent intent = new Intent(this, myplants.class);
        startActivity(intent);
    }
    public void profile(View v) {
        Intent intent = new Intent(this, profile.class);
        startActivity(intent);
    }
    public void weather(View v) {
        Intent intent = new Intent(this, weather.class);
        startActivity(intent);
    }
}