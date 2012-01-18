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


import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import org.netbeans.modules.versioning.core.DelegatingVCS;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VersioningSystem;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.util.Lookup;
import org.netbeans.modules.versioning.spi.testvcs.TestVCS;
import org.netbeans.modules.versioning.spi.testvcs.TestVCSInterceptor;
import org.openide.filesystems.FileChangeAdapter;

/**
 * Versioning SPI unit tests of VCSInterceptor.
 * 
 * @author Maros Sandor
 */
public class VCSInterceptorTestCase extends AbstractFSTestCase {
    
    private TestVCSInterceptor inteceptor;

    public VCSInterceptorTestCase(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Collection<? extends VCSSystemProvider> providers = Lookup.getDefault().lookupAll(VCSSystemProvider.class);
        for (VCSSystemProvider p : providers) {
            Collection<VCSSystemProvider.VersioningSystem> systems = p.getVersioningSystems();
            for (VCSSystemProvider.VersioningSystem vs : systems) {
                if(vs instanceof DelegatingVCS) {
                    DelegatingVCS dvcs = (DelegatingVCS)vs;
                    if("TestVCSDisplay".equals(dvcs.getDisplayName())) {
                        inteceptor = (TestVCSInterceptor) dvcs.getInterceptor();
                    }
                }
            }
        }
        inteceptor.clearTestData();
    }

    public void testIsMutable() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createData("checkme.txt");
        fo.canWrite();
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        
        assertTrue(inteceptor.getBeforeCreateFiles().contains(proxy));
        assertTrue(inteceptor.getDoCreateFiles().contains(proxy));
        assertTrue(inteceptor.getCreatedFiles().contains(proxy));
        assertFalse(inteceptor.getIsMutableFiles().contains(proxy));
        
        VCSFilesystemTestFactory.getInstance(this).setReadOnly(fo);
        assertFalse(fo.canWrite());
        assertTrue(inteceptor.getIsMutableFiles().contains(proxy));
    }

    public void testGetAttribute() throws IOException {
        FileObject folder = getVersionedFolder();
        FileObject fo = folder.createData("gotattr.txt");
        
        String attr = (String) fo.getAttribute("whatever");
        assertNull(attr);

        attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertTrue(attr.endsWith("gotattr.txt"));


        attr = (String) fo.getAttribute("whatever");
        assertNull(attr);

        fo = folder.createData("versioned.txt");
        Boolean battr = (Boolean) fo.getAttribute("ProvidedExtensions.VCSManaged");
        assertNotNull(battr);
        assertTrue(battr);
    }

    public void testRefreshRecursively() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createFolder("folder");
        fo.addRecursiveListener(new FileChangeAdapter());
        assertTrue(inteceptor.getRefreshRecursivelyFiles().contains(VCSFileProxy.createFileProxy(fo)));     
    }

    public void testChangedFile() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createData("deleteme.txt");
        
        OutputStream os = fo.getOutputStream();
        os.close();
        
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        assertTrue(inteceptor.getBeforeCreateFiles().contains(proxy));
        assertTrue(inteceptor.getDoCreateFiles().contains(proxy));
        assertTrue(inteceptor.getCreatedFiles().contains(proxy));
        assertTrue(inteceptor.getBeforeChangeFiles().contains(proxy));
        assertTrue(inteceptor.getAfterChangeFiles().contains(proxy));
    }
    
    public void testFileProtectedAndNotDeleted() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createData("deleteme.txt-do-not-delete");
        fo.delete();
        
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        assertTrue(proxy.isFile());
        assertTrue(inteceptor.getBeforeCreateFiles().contains(proxy));
        assertTrue(inteceptor.getDoCreateFiles().contains(proxy));
        assertTrue(inteceptor.getCreatedFiles().contains(proxy));
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(proxy));
        assertTrue(inteceptor.getDoDeleteFiles().contains(proxy));
        assertTrue(inteceptor.getDeletedFiles().contains(proxy));
    }

    public void testFileCreatedLockedRenamedDeleted() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createData("deleteme.txt");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        FileLock lock = fo.lock();
        fo.rename(lock, "deleteme", "now");
        lock.releaseLock();
        VCSFileProxy proxy2 = VCSFileProxy.createFileProxy(fo);
        fo.delete();
        assertTrue(inteceptor.getBeforeCreateFiles().contains(proxy));
        assertTrue(inteceptor.getDoCreateFiles().contains(proxy));
        assertTrue(inteceptor.getCreatedFiles().contains(proxy));
        assertTrue(inteceptor.getBeforeEditFiles().contains(proxy));
        assertTrue(inteceptor.getBeforeMoveFiles().contains(proxy));
        assertTrue(inteceptor.getAfterMoveFiles().contains(proxy));
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(proxy2));
        assertTrue(inteceptor.getDoDeleteFiles().contains(proxy2));
        assertTrue(inteceptor.getDeletedFiles().contains(proxy2));
    }

    public void testFileCopied() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createData("copyme.txt");

        FileObject fto = fo.copy(fo.getParent(), "copymeto", "txt");
        VCSFileProxy fromProxy = VCSFileProxy.createFileProxy(fo);
        VCSFileProxy toProxy = VCSFileProxy.createFileProxy(fto);

        assertTrue(inteceptor.getBeforeCopyFiles().contains(fromProxy));
        assertTrue(inteceptor.getBeforeCopyFiles().contains(toProxy));
        assertTrue(inteceptor.getDoCopyFiles().contains(fromProxy));
        assertTrue(inteceptor.getDoCopyFiles().contains(toProxy));
        assertTrue(inteceptor.getAfterCopyFiles().contains(fromProxy));
        assertTrue(inteceptor.getAfterCopyFiles().contains(toProxy));
    }

    public void testDeleteRecursively() throws IOException {
        FileObject versionedRoot = getVersionedFolder();
        FileObject deleteFolder = versionedRoot.createFolder("deletefolder");
        FileObject deepestFolder= deleteFolder.createFolder("folder1").createFolder("folder2").createFolder("folder3");
        FileObject fo = deepestFolder.createData("file");
        fo = deepestFolder.getParent().createData("file");
        fo = deepestFolder.getParent().getParent().createData("file");
        fo = deepestFolder.getParent().getParent().getParent().createData("file");
        
        deleteFolder.delete();
        
        assertTrue(inteceptor.getDeletedFiles().contains(VCSFileProxy.createFileProxy(deleteFolder)));
        assertFalse(deleteFolder.isValid());
    }
}
