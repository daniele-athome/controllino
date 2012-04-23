package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.R;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.TextView;


/**
 * Main activity: the servers list.
 * @author Daniele Ricci
 */
public class ServerListActivity extends ListActivity {
    private ListAdapter mAdapter;

    /** Called when the activity is first created. */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_list_screen);
        TextView text = (TextView) findViewById(android.R.id.empty);
        text.setText(Html.fromHtml(getString(R.string.list_services_empty)));

        Cursor c = Configuration.getInstance(this).getServers();
        startManagingCursor(c);

        // TODO ad-hoc CursorAdapter (based on BaseCursorListAdapter)
        mAdapter = new SimpleCursorAdapter(this,
            android.R.layout.simple_list_item_2, c,
            new String[] { "name", "address" },
            new int[] { android.R.id.text1, android.R.id.text2 });
        setListAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.servers_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_server:
                // TODO add server
                return true;

            case R.id.menu_settings:
                startActivity(new Intent(this, MainPreferences.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
