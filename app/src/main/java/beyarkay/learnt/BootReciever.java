package beyarkay.learnt;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class BootReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Intent sendNotificationI = new Intent(context, NotificationTimeRxr.class);
        PendingIntent sendNotificationPI = PendingIntent.getBroadcast(context, 2, sendNotificationI, PendingIntent.FLAG_UPDATE_CURRENT);
        String frequency = sharedPref.getString("pref_notification_frequency", "0");

        if (!frequency.equals("0")) {
            //set up an alarmManager to do a recurring notification
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (frequency.equals("-1")) {
                //if the user says 'never'(-1) then we want to check every minute if they have changed their minds
                frequency = "60";
            }
            int frequency_millis = Integer.parseInt(frequency) * 1000;
//            log("Notification Freq=" + (frequency));
            am.setRepeating(AlarmManager.RTC_WAKEUP, 5000, frequency_millis, sendNotificationPI);
        } else {
            //send a straight order for a notification (the notif's buttons call the same notification over and over)
            context.sendBroadcast(sendNotificationI);
        }
    }
}