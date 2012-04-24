package it.casaricci.controllino.ui;

import it.casaricci.controllino.R;
import it.casaricci.controllino.data.RecordInfo;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class RecordEditorAdapter extends ArrayAdapter<RecordInfo> {
    protected Context mContext;
    protected int mViewResource;
    protected int mTitleResource;
    protected int mSummaryResource;

    protected LayoutInflater mInflater;

    public RecordEditorAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, List<RecordInfo> objects) {
        super(context, resource, 0, objects);
        init(context, resource, titleResourceId, summaryResourceId);
    }

    public RecordEditorAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, RecordInfo[] objects) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null)
            view = mInflater.inflate(mViewResource, parent, false);
        else
            view = convertView;

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            holder.textTitle = (TextView) view.findViewById(mTitleResource);
            holder.textSummary = (TextView) view.findViewById(mSummaryResource);
            view.setTag(holder);

            // TODO this is valid only for input Dialog records
            LinearLayout widget = (LinearLayout) view.findViewById(android.R.id.widget_frame);
            ImageView icon = (ImageView) mInflater.inflate(R.layout.preference_dialog, widget, false);
            widget.addView(icon);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        RecordInfo item = getItem(position);
        holder.textTitle.setText(mContext.getString(item.getResourceId()));
        holder.textSummary.setText(item.getData());

        return view;
    }

    private static final class ViewHolder {
        public TextView textTitle;
        public TextView textSummary;
    }
}
