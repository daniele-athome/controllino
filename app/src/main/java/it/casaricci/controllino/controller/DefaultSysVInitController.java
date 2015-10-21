package it.casaricci.controllino.controller;

import it.casaricci.controllino.ConnectorService.ConnectorInterface;
import it.casaricci.controllino.data.ServiceData;


/**
 * A {@link SysVInitController} backed by a {@link ServiceData} service
 * definition. Not using a service type "sysvinit" will throw an exception.
 * @author Daniele Ricci
 */
public class DefaultSysVInitController extends SysVInitController {
    private final ServiceData mService;
    private UpdateShellListener mUpdateListener = new UpdateShellListener();
    private StartStopShellListener mStartStopListener = new StartStopShellListener(mUpdateListener);

    public DefaultSysVInitController(ConnectorInterface connector, ServiceData service) {
        super(connector, service.getCommand());
        if (!"sysvinit".equals(service.getType()))
            throw new IllegalArgumentException("not a Sys V init service.");
        mService = service;
    }

    @Override
    public CharSequence getServiceName() {
        return mService.getName();
    }

    @Override
    public CharSequence getServiceDescription() {
        return mService.getType() + ": " + mService.getCommand();
    }

    @Override
    public int getServiceIcon() {
        return ServiceData.getIconDrawable(mService.getIcon());
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
        public void onExecuteFinish(ShellController ctrl, int exitStatus, byte[] output) {
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
        start(mStartStopListener);
    }

    @Override
    public void stop() {
        stop(mStartStopListener);
    }

    @Override
    public void restart() {
        restart(mStartStopListener);
    }

    @Override
    public void reload() {
        reload(mStartStopListener);
    }

}
