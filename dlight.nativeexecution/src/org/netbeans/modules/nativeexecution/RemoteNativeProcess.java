/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.nativeexecution;

import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public final class RemoteNativeProcess extends AbstractNativeProcess {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private final ChannelExec channel;
    private InputStream out;
    private InputStream err;
    private OutputStream in;
    private Integer exitValue = null;

    public RemoteNativeProcess(NativeProcessInfo info) throws IOException {
        super(info);

        final String commandLine = info.getCommandLine();
        final ConnectionManager mgr = ConnectionManager.getInstance();

        ChannelExec echannel = null;
        synchronized (mgr) {
            final ExecutionEnvironment execEnv = info.getExecutionEnvironment();
            
            try {
                ConnectionManager.getInstance().connectTo(execEnv);
            } catch (Throwable ex) {
                Exceptions.printStackTrace(ex);
            }

            final Session session = ConnectionManagerAccessor.getDefault().
                    getConnectionSession(mgr, execEnv);

            if (session == null) {
                throw new IOException("Unable to create remote session!"); // NOI18N
            }

            final String workingDirectory = info.getWorkingDirectory(true);

            final Map<String, String> userEnv = getUserEnv(session);

            final Map<String, String> envVars = info.getEnvVariables(userEnv);

            String envSetup = "";

            if (!envVars.isEmpty()) {
                final StringBuilder envVarsAssign = new StringBuilder();
                final StringBuilder envVarsExport = new StringBuilder();

                for (String var : envVars.keySet()) {
                    envVarsAssign.append(var + "='" + envVars.get(var) + "' ");
                    envVarsExport.append(' ' + var);
                }

                envSetup = " && " + envVarsAssign.toString() + // NOI18N
                        "&& export " + envVarsExport; // NOI18N
            }

            final StringBuilder cmd = new StringBuilder();

            if (workingDirectory != null) {
                cmd.append("cd " + workingDirectory + " && "); // NOI18N
            }

            cmd.append("/bin/echo $$ " + envSetup + " && exec " + commandLine); // NOI18N

            try {
                echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
                echannel.setCommand(cmd.toString());
                echannel.connect();
            } catch (JSchException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        channel = echannel;

        try {
            out = channel.getInputStream();
            err = channel.getErrStream();
            in = channel.getOutputStream();
        } catch (Exception e) {
            log.severe("Failed to get streams from ChannelExec"); // NOI18N
            e.printStackTrace();
        }

        readPID(out);
    }

    @Override
    public OutputStream getOutputStream() {
        return in;
    }

    @Override
    public InputStream getInputStream() {
        return out;
    }

    @Override
    public InputStream getErrorStream() {
        return err;
    }

    @Override
    public int waitResult() throws InterruptedException {
        if (channel == null) {
            return -1;
        }

        while (channel.isConnected()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                // skip
            }
        }

        exitValue = Integer.valueOf(channel.getExitStatus());

        return exitValue.intValue();
    }
    private static final Object cancelLock = new Object();

    @Override
    public void cancel() {
        synchronized (cancelLock) {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
//                NativeTaskSupport.kill(execEnv, 9, getPID());
            }
        }
    }

    private static String loc(String key, Object... params) {
        return NbBundle.getMessage(RemoteNativeProcess.class, key, params);
    }

    private Map<String, String> getUserEnv(Session session) {
        Map<String, String> result = new HashMap<String, String>();

        try {
            ChannelExec echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
            echannel.setCommand("/bin/env"); // NOI18N
            echannel.connect();
            InputStream is = echannel.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String s;

            while (true) {
                s = br.readLine();
                if (s == null) {
                    break;
                }

                int eidx = s.indexOf('=');
                result.put(s.substring(0, eidx), s.substring(eidx + 1));
            }

        } catch (IOException ex) {
            log.warning("Unable to fetch user's env"); // NOI18N
        } catch (JSchException ex) {
            log.warning("Unable to fetch user's env"); // NOI18N
        }

        return result;
    }
}
