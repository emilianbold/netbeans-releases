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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.JGitStatus;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.progress.StatusListener;

/**
 *
 * @author ondra
 */
public class StatusCommand extends GitCommand {
    private final LinkedHashMap<File, GitStatus> statuses;
    private final File[] roots;
    private final ProgressMonitor monitor;
    private final StatusListener listener;

    public StatusCommand (Repository repository, File[] roots, ProgressMonitor monitor, StatusListener listener) {
        super(repository, monitor);
        this.roots = roots;
        this.monitor = monitor;
        this.listener = listener;
        statuses = new LinkedHashMap<File, GitStatus>();
    }

    @Override
    protected String getCommandDescription () {
        return "status"; //NOI18N
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        try {
            DirCache cache = repository.readDirCache();
            try {
                String workTreePath = repository.getWorkTree().getAbsolutePath();
                Collection<PathFilter> pathFilters = Utils.getPathFilters(repository.getWorkTree(), roots);
                Map<String, DiffEntry> renames = detectRenames(repository, cache, pathFilters);
                TreeWalk treeWalk = new TreeWalk(repository);
                if (!pathFilters.isEmpty()) {
                    treeWalk.setFilter(PathFilterGroup.create(pathFilters));
                }
                treeWalk.setRecursive(false);
                treeWalk.reset();
                ObjectId headId = repository.resolve(Constants.HEAD);
                if (headId != null) {
                    treeWalk.addTree(new RevWalk(repository).parseTree(headId));
                } else {
                    treeWalk.addTree(new EmptyTreeIterator());
                }
                // Index
                treeWalk.addTree(new DirCacheIterator(cache));
                // Working directory
                treeWalk.addTree(new FileTreeIterator(repository));
                final int T_HEAD = 0;
                final int T_INDEX = 1;
                final int T_WORKSPACE = 2;
                while (treeWalk.next() && !monitor.isCanceled()) {
                    String path = treeWalk.getPathString();
                    File file = new File(workTreePath + File.separator + path);
                    int mHead = treeWalk.getRawMode(T_HEAD);
                    int mIndex = treeWalk.getRawMode(T_INDEX);
                    int mWorking = treeWalk.getRawMode(T_WORKSPACE);
                    GitStatus.Status statusHeadIndex;
                    GitStatus.Status statusIndexWC;
                    GitStatus.Status statusHeadWC;
                    boolean tracked = mWorking != FileMode.TREE.getBits() && (mHead != FileMode.MISSING.getBits() || mIndex != FileMode.MISSING.getBits()); // is new and is not a folder
                    if (mHead == FileMode.MISSING.getBits() && mIndex != FileMode.MISSING.getBits()) {
                        statusHeadIndex = GitStatus.Status.STATUS_ADDED;
                    } else if (mIndex == FileMode.MISSING.getBits() && mHead != FileMode.MISSING.getBits()) {
                        statusHeadIndex = GitStatus.Status.STATUS_REMOVED;
                    } else if (mHead != mIndex || (mIndex != FileMode.TREE.getBits() && !treeWalk.idEqual(T_HEAD, T_INDEX))) {
                        statusHeadIndex = GitStatus.Status.STATUS_MODIFIED;
                    } else {
                        statusHeadIndex = GitStatus.Status.STATUS_NORMAL;
                    }
                    FileTreeIterator fti = treeWalk.getTree(T_WORKSPACE, FileTreeIterator.class);
                    DirCacheIterator indexIterator = treeWalk.getTree(T_INDEX, DirCacheIterator.class);
                    DirCacheEntry indexEntry = indexIterator != null ? indexIterator.getDirCacheEntry() : null;
                    boolean isFolder = false;
                    if (treeWalk.isSubtree()) {
                        if (mWorking == FileMode.TREE.getBits() && fti.isEntryIgnored() && !Utils.isUnderOrEqual(pathFilters, treeWalk)) { // root is under fti
                            statusIndexWC = statusHeadWC = GitStatus.Status.STATUS_IGNORED;
                            isFolder = true;
                        } else {
                            treeWalk.enterSubtree();
                            continue;
                        }
                    } else {
                        if (mWorking == FileMode.MISSING.getBits() && mIndex != FileMode.MISSING.getBits()) {
                            statusIndexWC = GitStatus.Status.STATUS_REMOVED;
                        } else if (mIndex == FileMode.MISSING.getBits() && mWorking != FileMode.MISSING.getBits()) {
                            if (fti.isEntryIgnored()) {
                                statusIndexWC = GitStatus.Status.STATUS_IGNORED;
                            } else {
                                statusIndexWC = GitStatus.Status.STATUS_ADDED;
                            }
                        } else if (mIndex != mWorking || (mWorking != 0 && mWorking != FileMode.TREE.getBits() && fti.isModified(indexEntry, true, Utils.checkExecutable(repository), repository.getFS()))) {
                            statusIndexWC = GitStatus.Status.STATUS_MODIFIED;
                        } else {
                            statusIndexWC = GitStatus.Status.STATUS_NORMAL;
                        }
                        if ((statusHeadIndex == GitStatus.Status.STATUS_MODIFIED || statusIndexWC == GitStatus.Status.STATUS_MODIFIED)
                                && (mHead != mWorking || !treeWalk.getObjectId(T_HEAD).equals(fti.getEntryObjectId()))) {
                            statusHeadWC = GitStatus.Status.STATUS_MODIFIED;
                        } else {
                            statusHeadWC = GitStatus.Status.STATUS_NORMAL;
                        }
                    }

                    boolean inConflict = false;
                    if (indexEntry != null) {
                        inConflict = indexEntry.getStage() > 0;
                    }
                    GitStatus status = new JGitStatus(tracked, path, workTreePath, file, statusHeadIndex, statusIndexWC, statusHeadWC, inConflict, isFolder, renames.get(path));
                    statuses.put(file, status);
                    listener.notifyStatus(status);
                }
            } finally {
                cache.unlock();
            }
        } catch (CorruptObjectException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    public Map<File, GitStatus> getStatuses() {
        return statuses;
    }

    private Map<String, DiffEntry> detectRenames (Repository repository, DirCache cache, Collection<PathFilter> filters) {
        List<DiffEntry> entries;
        TreeWalk treeWalk = new TreeWalk(repository);
        try {
            treeWalk.setRecursive(true);
            treeWalk.reset();
            ObjectId headId = repository.resolve(Constants.HEAD);
            if (headId != null) {
                treeWalk.addTree(new RevWalk(repository).parseTree(headId));
            } else {
                treeWalk.addTree(new EmptyTreeIterator());
            }
            // Index
            treeWalk.addTree(new DirCacheIterator(cache));
            entries = DiffEntry.scan(treeWalk);
            // remove adds from outside of the scanned subtree
            if (!filters.isEmpty()) {
                for (ListIterator<DiffEntry> it = entries.listIterator(); it.hasNext();) {
                    DiffEntry e = it.next();
                    if (e.getChangeType().equals(DiffEntry.ChangeType.ADD)) {
                        boolean contained = false;
                        for (PathFilter f : filters) {
                            if (e.getNewPath().startsWith(f.getPath())) {
                                contained = true;
                                break;
                            }
                        }
                        if (!contained) {
                            it.remove();
                        }
                    }
                }
            }
            RenameDetector d = new RenameDetector(repository);
            d.addAll(entries);
            entries = d.compute();
        } catch (IOException ex) {
            entries = Collections.<DiffEntry>emptyList();
        } finally {
            treeWalk.release();
        }
        Map<String, DiffEntry> renames = new HashMap<String, DiffEntry>();
        for (DiffEntry e : entries) {
            if (e.getChangeType().equals(DiffEntry.ChangeType.COPY) || e.getChangeType().equals(DiffEntry.ChangeType.RENAME)) {
                renames.put(e.getNewPath(), e);
            }
        }
        return renames;
    }

}
