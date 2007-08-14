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

package org.apache.tools.ant.module.run;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.OutputListener;

/**
 * Standard logger for producing Ant output messages.
 * @author Jesse Glick
 */
public final class StandardLogger extends AntLogger {
    
    private static final Logger ERR = Logger.getLogger(StandardLogger.class.getName());
    
    /**
     * Regexp matching an output line that is a column marker from a compiler or similar.
     * Captured groups:
     * <ol>
     * <li>spaces preceding caret; length indicates column number
     * </ol>
     * @see "#37358"
     */
    private static final Pattern CARET_SHOWING_COLUMN = Pattern.compile("^( *)\\^$"); // NOI18N
    /**
     * Regexp matching an output line indicating a change into a current working directory, as e.g. from make.
     * Captured groups:
     * <ol>
     * <li>new working dir
     * </ol>
     */
    private static final Pattern CWD_ENTER = Pattern.compile(".*Entering directory [`'\"]?([^`'\"]+)(['\"]|$|\\.\\.\\.$)"); // NOI18N
    /**
     * Regexp matching an output line indicating a change out of a current working directory.
     * Captured groups:
     * <ol>
     * <li>previous working dir
     * </ol>
     */
    private static final Pattern CWD_LEAVE = Pattern.compile(".*Leaving directory [`'\"]?([^`'\"]+)(['\"]|$|\\.\\.\\.$)"); // NOI18N
    /**
     * Regexp matching an output line which should (perhaps) be hyperlinked to a file.
     * Captured groups:
     * <ol>
     * <li>file name (rel/abs path or URL)
     * <li>line1
     * <li>col1
     * <li>line2
     * <li>col2
     * <li>message
     * </ol>
     */
    private static final Pattern HYPERLINK = Pattern.compile("\"?(.+?)\"?(?::|, line )(?:(\\d+):(?:(\\d+):(?:(\\d+):(\\d+):)?)?)? +(.+)"); // NOI18N
    
    /**
     * Data stored in the session.
     */
    private static final class SessionData {
        /** Time build was started. */
        public long startTime;
        /** Last-created hyperlink, in case we need to adjust the column number. */
        public Hyperlink lastHyperlink;
        /** Current stack of working directories for which output is being displayed; top is current location. */
        public Stack<File> currentDir = new Stack<File>();
        public SessionData() {}
    }
    
    /** used only for unit testing */
    private final long mockTotalTime;
    
    /** Default constructor for lookup. */
    public StandardLogger() {
        mockTotalTime = 0L;
    }
    
    /** used only for unit testing */
    StandardLogger(long mockTotalTime) {
        this.mockTotalTime = mockTotalTime;
    }
    
    @Override
    public boolean interestedInSession(AntSession session) {
        return true;
    }
    
    @Override
    public boolean interestedInAllScripts(AntSession session) {
        return true;
    }
    
