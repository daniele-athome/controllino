package it.casaricci.controllino.ui;

import it.casaricci.controllino.ConnectorService;
import it.casaricci.controllino.R;
import it.casaricci.controllino.controller.ApacheHTTPController;
import it.casaricci.controllino.controller.BaseController;
import it.casaricci.controllino.controller.DummyController;
import it.casaricci.controllino.controller.MySQLController;
import it.casaricci.controllino.controller.PostfixController;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class ServiceStatusListAdapter extends BaseAdapter {
	private final LayoutInflater mInflater;

	private ConnectorService mConnector;
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

	public void update(ConnectorService connector) {
		mConnector = connector;

		// TODO
		if (mList.size() == 0) {
    		mList.add(new DummyController(connector));
    		mList.add(new ApacheHTTPController(connector));
    		mList.add(new MySQLController(connector));
    		mList.add(new PostfixController(connector));
		}
		else {
		    for (BaseController ctrl : mList)
                ctrl.update();
		}
		notifyDataSetChanged();
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}
}
