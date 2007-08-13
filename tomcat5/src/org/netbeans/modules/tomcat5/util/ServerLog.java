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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ServerLog.java
 *
 * Created on September 13, 2004, 7:13 PM
 */

package org.netbeans.modules.tomcat5.util;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.util.LogSupport.LineInfo;
import org.openide.util.Exceptions;

/**
 * Tomcat server log reads from the Tomcat standard and error output and 
 * writes to output window.
 */ 
class ServerLog extends Thread {
    private final InputOutput io;
    private final OutputWriter writer;
    private final OutputWriter errorWriter;
    private final BufferedReader inReader;
    private final BufferedReader errReader;
    private final boolean autoFlush;
    private final boolean takeFocus;
    private volatile boolean done = false;
    private final ServerLogSupport logSupport;
    private final TomcatManager tomcatManager;

    /**
     * Tomcat server log reads from the Tomcat standard and error output and 
     * writes to output window.
     * 
     * @param tomcatManager Tomcat deployment manager
     * @param displayName output window display name.
     * @param in Tomcat standard output reader.
     * @param err Tomcat error output reader.
     * @param autoFlush should we flush after a change?
     * @param takeFocus should be the output window made visible after each
     *        changed?
     */
    public ServerLog(TomcatManager tomcatManager, String displayName, Reader in, Reader err, boolean autoFlush,
            boolean takeFocus) {
        super(displayName + " ServerLog - Thread"); // NOI18N
        setDaemon(true);
        inReader = new BufferedReader(in);
        errReader = new BufferedReader(err);
        this.autoFlush = autoFlush;
        this.takeFocus = takeFocus;
        this.tomcatManager = tomcatManager;
        io = UISupport.getServerIO(tomcatManager.getUri());
        try {
            io.getOut().reset();
        } 
        catch (IOException e) {
            Logger.getLogger(ServerLog.class.getName()).log(Level.INFO, null, e);
        }
        writer = io.getOut();
        errorWriter = io.getErr();
        io.select();
        logSupport = new ServerLogSupport();
    }

