package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Messenger;
import android.os.SystemClock;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;


import java.io.File;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-19
 * Time: 
 */
public class GPSTest extends TestClass {
    public enum TYPE {
        COLD_START, WARM_START, HOT_START
    }

    protected LocationManager locationManager;
    protected StringBuilder strCnData;
    private TYPE type;
    protected boolean isFixed;
    private int checkTime;
    private float fixedTime;

    public GPSTest(Messenger messenger, Activity activity, TYPE type, int checkTime) {
        super(messenger, activity);
        this.id = BISTApplication.GPSTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");

        this.type = type;
        this.checkTime = checkTime;
    }

    private boolean gpsStart(TYPE type) {

        Bundle bundle = new Bundle();
        switch (type) {
            case HOT_START:
                bundle.putBoolean("rti", true);
                break;
            case WARM_START:
                bundle.putBoolean("ephemeris", true);
                bundle.putBoolean("time", true);
                break;
            case COLD_START:
                bundle = null;
                break;
            default:
                return false;
        }
        boolean result = locationManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "delete_aiding_data", bundle);

        if (result) {
            sendMessage("Send " + type.toString() + " command successfully.", true);
        } else {
            sendMessage("Send " + type.toString() + "  command failed!", true);
            return false;
        }
        for (int i = 0; i < checkTime * 10; i++) {
            if (isFixed) {
                fixedTime = (float) i / 10;
                return true;
            }
            SystemClock.sleep(100);
        }
        return false;
    }

    public static final boolean isOPen(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    public static final void switchGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean classSetup() {
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (isOPen(activity)) {
            return locationManager != null;
        } else {
            switchGPS(activity);
            return locationManager != null && isOPen(activity);
        }
    }

    @Override
    public boolean testSetup() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                locationManager.addNmeaListener(nmeaListener);
                locationManager.addGpsStatusListener(gpsStatusListener);
            }
        });
        return true;
    }

    @Override
    public boolean testBegin() {
        sendMessage("\n" + type.toString(), true);
        if (gpsStart(type)) {
            sendMessage("\n" + fixedTime + "s", true);
        } else {
            sendMessage("\nFailed to fixed", true);
        }
        return isFixed;
    }

    @Override
    public boolean testCleanup() {
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                locationManager.removeGpsStatusListener(gpsStatusListener);
//                locationManager.removeNmeaListener(nmeaListener);
//                locationManager.removeUpdates(locationListener);
//            }
//        });
        return true;
    }

    private GpsStatus.NmeaListener nmeaListener = new GpsStatus.NmeaListener() {
        private boolean trigGPS;

        @Override
        public void onNmeaReceived(long timestamp, String nmea) {
            if (nmea == null || nmea == "" || nmea.length() <= 6)
                return;

            String headStr = nmea.substring(0, 6);
            if (headStr.equals("$GPGGA")) {
                if (!trigGPS) {
                    if (nmea.indexOf("E,1") == -1 && nmea.indexOf("E,2") == -1 && nmea.indexOf("W,1") == -1 && nmea.indexOf("W,2") == -1) {
                        sendMessage("\nstarted", true);
                        trigGPS = true;
                    }
                } else if (!isFixed) {
                    if (nmea.indexOf("E,1") != -1 || nmea.indexOf("E,2") != -1 || nmea.indexOf("W,1") != -1 || nmea.indexOf("W,2") != -1) {
                        sendMessage("\nFixed!!", true);

                        isFixed = true;
                        trigGPS = false;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                locationManager.removeGpsStatusListener(gpsStatusListener);
                                locationManager.removeNmeaListener(nmeaListener);
                                locationManager.removeUpdates(locationListener);
                            }
                        });
                    }
                }
            }
        }
    };

    protected LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    protected GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {

            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    sendMessage("GPS_EVENT_STARTED", true);
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    sendMessage("GPS_EVENT_FIRST_FIX", true);
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    sendMessage("GPS_EVENT_STOPPED", true);
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    if (locationManager == null) {
                        return;
                    }
                    GpsStatus status = locationManager.getGpsStatus(null);
                    int iSateNum = status.getMaxSatellites();
                    if (iSateNum > 0) {
                        Iterable<GpsSatellite> iSatellites = status.getSatellites();
                        Iterator<GpsSatellite> it = iSatellites.iterator();
                        strCnData = new StringBuilder();
                        while (it.hasNext()) {
                            GpsSatellite oSat = it.next();
                            int sv = oSat.getPrn();
                            float cn = oSat.getSnr();
                            strCnData.append("SV:").append(String.valueOf(sv)).append("  CN:").append(String.valueOf(cn))
                                    .append("\r\n");
                        }
                    }
                    break;
            }

        }
    };
}
