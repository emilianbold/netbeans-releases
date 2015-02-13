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

package org.netbeans.libs.git.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.jgit.Utils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 * Provides information about a certain commit, usually is returned by 
 * git commit or log command.
 * 
 * @author Jan Becicka
 */
public final class GitRevisionInfo {
    private static final Logger LOG = Logger.getLogger(GitRevisionInfo.class.getName());
    private RevCommit revCommit;
    private JGitRepository repository;
    private final Map<String, GitBranch> branches;
    private GitFileInfo[] modifiedFiles;
    private String shortMessage;
    //CLI:
    private String branch;
    private String revisionCode;
    private String message;
    private String autorAndMail;
    private String commiterAndMail;
    private String autorTime;
    private String commiterTime;
    private String[] parents;
    private final boolean isKIT;

    GitRevisionInfo (RevCommit commit, JGitRepository repository) {
        this(commit, Collections.<String, GitBranch>emptyMap(), repository);
    }

    GitRevisionInfo (RevCommit commit, Map<String, GitBranch> affectedBranches, JGitRepository repository) {
        this.revCommit = commit;
        this.repository = repository;
        this.branches = Collections.unmodifiableMap(affectedBranches);
        isKIT = true;
    }

    GitRevisionInfo(GitRevCommit status, JGitRepository repository) {
        this.branch = status.branch;
        this.branches = Collections.<String, GitBranch>emptyMap();
        this.revisionCode = status.revisionCode;
        this.message = status.message;
        this.autorAndMail = status.autorAndMail;
        autorTime = status.autorTime;
        modifiedFiles = new GitFileInfo[status.commitedFiles.size()];
        int i = 0;
        for (Map.Entry<String, GitRevisionInfo.GitFileInfo.Status> entry : status.commitedFiles.entrySet()) {
            VCSFileProxy file = VCSFileProxy.createFileProxy(repository.getLocation(), entry.getKey());
            GitFileInfo info = new GitFileInfo(file, entry.getKey(), entry.getValue(), null, null);
            modifiedFiles[i++] = info;
        }
        parents = status.parents.toArray(new String[status.parents.size()]);
        commiterTime = status.commiterTime;
        this.repository = repository;
        isKIT = false;
    }


    /**
     * @return id of the commit
     */
    public String getRevision () {
        if (isKIT) {
            return ObjectId.toString(revCommit.getId());
        } else {
            return revisionCode;
        }
    }

    /**
     * @return the first line of the commit message.
     */
    public String getShortMessage () {
        if (isKIT) {
            if (shortMessage == null) {
                String msg = revCommit.getFullMessage();
                StringBuilder sb = new StringBuilder();
                boolean empty = true;
                for (int pos = 0; pos < msg.length(); ++pos) {
                    char c = msg.charAt(pos);
                    if (c == '\r' || c == '\n') {
                        if (!empty) {
                            break;
                        }
                    } else {
                        sb.append(c);
                        empty = false;
                    }
                }
                shortMessage = sb.toString();
            }
            return shortMessage;
        } else {
            return message;
        }
    }

    /**
     * @return full commit message
     */
    public String getFullMessage () {
        if (isKIT) {
            return revCommit.getFullMessage();
        } else {
            return message;
        }
    }

    /**
     * @return time this commit was created in milliseconds.
     */
    public long getCommitTime () {
        if (isKIT) {
            // must be indeed author, that complies with CLI
            // committer time is different after rebase
            PersonIdent author = revCommit.getAuthorIdent();
            if (author == null) {
                return (long) revCommit.getCommitTime() * 1000;
            } else {
                return author.getWhen().getTime();
            }
        } else {
            if (autorTime != null) {
                //1423691643 -0800
                String[] s = autorTime.split(" ");
                long res = Long.parseLong(s[0])*1000;
                //int zone = Integer.parseInt(s[1]);
                //res += (zone/100)*3600*1000;
                return res;
            }
            if (commiterTime != null) {
                String[] s = commiterTime.split(" ");
                long res = Long.parseLong(s[0])*1000;
                //int zone = Integer.parseInt(s[1]);
                //res += (zone/100)*3600*1000;
                return res;
            }
        }
        return -1;
    }

    /**
     * @return author of the commit
     */
    public GitUser getAuthor () {
        if (isKIT) {
            return GitClassFactoryImpl.getInstance().createUser(revCommit.getAuthorIdent());
        } else {
            if (autorAndMail != null) {
                int i = autorAndMail.indexOf("<");
                return new GitUser(autorAndMail.substring(0,i).trim(), autorAndMail.substring(i));
            }
        }
        return null;
    }

    /**
     * @return person who actually committed the changes, may or may not be the same as a return value of the <code>getAuthor</code> method.
     */
    public GitUser getCommitter () {
        if (isKIT) {
            return GitClassFactoryImpl.getInstance().createUser(revCommit.getCommitterIdent());
        } else {
            if (commiterAndMail != null) {
                int i = commiterAndMail.indexOf("<");
                return new GitUser(commiterAndMail.substring(0,i).trim(), commiterAndMail.substring(i));
            }
        }
        return null;
    }
    
