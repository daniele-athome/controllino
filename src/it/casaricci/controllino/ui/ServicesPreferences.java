package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.R;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CursorAdapter;


/**
 * Manages configured system services.
 * @author Daniele Ricci
 */
public class ServicesPreferences extends ListActivity {
    private CursorAdapter mAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs_services);

        Cursor c = Configuration.getInstance(this).getServices();
        startManagingCursor(c);

        mAdapter = new ServicesListAdapter(this,
            R.layout.preference,
            android.R.id.title, android.R.id.summary, c);
        setListAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.services_menu, menu);
        return true;
    }

}
