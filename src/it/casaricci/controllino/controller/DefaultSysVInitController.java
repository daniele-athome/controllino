package it.casaricci.controllino.controller;

import it.casaricci.controllino.ConnectorService;
import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServiceData;

import java.lang.reflect.Field;


/**
 * A {@link SysVInitController} backed by a {@link ServiceData} service
 * definition. Not using a service type "sysvinit" will throw an exception.
 * @author Daniele Ricci
 */
public class DefaultSysVInitController extends SysVInitController {
    private final ServiceData mService;
    private UpdateShellListener mUpdateListener = new UpdateShellListener();

    public DefaultSysVInitController(ConnectorService connector, ServiceData service) {
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
        // TODO
        return mService.getType() + ": " + mService.getCommand();
    }

    @Override
    public int getServiceIcon() {
        try {
            Field _iconId = R.drawable.class.getField(mService.getIcon());
            return _iconId.getInt(null);
        }
        catch (Exception e) {
            return 0;
        }
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
