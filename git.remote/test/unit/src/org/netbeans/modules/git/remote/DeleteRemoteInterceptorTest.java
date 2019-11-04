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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;

/**
 *
 */
@ClassForAllEnvironments(section = "remote.svn")
public class DeleteRemoteInterceptorTest extends AbstractRemoteGitTestCase {

    public static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";

    public DeleteRemoteInterceptorTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
   
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteUnversionedFile");
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteVersionedFile");
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteVersionedFileExternally");
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteVersionedFolder");
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteVersionedFolder_NoMetadata");
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteVersionedFolder_NoMetadata_FO");
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteNotVersionedFolder");
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteRepositoryLocationRoot");
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteVersionedFileTree");
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteNotVersionedFileTree");
        addTest(suite, DeleteRemoteInterceptorTest.class, "createNewFile");
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteA_CreateA");
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteA_CreateA_RunAtomic");
        addTest(suite, DeleteRemoteInterceptorTest.class, "deleteA_RenameB2A_DO_129805");
        return(suite);
    }

    @Override
    protected boolean isFailed() {
        return Arrays.asList().contains(testName);
    }

    @Override
    protected boolean isRunAll() {return false;}

    public void deleteUnversionedFile () throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(file);
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());

        // delete
        refreshHandler.setFilesToRefresh(Collections.singleton(file));
        delete(file);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(file.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
    }

    public void deleteVersionedFile() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(file);
        add(file);
        commit(repositoryLocation);
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());

        // delete
        refreshHandler.setFilesToRefresh(Collections.singleton(file));
        delete(file);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(file.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        commit(repositoryLocation);
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
    }

    public void deleteVersionedFileExternally() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        refreshHandler.setFilesToRefresh(Collections.singleton(file));
        repositoryLocation.toFileObject().createData(file.getName());
        assertTrue(refreshHandler.waitForFilesToRefresh());
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
if(false){        
        refreshHandler.setFilesToRefresh(Collections.singleton(file));
        VersioningSupport.refreshFor(new VCSFileProxy[]{file});
        assertTrue(refreshHandler.waitForFilesToRefresh());}
else    getCache().refreshAllRoots(Collections.singleton(file));
// Remote file system does not support externaly deleted file.
// If we try to delete externaly, the file will exists in remote FS.
// If we try ro delete by VCSFileProxySupport, the file will be deleted from index
if(false)assertEquals(EnumSet.of(Status.REMOVED_INDEX_WORKING_TREE, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
else    assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        delete(true, file);
        commit(repositoryLocation);
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
    }

    public void deleteVersionedFolder() throws Exception {
        if (skipTest()) {
            return;
        }
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
        refreshHandler.setFilesToRefresh(Collections.singleton(folder));
        delete(folder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(folder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
    }
    
    public void deleteVersionedFolder_NoMetadata() throws Exception {
        if (skipTest()) {
            return;
        }
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
    
    public void deleteVersionedFolder_NoMetadata_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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

    public void deleteNotVersionedFolder() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(repositoryLocation, "folder2");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy file = VCSFileProxy.createFileProxy(folder, "file");
        VCSFileProxySupport.createNew(file);

        // delete
        refreshHandler.setFilesToRefresh(Collections.singleton(folder));
        delete(folder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(folder.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
    }

    public void deleteRepositoryLocationRoot() throws Exception {
        if (skipTest()) {
            return;
        }
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

    public void deleteVersionedFileTree() throws Exception {
        if (skipTest()) {
            return;
        }
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
        refreshHandler.setFilesToRefresh(Collections.singleton(folder));
        delete(folder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file11).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file12).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file21).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file22).getStatus());
    }

    public void deleteNotVersionedFileTree() throws Exception {
        if (skipTest()) {
            return;
        }
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
        refreshHandler.setFilesToRefresh(Collections.singleton(folder));
        delete(folder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

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

    public void createNewFile() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "file");

        refreshHandler.setFilesToRefresh(Collections.singleton(file));
        // create
        FileObject fo = repositoryLocation.toFileObject();
        fo.createData(file.getName());
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(file.exists());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
    }

    public void deleteA_CreateA() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());

        // delete
        refreshHandler.setFilesToRefresh(Collections.singleton(fileA));
        FileObject fo = fileA.toFileObject();
        fo.delete();
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test if deleted
        assertFalse(fileA.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());

        // create
        refreshHandler.setFilesToRefresh(Collections.singleton(fileA));
        fo.getParent().createData(fo.getName());
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
    }

    public void deleteA_CreateA_RunAtomic() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        final VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        refreshHandler.setFilesToRefresh(Collections.singleton(fileA));
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertTrue(refreshHandler.waitForFilesToRefresh());

        final FileObject fo = fileA.toFileObject();
        AtomicAction a = new AtomicAction() {
            @Override
            public void run() throws IOException {
                fo.delete();
                fo.getParent().createData(fo.getName());
            }
        };
        refreshHandler.setFilesToRefresh(Collections.singleton(fileA));
        fo.getFileSystem().runAtomicAction(a);
        assertTrue(refreshHandler.waitForFilesToRefresh());
if(false);
else    getCache().refreshAllRoots(fileA);
        // test
        assertTrue(fileA.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
    }

    public void deleteA_RenameB2A_DO_129805() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        VCSFileProxySupport.createNew(fileB);
        add();
        commit();

        // delete A
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        delete(fileA);
        // rename B to A
        renameDO(fileB, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fileB.exists());
        assertTrue(fileA.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
    }
}
