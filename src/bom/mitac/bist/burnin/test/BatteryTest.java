package bom.mitac.bist.burnin.test;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Messenger;
import android.os.SystemClock;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;
import bom.mitac.bist.burnin.util.FileManager;
import bom.mitac.bist.burnin.util.Validator;
import android.util.Log;


public class BatteryTest extends TestClass {

    private String Battery_info = "";
    private int battThreshold = 0;
    private int currPercent = 0;

    public BatteryTest(Messenger messenger, Activity activity, int battLevel) {
        super(messenger, activity);
        this.id = BISTApplication.BatteryTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
        battThreshold = battLevel;
    }

    @Override
    public boolean classSetup() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        activity.registerReceiver(mBroadcastReceiver, filter);
        //If current battery percentage is less than the value of current battery, 
        //Don't do test;
        SystemClock.sleep(500);
        if (battThreshold > currPercent) {
            return false;
        }
        return true;
    }

    @Override
    public boolean testBegin() {
        sendMessage(Battery_info, true);
        return true;
    }

    @Override
    public boolean testEnd() {
        return true;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int capacity = intent.getIntExtra("level", 0);
                int voltage = intent.getIntExtra("voltage", 0);
                int temperature = intent.getIntExtra("temperature", 0);
                currPercent = capacity;

                Battery_info = "Capacity:" + String.valueOf(capacity) + "\n"
                        + "Voltage:" + String.valueOf(voltage) + "\n"
                        + "Temperature:" + String.valueOf(temperature) + "\n";
            }
        }
    };
}
