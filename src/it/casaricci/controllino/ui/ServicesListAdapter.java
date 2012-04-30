package it.casaricci.controllino.ui;

import it.casaricci.controllino.data.ServiceData;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class ServicesListAdapter extends BaseCursorListAdapter {
    private int mIconResourceId;

    public ServicesListAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, int iconResourceId, Cursor cursor) {
        super(context, resource, titleResourceId, summaryResourceId, cursor);
        mIconResourceId = iconResourceId;
    }

    @Override
    public ServiceData getItem(int position) {
        Cursor c = (Cursor) super.getItem(position);
        return (c != null) ? ServiceData.fromCursor(c) : null;
    }

    @Override
    public void createView(ViewHolder holder, View view, Context context, Cursor cursor, ViewGroup parent) {
        View icon = view.findViewById(mIconResourceId);
        holder.viewOthers = new View[] { icon };
    }

    @Override
    public void bindData(BaseCursorListAdapter.ViewHolder holder, Context context, Cursor cursor) {
        ServiceData item = ServiceData.fromCursor(cursor);
        holder.textTitle.setText(item.getName() + " " + item.getVersion());

        // service type
        CharSequence displayType;
        int stringId = ServiceData.getTypeString(item.getType());
        if (stringId > 0)
            displayType = context.getString(stringId);
        else
            displayType = item.getType();

        holder.textSummary.setText(displayType);

        // service icon
        int iconId = ServiceData.getIconDrawable(item.getIcon());
        if (iconId > 0) {
            holder.viewOthers[0].setVisibility(View.VISIBLE);
            ((ImageView) holder.viewOthers[0]).setImageResource(iconId);
        }
        else {
            holder.viewOthers[0].setVisibility(View.GONE);
        }
    }

}
