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

package org.netbeans.libs.git;

import java.io.File;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.netbeans.libs.git.jgit.JGitCredentialsProvider;
import org.netbeans.libs.git.jgit.JGitRepository;
import org.netbeans.libs.git.jgit.JGitUserInfo;
import org.netbeans.libs.git.jgit.commands.AddCommand;
import org.netbeans.libs.git.jgit.commands.BlameCommand;
import org.netbeans.libs.git.jgit.commands.CatCommand;
import org.netbeans.libs.git.jgit.commands.CheckoutIndexCommand;
import org.netbeans.libs.git.jgit.commands.CheckoutRevisionCommand;
import org.netbeans.libs.git.jgit.commands.CleanCommand;
import org.netbeans.libs.git.jgit.commands.CommitCommand;
import org.netbeans.libs.git.jgit.commands.ConflictCommand;
import org.netbeans.libs.git.jgit.commands.CopyCommand;
import org.netbeans.libs.git.jgit.commands.CreateBranchCommand;
import org.netbeans.libs.git.jgit.commands.CreateTagCommand;
import org.netbeans.libs.git.jgit.commands.DeleteBranchCommand;
import org.netbeans.libs.git.jgit.commands.DeleteTagCommand;
import org.netbeans.libs.git.jgit.commands.ExportCommitCommand;
import org.netbeans.libs.git.jgit.commands.ExportDiffCommand;
import org.netbeans.libs.git.jgit.commands.FetchCommand;
import org.netbeans.libs.git.jgit.commands.GetCommonAncestorCommand;
import org.netbeans.libs.git.jgit.commands.GetPreviousCommitCommand;
import org.netbeans.libs.git.jgit.commands.GetRemotesCommand;
import org.netbeans.libs.git.jgit.commands.IgnoreCommand;
import org.netbeans.libs.git.jgit.commands.InitRepositoryCommand;
import org.netbeans.libs.git.jgit.commands.ListBranchCommand;
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
import org.netbeans.libs.git.jgit.commands.RenameCommand;
import org.netbeans.libs.git.jgit.commands.ResetCommand;
import org.netbeans.libs.git.jgit.commands.RevertCommand;
import org.netbeans.libs.git.jgit.commands.SetRemoteCommand;
import org.netbeans.libs.git.jgit.commands.StatusCommand;
import org.netbeans.libs.git.jgit.commands.UnignoreCommand;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.NotificationListener;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.progress.RevisionInfoListener;
import org.netbeans.libs.git.progress.StatusListener;

/**
 *
 * @author ondra
 */
public final class GitClient {
    private final DelegateListener delegateListener;

    public enum ResetType {
        SOFT {
            @Override
            public String toString() {
                return "--soft"; //NOI18N
            }
        }, MIXED {
            @Override
            public String toString() {
                return "--mixed"; //NOI18N
            }
        }, HARD {
            @Override
            public String toString() {
                return "--hard"; //NOI18N
            }
        }
    }

    public enum DiffMode {
        HEAD_VS_INDEX,
        HEAD_VS_WORKINGTREE,
        INDEX_VS_WORKINGTREE
    }
    
    private final JGitRepository gitRepository;
    private final Set<NotificationListener> listeners;
    private JGitCredentialsProvider credentialsProvider;

    GitClient (JGitRepository gitRepository) {
        this.gitRepository = gitRepository;
        listeners = new HashSet<NotificationListener>();
        delegateListener = new DelegateListener();
    }

    /**
     * Adds all files under the given roots to the index
     * @param roots
     * @param monitor
     * @throws GitException an error occurs
     */
    public void add (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        AddCommand cmd = new AddCommand(repository, roots, monitor, delegateListener);
        cmd.execute();
    }

