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

package org.openide.filesystems;

import org.netbeans.junit.*;
import junit.textui.TestRunner;

/**
 * Test that MultiFileSystem does not refresh more than it needs to
 * when you call setDelegates.
 * @see "#29354"
 * @author Jesse Glick
 */
public class MultiFileSystemRefreshTest extends NbTestCase implements FileChangeListener {

    public MultiFileSystemRefreshTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(MultiFileSystemRefreshTest.class));
    }

    private FileSystem fs1, fs2;
    protected void setUp() throws Exception {
        super.setUp();
        fs1 = TestUtilHid.createLocalFileSystem("mfsrefresh1"+getName() + "1", new String[] {
            "a/b/c.txt",
            "a/b/d.txt",
            "e/f.txt",
        });
        fs2 = TestUtilHid.createLocalFileSystem("mfsrefresh2"+getName() + "2", new String[] {
            "e/g.txt",
        });
    }
    protected void tearDown() throws Exception {
        TestUtilHid.destroyLocalFileSystem(getName() + "1");
        TestUtilHid.destroyLocalFileSystem(getName() + "2");
        super.tearDown();
    }
    
    private int count;

    public void testAttributes106242() throws Exception {
        MultiFileSystem mfs = new MultiFileSystem(new FileSystem[] {fs1, fs2});
        FileObject eFs1 = fs1.findResource("e");
        assertNotNull(eFs1);
        FileObject eFs2 = fs2.findResource("e");        
        assertNotNull(eFs2);
        eFs1.setAttribute("e", 100);
        eFs2.setAttribute("e", 200);
        FileObject eMfs = mfs.findResource("e");
        assertEquals(100, eMfs.getAttribute("e"));
        mfs.setDelegates(new FileSystem[] {fs2, fs1});
        assertEquals(200, eMfs.getAttribute("e"));
        mfs.setDelegates(new FileSystem[] {fs1, fs2});
        assertEquals(100, eMfs.getAttribute("e"));        
    }
    
    public void testSetDelegatesFiring() throws Exception {
        MultiFileSystem mfs = new MultiFileSystem(new FileSystem[] {fs1, fs2});
        //mfs.addFileChangeListener(this);
        FileObject a = mfs.findResource("a");
        assertNotNull(a);
        assertEquals(1, a.getChildren().length);
        a.addFileChangeListener(this);
        FileObject e = mfs.findResource("e");
        assertNotNull(e);
        assertEquals(2, e.getChildren().length);
        e.addFileChangeListener(this);
        count = 0;
        mfs.setDelegates(new FileSystem[] {fs1});
        System.err.println("setDelegates done");
        assertEquals(1, a.getChildren().length);
        assertEquals(1, e.getChildren().length);
        assertEquals(1, count);
    }
    
    public void fileAttributeChanged(FileAttributeEvent fe) {
        System.err.println("attr changed: " + fe);
        count++;
    }
    
    public void fileChanged(FileEvent fe) {
        System.err.println("changed: " + fe);
        count++;
    }
    
    public void fileDataCreated(FileEvent fe) {
        System.err.println("created: " + fe);
        count++;
    }
    
    public void fileDeleted(FileEvent fe) {
        System.err.println("deleted: " + fe);
        count++;
    }
    
    public void fileFolderCreated(FileEvent fe) {
        System.err.println("folder created: " + fe);
        count++;
    }
    
    public void fileRenamed(FileRenameEvent fe) {
        System.err.println("renamed: " + fe);
        count++;
    }
    
}
