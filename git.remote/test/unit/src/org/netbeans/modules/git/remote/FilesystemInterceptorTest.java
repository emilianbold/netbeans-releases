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
package org.netbeans.modules.git.remote;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.remotefs.versioning.spi.FilesystemInterceptorProviderImpl;
import org.netbeans.modules.remotefs.versioning.spi.VersioningAnnotationProviderImpl;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;

/**
 *
 * @author ondra
 */
public class FilesystemInterceptorTest extends AbstractGitTestCase {

    public static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";
    private StatusRefreshLogHandler h;

    public FilesystemInterceptorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        if (Utilities.isWindows()) {
            throw new UnsupportedOperationException("Unsupported platform");
        }
        super.setUp();
        MockServices.setServices(new Class[] {VersioningAnnotationProviderImpl.class, GitVCS.class, FilesystemInterceptorProviderImpl.class});
        System.setProperty("versioning.git.handleExternalEvents", "false");
        System.setProperty("org.netbeans.modules.masterfs.watcher.disable", "true");
        System.setProperty("org.netbeans.modules.git.remote.localfilesystem.enable", "true");
        Git.STATUS_LOG.setLevel(Level.ALL);
        h = new StatusRefreshLogHandler(repositoryLocation);
        Git.STATUS_LOG.addHandler(h);
    }

    @Override
    protected void tearDown() throws Exception {
        Git.STATUS_LOG.removeHandler(h);
        super.tearDown();
    }

    public void testSeenRootsLogin () throws Exception {
        VCSFileProxy folderA = VCSFileProxy.createFileProxy(repositoryLocation, "folderA");
        VCSFileProxy fileA1 = VCSFileProxy.createFileProxy(folderA, "file1");
        VCSFileProxy fileA2 = VCSFileProxy.createFileProxy(folderA, "file2");
        VCSFileProxySupport.mkdirs(folderA);
        VCSFileProxySupport.createNew(fileA1);
        VCSFileProxySupport.createNew(fileA2);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxy fileB1 = VCSFileProxy.createFileProxy(folderB, "file1");
        VCSFileProxy fileB2 = VCSFileProxy.createFileProxy(folderB, "file2");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxySupport.createNew(fileB1);
        VCSFileProxySupport.createNew(fileB2);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxy fileC1 = VCSFileProxy.createFileProxy(folderC, "file1");
        VCSFileProxy fileC2 = VCSFileProxy.createFileProxy(folderC, "file2");
        VCSFileProxySupport.mkdirs(folderC);
        VCSFileProxySupport.createNew(fileC1);
        VCSFileProxySupport.createNew(fileC2);

        FilesystemInterceptor interceptor = Git.getInstance().getVCSInterceptor();
        Field f = FilesystemInterceptor.class.getDeclaredField("gitFolderEventsHandler");
        f.setAccessible(true);
        Object hgFolderEventsHandler = f.get(interceptor);
        f = hgFolderEventsHandler.getClass().getDeclaredField("seenRoots");
        f.setAccessible(true);
        HashMap<VCSFileProxy, Set<VCSFileProxy>> map = (HashMap) f.get(hgFolderEventsHandler);

        LogHandler handler = new LogHandler();
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToInitializeRoots(folderA);
        interceptor.pingRepositoryRootFor(folderA);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        Set<VCSFileProxy> files = map.get(repositoryLocation);
        assertEquals(1, files.size());
        assertTrue(files.contains(folderA));
        handler.setFilesToInitializeRoots(fileA1);
        interceptor.pingRepositoryRootFor(fileA1);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        files = map.get(repositoryLocation);
        assertEquals(1, files.size());
        assertTrue(files.contains(folderA));

        handler.setFilesToInitializeRoots(fileB1);
        interceptor.pingRepositoryRootFor(fileB1);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(2, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));

        handler.setFilesToInitializeRoots(fileB2);
        interceptor.pingRepositoryRootFor(fileB2);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(3, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));
        assertTrue(files.contains(fileB2));

        handler.setFilesToInitializeRoots(folderC);
        interceptor.pingRepositoryRootFor(folderC);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(4, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));
        assertTrue(files.contains(fileB2));
        assertTrue(files.contains(folderC));

        handler.setFilesToInitializeRoots(folderB);
        interceptor.pingRepositoryRootFor(folderB);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(3, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));
        assertTrue(files.contains(folderC));

        handler.setFilesToInitializeRoots(repositoryLocation);
        interceptor.pingRepositoryRootFor(repositoryLocation);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        Git.STATUS_LOG.removeHandler(handler);
        assertEquals(1, files.size());
        assertTrue(files.contains(repositoryLocation));
    }

    private class LogHandler extends Handler {
        private VCSFileProxy fileToInitialize;
        private boolean filesInitialized;
        private final HashSet<VCSFileProxy> initializedFiles = new HashSet<VCSFileProxy>();

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("GitFolderEventsHandler.initializeFiles: finished")) {
                synchronized (this) {
                    filesInitialized = true;
                    notifyAll();
                }
            } else if (record.getMessage().contains("GitFolderEventsHandler.initializeFiles: ")) {
                if (record.getParameters()[0].equals(fileToInitialize.getPath())) {
                    synchronized (this) {
                        initializedFiles.add(fileToInitialize);
                        notifyAll();
                    }
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        private void setFilesToInitializeRoots (VCSFileProxy file) {
            fileToInitialize = file;
            initializedFiles.clear();
            filesInitialized = false;
        }

        private boolean waitForFilesToInitializeRoots() throws InterruptedException {
            for (int i = 0; i < 20; ++i) {
                synchronized (this) {
                    if (filesInitialized && initializedFiles.contains(fileToInitialize)) {
                        return true;
                    }
                    wait(500);
                }
            }
            return false;
        }
    }

    public void testGetWrongAttribute () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "attrfile");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.toFileObject();

        String str = (String) fo.getAttribute("peek-a-boo");
        assertNull(str);
    }

    public void testGetRemoteLocationAttribute () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "attrfile");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.toFileObject();

        String str = (String) fo.getAttribute(PROVIDED_EXTENSIONS_REMOTE_LOCATION);
        // TODO implement getRemoteRepositoryURL
