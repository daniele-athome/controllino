package it.casaricci.controllino.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;


/**
 * A simple generic {@link CursorAdapter}.
 * @author Daniele Ricci
 */
public abstract class BaseCursorListAdapter extends CursorAdapter {
    protected int mViewResource;
    protected int mTitleResource;
    protected int mSummaryResource;

    protected LayoutInflater mInflater;

    public BaseCursorListAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, Cursor cursor) {
        super(context, cursor);
        init(context, resource, titleResourceId, summaryResourceId);
    }

    private void init(Context context, int resource, int titleResourceId, int summaryResourceId) {
        mViewResource = resource;
        mTitleResource = titleResourceId;
        mSummaryResource = summaryResourceId;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(mViewResource, parent, false);

        ViewHolder holder = new ViewHolder();
        holder.textTitle = (TextView) view.findViewById(mTitleResource);
        holder.textSummary = (TextView) view.findViewById(mSummaryResource);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        bindData(holder, context, cursor);
    }

    public abstract void bindData(ViewHolder holder, Context context, Cursor cursor);

    protected final static class ViewHolder {
        public TextView textTitle;
        public TextView textSummary;
    }

}
