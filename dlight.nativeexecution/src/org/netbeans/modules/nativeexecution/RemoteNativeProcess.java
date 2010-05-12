/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.nativeexecution;

import com.jcraft.jsch.Channel;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.nativeexecution.JschSupport.ChannelParams;
import org.netbeans.modules.nativeexecution.JschSupport.ChannelStreams;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.netbeans.modules.nativeexecution.api.util.UnbufferSupport;

public final class RemoteNativeProcess extends AbstractNativeProcess {

    private final static int startupErrorExitValue = 184;
    private final static Object lock = RemoteNativeProcess.class.getName() + "Lock"; // NOI18N
    private ChannelStreams streams = null;
    private Integer exitValue = null;

    public RemoteNativeProcess(NativeProcessInfo info) {
        super(info);
    }

    @Override
    protected void create() throws Throwable {
        if (isInterrupted()) {
            throw new InterruptedException();
        }

        final String commandLine = info.getCommandLineForShell();
        final MacroMap envVars = info.getEnvironment().clone();

        // Setup LD_PRELOAD to load unbuffer library...
        if (info.isUnbuffer()) {
            UnbufferSupport.initUnbuffer(info.getExecutionEnvironment(), envVars);
        }

        if (isInterrupted()) {
            throw new InterruptedException();
        }

        ChannelParams params = new ChannelParams();
        params.setX11Forwarding(info.getX11Forwarding());

        synchronized (lock) {
            // To execute a command we use shell (/bin/sh) as a trampoline ..
            streams = JschSupport.startCommand(info.getExecutionEnvironment(), "/bin/sh -s", params); // NOI18N

            setErrorStream(streams.err);
            setInputStream(streams.out);
            setOutputStream(streams.in);

            // 1. get the PID of the shell
            streams.in.write("echo $$\n".getBytes()); // NOI18N
            streams.in.flush();

            final String workingDirectory = info.getWorkingDirectory(true);

            // 2. cd to working directory
            if (workingDirectory != null) {
                streams.in.write(EnvWriter.getBytes(
                        "cd \"" + workingDirectory + "\" || exit " + startupErrorExitValue + "\n", true)); // NOI18N
            }

            // 3. setup env
            String envFile = HostInfoUtils.getHostInfo(info.getExecutionEnvironment()).getEnvFile();
            streams.in.write(EnvWriter.getBytes(". " + envFile + ">/dev/null 2>&1\n", true)); // NOI18N

            EnvWriter ew = new EnvWriter(streams.in, true);
            ew.write(envVars);

            // 4. additional setup
            if (info.getInitialSuspend()) {
                streams.in.write("ITS_TIME_TO_START=\n".getBytes()); // NOI18N
                streams.in.write("trap 'ITS_TIME_TO_START=1' CONT\n".getBytes()); // NOI18N
                streams.in.write("while [ -z \"$ITS_TIME_TO_START\" ]; do sleep 1; done\n".getBytes()); // NOI18N
            }

            // 5. finally do exec
            streams.in.write(EnvWriter.getBytes("exec " + commandLine + "\n", true)); // NOI18N
            streams.in.flush();

            readPID(streams.out);
        }
    }

    @Override
    public int waitResult() throws InterruptedException {
        if (streams == null || streams.channel == null) {
            return -1;
        }

        while (streams.channel.isConnected()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                interrupt();
                throw ex;
            }
        }

        exitValue = Integer.valueOf(streams.channel.getExitStatus());

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
        Channel channel;

        synchronized (lock) {
            channel = streams == null ? null : streams.channel;

            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }

        int pid = 0;

        try {
            pid = getPID();
        } catch (IOException ex) {
        }

        if (pid > 0) {
            try {
                CommonTasksSupport.sendSignal(info.getExecutionEnvironment(), pid, Signal.SIGKILL, null).get();
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
            }
        }
    }
}
