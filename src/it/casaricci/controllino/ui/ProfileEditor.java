package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.R;
import it.casaricci.controllino.data.RecordInfo;
import it.casaricci.controllino.data.ServiceData;

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
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.merge.MergeAdapter;


/**
 * Server profile editor.
 * @author Daniele Ricci
 */
public class ProfileEditor extends ListActivity {
    public static final int REQUEST_SERVICE_EDITOR = 1;

    public static final String EXTRA_PROFILE_ID = "it.casaricci.controllino.profileId";

    public static final int RESULT_DELETED = RESULT_FIRST_USER;

    /** The profile metadata adapter. */
    private ProfileEditorMetadataAdapter mMetadataAdapter;
    /** Profile services adapter. */
    private ProfileServicesAdapter mServicesAdapter;
    private List<ServiceData> mServicesList;
    /** Profile Id - if any. */
    private long mProfileId;
    /** Dirty profile flag. */
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
        mProfileId = i.getLongExtra(EXTRA_PROFILE_ID, 0);

        if (mProfileId > 0) {
            // load profile metadata
            Cursor c = mConfig.getProfile(mProfileId);
            c.moveToNext();
            list.add(new RecordInfo("name", c.getString(1), R.string.profile_meta_name));
            list.add(new RecordInfo("os_name", c.getString(2), R.string.profile_meta_osname));
            list.add(new RecordInfo("os_version", c.getString(3), R.string.profile_meta_osversion));
            c.close();
        }
        else {
            // TODO i18n default template
            list.add(new RecordInfo("name", "New profile", R.string.profile_meta_name));
            list.add(new RecordInfo("os_name", "Debian", R.string.profile_meta_osname));
            list.add(new RecordInfo("os_version", "6.0.4", R.string.profile_meta_osversion));
        }

        MergeAdapter adapter = new MergeAdapter();

        // metadata adapter
        mMetadataAdapter = new ProfileEditorMetadataAdapter
            (this, R.layout.preference, android.R.id.title, android.R.id.summary, list);
        adapter.addAdapter(mMetadataAdapter);

        // services label
        TextView v = (TextView) getLayoutInflater().inflate(android.R.layout.preference_category, null, false);
        v.setText(R.string.profile_header_services);
        adapter.addView(v);

        // services adapter
        mServicesList = new ArrayList<ServiceData>();

        if (mProfileId > 0) {
            Cursor c = mConfig.getServices(mProfileId);
            while (c.moveToNext())
                mServicesList.add(ServiceData.fromCursor(c));
            c.close();
        }

