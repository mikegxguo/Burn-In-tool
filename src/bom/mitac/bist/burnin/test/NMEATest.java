package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Messenger;
import android.os.SystemClock;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;


import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-27
 * Time: 下午3:07
 */
public class NMEATest extends TestClass {

    private int currentTime;
    private int targetTime;
    protected LocationManager locationManager;

    public NMEATest(Messenger messenger, Activity activity, int targetTime) {
        super(messenger, activity);
        this.id = BISTApplication.GPSTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");

        this.targetTime = targetTime;
    }

    @Override
    public boolean classSetup() {
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (GPSTest.isOPen(activity)) {
            return locationManager != null;
        } else {
            GPSTest.switchGPS(activity);
            return locationManager != null && GPSTest.isOPen(activity);
        }
    }

    @Override
    public boolean testBegin() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                locationManager.addNmeaListener(nmeaListener);
            }
        });
        currentTime = 0;
        while (currentTime < targetTime) {
            if (isStopped) {
                return false;
            }
            SystemClock.sleep(1000);
        }
        return true;
    }

    @Override
    public boolean testCleanup() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationManager.removeNmeaListener(nmeaListener);
                locationManager.removeUpdates(locationListener);
            }
        });
        return true;
    }

    private GpsStatus.NmeaListener nmeaListener = new GpsStatus.NmeaListener() {

        @Override
        public void onNmeaReceived(long timestamp, String nmea) {
            if (nmea == null || nmea == "" || nmea.length() <= 6)
                return;
            if (currentTime < targetTime) {
                sendMessage("GPS NMEA: " + nmea, true);
                currentTime++;
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
}
