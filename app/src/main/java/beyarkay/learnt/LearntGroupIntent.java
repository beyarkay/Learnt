package beyarkay.learnt;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


public class LearntGroupIntent extends IntentService {
    DBHelper db;
    Handler handler;
    NotificationManager nm;
    SharedPreferences sharedPref;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final long id = intent.getExtras().getLong(String.valueOf(R.id.TAG_GROUP_ID));
        db = new DBHelper(getApplicationContext());
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
        final Group g = db.getGroup(id);

        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        getApplicationContext().sendBroadcast(it);
        handler.post(new Runnable() {
            @Override
            public void run() {
                log("Pressed the 'Learnt This' button on the notification");
                g.setLearntState(g.getLearntState() + intent.getExtras().getInt(String.valueOf(R.id.TAG_LEARNT_STATE)));
                db.updateGroup(g, id);

                Intent updateGroupsI = new Intent();
                updateGroupsI.setAction(getString(R.string.group_deleted_intent));
//                groupDeletedI.putExtra("id", id);
                getApplicationContext().sendBroadcast(updateGroupsI);
                if (g.getLearntState() == LearntDB.GroupsTable.LEARNT_FULLY) {
                    Toast.makeText(
                            getApplicationContext(),/* msgs[(int) Math.random() * msgs.length] + */
                            "Pair Completely Learnt!: \n" + g.getTerm() + " ~ " + g.getDefinition(), Toast.LENGTH_LONG).show();

                } else if (intent.getExtras().getInt(String.valueOf(R.id.TAG_LEARNT_STATE)) == LearntDB.GroupsTable.LEARNT_FORWARDS) {
                    Toast.makeText(
                            getApplicationContext(),/* msgs[(int) Math.random() * msgs.length] + */
                            "Pair Learnt: \n" + g.getTerm() + " → " + g.getDefinition(), Toast.LENGTH_LONG).show();

                } else if(intent.getExtras().getInt(String.valueOf(R.id.TAG_LEARNT_STATE)) == LearntDB.GroupsTable.LEARNT_BACKWARDS) {
                    Toast.makeText(
                            getApplicationContext(),/* msgs[(int) Math.random() * msgs.length] + */
                            "Pair Learnt: \n" + g.getDefinition() + " → " + g.getTerm(), Toast.LENGTH_LONG).show();
                }
                log("changed learntState: " + g.toString());
                db.close();

                if (sharedPref.getString("pref_notification_frequency", "0").equals("0")) {
                    sendBroadcast(new Intent(getApplicationContext(), NotificationTimeRxr.class));
                }
            }
        });
    }

    public LearntGroupIntent() {
        super("LearntGroupIntent");
    }

    public void log(String msg) {
        if (ActivityMacroView.shouldLog) {
            Log.i("Learnt.LearntIntent", msg);
        }
    }
}