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
import org.netbeans.lib.cvsclient.command.log.LogCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.annotate.AnnotateCommand;
import org.netbeans.lib.cvsclient.command.annotate.AnnotateInformation;
import org.netbeans.lib.cvsclient.command.annotate.AnnotateLine;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;

import java.util.*;


/**
 * Executes a given 'log' command.
 * 
 * @author Petr Kuzel
 */
public class LogExecutor extends ExecutorSupport {

    private final List listeners = new ArrayList(1);

    public LogExecutor(CvsVersioningSystem cvs, LogCommand cmd) {
        this(cvs, cmd, null);
    }
    
    public LogExecutor(CvsVersioningSystem cvs, LogCommand cmd, GlobalOptions options) {
        super(cvs, cmd, options);
    }

    protected void commandFinished(ClientRuntime.Result result) {
    }

    public void fileInfoGenerated(FileInfoEvent e) {
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
}
