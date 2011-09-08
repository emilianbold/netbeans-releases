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

package org.netbeans.libs.git.jgit;

import org.netbeans.libs.git.GitBlameResult;
import org.netbeans.libs.git.GitClientCallback;
import org.netbeans.libs.git.GitException.MissingObjectException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitRepositoryState;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.jgit.commands.InitRepositoryCommand;
import org.netbeans.libs.git.jgit.commands.ListBranchCommand;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.jgit.commands.CatCommand;
import java.io.OutputStream;
import org.netbeans.libs.git.jgit.commands.ResetCommand;
import org.netbeans.libs.git.jgit.commands.RenameCommand;
import org.netbeans.libs.git.jgit.commands.CopyCommand;
import org.netbeans.libs.git.jgit.commands.StatusCommand;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitPullResult;
import org.netbeans.libs.git.GitPushResult;
import org.netbeans.libs.git.GitRevertResult;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.GitUser;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.jgit.commands.AddCommand;
import org.netbeans.libs.git.jgit.commands.BlameCommand;
import org.netbeans.libs.git.jgit.commands.CheckoutIndexCommand;
import org.netbeans.libs.git.jgit.commands.CheckoutRevisionCommand;
import org.netbeans.libs.git.jgit.commands.CleanCommand;
import org.netbeans.libs.git.jgit.commands.CommitCommand;
import org.netbeans.libs.git.jgit.commands.GetCommonAncestorCommand;
import org.netbeans.libs.git.jgit.commands.ConflictCommand;
import org.netbeans.libs.git.jgit.commands.CreateBranchCommand;
import org.netbeans.libs.git.jgit.commands.CreateTagCommand;
import org.netbeans.libs.git.jgit.commands.DeleteBranchCommand;
import org.netbeans.libs.git.jgit.commands.DeleteTagCommand;
import org.netbeans.libs.git.jgit.commands.ExportCommitCommand;
import org.netbeans.libs.git.jgit.commands.ExportDiffCommand;
import org.netbeans.libs.git.jgit.commands.FetchCommand;
import org.netbeans.libs.git.jgit.commands.GetPreviousCommitCommand;
import org.netbeans.libs.git.jgit.commands.GetRemotesCommand;
import org.netbeans.libs.git.jgit.commands.IgnoreCommand;
import org.netbeans.libs.git.jgit.commands.ListModifiedIndexEntriesCommand;
import org.netbeans.libs.git.jgit.commands.ListRemoteBranchesCommand;
import org.netbeans.libs.git.jgit.commands.ListRemoteTagsCommand;
import org.netbeans.libs.git.jgit.commands.ListTagCommand;
import org.netbeans.libs.git.jgit.commands.LogCommand;
import org.netbeans.libs.git.jgit.commands.MergeCommand;
import org.netbeans.libs.git.jgit.commands.PullCommand;
import org.netbeans.libs.git.jgit.commands.PushCommand;
import org.netbeans.libs.git.jgit.commands.RemoveCommand;
import org.netbeans.libs.git.jgit.commands.RemoveRemoteCommand;
import org.netbeans.libs.git.jgit.commands.RevertCommand;
import org.netbeans.libs.git.jgit.commands.SetRemoteCommand;
import org.netbeans.libs.git.jgit.commands.UnignoreCommand;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.NotificationListener;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.progress.RevisionInfoListener;
import org.netbeans.libs.git.progress.StatusListener;

/**
 *
 * @author ondra
 * @author Tomas Stupka
 */
public class JGitClient implements GitClient, StatusListener, FileListener, RevisionInfoListener {
    private final JGitRepository gitRepository;
    private final Set<NotificationListener> listeners;
    private JGitCredentialsProvider credentialsProvider;

    public JGitClient (JGitRepository gitRepository) {
        this.gitRepository = gitRepository;
        this.listeners = new HashSet<NotificationListener>();
    }

    /**
     * Adds all files under the given roots to the index
     * @param roots
     * @param monitor
     * @throws GitException an error occurs
     */
    @Override
    public void add (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        AddCommand cmd = new AddCommand(repository, roots, monitor, this);
        cmd.execute();
    }

    @Override
    public void addNotificationListener (NotificationListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public GitBlameResult blame (File file, String revision, ProgressMonitor monitor) throws MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        BlameCommand cmd = new BlameCommand(repository, file, revision, monitor);
        cmd.execute();
        return cmd.getResult();
    }

    @Override
    public boolean catFile (File file, String revision, OutputStream out, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        CatCommand cmd = new CatCommand(repository, file, revision, out, monitor);
        cmd.execute();
        return cmd.foundInRevision();
    }

