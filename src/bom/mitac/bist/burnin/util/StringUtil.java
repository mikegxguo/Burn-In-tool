package bom.mitac.bist.burnin.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-5-9
 * Time: 上午10:46
 */
public class StringUtil {
    public static List<Integer> indexOf(String ab, String b) {
        List<Integer> list = new ArrayList<Integer>();
        int cutIndex = 0;
        while (true) {
            int index = ab.indexOf(b);
            if (index == -1)
                break;
            list.add(index + cutIndex);
            ab = ab.substring(index + b.length());
            cutIndex = index + cutIndex + b.length();
        }
        return list;
    }
}
