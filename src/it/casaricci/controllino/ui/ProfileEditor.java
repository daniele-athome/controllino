package it.casaricci.controllino.ui;

import it.casaricci.controllino.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;


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

        // TODO
        Object[] list = new Object[] {
            "Name",
            "OS name",
            "OS version"
        };

        // TODO MergeAdapter :)
        ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(this, android.R.layout.simple_list_item_2, android.R.id.text1, list);
        setListAdapter(adapter);
    }

    public static Intent newEditor(Context context) {
        return new Intent(context, ProfileEditor.class);
    }
}
