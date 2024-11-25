package com.example.rockpaperscissors;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class JoinActivity extends AppCompatActivity {
    private static final int PORT = 8080;
    private static final String TAG = "JoinActivity";
    public static Socket socket;
    private EditText ipAddressInput;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // Allow networking operations on main thread for simplicity (not recommended for production)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ipAddressInput = findViewById(R.id.ipAddressInput);
        connectButton = findViewById(R.id.connectButton);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hostIp = ipAddressInput.getText().toString().trim();
                if (isValidIpAddress(hostIp)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.i(TAG, "Attempting to find host at " + hostIp);
                                socket = new Socket();
                                socket.connect(new InetSocketAddress(hostIp, PORT), 1000);
                                Log.d(TAG, "Connected to host at: " + hostIp);

                                // If found, proceed to GameActivity
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(JoinActivity.this, GameActivity.class);
                                        startActivity(intent);
                                    }
                                });
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to connect to: " + hostIp, e);
                                runOnUiThread(() -> Toast.makeText(JoinActivity.this, "Failed to connect to: " + hostIp, Toast.LENGTH_SHORT).show());
                            }
                        }
                    }).start();
                } else {
                    alert("Invalid IP Address", "Please enter a valid IP address.");
                }
            }
        });
    }

    private boolean isValidIpAddress(String ipAddress) {
        try {
            InetAddress inet = InetAddress.getByName(ipAddress);
            return inet.getHostAddress().equals(ipAddress) && ipAddress.split("\\.").length == 4;
        } catch (Exception e) {
            return false;
        }
    }

    private void alert(String title, String msg) {
        new AlertDialog.Builder(JoinActivity.this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog
                    }
                })
                .setCancelable(false)
                .show();
    }
}
