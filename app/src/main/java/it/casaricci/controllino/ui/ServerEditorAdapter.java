package it.casaricci.controllino.ui;

import it.casaricci.controllino.data.RecordInfo;

import java.util.List;

import android.content.Context;


/**
 * Adapter for {@link ServerEditor}.
 * @author Daniele Ricci
 */
public class ServerEditorAdapter extends RecordEditorAdapter {

    public ServerEditorAdapter(Context context, int resource,
        int titleResourceId, int summaryResourceId, List<RecordInfo> objects) {
        super(context, resource, titleResourceId, summaryResourceId, objects);
    }

    public ServerEditorAdapter(Context context, int resource,
        int titleResourceId, int summaryResourceId, RecordInfo[] objects) {
        super(context, resource, titleResourceId, summaryResourceId, objects);
    }

}
