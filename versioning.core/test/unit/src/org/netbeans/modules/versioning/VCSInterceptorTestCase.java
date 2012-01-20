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
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.modules.versioning.core.DelegatingVCS;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;

import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.versioning.spi.testvcs.TestVCSInterceptor;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileLock;

/**
 * Versioning SPI unit tests of VCSInterceptor.
 * 
 * @author Maros Sandor
 */
public class VCSInterceptorTestCase extends AbstractFSTestCase {
    
    private TestVCSInterceptor inteceptor;
    private FSInterceptorLogHandler logHandler;

    private static final String getAttributeFormat = "getAttribute {0}, {1}";
    private static final String canWriteFormat = "canWrite {0}";
    private static final String fileLockedFormat = "fileLocked {0}";
    private static final String beforeChangedFormat = "beforeChange {0}";
    private static final String fileChangedFormat = "fileChanged {0}";
    private static final String beforeCreateFormat = "beforeCreate {0}, {1}, {2}";
    private static final String createdFormat = "createSuccess {0}";
    private static final String createdExternalyFormat = "createdExternally {0}";
    private static final String beforeDeleteFormat = "getDeleteHandler {0}";
    private static final String deleteHandleFormat = "delete handle {0}";
    private static final String deletedExternalyFormat = "deletedExternally {0}";
    private static final String deleteSuccessFormat = "deleteSuccess {0}";
    
    static String[] formats = new String[] {
        getAttributeFormat,
        fileLockedFormat,
        beforeChangedFormat,
        fileChangedFormat,
        beforeDeleteFormat,
        deleteHandleFormat,
        deletedExternalyFormat,
        deleteSuccessFormat,
        beforeCreateFormat,
        createdFormat,
        canWriteFormat,
        createdExternalyFormat
    };
    
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
        logHandler = new FSInterceptorLogHandler();
        VersioningManager.LOG.addHandler(logHandler);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testFileCreateVersioned() throws IOException {
        FileObject fo = getVersionedFolder();
        logHandler.messages.clear();
                
        fo = fo.createData("checkme.txt");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        
        assertTrue(inteceptor.getBeforeCreateFiles().contains(proxy));
        assertTrue(inteceptor.getDoCreateFiles().contains(proxy));
        assertTrue(inteceptor.getCreatedFiles().contains(proxy));
        
        assertEquals(2, logHandler.messages.size());
        logHandler.assertEvent(beforeCreateFormat, true, proxy.getParentFile(), proxy.getName(), false);
        logHandler.assertEvent(createdFormat, true, proxy);
    }
    
    public void testFileCreateNotVersioned() throws IOException {
        FileObject fo = getNotVersionedFolder();
        logHandler.messages.clear();
                
        fo = fo.createData("checkme.txt");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        
        assertFalse(inteceptor.getBeforeCreateFiles().contains(proxy));
        assertFalse(inteceptor.getDoCreateFiles().contains(proxy));
        assertFalse(inteceptor.getCreatedFiles().contains(proxy));
        
        assertEquals(2, logHandler.messages.size());
        logHandler.assertEvent(beforeCreateFormat, true, proxy.getParentFile(), proxy.getName(), false);
        logHandler.assertEvent(createdFormat, true, proxy);
    }
    
    public void testIsMutable() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createData("checkme.txt");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        logHandler.messages.clear();
        
        assertTrue(fo.canWrite());
        assertEquals(0, logHandler.messages.size());
        
        assertFalse(inteceptor.getIsMutableFiles().contains(proxy));
        
        VCSFilesystemTestFactory.getInstance(this).setReadOnly(getRelativePath(proxy));
        assertFalse(fo.canWrite());
        assertTrue(inteceptor.getIsMutableFiles().contains(proxy));
        