    @Override
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }
    
    @Override
    public String[] interestedInTasks(AntSession session) {
        return AntLogger.ALL_TASKS;
    }
    
    @Override
    public int[] interestedInLogLevels(AntSession session) {
        int verb = session.getVerbosity();
        assert verb >= AntEvent.LOG_ERR && verb <= AntEvent.LOG_DEBUG : verb;
        int[] levels = new int[verb + 1];
        for (int i = 0; i <= verb; i++) {
            levels[i] = i;
        }
        return levels;
    }
    
    private SessionData getSessionData(AntSession session) {
        SessionData data = (SessionData) session.getCustomData(this);
        if (data == null) {
            data = new SessionData();
            session.putCustomData(this, data);
        }
        return data;
    }
    
    @Override
    public void buildInitializationFailed(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        // Write errors to the output window, since
        // a lot of errors could be annoying as dialogs
        Throwable t = event.getException();
        if (event.getSession().getVerbosity() >= AntEvent.LOG_VERBOSE) {
            deliverStackTrace(t, event);
        } else {
            event.getSession().println(t.toString(), true, null);
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_target_failed_status", event.getSession().getDisplayName()));
        event.consume();
    }

    private static void deliverBlockOfTextAsLines(String lines, AntEvent originalEvent, int level) {
        StringTokenizer tok = new StringTokenizer(lines, "\r\n"); // NOI18N
        while (tok.hasMoreTokens()) {
            String line = tok.nextToken();
            originalEvent.getSession().deliverMessageLogged(originalEvent, line, level);
        }
    }
    
    private static void deliverStackTrace(Throwable t, AntEvent originalEvent) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        deliverBlockOfTextAsLines(sw.toString(), originalEvent, AntEvent.LOG_ERR);
    }
    
    @Override
    public void buildStarted(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        getSessionData(event.getSession()).startTime = System.currentTimeMillis();
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_running_ant", event.getSession().getDisplayName()));
        // no messages printed for now
        event.consume();
    }
    
    @Override
    public void buildFinished(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        AntSession session = event.getSession();
        Throwable t = event.getException();
        long time = System.currentTimeMillis() - getSessionData(session).startTime; // #10305
        if (mockTotalTime != 0L) {
            time = mockTotalTime;
        }
        if (t == null) {
            session.println(formatMessageWithTime("FMT_finished_target_printed", time), false, null);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_finished_target_status", session.getDisplayName()));
        } else {
            if (t.getCause() instanceof ThreadDeath) {
                // Sometimes wrapped, but we really want to know just that the thread was stopped.
                t = t.getCause();
            }
            if (!session.isExceptionConsumed(t)) {
                session.consumeException(t);
                if (t.getClass().getName().equals("org.apache.tools.ant.BuildException") && session.getVerbosity() < AntEvent.LOG_VERBOSE) { // NOI18N
                    // Stack trace probably not required.
                    // Check for hyperlink to handle e.g. <fail>
                    // which produces a BE whose toString is the location + message.
                    // But send to other loggers since they may wish to suppress such an error.
                    String msg = t.toString();
                    deliverBlockOfTextAsLines(msg, event, AntEvent.LOG_ERR);
                } else if (!(t instanceof ThreadDeath) || event.getSession().getVerbosity() >= AntEvent.LOG_VERBOSE) {
                    // ThreadDeath can be thrown when killing an Ant process, so don't print it normally
                    deliverStackTrace(t, event);
                }
            }
            if (t instanceof ThreadDeath) {
                event.getSession().println(formatMessageWithTime("FMT_target_stopped_printed", time), true, null);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_target_stopped_status", event.getSession().getDisplayName()));
            } else {
                event.getSession().println(formatMessageWithTime("FMT_target_failed_printed", time), true, null); // #10305
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_target_failed_status", event.getSession().getDisplayName()));
            }
        }
        event.consume();
    }
    
    /** Formats the millis in a human readable String.
     * Total time: {0} minutes
     *             {1} seconds
     */
    private static String formatMessageWithTime(String key, long millis) {
        int secs = (int) (millis / 1000);
        int minutes = secs / 60;
        int seconds = secs % 60;
        return NbBundle.getMessage(StandardLogger.class, key, new Integer(minutes), new Integer(seconds));
    }
    
    @Override
    public void targetStarted(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        // XXX this could start indenting messages, perhaps
        String name = event.getTargetName();
        if (name != null) {
            // Avoid printing internal targets normally:
            int minlevel = (name.length() > 0 && name.charAt(0) == '-') ? AntEvent.LOG_VERBOSE : AntEvent.LOG_INFO;
            if (event.getSession().getVerbosity() >= minlevel) {
                event.getSession().println(NbBundle.getMessage(StandardLogger.class, "MSG_target_started_printed", name), false, null);
            }
        }
        event.consume();
    }
    
    @Override
    public void messageLogged(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        event.consume();
        AntSession session = event.getSession();
        String line = event.getMessage();
        if (line.equals("Trying to override old definition of task java") && event.getLogLevel() == AntEvent.LOG_WARN) { // NOI18N
            return; // #56341
        }
        ERR.log(Level.FINE, "Received message: {0}", line);
        if (line.indexOf('\n') != -1) {
            // Multiline message. Should be split into blocks and redelivered,
            // to allow other loggers (e.g. JavaAntLogger) to process individual
            // lines (e.g. stack traces). Note that other loggers are still capable
            // of handling the original multiline message specially. Note also that
            // only messages at or above the session verbosity will be split.
            deliverBlockOfTextAsLines(line, event, event.getLogLevel());
            return;
        }
        Matcher m = CARET_SHOWING_COLUMN.matcher(line);
        if (m.matches()) {
            // #37358: adjust the column number of the last hyperlink accordingly.
            ERR.fine("  Looks like a special caret line");
            SessionData data = getSessionData(session);
            if (data.lastHyperlink != null) {
                // For "  ^", infer a column number of 3.
                data.lastHyperlink.setColumn1(m.group(1).length() + 1);
                data.lastHyperlink = null;
                // Don't print the actual caret line, just noise.
                return;
            }
        }
        m = CWD_ENTER.matcher(line);
        if (m.matches()) {
            ERR.fine("  Looks like a change of CWD");
            File d = new File(m.group(1));
            if (d.isDirectory()) {
                Stack<File> stack = getSessionData(session).currentDir;
                stack.push(d);
                ERR.log(Level.FINE, "  ...is a change of CWD; stack now: {0}", stack);
            }
        }
        m = CWD_LEAVE.matcher(line);
        if (m.matches()) {
            ERR.fine("  Looks like a change of CWD back out");
            File d = new File(m.group(1));
            Stack<File> stack = getSessionData(session).currentDir;
            if (stack.empty()) {
                ERR.log(Level.FINE, "  ...but there was nowhere to change out of");
            } else {
                File previous = stack.pop();
                if (!previous.equals(d)) {
                    ERR.log(Level.FINE, "  ...stack mismatch: {0} vs. {1}", new Object[] {previous, d});
                }
            }
        }
        OutputListener hyperlink = findHyperlink(session, line);
        if (hyperlink instanceof Hyperlink) {
            getSessionData(session).lastHyperlink = (Hyperlink) hyperlink;
        }
        // XXX should translate tabs to spaces here as a safety measure (esp. since output window messes it up...)
        event.getSession().println(line, event.getLogLevel() <= AntEvent.LOG_WARN, hyperlink);
    }
    
    @Override
    public void taskFinished(AntEvent event) {
        // Do not consider hyperlinks from previous tasks.
        getSessionData(event.getSession()).lastHyperlink = null;
    }

    /**
     * Possibly hyperlink a message logged event.
     */
    private OutputListener findHyperlink(AntSession session, String line) {
        Stack<File> cwd = getSessionData(session).currentDir;
        Matcher m = HYPERLINK.matcher(line);
        if (!m.matches()) {
            ERR.fine("does not look like a hyperlink");
            return null;
        }
        String path = m.group(1);
        File file;
        if (path.startsWith("file:")) {
            try {
                file = new File(new URI(path));
            } catch (URISyntaxException e) {
                ERR.log(Level.FINE, "invalid URI, skipping", e);
                return null;
            } catch (IllegalArgumentException e) {
                ERR.log(Level.FINE, "invalid URI, skipping", e);
                return null;
            }
        } else {
            file = new File(path);
            if (!file.isAbsolute()) {
                if (cwd.isEmpty()) {
                    ERR.fine("Non-absolute path with no CWD, skipping");
                    // don't waste time on File.exists!
                    return null;
                } else {
                    file = new File(cwd.peek(), path);
                }
            }
        }
        if (!file.exists()) {
            ERR.log(Level.FINE, "no such file {0}, skipping", file);
            return null;
        }

        int line1 = -1, col1 = -1, line2 = -1, col2 = -1;
        String num = m.group(2);
        try {
            if (num != null) {
                line1 = Integer.parseInt(num);
                num = m.group(3);
                if (num != null) {
                    col1 = Integer.parseInt(num);
                    num = m.group(4);
                    if (num != null) {
                        line2 = Integer.parseInt(num);
                        col2 = Integer.parseInt(m.group(5));
                    }
                }
            }
        } catch (NumberFormatException e) {
            ERR.log(Level.FINE, "bad line/col #", e);
            return null;
        }

        String message = m.group(6);
        
        file = FileUtil.normalizeFile(file); // do this late, after File.exists
        ERR.log(Level.FINE, "Hyperlink: {0} [{1}:{2}:{3}:{4}]: {5}", new Object[] {file, line1, col1, line2, col2, message});
        try {
            return session.createStandardHyperlink(file.toURI().toURL(), message, line1, col1, line2, col2);
        } catch (MalformedURLException e) {
            assert false : e;
            return null;
        }
    }

}
