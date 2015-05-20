package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Messenger;
import android.os.SystemClock;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;
import bom.mitac.bist.burnin.util.BCRManager;

import com.mitac.cell.device.bcr.McBcrMessage;
import com.mitac.cell.device.bcr.MiBcrListener;
import com.mitac.cell.device.bcr.utility.BARCODE;


/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-13
 * Time: 上午11:40
 */
public class BCRTest extends TestClass {
    private static final String BCR_INTENT = "com.cell.device.bcr";

    private boolean result;
    private boolean isReady;
    // Scanned times
    private int times;

    private BCRManager bcrManager;
    private MiBcrListener bcrListener = new MiBcrListener() {

        @Override
        public void onScanned(String decodedData, BARCODE.TYPE barcodeType, int length) {
            result = true;
            sendMessage("Code: " + decodedData, true);
        }

        @Override
        public void onStatusChanged(int status) {
            switch (status) {
                case McBcrMessage.Status_Reading:
                    break;
                case McBcrMessage.Status_ScanStopped:
                    break;
                case McBcrMessage.Status_SettingChanged:
                    break;
            }
        }

    };

    public BCRTest(Messenger messenger, Activity activity, boolean isContinuous) {
        super(messenger, activity, isContinuous);
        this.id = BISTApplication.BCRTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
    }

    public BCRTest(Messenger messenger, Activity activity, int cycles) {
        super(messenger, activity, cycles);
        this.id = BISTApplication.BCRTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
    }

    @Override
    public boolean classSetup() {

        IntentFilter bcrIntent = new IntentFilter(BCR_INTENT);
        activity.registerReceiver(bcrReceiver, bcrIntent);

        for (int i = 0; i < 20; i++) {
            if (isReady) {
                break;
            } else {
                SystemClock.sleep(1000);
            }
        }

        for (int i = 0; i < 40; i++) {
            if (i % 20 == 0) {
                SystemClock.sleep(3000);
                bcrManager = BCRManager.getInstance(activity);
                SystemClock.sleep(1000);
                bcrManager.open();
                SystemClock.sleep(1000);
                String keybdString = "must://bcr/bcr?keybd_wedge=%s&copy_to_clipboard=%s";
                String command = String.format(keybdString, "false", "false");
                bcrManager.set(command);

                String goodVibration = "must://bcr/indication/good_read?vibration_enable=%s";
                command = String.format(goodVibration, "false");
                bcrManager.set(command);

                String badVibration = "must://bcr/indication/bad_read?vibration_enable=%s";
                command = String.format(badVibration, "false");
                bcrManager.set(command);

                String scanMode = "must://bcr/bcr?read_mode=%s&read_time=%s&read_reset_time=%s&";
                command = String.format(scanMode, "Single", "3000", "0");
                bcrManager.set(command);
                bcrManager.setListener(bcrListener);
            }
            if (i % 20 == 19 && bcrManager != null) {
                bcrManager.finish();
            }
            if (bcrManager == null || !bcrManager.isReady()) {
                SystemClock.sleep(1000);
            } else {
                return true;
            }
        }
//        SystemClock.sleep(3000);
//        if (bcrManager != null) {
//            bcrManager.open();
//            String formatSetString = "must://bcr/bcr?read_mode=%s&read_time=%s&read_reset_time=%s&";
//            String defaultUri = String.format(formatSetString, "Single", "3000", "0");
//            bcrManager.set(defaultUri);
//            bcrManager.setListener(bcrListener);
//            return bcrManager.isReady();
//        }
        return false;
    }

    @Override
    public boolean testSetup() {
        times = 0;
        if (bcrManager != null) {
//            bcrManager.setListener(bcrListener);
            return bcrManager.isReady();
        }
        return true;
    }

    @Override
    public boolean testBegin() {
//        int scannedTimes = 0;
//        for (int i = 0; i < 10; i++) {
//            result = false;
//            if (bcrManager != null) {
//                sendMessage("Start to scan", true);
//                bcrManager.startScan();
//                for (int j = 0; j < 30; j++) {
//                    if (result) {
//                        break;
//                    } else {
//                        SystemClock.sleep(100);
//                    }
//                }
//                if (result) {
//                    scannedTimes++;
//                } else {
//                    sendMessage("Fail to scan", true);
//                }
//                bcrManager.stopScan();
//                SystemClock.sleep(1000);
//            }
//        }
//        if (scannedTimes > 8) {
//            return true;
//        } else {
//            return false;
//        }


        result = false;
        if (bcrManager != null) {
            sendMessage("Start to scan", true);
            bcrManager.startScan();
            for (int i = 0; i < 30; i++) {
                if (result) {
                    break;
                } else {
                    SystemClock.sleep(100);
                }
            }
            if (result) {
                times++;
            } else {
                sendMessage("Fail to scan", true);
            }
            bcrManager.stopScan();
        }
        return result;
    }

    private BroadcastReceiver bcrReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra(McBcrMessage.Key);
            if (key.equals(McBcrMessage.Key_Status)){
                if (intent.getIntExtra(McBcrMessage.Key_Status, 0) == McBcrMessage.Status_Ready){
                    isReady = true;
                }
            }
        }
    };

//    @Override
//    public boolean testCleanup() {
////        if (bcrManager != null) {
////            bcrManager.stopListening();
////        }
//        // scanned 9 times of 10, then this cycle is passed.
//        if (times > cycles - 2) {
//            isPassed = true;
//        } else {
//            isPassed = false;
//        }
//        return true;
//    }
}
