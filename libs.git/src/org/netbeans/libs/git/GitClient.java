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
import java.util.List;
import java.util.Map;
import org.netbeans.libs.git.progress.NotificationListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public interface GitClient {

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

    /**
     * Adds all files under the given roots to the index
     * @param roots
     * @param monitor
     * @throws GitException an error occurs
     */
    public void add(File[] roots, ProgressMonitor monitor) throws GitException;

    public void addNotificationListener (NotificationListener listener);

    /**
     * Annotates lines of a given file in a given revision
     * @param file
     * @param revision null for blaming a checked-out file against HEAD
     * @param monitor
     * @return
     * @throws org.netbeans.libs.git.GitException.MissingObjectException
     * @throws GitException 
     */
    public GitBlameResult blame (File file, String revision, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException;

    /**
     * Prints file's content in the given revision to output stream
     * @param file
     * @param revision git revision, never null
     * @param out output stream
     * @return true if the file was found in the specified revision and printed to out, otherwise false
     * @throws GitException
     * @throws GitException.MissingObjectException if the given revision does not exist
     */
    public boolean catFile (File file, String revision, java.io.OutputStream out, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException;

    /**
     * Prints content of an index entry accordant with the given file to output stream
     * @param file
     * @param stage 
     * @param out output stream
     * @return true if the file was found in the index and printed to out, otherwise false
     * @throws GitException
     */
    public boolean catIndexEntry (File file, int stage, java.io.OutputStream out, ProgressMonitor monitor) throws GitException;

    /**
     * Checks out the index into the working copy root. Does not move HEAD.
     * @param revision if not null, index is updated with the revision content before checking out to WC
     * @param roots files/folders to checkout
     * @param recursively if set to <code>true</code>, all files under given roots will be checked out, otherwise only roots and direct file children will be affected.
     * @throws GitException other error
     */
    public void checkout(File[] roots, String revision, boolean recursively, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException;

    /**
     * Checks out a given revision.
     * @param revision cannot be null. If the value equals to anything other than an existing branch name, the revision will be checked out
     * and the working tree will be in the detached HEAD state.
     * @param failOnConflict if set to false, the command tries to merge local changes into the new branch
     * @throws GitException other error
     */
    public void checkoutRevision (String revision, boolean failOnConflict, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException;

    /**
     * Cleans the working tree by recursively removing files that are not under 
     * version control starting from the given roots.
     * @param roots
     * @param monitor
     * @throws GitException 
     */
    public void clean(File[] roots, ProgressMonitor monitor) throws GitException;
    
    /**
     * Commits all changes made in the index to all files under the given roots
     * @param roots
     * @param commitMessage
     * @param author
     * @param commiter
     * @param monitor
     * @throws GitException an error occurs
     */
    public GitRevisionInfo commit(File[] roots, String commitMessage, GitUser author, GitUser commiter, ProgressMonitor monitor) throws GitException;

    /**
     * Modifies the index. The entries representing files under the source are copied and the newly created entries represent the corresponding files under the target.
     * @param source
     * @param target
     * @param monitor
     * @throws GitException
     */
    public void copyAfter (File source, File target, ProgressMonitor monitor) throws GitException;

    /**
     * Creates a new branch with a given name, starting at revision
     * @param branchName
     * @param revision
     * @param monitor
     * @return created branch
     * @throws GitException  an error occurs
     */
    public GitBranch createBranch (String branchName, String revision, ProgressMonitor monitor) throws GitException;

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
    public GitTag createTag (String tagName, String taggedObject, String message, boolean signed, boolean forceUpdate, ProgressMonitor monitor) throws GitException;

    /**
     * Deletes a given branch from the repository
     * @param branchName
     * @param forceDeleteUnmerged if set to true then trying to delete an unmerged branch will not fail but will forcibly delete the branch
     * @param monitor
     * @throws GitException.NotMergedException branch has not been fully merged yet and forceDeleteUnmerged is set to false
     * @throws GitException 
     */
    public void deleteBranch (String branchName, boolean forceDeleteUnmerged, ProgressMonitor monitor) throws GitException.NotMergedException, GitException;

    /**
     * Deletes a given tag from the repository
     * @param tagName
     * @param monitor
     * @throws GitException 
     */
    public void deleteTag (String tagName, ProgressMonitor monitor) throws GitException;

    /**
     * Exports a given commit in the format accepted by git am
     * @param commit 
     * @param out 
     * @param monitor 
     * @throws GitException
     */
    public void exportCommit (String commit, OutputStream out, ProgressMonitor monitor) throws GitException;
    
    /**
     * Exports changes in files under given roots to the given output stream
     * @param roots
     * @param mode
     * @param out
     * @param monitor
     * @throws GitException 
     */
    public void exportDiff (File[] roots, DiffMode mode, OutputStream out, ProgressMonitor monitor) throws GitException;
    
    /**
     * Fetches remote changes for references specified in the config file under a given remote.
     * @param remote should be a name of a remote set up in the repository config file
     * @param monitor
     * @return 
     * @throws GitException 
     * @throws GitException.AuthorizationException unauthorized access
     */
    public Map<String, GitTransportUpdate> fetch (String remote, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException;
    
    /**
     * Fetches remote changes for given reference specifications.
     * @param remote preferably a name of a remote, but can also be directly a URL of a remote repository
     * @param fetchRefSpecifications 
     * @param monitor
     * @return 
     * @throws GitException 
     * @throws GitException.AuthorizationException unauthorized access
     */
    public Map<String, GitTransportUpdate> fetch (String remote, List<String> fetchRefSpecifications, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException;
    
    /**
     * Returns all branches
     * @param all if false then only local branches will be returned
     * @return
     */
    public Map<String, GitBranch> getBranches (boolean all, ProgressMonitor monitor) throws GitException;

    /**
     * Returns all tags in the repository
     * @param monitor
     * @param allTags if set to false, only commit tags, otherwise tags for all objects are returned
     * @return
     * @throws GitException 
     */
    public Map<String, GitTag> getTags (ProgressMonitor monitor, boolean allTags) throws GitException;

    /**
     * Returns a common ancestor for given revisions or null if none found.
     * @param revisions
     * @param monitor
     * @return
     * @throws GitException 
     */
    public GitRevisionInfo getCommonAncestor (String[] revisions, ProgressMonitor monitor) throws GitException;

    /**
     * Returns an ancestor revision that modified a given file in any way
     * @param file limit the result only on revision that actually modified somehow the file
     * @param revision
     * @param monitor
     * @return
     * @throws GitException 
     */
    public GitRevisionInfo getPreviousRevision (File file, String revision, ProgressMonitor monitor) throws GitException;

    /**
     * Similar to {@link #getStatus(java.io.File[], org.netbeans.libs.git.progress.ProgressMonitor)}, but returns only conflicts.
     * @param roots 
     * @param monitor
     * @return
     */
    public Map<File, GitStatus> getConflicts (File[] roots, ProgressMonitor monitor) throws GitException;

    /**
     * Returns an array of statuses for files under given roots
     * @param roots root folders or files
     * @return status array
     * @throws GitException when an error occurs
     */
    public Map<File, GitStatus> getStatus (File[] roots, ProgressMonitor monitor) throws GitException;

    /**
     * Returns remote configuration set up for this repository identified by a given remoteName
     * @param remoteName
     * @param monitor
     * @return
     * @throws GitException 
     */
    public GitRemoteConfig getRemote (String remoteName, ProgressMonitor monitor) throws GitException;

    /**
     * Returns all remote configurations set up for this repository
     * @param monitor
     * @return
     * @throws GitException 
     */
    public Map<String, GitRemoteConfig> getRemotes (ProgressMonitor monitor) throws GitException;
    
    /**
     * Returns the current state of the repository this client is associated with.
     * @return current repository state
     * @throws GitException an error occurs
     */
    public GitRepositoryState getRepositoryState (ProgressMonitor monitor) throws GitException;

    /**
     * Ignores given files
     * @param files
     * @param monitor
     * @return array of .gitignore modified during the ignore process
     * @throws GitException an error occurs
     */
    public File[] ignore (File[] files, ProgressMonitor monitor) throws GitException;

    /**
     * Initializes an empty git repository
     * @throws GitException if the repository could not be created either because it already exists inside <code>workDir</code> or cannot be created for other reasons.
     * XXX init what???
     */
    public void init (ProgressMonitor monitor) throws GitException;

    /**
     * TODO is this method really necessary?
     * Returns files that are marked as modified between the HEAD and Index.
     * @param roots
     * @throws GitException when an error occurs
     */
    public File[] listModifiedIndexEntries (File[] roots, ProgressMonitor monitor) throws GitException;
    
    /**
     * Returns branches in a given remote repository
     * @param remoteRepositoryUrl url of the remote repository
     * @param monitor
     * @return
     * @throws GitException 
     * @throws GitException.AuthorizationException unauthorized access
     */
    public Map<String, GitBranch> listRemoteBranches (String remoteRepositoryUrl, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException;
    
    /**
     * Returns pairs tag name/id from a given remote repository
     * @param remoteRepositoryUrl url of the remote repository
     * @param monitor
     * @return
     * @throws GitException 
     * @throws GitException.AuthorizationException unauthorized access
     */
    public Map<String, String> listRemoteTags (String remoteRepositoryUrl, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException;

    /**
     * Digs through the repository's history and returns the revision information belonging to the given revision string.
     * @param revision
     * @param monitor
     * @return revision
     * @throws GitException.MissingObjectException no such revision exists
     * @throws GitException other error occurs
     */
    public GitRevisionInfo log (String revision, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException;

    /**
     * Digs through the repository's history and returns revisions according to the given search criteria.
     * @param searchCriteria
     * @param monitor 
     * @return revisions that fall between the given boundaries
     * @throws GitException.MissingObjectException revision specified in search criteria (or head if no such revision is specified) does not exist
     * @throws GitException other error occurs
     */
    public GitRevisionInfo[] log (SearchCriteria searchCriteria, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException;
    
    /**
     * Merges a given revision with the current head
     * @param revision
     * @param monitor
     * @return result of the merge
     * @throws GitException.CheckoutConflictException there are local modifications in Working Tree, merge fails in such a case
     * @throws GitException an error occurs
     */
    public GitMergeResult merge (String revision, ProgressMonitor monitor) throws GitException.CheckoutConflictException, GitException;
    
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
            GitException.CheckoutConflictException, GitException.MissingObjectException, GitException;
    
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
    public GitPushResult push (String remote, List<String> pushRefSpecifications, List<String> fetchRefSpecifications, ProgressMonitor monitor) throws GitException.AuthorizationException, GitException;

    /**
     * Removes given files/folders from the index and/or from the working tree
     * @param roots files/folders to remove, can not be empty
     * @param cached if <code>true</code> the working tree will not be affected
     * @param monitor
     */
    public void remove (File[] roots, boolean cached, ProgressMonitor monitor) throws GitException;
    public void removeNotificationListener (NotificationListener listener);
    
    /**
     * Removes remote configuration from the config file
     * @param remote name of the remote
     * @param monitor 
     */
    public void removeRemote (String remote, ProgressMonitor monitor) throws GitException;

    /**
     * Renames source file or folder to target
     * @param source file or folder to be renamed
     * @param target target file or folder. Must not yet exist.
     * @param after set to true if you don't only want to correct the index
     * @throws GitException
     */
    public void rename (File source, File target, boolean after, ProgressMonitor monitor) throws GitException;
    
    /**
     * Updates entries for given files in the index with those from the given revision
     * @param revision revision to go back to
     * @param roots files or folders to update in the index
     * @param recursively if set to <code>true</code>, all files under given roots will be affected, otherwise only roots and direct file children will be modified in the index.
     * @throws GitException
     */
    public void reset (File[] roots, String revision, boolean recursively, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException;

    /**
     * Sets HEAD to the given revision and updates index and working copy accordingly to the given reset type
     * @param revisionStr revision HEAD will reference to
     * @param resetType type of reset, see git help reset
     * @throws GitException
     */
    public void reset (String revision, ResetType resetType, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException;

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
    public GitRevertResult revert (String revision, String commitMessage, boolean commit, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException.CheckoutConflictException, GitException;

    /**
     * Sets callback for this client. Some actions (like inter-repository commands) may need it for its work.
     */
    public void setCallback (GitClientCallback callback);
    
    /**
     * Sets the remote configuration in the configuration file.
     * @param remoteConfig
     * @param monitor 
     */
    public void setRemote (GitRemoteConfig remoteConfig, ProgressMonitor monitor) throws GitException;

    /**
     * Unignores given files
     * @param files
     * @param monitor
     * @return array of .gitignore modified during the unignore process
     * @throws GitException an error occurs
     */
    public File[] unignore (File[] files, ProgressMonitor monitor) throws GitException;

    /**
     * Returns the user from this clients repository
     */
    public GitUser getUser() throws GitException;
}
