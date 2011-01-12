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

    /**
     * Adds all files under the given roots to the index
     * @param roots
     * @param monitor
     * @throws GitException an error occurs
     */
    public void add(File[] roots, ProgressMonitor monitor) throws GitException;

    public void addNotificationListener (NotificationListener listener);

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
     * @throws GitException other error
     */
    public void checkout(File[] roots, String revision, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException;

    /**
     * Cleans the working tree by recursively removing files that are not under 
 *   * version control starting from the given roots.
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
     * Returns all branches
     * @param all if false then only local branches will be returned
     * @return
     */
    public Map<String, GitBranch> getBranches (boolean all, ProgressMonitor monitor) throws GitException;

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
     * Returns the current state of the repository this client is associated with.
     * @return current repository state
     * @throws GitException an error occurs
     */
    public GitRepositoryState getRepositoryState (ProgressMonitor monitor) throws GitException;

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
     * Digs through the repository's history and returns the revision information belonging to the given revision string.
     * @param revision
     * @param monitor
     * @return revision
     */
    public GitRevisionInfo log (String revision, ProgressMonitor monitor) throws GitException;

    /**
     * Digs through the repository's history and returns revisions according to the given search criteria.
     * @param searchCriteria
     * @param monitor 
     * @return revisions that fall between the given boundaries
     */
    public GitRevisionInfo[] log (SearchCriteria searchCriteria, ProgressMonitor monitor) throws GitException;

    /**
     * Removes given files/folders from the index and/or from the working tree
     * @param roots files/folders to remove, can not be empty
     * @param cached if <code>true</code> the working tree will not be affected
     * @param monitor
     */
    public void remove (File[] roots, boolean cached, ProgressMonitor monitor) throws GitException;
    public void removeNotificationListener (NotificationListener listener);

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
     * @throws GitException
     */
    public void reset (File[] roots, String revision, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException;

    /**
     * Sets HEAD to the given revision and updates index and working copy accordingly to the given reset type
     * @param revisionStr revision HEAD will reference to
     * @param resetType type of reset, see git help reset
     * @throws GitException
     */
    public void reset (String revision, ResetType resetType, ProgressMonitor monitor) throws GitException.MissingObjectException, GitException;

    /**
     * Returns the user from this clients repository
     */
    public GitUser getUser() throws GitException;
}
