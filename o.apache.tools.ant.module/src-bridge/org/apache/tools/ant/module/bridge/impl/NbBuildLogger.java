/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick
 */

package org.apache.tools.ant.module.bridge.impl;

import java.io.*;
import java.text.MessageFormat;

import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

import org.apache.tools.ant.*;

/** NetBeans-sensitive build logger.
 */
final class NbBuildLogger implements BuildLogger {
    
    private boolean updateStatusLine;
    
    public NbBuildLogger(boolean updateStatusLine) {
        this.updateStatusLine = updateStatusLine;
    }
    
    // [PENDING] more coherent selection of messages based on verbosity
    
    private int level = Project.MSG_INFO;
    private PrintStream out, err;
    
    /** Time when build started. */
    private long startTime = System.currentTimeMillis();
    
    public void setEmacsMode(boolean ignore) {
        // Gack! Why is this part of an *interface*?!
    }
    
    public void setMessageOutputLevel(int l) {
        level = l;
    }
    
    public void setErrorPrintStream(PrintStream ps) {
        err = ps;
    }
    
    public void setOutputPrintStream(PrintStream ps) {
        out = ps;
    }
    
    public void messageLogged(BuildEvent ev) {
        if (ev.getPriority() <= level) {
            if (ev.getPriority() <= Project.MSG_WARN) {
                /* No good! Overlinks e.g. compile messages. How to solve??
                Task t = ev.getTask ();
                if (t != null) {
                    // This hyperlinks it:
                    err.print (t.getLocation ());
                }
                 */
                err.println(ev.getMessage());
            } else {
                out.println(ev.getMessage());
            }
        }
    }
    
    public void buildStarted(BuildEvent ev) {
        startTime = System.currentTimeMillis();
        
        if (updateStatusLine) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbBuildLogger.class, "MSG_running_ant"));
        }
        // no messages printed for now
    }
    
    public void buildFinished(BuildEvent ev) {
        Throwable t = ev.getException();
        // result message
        err.println();
        if (t == null) {
            // [PENDING] should check for target member (and TargetExecutor should set it...)
            err.println(NbBundle.getMessage(NbBuildLogger.class, "MSG_finished_target_printed"));
            if (updateStatusLine) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbBuildLogger.class, "MSG_finished_target_status"));
            }
        } else {
            if ((t instanceof BuildException) && level < Project.MSG_VERBOSE) {
                // Stack trace probably not required.
                err.println(t);
            } else {
                t.printStackTrace(err);
            }
            err.println(NbBundle.getMessage(NbBuildLogger.class, "MSG_target_failed_printed")); // #10305
            if (updateStatusLine) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbBuildLogger.class, "MSG_target_failed_status"));
            }
        }
        
        // time
        err.println();
        err.println(formatTime(System.currentTimeMillis() - startTime)); // #10305
    }
    
    public void targetStarted(BuildEvent ev) {
        out.println(NbBundle.getMessage(NbBuildLogger.class, "MSG_target_started_printed", ev.getTarget().getName()));
    }
    
    public void targetFinished(BuildEvent ev) {
        // ignore for now
    }
    
    public void taskStarted(BuildEvent ev) {
        // ignore for now
    }
    
    public void taskFinished(BuildEvent ev) {
        // ignore for now
    }
    
    /** Formats the millis in a human readable String.
     * Total time: {0} minutes
     *             {1} seconds
     */
    protected static String formatTime(long millis) {
        int secs = (int) (millis / 1000);
        int minutes = secs / 60;
        int seconds = secs % 60;
        
        // get resourcestring and set up MessageFormat
        String msgformat= NbBundle.getMessage(NbBuildLogger.class, "MSG_finished_target_time");
        MessageFormat mf = new MessageFormat(msgformat);
        
        // return formatted String
        Integer[] values = {new Integer(minutes), new Integer(seconds)};
        return mf.format(values);
    }
}
