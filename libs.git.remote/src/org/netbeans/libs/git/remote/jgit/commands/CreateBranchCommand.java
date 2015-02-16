/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.libs.git.remote.jgit.commands;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.remote.GitBranch;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.jgit.DelegatingGitProgressMonitor;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.jgit.Utils;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VersioningSupport;

/**
 *
 * @author ondra
 */
public class CreateBranchCommand extends GitCommand {
    public static final boolean KIT = false;
    private final String revision;
    private final String branchName;
    private GitBranch branch;
    private final ProgressMonitor monitor;
    private static final Logger LOG = Logger.getLogger(CreateBranchCommand.class.getName());

    public CreateBranchCommand (JGitRepository repository, GitClassFactory gitFactory, String branchName, String revision, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.branchName = branchName;
        this.revision = revision;
        this.monitor = monitor;
    }
    
    @Override
    protected void run () throws GitException {
        if (KIT) {
            runKit();
        } else {
            runCLI();
        }
    }

//<editor-fold defaultstate="collapsed" desc="KIT">
    protected void runKit () throws GitException {
        Repository repository = getRepository().getRepository();
        org.eclipse.jgit.api.CreateBranchCommand cmd = new Git(repository).branchCreate();
        cmd.setName(branchName);
        if (revision.startsWith(Constants.R_HEADS) || revision.startsWith(Constants.R_REMOTES)) {
            cmd.setUpstreamMode(SetupUpstreamMode.TRACK);
        } else {
            Utils.findCommit(repository, revision); // does it exist?
        }
        cmd.setStartPoint(revision);
        String createdBranchName = branchName;
        try {
            Ref ref = cmd.call();
            createdBranchName = ref.getName().substring(Constants.R_HEADS.length());
        } catch (RefNotFoundException ex) {
            if (!createBranchInEmptyRepository(repository)) {
                throw new GitException(ex);
            }
        } catch (JGitInternalException | GitAPIException ex) {
            throw new GitException(ex);
        }
        ListBranchCommand branchCmd = new ListBranchCommand(getRepository(), getClassFactory(), false, new DelegatingGitProgressMonitor(monitor));
        branchCmd.run();
        Map<String, GitBranch> branches = branchCmd.getBranches();
        branch = branches.get(createdBranchName);
        if (branch == null) {
            LOG.log(Level.WARNING, "Branch {0}/{1} probably created but not in the branch list: {2}",
                    new Object[] { branchName, createdBranchName, branches.keySet() });
        }
    }

    private boolean createBranchInEmptyRepository (Repository repository) throws GitException {
        // is this an empty repository after a fresh clone of an empty repository?
        if (revision.startsWith(Constants.R_REMOTES)) {
            try {
                if (Utils.parseObjectId(repository, Constants.HEAD) == null) {
                    StoredConfig config = repository.getConfig();
                    String[] elements = revision.split("/", 4);
                    String remoteName = elements[2];
                    String remoteBranchName = elements[3];
                    config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
                            ConfigConstants.CONFIG_KEY_REMOTE, remoteName);
                    config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
                            ConfigConstants.CONFIG_KEY_MERGE, Constants.R_HEADS + remoteBranchName);
                    config.save();
                    return true;
                }
            } catch (IOException ex) {
                throw new GitException(ex);
            } catch (GitException ex) {
                throw ex;
            }
        }
        return false;
    }

    public GitBranch getBranch () {
        return branch;
    }
//</editor-fold>
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(3);
        super.prepare();
        addArgument(0, "branch"); //NOI18N
        addArgument(0, "--track"); //NOI18N
        addArgument(0, branchName);
        addArgument(0, revision);
        addArgument(1, "branch"); //NOI18N
        addArgument(1, "--track"); //NOI18N
        addArgument(1, branchName);
        addArgument(2, "branch"); //NOI18N
        addArgument(2, "-vv"); //NOI18N
        addArgument(2, "--all"); //NOI18N
    }
    
    private void runCLI() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        String cmd = getCommandLine();
        try {
            Map<String, GitBranch> branches = new LinkedHashMap<String, GitBranch>();
            boolean runner = runner(canceled, 0, branches);
            if (!runner) {
                runner(canceled, 1, branches);
            }
            runner(canceled, 2, branches);
            branch = branches.get(branchName);
            if (branch == null) {
                LOG.log(Level.WARNING, "Branch {0}/{1} probably created but not in the branch list: {2}",
                        new Object[] { branchName, branchName, branches.keySet() });
            }
            
            //command.commandCompleted(exitStatus.exitCode);
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        } finally {
            //command.commandFinished();
        }
    }
    
    private boolean runner(ProcessUtils.Canceler canceled, int command, Map<String, GitBranch> branches) {
        if(canceled.canceled()) {
            return true;
        }
        org.netbeans.api.extexecution.ProcessBuilder processBuilder = VersioningSupport.createProcessBuilder(getRepository().getLocation());
        String executable = getExecutable();
        String[] args = getCliArguments(command);
        ProcessUtils.ExitStatus exitStatus = ProcessUtils.executeInDir(getRepository().getLocation().getPath(), null, false, canceled, processBuilder, executable, args); //NOI18N
        if(canceled.canceled()) {
            return true;
        }
        if (exitStatus.output!= null && exitStatus.isOK()) {
            ListBranchCommand.parseBranches(exitStatus.output, getClassFactory(), branches);
        }
        if (exitStatus.error != null && !exitStatus.isOK()) {
            if (exitStatus.error.contains("fatal: Cannot setup tracking information; starting point is not a branch.")) {
                return false;
            }
        }
        return true;
    }
    
}
