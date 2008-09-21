/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.StatFiles;

/**
 * @author Radek Matous
 */
public class StatFilesTest extends NbTestCase {

    private StatFiles monitor;
    private File testFile = null;

    public StatFilesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        FileObjectFactory.WARNINGS = false;
        clearWorkDir();
        testFile = new File(getWorkDir(), "testLockFile.txt");
        if (!testFile.exists()) {
            testFile.createNewFile();
        }

        assertTrue(testFile.exists());
        //init
        FileUtil.toFileObject(testFile);
        monitor = new StatFiles();
        System.setSecurityManager(monitor);
        monitor.reset();
    }

    private File getFile(FileObject fo) {
        return ((BaseFileObj) fo).getFileName().getFile();
    }

    private FileObject getFileObject(File f) {
        return FileBasedFileSystem.getFileObject(f);
    }

    @RandomlyFails
    public void testToFileObject() throws IOException {
        File workDir = getWorkDir();
        monitor.reset();
        monitor();
        assertNotNull(FileUtil.toFileObject(workDir));
        monitor.getResults().assertResult(4, StatFiles.ALL);
    }

    /** Tests it is not neccessary to create FileObjects for the whole path. */
    public void testGetFileObject23() throws IOException {
        File workDir = getWorkDir();
        File rootFile = null;
        Stack<String> stack = new Stack<String>();
        while (workDir != null) {
            stack.push(workDir.getName());
            rootFile = workDir;
            workDir = workDir.getParentFile();
        }
        String relativePath = "";
        while (!stack.empty()) {
            relativePath += stack.pop() + "/";
        }
        FileObject root = FileUtil.toFileObject(rootFile);
        monitor.reset();
        assertNotNull(root.getFileObject(relativePath));
        assertEquals(1, monitor.getResults().statResult(StatFiles.ALL));
    }

    @RandomlyFails
    public void testGetCachedChildren() throws IOException {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        List<FileObject> l = new ArrayList<FileObject>();
        parent = parent.createFolder("parent");
        for (int i = 0; i < 10; i++) {
            l.add(parent.createData("file" + i));
            l.add(parent.createFolder("fold" + i));
        }

        monitor.reset();
        //20 x FileObject + 1 File.listFiles
        FileObject[] children = parent.getChildren();
        assertEquals(1, monitor.getResults().statResult(StatFiles.ALL));
        assertEquals(1, monitor.getResults().statResult(StatFiles.READ));
        //second time
        monitor.reset();
        children = parent.getChildren();
        assertEquals(0, monitor.getResults().statResult(StatFiles.ALL));
    }

    /** Testing that queries for siblings in fully expanded folder do not generate
     * new disk touches. */
    @RandomlyFails // NB-Core-Build #1377
    public void testGetChildrenCaches() throws IOException {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        List<FileObject> l = new ArrayList<FileObject>();
        parent = parent.createFolder("parent");
        for (int i = 0; i < 20; i++) {
            l.add(parent.createData("file" + i + ".txt"));
        }

        monitor.reset();
        //20 x FileObject + 1 File.listFiles
        FileObject[] children = parent.getChildren();
        assertEquals(1, monitor.getResults().statResult(StatFiles.ALL));
        assertEquals(1, monitor.getResults().statResult(StatFiles.READ));
        for (FileObject ch : children) {
            assertNull("No sibling", FileUtil.findBrother(ch, "exe"));
        }
        assertEquals("No aditional touches", 1, monitor.getResults().statResult(StatFiles.ALL));
        assertEquals("No aditional reads", 1, monitor.getResults().statResult(StatFiles.READ));
    }

    public void testLockFile() throws IOException {
        FileObject fobj = getFileObject(testFile);
        monitor.reset();
        final FileLock lock = fobj.lock();
        try {
            assertEquals(0, monitor.getResults().statResult(StatFiles.ALL));
            assertEquals(0, monitor.getResults().statResult(StatFiles.READ));
            //second time
            monitor.reset();
            FileLock lock2 = null;
            try {
                lock2 = fobj.lock();
                fail();
            } catch (IOException ex) {
            }
            assertEquals(0, monitor.getResults().statResult(StatFiles.ALL));
        } finally {
            lock.releaseLock();
        }
    }

    @RandomlyFails // frequent failures in NB-Core-Build
    public void testGetFileObject2() throws IOException {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        parent = parent.createFolder("parent");
        File nbbuild = new File(getFile(parent), "nbbuild");
        File pXml = new File(nbbuild, "project.xml");
        assertTrue(nbbuild.mkdir());
        assertTrue(pXml.createNewFile());
        monitor.reset();
        FileObject ch = parent.getFileObject("nbbuild/project.xml");
        assertEquals(2, monitor.getResults().statResult(StatFiles.ALL));
        assertEquals(2, monitor.getResults().statResult(StatFiles.READ));
        //second time
        monitor.reset();
        ch = parent.getFileObject("nbbuild/project.xml");
        assertEquals(0, monitor.getResults().statResult(StatFiles.ALL));
    }

    public void testIssueFileObject() throws IOException {
        FileObject parent = FileBasedFileSystem.getFileObject(testFile).getParent();

        //parent exists with cached info + testFile not exists
        monitor.reset();
        assertGC("", new WeakReference(FileBasedFileSystem.getFileObject(testFile)));
        assertNotNull(FileBasedFileSystem.getFileObject(testFile));
        assertEquals(1, monitor.getResults().statResult(testFile, StatFiles.ALL));
        assertEquals(1, monitor.getResults().statResult(testFile, StatFiles.READ));

        //parent not exists + testFile not exists
        monitor.reset();
        parent = null;
        assertGC("", new WeakReference(parent));
        assertGC("", new WeakReference(FileBasedFileSystem.getFileObject((testFile))));
        assertNotNull(FileBasedFileSystem.getFileObject((testFile)));
        assertEquals(2, monitor.getResults().statResult(testFile, StatFiles.ALL));
        assertEquals(2, monitor.getResults().statResult(testFile, StatFiles.READ));


        parent = FileBasedFileSystem.getFileObject((testFile)).getParent();
        monitor.reset();
        FileObject fobj = FileBasedFileSystem.getFileObject((testFile));
        assertNotNull(fobj);
        assertEquals(1, monitor.getResults().statResult(testFile, StatFiles.ALL));
        assertEquals(1, monitor.getResults().statResult(testFile, StatFiles.READ));

        monitor.reset();
        File tFile = testFile.getParentFile();
        assertTrue(String.valueOf(monitor.getResults().statResult(tFile, StatFiles.ALL)), monitor.getResults().statResult(tFile, StatFiles.ALL) <= 1);
        while (tFile != null && tFile.getParentFile() != null) {
            assertTrue(String.valueOf(monitor.getResults().statResult(tFile, StatFiles.ALL)), monitor.getResults().statResult(tFile, StatFiles.ALL) < 1);
            tFile = tFile.getParentFile();
        }
        //second time        
        monitor.reset();
        fobj = getFileObject(testFile);
        assertEquals(0, monitor.getResults().statResult(StatFiles.ALL));
        assertEquals(0, monitor.getResults().statResult(StatFiles.READ));
    }

    public void testGetParent() {
        FileObject fobj = getFileObject(testFile);
        monitor.reset();
        FileObject parent = fobj.getParent();
        assertEquals(1, monitor.getResults().statResult(StatFiles.ALL));
        assertEquals(1, monitor.getResults().statResult(StatFiles.READ));
        monitor.reset();
        parent = fobj.getParent();
        assertEquals(0, monitor.getResults().statResult(StatFiles.ALL));
        assertEquals(0, monitor.getResults().statResult(StatFiles.READ));

        //second time
        monitor.reset();
        parent = fobj.getParent();
        assertEquals(0, monitor.getResults().statResult(StatFiles.ALL));
    }

    //on trunk fails: expected:<0> but was:<1>
    public void testGetCachedFileObject() throws IOException {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        parent = parent.createFolder("parent");
        FileObject child = parent.createData("child");
        monitor.reset();
        FileObject ch = parent.getFileObject("child");
        assertEquals(0, monitor.getResults().statResult(StatFiles.ALL));
        assertEquals(0, monitor.getResults().statResult(StatFiles.READ));
        //second time
        monitor.reset();
        ch = parent.getFileObject("child");
        assertEquals(0, monitor.getResults().statResult(StatFiles.ALL));
    }

    //on trunk fails: expected:<2> but was:<5>
    public void testGetFileObject() throws IOException {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        parent = parent.createFolder("parent");
        assertTrue(new File(getFile(parent), "child").createNewFile());
        monitor.reset();
        FileObject ch = parent.getFileObject("child");
        assertEquals(2, monitor.getResults().statResult(StatFiles.ALL));
        assertEquals(2, monitor.getResults().statResult(StatFiles.READ));
        //second time
        monitor.reset();
        ch = parent.getFileObject("child");
        assertEquals(0, monitor.getResults().statResult(StatFiles.ALL));
    }


    //on trunk fails: expected:<1> but was:<3>    
    public void testGetCachedChild() {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        monitor.reset();
        FileObject[] childs = parent.getChildren();
        assertEquals(1, monitor.getResults().statResult(StatFiles.ALL));
        assertEquals(1, monitor.getResults().statResult(StatFiles.READ));
        //second time
        monitor.reset();
        childs = parent.getChildren();
        assertEquals(0, monitor.getResults().statResult(StatFiles.ALL));
    }


    //on trunk fails: expected:<21> but was:<91>    
    public void testGetChildren() throws IOException {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        parent = parent.createFolder("parent");
        File pFile = getFile(parent);
        for (int i = 0; i < 10; i++) {
            assertTrue(new File(pFile, "file" + i).createNewFile());
            assertTrue(new File(pFile, "fold" + i).mkdir());
        }
        monitor.reset();
        FileObject[] children = parent.getChildren();
        //20 x children, 1 x File.listFiles 
        assertEquals(21, monitor.getResults().statResult(StatFiles.ALL));
        assertEquals(21, monitor.getResults().statResult(StatFiles.READ));
        //second time
        monitor.reset();
        children = parent.getChildren();
        assertEquals(0, monitor.getResults().statResult(StatFiles.ALL));
    }

    public void testRefreshFile() {
        FileObject fobj = getFileObject(testFile);
        monitor.reset();
        fobj.refresh();
        assertEquals(2, monitor.getResults().statResult(StatFiles.ALL));
        assertEquals(2, monitor.getResults().statResult(StatFiles.READ));
        //second time
        monitor.reset();
        fobj.refresh();
        assertEquals(2, monitor.getResults().statResult(StatFiles.ALL));
    }

    private void monitor() {
        monitor.setMonitor(new StatFiles.Monitor() {

            public void checkRead(File file) {
            }

            public void checkAll(File file) {
            }
        });
    }
}
