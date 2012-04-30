package it.casaricci.controllino.controller;

import it.casaricci.controllino.ConnectorService.ConnectorInterface;
import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServiceData;

import java.lang.reflect.Field;


/**
 * An {@link UpstartController} backed by a {@link ServiceData} service
 * definition. Not using a service type "upstart" will throw an exception.
 * @author Daniele Ricci
 */
public class DefaultUpstartController extends UpstartController {
    private final ServiceData mService;
    private UpdateShellListener mUpdateListener = new UpdateShellListener();
    private StartStopShellListener mStartStopListener = new StartStopShellListener(mUpdateListener);

    public DefaultUpstartController(ConnectorInterface connector, ServiceData service) {
        super(connector, service.getCommand());
        if (!"upstart".equals(service.getType()))
            throw new IllegalArgumentException("not an Upstart service.");
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
        public void onExecuteFinish(ShellController ctrl, int exitStatus, byte[] output) {
            if (exitStatus == 0)
                setStatus(parseStatusOutput(output));
            else
                setStatus(Status.STATUS_UNKNOWN);
        }
    }

    private Status parseStatusOutput(byte[] output) {
        String script = mService.getCommand();

        // default charset on Android is UTF-8
        String out = new String(output);

        String compareTo;

        // goal: start
        compareTo = script + " start/";
        if (out.substring(0, compareTo.length()).equals(compareTo)) {
            if (out.substring(compareTo.length(), compareTo.length() + "starting".length()).equals("starting"))
                return Status.STATUS_STARTING;
            if (out.substring(compareTo.length(), compareTo.length() + "running".length()).equals("running"))
                return Status.STATUS_RUNNING;
        }

        // goal: stop
        compareTo = script + " stop/";
        if (out.substring(0, compareTo.length()).equals(compareTo)) {
            if (out.substring(compareTo.length(), compareTo.length() + "stopping".length()).equals("stopping"))
                return Status.STATUS_STOPPING;
            if (out.substring(compareTo.length(), compareTo.length() + "waiting".length()).equals("waiting"))
                return Status.STATUS_STOPPED;
        }

        return Status.STATUS_UNKNOWN;
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
