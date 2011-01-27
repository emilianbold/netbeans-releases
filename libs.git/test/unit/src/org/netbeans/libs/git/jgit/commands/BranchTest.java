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
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class BranchTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;
    private static final String BRANCH_NAME = "new_branch";
    private static final String BRANCH_NAME_2 = "new_branch2";
    private static final String BRANCH_NAME_3 = "new_branch3";

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
    
    public void testCreateBranch () throws Exception {
        File f = new File(workDir, "file");
        write(f, "hello");
        File[] files = new File[] { f };
        add(files);
        commit(files);
        write(f, "hello again");
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        GitRevisionInfo[] logs = client.log(new SearchCriteria(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        String lastCommitId = logs[0].getRevision();
        String commitId = logs[1].getRevision();

        GitBranch branch = client.createBranch(BRANCH_NAME, commitId, ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertTrue(branches.containsKey("master"));
        assertTrue(branches.containsKey(BRANCH_NAME));
        assertEquals(BRANCH_NAME, branch.getName());
        assertEquals(commitId, branch.getId());
        assertFalse(branch.isActive());
        assertFalse(branch.isRemote());
        branch = branches.get(BRANCH_NAME);
        assertEquals(BRANCH_NAME, branch.getName());
        assertEquals(commitId, branch.getId());
        assertFalse(branch.isActive());
        assertFalse(branch.isRemote());
        assertTrue(branches.get("master").isActive());
        assertEquals(commitId, read(new File(workDir, ".git/refs/heads/" + BRANCH_NAME)));

        client.createBranch(BRANCH_NAME_2, Constants.HEAD, ProgressMonitor.NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(3, branches.size());
        assertTrue(branches.containsKey("master"));
        assertTrue(branches.containsKey(BRANCH_NAME));
        assertTrue(branches.containsKey(BRANCH_NAME_2));
        assertTrue(branches.get("master").isActive());
        assertEquals(lastCommitId, read(new File(workDir, ".git/refs/heads/" + BRANCH_NAME_2)));
        client.createBranch(BRANCH_NAME_3, "refs/heads/master", ProgressMonitor.NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(4, branches.size());
        assertTrue(branches.containsKey("master"));
        assertTrue(branches.containsKey(BRANCH_NAME));
        assertTrue(branches.containsKey(BRANCH_NAME_2));
        assertTrue(branches.containsKey(BRANCH_NAME_3));
        assertTrue(branches.get("master").isActive());
        assertEquals(lastCommitId, read(new File(workDir, ".git/refs/heads/" + BRANCH_NAME_3)));

        try {
            client.createBranch(BRANCH_NAME, commitId, ProgressMonitor.NULL_PROGRESS_MONITOR);
            fail("Branch should not have been created, it already existed");
        } catch (GitException ex) {
            // OK
            assertEquals("Ref " + BRANCH_NAME + " already exists", ex.getCause().getMessage());
        }
        branches = client.getBranches(true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(4, branches.size());
        assertTrue(branches.get("master").isActive());
        assertEquals(commitId, read(new File(workDir, ".git/refs/heads/" + BRANCH_NAME)));
    }
    
    public void testFileProtocolFails () throws Exception {
        try {
            Transport.open(repository, new URIish(workDir.toURI().toURL()));
            fail("Workaround not needed, fix ListRemoteBranchesCommand - Transport.open(String) to Transport.open(URL)");
        } catch (NotSupportedException ex) {
            
        }
    }
    
    public void testListRemoteBranches () throws Exception {
        File otherWT = new File(workDir.getParentFile(), "repo2");
        GitClient client = getClient(otherWT);
        client.init(ProgressMonitor.NULL_PROGRESS_MONITOR);
        File f = new File(otherWT, "f");
        write(f, "init");
        client.add(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        GitBranch branch = client.createBranch(BRANCH_NAME, "master", ProgressMonitor.NULL_PROGRESS_MONITOR);
        write(f, "change on master");
        client.add(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        GitRevisionInfo master = client.commit(new File[] { f }, "change on master", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(otherWT.toURI().toURL(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, remoteBranches.size());
        assertEquals(branch.getId(), remoteBranches.get(BRANCH_NAME).getId());
        assertEquals(master.getRevision(), remoteBranches.get("master").getId());
    }
}
