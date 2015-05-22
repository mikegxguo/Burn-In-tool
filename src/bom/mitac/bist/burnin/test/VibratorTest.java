package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.content.Context;
import android.os.Messenger;
import android.os.Vibrator;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;
import bom.mitac.bist.burnin.module.TestClassLongTime;


import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-25
 * Time: 下午2:54
 */
public class VibratorTest extends TestClassLongTime {
    private Vibrator vibrator;
    private long[] time = {500, 500, 500, 500, 500, 500, 500, 500};

    public VibratorTest(Messenger messenger, Activity activity) {
        super(messenger, activity);
        this.id = BISTApplication.VibratorTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
    }

    @Override
    public boolean classSetup() {
        vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        return vibrator != null;
    }

    @Override
    public boolean testBegin() {
        if (vibrator == null)
            return false;
        sendMessage("Start to vibrate", true);
        vibrator.vibrate(time, 0);
        return true;
    }

    @Override
    public boolean testEnd() {
        if (vibrator == null)
            return false;
        sendMessage("Stop vibrating", true);
        vibrator.cancel();
        return true;
    }
}