    /**
     * Returns the information about the files affected (modified, deleted or added) by this commit.
     * <strong>First time call should not be done from the EDT.</strong> When called for the first time the method execution can take a big amount of time
     * because it compares the commit tree with its parents and identifies the modified files. 
     * Any subsequent call to the first <strong>successful</strong> call will return the cached value and will be fast.
     * @return files affected by this change set
     * @throws GitException when an error occurs
     */
    public java.util.Map<VCSFileProxy, GitFileInfo> getModifiedFiles () throws GitException {
        if (modifiedFiles == null) {
            synchronized (this) {
                listFiles();
            }
        }
        Map<VCSFileProxy, GitFileInfo> files = new HashMap<VCSFileProxy, GitFileInfo>(modifiedFiles.length);
        for (GitFileInfo info : modifiedFiles) {
            files.put(info.getFile(), info);
        }
        return files;
    }
    
    /**
     * @return commit ids of this commit's parents
     */
    public String[] getParents () {
        if (isKIT) {
            String[] parents = new String[revCommit.getParentCount()];
            for (int i = 0; i < revCommit.getParentCount(); ++i) {
                parents[i] = ObjectId.toString(revCommit.getParent(i).getId());
            }
            return parents;
        } else {
            return parents;
        }
    }
    
    /**
     * @return all branches known to contain this commit.
     * @since 1.14
     */
    public Map<String, GitBranch> getBranches () {
        return branches;
    }
    
    private void listFiles() throws GitException {
        RevWalk revWalk = new RevWalk(repository.getRepository());
        TreeWalk walk = new TreeWalk(repository.getRepository());
        try {
            List<GitFileInfo> result;
            walk.reset();
            walk.setRecursive(true);
            RevCommit parentCommit = null;
            if (revCommit.getParentCount() > 0) {
                for (RevCommit commit : revCommit.getParents()) {
                    revWalk.markStart(revWalk.lookupCommit(commit));
                }
                revWalk.setRevFilter(RevFilter.MERGE_BASE);
                Iterator<RevCommit> it = revWalk.iterator();
                if (it.hasNext()) {
                    parentCommit = it.next();
                }
                if (parentCommit != null) {
                    walk.addTree(parentCommit.getTree().getId());
                }
            }
            walk.addTree(revCommit.getTree().getId());
            walk.setFilter(AndTreeFilter.create(TreeFilter.ANY_DIFF, PathFilter.ANY_DIFF));
            if (parentCommit != null) {
                result = Utils.getDiffEntries(repository, walk, GitClassFactoryImpl.getInstance());
            } else {
                result = new ArrayList<GitFileInfo>();
                while (walk.next()) {
                    result.add(new GitFileInfo(VCSFileProxy.createFileProxy(repository.getLocation(), walk.getPathString()), walk.getPathString(), GitFileInfo.Status.ADDED, null, null));
                }
            }
            this.modifiedFiles = result.toArray(new GitFileInfo[result.size()]);
        } catch (IOException ex) {
            throw new GitException(ex);
        } finally {
            revWalk.release();
            walk.release();
        }
    }

    private static Map<String, GitBranch> buildBranches (RevCommit commit, Map<String, GitBranch> branches) {
        Map<String, GitBranch> retval = new LinkedHashMap<>(branches.size());
        
        return retval;
    }
    
    /**
     * Provides information about what happened to a file between two different commits.
     * If the file is copied or renamed between the two commits, you can get the path
     * of the original file.
     */
    public static final class GitFileInfo {

        /**
         * State of the file in the second commit in relevance to the first commit.
         */
        public static enum Status {
            ADDED,
            MODIFIED,
            RENAMED,
            COPIED,
            REMOVED,
            UNKNOWN
        }

        private final String relativePath;
        private final String originalPath;
        private final Status status;
        private final VCSFileProxy file;
        private final VCSFileProxy originalFile;

        GitFileInfo (VCSFileProxy file, String relativePath, Status status, VCSFileProxy originalFile, String originalPath) {
            this.relativePath = relativePath;
            this.status = status;
            this.file = file;
            this.originalFile = originalFile;
            this.originalPath = originalPath;
        }

        /**
         * @return relative path of the file to the root of the repository
         */
        public String getRelativePath() {
            return relativePath;
        }

        /**
         * @return the relative path of the original file this file was copied or renamed from.
         *         For other statuses than <code>COPIED</code> or <code>RENAMED</code> it may be <code>null</code> 
         *         or the same as the return value of <code>getPath</code> method
         */
        public String getOriginalPath() {
            return originalPath;
        }

        /**
         * @return state of the file between the two commits
         */
        public Status getStatus() {
            return status;
        }

        /**
         * @return the file this refers to
         */
        public VCSFileProxy getFile () {
            return file;
        }

        /**
         * @return the original file this file was copied or renamed from.
         *         For other statuses than <code>COPIED</code> or <code>RENAMED</code> it may be <code>null</code> 
         *         or the same as the return value of <code>getFile</code> method
         */
        public VCSFileProxy getOriginalFile () {
            return originalFile;
        }
    }
    
    public static final class GitRevCommit {
        public String branch;
        public String revisionCode;
        public String treeCode;
        public String message;
        public String autorAndMail;
        public String autorTime;
        public String commiterAndMail;
        public String commiterTime;
        public LinkedHashMap<String, GitRevisionInfo.GitFileInfo.Status> commitedFiles = new LinkedHashMap<String, GitRevisionInfo.GitFileInfo.Status>();
        public ArrayList<String> parents = new ArrayList<String>();

        public GitRevCommit() {
        }
    }

    
}
