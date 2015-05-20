package bom.mitac.bist.burnin.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-5-7
 * Time: 下午5:45
 */
public class SystemInformation {
    public static int getSKUID() {
        String[] CAT = {"cat", "/sys/sys_info/sku_id" };
        String temp = CommandManager.run_command(CAT, "/system/bin").replaceAll("\\D", "");
        if(temp.equals("")){
        	temp = "0";
        }
        int skuid = Integer.valueOf(temp);
        return skuid;
    }

    public static String getIMAGE() {
        return android.os.Build.DISPLAY;
    }

    public static String getToolVersion(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String version = null;
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static String getRunnintTime() {
        String[] CAT = {"cat", "/proc/uptime" };
        return CommandManager.run_command(CAT, "/system/bin").substring(0, 20);
    }
}
