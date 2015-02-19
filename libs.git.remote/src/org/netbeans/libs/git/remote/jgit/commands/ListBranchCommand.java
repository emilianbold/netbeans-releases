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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.libs.git.remote.GitBranch;
import org.netbeans.libs.git.remote.GitConstants;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VersioningSupport;

/**
 *
 * @author ondra
 */
public class ListBranchCommand extends GitCommand {
    public static final boolean KIT = false;
    private final boolean all;
    private final ProgressMonitor monitor;
    private Map<String, GitBranch> branches;

    public ListBranchCommand (JGitRepository repository, GitClassFactory gitFactory, boolean all, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.all = all;
        this.monitor = monitor;
    }

    @Override
    protected void run () throws GitException {
        if (KIT) {
            //runKit();
        } else {
            runCLI();
        }
    }

    public Map<String, GitBranch> getBranches () {
        return branches;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "branch"); //NOI18N
        addArgument(0, "-vv"); //NOI18N
        if (all) {
            addArgument(0, "--all"); //NOI18N
        }
    }
    
    private void runCLI() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        String cmd = getCommandLine();
        branches = new LinkedHashMap<String, GitBranch>();
        try {
            runner(canceled, 0, new Parser() {

                @Override
                public void outputParser(String output) {
                    parseBranches(output, getClassFactory(), branches);
                }
            });
            
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

    private void runner(ProcessUtils.Canceler canceled, int command, Parser parser) {
        if(canceled.canceled()) {
            return;
        }
        org.netbeans.api.extexecution.ProcessBuilder processBuilder = VersioningSupport.createProcessBuilder(getRepository().getLocation());
        String executable = getExecutable();
        String[] args = getCliArguments(command);
        ProcessUtils.ExitStatus exitStatus = ProcessUtils.executeInDir(getRepository().getLocation().getPath(), getEnvVar(), false, canceled, processBuilder, executable, args); //NOI18N
        if(canceled.canceled()) {
            return;
        }
        if (exitStatus.output!= null && exitStatus.isOK()) {
            parser.outputParser(exitStatus.output);
        }
        if (exitStatus.error != null && !exitStatus.isOK()) {
            parser.errorParser(exitStatus.error);
        }
    }
    
    static void parseBranches(String output, GitClassFactory factory, Map<String, GitBranch> branches) {
        //#git branch -a -v
        //* master                18d0fec Post 2.3 cycle (batch #1)
        //  remotes/origin/HEAD   -> origin/master
        //  remotes/origin/maint  9874fca Git 2.3
        //  remotes/origin/master 18d0fec Post 2.3 cycle (batch #1)
        //  remotes/origin/next   021ec32 Merge branch 'mg/push-repo-option-doc' into next
        //  remotes/origin/pu     f5d0ad1 Merge branch 'nd/slim-index-pack-memory-usage' into pu
        //  remotes/origin/todo   5914a77 Meta/Announce: adjust to possible change to the top-level RelNotes
        //#git branch -vv --all
        //* master                8408c76 [origin/master] init commit
        //  remotes/origin/master 8408c76 init commit        
        //#git branch -vv --all
        //* master                79c5362 [origin/master] init commit
        //  nova1                 79c5362 [master] init commit
        //  remotes/origin/master 79c5362 init commit
        //#git branch -vv --all
        //* master    295d064 commit
        //  newbranch fa663c1 [master: behind 2] initial commit        
        //#git branch -vv --all
        //  master    699a8aa commit
        //* newbranch 1e2f87c [master: ahead 1, behind 1] commit
        //#git branch -vv --all
        //* (no branch) 024ac8c change
        //  master      024ac8c change
        //  nova        024ac8c change
        HashMap<String, String> links = new HashMap<String,String>();
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("* ") || line.startsWith("  ")) {
                boolean def = '*' == line.charAt(0);
                line = line.substring(2);
                String branchName = null;
                if (line.startsWith("(no branch)")) {
                    branchName = "(no branch)";
                    line = "(no_branch)"+line.substring(11);
                }
                String[] s = line.split("\\s+");
                if (s.length > 1 && "->".equals(s[1])) {
                    continue;
                }
                if (branchName == null) {
                    branchName = s[0];
                }
                boolean remote = branchName.startsWith("remotes/");
                if (remote) {
                    branchName = branchName.substring(8);
                }
                GitBranch createBranch;
                if (s.length > 1) {
                    int i = line.indexOf('[');
                    int j = line.indexOf(']');
                    if (i > 0 && j > 0 && i < j) {
                        String link = line.substring(i+1, j);
                        int k = link.indexOf(':');
                        if (k > 0) {
                            links.put(branchName, link.substring(0, k));
                        } else {
                            links.put(branchName, link);
                        }
                    }
                    createBranch = factory.createBranch(branchName, remote, def, s[1]);
                } else {
                    createBranch = factory.createBranch(branchName, remote, def, GitConstants.HEAD);
                }
                branches.put(branchName, createBranch);
                //public abstract GitBranch createBranch (String name, boolean remote, boolean active, ObjectId id);
                continue;
            }
        }
        for (Map.Entry<String, String> entry : links.entrySet()) {
            GitBranch orig = branches.get(entry.getKey());
            GitBranch link = branches.get(entry.getValue());
            if (orig != null && link != null && link.isRemote()) {
                factory.setBranchTracking(orig, link);
            }
        }
    }

    private abstract class Parser {
        public abstract void outputParser(String output);
        public void errorParser(String error){
        }
    }
}
