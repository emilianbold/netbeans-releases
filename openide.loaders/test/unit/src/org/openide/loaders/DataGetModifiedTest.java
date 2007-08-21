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

import java.util.Iterator;
import org.openide.filesystems.*;
import java.beans.*;
import org.netbeans.junit.*;
import org.openide.cookies.EditorCookie;

public class DataGetModifiedTest extends NbTestCase {

    public DataGetModifiedTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir ();
        lfs = TestUtilHid.createLocalFileSystem (getWorkDir (), fsstruct);
        do1 = DataObject.find(lfs.findResource("/Dir/SubDir/A.txt"));
        do2 = DataObject.find(lfs.findResource("/Dir/SubDir/B.txt"));
        do3 = DataObject.find(lfs.findResource("/Dir/SubDir/C.txt"));
    }
    
    //Clear all stuff when the test finish
    @Override
    protected void tearDown() throws Exception {
        TestUtilHid.destroyLocalFileSystem (getName());
    }

    
    public void testCanChangeModifiedFilesWhenIterating() throws Exception {
        do1.getLookup().lookup(EditorCookie.class).openDocument().insertString(0, "Ahoj", null);
        do3.getLookup().lookup(EditorCookie.class).openDocument().insertString(0, "Ahoj", null);
        
        Iterator<DataObject> it = DataObject.getRegistry().getModifiedSet().iterator();
        assertTrue("There is modified 1", it.hasNext());
        DataObject m1 = it.next();
        if (m1 != do1 && m1 != do3) {
            fail("Strange modified object1 " + m1);
        }
        
        do2.getLookup().lookup(EditorCookie.class).openDocument().insertString(0, "Ahoj", null);
        
        
        assertTrue("There is modified 2", it.hasNext());
        
        DataObject m2 = it.next();
        if (m2 != do1 && m2 != do3) {
            fail("Strange modified object2 " + m2);
        }
        if (m1 == m2) {
            fail("Same modified object twice: " + m1 + " = " + m2);
        }
        
        assertFalse("No third object added when iterating", it.hasNext());
        
        assertEquals("But now visible", 3, DataObject.getRegistry().getModifiedSet().size());
    }
    
    
    private String fsstruct [] = new String [] {
        "Dir/SubDir/X.txt",
        "Dir/SubDir/T.txt",
        "Dir/SubDir/A.txt",
        "Dir/SubDir/B.txt",
        "Dir/SubDir/C.txt",
    };
    private FileSystem lfs;
    private DataObject do1;
    private DataObject do2;
    private DataObject do3;
}
