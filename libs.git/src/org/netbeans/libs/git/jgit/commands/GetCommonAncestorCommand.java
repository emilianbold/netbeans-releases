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

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoMergeBaseException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.RecursiveMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitObjectType;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class GetCommonAncestorCommand extends GitCommand {
    private final String[] revisions;
    private GitRevisionInfo revision;

    public GetCommonAncestorCommand (Repository repository, GitClassFactory gitFactory, String[] revisions, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.revisions = revisions;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        RevWalk walk = null;
        try {
            if (revisions.length == 0) {
                revision = null;
            } else if (revisions.length == 1) {
                revision = getClassFactory().createRevisionInfo(Utils.findCommit(repository, revisions[0]), repository);
            } else {
                // it would be great if JGit exposed RecursiveMerger.getBaseCommit as public
                walk = new RevWalk(repository);
                for (String rev : revisions) {
                    walk.markStart(walk.lookupCommit(Utils.findCommit(repository, rev)));
                }
                walk.setRevFilter(RevFilter.MERGE_BASE);
                Iterator<RevCommit> it = walk.iterator();
                if (it.hasNext()) {
                    revision = getClassFactory().createRevisionInfo(it.next(), repository);
                }
                if (it.hasNext()) {
                    revision = null;
                }
                if (revision == null) {
                    // try resursive strategy
                    DummyMerger merger = new DummyMerger(repository);
                    RevCommit base = merger.getBaseCommit(Utils.findCommit(repository, revisions[0]), Utils.findCommit(repository, revisions[1]));
                    for (int i = 2; base != null && i < revisions.length; ++i) {
                        base = merger.getBaseCommit(base, Utils.findCommit(repository, revisions[i]));
                    }
                    if (base != null) {
                        revision = getClassFactory().createRevisionInfo(base, repository);
                    }
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
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git merge-base "); //NOI18N
        for (String s : revisions) {
            sb.append(s).append(' ');
        }
        return sb.toString();
    }
    
    public GitRevisionInfo getRevision () {
        return revision;
    }
    
    private class DummyMerger extends RecursiveMerger {
        
        private DummyMerger (Repository repository) {
            super(repository);
        }

        @Override
        protected RevCommit getBaseCommit (RevCommit a, RevCommit b) throws IncorrectObjectTypeException, IOException {
            return super.getBaseCommit(a, b);
        }
        
    }
}
