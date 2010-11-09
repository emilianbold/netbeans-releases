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
import java.util.Iterator;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class BranchTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public BranchTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testListBranches () throws Exception {
        GitClient client = getClient(workDir);
        Map<String, GitBranch> branches = client.getBranches(false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        
        File f = new File(workDir, "file");
        write(f, "hello");
        File[] files = new File[] { f };
        client.add(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.commit(files, "init", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        write(f, "hello again");
        client.commit(files, "change", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);

        Iterator<RevCommit> it = new Git(repository).log().call().iterator();
        RevCommit info = it.next();
        String commitId = info.getId().getName();

        branches = client.getBranches(true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, branches.size());
        assertEquals("master", branches.get("master").getName());
        assertEquals(commitId, branches.get("master").getId());
        assertFalse(branches.get("master").isRemote());
        assertTrue(branches.get("master").isActive());

        write(new File(workDir, ".git/refs/heads/nova"), commitId);
        branches = client.getBranches(true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertEquals("master", branches.get("master").getName());
        assertFalse(branches.get("master").isRemote());
        assertTrue(branches.get("master").isActive());
        assertEquals(commitId, branches.get("master").getId());
        assertEquals("nova", branches.get("nova").getName());
        assertFalse(branches.get("nova").isRemote());
        assertFalse(branches.get("nova").isActive());
        assertEquals(commitId, branches.get("nova").getId());

        Thread.sleep(1100);
        write(new File(workDir, ".git/HEAD"), commitId);
        branches = client.getBranches(true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(3, branches.size());
        assertEquals(GitBranch.NO_BRANCH, branches.get(GitBranch.NO_BRANCH).getName());
        assertFalse(branches.get(GitBranch.NO_BRANCH).isRemote());
        assertTrue(branches.get(GitBranch.NO_BRANCH).isActive());
        assertEquals(commitId, branches.get(GitBranch.NO_BRANCH).getId());
        assertEquals("master", branches.get("master").getName());
        assertFalse(branches.get("master").isRemote());
        assertFalse(branches.get("master").isActive());
        assertEquals("nova", branches.get("nova").getName());
        assertFalse(branches.get("nova").isRemote());
        assertFalse(branches.get("nova").isActive());
    }
}
