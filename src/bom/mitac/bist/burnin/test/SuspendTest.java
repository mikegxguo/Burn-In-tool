package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.SystemClock;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClassLongTime;
import bom.mitac.bist.burnin.module.TestFactory;
import bom.mitac.bist.burnin.util.LockScreenAdmin;


import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-19
 * Time: 下午5:06
 */
public class SuspendTest extends TestClassLongTime {
    private static final String ALARM = "alarm";

    private int suspendTime;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    private boolean isSuspended;
    private boolean isResumed;
    private boolean isAlarmed;
    private long startTime;
    private long stopTime;

    public SuspendTest(Messenger messenger, Activity activity, int suspendTime) {
        super(messenger, activity);
        this.id = BISTApplication.SuspendTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");

        this.suspendTime = (suspendTime * 1000)-5000;
    }

    public void registerAlarm() {
        if (alarmManager == null || suspendTime == 0 || pendingIntent == null) {
            return;
        }
        sendMessage("Register Alarm", true);
        sendMessage("suspendTime: "+suspendTime, true);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + suspendTime, pendingIntent);
    }

    public void unregisterAlarm() {
        if (alarmManager == null || pendingIntent == null) {
            return;
        }
        sendMessage("Unregister Alarm", true);
        alarmManager.cancel(pendingIntent);
    }

    // Suspend and resume
    public void lockScreen() {
        sendMessage("Lock Screen", true);
        isSuspended = false;

        // lock the Screen
        DevicePolicyManager localDevicePolicyManager = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName localComponentName = new ComponentName(activity, LockScreenAdmin.class);
        if (localDevicePolicyManager.isAdminActive(localComponentName)) {
            localDevicePolicyManager.lockNow();
        }
    }

    // Suspend and resume
    public void wakeup() {
        if (isResumed)
            return;

        sendMessage("Wake Up", true);
        // Shine the screen
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        @SuppressWarnings("deprecation")
        PowerManager.WakeLock mWakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Bright");
        mWakelock.acquire();
        mWakelock.release();
    }

    private boolean activeManage() {
        // Launch permission panel
        ComponentName componentName = new ComponentName(activity, LockScreenAdmin.class);
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!devicePolicyManager.isAdminActive(componentName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            // List of permission
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);

            // Explanation
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Lock Screen");

            activity.startActivity(intent);

            for (int i = 0; i < 100; i++) {
                SystemClock.sleep(100);
                if (devicePolicyManager.isAdminActive(componentName)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                sendMessage("ACTION_SCREEN_OFF", true);
                isSuspended = true;
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                sendMessage("ACTION_SCREEN_ON", true);
                // SCREEN_ON will occurred before SCREEN_OFF at the first time of Suspend test.
                // It's strange, but this failure record can be avoided by the follow way .
                if (isSuspended) {
                    isResumed = true;
                    stopTime = System.currentTimeMillis();
                }
//                isPassed = isAlarmed && isSuspended && isResumed;
            } else if (action.equals(ALARM)) {
                sendMessage("ALARM", true);
                isAlarmed = true;
                wakeup();
            }
        }
    };

    @Override
    public boolean classSetup() {
        IntentFilter intentFilter = new IntentFilter(ALARM);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        activity.registerReceiver(broadcastReceiver, intentFilter);
        alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(activity, 0, new Intent(ALARM), PendingIntent.FLAG_UPDATE_CURRENT);
        return alarmManager != null && pendingIntent != null && activeManage();
    }

    @Override
    public boolean testBegin() {
//        if (!VideoTest.isVideoTestPassed) {
//            sendMessage("Camera or Video crashed, skip suspend time in case of test blocking", true);
//            return false;
//        }
        isAlarmed = isSuspended = isResumed = false;
        startTime = System.currentTimeMillis();
        stopTime = 0;
        registerAlarm();
        lockScreen();
        return true;
    }

    @Override
    public boolean testEnd() {
        for (int i = 0; i < 10; i++) {
            if (isResumed) {
                break;
            } else {
                SystemClock.sleep(1000);
                if (i % 3 == 2) {
                    wakeup();
                }
            }
        }
        unregisterAlarm();

        if (stopTime == 0)
            stopTime = System.currentTimeMillis();
        long sleepTime = stopTime - startTime;
        if (!isSuspended || !isResumed || sleepTime < (suspendTime - 5000) || sleepTime > (suspendTime + 5000)) {
            isPassed = false;
        } else {
            isPassed = true;
        }
        return true;
    }
}
