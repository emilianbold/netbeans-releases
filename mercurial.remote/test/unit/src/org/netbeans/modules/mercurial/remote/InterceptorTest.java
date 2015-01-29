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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mercurial.remote;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.mercurial.remote.config.HgConfigFiles;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgRepositoryContextCache;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 *
 * @author tomas
 */
@ClassForAllEnvironments(section = "remote.svn")
public class InterceptorTest extends  RemoteVersioningTestBase {

    public InterceptorTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, InterceptorTest.class, "getAttributeRefreh");
        addTest(suite, InterceptorTest.class, "getAttributeWrong");
        addTest(suite, InterceptorTest.class, "getAttributeNotCloned");
        addTest(suite, InterceptorTest.class, "getAttributeClonedRoot");
        addTest(suite, InterceptorTest.class, "getAttributeCloned");
        addTest(suite, InterceptorTest.class, "getAttributeClonedOnlyPush");
        addTest(suite, InterceptorTest.class, "getAttributeClonedPull");
        addTest(suite, InterceptorTest.class, "getAttributeClonedPullWithCredentials");
        addTest(suite, InterceptorTest.class, "fullScanLimitedOnVisibleRoots");
        addTest(suite, InterceptorTest.class, "copyFile_SingleRepo_FO");
        addTest(suite, InterceptorTest.class, "copyFile_SingleRepo_DO");
        addTest(suite, InterceptorTest.class, "copyUnversionedFile_SingleRepo_FO");
        addTest(suite, InterceptorTest.class, "copyUnversionedFile_SingleRepo_DO");
        addTest(suite, InterceptorTest.class, "copyFolder_SingleRepo_FO");
        addTest(suite, InterceptorTest.class, "copyFolder_SingleRepo_DO");
        addTest(suite, InterceptorTest.class, "copyUnversionedFolder_SingleRepo_FO");
        addTest(suite, InterceptorTest.class, "copyUnversionedFolder_SingleRepo_DO");
        addTest(suite, InterceptorTest.class, "copyTree_SingleRepo_FO");
        addTest(suite, InterceptorTest.class, "copyTree_SingleRepo_DO");
        addTest(suite, InterceptorTest.class, "copyUnversionedTree_SingleRepo_FO");
        addTest(suite, InterceptorTest.class, "copyUnversionedTree_SingleRepo_DO");
        addTest(suite, InterceptorTest.class, "copyTree_TwoRepos_FO");
        addTest(suite, InterceptorTest.class, "copyTree_TwoRepos_DO");
        addTest(suite, InterceptorTest.class, "copyTree_UnversionedTarget_FO");
        addTest(suite, InterceptorTest.class, "copyTree_UnversionedTarget_DO");
        addTest(suite, InterceptorTest.class, "moveFileToIgnoredFolder_DO");
        addTest(suite, InterceptorTest.class, "moveFileToIgnoredFolder_FO");
        addTest(suite, InterceptorTest.class, "renameFileChangeCase_DO");
        addTest(suite, InterceptorTest.class, "renameFileChangeCase_FO");
        addTest(suite, InterceptorTest.class, "renameFolderChangeCase_DO");
        addTest(suite, InterceptorTest.class, "renameFolderChangeCase_FO");
        addTest(suite, InterceptorTest.class, "copyFileToIgnoredFolder_DO");
        addTest(suite, InterceptorTest.class, "copyFileToIgnoredFolder_FO");
        addTest(suite, InterceptorTest.class, "deleteFile_FO");
        addTest(suite, InterceptorTest.class, "deleteFileDO");
        addTest(suite, InterceptorTest.class, "deleteFolder_FO");
        addTest(suite, InterceptorTest.class, "deleteFolder_DO");
        addTest(suite, InterceptorTest.class, "isModifiedAttributeFile");
        return(suite);
    }
    
    public void getAttributeRefreh() throws HgException, IOException {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        FileObject fo = file.toFileObject();
        Runnable attr = (Runnable) fo.getAttribute("ProvidedExtensions.Refresh");
        assertNotNull(attr);

        attr.run();
        // XXX check status
    }

    public void getAttributeWrong() throws HgException, IOException {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        FileObject fo = file.toFileObject();
        String attr = (String) fo.getAttribute("peek-a-boo");
        assertNull(attr);
    }

    public void getAttributeNotCloned() throws HgException, IOException {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        FileObject fo = file.toFileObject();
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNull(attr);
    }

    public void getAttributeClonedRoot() throws HgException, IOException {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        VCSFileProxy cloned = clone(getWorkTreeDir());

        FileObject fo = cloned.toFileObject();
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(getWorkTreeDir().getPath(), attr);
    }

    public void getAttributeCloned() throws HgException, IOException {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        VCSFileProxy cloned = clone(getWorkTreeDir());

        FileObject fo = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(cloned, folder.getName()), file.getName()).toFileObject();
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(getWorkTreeDir().getPath(), attr);
    }

    public void getAttributeClonedOnlyPush() throws HgException, IOException {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        VCSFileProxy cloned = clone(getWorkTreeDir());

        String defaultPush = "http://a.repository.far.far/away";
        new HgConfigFiles(cloned).removeProperty(HgConfigFiles.HG_PATHS_SECTION, HgConfigFiles.HG_DEFAULT_PULL);
        new HgConfigFiles(cloned).removeProperty(HgConfigFiles.HG_PATHS_SECTION, HgConfigFiles.HG_DEFAULT_PULL_VALUE);
        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PUSH, defaultPush);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(cloned, folder.getName()), file.getName()).toFileObject();
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPush, attr);
    }

    public void getAttributeClonedPull() throws HgException, IOException {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        VCSFileProxy cloned = clone(getWorkTreeDir());

        String defaultPull = "http://a.repository.far.far/away";
        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PULL, defaultPull);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(cloned, folder.getName()), file.getName()).toFileObject();
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPull, attr);
    }

    public void getAttributeClonedPullWithCredentials() throws HgException, IOException {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        VCSFileProxy cloned = clone(getWorkTreeDir());

        String defaultPull = "http://so:secure@a.repository.far.far/away";
        String defaultPullReturned = "http://a.repository.far.far/away";

        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PULL, defaultPull);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(cloned, folder.getName()), file.getName()).toFileObject();
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPullReturned, attr);
    }

    public void fullScanLimitedOnVisibleRoots () throws Exception {
        VCSFileProxy repo = VCSFileProxy.createFileProxy(getWorkTreeDir().getParentFile(), String.valueOf(System.currentTimeMillis()));
        VCSFileProxySupport.mkdir(repo);
        VCSFileProxy folderA = VCSFileProxy.createFileProxy(repo, "folderA");
        VCSFileProxy fileA1 = VCSFileProxy.createFileProxy(folderA, "file1");
        VCSFileProxy fileA2 = VCSFileProxy.createFileProxy(folderA, "file2");
        VCSFileProxySupport.mkdirs(folderA);
        VCSFileProxySupport.createNew(fileA1);
        VCSFileProxySupport.createNew(fileA2);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repo, "folderB");
        VCSFileProxy fileB1 = VCSFileProxy.createFileProxy(folderB, "file1");
        VCSFileProxy fileB2 = VCSFileProxy.createFileProxy(folderB, "file2");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxySupport.createNew(fileB1);
        VCSFileProxySupport.createNew(fileB2);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repo, "folderC");
        VCSFileProxy fileC1 = VCSFileProxy.createFileProxy(folderC, "file1");
        VCSFileProxy fileC2 = VCSFileProxy.createFileProxy(folderC, "file2");
        VCSFileProxySupport.mkdirs(folderC);
        VCSFileProxySupport.createNew(fileC1);
        VCSFileProxySupport.createNew(fileC2);

        HgCommand.doCreate(repo, NULL_LOGGER);

        MercurialInterceptor interceptor = Mercurial.getInstance().getMercurialInterceptor();
        Field f = MercurialInterceptor.class.getDeclaredField("hgFolderEventsHandler");
        f.setAccessible(true);
        Object hgFolderEventsHandler = f.get(interceptor);
        f = hgFolderEventsHandler.getClass().getDeclaredField("seenRoots");
        f.setAccessible(true);
        HashMap<VCSFileProxy, Set<VCSFileProxy>> map = (HashMap) f.get(hgFolderEventsHandler);

        getCache().markAsSeenInUI(folderA);
        // some time for bg threads
        Thread.sleep(3000);
        Set<VCSFileProxy> files = map.get(repo);
        assertTrue(files.contains(folderA));

        getCache().markAsSeenInUI(fileB1);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));

        getCache().markAsSeenInUI(fileB2);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));

        getCache().markAsSeenInUI(folderC);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));
        assertTrue(files.contains(folderC));

        getCache().markAsSeenInUI(repo);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(files.contains(repo));

        VCSFileProxySupport.delete(repo);
    }

    public void copyFile_SingleRepo_FO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(folder, "copy");

        commit(folder);
        copyFO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file).getStatus());
        FileInformation st = getCachedStatus(copy, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());

        VCSFileProxy target = createFolder("target");
        copy =VCSFileProxy.createFileProxy(target, file.getName());
        copyFO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file).getStatus());
        st = getCachedStatus(copy, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
    }

    public void copyFile_SingleRepo_DO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");
        VCSFileProxy targetFolder = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(targetFolder, file.getName());

        commit(folder);
        copyDO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file).getStatus());
        FileInformation st = getCachedStatus(copy, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
    }

    public void copyUnversionedFile_SingleRepo_FO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy file = createFile(folder, "file");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(folder, "copy");

        copyFO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file).getStatus());
        FileInformation st = getCachedStatus(copy, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));

        VCSFileProxy target = createFolder("target");
        copy = VCSFileProxy.createFileProxy(target, file.getName());
        copyFO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file).getStatus());
        st = getCachedStatus(copy, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
    }

    public void copyUnversionedFile_SingleRepo_DO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy file = createFile(folder, "file");
        VCSFileProxy targetFolder = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(targetFolder, file.getName());

        copyDO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file).getStatus());
        FileInformation st = getCachedStatus(copy, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
    }

    public void copyFolder_SingleRepo_FO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(folder.getParentFile(), "copy");
        VCSFileProxy copiedFile1 = VCSFileProxy.createFileProxy(copy, file1.getName());
        VCSFileProxy copiedFile2 = VCSFileProxy.createFileProxy(copy, file2.getName());

        commit(folder);
        copyFO(folder,copy);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(copiedFile1.exists());
        assertTrue(copiedFile2.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file2).getStatus());
        FileInformation st = getCachedStatus(copiedFile1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
        st = getCachedStatus(copiedFile2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());


        VCSFileProxy target = createFolder("target");
        copy = VCSFileProxy.createFileProxy(target, folder.getName());
        copiedFile1 = VCSFileProxy.createFileProxy(copy, file1.getName());
        copiedFile2 = VCSFileProxy.createFileProxy(copy, file2.getName());
        copyFO(folder, copy);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(copiedFile1.exists());
        assertTrue(copiedFile2.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file2).getStatus());
        st = getCachedStatus(copiedFile1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
        st = getCachedStatus(copiedFile2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
    }

    public void copyFolder_SingleRepo_DO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy copiedFile1 = VCSFileProxy.createFileProxy(copy, file1.getName());
        VCSFileProxy copiedFile2 = VCSFileProxy.createFileProxy(copy, file2.getName());

        commit(folder);
        copyDO(folder,copy);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(copiedFile1.exists());
        assertTrue(copiedFile2.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file2).getStatus());
        FileInformation st = getCachedStatus(copiedFile1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
        st = getCachedStatus(copiedFile2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
    }

    public void copyUnversionedFolder_SingleRepo_FO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy copiedFile1 = VCSFileProxy.createFileProxy(copy, file1.getName());
        VCSFileProxy copiedFile2 = VCSFileProxy.createFileProxy(copy, file2.getName());

        copyFO(folder,copy);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(copiedFile1.exists());
        assertTrue(copiedFile2.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file2).getStatus());
        FileInformation st = getCachedStatus(copiedFile1, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
        st = getCachedStatus(copiedFile2, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
    }

    public void copyUnversionedFolder_SingleRepo_DO() throws Exception {
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy copiedFile1 = VCSFileProxy.createFileProxy(copy, file1.getName());
        VCSFileProxy copiedFile2 = VCSFileProxy.createFileProxy(copy, file2.getName());

        copyDO(folder,copy);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(copiedFile1.exists());
        assertTrue(copiedFile2.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file2).getStatus());
        FileInformation st = getCachedStatus(copiedFile1, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
        st = getCachedStatus(copiedFile2, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
    }

    public void copyTree_SingleRepo_FO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, "copy");
        VCSFileProxy[] copies = prepareTree(folder, copy);

        commit(folder);

        copyFO(folder, copy);
        for (VCSFileProxy f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
            assertTrue(st.getStatus(null).isCopied());
        }
    }

    public void copyTree_SingleRepo_DO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy[] copies = prepareTree(folder, copy);

        commit(folder);

        copyDO(folder, copy);
        for (VCSFileProxy f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
            assertTrue(st.getStatus(null).isCopied());
        }
    }

    public void copyUnversionedTree_SingleRepo_FO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy[] copies = prepareTree(folder, copy);

        copyFO(folder, copy);
        for (VCSFileProxy f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
            assertNull(st.getStatus(null));
        }
    }

    public void copyUnversionedTree_SingleRepo_DO() throws Exception {
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy[] copies = prepareTree(folder, copy);

        copyDO(folder, copy);
        for (VCSFileProxy f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
            assertNull(st.getStatus(null));
        }
    }

    public void copyTree_TwoRepos_FO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy target = createFolder("target");
        HgCommand.doCreate(target, NULL_LOGGER);
        Mercurial.getInstance().versionedFilesChanged();
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy[] copies = prepareTree(folder, copy);

        copyFO(folder, copy);
        for (VCSFileProxy f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
            assertNull(st.getStatus(null));
        }
    }

    public void copyTree_TwoRepos_DO() throws Exception {
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy target = createFolder("target");
        HgCommand.doCreate(target, NULL_LOGGER);
        Mercurial.getInstance().versionedFilesChanged();
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy[] copies = prepareTree(folder, copy);

        copyDO(folder, copy);
        for (VCSFileProxy f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
            assertNull(st.getStatus(null));
        }
    }

    public void copyTree_UnversionedTarget_FO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy target = VCSFileProxy.createFileProxy(getWorkTreeDir().getParentFile(), "mercurialtest_target_" + getName() + "_" + System.currentTimeMillis());
        VCSFileProxySupport.mkdirs(target);
        try {
            VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
            VCSFileProxy[] copies = prepareTree(folder, copy);

            copyFO(folder, copy);
            Mercurial.getInstance().versionedFilesChanged();
            for (VCSFileProxy f : copies) {
                assertTrue(f.exists());
                FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED);
                assertNull(st.getStatus(null));
                assertNull(Mercurial.getInstance().getRepositoryRoot(f));
            }
        } finally {
            // cleanup, temp folder is outside workdir
            VCSFileProxySupport.delete(target);
        }
    }

    public void copyTree_UnversionedTarget_DO() throws Exception {
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy target = VCSFileProxy.createFileProxy(getWorkTreeDir().getParentFile(), "mercurialtest_target_" + getName() + "_" + System.currentTimeMillis());
        VCSFileProxySupport.mkdirs(target);
        try {
            VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
            VCSFileProxy[] copies = prepareTree(folder, copy);

            copyDO(folder, copy);
            Mercurial.getInstance().versionedFilesChanged();
            for (VCSFileProxy f : copies) {
                assertTrue(f.exists());
                FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED);
                assertNull(st.getStatus(null));
                assertNull(Mercurial.getInstance().getRepositoryRoot(f));
            }
        } finally {
            // cleanup, temp folder is outside workdir
            VCSFileProxySupport.delete(target);
        }
    }
    
    public void moveFileToIgnoredFolder_DO () throws Exception {
        // prepare
        VCSFileProxy folder = createFolder("ignoredFolder");
        HgUtils.addIgnored(folder.getParentFile(), new VCSFileProxy[] { folder });
        VCSFileProxy toFolder = createFolder(folder, "toFolder");
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFile);
        
        // move
        moveDO(fromFile, toFile);
        
        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void moveFileToIgnoredFolder_FO () throws Exception {
        // prepare
        VCSFileProxy folder = createFolder("ignoredFolder");
        HgUtils.addIgnored(folder.getParentFile(), new VCSFileProxy[] { folder });
        VCSFileProxy toFolder = createFolder(folder, "toFolder");
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFile);
        
        // move
        moveFO(fromFile, toFile);
        
        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void renameFileChangeCase_DO () throws Exception {
        // prepare
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(getWorkTreeDir(), "FILE");
        commit(fromFile);
        
        // move
        renameDO(fromFile, toFile.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(isParentHasChild(toFile));
            assertFalse(isParentHasChild(fromFile));
        } else {
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void renameFileChangeCase_FO () throws Exception {
        // prepare
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(getWorkTreeDir(), "FILE");
        commit(fromFile);
        
        // move
        renameFO(fromFile, toFile.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(isParentHasChild(toFile));
            assertFalse(isParentHasChild(fromFile));
        } else {
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void renameFolderChangeCase_DO () throws Exception {
        // prepare
        VCSFileProxy fromFolder = createFolder("folder");
        VCSFileProxy fromFile = createFile(fromFolder, "file");
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(getWorkTreeDir(), "FOLDER");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFolder);
        
        // move
        renameDO(fromFolder, toFolder.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(isParentHasChild(toFolder));
            assertFalse(isParentHasChild(fromFolder));
        } else {
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void renameFolderChangeCase_FO () throws Exception {
        // prepare
        VCSFileProxy fromFolder = createFolder("folder");
        VCSFileProxy fromFile = createFile(fromFolder, "file");
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(getWorkTreeDir(), "FOLDER");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFolder);
        
        // move
        renameFO(fromFolder, toFolder.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(isParentHasChild(toFolder));
            assertFalse(isParentHasChild(fromFolder));
        } else {
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }

    public void copyFileToIgnoredFolder_DO () throws Exception {
        // prepare
        VCSFileProxy folder = createFolder("ignoredFolder");
        HgUtils.addIgnored(folder.getParentFile(), new VCSFileProxy[] { folder });
        VCSFileProxy toFolder = createFolder(folder, "toFolder");
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFile);
        
        // move
        copyDO(fromFile, toFile);
        
        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void copyFileToIgnoredFolder_FO () throws Exception {
        // prepare
        VCSFileProxy folder = createFolder("ignoredFolder");
        HgUtils.addIgnored(folder.getParentFile(), new VCSFileProxy[] { folder });
        VCSFileProxy toFolder = createFolder(folder, "toFolder");
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFile);
        
        // move
        copyFO(fromFile, toFile);
        
        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void deleteFile_FO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file1");
        commit(folder);
        
        deleteFO(file);
        assertFalse(file.exists());
        assertTrue(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file).getStatus());
    }
    
    public void deleteFileDO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file1");
        commit(folder);
        
        deleteDO(file);
        assertFalse(file.exists());
        assertTrue(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file).getStatus());
    }
    
    public void deleteFolder_FO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        commit(folder);
        
        deleteFO(folder);
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file2).getStatus());
    }
    
    public void deleteFolder_DO () throws Exception {
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        commit(folder);
        
        deleteDO(folder);
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file2).getStatus());
    }
    
    public void isModifiedAttributeFile () throws Exception {
        // file is outside of versioned space, attribute should be unknown
        VCSFileProxy file = VCSFileProxy.createFileProxy(getWorkTreeDir().getParentFile(), "testIsModifiedAttributeFile.txt");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.normalizeFile().toFileObject();
        String attributeModified = "ProvidedExtensions.VCSIsModified";
        
        Object attrValue = fo.getAttribute(attributeModified);
        assertNull(attrValue);
        
        // file inside a git repo
        file = VCSFileProxy.createFileProxy(getWorkTreeDir(), "file");
        write(file, "init");
        fo = file.normalizeFile().toFileObject();
        // new file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        HgCommand.doAdd(getWorkTreeDir(), file, NULL_LOGGER);
        // added file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        commit(file);
        
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

}
