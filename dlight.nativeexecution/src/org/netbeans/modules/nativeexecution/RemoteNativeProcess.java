/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.nativeexecution;

import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.MacroMap;
import org.netbeans.modules.nativeexecution.support.UnbufferSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public final class RemoteNativeProcess extends AbstractNativeProcess {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private final ChannelStreams cstreams;
    private Integer exitValue = null;

    public RemoteNativeProcess(NativeProcessInfo info) throws IOException {
        super(info);

        final String commandLine = info.getCommandLine();

        final ConnectionManager mgr = ConnectionManager.getInstance();
        ChannelStreams cs = null;

        final ExecutionEnvironment execEnv = info.getExecutionEnvironment();

        try {
            final Session session = ConnectionManagerAccessor.getDefault().
                    getConnectionSession(mgr, execEnv, true);

            final String sh = HostInfoUtils.getShell(execEnv);
            final MacroMap envVars = info.getEnvVariables();

            // Setup LD_PRELOAD to load unbuffer library...
            UnbufferSupport.initUnbuffer(info, envVars);

            try {
                cs = execCommand(session, sh + " -s"); // NOI18N
            } catch (JSchException ex) {
                throw new IOException("Unable to start " + sh + " on " + ex.toString()); // NOI18N
            }

            cs.in.write("echo $$\n".getBytes()); // NOI18N
            cs.in.flush();

            final String workingDirectory = info.getWorkingDirectory(true);

            if (workingDirectory != null) {
                cs.in.write(("cd " + workingDirectory + "\n").getBytes()); // NOI18N
                cs.in.flush();
            }


            EnvWriter ew = new EnvWriter(cs.in);
            ew.write(envVars);

            cs.in.write(("exec " + commandLine + "\n").getBytes()); // NOI18N
            cs.in.flush();

        } catch (CancellationException ex) {
            Exceptions.printStackTrace(ex);
            interrupt();
        } catch (Throwable ex) {
            interrupt();
        }

        if (cs == null || isInterrupted()) {
            if (cs != null && cs.channel != null) {
                cs.channel.disconnect();
            }

            cstreams = new ChannelStreams(
                    null,
                    new ByteArrayInputStream(new byte[0]),
                    new ByteArrayInputStream(new byte[0]),
                    new ByteArrayOutputStream());
            return;
        }

        cstreams = cs;

        try {
            readPID(cs.out);
        } catch (IOException ex) {
            interrupt();
        } finally {
//            if (isInterrupted()) {
//                throw new InterruptedIOException("Process interrupted"); // NOI18N
//            }
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return cstreams.in;
    }

    @Override
    public InputStream getInputStream() {
        return cstreams.out;
    }

    @Override
    public InputStream getErrorStream() {
        return cstreams.err;
    }

    @Override
    public int waitResult() throws InterruptedException {
        if (cstreams.channel == null) {
            return -1;
        }

        while (cstreams.channel.isConnected()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                interrupt();
                throw ex;
            }
        }

        exitValue = Integer.valueOf(cstreams.channel.getExitStatus());

        return exitValue.intValue();
    }

    @Override
    public void cancel() {
        ChannelExec channel;

        synchronized (this) {
            channel = cstreams == null ? null : cstreams.channel;
        }

        if (channel != null && channel.isConnected()) {
            channel.disconnect();
//          NativeTaskSupport.kill(execEnv, 9, getPID());
        }
    }

    private static String loc(String key, Object... params) {
        return NbBundle.getMessage(RemoteNativeProcess.class, key, params);
    }

    private ChannelStreams execCommand(
            final Session session,
            final String command) throws IOException, JSchException {
        int retry = 2;

        while (retry-- > 0) {
            try {
                ChannelExec echannel;

                synchronized (session) {
                    echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
                    echannel.setCommand(command);
                    echannel.connect();
                }

                return new ChannelStreams(echannel,
                        echannel.getInputStream(),
                        echannel.getErrStream(),
                        echannel.getOutputStream());
            } catch (JSchException ex) {
                Throwable cause = ex.getCause();
                if (cause != null && cause instanceof NullPointerException) {
                    // Jsch bug... retry? ;)
                } else {
                    throw ex;
                }
            } catch (NullPointerException npe) {
                // Jsch bug... retry? ;)
            }
        }

        throw new IOException("Failed to execute " + command); // NOI18N
    }

    private static class ChannelStreams {

        final InputStream out;
        final InputStream err;
        final OutputStream in;
        final ChannelExec channel;

        public ChannelStreams(ChannelExec channel, InputStream out,
                InputStream err, OutputStream in) {
            this.channel = channel;
            this.out = out;
            this.err = err;
            this.in = in;
        }
    }
}
