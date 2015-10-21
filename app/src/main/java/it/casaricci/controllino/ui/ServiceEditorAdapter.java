package it.casaricci.controllino.ui;

import it.casaricci.controllino.data.RecordInfo;

import java.util.List;

import android.content.Context;


/**
 * Adapter for {@link ServiceEditor}.
 * @author Daniele Ricci
 */
public class ServiceEditorAdapter extends RecordEditorAdapter {

    public ServiceEditorAdapter(Context context, int resource,
        int titleResourceId, int summaryResourceId, List<RecordInfo> objects) {
        super(context, resource, titleResourceId, summaryResourceId, objects);
    }

    public ServiceEditorAdapter(Context context, int resource,
        int titleResourceId, int summaryResourceId, RecordInfo[] objects) {
        super(context, resource, titleResourceId, summaryResourceId, objects);
    }

}
