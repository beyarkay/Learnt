package beyarkay.learnt;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Objects;


public class PrefFragment extends android.preference.PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        bindPreferenceSummaryToValue(findPreference("pref_notification_frequency"));
        bindPreferenceSummaryToValue(findPreference("pref_notification_direction"));
        bindPreferenceSummaryToValue(findPreference("pref_notification_prompt"));
//        bindPreferenceSummaryToValue(findPreference("pref_term_header"));
//        bindPreferenceSummaryToValue(findPreference("pref_definition_header"));
    }

private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0 ? listPreference.getEntries()[index] : null);

        }
//        else if (preference instanceof RingtonePreference) {
//            // For ringtone preferences, look up the correct display value
//            // using RingtoneManager.
//            if (TextUtils.isEmpty(stringValue)) {
//                // Empty values correspond to 'silent' (no ringtone).
//                preference.setSummary(R.string.pref_ringtone_silent);
//
//            } else {
//                Ringtone ringtone = RingtoneManager.getRingtone(
//                        preference.getContext(), Uri.parse(stringValue));
//
//                if (ringtone == null) {
//                    // Clear the summary if there was a lookup error.
//                    preference.setSummary(null);
//                } else {
//                    // Set the summary to reflect the new ringtone display
//                    // name.
//                    String name = ringtone.getTitle(preference.getContext());
//                    preference.setSummary(name);
//                }
//            }
//
//        }
        else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }
};

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        Object pm;
        try {
            pm = sharedPref.getString(preference.getKey(), "");
        } catch (ClassCastException cce) {
            pm = sharedPref.getInt(preference.getKey(), 0);
        }
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, pm);
    }
}