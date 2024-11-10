package com.example.rockpaperscissors;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HostActivity extends AppCompatActivity {
    private static final int PORT = 8080;
    private TextView ipAddressTv;
    public static ServerSocket serverSocket = null;
    public static Socket clientSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
         ipAddressTv = findViewById(R.id.ipAddress);

        // Allow networking operations on main thread for simplicity (not recommended for production)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    String hostIp = NetworkUtils.getIPAddress(HostActivity.this);
                    String tv_ipAddress = "Your Ip Address: " + hostIp;
                    ipAddressTv.setText(tv_ipAddress);
                    Log.i("HostActivity", "Host ip address: " + hostIp);
                    Log.d("HostActivity", "Server started. Waiting for client...");
                    clientSocket = serverSocket.accept();
                    Log.d("HostActivity", "Client connected.");

                    // When a client connects, proceed to GameActivity
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Intent intent = new Intent(HostActivity.this, GameActivity.class);
                            startActivity(intent);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void alert(String title, String msg) {
        new AlertDialog.Builder(HostActivity.this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        findViewById(R.id.goHomeButton).setVisibility(View.VISIBLE);
                    }
                })
                .setCancelable(false)
                .show();
    }
}