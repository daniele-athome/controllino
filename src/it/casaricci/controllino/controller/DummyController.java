package it.casaricci.controllino.controller;

import it.casaricci.controllino.ConnectorService;


/**
 * A dummy controller, just for testing.
 * @author Daniele Ricci
 */
public class DummyController extends BaseController {

	public DummyController(ConnectorService connector) {
		super(connector);
		mStatus = Status.STATUS_STOPPED;
	}

	@Override
	public void update() {
		// nothing to do here
	    setStatus(mStatus);
	}

	@Override
	public CharSequence getServiceName() {
		return "Dummy service";
	}

	@Override
	public CharSequence getServiceDescription() {
		return "A dummy controller for a dummy service.";
	}

    @Override
    public void start() {
        setStatus(Status.STATUS_RUNNING);
        notifySuccess();
    }

    @Override
    public void stop() {
        setStatus(Status.STATUS_STOPPED);
        notifySuccess();
    }

    @Override
    public void restart() {
        // TODO
    }

    @Override
    public void reload() {
        // TODO
    }
}
