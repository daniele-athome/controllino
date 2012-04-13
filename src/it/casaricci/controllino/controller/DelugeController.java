package it.casaricci.controllino.controller;

import it.casaricci.controllino.ConnectorService;
import it.casaricci.controllino.R;


/**
 * Deluge daemon controller.
 * @author Daniele Ricci
 */
public class DelugeController extends SysVInitController {
	private UpdateShellListener mUpdateListener = new UpdateShellListener();

    public DelugeController(ConnectorService connector) {
        super(connector, "deluge-daemon");
    }

    @Override
    public CharSequence getServiceName() {
        return "Deluge torrent daemon";
    }

    @Override
    public CharSequence getServiceDescription() {
        return "Deluge torrent daemon using System V init script.";
    }

    @Override
    public int getServiceIcon() {
        return R.drawable.ic_launcher;
    }

    @Override
    public void update() {
    	update(mUpdateListener, true);
    }

    private final class UpdateShellListener implements ShellExecuteListener {

        @Override
        public void onError(ShellController ctrl, Throwable e) {
            // TODO error handling
            setStatus(Status.STATUS_ERROR);
        }

        @Override
        public void onExecuteFinish(ShellController ctrl, int exitStatus) {
            if (exitStatus == 0)
                setStatus(Status.STATUS_RUNNING);
            else if (exitStatus == 3)
                setStatus(Status.STATUS_STOPPED);
            else
                setStatus(Status.STATUS_UNKNOWN);
        }
    }

    @Override
    public void start() {
        start(new StartStopShellListener(mUpdateListener));
    }

    @Override
    public void stop() {
        stop(new StartStopShellListener(mUpdateListener));
    }

    @Override
    public void restart() {
        restart(new StartStopShellListener(mUpdateListener));
    }

    @Override
    public void reload() {
        reload(new StartStopShellListener(mUpdateListener));
    }

}
