package com.example.rockpaperscissors;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class JoinActivity extends AppCompatActivity {
    private static final int PORT = 8080;
    private static final String TAG = "JoinActivity";
    public static Socket socket;
    private EditText ipAddressInput1;
    private EditText ipAddressInput2;
    private EditText ipAddressInput3;
    private EditText ipAddressInput4;
    private TextView dot1;
    private TextView dot2;
    private TextView dot3;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // Allow networking operations on main thread for simplicity (not recommended for production)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ipAddressInput1 = findViewById(R.id.ipAddressInput1);
        ipAddressInput2 = findViewById(R.id.ipAddressInput2);
        ipAddressInput3 = findViewById(R.id.ipAddressInput3);
        ipAddressInput4 = findViewById(R.id.ipAddressInput4);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        dot1.setText(".");
        dot2.setText(".");
        dot3.setText(".");

        connectButton = findViewById(R.id.connectButton);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String hostIp = ipAddressInput.getText().toString().trim();
                String hostIp = ipAddressInput1.getText() + "." + ipAddressInput2.getText() + "." + ipAddressInput3.getText() + "." + ipAddressInput4.getText();
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

        String ipHost = NetworkUtils.getIPAddress(JoinActivity.this);
        short subnetMask = NetworkUtils.getSubnetMask();

        Toast.makeText(this, "Ip address: " + NetworkUtils.getIPAddress(JoinActivity.this) + " \t/" + NetworkUtils.getSubnetMask(), Toast.LENGTH_LONG).show();
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
