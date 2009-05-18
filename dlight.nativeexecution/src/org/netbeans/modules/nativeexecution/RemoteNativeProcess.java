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
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.MacroMap;
import org.netbeans.modules.nativeexecution.support.UnbufferSupport;

public final class RemoteNativeProcess extends AbstractNativeProcess {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private final static Object lock = new String(RemoteNativeProcess.class.getName());
    private ChannelStreams cstreams = null;
    private Integer exitValue = null;

    public RemoteNativeProcess(NativeProcessInfo info) {
        super(info);
    }

    protected void create() throws Throwable {
        try {
            final String commandLine = info.getCommandLineForShell();
            final ConnectionManager mgr = ConnectionManager.getInstance();
            final ExecutionEnvironment execEnv = info.getExecutionEnvironment();
            final Session session = ConnectionManagerAccessor.getDefault().
                    getConnectionSession(mgr, execEnv, true);

            final String sh = hostInfo.getShell();
            final MacroMap envVars = info.getEnvVariables();

            // Setup LD_PRELOAD to load unbuffer library...
            UnbufferSupport.initUnbuffer(info, envVars);

            // Always prepend /bin and /usr/bin to PATH
            envVars.put("PATH", "/bin:/usr/bin:$PATH"); // NOI18N

            synchronized (lock) {
                cstreams = execCommand(session, sh + " -s"); // NOI18N
                cstreams.in.write("echo $$\n".getBytes()); // NOI18N
                cstreams.in.flush();

                final String workingDirectory = info.getWorkingDirectory(true);

                if (workingDirectory != null) {
                    cstreams.in.write(("cd " + workingDirectory + "\n").getBytes()); // NOI18N
                    cstreams.in.flush();
                }

                EnvWriter ew = new EnvWriter(cstreams.in);
                ew.write(envVars);

                cstreams.in.write(("exec " + commandLine + "\n").getBytes()); // NOI18N
                cstreams.in.flush();

                readPID(cstreams.out);
            }
        } catch (Throwable ex) {
            String msg = ex.getMessage() == null ? ex.toString() : ex.getMessage();

            cstreams = new ChannelStreams(
                    cstreams == null ? null : cstreams.channel,
                    new ByteArrayInputStream(new byte[0]),
                    new ByteArrayInputStream(msg.getBytes()),
                    new ByteArrayOutputStream());
            throw ex;
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

        synchronized (lock) {
            channel = cstreams == null ? null : cstreams.channel;

            if (channel != null && channel.isConnected()) {
                channel.disconnect();
//          NativeTaskSupport.kill(execEnv, 9, getPID());
            }
        }

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
                echannel.connect(10000);

                return new ChannelStreams(echannel,
                        echannel.getInputStream(),
                        echannel.getErrStream(),
                        echannel.getOutputStream());
            } catch (JSchException ex) {
                String message = ex.getMessage();
                Throwable cause = ex.getCause();
                if (cause != null && cause instanceof NullPointerException) {
                    // Jsch bug... retry? ;)
                    } else if ("channel is not opened.".equals(message)) { // NOI18N
                    log.fine("RETRY to open jsch channel in 0.5 seconds [" + retry + "]..."); // NOI18N
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
