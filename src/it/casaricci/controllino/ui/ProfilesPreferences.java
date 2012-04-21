package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.R;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;


/**
 * Manages configured server profiles (templates).
 * @author Daniele Ricci
 */
public class ProfilesPreferences extends ListActivity {
    private ListAdapter mAdapter;

    /** Called when the activity is first created. */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs_profiles);

        Cursor c = Configuration.getInstance(this).getProfiles();
        startManagingCursor(c);

        mAdapter = new SimpleCursorAdapter(this,
            android.R.layout.simple_list_item_2, c,
            new String[] { "name", "os_name" },
            new int[] { android.R.id.text1, android.R.id.text2 });
        setListAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profiles_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_profile:
                startActivity(ProfileEditor.newEditor(this));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
