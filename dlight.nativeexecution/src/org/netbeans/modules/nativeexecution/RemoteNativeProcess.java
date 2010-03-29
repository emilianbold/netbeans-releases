/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.nativeexecution;

import com.jcraft.jsch.ChannelExec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.modules.nativeexecution.JschSupport.ChannelParams;
import org.netbeans.modules.nativeexecution.JschSupport.ChannelStreams;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.UnbufferSupport;

public final class RemoteNativeProcess extends AbstractNativeProcess {

    private final static int startupErrorExitValue = 184;
    private final static Object lock = RemoteNativeProcess.class.getName() + "Lock"; // NOI18N
    private ChannelStreams cstreams = null;
    private Integer exitValue = null;

    public RemoteNativeProcess(NativeProcessInfo info) {
        super(info);
    }

    @Override
    protected void create() throws Throwable {
        ChannelStreams streams = null;

        try {
            if (isInterrupted()) {
                throw new InterruptedException();
            }

            final String commandLine = info.getCommandLineForShell();
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

            ChannelParams params = new ChannelParams();
            params.setX11Forwarding(info.getX11Forwarding());

            synchronized (lock) {
                streams = JschSupport.execCommand(info.getExecutionEnvironment(), hostInfo.getShell() + " -s", params); // NOI18N
                streams.in.write("echo $$\n".getBytes()); // NOI18N
                streams.in.flush();

                final String workingDirectory = info.getWorkingDirectory(true);

                if (workingDirectory != null) {
                    streams.in.write(EnvWriter.getBytes(
                            "cd \"" + workingDirectory + "\" || exit " + startupErrorExitValue + "\n", true)); // NOI18N
                }

                EnvWriter ew = new EnvWriter(streams.in, true);
                ew.write(envVars);

                if (info.getInitialSuspend()) {
                    streams.in.write("ITS_TIME_TO_START=\n".getBytes()); // NOI18N
                    streams.in.write("trap 'ITS_TIME_TO_START=1' CONT\n".getBytes()); // NOI18N
                    streams.in.write("while [ -z \"$ITS_TIME_TO_START\" ]; do sleep 1; done\n".getBytes()); // NOI18N
                }

                streams.in.write(EnvWriter.getBytes("exec " + commandLine + "\n", true)); // NOI18N
                streams.in.flush();

                readPID(streams.out);
            }
        } catch (Throwable ex) {
            String msg = (ex.getMessage() == null ? ex.toString() : ex.getMessage()) + "\n"; // NOI18N

            streams = new ChannelStreams(null,
                    new ByteArrayInputStream(new byte[0]),
                    new ByteArrayInputStream(msg == null ? new byte[0] : msg.getBytes()),
                    new ByteArrayOutputStream());

            throw ex;
        } finally {
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
}
