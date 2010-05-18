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

package org.netbeans.modules.versioning.system.cvss.ui.actions.commit;

import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.commit.CommitInformation;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import java.util.*;
import java.io.File;
import java.io.IOException;

/**
 * Executes a given 'commit' command and refreshes file statuses.
 * 
 * @author Maros Sandor
 */
public class CommitExecutor extends ExecutorSupport {
    
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
    public static CommitExecutor [] splitCommand(CommitCommand cmd, CvsVersioningSystem cvs, GlobalOptions options) {
        Command [] cmds = new org.netbeans.lib.cvsclient.command.Command[0];
        ResourceBundle loc = NbBundle.getBundle(CommitExecutor.class);
        if (cmd.getDisplayName() == null) cmd.setDisplayName(loc.getString("MSG_CommitExecutor_CmdDisplayName"));
        try {
            cmds = prepareBasicCommand(cmd);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
        CommitExecutor [] executors = new CommitExecutor[cmds.length]; 
        for (int i = 0; i < cmds.length; i++) {
            Command command = cmds[i];
            executors[i] = new CommitExecutor(cvs, (CommitCommand) command, options);
        }
        return executors;
    }
    
    private CommitExecutor(CvsVersioningSystem cvs, CommitCommand cmd, GlobalOptions options) {
        super(cvs, cmd, options);
    }

    /**
     * Refreshes statuse of relevant files after this command terminates.
     */
    protected void commandFinished(ClientRuntime.Result result) {
        
        CommitCommand xcmd = (CommitCommand) cmd;
        
        for (Iterator i = toRefresh.iterator(); i.hasNext();) {
            CommitInformation info = (CommitInformation) i.next();
            if (info.getFile() == null) continue;
            int repositoryStatus = FileStatusCache.REPOSITORY_STATUS_UNKNOWN;
            String type = info.getType();
            if (CommitInformation.CHANGED.equals(type) || CommitInformation.ADDED.equals(type)) {
                repositoryStatus = FileStatusCache.REPOSITORY_STATUS_UPTODATE;                
            } else if (CommitInformation.REMOVED.equals(type) || CommitInformation.TO_ADD.equals(type)) {
                repositoryStatus = FileStatusCache.REPOSITORY_STATUS_UNKNOWN;
            }
            cache.refreshCached(info.getFile(), repositoryStatus);
        }

        if (cmd.hasFailed()) return;

    }

}
