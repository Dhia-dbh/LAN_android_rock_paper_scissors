package com.example.rockpaperscissors;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JoinActivity extends AppCompatActivity {
    private static final int PORT = 8080;
    private static final String TAG = "JoinActivity";
    public static Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // Allow networking operations on main thread for simplicity (not recommended for production)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        new Thread(() -> {
            String subnet = NetworkUtils.getSubnet(JoinActivity.this);
            ExecutorService executor = Executors.newFixedThreadPool(255);

            for (int i = 1; i < 255; i++) {
                final int hostIp = i;
                executor.submit(() -> {

                    String host = subnet + hostIp;
                    try {
                        Log.d(TAG, "Attempting to find host at " + host);
                        Socket testSocket = new Socket(host, PORT);

                        Log.d(TAG, "Host " + host + " is reachable on port " + PORT);

                        // Assign the working socket reference
                        socket = testSocket;

                        // Proceed to GameActivity only if socket is valid and connected
                        if (socket.isConnected()) {
                            runOnUiThread(() -> {
                                Intent intent = new Intent(JoinActivity.this, GameActivity.class);
                                intent.putExtra("socket_connected", true); // Pass a flag indicating socket is connected
                                startActivity(intent);
                            });
                            // Shutdown executor since we have found the target host
                            executor.shutdownNow();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to connect to: " + host);
                        e.printStackTrace();
                    }

                });
            }

        }).start();
    }
}
