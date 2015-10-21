package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.ConnectorService.ConnectorInterface;
import it.casaricci.controllino.R;
import it.casaricci.controllino.controller.BaseController;
import it.casaricci.controllino.controller.ShellController;
import it.casaricci.controllino.data.ServiceData;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class ServiceStatusListAdapter extends BaseAdapter {
	private final LayoutInflater mInflater;

	private ConnectorInterface mConnector;
	private List<BaseController> mList = new ArrayList<BaseController>();

	public ServiceStatusListAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    public View getView(int position, View convertView, ViewGroup parent) {
    	ServiceStatusItem view;
    	if (convertView != null && convertView instanceof ServiceStatusItem) {
    		view = (ServiceStatusItem) convertView;
    	}
    	else {
            view = (ServiceStatusItem) mInflater.inflate(R.layout.status_list_item, parent, false);
    	}

    	view.bind(position, mList.get(position), mConnector);
        return view;
    }

	public void update(ConnectorInterface connector) {
		mConnector = connector;

		if (mList.size() == 0) {
	        // get services from connector's profile
		    Cursor c = Configuration.getInstance(mInflater.getContext()).getServices(connector.profileId);
		    while (c.moveToNext()) {
		        BaseController ctrl = null;
		        ServiceData data = ServiceData.fromCursor(c);
		        ctrl = ShellController.newInstance(connector, data);

		        if (ctrl != null)
		            mList.add(ctrl);
		    }
		    c.close();
		}

	    for (BaseController ctrl : mList)
            ctrl.update();

		notifyDataSetChanged();
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}
}
