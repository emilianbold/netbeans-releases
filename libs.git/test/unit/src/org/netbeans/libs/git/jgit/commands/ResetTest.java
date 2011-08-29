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
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitClient.ResetType;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class ResetTest extends AbstractGitTestCase {

    private File workDir;
    private Repository repository;

    public ResetTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testResetSoft () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File file2 = new File(workDir, "file2");
        write(file2, "blablablabla in file2");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files,ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        write(file1, "change in content");
        add(file1);
        commit(files);

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        client.reset(revision, ResetType.SOFT, ProgressMonitor.NULL_PROGRESS_MONITOR);

        assertEquals(revision, new Git(repository).log().call().iterator().next().getId().getName());
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    public void testResetMixed () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File file2 = new File(workDir, "file2");
        write(file2, "blablablabla in file2");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files,ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        write(file1, "change in content");
        add(file1);
        commit(files);

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        client.reset(revision, ResetType.MIXED, ProgressMonitor.NULL_PROGRESS_MONITOR);

        assertEquals(revision, new Git(repository).log().call().iterator().next().getId().getName());
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    public void testResetHard () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File file2 = new File(workDir, "file2");
        write(file2, "blablablabla in file2");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files,ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        write(file1, "change in content");
        add(file1);
        commit(files);

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        long ts = file2.lastModified();
        Thread.sleep(1000);
        client.reset(revision, ResetType.HARD, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(ts, file2.lastModified());

        assertEquals(revision, new Git(repository).log().call().iterator().next().getId().getName());
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    public void testResetHardTypeConflict () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File file2 = new File(file1, "f");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files,ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        client.remove(files, false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        file1.mkdirs();
        assertTrue(file1.isDirectory());
        write(file2, "ssss");
        write(new File(file1, "untracked"), "ssss");
        add(file2);
        commit(files);

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        RevCommit commitCurrent = logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        client.reset(revision, ResetType.HARD, ProgressMonitor.NULL_PROGRESS_MONITOR);

        assertEquals(revision, new Git(repository).log().call().iterator().next().getId().getName());
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertTrue(file1.isFile());

        String currentRevision = commitCurrent.getId().getName();
        client.reset(currentRevision, ResetType.HARD, ProgressMonitor.NULL_PROGRESS_MONITOR);

        assertEquals(currentRevision, new Git(repository).log().call().iterator().next().getId().getName());
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertTrue(file1.isDirectory());
    }

    public void testResetHardOverwritesModification () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File[] files = new File[] { file1 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files,ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        write(file1, "change in content");
        add(file1);
        commit(files);
        write(file1, "hello, i have local modifications");

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        client.reset(revision, ResetType.HARD, ProgressMonitor.NULL_PROGRESS_MONITOR);

        assertEquals(revision, new Git(repository).log().call().iterator().next().getId().getName());
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertEquals("blablablabla", read(file1));
    }

    public void testResetHardRemoveFile () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File file2 = new File(workDir, "file2");
        write(file2, "blablablabla");
        File[] files = new File[] { file1 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        add(file2);
        files = new File[] { file1, file2 };
        commit(files);

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        assertTrue(file2.exists());
        client.reset(revision, ResetType.HARD, ProgressMonitor.NULL_PROGRESS_MONITOR);

        assertEquals(revision, new Git(repository).log().call().iterator().next().getId().getName());
        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertFalse(file2.exists());
    }

    public void testResetPaths () throws Exception {
        File file1 = new File(workDir, "file1"); // index entry will be modified
        write(file1, "blablablabla");
        File file2 = new File(workDir, "file2"); // index entry will be left alone
        write(file2, "blablablabla in file2");
        File file3 = new File(workDir, "file3"); // index entry will be added
        write(file3, "blablablabla in file3");
        File file4 = new File(workDir, "file4"); // index entry will be removed
        File[] files = new File[] { file1, file2, file3, file4 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);

        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(3, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file3, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        String content = "change in content";
        write(file1, content);
        write(file4, "blablablabla in file4");
        client.add(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        commit(files);
        write(file2, "change in content in file 2");
        client.add(new File[] { file2 }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.remove(new File[] { file3 }, false,ProgressMonitor.NULL_PROGRESS_MONITOR);

        LogCommand cmd = new Git(repository).log();
        Iterator<RevCommit> logs = cmd.call().iterator();
        logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        client.reset(new File[] { file1, file3, file4 }, revision, true, ProgressMonitor.NULL_PROGRESS_MONITOR);

        // file1: modified HEAD-INDEX
        // file2: stays modified HEAD-INDEX
        // file3: removed in WT, normal HEAD-INDEX
        // file4: removed in index, normal in WT

        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(4, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file3, true, Status.STATUS_NORMAL, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, file4, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_NORMAL, false);
        assertEquals(content, read(file1));
    }
    
    public void testResetPaths_NonRecursive () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File file1 = new File(folder, "file1"); // index entry will be modified
        write(file1, "blablablabla");
        File subfolder = new File(folder, "subfolder");
        subfolder.mkdirs();
        File file2 = new File(subfolder, "file2"); // index entry will be left alone
        write(file2, "blablablabla in file2");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        String content = "change in content";
        write(file1, content);
        write(file2, content);
        client.add(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        // children
        client.reset(new File[] { folder }, "HEAD", false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);

        write(file1, content);
        // recursive
        client.reset(new File[] { folder }, "HEAD", true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
    }

    public void testResetPathsChangeType () throws Exception {
        File file = new File(workDir, "f"); // index entry will be modified
        File file2 = new File(file, "file");
        write(file, "blablablabla");
        File[] files = new File[] { file, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        client.remove(files, false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        commit(files);
        file.mkdirs();
        write(file2, "aaaa");
        add(file2);
        commit(files);

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        String revisionCurrent = logs.next().getId().getName();
        logs.next();
        String revisionPrevious = logs.next().getId().getName();

        client.reset(files, revisionPrevious, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file, true, Status.STATUS_ADDED, Status.STATUS_REMOVED, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_NORMAL, false);

        client.reset(files, revisionCurrent, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    // TODO: more tests when branches are implemented
    // TODO: more tests when tags are implemented
    // TODO: more tests for conflicts reset
}
