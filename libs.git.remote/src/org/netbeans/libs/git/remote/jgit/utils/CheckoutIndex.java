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

package org.netbeans.libs.git.remote.jgit.utils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Logger;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.netbeans.libs.git.remote.GitException;
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
public class CheckoutIndex {

    private final JGitRepository repository;
    private final DirCache cache;
    private final VCSFileProxy[] roots;
    private final FileListener listener;
    private final ProgressMonitor monitor;
    private final boolean checkContent;
    private final boolean recursively;
    private static final Logger LOG = Logger.getLogger(CheckoutIndex.class.getName());

    public CheckoutIndex (JGitRepository repository, DirCache cache, VCSFileProxy[] roots, boolean recursively, FileListener listener, ProgressMonitor monitor, boolean checkContent) {
        this.repository = repository;
        this.cache = cache;
        this.roots = roots;
        this.listener = listener;
        this.monitor = monitor;
        this.checkContent = checkContent;
        this.recursively = recursively;
    }

    public void checkout () throws IOException, GitException {
        TreeWalk treeWalk = new TreeWalk(repository.getRepository());
        Collection<String> relativePaths = Utils.getRelativePaths(repository.getLocation(), roots);
        if (!relativePaths.isEmpty()) {
            treeWalk.setFilter(PathFilterGroup.createFromStrings(relativePaths));
        }
        treeWalk.setRecursive(true);
        treeWalk.reset();
        treeWalk.addTree(new DirCacheIterator(cache));
        treeWalk.addTree(new FileTreeIterator(repository.getRepository()));
        String lastAddedPath = null;
        ObjectReader od = repository.getRepository().newObjectReader();
        try {
        while (treeWalk.next() && !monitor.isCanceled()) {
            VCSFileProxy path = VCSFileProxy.createFileProxy(repository.getLocation(), treeWalk.getPathString());
            if (treeWalk.getPathString().equals(lastAddedPath)) {
                // skip conflicts
                continue;
            } else {
                lastAddedPath = treeWalk.getPathString();
            }
            DirCacheIterator dit = treeWalk.getTree(0, DirCacheIterator.class);
            FileTreeIterator fit = treeWalk.getTree(1, FileTreeIterator.class);
            if (dit != null && (recursively || directChild(roots, repository.getLocation(), path)) && (fit == null || fit.isModified(dit.getDirCacheEntry(), checkContent, od))) {
                // update entry
                listener.notifyFile(path, treeWalk.getPathString());
                checkoutEntry(repository.getRepository(), path, dit.getDirCacheEntry(), od);
            }
        }
        } finally {
            od.release();
            treeWalk.release();
        }
    }

    public void checkoutEntry (Repository repository, VCSFileProxy file, DirCacheEntry e, ObjectReader od) throws IOException, GitException {
        // ... create/overwrite this file ...
        if (!ensureParentFolderExists(file.getParentFile())) {
            return;
        }

        boolean exists = file.exists();
        if (exists && e.getFileMode() == FileMode.SYMLINK) {
            monitor.notifyWarning(MessageFormat.format(Utils.getBundle(CheckoutIndex.class).getString("MSG_Warning_SymLink"), file.getPath())); //NOI18N
            return;
        }

        if (Utils.isFromNested(e.getFileMode().getBits())) {
            if (!exists) {
                VCSFileProxySupport.mkdirs(file);
            }
        } else {
            if (exists && file.isDirectory()) {
                monitor.notifyWarning(MessageFormat.format(Utils.getBundle(CheckoutIndex.class).getString("MSG_Warning_ReplacingDirectory"), file.getPath())); //NOI18N
                Utils.deleteRecursively(file);
            }
            VCSFileProxySupport.createNew(file);
            if (file.isFile()) {
                //TODO: temporary to compile module
                DirCacheCheckout.checkoutEntry(repository, file.toFile(), e, od);
            } else {
                monitor.notifyError(MessageFormat.format(Utils.getBundle(CheckoutIndex.class).getString("MSG_Warning_CannotCreateFile"), file.getPath())); //NOI18N
            }
        }
    }

    private boolean ensureParentFolderExists (VCSFileProxy parentFolder) {
        VCSFileProxy predecessor = parentFolder;
        while (!predecessor.exists()) {
            predecessor = predecessor.getParentFile();
        }
        if (predecessor.isFile()) {
            VCSFileProxySupport.delete(predecessor);
            if (predecessor.exists()) {
                monitor.notifyError(MessageFormat.format(Utils.getBundle(CheckoutIndex.class).getString("MSG_Warning_CannotCreateFile"), predecessor.getPath())); //NOI18N
                return false;
            }
            monitor.notifyWarning(MessageFormat.format(Utils.getBundle(CheckoutIndex.class).getString("MSG_Warning_ReplacingFile"), predecessor.getPath())); //NOI18N
        }
        return VCSFileProxySupport.mkdirs(parentFolder) || parentFolder.exists();
    }

    private boolean directChild (VCSFileProxy[] roots, VCSFileProxy workTree, VCSFileProxy path) {
        if (roots.length == 0) {
            roots = new VCSFileProxy[] { workTree };
        }
        for (VCSFileProxy parent : roots) {
            if (parent.equals(path) || parent.equals(path.getParentFile())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWindows () {
        return System.getProperty("os.name", "").toLowerCase().contains("windows"); //NOI18N
    }
}
