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

package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObj;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.WriteLockUtils;
import org.netbeans.modules.masterfs.providers.ProvidedExtensionsTest;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileObjectTestHid;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.TestBaseHid;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.test.StatFiles;
import org.openide.util.Utilities;
import org.openide.util.io.NbMarshalledObject;


public class BaseFileObjectTestHid extends TestBaseHid{
    private FileObject root;

    public BaseFileObjectTestHid(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        root = testedFS.findResource(getResourcePrefix());
    }

    protected String[] getResources(String testName) {
        return new String[] {"testdir/ignoredir/nextdir/", 
                             "testdir/mountdir/nextdir/",
                             "testdir/mountdir2/nextdir/",
                             "testdir/mountdir2/fname/notestfile",
                             "testdir/mountdir2/fname2/testfile",
                             "testdir/mountdir4/file.ext",
                             "testdir/mountdir5/file.ext",
                             "testdir/mountdir6/file.ext",
                             "testdir/mountdir6/file2.ext",
                             "testdir/mountdir7/file2.ext",
                             "testdir/mountdir8/",
                             "testdir/mountdir9/",                
        };
    }

    public void testRootToFileObject() throws Exception {
        FileObjectFactory fs = FileObjectFactory.getInstance(getWorkDir());
        assertNotNull(fs);
        FileObject root1 = fs.getRoot();
        assertNotNull(root1);
        assertNotNull(FileUtil.toFile(root1));
    }

    public void testMoveOfAFolderDoesNotTouchSubhierarchy() throws Exception {
        FileObjectFactory fs = FileObjectFactory.getInstance(getWorkDir());
        assertNotNull(fs);
        FileObject root1 = fs.getValidFileObject(getWorkDir(), FileObjectFactory.Caller.Others);

        FileObject where = root1.createFolder("else").createFolder("sub").createFolder("subsub");
        FileObject fo = root1.createFolder("something");
        FileObject kidTxt = fo.createData("kid.txt");
        FileObject kid = fo.createFolder("kid");
        File kidFile = FileUtil.toFile(kid);
        File kidTxtFile = FileUtil.toFile(kidTxt);

        accessMonitor = new StatFiles();
        FileLock lock = fo.lock();
        FileObject newFolder = fo.move(lock, where, fo.getNameExt(), null);
        assertEquals("Subfolder", where, newFolder.getParent());

        assertNotNull("Folder found", newFolder.getFileObject("kid"));
        assertNotNull("File found", newFolder.getFileObject("kid.txt"));
        assertFalse("No longer valid file", kidTxt.isValid());
        assertFalse("No longer valid dir", kid.isValid());

        String msg = 
            accessMonitor.getResults().statResultStack(kidFile, StatFiles.ALL) + "\n" +
            accessMonitor.getResults().statResultStack(kidTxtFile, StatFiles.ALL) + "\n";
        final Set<File> files = accessMonitor.getResults().getFiles();
        if (files.contains(kidFile) || files.contains(kidTxtFile)) {
            fail(msg);
        }
    }
    
    public void testRefresh109490() throws Exception {
        final File wDir = new File(getWorkDir(), getName());
        wDir.mkdir();
        final FileObject wDirFo = FileUtil.toFileObject(wDir);
        final List<FileEvent> fileEvents = new ArrayList<FileEvent>();
        FileSystem fs = wDirFo.getFileSystem();
        FileChangeListener fListener = new FileChangeAdapter(){
            @Override
                public void fileDataCreated(FileEvent fe) {
                    super.fileDataCreated(fe);
                    fileEvents.add(fe);
                }            
            };
        try {
            fs.addFileChangeListener(fListener);

            File file = new File(wDir, "testao.f");
            File file2 = new File(wDir, "testc1.f");
            assertEquals(file.hashCode(), file2.hashCode());
            wDirFo.getChildren();
            assertTrue(file.createNewFile());
            assertTrue(file2.createNewFile());
            assertEquals(0, fileEvents.size());
            fs.refresh(true);
            assertEquals(2, fileEvents.size());
            assertEquals(Arrays.asList(wDirFo.getChildren()).toString(), 2,wDirFo.getChildren().length);
            assertTrue(Arrays.asList(wDirFo.getChildren()).toString().indexOf(file.getName()) != -1);            
            assertTrue(Arrays.asList(wDirFo.getChildren()).toString().indexOf(file2.getName()) != -1);                        
            
        } finally {
            fs.removeFileChangeListener(fListener);
        }
    }
    public void testOnWindowsIssue118874 () throws Exception {
        if (!Utilities.isWindows()) return;
        File f = getWorkDir();
        FileObject fo = FileUtil.toFileObject(f);
        FileObject[] childs = fo.getChildren();
        assertNotNull(fo);

        FileSystem fs = fo.getFileSystem();
        assertNotNull(fs);
        final Set<FileObject> s = new HashSet<FileObject>();
        fs.addFileChangeListener(new FileChangeAdapter(){
            @Override
            public void fileFolderCreated(FileEvent fe) {
                s.add(fe.getFile());
            }            
        });
        
        File test = new File(f, getName());
        assertFalse(test.exists());
        test = new File(test.getAbsolutePath().toLowerCase());
        assertFalse(test.exists());
        FileObject foTest = FileUtil.toFileObject(test);
        assertNull(foTest);
                
        FileObject testFo = FileUtil.createFolder(test);
        assertNotNull(testFo);
        assertEquals(1, s.size()); 
    }
    
