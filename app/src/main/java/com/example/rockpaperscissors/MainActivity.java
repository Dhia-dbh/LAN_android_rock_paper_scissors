package com.example.rockpaperscissors;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onHostGameClick(View view) {
        Intent intent = new Intent(this, HostActivity.class);
        startActivity(intent);
    }

    public void onJoinGameClick(View view) {
        Intent intent = new Intent(this, JoinActivity.class);
        startActivity(intent);
    }
}