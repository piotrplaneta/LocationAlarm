package edu.uj.pmd.locationalarm.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import edu.uj.pmd.locationalarm.activities.AlarmActivity;

/**
 * User: piotrplaneta
 * Date: 29.12.2012
 * Time: 13:08
 */
public class BatteryStateReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.PARTIAL_WAKE_LOCK, "batteryStateReceiver");
        wakeLock.acquire();
        boolean batteryLow = intent.getAction().equals(Intent.ACTION_BATTERY_LOW);

        if(batteryLow) {
                Intent alarmIntent = new Intent(context, AlarmActivity.class);
                alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                alarmIntent.putExtra("lowBattery", true);
                context.startActivity(alarmIntent);
        }
        wakeLock.release();
    }
}
