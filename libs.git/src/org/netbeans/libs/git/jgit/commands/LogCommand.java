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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitObjectType;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.JGitRevisionInfo;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.progress.RevisionInfoListener;

/**
 *
 * @author ondra
 */
public class LogCommand extends GitCommand {
    private final ProgressMonitor monitor;
    private final RevisionInfoListener listener;
    private final List<GitRevisionInfo> revisions;
    private String revision;
    private String revisionFrom;
    private String revisionTo;
    private int limit;

    public LogCommand (Repository repository, ProgressMonitor monitor, RevisionInfoListener listener) {
        super(repository, monitor);
        this.monitor = monitor;
        this.listener = listener;
        this.limit = -1;
        this.revisions = new LinkedList<GitRevisionInfo>();
    }

    public void setRevision (String revision) {
        this.revision = revision;
    }

    public void setRevisionFrom (String revisionFrom) {
        this.revisionFrom = revisionFrom;
    }

    public void setRevisionTo (String revisionTo) {
        this.revisionTo = revisionTo;
    }

    public void setLimit (int limit) {
        this.limit = limit;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        if (revision != null) {
            RevCommit commit = Utils.findCommit(repository, revision);
            addRevision(new JGitRevisionInfo(commit, repository));
        } else {
            org.eclipse.jgit.api.LogCommand cmd = new Git(repository).log();
            try {
                if (revisionTo != null && revisionFrom != null) {
                    cmd.addRange(Utils.findCommit(repository, revisionFrom), Utils.findCommit(repository, revisionTo));
                } else if (revisionTo != null) {
                    cmd.add(Utils.findCommit(repository, revisionTo));
                } else if (revisionFrom != null) {
                    cmd.not(Utils.findCommit(repository, revisionFrom));
                } else {
                    BranchCommand branchCommand = new BranchCommand(repository, false, ProgressMonitor.NULL_PROGRESS_MONITOR);
                    branchCommand.execute();
                    for (Map.Entry<String, GitBranch> e : branchCommand.getBranches().entrySet()) {
                        cmd.add(Utils.findCommit(repository, e.getValue().getId()));
                    }
                }
                int remaining = limit;
                for (Iterator<RevCommit> it = cmd.call().iterator(); it.hasNext() && !monitor.isCanceled() && remaining != 0; --remaining) {
                    RevCommit commit = it.next();
                    addRevision(new JGitRevisionInfo(commit, repository));
                }
            } catch (MissingObjectException ex) {
                throw new GitException.MissingObjectException(ex.getObjectId().toString(), GitObjectType.COMMIT);
            } catch (Exception ex) {
                throw new GitException(ex);
            }
        } 
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git log --name-status "); //NOI18N
        if (revision != null) {
            sb.append("--no-walk ").append(revision);
        } else if (revisionTo != null && revisionFrom != null) {
            sb.append(revisionFrom).append("..").append(revisionTo); //NOI18N
        } else if (revisionTo != null) {
            sb.append(revisionTo);
        } else if (revisionFrom != null) {
            sb.append(revisionFrom).append(".."); //NOI18N
        }
        return sb.toString();
    }

    public GitRevisionInfo[] getRevisions () {
        return revisions.toArray(new GitRevisionInfo[revisions.size()]);
    }

    private void addRevision (JGitRevisionInfo info) {
        revisions.add(info);
        listener.notifyRevisionInfo(info);
    }

}
