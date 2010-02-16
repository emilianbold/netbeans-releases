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
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.UnbufferSupport;

public final class RemoteNativeProcess extends AbstractNativeProcess {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private final static int startupErrorExitValue = 184;
    private final static Object lock = RemoteNativeProcess.class.getName() + "Lock"; // NOI18N
    private ChannelStreams cstreams = null;
    private Integer exitValue = null;

    public RemoteNativeProcess(NativeProcessInfo info) {
        super(info);
    }

    @Override
    protected void create() throws Throwable {
        Throwable exception = null;
        ChannelStreams streams = null;

        try {
            if (isInterrupted()) {
                throw new InterruptedException();
            }

            final String commandLine = info.getCommandLineForShell();
            final ConnectionManager mgr = ConnectionManager.getInstance();
            final ExecutionEnvironment execEnv = info.getExecutionEnvironment();

            final String sh = hostInfo.getShell();
            final MacroMap envVars = info.getEnvironment().clone();

            // Setup LD_PRELOAD to load unbuffer library...
            if (info.isUnbuffer()) {
                UnbufferSupport.initUnbuffer(info.getExecutionEnvironment(), envVars);
            }

            // Always append /bin and /usr/bin to PATH
            envVars.appendPathVariable("PATH", hostInfo.getPath() + ":/bin:/usr/bin"); // NOI18N

            if (isInterrupted()) {
                throw new InterruptedException();
            }

            synchronized (lock) {
                try {
                    final Session session = ConnectionManagerAccessor.getDefault().
                            getConnectionSession(mgr, execEnv, true);

                    streams = execCommand(session, sh + " -s"); // NOI18N
                    streams.in.write("echo $$\n".getBytes()); // NOI18N
                    streams.in.flush();

                    final String workingDirectory = info.getWorkingDirectory(true);

                    if (workingDirectory != null) {
                        streams.in.write(EnvWriter.getBytesWithRemoteCharset("cd \"" + workingDirectory + "\" || exit " + startupErrorExitValue + "\n")); // NOI18N
                    }

                    EnvWriter ew = new EnvWriter(streams.in);
                    ew.write(envVars);

                    if (info.getInitialSuspend()) {
                        streams.in.write("ITS_TIME_TO_START=\n".getBytes()); // NOI18N
                        streams.in.write("trap 'ITS_TIME_TO_START=1' CONT\n".getBytes()); // NOI18N
                        streams.in.write("while [ -z \"$ITS_TIME_TO_START\" ]; do sleep 1; done\n".getBytes()); // NOI18N
                    }
                    streams.in.write(EnvWriter.getBytesWithRemoteCharset("exec " + commandLine + "\n")); // NOI18N
                    streams.in.flush();

                    readPID(streams.out);
                } catch (Throwable ex) {
                    exception = ex;
                }
            }
        } catch (Throwable ex) {
            exception = ex;
        } finally {
            if (streams == null) {
                String error = null;

                if (exception != null) {
                    error = (exception.getMessage() == null ? exception.toString() : exception.getMessage()) + "\n"; // NOI18N
                }

                streams = new ChannelStreams(null,
                        new ByteArrayInputStream(new byte[0]),
                        new ByteArrayInputStream(error == null ? new byte[0] : error.getBytes()),
                        new ByteArrayOutputStream());
            }

            cstreams = streams;
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

        if (exitValue == startupErrorExitValue) {
            exitValue = -1;
        }

        if (getState() == State.CANCELLED) {
            throw new InterruptedException();
        }

        return exitValue.intValue();
    }

    @Override
    public synchronized void cancel() {
        ChannelExec channel;

        synchronized (lock) {
            channel = cstreams == null ? null : cstreams.channel;

            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }

        ProcessUtils.destroy(this);
    }

    private ChannelStreams execCommand(
            final Session session,
            final String command)
            throws IOException, JSchException {
        int retry = 2;

        while (retry-- > 0) {
            try {
                ChannelExec echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
                echannel.setCommand(command);
                echannel.setXForwarding(info.getX11Forwarding());
                echannel.connect(10000);

                return new ChannelStreams(echannel,
                        echannel.getInputStream(),
                        echannel.getErrStream(),
                        echannel.getOutputStream());
            } catch (JSchException ex) {
                String message = ex.getMessage();
                Throwable cause = ex.getCause();
                if (cause != null && cause instanceof NullPointerException) {
                    // Jsch bug... retry?
                } else if ("java.io.InterruptedIOException".equals(message)) { // NOI18N
                    log.log(Level.FINE, "RETRY to open jsch channel in 0.5 seconds [%s]...", retry); // NOI18N
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex1) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else if ("channel is not opened.".equals(message)) { // NOI18N
                    log.log(Level.FINE, "RETRY to open jsch channel in 0.5 seconds [%s]...", retry); // NOI18N
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex1) {
                        Thread.currentThread().interrupt();
                        break;
                    }
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
