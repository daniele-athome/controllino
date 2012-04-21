package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.R;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListAdapter;


/**
 * Manages configured system services.
 * @author Daniele Ricci
 */
public class ServicesPreferences extends ListActivity {
    private ListAdapter mAdapter;

    /** Called when the activity is first created. */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs_services);

        Cursor c = Configuration.getInstance(this).getServices();
        startManagingCursor(c);

        mAdapter = new SimpleCursorAdapter(this,
            android.R.layout.simple_list_item_2, c,
            new String[] { "name", "version" },
            new int[] { android.R.id.text1, android.R.id.text2 });
        setListAdapter(mAdapter);
    }

}
