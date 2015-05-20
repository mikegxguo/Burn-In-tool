package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Messenger;
import android.os.SystemClock;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;
import bom.mitac.bist.burnin.util.*;


import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-13
 * Time: 下午12:03
 */
public class WIFITest extends TestClass {

    // TODO implement the TestClassLongTime

    private String host;
    private String user;
    private String password;
    private String local;
    private String localDownloaded;
    private String remote;
    private boolean isLoading;
    private FTPManager ftpManager;

    public WIFITest(Messenger messenger, Activity activity, int cycles, String host, String user, String password) {
        super(messenger, activity, cycles);
        this.id = BISTApplication.WIFITest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");

        this.host = host;
        this.user = user;
        this.password = password;
    }

    public WIFITest(Messenger messenger, Activity activity, boolean isContinuous, String host, String user, String password) {
        super(messenger, activity, isContinuous);
        this.id = BISTApplication.WIFITest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");

        this.host = host;
        this.user = user;
        this.password = password;
    }

//    public WIFITest(Messenger messenger, Activity Activity) {
//        this.messenger = messenger;
//        this.Activity = Activity;
//
//        this.isContinous = false;
//        this.id = BISTApplication.WIFITest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, "WIFITest.txt");
//
//        this.ftpManager = FTPManager.getInstance(60000, 5000, 60000);
//    }

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

    private boolean tryTurnWifiOn() {
        log("tryTurnWifiOn");
        log("changeWifiStatus " + true);
        changeWifiStatus(activity, true);
        for (int i = 0; i < 10; i++) {
            if (isStopped) {
                return false;
            } else if (isWifiEnabled(activity)) {
                break;
            }

            if (i == 9) {
                // The wifi is not enabled after 10 cycle. Change the status and re-check.
                log("changeWifiStatus " + false);
                changeWifiStatus(activity, false);
                SystemClock.sleep(3000);
                log("changeWifiStatus " + true);
                changeWifiStatus(activity, true);
                i = -1;
            } else {
                SystemClock.sleep(3000);
            }
        }
        return true;
    }

    private boolean tryConnectWifiAP() {
        log("tryConnectWifiAP");
        tryTurnWifiOn();
        for (int i = 0; i < 10; i++) {
            if (isStopped) {
                return false;
            } else if (isWifiConnected(activity)) {
                break;
            }
            if (i == 9) {
                // The wifi is not enabled after 10 cycle. Change the status and re-check.
                changeWifiStatus(activity, false);
                SystemClock.sleep(3000);
                tryTurnWifiOn();
                i = -1;
            } else {
                SystemClock.sleep(3000);
            }
        }
        return true;
    }

    private boolean connect() {
        if (!Validator.isString(host, user, password) || ftpManager == null)
            return false;

        try {
            return ftpManager.connect(host, 21, user, password);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void disconnect() {
        if (ftpManager == null) {
            return;
        }
        try {
            ftpManager.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean upload() {
        if (!Validator.isString(local, remote) || ftpManager == null || !ftpManager.isConnected()) {
            return false;
        }
        return ftpManager.upload(local, remote);
    }

    private boolean download() {
        if (!Validator.isString(remote, localDownloaded) || ftpManager == null || !ftpManager.isConnected()) {
            return false;
        }
        return ftpManager.download(remote, localDownloaded);
    }

    private boolean compare() {
        try {
            return FileManager.compare(new File(local), new File(localDownloaded));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean classSetup() {
        ftpManager = FTPManager.getInstance(30000, 3000, 300000);
        return ftpManager != null && Validator.isString(host, user, password);
    }

    @Override
    public boolean testSetup() {
        if (tryConnectWifiAP()) {
            File localFile = new File(BISTApplication.BASE_PATH, "FTPUpload");
            try {
                sendMessage("Creating 5M file on the disk", true);
                FileManager.create(localFile, 5 * 1024);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!Validator.isFile(localFile)) {
                return false;
            } else {
                local = BISTApplication.BASE_PATH + File.separator + "FTPUpload";
                localDownloaded = BISTApplication.BASE_PATH + File.separator + "FTPDownload";
                remote = BISTApplication.REMOTE_FTP_PATH + File.separator + "WIFITest";
            }

            sendMessage("Success", true);
            boolean isConnected = false;
            for (int i = 0; i < 3; i++) {
                if (isStopped) {
                    return false;
                }
                sendMessage("Connecting FTP", true);
                isConnected = connect();
                if (isConnected) {
                    break;
                } else {
                    SystemClock.sleep(5000);
                }
            }
            if (!isConnected) {
                return false;
            }
            sendMessage("Connected.", true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean testBegin() {
        if (isStopped) {
            return true;
        }
        isLoading = true;
        sendMessage("Uploading.", true);
        if (upload()) {
            sendMessage("Uploaded.", true);

            if (isStopped) {
                return true;
            }

            sendMessage("Downloading.", true);
            if (download()) {
                sendMessage("Downloaded.", true);
                isLoading = true;

                if (isStopped) {
                    return true;
                }
                sendMessage("Comparing.", true);
                if (compare()) {
                    sendMessage("Success.", true);
                    return true;
                }
            }
        }
        sendMessage("Failed.", true);
        return false;
    }

    @Override
    public boolean testEnd() {
        if (ftpManager == null || !ftpManager.isConnected() || !isLoading) {
        } else {
            ftpManager.abort();
        }
        return super.testEnd();
    }

    @Override
    public boolean testCleanup() {
        sendMessage("Disconnecting.", true);
        disconnect();
        sendMessage("Disconnected.", true);
        return true;
    }

}