    @Override
    public boolean catIndexEntry (File file, int stage, OutputStream out, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CatCommand cmd = new CatCommand(repository, file, stage, out, monitor);
        cmd.execute();
        return cmd.foundInRevision();
    }

    @Override
    public void checkout (File[] roots, String revision, boolean recursively, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        if (revision != null) {
            ResetCommand cmd = new ResetCommand(repository, revision, roots, recursively, monitor, this);
            cmd.execute();
        }
        if (!monitor.isCanceled()) {
            CheckoutIndexCommand cmd = new CheckoutIndexCommand(repository, roots, recursively, monitor, this);
            cmd.execute();
        }
    }

    @Override
    public void checkoutRevision (String revision, boolean failOnConflict, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        if (!failOnConflict) {
            throw new IllegalArgumentException("Currently unsupported. failOnConflict must be set to true. JGit lib is buggy."); //NOI18N
        }
        Repository repository = gitRepository.getRepository();
        CheckoutRevisionCommand cmd = new CheckoutRevisionCommand(repository, revision, failOnConflict, monitor, this);
        cmd.execute();
    }
   
    @Override
    public void clean(File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CleanCommand cmd = new CleanCommand(repository, roots, monitor, this);
        cmd.execute();        
    }

    /**
     * Commits all changes made in the index to all files under the given roots
     * @param roots
     * @param commitMessage 
     * @param monitor
     * @throws GitException an error occurs
     */
    @Override
    public GitRevisionInfo commit (File[] roots, String commitMessage, GitUser author, GitUser commiter, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CommitCommand cmd = new CommitCommand(repository, roots, commitMessage, author, commiter, monitor);
        cmd.execute();
        return cmd.revision;
    }

    /**
     * Modifies the index. The entries representing files under the source are copied and the newly created entries represent the corresponding files under the target.
     * @param source
     * @param target
     * @param monitor
     * @throws GitException
     */
    @Override
    public void copyAfter (File source, File target, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CopyCommand cmd = new CopyCommand(repository, source, target, monitor, this);
        cmd.execute();
    }

    @Override
    public GitBranch createBranch (String branchName, String revision, ProgressMonitor monitor) throws GitException {
        CreateBranchCommand cmd = new CreateBranchCommand(gitRepository.getRepository(), branchName, revision, monitor);
        cmd.execute();
        return cmd.getBranch();
    }

    @Override
    public GitTag createTag (String tagName, String taggedObjectId, String message, boolean signed, boolean forceUpdate, ProgressMonitor monitor) throws GitException.RefUpdateException, GitException {
        CreateTagCommand cmd = new CreateTagCommand(gitRepository.getRepository(), tagName, taggedObjectId, message, signed, forceUpdate, monitor);
        cmd.execute();
        return cmd.getTag();
    }

    @Override
    public void deleteBranch (String branchName, boolean forceDeleteUnmerged, ProgressMonitor monitor) throws GitException.NotMergedException, GitException {
        DeleteBranchCommand cmd = new DeleteBranchCommand(gitRepository.getRepository(), branchName, forceDeleteUnmerged, monitor);
        cmd.execute();
    }

    @Override
    public void deleteTag (String tagName, ProgressMonitor monitor) throws GitException.RefUpdateException, GitException {
        DeleteTagCommand cmd = new DeleteTagCommand(gitRepository.getRepository(), tagName, monitor);
        cmd.execute();
    }

    @Override
    public void exportCommit (String commit, OutputStream out, ProgressMonitor monitor) throws GitException {
        ExportCommitCommand cmd = new ExportCommitCommand(gitRepository.getRepository(), commit, out, monitor, this);
        cmd.execute();
    }

    @Override
    public void exportDiff (File[] roots, DiffMode mode, OutputStream out, ProgressMonitor monitor) throws GitException {
        ExportDiffCommand cmd = new ExportDiffCommand(gitRepository.getRepository(), roots, mode, out, monitor, this);
        cmd.execute();
    }

