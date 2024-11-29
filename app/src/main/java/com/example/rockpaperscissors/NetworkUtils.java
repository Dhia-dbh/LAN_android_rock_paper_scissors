package com.example.rockpaperscissors;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

public class NetworkUtils {

    public static String getIPAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            int mask = wifiInfo.getNetworkId();
            Log.d("InterfaceAddress", "Network id: " + wifiInfo.getNetworkId());
            return Formatter.formatIpAddress(ipAddress);
        }
        return null;
    }
    public static short getSubnetMask() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress inetAddress : inetAddresses) {
                    // Check if the address is an IPv4 address (ignores IPv6)
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                        short subnetMask = networkInterface.getInterfaceAddresses().get(1).getNetworkPrefixLength();
                        // Format and return subnet mask
                        Log.d("InterfaceAddress", "Subnet mask: " + subnetMask);
                        return subnetMask;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
