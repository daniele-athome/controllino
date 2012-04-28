package it.casaricci.controllino.ui;

import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServiceData;

import java.lang.reflect.Field;

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
        int stringId = 0;
        CharSequence displayType;
        try {
            Field _stringId = R.string.class.getField("script_" + item.getType());
            stringId = _stringId.getInt(null);
        }
        catch (Exception e) {
            // ignore
        }

        if (stringId > 0)
            displayType = context.getString(stringId);
        else
            displayType = item.getType();

        holder.textSummary.setText(displayType);

        // service icon
        int iconId = 0;
        try {
            Field _iconId = R.drawable.class.getField(item.getIcon());
            iconId = _iconId.getInt(null);
        }
        catch (Exception e) {
            // ignore
        }

        if (iconId > 0) {
            holder.viewOthers[0].setVisibility(View.VISIBLE);
            ((ImageView) holder.viewOthers[0]).setImageResource(iconId);
        }
        else {
            holder.viewOthers[0].setVisibility(View.GONE);
        }
    }

}
