package it.casaricci.controllino.controller;

import it.casaricci.controllino.ConnectorService.ConnectorInterface;


/**
 * A standard System V init script controller.
 * @author Daniele Ricci
 */
public abstract class SysVInitController extends ShellController {
    private static final String PREFIX = "/etc/init.d/";
    private String mScriptName;

    /** Generic start/stop {@link ShellExecuteListener}. */
    public final class StartStopShellListener implements ShellExecuteListener {
    	private ShellExecuteListener mUpdateListener;

    	public StartStopShellListener(ShellExecuteListener updateListener) {
    		mUpdateListener = updateListener;
		}

        @Override
        public void onError(ShellController ctrl, Throwable e) {
            // TODO error handling
            setStatus(Status.STATUS_ERROR);
        }

        @Override
        public void onExecuteFinish(ShellController ctrl, int exitStatus, byte[] output) {
            if (exitStatus == 0) {
            	// execution successful, update status
            	notifySuccess();
            	update(mUpdateListener, false);
            }
            else {
            	// launch error, notify the user
            	notifyError(new StartStopException());
            }
        }
    }

    public SysVInitController(ConnectorInterface connector, String name) {
        super(connector);
        mScriptName = name;
    }

    private void executeInitScript(String params) {
        execute(PREFIX + mScriptName + " " + params);
    }

    public void update(ShellExecuteListener listener, boolean setChecking) {
    	if (setChecking)
    		setStatus(Status.STATUS_CHECKING);
        setShellExecuteListener(listener);
        executeInitScript("status");
    }

    public void start(ShellExecuteListener listener) {
        setStatus(Status.STATUS_STARTING);
        setShellExecuteListener(listener);
        executeInitScript("start");
    }

    public void stop(ShellExecuteListener listener) {
        setStatus(Status.STATUS_STOPPING);
        setShellExecuteListener(listener);
        executeInitScript("stop");
    }

    public void restart(ShellExecuteListener listener) {
        setStatus(Status.STATUS_RESTARTING);
        setShellExecuteListener(listener);
        executeInitScript("restart");
    }

    public void reload(ShellExecuteListener listener) {
        setStatus(Status.STATUS_RELOADING);
        setShellExecuteListener(listener);
        executeInitScript("reload");
    }

    public String getScriptType() {
        return "sysvinit";
    }

}
