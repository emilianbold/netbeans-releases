/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.versioning;

import org.netbeans.modules.versioning.core.Utils;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.Field;
import java.security.Permission;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.spi.testvcs.TestVCS;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.spi.testvcs.TestAnnotatedVCS;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

public class GetOwnerTestCase extends NbTestCase {
    
    protected File dataRootDir;
    private StatFiles accessMonitor;
    private SecurityManager defaultSecurityManager;
    protected File versionedFolder;
    protected File unversionedFolder;

    public GetOwnerTestCase(String testName) {
        super(testName);
        accessMonitor = new StatFiles();
    }

    protected File getVersionedFolder() {
        if (versionedFolder == null) {
            versionedFolder = new File(dataRootDir, "workdir/root-" + TestAnnotatedVCS.VERSIONED_FOLDER_SUFFIX);
            versionedFolder.mkdirs();
            new File(versionedFolder, TestAnnotatedVCS.TEST_VCS_METADATA).mkdirs();
        }
        return versionedFolder;
    }
    
    protected File getUnversionedFolder() {
        if (unversionedFolder == null) {
            unversionedFolder = new File(dataRootDir, "workdir/unversioned/");
            unversionedFolder.mkdirs();
        }
        return unversionedFolder;
    }

    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances();
        dataRootDir = getWorkDir();
        File userdir = new File(getWorkDir(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        if(accessMonitor != null) {
            if(defaultSecurityManager == null) {
                defaultSecurityManager = System.getSecurityManager();
            }
            System.setSecurityManager(accessMonitor);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if(accessMonitor != null) {
            System.setSecurityManager(defaultSecurityManager);
        }
    }
     
    public void testVCSSystemDoesntAwakeOnUnrelatedGetOwner() throws IOException {
        
        assertNull(TestAnnotatedVCS.INSTANCE);
        
        File f = new File(getUnversionedFolder(), "sleepingfile");
        f.createNewFile();
        
        assertNull(TestAnnotatedVCS.INSTANCE);
        VersioningSystem owner = VersioningManager.getInstance().getOwner(toVCSFileProxy(f));
        assertNull(owner);
        
        assertNull(TestAnnotatedVCS.INSTANCE);
    }  

    public void testGetOwnerKnowFileType() throws IOException {
        assertTrue(VersioningSupport.getOwner(toVCSFileProxy(getVersionedFolder())).getClass() == getVCS());

        File f = new File(getVersionedFolder(), "file");
        f.createNewFile();
        testGetOwnerKnownFileType(toVCSFileProxy(f), true);
        
        f = new File(getVersionedFolder(), "folder");
        f.mkdirs();
        testGetOwnerKnownFileType(toVCSFileProxy(f), false);                         
    }

    private void testGetOwnerKnownFileType(VCSFileProxy proxy, boolean isFile) throws IOException {    
        accessMonitor.files.clear();
        VersioningSystem vs = VersioningManager.getInstance().getOwner(proxy, isFile); // true => its a file, no io.file.isFile() call needed
        assertNotNull(vs);
        
        // file wasn't accessed even on first shot
        assertFalse(accessMonitor.files.contains(proxy.getPath()));
        
        accessMonitor.files.clear();
        vs = VersioningManager.getInstance().getOwner(proxy, isFile);
        assertNotNull(vs);
        
        // file wasn't accessed
        assertFalse(accessMonitor.files.contains(proxy.getPath()));               
    }
    
    public void testGetOwnerVersioned() throws IOException {
        assertTrue(VersioningSupport.getOwner(toVCSFileProxy(getVersionedFolder())).getClass() == getVCS());
        File aRoot = new File(getVersionedFolder(), "a.txt");
        aRoot.createNewFile();
        VCSFileProxy rootProxy = toVCSFileProxy(aRoot);
        assertTrue(VersioningSupport.getOwner(rootProxy).getClass() ==  getVCS());
        
        aRoot = new File(getVersionedFolder(), "b-folder");
        aRoot.mkdirs();
        assertTrue(VersioningSupport.getOwner(rootProxy).getClass() ==  getVCS());
        aRoot = new File(aRoot, "deep-file");
        aRoot.createNewFile();
        assertTrue(VersioningSupport.getOwner(rootProxy).getClass() ==  getVCS());
        
        aRoot = new File(getVersionedFolder(), "nonexistent-file");
        assertTrue(VersioningSupport.getOwner(rootProxy).getClass() ==  getVCS());
    }
    
    public void testGetOwnerUnversioned() throws IOException {
        File aRoot = File.listRoots()[0];
        VCSFileProxy rootProxy = toVCSFileProxy(aRoot);
        assertNull(VersioningSupport.getOwner(rootProxy));
        aRoot = dataRootDir;
        assertNull(VersioningSupport.getOwner(rootProxy));
        aRoot = new File(dataRootDir, "workdir");
        assertNull(VersioningSupport.getOwner(rootProxy));               
        
        assertNull(VersioningSupport.getOwner(toVCSFileProxy(getUnversionedFolder())));        

        File f = new File(getUnversionedFolder(), "a.txt");
        f.createNewFile();
        assertNull(VersioningSupport.getOwner(toVCSFileProxy(f)));
        
        f = new File(getUnversionedFolder(), "notexistent.txt");
        assertNull(VersioningSupport.getOwner(toVCSFileProxy(f)));        
    }
    
    public void testFileOwnerCache() throws IOException {
        testFileOwnerCache(true /* versioned */ , false /* file */);
        testFileOwnerCache(false/* versioned */ , false /* file */);
    }
        
    public void testFolderOwnerCache() throws IOException {
        testFileOwnerCache(true /* unversioned */ , true /* folder */);
        testFileOwnerCache(false/* unversioned */ , true /* folder */);
    }

    public void testExcludedFolders () throws Exception {
        Field f = Utils.class.getDeclaredField("unversionedFolders");
        f.setAccessible(true);
        f.set(Utils.class, (File[]) null);

        File a = new File(getWorkDir(), "a");
        File b = new File(getWorkDir(), "b");
        System.setProperty("versioning.unversionedFolders", a.getAbsolutePath() + ";" + b.getAbsolutePath() + ";");
        File c = new File(getWorkDir(), "c");
        VersioningSupport.getPreferences().put("unversionedFolders", c.getAbsolutePath()); //NOI18N
        File userdir = new File(getWorkDir(), "userdir");
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        assertTrue(VersioningSupport.isExcluded(toVCSFileProxy(a)));
        assertTrue(VersioningSupport.isExcluded(toVCSFileProxy(b)));
        assertTrue(VersioningSupport.isExcluded(toVCSFileProxy(c)));
        assertTrue(VersioningSupport.isExcluded(toVCSFileProxy(userdir)));
        assertTrue(VersioningSupport.isExcluded(toVCSFileProxy(new File(userdir, "ffff"))));
        assertFalse(VersioningSupport.isExcluded(toVCSFileProxy(userdir.getParentFile())));

        assertEquals(4, ((VCSFileProxy[]) f.get(Utils.class)).length);

        // what if someone still wants to have userdir versioned?
        System.setProperty("versioning.netbeans.user.versioned", "true");
        
        f.set(Utils.class, (VCSFileProxy[]) null);
        
        assertTrue(VersioningSupport.isExcluded(toVCSFileProxy(a)));
        assertTrue(VersioningSupport.isExcluded(toVCSFileProxy(b)));
        assertTrue(VersioningSupport.isExcluded(toVCSFileProxy(c)));
        assertFalse(VersioningSupport.isExcluded(toVCSFileProxy(userdir)));
        assertFalse(VersioningSupport.isExcluded(toVCSFileProxy(new File(userdir, "ffff"))));
        assertFalse(VersioningSupport.isExcluded(toVCSFileProxy(userdir.getParentFile())));

        assertEquals(3, ((VCSFileProxy[]) f.get(Utils.class)).length);
    }

    private void testFileOwnerCache(boolean isVersioned, boolean isFolder) throws IOException {
        File folder = isVersioned ? getVersionedFolder() : getUnversionedFolder();
        File child = new File(folder, "file");
        File child2 = new File(folder, "file2");
        if(isFolder) {
            child.mkdirs();
            child2.mkdirs();
        } else {
            child.createNewFile();
            child2.createNewFile();
        }
        
        assertFileAccess(child, isVersioned, true /* access */);
        
        // try again - shouldn't be accessed anymore
        assertFileAccess(child, isVersioned, false /* no access */);        
        
        // try few more times some other file no file access expected
        assertFileAccess(child2, isVersioned, true /* access */);
        for (int i = 0; i < 100; i++) {
            // try some other file
            assertFileAccess(child2, isVersioned, false /* no access */);
        }        
        
        // try the first file again
        assertFileAccess(child, isVersioned, false /* no access */);        
    }
    
    private void assertFileAccess(File f, boolean versioned, boolean access) throws IOException {
        VCSFileProxy proxy = toVCSFileProxy(f);
        accessMonitor.files.clear();
        org.netbeans.modules.versioning.core.spi.VersioningSystem vs = VersioningSupport.getOwner(proxy);
        if(versioned && vs == null) {
            fail("no VersioningSystem returned for versioned file " + f);
        } else if(!versioned && vs != null) {
            fail("VersioningSystem returned for unversioned file " + f);
        }
        // file was accessed
        boolean accessed = accessMonitor.files.contains(f.getAbsolutePath());
//        if(access && !accessed) {
//            fail(f + " was not but should be accessed");
//        } else 
        if (!access && accessed) {
            fail(f + " was accessed but shouldn't");
        }
    }    

    private class StatFiles extends SecurityManager {
        private List<String> files = new LinkedList<String>();        
        @Override
        public void checkRead(String file) {
            files.add(file);
        }       
        @Override
        public void checkPermission(Permission perm) {
        }
    }    

    protected VCSFileProxy toVCSFileProxy(File file) throws IOException {
        return VCSTestFactory.getInstance(this).toVCSFileProxy(file);
    }

    private Class getVCS() {
        return TestAnnotatedVCS.class;
    }    
}
