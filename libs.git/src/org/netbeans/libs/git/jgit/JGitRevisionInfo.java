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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.netbeans.libs.git.GitFileInfo;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;

/**
 * ChangeSet represents one revision.
 * @author Jan Becicka
 */
public final class JGitRevisionInfo implements GitRevisionInfo {

    private RevCommit revCommit;
    private Repository repository;
    private GitFileInfo[] modifiedFiles;
    private static final Logger LOG = Logger.getLogger(JGitRevisionInfo.class.getName());

    public JGitRevisionInfo(RevCommit commit, Repository repository) {
        this.revCommit = commit;
        this.repository = repository;
    }

    /**
     * revision string
     * @return
     */
    @Override
    public final String getRevision () {
        return ObjectId.toString(revCommit.getId());
    }

    /**
     * returns short message
     * @return
     */
    @Override
    public final String getShortMessage () {
        return revCommit.getShortMessage();
    }

    /**
     * returns full message
     *
     * @return
     */
    @Override
    public final String getFullMessage () {
        return revCommit.getFullMessage();
    }

    /**
     * getter for commit time, time is in milliseconds
     * @return
     */
    @Override
    public final long getCommitTime () {
        return (long) revCommit.getCommitTime() * 1000;
    }

    /**
     * returns author of this change set
     * @return
     */
    @Override
    public final JGitUserInfo getAuthor () {
        return new JGitUserInfo(revCommit.getAuthorIdent());
    }

    @Override
    public GitUser getCommitter() {
        return new JGitUserInfo(revCommit.getCommitterIdent());
    }
    
    /**
     * files affected by this change set
     * @return
     * @throws GitException
     */
    @Override
    public final Map<File, GitFileInfo> getModifiedFiles () throws GitException {
        if (modifiedFiles == null) {
            synchronized (this) {
                listFiles();
            }
        }
        Map<File, GitFileInfo> files = new HashMap<File, GitFileInfo>(modifiedFiles.length);
        for (GitFileInfo info : modifiedFiles) {
            files.put(info.getFile(), info);
        }
        return files;
    }

    private void listFiles() throws GitException {
        try {
            ArrayList<GitFileInfo> result = new ArrayList<GitFileInfo>();

            TreeWalk walk = new TreeWalk(repository);
            walk.reset();
            walk.setRecursive(true);
            walk.addTree(revCommit.getTree().getId());
            RevWalk revWalk = new RevWalk(repository);
            for (int i = 0; i < revCommit.getParentCount(); ++i) {
                RevCommit parentCommit = revWalk.parseCommit(revCommit.getParent(i).getId());
                walk.addTree(parentCommit.getTree().getId());
            }
            revWalk.release();
            walk.setFilter(AndTreeFilter.create(TreeFilter.ANY_DIFF, PathFilter.ANY_DIFF));

            final int nTree = walk.getTreeCount();
            while (walk.next()) {
                GitStatus.Status changeType = GitStatus.Status.STATUS_ADDED;
                final int currentMode = walk.getRawMode(0);
                if (nTree > 1) {
                    changeType = GitStatus.Status.STATUS_MODIFIED;
                    final int m1 = walk.getRawMode(1);
                    if (currentMode == 0) {
                       changeType = GitStatus.Status.STATUS_REMOVED;
                    } else if (!presentInParents(walk)) {
                       changeType = GitStatus.Status.STATUS_ADDED;
                    }
                }
                result.add(new GitFileInfo(new File(repository.getWorkTree(), walk.getPathString()), walk.getPathString(), changeType));
            }
            this.modifiedFiles = result.toArray(new GitFileInfo[result.size()]);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    private boolean presentInParents (TreeWalk walk) {
        boolean present = true;
        for (int i = 1; i < walk.getTreeCount(); ++i) {
            if (walk.getRawMode(i) == 0) {
                present = false;
                break;
            }
        }
        return present;
    }
}
