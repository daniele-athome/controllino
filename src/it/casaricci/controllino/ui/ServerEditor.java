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
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


/**
 * Server configuration editor.
 * @author Daniele Ricci
 */
public class ServerEditor extends ListActivity {

    public static final String EXTRA_SERVER_ID = "it.casaricci.controllino.serverId";

    public static final int RESULT_DELETED = RESULT_FIRST_USER;

    private static final String DEFAULT_HOST = "ssh.example.com";
    private static final String DEFAULT_PORT = "22";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";

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
            list.add(new RecordInfo("name", getString(R.string.tmpl_server_name), R.string.server_field_name));
            list.add(new RecordInfo("profile_id", null, R.string.server_field_profile, RecordInfo.TYPE_SERVER_PROFILE));
            list.add(new RecordInfo("address", DEFAULT_HOST, R.string.server_field_address, RecordInfo.TYPE_ADDRESS_URL));
            list.add(new RecordInfo("port", DEFAULT_PORT, R.string.server_field_port, RecordInfo.TYPE_NUMBER));
            list.add(new RecordInfo("username", DEFAULT_USERNAME, R.string.server_field_username, RecordInfo.TYPE_USERNAME));
            list.add(new RecordInfo("password", DEFAULT_PASSWORD, R.string.server_field_password, RecordInfo.TYPE_PASSWORD));
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
            i.setTitle(R.string.menu_discard_changes);
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
        if (end(RESULT_OK, true, false))
            finish();
    }

    private boolean end(int resultCode, boolean save, boolean ignoreDirty) {
        boolean exit = true;
        if (ignoreDirty ? true : mDirty) {
            if (save)
                exit = save();
            setResult(resultCode);
        }

        return exit;
    }

    private void delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setTitle(R.string.menu_delete_server)
            .setMessage(R.string.msg_server_delete_confirm)
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

    /**
     * Saves the server the user is editing.
     * @return true if we can leave the activity, false to stop it.
     */
    private boolean save() {
        // retrieve records from adapter
        RecordInfo name = mAdapter.getItem(0);
        RecordInfo profile = mAdapter.getItem(1);
        RecordInfo host = mAdapter.getItem(2);
        RecordInfo port = mAdapter.getItem(3);
        RecordInfo username = mAdapter.getItem(4);
        RecordInfo password = mAdapter.getItem(5);

        // no profile selected - warn the user about that
        if (profile.getDataId() <= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                .setTitle(R.string.prefs_server_editor)
                .setMessage(R.string.msg_server_no_profile_selected)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mServerId > 0)
                            mConfig.removeServer(mServerId);
                        end(RESULT_DELETED, false, true);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();

            return false;
        }

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

        return true;
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
            int count = c.getCount();
            if (count <= 0) {
                Toast.makeText(this, R.string.msg_no_profiles_found,
                    Toast.LENGTH_LONG).show();
                return;
            }

            final String[] items = new String[count];
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
                    txt.setTransformationMethod(PasswordTransformationMethod.getInstance());
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
