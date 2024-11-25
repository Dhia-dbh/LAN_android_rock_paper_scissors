package com.example.rockpaperscissors;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class HostActivity extends AppCompatActivity {
    private static final int PORT = 8080;
    private static final String TAG = "HostActivity";
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
                    serverSocket.setSoTimeout(30000); // Set a timeout for accepting connections
                    String hostIp = NetworkUtils.getIPAddress(HostActivity.this);
                    if (hostIp != null) {
                        String tv_ipAddress = "Your IP Address: " + hostIp;
                        runOnUiThread(() -> ipAddressTv.setText(tv_ipAddress));
                        Log.i(TAG, "Host IP address: " + hostIp);
                        Log.d(TAG, "Server started. Waiting for client...");
                    } else {
                        runOnUiThread(() -> showToast("Unable to retrieve IP address."));
                        return;
                    }

                    clientSocket = serverSocket.accept();
                    Log.d(TAG, "Client connected.");

                    // When a client connects, proceed to GameActivity
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(HostActivity.this, GameActivity.class);
                            startActivity(intent);
                        }
                    });
                } catch (SocketTimeoutException e) {
                    Log.e(TAG, "Server socket accept timed out.", e);
                    runOnUiThread(() -> showToast("Connection timed out. No client connected."));
                } catch (IOException e) {
                    Log.e(TAG, "IOException while starting server or accepting client.", e);
                    runOnUiThread(() -> showToast("Error while starting server or accepting client."));
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
                Log.e(TAG, "Error closing server socket", e);
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

    private void showToast(String message) {
        Toast.makeText(HostActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
