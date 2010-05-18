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

package org.netbeans.modules.versioning.system.cvss.ui.actions.log;

import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;
import org.netbeans.modules.versioning.system.cvss.util.CommandDuplicator;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.netbeans.lib.cvsclient.event.MessageEvent;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import java.util.*;
import java.io.File;
import java.io.IOException;

/**
 * Executes a given 'rlog' command.
 * 
 * @author Maros Sandor
 */
public class RLogExecutor extends ExecutorSupport {
    
    private final File localRoot;
    private boolean     failedOnSymbolicLink;

    /**
     * Splits the original command into more commands if the original
     * command would execute on incompatible files.
     * See {@link #prepareBasicCommand(org.netbeans.lib.cvsclient.command.BasicCommand)}
     * for more information.
     *
     * @param cmd command o execute
     * @param roots folders that represent remote repositories to operate on
     * @param options global option for the command
     * @return array of executors that will execute the command (or array of splitted commands)
     */ 
    public static RLogExecutor [] splitCommand(RlogCommand cmd, File [] roots, GlobalOptions options) {
        if (cmd.getDisplayName() == null) cmd.setDisplayName(NbBundle.getMessage(RLogExecutor.class, "MSG_RLogExecutor_CmdDisplayName"));
        if (options == null) options = CvsVersioningSystem.createGlobalOptions();
        
        CvsVersioningSystem cvs = CvsVersioningSystem.getInstance();
        AdminHandler ah = cvs.getAdminHandler();
        CommandDuplicator cloner = CommandDuplicator.getDuplicator(cmd);

        List executors = new ArrayList();
        try {
            File [][] split = ExecutorSupport.splitByCvsRoot(roots);
            for (int i = 0; i < split.length; i++) {
                File [] files = split[i];
                GlobalOptions currentOptions = (GlobalOptions) options.clone();
                currentOptions.setCVSRoot(Utils.getCVSRootFor(files[0]));
                String remoteRepository = null;
                File directory = null;
                for (int j = 0; j < files.length; j++) {
                    File file = files[j];
                    File dir = file.isDirectory() ? file : file.getParentFile();
                    String repository = ah.getRepositoryForDirectory(dir.getAbsolutePath(), "").substring(1); // NOI18N
                    if (remoteRepository == null || remoteRepository.equals(repository)) {
                        remoteRepository = repository;
                        directory = dir;
                    } else {
                        RlogCommand command = (RlogCommand) cloner.duplicate();
                        command.setModule(remoteRepository);
                        command.setDisplayName(NbBundle.getMessage(RLogExecutor.class, "MSG_RLogExecutor_CmdContext", remoteRepository));
                        RLogExecutor executor = new RLogExecutor(cvs, command, directory, currentOptions);
                        executors.add(executor);
                        remoteRepository = repository;
                        directory = dir;
                    }
                }
                RlogCommand command = (RlogCommand) cloner.duplicate();
                command.setModule(remoteRepository);
                command.setDisplayName(NbBundle.getMessage(RLogExecutor.class, "MSG_RLogExecutor_CmdContext", remoteRepository));
                RLogExecutor executor = new RLogExecutor(cvs, command, directory, currentOptions);
                executors.add(executor);
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return new RLogExecutor[0];
        }
        return (RLogExecutor[]) executors.toArray(new RLogExecutor[executors.size()]);
    }

    private RLogExecutor(CvsVersioningSystem cvs, RlogCommand cmd, File localRoot, GlobalOptions options) {
        super(cvs, cmd, options);
        this.localRoot = localRoot;
    }
    
    public File getFile() {
        return localRoot;
    }

    public void fileInfoGenerated(FileInfoEvent e) {
        LogInformation information = (LogInformation) e.getInfoContainer();
        String remotePath = information.getRepositoryFilename();
        File f = remote2local(remotePath);
        information.setFile(f);
        super.fileInfoGenerated(e);
    }

    protected void commandFinished(ClientRuntime.Result result) {
        // repository command, nothing to do here
    }

    /**
     * Translates repository path of a file to local path in user workdir. This includes some heuristics
     * because we cannot do not know the actual path to repository on server.
     * 
     * @param remotePath remote path on server as reposted by the rlog command
     * @return File local location of the remote file (may not exist if the file is not checked out)
     */ 
    private File remote2local(String remotePath) {
        AdminHandler ah = CvsVersioningSystem.getInstance().getAdminHandler();
        String repository;
        try {
            // /javacvs/cvsmodule
            repository = ah.getRepositoryForDirectory(localRoot.getAbsolutePath(), "").substring(1); // NOI18N
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
        
        int idx = remotePath.indexOf(repository);
        if (idx == -1) {
            return null;
        }
        idx = remotePath.indexOf('/', idx + repository.length());
        if (idx == -1) {
            return null;
        }
        String remoteRelativePath = remotePath.substring(idx);
        
        idx = remoteRelativePath.indexOf(',');
        if (idx != -1) {
            remoteRelativePath = remoteRelativePath.substring(0, idx);
        }

        idx = remoteRelativePath.lastIndexOf('/');
        if (idx != -1 && idx >= 6) {
            if ("/Attic".equals(remoteRelativePath.substring(idx - 6, idx))) { // NOI18N
                remoteRelativePath = remoteRelativePath.substring(0, idx - 6) + remoteRelativePath.substring(idx);
            }
        }
        
        return new File(localRoot, remoteRelativePath);
    }

    public List getLogEntries() {
        return toRefresh;
    }

    /**
     * Overridden to detect failures due to symbolic link server misconfiguration.
     * 
     * @param e a message
     */ 
    public void messageSent(MessageEvent e) {
        super.messageSent(e);
        if (!failedOnSymbolicLink && e.isError()) {
            String msg = e.getMessage();
            failedOnSymbolicLink = msg != null && msg.indexOf("failed assertion `strncmp (repository,") != -1;
        }
    }

    public boolean hasFailedOnSymbolicLink() {
        return failedOnSymbolicLink;
    }
    
    /**
     * Does not log anything by default.
     * 
     * @return false
     */ 
    protected boolean logCommandOutput() {
        return false;
    }

    /**
     * Be quiet if the command failed due to symbolic link problems.
     */ 
    protected void report(String title, String prompt, List messages, int type) {
        if (!failedOnSymbolicLink) super.report(title, prompt, messages, type);
    }

}
