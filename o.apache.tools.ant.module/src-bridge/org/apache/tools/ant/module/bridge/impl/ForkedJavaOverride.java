/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.apache.tools.ant.module.bridge.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.run.StandardLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.Redirector;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputWriter;

/**
 * Replacement for Ant's java task which directly sends I/O to the output without line buffering.
 * Idea from ide/projectimport/bluej/antsrc/org/netbeans/bluej/ant/task/BlueJava.java.
 * See issue #56341.
 */
public class ForkedJavaOverride extends Java {

    private static final RequestProcessor PROCESSOR = new RequestProcessor(ForkedJavaOverride.class.getName(), Integer.MAX_VALUE);

    // should be consistent with java.project.JavaAntLogger.STACK_TRACE
    private static final String JIDENT = "[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*"; // NOI18N
    private static final Pattern STACK_TRACE = Pattern.compile(
            "((?:" + JIDENT + "[.])*)(" + JIDENT + ")[.](?:" + JIDENT + "|<init>|<clinit>)" + // NOI18N
            "[(](?:(" + JIDENT + "[.]java):([0-9]+)|Unknown Source)[)]"); // NOI18N

    public ForkedJavaOverride() {
        redirector = new NbRedirector(this);
        super.setFork(true);
    }

    @Override
    public void setFork(boolean fork) {
        // #47645: ignore! Does not work to be set to false.
    }

    private void useStandardRedirector() { // #121512, #168153
        if (redirector instanceof NbRedirector) {
            redirector = new Redirector(this);
        }
    }
    public @Override void setInput(File input) {
        useStandardRedirector();
        super.setInput(input);
    }
    public @Override void setInputString(String inputString) {
        useStandardRedirector();
        super.setInputString(inputString);
    }
    public @Override void setOutput(File out) {
        useStandardRedirector();
        super.setOutput(out);
    }
    public @Override void setOutputproperty(String outputProp) {
        useStandardRedirector();
        super.setOutputproperty(outputProp);
    }
    public @Override void setError(File error) {
        useStandardRedirector();
        super.setError(error);
    }
    public @Override void setErrorProperty(String errorProperty) {
        useStandardRedirector();
        super.setErrorProperty(errorProperty);
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

            private Thread outTask;
            private Thread errTask;

            NbOutputStreamHandler() {}

            public void start() throws IOException {}

            public void stop() {
                if (errTask != null) {
                    try {
                        errTask.join();
                    } catch (InterruptedException ex) {
                    }
                }
                if (outTask != null) {
                    try {
                        outTask.join();
                    } catch (InterruptedException ex) {
                    }
                }
            }

            public void setProcessOutputStream(InputStream inputStream) throws IOException {
                OutputStream os = getOutputStream();
                Integer logLevel = null;
                if (os == null || os instanceof LogOutputStream) {
                    os = AntBridge.delegateOutputStream(false);
                    logLevel = Project.MSG_INFO;
                }
                outTask = new Thread(Thread.currentThread().getThreadGroup(), new Copier(inputStream, os, logLevel, outEncoding), 
                        "Out Thread for " + getProject().getName()); // NOI18N
                outTask.start();
            }

            public void setProcessErrorStream(InputStream inputStream) throws IOException {
                OutputStream os = getErrorStream();
                Integer logLevel = null;
                if (os == null || os instanceof LogOutputStream) {
                    os = AntBridge.delegateOutputStream(true);
                    logLevel = Project.MSG_WARN;
                }
                errTask = new Thread(Thread.currentThread().getThreadGroup(), new Copier(inputStream, os, logLevel, errEncoding), 
                        "Err Thread for " + getProject().getName()); // NOI18N
                errTask.start();
            }

            public void setProcessInputStream(OutputStream outputStream) throws IOException {
                InputStream is = getInputStream();
                if (is == null) {
                    is = AntBridge.delegateInputStream();
                }
                new Thread(Thread.currentThread().getThreadGroup(), new Copier(is, outputStream, null, null), 
                        "In Thread for " + getProject().getName()).start(); // NOI18N
            }

        }

    }

    private class Copier implements Runnable {

        private final InputStream in;
        private final OutputStream out;
        private final Integer logLevel;
        private final String encoding;
        private final RequestProcessor.Task flusher;
        private final ByteArrayOutputStream currentLine;
        private OutputWriter ow = null;
        private boolean err;
        private AntSession session = null;

        public Copier(InputStream in, OutputStream out, Integer logLevel, String encoding/*, long init*/) {
            this.in = in;
            this.out = out;
            this.logLevel = logLevel;
            this.encoding = encoding;
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
            
            if (ow == null && logLevel != null) {
                Vector v = getProject().getBuildListeners();
                for (Object o : v) {
                    if (o instanceof NbBuildLogger) {
                        NbBuildLogger l = (NbBuildLogger) o;
                        err = logLevel != Project.MSG_INFO;
                        ow = err ? l.err : l.out;
                        session = l.thisSession;
                        break;
                    }
                }
            }
            try {
                try {
                    int c;
                    while ((c = in.read()) != -1) {
                        if (logLevel == null) {
                            // Input gets sent immediately.
                            out.write(c);
                            out.flush();
                        } else {
                            synchronized (this) {
                                if (c == '\n') {
                                    String str = currentLine.toString(encoding);
                                    int len = str.length();
                                    if (len > 0 && str.charAt(len - 1) == '\r') {
                                        str = str.substring(0, len - 1);
                                    }
                                    // skip stack traces (hyperlinks are created by JavaAntLogger), everything else write directly
                                    if (!STACK_TRACE.matcher(str).find()) {
                                        StandardLogger.findHyperlink(str, session, null).println(session, err);
                                    }
                                    log(str, logLevel);
                                    currentLine.reset();
                                } else {
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
                if (currentLine.size() > 0) {
                    String str = currentLine.toString(encoding);
                    ow.write(str);
                    log(str, logLevel);
                }
            } catch (IOException x) {
                // probably safe to ignore
            } catch (ThreadDeath d) {
                // OK, build just stopped.
            }
            currentLine.reset();
        }

    }

}
