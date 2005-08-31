/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.log;

import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.log.LogCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

import java.util.*;
import java.io.IOException;


/**
 * Executes a given 'log' command.
 * 
 * @author Petr Kuzel
 * @author Maros Sandor
 */
public class LogExecutor extends ExecutorSupport {

    private final List listeners = new ArrayList(1);

    private boolean silent;

    /**
     * Executes the given command by posting it to CVS module engine. It returns immediately, the command is
     * executed in the background. This method may split the original command into more commands if the original
     * command would execute on incompatible files. See {@link #prepareBasicCommand(org.netbeans.lib.cvsclient.command.BasicCommand)} 
     * for more information.
     * 
     * @param cmd command o execute
     * @param options global option for the command
     * @return array of executors that will execute the command (or array of splitted commands)
     */ 
    public static LogExecutor [] executeCommand(LogCommand cmd, GlobalOptions options) {
        Command [] cmds = new org.netbeans.lib.cvsclient.command.Command[0];
        if (cmd.getDisplayName() == null) cmd.setDisplayName(NbBundle.getMessage(LogExecutor.class, "MSG_LogExecutor_CmdDisplayName"));
        try {
            cmds = prepareBasicCommand(cmd);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
        LogExecutor [] executors = new LogExecutor[cmds.length]; 
        for (int i = 0; i < cmds.length; i++) {
            Command command = cmds[i];
            executors[i] = new LogExecutor(CvsVersioningSystem.getInstance(), (LogCommand) command, options);
            executors[i].execute();
        }
        return executors;
    }
    
    public LogExecutor(CvsVersioningSystem cvs, LogCommand cmd) {
        this(cvs, cmd, null);
    }
    
    public LogExecutor(CvsVersioningSystem cvs, LogCommand cmd, GlobalOptions options) {
        super(cvs, cmd, options);
    }

    protected void commandFinished(ClientRuntime.Result result) {
    }

    public void fileInfoGenerated(FileInfoEvent e) {
        super.fileInfoGenerated(e);
        LogInformation li = (LogInformation) e.getInfoContainer();
        List revisions = li.getRevisionList();
        Iterator rit = revisions.iterator();
        Map messages = new HashMap(revisions.size());
        while (rit.hasNext()) {
            LogInformation.Revision rev = (LogInformation.Revision) rit.next();
            String message = rev.getMessage();
            String revId = rev.getNumber();
            messages.put(revId, message);
        }
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            LogOutputListener listener = (LogOutputListener) it.next();
            listener.commitMessages(messages);
        }
    }

    public void addLogOutputListener(LogOutputListener listener) {
        listeners.add(listener);
    }

    public List getLogEntries() {
        return toRefresh;
    }

    protected boolean logCommandOutput() {
        return silent == false;
    }

    void setSilent(boolean silent) {
        this.silent = silent;
    }
}
