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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

/**
 *
 * @author vita
 */
public class CompoundFolderChildrenTest extends NbTestCase {

    /** Creates a new instance of FolderChildrenTest */
    public CompoundFolderChildrenTest(String name) {
        super(name);
    }

    protected void setUp() throws java.lang.Exception {
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(
            new String[] {
                "Tmp/"
            },
            getWorkDir(), new Object[] {},
            getClass().getClassLoader(), 
            null
        );
    }

    // test collecting files on different layers

    public void testCollecting() throws Exception {
        String fileName1 = "file-on-layer-1.instance";
        String fileName2 = "file-on-layer-2.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);

        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B" }, false);
        List files = cfch.getChildren();
        
        assertEquals("Wrong number of files", 2, files.size());
        assertNotNull("Files do not contain " + fileName1, findFileByName(files, fileName1));
        assertNotNull("Files do not contain " + fileName2, findFileByName(files, fileName2));
        
        cfch = new CompoundFolderChildren(new String [] { "Tmp/X/Y/Z" });
        files = cfch.getChildren();

        assertEquals("Wrong number of files", 0, files.size());
    }
    
    // test hiding files on lower layer by files on higher layers

    public void testHidingSameFilesOnLowerLayers() throws Exception {
        String fileName = "some-file.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName);

        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B" }, false);
        List files = cfch.getChildren();
        
        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong layerA file", fileName, ((FileObject) files.get(0)).getNameExt());
    }
    
    // test hidden files

// This one's failing, because the filesystem doesn't show files with the _hidden suffix
//    public void testFilesHiddenBySuffix() throws Exception {
//        String fileName1 = "file-on-layer-A.instance";
//        String fileName2 = "file-on-layer-B.instance";
//        EditorTestLookup.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
//        EditorTestLookup.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
//        EditorTestLookup.createFile(getWorkDir(), "Tmp/A/" + fileName2);
//
//        File markerFile = new File(getWorkDir(), "Tmp/A/B/C/D/" + fileName2 + "_hidden");
//        markerFile.createNewFile();
//        
//        // Check precondition
//        FileObject f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/C/D/");
//        f.refresh();
//        
//        f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/C/D/" + fileName2 + "_hidden");
//        assertNotNull("The _hidden file does not exist", f);
//
//        f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/" + fileName2);
//        assertNotNull("The original file on the second layer that should be hidden does not exist", f);
//
//        f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/" + fileName2);
//        assertNotNull("The original file on the third layer that should be hidden does not exist", f);
//        
//        // Test compound children
//        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
//        List files = cfch.getChildren();
//        
//        assertEquals("Wrong number of files", 1, files.size());
//        assertEquals("Wrong layerA file", fileName1, ((FileObject) files.get(0)).getNameExt());
//    }

    public void testFilesHiddenByAttribute() throws Exception {
        String fileName1 = "file-on-layer-A.instance";
        String fileName2 = "file-on-layer-B.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/" + fileName2);

        // Check precondition
        FileObject f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/" + fileName2);
        assertNotNull("The hidden file on the second layer does not exist", f);

        // Mark the file as hidden, which should hide both this file and
        // the same one on the third layer.
        f.setAttribute("hidden", Boolean.TRUE);
        
        f = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/" + fileName2);
        assertNotNull("The original file on the third layer that should be hidden does not exist", f);
        
        // Test compound children
        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
        List files = cfch.getChildren();
        
        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong layerA file", fileName1, ((FileObject) files.get(0)).getNameExt());
    }
    
    // test sorting using attributes on different layers

    public void testSorting() throws Exception {
        // Create files
        String fileName1 = "file-1.instance";
        String fileName2 = "file-2.instance";
        String fileName3 = "file-3.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/" + fileName3);

        // Set the sorting attributes
        FileObject layer1 = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B/C/D");
        FileObject layer2 = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A/B");
        FileObject layer3 = Repository.getDefault().getDefaultFileSystem().findResource("Tmp/A");
        
        layer1.setAttribute("file-3.instance/file-1.instance", Boolean.TRUE);
        layer2.setAttribute("file-2.instance/file-3.instance", Boolean.TRUE);
        
        // Create compound children
        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
        List files = cfch.getChildren();

        assertEquals("Wrong number of files", 3, files.size());
        assertEquals("Wrong first file", fileName2, ((FileObject) files.get(0)).getNameExt());
        assertEquals("Wrong second file", fileName3, ((FileObject) files.get(1)).getNameExt());
        assertEquals("Wrong third file", fileName1, ((FileObject) files.get(2)).getNameExt());
    }
    
    // test events

    private FileObject findFileByName(List files, String nameExt) {
        for (Iterator i = files.iterator(); i.hasNext(); ) {
            FileObject f = (FileObject) i.next();
            if (nameExt.equals(f.getNameExt())) {
                return f;
            }
        }
        return null;
    }
    
    private static class L implements PropertyChangeListener {
        public int changeEventsCnt = 0;
        public PropertyChangeEvent lastEvent = null;
        
        public void propertyChange(PropertyChangeEvent evt) {
            changeEventsCnt++;
            lastEvent = evt;
        }
        
        public void reset() {
            changeEventsCnt = 0;
            lastEvent = null;
        }
    } // End of L class
}
