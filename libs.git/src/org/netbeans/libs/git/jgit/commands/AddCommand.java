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
import java.io.InputStream;
import java.util.Collection;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuildIterator;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileProgressMonitor;

/**
 *
 * @author ondra
 */
public class AddCommand extends GitCommand {
    private final File[] roots;
    private final FileProgressMonitor monitor;

    public AddCommand (Repository repository, File[] roots, FileProgressMonitor monitor) {
        super(repository, monitor);
        this.roots = roots;
        this.monitor = monitor;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        try {
            DirCache cache = repository.lockDirCache();
            try {
                ObjectInserter inserter = repository.newObjectInserter();
                DirCacheBuilder builder = cache.builder();
                TreeWalk treeeWalk = new TreeWalk(repository);
                Collection<String> relativePaths = Utils.getRelativePaths(repository.getWorkTree(), roots);
                if (!relativePaths.isEmpty()) {
                    treeeWalk.setFilter(PathFilterGroup.createFromStrings(relativePaths));
                }
                treeeWalk.setRecursive(true);
                treeeWalk.reset();
                treeeWalk.addTree(new DirCacheBuildIterator(builder));
                treeeWalk.addTree(new FileTreeIterator(repository));
                String lastAddedFile = null;
                while (treeeWalk.next()) {
                    String path = treeeWalk.getPathString();
                    WorkingTreeIterator f = treeeWalk.getTree(1, WorkingTreeIterator.class);
                    if (treeeWalk.getTree(0, DirCacheIterator.class) == null && f != null && f.isEntryIgnored()) {
                        // file is not in index but is ignored, do nothing
                    } else if (!(path.equals(lastAddedFile))) {
                        if (f != null) { // the file exists
                            File file = new File(repository.getWorkTree().getAbsolutePath() + File.separator + path);
                            long sz = f.getEntryLength();
                            DirCacheEntry entry = new DirCacheEntry(path);
                            entry.setLength(sz);
                            entry.setLastModified(f.getEntryLastModified());
                            entry.setFileMode(f.getEntryFileMode());
                            InputStream in = f.openEntryStream();
                            try {
                                entry.setObjectId(inserter.insert(Constants.OBJ_BLOB, sz, in));
                            } finally {
                                in.close();
                            }
                            DirCacheIterator it = treeeWalk.getTree(0, DirCacheIterator.class);
                            if (it == null || !it.getDirCacheEntry().getObjectId().equals(entry.getObjectId())) {
                                monitor.notifyFile(file);
                            }
                            builder.add(entry);
                            lastAddedFile = path;
                        } else {
                            DirCacheIterator c = treeeWalk.getTree(0, DirCacheIterator.class);
                            builder.add(c.getDirCacheEntry());
                        }
                    }
                }
                inserter.flush();
                builder.commit();
            } finally {
                cache.unlock();
            }
        } catch (CorruptObjectException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

}
