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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
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
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.NotTreeFilter;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.netbeans.libs.git.GitConflictDescriptor;
import org.netbeans.libs.git.GitConflictDescriptor.Type;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.JGitConflictDescriptor;
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
    private static final String PROP_TRACK_SYMLINKS = "org.netbeans.libs.git.trackSymLinks"; //NOI18N

    public StatusCommand (Repository repository, File[] roots, ProgressMonitor monitor, StatusListener listener) {
        super(repository, monitor);
        this.roots = roots;
        this.monitor = monitor;
        this.listener = listener;
        statuses = new LinkedHashMap<File, GitStatus>();
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git status"); //NOI18N
        for (File root : roots) {
            sb.append(" ").append(root.getAbsolutePath());
        }
        return sb.toString();
    }

    @Override
    protected boolean prepareCommand () throws GitException {
        return getRepository().getDirectory().exists();
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        try {
            DirCache cache = repository.readDirCache();
            try {
                String workTreePath = repository.getWorkTree().getAbsolutePath();
                Collection<PathFilter> pathFilters = Utils.getPathFilters(repository.getWorkTree(), roots);
                Map<String, DiffEntry> renames = pathFilters.isEmpty() ? Collections.<String, DiffEntry>emptyMap() : detectRenames(repository, cache, pathFilters);
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
                String lastPath = null;
                GitStatus[] conflicts = new GitStatus[3];
                List<GitStatus> symLinks = new LinkedList<GitStatus>();
                boolean checkExecutable = Utils.checkExecutable(repository);
                boolean trackSymLinks = Boolean.valueOf(System.getProperty(PROP_TRACK_SYMLINKS, Boolean.FALSE.toString()));
                while (treeWalk.next() && !monitor.isCanceled()) {
                    String path = treeWalk.getPathString();
                    boolean symlink = false;
                    if (path.equals(lastPath)) {
                        symlink = isKnownSymlink(symLinks, path);
                    } else {
                        handleConflict(conflicts, workTreePath);
                        handleSymlink(symLinks, workTreePath);
                    }
                    lastPath = path;
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
                    if (!symlink && treeWalk.isSubtree()) {
                        if (mWorking == FileMode.TREE.getBits() && fti.isEntryIgnored()) {
                            Collection<TreeFilter> subTreeFilters = getSubtreeFilters(pathFilters, path);
                            if (!subTreeFilters.isEmpty()) {
                                // caller requested a status for a file under an ignored folder
                                treeWalk.setFilter(AndTreeFilter.create(treeWalk.getFilter(), OrTreeFilter.create(NotTreeFilter.create(PathFilter.create(path)), 
                                        subTreeFilters.size() > 1 ? OrTreeFilter.create(subTreeFilters) : subTreeFilters.iterator().next())));
                                treeWalk.enterSubtree();
                            }
                            if (includes(pathFilters, treeWalk)) {
                                // ignored folder statu is requested by a caller
                                statusIndexWC = statusHeadWC = GitStatus.Status.STATUS_IGNORED;
                                isFolder = true;
                            } else {
                                continue;
                            }
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
                        } else if (!isExistingSymlink(mIndex, mWorking) && (differ(mIndex, mWorking, checkExecutable) || (mWorking != 0 && mWorking != FileMode.TREE.getBits() && fti.isModified(indexEntry, true)))) {
                            statusIndexWC = GitStatus.Status.STATUS_MODIFIED;
                        } else {
                            statusIndexWC = GitStatus.Status.STATUS_NORMAL;
                        }
                        if (mWorking == FileMode.MISSING.getBits() && mHead != FileMode.MISSING.getBits()) {
                            statusHeadWC = GitStatus.Status.STATUS_REMOVED;
                        } else if (mHead == FileMode.MISSING.getBits() && mWorking != FileMode.MISSING.getBits()) {
                            statusHeadWC = GitStatus.Status.STATUS_ADDED;
                        } else if (!isExistingSymlink(mIndex, mWorking) && (differ(mHead, mWorking, checkExecutable) || (mWorking != 0 && mWorking != FileMode.TREE.getBits() && !treeWalk.getObjectId(T_HEAD).equals(fti.getEntryObjectId())))) {
                            statusHeadWC = GitStatus.Status.STATUS_MODIFIED;
                        } else {
                            statusHeadWC = GitStatus.Status.STATUS_NORMAL;
                        }
                    }

                    int stage = indexEntry == null ? 0 : indexEntry.getStage();

                    GitStatus status = new JGitStatus(tracked, path, workTreePath, file, statusHeadIndex, statusIndexWC, statusHeadWC, null, isFolder, renames.get(path));
                    if (stage == 0) {
                        if (!trackSymLinks && isSymlinkFolder(mHead, mWorking)) {
                            symLinks.add(status);
                        } else {
                            addStatus(file, status);
                        }
                    } else {
                        conflicts[stage - 1] = status;
                    }
                }
                handleConflict(conflicts, workTreePath);
                handleSymlink(symLinks, workTreePath);
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
        assert !filters.isEmpty();
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
            treeWalk.setFilter(PathFilterGroup.create(filters));
            // Index
            treeWalk.addTree(new DirCacheIterator(cache));
            entries = DiffEntry.scan(treeWalk);
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

    protected final void handleConflict (GitStatus[] conflicts, String workTreePath) {
        if (conflicts[0] != null || conflicts[1] != null || conflicts[2] != null) {
            GitStatus status;
            Type type;
            if (conflicts[1] == null && conflicts[2] == null) {
                type = Type.BOTH_DELETED;
                status = conflicts[0];
            } else if (conflicts[1] == null && conflicts[2] != null) {
                type = Type.DELETED_BY_US;
                status = conflicts[2];
            } else if (conflicts[1] != null && conflicts[2] == null) {
                type = Type.DELETED_BY_THEM;
                status = conflicts[1];
            } else if (conflicts[0] == null) {
                type = Type.BOTH_ADDED;
                status = conflicts[1];
            } else {
                type = Type.BOTH_MODIFIED;
                status = conflicts[1];
            }
            // how do we get other types??
            GitConflictDescriptor desc = new JGitConflictDescriptor(type);
            status = new JGitStatus(true, status.getRelativePath(), workTreePath, status.getFile(), GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL,
                    GitStatus.Status.STATUS_NORMAL, desc, status.isFolder(), null);
            addStatus(status.getFile(), status);
        }
        // clear conflicts cache
        Arrays.fill(conflicts, null);
    }

    protected final void addStatus (File file, GitStatus status) {
        statuses.put(file, status);
        listener.notifyStatus(status);
    }

    /**
     * Any filter includes this path but only by denoting any of it's ancestors or the path itself
     * Any filter that applies to a file/folder under the given path will not be taken into account
     * @param filters
     * @param treeWalk
     * @return 
     */
    public static boolean includes (Collection<PathFilter> filters, TreeWalk treeWalk) {
        boolean retval = filters.isEmpty();
        for (PathFilter filter : filters) {
            if (filter.include(treeWalk) && treeWalk.getPathString().length() >= filter.getPath().length()) {
                retval = true;
                break;
            }
        }
        return retval;
    }

    private static Collection<TreeFilter> getSubtreeFilters(Collection<PathFilter> filters, String path) {
        List<TreeFilter> subtreeFilters = new LinkedList<TreeFilter>();
        for (PathFilter filter : filters) {
            if (filter.getPath().startsWith(path + "/")) { //NOI18N
                subtreeFilters.add(filter);
            }
        }
        return subtreeFilters;
    }

    private boolean differ (int fileMode1, int fileModeWorking, boolean checkFileMode) {
        boolean differ;
        if (isExistingSymlink(fileMode1, fileModeWorking)) {
            differ = false;
        } else {
            int difference = fileMode1 ^ fileModeWorking;
            if (checkFileMode) {
                differ = difference != 0;
            } else {
                differ = (difference & ~0111) != 0;
            }
        }
        return differ;
    }

    private boolean isExistingSymlink (int fileMode1, int fileModeWorking) {
        return (fileModeWorking & FileMode.TYPE_FILE) == FileMode.TYPE_FILE && (fileMode1 & FileMode.TYPE_SYMLINK) == FileMode.TYPE_SYMLINK;
    }

    private boolean isKnownSymlink (List<GitStatus> symLinks, String path) {
        return !symLinks.isEmpty() && path.equals(symLinks.get(0).getRelativePath());
    }

    private boolean isSymlinkFolder (int mHead, int mWorking) {
        // it seems symlink to a folder has always mWorking set to 0
        return mWorking == 0 && (mHead & FileMode.TYPE_SYMLINK) == FileMode.TYPE_SYMLINK;
    }

    private void handleSymlink (List<GitStatus> symLinks, String workTreePath) {
        if (!symLinks.isEmpty()) {
            boolean removed = symLinks.size() == 1;
            GitStatus status = symLinks.get(0);
            status = new JGitStatus(true, status.getRelativePath(), workTreePath, status.getFile(), status.getStatusHeadIndex(),
                    !removed || status.getStatusHeadIndex() == GitStatus.Status.STATUS_REMOVED ? GitStatus.Status.STATUS_NORMAL : GitStatus.Status.STATUS_REMOVED,
                    removed ? GitStatus.Status.STATUS_REMOVED : GitStatus.Status.STATUS_NORMAL,
                    null, status.isFolder(), null);
            addStatus(status.getFile(), status);
            symLinks.clear();
        }
    }
}
