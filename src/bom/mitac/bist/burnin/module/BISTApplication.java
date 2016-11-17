package bom.mitac.bist.burnin.module;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import bom.mitac.bist.burnin.test.CellularTest;
import bom.mitac.bist.burnin.util.TimeStamp;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-28
 * Time: 下午3:40
 */
public class BISTApplication extends Application {
    // Const
    public static final String VIDEO_PATH = "video_path";
    public static final String VIDEO_TIME = "video_time";
    public static final String VIDEO_VOLUME = "video_volume";
    public static final String CAMERA_FACING = "camera_facing";
    public static final String CAMERA_SHOT_TIMES = "camera_shot_times";
    public static final String CAMERA_DELETE_FILE = "camera_delete_file";
    public static final String SUSPEND_TIME = "suspend_time";


    // Path
    public static final String BASE_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "BIST";
    public static final String OLD_LOG_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "BIST" + File.separator + "Old";
    public static final String CONFIG_PATH = BASE_PATH
            + File.separator + "config.txt";
    public static final String PIC_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "BIST" + File.separator + "PIC";
    //    public static String LOG_PATH = BASE_PATH
//            + File.separator + TimeStamp.getTimeStamp(TimeStamp.TimeType.FULL_S_TYPE);
    public static final String REMOTE_FTP_PATH = File.separator + "BIST";
//    public static final String EXT_SDCARD_PATH = File.separator + "mnt" + File.separator + "ext_sdcard"
//            + File.separator + "BIST";
//    public static final String EXT_SDCARD_PATH = File.separator + "storage/E50B-0AF9"
//     public static final String EXT_SDCARD_PATH = File.separator + "storage/30DF-0E81"
//            + File.separator + "BIST";
    public static final String EXT_USB_PATH = File.separator + "mnt" + File.separator + "usb1_storage"
            + File.separator + "BIST";
    public static final String LOG_NAME = "BurnIn.txt";
    public static final String TOTAL_LOG_NAME = "Total.txt";
    //The global variable remembers the status of suspend test
    public static boolean g_bEndSuspendTest = true;

    // ID
    public static final int COMMAND = -8000;
    public static final int KEY = -8001;
    public static final int TIME_CYCLE = -8002;

    public static final int INANDTest_ID = -8901;
    public static final int SDTest_ID = -8902;
    public static final int BTTest_ID = -8903;
    public static final int GPSTest_ID = -8904;
    //    public static final int CameraTest_ID = -8905;
    public static final int NFCTest_ID = -8906;
    public static final int BCRTest_ID = -8907;
    public static final int CellularTest_ID = -8908;
    public static final int WIFITest_ID = -8909;
    public static final int SuspendTest_ID = -8910;
    public static final int TemperatureTest_ID = -8911;
    public static final int VibratorTest_ID = -8912;
    public static final int BKLTest_ID = -8913;
    public static final int SensorTest_ID = -8914;
    public static final int PingTest_ID = -8915;
    public static final int IMEITest_ID = -8916;
    public static final int FlashTest_ID = -8917;
    public static final int BatteryTest_ID = -8918;
    public static final int VideoTest_ID = -8919;
    public static final int RebootTest_ID = -8920;
    public static final int USBTest_ID = -8921;
    public static final int CameraTest_BACK_ID = -8922;
    public static final int CameraTest_FRONT_ID = -8923;

    // IDs
    public static final int[] IDS = {
            INANDTest_ID, SDTest_ID, BTTest_ID, NFCTest_ID, BCRTest_ID, CellularTest_ID,
            SuspendTest_ID, TemperatureTest_ID, VibratorTest_ID, BKLTest_ID,
            SensorTest_ID, WIFITest_ID, PingTest_ID, IMEITest_ID, FlashTest_ID,
            BatteryTest_ID, VideoTest_ID, RebootTest_ID, USBTest_ID,
//            CameraTest_ID, // back
//            CameraTest_ID, // front
            CameraTest_BACK_ID,  // back
            CameraTest_FRONT_ID,  // front
//            GPSTest_ID,     // hot
//            GPSTest_ID,     // warm
//            GPSTest_ID,     // cold
            GPSTest_ID,     // nmea
    };

//    public static final String INANDTest_NAME = "INANDTest";
//    public static final String SDTest_NAME = "SDTest";
//    public static final String BTTest_NAME = "BTTest";
//    public static final String GPSTest_NAME = "GPSTest";
//    public static final String CameraTest_NAME = "CameraTest";
//    public static final String NFCTest_NAME = "NFCTest";
//    public static final String BCRTest_NAME = "BCRTest";
//    public static final String CellularTest_NAME = "CellularTest";
//    public static final String WIFITest_NAME = "WIFITest";
//    public static final String SuspendTest_NAME = "SuspendTest";
//    public static final String TemperatureTest_NAME = "TemperatureTest";
//    public static final String VibratorTest_NAME = "VibratorTest";
//    public static final String BKLTest_NAME = "BKLTest";
//    public static final String SensorTest_NAME = "SensorTest";
//    public static final String PingTest_NAME = "PingTest";
//    public static final String IMEITest_NAME = "IMEITest";
//    public static final String FlashTest_NAME = "INANDTest";
//    public static final String BatteryTest_NAME = "BatteryTest";
//    public static final String VideoTest_NAME = "VideoTest";

