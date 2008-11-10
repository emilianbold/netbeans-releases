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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.collab.channel.output;

import com.sun.collablet.*;

import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.OutputListener;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.net.MalformedURLException;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.api.project.*;

import org.netbeans.modules.collab.channel.filesharing.*;
import org.netbeans.modules.collab.core.Debug;


@org.openide.util.lookup.ServiceProvider(service=org.apache.tools.ant.module.spi.AntLogger.class, position=100)
public class CollabAntLogger extends AntLogger {
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(CollabAntLogger.class.getName());
    private static final boolean LOGGABLE = ERR.isLoggable(ErrorManager.INFORMATIONAL);
    private static final Pattern CARET_SHOWING_COLUMN = Pattern.compile("^( *)\\^$"); // NOI18N
    private StringBuffer buffer = new StringBuffer();
    private Conversation[] convs;

    /**
     * Creates a new instance of CollabAntLogger
     */
    public CollabAntLogger() {
    }

    public boolean interestedInAllScripts(AntSession session) {
        return true;
    }

    public int[] interestedInLogLevels(AntSession session) {
        int verb = session.getVerbosity();
        assert (verb >= AntEvent.LOG_ERR) && (verb <= AntEvent.LOG_DEBUG) : verb;

        int[] levels = new int[verb + 1];

        for (int i = 0; i <= verb; i++) {
            levels[i] = i;
        }

        return levels;
    }

