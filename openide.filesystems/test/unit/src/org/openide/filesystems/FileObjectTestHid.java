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

package org.openide.filesystems;

import org.openide.filesystems.test.StatFiles;
import org.xml.sax.*;
import org.openide.util.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.ref.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.logging.Level;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author  rm111737
 */
public class FileObjectTestHid extends TestBaseHid {

    private final static String FOLDER_CHILD_NAME= "testFolder";
    private final static String FOLDER_CHILD= "/"+FOLDER_CHILD_NAME;

    private final static String FILE_CHILD_NAME= "test";
    private final static String FILE_CHILD_EXT= "txt";
    private final static String FILE_CHILD= "/"+FILE_CHILD_NAME+"." +FILE_CHILD_EXT;
    
    /**Should be deleted and testedFS renamed to fs*/
    private FileSystem fs;
    
    private FileObject root;
    private static Set res = null;


    /** Here add necessary resources. But prefered to use:
     *  root, getFile1, getFile2, getFolder1, getFolder2,
     */
    private static String[] resources = new String [] {
    };
    
    public FileObjectTestHid(java.lang.String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        FileSystemFactoryHid.setServices(this);
        
        super.setUp();
        
        /**Should be deleted and testedFS renamed to fs*/
        fs = this.testedFS;
        root = fs.findResource(getResourcePrefix());

        StreamPool.LOG.setLevel(Level.WARNING); // do not print InterruptedException's at INFO
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        fs = this.testedFS = null;
        allTestedFS = null;        
        //Reference ref = new WeakReference (root);
        root = null;
        //assertGC("", ref);
    }

    public void testEventsDelivery81746() throws Exception {
        doEventsDelivery81746(1);
    }

    public void testEventsDeliveryInInnerAtomicActions82459() throws Exception {
        doEventsDelivery81746(2);
    }
    
    private void doEventsDelivery81746(final int howDeep) throws Exception {
        checkSetUp();
        final FileObject fold = getTestFolder1(root);
        if (fold.getFileSystem().isReadOnly()) {
            return;
        }
        class L extends FileChangeAdapter {
            public int cnt;
            
            @Override
            public void fileDataCreated(FileEvent fe) {
                cnt++;
            }
        }
        
        final FileChangeListener noFileDataCreatedListener = new FileChangeAdapter(){
            @Override
            public void fileDataCreated(FileEvent fe) {
                fail();
            }
        };
        final FileChangeListener listener1 = new FileChangeAdapter(){
            @Override
            public void fileDataCreated(FileEvent fe) {
                try {
                    fold.getFileSystem().removeFileChangeListener(noFileDataCreatedListener);
                    fold.getFileSystem().addFileChangeListener(noFileDataCreatedListener);
                } catch (FileStateInvalidException ex) {
                    FileObjectTestHid.this.fsFail("");
                }
            }
        };
        
        final L countingL = new L();
        try {
            fold.getFileSystem().addFileChangeListener(listener1);
            fold.addFileChangeListener(countingL);
            fold.getFileSystem().addFileChangeListener(countingL);
            fold.getFileSystem().runAtomicAction(new FileSystem.AtomicAction(){
                private int stillDeep = howDeep;
                
                public void run() throws java.io.IOException {
                    if (--stillDeep > 0) {
                        fold.getFileSystem().runAtomicAction(this);
                        assertEquals("No events in inner actions", 0, countingL.cnt);
                        return;
                    }
                    
                    
                    fold.createData("file1");
                    fold.createData("file2");
                }
            });
        } finally {
            fold.getFileSystem().removeFileChangeListener(listener1);
            fold.getFileSystem().removeFileChangeListener(noFileDataCreatedListener);
        }
    }
    
    public void  testEventsDelivery81746_2() throws Exception {
        checkSetUp();
        final FileObject fold = getTestFolder1(root);
        if (fold.getFileSystem().isReadOnly()) {
            return;
        }
        final FileChangeListener noFileDataCreatedListener = new FileChangeAdapter(){
            @Override
            public void fileDataCreated(FileEvent fe) {
                fail();
            }
        };
        class Once implements Runnable {
            int once;

            public void run() {
                assertEquals("No calls yet", 0, once);
                once++;
            }
        }
        final Once post = new Once();
        final FileChangeListener listener1 = new FileChangeAdapter(){
            @Override
            public void fileDataCreated(FileEvent fe) {
                fold.removeFileChangeListener(noFileDataCreatedListener);
                fold.addFileChangeListener(noFileDataCreatedListener);
                fe.runWhenDeliveryOver(post);
            }
        };
        assertEquals("Not called yet", 0, post.once);
        try {
            fold.getFileSystem().addFileChangeListener(listener1);
            fold.getFileSystem().runAtomicAction(new FileSystem.AtomicAction(){
                public void run() throws java.io.IOException {
                    fold.createData("file1");
                    fold.createData("file2");
                    assertEquals("Called not", 0, post.once);
                }
            });
            assertEquals("Called once", 1, post.once);
        } finally {
            fold.getFileSystem().removeFileChangeListener(listener1);
            fold.removeFileChangeListener(noFileDataCreatedListener);
        }
    }

    public void  testEventsDeliveryWhenFinishedOnFolder() throws Exception {
        checkSetUp();
        final FileObject fold = getTestFolder1(root);
        if (fold.getFileSystem().isReadOnly()) {
            return;
        }
        final FileChangeListener noFileDataCreatedListener = new FileChangeAdapter(){
            @Override
            public void fileDataCreated(FileEvent fe) {
                fail();
            }
        };
        class Once implements Runnable {
            int once;

            public void run() {
                assertEquals("No calls yet", 0, once);
                once++;
            }
        }
        final Once post = new Once();
        final FileChangeListener listener1 = new FileChangeAdapter(){
            @Override
            public void fileDataCreated(FileEvent fe) {
                fold.removeFileChangeListener(noFileDataCreatedListener);
                fold.addFileChangeListener(noFileDataCreatedListener);
                fe.runWhenDeliveryOver(post);
            }
        };
        assertEquals("Not called yet", 0, post.once);
        try {
            fold.addFileChangeListener(listener1);
            fold.getFileSystem().runAtomicAction(new FileSystem.AtomicAction(){
                public void run() throws java.io.IOException {
                    FileObject fo1 = fold.createData("file1");
                    OutputStream os = fo1.getOutputStream();
                    os.write("Ahoj\n".getBytes());
                    os.close();
                    FileObject fo2 = fold.createData("file2");
                    FileLock l = fo1.lock();
                    fo1.rename(l, "filerenamed", "txt");
                    l.releaseLock();
                    fo2.delete();
                    FileObject fo3 = fold.createFolder("folder");
                    fo3.setAttribute("newAttr", 10);
                    assertEquals("attr set", 10, fo3.getAttribute("newAttr"));
                    assertEquals("Called not", 0, post.once);
                }
            });
            assertEquals("Called once", 1, post.once);
        } finally {
            fold.removeFileChangeListener(listener1);
            fold.removeFileChangeListener(noFileDataCreatedListener);
        }
    }
    
    /** Test of copy method, of class org.openide.filesystems.FileObject. */
    public void  testCopy()  {
        checkSetUp();
        FileObject fold = getTestFolder1(root);
        FileObject fo1 = getTestFile1(fold);
        FileObject fo2 = getTestFile2(fold);
        
        try {
            fo1.copy(fold,fo2.getName(),fo2.getExt());
        } catch (IOException iex) {
            /** Test passed*/
            return;
        }
        fsFail  ("copy  should fire exception if file already exists");        
    }
    
    
    /** Test of copy method, of class org.openide.filesystems.FileObject. */
    public void  testCopy1() throws IOException {
        checkSetUp();
        FileObject fold = getTestFolder1(root);
        FileObject fo1 = getTestFile1(fold);
        FileObject fo2 = getTestFile2(fold);
        FileObject fo3 = null;
        
        String testStr = "text...";
        String attrName = "attrName";
        String value = "value";
        
        registerDefaultListener(fold);
        
        try {
            writeStr(fo1, testStr);
            fo1.setAttribute(attrName,value);
            fo3 = fo1.copy(fold,fo2.getExt(),fo2.getName());
        } catch (IOException iex) {
            fsAssert("expected copy will success on writable FS",
            fs.isReadOnly() || fo3.isReadOnly() || fo1.isReadOnly());
            return;
        }
        fsAssert("no exception fired but copy returned null",fo3 != null);
        fsAssert("content of source and target should equal",testStr.equals(readStr(fo3)));
        fsAssert("attributes should be copied too",
        value.equals((String)fo3.getAttribute(attrName)) );
        fileDataCreatedAssert("parent should fire fileDataCreated",1);
    }
    
    /** Test of copy method, of class org.openide.filesystems.FileObject. */
    public void  testCopy1_FS() throws IOException {
        checkSetUp();
        FileObject fold = getTestFolder1(root);
        FileObject fo1 = getTestFile1(fold);
        FileObject fo2 = getTestFile2(fold);
        FileObject fo3 = null;
        
        String testStr = "text...";
        String attrName = "attrName";
        String value = "value";
        
        registerDefaultListener(testedFS);
        
        try {
            writeStr(fo1, testStr);
            fo1.setAttribute(attrName,value);
            fo3 = fo1.copy(fold,fo2.getExt(),fo2.getName());
        } catch (IOException iex) {
            fsAssert("expected copy will success on writable FS",
            fs.isReadOnly() || fo3.isReadOnly() || fo1.isReadOnly());
            return;
        }
        fsAssert("no exception fired but copy returned null",fo3 != null);
        fsAssert("content of source and target should equal",testStr.equals(readStr(fo3)));
        fsAssert("attributes should be copied too",
        value.equals((String)fo3.getAttribute(attrName)) );
        fileDataCreatedAssert("parent should fire fileDataCreated",1);
    }
    
    
    
    /** Test of move method, of class org.openide.filesystems.FileObject. */
    public void  testMove() {
        checkSetUp();
        FileObject fold = getTestFolder1(root);
        FileObject fo1 = getTestFile1(fold);
        FileObject fo2 = getTestFile2(fold);
        FileLock lock = null;
        
        try {
            lock = fo1.lock();
            fo1.move(lock, fold,fo2.getName(),fo2.getExt());
        } catch (IOException iex) {
            /** Test passed*/
            return;
        } finally {
            if (lock != null) lock.releaseLock();            
        }
        fsFail  ("move  should fire exception if file already exists");
    }
    
    /** Test of move method, of class org.openide.filesystems.FileObject. */
    public void  testMove1() throws IOException {
        checkSetUp();
        FileObject fold = getTestFolder1(root);
        FileObject fo1 = getTestFile1(fold);
        FileObject fo2 = getTestFile2(fold);
        FileObject fo3 = null;
        
        String testStr = "text...";
        String attrName = "attrName";
        String value = "value";
        
        registerDefaultListener(fold);
        
        try {
            writeStr(fo1, testStr);
            fo1.setAttribute(attrName,value);
            FileLock lock = fo1.lock();
            fold.getChildren();
            fo3 = fo1.move(lock,fold,fo2.getExt(),fo2.getName());
            lock.releaseLock();
        } catch (IOException iex) {
            fsAssert("expected move will success on writable FS",
            fs.isReadOnly() || fo3.isReadOnly() || fo1.isReadOnly());
            return;
        }
        fsAssert("no exception fired but copy returned null",fo3 != null);
        fsAssert("content of source and target should equal",testStr.equals(readStr(fo3)));
        fsAssert("attributes should be copied too",
        value.equals((String)fo3.getAttribute(attrName)) );
        fsAssert ("",fold.getFileObject(fo3.getName(),fo3.getExt()) != null);
        if (fo1.equals(fo3))
            this.fileRenamedAssert("File was actually renamed.",1);
        else {
            fileDeletedAssert("parent should fire fileDeleted",1);        
            fileDataCreatedAssert("parent should fire fileDataCreated",1);
        }
    }
    
