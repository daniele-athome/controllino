package it.casaricci.controllino.controller;

import it.casaricci.controllino.ConnectorService;


/**
 * The basic controller interface.
 * @author Daniele Ricci
 */
public abstract class BaseController {

	/** Process status. */
	public enum Status {
	    STATUS_CHECKING,
		STATUS_UNKNOWN,
		STATUS_RUNNING,
		STATUS_STARTING,
		STATUS_STOPPED,
		STATUS_STOPPING,
		STATUS_RESTARTING,
		STATUS_RELOADING,
		STATUS_ERROR
	}

	protected ConnectorService mConnector;
	protected Status mStatus = Status.STATUS_CHECKING;

    protected StatusChangedListener mStatusChangedListener;
    protected ResultListener mResultListener;

    protected boolean mDirty;

	public BaseController(ConnectorService connector) {
	    mConnector = connector;
	    // mark as dirty immediately
	    mDirty = true;
    }

	/** Returns the service name. */
	public abstract CharSequence getServiceName();

	/** Returns the service description. */
	public abstract CharSequence getServiceDescription();

	/** Returns the service icon resource. */
	public abstract int getServiceIcon();

	/** Returns the status of the process monitored by this controller. */
	public Status getStatus() {
	    return mStatus;
	}

    protected void setStatus(Status status) {
        setStatus(status, true);
    }

	protected void setStatus(Status status, boolean fireListener) {
	    mStatus = status;
	    // status is now set, mark as clean
	    mDirty = false;
	    if (fireListener)
	        statusChanged();
	}

	public void setResultListener(ResultListener listener) {
		mResultListener = listener;
	}

    public boolean isDirty() {
        return mDirty;
    }

	protected void notifyError(Throwable e) {
		if (mResultListener != null)
			mResultListener.onError(this, e);
	}

	protected void notifySuccess() {
		if (mResultListener != null)
			mResultListener.onSuccess(this);
	}

	public interface ResultListener {
		public void onSuccess(BaseController ctrl);
		public void onError(BaseController ctrl, Throwable e);
	}

	/** Updates the status of this controller. */
	public abstract void update();

	public void setStatusChangedListener(StatusChangedListener listener) {
	    mStatusChangedListener = listener;
	}

	/** Fires a status changed event. */
	public void statusChanged() {
	    if (mStatusChangedListener != null)
	        mStatusChangedListener.onStatusChanged(this);
	}

	public interface StatusChangedListener {
	    public void onStatusChanged(BaseController ctrl);
	}

	/** Starts the service. */
	public abstract void start();

	/** Stops the service. */
	public abstract void stop();

	/** Restarts the service. */
	public abstract void restart();

	/** Reloads the service. */
	public abstract void reload();
}