    @Override
    public Map<String, GitTransportUpdate> fetch (String remote, ProgressMonitor monitor) throws GitException {
        FetchCommand cmd = new FetchCommand(gitRepository.getRepository(), remote, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getUpdates();
    }

    @Override
    public Map<String, GitTransportUpdate> fetch (String remote, List<String> fetchRefSpecifications, ProgressMonitor monitor) throws GitException {
        FetchCommand cmd = new FetchCommand(gitRepository.getRepository(), remote, fetchRefSpecifications, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getUpdates();
    }
    
    @Override
    public Map<String, GitBranch> getBranches (boolean all, ProgressMonitor monitor) throws GitException {
        ListBranchCommand cmd = new ListBranchCommand(gitRepository.getRepository(), all, monitor);
        cmd.execute();
        return cmd.getBranches();
    }

    @Override
    public Map<String, GitTag> getTags (ProgressMonitor monitor, boolean all) throws GitException {
        ListTagCommand cmd = new ListTagCommand(gitRepository.getRepository(), all, monitor);
        cmd.execute();
        return cmd.getTags();
    }

    @Override
    public GitRevisionInfo getCommonAncestor (String[] revisions, ProgressMonitor monitor) throws GitException {
        GetCommonAncestorCommand cmd = new GetCommonAncestorCommand(gitRepository.getRepository(), revisions, monitor);
        cmd.execute();
        return cmd.getRevision();
    }

    @Override
    public GitRevisionInfo getPreviousRevision (File file, String revision, ProgressMonitor monitor) throws GitException {
        GetPreviousCommitCommand cmd = new GetPreviousCommitCommand(gitRepository.getRepository(), file, revision, monitor);
        cmd.execute();
        return cmd.getRevision();
    }

    /**
     * Much faster then an equivalent call to {@link #getStatus(java.io.File[], org.netbeans.libs.git.progress.ProgressMonitor) }
     */
    @Override
    public Map<File, GitStatus> getConflicts (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        ConflictCommand cmd = new ConflictCommand(repository, roots, monitor, this);
        cmd.execute();
        return cmd.getStatuses();
    }

    @Override
    /**
     * Returns an array of statuses for files under given roots
     * @param roots root folders or files
     * @return status array
     * @throws GitException when an error occurs
     */
    public Map<File, GitStatus> getStatus (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        StatusCommand cmd = new StatusCommand(repository, roots, monitor, this);
        cmd.execute();
        return cmd.getStatuses();
    }

    @Override
    public GitRemoteConfig getRemote (String remoteName, ProgressMonitor monitor) throws GitException {
        return getRemotes(monitor).get(remoteName);
    }

    @Override
    public Map<String, GitRemoteConfig> getRemotes (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        GetRemotesCommand cmd = new GetRemotesCommand(repository, monitor);
        cmd.execute();
        return cmd.getRemotes();
    }
    
    @Override
    public GitRepositoryState getRepositoryState (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RepositoryState state = repository.getRepositoryState();
        switch (state) {
            case APPLY:
                return GitRepositoryState.APPLY;
            case BARE:
                return GitRepositoryState.BARE;
            case BISECTING:
                return GitRepositoryState.BISECTING;
            case MERGING:
                return GitRepositoryState.MERGING;
            case MERGING_RESOLVED:
                return GitRepositoryState.MERGING_RESOLVED;
            case REBASING:
            case REBASING_INTERACTIVE:
            case REBASING_MERGE:
            case REBASING_REBASING:
                return GitRepositoryState.REBASING;
            case SAFE:
                return GitRepositoryState.SAFE;
            default:
                throw new IllegalStateException(state.getDescription());
        }
    }

    @Override
    public File[] ignore (File[] files, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        IgnoreCommand cmd = new IgnoreCommand(repository, files, monitor, this);
        cmd.execute();
        return cmd.getModifiedIgnoreFiles();
    }
    
    @Override
    /**
     * Initializes an empty git repository
     * @throws GitException if the repository could not be created either because it already exists inside <code>workDir</code> or cannot be created for other reasons.
     */
    public void init (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        InitRepositoryCommand cmd = new InitRepositoryCommand(repository, monitor);
        cmd.execute();
    }

    @Override
    public File[] listModifiedIndexEntries (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        ListModifiedIndexEntriesCommand cmd = new ListModifiedIndexEntriesCommand(repository, roots, monitor, this);
        cmd.execute();
        return cmd.getFiles();
    }

    @Override
    public Map<String, GitBranch> listRemoteBranches (String remoteRepositoryUrl, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        ListRemoteBranchesCommand cmd = new ListRemoteBranchesCommand(repository, remoteRepositoryUrl, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getBranches();
    }

    @Override
    public Map<String, String> listRemoteTags (String remoteRepositoryUrl, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        ListRemoteTagsCommand cmd = new ListRemoteTagsCommand(repository, remoteRepositoryUrl, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getTags();
    }

    @Override
    public GitRevisionInfo log (String revision, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        LogCommand cmd = new LogCommand(repository, revision, monitor, this);
        cmd.execute();
        GitRevisionInfo[] revisions = cmd.getRevisions();
        return revisions.length == 0 ? null : revisions[0];
    }

    @Override
    public GitRevisionInfo[] log (SearchCriteria searchCriteria, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        LogCommand cmd = new LogCommand(repository, searchCriteria, monitor, this);
        cmd.execute();
        return cmd.getRevisions();
    }

    @Override
    public GitMergeResult merge (String revision, ProgressMonitor monitor) throws GitException.CheckoutConflictException, GitException {
        Repository repository = gitRepository.getRepository();
        MergeCommand cmd = new MergeCommand(repository, revision, monitor);
        cmd.execute();
        return cmd.getResult();
    }

    @Override
    public GitPullResult pull (String remote, List<String> fetchRefSpecifications, String branchToMerge, ProgressMonitor monitor) throws GitException {
        PullCommand cmd = new PullCommand(gitRepository.getRepository(), remote, fetchRefSpecifications, branchToMerge, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getResult();
    }

    @Override
    public GitPushResult push (String remote, List<String> pushRefSpecifications, List<String> fetchRefSpecifications, ProgressMonitor monitor) throws GitException {
        PushCommand cmd = new PushCommand(gitRepository.getRepository(), remote, pushRefSpecifications, fetchRefSpecifications, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getResult();
    }

    /**
     * Removes given files/folders from the index and/or from the working tree
     * @param roots files/folders to remove
     * @param cached if <code>true</code> the working tree will not be affected
     * @param monitor
     */
    @Override
    public void remove(File[] roots, boolean cached, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RemoveCommand cmd = new RemoveCommand(repository, roots, cached, monitor, this);
        cmd.execute();
    }

    @Override
    public void removeNotificationListener(NotificationListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public void removeRemote (String remote, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RemoveRemoteCommand cmd = new RemoveRemoteCommand(repository, remote, monitor);
        cmd.execute();
    }

    /**
     * Renames source file or folder to target
     * @param source file or folder to be renamed
     * @param target target file or folder. Must not yet exist.
     * @param after set to true if you don't only want to correct the index
     * @throws GitException
     */
    @Override
    public void rename (File source, File target, boolean after, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RenameCommand cmd = new RenameCommand(repository, source, target, after, monitor, this);
        cmd.execute();
    }

    @Override
    public void reset (File[] roots, String revision, boolean recursively, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        ResetCommand cmd = new ResetCommand(repository, revision, roots, recursively, monitor, this);
        cmd.execute();
    }

    @Override
    public void reset (String revision, ResetType resetType, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        ResetCommand cmd = new ResetCommand(repository, revision, resetType, monitor, this);
        cmd.execute();
    }

    @Override
    public GitRevertResult revert (String revision, String message, boolean commit, ProgressMonitor monitor) throws MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        RevertCommand cmd = new RevertCommand(repository, revision, message, commit, monitor);
        cmd.execute();
        return cmd.getResult();
    }

    @Override
    public void setCallback (GitClientCallback callback) {
        this.credentialsProvider = callback == null ? null : new JGitCredentialsProvider(callback);
    }

    @Override
    public void setRemote (GitRemoteConfig remoteConfig, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        SetRemoteCommand cmd = new SetRemoteCommand(repository, remoteConfig, monitor, this);
        cmd.execute();
    }

    @Override
    public GitUser getUser() throws GitException {        
        return new JGitUserInfo(new PersonIdent(gitRepository.getRepository()));
    }
    
    @Override
    public File[] unignore (File[] files, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        UnignoreCommand cmd = new UnignoreCommand(repository, files, monitor, this);
        cmd.execute();
        return cmd.getModifiedIgnoreFiles();
    }

    // <editor-fold defaultstate="collapsed" desc="listener methods">
    @Override
    public void notifyFile (File file, String relativePathToRoot) {
        List<NotificationListener> lists;
        synchronized (listeners) {
            lists = new LinkedList<NotificationListener>(listeners);
        }
        for (NotificationListener list : lists) {
            if (list instanceof FileListener) {
                ((FileListener) list).notifyFile(file, relativePathToRoot);
            }
        }
    }

    @Override
    public void notifyStatus(GitStatus status) {
        List<NotificationListener> lists;
        synchronized (listeners) {
            lists = new LinkedList<NotificationListener>(listeners);
        }
        for (NotificationListener list : lists) {
            if (list instanceof StatusListener) {
                ((StatusListener) list).notifyStatus(status);
            }
        }
    }

    @Override
    public void notifyRevisionInfo (GitRevisionInfo info) {
        List<NotificationListener> lists;
        synchronized (listeners) {
            lists = new LinkedList<NotificationListener>(listeners);
        }
        for (NotificationListener list : lists) {
            if (list instanceof RevisionInfoListener) {
                ((RevisionInfoListener) list).notifyRevisionInfo(info);
            }
        }
    }// </editor-fold>
}
