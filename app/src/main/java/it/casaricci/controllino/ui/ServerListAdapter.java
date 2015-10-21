package it.casaricci.controllino.ui;

import it.casaricci.controllino.data.ServerData;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;


public class ServerListAdapter extends BaseCursorListAdapter {

    public ServerListAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, Cursor cursor) {
        super(context, resource, titleResourceId, summaryResourceId, cursor);
    }

    @Override
    public ServerData getItem(int position) {
        Cursor c = (Cursor) super.getItem(position);
        return (c != null) ? ServerData.fromCursor(c) : null;
    }

    @Override
    public void createView(ViewHolder holder, View view, Context context, Cursor cursor, ViewGroup parent) {
    }

    @Override
    public void bindData(BaseCursorListAdapter.ViewHolder holder, Context context, Cursor cursor) {
        ServerData item = ServerData.fromCursor(cursor);
        holder.textTitle.setText(item.getName());
        holder.textSummary.setText(item.getHost());
    }

}