    /** Test of move method, of class org.openide.filesystems.FileObject. */
    public void  testMove2() throws IOException {
        checkSetUp();
        if (fs.isReadOnly()) return;
        FileObject fold = getTestFolder1(root);

        FileObject fold1 = fold.createFolder("A");
        FileObject fold2 = fold.createFolder("B");

        FileObject toMove = fold1.createData("something");
        FileLock lock = toMove.lock();
        try {
            FileObject toMove2 = null;
            assertNotNull (toMove2 = toMove.move(lock, fold2, toMove.getName(), toMove.getExt()));
            lock.releaseLock();
            lock = toMove2.lock();
            assertNotNull (toMove2.move(lock, fold1, toMove.getName(), toMove.getExt()));
            
        } finally {
            lock.releaseLock();
        }
    }

    public void  testMoveFolder() throws IOException {
        checkSetUp();
        if (fs.isReadOnly()) return;
        FileObject fold = getTestFolder1(root);

        FileObject fold1 = fold.createFolder("A");
        FileObject fold2 = fold.createFolder("B");

        FileObject toMove = fold1.createFolder("something");
        toMove.createData("kid");
        FileLock lock = toMove.lock();
        FileObject last = null;
        try {
            FileObject toMove2 = null;
            assertNotNull (toMove2 = toMove.move(lock, fold2, toMove.getName(), toMove.getExt()));
            lock.releaseLock();
            lock = toMove2.lock();
            assertNotNull(last = toMove2.move(lock, fold1, toMove.getName(), toMove.getExt()));
        } finally {
            lock.releaseLock();
        }
        assertTrue("Folder remains folder", last.isFolder());
        assertEquals("One child remains", 1, last.getChildren().length);
        FileObject created = last.getChildren()[0];
        assertEquals("kid", created.getNameExt());
        assertTrue("is data", created.isData());
    }

    /** Test of move method, of class org.openide.filesystems.FileObject. */
    public void  testMove1_Fs() throws IOException {
        checkSetUp();
        FileObject fold = getTestFolder1(root);
        FileObject fo1 = getTestFile1(fold);
        FileObject fo2 = getTestFile2(fold);
        FileObject fo3 = null;
        
        String testStr = "text...";
        String attrName = "attrName";
        String value = "value";
        
        registerDefaultListener(testedFS);
        
        try {
            writeStr(fo1, testStr);
            fo1.setAttribute(attrName,value);
            FileLock lock = fo1.lock();
            fold.getChildren();
            fo3 = fo1.move(lock,fold,fo2.getExt(),fo2.getName());
            lock.releaseLock();
        } catch (IOException iex) {
            fsAssert("expected move will success on writable FS",
            fs.isReadOnly() || fo3.isReadOnly() || fo1.isReadOnly());
            return;
        }
        fsAssert("no exception fired but copy returned null",fo3 != null);
        fsAssert("content of source and target should equal",testStr.equals(readStr(fo3)));
        fsAssert("attributes should be copied too",
        value.equals((String)fo3.getAttribute(attrName)) );
        fsAssert ("",fold.getFileObject(fo3.getName(),fo3.getExt()) != null);
        if (fo1.equals(fo3))
            this.fileRenamedAssert("File was actually renamed.",1);
        else {
            fileDeletedAssert("parent should fire fileDeleted",1);        
            fileDataCreatedAssert("parent should fire fileDataCreated",1);
        }
    }
    
    
    /** Test whether the read is forbiden while somebody is writing
     */
    @RandomlyFails
    public void testWriteReadExclusion() throws Exception {
        testWriteReadExclusion(false);
    }

    @RandomlyFails
    public void testWriteReadExclusionDeadlock() throws Exception {
        testWriteReadExclusion(true);
    }

    private void testWriteReadExclusion(final boolean deadlockInWrite) throws Exception {
        checkSetUp();
        FileObject fold = getTestFolder1(root);
        final FileObject fo1 = getTestFile1(fold);
        if (fs.isReadOnly() || fo1.isReadOnly()) return;
        firstThreadImpl1(fo1, deadlockInWrite);
        firstThreadImpl2(fo1, deadlockInWrite);
    }

    private static void firstThreadImpl1(final FileObject fo1, final boolean deadlockInWrite) throws IOException, InterruptedException {
        RequestProcessor.Task secondThread;
        writeStr(fo1, "text");
        
        secondThread = startSecondThreadAndWait(fo1, deadlockInWrite);

        InputStream is = null;
        try {
            is  = fo1.getInputStream();
            byte[] arr = new byte[200];
            int len = is.read(arr);
            assertEquals("Read all four bytes", 4, len);
            for (int i = 1; i <= 4; i++) {
                assertEquals(i + " th byte is " + i, i, arr[i - 1]);
            }
        } catch (IOException ex) {
            // FileAlreadyLockedException is fine, means we could not read the stream
            if (!deadlockInWrite) {
                throw ex;
            }
            assertTrue("FileAlreadyLockedException is fine here but just for dedlock in write", deadlockInWrite);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
            synchronized (FileObjectTestHid.class) {
                // let the writer thread finish
                FileObjectTestHid.class.notifyAll();
            }
            secondThread.waitFinished();
        }
    }

    private static void firstThreadImpl2(final FileObject fo1, final boolean deadlockInWrite) throws IOException ,InterruptedException {
        RequestProcessor.Task secondThread = null;
        writeStr(fo1, "text");            

        InputStream is = null;
        try {
            is  = fo1.getInputStream();
            secondThread = startSecondThreadAndWait(fo1, deadlockInWrite);
            
            byte[] arr = new byte[200];
            int len = is.read(arr);
            assertEquals("Initial data only" , "text".length(), len);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
            synchronized (FileObjectTestHid.class) {
                // let the writer thread finish
                FileObjectTestHid.class.notifyAll();
            }
            if (secondThread != null) secondThread.waitFinished();
        }
    }
    
    private static void secondThreadImpl(final FileObject fo1, final boolean deadlockInWrite)  {
        OutputStream os = null;
        FileLock lock = null;
        try {
            try {
                lock  = fo1.lock();
                os  = fo1.getOutputStream(lock);
                os.write(1);
                os.write(2);
                os.flush();

                synchronized (FileObjectTestHid.class) {
                    FileObjectTestHid.class.notify();
                    // block for a while so the other thread
                    // can do the partial read
                    FileObjectTestHid.class.wait(deadlockInWrite ? 0 : 1000);
                }

                os.write(3);
                os.write(4);
            } finally {
                if (lock != null) lock.releaseLock();
                if (os != null) os.close();
                synchronized (FileObjectTestHid.class) {
                    FileObjectTestHid.class.notify();
                }                            
            }
        } catch (Exception e) {
        } 
    }

    private static RequestProcessor.Task startSecondThreadAndWait(final FileObject fo1, final boolean deadlockInWrite) throws InterruptedException {
        RequestProcessor.Task secondThread;
        synchronized (FileObjectTestHid.class) {
            secondThread = new RequestProcessor("Writes with delay").post(new Runnable() {
                public void run() {
                    secondThreadImpl(fo1, deadlockInWrite);
                }
            });
            FileObjectTestHid.class.wait();
        }
        return secondThread;
    }
    
    public void  testGetPath1() {
        checkSetUp();
        FileObject fold1 = getTestFolder1(root);
        FileObject fold2 = getTestFolder1(fold1);
        FileObject fo1 = getTestFile1(fold2);
        
        FileObject result = fs.findResource(fo1.getPath());
        fsAssert("findResource problem", result != null);
        fsAssert("findResource problem",fo1.equals(result));
    }
    
    public void  testGetPath2() {
        checkSetUp();
        FileObject fold1 = getTestFolder1(root);
        FileObject fold2 = getTestFolder1(fold1);
        FileObject fo1 = null;
        try {
            fo1 = fold2.createFolder("a.b.c");        
        } catch (IOException iex) {
            fsAssert("There is not possible to create folder a.b.c",
            fs.isReadOnly() || fold2.isReadOnly());
            return;
        }
        
        FileObject result = fs.findResource(fo1.getPath());
        fsAssert("findResource problem", result != null);
        fsAssert("findResource problem",fo1.equals(result));
    }
    
    public void  testGetPath3() throws  IOException{
        /** There is no possible to create filenames ending in dots on Win platforms*/
        if ((org.openide.util.Utilities.isWindows () ||
                (org.openide.util.Utilities.getOperatingSystem () == org.openide.util.Utilities.OS_OS2))) {
            return;
        }
        checkSetUp();
        FileObject fold1 = getTestFolder1(root);
        FileObject fold2 = getTestFolder1(fold1);
        
        FileObject fo1 = null;
        try {
            fo1 = fold2.createFolder("a.b.c.");
        } catch (IOException iex) {
            fsAssert("There is not possible to create folder a.b.c",
            fs.isReadOnly() || fold2.isReadOnly());
            return;
        }

        
        FileObject result = fs.findResource(fo1.getPath());
        fsAssert("findResource problem", result != null);
        fsAssert("findResource problem",fo1.equals(result));
    }

    public void  testGetPath4() throws  IOException{
        checkSetUp();
        FileObject fold1 = getTestFolder1(root);
        FileObject fold2 = getTestFolder1(fold1);

        FileObject fo1 = null;
        try {
            fo1 = fold2.createData("a.b.c.java");        
        } catch (IOException iex) {
            fsAssert("There is not possible to create folder a.b.c",
            fs.isReadOnly() || fold2.isReadOnly());
            return;
        }
        
        FileObject result = fs.findResource(fo1.getPath());
        fsAssert("findResource problem", result != null);
        fsAssert("findResource problem",fo1.equals(result));
    }

    public void  testGetPath5() throws  IOException{
        checkSetUp();
        FileObject fold1 = getTestFolder1(root);
        FileObject fold2 = getTestFolder1(fold1);


        FileObject fo1 = null;
        try {
            fo1 = fold2.createData("a.b.c","java");        
        } catch (IOException iex) {
            fsAssert("There is not possible to create folder a.b.c",
            fs.isReadOnly() || fold2.isReadOnly());
            return;
        }
        
        FileObject result = fs.findResource(fo1.getPath());
        fsAssert("findResource problem", result != null);
        fsAssert("findResource problem",fo1.equals(result));
    }
    
    /** Test of getPackageName method, of class org.openide.filesystems.FileObject. * /
    public void  testGetPackageName() {
        checkSetUp();
        FileObject fold1 = getTestFolder1(root);
        FileObject fold2 = getTestFolder1(fold1);
        FileObject fo1 = getTestFile1(fold2);
        
        String getPackageNameExt = fo1.getPackageName('/')+"."+fo1.getExt();
        FileObject result = fs.findResource(getPackageNameExt);
        fsAssert("getPackageName or getPackageNameExt problem: " +
        "!fo1.getPath("+fo1.getPath()+").equals("+getPackageNameExt+")",fo1.getPath().equals(getPackageNameExt));
        fsAssert("getPackageName or findResource problem",result != null);
        fsAssert("getPackageName or findResource problem",fo1.equals(result));
    }
    */

    /** Test of getPackageName method, of class org.openide.filesystems.FileObject. * /
    public void  testGetPackageName2() {
        checkSetUp();
        FileObject fold1 = getTestFolder1(root);
        FileObject fold2 = getTestFolder1(fold1);
        FileObject fo1 = null;
        
        try {
            fo1 = fold2.createFolder("a.b.c");        
        } catch (IOException iex) {
            fsAssert("There is not possible to create folder a.b.c",
            fs.isReadOnly() || fold2.isReadOnly());
            return;
        }
        
        String getPackageNameExt = fo1.getPackageName('/')+"."+fo1.getExt();
        FileObject result = fs.findResource(getPackageNameExt);
        fsAssert("getPackageName or getPackageNameExt problem: " +
        "!fo1.getPath("+fo1.getPath()+").equals("+getPackageNameExt+")",fo1.getPath().equals(getPackageNameExt));
        fsAssert("getPackageName or findResource problem",result != null);
        fsAssert("getPackageName or findResource problem",fo1.equals(result));
    }
    */
    
    /** Test of getNameExt method, of class org.openide.filesystems.FileObject. */
    public void  testGetNameExt() {
        checkSetUp();
        FileObject fold1 = getTestFolder1(root);
        FileObject fold2 = getTestFolder1(fold1);
        FileObject fo1 = getTestFile1(fold2);
        
        fsAssert("getNameExt problem",fo1.getNameExt().equals(fo1.getName() + "." +fo1.getExt()));
    }
    
