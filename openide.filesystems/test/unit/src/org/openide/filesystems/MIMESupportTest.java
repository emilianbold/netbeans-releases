/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems;
import java.io.IOException;
import java.lang.ref.*;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class MIMESupportTest extends NbTestCase {
    
    public MIMESupportTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testFindMIMETypeCanBeGarbageCollected() throws IOException {
        FileObject fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "Ahoj.bla");
        
        String expResult = "content/unknown";
        String result = FileUtil.getMIMEType(fo);
        assertEquals("some content found", expResult, result);
        
        WeakReference r = new WeakReference(fo);
        fo = null;
        assertGC("Can be GCed", r);
    }
    
}
