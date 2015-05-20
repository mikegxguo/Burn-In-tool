package bom.mitac.bist.burnin.test;

import java.io.File;

import android.app.Activity;
import android.os.Messenger;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.WindowManager;
import bom.mitac.bist.burnin.activity.CameraActivity;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;
import bom.mitac.bist.burnin.util.ScreenShot;


public class BKLTest extends TestClass {

    int oldBright;
    int defaultBright;

    public BKLTest(Messenger messenger, Activity activity) {
        super(messenger, activity);
        this.id = BISTApplication.BKLTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
    }

    public void setBrightness(final float brightness) {
        final WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        Log.d("MyApp", "My" + brightness);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lp.screenBrightness = brightness;
                activity.getWindow().setAttributes(lp);

            }
        });

    }

    public int GetSystemBrightness() {
        try {
            // Return the setting's current value
            return Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException snfe) {
            Log.e("Backlight Test", snfe.getMessage());
            return -1;
        }
    }

    @Override
    public boolean classSetup() {
        oldBright = GetSystemBrightness();
        defaultBright = oldBright;
        return true;
    }

    @Override
    public boolean testBegin() {
        sendMessage("Backlight test start!", true);
        
//        File testdir = new File ("/mnt/sdcard/BIST/PIC/screen1.png");
//        Log.d("error","test dir ="+testdir);
//        ScreenShot.shoot(activity, testdir);
//        SystemClock.sleep(5000);
        
        while (true) {
            if (isStopped) {
                break;
            }
            for (int j = 0; j <= 256; j++) {
                if (isStopped) {
                    break;
                }
                if (oldBright - j >= 0) {
                    setBrightness((oldBright - j) / 255.0f);
                    if ((oldBright - j) % 50 == 0)
                        sendMessage("Current back light is:" + (oldBright - j), true);
                    try {
                        Thread.currentThread();
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    oldBright = 0;
                    break;
                }
            }
            for (int j = 0; j <= 256; j++) {
                if (isStopped) {
                    break;
                }
                if (oldBright + j <= 255) {
                    setBrightness((oldBright + j) / 255.0f);
                    if ((oldBright + j) % 50 == 0)
                        sendMessage("Current back light is:" + (oldBright + j), true);
                    try {
                        Thread.currentThread();
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    oldBright = 255;
                    break;
                }
            }

        }
        return true;
    }

    @Override
    public boolean testEnd() {
        setBrightness(defaultBright / 255.0f);
        return true;
    }

}