package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.R;
import it.casaricci.controllino.data.RecordInfo;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;


/**
 * Server configuration editor.
 * @author Daniele Ricci
 */
public class ServerEditor extends ListActivity {

    public static final String EXTRA_SERVER_ID = "it.casaricci.controllino.serverId";

    public static final int RESULT_DELETED = RESULT_FIRST_USER;

    /** The server editor adapter. */
    private ServerEditorAdapter mAdapter;
    /** Service Id - if any. */
    private long mServerId;
    /** Dirty server flag. */
    private boolean mDirty;
    /** Configuration instance. */
    private Configuration mConfig;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_editor);

        mConfig = Configuration.getInstance(this);
        List<RecordInfo> list = new ArrayList<RecordInfo>();

        Intent i = getIntent();
        mServerId = i.getLongExtra(EXTRA_SERVER_ID, 0);

        if (mServerId > 0) {
            // load server
            Bundle profileData = new Bundle();
            Cursor c = mConfig.getServer(mServerId, profileData);
            c.moveToNext();
            list.add(new RecordInfo("name", c.getString(2), R.string.server_field_name));
            list.add(new RecordInfo("profile_id", profileData.getString("name"),
                R.string.server_field_profile, profileData.getLong("id"), RecordInfo.TYPE_SERVER_PROFILE));
            list.add(new RecordInfo("address", c.getString(3), R.string.server_field_address, RecordInfo.TYPE_ADDRESS_URL));
            list.add(new RecordInfo("port", c.getString(4), R.string.server_field_port, RecordInfo.TYPE_NUMBER));
            list.add(new RecordInfo("username", c.getString(5), R.string.server_field_username, RecordInfo.TYPE_USERNAME));
            list.add(new RecordInfo("password", c.getString(6), R.string.server_field_password, RecordInfo.TYPE_PASSWORD));
            c.close();
        }
        else {
            list.add(new RecordInfo("name", "New server", R.string.server_field_name));
            list.add(new RecordInfo("profile_id", null, R.string.server_field_profile, RecordInfo.TYPE_SERVER_PROFILE));
            list.add(new RecordInfo("address", "ssh.example.com", R.string.server_field_address, RecordInfo.TYPE_ADDRESS_URL));
            list.add(new RecordInfo("port", "22", R.string.server_field_port, RecordInfo.TYPE_NUMBER));
            list.add(new RecordInfo("username", "root", R.string.server_field_username, RecordInfo.TYPE_USERNAME));
            list.add(new RecordInfo("password", "root", R.string.server_field_password, RecordInfo.TYPE_PASSWORD));
        }

        // editor adapter
        mAdapter = new ServerEditorAdapter
            (this, R.layout.preference, android.R.id.title, android.R.id.summary, list);

        setListAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.server_editor_menu, menu);
        if (mServerId <= 0) {
            MenuItem i = menu.findItem(R.id.menu_delete_server);
            i.setVisible(false);
        }
        else {
            MenuItem i = menu.findItem(R.id.menu_discard_server);
            // TODO i18n
            i.setTitle("Discard changes");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.menu_delete_server:
                // delete server (upon confirmation)
                delete();
                return true;

            case R.id.menu_discard_server:
                // discard server
                end(RESULT_CANCELED, false, true);
                finish();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        end(RESULT_OK, true, false);
        super.onBackPressed();
    }

    private void end(int resultCode, boolean save, boolean ignoreDirty) {
        if (ignoreDirty ? true : mDirty) {
            if (save)
                save();
            setResult(resultCode);
        }
    }

    private void delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setTitle("Delete server")
            .setMessage("Server will be deleted.")
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mConfig.removeServer(mServerId);
                    end(RESULT_DELETED, false, true);
                    finish();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show();
    }

    /** Saves the server the user is editing. */
    private void save() {
        // retrieve records from adapter
        RecordInfo name = mAdapter.getItem(0);
        RecordInfo profile = mAdapter.getItem(1);
        RecordInfo host = mAdapter.getItem(2);
        RecordInfo port = mAdapter.getItem(3);
        RecordInfo username = mAdapter.getItem(4);
        RecordInfo password = mAdapter.getItem(5);

        // existing server
        if (mServerId > 0) {
            mConfig.updateServer(mServerId,
                name.getData(), host.getData(), Integer.parseInt(port.getData()),
                username.getData(), password.getData(), profile.getDataId());
        }
        // new server
        else {
            mServerId = mConfig.addServer
                (name.getData(), host.getData(), Integer.parseInt(port.getData()),
                    username.getData(), password.getData(), profile.getDataId());
        }
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        Object item = list.getItemAtPosition(position);
        if (item instanceof RecordInfo)
            editRecord((RecordInfo) item);
    }

    private void editRecord(final RecordInfo info) {
        mDirty = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(info.getResourceId());

        if (info.getType() == RecordInfo.TYPE_SERVER_PROFILE) {
            Cursor c = mConfig.getProfiles();
            final String[] items = new String[c.getCount()];
            final long[] itemsId = new long[items.length];
            int i = 0;
            while (c.moveToNext()) {
                itemsId[i] = c.getLong(0);
                items[i] = c.getString(1);
                i++;
            }
            c.close();

            builder
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        info.setData(items[which]);
                        info.setDataId(itemsId[which]);
                        // invalidate ListView
                        mAdapter.notifyDataSetChanged();
                    }
                });

        }
        else {
            LayoutInflater inflater = getLayoutInflater();
            final View view = inflater.inflate(R.layout.edittext_dialog, null);
            final EditText txt = (EditText) view.findViewById(R.id.textinput);

            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == Dialog.BUTTON_POSITIVE) {
                        String text = txt.getText().toString();

                        info.setData(text.trim());
                        // invalidate ListView
                        mAdapter.notifyDataSetChanged();
                    }
                }
            };
            builder
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, null)
                .setView(view);

            switch (info.getType()) {
                case RecordInfo.TYPE_TEXT:
                    txt.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                    break;
                case RecordInfo.TYPE_ADDRESS_URL:
                    txt.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                    break;
                case RecordInfo.TYPE_USERNAME:
                    txt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    break;
                case RecordInfo.TYPE_PASSWORD:
                    txt.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    break;
                case RecordInfo.TYPE_NUMBER:
                    txt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    break;
            }

            txt.setText(info.getData());
            txt.setSelection(txt.getText().length());
        }

        final Dialog dialog = builder.create();
        dialog.show();
    }

    public static Intent newEditor(Context context) {
        return new Intent(context, ServerEditor.class);
    }

    public static Intent fromServerId(Context context, long serverId) {
        Intent i = new Intent(context, ServerEditor.class);
        i.putExtra(EXTRA_SERVER_ID, serverId);
        return i;
    }

}
