package it.casaricci.controllino.ui;

import it.casaricci.controllino.data.ServiceData;
import android.content.Context;
import android.database.Cursor;


public class ServicesListAdapter extends BaseCursorListAdapter {

    public ServicesListAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, Cursor cursor) {
        super(context, resource, titleResourceId, summaryResourceId, cursor);
    }

    @Override
    public ServiceData getItem(int position) {
        Cursor c = (Cursor) super.getItem(position);
        return (c != null) ? ServiceData.fromCursor(c) : null;
    }

    @Override
    public void bindData(BaseCursorListAdapter.ViewHolder holder, Context context, Cursor cursor) {
        ServiceData item = ServiceData.fromCursor(cursor);
        holder.textTitle.setText(item.getName());
        holder.textSummary.setText(item.getVersion());
    }

}
