/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.masterfs.providers;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Radek Matous
 */
public class ProvidedExtensionsTest extends NbTestCase {
    private ProvidedExtensionsImpl iListener;

    static {
        MockServices.setServices(AnnotationProviderImpl.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AnnotationProvider provider = (AnnotationProvider)Lookups.metaInfServices(
                Thread.currentThread().getContextClassLoader()).lookup(AnnotationProvider.class);
        assertNotNull(provider);
        iListener = lookupImpl();
        assertNotNull(iListener);
        clearWorkDir();
    }
    
    private ProvidedExtensionsImpl lookupImpl() {
        Lookup.Result result = Lookups.metaInfServices(Thread.currentThread().getContextClassLoader()).
                lookup(new Lookup.Template(AnnotationProvider.class));
        Collection all = result.allInstances();
        for (Iterator it = all.iterator(); it.hasNext();) {
            AnnotationProvider ap = (AnnotationProvider) it.next();
            InterceptionListener iil = ap.getInterceptionListener();
            if (iil instanceof ProvidedExtensions) {
                return (ProvidedExtensionsImpl)iil;
            }
        }
        return null;
    }
    
    public ProvidedExtensionsTest(String testName) {
        super(testName);
    }
    
    public void testImplsFileLock() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        FileObject toLock = fo.createData(getName());
        assertNotNull(toLock);
        assertEquals(0, iListener.implsFileLockCalls);
        FileLock fLock = toLock.lock();
        try {
            assertTrue(fLock.isValid());
            assertEquals(0, iListener.implsFileUnlockCalls);                        
            assertEquals(1, iListener.implsFileLockCalls);            
        } finally {
            fLock.releaseLock();
            assertEquals(1, iListener.implsFileUnlockCalls);                                    
        }        
    }
    
