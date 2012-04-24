package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServiceData;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Manages configured system services.
 * @author Daniele Ricci
 */
public class ServicesPreferences extends ListActivity {
    public static final int REQUEST_SERVICE_EDITOR = 1;

    private CursorAdapter mAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs_services);
        TextView text = (TextView) findViewById(android.R.id.empty);
        text.setText(Html.fromHtml(getString(R.string.list_services_empty)));

        Cursor c = Configuration.getInstance(this).getServices();
        startManagingCursor(c);

        mAdapter = new ServicesListAdapter(this,
            R.layout.preference_icon,
            android.R.id.title, android.R.id.summary, android.R.id.icon, c);
        setListAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.services_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_service:
                newService();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SERVICE_EDITOR) {
            if (resultCode == RESULT_OK) {
                // TODO i18n
                Toast.makeText(this, "Service saved.", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == ServiceEditor.RESULT_DELETED) {
                // TODO i18n
                Toast.makeText(this, "Service deleted.", Toast.LENGTH_SHORT).show();
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        ServiceData item = (ServiceData) list.getItemAtPosition(position);
        startActivityForResult(ServiceEditor.fromServiceId(this, item.getId()), REQUEST_SERVICE_EDITOR);
    }

    /** Builtin Service templates. */
    private static final String[][] serviceTemplates = {
        { "Apache HTTP server", "2.x", "sysvinit", "apache2", "ctrl_apache" },
        { "MySQL server", "5.x", "sysvinit", "mysql", "ctrl_mysql" }
    };

    public void newService() {
        CharSequence[] items = new CharSequence[serviceTemplates.length + 1];
        // TODO i18n
        items[0] = "No template";
        for (int i = 0; i < serviceTemplates.length; i++)
            items[i + 1] = serviceTemplates[i][0];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                if (which > 0) {
                    String[] tmpl = serviceTemplates[which - 1];
                    intent = ServiceEditor.newEditor(ServicesPreferences.this,
                        tmpl[0], tmpl[1], tmpl[2], tmpl[3], tmpl[4]);
                }
                else {
                    intent = ServiceEditor.newEditor(ServicesPreferences.this);
                }

                // start service editor
                startActivityForResult(intent, REQUEST_SERVICE_EDITOR);
            }
        };

        builder
            // TODO i18n
            .setTitle("Choose a template")
            .setItems(items, listener);

        builder.create().show();

    }

}
