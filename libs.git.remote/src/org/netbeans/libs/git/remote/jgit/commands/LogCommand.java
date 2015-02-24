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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.libs.git.remote.GitBranch;
import org.netbeans.libs.git.remote.GitConstants;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.GitObjectType;
import org.netbeans.libs.git.remote.GitRevisionInfo;
import org.netbeans.libs.git.remote.SearchCriteria;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.libs.git.remote.progress.RevisionInfoListener;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VersioningSupport;

/**
 *
 * @author ondra
 */
public class LogCommand extends GitCommand {
    public static final boolean KIT = false;
    private final ProgressMonitor monitor;
    private final RevisionInfoListener listener;
    private final List<GitRevisionInfo> revisions;
    private final String revision;
    private final SearchCriteria criteria;
    private final boolean fetchBranchInfo;
    private final Revision revisionPlaseHolder;
    private static final Logger LOG = Logger.getLogger(LogCommand.class.getName());

    public LogCommand (JGitRepository repository, GitClassFactory gitFactory, SearchCriteria criteria,
            boolean fetchBranchInfo, ProgressMonitor monitor, RevisionInfoListener listener) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.listener = listener;
        this.criteria = criteria;
        this.fetchBranchInfo = fetchBranchInfo;
        this.revision = null;
        this.revisions = new LinkedList<>();
        if (fetchBranchInfo) {
            this.revisionPlaseHolder = new Revision();
        } else {
            this.revisionPlaseHolder = null;
        }
    }
    
    public LogCommand (JGitRepository repository, GitClassFactory gitFactory, String revision, ProgressMonitor monitor, RevisionInfoListener listener) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.listener = listener;
        this.criteria = null;
        this.fetchBranchInfo = false;
        this.revision = revision;
        this.revisions = new LinkedList<>();
        this.revisionPlaseHolder = null;
    }
    
    @Override
    protected void run () throws GitException {
        if (KIT) {
            //runKit();
        } else {
            runCLI();
        }
    }

    public GitRevisionInfo[] getRevisions () {
        return revisions.toArray(new GitRevisionInfo[revisions.size()]);
    }
    
    @Override
    protected void prepare() throws GitException {
        if (fetchBranchInfo) {
            setCommandsNumber(2);
        }
        super.prepare();
        addArgument(0, "log"); //NOI18N
        addArgument(0, "--raw"); //NOI18N
        addArgument(0, "--pretty=raw"); //NOI18N
        if (criteria != null && criteria.isFollow() && criteria.getFiles() != null && criteria.getFiles().length == 1) {
            addArgument(0, "--follow"); //NOI18N
        }
        if (criteria != null && !criteria.isIncludeMerges()) {
            addArgument(0, "--no-merges"); //NOI18N
        } else {
            addArgument(0, "-m"); //NOI18N
        }
        
        if (revision != null) {
            addArgument(0, "--no-walk"); //NOI18N
            addArgument(0, revision);
        } else if (criteria.getRevisionTo() != null && criteria.getRevisionFrom() != null) {
            if (criteria.getRevisionFrom().equals(criteria.getRevisionTo())) {
                addArgument(0, criteria.getRevisionFrom());
            } else {
                addArgument(0, criteria.getRevisionFrom()+"^.."+criteria.getRevisionTo());
            }
        } else if (criteria.getRevisionTo() != null) {
            addArgument(0, criteria.getRevisionTo());
        } else if (criteria.getRevisionFrom() != null) {
            addArgument(0, criteria.getRevisionFrom()+"^..");
        } else {
            addArgument(0, "--all");
        }
        if (criteria != null && criteria.getUsername() != null) {
            addArgument(0, "--author="+criteria.getUsername());
        }
        if (criteria != null && criteria.getMessage() != null) {
            String pattern = criteria.getMessage();
            if (pattern.indexOf('\n')>=0) {
                pattern = pattern.substring(0,pattern.indexOf('\n'));
            }
            if (!pattern.startsWith("^") && !pattern.startsWith(".*")) {
                pattern = ".*" + pattern;
            }
            if (!pattern.endsWith("$") && !pattern.endsWith(".*")) {
                pattern = pattern + ".*";
            }
            addArgument(0, "--grep="+pattern);
        }
        if (criteria != null && criteria.getFrom() != null && criteria.getTo() != null) {
            addArgument(0, "--since="+criteria.getFrom().toString());
            addArgument(0, "--until="+criteria.getTo().toString());
        } else if (criteria != null && criteria.getFrom() != null) {
            addArgument(0, "--since="+criteria.getFrom().toString());
        } else if (criteria != null && criteria.getTo() != null) {
            addArgument(0, "--until="+criteria.getTo().toString());
        }
        if (criteria != null && criteria.getLimit() > 0) {
            addArgument(0, "-"+criteria.getLimit());
        }
        
        if (criteria != null && criteria.getFiles().length > 0) {
            addArgument(0, "--full-diff");
            addArgument(0, "--");
            addFiles(0, criteria.getFiles());
        }
        if (fetchBranchInfo) {
            addArgument(1, "branch");
            addArgument(1, "-vv"); //NOI18N
            addArgument(1, "--all"); //NOI18N
            addArgument(1, "--contains");
            addArgument(1, revisionPlaseHolder);
        }
        
    }
    
    private void runCLI() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        String cmd = getCommandLine();
        try {
            LinkedHashMap<String, GitRevisionInfo.GitRevCommit> statuses = new LinkedHashMap<String, GitRevisionInfo.GitRevCommit>();
            runner(canceled, 0, statuses, new Parser() {

                @Override
                public void outputParser(String output, LinkedHashMap<String, GitRevisionInfo.GitRevCommit> statuses) {
                    parseLog(output, statuses);
                }

                @Override
                public void errorParser(String error) throws GitException.MissingObjectException {
                    for (String msg : error.split("\n")) { //NOI18N
                        if (msg.startsWith("fatal: Invalid object")) {
                            throw new GitException.MissingObjectException(GitConstants.HEAD ,GitObjectType.COMMIT);
                        }
                    }
                }

            });
            for(Map.Entry<String, GitRevisionInfo.GitRevCommit> entry : statuses.entrySet()) {
                if (fetchBranchInfo) {
                    revisionPlaseHolder.setContent(entry.getKey());
                    Map<String, GitBranch> branches = new LinkedHashMap<String, GitBranch>();
                    runner2(canceled, 1, branches);
                    revisions.add(getClassFactory().createRevisionInfo(entry.getValue(), branches, getRepository()));
                } else {
                    revisions.add(getClassFactory().createRevisionInfo(entry.getValue(), getRepository()));
                }
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
    
    private void runner(ProcessUtils.Canceler canceled, int command, LinkedHashMap<String, GitRevisionInfo.GitRevCommit> statuses, Parser parser) throws IOException, GitException.MissingObjectException {
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
        if (exitStatus.error != null && !exitStatus.isOK()) {            
            parser.errorParser(exitStatus.error);
        }
        if (exitStatus.output!= null && exitStatus.isOK()) {
            parser.outputParser(exitStatus.output, statuses);
        }
    }

    private boolean runner2(ProcessUtils.Canceler canceled, int command, Map<String, GitBranch> branches) throws IOException, GitException.MissingObjectException {
        if(canceled.canceled()) {
            return false;
        }
        org.netbeans.api.extexecution.ProcessBuilder processBuilder = VersioningSupport.createProcessBuilder(getRepository().getLocation());
        String executable = getExecutable();
        String[] args = getCliArguments(command);
        ProcessUtils.ExitStatus exitStatus = ProcessUtils.executeInDir(getRepository().getLocation().getPath(), getEnvVar(), false, canceled, processBuilder, executable, args); //NOI18N
        if(canceled.canceled()) {
            return false;
        }
        if (exitStatus.error != null && !exitStatus.isOK()) {            
            return false;
        }
        if (exitStatus.output!= null && exitStatus.isOK()) {
            ListBranchCommand.parseBranches(exitStatus.output, getClassFactory(), branches);
        }
        return true;
    }

    private void parseLog(String output, LinkedHashMap<String, GitRevisionInfo.GitRevCommit> statuses) {
        //#git --no-pager log --name-status --no-walk 0254bffe448b1951af6edef531d80f8e629c575a"
        //commit 9c0e341a6a9197e2408862d2e6ff4b7635a01f9b (from 19f759b14972f669dc3eb203c06944e03365f6bc)
        //Merge: 1126f32 846626a
        //Author: Alexander Simon <alexander.simon@oracle.com>
        //Date:   Tue Feb 17 16:12:39 2015 +0300
        //
        //    Merge b
        GitRevisionInfo.GitRevCommit status = new GitRevisionInfo.GitRevCommit();
        StringBuilder buf = new StringBuilder();
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("committer")) {
                String s = line.substring(9).trim();
                int i = s.indexOf(">");
                if (i > 0) {
                    status.commiterAndMail = s.substring(0, i + 1);
                    status.commiterTime = s.substring(i + 1).trim();
                }
                continue;
            }
            if (line.startsWith("commit")) {
                String revCode = line.substring(6).trim();
                int i = revCode.indexOf('(');
                if (i > 0) {
                    revCode = revCode.substring(0, i-1).trim();
                }
                if (status.revisionCode != null) {
                    status.message = buf.toString();
                    buf.setLength(0);
                    statuses.put(status.revisionCode, status);
                    if (statuses.containsKey(revCode)) {
                        status = statuses.get(revCode);
                    } else {
                        status = new GitRevisionInfo.GitRevCommit();
                    }
                }
                status.revisionCode = revCode;
                continue;
            }
            if (line.startsWith("tree")) {
                status.treeCode = line.substring(4).trim();
                continue;
            }
            if (line.startsWith("parent")) {
                status.parents.add(line.substring(6).trim());
                continue;
            }
            if (line.startsWith("author")) {
                String s = line.substring(6).trim();
                int i = s.indexOf(">");
                if (i > 0) {
                    status.autorAndMail = s.substring(0, i + 1);
                    status.autorTime = s.substring(i + 1).trim();
                }
                continue;
            }
            if (line.startsWith(" ")) {
                //if (buf.length() > 0) {
                //    buf.append('\n');
                //}
                buf.append(line.trim());
                buf.append('\n');
                continue;
            }
            if (line.startsWith(":")) {
                String[] s = line.split("\\s");
                if (s.length > 2) {
                    String file = s[s.length - 1];
                    String st = s[s.length - 2];
                    GitRevisionInfo.GitFileInfo.Status gitSt = GitRevisionInfo.GitFileInfo.Status.UNKNOWN;
                    if ("A".equals(st)) {
                        gitSt = GitRevisionInfo.GitFileInfo.Status.ADDED;
                    } else if ("M".equals(st)) {
                        gitSt = GitRevisionInfo.GitFileInfo.Status.MODIFIED;
                    } else if ("R".equals(st)) {
                        gitSt = GitRevisionInfo.GitFileInfo.Status.RENAMED;
                    } else if ("C".equals(st)) {
                        gitSt = GitRevisionInfo.GitFileInfo.Status.COPIED;
                    } else if ("D".equals(st)) {
                        gitSt = GitRevisionInfo.GitFileInfo.Status.REMOVED;
                    }
                    status.commitedFiles.put(file, gitSt);
                }
                continue;
            }
        }
        if (status.revisionCode != null) {
            status.message = buf.toString();
            statuses.put(status.revisionCode, status);
        }
    }
                

    private abstract class Parser {
        public abstract void outputParser(String output, LinkedHashMap<String, GitRevisionInfo.GitRevCommit> statuses) throws IOException;
        public void errorParser(String error) throws GitException.MissingObjectException {
        }
    }
}