//        assertNotNull(str);
//        assertEquals(repositoryLocation.getAbsolutePath().toString(), str);
    }

    public void testModifyVersionedFile () throws Exception {
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        h.setFilesToRefresh(Collections.singleton(file));
        VCSFileProxySupport.createNew(file);
        add();
        commit();
        FileObject fo = file.normalizeFile().toFileObject();
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
        assertTrue(h.waitForFilesToRefresh());

        h.setFilesToRefresh(Collections.singleton(file));
        PrintWriter pw = new PrintWriter(fo.getOutputStream());
        pw.println("hello new file");
        pw.close();
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_WORKING_TREE, Status.MODIFIED_INDEX_WORKING_TREE), getCache().getStatus(file).getStatus());
    }

    public void testDeleteUnversionedFile () throws Exception {
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(file);
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());

        // delete
        h.setFilesToRefresh(Collections.singleton(file));
        delete(file);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(file.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
    }

    public void testDeleteVersionedFile() throws Exception {
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(file);
        add(file);
        commit(repositoryLocation);
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());

        // delete
        h.setFilesToRefresh(Collections.singleton(file));
        delete(file);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(file.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        commit(repositoryLocation);
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
    }

    public void testDeleteVersionedFileExternally() throws Exception {
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        h.setFilesToRefresh(Collections.singleton(file));
        repositoryLocation.toFileObject().createData(file.getName());
        assertTrue(h.waitForFilesToRefresh());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        add(file);
        commit(repositoryLocation);
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());

        // delete externally
        VCSFileProxySupport.delete(file);

        // test
        assertFalse(file.exists());

        // notify changes
        h.setFilesToRefresh(Collections.singleton(file));
        VersioningSupport.refreshFor(new VCSFileProxy[]{file});
        assertTrue(h.waitForFilesToRefresh());
        assertEquals(EnumSet.of(Status.REMOVED_INDEX_WORKING_TREE, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        delete(true, file);
        commit(repositoryLocation);
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
    }

    public void testDeleteVersionedFolder() throws Exception {
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(repositoryLocation, "folder1");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy file = VCSFileProxy.createFileProxy(folder, "file");
        VCSFileProxySupport.createNew(file);
        add();
        commit();
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());

        // delete
        h.setFilesToRefresh(Collections.singleton(folder));
        delete(folder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(folder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
    }
    
    public void testDeleteVersionedFolder_NoMetadata() throws Exception {
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(repositoryLocation, "folder1");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy file = VCSFileProxy.createFileProxy(folder, "file");
        VCSFileProxySupport.createNew(file);
        add();
        commit();
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());

        // delete
        Utils.deleteRecursively(VCSFileProxy.createFileProxy(repositoryLocation, ".git"));
        delete(folder);
        
        // test
        assertFalse(folder.exists());
    }
    
    public void testDeleteVersionedFolder_NoMetadata_FO() throws Exception {
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(repositoryLocation, "folder1");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy file = VCSFileProxy.createFileProxy(folder, "file");
        VCSFileProxySupport.createNew(file);
        add();
        commit();
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());

        // delete
        Utils.deleteRecursively(VCSFileProxy.createFileProxy(repositoryLocation, ".git"));
        deleteFO(folder);
        
        // test
        assertFalse(folder.exists());
    }

    public void testDeleteNotVersionedFolder() throws Exception {
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(repositoryLocation, "folder2");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy file = VCSFileProxy.createFileProxy(folder, "file");
        VCSFileProxySupport.createNew(file);

        // delete
        h.setFilesToRefresh(Collections.singleton(folder));
        delete(folder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(folder.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
    }

    public void testDeleteRepositoryLocationRoot() throws Exception {
        // delete
        VCSFileProxy f = VCSFileProxy.createFileProxy(repositoryLocation, ".aaa");
        VCSFileProxySupport.createNew(f);
        add(f);
        commit(f);
        f = VCSFileProxy.createFileProxy(repositoryLocation, "aaa");
        VCSFileProxySupport.createNew(f);
        add(f);
        commit(f);

        delete(repositoryLocation);

        // test
        assertFalse(repositoryLocation.exists());
    }

    public void testDeleteVersionedFileTree() throws Exception {
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(folder, "folder1");
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(folder, "folder2");
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(folder1, "file1");
        VCSFileProxySupport.createNew(file11);
        VCSFileProxy file12 = VCSFileProxy.createFileProxy(folder1, "file2");
        VCSFileProxySupport.createNew(file12);
        VCSFileProxy file21 = VCSFileProxy.createFileProxy(folder2, "file1");
        VCSFileProxySupport.createNew(file21);
        VCSFileProxy file22 = VCSFileProxy.createFileProxy(folder2, "file2");
        VCSFileProxySupport.createNew(file22);

        add();
        commit();

        // delete
        h.setFilesToRefresh(Collections.singleton(folder));
        delete(folder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file11).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file12).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file21).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file22).getStatus());
    }

    public void testDeleteNotVersionedFileTree() throws Exception {
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(folder, "folder1");
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(folder, "folder2");
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(folder1, "file1");
        VCSFileProxySupport.createNew(file11);
        VCSFileProxy file12 = VCSFileProxy.createFileProxy(folder1, "file2");
        VCSFileProxySupport.createNew(file12);
        VCSFileProxy file21 = VCSFileProxy.createFileProxy(folder2, "file1");
        VCSFileProxySupport.createNew(file21);
        VCSFileProxy file22 = VCSFileProxy.createFileProxy(folder2, "file2");
        VCSFileProxySupport.createNew(file22);

        // delete
        h.setFilesToRefresh(Collections.singleton(folder));
        delete(folder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertFalse(file11.exists());
        assertFalse(file12.exists());
        assertFalse(file21.exists());
        assertFalse(file22.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file22).getStatus());
    }

    public void testCreateNewFile() throws Exception {
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "file");

        h.setFilesToRefresh(Collections.singleton(file));
        // create
        FileObject fo = repositoryLocation.toFileObject();
        fo.createData(file.getName());
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(file.exists());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
    }

    public void testDeleteA_CreateA() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());

        // delete
        h.setFilesToRefresh(Collections.singleton(fileA));
        FileObject fo = fileA.toFileObject();
        fo.delete();
        assertTrue(h.waitForFilesToRefresh());

        // test if deleted
        assertFalse(fileA.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());

        // create
        h.setFilesToRefresh(Collections.singleton(fileA));
        fo.getParent().createData(fo.getName());
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
    }

    public void testDeleteA_CreateA_RunAtomic() throws Exception {
        // init
        final VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        h.setFilesToRefresh(Collections.singleton(fileA));
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertTrue(h.waitForFilesToRefresh());

        final FileObject fo = fileA.toFileObject();
        AtomicAction a = new AtomicAction() {
            @Override
            public void run() throws IOException {
                fo.delete();
                fo.getParent().createData(fo.getName());
            }
        };
        h.setFilesToRefresh(Collections.singleton(fileA));
        fo.getFileSystem().runAtomicAction(a);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
    }

    public void testRenameVersionedFile_DO() throws Exception {
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "toFile");

        // rename
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        renameDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertTrue(info.isRenamed());
        assertEquals(fromFile, info.getOldFile());
    }

    public void testMoveVersionedFile_DO() throws Exception {
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        moveDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertTrue(info.isRenamed());
        assertEquals(fromFile, info.getOldFile());
    }

    public void testMoveVersionedFile2Repos_DO() throws Exception {
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        add(fromFile);
        commit(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        moveDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveVersionedFolder2Repos_DO () throws Exception {
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation2, "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveFileTree2Repos_DO () throws Exception {
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        add(repositoryLocation);
        commit(repositoryLocation);

        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());

        assertFalse(fromFolder.exists());
        assertFalse(fromFolder1.exists());
        assertFalse(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());
    }

    public void testMoveVersionedFile2Repos_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        add(repositoryLocation);
        commit(repositoryLocation);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        moveFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveVersionedFolder2Repos_FO() throws Exception {
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation2, "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveFileTree2Repos_FO() throws Exception {
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        add(repositoryLocation);
        commit(repositoryLocation);

        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());

        assertFalse(fromFolder.exists());
        assertFalse(fromFolder1.exists());
        assertFalse(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());
    }

    public void testRenameUnversionedFile_DO() throws Exception {
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "toFile");

        // rename
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        renameDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveUnversionedFile_DO() throws Exception {
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // rename
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        moveDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testRenameUnversionedFolder_DO() throws Exception {
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "fromFolder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // rename
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        renameDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveUnversionedFolder_DO() throws Exception {
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }
    
    public void testRenameFileChangeCase_DO () throws Exception {
        // prepare
        VCSFileProxy fromFile = createFile(repositoryLocation, "file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "FILE");
        add(fromFile);
        commit(fromFile);
        
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        renameDO(fromFile, toFile);
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(Arrays.asList(toFile.getParentFile().listFiles()).contains(toFile.getName()));
            assertFalse(Arrays.asList(fromFile.getParentFile().listFiles()).contains(fromFile.getName()));
        } else {
            assertTrue(h.waitForFilesToRefresh());
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
            assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
        }
    }
    
    public void testRenameFileChangeCase_FO () throws Exception {
        // prepare
        VCSFileProxy fromFile = createFile(repositoryLocation, "file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "FILE");
        add(fromFile);
        commit(fromFile);
        
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        renameFO(fromFile, toFile);
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(Arrays.asList(toFile.getParentFile().listFiles()).contains(toFile.getName()));
            assertFalse(Arrays.asList(fromFile.getParentFile().listFiles()).contains(fromFile.getName()));
        } else {
            assertTrue(h.waitForFilesToRefresh());
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
            assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
        }
    }
    
    public void testRenameFolderChangeCase_DO () throws Exception {
        // prepare
        VCSFileProxy fromFolder = createFolder(repositoryLocation, "folder");
        VCSFileProxy fromFile = createFile(fromFolder, "file");
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "FOLDER");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        add(fromFolder);
        commit(fromFolder);
        
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        renameDO(fromFolder, toFolder);
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(Arrays.asList(toFolder.getParentFile().listFiles()).contains(toFolder.getName()));
            assertFalse(Arrays.asList(fromFolder.getParentFile().listFiles()).contains(fromFolder.getName()));
        } else {
            assertTrue(h.waitForFilesToRefresh());
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
            assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
        }
    }
    
    public void testRenameFolderChangeCase_FO () throws Exception {
        // prepare
        VCSFileProxy fromFolder = createFolder(repositoryLocation, "folder");
        VCSFileProxy fromFile = createFile(fromFolder, "file");
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "FOLDER");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        add(fromFolder);
        commit(fromFolder);
        
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        renameFO(fromFolder, toFolder);
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(Arrays.asList(toFolder.getParentFile().listFiles()).contains(toFolder.getName()));
            assertFalse(Arrays.asList(fromFolder.getParentFile().listFiles()).contains(fromFolder.getName()));
        } else {
            assertTrue(h.waitForFilesToRefresh());
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
            assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
        }
    }

    public void testCopyVersionedFile_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        // sadly does not work in jgit
        assertFalse(info.isCopied());
    }

    public void testCopyUnversionedFile_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyUnversionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyAddedFile2UnversionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // copy
        copyDO(fromFile, toFile);
        getCache().refreshAllRoots(Collections.singleton(fromFile));
        getCache().refreshAllRoots(Collections.singleton(toFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyVersionedFile2UnversionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy unversionedFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), getName() + "_unversioned");
        VCSFileProxySupport.mkdirs(unversionedFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(unversionedFolder, fromFile.getName());

        // add
        add(fromFile);
        commit();

        // copy
        copyDO(fromFile, toFile);
        getCache().refreshAllRoots(Collections.singleton(fromFile));
        getCache().refreshAllRoots(Collections.singleton(toFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyVersionedFolder2UnversionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy unversionedFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), getName() + "_unversioned");
        VCSFileProxySupport.mkdirs(unversionedFolder);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(unversionedFolder, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFolder);
        commit(fromFolder);

        // copy
        copyDO(fromFolder, toFolder);
        getCache().refreshAllRoots(new HashSet<VCSFileProxy>(Arrays.asList(fromFile, toFile)));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyAddedFile2VersionedFolder_DO() throws Exception {
        // init
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(repositoryLocation);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // rename
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyA2B2C_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileB, fileC)));
        copyDO(fileA, fileB);
        copyDO(fileB, fileC);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fileA));

        // test
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
    }

    public void testCopyVersionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(toParent, fromFolder.getName()), fromFile.getName());
        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyFileTree_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        VCSFileProxySupport.mkdirs(toFolderParent);

        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());

        // move
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void testCopyVersionedFile2Repos_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyVersionedFolder2Repos_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation2, "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        add(repositoryLocation);
        commit(repositoryLocation);

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyFileTree2Repos_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        add(repositoryLocation);
        commit(repositoryLocation);

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void testCopyVersionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyUnversionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyUnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyAddedFile2UnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // copy
        copyFO(fromFile, toFile);
        getCache().refreshAllRoots(Collections.singleton(fromFile));
        getCache().refreshAllRoots(Collections.singleton(toFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyAddedFile2VersionedFolder_FO() throws Exception {
        // init
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        add();
        commit();
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyA2B2C_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // copy
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileB, fileC)));
        copyFO(fileA, fileB);
        copyFO(fileB, fileC);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fileA));

        // test
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void testCopyVersionedFile2UnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), getName() + "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);
        commit(repositoryLocation);

        // copy
        copyFO(fromFile, toFile);
        getCache().refreshAllRoots(new HashSet<VCSFileProxy>(Arrays.asList(fromFile, toFile)));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyVersionedFolder2UnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), getName() + "toFolder");

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFolder);
        commit();

        // copy
        copyFO(fromFolder, toFolder);
        getCache().refreshAllRoots(new HashSet<VCSFileProxy>(Arrays.asList(fromFile, toFile)));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyVersionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyFileTree_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        VCSFileProxySupport.mkdirs(toFolderParent);

        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void testCopyVersionedFile2Repos_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyVersionedFolder2Repos_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(initSecondRepository(), "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");

        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyFileTree2Repos_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(initSecondRepository(), "toFolder");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        add();
        commit();

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void testCopyA2CB2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxySupport.createNew(fileB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // copy
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileC, fileA)));
        copyFO(fileA, fileC);
        copyFO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fileB));

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertTrue(fileB.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void testRenameAddedFile_DO () throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "toFile");

        // add
        add(fromFile);

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFile, toFile)));
        renameDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveAddedFile2Folder_DO () throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFile, toFile)));
        moveDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveAddedFile2UnversionedFolder_DO () throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFile)));
        moveDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testRenameA2B2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "to");

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB)));
        renameDO(fileA, fileB);
        renameDO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
    }

    public void testMoveA2B2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(folder);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folder, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB)));
        moveDO(fileA, fileB);
        moveDO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
    }

    public void testRenameA2B2C_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(repositoryLocation, "C");

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB, fileC)));
        renameDO(fileA, fileB);
        renameDO(fileB, fileC);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        FileInformation info = getCache().getStatus(fileC);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertTrue(info.isRenamed());
        assertEquals(fileA, info.getOldFile());
    }

    public void testRenameA2B2C2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(repositoryLocation, "C");

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB, fileC)));
        renameDO(fileA, fileB);
        renameDO(fileB, fileC);
        renameDO(fileC, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());
    }

    public void testMoveA2CB2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        write(fileA, "aaa");
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        write(fileB, "bbb");
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB, fileC)));
        moveDO(fileA, fileC);
        moveDO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void testMoveA2CB2A_FO () throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        write(fileA, "aaa");
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        write(fileB, "bbb");
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB, fileC)));
        moveFO(fileA, fileC);
        moveFO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void testRenameA2CB2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        write(fileA, "aaa");
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        write(fileB, "bbb");
        add();
        commit();

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(repositoryLocation, "C");

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB, fileC)));
        renameDO(fileA, fileC);
        renameDO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void testRenameA2CB2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        write(fileA, "aaa");
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        write(fileB, "bbb");
        add();
        commit();

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(repositoryLocation, "C");

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB, fileC)));
        renameFO(fileA, fileC);
        renameFO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void testMoveA2B2C2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB, fileC)));
        moveDO(fileA, fileB);
        moveDO(fileB, fileC);
        moveDO(fileC, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());

        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB)));
        moveDO(fileA, fileB);
        assertTrue(h.waitForFilesToRefresh());
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileB, fileC)));
        moveDO(fileB, fileC);
        assertTrue(h.waitForFilesToRefresh());
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileC)));
        moveDO(fileC, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());
    }

    public void testRenameA2B_CreateA_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        // rename
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB)));
        renameDO(fileA, fileB);
        // create from file
        fileA.getParentFile().toFileObject().createData(fileA.getName());
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        FileInformation info = getCache().getStatus(fileB);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
    }

    public void testMoveA2B_CreateA_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB)));
        moveDO(fileA, fileB);

        // create from file
        fileA.getParentFile().toFileObject().createData(fileA.getName());
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        FileInformation info = getCache().getStatus(fileB);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
    }

    public void testDeleteA_RenameB2A_DO_129805() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        VCSFileProxySupport.createNew(fileB);
        add();
        commit();

        // delete A
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB)));
        delete(fileA);
        // rename B to A
        renameDO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fileB.exists());
        assertTrue(fileA.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
    }

    public void testRenameVersionedFolder_DO () throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy file = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(file);
        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, file.getName());

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFolder, toFolder)));
        renameDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertTrue(info.isRenamed());
        assertEquals(file, info.getOldFile());
    }

    public void testMoveVersionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy file = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(file);
        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, file.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertTrue(info.isRenamed());
        assertEquals(file, info.getOldFile());
    }

    public void testRenameFileTree_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);
        add();
        commit();

        // rename
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFolder, toFolder)));
        renameDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, "folder2");
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        FileInformation info11 = getCache().getStatus(toFile11);
        FileInformation info12 = getCache().getStatus(toFile12);
        FileInformation info21 = getCache().getStatus(toFile21);
        FileInformation info22 = getCache().getStatus(toFile22);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info11.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info12.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info21.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info22.getStatus());
        assertEquals(fromFile11, info11.getOldFile());
        assertEquals(fromFile12, info12.getOldFile());
        assertEquals(fromFile21, info21.getOldFile());
        assertEquals(fromFile22, info22.getOldFile());
        assertTrue(info11.isRenamed());
        assertTrue(info12.isRenamed());
        assertTrue(info21.isRenamed());
        assertTrue(info22.isRenamed());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile22).getStatus());
    }

    public void testMoveFileTree_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        VCSFileProxySupport.mkdirs(toFolderParent);

        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        FileInformation info11 = getCache().getStatus(toFile11);
        FileInformation info12 = getCache().getStatus(toFile12);
        FileInformation info21 = getCache().getStatus(toFile21);
        FileInformation info22 = getCache().getStatus(toFile22);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info11.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info12.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info21.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info22.getStatus());
        assertEquals(fromFile11, info11.getOldFile());
        assertEquals(fromFile12, info12.getOldFile());
        assertEquals(fromFile21, info21.getOldFile());
        assertEquals(fromFile22, info22.getOldFile());
        assertTrue(info11.isRenamed());
        assertTrue(info12.isRenamed());
        assertTrue(info21.isRenamed());
        assertTrue(info22.isRenamed());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile22).getStatus());
    }

    public void testRenameVersionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "toFile");

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFile, toFile)));
        renameFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fromFile, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testMoveVersionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFile, toFile)));
        moveFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fromFile, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testRenameUnversionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "toFile");

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFile, toFile)));
        renameFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveUnversionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFile, toFile)));
        moveFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testRenameUnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "fromFolder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFolder, toFolder)));
        renameFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveUnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testRenameAddedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "toFile");

        // add
        add(fromFile);

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFile, toFile)));
        renameFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveAddedFile2UnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFile)));
        moveFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveAddedFile2VersionedFolder_FO() throws Exception {
        // init
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        //commit(repositoryLocation);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFile, toFile)));
        moveFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testRenameA2B2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.createNew(fileA);
        commit(repositoryLocation);

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "to");

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB)));
        renameFO(fileA, fileB);
        renameFO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
    }

    public void testMoveA2B2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        assertFalse(fileA.exists());
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        assertFalse(folder.exists());
        VCSFileProxySupport.mkdirs(folder);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folder, fileA.getName());
        assertFalse(fileB.exists());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB)));
        moveFO(fileA, fileB);
        moveFO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
    }

    public void testRenameA2B2C_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(repositoryLocation, "C");

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB, fileC)));
        renameFO(fileA, fileB);
        renameFO(fileB, fileC);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        FileInformation info = getCache().getStatus(fileC);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fileA, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testMoveA2B2C_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB, fileC)));
        moveFO(fileA, fileB);
        moveFO(fileB, fileC);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        FileInformation info = getCache().getStatus(fileC);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fileA, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testRenameA2B2C2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(repositoryLocation, "C");

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB, fileC)));
        renameFO(fileA, fileB);
        renameFO(fileB, fileC);
        renameFO(fileC, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());
    }

    public void testMoveA2B2C2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB, fileC)));
        moveFO(fileA, fileB);
        moveFO(fileB, fileC);
        moveFO(fileC, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());
    }

    public void testRenameA2B_CreateA_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        // rename
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB)));
        renameFO(fileA, fileB);
        assertTrue(h.waitForFilesToRefresh());

        // create from file
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA)));
        fileA.getParentFile().toFileObject().createData(fileA.getName());
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());

        //should be uptodate
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        FileInformation info = getCache().getStatus(fileB);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fileA, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testMoveA2B_CreateA_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA, fileB)));
        moveFO(fileA, fileB);
        assertTrue(h.waitForFilesToRefresh());

        // create from file
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fileA)));
        fileA.getParentFile().toFileObject().createData(fileA.getName());
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());

        //should be uptodate
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        FileInformation info = getCache().getStatus(fileB);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fileA, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testRenameVersionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // rename
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFolder, toFolder)));
        renameFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fromFile, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testMoveVersionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fromFile, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testRenameFileTree_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);
        add();
        commit();

        // rename
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFolder, toFolder)));
        renameFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, "folder2");
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        FileInformation info11 = getCache().getStatus(toFile11);
        FileInformation info12 = getCache().getStatus(toFile12);
        FileInformation info21 = getCache().getStatus(toFile21);
        FileInformation info22 = getCache().getStatus(toFile22);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info11.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info12.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info21.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info22.getStatus());
        assertEquals(fromFile11, info11.getOldFile());
        assertEquals(fromFile12, info12.getOldFile());
        assertEquals(fromFile21, info21.getOldFile());
        assertEquals(fromFile22, info22.getOldFile());
        assertTrue(info11.isRenamed());
        assertTrue(info12.isRenamed());
        assertTrue(info21.isRenamed());
        assertTrue(info22.isRenamed());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void testMoveFileTree_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        VCSFileProxySupport.mkdirs(toFolderParent);

        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());

        // move
        h.setFilesToRefresh(new HashSet<VCSFileProxy>(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        FileInformation info11 = getCache().getStatus(toFile11);
        FileInformation info12 = getCache().getStatus(toFile12);
        FileInformation info21 = getCache().getStatus(toFile21);
        FileInformation info22 = getCache().getStatus(toFile22);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info11.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info12.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info21.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info22.getStatus());
        assertEquals(fromFile11, info11.getOldFile());
        assertEquals(fromFile12, info12.getOldFile());
        assertEquals(fromFile21, info21.getOldFile());
        assertEquals(fromFile22, info22.getOldFile());
        assertTrue(info11.isRenamed());
        assertTrue(info12.isRenamed());
        assertTrue(info21.isRenamed());
        assertTrue(info22.isRenamed());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }
    
    public void testMoveFileToIgnoredFolder_DO () throws Exception {
        // prepare
        VCSFileProxy ignored = createFolder(repositoryLocation, "ignoredFolder");
        getClient(repositoryLocation).ignore(new VCSFileProxy[] { ignored }, GitUtils.NULL_PROGRESS_MONITOR);
        VCSFileProxy toFolder = createFolder(ignored, "toFolder");
        VCSFileProxy fromFile = createFile(repositoryLocation, "file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        add(fromFile);
        commit(fromFile);
        getCache().refreshAllRoots(ignored);
        
        // move
        moveDO(fromFile, toFile);
        
        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        getCache().refreshAllRoots(fromFile, toFile);
        assertTrue(getCache().getStatus(fromFile).containsStatus(FileInformation.Status.REMOVED_HEAD_INDEX));
        assertTrue(getCache().getStatus(toFile).containsStatus(FileInformation.Status.NOTVERSIONED_EXCLUDED));
    }
    
    public void testMoveFileToIgnoredFolder_FO () throws Exception {
        // prepare
        VCSFileProxy ignored = createFolder(repositoryLocation, "ignoredFolder");
        getClient(repositoryLocation).ignore(new VCSFileProxy[] { ignored }, GitUtils.NULL_PROGRESS_MONITOR);
        VCSFileProxy toFolder = createFolder(ignored, "toFolder");
        VCSFileProxy fromFile = createFile(repositoryLocation, "file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        add(fromFile);
        commit(fromFile);
        getCache().refreshAllRoots(ignored);
        
        // move
        moveFO(fromFile, toFile);
        
        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        getCache().refreshAllRoots(fromFile, toFile);
        assertTrue(getCache().getStatus(fromFile).containsStatus(FileInformation.Status.REMOVED_HEAD_INDEX));
        assertTrue(getCache().getStatus(toFile).containsStatus(FileInformation.Status.NOTVERSIONED_EXCLUDED));
    }

    public void testCopyFileToIgnoredFolder_DO () throws Exception {
        // prepare
        VCSFileProxy ignored = createFolder(repositoryLocation, "ignoredFolder");
        getClient(repositoryLocation).ignore(new VCSFileProxy[] { ignored }, GitUtils.NULL_PROGRESS_MONITOR);
        VCSFileProxy toFolder = createFolder(ignored, "toFolder");
        VCSFileProxy fromFile = createFile(repositoryLocation, "file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        add(fromFile);
        commit(fromFile);
        getCache().refreshAllRoots(ignored);
        
        // copy
        copyDO(fromFile, toFile);
        
        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());
        getCache().refreshAllRoots(fromFile, toFile);
        assertTrue(getCache().getStatus(fromFile).containsStatus(FileInformation.Status.UPTODATE));
        assertTrue(getCache().getStatus(toFile).containsStatus(FileInformation.Status.NOTVERSIONED_EXCLUDED));
    }

    public void testCopyFileToIgnoredFolder_FO () throws Exception {
        // prepare
        VCSFileProxy ignored = createFolder(repositoryLocation, "ignoredFolder");
        getClient(repositoryLocation).ignore(new VCSFileProxy[] { ignored }, GitUtils.NULL_PROGRESS_MONITOR);
        VCSFileProxy toFolder = createFolder(ignored, "toFolder");
        VCSFileProxy fromFile = createFile(repositoryLocation, "file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        add(fromFile);
        commit(fromFile);
        getCache().refreshAllRoots(ignored);
        
        // copy
        copyFO(fromFile, toFile);
        
        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());
        getCache().refreshAllRoots(fromFile, toFile);
        assertTrue(getCache().getStatus(fromFile).containsStatus(FileInformation.Status.UPTODATE));
        assertTrue(getCache().getStatus(toFile).containsStatus(FileInformation.Status.NOTVERSIONED_EXCLUDED));
    }
    
    public void testIsModifiedAttributeFile () throws Exception {
        // file is outside of versioned space, attribute should be unknown
        VCSFileProxy file = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(getWorkDir()), "file");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.normalizeFile().toFileObject();
        String attributeModified = "ProvidedExtensions.VCSIsModified";
        
        Object attrValue = fo.getAttribute(attributeModified);
        assertNull(attrValue);
        
        // file inside a git repo
        file = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        write(file, "init");
        fo = file.toFileObject();
        // new file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        add();
        // added file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        commit();
        
        // unmodified file, returns FALSE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
        
        write(file, "modification");
        // modified file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        write(file, "init");
        // back to up to date
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
    }

    private void renameDO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(from.toFileObject());
        daoFrom.rename(to.getName());
    }

    private void renameFO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        // ensure parent is known by filesystems
        // otherwise no event will be thrown
        FileObject parent = from.getParentFile().toFileObject();
        FileObject foFrom = from.toFileObject();
        FileLock lock = foFrom.lock();
        try {
            foFrom.rename(lock, to.getName(), null);
        } finally {
            lock.releaseLock();
        }
    }

    private void moveDO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(from.toFileObject());
        DataObject daoTarget = DataObject.find(to.getParentFile().toFileObject());
        daoFrom.move((DataFolder) daoTarget);
    }

    private void copyDO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(from.toFileObject());
        DataObject daoTarget = DataObject.find(to.getParentFile().toFileObject());
        daoFrom.copy((DataFolder) daoTarget);
    }

    private void moveFO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        FileObject foFrom = from.toFileObject();
        assertNotNull(foFrom);
        FileObject foTarget = to.getParentFile().toFileObject();
        assertNotNull(foTarget);
        FileLock lock = foFrom.lock();
        try {
            foFrom.move(lock, foTarget, to.getName(), null);
        } finally {
            lock.releaseLock();
        }
    }

    private void copyFO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        FileObject foFrom = from.toFileObject();
        assertNotNull(foFrom);
        FileObject foTarget = to.getParentFile().toFileObject();
        assertNotNull(foTarget);
        FileLock lock = foFrom.lock();
        try {
            foFrom.copy(foTarget, getName(to), getExt(to));
        } finally {
            lock.releaseLock();
        }
    }

    private void delete(VCSFileProxy file) throws IOException {
        DataObject dao = DataObject.find(file.toFileObject());
        dao.delete();
    }
    
    private void deleteFO (VCSFileProxy toDelete) throws DataObjectNotFoundException, IOException {
        FileObject fo = toDelete.toFileObject();
        assertNotNull(fo);
        FileLock lock = fo.lock();
        try {
            fo.delete(lock);
        } finally {
            lock.releaseLock();
        }
    }

    private String getName(VCSFileProxy f) {
        String ret = f.getName();
        int idx = ret.lastIndexOf(".");
        return idx > -1 ? ret.substring(0, idx) : ret;
    }

    private String getExt(VCSFileProxy f) {
        String ret = f.getName();
        int idx = ret.lastIndexOf(".");
        return idx > -1 ? ret.substring(idx) : null;
    }
}
