package it.casaricci.controllino.ui;

import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServerProfileInfo;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileEditorMetadataAdapter extends ArrayAdapter<ServerProfileInfo> {
    private Context mContext;
    private int mViewResource;
    private int mTitleResource;
    private int mSummaryResource;

    private LayoutInflater mInflater;

    public ProfileEditorMetadataAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, List<ServerProfileInfo> objects) {
        super(context, resource, 0, objects);
        init(context, resource, titleResourceId, summaryResourceId);
    }

    public ProfileEditorMetadataAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, ServerProfileInfo[] objects) {
        super(context, 0, 0, objects);
        init(context, resource, titleResourceId, summaryResourceId);
    }

    private void init(Context context, int resource, int titleResourceId, int summaryResourceId) {
        mViewResource = resource;
        mTitleResource = titleResourceId;
        mSummaryResource = summaryResourceId;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ProfileEditorMetadataAdapter(Context context,
        int textViewResourceId, List<ServerProfileInfo> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null)
            view = mInflater.inflate(mViewResource, parent, false);
        else
            view = convertView;

        TextView textTitle = (TextView) view.findViewById(mTitleResource);
        TextView textSummary = (TextView) view.findViewById(mSummaryResource);
        if (convertView == null)  {
            LinearLayout widget = (LinearLayout) view.findViewById(android.R.id.widget_frame);
            ImageView icon = (ImageView) mInflater.inflate(R.layout.preference_dialog, widget, false);
            widget.addView(icon);
        }

        ServerProfileInfo item = getItem(position);
        textTitle.setText(mContext.getString(item.getResourceId()));
        textSummary.setText(item.getData());

        return view;
    }
}
