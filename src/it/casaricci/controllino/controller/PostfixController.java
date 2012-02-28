package it.casaricci.controllino.controller;

import it.casaricci.controllino.ConnectorService;
import it.casaricci.controllino.R;


/**
 * Postfix server controller.
 * @author Daniele Ricci
 */
public class PostfixController extends SysVInitController {
	private UpdateShellListener mUpdateListener = new UpdateShellListener();

    public PostfixController(ConnectorService connector) {
        super(connector, "postfix");
    }

    @Override
    public CharSequence getServiceName() {
        return "Postfix server";
    }

    @Override
    public CharSequence getServiceDescription() {
        return "Postfix server using System V init script.";
    }

    @Override
    public int getServiceIcon() {
        return R.drawable.ctrl_postfix;
    }

    @Override
    public void update() {
    	update(mUpdateListener, true);
    }

    private final class UpdateShellListener implements ShellExecuteListener {

        @Override
        public void onError(ShellController ctrl, Throwable e) {
            // TODO
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
