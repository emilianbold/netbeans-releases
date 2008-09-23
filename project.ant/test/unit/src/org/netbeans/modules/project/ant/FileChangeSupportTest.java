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
import org.netbeans.junit.RandomlyFails;
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

    @RandomlyFails // #146525: jtulach claims failures possible
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
        
        private final List<String> events = new ArrayList<String>();
        
        public L() {}
        
        public List<String> check() {
            List<String> toret = new ArrayList<String>(events);
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
