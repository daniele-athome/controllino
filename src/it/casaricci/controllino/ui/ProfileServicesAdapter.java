package it.casaricci.controllino.ui;

import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServiceData;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ProfileServicesAdapter extends ArrayAdapter<ServiceData> {
    protected Context mContext;
    protected int mViewResource;
    protected int mTitleResource;
    protected int mSummaryResource;
    protected int mIconResource;

    protected LayoutInflater mInflater;

    public ProfileServicesAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, int iconResourceId, List<ServiceData> objects) {
        super(context, resource, 0, objects);
        init(context, resource, titleResourceId, summaryResourceId, iconResourceId);
    }

    public ProfileServicesAdapter(Context context, int resource,
            int titleResourceId, int summaryResourceId, int iconResourceId, ServiceData[] objects) {
        super(context, 0, 0, objects);
        init(context, resource, titleResourceId, summaryResourceId, iconResourceId);
    }

    private void init(Context context, int resource, int titleResourceId, int summaryResourceId, int iconResourceId) {
        mViewResource = resource;
        mTitleResource = titleResourceId;
        mSummaryResource = summaryResourceId;
        mIconResource = iconResourceId;
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
            holder.imageIcon = (ImageView) view.findViewById(mIconResource);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        ServiceData item = getItem(position);
        holder.textTitle.setText(item.getName() + " " + item.getVersion());
        holder.textSummary.setText(item.getType());

        int iconId = 0;
        try {
            Field _iconId = R.drawable.class.getField(item.getIcon());
            iconId = _iconId.getInt(null);
        }
        catch (Exception e) {
            // ignore
        }

        if (iconId > 0) {
            holder.imageIcon.setVisibility(View.VISIBLE);
            holder.imageIcon.setImageResource(iconId);
        }
        else {
            holder.imageIcon.setVisibility(View.GONE);
        }

        return view;
    }

    private static final class ViewHolder {
        public TextView textTitle;
        public TextView textSummary;
        public ImageView imageIcon;
    }

}
