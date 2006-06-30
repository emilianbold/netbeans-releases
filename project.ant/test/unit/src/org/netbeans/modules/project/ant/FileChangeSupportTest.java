/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ant;

// XXX testRenames
// XXX testRemoveListener
// XXX testMultipleListenersOnSameFile
// XXX testSameListenerOnMultipleFiles

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test {@link FileChangeSupport}.
 * @author Jesse Glick
 */
public class FileChangeSupportTest extends NbTestCase {
    
    static {
        FileChangeSupportTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    public FileChangeSupportTest(String testName) {
        super(testName);
    }

    private static final char SEP = File.separatorChar;
    
    private static final long SLEEP = 1000; // msec to sleep before touching a file again
    
    private FileObject scratch;
    private String scratchPath;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        scratchPath = FileUtil.toFile(scratch).getAbsolutePath();
    }
    
    public void testSimpleModification() throws Exception {
        FileObject dir = scratch.createFolder("dir");
        FileObject file = dir.createData("file");
        File fileF = FileUtil.toFile(file);
        L l = new L();
        FileChangeSupport.DEFAULT.addListener(l, fileF);
        TestUtil.createFileFromContent(null, dir, "file");
        assertEquals("one mod in file", Collections.singletonList("M:" + fileF), l.check());
        assertEquals("that's all", Collections.EMPTY_LIST, l.check());
        TestUtil.createFileFromContent(null, dir, "file");
        assertEquals("another mod in file", Collections.singletonList("M:" + fileF), l.check());
        dir.createData("bogus");
        assertEquals("nothing from a different file", Collections.EMPTY_LIST, l.check());
        TestUtil.createFileFromContent(null, dir, "bogus");
        assertEquals("even after touching the other file", Collections.EMPTY_LIST, l.check());
    }
    
    public void testCreation() throws Exception {
        File fileF = new File(scratchPath + SEP + "dir" + SEP + "file2");
        L l = new L();
        FileChangeSupport.DEFAULT.addListener(l, fileF);
        FileObject dir = scratch.createFolder("dir");
        assertEquals("no mods yet, just made parent dir", Collections.EMPTY_LIST, l.check());
        FileObject file = dir.createData("file2");
        assertEquals("got file creation event", Collections.singletonList("C:" + fileF), l.check());
        TestUtil.createFileFromContent(null, dir, "file2");
        assertEquals("and then a mod in file", Collections.singletonList("M:" + fileF), l.check());
        dir.createData("file2a");
        assertEquals("nothing from a different file", Collections.EMPTY_LIST, l.check());
    }
    
    public void testDeletion() throws Exception {
        File fileF = new File(scratchPath + SEP + "dir" + SEP + "file3");
        L l = new L();
        FileChangeSupport.DEFAULT.addListener(l, fileF);
        FileObject dir = scratch.createFolder("dir");
        assertEquals("no mods yet, just made parent dir", Collections.EMPTY_LIST, l.check());
        FileObject file = dir.createData("file3");
        assertEquals("got file creation event", Collections.singletonList("C:" + fileF), l.check());
        file.delete();
        assertEquals("got file deletion event", Collections.singletonList("D:" + fileF), l.check());
        dir.delete();
        assertEquals("nothing from deleting containing dir when file already deleted", Collections.EMPTY_LIST, l.check());
        dir = scratch.createFolder("dir");
        assertEquals("remade parent dir", Collections.EMPTY_LIST, l.check());
        file = dir.createData("file3");
        assertEquals("recreated file", Collections.singletonList("C:" + fileF), l.check());
        dir.delete();
        assertEquals("got file deletion event after dir deleted", Collections.singletonList("D:" + fileF), l.check());
    }
    
    public void testDiskChanges() throws Exception {
        File fileF = new File(scratchPath + SEP + "dir" + SEP + "file2");
        L l = new L();
        FileChangeSupport.DEFAULT.addListener(l, fileF);
        File dirF = new File(scratchPath + SEP + "dir");
        dirF.mkdir();
        new FileOutputStream(fileF).close();
        scratch.getFileSystem().refresh(false);
        assertEquals("got file creation event", Collections.singletonList("C:" + fileF), l.check());
        Thread.sleep(SLEEP); // make sure timestamp changes
        new FileOutputStream(fileF).close();
        scratch.getFileSystem().refresh(false);
        assertEquals("and then a mod in file", Collections.singletonList("M:" + fileF), l.check());
        fileF.delete();
        dirF.delete();
        scratch.getFileSystem().refresh(false);
        assertEquals("and then a file deletion event", Collections.singletonList("D:" + fileF), l.check());
    }
    
    public void test66444() throws Exception {
        File fileF = new File(scratchPath + SEP + "dir" + SEP + "file2");
        L l = new L();
        FileChangeSupport.DEFAULT.addListener(l, fileF);
        File dirF = new File(scratchPath + SEP + "dir");
        
        for (int cntr = 0; cntr < 50; cntr++) {
            dirF.mkdir();
            new FileOutputStream(fileF).close();
            scratch.getFileSystem().refresh(false);
            assertEquals("got file creation event, count=" + cntr, Collections.singletonList("C:" + fileF), l.check());
            fileF.delete();
            dirF.delete();
            scratch.getFileSystem().refresh(false);
            assertEquals("and then a file deletion event, count=" + cntr, Collections.singletonList("D:" + fileF), l.check());
        }
    }
    
    private static final class L implements FileChangeSupportListener {
        
        private final List/*<String>*/ events = new ArrayList();
        
        public L() {}
        
        public List/*<String>*/ check() {
            List toret = new ArrayList(events);
            events.clear();
            return toret;
        }
        
        public void fileCreated(FileChangeSupportEvent event) {
            events.add("C:" + event.getPath());
        }

        public void fileDeleted(FileChangeSupportEvent event) {
            events.add("D:" + event.getPath());
        }

        public void fileModified(FileChangeSupportEvent event) {
            events.add("M:" + event.getPath());
        }
        
    }
    
}