    /** Test of existsExt method, of class org.openide.filesystems.FileObject. */
    public void  testExistsExt() {
        checkSetUp();
        FileObject fo1 = getTestFile1(root);
        FileObject fo2 = getTestFile2(root);
        
        fsAssert("Expected both file`s names are equal. But extension differ.",fo1.existsExt(fo2.getExt()));
    }
    
    /** Test of hasExt method, of class org.openide.filesystems.FileObject. */
    public void  testHasExt() {
        checkSetUp();
        FileObject fo1 = getTestFile1(root);
        FileObject fo2 = getTestFile2(root);
        FileObject fo11 = getTestFile1(getTestFolder1(root));
        
        fsTestFrameworkErrorAssert("",fo1.getExt().equals(fo11.getExt()) &&
        !fo1.getExt().equals(fo2.getExt()));
        fsAssert("Unexpected ",fo1.hasExt(fo11.getExt()) && !fo1.hasExt(fo2.getExt()));
    }
    
    /** Test of fireFileDataCreatedEvent method, of class org.openide.filesystems.FileObject. */
    public void  testFireFileDataCreatedEvent() throws IOException {
        checkSetUp();
        registerDefaultListener(root);
        root.getChildren();
        
        FileObject fo;
        try {
            fo = root.createData("name","ext");
        } catch (IOException iex) {
            fsAssert("createData fired IOException. So there was expected fs or fo are read-only: " + iex.toString() ,
            fs.isReadOnly() || root.isReadOnly());
            fileDataCreatedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        }
        
        fileDataCreatedAssert("createData should fire event fileDataCreated",1);
        fsAssert("createData returned null",fo != null);
        
        fileFolderCreatedAssert("fireFolderDataCreatedEvent  should not be fired ",0);
        
        fileRenamedAssert("fireFileRenamedEvent should not be fired ",0);
        fileDeletedAssert("fireFileDeletedEvent should not be fired ",0);
        fileChangedAssert("fireFileChangedEvent should not be fired ",0);
    }

    /** Test of fireFileDataCreatedEvent method, of class org.openide.filesystems.FileObject. */
    public void  testFireFileDataCreatedEvent_FS() throws IOException {
        checkSetUp();
        registerDefaultListener(testedFS);
        root.getChildren();
        
        FileObject fo;
        try {
            fo = root.createData("name","ext");
        } catch (IOException iex) {
            fsAssert("createData fired IOException. So there was expected fs or fo are read-only: " + iex.toString() ,
            fs.isReadOnly() || root.isReadOnly());
            fileDataCreatedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        }
        
        fileDataCreatedAssert("createData should fire event fileDataCreated",1);
        fsAssert("createData returned null",fo != null);
        
        fileFolderCreatedAssert("fireFolderDataCreatedEvent  should not be fired ",0);
        
        fileRenamedAssert("fireFileRenamedEvent should not be fired ",0);
        fileDeletedAssert("fireFileDeletedEvent should not be fired ",0);
        fileChangedAssert("fireFileChangedEvent should not be fired ",0);
    }

    public void  testFireFileDataCreatedEvent_FS2() throws IOException {
        checkSetUp();
        registerDefaultListener(testedFS);
        root.getChildren();
        
        File rootFile = FileUtil.toFile(root);
        if (rootFile == null) return;
        (new File (rootFile, "testfile.test")).createNewFile();
        root.refresh();
        
        fileDataCreatedAssert("createData should fire event fileDataCreated",1);
        
        fileFolderCreatedAssert("fireFolderDataCreatedEvent  should not be fired ",0);        
        fileRenamedAssert("fireFileRenamedEvent should not be fired ",0);
        fileDeletedAssert("fireFileDeletedEvent should not be fired ",0);
        fileChangedAssert("fireFileChangedEvent should not be fired ",0);
    }
    
