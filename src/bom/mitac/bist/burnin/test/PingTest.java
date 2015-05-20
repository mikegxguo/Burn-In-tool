package bom.mitac.bist.burnin.test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.os.SystemClock;
import android.util.Log;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Messenger;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;
import bom.mitac.bist.burnin.util.CommandManager;
import bom.mitac.bist.burnin.util.WifiConnect;
import bom.mitac.bist.burnin.util.WifiConnect.WifiCipherType;

import org.apache.http.conn.util.InetAddressUtils;

public class PingTest extends TestClass {
    private WifiManager wifiManager;
    private WifiConnect wifi;
    private List<String> SSIDs;
    private String site;
    private String ping_result;
    private int ping_result_len;
    private int index;
    private String key_str;

    public PingTest(Messenger messenger, Activity activity, int cycles, String site, String SSID1, String SSID2, String SSID3) {
        super(messenger, activity, cycles);
        this.id = BISTApplication.WIFITest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");

        this.SSIDs = new ArrayList<String>(3);
        SSIDs.add(SSID1);
        SSIDs.add(SSID2);
        SSIDs.add(SSID3);

        this.site = site;
    }

    public static boolean changeWifiStatus(Activity activity, boolean connect) {
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Activity.WIFI_SERVICE);
        if (wifiManager == null)
            return false;
        wifiManager.setWifiEnabled(connect);
        if (connect)
            wifiManager.reconnect();
        return isWifiEnabled(activity);
    }

    public static boolean isWifiEnabled(Activity activity) {
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Activity.WIFI_SERVICE);
        if (wifiManager == null)
            return false;
        return wifiManager.isWifiEnabled();
    }

    public static boolean isWifiConnected(Activity activity) {
        ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Activity.CONNECTIVITY_SERVICE);
        if (connManager == null)
            return false;
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi == null)
            return false;
        return wifi.isConnected();
    }

    private static String getIPV4() {
        try {
            String ipv4;
            List<NetworkInterface> netList = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface net : netList) {
                List<InetAddress> addressList = Collections.list(net.getInetAddresses());
                for (InetAddress address : addressList) {
                    if (!address.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4 = address.getHostAddress())) {
                        return ipv4;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getIPV4(Activity activity) {
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Activity.WIFI_SERVICE);
        int ip = wifiManager.getConnectionInfo().getIpAddress();
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }

    private boolean tryTurnWifiOn() {
        changeWifiStatus(activity, true);
        for (int i = 0; i < 10; i++) {
            if (isStopped) {
                return false;
            } else if (isWifiEnabled(activity)) {
                return true;
            }
            SystemClock.sleep(1500);

//            if (i == 9) {
//                // The wifi is not enabled after 10 cycle. Change the status and re-check.
//                log("changeWifiStatus " + false);
//                changeWifiStatus(activity, false);
//                SystemClock.sleep(3000);
//                log("changeWifiStatus " + true);
//                changeWifiStatus(activity, true);
//                i = -1;
//            } else {
//                SystemClock.sleep(3000);
//            }
        }
        return false;
    }

    private boolean tryConnectWifiAP() {
        if (isWifiConnected(activity))
            return true;

        Collections.shuffle(SSIDs);

        while (!isStopped) {
            if (connect(SSIDs.get(0)))
                return true;

            if (connect(SSIDs.get(1)))
                return true;

            if (connect(SSIDs.get(2)))
                return true;
        }
        return false;
    }

    private boolean connect(String SSID) {
        if (!isWifiEnabled(activity))
            return false;
        sendMessage("Connecting AP: " + SSID, true);
        wifi.Connect(SSID, null, WifiCipherType.WIFICIPHER_NOPASS);
        for (int i = 0; i < 10; i++) {
            SystemClock.sleep(1000);
            if (isStopped) {
                break;
            } else if (isWifiConnected(activity)) {
                sendMessage("Connecting success", true);
                return isIPgot();
            }
        }
        sendMessage("Connecting failed", true);
        return false;
//        for (int i = 0; i < 10; i++) {
//            SystemClock.sleep(3000);
//            if (isStopped) {
//                break;
//            } else if (isWifiConnected(activity)) {
//                sendMessage("Connecting success", true);
//                return true;
//            }
//        }
//        sendMessage("Connecting failed", true);
//        return false;
    }

    private boolean ping() {

//        sendMessage("New Ping method: " + wifiManager.pingSupplicant(), true);
        String[] PING = {"ping", "-w", "4", site};
        String result = null;
        ping_result = CommandManager.run_command(PING, "/system/bin");

        sendMessage(ping_result, true);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (avgSpeed(ping_result) != null) {
            sendMessage("Ping SUCCESS!", true);
            return true;
        } else {
            sendMessage("Ping FAIL!", true);
            return false;
        }

    }

    private boolean isIPgot() {
        sendMessage("Fetching IP address", true);
        for (int i = 0; i < 10; i++) {
            SystemClock.sleep(1000);
            if (isStopped) {
                break;
            } else if (getIPV4(activity).length() > 7) {
                sendMessage("Success", true);
                return true;
            }
        }
        sendMessage("Failed", true);
        return false;
    }

    private String scan() {
        if (wifiManager == null) {
            return null;
        } else {
            StringBuilder apList = new StringBuilder();
            for (ScanResult scanResult : wifiManager.getScanResults()) {
                String ssid = scanResult.SSID;
                if (!apList.toString().contains(ssid)) {
                    apList.append(scanResult.SSID).append("\r\n");
                }
            }
            return apList.toString();
        }
    }

    private String avgSpeed(String str) {
        int position = str.indexOf("min/avg/max");
        if (position != -1) {
            String subStr = str.substring(position + 18);
            position = subStr.indexOf("/");
            subStr = subStr.substring(position + 1);
            position = subStr.indexOf("/");
            return subStr.substring(0, position);
        } else {
            return null;
        }
    }

    @Override
    public boolean classSetup() {
        wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        wifi = new WifiConnect(wifiManager);
        return true;
    }

    @Override
    public boolean testSetup() {
        if (tryTurnWifiOn()) {
            sendMessage("Wifi is ON", true);
            if (tryConnectWifiAP()) {
                sendMessage("Wifi is connected", true);
                return true;
            } else {
                sendMessage("Can not connect the wifi ap", true);
            }
        } else {
            sendMessage("Can not enable the wifi", true);
        }
        return false;
    }

    @Override
    public boolean testBegin() {
        sendMessage(scan(), true);
        for (int i = 0; i < 3; i++) {
            if (ping())
                return true;
        }
        return false;
    }

    @Override
    public boolean testCleanup() {
        sendMessage("IP: " + getIPV4(activity) + ", " + wifiManager.getConnectionInfo().toString(), true);
        changeWifiStatus(activity, false);
        return true;
    }
}
