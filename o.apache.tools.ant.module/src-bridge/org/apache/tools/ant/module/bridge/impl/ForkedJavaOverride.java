/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.apache.tools.ant.module.bridge.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.Redirector;
import org.openide.util.RequestProcessor;

/**
 * Replacement for Ant's java task which directly sends I/O to the output without line buffering.
 * Idea from ide/projectimport/bluej/antsrc/org/netbeans/bluej/ant/task/BlueJava.java.
 * See issue #56341.
 */
public class ForkedJavaOverride extends Java {

    private static final RequestProcessor PROCESSOR = new RequestProcessor(ForkedJavaOverride.class.getName(), Integer.MAX_VALUE);

    public ForkedJavaOverride() {
        redirector = new NbRedirector(this);
    }

    private class NbRedirector extends Redirector {

        private String outEncoding = System.getProperty("file.encoding"); // NOI18N
        private String errEncoding = System.getProperty("file.encoding"); // NOI18N

        public NbRedirector(Task task) {
            super(task);
        }

        public @Override ExecuteStreamHandler createHandler() throws BuildException {
            createStreams();
            return new NbOutputStreamHandler();
        }

        public @Override synchronized void setOutputEncoding(String outputEncoding) {
            outEncoding = outputEncoding;
            super.setOutputEncoding(outputEncoding);
        }

        public @Override synchronized void setErrorEncoding(String errorEncoding) {
            errEncoding = errorEncoding;
            super.setErrorEncoding(errorEncoding);
        }

        private class NbOutputStreamHandler implements ExecuteStreamHandler {

            private RequestProcessor.Task outTask;
            private RequestProcessor.Task errTask;
            //private RequestProcessor.Task inTask;

            //long init = System.currentTimeMillis();
            NbOutputStreamHandler() {}

            public void start() throws IOException {}

            public void stop() {
                /* XXX causes process to hang at end
                if (inTask != null) {
                    inTask.waitFinished();
                }
                */
                if (errTask != null) {
                    errTask.waitFinished();
                }
                if (outTask != null) {
                    outTask.waitFinished();
                }
            }

            public void setProcessOutputStream(InputStream inputStream) throws IOException {
                OutputStream os = getOutputStream();
                Integer logLevel = null;
                if (os == null || os instanceof LogOutputStream) {
                    os = AntBridge.delegateOutputStream(false);
                    logLevel = Project.MSG_INFO;
                }
                outTask = PROCESSOR.post(new Copier(inputStream, os, logLevel, outEncoding/*, init*/));
            }

            public void setProcessErrorStream(InputStream inputStream) throws IOException {
                OutputStream os = getErrorStream();
                Integer logLevel = null;
                if (os == null || os instanceof LogOutputStream) {
                    os = AntBridge.delegateOutputStream(true);
                    logLevel = Project.MSG_WARN;
                }
                errTask = PROCESSOR.post(new Copier(inputStream, os, logLevel, errEncoding/*, init*/));
            }

            public void setProcessInputStream(OutputStream outputStream) throws IOException {
                InputStream is = getInputStream();
                if (is == null) {
                    is = AntBridge.delegateInputStream();
                }
                /*inTask = */PROCESSOR.post(new Copier(is, outputStream, null, null/*, init*/));
            }

        }

    }

    private class Copier implements Runnable {

        private final InputStream in;
        private final OutputStream out;
        //final long init;
        private final Integer logLevel;
        private final String encoding;
        private final RequestProcessor.Task flusher;
        private final ByteArrayOutputStream currentLine;

        public Copier(InputStream in, OutputStream out, Integer logLevel, String encoding/*, long init*/) {
            this.in = in;
            this.out = out;
            this.logLevel = logLevel;
            this.encoding = encoding;
            //this.init = init;
            if (logLevel != null) {
                flusher = PROCESSOR.create(new Runnable() {
                    public void run() {
                        maybeFlush();
                    }
                });
                currentLine = new ByteArrayOutputStream();
            } else {
                flusher = null;
                currentLine = null;
            }
        }

        public void run() {
            /*
            StringBuilder content = new StringBuilder();
            long tick = System.currentTimeMillis();
            content.append(String.format("[init: %1.1fsec]", (tick - init) / 1000.0));
             */
            try {
                try {
                    int c;
                    while ((c = in.read()) != -1) {
                        /*
                        long newtick = System.currentTimeMillis();
                        if (newtick - tick > 100) {
                            content.append(String.format("[%1.1fsec]", (newtick - tick) / 1000.0));
                        }
                        tick = newtick;
                        content.append((char) c);
                         */
                        if (logLevel == null) {
                            // Input gets sent immediately.
                            out.write(c);
                            out.flush();
                        } else {
                            // Output and err are buffered (for a time) looking for a complete line.
                            synchronized (this) {
                                if (c == '\n') {
                                    log(currentLine.toString(encoding), logLevel);
                                    currentLine.reset();
                                } else if (c != '\r') {
                                    currentLine.write(c);
                                    flusher.schedule(250);
                                }
                            }
                        }
                    }
                } finally {
                    if (logLevel != null) {
                        maybeFlush();
                    }
                }
            } catch (IOException x) {
                // ignore IOException: Broken pipe from FileOutputStream.writeBytes in BufferedOutputStream.flush
            } catch (ThreadDeath d) {
                // OK, build just stopped.
                return;
            }
            //System.err.println("copied " + in + " to " + out + "; content='" + content + "'");
        }

        private synchronized void maybeFlush() {
            try {
                currentLine.writeTo(out);
                out.flush();
            } catch (IOException x) {
                // probably safe to ignore
            } catch (ThreadDeath d) {
                // OK, build just stopped.
            }
            currentLine.reset();
        }

    }

}
