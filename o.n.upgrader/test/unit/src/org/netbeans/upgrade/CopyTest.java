/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.upgrade;
import java.util.*;

import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MultiFileSystem;

/** Tests to check that copy of files works.
 *
 * @author Jaroslav Tulach
 */
public final class CopyTest extends org.netbeans.junit.NbTestCase {
    public CopyTest (String name) {
        super (name);
    }
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new junit.framework.TestSuite (CopyTest.class));
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
        
        org.openide.filesystems.TestUtilHid.destroyLocalFileSystem(getName ());
    }    
    
    public void testDoesSomeCopy () throws Exception {
        FileSystem fs = org.openide.filesystems.TestUtilHid.createLocalFileSystem (getName (), new String[] {
            "root/X.txt", 
            "root/Y.txt",
            "nonroot/Z.txt"
        });
        
        FileObject fo = fs.findResource ("root");
        FileObject tg = fs.getRoot().createFolder ("target");
        
        java.util.HashSet set = new java.util.HashSet ();
        set.add ("X.txt");
          Copy.copyDeep (fo, tg, set);

        assertEquals ("One file copied", 1, tg.getChildren().length);
        String n = tg.getChildren ()[0].getNameExt();
        assertEquals ("Name is X.txt", "X.txt", n);
        
    }
    
    public void testDoesDeepCopy () throws Exception {
        FileSystem fs = org.openide.filesystems.TestUtilHid.createLocalFileSystem (getName (), new String[] {
            "root/subdir/X.txt", 
            "root/Y.txt",
            "nonroot/Z.txt"
        });
        
        FileObject fo = fs.findResource ("root");
        FileObject tg = fs.getRoot().createFolder ("target");
        
        java.util.HashSet set = new java.util.HashSet ();
        set.add ("subdir/X.txt");
        Copy.copyDeep (fo, tg, set);
        
        assertEquals ("One file copied", 1, tg.getChildren().length);
        assertEquals ("Name is X.txt", "subdir", tg.getChildren ()[0].getNameExt());
        assertEquals ("One children of one child", 1, tg.getChildren()[0].getChildren().length);
        assertEquals ("X.txt", "X.txt", tg.getChildren()[0].getChildren()[0].getNameExt());
        
    }
    
    public void testCopyAttributes () throws Exception {
        FileSystem fs = org.openide.filesystems.TestUtilHid.createLocalFileSystem (getName (), new String[] {
            "root/X.txt", 
            "root/Y.txt",
            "nonroot/Z.txt"
        });
        FileObject x = fs.findResource ("root/X.txt");
        x.setAttribute ("ahoj", "yarda");
        
        FileObject fo = fs.findResource ("root");
        FileObject tg = fs.getRoot().createFolder ("target");
        
        java.util.HashSet set = new java.util.HashSet ();
        set.add ("X.txt");
        Copy.copyDeep (fo, tg, set);
        
        assertEquals ("One file copied", 1, tg.getChildren().length);
        assertEquals ("Name is X.txt", "X.txt", tg.getChildren ()[0].getNameExt());
        assertEquals ("attribute copied", "yarda", tg.getChildren()[0].getAttribute("ahoj"));
    }
    
    public void testCopyFolderAttributes () throws Exception {
        FileSystem fs = org.openide.filesystems.TestUtilHid.createLocalFileSystem (getName (), new String[] {
            "root/sub/X.txt", 
            "root/Y.txt",
            "nonroot/Z.txt"
        });
        FileObject x = fs.findResource ("root/sub");
        x.setAttribute ("ahoj", "yarda");
        
        FileObject fo = fs.findResource ("root");
        FileObject tg = fs.getRoot().createFolder ("target");
        
        java.util.HashSet set = new java.util.HashSet ();
        set.add ("sub");
        set.add ("sub/X.txt");
        Copy.copyDeep (fo, tg, set);
        
        assertEquals ("One file copied", 1, tg.getChildren().length);
        assertEquals ("Name of the dir is sub", "sub", tg.getChildren ()[0].getNameExt());
        assertEquals ("attribute copied", "yarda", tg.getChildren()[0].getAttribute("ahoj"));
        assertEquals ("X.txt", "X.txt", tg.getChildren()[0].getChildren()[0].getNameExt());
    }
    
    public void testDoNotCopyEmptyDirs () throws Exception {
        FileSystem fs = org.openide.filesystems.TestUtilHid.createLocalFileSystem (getName (), new String[] {
            "root/sub/X.txt", 
            "root/Y.txt",
            "nonroot/Z.txt"
        });
        FileObject x = fs.findResource ("root/sub");
        
        FileObject fo = fs.findResource ("root");
        FileObject tg = fs.getRoot().createFolder ("target");
        
        java.util.HashSet set = new java.util.HashSet ();
        Copy.copyDeep (fo, tg, set);
        
        assertEquals ("Nothing copied", 0, tg.getChildren().length);
    }
    
    public void testDoNotOverwriteFiles () throws Exception {
        java.util.HashSet set = new java.util.HashSet ();
        set.add ("X.txt");
        
        FileSystem fs = org.openide.filesystems.TestUtilHid.createLocalFileSystem (getName (), new String[] {
            "root/project/X.txt", 
            "root/X.txt",
            "nonroot/Z.txt"
        });
        
        writeTo (fs, "root/project/X.txt", "content-project");
        writeTo (fs, "root/X.txt", "content-global");

        FileObject tg = fs.getRoot().createFolder ("target");
        
        FileObject project = fs.findResource ("root/project");
        Copy.copyDeep (project, tg, set);
        
        
        
        FileObject root = fs.findResource ("root");
        Copy.copyDeep (root, tg, set);

        
        FileObject x = tg.getFileObject ("X.txt");
        assertNotNull ("File copied", x);
        
        byte[] arr = new byte[300];
        int len = x.getInputStream ().read (arr);
        String content = new String (arr, 0, len);
        
        assertEquals ("The content is kept from project", content, "content-project");
    }
    
    public void testDoesNotCopyHiddenFiles () throws Exception {
        String[] res = {
            "root/Yes.txt", 
            "root/X.txt_hidden", 
        };
        FileSystem fs = org.openide.filesystems.TestUtilHid.createLocalFileSystem (getName (), res);
        MultiFileSystem mfs = new MultiFileSystem (new FileSystem[] { fs });
        
        FileObject fo = mfs.findResource ("root");
        FileObject tg = mfs.getRoot().createFolder ("target");
        
        HashSet set = new HashSet ();
        set.add ("Yes.txt");
        set.add ("X.txt_hidden");
        Copy.copyDeep (fo, tg, set);
        
        assertEquals ("One file copied", 1, tg.getChildren().length);
        assertEquals ("Name is Yes.txt", "Yes.txt", tg.getChildren ()[0].getNameExt());
    }
    
    private static void writeTo (FileSystem fs, String res, String content) throws java.io.IOException {
        FileObject fo = org.openide.filesystems.FileUtil.createData (fs.getRoot (), res);
        org.openide.filesystems.FileLock lock = fo.lock ();
        java.io.OutputStream os = fo.getOutputStream (lock);
        os.write (content.getBytes ());
        os.close ();
        lock.releaseLock ();
    }
 }