        mServicesAdapter = new ProfileServicesAdapter(this,
            R.layout.preference_icon, android.R.id.title, android.R.id.summary,
            android.R.id.icon, mServicesList);
        adapter.addAdapter(mServicesAdapter);
        setListAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_editor_menu, menu);
        if (mProfileId <= 0) {
            MenuItem i = menu.findItem(R.id.menu_delete_profile);
            i.setVisible(false);
        }
        else {
            MenuItem i = menu.findItem(R.id.menu_discard_profile);
            i.setTitle(R.string.menu_discard_changes);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.menu_add_services:
                // add services from list
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                Cursor c = mConfig.getServices();
                int count = c.getCount();

                if (count <= 0) {
                    Toast.makeText(this, R.string.msg_no_services_found,
                        Toast.LENGTH_LONG).show();
                }
                else {
                    CharSequence[] items = new CharSequence[c.getCount()];
                    final long[] itemsId = new long[items.length];
                    final boolean[] selected = new boolean[items.length];
                    int i = 0;

                    while (c.moveToNext()) {
                        itemsId[i] = c.getLong(0);
                        items[i] = c.getString(1) + " " + c.getString(2);
                        i++;
                    }
                    c.close();

                    final DialogInterface.OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            selected[which] = isChecked;
                        }
                    };
                    final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // go through all selected and add each one to profile
                            for (int i = 0; i < selected.length; i++) {
                                if (selected[i]) {
                                    long sid = itemsId[i];

                                    // check if chosen service has already been added
                                    if (mServicesAdapter.getPosition(new ServiceData(sid)) < 0) {
                                        mDirty = true;
                                        Cursor c = mConfig.getService(sid);
                                        c.moveToNext();
                                        mServicesAdapter.add(ServiceData.fromCursor(c));
                                        c.close();
                                    }
                                }
                            }
                        }
                    };

                    builder
                        .setTitle(R.string.menu_add_services)
                        .setPositiveButton(android.R.string.ok, clickListener)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setMultiChoiceItems(items, null, listener);

                    builder.create().show();
                }

                return true;

            case R.id.menu_delete_profile:
                // delete profile (upon confirmation)
                delete(this, mProfileId, mConfig, new Runnable() {
                    public void run() {
                        end(RESULT_DELETED, false, true);
                        finish();
                    }
                });
                return true;

            case R.id.menu_discard_profile:
                // discard profile
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
        if (ignoreDirty ? true : mDirty) {
            if (save)
                save();
            setResult(resultCode);
        }
    }

    /** Saves the profile the user is editing. */
    private void save() {
        // retrieve metadata from adapter
        RecordInfo name = mMetadataAdapter.getItem(0);
        RecordInfo osName = mMetadataAdapter.getItem(1);
        RecordInfo osVersion = mMetadataAdapter.getItem(2);

        // existing profile
        if (mProfileId > 0) {
            mConfig.updateProfile(mProfileId,
                name.getData(), osName.getData(), osVersion.getData(),
                ServiceData.toIdList(mServicesList));
        }
        // new profile
        else {
            mProfileId = mConfig.addProfile
                (name.getData(), osName.getData(), osVersion.getData(),
                    ServiceData.toIdList(mServicesList));
        }
    }

    public static void delete(Context context, final long id, final Configuration config, final Runnable action) {
        // check if this profile is being used by a server
        int msgId;
        boolean used = (config.getProfileUsageCount(id) > 0);
        if (used)
            msgId = R.string.msg_profile_delete_used_warn;
        else
            msgId = R.string.msg_profile_delete_confirm;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
            .setTitle("Delete profile")
            .setMessage(msgId);

        if (!used) {
            builder
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        config.removeProfile(id);
                        if (action != null)
                            action.run();
                    }
                });
        }

        builder
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show();
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        Object item = list.getItemAtPosition(position);
        if (item instanceof RecordInfo) {
            editMetadata((RecordInfo) item);
        }
        else if (item instanceof ServiceData) {
            final ServiceData data = (ServiceData) item;
            CharSequence[] items = new CharSequence[] {
                getString(R.string.menu_remove_service),
                getString(R.string.menu_edit_service)
            };

            mDirty = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                .setTitle(data.toString())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            mServicesAdapter.remove(data);
                        }
                        else if (which == 1) {
                            startActivityForResult(ServiceEditor
                                .fromServiceId(ProfileEditor.this, data.getId()),
                                    REQUEST_SERVICE_EDITOR);
                        }
                    }
                })
                .create()
                .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SERVICE_EDITOR) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.msg_service_saved, Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == ServiceEditor.RESULT_DELETED) {
                Toast.makeText(this, R.string.msg_service_deleted, Toast.LENGTH_SHORT).show();
            }
            refreshServices();
        }
    }

    private void editMetadata(final RecordInfo info) {
        mDirty = true;
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
                    mMetadataAdapter.notifyDataSetChanged();
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setTitle(info.getResourceId())
            .setPositiveButton(android.R.string.ok, listener)
            .setNegativeButton(android.R.string.cancel, null)
            .setView(view);

        txt.setText(info.getData());
        txt.setSelection(txt.getText().length());
        txt.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        final Dialog dialog = builder.create();
        dialog.show();
    }

    private void refreshServices() {
        // TODO different things to do if the service exists or is new
    }

    public static Intent newEditor(Context context) {
        return new Intent(context, ProfileEditor.class);
    }

    public static Intent fromProfileId(Context context, long profileId) {
        Intent i = new Intent(context, ProfileEditor.class);
        i.putExtra(EXTRA_PROFILE_ID, profileId);
        return i;
    }

}
