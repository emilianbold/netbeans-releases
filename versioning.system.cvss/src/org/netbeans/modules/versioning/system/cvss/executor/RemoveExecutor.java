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

package org.netbeans.modules.versioning.system.cvss.executor;

import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.remove.RemoveCommand;
import org.netbeans.lib.cvsclient.command.remove.RemoveInformation;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.io.IOException;
import java.io.File;

/**
 * Executes cvs remove command.
 *
 * @author Maros Sandor
 */
public class RemoveExecutor extends ExecutorSupport {

    private Set refreshedFiles;

    /**
     * Splits the original command into more commands if the original
     * command would execute on incompatible files.
     * See {@link #prepareBasicCommand(org.netbeans.lib.cvsclient.command.BasicCommand)}
     * for more information.
     *
     * @param cmd command o execute
     * @param cvs CVS engine to use
     * @param options global option for the command
     * @return array of executors that will execute the command (or array of splitted commands)
     */
    public static RemoveExecutor [] splitCommand(RemoveCommand cmd, CvsVersioningSystem cvs, GlobalOptions options) {
        Command [] cmds = new org.netbeans.lib.cvsclient.command.Command[0];
        if (cmd.getDisplayName() == null) cmd.setDisplayName(NbBundle.getMessage(RemoveExecutor.class, "MSG_RemoveExecutor_CmdDisplayName"));
        try {
            cmds = prepareBasicCommand(cmd);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
        RemoveExecutor [] executors = new RemoveExecutor[cmds.length];
        for (int i = 0; i < cmds.length; i++) {
            Command command = cmds[i];
            executors[i] = new RemoveExecutor(cvs, (RemoveCommand) command, options);
        }
        return executors;
    }

    private RemoveExecutor(CvsVersioningSystem cvs, RemoveCommand cmd, GlobalOptions options) {
        super(cvs, cmd, options);
    }

    /**
     * Refreshes statuse of relevant files after this command terminates.
     */
    protected void commandFinished(ClientRuntime.Result result) {

        RemoveCommand xcmd = (RemoveCommand) cmd;

        // files that we have information that changed
        refreshedFiles = new HashSet(toRefresh.size());

        for (Iterator i = toRefresh.iterator(); i.hasNext();) {
            RemoveInformation info = (RemoveInformation) i.next();
            if (info.getFile() == null) continue;
            int repositoryStatus = FileStatusCache.REPOSITORY_STATUS_UNKNOWN;
            if (info.isRemoved()) {
                repositoryStatus = FileStatusCache.REPOSITORY_STATUS_REMOVED;
            } else {
                repositoryStatus = FileStatusCache.REPOSITORY_STATUS_UNKNOWN;
            }
            cache.refreshCached(info.getFile(), repositoryStatus);
            refreshedFiles.add(info.getFile());
        }

        if (cmd.hasFailed()) return;

        // refresh all command roots
        File [] files = xcmd.getFiles();
        for (int i = 0; i < files.length; i++) {
            refreshRecursively(files[i]);
            FileObject fo = FileUtil.toFileObject(files[i]);
            if (fo != null) {
                fo.refresh(true);
            }
        }
    }

    private void refreshRecursively(File file) {
        if (cvs.isIgnoredFilename(file)) return;
        if (refreshedFiles.contains(file)) return;
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                refreshRecursively(files[i]);
            }
            cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        } else {
            cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }
}
