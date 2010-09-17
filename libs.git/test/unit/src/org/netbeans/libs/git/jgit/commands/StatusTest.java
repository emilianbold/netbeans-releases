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
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.StatusProgressMonitor;

/**
 *
 * @author ondra
 */
public class StatusTest extends AbstractGitTestCase {

    private File workDir;
    private Repository repository;

    public StatusTest(String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testMiscStatus () throws Exception {
        write(new File(workDir, ".gitignore"), "ignored");
        File untracked = new File(workDir, "untracked");
        write(untracked, "untracked");
        File ignored = new File(workDir, "ignored");
        write(ignored, "ignored");
        File added_uptodate = new File(workDir, "added-uptodate");
        write(added_uptodate, "added-uptodate");
        File added_modified = new File(workDir, "added-modified");
        write(added_modified, "added_modified");
        File added_deleted = new File(workDir, "added-deleted");
        write(added_deleted, "added_deleted");

        File uptodate_uptodate = new File(workDir, "uptodate-uptodate");
        write(uptodate_uptodate, "uptodate_uptodate");
        File uptodate_modified = new File(workDir, "uptodate-modified");
        write(uptodate_modified, "uptodate_modified");
        File uptodate_deleted = new File(workDir, "uptodate-deleted");
        write(uptodate_deleted, "uptodate_deleted");

        File modified_uptodate = new File(workDir, "modified-uptodate");
        write(modified_uptodate, "modified_uptodate");
        File modified_modified = new File(workDir, "modified-modified");
        write(modified_modified, "modified_modified");
        File modified_reset = new File(workDir, "modified-reset");
        write(modified_reset, "modified_reset");
        File modified_deleted = new File(workDir, "modified-deleted");
        write(modified_deleted, "modified_deleted");

        // we cannot
        File deleted_uptodate = new File(workDir, "deleted-uptodate");
        write(deleted_uptodate, "deleted_uptodate");
        File deleted_untracked = new File(workDir, "deleted-untracked");
        write(deleted_untracked, "deleted_untracked");

        repository.getIndex().add(workDir, uptodate_uptodate);
        repository.getIndex().add(workDir, uptodate_modified);
        repository.getIndex().add(workDir, uptodate_deleted);
        repository.getIndex().add(workDir, modified_uptodate);
        repository.getIndex().add(workDir, modified_modified);
        repository.getIndex().add(workDir, modified_reset);
        repository.getIndex().add(workDir, modified_deleted);
        repository.getIndex().add(workDir, deleted_uptodate);
        repository.getIndex().add(workDir, deleted_untracked);
        repository.getIndex().write();
        // TODO commit through facade needed
        CommitCommand cmd = new Git(repository).commit();
        cmd.setMessage("initial commit");
        cmd.call();
//        getLocalGitRepository().commit(new File[] {uptodate_uptodate, uptodate_modified, uptodate_deleted
//                , modified_uptodate, modified_modified, modified_deleted, deleted_uptodate, deleted_untracked}, "initial commit");
        repository.getIndex().add(workDir, added_uptodate);
        repository.getIndex().add(workDir, added_modified);
        repository.getIndex().add(workDir, added_deleted);
        repository.getIndex().write();
        write(modified_deleted, "modification modified_deleted");
        write(modified_modified, "modification modified_modified");
        write(modified_reset, "modification modified_reset");
        write(modified_uptodate, "modification modified_uptodate");
        repository.getIndex().add(workDir, modified_deleted);
        repository.getIndex().add(workDir, modified_modified);
        repository.getIndex().add(workDir, modified_reset);
        repository.getIndex().add(workDir, modified_uptodate);
        repository.getIndex().write();
        deleted_uptodate.delete();
        deleted_untracked.delete();
        repository.getIndex().remove(workDir, deleted_uptodate);
        repository.getIndex().remove(workDir, deleted_untracked);
        repository.getIndex().write();
        write(added_modified, "modification2 added_modified");
        write(uptodate_modified, "modification2 uptodate_modified");
        write(modified_modified, "modification2 modified_modified");
        write(modified_reset, "modified_reset");
        added_deleted.delete();
        modified_deleted.delete();
        uptodate_deleted.delete();
        deleted_untracked.createNewFile();

        TestStatusProgressMonitor monitor = new TestStatusProgressMonitor();
        Map<File, GitStatus> statuses = getClient(workDir).getStatus(new File[] { workDir }, monitor);
        assertFalse(statuses.isEmpty());
        assertStatus(statuses, workDir, untracked, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, added_uptodate, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, added_modified, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, monitor);
        assertStatus(statuses, workDir, added_deleted, true, Status.STATUS_ADDED, Status.STATUS_REMOVED, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, uptodate_uptodate, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, uptodate_modified, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, monitor);
        assertStatus(statuses, workDir, uptodate_deleted, true, Status.STATUS_NORMAL, Status.STATUS_REMOVED, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, modified_uptodate, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false, monitor);
        assertStatus(statuses, workDir, modified_modified, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, monitor);
        assertStatus(statuses, workDir, modified_reset, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, modified_deleted, true, Status.STATUS_MODIFIED, Status.STATUS_REMOVED, Status.STATUS_MODIFIED, false, monitor);
        assertStatus(statuses, workDir, deleted_uptodate, true, Status.STATUS_REMOVED, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, deleted_untracked, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_NORMAL, false, monitor);
        // what about isIgnored() here?
        assertStatus(statuses, workDir, ignored, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_NORMAL, false, monitor);
    }

    public void testStatusSingleFile () throws Exception {
        File untracked = new File(workDir, "untracked");
        write(untracked, "untracked");
        File added_modified = new File(workDir, "added-modified");
        write(added_modified, "added_modified");
        File uptodate_modified = new File(workDir, "uptodate-modified");
        write(uptodate_modified, "uptodate_modified");
        File modified_modified = new File(workDir, "modified-modified");
        write(modified_modified, "modified_modified");

        repository.getIndex().add(workDir, uptodate_modified);
        repository.getIndex().add(workDir, modified_modified);
        repository.getIndex().write();
        // TODO commit through facade needed
        CommitCommand cmd = new Git(repository).commit();
        cmd.setMessage("initial commit");
        cmd.call();
        // getLocalGitRepository().commit(new File[] {uptodate_modified, modified_modified}, "initial commit");
        repository.getIndex().add(workDir, added_modified);
        repository.getIndex().write();
        write(modified_modified, "modification modified_modified");
        repository.getIndex().add(workDir, modified_modified);
        repository.getIndex().write();
        write(added_modified, "modification2 added_modified");
        write(uptodate_modified, "modification2 uptodate_modified");
        write(modified_modified, "modification2 modified_modified");

        GitClient client = getClient(workDir);
        TestStatusProgressMonitor monitor = new TestStatusProgressMonitor();
        Map<File, GitStatus> statuses = client.getStatus(new File[] { untracked }, monitor);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, untracked, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_NORMAL, false, monitor);
        monitor = new TestStatusProgressMonitor();
        statuses = client.getStatus(new File[] { added_modified }, monitor);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, added_modified, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, monitor);
        monitor = new TestStatusProgressMonitor();
        statuses = client.getStatus(new File[] { uptodate_modified }, monitor);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, uptodate_modified, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, monitor);
        monitor = new TestStatusProgressMonitor();
        statuses = client.getStatus(new File[] { modified_modified }, monitor);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, modified_modified, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, monitor);
    }

    public void testStatusTree () throws Exception {
        File folder = new File(workDir, "folder1");
        folder.mkdirs();
        write(new File(folder, "untracked1"), "untracked");
        write(new File(folder, "untracked2"), "untracked");
        folder = new File(workDir, "folder2");
        folder.mkdirs();
        File f1 = new File(folder, "f1");
        write(f1, "f1");
        File f2 = new File(folder, "f2");
        write(f2, "f2");
        File folder21 = new File(folder, "folder21");
        folder21.mkdirs();
        File f3 = new File(folder21, "f3");
        write(f3, "f3");
        File f4 = new File(folder21, "f4");
        write(f4, "f4");
        File folder22 = new File(folder, "folder22");
        folder22.mkdirs();
        File f5 = new File(folder22, "f5");
        write(f5, "f5");
        File f6 = new File(folder22, "f6");
        write(f6, "f6");

        // TODO commit through facade needed
        repository.getIndex().add(workDir, f1);
        repository.getIndex().add(workDir, f2);
        repository.getIndex().add(workDir, f3);
        repository.getIndex().add(workDir, f4);
        repository.getIndex().add(workDir, f5);
        repository.getIndex().add(workDir, f6);
        repository.getIndex().write();
        CommitCommand cmd = new Git(repository).commit();
        cmd.setMessage("initial commit");
        cmd.call();
        //getLocalGitRepository().commit(new File[] {f1, f2, f3, f4, f5, f6}, "initial commit");

        GitClient client = getClient(workDir);
        TestStatusProgressMonitor monitor = new TestStatusProgressMonitor();
        Map<File, GitStatus> statuses = client.getStatus(new File[] { folder }, monitor);
        assertEquals(6, statuses.size());
        assertStatus(statuses, workDir, f1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f3, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f4, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f5, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f6, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
    }
    
    public void testStatusDifferentTree () throws Exception {
        File folder = new File(workDir.getParent(), "folder1");
        folder.mkdirs();
        try {
            StatusProgressMonitor monitor = new TestStatusProgressMonitor();
            getClient(workDir).getStatus(new File[] { folder }, monitor);
            fail("Different tree");
        } catch (IllegalArgumentException ex) {
            // OK
        }
    }

    private void assertStatus(Map<File, GitStatus> statuses, File repository, File file, boolean tracked, Status headVsIndex, Status indexVsWorking, Status headVsWorking, boolean conflict, TestStatusProgressMonitor monitor) {
        assertStatus(statuses, repository, file, tracked, headVsIndex, indexVsWorking, headVsWorking, conflict);
        assertStatus(monitor.notifiedStatuses, repository, file, tracked, headVsIndex, indexVsWorking, headVsWorking, conflict);
    }

    private static class TestStatusProgressMonitor extends StatusProgressMonitor {
        private final Map<File, GitStatus> notifiedStatuses;

        public TestStatusProgressMonitor() {
            notifiedStatuses = new HashMap<File, GitStatus>();
        }

        @Override
        public void notifyStatus(GitStatus status) {
            notifiedStatuses.put(status.getFile(), status);
        }
    }
}
