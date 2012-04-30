package it.casaricci.controllino.controller;

import it.casaricci.controllino.ConnectorService.ConnectorInterface;
import it.casaricci.controllino.data.ServiceData;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.ByteArrayBuffer;

import android.util.Log;

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
        scriptTypes.put("upstart", DefaultUpstartController.class);
    }

    private Executor mExecutor;
    private ShellExecuteListener mShellExecuteListener;

    public ShellController(ConnectorInterface connector) {
        super(connector);
    }

    /**
     * Dynamically creates a new {@link ShellController} instance by looking up
     * the script types map.
     */
    public static ShellController newInstance(ConnectorInterface connector,
            ServiceData service) {
        try {
            Class<? extends ShellController> klass = scriptTypes.get(service.getType());
            if (klass != null) {
                Constructor<? extends ShellController> ctor = klass
                    .getConstructor(ConnectorInterface.class, ServiceData.class);
                return ctor.newInstance(connector, service);
            }
        }
        catch (Throwable e) {
            // TODO TAG
            Log.e("ShellController", "unable to create controller instance", e);
        }

        return null;
    }

    public interface ShellExecuteListener {
        public void onError(ShellController ctrl, Throwable e);
        public void onExecuteFinish(ShellController ctrl, int exitStatus, byte[] output);
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
            ByteArrayBuffer buf = null;
            try {
                Session sess = mConnector.session;
                channel = sess.openChannel("exec");
                channel.setInputStream(null);
                ((ChannelExec) channel).setCommand(mExec);
                channel.connect();

                InputStream in=channel.getInputStream();
                buf = new ByteArrayBuffer(0);

                byte[] tmp=new byte[1024];
                while(true){
                    while(in.available()>0){
                        int i=in.read(tmp, 0, 1024);
                        if(i<0)break;
                        // append to buffer
                        buf.append(tmp, 0, i);
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
                mShellExecuteListener.onExecuteFinish(ShellController.this, exitStatus, buf != null ? buf.buffer() : null);
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
