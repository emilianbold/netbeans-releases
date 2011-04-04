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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class AddTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public AddTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testAddNoRoots () throws Exception {
        File file = new File(workDir, "file");
        file.createNewFile();
        assertNullDirCacheEntry(Collections.singleton(file));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.add(new File[0], m);
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        assertDirCacheSize(1);
        assertDirCacheEntry(Collections.singleton(file));
    }
    
    public void testAddFileToEmptyIndex () throws Exception {
        File file = new File(workDir, "file");
        file.createNewFile();

        assertNullDirCacheEntry(Collections.singleton(file));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.add(new File[] { file }, m);
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        assertDirCacheSize(1);
        assertDirCacheEntry(Collections.singleton(file));

        // no error while adding the same file twice
        m = new Monitor();
        client.addNotificationListener(m);
        client.add(new File[] { file }, m);
        assertEquals(Collections.<File>emptySet(), m.notifiedFiles);
        assertDirCacheEntry(Collections.singleton(file));

        write(file, "hello, i've changed");
        assertDirCacheEntryModified(Collections.singleton(file));
        m = new Monitor();
        client.addNotificationListener(m);
        client.add(new File[] { file }, m);
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        assertDirCacheSize(1);
        assertDirCacheEntry(Collections.singleton(file));
    }

    public void testAddFileToNonEmptyIndex () throws Exception {
        File file = new File(workDir, "file");
        file.createNewFile();
        File file2 = new File(workDir, "file2");
        file2.createNewFile();
        File file3 = new File(workDir, "file3");
        file3.createNewFile();

        assertNullDirCacheEntry(Arrays.asList(file, file2, file3));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.add(new File[] { file }, m);
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        assertDirCacheSize(1);
        assertDirCacheEntry(Arrays.asList(file));
        assertNullDirCacheEntry(Arrays.asList(file2, file3));

        m = new Monitor();
        client.addNotificationListener(m);
        client.add(new File[] { file2 }, m);
        assertEquals(Collections.singleton(file2), m.notifiedFiles);
        assertDirCacheSize(2);
        assertDirCacheEntry(Arrays.asList(file, file2));
        assertNullDirCacheEntry(Arrays.asList(file3));

        m = new Monitor();
        client.addNotificationListener(m);
        client.add(new File[] { file, file2 }, m);
        assertEquals(Collections.<File>emptySet(), m.notifiedFiles);
        assertDirCacheSize(2);
        assertDirCacheEntry(Arrays.asList(file, file2));
        assertNullDirCacheEntry(Arrays.asList(file3));
    }

    public void testAddFolder () throws Exception {
        File file = new File(workDir, "file");
        write(file, "file");
        File folder1 = new File(workDir, "folder1");
        folder1.mkdirs();
        File file1_1 = new File(folder1, "file1");
        write(file1_1, "file1_1");
        File file1_2 = new File(folder1, "file2");
        write(file1_2, "file1_2");
        File subfolder1 = new File(folder1, "subfolder");
        subfolder1.mkdirs();
        File file1_1_1 = new File(subfolder1, "file1");
        write(file1_1_1, "file1_1_1");
        File file1_1_2 = new File(subfolder1, "file2");
        write(file1_1_2, "file1_1_2");

        File folder2 = new File(workDir, "folder2");
        folder2.mkdirs();
        File file2_1 = new File(folder2, "file1");
        write(file2_1, "file2_1");
        File file2_2 = new File(folder2, "file2");
        write(file2_2, "file2_2");
        File subfolder2 = new File(folder2, "subfolder");
        subfolder2.mkdirs();
        File file2_1_1 = new File(subfolder2, "file1");
        write(file2_1_1, "file2_1_1");
        File file2_1_2 = new File(subfolder2, "file2");
        write(file2_1_2, "file2_1_2");

        assertNullDirCacheEntry(Arrays.asList(file, file1_1, file1_2, file1_1_1, file1_1_2, file2_1, file2_2, file2_1_1, file2_1_2));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.add(new File[] { subfolder1 }, m);
        assertEquals(new HashSet<File>(Arrays.asList(file1_1_1, file1_1_2)), m.notifiedFiles);
        assertDirCacheSize(2);
        assertDirCacheEntry(Arrays.asList(file1_1_1, file1_1_2));
        assertNullDirCacheEntry(Arrays.asList(file, file1_1, file1_2, file2_1, file2_2, file2_1_1, file2_1_2));

        m = new Monitor();
        client.addNotificationListener(m);
        client.add(new File[] { folder1 }, m);
        assertEquals(new HashSet<File>(Arrays.asList(file1_1, file1_2)), m.notifiedFiles);
        assertDirCacheSize(4);
        assertDirCacheEntry(Arrays.asList(file1_1, file1_2, file1_1_1, file1_1_2));
        assertNullDirCacheEntry(Arrays.asList(file, file2_1, file2_1_1, file2_1_2));

        m = new Monitor();
        client.addNotificationListener(m);
        client.add(new File[] { folder2 }, m);
        assertEquals(new HashSet<File>(Arrays.asList(file2_1, file2_2, file2_1_1, file2_1_2)), m.notifiedFiles);
        assertDirCacheSize(8);
        assertDirCacheEntry(Arrays.asList(file1_1, file1_2, file1_1_1, file1_1_2, file2_1, file2_2, file2_1_1, file2_1_2));
    }

    public void testAddIgnored () throws Exception {
        File folder1 = new File(workDir, "folder1");
        folder1.mkdirs();
        File file1_1 = new File(folder1, "file1_1");
        write(file1_1, "file1_1");
        File file1_2 = new File(folder1, "file1_2");
        write(file1_2, "file1_2");

        File folder2 = new File(workDir, "folder2");
        folder2.mkdirs();
        File file2_1 = new File(folder2, "file2_1");
        write(file2_1, "file2_1");
        File file2_2 = new File(folder2, "file2_2");
        write(file2_2, "file2_2");

        write(new File(workDir, ".gitignore"), "file1_1\nfolder2");

        assertNullDirCacheEntry(Arrays.asList(file1_1, file2_1, file1_2, file2_2));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.add(new File[] { folder1, folder2 }, m);
        assertEquals(new HashSet<File>(Arrays.asList(file1_2)), m.notifiedFiles);
        assertDirCacheSize(1);
        assertDirCacheEntry(Arrays.asList(file1_2));
        assertNullDirCacheEntry(Arrays.asList(file1_1, file2_1, file2_2));
    }
    
    public void testAddIgnoreExecutable () throws Exception {
        File f = new File(workDir, "f");
        write(f, "hi, i am executable");
        f.setExecutable(true);
        File[] roots = { f };
        GitClient client = getClient(workDir);
        StoredConfig config = repository.getConfig();
        config.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_FILEMODE, false);
        config.save();
        // add should not set executable bit in index
        add(roots);
        Map<File, GitStatus> statuses = client.getStatus(roots, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false);
        
        // index should differ from wt
        config.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_FILEMODE, true);
        config.save();
        statuses = client.getStatus(roots, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_ADDED, false);
    }
    
    public void testUpdateIndexIgnoreExecutable () throws Exception {
        File f = new File(workDir, "f");
        write(f, "hi, i am not executable");
        File[] roots = { f };
        add(roots);
        commit(roots);
        f.setExecutable(true);
        GitClient client = getClient(workDir);
        StoredConfig config = repository.getConfig();
        config.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_FILEMODE, false);
        config.save();
        write(f, "hi, i am executable");
        // add should not set executable bit in index
        add(roots);
        Map<File, GitStatus> statuses = client.getStatus(roots, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        
        // index should differ from wt
        config.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_FILEMODE, true);
        config.save();
        statuses = client.getStatus(roots, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
    }

    public void testCancel () throws Exception {
        final File file = new File(workDir, "file");
        file.createNewFile();
        final File file2 = new File(workDir, "file2");
        file2.createNewFile();

        final Monitor m = new Monitor();
        final GitClient client = getClient(workDir);
        final Exception[] exs = new Exception[1];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file, file2 },m);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });
        m.cont = false;
        t1.start();
        m.waitAtBarrier();
        m.cancel();
        m.cont = true;
        t1.join();
        assertTrue(m.isCanceled());
        assertEquals(1, m.count);
        assertEquals(null, exs[0]);
    }

    private void assertDirCacheEntry (Collection<File> files) throws IOException {
        DirCache cache = repository.lockDirCache();
        for (File f : files) {
            String relativePath = Utils.getRelativePath(workDir, f);
            DirCacheEntry e = cache.getEntry(relativePath);
            assertNotNull(e);
            assertEquals(relativePath, e.getPathString());
            assertEquals(f.lastModified(), e.getLastModified());
            InputStream in = new FileInputStream(f);
            try {
                assertEquals(e.getObjectId(), repository.newObjectInserter().idFor(Constants.OBJ_BLOB, f.length(), in));
            } finally {
                in.close();
            }
            if (e.getLength() == 0 && f.length() != 0) {
                assertTrue(e.isSmudged());
            } else {
                assertEquals(f.length(), e.getLength());
            }
        }
        cache.unlock();
    }

    private void assertDirCacheEntryModified (Collection<File> files) throws IOException {
        DirCache cache = repository.lockDirCache();
        for (File f : files) {
            String relativePath = Utils.getRelativePath(workDir, f);
            DirCacheEntry e = cache.getEntry(relativePath);
            assertNotNull(e);
            assertEquals(relativePath, e.getPathString());
            InputStream in = new FileInputStream(f);
            try {
                assertNotSame(e.getObjectId(), repository.newObjectInserter().idFor(Constants.OBJ_BLOB, f.length(), in));
            } finally {
                in.close();
            }
        }
        cache.unlock();
    }

    private void assertNullDirCacheEntry (Collection<File> files) throws Exception {
        DirCache cache = repository.lockDirCache();
        for (File f : files) {
            DirCacheEntry e = cache.getEntry(Utils.getRelativePath(workDir, f));
            assertNull(e);
        }
        cache.unlock();
    }

    private void assertDirCacheSize (int expectedSize) throws IOException {
        DirCache cache = repository.lockDirCache();
        try {
            assertEquals(expectedSize, cache.getEntryCount());
        } finally {
            cache.unlock();
        }
    }
}
