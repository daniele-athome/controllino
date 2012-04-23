package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServerProfileData;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;


/**
 * Manages configured server profiles (templates).
 * @author Daniele Ricci
 */
public class ProfilesPreferences extends ListActivity {
    public static final int REQUEST_PROFILE_EDITOR = 1;

    private CursorAdapter mAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs_profiles);

        Cursor c = Configuration.getInstance(this).getProfiles();
        startManagingCursor(c);

        mAdapter = new ProfilesListAdapter(this,
            R.layout.preference,
            android.R.id.title, android.R.id.summary, c);
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
                startActivityForResult(ProfileEditor.newEditor(this), REQUEST_PROFILE_EDITOR);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PROFILE_EDITOR) {
            if (resultCode == RESULT_OK) {
                // TODO i18n
                Toast.makeText(this, "Profile saved.", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == ProfileEditor.RESULT_DELETED) {
                // TODO i18n
                Toast.makeText(this, "Profile deleted.", Toast.LENGTH_SHORT).show();
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        ServerProfileData item = (ServerProfileData) list.getItemAtPosition(position);
        startActivityForResult(ProfileEditor.fromProfileId(this, item.getId()), REQUEST_PROFILE_EDITOR);
    }

}
