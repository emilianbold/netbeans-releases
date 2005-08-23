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
import org.netbeans.lib.cvsclient.command.PipedFileInformation;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Fetches given revision.
 *
 * @author Petr Kuzel
 */
final class VersionsCacheExecutor extends ExecutorSupport {

    private boolean finished;
    private File checkedOutVersion;
    private ClientRuntime.Result result;

    public VersionsCacheExecutor(Command cmd, GlobalOptions options) {
        super(CvsVersioningSystem.getInstance(), cmd, options);
    }

    protected synchronized void commandFinished(ClientRuntime.Result result) {
        finished = true;
        this.result = result;
        if (checkedOutVersion == null) {
            System.err.println("CVS: " + cmd.getCVSCommand() + " misses piped response!");
        }
        notifyAll();
    }

    public void fileInfoGenerated(FileInfoEvent e) {
        PipedFileInformation info = (PipedFileInformation) e.getInfoContainer();
        checkedOutVersion = FileUtil.normalizeFile(info.getTempFile());
    }

    // TODO move to ExecutorSupport directly?
    /**
     * Waits for command termination.
     *
     * @throws Throwable on any error {@link #getCheckedOutVersion()} will return <code>null</code>
     */
    public final synchronized void waitFinished() throws Throwable {
        try {
            if (getTask() == null) throw new Exception("Command was not executed");
            while (finished == false) {
                wait();
            }
            if (result.isAborted()) {
                throw new IOException("Aborted by user");
            } else if (result.getError() != null) {
                throw result.getError();
            }
        } catch (InterruptedException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Revision loading interrupted");
            throw e;
        }
    }

    public File getCheckedOutVersion() {
        return checkedOutVersion;
    }
}