    /** Test of fireFileFolderCreatedEvent method, of class org.openide.filesystems.FileObject. */
    public void  testFireFileFolderCreatedEvent() {
        checkSetUp();
        root.getChildren();        
        registerDefaultListener(root);
        
        FileObject fo;
        try {
            fo = root.createFolder("name");
        } catch (IOException iex) {
            fsAssert("createFolder fired IOException. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileFolderCreatedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        }
        fileFolderCreatedAssert("createFolder should fire event fileFolderCreated",1);
        fsAssert("createFolder returned null",fo != null);
        
        fileDataCreatedAssert("fireFileDataCreatedEvent should not be fired ",0);
        fileRenamedAssert("fireFileRenamedEvent should not be fired ",0);
        fileDeletedAssert("fireFileDeletedEvent should not be fired ",0);
        fileChangedAssert("fireFileChangedEvent should not be fired ",0);
    }

    /** Test of fireFileFolderCreatedEvent method, of class org.openide.filesystems.FileObject. */
    public void  testFireFileFolderCreatedEvent_FS() {
        checkSetUp();
        root.getChildren();        
        registerDefaultListener(testedFS);
        
        FileObject fo;
        try {
            fo = root.createFolder("name");
        } catch (IOException iex) {
            fsAssert("createFolder fired IOException. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileFolderCreatedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        }
        fileFolderCreatedAssert("createFolder should fire event fileFolderCreated",1);
        fsAssert("createFolder returned null",fo != null);
        
        fileDataCreatedAssert("fireFileDataCreatedEvent should not be fired ",0);
        fileRenamedAssert("fireFileRenamedEvent should not be fired ",0);
        fileDeletedAssert("fireFileDeletedEvent should not be fired ",0);
        fileChangedAssert("fireFileChangedEvent should not be fired ",0);
    }
    
    /** Test of fireFileChangedEvent method, of class org.openide.filesystems.FileObject. */
    public void  testFireFileChangedEvent() throws IOException{
        checkSetUp();
        FileObject fo = getTestFile1(root);
        registerDefaultListener(fo);
        try {
            writeStr(fo,"Text ...");
        } catch (IOException iex) {
            fsAssert("FileObject could not be modified. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileChangedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        }
        
        fileChangedAssert("FileObject was modified. fireFileChangedEvent should be fired",1);
        
        fileDataCreatedAssert("fireFileDataCreatedEvent should not be fired ",0);
        fileFolderCreatedAssert("fireFolderDataCreatedEvent  should not be fired ",0);
        fileRenamedAssert("fireFileRenamedEvent should not be fired ",0);
        fileDeletedAssert("fireFileDeletedEvent should not be fired ",0);
    }

    /** Test of fireFileChangedEvent method, of class org.openide.filesystems.FileObject. */
    public void  testFireFileChangedEvent_FS() throws IOException{
        checkSetUp();
        FileObject fo = getTestFile1(root);
        registerDefaultListener(testedFS);
        try {
            writeStr(fo,"Text ...");
        } catch (IOException iex) {
            fsAssert("FileObject could not be modified. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileChangedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        }
        
        fileChangedAssert("FileObject was modified. fireFileChangedEvent should be fired",1);
        
        fileDataCreatedAssert("fireFileDataCreatedEvent should not be fired ",0);
        fileFolderCreatedAssert("fireFolderDataCreatedEvent  should not be fired ",0);
        fileRenamedAssert("fireFileRenamedEvent should not be fired ",0);
        fileDeletedAssert("fireFileDeletedEvent should not be fired ",0);
    }
    
    /** Test of fireFileDeletedEvent method, of class org.openide.filesystems.FileObject. */
    public void  testFireFileDeletedEvent() throws IOException {
        checkSetUp();
        FileObject fo1 = getTestFile1(root);
        FileObject fo2 = getTestFile2(root);
        
        registerDefaultListener(root);
        registerDefaultListener(fo1);
        registerDefaultListener(fo2);
        
        FileLock lock1 = null;
        FileLock lock2 = null;
        try {
            lock1 = fo1.lock();
            lock2 = fo2.lock();
            fo1.delete(lock1);
            fileDeletedAssert("FileObject was deleted. fireFileDeletedEvent should be fired",2);
            fo2.delete(lock2);
            fileDeletedAssert("FileObject was deleted. fireFileDeletedEvent should be fired",4);
        } catch (IOException iex) {
            fsAssert("FileObject could not be deleted. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileDeletedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        } finally {
            if (lock1 != null) lock1.releaseLock();
            if (lock2 != null) lock2.releaseLock();
        }
        
        fileDataCreatedAssert("fireFileDataCreatedEvent should not be fired ",0);
        fileFolderCreatedAssert("fireFolderDataCreatedEvent  should not be fired ",0);
        fileRenamedAssert("fireFileRenamedEvent should not be fired ",0);
        fileChangedAssert("fireFileChangedEvent should not be fired ",0);
        
        fsAssert("FileObject should be invalid after delete",!fo1.isValid());
        fsAssert("FileObject should be invalid after delete",!fo2.isValid());
    }

    public void  testFireFileDeletedEvent2() throws IOException {
        checkSetUp();
        FileObject fo1 = getTestFolder1(root);
        FileObject fo2 = getTestFolder1(fo1);
        FileObject fo3 = getTestFile1(fo2);
                
        registerDefaultListener(fo3);                
        FileLock lock1 = null;
        try {
            lock1 = fo1.lock();
            fo1.delete(lock1);
            fileDeletedAssert("FileObject was deleted. fireFileDeletedEvent should be fired",1);
            fsAssert("FileObject should be invalid after delete",!fo1.isValid());
            fsAssert("FileObject should be invalid after delete",!fo2.isValid());
            fsAssert("FileObject should be invalid after delete",!fo3.isValid());            
            
        } catch (IOException iex) {
            fsAssert("FileObject could not be deleted. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileDeletedAssert("fs or fo is read-only. So no event should be fired",0);
        } finally {
            if (lock1 != null) lock1.releaseLock();
        }
        
    }
    
    
    /** Test of fireFileAttributeChangedEvent method, of class org.openide.filesystems.FileObject. */
    public void  testFireFileAttributeChangedEvent() {
        checkSetUp();
        FileObject fo1 = getTestFile1(root);
        String value = "value";
        
        registerDefaultListener(fo1);
        try {
            fo1.setAttribute("attrName",value);
            fileAttributeChangedAssert("",1);
            fsAssert("",((String)fo1.getAttribute("attrName")).equals(value));
        } catch (IOException iex) {
            fsAssert("Attribute could not be set. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileAttributeChangedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        }
    }

    /** Test of fireFileAttributeChangedEvent method, of class org.openide.filesystems.FileObject. */
    public void  testFireFileAttributeChangedEvent_FS() {
        checkSetUp();
        FileObject fo1 = getTestFile1(root);
        String value = "value";
        
        registerDefaultListener(testedFS);
        try {
            fo1.setAttribute("attrName",value);
            fileAttributeChangedAssert("",1);
            fsAssert("",((String)fo1.getAttribute("attrName")).equals(value));
        } catch (IOException iex) {
            fsAssert("Attribute could not be set. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileAttributeChangedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        }
    }
    
    /** Test of fireFileRenamedEvent method, of class org.openide.filesystems.FileObject. */
    public void  testFireFileRenamedEvent() {
        checkSetUp();
        FileObject fo = getTestFile1(root);
        registerDefaultListener(fo);
        FileLock lock = null;
        
        try {
            lock = fo.lock();
            fo.rename(lock,fo.getName()+"X",fo.getExt()+"X");
        } catch (IOException iex) {
            fsAssert("FileObject could not be renamed. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileRenamedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        } finally {
            if (lock != null) lock.releaseLock();
        }
        
        fileRenamedAssert("",1);        
        fileDataCreatedAssert("fireFileDataCreatedEvent should not be fired ",0);
        fileFolderCreatedAssert("fireFolderDataCreatedEvent  should not be fired ",0);
        fileDeletedAssert("fireFileDeletedEvent should not be fired ",0);
    }

    /** Test of fireFileRenamedEvent method, of class org.openide.filesystems.FileObject. */
    public void  testFireFileRenamedEvent_FS() {
        checkSetUp();
        final FileObject fo = getTestFile1(root);
        registerDefaultListener(testedFS);
        
        class Immediate extends FileChangeAdapter implements Runnable {
            int cnt;
            FileRenameEvent fe;
            @Override
            public void fileRenamed(FileRenameEvent fe) {
                int prev = cnt;
                fe.runWhenDeliveryOver(this);
                assertEquals("run() not called immediately", prev, cnt);
                this.fe = fe;
            }
            public void run() {
                cnt++;
            }

            void testCallOutsideOfDeliverySystem() {
                assertNotNull("There shall be an event", fe);
                int prev = cnt;
                fe.runWhenDeliveryOver(this);
                assertEquals("run() called immediately", prev + 1, cnt);
            }
        }
        Immediate immediately = new Immediate();
        fo.addFileChangeListener(immediately);

        final FileLock[] lock = new FileLock[1];
        try {
            lock[0] = fo.lock();
            FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    fo.rename(lock[0],fo.getName()+"X",fo.getExt()+"X");
                }
            });
        } catch (IOException iex) {
            fsAssert("FileObject could not be renamed. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileRenamedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        } finally {
            if (lock[0] != null) lock[0].releaseLock();
        }
        assertEquals("One call", 1, immediately.cnt);
        immediately.testCallOutsideOfDeliverySystem();
        
        fileRenamedAssert("",1);        
        fileDataCreatedAssert("fireFileDataCreatedEvent should not be fired ",0);
        fileFolderCreatedAssert("fireFolderDataCreatedEvent  should not be fired ",0);
        fileDeletedAssert("fireFileDeletedEvent should not be fired ",0);
    }
    
    /** Test of getMIMEType method, of class org.openide.filesystems.FileObject. */
    public void  testGetMIMEType() {
        checkSetUp();
        FileObject fo = getTestFile1(root);
        String mimeType = "mimeType";
        
        FileUtil.setMIMEType(fo.getExt(),mimeType);
        String actualMT = fo.getMIMEType();
        // deregister
        FileUtil.setMIMEType(fo.getExt(), null);
        fsAssert("mimeType for this fo was registered; was really " + actualMT, actualMT.equals(mimeType));
    }

    public void testGetMIMETypeWithResolver() {
        checkSetUp();
        FileObject fo = getTestFile1(root);
        FileSystemFactoryHid.setServices(this, MR.class);
        
        String actualMT = fo.getMIMEType();
        assertNotNull("MIMEResolver not accessed at all.", MR.tested);
        assertEquals("Wrong MIME type recognized.", fo.getExt(), actualMT);
    }

    /** Test whether MIME type for the same FileObject is properly cached 
     * without unnecessary disk accesses and whether cache is freed when
     * undelying file is modified.
     */
    public void testGetMIMETypeCached() throws IOException {
        checkSetUp();
        FileObject fo = getTestFile1(root);
        FileSystemFactoryHid.setServices(this, MR.class);

        StatFiles accessCounter = new StatFiles();
        accessCounter.register();
        for (int i = 0; i < 100; i++) {
            assertEquals("Wrong MIME type recognized.", fo.getExt(), fo.getMIMEType());
            assertNotNull("MIMEResolver not accessed at all.", MR.tested);
        }
        accessCounter.unregister();
        accessCounter.getResults().assertResult(2, StatFiles.READ);
        
        if(fo.canWrite()) {
            String beforeRename = fo.getMIMEType();
            FileLock lock = fo.lock();
            fo.rename(lock, fo.getName(), "newDummyExt");
            lock.releaseLock();
            String afterRename = fo.getMIMEType();
            assertFalse("MIME type after rename must be different.", beforeRename.equals(afterRename));

            OutputStream os = fo.getOutputStream();
            os.write(42);
            os.close();
            MR.tested = null;
            assertEquals("Wrong MIME type recognized.", fo.getExt(), fo.getMIMEType());
            assertNotNull("After file modification cache must be cleaned and MIMEResolver accessed.", MR.tested);
        }
    }

    public void testGetMIMETypeCachedInAtomicAction() throws IOException {
        FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                testGetMIMETypeCached();
            }
        });
    }

    public static final class MR extends MIMEResolver {
        public static FileObject tested;
        
        public String findMIMEType(FileObject fo) {
            try {
                return f(fo);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        private String f(FileObject fo) throws IOException {
            InputStream is = fo.getInputStream();
            byte[] arr = new byte[4096];
            is.read(arr);
            is.close();
            tested = fo;
            return fo.getExt();
        }
    }
    
    /** Default mime type for files with strange chacracters is content/unknown
     */
    public void testDefaultMimeTypeForBinaryFiles () throws Exception {
        checkSetUp();
        FileObject fo;
        try {
            fo = FileUtil.createData(root, "file.jarda"); // file with completely strange extension
        } catch (IOException iex) {
            fsAssert(
            "Does not seem to be writeable. So there was expected.",
            !root.canWrite () && fs.isReadOnly()
            );
            return;
        }
        FileLock lock = fo.lock();
        
        InputStream is = getClass ().getResourceAsStream("FileObjectTestHid.class");
        OutputStream os = fo.getOutputStream (lock);
        FileUtil.copy (is, os);
        is.close ();
        os.close ();
        lock.releaseLock ();
        
        assertEquals ("File is recognized as unknown", "content/unknown", fo.getMIMEType());
    }

    /** Mime type for folder fallbacks to content/unknown, issue 42965
     */
    public void testFolderMimeTypeIsUnknown () throws Exception {
        checkSetUp();
        FileObject fo;
        try {
            fo = FileUtil.createFolder (root, "SomeStrangeFolder"); 
        } catch (IOException iex) {
            fsAssert(
            "Does not seem to be writeable. So there was expected.",
            !root.canWrite () && fs.isReadOnly()
            );
            return;
        }
        
        assertEquals ("File is recognized as unknown", "content/unknown", fo.getMIMEType());
    }
    
    /** Test of getChildren method, of class org.openide.filesystems.FileObject. */
    public void  testGetChildren() {
        checkSetUp();
        FileObject[] childs = root.getChildren();
        FileObject fo1 = getTestFile1(root);
        FileObject fo2 = getTestFile2(root);
        FileObject fold1 = getTestFolder1(root);
        FileObject fold2 = getTestFolder2(root);
        
        
        fsAssert("Expected 2 folders and 2 files under root",childs.length >= 4);
        fsAssert(fo1.getNameExt() + " should be child of root" ,Arrays.asList(childs).contains(fo1) );
        fsAssert(fo2.getNameExt() + " should be child of root" ,Arrays.asList(childs).contains(fo2) );
        fsAssert(fold1.getNameExt() + " should be child of root" ,Arrays.asList(childs).contains(fold1));
        fsAssert(fold2.getNameExt() + " should be child of root" ,Arrays.asList(childs).contains(fold2));
    }
    
    /** Test of getFolders method, of class org.openide.filesystems.FileObject. */
    public void  testGetFolders() {
        checkSetUp();
        FileObject fold1 = getTestFolder1(root);
        FileObject fold2 = getTestFolder2(root);
        
        List list= makeList(root.getFolders(false));
        fsAssert("Expected that enumeration will include FileObjects according to TestSetup",list.contains(fold1));
        fsAssert("Expected that enumeration will include FileObjects according to TestSetup",list.contains(fold2));        
    }
    
    /** Test of getFolders method, of class org.openide.filesystems.FileObject. */
    public void  testGetFolders1() {
        checkSetUp();
        
        List childs = makeList(root.getChildren(false));
        List folders = makeList(root.getFolders(false));
        List datas = makeList(root.getData(false));
        
        fsAssert("Expected that numbre of children equals number of folders + number of datas ",
        childs.size() == (folders.size() + datas.size()));
    }
    
    /** Test of getFolders method, of class org.openide.filesystems.FileObject. */
    public void  testGetFolders2() {
        checkSetUp();
        
        List childs = makeList(root.getChildren(true));
        List folders = makeList(root.getFolders(true));
        List datas = makeList(root.getData(true));
        
        fsAssert("Expected that numbre of children equals number of folders + number of datas ",
        childs.size() == (folders.size() + datas.size()));
    }
    
    /** Test of getFolders method, of class org.openide.filesystems.FileObject. */
    public void  testGetFolders3() {
        checkSetUp();
        
        for (FileObject fo : makeList(root.getFolders(true))) {
            fsAssert("getData should return FileObjects that return isFolder () == true and isData () == false",
                     fo.isFolder() && !fo.isData());
        }
    }
    
    /** Test of getData method, of class org.openide.filesystems.FileObject. */
    public void  testGetData() {
        checkSetUp();
        FileObject file1 = getTestFile1(root);
        FileObject file2 = getTestFile2(root);
        
        List list= makeList(root.getData(false));
        fsAssert("Expected that enumeration will include FileObjects according to TestSetup",list.contains(file1));
        fsAssert("Expected that enumeration will include FileObjects according to TestSetup",list.contains(file2));
    }
    
    /** Test of getData method, of class org.openide.filesystems.FileObject. */
    public void  testGetData1() {
        checkSetUp();
        
        for (FileObject fo : makeList(root.getData(true))) {
            fsAssert("getData should return FileObjects that return isFolder () == false and isData () == true",
                     !fo.isFolder() && fo.isData());
        }
    }


    public void testNbfsTransformation () throws FileStateInvalidException{
        checkSetUp();
        // additional check
        String sysName = fs.getSystemName();
        if (sysName == null || sysName.length() == 0)
            return;
        if (Repository.getDefault().findFileSystem(fs.getSystemName()) == null)
            Repository.getDefault().addFileSystem(fs);

        try {
            FileObject file1 = getTestFile1(root);
            URL u = FileURL.encodeFileObject(file1);
            FileObject  file2 = FileURL.decodeURL(u);
            fsAssert("Nbfs check: both files should be equal: " + file1 + " | "  + file2 + " url: " + u + " fs: " + fs.getSystemName(), file1 == file2);
        } finally {
            Repository.getDefault().removeFileSystem(fs);            
        }
    }

    public void testNbfsTransformation2() throws FileStateInvalidException, IOException, SAXException, ParserConfigurationException {
        checkSetUp();
        // additional check
        if (fs.isReadOnly() || root.isReadOnly()) return;
        String sysName = fs.getSystemName();
        if (sysName == null || sysName.length() == 0)
            return;
        if (Repository.getDefault().findFileSystem(fs.getSystemName()) == null)
            Repository.getDefault().addFileSystem(fs);

        
        InputStream inputStream = null;
        try {
            FileObject test = getTestFolder1(root);
            FileObject f = test.createData("layer.xml");
            createSimpleXML(f);
            SAXParserFactory pFactory = SAXParserFactory.newInstance();
            pFactory.setValidating (false);
            Parser p = pFactory.newSAXParser().getParser();
            p.setDocumentHandler(new HandlerBase());
            URL u = f.getURL();
            p.parse(u.toExternalForm());
            //
            byte[] b = new byte[10];
            inputStream = u.openConnection().getInputStream();
            inputStream.read(b);
            fsAssert("Nbfs check: unexpected content ", (new String (b)).startsWith("<?xml"));
        } finally {
            Repository.getDefault().removeFileSystem(fs);
            if (inputStream != null) inputStream.close();
        }
    }

    private void createSimpleXML(FileObject f) throws IOException {
        FileLock fLock = f.lock();
        OutputStream os = f.getOutputStream(fLock);
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("<?xml version=\"1.0\"?>");
            sb.append("<filesystem>");
            sb.append("</filesystem>");
            os.write(sb.toString().getBytes());
            os.close();
            fLock.releaseLock();
        } finally {
            if (os != null) os.close();
        }
    }

    public void  testSetAttrWithNullValue () throws IOException {
        checkSetUp();
        // MFS stores VoidValue instead of null, which should
        // be also fixed and tested, but in standalone test
        if (fs instanceof MultiFileSystem) return;
        FileObject file1 = getTestFile1(root);
        File f1 = getNbAttrs(file1);

        if (f1 != null) {
            file1.setAttribute("key", "value");
            file1.setAttribute("key", null);

            fsAssert(".nbattrs shouldn't exist: " + f1.getAbsolutePath(), !f1.exists() );
        }
    }

    private File getNbAttrs(FileObject file1) {
        File f1 = FileUtil.toFile(file1);
        if (f1 == null) return null;

        return new File (f1.getParentFile(), ".nbattrs");
    }

    /** Test of getFileObject method, of class org.openide.filesystems.FileObject. */
    public void  testGetFileObject() {
        checkSetUp();
        FileObject file1 = getTestFile1(root);
        FileObject file2 = getTestFile2(root);
        FileObject fold1 = getTestFolder1(root);
        FileObject fold2 = getTestFolder2(root);
        
        
        fsAssert("Result doesn`t correspond to TestSetup",root.getFileObject(file1.getName(),file1.getExt()).equals(file1));
        fsAssert("Result doesn`t correspond to TestSetup",root.getFileObject(file2.getName(),file2.getExt()).equals(file2));
        fsAssert("Result doesn`t correspond to TestSetup",root.getFileObject(fold1.getName()).equals(fold1));
        fsAssert("Result doesn`t correspond to TestSetup",root.getFileObject(fold2.getName()).equals(fold2));
    }

    public void  testGetFileObject2() {
        checkSetUp();
        FileObject fold1 = getTestFolder1(root);
        FileObject fold2 = getTestFolder1(fold1);
        
        FileObject file1 = getTestFile1(fold2);
        FileObject file2 = getTestFile2(fold2);


        assertEquals("",FileUtil.getRelativePath(root, root));        
        assertEquals(null,FileUtil.getRelativePath(file1, root));        
        
        assertEquals(file1, root.getFileObject(FileUtil.getRelativePath(root, file1)));
        assertEquals(file2, root.getFileObject(FileUtil.getRelativePath(root, file2)));        

        assertEquals(file1, fold1.getFileObject(FileUtil.getRelativePath(fold1, file1)));
        assertEquals(file2, fold1.getFileObject(FileUtil.getRelativePath(fold1, file2)));                
        
        assertEquals(file1, fold2.getFileObject(FileUtil.getRelativePath(fold2, file1)));
        assertEquals(file2, fold2.getFileObject(FileUtil.getRelativePath(fold2, file2)));
    }
    
    //////////////////////////    
    /** Test of getFileSystem method, of class org.openide.filesystems.FileObject. */
    public void testGetFileSystem() throws FileStateInvalidException {
        checkSetUp();

        fsAssert ("FileObject should return his FileSystem",
        root.getFileSystem().equals(fs));
        fsAssert ("FileObject should return his FileSystem",
        getTestFolder1 (root).getFileSystem().equals(fs));        
        fsAssert ("FileObject should return his FileSystem",
        getTestFile1 (root).getFileSystem().equals(fs));                
    }
    
    /** Test of getParent method, of class org.openide.filesystems.FileObject. */
    public void testGetParent() {
        checkSetUp();
        
        FileObject folder1 = getTestFolder1 (root);
        FileObject file1 = getTestFile1(folder1);        
        fsAssert ("Unexpected parent",file1.getParent().equals(folder1));
        fsAssert ("Unexpected parent",folder1.getParent().equals(root));
        /** Because of MasterFileSystemTest */
        if (root == fs.getRoot()) 
            fsAssert ("Parent of parent should be null",root.getParent() == null);                
    }
        

    /** Test of isFolder method, of class org.openide.filesystems.FileObject. */
    public void testIsFolder() {
        checkSetUp();
        
        FileObject folder1 = getTestFolder1 (root);
        FileObject folder2 = getTestFolder2 (root);
        FileObject file1 = getTestFile1 (root);
        FileObject file2 = getTestFile2 (root);
        
        fsAssert ("Expected to be folder",folder1.isFolder());
        fsAssert ("Expected to be folder",folder2.isFolder());        
        fsAssert ("Expected not to be folder",!file1.isFolder());        
        fsAssert ("Expected not to be folder",!file2.isFolder());                        
    }
    
    /** Test of lastModified method, of class org.openide.filesystems.FileObject. */    
    //public void testLastModified() {}

        
    /** Test of isRoot method, of class org.openide.filesystems.FileObject. */
    public void testIsRoot() {
        if (fs.getRoot() != root) return;
        checkSetUp();
        FileObject testedRoot = root;
        FileObject folder1 = getTestFolder1 (testedRoot);
        FileObject folder2 = getTestFolder2 (testedRoot);
        FileObject file1 = getTestFile1 (testedRoot);
        FileObject file2 = getTestFile2 (testedRoot);
        
        fsAssert ("Expected to be root",testedRoot.isRoot());
        fsAssert ("Expected not to be root",!folder1.isRoot());                
        fsAssert ("Expected not to be root",!folder2.isRoot());        
        fsAssert ("Expected not to be root",!file1.isRoot());        
        fsAssert ("Expected not to be root",!file2.isRoot());                        
    }
    
    /** Test of isData method, of class org.openide.filesystems.FileObject. */
    public void testIsData() {
        checkSetUp();
        
        FileObject folder1 = getTestFolder1 (root);
        FileObject folder2 = getTestFolder2 (root);
        FileObject file1 = getTestFile1 (root);
        FileObject file2 = getTestFile2 (root);
        
        fsAssert ("Expected not to be data",!root.isData());
        fsAssert ("Expected not to be data",!folder1.isData());                
        fsAssert ("Expected not to be data",!folder2.isData());        
        fsAssert ("Expected to be data",file1.isData());        
        fsAssert ("Expected to be data",file2.isData());                                
    }
        

    /** Test of isValid method, of class org.openide.filesystems.FileObject. */    
    public void testIsValid2 () {
        checkSetUp();
        
        FileObject folder1 = getTestFolder1 (root);
        FileObject folder2 = getTestFolder2 (root);
        FileObject file1 = getTestFile1 (root);
        FileObject file2 = getTestFile2 (root);
        
        fsAssert ("Expected to be valid",root.isValid());
        fsAssert ("Expected to be valid",folder1.isValid());                
        fsAssert ("Expected to be valid",folder2.isValid());        
        fsAssert ("Expected be valid",file1.isValid());        
        fsAssert ("Expected be valid",file2.isValid());                                
        
        FileLock lock1 = null;
        FileLock lock2 = null; 
        FileLock lockf1 = null;
        FileLock lockf2 = null;
        try {
            lock1 = folder1.lock();
            lock2 = folder2.lock();            
            lockf1 = file1.lock();
            lockf2 = file2.lock();            
            
            folder1.delete (lock1);
            folder2.delete (lock2);            
            file1.delete (lockf1);
            file2.delete (lockf2);            
            
            fsAssert ("Expected not to be valid",!folder1.isValid());                
            fsAssert ("Expected not to be valid",!folder2.isValid());        
            fsAssert ("Expected not to be valid",!file1.isValid());        
            fsAssert ("Expected not to be valid",!file2.isValid());                                
            
            
        } catch (IOException iex) {
            fsAssert("FileObject could not be deleted. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());            
            return;
            
        } finally {
          if (lock1 != null)   lock1.releaseLock();
          if (lock2 != null)   lock2.releaseLock();          
          if (lockf1 != null)   lockf1.releaseLock();
          if (lockf2 != null)   lockf2.releaseLock();                    
        }        
    }
    
    /** Test of delete method, of class org.openide.filesystems.FileObject. */    
    public void testDelete() throws IOException {
        checkSetUp();
        
        FileObject folder1 = getTestFolder1 (root);
        FileObject file1 = getTestFile1 (root);
        
        
        FileLock lock1 = null;
        FileLock lockf1 = null;
        
        /** delete first time*/
        try {
            lock1 = folder1.lock();
            lockf1 = file1.lock();
            
            folder1.delete (lock1);
            file1.delete (lockf1);
                        
        } catch (IOException iex) {
            fsAssert("FileObject could not be deleted. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());            
            return;
            
        } finally {
          if (lock1 != null)   lock1.releaseLock();
          if (lockf1 != null)   lockf1.releaseLock();
        }                

        fsAssert ("Expected not to be valid",!folder1.isValid());                
        fsAssert ("Expected not to be valid",!file1.isValid());        

        /** delete second time*/        
        try {
            lock1 = folder1.lock();
            lockf1 = file1.lock();
            
            folder1.delete (lock1);
            file1.delete (lockf1);
                        
        } catch (IOException iex) {
            return;
            
        } finally {
          if (lock1 != null)   lock1.releaseLock();
          if (lockf1 != null)   lockf1.releaseLock();
        }                
    }

    public void testDelete2() throws Exception {
        checkSetUp();
        
        FileObject folder = getTestFolder1 (root);
        /** delete first time*/
        try {
            FileObject testFo = FileUtil.createFolder(folder,"testDelete2/subTest2");
            assertNotNull(testFo);
            
            FileObject newFolder = testFo.createFolder("subTest");
            assertNotNull(newFolder);            
            assertEquals(1,testFo.getChildren().length);

            newFolder.delete();
            assertFalse(newFolder.isValid());
            assertEquals(0,testFo.getChildren().length);
            implOfTestGetFileObjectForSubversion(testFo, newFolder.getNameExt());                        
        } catch (IOException iex) {
            fsAssert("FileObject could not be deleted. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());            
            return;
        }
            
    }

    public static void implOfTestGetFileObjectForSubversion(final FileObject folder, final String childName) throws InterruptedException {        
        final List l = new ArrayList();
        folder.addFileChangeListener(new FileChangeAdapter(){
            @Override
            public void fileFolderCreated(FileEvent fe) {
                l.add(fe.getFile());
                synchronized(l) {
                    l.notifyAll();
                }
            }                
        });
        FileObject child = folder.getFileObject(childName);
        synchronized(l) {
            l.wait(1000);
        }
        if (l.size() == 0) {
            assertNull(child);        
        } else {
            assertNotNull(child);        
        }
    }

    public void testRecursiveListener() throws IOException {
        checkSetUp();

        FileObject folder1 = getTestFolder1 (root);
        /** delete first time*/
        try {
            FileObject obj = FileUtil.createData(folder1, "my/sub/children/children.java");
            FileObject sub = obj.getParent().getParent();

            class L implements FileChangeListener {
                StringBuilder sb = new StringBuilder();

                public void fileFolderCreated(FileEvent fe) {
                    sb.append("FolderCreated");
                }

                public void fileDataCreated(FileEvent fe) {
                    sb.append("DataCreated");
                }

                public void fileChanged(FileEvent fe) {
                    sb.append("Changed");
                }

                public void fileDeleted(FileEvent fe) {
                    sb.append("Deleted");
                }

                public void fileRenamed(FileRenameEvent fe) {
                    sb.append("Renamed");
                }

                public void fileAttributeChanged(FileAttributeEvent fe) {
                    sb.append("AttributeChanged");
                }

                public void assertMessages(String txt, String msg) {
                    assertEquals(txt, msg, sb.toString());
                    sb.setLength(0);
                }
            }
            L recursive = new L();
            L flat = new L();

            sub.addFileChangeListener(flat);
            sub.addRecursiveListener(recursive);

            FileObject fo = obj.getParent().createData("sibling.java");

            flat.assertMessages("No messages in flat mode", "");
            recursive.assertMessages("Creation", "DataCreated");

            fo.setAttribute("jarda", "hello");

            flat.assertMessages("No messages in flat mode", "");
            recursive.assertMessages("attr", "AttributeChanged");

            final OutputStream os = fo.getOutputStream();
            os.write(10);
            os.close();

            flat.assertMessages("No messages in flat mode", "");
            recursive.assertMessages("written", "Changed");

            fo.delete();

            flat.assertMessages("No messages in flat mode", "");
            recursive.assertMessages("gone", "Deleted");

            FileObject subdir = sub.createFolder("testFolder");

            flat.assertMessages("Direct Folder notified", "FolderCreated");
            recursive.assertMessages("Direct Folder notified", "FolderCreated");

            subdir.createData("subchild.txt");

            recursive.assertMessages("SubFolder's change notified", "DataCreated");
            flat.assertMessages("SubFolder's change not important", "");

            sub.getParent().createData("unimportant.txt");

            flat.assertMessages("No messages in flat mode", "");
            recursive.assertMessages("No messages in recursive mode", "");

            sub.removeRecursiveListener(recursive);

            sub.createData("test.data");

            flat.assertMessages("Direct file notified", "DataCreated");
            recursive.assertMessages("No longer active", "");

            WeakReference<L> ref = new WeakReference<L>(recursive);
            recursive = null;
            assertGC("Listener can be GCed", ref);

        } catch (IOException iex) {
            if (fs.isReadOnly() || root.isReadOnly()) return;
            throw iex;
        } finally {
        }
    }

    
    /** Test of delete method, of class org.openide.filesystems.FileObject. */    
    public void testCreateDeleteFolderCreate () throws IOException {
        checkSetUp();
        
        FileObject folder1 = getTestFolder1 (root);
        FileObject file1 = getTestFile1 (root);
        
        
        /** delete first time*/
        try {
            FileObject obj = FileUtil.createData (folder1, "my/sub/children/children.java");
            FileObject fo = folder1.getFileObject ("my");
            
            assertNotNull (fo);
            assertTrue (fo.isValid ());
            assertTrue (fo.isFolder ());
            assertTrue (obj.isValid ());
            
            fo.delete ();
            
            assertFalse ("Not valid anymore", fo.isValid ());
            assertFalse ("Neither the data file", obj.isValid ());

            FileObject newObj = FileUtil.createData (folder1, "my/sub/children/children.java");
            
            assertTrue ("old data file is not valid", !obj.isValid () || obj == newObj);
            assertTrue ("New one is ", newObj.isValid ());
            assertEquals ("They have the same name", newObj.getPath (), obj.getPath ());
            
        } catch (IOException iex) {
            if (fs.isReadOnly() || root.isReadOnly()) return;
            throw iex;
        } finally {
        }                
    }
    
    /** Test of getAttribute method, of class org.openide.filesystems.FileObject. */        
    public void testGetAttribute() {
        checkSetUp();
        
        FileObject file1 = getTestFile1 (root);
        FileObject file2 = getTestFile2 (root);        
        
        fsAssert ("If attributes are not set getAttribute should return null",
        file1.getAttribute("UnexpectedName") == null);
        fsAssert ("If attributes are not set getAttribute should return null",
        file2.getAttribute("UnexpectedName") == null);        
        try {
            file1.setAttribute("attrName","attrName");
            String value = (String)file1.getAttribute("attrName");
            fsAssert ("setAttibute or getAttribute failure: " + value,value != null);
            fsAssert ("setAttibute or getAttribute failure: " + value,value.equals("attrName"));            
        } catch (IOException iex) {
            fsAssert("Attributes could not be attached to FileObject. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());            
        }
        
    }
    
    /** Test of setAttribute method, of class org.openide.filesystems.FileObject. */        
    public void testSetAttribute() throws IOException {
        checkSetUp();
        
        FileObject file1 = getTestFile1 (root);
        FileObject file2 = getTestFile2 (root);        
        
        try {
            file1.setAttribute("attrName","value");
            file2.setAttribute("attrName","value");            
            fsAssert ("setAttibute or getAttribute failure",
            file1.getAttribute("attrName").equals(file2.getAttribute("attrName")));
        } catch (IOException iex) {
            fsAssert("Attributes could not be attached to FileObject. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());            
        }
        
    }
    
    /** Test of getAttributes method, of class org.openide.filesystems.FileObject. */        
    public void testGetAttributes() {
        checkSetUp();
        String[] names = new String[] {"name1","name2","name3","name4"};        
        List namesList = Arrays.asList(names);
        List<String> compareList = new ArrayList<String> ();                
        FileObject file1 = getTestFile1 (root);
                
        try {
            for (int i = 0; i < names.length; i++) 
                file1.setAttribute(names[i],"value");
                        
             Enumeration en = file1.getAttributes();
             while (en.hasMoreElements()) {
                String name = (String)en.nextElement();
                fsAssert ("Expected getAttributes return this key: "+ name,namesList.contains(name));
                compareList.add (name);
             }            
             fsAssert ("All keys should be enumerated: " + namesList.size() +"|"+compareList.size(),namesList.size() == compareList.size());
        } catch (IOException iex) {
            fsAssert("Attributes could not be attached to FileObject. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());            
        }                
    }

    /** Test of createFolder  method, of class org.openide.filesystems.FileObject. */                
    public void testCreateFolder() throws IOException {
        checkSetUp();
        
        FileObject fo = null;
        try {
            fo = root.createFolder("Folder");
        } catch (IOException iex) {
            fsAssert ("Writable fs should allow to create new folder",fs.isReadOnly() || root.isReadOnly());             
            return;
        }

        fsAssert ("Writable fs should allow to create new folder",fo != null);        
        
        try {
            root.createFolder("Folder");
        } catch (IOException iex) {
          return;   //OK
        }
        
        fsFail  ("createData should fire exception if file already exists");                        
    }

    /** Test of createFolder  method, of class org.openide.filesystems.FileObject. */                
    public void testCreateFolder2() throws IOException {
        checkSetUp();
        
        FileObject fo = null;
        try {
            fo = root.createFolder("Folder.With.Dot");
        } catch (IOException iex) {
            fsAssert ("Writable fs should allow to create new folder",fs.isReadOnly() || root.isReadOnly());             
            return;
        }

        fsAssert ("Writable fs should allow to create new folder",fo != null);                
    }

    /** Test of createFolder method, of class org.openide.filesystems.FileObject.
     * Folder name with slash is not allowed (see #153000). */
    public void testCreateFolderWithSlash() throws IOException {
        checkSetUp();
        String[] folderNames = {"Folder/", "/Folder", "\\Folder", "Folder\\", "Fol/der", "Fol\\der"};
        for (String folderName : folderNames) {
            try {
                root.createFolder(folderName);
                fsFail("Folder name with slash should cause exception (" + folderName + ").");
            } catch (IOException ioe) {
                // OK - thrown from AbstractFileObject
            } catch (IllegalArgumentException iae) {
                // OK - thrown from FolderObj
            }
        }
    }

    /** Test of createData  method, of class org.openide.filesystems.FileObject. */            
    public void testCreateData () throws IOException {
        checkSetUp();
        
        FileObject fo = null;
        try {
            fo = root.createData("Data","tst");
        } catch (IOException iex) {
            fsAssert ("Writable fs should allow to create new data",fs.isReadOnly() || root.isReadOnly());             
            return;
        }
                
        fsAssert ("Writable fs should allow to create new data",fo != null);        
        
        try {
            root.createData("Data","tst");
        } catch (IOException iex) {
          return;   //OK
        }
        
        fsFail  ("createData should fire exception if file already exists");                
    }

    /** Test of createData  method, of class org.openide.filesystems.FileObject. */            
    public void testCreateData2 () throws IOException {
        checkSetUp();
        
        FileObject fo = null;
        try {
            fo = root.createData("Data.With.Dot","tst");
        } catch (IOException iex) {
            fsAssert ("Writable fs should allow to create new data",fs.isReadOnly() || root.isReadOnly());             
            return;
        }
                
        fsAssert ("Writable fs should allow to create new data",fo != null);        
        
    }
    
    /** Test of getSize  method, of class org.openide.filesystems.FileObject. */                
    public void testGetSize() {
        checkSetUp();
        
        String testStr = "Content of file. Size of this file is important";
        FileObject fo1 = getTestFile1 (root);
        
        try {
            writeStr(fo1, testStr);
        } catch (IOException iex) {
            fsAssert("File was modified",
            fs.isReadOnly() || fo1.isReadOnly()) ;
            return;
        }
        fsAssert ("Unexpected size of file",fo1.getSize() == testStr.length());
    }

    /** Test of getSize  method, of class org.openide.filesystems.FileObject. */                
    public void testGetSize1() {
        checkSetUp();
        FileObject fo1 = getTestFile1 (root);
                
        fsAssert ("Size of file should be >= 0",fo1.getSize() >= 0);
    }
    
    /** Test of getInputStream()  method, of class org.openide.filesystems.FileObject. */                    
    public void testGetInputStream() throws java.io.FileNotFoundException, IOException {
        checkSetUp();
        
        FileObject fo1 = getTestFile1 (root);
        InputStream is = null;
        FileLock lock = null;
        try {
            is = fo1.getInputStream();            
        } catch (IOException iex) {            
            fsAssert  ("Expected that FS provides InputStream ",fo1.getSize () == 0);
        } finally {
            if (is != null) is.close();                                    
        }
        
        try {
            lock = fo1.lock ();
            fo1.delete(lock);
        } catch (IOException iex) {
            fsAssert("FileObject shopuld be allowd to be modified.",
            fs.isReadOnly() || fo1.isReadOnly()) ;
            return;
        } finally {
            if (lock != null) lock.releaseLock();

        }
        
        fsAssert ("After delete should be invalid",!fo1.isValid());        
    }
    public void testAsBytesAndString() throws java.io.FileNotFoundException, IOException {
        checkSetUp();

        FileObject fo1 = getTestFile1 (root);
        try {
            OutputStream os = fo1.getOutputStream();
            String txt = "Ahoj\nJak\nSe\nMas";
            os.write(txt.getBytes("UTF-8"));
            os.close();
            byte[] arr = fo1.asBytes();
            assertNotNull("Arrays is read", arr);
            assertEquals("Right length bytes", txt.length(), arr.length);
            assertEquals(txt, new String(arr, "UTF-8"));
            assertEquals(txt, fo1.asText("UTF-8"));

            ArrayList<String> all = new ArrayList<String>();
            List<String> lines = fo1.asLines("UTF-8");
            for (String l : lines) {
                // it is possible to rewrite the content, if the file is small
                fo1.getOutputStream().close();
                all.add(l);
            }
            assertEquals("Four lines: " + txt, 4, all.size());
            assertEquals("Computed remain the same as read", all, lines);

            ListIterator<String> it = lines.listIterator(all.size());
            for (int i = all.size(); i > 0; ) {
                i--;
                assertEquals("Right index", i, it.previousIndex());
                assertEquals("Ith " + i, all.get(i), it.previous());
            }
        } catch (IOException iex) {
            fsAssert  ("Expected that FS provides InputStream ",fo1.getSize () == 0);
        }
        FileLock lock = null;

        try {
            lock = fo1.lock ();
            fo1.delete(lock);
        } catch (IOException iex) {
            fsAssert("FileObject shopuld be allowd to be modified.",
            fs.isReadOnly() || fo1.isReadOnly()) ;
            return;
        } finally {
            if (lock != null) lock.releaseLock();
        }
        fsAssert ("After delete should be invalid",!fo1.isValid());
    }
    public void testBigFileAndAsString() throws Exception {
        checkSetUp();

        FileObject fo1 = getTestFile1 (root);
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("Hi how are you, boy! ");
            }
            sb.append("\n");

            OutputStream os = fo1.getOutputStream();
            for (int i = 0; i < 10; i++) {
                os.write(sb.toString().getBytes("UTF-8"));
            }
            os.close();
            if (64 * 1024 > fo1.getSize()) {
                fail("We need to generate big file: " + fo1.getSize());
            }

            OutputStream tmp = null;
            FileLock lock = null;
            int acquire = 3;
            List<String> lines = fo1.asLines("UTF-8");
            LinkedList<String> reverse = new LinkedList<String>();
            for (int i = lines.size() - 1; i >= 0; i--) {
                reverse.add(0, lines.get(i));
            }
            Reference<Object> ref = new WeakReference<Object>(lines);
            lines = null;
            assertGC("The list of strings can be GCed", ref);

            ArrayList<String> all = new ArrayList<String>();
            for (String l : fo1.asLines("UTF-8")) {
                int cnt = all.size();
                if (cnt % 100 == 0 && acquire-- > 0) {
                    try {
                        lock = fo1.lock();
                        tmp = fo1.getOutputStream(lock);
                        tmp.write(0);
                        fail("The input stream is open for big files. cnt = " + cnt);
                    } catch (IOException ex) {
                        // OK
                    } finally {
                        lock.releaseLock();
                        if (tmp != null) {
                            tmp.close();
                        }
                    }
                }
                all.add(l);
            }
            assertEquals("Some lines: ", 10, all.size());
            assertEquals("Generated same as read", all, fo1.asLines());
            assertEquals("Normal and reverse are same", all, reverse);
        } catch (IOException iex) {
            fsAssert  ("Expected that FS provides InputStream ",fo1.getSize () == 0);
        }

        final int[] finalizeCalled = { 0 };
        Reference<Object> ref = new WeakReference<Object>(new Object() {
            @Override
            protected void finalize() throws Throwable {
                synchronized (finalizeCalled) {
                    finalizeCalled[0]++;
                    finalizeCalled.notifyAll();
                }
            }
        });
        assertGC("Reference can disapper", ref);
        synchronized (finalizeCalled) {
            int cnt = 5;
            while (cnt-- > 0 && finalizeCalled[0] == 0) {
                finalizeCalled.wait();
            }
            assertEquals("Finalizers has been run", 1, finalizeCalled[0]);
        }




        try {
            OutputStream os = fo1.getOutputStream();
            os.write("Ahoj".getBytes());
            os.close();
            
            FileLock lock = fo1.lock ();
            fo1.delete(lock);
        } catch (IOException iex) {
            fsAssert("FileObject shopuld be allowd to be modified.",
            fs.isReadOnly() || fo1.isReadOnly()) ;
            return;
        }
        fsAssert ("After delete should be invalid",!fo1.isValid());
    }

    /** Test of getOutputStream()  method, of class org.openide.filesystems.FileObject. */                        
    public void testGetOutputStream() throws java.io.IOException {
        checkSetUp();
        
        FileObject fo1 = getTestFile1 (root);
        OutputStream os = null;
        FileLock lock = null;
        
        try {
            lock = fo1.lock ();
            os = fo1.getOutputStream(lock);                        
            os.close();
            os = null;
            fo1.delete(lock);            
        } catch (IOException iex) {
            fsAssert("FileObject should be allowed to be modified.",
            fs.isReadOnly() || fo1.isReadOnly()) ;
            return;
        } finally {
            if (lock != null) lock.releaseLock();
            if (os != null) os.close();
        }
        
        fsAssert ("After should be invalid",!fo1.isValid());
        
    }

    public void testGetOutputStream1() throws java.io.IOException {
        checkSetUp();
        
        FileObject fo1 = getTestFile1 (root);
        registerDefaultListener(fo1.getParent());        
        OutputStream os = null;
        FileLock lock = null;
        
        try {
            lock = fo1.lock ();
            os = fo1.getOutputStream(lock);                        
            fileChangedAssert("No event should be fired",0);                                                
            os.write(new byte[] {'a','b'});
            os.close ();
            os = null;
            fileChangedAssert("Only one event should be fired",1);                                    
        } catch (IOException iex) {
            fsAssert("FileObject should be allowed to be modified.",            
            fs.isReadOnly() || fo1.isReadOnly()) ;
            return;
        } finally {
            if (lock != null) lock.releaseLock();
            if (os != null) os.close();
        }        
    }

    public void testGetOutputStream1_FS() throws java.io.IOException {
        checkSetUp();        
        FileObject fo1 = getTestFile1 (root);
        registerDefaultListener(testedFS);        
        OutputStream os = null;
        FileLock lock = null;
        
        try {
            lock = fo1.lock ();
            os = fo1.getOutputStream(lock);                        
            fileChangedAssert("No event should be fired",0);                                                
            os.write("alkdsakldsaklafdsaklfalkfaklfalkf".getBytes());
            os.close ();
            os = null;
            fileChangedAssert("Only one event should be fired",1);
            fo1.refresh(false);
            fileChangedAssert("Unexpected event",1);            
        } catch (IOException iex) {
            fsAssert("FileObject should be allowed to be modified.",            
            fs.isReadOnly() || fo1.isReadOnly()) ;
            return;
        } finally {
            if (lock != null) lock.releaseLock();
            if (os != null) os.close();
        }        
    }

    public void testOutputStream75826() throws IOException {
        checkSetUp();
        if (testedFS.isReadOnly()) return;
        FileObject testFo = getTestFile1(root);
        FileLock lock = testFo.lock();
        try {
            testFo.getOutputStream();
            fail();
        } catch (IOException ex) {
        }finally {
            lock.releaseLock();
        }
    }
        
    /** Test of isReadOnly()  method, of class org.openide.filesystems.FileObject. */                            
    public void testIsReadOnly() {
        FileObject file1 = getTestFile1 (root);        
        FileObject file2 = getTestFile2 (root);                

        fsAssert ("Expected that if fs is read-only than all files are read-only",fs.isReadOnly() == file1.isReadOnly());
        fsAssert ("Expected that if fs is read-only than all files are read-only",fs.isReadOnly() == file2.isReadOnly());
        
    }


    /** Test of lock()  method, of class org.openide.filesystems.FileObject. */                            
    public void testLock() throws IOException {
        checkSetUp();
        
        FileObject fo1 = getTestFile1 (root);
        assertFalse(fo1.isLocked());
        FileLock lock = null;
        try {
            lock = fo1.lock ();   
            assertTrue(fo1.isLocked());
        } catch (IOException iex) {
            fsAssert("FileObject could not be locked",            
            fs.isReadOnly() || fo1.isReadOnly() ) ;
            return;
        }
        try {
            fo1.lock ();
            fsFail ("FileAlreadyLockedException  should be fired");
        } catch (FileAlreadyLockedException fax) {
            return;
        } finally {
            if (lock != null) {
                lock.releaseLock();
                assertFalse(fo1.isLocked());
        }       
    }
    }

    public void testUseWrongLock()  throws IOException {
        if (!root.canWrite() || root.getFileSystem().isReadOnly()) {
            return;
        }
        checkSetUp();
        
        FileObject fo1 = getTestFile1 (root);
        FileObject fo2 = getTestFile2 (root);
        FileObject target = getTestFolder1(root);

        FileLock lock = null;
        try {
            lock = fo1.lock();
            try {
                fo2.delete(lock);
                fail();
            } catch (IOException ex) {}
            try {
                fo2.move(lock,target,"by","hi");
                fail();
            } catch (IOException ex) {}
            try {
                fo2.rename(lock,"hi","by");
                fail();
            } catch (IOException ex) {}
        } finally {
            if (lock != null) lock.releaseLock();
        }
    }


    /** Test of rename method, of class org.openide.filesystems.FileObject. */
    public void testRename() throws IOException {
        checkSetUp();
        FileObject fold = getTestFolder1(root);
        FileObject fo1 = getTestFile1(fold);
        FileObject fo2 = getTestFile2(fold);
        
        String attrName = "attrName";
        String value = "value56";
        FileLock lock = null;
        
        registerDefaultListener(fold);
        
        int hash = fo1.hashCode();
        try {
            fo1.setAttribute(attrName,value);
            fsAssert("attributes should be saved " + fo1.getAttribute(attrName),value.equals((String)fo1.getAttribute(attrName)) );            
            lock = fo1.lock();
            fo1.rename(lock,fo2.getExt(),fo2.getName());
        } catch (IOException iex) {
            fsAssert("expected rename will success on writable FS",
            fs.isReadOnly() ||  fo1.isReadOnly());
            return;
        } finally {
            if (lock != null) lock.releaseLock();   
        }
        fsAssert("attributes should be available too: " + fo1.getAttribute(attrName),value.equals((String)fo1.getAttribute(attrName)) );
        fsAssert ("",fold.getFileObject(fo1.getName(),fo1.getExt()) != null);
        fsAssert ("",fo1.getName().equals(fo2.getExt()) && fo1.getExt().equals(fo2.getName()));        
        this.fileRenamedAssert("File was actually renamed.",1);
        assertEquals("Hashcode needs to stay", hash, fo1.hashCode());
    }
    

    /** Test of rename method, of class org.openide.filesystems.FileObject. */
    public void testRename_FS() throws IOException {
        checkSetUp();
        FileObject fold = getTestFolder1(root);
        FileObject fo1 = getTestFile1(fold);
        FileObject fo2 = getTestFile2(fold);
        
        String attrName = "attrName";
        String value = "value56";
        FileLock lock = null;
        
        registerDefaultListener(testedFS);
        
        try {
            fo1.setAttribute(attrName,value);
            fsAssert("attributes should be saved " + fo1.getAttribute(attrName),value.equals((String)fo1.getAttribute(attrName)) );            
            lock = fo1.lock();
            fo1.rename(lock,fo2.getExt(),fo2.getName());
        } catch (IOException iex) {
            fsAssert("expected rename will success on writable FS",
            fs.isReadOnly() ||  fo1.isReadOnly());
            return;
        } finally {
            if (lock != null) lock.releaseLock();   
        }
        fsAssert("attributes should be available too: " + fo1.getAttribute(attrName),value.equals((String)fo1.getAttribute(attrName)) );
        fsAssert ("",fold.getFileObject(fo1.getName(),fo1.getExt()) != null);
        fsAssert ("",fo1.getName().equals(fo2.getExt()) && fo1.getExt().equals(fo2.getName()));        
        this.fileRenamedAssert("File was actually renamed.",1);
    }
    
    public void testRename2() throws IOException {
        checkSetUp();
        FileObject fold = getTestFolder1(root);
        FileObject fo1 = getTestFolder1(fold);
        
        String attrName = "attrName";
        String value = "value";
        FileLock lock = null;
        
        registerDefaultListener(fold);
        
        try {
            fo1.setAttribute(attrName,value);
            fsAssert("attributes should be saved " + fo1.getAttribute(attrName),
            value.equals((String)fo1.getAttribute(attrName)) );            
            lock = fo1.lock();
            fo1.rename(lock,"testXY","");
        } catch (IOException iex) {
            fsAssert("expected rename will success on writable FS",fs.isReadOnly() || fo1.isReadOnly());
            return;
        } finally {
            if (lock != null) lock.releaseLock();   
        }
        fsAssert("attributes should be available too: " + fo1.getAttribute(attrName),
        value.equals((String)fo1.getAttribute(attrName)) );
        fsAssert ("",fold.getFileObject(fo1.getName(),fo1.getExt()) != null);
        fsAssert ("",fo1.getName().equals("testXY") && fo1.getExt().equals(""));
        this.fileRenamedAssert("File was actually renamed.",1);
    }

    public void testRename2_FS() throws IOException {
        checkSetUp();
        FileObject fold = getTestFolder1(root);
        FileObject fo1 = getTestFolder1(fold);
        
        String attrName = "attrName";
        String value = "value";
        FileLock lock = null;
        
        registerDefaultListener(testedFS);
        
        try {
            fo1.setAttribute(attrName,value);
            fsAssert("attributes should be saved " + fo1.getAttribute(attrName),
            value.equals((String)fo1.getAttribute(attrName)) );            
            lock = fo1.lock();
            fo1.rename(lock,"testXY","");
        } catch (IOException iex) {
            fsAssert("expected rename will success on writable FS",fs.isReadOnly() || fo1.isReadOnly());
            return;
        } finally {
            if (lock != null) lock.releaseLock();   
        }
        fsAssert("attributes should be available too: " + fo1.getAttribute(attrName),
        value.equals((String)fo1.getAttribute(attrName)) );
        fsAssert ("",fold.getFileObject(fo1.getName(),fo1.getExt()) != null);
        fsAssert ("",fo1.getName().equals("testXY") && fo1.getExt().equals(""));        
        this.fileRenamedAssert("File was actually renamed.",1);
    }
    
    /** Test of addFileChangeListener method, of class org.openide.filesystems.FileObject. */        
    public void testAddFileChangeListener() {
        checkSetUp();
        FileObject fo1 = getTestFile1(root);
        String value = "value";
        
        registerDefaultListener(fo1);
        registerDefaultListener(fo1);
        registerDefaultListener(fo1);
        registerDefaultListener(fo1);
        registerDefaultListener(fo1);        
        try {
            fo1.setAttribute("attrName",value);
            fileAttributeChangedAssert("",5);
            fsAssert("",((String)fo1.getAttribute("attrName")).equals(value));
        } catch (IOException iex) {
            fsAssert("Attribute could not be set. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileAttributeChangedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        }
    }

    /** Test of addFileChangeListener method, of class org.openide.filesystems.FileObject. */        
    public void testAddFileChangeListener_FS() {
        checkSetUp();
        FileObject fo1 = getTestFile1(root);
        String value = "value";
        
        registerDefaultListener(testedFS);
        registerDefaultListener(testedFS);
        registerDefaultListener(testedFS);
        registerDefaultListener(testedFS);
        registerDefaultListener(testedFS);        
        try {
            fo1.setAttribute("attrName",value);
            fileAttributeChangedAssert("",5);
            fsAssert("",((String)fo1.getAttribute("attrName")).equals(value));
        } catch (IOException iex) {
            fsAssert("Attribute could not be set. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileAttributeChangedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        }
    }
    
    /** Test of removeFileChangeListener method, of class org.openide.filesystems.FileObject. */
    public void testRemoveFileChangeListener() throws IOException  {
        checkSetUp();
        FileObject fo1 = getTestFile1(root);
        String value = "value";
        
        registerDefaultListener(fo1);
        FileChangeListener secondListener = createFileChangeListener ();
        fo1.addFileChangeListener(secondListener);
        try {
            fo1.setAttribute("attrName",value);
            fileAttributeChangedAssert("",2);
            fsAssert("",((String)fo1.getAttribute("attrName")).equals(value));
        } catch (IOException iex) {
            fsAssert("Attribute could not be set. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileAttributeChangedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        }

        fo1.removeFileChangeListener(secondListener);
        fo1.setAttribute("attrName","value2");
        fileAttributeChangedAssert("",3);
        fsAssert("",((String)fo1.getAttribute("attrName")).equals("value2"));        
    }
    
    
    
    /** Test of removeFileChangeListener method, of class org.openide.filesystems.FileObject. */
    public void testRemoveFileChangeListener_FS() throws IOException  {
        checkSetUp();
        FileObject fo1 = getTestFile1(root);
        String value = "value";
        
        registerDefaultListener(testedFS);
        FileChangeListener secondListener = createFileChangeListener ();
        fo1.addFileChangeListener(secondListener);
        try {
            fo1.setAttribute("attrName",value);
            fileAttributeChangedAssert("",2);
            fsAssert("",((String)fo1.getAttribute("attrName")).equals(value));
        } catch (IOException iex) {
            fsAssert("Attribute could not be set. So there was expected fs or fo are read-only",
            fs.isReadOnly() || root.isReadOnly());
            fileAttributeChangedAssert("fs or fo is read-only. So no event should be fired",0);
            return;
        }

        fo1.removeFileChangeListener(secondListener);
        fo1.setAttribute("attrName","value2");
        fileAttributeChangedAssert("",3);
        fsAssert("",((String)fo1.getAttribute("attrName")).equals("value2"));        
    }
    
    /** Test of setImportant method, of class org.openide.filesystems.FileObject. */
    public void testSetImportant() {
        /** Don`t know how to test it.*/
        checkSetUp();
        
        FileObject fo1 = getTestFile1 (root);
        fo1.setImportant(true);
        fo1.setImportant(false);        
    }
    
    
    
    /** Test of refresh method, of class org.openide.filesystems.FileObject. */
    public void  testRefresh() throws IOException{
        checkSetUp();
        FileObject fo = getTestFile1 (root);
        File f = FileUtil.toFile(fo);
        if (f == null) return;

        
        registerDefaultListener(fo);
        if (f.exists()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return;
            }
            fsAssert("delete failed",f.delete());
            fsAssert("file can't be created", f.createNewFile());            
        }
        fo.refresh();
        fileChangedAssert("unexpected count of events", 1);                
    }

    /** Test of refresh method, of class org.openide.filesystems.FileObject. */
    public void  testRefresh2 () throws IOException{
        checkSetUp();
        FileObject fo = getTestFile1 (root);
        File f = FileUtil.toFile(fo);
        if (f == null) return;
        fo.getParent().getChildren();
        
        registerDefaultListener(fo);
        if (f.exists()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return;
            }
            fsAssert("delete failed",f.delete());
            fsAssert("file can't be created", f.createNewFile());            
        }
        fo.getParent().refresh();
        fileChangedAssert("unexpected count of events", 1);                
    }
    
    public void  testRefresh3 () throws IOException{        
        String assertMessage = "unexpected count of events";
        checkSetUp();
        if (!root.canWrite() || fs.isReadOnly() || fs instanceof MultiFileSystem) {
            return;
        }
        fs.refresh(true);
        
        FileObject[] fos = new FileObject [2];
        fos[0] = getTestFile1 (root);
        fos[1] = root.createFolder("toDelFolder");        
        assertNotNull(fos[0]);
        assertNotNull(fos[1]);        
        
        File file = FileUtil.toFile(fos[0]);
        File folder = FileUtil.toFile(fos[1]);
        
        // for filesystems that don't provide toFile conversion
        if (file == null || folder == null) return;


        root.getChildren ();
        for (int i = 0; i < 4; i++) {
            assertTrue(file.exists());
            assertTrue(folder.exists());
            
            registerListenerForTestRefresh3(i);

            file.delete();
            refreshForTestRefresh3(i);
            assertNull(root.getFileObject(file.getName()));                        
            fileDeletedAssert(assertMessage, 1);
        
            folder.delete();
            refreshForTestRefresh3(i);
            assertNull(root.getFileObject(folder.getName()));                                    
            fileDeletedAssert(assertMessage, 2);
        

            new File(file.getParentFile(), file.getName()).createNewFile();
            refreshForTestRefresh3(i);
            assertNotNull(root.getFileObject(file.getName()));                                                            
            fileDataCreatedAssert(assertMessage + " : " + i, 1);

            new File(folder.getParentFile(), folder.getName()).mkdir();
            refreshForTestRefresh3(i);
            assertNotNull(root.getFileObject(folder.getName()));                                    
            fileFolderCreatedAssert(assertMessage, 1);
        }

        
    }

    private void refreshForTestRefresh3(int mode) {        
        switch (mode) {
            case 0:
                fs.refresh(true);
                break;
            case 1:
                root.refresh();
                break;
            case 2:
                fs.refresh(true);
                break;
            case 3:
                root.refresh();
                break;                    
                
        }
        
    }

    private void registerListenerForTestRefresh3(int mode) {
        switch (mode) {
            case 0:
                registerDefaultListener(fs);
                break;
            case 1:
                deregisterDefaultListener(fs);
                registerDefaultListener(root);
                break;
            case 2:
                deregisterDefaultListener(root);
                registerDefaultListener(root);                
                break;
            case 3:
                deregisterDefaultListener(root);
                registerDefaultListener(fs);                
                break;                    
                
        }
    }
    
    /** Test of getURL method, of class org.openide.filesystems.FileObject. */
    public void  testGetURL() throws Exception {
        checkSetUp();
        
        FileObject fo1 = getTestFile1 (root);
        URL url = null;
        url  = fo1.getURL();
        /** Only invalid files may fire FileStateInvalidException*/
        fsAssert ("Expected valid url",url != null);                
        
        // #39613: check that it actually works!
        // Note that since getURL now produces a file: URL for files with File's,
        // local files will pass this test. Using 'nbhost' they may not.
        FileObject f2 = getTestFile1(root);
        FileObject f1 = f2.getParent();
        assertNotNull("had a parent of " + f2, f1);
        URL u1 = f1.getURL();
        assertNotNull("had a URL for " + f1, u1);
        URI uri1 = new URI(u1.toExternalForm());
        String path1 = uri1.getPath();
        if (path1 != null) {
            assertTrue("path of " + uri1 + " ends with /", path1.endsWith("/"));
            String path2 = path1 + f2.getNameExt();
            assertNull("No query for " + uri1, uri1.getQuery());
            assertNull("No fragment for " + uri1, uri1.getFragment());
            URI uri2 = new URI(uri1.getScheme(), path2, null);
            Repository.getDefault().addFileSystem(fs); // so that fFO will work
            FileObject[] fos;
            try {
                fos = URLMapper.findFileObjects(uri2.toURL());
            } finally {
                Repository.getDefault().removeFileSystem(fs);
            }
            assertTrue("computed child URI " + uri2 + " is correct as is in: " + Arrays.asList(fos), Arrays.asList(fos).contains(f2));
        } else {
            // No path component in the URI; clearly cannot work with
            // it this way.
        }
    }
    
 
    /*#46885: File not refreshed in editor if modified externally the first time after an internal modification*/
    public void testExternalChange () throws Exception {        
        checkSetUp();
        if (!root.canWrite() || fs.isReadOnly()) {
            return;
        }
        
        
        FileObject fo1 = getTestFile1 (root);
        registerDefaultListener(fo1);
        File f = FileUtil.toFile(fo1);
        if (f == null) return;
        
        FileLock lck = fo1.lock();
        lck.releaseLock();
        Thread.sleep(2000);        
        FileOutputStream fos = new FileOutputStream(f);
        fos.close();
        fo1.refresh();
        fileChangedAssert("expected FileChangeListener: ", 1);        
    }
    
    protected String[] getResources(String testName) {
        if (res == null ) {
            res = new HashSet(Arrays.asList(resources));
            createResource("",0,3, true);
        }
        
        
        String[] retVal = new String[res.size()];
        res.toArray(retVal);
        return retVal;
    }
    
    private static void createResource(String prevLevel, int level, int maxLevel, boolean folder) {
        if (level < maxLevel && prevLevel.indexOf('.') == -1) {
            for (int i = 0; i < 2; i++) {
                createResource(prevLevel + FOLDER_CHILD + new Integer(i).toString(),level + 1, maxLevel, true);
                createResource(prevLevel + FILE_CHILD + new Integer(i).toString(),level + 1, maxLevel, false);
            }
        }
        
        //System.out.println(prevLevel);
        if (prevLevel.startsWith("/") && prevLevel.length() != 0)
            res.add(folder ? (prevLevel + "/") : prevLevel);
        
    }
    
    
    /** folder can be root or child */
    private FileObject getTestFile1(FileObject folder) {
        return getChild(folder, false, 0, FOLDER_CHILD_NAME, FILE_CHILD_NAME, FILE_CHILD_EXT);
    }
    
    private FileObject getTestFolder1(FileObject folder) {
        return getChild(folder, true, 0 ,FOLDER_CHILD_NAME, FILE_CHILD_NAME,FILE_CHILD_EXT);
    }
    
    private FileObject getTestFile2(FileObject folder) {
        return getChild(folder, false, 1, FOLDER_CHILD_NAME, FILE_CHILD_NAME, FILE_CHILD_EXT);
    }
    
    private FileObject getTestFolder2(FileObject folder) {
        return getChild(folder, true, 1 ,FOLDER_CHILD_NAME, FILE_CHILD_NAME,FILE_CHILD_EXT);
    }
    
    
    private FileObject getChild(FileObject folder, boolean isFolder, int fileNumber, String folderName, String fileName, String fileExt) {
        FileObject retVal;
        
        if (isFolder) {
            retVal = folder.getFileObject(folderName+new Integer(fileNumber).toString());
            fsTestFrameworkErrorAssert  ("Unexpected setUp behaviour: resource " + FOLDER_CHILD_NAME +" not found",retVal != null);
            fsTestFrameworkErrorAssert("Not really a folder: " + retVal.getPath(), retVal.isFolder());
            return retVal;
        }
        
        retVal = folder.getFileObject(fileName,fileExt+new Integer(fileNumber).toString());
        fsTestFrameworkErrorAssert  ("Unexpected setUp behaviour: resource " + FILE_CHILD_NAME +" not found",retVal != null);
        fsTestFrameworkErrorAssert("Not really a file: " + retVal.getPath(), retVal.isData());
        return retVal;
    }
    
    private static void writeStr(FileObject fo, String str) throws IOException {
        FileLock lock = fo.lock();
        OutputStream os = fo.getOutputStream(lock);
        try {
            os.write(str.getBytes());
        } finally {
            lock.releaseLock();
            if (os != null) os.close();
        }
        //os.close();
        
    }
    
    private String readStr(FileObject fo) throws IOException {
        InputStream is = fo.getInputStream();

        try {
            byte[] content = new byte[is.available()];
            is.read(content);
            is.close();
            return new String(content);
        } finally {
            if (is != null) is.close ();
        }
    }
    
    private void checkSetUp() {
        fsTestFrameworkErrorAssert  ("Unexpected setUp behaviour: fs == null", fs != null);
        fsTestFrameworkErrorAssert  ("Unexpected setUp behaviour: root == null: " + getResourcePrefix(), root != null);
    }
    
    private List<? extends FileObject> makeList(Enumeration<? extends FileObject> e) {
        List<FileObject> l = new LinkedList<FileObject>();
        while (e.hasMoreElements()) {
            l.add(e.nextElement());
        }
        return l;
    }
    
}
