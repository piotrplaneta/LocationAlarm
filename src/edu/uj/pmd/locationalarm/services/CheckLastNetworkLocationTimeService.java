package edu.uj.pmd.locationalarm.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import edu.uj.pmd.locationalarm.utilities.LocalizationUtilities;

/**
 * User: piotrplaneta
 * Date: 26.11.2012
 * Time: 18:52
 */
public class CheckLastNetworkLocationTimeService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (NetworkLocalizationService.isRunning) {
            LocalizationUtilities.stopNetworkLocalizationService(this);
        }
        return START_STICKY;
    }
}
