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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuildIterator;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.CoreConfig;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.util.IO;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.jgit.Utils;
import org.netbeans.libs.git.remote.progress.FileListener;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * @author ondra
 */
public class AddCommand extends GitCommand {
    private final VCSFileProxy[] roots;
    private final ProgressMonitor monitor;
    private final FileListener listener;

    public AddCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy[] roots, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.monitor = monitor;
        this.listener = listener;
    }

    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument("add"); //NOI18N
        addFiles(roots);
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository().getRepository();
        try {
            DirCache cache = null;
            ObjectInserter inserter = repository.newObjectInserter();
            ObjectReader or = repository.newObjectReader();
            try {
                cache = repository.lockDirCache();
                DirCacheBuilder builder = cache.builder();
                TreeWalk treeWalk = new TreeWalk(repository);
                Collection<String> relativePaths = Utils.getRelativePaths(getRepository().getLocation(), roots);
                if (!relativePaths.isEmpty()) {
                    treeWalk.setFilter(PathFilterGroup.createFromStrings(relativePaths));
                }
                treeWalk.setRecursive(false);
                treeWalk.reset();
                treeWalk.addTree(new DirCacheBuildIterator(builder));
                treeWalk.addTree(new FileTreeIterator(repository));
                String lastAddedFile = null;
                WorkingTreeOptions opt = repository.getConfig().get(WorkingTreeOptions.KEY);
                boolean autocrlf = opt.getAutoCRLF() != CoreConfig.AutoCRLF.FALSE;
                while (treeWalk.next() && !monitor.isCanceled()) {
                    String path = treeWalk.getPathString();
                    WorkingTreeIterator f = treeWalk.getTree(1, WorkingTreeIterator.class);
                    DirCacheIterator dcit = treeWalk.getTree(0, DirCacheIterator.class);
                    if (f != null && (dcit == null && f.isEntryIgnored())) {
                        // file is not in index but is ignored, do nothing
                    } else if (!(path.equals(lastAddedFile))) {
                        if (f != null) { // the file exists
                            VCSFileProxy file = VCSFileProxy.createFileProxy(getRepository().getLocation(), path);
                            DirCacheEntry entry = new DirCacheEntry(path);
                            entry.setLastModified(f.getEntryLastModified());
                            int fm = f.getEntryFileMode().getBits();
                            long sz = f.getEntryLength();
                            boolean symlink = VCSFileProxySupport.isSymlink(file);
                            if (Utils.isFromNested(fm)) {
                                entry.setFileMode(f.getIndexFileMode(dcit));
                                entry.setLength(sz);
                                entry.setObjectId(f.getEntryObjectId());
                            } else if (symlink) {
                                String link = VCSFileProxySupport.readSymbolicLinkPath(file);
                                entry.setFileMode(FileMode.SYMLINK);
                                entry.setLength(0);
                                //TODO: get time stamp from link
                                entry.setLastModified(file.lastModified());
                                entry.setObjectId(inserter.insert(Constants.OBJ_BLOB, Constants.encode(link)));
                            } else if ((f.getEntryFileMode().getBits() & FileMode.TYPE_TREE) == FileMode.TYPE_TREE) {
                                treeWalk.enterSubtree();
                                continue;
                            } else {
                                FileMode indexFileMode = f.getIndexFileMode(dcit);
                                if (dcit == null && indexFileMode == FileMode.EXECUTABLE_FILE && !opt.isFileMode()) {
                                    // new files should not set exec flag if filemode is set to false
                                    indexFileMode = FileMode.REGULAR_FILE;
                                }
                                entry.setFileMode(indexFileMode);
                                InputStream in = f.openEntryStream();
                                try {
                                    if (autocrlf) {
                                        ByteBuffer buf = IO.readWholeStream(in, (int) sz);
                                        entry.setObjectId(inserter.insert(Constants.OBJ_BLOB, buf.array(), buf.position(), buf.limit() - buf.position()));
                                    } else {
                                        entry.setObjectId(inserter.insert(Constants.OBJ_BLOB, sz, in));
                                    }
                                    entry.setLength(sz);
                                } finally {
                                    in.close();
                                }
                            }
                            ObjectId oldId = treeWalk.getObjectId(0);
                            if (ObjectId.equals(oldId, ObjectId.zeroId()) || !ObjectId.equals(oldId, entry.getObjectId())) {
                                listener.notifyFile(file, path);
                            }
                            builder.add(entry);
                            lastAddedFile = path;
                        } else if (treeWalk.isSubtree()) {
                            // this is a folder but does not exist on disk any more
                            // still needs to go through all the index entries and copy
                            treeWalk.enterSubtree();
                        } else {
                            DirCacheIterator c = treeWalk.getTree(0, DirCacheIterator.class);
                            builder.add(c.getDirCacheEntry());
                        }
                    }
                }
                if (!monitor.isCanceled()) {
                    inserter.flush();
                    builder.commit();
                }
            } finally {
                inserter.release();
                or.release();
                if (cache != null ) {
                    cache.unlock();
                }
            }
        } catch (CorruptObjectException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

}