    public void testExternalDelete96433 () throws Exception {
        File f = getWorkDir();
        FileObject fo = FileUtil.toFileObject(f);
        assertNotNull(fo);
        File testFolder = new File(f,"testfold");
        FileObject testFo = FileUtil.createFolder(testFolder);
        FileUtil.createData(testFo, "a");
        FileUtil.createData(testFo, "b");
        FileUtil.createData(testFo, "c");        
        FileUtil.createData(testFo, "d");
        FileUtil.createData(testFo, "e");        
        FileUtil.createData(testFo, "f");        
        FileObject[] childs = testFo.getChildren();
        assertEquals(6, childs.length);
        final List<FileObject> l = new ArrayList<FileObject>();
        FileChangeListener fclFS = new FileChangeAdapter(){
            @Override
            public void fileDeleted(FileEvent fe) {
                l.add(fe.getFile());
            }            
        };
        FileChangeListener fclFo = new FileChangeAdapter(){
            @Override
            public void fileDeleted(FileEvent fe) {
                fe.getFile().getChildren();
                fe.getFile().getParent().getChildren();
                Enumeration en =  fe.getFile().getParent().getChildren(true);
                while(en.hasMoreElements()) {
                    if (fe.getFile().equals((FileObject)en.nextElement())) {
                        fail(fe.getFile().getPath());
                    }
                }
            }            
        };
        testFo.refresh();
        testFo.getFileSystem().refresh(false);                
        for (int i = 0; i < childs.length; i++) {
            FileObject fileObject = childs[i];
            fileObject.refresh();
        }

        testFo.addFileChangeListener(fclFo);               
        testFo.getFileSystem().addFileChangeListener(fclFS);
        try {
            File[] files = testFolder.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                assertTrue(file.delete());
            }
            assertEquals(0,testFolder.list().length);
            assertTrue(testFolder.delete());
            fo.getFileSystem().refresh(false);
            assertEquals(7,l.size());
            fo.getFileSystem().refresh(false);        
            assertEquals(7,l.size());
        } finally {
            testFo.getFileSystem().removeFileChangeListener(fclFo);
            testFo.getFileSystem().removeFileChangeListener(fclFS);
            testFo.refresh();
            testFo.getFileSystem().refresh(false);
            for (int i = 0; i < childs.length; i++) {
                FileObject fileObject = childs[i];
                fileObject.refresh();
            }            
        }
    }
    
    public void  testCreateNotExistingFolderOrDataFile() throws IOException {
        final File wDir = getWorkDir();
        final File fold = new File(wDir,"a/b/c");
        final File data = new File(fold,"c.data");
        implCreateFolderOrDataFile(fold, data);        
    }

    public void  testCreateExistingFolderOrDataFile() throws IOException {
        final File wDir = getWorkDir();
        final File fold = new File(wDir,"a/b/c");
        final File data = new File(fold,"c.data");

        assertTrue(fold.mkdirs());
        assertTrue(data.createNewFile());                
                
        implCreateFolderOrDataFile(fold, data);        
    }

    public void  testCreateFolderOrDataFileExternalChange() throws IOException {
        final File wDir = getWorkDir();
        final File fold = new File(wDir,"a/b/c");
        final File data = new File(fold,"c.data");
        createFiles(data, fold);                
                
        implCreateFolderOrDataFile(fold, data);        
        
        FileObject foldFo = FileUtil.toFileObject(fold);
        FileObject dataFo = FileUtil.toFileObject(data);
        assertNotNull(foldFo);
        assertNotNull(dataFo);
        deleteFiles(data, wDir);
        
        implCreateFolderOrDataFile(fold, data);                

        deleteFiles(data, wDir);       
        foldFo.getFileSystem().refresh(false);
        foldFo = FileUtil.toFileObject(fold);
        dataFo = FileUtil.toFileObject(data);
        assertNull(foldFo);
        assertNull(dataFo);
        createFiles(data, fold);                

        implCreateFolderOrDataFile(fold, data);                        
    }
    //disabled because may cause that dialog is poped up on Windows promting user to put floppy disk in
    /*public void  testCreateFolderOrDataFileWithNotExistingRoot() throws Exception {
        File wDir = null;
        for (char d = 'A'; d < 'Z'; d++ ) {
            wDir = new File(String.valueOf(d)+":/");
            if (!wDir.exists()) {
                break;
            }
        }
        final File fold = new File(wDir,"a/b/c");
        final File data = new File(fold,"c.data");
        try {
            implCreateFolderOrDataFile(fold, data);        
            fail();
        } catch (IOException ex) {            
        }        
    }*/

    public void testCreateFolderOrDataFile_ReadOnly() throws Exception {
        final File wDir = getWorkDir();
        final File fold = new File(wDir,"a/b/c");
        final File data = new File(fold,"a/b/c.data");
        assertTrue(getWorkDir().setReadOnly());
        try {
            implCreateFolderOrDataFile(fold, data);        
            fail();
        } catch (IOException ex) {            
        } finally {
            assertTrue(getWorkDir().setWritable(true));        
        }
    }
    
    private static void createFiles(final File data, final File fold) throws IOException {
        assertTrue(fold.mkdirs());
        assertTrue(data.createNewFile());                
    }

    private static void deleteFiles(final File data, final File wDir) {        
        File tmp = data;
        while(!tmp.equals(wDir)) {
            assertTrue(tmp.delete());    
            tmp = tmp.getParentFile(); 
        }                        
    }
        
    private void implCreateFolderOrDataFile(final File fold, final File data) throws IOException {        
        FileObject foldFo = FileUtil.createFolder(fold);
        assertNotNull(foldFo);        
        assertTrue(foldFo.isValid());
        assertNotNull(FileUtil.toFile(foldFo));        
        assertEquals(FileUtil.toFile(foldFo),fold);                
        assertTrue(foldFo.isFolder());        
        
        FileObject dataFo = FileUtil.createData(data);         
        assertNotNull(dataFo);        
        assertTrue(dataFo.isValid());                
        assertNotNull(FileUtil.toFile(dataFo));        
        assertEquals(FileUtil.toFile(dataFo),data);                
        assertTrue(dataFo.isData());        
    }
    
    public void  testGetNameExt2() throws IOException {
        FileObject fold1 = FileUtil.createFolder(
                FileBasedFileSystem.getFileObject(getWorkDir()),getName());
        assertNotNull(fold1);
        testComposeNameImpl(fold1.createData("a.b"));
        testComposeNameImpl(fold1.createData(".b"));
        if (!Utilities.isWindows()) {
            testComposeNameImpl(fold1.createData("a."));
        }
    }

    private void testComposeNameImpl(FileObject fo) throws IOException {
        assertTrue(fo.isValid() && fo.isData());
        String fullName = fo.getNameExt();
        String ext = fo.getExt();
        String name = fo.getName();
        FileObject parent = fo.getParent();
        fo.delete();
        FileObject fo2 = parent.createData(name, ext);
        assertEquals(fullName, fo2.getNameExt());
        assertEquals(name, fo2.getName());
        assertEquals(ext, fo2.getExt());
    }

    public void testFileUtilToFileObjectIsValid() throws Exception {
        char SEP = File.separatorChar;
        final File fileF = new File(FileUtil.toFile(root).getAbsolutePath() + SEP + "dir" + SEP + "file2");
        File dirF = fileF.getParentFile();
        
        for (int cntr = 0; cntr < 10; cntr++) {
            dirF.mkdir();
            new FileOutputStream(fileF).close();
            root.getFileSystem().refresh(false);
            final List<Boolean> valid = new ArrayList<Boolean>();
            FileObject fo = FileUtil.toFileObject(fileF);
            fo.addFileChangeListener(new FileChangeListener() {
                public void fileAttributeChanged(FileAttributeEvent fe) {
                    update();
                }
                public void fileChanged(FileEvent fe) {
                    update();
                }
                public void fileDataCreated(FileEvent fe) {
                    update();
                }
                public void fileDeleted(FileEvent fe) {
                    update();
                }
                public void fileFolderCreated(FileEvent fe) {
                    update();
                }
                public void fileRenamed(FileRenameEvent fe) {
                    update();
                }
                
                private void update() {
                    FileObject fo;
                    File f = fileF;
                    
                    while ((fo = FileUtil.toFileObject(f)) == null) {
                        f = f.getParentFile();
                    }
                    
                    valid.add(fo.isValid());
                }
            });
            fileF.delete();
            dirF.delete();
            root.getFileSystem().refresh(false);
            
            assertTrue("at least one event", valid.size() > 0);
            
            for (boolean item : valid) {
                assertTrue("valid=" + valid + ", count=" + cntr, item);
            }
        }
    }
    
    public void testRefresh69744() throws Exception {
        File thisTest = new File(getWorkDir(),"thisFolder/thisFolder");
        thisTest.mkdirs();
        thisTest = new File(thisTest,"thisTest");
        thisTest.createNewFile();
        FileObject testf = FileUtil.toFileObject(thisTest);
        assertNotNull(testf);
        assertGC("",new WeakReference<FileObject>(testf.getParent()));
        modifyFileObject(testf, "abc");
        FileSystem fs = testf.getFileSystem();
        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter() {
            @Override
            public void fileChanged(FileEvent fe) {
                l.add(fe);
            }
        };
        Thread.sleep(1500);
        fs.addFileChangeListener(fcl);
        try {
            modifyFileObject(testf, "def");
            assertFalse(l.isEmpty());
        } finally {
            fs.removeFileChangeListener(fcl);
        }
    }
    
    private void modifyFileObject(final FileObject testf, String content) throws IOException {
        FileLock lock = null;
        OutputStream os = null;
        try {
            lock = testf.lock();
            os = testf.getOutputStream(lock);
            os.write(content.getBytes());
        } finally {
            if (os != null) os.close();
            if (lock != null) lock.releaseLock();            
        }
    }
    
    public void testCaseInsensitivity() throws Exception {
        if (!Utilities.isWindows()) return;
        
        File testa = new File(getWorkDir(), "a");
        File testA = new File(getWorkDir(), "A");
        
        if (testA.exists()) {
            assertTrue(testA.delete());
        }
        if (!testa.exists()) {
            assertTrue(testa.createNewFile());
        }

        //FileBasedFileSystem's case sensitivity depends on platform. This is different behaviour
        // than originally provided by AbstractFileSystem.
        FileObject A = root.getFileObject("A");
        assertNotNull(A);
        assertNotNull(root.getFileObject("a"));
        assertSame(root.getFileObject("A"), root.getFileObject("a"));
        assertSame(URLMapper.findFileObject(testa.toURI().toURL()), 
                URLMapper.findFileObject(testA.toURI().toURL()));
        
        //but 
        root.getChildren();
        assertEquals("A",root.getFileObject("A").getName());        
        assertEquals("A",root.getFileObject("a").getName());        
    }

    private class TestListener extends FileChangeAdapter {
        private List<FileObject> fileObjects;
        TestListener(List<FileObject> fileObjects) {
            this.fileObjects = fileObjects;
        }
        @Override
        public void fileFolderCreated(FileEvent fe) {
            assertTrue(fileObjects.remove(fe.getFile())); 
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            assertTrue(fileObjects.remove(fe.getFile())); 
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            assertTrue(fileObjects.remove(fe.getFile())); 
        }        
    }
    
    public void testSimulatesRefactoringRename() throws Exception {
        assertNotNull(root);
        FileSystem fs = root.getFileSystem();
        assertNotNull(fs);        
        FileObject main = root.createData("Main.java");
        FileUtil.createData(root,"subpackage/newclass.java");
        final List<FileObject> fileObjects = new ArrayList<FileObject>();
        final Set<FileObject> allSubPackages = new HashSet<FileObject>();
        final TestListener tl = new TestListener(fileObjects);
        fs.addFileChangeListener(tl);
        try {
            fs.runAtomicAction(new FileSystem.AtomicAction(){
                public void run() throws IOException {
                    FileObject subpackage = root.getFileObject("subpackage");
                    allSubPackages.add(subpackage);
                    FileObject newclass = subpackage.getFileObject("newclass.java");
                    FileObject subpackage1 = root.createFolder("subpackage1");
                    fileObjects.add(subpackage1);
                    allSubPackages.add(subpackage1);                    
                    FileObject newclass1 = subpackage1.createData("newclass.java");
                    fileObjects.add(newclass1);
                    subpackage.delete();
                    fileObjects.add(subpackage);
                    fileObjects.add(newclass);
                }
            });
        } finally {
            fs.removeFileChangeListener(tl);
        }
        assertTrue(fileObjects.isEmpty());
        assertNotNull(root.getFileObject("Main.java"));
        assertNotNull(root.getFileObject("subpackage1"));
        assertNotNull(root.getFileObject("subpackage1/newclass.java"));
        FileObjectTestHid.implOfTestGetFileObjectForSubversion(root, "subpackage");                                
        final String subpackageName = Utilities.isWindows() ? 
            "subpackage2" : "Subpackage";
        fs.addFileChangeListener(tl);
        try {
             fs.runAtomicAction(new FileSystem.AtomicAction(){
                public void run() throws IOException {
                    FileObject subpackage1 = root.getFileObject("subpackage1");
                    FileObject newclass = root.getFileObject("subpackage1/newclass.java");
                    FileObject Subpackage = root.createFolder(subpackageName);
                    allSubPackages.add(Subpackage);
                    assertEquals(3,allSubPackages.size());
                    
                    fileObjects.add(Subpackage);
                    FileObject newclass1 = Subpackage.createData("newclass.java");
                    fileObjects.add(newclass1);
                    subpackage1.delete();
                    fileObjects.add(subpackage1);
                    fileObjects.add(newclass);
                }
            });
        } finally {
            fs.removeFileChangeListener(tl);
        }
        assertTrue(fileObjects.isEmpty());
        assertNotNull(root.getFileObject("Main.java"));
        assertNotNull(root.getFileObject(subpackageName+"/newclass.java"));
        FileObjectTestHid.implOfTestGetFileObjectForSubversion(root, "subpackage1");                                        
        assertEquals(3,allSubPackages.size());
    }
    
    public void testRefresh60479 () throws Exception {
        final List<FileEvent> l = new ArrayList<FileEvent>();
        File rootFile = FileUtil.toFile(root);
        assertTrue(rootFile.exists());
        
        File testFile = new File (rootFile, "testRefresh60479.txt");
        assertTrue(testFile.createNewFile());
        assertTrue(testFile.exists());
        
        FileObject testFo = FileUtil.toFileObject(testFile);
        assertNotNull(testFo);
        FileLock lock = testFo.lock();        
        OutputStream os = null;
        
        try {
            os = testFo.getOutputStream(lock);
            os.write("abcdefgh".getBytes());
            lock.releaseLock();
            os.close();
            Thread.sleep(3000);
            os = new FileOutputStream(testFile);
            os.write("ijkl".getBytes());            
            os.close();            
        } finally {            
            if (lock != null && lock.isValid()) {
                lock.releaseLock();
            }
            if (os != null) {
                os.close();
            }
        }
        
        testFo.addFileChangeListener(new FileChangeAdapter(){
            @Override
            public void fileChanged(FileEvent fe) {
                l.add(fe);
            }
            
        });
        
        testFo.refresh(true);
        assertEquals(1,l.size());
    }
            
    public void testNormalization51910 () throws Exception {
        if (!Utilities.isWindows()) return;
        
        File rootFile = FileUtil.toFile(root);
        assertTrue(rootFile.exists());
        
        File testFile = new File (rootFile, "abc.txt");
        assertTrue(testFile.createNewFile());
        assertTrue(testFile.exists());
        
        File testFile2 = new File (rootFile, "ABC.TXT");
        assertTrue(testFile2.exists());
        
        
        assertEquals(FileUtil.normalizeFile(testFile).toURI().toURL(), FileUtil.normalizeFile(testFile2).toURI().toURL());
    }   
    
    public void testEventsAfterCreatedFiles55550() throws Exception {
        FileObject parent = root.getFileObject("testdir/mountdir8");  
        assertNotNull(parent);
        assertTrue(parent.isFolder());
        parent.getChildren();
        
        File parentFile = FileUtil.toFile(parent);
        assertNotNull(parentFile);
        assertTrue(parentFile.getAbsolutePath(),parentFile.exists());
        File newFile = new File(parentFile, "sun-web.xml");
        assertFalse(newFile.getAbsolutePath(),newFile.exists());
                        
        class FCLImpl extends FileChangeAdapter {
            boolean created;
            @Override
            public void fileDataCreated(FileEvent e) {
                created = true;
                synchronized(BaseFileObjectTestHid.this) {
                    BaseFileObjectTestHid.this.notifyAll();
                }
            }
        }        
        FCLImpl fl = new FCLImpl();        
        parent.addFileChangeListener(fl);
        
        assertTrue(newFile.getAbsolutePath(), newFile.createNewFile());
        assertTrue(newFile.exists());
        
        // !!!! This is the source of the problem !!!
        // ask for the new file
        // remove this line ans the test passes
        FileUtil.toFileObject(newFile);
        
        
        parent.refresh();
        synchronized(this) {
            wait(1000);
        }
        parent.removeFileChangeListener(fl);
        assertTrue("Didn't receive a FileEvent on the parent.", fl.created);
    }
    
    public void testIssue49037 () throws Exception {
        assertNotNull(root);
        FileObject fo = root.getFileObject("testdir/");
        assertNotNull(fo);
        
        File f = FileUtil.toFile (fo);
        assertNotNull(f);
        
        File newFile = new File (f, "absolutelyNewFile");
        assertFalse(newFile.exists());
        
        new FileOutputStream (newFile).close();
        assertTrue(newFile.exists());
        assertNotNull(FileUtil.toFileObject(newFile));        
    }
    
        
    @SuppressWarnings("deprecation")
    public void testFileUtilFromFile () throws Exception {        
        assertNotNull(root);
        
        File f = FileUtil.normalizeFile(getWorkDir());
        IgnoreDirFileSystem ifs = new IgnoreDirFileSystem();
        ifs.setRootDirectory(f);
        
        Repository.getDefault().addFileSystem(ifs);
        Repository.getDefault().addFileSystem(testedFS);
        
        FileObject[] fos = FileUtil.fromFile(f);
        assertTrue(fos.length > 0);
        assertEquals(fos[0].getFileSystem(), testedFS );
        
    }
    
    public void testIssue45485 () {
        assertNotNull(root);        
        FileObject testdir = root.getFileObject("testdir.");        
        assertNull(testdir);
        // #176032
        testdir = root.getFileObject(".");
        assertNotNull(testdir);
        testdir = root.getFileObject("..");
        assertNotNull(testdir);
    }
    
    public void testDeleteNoCaptureExternalChanges () throws Exception {
        String externalName = "newfile.external";        
        assertNotNull(root);
        FileObject fileObject = root.getFileObject("testdir/mountdir5/file.ext");        
        assertNotNull(fileObject);
        File f = FileUtil.toFile(fileObject);
                        
        assertNotNull(f);
        f = FileUtil.normalizeFile(f);
        assertTrue(f.exists());
        assertTrue (f.delete());
        fileObject.refresh();
        
        assertFalse(fileObject.isValid());
    }
        
    public void testFindResourceNoCaptureExternalChanges () throws Exception {
        String externalName = "newfile.external";        
        assertNotNull(root);
        FileObject fileObject = root.getFileObject("testdir");        
        assertNotNull(fileObject);
        File f = FileUtil.toFile(fileObject);
        
        assertNull(testedFS.findResource(new File (f, externalName).getAbsolutePath().replace('\\',File.separatorChar)));
        assertNull(fileObject.getFileObject(externalName));
        
        assertNotNull(f);
        f = FileUtil.normalizeFile(f);
        assertTrue(f.exists());
        f = new File (f, externalName);
        assertTrue(!f.exists());       
        assertTrue(f.getAbsolutePath(),f.createNewFile());
        fileObject.refresh();
        assertNotNull(FileBasedFileSystem.getFileObject(f));
    }

    public void testGetFileObjectNoCaptureExternalChanges () throws Exception {
        String externalName = "newfile.external2";        
        assertNotNull(root);
        FileObject fileObject = root.getFileObject("testdir");        
        assertNotNull(fileObject);
        File f = FileUtil.toFile(fileObject);
        
        assertNull(FileBasedFileSystem.getFileObject(new File (f, externalName)));
        assertNull(fileObject.getFileObject(externalName));
        
        assertNotNull(f);
        f = FileUtil.normalizeFile(f);
        assertTrue(f.exists());
        f = new File (f, externalName);
        assertTrue(!f.exists());        
        assertTrue(f.getAbsolutePath(),f.createNewFile());
        fileObject.refresh();
        assertNotNull(fileObject.getFileObject(externalName));        
    }
    
    public void testToFileObjectCaptureExternalChanges () throws Exception {
        FileObject testFolder_Fo = FileUtil.toFileObject(getWorkDir()).createFolder(getName());        
        assertNotNull(testFolder_Fo);
        File testFolder = FileUtil.normalizeFile(FileUtil.toFile(testFolder_Fo));
        assertNotNull(testFolder);
        assertTrue(testFolder.exists());

        String externalName = "newfile.external3";                
        File newFile = new File (testFolder, externalName);
        assertFalse(newFile.exists());        
        assertNull(FileBasedFileSystem.getFileObject(newFile));
        assertNull(testFolder_Fo.getFileObject(newFile.getName()));
        assertNull(FileUtil.toFileObject(newFile));        
        
        assertTrue(newFile.createNewFile());
        assertNotNull(FileUtil.toFileObject(newFile));        
    }
    

    public void testGetFileObject47885 () throws Exception {
        assertNotNull(root);
        
        FileObject fileObject = root.getFileObject("testdir/mountdir4/file.ext");        
        assertNotNull(fileObject);
        
        fileObject = root.getFileObject("testdir/mountdir4/file", "ext");        
        assertNull(fileObject);
        
        fileObject = root.getFileObject("testdir\\mountdir4\\file.ext");        
        assertNull(fileObject);
    }
    
    
    public void testValidRoots () throws Exception {
        assertNotNull(testedFS.getRoot());    
        assertTrue(testedFS.getRoot().isValid());            
        
        FileSystemView fsv = FileSystemView.getFileSystemView();                
        File[] roots = File.listRoots();
        boolean validRoot = false;
        for (int i = 0; i < roots.length; i++) {
            FileObject root1 = FileUtil.toFileObject(roots[i]);
            if (!roots[i].exists()) {
               assertNull(root1);
               continue; 
            }
            
            assertNotNull(roots[i].getAbsolutePath (),root1);
            assertTrue(root1.isValid());
            if (testedFS == root1.getFileSystem()) {
                validRoot = true;
            }
        }
        assertTrue(validRoot);
    }
    
    public void testDeserializationOfMasterFSLeadsToTheSameFileSystem () throws Exception {
        NbMarshalledObject stream = new NbMarshalledObject (testedFS);
        Object obj = stream.get ();
        assertNotNull(obj);
        //assertSame ("After deserialization it is still the same", testedFS, obj);
    }
    

    public void testNormalizeDrivesOnWindows48681 () {
        if ((Utilities.isWindows () || (Utilities.getOperatingSystem () == Utilities.OS_OS2))) {
            File[] roots = File.listRoots();
            for (int i = 0; i < roots.length; i++) {
                File file = roots[i];
                if (FileSystemView.getFileSystemView().isFloppyDrive(file) || !file.exists()) {
                    continue;
                }
                File normalizedFile = FileUtil.normalizeFile(file);
                File normalizedFile2 = FileUtil.normalizeFile(new File (file, "."));
                
                assertEquals (normalizedFile.getPath(), normalizedFile2.getPath());
            }
            
        }
    }
    
    public void testJarFileSystemDelete () throws Exception {
        assertNotNull(root);
        FileObject folderFo = root.getFileObject("testdir/mountdir7");
        File folder = FileUtil.toFile(folderFo);
        assertNotNull(folder);
        
        File f = new File (folder,"jfstest.jar");        
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        JarOutputStream jos = new JarOutputStream (new FileOutputStream (f));        
        jos.putNextEntry(new ZipEntry ("a/b/c/c.txt"));
        jos.putNextEntry(new ZipEntry ("a/b/d/d.txt"));
                        
       jos.close();        

        FileObject parent = FileUtil.toFileObject(f.getParentFile());
        parent.getChildren();
        JarFileSystem jfs = new JarFileSystem  ();
        try {
            jfs.setJarFile(f);
        } catch (Exception ex) {
            fail ();
        }
        

        ArrayList<FileObject> all = new ArrayList<FileObject>();
        FileObject jfsRoot = jfs.getRoot();
        Enumeration<? extends FileObject> en = jfsRoot.getChildren(true);
        while (en.hasMoreElements()) {
            all.add ((FileObject) en.nextElement());                        
        }

        assertTrue (all.size() > 0); 
        
        final ArrayList<FileObject> deleted = new ArrayList<FileObject>();
        jfs.addFileChangeListener(new FileChangeAdapter() {
            @Override
            public void fileDeleted(FileEvent fe) {
                super.fileDeleted(fe);
                deleted.add (fe.getFile());
            }
        });
        
        Thread.sleep(1000);
        assertTrue (f.getAbsolutePath(), f.delete());
        parent.refresh();
        assertEquals (deleted.size(), all.size());

        for (int i = 0; i < all.size(); i++) {
            FileObject fileObject = all.get(i);
            assertFalse (fileObject.isValid());
        }
        
        
        assertFalse (jfsRoot.isValid());        
    }
    
        

    public void testLockFileAfterCrash() throws Exception {
        FileObject testFo = FileUtil.createData(root,"/testAfterCrash/testfile.data");
        File testFile = FileUtil.toFile(testFo);
  
        
        File lockFile = WriteLockUtils.getAssociatedLockFile(testFile);
        if (!lockFile.exists()) {
            assertTrue(lockFile.createNewFile());
        }
                
        assertTrue(lockFile.exists());

        FileObject lockFo = FileUtil.toFileObject(lockFile);        
        assertNull(lockFo);        
        testFo.delete();        
        
        
        lockFo = FileUtil.toFileObject(lockFile);        
        String msg = (lockFo != null) ? lockFo.toString() : "";
        assertNull(msg,lockFo);
    }

    public void testDeletedFileDoesNotReturnInputStream() throws Exception {
        final FileObject testFo = FileUtil.createData(root,"testfile.data");
        final File testFile = FileUtil.toFile(testFo);
        final Logger LOGGER = Logger.getLogger(FileObj.class.getName());
        final Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if ("FileObj.getInputStream_after_is_valid".equals(record.getMessage())) {
                    testFile.delete();
                }
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        };
        final Level originalLevel = LOGGER.getLevel();
        LOGGER.setLevel(Level.FINEST);
        try {
            LOGGER.addHandler(handler);
            try {
                testFo.getInputStream();
                assertTrue("Exception not thrown by deleted file getInputStream()", false);
            } catch (FileNotFoundException e) {
                //pass - expected exception
            } finally {
                LOGGER.removeHandler(handler);
            }
        } finally {
            LOGGER.setLevel(originalLevel);
        }
    }
    
    private class IgnoreDirFileSystem extends LocalFileSystem {
        org.openide.filesystems.FileSystem.Status status = new org.openide.filesystems.FileSystem.HtmlStatus() {
            public String annotateName (String name, java.util.Set files) {
                java.lang.StringBuffer sb = new StringBuffer (name);                
                Iterator it = files.iterator ();
                while (it.hasNext()) {                    
                    FileObject fo = (FileObject)it.next();
                    try {
                        if (fo.getFileSystem() instanceof IgnoreDirFileSystem) {
                            sb.append ("," +fo.getNameExt());//NOI18N
                        }
                    } catch (Exception ex) {
                        fail ();
                    }
                }
                                
                return sb.toString () ;
            }

            public java.awt.Image annotateIcon (java.awt.Image icon, int iconType, java.util.Set files) {
                return icon;
            }

            public String annotateNameHtml(String name, Set files) {
                return annotateName (name, files);
            }            
            
        };        
        
        @Override
        public org.openide.filesystems.FileSystem.Status getStatus() {
            return status;
        }
        
        @Override
        protected String[] children(String name) {
            String[] strings = super.children(name);
            return strings;
        }
    }
}
