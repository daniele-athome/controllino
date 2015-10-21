package it.casaricci.controllino.ui;

import it.casaricci.controllino.data.RecordInfo;

import java.util.List;

import android.content.Context;


public class ProfileEditorMetadataAdapter extends RecordEditorAdapter {

    public ProfileEditorMetadataAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, List<RecordInfo> objects) {
        super(context, resource, titleResourceId, summaryResourceId, objects);
    }

    public ProfileEditorMetadataAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, RecordInfo[] objects) {
        super(context, resource, titleResourceId, summaryResourceId, objects);
    }
}
