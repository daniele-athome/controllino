package it.casaricci.controllino.ui;

import it.casaricci.controllino.R;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;


/**
 * The main preferences window.
 * @author Daniele Ricci
 */
public class MainPreferences extends PreferenceActivity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_main);

        PreferenceScreen pServices = (PreferenceScreen) getPreferenceScreen()
            .findPreference("pref_services");
        final Intent pServicesIntent = new Intent(this, ServicesPreferences.class);

        pServices.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(pServicesIntent);
                return true;
            }
        });

        PreferenceScreen pProfiles = (PreferenceScreen) getPreferenceScreen()
            .findPreference("pref_profiles");
        final Intent pProfilesIntent = new Intent(this, ProfilesPreferences.class);

        pProfiles.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(pProfilesIntent);
                return true;
            }
        });
    }

}
