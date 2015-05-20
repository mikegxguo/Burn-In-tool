/**
 * @Title SystemTime.java
 * @Package com.feong.util.java
 * @author Alex feong@live.com
 * @date 2013-11-4 1:27:52pm
 * @version V1.0
 */
package bom.mitac.bist.burnin.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Remove the Locale.getDefault() parameter @2013-12-9 16:30:33
 *
 * V 1.0
 * 2014-3-12 18:26:05
 * remove DateFormat from android package
 *
 */
public class TimeStamp {

    public static enum TimeType {
        FULL_L_TYPE, FULL_M_TYPE, FULL_S_TYPE,
        TINY_TYPE_DATE, TINY_TYPE_TIME,
        UTC_DATE
    }

    /**
     * To return the string of current system time
     *
     * @param type Use TimeType.FULL_TYPE to return yyyy-MM-dd HH:mm:ss format, use TimeType.SIMPLE_TYPE to return yyyyMMdd_HHmmss format
     * @return Current system time
     */
    public static String getTimeStamp(TimeType type) {
        switch (type) {

            case FULL_L_TYPE:
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
            case FULL_M_TYPE:
                return new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis());
            case FULL_S_TYPE:
                return new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis());
            case TINY_TYPE_DATE:
                return new SimpleDateFormat("MMddyy").format(System.currentTimeMillis());
            case TINY_TYPE_TIME:
                return new SimpleDateFormat("HHmmss").format(System.currentTimeMillis());
            case UTC_DATE:
                Calendar cal = Calendar.getInstance(Locale.CHINA);
                int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
                int dstOffset = cal.get(Calendar.DST_OFFSET);
                cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
                return new SimpleDateFormat("kkmmss").format(cal);
            default:
                return null;


//			case FULL_TYPE :
//				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
//			case SIMPLE_TYPE :
//				return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
//            case TINY_TYPE :
//                return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(System.currentTimeMillis());
//			default :
//				return "";
        }
    }
}