        assertEquals(1, logHandler.messages.size());
        logHandler.assertEvent(canWriteFormat, true, proxy);
    }

    public void testGetAttribute() throws IOException {
        FileObject folder = getVersionedFolder();
        FileObject fo = folder.createData("gotattr.txt");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        
        logHandler.messages.clear();
        String attr = (String) fo.getAttribute("whatever");
        assertNull(attr);

        assertEquals(0, logHandler.messages.size());
        logHandler.assertEvent(getAttributeFormat, false, proxy, "whatever");
        
        logHandler.messages.clear();
        attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertTrue(attr.endsWith("gotattr.txt"));
        assertEquals(1, logHandler.messages.size());
        logHandler.assertEvent(getAttributeFormat, true, proxy, "ProvidedExtensions.RemoteLocation");
        
        fo = folder.createData("versioned.txt");
        proxy = VCSFileProxy.createFileProxy(fo);
        logHandler.messages.clear();
        Boolean battr = (Boolean) fo.getAttribute("ProvidedExtensions.VCSManaged");
        assertNotNull(battr);
        assertTrue(battr);
        assertEquals(1, logHandler.messages.size());
        logHandler.assertEvent(getAttributeFormat, true, proxy, "ProvidedExtensions.VCSManaged");
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
    
    public void testModifyFileOnDemand() throws Exception {
        // init
        inteceptor.deleteHandler = deleteHandler;
        FileObject fo = getVersionedFolder().createData("file");
        logHandler.messages.clear();
        
        // modify
        OutputStream os = fo.getOutputStream();
        os.write(new byte[] { 'a', 0 });
        os.close();
        
        // test
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        assertTrue(inteceptor.getBeforeEditFiles().contains(proxy));
        assertTrue(inteceptor.getBeforeChangeFiles().contains(proxy));
        assertTrue(inteceptor.getAfterChangeFiles().contains(proxy));
        
        assertEquals(3, logHandler.messages.size());
        logHandler.assertEvent(fileLockedFormat, true, proxy);
        logHandler.assertEvent(beforeChangedFormat, true, proxy);
        logHandler.assertEvent(fileChangedFormat, true, proxy);
    }

    public void testDeleteNotVersionedFile() throws Exception {
        // init     
        inteceptor.deleteHandler = deleteHandler;
        FileObject fo = getNotVersionedFolder().createData("file");
        logHandler.messages.clear();
        
        // delete
        fo.delete();
        
        // test
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        assertFalse(fo.isValid());
        assertFalse(inteceptor.getBeforeDeleteFiles().contains(proxy));
        assertFalse(inteceptor.getDoDeleteFiles().contains(proxy));
        assertFalse(inteceptor.getDeletedFiles().contains(proxy));
        
        assertEquals(3, logHandler.messages.size());
        logHandler.assertEvent(fileLockedFormat, true, proxy);
        logHandler.assertEvent(beforeDeleteFormat, true, proxy);
        logHandler.assertEvent(deleteSuccessFormat, true, proxy);
    }
    
    public void testDeleteVersionedFile() throws Exception {
        // init     
        inteceptor.deleteHandler = deleteHandler;
        FileObject fo = getVersionedFolder().createData("file");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        logHandler.messages.clear();

        // delete
        fo.delete();
        // test
        assertFalse(fo.isValid());
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(proxy));
        assertTrue(inteceptor.getDoDeleteFiles().contains(proxy));
        assertTrue(inteceptor.getDeletedFiles().contains(proxy));
        
        assertEquals(4, logHandler.messages.size());
        logHandler.assertEvent(fileLockedFormat, true, proxy);
        logHandler.assertEvent(beforeDeleteFormat, true, proxy);
        logHandler.assertEvent(deleteHandleFormat, true, proxy);
        logHandler.assertEvent(deleteSuccessFormat, true, proxy);
    }

    public void testDeleteVersionedFolder() throws Exception {
        // init       
        inteceptor.deleteHandler = deleteHandler;
        FileObject fo = getVersionedFolder().createFolder("folder");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        logHandler.messages.clear();

        // delete
        fo.delete();

        // test
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(proxy));
        assertTrue(inteceptor.getDoDeleteFiles().contains(proxy));
        assertTrue(inteceptor.getDeletedFiles().contains(proxy));
        
        assertEquals(3, logHandler.messages.size());
        logHandler.assertEvent(beforeDeleteFormat, true, proxy);
        logHandler.assertEvent(deleteHandleFormat, true, proxy);
        logHandler.assertEvent(deleteSuccessFormat, true, proxy);
    }

    public void testDeleteNotVersionedFolder() throws IOException {
        // init        
        inteceptor.deleteHandler = deleteHandler;
        FileObject fo = getNotVersionedFolder().createFolder("folder");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        logHandler.messages.clear();
        
        // delete
        fo.delete();
        
        // test
        assertFalse(inteceptor.getBeforeDeleteFiles().contains(proxy));
        assertFalse(inteceptor.getDoDeleteFiles().contains(proxy));
        assertFalse(inteceptor.getDeletedFiles().contains(proxy));
        
        assertEquals(2, logHandler.messages.size());
        logHandler.assertEvent(beforeDeleteFormat, true, proxy);
        logHandler.assertEvent(deleteSuccessFormat, true, proxy);
    }    

    public void testDeleteVersionedFileTree() throws IOException {
        inteceptor.deleteHandler = deleteHandler;
        FileObject versionedRoot = getVersionedFolder();
        FileObject deleteFolder = versionedRoot.createFolder("deletefolder");
        VCSFileProxy deleteProxy = VCSFileProxy.createFileProxy(deleteFolder);
        
        deleteFolderTree(deleteFolder, deleteProxy);
        
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(deleteProxy));
        assertTrue(inteceptor.getDoDeleteFiles().contains(deleteProxy));
        assertTrue(inteceptor.getDeletedFiles().contains(deleteProxy));
        
        assertEquals(3, logHandler.messages.size());
        logHandler.assertEvent(beforeDeleteFormat, true, deleteProxy);
        logHandler.assertEvent(deleteHandleFormat, true, deleteProxy);
        logHandler.assertEvent(deleteSuccessFormat, true, deleteProxy);
    }
    
    public void testDeleteNotVersionedFileTree() throws IOException {
        inteceptor.deleteHandler = deleteHandler;
        FileObject versionedRoot = getNotVersionedFolder();
        FileObject deleteFolder = versionedRoot.createFolder("deletefolder");
        VCSFileProxy deleteProxy = VCSFileProxy.createFileProxy(deleteFolder);
        
        deleteFolderTree(deleteFolder, deleteProxy);
        
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(deleteProxy));
        assertTrue(!inteceptor.getDoDeleteFiles().contains(deleteProxy));
        assertTrue(!inteceptor.getDeletedFiles().contains(deleteProxy));
        
        assertEquals(2, logHandler.messages.size());
        logHandler.assertEvent(beforeDeleteFormat, true, deleteProxy);
        logHandler.assertEvent(deleteSuccessFormat, true, deleteProxy);
    }

    private void deleteFolderTree(FileObject deleteFolder, final VCSFileProxy deleteProxy) throws IOException {
        FileObject folder1 = deleteFolder.createFolder("folder1");
        FileObject folder2 = folder1.createFolder("folder2");
        FileObject folder3 = folder2.createFolder("folder3");
        
        FileObject fo3 = folder3.createData("file");
        FileObject fo2 = folder2.createData("file");
        FileObject fo1 = folder1.createData("file");
        FileObject fo0 = deleteFolder.createData("file");
        logHandler.messages.clear();
        
        deleteFolder.delete();
        
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(folder1)));
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(folder2)));
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(folder3)));
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(fo1)));
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(fo2)));
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(fo3)));
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(fo0)));
        assertFalse(deleteFolder.isValid());
    }
    
    private String getRelativePath(VCSFileProxy proxy) throws IOException {
        String path = proxy.getPath();
        String rootPath = getRoot(path);
        path = path.substring(rootPath.length());
        if(path.startsWith("/")) {
            path = path.substring(1, path.length());
        }
        return path;
//        VCSFilesystemTestFactory factory = VCSFilesystemTestFactory.getInstance(VCSInterceptorTestCase.this);
//        String path = proxy.getPath();
//        path = path.substring(factory.getRootPath().length());
//        if(path.startsWith("/")) {
//            path = path.substring(1, path.length());
//        }
//        return path;
    }
    
    private TestVCSInterceptor.DeleteHandler deleteHandler = new TestVCSInterceptor.DeleteHandler() {
        @Override
        public void delete(VCSFileProxy proxy) throws IOException {
            VCSFilesystemTestFactory.getInstance(VCSInterceptorTestCase.this).delete(getRelativePath(proxy));
        }
    };
    
    private static class FSInterceptorLogHandler extends Handler {

        List<String> messages = new LinkedList<String>();

        @Override
        public void publish(LogRecord record) {
            String msg = record.getMessage();
            if(msg == null) return;
            for (String format : formats) {
                String f = format.substring(0, format.indexOf(" {0}")); 
                if(msg.startsWith(f)) {
                    messages.add(new MessageFormat(record.getMessage()).format(record.getParameters()).trim());
                    break;
                }
            }
        }

        @Override
        public void flush() {
            //
        }

        @Override
        public void close() throws SecurityException {
            //
        }

        private void assertEvent(String format, boolean bl, Object... proxies) {
            boolean contains = !messages.contains(new MessageFormat(format).format(proxies));
            if(contains && bl) {
                fail(new MessageFormat(format).format(proxies) + " should be intercepted but wasn't");
            } else if(!contains && !bl){
                fail(new MessageFormat(format).format(proxies) + " shouldn't be intercepted but was");
            }
        }
    }
    
}
