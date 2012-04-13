package it.casaricci.controllino.controller;

import it.casaricci.controllino.ConnectorService;
import it.casaricci.controllino.R;


/**
 * Apache HTTP server controller.
 * @author Daniele Ricci
 */
public class ApacheHTTPController extends SysVInitController {
	private UpdateShellListener mUpdateListener = new UpdateShellListener();

    public ApacheHTTPController(ConnectorService connector) {
        super(connector, "apache2");
    }

    @Override
    public CharSequence getServiceName() {
        return "Apache HTTP server";
    }

    @Override
    public CharSequence getServiceDescription() {
        return "Apache server using System V init script.";
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
            else if (exitStatus == 1 || exitStatus == 3)
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
