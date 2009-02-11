/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.nativeexecution.api.impl;

import org.netbeans.modules.nativeexecution.util.ConnectionManager;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

        final String commandLine = info.getCommandLine(true);
        final ConnectionManager mgr = ConnectionManager.getInstance();

        ChannelExec echannel = null;
        synchronized (mgr) {
            final ExecutionEnvironment execEnv = info.getExecutionEnvironment();

            final Session session = ConnectionManagerAccessor.getDefault().
                    getConnectionSession(mgr, execEnv);

            if (session == null) {
                throw new IOException("Unable to create remote session!"); // NOI18N
            }

            final String workingDirectory = info.getWorkingDirectory(true);
            final Map<String, String> envVars = info.getEnvVariables(true);

            String envSetup = "";

            if (!envVars.isEmpty()) {
                final StringBuilder envVarsAssign = new StringBuilder();
                final StringBuilder envVarsExport = new StringBuilder();

                for (String var : envVars.keySet()) {
                    envVarsAssign.append(var + '=' + envVars.get(var) + ' ');
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
}
