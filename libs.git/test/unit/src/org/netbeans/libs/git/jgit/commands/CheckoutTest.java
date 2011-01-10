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
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.WindowCache;
import org.eclipse.jgit.storage.file.WindowCacheConfig;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CheckoutTest extends AbstractGitTestCase {

    private File workDir;
    private Repository repository;

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
        client.checkout(new File[] { file1 }, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file1));
        assertEquals(content2, read(file2));

        file1.delete();
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        client.checkout(new File[] { file1 }, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file1));
        assertEquals(content2, read(file2));
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
        client.checkout(new File[] { file1 }, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
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
        client.checkout(new File[] { folder }, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
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
        client.checkout(new File[] { file1 }, currentRevision, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertEquals(content1, read(file1));
        assertEquals(currentRevision, new Git(repository).log().call().iterator().next().getId().getName());

        write(file1, "another change in file 1");
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        client.checkout(new File[] { file1 }, previousRevision, ProgressMonitor.NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals("file 1 content", read(file1));
        assertEquals(currentRevision, new Git(repository).log().call().iterator().next().getId().getName());
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
