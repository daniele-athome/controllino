package it.casaricci.controllino;

import it.casaricci.controllino.ConnectorService.ConnectorInterface;

import java.io.InputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;


/**
 * A threaded shell executor.
 * @author Daniele Ricci
 */
public class Executor extends Thread {
    private final ConnectorInterface mConnector;
    private final String mExec;
    private ExecuteListener mListener;

    public Executor(ConnectorInterface conn, String exec) {
        mConnector = conn;
        mExec = exec;
    }

    public void setListener(ExecuteListener listener) {
        mListener = listener;
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
            if (mListener != null)
                mListener.onError(this, e);
        }
        finally {
            try { channel.disconnect(); } catch (Exception e) {}
        }

        if (mListener != null)
            mListener.onExecuteFinish(this, exitStatus);
    }

    public interface ExecuteListener {
        public void onError(Executor exec, Throwable e);
        public void onExecuteFinish(Executor exec, int exitStatus);
    }

}
