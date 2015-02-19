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

import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.GitRevertResult;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class RevertCommand extends GitCommand {

    private final ProgressMonitor monitor;
    private final String revisionStr;
    private GitRevertResult result;
    private final String message;
    private final boolean commit;

    public RevertCommand (JGitRepository repository, GitClassFactory gitFactory, String revision, String message, boolean commit, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.revisionStr = revision;
        this.message = message;
        this.commit = commit;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "revert"); //NOI18N
        if (!commit) {
            addArgument(0, "-n"); //NOI18N
        }
        addArgument(0, revisionStr);
    }

    @Override
    protected void run() throws GitException {
//        Repository repository = getRepository().getRepository();
//        RevCommit revertedCommit = Utils.findCommit(repository, revisionStr);
//        RevWalk revWalk = new RevWalk(repository);
//        DirCache dc = null;
//        GitRevertResult NO_CHANGE_INSTANCE = getClassFactory().createRevertResult(GitRevertResult.Status.NO_CHANGE, null, null, null);
//        try {
//            Ref headRef = repository.getRef(Constants.HEAD);
//            if (headRef == null) {
//                throw new GitException.MissingObjectException(Constants.HEAD, GitObjectType.COMMIT);
//            }
//            RevCommit headCommit = revWalk.parseCommit(headRef.getObjectId());
//            if (revertedCommit.getParentCount() != 1) {
//                throw new GitException("Cannot revert a merge commit");
//            }
//            RevCommit srcParent = revertedCommit.getParent(0);
//            revWalk.parseHeaders(srcParent);
//
//            ResolveMerger merger = (ResolveMerger) MergeStrategy.RECURSIVE.newMerger(repository);
//            merger.setWorkingTreeIterator(new FileTreeIterator(repository));
//            merger.setBase(revertedCommit.getTree());
//            String commitMessage = message == null || message.isEmpty() 
//                    ? "Revert \"" + revertedCommit.getShortMessage() + "\"" + "\n\n" + "This reverts commit " + revertedCommit.getId().getName() + "." //NOI18N
//                    : message;
//            if (merger.merge(headCommit, srcParent)) {
//                if (AnyObjectId.equals(headCommit.getTree().getId(), merger.getResultTreeId())) {
//                    result = NO_CHANGE_INSTANCE;
//                } else {
//                    DirCacheCheckout dco = new DirCacheCheckout(repository, headCommit.getTree(), dc = repository.lockDirCache(), merger.getResultTreeId());
//                    dco.setFailOnConflict(true);
//                    dco.checkout();
//                    if (commit) {
//                        RevCommit newHead = new Git(getRepository().getRepository()).commit().setMessage(commitMessage).call();
//                        result = getClassFactory().createRevertResult(GitRevertResult.Status.REVERTED, getClassFactory().createRevisionInfo(newHead, getRepository()), null, null);
//                    } else {
//                        result = getClassFactory().createRevertResult(GitRevertResult.Status.REVERTED_IN_INDEX, null, null, null);
//                    }
//                }
//            } else {
//                if (merger.getFailingPaths() != null) {
//                    result = getClassFactory().createRevertResult(GitRevertResult.Status.FAILED, null,
//                            merger.getMergeResults() == null ? null : getFiles(getRepository().getLocation(), merger.getMergeResults().keySet()),
//                            getFiles(getRepository().getLocation(), merger.getFailingPaths().keySet()));
//                } else {
//                    String mergeMessageWithConflicts = new MergeMessageFormatter().formatWithConflicts(commitMessage, merger.getUnmergedPaths());
//                    repository.writeMergeCommitMsg(mergeMessageWithConflicts);
//                    result = getClassFactory().createRevertResult(GitRevertResult.Status.CONFLICTING, null, 
//                            merger.getMergeResults() == null ? null : getFiles(getRepository().getLocation(), merger.getMergeResults().keySet()),
//                            null);
//                }
//            }
//        } catch (JGitInternalException ex) {
//            if (ex.getCause() instanceof CheckoutConflictException) {
//                String[] lines = ex.getCause().getMessage().split("\n"); //NOI18N
//                if (lines.length > 1) {
//                    throw new GitException.CheckoutConflictException(Arrays.copyOfRange(lines, 1, lines.length), ex.getCause());
//                }
//            }
//            throw new GitException(ex);
//        } catch (IOException ex) {
//            throw new GitException(ex);
//        } catch (GitAPIException ex) {
//            throw new GitException(ex);
//        } finally {
//            if (dc != null) {
//                dc.unlock();
//            }
//            revWalk.release();
//        }
    }

    public GitRevertResult getResult () {
        return result;
    }
}
