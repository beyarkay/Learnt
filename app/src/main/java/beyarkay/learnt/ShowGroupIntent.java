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

public class ShowGroupIntent extends IntentService {
    Handler handler;
    NotificationManager nm;
    SharedPreferences sharedPref;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        DBHelper db = new DBHelper(getApplicationContext());
        final long id = intent.getLongExtra(String.valueOf(R.id.TAG_GROUP_ID), -1);
        Group g = db.getGroup(id);
        final String toastText = g.getTerm() + " ~ " + g.getDefinition();
        final boolean useSmartDuration = sharedPref.getBoolean("pref_use_smart_duration", true);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        getApplicationContext().sendBroadcast(it);

        handler.post(new Runnable() {
            @Override
            public void run() {

                //3500 is length in millis per Toast.LENGTH_LONG
                int millis_per_char = sharedPref.getInt("pref_millis_per_char", 60);
                int repetitions = (int) Math.ceil(toastText.length() * millis_per_char / 2000.0);
                for (int i = 0; i < repetitions; i++) {
                    Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                }
                if (sharedPref.getString("pref_notification_frequency", "0").equals("0")) {
                    sendBroadcast(new Intent(getApplicationContext(), NotificationTimeRxr.class));
                }
            }
        });
        g.incrementTimesShownF();
        db.updateGroup(g, g.getId());
        log("Incremented Group: " + g.toString());
    }

    public ShowGroupIntent() {
        super("ShowGroupIntent");
    }


    public void log(String msg) {
        Log.i("Learnt.ShowGroupIntent", msg);
    }
}
