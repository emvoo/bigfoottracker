package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * PreferencesActivity
 */
public class PreferencesActivity extends PreferenceActivity {
    public static final String KEY_PREF_DBSTORE = "dbstore";
    SharedPreferences sharedPref;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        Preference pref = findPreference(KEY_PREF_DBSTORE);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!sharedPref.getBoolean(KEY_PREF_DBSTORE, true)) {
                    Toast.makeText(PreferencesActivity.this, "Any data saved during this session will be removed from database when you close the application.", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }
}
