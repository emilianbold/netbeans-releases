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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.ErrorManager;
import org.openide.util.Cancellable;

/**
 * Runnable that actually performs a command and stores an exception it may throw and its isFinished state.
 * 
 * @author Maros Sandor
 */
class CommandRunnable implements Runnable, Cancellable {

    private final Client        client;
    private final GlobalOptions options;
    private final Command       cmd;
    private Throwable           failure;
    
    private boolean             finished;
    private boolean             aborted;
    private ProgressHandle      progressHandle;

    public CommandRunnable(Client client, GlobalOptions options, Command cmd) {
        this.client = client;
        this.options = options;
        this.cmd = cmd;
    }

    void setProgressHandle(ProgressHandle handle) {
        progressHandle = handle;
    }
    
    public void run() {
        progressHandle.start();
        try {
            client.executeCommand(cmd, options);
        } catch (Throwable e) {
            failure = e;
        } finally {
            progressHandle.finish();
            finished = true;
            try {
                client.getConnection().close();
            } catch (Throwable e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
    }
    
    public Throwable getFailure() {
        return failure;
    }
    
    public boolean isFinished() {
        return finished;
    }

    public boolean isAborted() {
        return aborted;
    }

    public boolean cancel() {
        if (finished || aborted) return false;
        aborted = true;
        client.abort();
        Thread.currentThread().interrupt();
        return true;
    }
}
