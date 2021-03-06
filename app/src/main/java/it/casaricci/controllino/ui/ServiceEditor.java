package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.R;
import it.casaricci.controllino.controller.ShellController;
import it.casaricci.controllino.data.RecordInfo;
import it.casaricci.controllino.data.ServiceData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * System service editor.
 * @author Daniele Ricci
 */
public class ServiceEditor extends ListActivity {

    public static final String EXTRA_SERVICE_ID = "it.casaricci.controllino.serviceId";
    public static final String EXTRA_SERVICE_NAME = "it.casaricci.controllino.service.name";
    public static final String EXTRA_SERVICE_VERSION = "it.casaricci.controllino.service.version";
    public static final String EXTRA_SERVICE_TYPE = "it.casaricci.controllino.service.type";
    public static final String EXTRA_SERVICE_COMMAND = "it.casaricci.controllino.service.command";
    public static final String EXTRA_SERVICE_ICON = "it.casaricci.controllino.service.icon";
    public static final String EXTRA_NEW_DIRTY = "it.casaricci.controllino.service.newDirty";

    public static final int RESULT_DELETED = RESULT_FIRST_USER;

    private static final String DEFAULT_SCRIPT_TYPE = "sysvinit";

    /** The service editor adapter. */
    private ServiceEditorAdapter mAdapter;
    /** Service Id - if any. */
    private long mServiceId;
    /** Icon resource name - if any. */
    private String mIconResId;
    /** Dirty service flag. */
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
        mServiceId = i.getLongExtra(EXTRA_SERVICE_ID, 0);

        if (mServiceId > 0) {
            // load service
            Cursor c = mConfig.getService(mServiceId);
            c.moveToNext();

            int selected = 0;
            String type = c.getString(3);
            for (int dx = 0; dx < scriptTypes.length; dx++) {
                if (scriptTypes[dx].equals(type)) {
                    selected = dx;
                    break;
                }
            }

            list.add(new RecordInfo("name", c.getString(1), R.string.service_field_name));
            list.add(new RecordInfo("version", c.getString(2), R.string.service_field_version));
            list.add(new RecordInfo("type", type, R.string.service_field_type, selected, RecordInfo.TYPE_SCRIPT_TYPE));
            list.add(new RecordInfo("command", c.getString(4), R.string.service_field_command));
            mIconResId = c.getString(5);
            c.close();
        }
        else {
            String name = i.getStringExtra(EXTRA_SERVICE_NAME);
            String version = i.getStringExtra(EXTRA_SERVICE_VERSION);
            String type = i.getStringExtra(EXTRA_SERVICE_TYPE);
            String command = i.getStringExtra(EXTRA_SERVICE_COMMAND);

            // something was sent through a template, mark as dirty
            if (name != null || version != null || type != null || command != null)
                mDirty = i.getBooleanExtra(EXTRA_NEW_DIRTY, false);

            if (name == null) name = getString(R.string.tmpl_service_name);
            if (version == null) version = getString(R.string.tmpl_service_version);
            if (type == null) type = DEFAULT_SCRIPT_TYPE;
            if (command == null) command = "initscript";

            mIconResId = i.getStringExtra(EXTRA_SERVICE_ICON);

            list.add(new RecordInfo("name", name, R.string.service_field_name));
            list.add(new RecordInfo("version", version, R.string.service_field_version));
            list.add(new RecordInfo("type", type, R.string.service_field_type, RecordInfo.TYPE_SCRIPT_TYPE));
            list.add(new RecordInfo("command", command, R.string.service_field_command));
        }

        // editor adapter
        mAdapter = new ServiceEditorAdapter
            (this, R.layout.preference, android.R.id.title, android.R.id.summary, list);

        setListAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.service_editor_menu, menu);
        if (mServiceId <= 0) {
            MenuItem i = menu.findItem(R.id.menu_delete_service);
            i.setVisible(false);
        }
        else {
            MenuItem i = menu.findItem(R.id.menu_discard_service);
            i.setTitle(R.string.menu_discard_changes);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.menu_delete_service:
                // delete service (upon confirmation)
                delete(this, mServiceId, mConfig, new Runnable() {
                    public void run() {
                        end(RESULT_DELETED, false, true);
                        finish();
                    }
                });
                return true;

            case R.id.menu_discard_service:
                // discard service
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
        finish();
    }

    private void end(int resultCode, boolean save, boolean ignoreDirty) {
        if (ignoreDirty || mDirty) {
            if (save)
                save();
            setResult(resultCode);
        }
    }

    public static void delete(Context context, final long id, final Configuration config, final Runnable action) {
        // we ask confirmation anyhow, but we need to change the message if
        // this service is being used by a profile
        int msgId;
        if (config.getServiceUsageCount(id) > 0)
            msgId = R.string.msg_service_delete_used_warn;
        else
            msgId = R.string.msg_service_delete_confirm;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
            .setTitle(R.string.menu_delete_service)
            .setMessage(msgId)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    config.removeService(id);
                    if (action != null)
                        action.run();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show();
    }

    /** Saves the service the user is editing. */
    private void save() {
        // retrieve records from adapter
        RecordInfo name = mAdapter.getItem(0);
        RecordInfo version = mAdapter.getItem(1);
        RecordInfo type = mAdapter.getItem(2);
        RecordInfo command = mAdapter.getItem(3);

        // existing service
        if (mServiceId > 0) {
            mConfig.updateService(mServiceId,
                name.getData(), version.getData(), type.getData(),
                    command.getData(), mIconResId);
        }
        // new service
        else {
            mServiceId = mConfig.addService
                (name.getData(), version.getData(), type.getData(),
                    command.getData(), mIconResId);
        }
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        Object item = list.getItemAtPosition(position);
        if (item instanceof RecordInfo)
            editRecord((RecordInfo) item);
    }

    private final static String[] scriptTypes = new String[ShellController.scriptTypes.size()];
    private static String[] scriptTypesNames;
    static {
        int i = 0;
        for (Map.Entry<String, Class<? extends ShellController>> entry :
            ShellController.scriptTypes.entrySet()) {
            scriptTypes[i++] = entry.getKey();
        }
    }

    private void editRecord(final RecordInfo info) {
        mDirty = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setTitle(info.getResourceId());

        if (info.getType() == RecordInfo.TYPE_SCRIPT_TYPE) {
            if (scriptTypesNames == null) {
                scriptTypesNames = new String[scriptTypes.length];
                int i = 0;
                for (Map.Entry<String, Class<? extends ShellController>> entry :
                            ShellController.scriptTypes.entrySet()) {

                    int stringId = ServiceData.getTypeString(entry.getKey());
                    if (stringId > 0)
                        scriptTypesNames[i] = getString(stringId);
                    else
                        scriptTypesNames[i] = entry.getKey();

                    i++;
                }
            }

            int selected = (info.getDataId() >= 0) ? (int) info.getDataId() : 0;
            builder
                .setSingleChoiceItems(scriptTypesNames, selected,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        info.setDataId(which);
                        info.setData(scriptTypes[which]);
                        // invalidate ListView
                        mAdapter.notifyDataSetChanged();
                        dialog.dismiss();
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

            txt.setText(info.getData());
            txt.setSelection(txt.getText().length());
            txt.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        }

        final Dialog dialog = builder.create();
        dialog.show();
    }

    public static Intent newEditor(Context context) {
        return new Intent(context, ServiceEditor.class);
    }

    public static Intent newEditor(Context context, String name, String version,
            String type, String command, String icon, boolean newDirty) {
        return newEditor(context)
            .putExtra(EXTRA_SERVICE_NAME, name)
            .putExtra(EXTRA_SERVICE_VERSION, version)
            .putExtra(EXTRA_SERVICE_TYPE, type)
            .putExtra(EXTRA_SERVICE_COMMAND, command)
            .putExtra(EXTRA_SERVICE_ICON, icon)
            .putExtra(EXTRA_NEW_DIRTY, newDirty);
    }

    public static Intent fromServiceId(Context context, long serviceId) {
        Intent i = new Intent(context, ServiceEditor.class);
        i.putExtra(EXTRA_SERVICE_ID, serviceId);
        return i;
    }

}
