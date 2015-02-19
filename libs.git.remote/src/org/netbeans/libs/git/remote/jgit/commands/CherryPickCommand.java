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

import org.netbeans.libs.git.remote.GitCherryPickResult;
import org.netbeans.libs.git.remote.GitClient;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.progress.FileListener;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CherryPickCommand extends GitCommand {

    private final String[] revisions;
    private GitCherryPickResult result;
    private final ProgressMonitor monitor;
    private final GitClient.CherryPickOperation operation;
    private final FileListener listener;
    private static final String SEQUENCER = "sequencer";
    private static final String SEQUENCER_HEAD = "head";
    private static final String SEQUENCER_TODO = "todo";

    public CherryPickCommand (JGitRepository repository, GitClassFactory gitFactory, String[] revisions,
            GitClient.CherryPickOperation operation, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.revisions = revisions;
        this.operation = operation;
        this.monitor = monitor;
        this.listener = listener;
    }

    @Override
    protected void run () throws GitException {
//        Repository repository = getRepository().getRepository();
//        ObjectId originalCommit = getOriginalCommit();
//        ObjectId head = getHead();
//        List<RebaseTodoLine> steps;
//        try {
//            switch (operation) {
//                case BEGIN:
//                    // initialize sequencer and cherry-pick steps if there are
//                    // more commits to cherry-pick
//                    steps = prepareCommand(head);
//                    // apply the selected steps
//                    applySteps(steps, false);
//                    break;
//                case ABORT:
//                    // delete the sequencer and reset to the original head
//                    if (repository.getRepositoryState() == RepositoryState.CHERRY_PICKING
//                            || repository.getRepositoryState() == RepositoryState.CHERRY_PICKING_RESOLVED) {
//                        if (originalCommit == null) {
//                            // maybe the sequencer is not created in that case simply reset to HEAD
//                            originalCommit = head;
//                        }
//                    }
//                    Utils.deleteRecursively(getSequencerFolder());
//                    if (originalCommit != null) {
//                        ResetCommand reset = new ResetCommand(getRepository(), getClassFactory(),
//                                originalCommit.name(), GitClient.ResetType.HARD, new DelegatingGitProgressMonitor(monitor), listener);
//                        reset.execute();
//                    }
//                    result = createCustomResult(GitCherryPickResult.CherryPickStatus.ABORTED);
//                    break;
//                case QUIT:
//                    // used to reset the sequencer only
//                    Utils.deleteRecursively(getSequencerFolder());
//                    switch (repository.getRepositoryState()) {
//                        case CHERRY_PICKING:
//                            // unresolved conflicts
//                            result = createResult(CherryPickResult.CONFLICT);
//                            break;
//                        case CHERRY_PICKING_RESOLVED:
//                            result = createCustomResult(GitCherryPickResult.CherryPickStatus.UNCOMMITTED);
//                            break;
//                        default:
//                            result = createCustomResult(GitCherryPickResult.CherryPickStatus.OK);
//                            break;
//                    }
//                    break;
//                case CONTINUE:
//                    switch (repository.getRepositoryState()) {
//                        case CHERRY_PICKING:
//                            // unresolved conflicts, cannot continue
//                            result = createResult(CherryPickResult.CONFLICT);
//                            break;
//                        case CHERRY_PICKING_RESOLVED:
//                            // cannot continue without manual commit
//                            result = createCustomResult(GitCherryPickResult.CherryPickStatus.UNCOMMITTED);
//                            break;
//                        default:
//                            // read steps from sequencer and apply them
//                            // if sequencer is empty this will be a noop
//                            steps = readTodoFile(getRepository());
//                            applySteps(steps, true);
//                            break;
//                    }
//                    break;
//                default:
//                    throw new IllegalStateException("Unexpected operation " + operation.name());
//            }
//        } catch (GitAPIException | IOException ex) {
//            throw new GitException(ex);
//        }
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "cherry-pick"); //NOI18N
        if (operation == GitClient.CherryPickOperation.BEGIN) {
            for (String rev : revisions) {
                addArgument(0, rev);
            }
        } else {
            addArgument(0, operation.toString());
        }
    }

    public GitCherryPickResult getResult () {
        return result;
    }

}
