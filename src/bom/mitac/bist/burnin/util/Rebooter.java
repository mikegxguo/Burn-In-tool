package bom.mitac.bist.burnin.util;

import bom.mitac.bist.burnin.activity.VideoActivity;
import bom.mitac.bist.burnin.rebootaging.AgingActivity;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-4-17
 * Time: 下午2:37
 */
public class Rebooter {

    public static void reboot(Context context) {
        if (context == null)
            return;

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.example.rebooter", "com.example.rebooter.MyActivity"));
        context.startActivity(intent);
    }

    public static boolean isRebooterInstalled(Context context) {
        try {
            context.getPackageManager().getApplicationInfo("com.example.rebooter", PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

    public static void runRebootAgency(Activity activity) {

        Intent intent = new Intent();
        intent.setClass(activity, AgingActivity.class);
        activity.startActivity(intent);
//        activity.finish();
		
//        Intent intent = new Intent();
//        intent.setComponent(new ComponentName("com.mitac.bist.burnin.rebootaging", "com.mitac.bist.burnin.rebootaging.AgingActivity"));
//        context.startActivity(intent);
    }

    public static boolean isRebootAgencyInstalled(Context context) {
        try {
            context.getPackageManager().getApplicationInfo("jp.casio.vx.util.rebootaging3", PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }



}