    public void testImplsBeforeChange() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        FileObject toChange = fo.createData("aa");
        assertNotNull(toChange);
        OutputStream os = toChange.getOutputStream();
        try {
            assertEquals(1, iListener.implsBeforeChangeCalls);            
        } finally {
            os.close();
        }
        
    }

    public void testImplsCanWrite() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        FileObject toChange = fo.createData("cw");
        assertNotNull(toChange);
        boolean cw = toChange.canWrite();
        assertEquals(1, iListener.implsCanWriteCalls);            
    }
    
    public void testImplsMove() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        FileObject toMove = fo.createData("aa");
        assertNotNull(toMove);
        FileObject whereToMove = fo.createFolder("aafolder");
        assertNotNull(whereToMove);
        
        iListener.clear();
        FileLock lock = toMove.lock();
        iListener.setLock(lock);
        try {
            assertEquals(0,iListener.implsMoveCalls);
            assertEquals(0,iListener.moveImplCalls);
            iListener.setImplsMoveRetVal(true);
            assertNotNull(toMove.move(lock, whereToMove, toMove.getName(), toMove.getExt()));
            assertEquals(1,iListener.implsMoveCalls);
            assertEquals(1,iListener.moveImplCalls);
        } finally {
            if (lock != null) {
                iListener.setLock(null);
                lock.releaseLock();
            }
        }
    }
    
    public void testImplsMove2() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        FileObject toMove = fo.createData("aa");
        assertNotNull(toMove);
        FileObject whereToMove = fo.createFolder("aafolder");
        assertNotNull(whereToMove);
        
        iListener.clear();
        FileLock lock = toMove.lock();
        iListener.setLock(lock);
        try {
            assertEquals(0,iListener.implsMoveCalls);
            assertEquals(0,iListener.moveImplCalls);
            iListener.setImplsMoveRetVal(false);
            assertNotNull(toMove.move(lock, whereToMove, toMove.getName(), toMove.getExt()));
            assertEquals(1,iListener.implsMoveCalls);
            assertEquals(0,iListener.moveImplCalls);
        } finally {
            if (lock != null) {
                iListener.setLock(null);
                lock.releaseLock();
            }
        }
    }
    
    public void testImplsRename() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        FileObject toRename = fo.createData("aa");
        assertNotNull(toRename);
        
        iListener.clear();
        FileLock lock = toRename.lock();
        iListener.setLock(lock);
        try {
            assertEquals(0,iListener.implsRenameCalls);
            assertEquals(0,iListener.renameImplCalls);
            iListener.setImplsRenameRetVal(true);
            assertTrue(toRename.isValid());
            assertNull(toRename.getParent().getFileObject(toRename.getExt(), toRename.getName()));
            toRename.rename(lock,toRename.getExt(), toRename.getName());
            assertEquals(1,iListener.implsRenameCalls);
            assertEquals(1,iListener.renameImplCalls);
        } finally {
            if (lock != null) {
                iListener.setLock(null);
                lock.releaseLock();
            }
        }
    }

    public void testAfterAtomicAction() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        fo.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileUtil.createData(new File(getWorkDir(),"a/b/c/d/e/f/g.txt"));
                assertEquals(0, iListener.implsCreateSuccessCalls);
            }            
        });
        assertEquals(7, iListener.implsCreateSuccessCalls);
    }
    
    public void testImplsRename2() throws IOException {
        final List events = new ArrayList();
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        final FileObject toRename = fo.createData(getName());
        assertNotNull(toRename);
        
        iListener.clear();
        FileLock lock = toRename.lock();
        iListener.setLock(lock);
        FileChangeListener fcl = null;
        try {
            final String origNameExt = toRename.getNameExt();
            final String origPath = toRename.getPath();
            final File origFile = FileUtil.toFile(toRename);

            iListener.setImplsRenameRetVal(true);
            fcl =  new FileChangeAdapter() {
                public void fileRenamed(FileRenameEvent fe)  {
                    events.add(fe);
                    assertFalse(fe.getFile().getNameExt().equals(origNameExt));
                    assertNull(toRename.getParent().getFileObject(origNameExt));
                    File f = FileUtil.toFile(toRename);
                    assertNotNull(f);
                    assertNotNull(FileUtil.toFileObject(f));
                    /* sometimes fails:
                        assertSame(toRename, FileUtil.toFileObject(f));
                    */
                    assertTrue(f.exists());
                    FileObject delegate = FileBasedFileSystem.getFileObject(f);
                    assertNotNull(delegate);
                    assertTrue(delegate.isValid());
                    assertNull(FileBasedFileSystem.getFileObject(origFile));
                    assertTrue(toRename.isValid());

                    assertSame(toRename, fe.getFile());
                    assertSame(toRename.getParent(), fe.getFile().getParent());
                    try {
                        assertNotNull(toRename.getFileSystem().findResource(toRename.getPath()));
                        assertNull(toRename.getFileSystem().findResource(origPath));

                    } catch (FileStateInvalidException ex) {
                        fail();
                    }

                    assertEquals("bb",toRename.getName());
                    assertEquals("ext",toRename.getExt());
                    assertFalse(origNameExt.equals(toRename.getNameExt()));

                    assertEquals("bb",fe.getFile().getName());
                    assertEquals("ext",fe.getFile().getExt());
                    assertFalse(origNameExt.equals(fe.getFile().getNameExt()));

                    assertFalse(fe.getName().equals(fe.getFile().getName()));
                    assertFalse(fe.getExt().equals(fe.getFile().getName()));

                    //refreshes shouldn't generate any additional events
                    toRename.refresh();
                    toRename.getParent().refresh();                    
                }

                public void fileChanged(FileEvent fe) {
                    fail();
                }

                public void fileDeleted(FileEvent fe) {
                    fail();
                }

                public void fileDataCreated(FileEvent fe) {
                    fail();
                }

                public void fileFolderCreated(FileEvent fe) {
                    fail();
                }
            };
            toRename.getParent().addFileChangeListener(fcl);
            toRename.addFileChangeListener(fcl);
            toRename.getFileSystem().addFileChangeListener(fcl);
            toRename.rename(lock,"bb", "ext");
            assertNull(toRename.getParent().getFileObject(origNameExt));
            
        } finally {
            toRename.getParent().removeFileChangeListener(fcl);
            toRename.removeFileChangeListener(fcl);
            toRename.getFileSystem().removeFileChangeListener(fcl);

            if (lock != null) {
                iListener.setLock(null);
                lock.releaseLock();
            }
        }
        assertEquals(3,events.size());
    }

    public void testImplsRename3() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        final FileObject toRename = fo.createData("aa");
        assertNotNull(toRename);
        
        iListener.clear();
        FileLock lock = toRename.lock();
        iListener.setLock(lock);
        try {
            final String origNameExt = toRename.getNameExt();
            final String origPath = toRename.getPath();
            final File origFile = FileUtil.toFile(toRename);
            final File newFile = new File(origFile.getParentFile(),FileInfo.composeName("bb", "ext"));


            iListener.setImplsRenameRetVal(true);
            FileChangeListener fcl =  new FileChangeAdapter() {
                @Override
                public void fileRenamed(FileRenameEvent fe)  {
                    try {                        
                        File f = FileUtil.toFile(toRename);
                        assertNotNull(f);
                        assertNotNull(FileUtil.toFileObject(f));
                        /* sometimes fails:
                            assertSame(toRename, FileUtil.toFileObject(f));
                        */
                        assertTrue(f.exists());
                        FileObject delegate = FileBasedFileSystem.getFileObject(f);
                        assertNotNull(delegate);
                        assertTrue(delegate.isValid());
                        if (fe.getFile().getNameExt().equals(newFile.getName())) {
                            assertNull(FileBasedFileSystem.getFileObject(origFile));
                            assertNotNull(FileBasedFileSystem.getFileObject(newFile));
                            assertNotNull(FileUtil.toFileObject(newFile));
                            assertNull(FileUtil.toFileObject(origFile));
                        } else {
                            assertNotNull(FileBasedFileSystem.getFileObject(origFile));
                            assertNull(FileBasedFileSystem.getFileObject(newFile));
                            assertNull(FileUtil.toFileObject(newFile));
                            assertNotNull(FileUtil.toFileObject(origFile));
                        }
                        assertTrue(toRename.isValid());

                        assertSame(toRename, fe.getFile());
                        assertSame(toRename.getParent(), fe.getFile().getParent());
                        try {
                            assertNotNull(toRename.getFileSystem().findResource(toRename.getPath()));

                        } catch (FileStateInvalidException ex) {
                            fail();
                        }

                        assertFalse(fe.getName().equals(fe.getFile().getName()));
                        assertFalse(fe.getExt().equals(fe.getFile().getName()));

                        //refreshes shouldn't generate any additional events
                        toRename.refresh();
                        toRename.getParent().refresh();
                    } catch(AssertionFailedError afe) {
                        afe.printStackTrace();
                        throw afe;
                    }
                }

                public void fileChanged(FileEvent fe) {
                    fail();
                }

                public void fileDeleted(FileEvent fe) {
                    fail();
                }

                public void fileDataCreated(FileEvent fe) {
                    fail();
                }

                public void fileFolderCreated(FileEvent fe) {
                    fail();
                }
            };
            toRename.getParent().addFileChangeListener(fcl);
            toRename.addFileChangeListener(fcl);
            toRename.getFileSystem().addFileChangeListener(fcl);
            toRename.rename(lock,"bb", "ext");
            assertNull(toRename.getParent().getFileObject(origNameExt));
            toRename.rename(lock,"aa", "");
            toRename.rename(lock,"bb", "ext");
            toRename.rename(lock,"aa", "");
            toRename.rename(lock,"bb", "ext");
            toRename.rename(lock,"aa", "");
        } finally {
            if (lock != null) {
                iListener.setLock(null);
                lock.releaseLock();
            }
        }
    }

    
    public static class AnnotationProviderImpl extends InterceptionListenerTest.AnnotationProviderImpl  {
        private ProvidedExtensionsImpl impl = new ProvidedExtensionsImpl(this);
        public InterceptionListener getInterceptionListener() {
            return impl;
        }
    }
    
    public static class ProvidedExtensionsImpl extends ProvidedExtensions  {
        private int implsMoveCalls;
        private int moveImplCalls;
        private int implsRenameCalls;
        private int renameImplCalls;
        private int implsBeforeChangeCalls;
        private int implsCreateSuccessCalls;        
        private int implsFileLockCalls;
        private int implsFileUnlockCalls;
        private int implsCanWriteCalls;
        
        private static  boolean implsMoveRetVal = true;
        private static boolean implsRenameRetVal = true;
        private static boolean implsDeleteRetVal = false;

        private static int cnt;
        
        public static FileLock lock;
        private final AnnotationProviderImpl provider;

        public ProvidedExtensionsImpl() {
            this(null);
        }

        public ProvidedExtensionsImpl(AnnotationProviderImpl p) {
            this.provider = p;
            cnt++;
        }
        
        public static void assertCreated(String msg, boolean reallyCreated) {
            if (reallyCreated) {
                assertEquals(msg, 1, cnt);
            } else {
                assertEquals(msg, 0, cnt);
            }
        }
        
        public void clear() {
            implsMoveCalls = 0;
            moveImplCalls = 0;
            implsRenameCalls = 0;
            renameImplCalls = 0;
            implsBeforeChangeCalls = 0;
            implsCreateSuccessCalls = 0;
            implsFileLockCalls = 0;
            implsCanWriteCalls = 0;
        }

        public boolean canWrite(File f) {
            implsCanWriteCalls++;
            return super.canWrite(f);
        }

        public void fileLocked(FileObject fo) {
            super.fileLocked(fo);
            implsFileLockCalls++;
        }

        public void fileUnlocked(FileObject fo) {
            super.fileUnlocked(fo);
            implsFileUnlockCalls++;
        }

        
        public void createSuccess(FileObject fo) {
            super.createSuccess(fo);
            assertNotNull(FileUtil.toFile(fo));
            implsCreateSuccessCalls++;
        }

        public void beforeChange(FileObject f) {
            assertNotNull(FileUtil.toFile(f));
            implsBeforeChangeCalls++;
        }    
        
        @Override
        public ProvidedExtensions.DeleteHandler getDeleteHandler(File f) {
            return (!isImplsDeleteRetVal()) ? null : new ProvidedExtensions.DeleteHandler(){
                final Set s = new HashSet();
                @Override
                public boolean delete(File file) {
                    if (file.isDirectory()) {
                        File[] childs = file.listFiles(new FileFilter() {
                            public boolean accept(File pathname) {
                                boolean accepted = pathname.isFile();
                                if (!accepted && pathname.isDirectory()) {
                                    accepted = !s.contains(pathname);
                                    if (!s.contains(pathname)) {
                                        s.add(pathname);
                                    }
                                } 
                                return accepted;
                            }
                        });
                        return childs.length == 0;
                    }
                    return file.delete();
                }                
            };
        }
                
        @Override
        public ProvidedExtensions.IOHandler getRenameHandler(final File from, final String newName) {
            implsRenameCalls++;
            final File f = new File(from.getParentFile(),newName);
            
            return (!isImplsRenameRetVal()) ? null : new ProvidedExtensions.IOHandler(){
                public void handle() throws IOException {
                    renameImplCalls++;
                    assertTrue(from.renameTo(f));
                }
            };
        }
        
        @Override
        public ProvidedExtensions.IOHandler getMoveHandler(final File from, final File to) {
            implsMoveCalls++;
            return (!isImplsMoveRetVal()) ? null : new ProvidedExtensions.IOHandler(){
                public void handle() throws IOException {
                    moveImplCalls++;
                    if (to.exists()) {
                        throw new IOException();
                    }
                    assertTrue(from.exists());
                    assertFalse(to.exists());
                    
                    assertFalse(from.equals(to));
                    if (from.isDirectory()) {
                        from.renameTo(to);
                    } else {
                        InputStream inputStream = new FileInputStream(from);
                        OutputStream outputStream = new FileOutputStream(to);
                        try {
                            FileUtil.copy(inputStream, outputStream);
                        } finally {
                            if (inputStream != null) inputStream.close();
                            if (outputStream != null) outputStream.close();
                        }
                        assertTrue(from.delete());
                    }
                    
                    assertFalse(from.exists());
                    assertTrue(to.exists());
                }
            };
        }
        
        public static FileLock getLock() {
            return lock;
        }
        
        public static void setLock(FileLock lock) {
            ProvidedExtensionsImpl.lock = lock;
        }

        public static boolean isImplsMoveRetVal() {
            return implsMoveRetVal;
        }

        public static void setImplsMoveRetVal(boolean implsMoveRetVal) {
            ProvidedExtensionsImpl.implsMoveRetVal = implsMoveRetVal;
        }

        public static boolean isImplsRenameRetVal() {
            return implsRenameRetVal;
        }

        public static void setImplsRenameRetVal(boolean implsRenameRetVal) {
            ProvidedExtensionsImpl.implsRenameRetVal = implsRenameRetVal;
        }

        public static boolean isImplsDeleteRetVal() {
            return implsDeleteRetVal;
        }

        public static void setImplsDeleteRetVal(boolean implsDeleteRetVal) {
            ProvidedExtensionsImpl.implsDeleteRetVal = implsDeleteRetVal;
        }
    }
}
