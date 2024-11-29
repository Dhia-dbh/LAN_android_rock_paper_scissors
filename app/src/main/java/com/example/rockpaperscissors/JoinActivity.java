package com.example.rockpaperscissors;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
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

        InputFilter rangeFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String result = dest.toString() + source.toString();
                // Validate the number is between 0 and 255
                if (result.matches("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$") || result.isEmpty()) {
                    return null; // Allow valid input
                } else {
                    return ""; // Reject invalid input
                }
            }
        };

        ipAddressInput1.setFilters(new InputFilter[]{rangeFilter});
        ipAddressInput2.setFilters(new InputFilter[]{rangeFilter});
        ipAddressInput3.setFilters(new InputFilter[]{rangeFilter});
        ipAddressInput4.setFilters(new InputFilter[]{rangeFilter});

        String ipAddress = NetworkUtils.getIPAddress(this);
        Log.d("JoinActivity", "ipAddress: " + ipAddress);
        short subnetMask = NetworkUtils.getSubnetMask();
        assert ipAddress != null;
        String[] ipAddresses = ipAddress.split("\\.");
        if(subnetMask == 24){
            ipAddressInput1.setText(ipAddresses[0]);
            //ipAddressInput1.setFocusable(false);
            ipAddressInput2.setText(ipAddresses[1]);
            //ipAddressInput2.setFocusable(false);
            ipAddressInput3.setText(ipAddresses[2]);
            //ipAddressInput3.setFocusable(false);
        }

        // TextWatcher to auto-focus onto the next EditText when length reaches 3
        ipAddressInput1.addTextChangedListener(createTextWatcher(ipAddressInput2));
        ipAddressInput2.addTextChangedListener(createTextWatcher(ipAddressInput3));
        ipAddressInput3.addTextChangedListener(createTextWatcher(ipAddressInput4));
        
        
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
    }

    private TextWatcher createTextWatcher(final EditText nextEditText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {}


            @Override
            public void afterTextChanged(Editable editable) {
                // Move focus to the next EditText when the length reaches 3
                if (editable.length() == 3) {
                    nextEditText.requestFocus();
                }
            }
        };
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
