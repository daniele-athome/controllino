package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServerProfileData;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Manages configured server profiles (templates).
 * @author Daniele Ricci
 */
public class ProfilesPreferences extends ListActivity {
    public static final int REQUEST_PROFILE_EDITOR = 1;

    private CursorAdapter mAdapter;
    private Configuration mConfig;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs_profiles);
        TextView text = (TextView) findViewById(android.R.id.empty);
        text.setText(Html.fromHtml(getString(R.string.list_profiles_empty)));

        mConfig = Configuration.getInstance(this);
        mAdapter = new ProfilesListAdapter(this,
            R.layout.preference,
            android.R.id.title, android.R.id.summary, null);
        setListAdapter(mAdapter);
        registerForContextMenu(getListView());
    }

    /** No search here. */
    @Override
    public boolean onSearchRequested() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // reload data
        refresh();
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

    private static final int MENU_EDIT = 1;
    private static final int MENU_DELETE = 2;
    private static final int MENU_CLONE = 3;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        ServerProfileData data = (ServerProfileData) mAdapter.getItem(info.position);

        menu.setHeaderTitle(data.getName());
        // TODO i18n
        menu.add(Menu.NONE, MENU_EDIT, MENU_EDIT, "Edit profile");
        menu.add(Menu.NONE, MENU_DELETE, MENU_DELETE, "Delete profile");
        menu.add(Menu.NONE, MENU_CLONE, MENU_CLONE, "Clone profile");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
            .getMenuInfo();
        ServerProfileData data = (ServerProfileData) mAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case MENU_EDIT:
                edit(data);
                return true;

            case MENU_DELETE:
                delete(data.getId());
                return true;

            case MENU_CLONE:
                clone(data);
                return true;
        }

        return super.onContextItemSelected(item);
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
            // onResume will refresh()
        }
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        ServerProfileData item = (ServerProfileData) list.getItemAtPosition(position);
        edit(item);
    }

    private void refresh() {
        Cursor old = mAdapter.getCursor();
        Cursor c = mConfig.getProfiles();
        startManagingCursor(c);
        mAdapter.changeCursor(c);
        if (old != null)
            stopManagingCursor(old);
    }

    private void edit(ServerProfileData item) {
        startActivityForResult(ProfileEditor.fromProfileId(this, item.getId()), REQUEST_PROFILE_EDITOR);
    }

    private void delete(final long id) {
        ProfileEditor.delete(this, id, mConfig, new Runnable() {
            public void run() {
                refresh();
            }
        });
    }

    private void clone(ServerProfileData item) {
        // TODO
    }

}
