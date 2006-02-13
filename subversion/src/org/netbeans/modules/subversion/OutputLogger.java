/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion;

import java.io.File;
import java.io.IOException;
import org.openide.ErrorManager;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;

/**
 *
 * @author Tomas Stupka
 * 
 * XXX Dummy output logging implementation ...
 *
 */
public class OutputLogger implements ISVNNotifyListener {
    
    private InputOutput log;
    private boolean ignoreCommand = false;
    
    public OutputLogger() {
        log = IOProvider.getDefault().getIO("SVN Output", false);        
    }
    
    public void logCommandLine(String commandLine) {
        logln(commandLine, false);
        flushLog();
    }
    
    public void logCompleted(String message) {
        logln(message, ignoreCommand);
        closeLog();
    }
    
    public void logError(String message) {
        logln(message, false);
        flushLog();
    }
    
    public void logMessage(String message) {
        logln(message, ignoreCommand);
        flushLog(); 
    }
    
    public void logRevision(long revision, String path) {
       // logln(" revision " + revision + ", path = '" + path + "'");
    }
    
    public void onNotify(File path, SVNNodeKind kind) {
        //logln(" file " + path + ", kind " + kind);
    }
    
    public void setCommand(int command) {
        ignoreCommand = command == ISVNNotifyListener.Command.INFO ||
                        command == ISVNNotifyListener.Command.STATUS ||
                        command == ISVNNotifyListener.Command.LS;
    }
            
    private void logln(String message, boolean ignore) {
        log(message + "\n", null, ignore);
    }
    
    private void log(String message, boolean ignore) {
        log(message, null, ignore);
    }
    
    private void log(String message, OutputListener hyperlinkListener, boolean ignore) {        
        if(ignore) {
            return;
        }
        if (log.isClosed()) {
            log = IOProvider.getDefault().getIO("SVN Output", false);
            try {
                // XXX workaround, otherwise it writes to nowhere
                log.getOut().reset();
            } catch (IOException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.notify(e);
            }
            //log.select();
        }
        if (hyperlinkListener != null) {
            try {
                log.getOut().println(message, hyperlinkListener);
            } catch (IOException e) {
                log.getOut().write(message);
            }
        } else {
            log.getOut().write(message);
        }
    }    

    private void logError(Throwable e) {
        e.printStackTrace(log.getOut());
    }

    private void focusLog() {
        log.select();
    }

    private void flushLog() {
        log.getOut().flush(); // XXX in CVS is only close ???
    }

    private void closeLog() {
        log.getOut().close();
    }
    
}
