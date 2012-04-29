package it.casaricci.controllino.controller;

import it.casaricci.controllino.ConnectorService.ConnectorInterface;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;


/**
 * Abstract shell command controller.
 * Extend this to create some useful shell-based controllers.
 * @author Daniele Ricci
 */
public abstract class ShellController extends BaseController {
    public static final Map<String, Class<? extends ShellController>> scriptTypes;
    static {
        scriptTypes = new HashMap<String, Class<? extends ShellController>>();
        scriptTypes.put("sysvinit", DefaultSysVInitController.class);
    }

    private Executor mExecutor;
    private ShellExecuteListener mShellExecuteListener;

    public ShellController(ConnectorInterface connector) {
        super(connector);
    }

    public interface ShellExecuteListener {
        public void onError(ShellController ctrl, Throwable e);
        public void onExecuteFinish(ShellController ctrl, int exitStatus);
    }

    public static class StartStopException extends Exception {
		private static final long serialVersionUID = 1703215303501554197L;

		public StartStopException() {
		}

		public StartStopException(String detailMessage) {
			super(detailMessage);
		}

		public StartStopException(Throwable throwable) {
			super(throwable);
		}

		public StartStopException(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}
    }

    private final class Executor extends Thread {
        private final String mExec;

        public Executor(String exec) {
            mExec = exec;
        }

        @Override
        public void run() {
            Channel channel = null;
            int exitStatus = -1;
            try {
                Session sess = mConnector.session;
                channel = sess.openChannel("exec");
                channel.setInputStream(null);
                ((ChannelExec) channel).setCommand(mExec);
                channel.connect();

                InputStream in=channel.getInputStream();

                byte[] tmp=new byte[1024];
                while(true){
                    while(in.available()>0){
                        int i=in.read(tmp, 0, 1024);
                        if(i<0)break;
                    }
                    if (channel.isClosed()) {
                        exitStatus = channel.getExitStatus();
                        break;
                    }
                    try{Thread.sleep(500);}catch(Exception ee){}
                }
            }
            catch (Exception e) {
                if (mShellExecuteListener != null)
                    mShellExecuteListener.onError(ShellController.this, e);
            }
            finally {
                try { channel.disconnect(); } catch (Exception e) {}
            }

            // invalidate executor instance
            mExecutor = null;

            if (mShellExecuteListener != null)
                mShellExecuteListener.onExecuteFinish(ShellController.this, exitStatus);
        }
    }

    public void setShellExecuteListener(ShellExecuteListener listener) {
        mShellExecuteListener = listener;
    }

    protected void execute(final String exec) {
        if (mExecutor == null) {
            mExecutor = new Executor(exec);
            mExecutor.start();
        }
    }

    /** Returns the script type used by this controller. */
    public abstract String getScriptType();

}
