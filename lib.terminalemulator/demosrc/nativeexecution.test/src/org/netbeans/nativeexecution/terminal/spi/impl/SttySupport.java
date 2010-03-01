/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.nativeexecution.terminal.spi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.nativeexecution.terminal.spi.impl.PtyCreatorImpl.PtyImplementation;

/**
 *
 * @author ak119685
 */
public final class SttySupport {

    private final static HashMap<ExecutionEnvironment, SttySupport> cache =
            new HashMap<ExecutionEnvironment, SttySupport>();
    private final Process sh;
    private final OutputStream in;
    private final InputStream out;
    private final OutputProcessor outProcessor;
//    private final InputStream err;
//    private final OutputProcessor errProcessor;
    private final ExecutionEnvironment env;
    private volatile boolean stopped = false;

    private SttySupport(final ExecutionEnvironment env) throws IOException {
        this.env = env;

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable("/bin/sh").setArguments("-s"); // NOI18N
        npb.getEnvironment().put("LC_ALL", "C"); // NOI18N

        sh = npb.call();
        in = sh.getOutputStream();
        out = sh.getInputStream();
        outProcessor = new OutputProcessor(out);


//        err = sh.getErrorStream();
//        errProcessor = new OutputProcessor(err);
    }

    public static synchronized SttySupport getFor(final ExecutionEnvironment env) throws IOException {
        if (!cache.containsKey(env)) {
            SttySupport newSupport = new SttySupport(env);
            cache.put(env, newSupport);
        }

        return cache.get(env);
    }

    // TODO: When?
    void stop() {
        if (stopped) {
            return;
        }

        synchronized (cache) {
            cache.remove(env);
        }

        ProcessUtils.destroy(sh);
    }

    public synchronized String[] apply(final PtyImplementation pty, final String args) throws IOException {
        if (stopped) {
            throw new IllegalArgumentException("This SttySupport is already stopped... "); // NOI18N
        }

        post("/bin/stty " + args + " < " + pty.getSlaveName() + " && echo ==="); // NOI18N
        return outProcessor.getOutput();
    }

    private void post(String cmd) throws IOException {
        in.write((cmd + "\n").getBytes()); // NOI18N
        in.flush();
    }

    private static class OutputProcessor {

        private final ArrayList<String> resultBuffer = new ArrayList<String>();
        private final StringBuilder lineBuffer = new StringBuilder();
        private final InputStream out;

        public OutputProcessor(InputStream out) {
            this.out = out;
        }

        public String[] getOutput() throws IOException {
            resultBuffer.clear();
            lineBuffer.setLength(0);

            int c;

            while (true) {
                if ((c = out.read()) < 0) {
                    break;
                }

                if (c == '\n') {
                    String line = lineBuffer.toString();
                    lineBuffer.setLength(0);
                    if (line.startsWith("===")) { // NOI18N
                        break;
                    }
                    resultBuffer.add(line);
                } else {
                    lineBuffer.append((char) c);
                }
            }

            return resultBuffer.toArray(new String[0]);
        }
    }
}