    private void processLine(String line) {
        ServerLogSupport.LineInfo lineInfo = logSupport.analyzeLine(line);
        if (lineInfo.isError()) {
            if (lineInfo.isAccessible()) {
                try {
                    errorWriter.println(line, logSupport.getLink(lineInfo.message() , lineInfo.path(), lineInfo.line()));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                errorWriter.println(line);
            }
        } else {
            writer.println(line);
            if (line.startsWith("SEVERE: WSSERVLET11: failed to parse runtime descriptor: java.lang.LinkageError:")) { // NOI18N
                File file = InstalledFileLocator.getDefault().locate("modules/ext/jaxws21/api/jaxws-api.jar", null, false); // NOI18N
                File endoresedDir = tomcatManager.getTomcatProperties().getJavaEndorsedDir();
                if (file != null) {
                    writer.println(NbBundle.getMessage(ServerLog.class, "MSG_WSSERVLET11", file.getParent(), endoresedDir));
                } else {
                    writer.println(NbBundle.getMessage(ServerLog.class, "MSG_WSSERVLET11_NOJAR", endoresedDir));
                }
            }
        }
    }

    public void run() {
        try {
            while(!done) {                    
                boolean isInReaderReady = false;
                boolean isErrReaderReady = false;
                boolean updated = false;
                int count = 0;
                // take a nap after 1024 read cycles, this should ensure responsiveness
                // even if log file is growing fast
                while (((isInReaderReady = inReader.ready()) || (isErrReaderReady = errReader.ready())) 
                        && count++ < 1024) {
                    if (done) {
                        return;
                    }
                    updated = true;
                    if (isInReaderReady) {
                        String line = inReader.readLine();
                        // finish, if we have reached the end of the stream
                        if (line == null) {
                            return;
                        }
                        processLine(line);
                    }
                    if (isErrReaderReady) {
                        String line = errReader.readLine();
                        // finish, if we have reached the end of the stream
                        if (line == null) {
                            return;
                        }
                        processLine(line);
                    }
                }
                if (updated) {
                    if (autoFlush) {
                        writer.flush();
                        errorWriter.flush();
                    }
                    if (takeFocus) {
                        io.select();
                    }
                }
                sleep(100); // take a nap
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerLog.class.getName()).log(Level.INFO, null, ex);
        } catch (InterruptedException e) {
            // no op - the thread was interrupted 
        } finally {
            logSupport.detachAnnotation();
        }
    }
    
    /**
     * Test whether ServerLog thread is still running.
     *
     * @return <code>true</code> if the thread is still running, <code>false</code>
     *         otherwise.
     */
    public boolean isRunning() {
        return !(done);
    }
    
    /**
     * Make the log tab visible.
     */
    public void takeFocus() {
        io.select();
    }

    public void interrupt() {
        super.interrupt();
        done = true;
    }
    
    /**
     * Support class for Tomcat server output log line analyzation and for 
     * creating links in the output window.
     */
    static class ServerLogSupport extends LogSupport {
        private String prevMessage;
        private GlobalPathRegistry globalPathRegistry = GlobalPathRegistry.getDefault();
        
        public LineInfo analyzeLine(String logLine) {
            String path = null;
            int line = -1;
            String message = null;
            boolean error = false;
            boolean accessible = false;

            logLine = logLine.trim();
            int lineLenght = logLine.length();

            // look for unix file links (e.g. /foo/bar.java:51: 'error msg')
            if (logLine.startsWith("/")) {
                error = true;
                int colonIdx = logLine.indexOf(':');
                if (colonIdx > -1) {
                    path = logLine.substring(0, colonIdx);
                    accessible = true;
                    if (lineLenght > colonIdx) {
                        int nextColonIdx = logLine.indexOf(':', colonIdx + 1);
                        if (nextColonIdx > -1) {
                            String lineNum = logLine.substring(colonIdx + 1, nextColonIdx);
                            try {
                                line = Integer.valueOf(lineNum).intValue();
                            } catch(NumberFormatException nfe) { 
                                // ignore it
                                Logger.getLogger(ServerLog.class.getName()).log(Level.INFO, null, nfe);
                            }
                            if (lineLenght > nextColonIdx) {
                                message = logLine.substring(nextColonIdx + 1, lineLenght); 
                            }
                        }
                    }
                }
            }
            // look for windows file links (e.g. c:\foo\bar.java:51: 'error msg')
            else if (lineLenght > 3 && Character.isLetter(logLine.charAt(0))
                        && (logLine.charAt(1) == ':') && (logLine.charAt(2) == '\\')) {
                error = true;
                int secondColonIdx = logLine.indexOf(':', 2);
                if (secondColonIdx > -1) {
                    path = logLine.substring(0, secondColonIdx);
                    accessible = true;
                    if (lineLenght > secondColonIdx) {
                        int thirdColonIdx = logLine.indexOf(':', secondColonIdx + 1);
                        if (thirdColonIdx > -1) {
                            String lineNum = logLine.substring(secondColonIdx + 1, thirdColonIdx);
                            try {
                                line = Integer.valueOf(lineNum).intValue();
                            } catch(NumberFormatException nfe) { 
                                // ignore it
                                Logger.getLogger(ServerLog.class.getName()).log(Level.INFO, null, nfe);
                            }
                            if (lineLenght > thirdColonIdx) {
                                message = logLine.substring(thirdColonIdx + 1, lineLenght);
                            }
                        }
                    }
                }
            }
            // look for stacktrace links (e.g. at java.lang.Thread.run(Thread.java:595)
            //                                 at t.HyperlinkTest$1.run(HyperlinkTest.java:24))
            else if (logLine.startsWith("at ") && lineLenght > 3) {
                error = true;
                int parenthIdx = logLine.indexOf('(');
                if (parenthIdx > -1) {
                    String classWithMethod = logLine.substring(3, parenthIdx);
                    int lastDotIdx = classWithMethod.lastIndexOf('.');
                    if (lastDotIdx > -1) {  
                        int lastParenthIdx = logLine.lastIndexOf(')');
                        int lastColonIdx = logLine.lastIndexOf(':');
                        if (lastParenthIdx > -1 && lastColonIdx > -1) {
                            String lineNum = logLine.substring(lastColonIdx + 1, lastParenthIdx);
                            try {
                                line = Integer.valueOf(lineNum).intValue();
                            } catch(NumberFormatException nfe) {
                                // ignore it
                                Logger.getLogger(ServerLog.class.getName()).log(Level.INFO, null, nfe);
                            }
                            message = prevMessage;
                        }
                        int firstDolarIdx = classWithMethod.indexOf('$'); // > -1 for inner classes
                        String className = classWithMethod.substring(0, firstDolarIdx > -1 ? firstDolarIdx : lastDotIdx);
                        path = className.replace('.','/') + ".java"; // NOI18N
                        accessible = globalPathRegistry.findResource(path) != null;
                    }
                }
            }
            // every other message treat as normal info message
            else {
                prevMessage = logLine;
            }
            return new LineInfo(path, line, message, error, accessible);
        }
    }
}