/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.versioning.system.cvss.executor;

import org.openide.util.NbBundle;
import org.openide.ErrorManager;
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
