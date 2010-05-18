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

package org.netbeans.modules.mercurial;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class InterceptorTest extends AbstractHgTest {

    public InterceptorTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // create
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        System.setProperty("netbeans.user", "/tmp/hgtest");
    }

    public void testGetAttributeRefreh() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        FileObject fo = FileUtil.toFileObject(file);
        Runnable attr = (Runnable) fo.getAttribute("ProvidedExtensions.Refresh");
        assertNotNull(attr);

        attr.run();
        // XXX check status
    }

    public void testGetAttributeWrong() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        FileObject fo = FileUtil.toFileObject(file);
        String attr = (String) fo.getAttribute("peek-a-boo");
        assertNull(attr);
    }

    public void testGetAttributeNotCloned() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        FileObject fo = FileUtil.toFileObject(file);
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNull(attr);
    }

    public void testGetAttributeClonedRoot() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkDir());

        FileObject fo = FileUtil.toFileObject(cloned);
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(getWorkDir().getAbsolutePath(), attr);
    }

    public void testGetAttributeCloned() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkDir());

        FileObject fo = FileUtil.toFileObject(new File(new File(cloned, folder.getName()), file.getName()));
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(getWorkDir().getAbsolutePath(), attr);
    }

    public void testGetAttributeClonedOnlyPush() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkDir());

        String defaultPush = "http://a.repository.far.far/away";
        new HgConfigFiles(cloned).removeProperty(HgConfigFiles.HG_PATHS_SECTION, HgConfigFiles.HG_DEFAULT_PULL);
        new HgConfigFiles(cloned).removeProperty(HgConfigFiles.HG_PATHS_SECTION, HgConfigFiles.HG_DEFAULT_PULL_VALUE);
        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PUSH, defaultPush);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = FileUtil.toFileObject(new File(new File(cloned, folder.getName()), file.getName()));
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPush, attr);
    }

    public void testGetAttributeClonedPull() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkDir());

        String defaultPull = "http://a.repository.far.far/away";
        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PULL, defaultPull);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = FileUtil.toFileObject(new File(new File(cloned, folder.getName()), file.getName()));
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPull, attr);
    }

    public void testGetAttributeClonedPullWithCredentials() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkDir());

        String defaultPull = "http://so:secure@a.repository.far.far/away";
        String defaultPullReturned = "http://a.repository.far.far/away";

        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PULL, defaultPull);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = FileUtil.toFileObject(new File(new File(cloned, folder.getName()), file.getName()));
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPullReturned, attr);
    }

    public void testFullScanLimitedOnVisibleRoots () throws Exception {
        File repo = new File("/tmp/", String.valueOf(System.currentTimeMillis()));
        repo.mkdir();
        File folderA = new File(repo, "folderA");
        File fileA1 = new File(folderA, "file1");
        File fileA2 = new File(folderA, "file2");
        folderA.mkdirs();
        fileA1.createNewFile();
        fileA2.createNewFile();
        File folderB = new File(repo, "folderB");
        File fileB1 = new File(folderB, "file1");
        File fileB2 = new File(folderB, "file2");
        folderB.mkdirs();
        fileB1.createNewFile();
        fileB2.createNewFile();
        File folderC = new File(repo, "folderC");
        File fileC1 = new File(folderC, "file1");
        File fileC2 = new File(folderC, "file2");
        folderC.mkdirs();
        fileC1.createNewFile();
        fileC2.createNewFile();

        HgCommand.doCreate(repo, NULL_LOGGER);

        MercurialInterceptor interceptor = Mercurial.getInstance().getMercurialInterceptor();
        Field f = MercurialInterceptor.class.getDeclaredField("hgFolderEventsHandler");
        f.setAccessible(true);
        Object hgFolderEventsHandler = f.get(interceptor);
        f = hgFolderEventsHandler.getClass().getDeclaredField("seenRoots");
        f.setAccessible(true);
        HashMap<File, Set<File>> map = (HashMap) f.get(hgFolderEventsHandler);

        getCache().getCachedStatus(folderA);
        // some time for bg threads
        Thread.sleep(3000);
        Set<File> files = map.get(repo);
        assertTrue(1 == files.size());
        assertTrue(files.contains(folderA));

        getCache().getCachedStatus(fileB1);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(2 == files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));

        getCache().getCachedStatus(fileB2);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(2 == files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));

        getCache().getCachedStatus(folderC);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(3 == files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));
        assertTrue(files.contains(folderC));

        getCache().getCachedStatus(repo);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(1 == files.size());
        assertTrue(files.contains(repo));

        Utils.deleteRecursively(repo);
    }

}
