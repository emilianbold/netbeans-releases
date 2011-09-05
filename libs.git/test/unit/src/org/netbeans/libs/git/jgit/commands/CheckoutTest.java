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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.WindowCache;
import org.eclipse.jgit.storage.file.WindowCacheConfig;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CheckoutTest extends AbstractGitTestCase {

    private File workDir;
    private Repository repository;
    private static final String BRANCH = "nova";
    
    public CheckoutTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testJGitCheckout () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        Git git = new Git(repository);
        org.eclipse.jgit.api.AddCommand cmd = git.add();
        cmd.addFilepattern("file1");
        cmd.call();

        org.eclipse.jgit.api.CommitCommand commitCmd = git.commit();
        commitCmd.setAuthor("author", "author@something");
        commitCmd.setMessage("commit message");
        commitCmd.call();

        String commitId = git.log().call().iterator().next().getId().getName();
        DirCache cache = repository.lockDirCache();
        try {
            DirCacheCheckout checkout = new DirCacheCheckout(repository, null, cache, new RevWalk(repository).parseCommit(repository.resolve(commitId)).getTree());
            checkout.checkout();
        } finally {
            cache.unlock();
        }
    }

    public void testCheckoutFilesFromIndex () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "file 1 content");
        File file2 = new File(workDir, "file2");
        write(file2, "file 2 content");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        String content1 = "change in file 1";
        write(file1, content1);
        write(file2, "change in file 2");
        add(files);

        write(file1, "another change in file 1");
        String content2 = "another change in file 2";
        write(file2, content2);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        client.checkout(new File[] { file1 }, null, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file1));
        assertEquals(content2, read(file2));

        file1.delete();
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        client.checkout(new File[] { file1 }, null, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file1));
        assertEquals(content2, read(file2));
    }

    public void testCheckoutFilesFromIndex_NotRecursive () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File file1 = new File(folder, "file1");
        write(file1, "file 1 content");
        File subFolder = new File(folder, "subfolder");
        subFolder.mkdirs();
        File file2 = new File(subFolder, "file2");
        write(file2, "file 2 content");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        String content1 = "change 1";
        write(file1, content1);
        write(file2, content1);
        add(files);

        String content2 = "change 2";
        write(file1, content2);
        write(file2, content2);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);

        // direct file descendants
        client.checkout(new File[] { folder }, null, false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file1));
        assertEquals(content2, read(file2));

        write(file1, content2);
        // recursive
        client.checkout(new File[] { folder }, null, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file1));
        assertEquals(content1, read(file2));
    }

    public void testCheckoutFilesFromIndexFolderToFile () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "file 1 content");
        File file2 = new File(file1, "file2");
        File[] files = new File[] { file1 };
        add(files);
        commit(files);

        file1.delete();
        file1.mkdirs();
        write(file2, "blabla");

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_REMOVED, false);
        client.checkout(new File[] { file1 }, null, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assert(file1.isFile());
        assertEquals("file 1 content", read(file1));
    }

    public void testCheckoutFilesFromIndexFileToFolder () throws Exception {
        File folder = new File(workDir, "folder");
        File subFolder = new File(folder, "folder");
        File file1 = new File(subFolder, "file2");
        subFolder.mkdirs();
        write(file1, "file 1 content");
        File[] files = new File[] { folder };
        add(files);
        commit(files);

        file1.delete();
        subFolder.delete();
        folder.delete();
        write(folder, "blabla");

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_REMOVED, false);
        client.checkout(new File[] { folder }, null, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assert(file1.isFile());
        assertEquals("file 1 content", read(file1));
    }

    public void testCheckoutPathsFromRevision () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "file 1 content");
        File[] files = new File[] { file1 };
        add(files);
        commit(files);

        String content1 = "change in file 1";
        write(file1, content1);
        add(files);
        commit(files);

        write(file1, "another change in file 1");

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        String currentRevision = logs.next().getId().getName();
        String previousRevision = logs.next().getId().getName();

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        client.checkout(new File[] { file1 }, currentRevision, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertEquals(content1, read(file1));
        assertEquals(currentRevision, new Git(repository).log().call().iterator().next().getId().getName());

        write(file1, "another change in file 1");
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        client.checkout(new File[] { file1 }, previousRevision, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals("file 1 content", read(file1));
        assertEquals(currentRevision, new Git(repository).log().call().iterator().next().getId().getName());
    }
    
    
    public void testCheckoutPathsFromRevision_NotRecursive () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File file1 = new File(folder, "file1");
        write(file1, "file 1 content");
        File subFolder = new File(folder, "subfolder");
        subFolder.mkdirs();
        File file2 = new File(subFolder, "file2");
        write(file2, "file 2 content");
        File[] files = new File[] { file1, file2 };
        add(files);
        GitClient client = getClient(workDir);
        commit(files);

        String content1 = "change 1";
        write(file1, content1);
        write(file2, content1);
        add(files);

        Map<File, GitStatus> statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);

        // direct file descendants
        client.checkout(new File[] { folder }, "HEAD", false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file2));

        write(file1, content1);
        add(files);
        // recursive
        client.checkout(new File[] { folder }, "HEAD", true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
    }

    public void testLargeFile () throws Exception {
        unpack("large.dat.zip");
        File large = new File(workDir, "large.dat");
        assertTrue(large.exists());
        assertEquals(2158310, large.length());
        add();
        DirCache cache = repository.readDirCache();
        DirCacheEntry e = cache.getEntry("large.dat");
        WindowCacheConfig cfg = new WindowCacheConfig();
        cfg.setStreamFileThreshold((int) large.length() - 1);
        WindowCache.reconfigure(cfg);
        DirCacheCheckout.checkoutEntry(repository, large, e);
    }
    
    public void testCheckoutBranch () throws Exception {
        File file = new File(workDir, "file");
        write(file, "initial");
        File[] files = new File[] { file };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.commit(files, "initial", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.createBranch(BRANCH, info.getRevision(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(file, Constants.MASTER);
        add(file);
        GitRevisionInfo masterInfo = client.commit(files, Constants.MASTER, null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        // test checkout
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.checkoutRevision(BRANCH, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        String logFileContent[] = read(new File(workDir, ".git/logs/HEAD")).split("\\n");
        assertEquals("checkout: moving from master to nova", logFileContent[logFileContent.length - 1].substring(logFileContent[logFileContent.length - 1].indexOf("checkout: ")));
        assertTrue(m.notifiedFiles.contains(file));
        assertEquals("initial", read(file));
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        Map<String, GitBranch> branches = client.getBranches(false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(BRANCH).isActive());
        
        write(file, BRANCH);
        add();
        GitRevisionInfo novaInfo = client.commit(files, BRANCH, null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        m = new Monitor();
        client.addNotificationListener(m);
        client.checkoutRevision(Constants.MASTER, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(m.notifiedFiles.contains(file));
        assertEquals(Constants.MASTER, read(file));
        statuses = client.getStatus(new File[] { workDir }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        branches = client.getBranches(false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(Constants.MASTER).isActive());
        
        m = new Monitor();
        client.addNotificationListener(m);
        client.checkoutRevision(BRANCH, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(m.notifiedFiles.contains(file));
        assertEquals(BRANCH, read(file));
        statuses = client.getStatus(new File[] { workDir }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        branches = client.getBranches(false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(BRANCH).isActive());
    }
    
    public void testCheckoutRevision () throws Exception {
        File file = new File(workDir, "file");
        write(file, "initial");
        File[] files = new File[] { file };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.commit(files, "initial", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(file, Constants.MASTER);
        add(file);
        GitRevisionInfo masterInfo = client.commit(files, Constants.MASTER, null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        // test checkout
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.checkoutRevision(info.getRevision(), true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        String logFileContent[] = read(new File(workDir, ".git/logs/HEAD")).split("\\n");
        assertEquals("checkout: moving from master to " + info.getRevision(), logFileContent[logFileContent.length - 1].substring(logFileContent[logFileContent.length - 1].indexOf("checkout: ")));
        assertTrue(m.notifiedFiles.contains(file));
        assertEquals("initial", read(file));
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        Map<String, GitBranch> branches = client.getBranches(false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(GitBranch.NO_BRANCH).isActive());
        
        write(file, BRANCH);
        add();
        GitRevisionInfo novaInfo = client.commit(files, BRANCH, null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        m = new Monitor();
        client.addNotificationListener(m);
        client.checkoutRevision(Constants.MASTER, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(m.notifiedFiles.contains(file));
        assertEquals(Constants.MASTER, read(file));
        statuses = client.getStatus(new File[] { workDir }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        branches = client.getBranches(false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(Constants.MASTER).isActive());
    }
    
    public void testCheckoutRevisionKeepLocalChanges () throws Exception {
        File file = new File(workDir, "file");
        write(file, "initial");
        File[] files = new File[] { file };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.commit(files, "initial", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.createBranch(BRANCH, info.getRevision(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(file, Constants.MASTER);
        
        // test checkout
        // the file remains modified in WT
        client.checkoutRevision(BRANCH, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(Constants.MASTER, read(file));
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        Map<String, GitBranch> branches = client.getBranches(false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(BRANCH).isActive());
        
        add(file);
        // the file remains modified in index
        client.checkoutRevision(Constants.MASTER, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(Constants.MASTER, read(file));
        statuses = client.getStatus(new File[] { workDir }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        branches = client.getBranches(false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(Constants.MASTER).isActive());
    }
    
    public void testCheckoutRevisionAddRemoveFile () throws Exception {
        File file = new File(workDir, "file");
        write(file, "initial");
        File[] files = new File[] { file };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.commit(files, "initial", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.createBranch(BRANCH, info.getRevision(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        remove(false, file);
        commit(files);
        
        // test checkout
        // the file is added to WT
        client.checkoutRevision(Constants.MASTER, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(file.exists());
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        
        // the file is removed from WT
        client.checkoutRevision(BRANCH, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(file.exists());
        statuses = client.getStatus(new File[] { workDir }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertNull(statuses.get(file));
    }
    
    public void testCheckoutRevisionMergeLocalChanges () throws Exception {
        File file = new File(workDir, "file");
        write(file, "initial");
        File[] files = new File[] { file };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.commit(files, "initial", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.createBranch(BRANCH, info.getRevision(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(file, BRANCH);
        add(file);
        client.commit(files, BRANCH, null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        write(file, "initial");
        try {
            client.checkoutRevision(Constants.MASTER, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
            fail("Should fail, there are conflicts");
        } catch (GitException.CheckoutConflictException ex) {
            assertEquals(1, ex.getConflicts().length);
            assertEquals(file.getName(), ex.getConflicts()[0]);
            Map<String, GitBranch> branches = client.getBranches(false, ProgressMonitor.NULL_PROGRESS_MONITOR);
            assertTrue(branches.get(BRANCH).isActive());
        }
        CheckoutRevisionCommand cmd = new CheckoutRevisionCommand(repository, Constants.MASTER, false, ProgressMonitor.NULL_PROGRESS_MONITOR, new FileListener() {
            @Override
            public void notifyFile (File file, String relativePathToRoot) { }
        });
        try {
            cmd.execute();
            // and if somehow works...
            client.checkoutRevision(Constants.MASTER, false, ProgressMonitor.NULL_PROGRESS_MONITOR);
            // and do not forget to fix this code when JGit is fixed.
            fail("Hey, JGit is fixed, why don't you fix me as well?");
        } catch (IllegalStateException ex) {
            assertEquals("Mixed stages not allowed: 2 file", ex.getMessage());
        }
    }

    public void testCheckoutNoHeadYet () throws Exception {
        final File otherWT = new File(workDir.getParentFile(), "repo2");
        GitClient client = getClient(otherWT);
        client.init(ProgressMonitor.NULL_PROGRESS_MONITOR);
        File f = new File(otherWT, "f");
        write(f, "init");
        client.add(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        client = getClient(workDir);
        client.fetch(otherWT.getAbsolutePath(), Arrays.asList(new String[] { "refs/heads/*:refs/remotes/origin/*" }), ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.checkoutRevision("origin/master", true, ProgressMonitor.NULL_PROGRESS_MONITOR);
    }

    private void unpack (String filename) throws IOException {
        File zipLarge = new File(getDataDir(), filename);
        ZipInputStream is = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipLarge)));
        ZipEntry entry;
        while ((entry = is.getNextEntry()) != null) {
            File unpacked = new File(workDir, entry.getName());
            FileChannel channel = new FileOutputStream(unpacked).getChannel();
            byte[] bytes = new byte[2048];
            try {
                int len;
                long size = entry.getSize();
                while (size > 0 && (len = is.read(bytes, 0, 2048)) > 0) {
                    ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, len);
                    int j = channel.write(buffer);
                    size -= len;
                }
            } finally {
                channel.close();
            }
        }
        ZipEntry e = is.getNextEntry();
    }
}
