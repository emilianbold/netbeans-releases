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

package org.openide.filesystems;

import java.util.*;
import org.netbeans.junit.*;
import junit.textui.TestRunner;

// XXX should only *unused* mask files be listed when propagateMasks?
// XXX write similar test for ParsingLayerCacheManager (simulate propagateMasks)

/**
 * Test that MultiFileSystem can mask files correctly.
 * @author Jesse Glick
 */
public class MultiFileSystemMaskTest extends NbTestCase {
    
    public MultiFileSystemMaskTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(MultiFileSystemMaskTest.class));
    }
    
    // XXX use this!
    private static String childrenNames(FileSystem fs) {
        FileObject folder = fs.findResource("folder");
        return childrenNames(folder);
    }
    
    private static String childrenNames(FileObject folder) {
        FileObject[] kids = folder.getChildren();
        List l = new ArrayList();
        for (int i = 0; i < kids.length; i++) {
            l.add(kids[i].getNameExt());
        }
        Collections.sort(l);
        StringBuffer b = new StringBuffer();
        Iterator i = l.iterator();
        if (i.hasNext()) {
            b.append(i.next());
            while (i.hasNext()) {
                b.append('/');
                b.append(i.next());
            }
        }
        return b.toString();
    }
    
    /**
     * Check that you can use one mask for more than one instance of a masked file.
     */
    public void testRepeatedMasks() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(new FileSystem[] {
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1_hidden",
                "folder/file2_hidden",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file1",
                "folder/file2",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file1",
                "folder/file3",
            }),
        });
        fs.setPropagateMasks(false);
        try {
            assertEquals("folder/file1_hidden masked two occurrences of folder/file1 and folder/file2_hidden masked one occurrence of folder/file2",
                "file3",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
            TestUtilHid.destroyXMLFileSystem(getName() + "3");
        }
    }
    public void testRepeatedMasksPropagate() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(new FileSystem[] {
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1_hidden",
                "folder/file2_hidden",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file1",
                "folder/file2",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file1",
                "folder/file3",
            }),
        });
        fs.setPropagateMasks(true);
        try {
            assertEquals("folder/file1_hidden masked two occurrences of folder/file1 and folder/file2_hidden masked one occurrence of folder/file2",
                "file1_hidden/file2_hidden/file3",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
            TestUtilHid.destroyXMLFileSystem(getName() + "3");
        }
    }
    
    /**
     * Check that a mask must precede the masked file in the delegates list.
     */
    public void testOutOfOrderMasks() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(new FileSystem[] {
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1",
                "folder/file2",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file2_hidden",
                "folder/file3_hidden",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file3",
            }),
        });
        fs.setPropagateMasks(false);
        try {
            assertEquals("folder/file2_hidden did not mask an earlier file but folder/file3_hidden masked a later one",
                "file1/file2",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
            TestUtilHid.destroyXMLFileSystem(getName() + "3");
        }
    }
    public void testOutOfOrderMasksPropagate() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(new FileSystem[] {
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1",
                "folder/file2",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file2_hidden",
                "folder/file3_hidden",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file3",
            }),
        });
        fs.setPropagateMasks(true);
        try {
            System.err.println("tOOOMP: " + childrenNames(fs));//XXX
            assertEquals("folder/file2_hidden did not mask an earlier file but folder/file3_hidden masked a later one",
                "file1/file2/file2_hidden/file3_hidden",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
            TestUtilHid.destroyXMLFileSystem(getName() + "3");
        }
    }
    
    /**
     * Check that a mask cannot be parallel to the masked file in the delegates list.
     */
    public void testParallelMasks() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(new FileSystem[] {
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file2",
                "folder/file2_hidden",
                "folder/file3",
            }),
        });
        fs.setPropagateMasks(false);
        try {
            assertEquals("folder/file2_hidden does not mask a file from the same layer",
                "file1/file2/file3",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
        }
    }
    public void testParallelMasksPropagate() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(new FileSystem[] {
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file2",
                "folder/file2_hidden",
                "folder/file3",
            }),
        });
        fs.setPropagateMasks(true);
        try {
            System.err.println("tPMP: " + childrenNames(fs));//XXX
            assertEquals("folder/file2_hidden does not mask a file from the same layer",
                "file1/file2/file2_hidden/file3",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
        }
    }
    
    // XXX test create -> mask -> recreate in same MFS
    
}