    public void addNotificationListener (NotificationListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Annotates lines of a given file in a given revision
     * @param file
     * @param revision null for blaming a checked-out file against HEAD
     * @param monitor
     * @return
     * @throws org.netbeans.libs.git.GitException.MissingObjectException
     * @throws GitException 
     */
    public GitBlameResult blame (File file, String revision, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        BlameCommand cmd = new BlameCommand(repository, file, revision, monitor);
        cmd.execute();
        return cmd.getResult();
    }

    /**
     * Prints file's content in the given revision to output stream
     * @param file
     * @param revision git revision, never null
     * @param out output stream
     * @return true if the file was found in the specified revision and printed to out, otherwise false
     * @throws GitException
     * @throws GitException.MissingObjectException if the given revision does not exist
     */
    public boolean catFile (File file, String revision, java.io.OutputStream out, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        CatCommand cmd = new CatCommand(repository, file, revision, out, monitor);
        cmd.execute();
        return cmd.foundInRevision();
    }

    /**
     * Prints content of an index entry accordant with the given file to output stream
     * @param file
     * @param stage 
     * @param out output stream
     * @return true if the file was found in the index and printed to out, otherwise false
     * @throws GitException
     */
    public boolean catIndexEntry (File file, int stage, java.io.OutputStream out, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CatCommand cmd = new CatCommand(repository, file, stage, out, monitor);
        cmd.execute();
        return cmd.foundInRevision();
    }

    /**
     * Checks out the index into the working copy root. Does not move HEAD.
     * @param revision if not null, index is updated with the revision content before checking out to WC
     * @param roots files/folders to checkout
     * @param recursively if set to <code>true</code>, all files under given roots will be checked out, otherwise only roots and direct file children will be affected.
     * @throws GitException other error
     */
    public void checkout(File[] roots, String revision, boolean recursively, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        if (revision != null) {
            ResetCommand cmd = new ResetCommand(repository, revision, roots, recursively, monitor, delegateListener);
            cmd.execute();
        }
        if (!monitor.isCanceled()) {
            CheckoutIndexCommand cmd = new CheckoutIndexCommand(repository, roots, recursively, monitor, delegateListener);
            cmd.execute();
        }
    }

    /**
     * Checks out a given revision.
     * @param revision cannot be null. If the value equals to anything other than an existing branch name, the revision will be checked out
     * and the working tree will be in the detached HEAD state.
     * @param failOnConflict if set to false, the command tries to merge local changes into the new branch
     * @throws GitException other error
     */
    public void checkoutRevision (String revision, boolean failOnConflict, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        if (!failOnConflict) {
            throw new IllegalArgumentException("Currently unsupported. failOnConflict must be set to true. JGit lib is buggy."); //NOI18N
        }
        Repository repository = gitRepository.getRepository();
        CheckoutRevisionCommand cmd = new CheckoutRevisionCommand(repository, revision, failOnConflict, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Cleans the working tree by recursively removing files that are not under 
     * version control starting from the given roots.
     * @param roots
     * @param monitor
     * @throws GitException 
     */
    public void clean(File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CleanCommand cmd = new CleanCommand(repository, roots, monitor, delegateListener);
        cmd.execute();        
    }
    
    /**
     * Commits all changes made in the index to all files under the given roots
     * @param roots
     * @param commitMessage
     * @param author
     * @param commiter
     * @param monitor
     * @throws GitException an error occurs
     */
    public GitRevisionInfo commit(File[] roots, String commitMessage, GitUser author, GitUser commiter, ProgressMonitor monitor) throws GitException {
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
    public void copyAfter (File source, File target, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        CopyCommand cmd = new CopyCommand(repository, source, target, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Creates a new branch with a given name, starting at revision
     * @param branchName
     * @param revision
     * @param monitor
     * @return created branch
     * @throws GitException  an error occurs
     */
    public GitBranch createBranch (String branchName, String revision, ProgressMonitor monitor) throws GitException {
        CreateBranchCommand cmd = new CreateBranchCommand(gitRepository.getRepository(), branchName, revision, monitor);
        cmd.execute();
        return cmd.getBranch();
    }

    /**
     * Creates a tag for any object represented by a given taggedObjectId. 
     * If message is set to null or an empty value and signed set to false than this method creates a lightweight tag
     * @param tagName
     * @param taggedObject
     * @param message
     * @param signed
     * @param forceUpdate
     * @param monitor
     * @return
     * @throws GitException 
     */
    public GitTag createTag (String tagName, String taggedObject, String message, boolean signed, boolean forceUpdate, ProgressMonitor monitor) throws GitException {
        CreateTagCommand cmd = new CreateTagCommand(gitRepository.getRepository(), tagName, taggedObject, message, signed, forceUpdate, monitor);
        cmd.execute();
        return cmd.getTag();
    }

    /**
     * Deletes a given branch from the repository
     * @param branchName
     * @param forceDeleteUnmerged if set to true then trying to delete an unmerged branch will not fail but will forcibly delete the branch
     * @param monitor
     * @throws GitException.NotMergedException branch has not been fully merged yet and forceDeleteUnmerged is set to false
     * @throws GitException 
     */
    public void deleteBranch (String branchName, boolean forceDeleteUnmerged, ProgressMonitor monitor) throws GitException.NotMergedException, GitException {
        DeleteBranchCommand cmd = new DeleteBranchCommand(gitRepository.getRepository(), branchName, forceDeleteUnmerged, monitor);
        cmd.execute();
    }

    /**
     * Deletes a given tag from the repository
     * @param tagName
     * @param monitor
     * @throws GitException 
     */
    public void deleteTag (String tagName, ProgressMonitor monitor) throws GitException {
        DeleteTagCommand cmd = new DeleteTagCommand(gitRepository.getRepository(), tagName, monitor);
        cmd.execute();
    }

    /**
     * Exports a given commit in the format accepted by git am
     * @param commit 
     * @param out 
     * @param monitor 
     * @throws GitException
     */
    public void exportCommit (String commit, OutputStream out, ProgressMonitor monitor) throws GitException {
        ExportCommitCommand cmd = new ExportCommitCommand(gitRepository.getRepository(), commit, out, monitor, delegateListener);
        cmd.execute();
    }
    
    /**
     * Exports changes in files under given roots to the given output stream
     * @param roots
     * @param mode
     * @param out
     * @param monitor
     * @throws GitException 
     */
    public void exportDiff (File[] roots, DiffMode mode, OutputStream out, ProgressMonitor monitor) throws GitException {
        ExportDiffCommand cmd = new ExportDiffCommand(gitRepository.getRepository(), roots, mode, out, monitor, delegateListener);
        cmd.execute();
    }
    
    /**
     * Fetches remote changes for references specified in the config file under a given remote.
     * @param remote should be a name of a remote set up in the repository config file
     * @param monitor
     * @return 
     * @throws GitException 
     * @throws GitException.AuthorizationException unauthorized access
     */
    public Map<String, GitTransportUpdate> fetch (String remote, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        FetchCommand cmd = new FetchCommand(gitRepository.getRepository(), remote, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getUpdates();
    }
    
    /**
     * Fetches remote changes for given reference specifications.
     * @param remote preferably a name of a remote, but can also be directly a URL of a remote repository
     * @param fetchRefSpecifications 
     * @param monitor
     * @return 
     * @throws GitException 
     * @throws GitException.AuthorizationException unauthorized access
     */
    public Map<String, GitTransportUpdate> fetch (String remote, List<String> fetchRefSpecifications, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        FetchCommand cmd = new FetchCommand(gitRepository.getRepository(), remote, fetchRefSpecifications, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getUpdates();
    }
    
    /**
     * Returns all branches
     * @param all if false then only local branches will be returned
     * @return
     */
    public Map<String, GitBranch> getBranches (boolean all, ProgressMonitor monitor) throws GitException {
        ListBranchCommand cmd = new ListBranchCommand(gitRepository.getRepository(), all, monitor);
        cmd.execute();
        return cmd.getBranches();
    }

    /**
     * Returns all tags in the repository
     * @param monitor
     * @param allTags if set to false, only commit tags, otherwise tags for all objects are returned
     * @return
     * @throws GitException 
     */
    public Map<String, GitTag> getTags (ProgressMonitor monitor, boolean allTags) throws GitException {
        ListTagCommand cmd = new ListTagCommand(gitRepository.getRepository(), allTags, monitor);
        cmd.execute();
        return cmd.getTags();
    }

    /**
     * Returns a common ancestor for given revisions or null if none found.
     * @param revisions
     * @param monitor
     * @return
     * @throws GitException 
     */
    public GitRevisionInfo getCommonAncestor (String[] revisions, ProgressMonitor monitor) throws GitException {
        GetCommonAncestorCommand cmd = new GetCommonAncestorCommand(gitRepository.getRepository(), revisions, monitor);
        cmd.execute();
        return cmd.getRevision();
    }

    /**
     * Returns an ancestor revision that modified a given file in any way
     * @param file limit the result only on revision that actually modified somehow the file
     * @param revision
     * @param monitor
     * @return
     * @throws GitException 
     */
    public GitRevisionInfo getPreviousRevision (File file, String revision, ProgressMonitor monitor) throws GitException {
        GetPreviousCommitCommand cmd = new GetPreviousCommitCommand(gitRepository.getRepository(), file, revision, monitor);
        cmd.execute();
        return cmd.getRevision();
    }

    /**
     * Similar to {@link #getStatus(java.io.File[], org.netbeans.libs.git.progress.ProgressMonitor)}, but returns only conflicts.
     * @param roots 
     * @param monitor
     * @return
     */
    public Map<File, GitStatus> getConflicts (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        ConflictCommand cmd = new ConflictCommand(repository, roots, monitor, delegateListener);
        cmd.execute();
        return cmd.getStatuses();
    }

    /**
     * Returns an array of statuses for files under given roots
     * @param roots root folders or files
     * @return status array
     * @throws GitException when an error occurs
     */
    public Map<File, GitStatus> getStatus (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        StatusCommand cmd = new StatusCommand(repository, roots, monitor, delegateListener);
        cmd.execute();
        return cmd.getStatuses();
    }

    /**
     * Returns remote configuration set up for this repository identified by a given remoteName
     * @param remoteName
     * @param monitor
     * @return
     * @throws GitException 
     */
    public GitRemoteConfig getRemote (String remoteName, ProgressMonitor monitor) throws GitException {
        return getRemotes(monitor).get(remoteName);
    }

    /**
     * Returns all remote configurations set up for this repository
     * @param monitor
     * @return
     * @throws GitException 
     */
    public Map<String, GitRemoteConfig> getRemotes (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        GetRemotesCommand cmd = new GetRemotesCommand(repository, monitor);
        cmd.execute();
        return cmd.getRemotes();
    }
    
    /**
     * Returns the current state of the repository this client is associated with.
     * @return current repository state
     * @throws GitException an error occurs
     */
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

    /**
     * Ignores given files
     * @param files
     * @param monitor
     * @return array of .gitignore modified during the ignore process
     * @throws GitException an error occurs
     */
    public File[] ignore (File[] files, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        IgnoreCommand cmd = new IgnoreCommand(repository, files, monitor, delegateListener);
        cmd.execute();
        return cmd.getModifiedIgnoreFiles();
    }

    /**
     * Initializes an empty git repository in a folder specified in the constructor
     * @throws GitException if the repository could not be created either because it already exists inside <code>workDir</code> or cannot be created for other reasons.
     */
    public void init (ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        InitRepositoryCommand cmd = new InitRepositoryCommand(repository, monitor);
        cmd.execute();
    }

    /**
     * Returns files that are marked as modified between the HEAD and Index.
     * @param roots
     * @throws GitException when an error occurs
     */
    public File[] listModifiedIndexEntries (File[] roots, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        ListModifiedIndexEntriesCommand cmd = new ListModifiedIndexEntriesCommand(repository, roots, monitor, delegateListener);
        cmd.execute();
        return cmd.getFiles();
    }
    
    /**
     * Returns branches in a given remote repository
     * @param remoteRepositoryUrl url of the remote repository
     * @param monitor
     * @return
     * @throws GitException 
     * @throws GitException.AuthorizationException unauthorized access
     */
    public Map<String, GitBranch> listRemoteBranches (String remoteRepositoryUrl, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        Repository repository = gitRepository.getRepository();
        ListRemoteBranchesCommand cmd = new ListRemoteBranchesCommand(repository, remoteRepositoryUrl, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getBranches();
    }
    
    /**
     * Returns pairs tag name/id from a given remote repository
     * @param remoteRepositoryUrl url of the remote repository
     * @param monitor
     * @return
     * @throws GitException 
     * @throws GitException.AuthorizationException unauthorized access
     */
    public Map<String, String> listRemoteTags (String remoteRepositoryUrl, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        Repository repository = gitRepository.getRepository();
        ListRemoteTagsCommand cmd = new ListRemoteTagsCommand(repository, remoteRepositoryUrl, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getTags();
    }

    /**
     * Digs through the repository's history and returns the revision information belonging to the given revision string.
     * @param revision
     * @param monitor
     * @return revision
     * @throws GitException.MissingObjectException no such revision exists
     * @throws GitException other error occurs
     */
    public GitRevisionInfo log (String revision, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        LogCommand cmd = new LogCommand(repository, revision, monitor, delegateListener);
        cmd.execute();
        GitRevisionInfo[] revisions = cmd.getRevisions();
        return revisions.length == 0 ? null : revisions[0];
    }

    /**
     * Digs through the repository's history and returns revisions according to the given search criteria.
     * @param searchCriteria
     * @param monitor 
     * @return revisions that fall between the given boundaries
     * @throws GitException.MissingObjectException revision specified in search criteria (or head if no such revision is specified) does not exist
     * @throws GitException other error occurs
     */
    public GitRevisionInfo[] log (SearchCriteria searchCriteria, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        LogCommand cmd = new LogCommand(repository, searchCriteria, monitor, delegateListener);
        cmd.execute();
        return cmd.getRevisions();
    }
    
    /**
     * Merges a given revision with the current head
     * @param revision
     * @param monitor
     * @return result of the merge
     * @throws GitException.CheckoutConflictException there are local modifications in Working Tree, merge fails in such a case
     * @throws GitException an error occurs
     */
    public GitMergeResult merge (String revision, ProgressMonitor monitor) throws GitException.CheckoutConflictException, GitException {
        Repository repository = gitRepository.getRepository();
        MergeCommand cmd = new MergeCommand(repository, revision, monitor);
        cmd.execute();
        return cmd.getResult();
    }
    
    /**
     * Pulls changes from a remote repository and merges a given remote branch to an active one.
     * @param remote preferably a name of a remote, but can also be directly a URL of a remote repository
     * @param fetchRefSpecifications 
     * @param branchToMerge a remote branch that will be merged into an active branch
     * @param monitor
     * @return 
     * @throws GitException 
     * @throws GitException.AuthorizationException unauthorized access
     * @throws GitException.CheckoutConflictException there are local changes in the working tree that would result in a merge conflict
     * @throws GitException.MissingObjectException given branch to merge does not exist
     */
    public GitPullResult pull (String remote, List<String> fetchRefSpecifications, String branchToMerge, ProgressMonitor monitor) throws GitException.AuthorizationException, 
            GitException.CheckoutConflictException, GitException.MissingObjectException, GitException {
        PullCommand cmd = new PullCommand(gitRepository.getRepository(), remote, fetchRefSpecifications, branchToMerge, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getResult();
    }
    
    /**
     * Pushes changes for given reference specifications.
     * @param remote preferably a name of a remote, but can also be directly a URL of a remote repository
     * @param pushRefSpecifications 
     * @param fetchRefSpecifications 
     * @param monitor
     * @return 
     * @throws GitException 
     * @throws GitException.AuthorizationException unauthorized access
     */
    public GitPushResult push (String remote, List<String> pushRefSpecifications, List<String> fetchRefSpecifications, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        PushCommand cmd = new PushCommand(gitRepository.getRepository(), remote, pushRefSpecifications, fetchRefSpecifications, monitor);
        cmd.setCredentialsProvider(this.credentialsProvider);
        cmd.execute();
        return cmd.getResult();
    }

    /**
     * Removes given files/folders from the index and/or from the working tree
     * @param roots files/folders to remove, can not be empty
     * @param cached if <code>true</code> the working tree will not be affected
     * @param monitor
     */
    public void remove (File[] roots, boolean cached, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RemoveCommand cmd = new RemoveCommand(repository, roots, cached, monitor, delegateListener);
        cmd.execute();
    }

    public void removeNotificationListener (NotificationListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Removes remote configuration from the config file
     * @param remote name of the remote
     * @param monitor 
     */
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
    public void rename (File source, File target, boolean after, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        RenameCommand cmd = new RenameCommand(repository, source, target, after, monitor, delegateListener);
        cmd.execute();
    }
    
    /**
     * Updates entries for given files in the index with those from the given revision
     * @param revision revision to go back to
     * @param roots files or folders to update in the index
     * @param recursively if set to <code>true</code>, all files under given roots will be affected, otherwise only roots and direct file children will be modified in the index.
     * @throws GitException
     */
    public void reset (File[] roots, String revision, boolean recursively, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        ResetCommand cmd = new ResetCommand(repository, revision, roots, recursively, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Sets HEAD to the given revision and updates index and working copy accordingly to the given reset type
     * @param revisionStr revision HEAD will reference to
     * @param resetType type of reset, see git help reset
     * @throws GitException
     */
    public void reset (String revision, ResetType resetType, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        Repository repository = gitRepository.getRepository();
        ResetCommand cmd = new ResetCommand(repository, revision, resetType, monitor, delegateListener);
        cmd.execute();
    }

    /**
     * Reverts already committed changes
     * @param revision
     * @param commitMessage used as the commit message for the revert commit. If set to null or an empty value, a default value will be used for the commit message
     * @param commit if set to false, the revert modifications will not be committed but will stay in index
     * @return 
     * @throws org.netbeans.libs.git.GitException.MissingObjectException
     * @throws GitException.CheckoutConflictException there are local modifications in Working Tree, merge fails in such a case
     * @throws GitException 
     */
    public GitRevertResult revert (String revision, String commitMessage, boolean commit, ProgressMonitor monitor)
            throws GitException.MissingObjectException, GitException.CheckoutConflictException, GitException {
        Repository repository = gitRepository.getRepository();
        RevertCommand cmd = new RevertCommand(repository, revision, commitMessage, commit, monitor);
        cmd.execute();
        return cmd.getResult();
    }

    /**
     * Sets callback for this client. Some actions (like inter-repository commands) may need it for its work.
     */
    public void setCallback (GitClientCallback callback) {
        this.credentialsProvider = callback == null ? null : new JGitCredentialsProvider(callback);
    }
    
    /**
     * Sets the remote configuration in the configuration file.
     * @param remoteConfig
     * @param monitor 
     */
    public void setRemote (GitRemoteConfig remoteConfig, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        SetRemoteCommand cmd = new SetRemoteCommand(repository, remoteConfig, monitor);
        cmd.execute();
    }

    /**
     * Unignores given files
     * @param files
     * @param monitor
     * @return array of .gitignore modified during the unignore process
     * @throws GitException an error occurs
     */
    public File[] unignore (File[] files, ProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        UnignoreCommand cmd = new UnignoreCommand(repository, files, monitor, delegateListener);
        cmd.execute();
        return cmd.getModifiedIgnoreFiles();
    }

    /**
     * Returns the user from this clients repository
     */
    public GitUser getUser() throws GitException {        
        return new JGitUserInfo(new PersonIdent(gitRepository.getRepository()));
    }
    
    private class DelegateListener implements StatusListener, FileListener, RevisionInfoListener {

        @Override
        public void notifyStatus (GitStatus status) {
            GitClient.this.notifyStatus(status);
        }

        @Override
        public void notifyFile (File file, String relativePathToRoot) {
            GitClient.this.notifyFile(file, relativePathToRoot);
        }

        @Override
        public void notifyRevisionInfo (GitRevisionInfo revisionInfo) {
            GitClient.this.notifyRevisionInfo(revisionInfo);
        }
        
    }
    
    // <editor-fold defaultstate="collapsed" desc="listener methods">
    private void notifyFile (File file, String relativePathToRoot) {
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

    private void notifyStatus (GitStatus status) {
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

    private void notifyRevisionInfo (GitRevisionInfo info) {
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
