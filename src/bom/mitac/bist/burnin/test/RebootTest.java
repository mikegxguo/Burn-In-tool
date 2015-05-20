package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.os.Messenger;
import android.os.SystemClock;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;
import bom.mitac.bist.burnin.util.Rebooter;
import bom.mitac.bist.burnin.util.Recorder;



/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-4-28
 * Time: 下午3:23
 */
public class RebootTest extends TestClass {
    private Recorder recorder;

    public RebootTest(Messenger messenger, Activity activity) {
        super(messenger, activity);

        this.id = BISTApplication.RebootTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
    }

    @Override
    public boolean classSetup() {
        return Rebooter.isRebooterInstalled(activity);
    }

    @Override
    public boolean testSetup() {
        recorder = Recorder.getInstance();
        return true;
    }

    @Override
    public boolean testBegin() {
        sendMessage(recorder.strTestCondition, true);
        if (recorder.rebootMoment == 0) {
            isPassed = true;
        } else if (recorder.rebootMoment < 5 * 60 * 1000) {
            isPassed = true;
            sendMessage("Last cycle cost " + recorder.rebootMoment / 1000 + "s", true);
        } else {
            isPassed = false;
            sendMessage("Last cycle cost " + recorder.rebootMoment / 1000 + "s", true);
            sendMessage("More than 5 minutes, mark it failed", true);
        }

        recorder.rebootMoment = System.currentTimeMillis();
        recorder.rebootTimes++;
        recorder.write();

        new Thread(){
            @Override
            public void run() {
                SystemClock.sleep(3000);
                Rebooter.reboot(activity);
            }
        }.start();
        return isPassed;
    }
}
