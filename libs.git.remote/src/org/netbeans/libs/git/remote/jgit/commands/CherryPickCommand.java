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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseCommand.Operation;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.RebaseTodoFile;
import org.eclipse.jgit.lib.RebaseTodoLine;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.SafeBufferedOutputStream;
import org.netbeans.libs.git.remote.GitCherryPickResult;
import org.netbeans.libs.git.remote.GitClient;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.GitRevisionInfo;
import org.netbeans.libs.git.remote.GitStatus;
import org.netbeans.libs.git.remote.jgit.DelegatingGitProgressMonitor;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.jgit.Utils;
import org.netbeans.libs.git.remote.progress.FileListener;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.libs.git.remote.progress.StatusListener;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

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
        Repository repository = getRepository().getRepository();
        ObjectId originalCommit = getOriginalCommit();
        ObjectId head = getHead();
        List<RebaseTodoLine> steps;
        try {
            switch (operation) {
                case BEGIN:
                    // initialize sequencer and cherry-pick steps if there are
                    // more commits to cherry-pick
                    steps = prepareCommand(head);
                    // apply the selected steps
                    applySteps(steps, false);
                    break;
                case ABORT:
                    // delete the sequencer and reset to the original head
                    if (repository.getRepositoryState() == RepositoryState.CHERRY_PICKING
                            || repository.getRepositoryState() == RepositoryState.CHERRY_PICKING_RESOLVED) {
                        if (originalCommit == null) {
                            // maybe the sequencer is not created in that case simply reset to HEAD
                            originalCommit = head;
                        }
                    }
                    Utils.deleteRecursively(getSequencerFolder());
                    if (originalCommit != null) {
                        ResetCommand reset = new ResetCommand(getRepository(), getClassFactory(),
                                originalCommit.name(), GitClient.ResetType.HARD, new DelegatingGitProgressMonitor(monitor), listener);
                        reset.execute();
                    }
                    result = createCustomResult(GitCherryPickResult.CherryPickStatus.ABORTED);
                    break;
                case QUIT:
                    // used to reset the sequencer only
                    Utils.deleteRecursively(getSequencerFolder());
                    switch (repository.getRepositoryState()) {
                        case CHERRY_PICKING:
                            // unresolved conflicts
                            result = createResult(CherryPickResult.CONFLICT);
                            break;
                        case CHERRY_PICKING_RESOLVED:
                            result = createCustomResult(GitCherryPickResult.CherryPickStatus.UNCOMMITTED);
                            break;
                        default:
                            result = createCustomResult(GitCherryPickResult.CherryPickStatus.OK);
                            break;
                    }
                    break;
                case CONTINUE:
                    switch (repository.getRepositoryState()) {
                        case CHERRY_PICKING:
                            // unresolved conflicts, cannot continue
                            result = createResult(CherryPickResult.CONFLICT);
                            break;
                        case CHERRY_PICKING_RESOLVED:
                            // cannot continue without manual commit
                            result = createCustomResult(GitCherryPickResult.CherryPickStatus.UNCOMMITTED);
                            break;
                        default:
                            // read steps from sequencer and apply them
                            // if sequencer is empty this will be a noop
                            steps = readTodoFile(getRepository());
                            applySteps(steps, true);
                            break;
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected operation " + operation.name());
            }
        } catch (GitAPIException | IOException ex) {
            throw new GitException(ex);
        }
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument("cherry-pick"); //NOI18N
        if (operation == GitClient.CherryPickOperation.BEGIN) {
            for (String rev : revisions) {
                addArgument(rev);
            }
        } else {
            addArgument(operation.toString());
        }
    }

    public GitCherryPickResult getResult () {
        return result;
    }

    static Operation getOperation (GitClient.RebaseOperationType operation) {
        return Operation.valueOf(operation.name());
    }

    private void applySteps (List<RebaseTodoLine> steps, boolean skipFirstStep) throws GitAPIException, IOException {
        Repository repository = getRepository().getRepository();
        ObjectReader or = repository.newObjectReader();
        CherryPickResult res = null;
        boolean skipped = false;
        List<Ref> cherryPickedRefs = new ArrayList<>();
        for (Iterator<RebaseTodoLine> it = steps.iterator(); it.hasNext(); ) {
            RebaseTodoLine step = it.next();
            if (step.getAction() == RebaseTodoLine.Action.PICK) {
                if (skipFirstStep && !skipped) {
                    it.remove();
                    writeTodoFile(getRepository(), steps);
                    skipped = true;
                    continue;
                }
                Collection<ObjectId> ids = or.resolve(step.getCommit());
                if (ids.size() != 1) {
                    throw new JGitInternalException("Could not resolve uniquely the abbreviated object ID");
                }
                org.eclipse.jgit.api.CherryPickCommand command = new Git(repository).cherryPick();
                command.include(ids.iterator().next());
                res = command.call();
                if (res.getStatus() == CherryPickResult.CherryPickStatus.OK) {
                    it.remove();
                    writeTodoFile(getRepository(), steps);
                    cherryPickedRefs.addAll(res.getCherryPickedRefs());
                } else {
                    break;
                }
            } else {
                it.remove();
            }
        }
        if (res == null) {
            result = createCustomResult(GitCherryPickResult.CherryPickStatus.OK, cherryPickedRefs);
        } else {
            result = createResult(res, cherryPickedRefs);
        }
        if (steps.isEmpty()) {
            // sequencer no longer needed
            Utils.deleteRecursively(getSequencerFolder());
        }
    }

    private GitCherryPickResult createResult (CherryPickResult res) {
        return createResult(res, Collections.<Ref>emptyList());
    }
    
    private GitCherryPickResult createResult (CherryPickResult res, List<Ref> cherryPickedRefs) {
        GitRevisionInfo currHead = getCurrentHead();
        
        GitCherryPickResult.CherryPickStatus status = GitCherryPickResult.CherryPickStatus.valueOf(res.getStatus().name());
        List<VCSFileProxy> conflicts;
        if (res.getStatus() == CherryPickResult.CherryPickStatus.CONFLICTING) {
            conflicts = getConflicts(currHead);
        } else {
            conflicts = Collections.<VCSFileProxy>emptyList();
        }
        List<GitRevisionInfo> commits = toCommits(cherryPickedRefs);
        return getClassFactory().createCherryPickResult(status, conflicts, getFailures(res), currHead, commits);
    }

    private List<GitRevisionInfo> toCommits (List<Ref> cherryPickedRefs) {
        List<GitRevisionInfo> commits = new ArrayList<>(cherryPickedRefs.size());
        Repository repository = getRepository().getRepository();
        RevWalk walk = new RevWalk(repository);
        for (Ref ref : cherryPickedRefs) {
            try {
                commits.add(getClassFactory().createRevisionInfo(Utils.findCommit(repository,
                        ref.getLeaf().getObjectId(), walk), getRepository()));
            } catch (GitException ex) {
                Logger.getLogger(CherryPickCommand.class.getName()).log(Level.INFO, null, ex);
            }
        }
        return commits;
    }

    private GitRevisionInfo getCurrentHead () {
        GitRevisionInfo currHead;
        Repository repository = getRepository().getRepository();
        try {
            currHead = getClassFactory().createRevisionInfo(Utils.findCommit(repository, Constants.HEAD), getRepository());
        } catch (GitException ex) {
            currHead = null;
        }
        return currHead;
    }

    private GitCherryPickResult createCustomResult (GitCherryPickResult.CherryPickStatus status) {
        return createCustomResult(status, Collections.<Ref>emptyList());
    }

    private GitCherryPickResult createCustomResult (GitCherryPickResult.CherryPickStatus status, List<Ref> cherryPickedRefs) {
        return getClassFactory().createCherryPickResult(status, Collections.<VCSFileProxy>emptyList(),
                Collections.<VCSFileProxy>emptyList(), getCurrentHead(), toCommits(cherryPickedRefs));
    }

    private List<VCSFileProxy> getConflicts (GitRevisionInfo info) {
        List<VCSFileProxy> conflicts;
        try {
            ConflictCommand cmd = new ConflictCommand(getRepository(), getClassFactory(), new VCSFileProxy[0],
                    new DelegatingGitProgressMonitor(monitor),
                    new StatusListener() {
                        @Override
                        public void notifyStatus (GitStatus status) { }
                    });
            cmd.execute();
            Map<VCSFileProxy, GitStatus> statuses = cmd.getStatuses();
            conflicts = new ArrayList<>(statuses.size());
            for (Map.Entry<VCSFileProxy, GitStatus> e : statuses.entrySet()) {
                if (e.getValue().isConflict()) {
                    conflicts.add(e.getKey());
                }
            }
        } catch (GitException ex) {
            Logger.getLogger(CherryPickCommand.class.getName()).log(Level.INFO, null, ex);
            conflicts = Collections.<VCSFileProxy>emptyList();
        }
        return conflicts;
    }

    private List<VCSFileProxy> getFailures (CherryPickResult result) {
        List<VCSFileProxy> files = new ArrayList<>();
        VCSFileProxy workDir = getRepository().getLocation();
        if (result.getStatus() == CherryPickResult.CherryPickStatus.FAILED) {
            Map<String, ResolveMerger.MergeFailureReason> obstructions = result.getFailingPaths();
            if (obstructions != null) {
                for (Map.Entry<String, ResolveMerger.MergeFailureReason> failure : obstructions.entrySet()) {
                    files.add(VCSFileProxy.createFileProxy(workDir, failure.getKey()));
                }
            }
        }
        return Collections.unmodifiableList(files);
    }

    private VCSFileProxy getSequencerFolder () {
        return VCSFileProxy.createFileProxy(getRepository().getLocation(), SEQUENCER);
    }

    private ObjectId getOriginalCommit () throws GitException {
        Repository repository = getRepository().getRepository();
        VCSFileProxy seqHead = VCSFileProxy.createFileProxy(getSequencerFolder(), SEQUENCER_HEAD);
        ObjectId originalCommitId = null;
        if (VCSFileProxySupport.canRead(seqHead)) {
            try {
                byte[] content = VCSFileProxySupport.readFully(seqHead, Integer.MAX_VALUE);
                if (content.length > 0) {
                    originalCommitId = ObjectId.fromString(content, 0);
                }
                if (originalCommitId != null) {
                    originalCommitId = repository.resolve(originalCommitId.getName() + "^{commit}");
                }
            } catch (IOException e) {
            }
        }
        return originalCommitId;
    }

	private ObjectId getHead () throws GitException {
		return Utils.findCommit(getRepository().getRepository(), Constants.HEAD);
	}

    private List<RebaseTodoLine> prepareCommand (ObjectId head) throws GitException, IOException {
        Repository repository = getRepository().getRepository();
        ObjectReader or = repository.newObjectReader();
        RevWalk walk = new RevWalk(or);
        List<RevCommit> commits = new ArrayList<>(revisions.length);
        for (String rev : revisions) {
            RevCommit commit = Utils.findCommit(repository, rev, walk);
            commits.add(commit);
        }
        List<RebaseTodoLine> steps = new ArrayList<>(commits.size());
        if (commits.size() == 1) {
            RevCommit commit = commits.get(0);
            steps.add(new RebaseTodoLine(RebaseTodoLine.Action.PICK,
                    or.abbreviate(commit), commit.getShortMessage()));
        } else if (!commits.isEmpty()) {
            VCSFileProxy sequencer = getSequencerFolder();
            VCSFileProxySupport.mkdirs(sequencer);
            try {
                for (RevCommit commit : commits) {
                    steps.add(new RebaseTodoLine(RebaseTodoLine.Action.PICK,
                            or.abbreviate(commit), commit.getShortMessage()));
                }
                writeTodoFile(getRepository(), steps);
                writeFile(VCSFileProxy.createFileProxy(sequencer, SEQUENCER_HEAD), head);
            } catch (IOException ex) {
                Utils.deleteRecursively(sequencer);
                throw new GitException(ex);
            }
        }
        return steps;
    }
    
    private void writeFile (VCSFileProxy file, ObjectId id) throws IOException {
        try (BufferedOutputStream bos = new SafeBufferedOutputStream(VCSFileProxySupport.getOutputStream(file))) {
            id.copyTo(bos);
            bos.write('\n');
        }
    }

    private void writeTodoFile (JGitRepository repository, List<RebaseTodoLine> steps) throws IOException {
        VCSFileProxy f = VCSFileProxy.createFileProxy(repository.getMetadataLocation(), SEQUENCER);
        if (f.canWrite()) {
            RebaseTodoFile todoFile = new RebaseTodoFile(repository.getRepository());
            todoFile.writeRebaseTodoFile(SEQUENCER + "/" + SEQUENCER_TODO, steps, false);
        }
    }

    private List<RebaseTodoLine> readTodoFile (JGitRepository repository) throws IOException {
        String path = SEQUENCER + "/" + SEQUENCER_TODO;
        VCSFileProxy f = VCSFileProxy.createFileProxy(repository.getMetadataLocation(), path);
        if (VCSFileProxySupport.canRead(f)) {
            RebaseTodoFile todoFile = new RebaseTodoFile(repository.getRepository());
            return todoFile.readRebaseTodo(SEQUENCER + "/" + SEQUENCER_TODO, true);
        }
        return Collections.<RebaseTodoLine>emptyList();
    }
}
