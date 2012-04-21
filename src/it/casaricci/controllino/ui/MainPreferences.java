package it.casaricci.controllino.ui;

import it.casaricci.controllino.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;


public class MainPreferences extends PreferenceActivity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_main);
    }

}
