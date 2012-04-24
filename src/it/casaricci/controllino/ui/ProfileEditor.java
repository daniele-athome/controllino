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
        setContentView(R.layout.profile_editor);

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
            // TODO i18n
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
        // TODO 18n
        v.setText("Services");
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
            // TODO i18n
            i.setTitle("Discard changes");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.menu_add_service:
                // add service from list
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                Cursor c = mConfig.getServices();
                int count = c.getCount();

                if (count <= 0) {
                    // TODO i18n
                    Toast.makeText(this, "No services defined.", Toast.LENGTH_LONG).show();
                }
                else {
                    CharSequence[] items = new CharSequence[c.getCount()];
                    final long[] itemsId = new long[c.getCount()];
                    int i = 0;

                    while (c.moveToNext()) {
                        itemsId[i] = c.getLong(0);
                        items[i] = c.getString(1) + " " + c.getString(2);
                        i++;
                    }
                    c.close();

                    final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            long sid = itemsId[which];

                            // check if chosen service has already been added
                            if (mServicesAdapter.getPosition(new ServiceData(sid)) < 0) {
                                mDirty = true;
                                Cursor c = mConfig.getService(sid);
                                c.moveToNext();
                                mServicesAdapter.add(ServiceData.fromCursor(c));
                                c.close();
                            }
                        }
                    };

                    builder
                        // TODO i18n
                        .setTitle("Add service")
                        .setItems(items, listener);

                    builder.create().show();
                }

                return true;

            case R.id.menu_delete_profile:
                // delete profile (upon confirmation)
                // TODO ask confirmation
                // TODO check if profile is used by some server
                mConfig.removeProfile(mProfileId);
                end(RESULT_DELETED, false, true);
                finish();

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
        super.onBackPressed();
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

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        Object item = list.getItemAtPosition(position);
        if (item instanceof RecordInfo) {
            editMetadata((RecordInfo) item);
        }
        else if (item instanceof ServiceData) {
            // TODO what to do with service??
            // TODO open a popup with more choices, for now just delete it
            mDirty = true;
            mServicesAdapter.remove((ServiceData) item);
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

    public static Intent newEditor(Context context) {
        return new Intent(context, ProfileEditor.class);
    }

    public static Intent fromProfileId(Context context, long profileId) {
        Intent i = new Intent(context, ProfileEditor.class);
        i.putExtra(EXTRA_PROFILE_ID, profileId);
        return i;
    }

}
