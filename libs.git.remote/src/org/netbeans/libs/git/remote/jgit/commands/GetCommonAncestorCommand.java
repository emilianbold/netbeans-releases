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

import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.GitRevisionInfo;
import org.netbeans.libs.git.remote.GitRevisionInfo.GitRevCommit;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VersioningSupport;

/**
 *
 * @author ondra
 */
public class GetCommonAncestorCommand extends GitCommand {
    public static final boolean KIT = false;
    private final String[] revisions;
    private GitRevisionInfo revision;
    private final Revision revisionPlaseHolder;
    private final ProgressMonitor monitor;

    public GetCommonAncestorCommand (JGitRepository repository, GitClassFactory gitFactory, String[] revisions, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.revisions = revisions;
        this.monitor = monitor;
        revisionPlaseHolder = new Revision();
    }
    
    @Override
    protected void run () throws GitException {
        if (KIT) {
            //runKit();
        } else {
            runCLI();
        }
    }

    protected void runKit () throws GitException {
//        Repository repository = getRepository().getRepository();
//        RevWalk walk = null;
//        try {
//            if (revisions.length == 0) {
//                revision = null;
//            } else {
//                walk = new RevWalk(repository);
//                List<RevCommit> commits = new ArrayList<>(revisions.length);
//                for (String rev : revisions) {
//                    commits.add(Utils.findCommit(repository, rev, walk));
//                }
//                revision = getSingleBaseCommit(walk, commits);
//            }
//        } catch (MissingObjectException ex) {
//            throw new GitException.MissingObjectException(ex.getObjectId().toString(), GitObjectType.COMMIT);
//        } catch (IOException ex) {
//            throw new GitException(ex);
//        } finally {
//            if (walk != null) {
//                walk.release();
//            }
//        }
    }
    
    public GitRevisionInfo getRevision () {
        return revision;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "merge-base"); //NOI18N
        for (String s : revisions) {
            addArgument(0, s);
        }
        addArgument(1, "log"); //NOI18N
        addArgument(1, "--raw"); //NOI18N
        addArgument(1, "--pretty=raw"); //NOI18N
        addArgument(1, "-1"); //NOI18N
        addArgument(1, revisionPlaseHolder); //NOI18N
    }
    
    private void runCLI() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        String cmd = getCommandLine();
        try {
            GitRevCommit status = new GitRevCommit();
            runner(canceled, 0, status, new Parser() {

                @Override
                public void outputParser(String output, GitRevCommit revision) {
                    parseCommit(output, revision);
                }
            });
            if (status.revisionCode != null) {
                revisionPlaseHolder.setContent(status.revisionCode);
                runner(canceled, 1, status, new Parser() {

                    @Override
                    public void outputParser(String output, GitRevCommit revision) {
                        CommitCommand.parseLog(output, revision);
                    }
                });
            }
            if (canceled.canceled()) {
                return;
            }
            revision = getClassFactory().createRevisionInfo(status, getRepository());
            
            //command.commandCompleted(exitStatus.exitCode);
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        } finally {
            //command.commandFinished();
        }
    }
    
    private void parseCommit(String output, GitRevCommit status) {
        for (String line : output.split("\n")) { //NOI18N
            line = line.trim();
            if (!line.isEmpty()) {
                status.revisionCode = line;
            }
        }
    }
    
    private void runner(ProcessUtils.Canceler canceled, int command, GitRevCommit list, Parser parser) throws GitException {
        if(canceled.canceled()) {
            return;
        }
        ProcessBuilder processBuilder = VersioningSupport.createProcessBuilder(getRepository().getLocation());
        String executable = getExecutable();
        String[] args = getCliArguments(command);
        ProcessUtils.ExitStatus exitStatus = ProcessUtils.executeInDir(getRepository().getLocation().getPath(), getEnvVar(), false, canceled, processBuilder, executable, args); //NOI18N
        if(canceled.canceled()) {
            return;
        }
        if (exitStatus.output!= null && exitStatus.isOK()) {
            parser.outputParser(exitStatus.output, list);
        }
        if (exitStatus.error != null && !exitStatus.isOK()) {
            parser.errorParser(exitStatus.error, list);
        }
    }
    

    private abstract class Parser {
        public abstract void outputParser(String output, GitRevCommit revision);
        public void errorParser(String error,GitRevCommit revision) throws GitException {
        }
    }

}
