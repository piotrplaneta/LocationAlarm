package edu.uj.pmd.locationalarm.services;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import edu.uj.pmd.locationalarm.R;
import edu.uj.pmd.locationalarm.utilities.AlarmNotificationUtilities;
import edu.uj.pmd.locationalarm.utilities.AppPreferences;
import edu.uj.pmd.locationalarm.utilities.LocalizationUtilities;

import java.util.Calendar;

/**
 * User: piotrplaneta
 * Date: 19.11.2012
 * Time: 19:38
 */
public class NetworkLocalizationService extends Service {
    private AppPreferences appPrefs;
    private LocationListener locationListener;
    private int locationUpdatesCount = 0;
    private PowerManager.WakeLock wakeLock;
    public static boolean isRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.appPrefs = new AppPreferences(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        startCheckLastNetworkLocationTime();

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.PARTIAL_WAKE_LOCK, "networkProvider");
        this.wakeLock.acquire();

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        this.locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                makeUseOfNewLocation(location);
                stopSelf();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        return START_STICKY;

    }

    private void startCheckLastNetworkLocationTime() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, CheckLastNetworkLocationTimeService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        Calendar time = Calendar.getInstance();
        time.add(Calendar.SECOND, 25);

        alarmManager.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
    }

    private void cancelUpdates() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
        if(wakeLock.isHeld()) {
            wakeLock.release();
        }

    }


    private void makeUseOfNewLocation(Location location) {
        locationUpdatesCount++;
        if(appPrefs.getDestinationRadius() > 0 && LocalizationUtilities.isBetterLocation(location)) {
            LocalizationUtilities.setCurrentBestLocation(location);
            float[] results = new float[1];
            Location.distanceBetween(LocalizationUtilities.getCurrentBestLocation().getLatitude(), LocalizationUtilities.getCurrentBestLocation().getLongitude(), appPrefs.getDestinationLatitude(), appPrefs.getDestinationLongitude(), results);
            AlarmNotificationUtilities.updateNotification(this, (String) getText(R.string.network_update), results[0]);
            if (results[0] < appPrefs.getDestinationRadius()*1000) {
                AlarmNotificationUtilities.wakeMeUp(this);
            }
        }
    }



    @Override
    public void onDestroy() {
        cancelUpdates();
        isRunning = false;
        super.onDestroy();
    }
}