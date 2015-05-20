package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.content.Context;
import android.os.Messenger;
import android.telephony.TelephonyManager;
import android.util.Log;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;


import java.io.File;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-27
 * Time: 下午3:26
 */
public class IMEITest extends TestClass {

    public IMEITest(Messenger messenger, Activity activity, int cycles) {
        super(messenger, activity, cycles);
        this.id = BISTApplication.IMEITest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
    }

    @Override
    public boolean classSetup() {
        String status = MySystemProperties.get("gsm.version.baseband", "Unknown");
        String imei = ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        if (status.isEmpty() || status.equals("Unkown") || imei == null || imei.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean testBegin() {
        String status = MySystemProperties.get("gsm.version.baseband", "Unknown");
        String imei = ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        if (status.isEmpty() || status.equals("Unkown") || imei == null || imei.isEmpty()) {
            sendMessage("Failed", true);
            return false;
        }
        sendMessage("FW: " + status, true);
        sendMessage("IMEI: " + imei, true);
        return true;
    }

    public static class MySystemProperties {
        private static final String TAG = "MySystemProperties";

        // String SystemProperties.get(String key){}
        public static String get(String key) {
            init();

            String value = null;

            try {
                value = (String) mGetMethod.invoke(mClassType, key);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return value;
        }

        //int SystemProperties.get(String key, int def){}
        public static int getInt(String key, int def) {
            init();

            int value = def;
            try {
                Integer v = (Integer) mGetIntMethod.invoke(mClassType, key, def);
                value = v.intValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return value;
        }

        public static String get(String key, String def) {
            init();

            String value = null;

            try {
                value = (String) mGetStringMethod.invoke(mClassType, key, def);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return value;

        }

//        public static int getSdkVersion() {
//            return getInt("ro.build.version.sdk", -1);
//        }

        //-------------------------------------------------------------------
        private static Class<?> mClassType = null;
        private static Method mGetMethod = null;
        private static Method mGetIntMethod = null;
        private static Method mGetStringMethod = null;

        private static void init() {
            try {
                if (mClassType == null) {
                    mClassType = Class.forName("android.os.SystemProperties");

                    mGetMethod = mClassType.getDeclaredMethod("get", String.class);
                    mGetStringMethod = mClassType.getDeclaredMethod("get", String.class, String.class);
                    mGetIntMethod = mClassType.getDeclaredMethod("getInt", String.class, int.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
