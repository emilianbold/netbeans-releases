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

package org.openide.loaders;

import java.util.Enumeration;
import junit.framework.TestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/** Mostly to test the correct behaviour of AWTTask.waitFinished.
 *
 * @author Jaroslav Tulach
 */
public class DataFolderCopyMoreWindowsLikeTest extends TestCase {
    DataFolder target;
    DataFolder source;
    DataFolder sub;

    public DataFolderCopyMoreWindowsLikeTest(String testName) {
        super (testName);
    }
    
    protected void setUp() throws Exception {
        FileObject root = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        FileObject[] arr = root.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        
        target = DataFolder.findFolder (FileUtil.createFolder (root, "Target"));
        source = DataFolder.findFolder (FileUtil.createFolder (root, "Source"));
        sub = DataFolder.findFolder (FileUtil.createFolder (root, "Source/Sub/"));
        FileUtil.createData (root, "Source/Sub/A.txt");
    }

    public void testCopyIntoTheSameFolderCreatesFolderNamed2 () throws Exception {
        sub.copy (source);
       
        assertFO ("Sibling to Sub created", "/Source/Sub_1/A.txt");
    }
    
    public void testCopyIntoDifferentEmptyFolderIsWithotuRenames () throws Exception {
        sub.copy (target);
       
        assertFO ("A.txt name preserved", "/Target/Sub/A.txt");
    }

    public void testCopyIntoDifferentNonEmptyFolderCreatesSibling () throws Exception {
        FileUtil.createData (Repository.getDefault ().getDefaultFileSystem ().getRoot(), "Target/Sub/A.txt");
        
        sub.copy (target);
       
        assertFO ("A_1.txt sibling created", "/Target/Sub/A_1.txt");
    }

    public void testMoveIntoTheSameFolderIsForbiden() throws Exception {
        FileObject old = source.getPrimaryFile ();
        
        sub.move (source);
        
        assertEquals ("No change", old, source.getPrimaryFile ());
    }
    
    private static void assertFO (String msg, String name) {
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().findResource (name);
        if (fo == null) {
            StringBuffer sb = new StringBuffer (msg);
            sb.append (" - cannot find ");
            sb.append (name);
            Enumeration en = Repository.getDefault ().getDefaultFileSystem ().getRoot ().getChildren (true);
            while (en.hasMoreElements ()) {
                sb.append ('\n');
                sb.append ("    ");
                sb.append (en.nextElement ());
            }
            fail (sb.toString ());
        }
    }
    
}
