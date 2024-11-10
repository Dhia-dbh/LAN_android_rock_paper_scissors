package com.example.rockpaperscissors;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtils {

    public static String getSubnet(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();

            // Convert the integer IP address to a human-readable format
            String ipString = Formatter.formatIpAddress(ipAddress);
            try {
                InetAddress inetAddress = InetAddress.getByName(ipString);
                byte[] ipBytes = inetAddress.getAddress();

                // Assuming a common /24 subnet mask (255.255.255.0)
                ipBytes[3] = 0;  // Set the last byte to 0 to represent the subnet

                // Create the subnet address
                InetAddress subnetAddress = InetAddress.getByAddress(ipBytes);
                return subnetAddress.getHostAddress();

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
