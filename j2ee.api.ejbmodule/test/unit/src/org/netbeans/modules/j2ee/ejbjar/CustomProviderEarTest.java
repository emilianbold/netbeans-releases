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

package org.netbeans.modules.j2ee.ejbjar;

import java.io.File;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.spi.ejbjar.EarProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 * Base class for web module project tests.
 * @author Pavel Buzek
 */
public class CustomProviderEarTest extends NbTestCase {
    
    static {
        CustomProviderTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    public CustomProviderEarTest (String name) {
        super(name);
    }
    
    private FileObject datadir;
    
    protected void setUp() throws Exception {
        super.setUp();
        File f = FileUtil.normalizeFile(new File(System.getProperty("test.data.dir")));
        assertTrue("example dir exists", f.exists());
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory (f);
        Repository.getDefault ().addFileSystem (lfs);
        datadir = FileUtil.toFileObject (f);
        assertNotNull ("no FileObject", datadir);
    }

    public void testProviders () throws Exception {
        Lookup.Result res = Lookup.getDefault ().lookup (new Lookup.Template (EarProvider.class));
        assertEquals ("there should be 2 instances - one from j2ee/ejbapi and one from tests", 2, res.allInstances ().size ());
    }
    
    public void testGetEar () throws Exception {
        FileObject foo = datadir.getFileObject ("a.foo");
        FileObject bar = datadir.getFileObject ("b.bar");
        Ear wm1 = Ear.getEar (foo);
        assertNotNull ("found ejb module", wm1);
        Ear wm2 = Ear.getEar (bar);
        assertNull ("no ejb module", wm2);
    }
}
