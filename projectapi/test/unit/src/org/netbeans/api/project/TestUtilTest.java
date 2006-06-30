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

package org.netbeans.api.project;

import java.net.URL;
import java.util.Date;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Test functionality of TestUtil.
 * @author Jesse Glick
 */
public class TestUtilTest extends NbTestCase {

    public TestUtilTest(String name) {
        super(name);
    }

    protected void tearDown() throws Exception {
        TestUtil.setLookup(new Object[0]);
        super.tearDown();
    }
    
    public void testSetLookup() throws Exception {
        TestUtil.setLookup(Lookups.singleton("hello"));
        assertEquals("initial lookup works", "hello", Lookup.getDefault().lookup(String.class));
        TestUtil.setLookup(Lookups.singleton("goodbye"));
        assertEquals("modified lookup works", "goodbye", Lookup.getDefault().lookup(String.class));
        TestUtil.setLookup(Lookup.EMPTY);
        assertEquals("cleared lookup works", null, Lookup.getDefault().lookup(String.class));
    }
    
    public void testCreateFileFromContent() throws Exception {
        URL content = TestUtilTest.class.getResource("TestUtilTest.class");
        assertNotNull("have TestUtilTest.class", content);
        int length = content.openConnection().getContentLength();
        assertTrue("have some length", length > 0);
        FileObject scratch = TestUtil.makeScratchDir(this);
        assertTrue("scratch is a dir", scratch.isFolder());
        assertEquals("scratch is empty", 0, scratch.getChildren().length);
        FileObject a = TestUtil.createFileFromContent(content, scratch, "d/a");
        assertTrue("a is a file", a.isData());
        assertEquals("right path", "d/a", FileUtil.getRelativePath(scratch, a));
        assertEquals("right length", length, (int)a.getSize());
        FileObject b = TestUtil.createFileFromContent(null, scratch, "d2/b");
        assertTrue("b is a file", b.isData());
        assertEquals("right path", "d2/b", FileUtil.getRelativePath(scratch, b));
        assertEquals("b is empty", 0, (int)b.getSize());
        Date created = b.lastModified();
        Thread.sleep(1500); // Unix has coarse timestamp marking
        assertEquals("got same b back", b, TestUtil.createFileFromContent(null, scratch, "d2/b"));
        Date modified = b.lastModified();
        assertTrue("touched and changed timestamp", modified.after(created));
    }
    
}
