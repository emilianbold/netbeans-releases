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

package org.netbeans.api.project;

import java.net.URI;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Test functionality of FileOwnerQuery.
 * @author Jesse Glick
 */
public class FileOwnerQueryTest extends NbTestCase {
    
    public FileOwnerQueryTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject randomfile;
    private FileObject projfile;
    private Project p;
    
    protected void setUp() throws Exception {
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("testproject");
        p = ProjectManager.getDefault().findProject(projdir);
        randomfile = scratch.createData("randomfile");
        projfile = projdir.createData("projfile");
        TestUtil.setLookup(new Object[] {
            TestUtil.testProjectFactory(),
        }, FileOwnerQueryTest.class.getClassLoader());
    }
    
    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        randomfile = null;
        projfile = null;
        p = null;
        TestUtil.setLookup(Lookup.EMPTY);
    }
    
    public void testFileOwner() throws Exception {
        assertEquals("correct project from projfile FileObject", p, FileOwnerQuery.getOwner(projfile));
        assertEquals("correct project from projfile URI", p, FileOwnerQuery.getOwner(FileUtil.toFile(projfile).toURI()));
        assertEquals("no project from randomfile FileObject", null, FileOwnerQuery.getOwner(randomfile));
        assertEquals("no project from randomfile URI", null, FileOwnerQuery.getOwner(FileUtil.toFile(randomfile).toURI()));
        assertEquals("no project in C:\\", null, FileOwnerQuery.getOwner(URI.create("file:/C:/")));
    }
    
}