    public boolean interestedInSession(AntSession session) {
        // we're only interested in those projects being shared
        File buildScript = session.getOriginatingScript();
        String[] targets = session.getOriginatingTargets();
        FileObject fo = FileUtil.toFileObject(buildScript);
        Project project = FileOwnerQuery.getOwner(fo);

        if (project == null) {
            return false;
        }

        convs = FilesharingCollablet.getConversations(project);
        Debug.out.println("*********** interested in session: " + convs.length);

        for (int i = 0; i < targets.length; i++) {
            Debug.out.println("*********** targets : " + targets[i]);
        }

        return (convs != null) && (convs.length > 0);
    }

    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }

    public String[] interestedInTasks(AntSession session) {
        return AntLogger.ALL_TASKS;
    }

    public void taskStarted(AntEvent event) {
        super.taskStarted(event);
    }

    public void targetStarted(AntEvent event) {
        //        if (event.isConsumed()) {
        //            return;
        //        }
        // XXX this could start indenting messages, perhaps
        String name = event.getTargetName();

        if (name != null) {
            // Avoid printing internal targets normally:
            int minlevel = ((name.length() > 0) && (name.charAt(0) == '-')) ? AntEvent.LOG_VERBOSE : AntEvent.LOG_INFO;

            if (event.getSession().getVerbosity() >= minlevel) {
                buffer.append(NbBundle.getMessage(CollabAntLogger.class, "MSG_target_started_printed", name) + "\r\n");
            }
        }

        //        event.consume();
    }

    public void buildFinished(AntEvent event) {
        //        if (event.isConsumed()) {
        //            return;
        //        }
        AntSession session = event.getSession();
        Throwable t = event.getException();
        long time = System.currentTimeMillis() - getSessionData(session).startTime; // #10305

        if (t == null) {
            buffer.append(formatMessageWithTime("FMT_finished_target_printed", time) + "\r\n");
        } else {
            if (!session.isExceptionConsumed(t)) {
                session.consumeException(t);

                if (
                    t.getClass().getName().equals("org.apache.tools.ant.BuildException") &&
                        (session.getVerbosity() < AntEvent.LOG_VERBOSE)
                ) { // NOI18N

                    // Stack trace probably not required.
                    // Check for hyperlink to handle e.g. <fail>
                    // which produces a BE whose toString is the location + message.
                    // But send to other loggers since they may wish to suppress such an error.
                    String msg = t.toString();
                    deliverBlockOfTextAsLines(msg, event, AntEvent.LOG_ERR);
                } else if (!(t instanceof ThreadDeath) || (event.getSession().getVerbosity() >= AntEvent.LOG_VERBOSE)) {
                    // ThreadDeath can be thrown when killing an Ant process, so don't print it normally
                    deliverStackTrace(t, event);
                }
            }

            buffer.append(formatMessageWithTime("FMT_target_failed_printed", time) + "\r\n");
        }

        //        event.consume();
        Debug.out.println(buffer.toString());
        shareOutput(event);

        reset();
    }

    private void shareOutput(AntEvent event) {
        String projectName = "";
        String displayName = "";

        FileObject fo = FileUtil.toFileObject(event.getSession().getOriginatingScript());
        Project project = FileOwnerQuery.getOwner(fo);

        if (project != null) {
            projectName = ProjectUtils.getInformation(project).getDisplayName();
        }

        String target = event.getTargetName();

        if (target == null) {
            displayName = NbBundle.getMessage(CollabAntLogger.class, "TITLE_output_notarget", projectName);
        } else {
            displayName = NbBundle.getMessage(CollabAntLogger.class, "TITLE_output_target", projectName, target);
        }

        Conversation[] conversations = getConversations();

        if (conversations == null) {
            return;
        }

        for (int i = 0; i < conversations.length; i++) {
            Collablet[] channels = conversations[i].getChannels();

            for (int j = 0; j < channels.length; j++) {
                if (channels[j] instanceof OutputCollablet) {
                    String id = conversations[i].getCollabSession().getUserPrincipal().getIdentifier() +
                        conversations[i].getIdentifier() + projectName;

                    ((OutputCollablet) channels[j]).shareAntOutput(displayName, id, buffer.toString());
                }
            }
        }
    }

    public void buildInitializationFailed(AntEvent event) {
        super.buildInitializationFailed(event);
    }

    public void buildStarted(AntEvent event) {
        Debug.out.println(" target " + event.getTargetName());

        //         if (event.isConsumed()) {
        //            return;
        //        }
        getSessionData(event.getSession()).startTime = System.currentTimeMillis();

        ////        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_running_ant", event.getSession().getDisplayName()));
        //        // no messages printed for now
        //        event.consume();
    }

    public void messageLogged(AntEvent event) {
        //        if (event.isConsumed()) {
        //            return;
        //        }
        //        event.consume();
        AntSession session = event.getSession();
        String line = event.getMessage();

        //        if (LOGGABLE) ERR.log("Received message: " + line);
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
            SessionData data = getSessionData(session);

            if (data.lastHyperlink != null) {
                // For "  ^", infer a column number of 3.
                data.lastHyperlink.setColumn1(m.group(1).length() + 1);
                data.lastHyperlink = null;

                // Don't print the actual caret line, just noise.
                return;
            }
        }

        OutputListener hyperlink = findHyperlink(session, line);

        if (hyperlink instanceof Hyperlink) {
            getSessionData(session).lastHyperlink = (Hyperlink) hyperlink;
        }

        // XXX should translate tabs to spaces here as a safety measure (esp. since output window messes it up...)
        //        event.getSession().println(line, event.getLogLevel() <= AntEvent.LOG_WARN, hyperlink);
        buffer.append(line + "\r\n");
    }

    public void targetFinished(AntEvent event) {
    }

    public void taskFinished(AntEvent event) {
    }

    public boolean interestedInScript(File script, AntSession session) {
        return true;
    }

    private Conversation[] getConversations() {
        return convs;
    }

    private void reset() {
        convs = null;
        buffer = new StringBuffer();
    }

    private SessionData getSessionData(AntSession session) {
        SessionData data = (SessionData) session.getCustomData(this);

        if (data == null) {
            data = new SessionData();
            session.putCustomData(this, data);
        }

        return data;
    }

    /** Formats the millis in a human readable String.
    * Total time: {0} minutes
    *             {1} seconds
    */
    private String formatMessageWithTime(String key, long millis) {
        int secs = (int) (millis / 1000);
        int minutes = secs / 60;
        int seconds = secs % 60;

        return NbBundle.getMessage(CollabAntLogger.class, key, new Integer(minutes), new Integer(seconds));
    }

    private void deliverBlockOfTextAsLines(String lines, AntEvent originalEvent, int level) {
        StringTokenizer tok = new StringTokenizer(lines, "\r\n"); // NOI18N

        while (tok.hasMoreTokens()) {
            String line = tok.nextToken();

            //            originalEvent.getSession().deliverMessageLogged(originalEvent, line, level);
            buffer.append(line + "\r\n");
        }
    }

    private void deliverStackTrace(Throwable t, AntEvent originalEvent) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        deliverBlockOfTextAsLines(sw.toString(), originalEvent, AntEvent.LOG_ERR);
    }

    /**
    * Possibly hyperlink a message logged event.
    */
    private static OutputListener findHyperlink(AntSession session, String line) {
        // #29246: handle new (Ant 1.5.1) URLifications:
        // [PENDING] Under JDK 1.4, could use new File(URI)... if Ant uses URI too (Jakarta BZ #8031)
        // XXX so tweak that for Ant 1.6 support!
        // XXX would be much easier to use a regexp here
        if (line.startsWith("file:///")) { // NOI18N
            line = line.substring(7);

            if (LOGGABLE) {
                ERR.log("removing file:///");
            }
        } else if (line.startsWith("file:")) { // NOI18N
            line = line.substring(5);

            if (LOGGABLE) {
                ERR.log("removing file:");
            }
        } else if ((line.length() > 0) && (line.charAt(0) == '/')) {
            if (LOGGABLE) {
                ERR.log("result: looks like Unix file");
            }
        } else if ((line.length() > 2) && (line.charAt(1) == ':') && (line.charAt(2) == '\\')) {
            if (LOGGABLE) {
                ERR.log("result: looks like Windows file");
            }
        } else {
            // not a file -> nothing to parse
            if (LOGGABLE) {
                ERR.log("result: not a file");
            }

            return null;
        }

        int colon1 = line.indexOf(':');

        if (colon1 == -1) {
            if (LOGGABLE) {
                ERR.log("result: no colon found");
            }

            return null;
        }

        String fileName = line.substring(0, colon1); //.replace(File.separatorChar, '/');
        File file = FileUtil.normalizeFile(new File(fileName));

        if (!file.exists()) {
            if (LOGGABLE) {
                ERR.log("result: no FO for " + fileName);
            }

            // maybe we are on Windows and filename is "c:\temp\file.java:25"
            // try to do the same for the second colon
            colon1 = line.indexOf(':', colon1 + 1);

            if (colon1 == -1) {
                if (LOGGABLE) {
                    ERR.log("result: no second colon found");
                }

                return null;
            }

            fileName = line.substring(0, colon1);
            file = FileUtil.normalizeFile(new File(fileName));

            if (!file.exists()) {
                if (LOGGABLE) {
                    ERR.log("result: no FO for " + fileName);
                }

                return null;
            }
        }

        int line1 = -1;
        int col1 = -1;
        int line2 = -1;
        int col2 = -1;
        int start = colon1 + 1; // start of message
        int colon2 = line.indexOf(':', colon1 + 1);

        if (colon2 != -1) {
            try {
                line1 = Integer.parseInt(line.substring(colon1 + 1, colon2).trim());
                start = colon2 + 1;

                int colon3 = line.indexOf(':', colon2 + 1);

                if (colon3 != -1) {
                    col1 = Integer.parseInt(line.substring(colon2 + 1, colon3).trim());
                    start = colon3 + 1;

                    int colon4 = line.indexOf(':', colon3 + 1);

                    if (colon4 != -1) {
                        line2 = Integer.parseInt(line.substring(colon3 + 1, colon4).trim());
                        start = colon4 + 1;

                        int colon5 = line.indexOf(':', colon4 + 1);

                        if (colon5 != -1) {
                            col2 = Integer.parseInt(line.substring(colon4 + 1, colon5).trim());

                            if (col2 == col1) {
                                col2 = -1;
                            }

                            start = colon5 + 1;
                        }
                    }
                }
            } catch (NumberFormatException nfe) {
                // Fine, rest is part of the message.
            }
        }

        String message = line.substring(start).trim();

        if (message.length() == 0) {
            message = null;
        }

        if (LOGGABLE) {
            ERR.log("Hyperlink: [" + file + "," + line1 + "," + col1 + "," + line2 + "," + col2 + "," + message + "]");
        }

        try {
            return session.createStandardHyperlink(file.toURI().toURL(), message, line1, col1, line2, col2);
        } catch (MalformedURLException e) {
            assert false : e;

            return null;
        }
    }

    /**
    * Data stored in the session.
    */
    private static final class SessionData {
        /** Time build was started. */
        public long startTime;

        /** Last-created hyperlink, in case we need to adjust the column number. */
        public Hyperlink lastHyperlink;

        public SessionData() {
        }
    }
}
