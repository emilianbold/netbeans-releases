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
import java.util.Collection;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.netbeans.libs.git.remote.GitClient;
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
 * @author Tomas Stupka
 */
public class CleanCommand extends GitCommand {
    private final VCSFileProxy[] roots;
    private final ProgressMonitor monitor;
    private final FileListener listener;

    public CleanCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy[] roots, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.monitor = monitor;
        this.listener = listener;
    }

    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument("clean"); //NOI18N
        addArgument("-d"); //NOI18N
        addFiles(roots);
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository().getRepository();        
        try {
            DirCache cache = null;
            try {
                cache = repository.lockDirCache();
                TreeWalk treeWalk = new TreeWalk(repository);
                Collection<PathFilter> pathFilters = Utils.getPathFilters(getRepository().getLocation(), roots);
                if (!pathFilters.isEmpty()) {
                    treeWalk.setFilter(PathFilterGroup.create(pathFilters));
                }
                treeWalk.setRecursive(false);
                treeWalk.setPostOrderTraversal(true);
                treeWalk.reset();
                                
                treeWalk.addTree(new FileTreeIterator(repository));
                while (treeWalk.next() && !monitor.isCanceled()) {
                    String path = treeWalk.getPathString();                    
                    WorkingTreeIterator f = treeWalk.getTree(0, WorkingTreeIterator.class);
                    if(f != null) { // file exists
                        if (!treeWalk.isPostChildren()) {
                            if (treeWalk.isSubtree()) {
                                treeWalk.enterSubtree();
                                continue;
                            } else {
                                deleteIfUnversioned(cache, path, f, getRepository(), treeWalk);
                            }
                        } else {
                            deleteIfUnversioned(cache, path, f, getRepository(), treeWalk);
                        }                        
                    }                    
                }
            } finally {
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

    private void deleteIfUnversioned(DirCache cache, String path, WorkingTreeIterator f, JGitRepository repository, TreeWalk treeWalk) throws IOException, NoWorkTreeException {
        if (cache.getEntry(path) == null &&  // not in index 
            !f.isEntryIgnored() &&             // not ignored
            !Utils.isFromNested(f.getEntryFileMode().getBits()))
        {            
            VCSFileProxy file = VCSFileProxy.createFileProxy(repository.getLocation(), path);                        
            if(file.isDirectory()) {
                VCSFileProxy[] s = file.listFiles();
                if(s != null && s.length > 0) { // XXX is there no better way to find out if empty?
                    // not empty
                    return; 
                }
            }
            VCSFileProxySupport.delete(file);
            listener.notifyFile(file, treeWalk.getPathString());
        }
    }    
}
