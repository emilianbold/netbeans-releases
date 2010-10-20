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
import java.io.OutputStream;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CatCommand extends GitCommand {
    private final String revision;
    private final File file;
    private final OutputStream os;
    private final ProgressMonitor monitor;
    private String relativePath;
    private boolean found;

    public CatCommand (Repository repository, File file, String revision, OutputStream out, ProgressMonitor monitor) {
        super(repository, monitor);
        this.file = file;
        this.revision = revision;
        this.os = out;
        this.monitor = monitor;
    }

    @Override
    protected boolean prepareCommand() throws GitException {
        boolean retval = super.prepareCommand();
        if (retval) {
            relativePath = Utils.getRelativePath(getRepository().getWorkTree(), file);
            if (relativePath.isEmpty()) {
                monitor.preparationsFailed("Cannot cat root: " + file);
                throw new GitException("Cannot cat root: " + file);
            }
        }
        return retval;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        try {
            RevCommit commit = Utils.findCommit(repository, revision);
            TreeWalk walk = new TreeWalk(repository);
            walk.reset();
            walk.addTree(commit.getTree());
            walk.setFilter(PathFilter.create(relativePath));
            found = false;
            while (!found && walk.next() && !monitor.isCanceled()) {
                if (relativePath.equals(walk.getPathString())) {
                    ObjectLoader loader = repository.getObjectDatabase().open(walk.getObjectId(0));
                    loader.copyTo(os);
                    os.close();
                    found = true;
                }
            }
        } catch (MissingObjectException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        return new StringBuilder("git show ").append(revision).append(" ").append(file).toString(); //NOI18N
    }

    public boolean foundInRevision () {
        return found;
    }

}
