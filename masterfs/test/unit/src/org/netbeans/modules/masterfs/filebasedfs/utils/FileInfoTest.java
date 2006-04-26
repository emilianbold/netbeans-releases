/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.utils;

import junit.framework.*;
import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Radek Matous
 */
public class FileInfoTest extends NbTestCase {
    
    public FileInfoTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(FileInfoTest.class);
        
        return suite;
    }

    public void testComposeName() {
        testComposeNameImpl("a.b");
        testComposeNameImpl(".b");
        testComposeNameImpl("a.");
    }


    private void testComposeNameImpl(final String fullName) {
        String ext = FileInfo.getExt(fullName);
        String name = FileInfo.getName(fullName);
        
        assertEquals(fullName,FileInfo.composeName(name, ext));
    }
}
