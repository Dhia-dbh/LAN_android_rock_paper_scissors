package com.example.rockpaperscissors;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class JoinActivity extends AppCompatActivity {
    private static final int PORT = 8080;
    private static final String TAG = "JoinActivity";
    public static Socket socket;
    private Button connectButton;
    private ListView ipAddressListView;
    private List<String> ipAddressList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // Allow networking operations on main thread for simplicity (not recommended for production)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        connectButton = findViewById(R.id.connectButton);
        ipAddressListView = findViewById(R.id.ipAddressListView);
        ipAddressList = new ArrayList<>();

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        findHostIps();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!ipAddressList.isEmpty()) {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(JoinActivity.this, android.R.layout.simple_list_item_1, ipAddressList);
                                    ipAddressListView.setAdapter(adapter);
                                } else {
                                    alert("Host Not Found", "No local host was found.");
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        ipAddressListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedIp = ipAddressList.get(position);
                connectToHost(selectedIp);
            }
        });
    }

    private void findHostIps() {
        ipAddressList.clear();
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
                            ipAddressList.add(potentialHost);
                        } catch (IOException ignored) {
                            // Failed to connect, continue searching
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while searching for host IPs.", e);
        }
    }

    private void connectToHost(String hostIp) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "Attempting to connect to host at " + hostIp);
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
                    Log.e(TAG, "Failed to connect to host: " + hostIp, e);
                    runOnUiThread(() -> Toast.makeText(JoinActivity.this, "Failed to connect to host: " + hostIp, Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
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
