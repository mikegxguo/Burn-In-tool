package bom.mitac.bist.burnin.util;

import android.content.Context;
import com.mitac.cell.device.bcr.McBcrConnection;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 13-11-18
 * Time: 下午7:11
 */
public class BCRManager extends McBcrConnection {
    private static BCRManager instance;

    private BCRManager(Context context) {
        super(context);
    }

    public static BCRManager getInstance(Context context) {
        if (instance == null)
            instance = new BCRManager(context);
        return instance;
    }

    public static String getStatus(Context context){
        if (instance == null)
            instance = new BCRManager(context);
        String status1 = instance.get("must://bcr/decoder/check_fw");
        String status2 = instance.get("must://bcr/decoder/factory_fw");
        return "check_fw: "+ status1+" factory_fw: "+status2;
    }

    public void finish() {
        if (instance != null) {
            instance.stopListening();
            instance = null;
        }
    }

}
