/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.nativeexecution.api.impl;

import org.netbeans.modules.nativeexecution.util.ConnectionManager;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public final class RemoteNativeProcess extends NativeProcess {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private final ChannelExec channel;
    private final Integer pid;
    private InputStream out = null;
    private InputStream err = null;
    private OutputStream in = null;
    private Integer exitValue = null;

    public RemoteNativeProcess(NativeProcessInfo info) throws IOException {
        final NativeProcessAccessor processInfo =
                NativeProcessAccessor.getDefault();

        processInfo.setListeners(this, info.getListeners());
        processInfo.setState(this, State.STARTING);

        final ConnectionManager mgr = ConnectionManager.getInstance();
        ChannelExec echannel = null;

        final String commandLine = info.getCommandLine();

        synchronized (mgr) {
            final ExecutionEnvironment execEnv = info.getExecutionEnvironment();

            final Session session = ConnectionManagerAccessor.getDefault().
                    getConnectionSession(mgr, execEnv);

            if (session == null) {
                throw new IOException("Unable to create remote session!"); // NOI18N
            }

            final String workingDirectory = info.getWorkingDirectory();
            final Map<String, String> envVars = info.getEnvVariables();

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

            processInfo.setID(this, commandLine);

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
            Logger.severe("Failed to get streams from ChannelExec"); // NOI18N
            e.printStackTrace();
        }

        // Read-out pid from the first line of output (result of 'echo $$')
        BufferedReader br = new BufferedReader(new InputStreamReader(out));
        String pidLine = br.readLine();
        Integer ppid = null;

        if (pidLine == null) {
            log.severe("Cannot get PID for " + commandLine); // NOI18N
        } else {
            try {
                ppid = new Integer(pidLine.trim());
            } catch (NumberFormatException ex) {
                log.severe("Cannot get PID for " + commandLine); // NOI18N
            }
        }

        pid = ppid;

        processInfo.setState(this, State.RUNNING);
    }

    @Override
    public int getPID() {
        if (pid == null) {
            throw new IllegalStateException("Process was not started"); // NOI18N
        }

        return pid.intValue();
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
