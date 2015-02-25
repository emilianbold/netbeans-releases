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

import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.progress.FileListener;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CheckoutRevisionCommand extends GitCommand {

    private final FileListener listener;
    private final ProgressMonitor monitor;
    private final String revision;
    private final boolean failOnConflict;

    public CheckoutRevisionCommand (JGitRepository repository, GitClassFactory gitFactory, String revision, boolean failOnConflict, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.revision = revision;
        this.listener = listener;
        this.monitor = monitor;
        this.failOnConflict = failOnConflict;
    }

    @Override
    protected void run () throws GitException {
        throw new GitException.UnsupportedCommandException();
//        Repository repository = getRepository().getRepository();
//        try {
//            Ref headRef = repository.getRef(Constants.HEAD);
//            if (headRef == null) {
//                throw new GitException("Corrupted repository, missing HEAD file in .git folder.");
//            }
//            ObjectId headTree = null;
//            try {
//                headTree = Utils.findCommit(repository, Constants.HEAD).getTree();
//            } catch (GitException.MissingObjectException ex) { }
//            Ref ref = repository.getRef(revision);
//            if (ref != null && !ref.getName().startsWith(Constants.R_HEADS) && !ref.getName().startsWith(Constants.R_REMOTES)) {
//                ref = null;
//            }
//            String fromName = headRef.getTarget().getName();
//            if (fromName.startsWith(Constants.R_HEADS)) {
//                fromName = fromName.substring(Constants.R_HEADS.length());
//            }
//            String refLogMessage = "checkout: moving from " + fromName; //NOI18N
//
//            cache = repository.lockDirCache();
//            DirCacheCheckout dco = null;
//            RevCommit commit;
//            try {
//                commit = Utils.findCommit(repository, revision);
//                dco = headTree == null ? new DirCacheCheckout(repository, cache, commit.getTree()) : new DirCacheCheckout(repository, headTree, cache, commit.getTree());
//                // JGit WA
//                if (!failOnConflict) {
//                    dco.preScanTwoTrees();
//                    cacheContents(dco.getConflicts());
//                    dco = headTree == null ? new DirCacheCheckout(repository, cache, commit.getTree()) : new DirCacheCheckout(repository, headTree, cache, commit.getTree());
//                }
//                // End of JGit WA
//                dco.setFailOnConflict(failOnConflict);
//                dco.checkout();
//                cache.lock();
//                
//                VCSFileProxy workDir = getRepository().getLocation();
//                for (String path : dco.getUpdated().keySet()) {
//                    // work-around for submodule roots
//                    DirCacheEntry e = cache.getEntry(path);
//                    if (FileMode.GITLINK.equals(e.getRawMode())) {
//                        VCSFileProxySupport.mkdirs(VCSFileProxy.createFileProxy(workDir, path));
//                    }
//                }
//                
//                notify(workDir, dco.getRemoved());
//                notify(workDir, dco.getConflicts());
//                notify(workDir, dco.getUpdated().keySet());
//                // JGit WA
//                if (!failOnConflict && dco.getConflicts().size() > 0) {
//                    mergeConflicts(dco.getConflicts(), cache);
//                }
//                // End of JGit WA
//            } catch (CheckoutConflictException ex) {
//                List<String> conflicts = dco.getConflicts();
//                throw new GitException.CheckoutConflictException(conflicts.toArray(new String[conflicts.size()]), ex);
//            } finally {
//                cache.unlock();
//            }
//            
//            if (!monitor.isCanceled()) {
//                String toName;
//                boolean detach = true;
//                if (ref == null) {
//                    toName = commit.getName();
//                } else {
//                    toName = ref.getName();
//                    if (toName.startsWith(Constants.R_HEADS)) {
//                        detach = false;
//                        toName = toName.substring(Constants.R_HEADS.length());
//                    } else if (toName.startsWith(Constants.R_REMOTES)) {
//                        toName = toName.substring(Constants.R_REMOTES.length());
//                    }
//                }
//                RefUpdate refUpdate = repository.updateRef(Constants.HEAD, detach);
//                refUpdate.setForceUpdate(false);
//                
//                refUpdate.setRefLogMessage(refLogMessage + " to " + toName, false); //NOI18N
//                RefUpdate.Result updateResult;
//                if (!detach)
//                    updateResult = refUpdate.link(ref.getName());
//                else {
//                    refUpdate.setNewObjectId(commit);
//                    updateResult = refUpdate.forceUpdate();
//                }
//
//                boolean ok = false;
//                switch (updateResult) {
//                case NEW:
//                        ok = true;
//                        break;
//                case NO_CHANGE:
//                case FAST_FORWARD:
//                case FORCED:
//                        ok = true;
//                        break;
//                default:
//                        break;
//                }
//
//                if (!ok) {
//                    throw new GitException("Unexpected result: " + updateResult.name());
//                }
//            }
//        } catch (IOException ex) {
//            throw new GitException(ex);
//        }
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "checkout"); //NOI18N
        addArgument(0, revision);
    }
}
