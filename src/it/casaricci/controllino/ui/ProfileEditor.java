package it.casaricci.controllino.ui;

import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServerProfileInfo;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;


/**
 * Server profile editor.
 * @author Daniele Ricci
 */
public class ProfileEditor extends ListActivity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_editor);

        List<ServerProfileInfo> list = new ArrayList<ServerProfileInfo>();

        list.add(new ServerProfileInfo(R.string.profile_meta_name, "New profile"));
        list.add(new ServerProfileInfo(R.string.profile_meta_osname, "Debian"));
        list.add(new ServerProfileInfo(R.string.profile_meta_osversion, "6.0.4"));

        MergeAdapter adapter = new MergeAdapter();

        ProfileEditorMetadataAdapter metadata = new ProfileEditorMetadataAdapter
            (this, R.layout.preference, android.R.id.title, android.R.id.summary, list);
        adapter.addAdapter(metadata);

        TextView v = (TextView) getLayoutInflater().inflate(android.R.layout.preference_category, null, false);
        v.setText("Services");
        adapter.addView(v);

        setListAdapter(adapter);
    }

    public static Intent newEditor(Context context) {
        return new Intent(context, ProfileEditor.class);
    }
}
