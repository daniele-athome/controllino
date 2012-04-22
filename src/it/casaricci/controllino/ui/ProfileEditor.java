package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServerProfileInfo;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;


/**
 * Server profile editor.
 * @author Daniele Ricci
 */
public class ProfileEditor extends ListActivity {
    private ListAdapter mServicesAdapter;
    /** Profile Id - if any. */
    private long mProfileId;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_editor);

        List<ServerProfileInfo> list = new ArrayList<ServerProfileInfo>();

        Intent i = getIntent();
        mProfileId = i.getLongExtra("it.casaricci.controllino.profileId", 0);

        if (mProfileId > 0) {
            // load profile metadata
            Cursor c = Configuration.getInstance(this).getProfile(mProfileId);
            list.add(new ServerProfileInfo(R.string.profile_meta_name, c.getString(1)));
            list.add(new ServerProfileInfo(R.string.profile_meta_osname, c.getString(2)));
            list.add(new ServerProfileInfo(R.string.profile_meta_osversion, c.getString(3)));
        }
        else {
            // TODO i18n
            list.add(new ServerProfileInfo(R.string.profile_meta_name, "New profile"));
            list.add(new ServerProfileInfo(R.string.profile_meta_osname, "Debian"));
            list.add(new ServerProfileInfo(R.string.profile_meta_osversion, "6.0.4"));
        }

        MergeAdapter adapter = new MergeAdapter();

        // metadata adapter
        ProfileEditorMetadataAdapter metadata = new ProfileEditorMetadataAdapter
            (this, R.layout.preference, android.R.id.title, android.R.id.summary, list);
        adapter.addAdapter(metadata);

        // services label
        TextView v = (TextView) getLayoutInflater().inflate(android.R.layout.preference_category, null, false);
        // TODO 18n
        v.setText("Services");
        adapter.addView(v);

        // services adapter
        if (mProfileId > 0) {
            Cursor c = Configuration.getInstance(this).getServices(mProfileId);
            startManagingCursor(c);

            mServicesAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, c,
                new String[] { "name", "version" },
                new int[] { android.R.id.text1, android.R.id.text2 });
            adapter.addAdapter(mServicesAdapter);
        }
        // TODO services adapter backed by array
        else {
            // TODO
        }

        setListAdapter(adapter);
    }

    public static Intent newEditor(Context context) {
        return new Intent(context, ProfileEditor.class);
    }
}
