package beyarkay.learnt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationTimeRxr extends BroadcastReceiver {
    ArrayList<Group> groupsForNotification = new ArrayList<>();
    ArrayList<Group> weightedGroupsForward = new ArrayList<>();
    ArrayList<Group> weightedGroupsBackward = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        DBHelper db = new DBHelper(context);
        int direction = Integer.parseInt(sharedPref.getString("pref_notification_direction", "-1"));

        /**
         * get the pool of groups from which to select the group for the notification.
         * If a direction hasn't been given, choose one at random,
         *      but make sure there actually are groups in that pool
         *
         */
        if (direction != LearntDB.GroupsTable.LEARNT_FORWARDS && direction != LearntDB.GroupsTable.LEARNT_BACKWARDS) {
            direction = Math.random() > 0.5 ? 1 : 2;
            groupsForNotification = db.getGroupsForNotifications(direction);
            if (groupsForNotification.size() == 0) {
                groupsForNotification = db.getGroupsForNotifications(2 / direction);      // f(x) = 2/x --> f(1)=2 && f(2)=1
            }
        } else {
            groupsForNotification = db.getGroupsForNotifications(direction);
        }
        // FIXME: 2017/07/25 This is all a mess and you can't think straight
//        for (int i = 0; i < groupsForNotification.size(); i++) {
//            Group g = groupsForNotification.get(i);
//            if (direction == LearntDB.GroupsTable.LEARNT_FORWARDS) {
//                for (int j = 0; j < g.getTimesShownF() + 1; j++) {
//                    weightedGroupsForward.addAt(g);
//                }
//            } else if (direction == LearntDB.GroupsTable.LEARNT_BACKWARDS) {
//                for (int j = 0; j < g.getTimesShownB() + 1; j++) {
//                    weightedGroupsBackward.addAt(g);
//                }
//            }
//        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (groupsForNotification.size() > 0 && !sharedPref.getString("pref_notification_frequency", "0").equals("-1")) {
            //choosing a random index somewhere in the first CHUNK_SIZE of groupsForNotification
            int indexOfList = (int) (Math.random() * (Math.min(7, groupsForNotification.size())));
            Intent showGroupI = new Intent(context, ShowGroupIntent.class);
            Intent learnGroupI = new Intent(context, LearntGroupIntent.class);
            Intent openActivityI = new Intent(context, ActivityMacroView.class);
            Group chosenGroup = groupsForNotification.get(indexOfList);
//            while (!groupsForNotification.get(indexOfList).isValid()) {
//                count++;
//                if (count > groupsForNotification.size()) {
//                    log("All the groupsForNotification are invalid");
//                    break;
//                }
//                indexOfList++;
//                if (indexOfList >= groupsForNotification.size()) {
//                    indexOfList = 0;
//                }
//            }
            log("chosen group: " + chosenGroup.toString());

            showGroupI.putExtra(String.valueOf(R.id.TAG_GROUP_ID), chosenGroup.getId());
            PendingIntent showGroupPI = PendingIntent.getService(context, 0, showGroupI, PendingIntent.FLAG_UPDATE_CURRENT);

            openActivityI.putExtra(String.valueOf(R.id.TAG_GROUP_ID), chosenGroup.getId());
            openActivityI.putExtra(String.valueOf(R.id.TAG_ID_TYPE), "group");
            PendingIntent openActivityPI = PendingIntent.getActivity(context, 0, openActivityI, 0);

            String content, title;
            Set set = db.getSet(chosenGroup.getSetId());
            title = set.getTitle() + ":";
            switch (direction) {
                case LearntDB.GroupsTable.LEARNT_FORWARDS:
                    content = set.getDefinitionTitle() + " of: " + chosenGroup.getTerm();
                    learnGroupI.putExtra(String.valueOf(R.id.TAG_LEARNT_STATE), LearntDB.GroupsTable.LEARNT_FORWARDS);
                    break;
                case LearntDB.GroupsTable.LEARNT_BACKWARDS:
                    content = set.getTermTitle() + " of: " + chosenGroup.getDefinition();
                    learnGroupI.putExtra(String.valueOf(R.id.TAG_LEARNT_STATE), LearntDB.GroupsTable.LEARNT_BACKWARDS);
                    break;
                default:
                    content = "error Content";
                    title = "error Title";
            }

            learnGroupI.putExtra(String.valueOf(R.id.TAG_GROUP_ID), chosenGroup.getId());
            learnGroupI.setAction("com.Learnt.delete_group_intent");
            PendingIntent learntGroupPI = PendingIntent.getService(context, 0, learnGroupI, PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                CharSequence name = "Learning";
                String description = "Reminder notifications for displaying the information from your sets";
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel channel = new NotificationChannel("beyarkay.learnt", name, importance);
                channel.setDescription(description);
                // Register the channel with the system
                notificationManager.createNotificationChannel(channel);
            }

            Notification notification;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = new NotificationCompat.Builder(context)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setColor(context.getResources().getColor(R.color.primary))
                    .setOngoing(sharedPref.getString("pref_notification_frequency", "0").equals("0"))
                    .setSmallIcon(R.drawable.ic_status_bar_icon_boarder)
                    .addAction(R.drawable.show_def_button, "Show me", showGroupPI)
                    .addAction(R.drawable.ic_done_white_24dp, "Learnt it", learntGroupPI)
                    .setContentIntent(openActivityPI)
                    .setChannelId("beyarkay.learnt")
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .build();

            notificationManager.notify(0, notification);

        } else {
            notificationManager.cancel(0);
        }
    }

    public void log(String msg) {
        if (ActivityMacroView.shouldLog) {
            Log.i("Learnt.NotifTimeRec____", msg);
        }
    }
}

