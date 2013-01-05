package edu.uj.pmd.locationalarm.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * User: piotrplaneta
 * Date: 19.11.2012
 * Time: 19:01
 */
public class AppPreferences {
    private static final String APP_SHARED_PREFS = "edu.uj.pmd.locationalarm_preferences";
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    public AppPreferences(Context context)
    {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_WORLD_READABLE);
        this.prefsEditor = appSharedPrefs.edit();
    }

    public Float getDestinationLatitude() {
        return appSharedPrefs.getFloat("destinationLatitude", -1);
    }

    public Float getDestinationLongitude() {
        return appSharedPrefs.getFloat("destinationLongitude", -1);
    }

    public int getDestinationRadius() {
        return appSharedPrefs.getInt("destinationRadius", -1);
    }

    public int getNoLocationTime() {
        return appSharedPrefs.getInt("noLocationTime", 5 * 60 * 1000);
    }

    public void setDestinationLatitude(Float latitude) {
        prefsEditor.putFloat("destinationLatitude", latitude);
        prefsEditor.commit();
    }

    public void setDestinationLongitude(Float longitude) {
        prefsEditor.putFloat("destinationLongitude", longitude);
        prefsEditor.commit();
    }

    public void setDestinationRadius(int radius) {
        prefsEditor.putInt("destinationRadius", radius);
        prefsEditor.commit();
    }

    public void setNoLocationTime(int time) {
        prefsEditor.putInt("noLocationTime", time);
        prefsEditor.commit();
    }

    public void clearDestination() {
        prefsEditor.clear();
        prefsEditor.commit();
    }
}
