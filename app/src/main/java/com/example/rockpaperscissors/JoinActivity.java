package com.example.rockpaperscissors;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;

public class JoinActivity extends AppCompatActivity {
    private static final int PORT = 8080;
    private static final String TAG = "JoinActivity";
    public static Socket socket;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // Allow networking operations on main thread for simplicity (not recommended for production)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        connectButton = findViewById(R.id.connectButton);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String hostIp = findHostIp();
                            if (hostIp != null) {
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
                            } else {
                                runOnUiThread(() -> alert("Host Not Found", "No local host was found."));
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to connect to host.", e);
                            runOnUiThread(() -> Toast.makeText(JoinActivity.this, "Failed to connect to host.", Toast.LENGTH_SHORT).show());
                        }
                    }
                }).start();
            }
        });
    }

    private String findHostIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.isSiteLocalAddress()) {
                        String potentialHost = address.getHostAddress();
                        try (Socket testSocket = new Socket()) {
                            testSocket.connect(new InetSocketAddress(potentialHost, PORT), 500);
                            return potentialHost;
                        } catch (IOException ignored) {
                            Log.d("JoinAcitivity", "Failed Connection to : " + potentialHost);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while searching for host IP.", e);
        }
        return null;
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
