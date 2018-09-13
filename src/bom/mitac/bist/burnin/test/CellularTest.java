package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Messenger;
import android.os.SystemClock;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;
import bom.mitac.bist.burnin.util.*;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-13
 * Time: 涓嬪�?2:03
 */
public class CellularTest extends TestClass {


    // TODO implement the TestClassLongTime

    private String host;
    private String user;
    private String password;
    private String local;
    private String localDownloaded;
    private String remote;
    private boolean isLoading;
    private FTPManager ftpManager;

    public CellularTest(Messenger messenger, Activity activity, int cycles, String host, String user, String password) {
        super(messenger, activity, cycles);
        this.id = BISTApplication.CellularTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");

        this.host = host;
        this.user = user;
        this.password = password;
    }

    public static void setCellularEnable(Activity activity, boolean enabled) {
        /*
        ConnectivityManager conMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        Class<?> conMgrClass = null; // ConnectivityManager绫�
        Field iConMgrField = null; // ConnectivityManager绫讳腑鐨勫瓧娈�
        Object iConMgr = null; // IConnectivityManager绫荤殑寮曠敤
        Class<?> iConMgrClass = null; // IConnectivityManager绫�
        Method setMobileDataEnabledMethod = null; // setMobileDataEnabled鏂规�?
        try {
            // 鍙栧緱ConnectivityManager绫�
            conMgrClass = Class.forName(conMgr.getClass().getName());
            // 鍙栧緱ConnectivityManager绫讳腑鐨勫璞Service
            iConMgrField = conMgrClass.getDeclaredField("mService");
            // 璁剧疆mService鍙闂�?            iConMgrField.setAccessible(true);
            // 鍙栧緱mService鐨勫疄渚嬪寲绫籌ConnectivityManager
            iConMgr = iConMgrField.get(conMgr);
            // 鍙栧緱IConnectivityManager绫�
            iConMgrClass = Class.forName(iConMgr.getClass().getName());
            // 鍙栧緱IConnectivityManager绫讳腑鐨剆etMobileDataEnabled(boolean)鏂规�?
            setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod(
                    "setMobileDataEnabled", Boolean.TYPE);
            // 璁剧疆setMobileDataEnabled鏂规硶鍙闂�
            setMobileDataEnabledMethod.setAccessible(true);
            // 璋冪敤setMobileDataEnabled鏂规�?
            setMobileDataEnabledMethod.invoke(iConMgr, enabled);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        */
    }

    public static boolean isCellularConnected(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }

    private boolean tryTurnCellularOn() {
        log("tryTurnCellularOn");
        log("setCellularEnable " + true);
        setCellularEnable(activity, true);
        for (int i = 0; i < 10; i++) {
            if (isStopped) {
                return false;
            } else if (isCellularConnected(activity)) {
                break;
            }

            if (i == 9) {
                // The Cellular is not enabled after 10 cycle. Change the status and re-check.
                log("setCellularEnable " + false);
                setCellularEnable(activity, false);
                SystemClock.sleep(3000);
                log("setCellularEnable " + true);
                setCellularEnable(activity, true);
                i = -1;
            } else {
                SystemClock.sleep(3000);
            }
        }
        return true;
    }

    private boolean tryConnectCellularNet() {
        log("tryConnectCellularNet");
        tryTurnCellularOn();
        for (int i = 0; i < 10; i++) {
            if (isStopped) {
                return false;
            } else if (isCellularConnected(activity)) {
                break;
            }
            if (i == 9) {
                // The wifi is not enabled after 10 cycle. Change the status and re-check.
                setCellularEnable(activity, false);
                SystemClock.sleep(3000);
                tryTurnCellularOn();
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

    private boolean testSequence() {

        if (isStopped) {
            return true;
        }
        isLoading = true;
        log("uploading");
        if (upload()) {
            log("uploaded");

            if (isStopped) {
                return true;
            }

            log("downloading");
            if (download()) {
                log("downloaded");
                isLoading = true;

                if (isStopped) {
                    return true;
                }
                log("comparing");
                if (compare()) {
                    log("compared");
                    return true;
                }
            }
        }
        log("failed");
        return false;
    }

    @Override
    public boolean classSetup() {
        ftpManager = FTPManager.getInstance(30000, 3000, 300000);
        return ftpManager != null && Validator.isString(host, user, password);
    }

    @Override
    public boolean testSetup() {
        WIFITest.changeWifiStatus(activity, false);
        return tryConnectCellularNet();
    }

    @Override
    public boolean testBegin() {
        File localFile = new File(BISTApplication.BASE_PATH, "FTPUpload");
        try {
            log("Creating");
            FileManager.create(localFile, 1);
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

        boolean isConnected = false;
        for (int i = 0; i < 3; i++) {
            if (isStopped) {
                return false;
            }
            log("connecting");
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

        return testSequence();

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
        log("disconnecting");
        disconnect();
        return true;
    }
}
