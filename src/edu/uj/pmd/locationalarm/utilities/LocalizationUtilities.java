package edu.uj.pmd.locationalarm.utilities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.widget.Toast;
import edu.uj.pmd.locationalarm.services.CheckLastLocationTimeService;
import edu.uj.pmd.locationalarm.services.GpsLocalizationService;
import edu.uj.pmd.locationalarm.services.NetworkLocalizationService;
import edu.uj.pmd.locationalarm.R;

import java.util.Calendar;
import java.util.List;

public class LocalizationUtilities {
	private static final int ONE_MINUTE = 1000 * 60 * 1;
    private static Location currentBestLocation = null;
    private static boolean isNoLocationAlarmRunning = false;
    private static boolean isNetworkLocationAlarmScheduled = false;
    private static boolean isGpsLocationAlarmScheduled = false;
    private static PendingIntent checkLastLocationTimeIntent;
    private static PendingIntent networkLocalizationServicePendingIntent;
    private static PendingIntent gpsLocalizationServicePendingIntent;

    public static Location getCurrentBestLocation() {
        return currentBestLocation;
    }

    public static void setCurrentBestLocation(Location currentBestLocation) {
        LocalizationUtilities.currentBestLocation = currentBestLocation;
    }

    public static boolean isBetterLocation(Location location) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
	    boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than one minute since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

    public static void setNoLocationAlarm(Activity activity) {
        if(!isNoLocationAlarmRunning) {
            isNoLocationAlarmRunning = true;
            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(activity, CheckLastLocationTimeService.class);
            checkLastLocationTimeIntent = PendingIntent.getService(activity, 0, intent, 0);

            Calendar time = Calendar.getInstance();
            time.add(Calendar.SECOND, 60);

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), 1 * 60 * 1000, checkLastLocationTimeIntent);
        }

    }

    public static void removeNoLocationAlarm(Activity activity) {
        isNoLocationAlarmRunning = false;
        currentBestLocation = null;
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(checkLastLocationTimeIntent);
        CheckLastLocationTimeService.setLastUpdate(-1);
    }

    public static void checkLocalizationServices(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        if(!providers.contains("network")) {
            Toast.makeText(context, R.string.noNetworkProvider, Toast.LENGTH_LONG).show();
        }
        if(!providers.contains("gps")) {
            Toast.makeText(context, R.string.noGpsProvider, Toast.LENGTH_LONG).show();
        }
    }

    public static void setLocalizationServices(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getAllProviders();
        if(providers.contains("network")) {
            LocalizationUtilities.setNetworkLocalizationService(context);
        }
        if(providers.contains("gps")) {
            LocalizationUtilities.setGpsLocalizationService(context);
        }
    }

    public static void setNetworkLocalizationService(Context context) {
        if(!isNetworkLocationAlarmScheduled) {
            isNetworkLocationAlarmScheduled = true;
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NetworkLocalizationService.class);
            networkLocalizationServicePendingIntent = PendingIntent.getService(context, 0, intent, 0);

            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 30 * 1000,
                    networkLocalizationServicePendingIntent);
        } else {
            if(!NetworkLocalizationService.isRunning) {
                Intent intent = new Intent(context, NetworkLocalizationService.class);
                context.startService(intent);
            }
        }
    }

    public static void setGpsLocalizationService(Context context) {
        if (!isGpsLocationAlarmScheduled) {
            isGpsLocationAlarmScheduled = true;
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, GpsLocalizationService.class);
            gpsLocalizationServicePendingIntent = PendingIntent.getService(context, 0, intent, 0);

            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 4 * 60 * 1000,
                    gpsLocalizationServicePendingIntent);
        } else {
            if(!GpsLocalizationService.isRunning) {
                Intent intent = new Intent(context, GpsLocalizationService.class);
                context.startService(intent);
            }
        }
    }

    public static void stopNetworkLocalizationService(Context context) {
        if (NetworkLocalizationService.isRunning) {
            context.stopService(new Intent(context, NetworkLocalizationService.class));
        }
    }

    public static void stopGpsLocalizationService(Context context) {
        if (GpsLocalizationService.isRunning) {
            context.stopService(new Intent(context, GpsLocalizationService.class));
        }
    }

    public static void cancelNetworkLocalizationService(Context context) {
        isNetworkLocationAlarmScheduled = false;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(networkLocalizationServicePendingIntent);
        stopNetworkLocalizationService(context);
    }

    public static void cancelGpsLocalizationService(Context context) {
        isGpsLocationAlarmScheduled = false;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(gpsLocalizationServicePendingIntent);
        stopGpsLocalizationService(context);
    }

}
