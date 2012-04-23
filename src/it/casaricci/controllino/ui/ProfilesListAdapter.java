package it.casaricci.controllino.ui;

import it.casaricci.controllino.data.ServerProfileData;
import android.content.Context;
import android.database.Cursor;


public class ProfilesListAdapter extends BaseCursorListAdapter {

    public ProfilesListAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, Cursor cursor) {
        super(context, resource, titleResourceId, summaryResourceId, cursor);
    }

    @Override
    public ServerProfileData getItem(int position) {
        Cursor c = (Cursor) super.getItem(position);
        return (c != null) ? ServerProfileData.fromCursor(c) : null;
    }

    @Override
    public void bindData(BaseCursorListAdapter.ViewHolder holder, Context context, Cursor cursor) {
        ServerProfileData item = ServerProfileData.fromCursor(cursor);
        holder.textTitle.setText(item.getName());
        holder.textSummary.setText(item.getOsName() + " " + item.getOsVersion());
    }

}
