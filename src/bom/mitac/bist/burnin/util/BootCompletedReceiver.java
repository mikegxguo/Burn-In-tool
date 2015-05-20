package bom.mitac.bist.burnin.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import bom.mitac.bist.burnin.activity.MainActivity;


/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-4-17
 * Time: 下午2:09
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private Recorder recorder;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//            SystemClock.sleep(5 * 1000);
//            Intent newIntent = new Intent(context, MainActivity.class);
//            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(newIntent);

            recorder = Recorder.read();
            if (recorder != null && recorder.isTesting) {
                SystemClock.sleep(recorder.rebootDelay * 1000);
                Intent newIntent = new Intent(context, MainActivity.class);
                newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(newIntent);
            }
        }
    }

}
