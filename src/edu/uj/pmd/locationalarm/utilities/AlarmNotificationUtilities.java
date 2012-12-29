package edu.uj.pmd.locationalarm.utilities;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import edu.uj.pmd.locationalarm.R;
import edu.uj.pmd.locationalarm.activities.AlarmActivity;
import edu.uj.pmd.locationalarm.activities.MainActivity;

/**
 * User: piotrplaneta
 * Date: 26.11.2012
 * Time: 11:36
 */
public class AlarmNotificationUtilities {
    private static Notification notification;
    private static int notificationId = 1234;
    private static boolean alarmIsRunning = false;
    private static double currentDistance = -1;
    private static boolean alarmWentOff = false;


    public static void updateNotification(Context context, String name, double distance) {
        if(currentDistance == -1 || distance != -1) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
            String distanceString;
            if (distance >= 0) {
                distanceString = (float)Math.round(distance)/1000 + " km" + " - " + name;
                currentDistance = (float)Math.round(distance)/1000;

            } else {
                distanceString = context.getString(R.string.unknown);

            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle(context.getText(R.string.notification_title))
                    .setContentText(distanceString)
                    .setSmallIcon(R.drawable.clock)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent);
            notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);
        }
    }

    public static void wakeMeUp(Service service) {
        if(!alarmWentOff) {
            alarmWentOff = true;
            Intent intent = new Intent(service, AlarmActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("noSignal", false);
            service.startActivity(intent);
        }
    }

    public static void removeNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        alarmIsRunning = false;
        alarmWentOff = false;
        currentDistance = -1;
    }

    public static void printStartingToast(Context context) {
        if(!alarmIsRunning) {
            Toast.makeText(context, context.getText(R.string.alarm_started), Toast.LENGTH_SHORT).show();
            alarmIsRunning = true;
        }

    }
}