    public static final String INANDTest_NAME = "INAND";
    public static final String SDTest_NAME = "SD";
    public static final String BTTest_NAME = "BT";
    public static final String GPSTest_NAME = "GPS";
    public static final String CameraTest_NAME = "Camera";
    public static final String NFCTest_NAME = "NFC";
    public static final String BCRTest_NAME = "BCR";
    public static final String CellularTest_NAME = "3G";
    public static final String WIFITest_NAME = "WIFI";
    public static final String SuspendTest_NAME = "Suspend";
    public static final String TemperatureTest_NAME = "Temperature";
    public static final String VibratorTest_NAME = "Vibrator";
    public static final String BKLTest_NAME = "BKL";
    public static final String SensorTest_NAME = "Sensor";
    public static final String PingTest_NAME = "Ping";
    public static final String IMEITest_NAME = "IMEI";
    public static final String FlashTest_NAME = "Flash";
    public static final String BatteryTest_NAME = "Battery";
    public static final String VideoTest_NAME = "Video";

    public static enum TEST_NAME {
        INAND, SD, BT, GPS, CAMERA, NFC, BCR,
        CELLULAR, WIFI, SUSPEND, TEMPERATURE,
        VIBRATOR, BKL, SENSOR, FLASH, BATTERY,
        VIDEO, REBOOT, USB
    }

    public static final Map<Integer, String> ID_NAME = new HashMap<Integer, String>() {
        // Save in one log file
//        public final String unitLogFile = "BIST_Testlog";

        //        {
//            put(INANDTest_ID, INANDTest_NAME);
//            put(SDTest_ID, SDTest_NAME);
//            put(VideoTest_ID, VideoTest_NAME);
//            put(BTTest_ID, BTTest_NAME);
//            put(GPSTest_ID, GPSTest_NAME);
//            put(CameraTest_ID, CameraTest_NAME);
//            put(NFCTest_ID, NFCTest_NAME);
//            put(BCRTest_ID, BCRTest_NAME);
//            put(CellularTest_ID, CellularTest_NAME);
//            put(WIFITest_ID, WIFITest_NAME);
//            put(SuspendTest_ID, SuspendTest_NAME);
//            put(TemperatureTest_ID, TemperatureTest_NAME);
//            put(VibratorTest_ID, VibratorTest_NAME);
//            put(BKLTest_ID, BKLTest_NAME);
//            put(SensorTest_ID, SensorTest_NAME);
//            put(PingTest_ID, PingTest_NAME);
//            put(IMEITest_ID, IMEITest_NAME);
//            put(FlashTest_ID, FlashTest_NAME);
//            put(BatteryTest_ID, BatteryTest_NAME);
//        }
//        // Save in one log file
//        {
//            put(INANDTest_ID, unitLogFile);
//            put(SDTest_ID, unitLogFile);
//            put(VideoTest_ID, unitLogFile);
//            put(BTTest_ID, unitLogFile);
//            put(GPSTest_ID, unitLogFile);
//            put(CameraTest_ID, unitLogFile);
//            put(NFCTest_ID, unitLogFile);
//            put(BCRTest_ID, unitLogFile);
//            put(CellularTest_ID, unitLogFile);
//            put(WIFITest_ID, unitLogFile);
//            put(SuspendTest_ID, unitLogFile);
//            put(TemperatureTest_ID, unitLogFile);
//            put(VibratorTest_ID, unitLogFile);
//            put(BKLTest_ID, unitLogFile);
//            put(SensorTest_ID, unitLogFile);
//            put(PingTest_ID, unitLogFile);
//            put(IMEITest_ID, unitLogFile);
//            put(FlashTest_ID, unitLogFile);
//            put(BatteryTest_ID, unitLogFile);
//        }
        {
            put(INANDTest_ID, TEST_NAME.INAND.toString());
            put(SDTest_ID, TEST_NAME.SD.toString());
            put(VideoTest_ID, TEST_NAME.VIDEO.toString());
            put(BTTest_ID, TEST_NAME.BT.toString());
            put(GPSTest_ID, TEST_NAME.GPS.toString());
//            put(CameraTest_ID, TEST_NAME.CAMERA.toString());
            put(NFCTest_ID, TEST_NAME.NFC.toString());
            put(BCRTest_ID, TEST_NAME.BCR.toString());
            put(CellularTest_ID, TEST_NAME.CELLULAR.toString());
            put(WIFITest_ID, TEST_NAME.WIFI.toString());
            put(SuspendTest_ID, TEST_NAME.SUSPEND.toString());
            put(TemperatureTest_ID, TEST_NAME.TEMPERATURE.toString());
            put(VibratorTest_ID, TEST_NAME.VIBRATOR.toString());
            put(BKLTest_ID, TEST_NAME.BKL.toString());
            put(SensorTest_ID, TEST_NAME.SENSOR.toString());
            put(PingTest_ID, TEST_NAME.WIFI.toString());
            put(IMEITest_ID, TEST_NAME.CELLULAR.toString());
            put(FlashTest_ID, TEST_NAME.FLASH.toString());
            put(BatteryTest_ID, TEST_NAME.BATTERY.toString());
            put(RebootTest_ID, TEST_NAME.REBOOT.toString());
            put(USBTest_ID, TEST_NAME.USB.toString());
            put(CameraTest_BACK_ID, TEST_NAME.CAMERA.toString());
            put(CameraTest_FRONT_ID, TEST_NAME.CAMERA.toString());
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                ex.printStackTrace();
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                writeException(TimeStamp.getTimeStamp(TimeStamp.TimeType.FULL_L_TYPE));
                writeException(sw.toString());
                try {
                    sw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void writeException(String str) {
        File logFile = new File(BASE_PATH, "Exception.txt");
        File path = new File(logFile.getParent());
        if (!path.exists()) {
            if (!path.mkdirs()) {
                return;
            }
        }

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(logFile, true);
            fileWriter.append(str);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
