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
import java.util.Iterator;
import org.eclipse.jgit.diff.DiffConfig;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.FollowFilter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.AndRevFilter;
import org.eclipse.jgit.revwalk.filter.MaxCountRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.GitObjectType;
import org.netbeans.libs.git.remote.GitRevisionInfo;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.jgit.Utils;
import org.netbeans.libs.git.remote.jgit.utils.CancelRevFilter;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * @author ondra
 */
public class GetPreviousCommitCommand extends GitCommand {
    private final String revision;
    private GitRevisionInfo previousRevision;
    private final VCSFileProxy file;
    private final ProgressMonitor monitor;

    public GetPreviousCommitCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy file, String revision, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.file = file;
        this.revision = revision;
        this.monitor = monitor;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository().getRepository();
        RevWalk walk = null;
        try {
            RevCommit rev = Utils.findCommit(repository, revision);
            if (rev.getParentCount() == 1) {
                walk = new RevWalk(repository);
                walk.markStart(walk.parseCommit(rev.getParent(0)));
                String path = Utils.getRelativePath(getRepository().getLocation(), file);
                if (path != null && !path.isEmpty()) {
                    walk.setTreeFilter(FollowFilter.create(path, repository.getConfig().get(DiffConfig.KEY)));
                }
                walk.setRevFilter(AndRevFilter.create(new RevFilter[] { new CancelRevFilter(monitor), MaxCountRevFilter.create(1) }));
                Iterator<RevCommit> it = walk.iterator();
                if (it.hasNext()) {
                    previousRevision = getClassFactory().createRevisionInfo(new RevWalk(repository).parseCommit(it.next()), getRepository());
                }
            }
        } catch (MissingObjectException ex) {
            throw new GitException.MissingObjectException(ex.getObjectId().toString(), GitObjectType.COMMIT);
        } catch (IOException ex) {
            throw new GitException(ex);
        } finally {
            if (walk != null) {
                walk.release();
            }
        }
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "log"); //NOI18N
        addArgument(0, revision);
        addArgument(0, "--"); //NOI18N
        addArgument(0, Utils.getRelativePath(getRepository().getLocation(), file));
    }

    public GitRevisionInfo getRevision () {
        return previousRevision;
    }
}
