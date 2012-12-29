package edu.uj.pmd.locationalarm.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import edu.uj.pmd.locationalarm.activities.AlarmActivity;
import edu.uj.pmd.locationalarm.utilities.AppPreferences;
import edu.uj.pmd.locationalarm.utilities.LocalizationUtilities;

import java.util.Date;

/**
 * User: piotrplaneta
 * Date: 22.11.2012
 * Time: 11:49
 */
public class CheckLastLocationTimeService extends Service {
    private static long lastUpdate = -1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void setLastUpdate(long lastUpdate) {
        CheckLastLocationTimeService.lastUpdate = lastUpdate;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.PARTIAL_WAKE_LOCK, "lastLocationTime");
        wakeLock.acquire();
        AppPreferences appPreferences = new AppPreferences(getApplicationContext());
        if(LocalizationUtilities.getCurrentBestLocation() == null && lastUpdate == -1) {
            lastUpdate = new Date().getTime();
        }
        if(LocalizationUtilities.getCurrentBestLocation() != null) {
            lastUpdate = LocalizationUtilities.getCurrentBestLocation().getTime();
        }
        Date date = new Date();
        if (date.getTime() - lastUpdate > appPreferences.getNoLocationTime()) {
            Intent alarmIntent = new Intent(this, AlarmActivity.class);
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            alarmIntent.putExtra("noSignal", true);
            this.startActivity(alarmIntent);
        }
        wakeLock.release();
        return START_STICKY;
    }
}
