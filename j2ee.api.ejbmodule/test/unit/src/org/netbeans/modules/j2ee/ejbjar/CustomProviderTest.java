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

package org.netbeans.modules.j2ee.ejbjar;

import java.io.File;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
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
public class CustomProviderTest extends NbTestCase {
    
    static {
        CustomProviderTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    public CustomProviderTest (String name) {
        super(name);
    }
    
    private FileObject datadir;
    
    protected void setUp() throws Exception {
        super.setUp();
        File f = getDataDir();
        assertTrue("example dir exists", f.exists());
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory (f);
        Repository.getDefault ().addFileSystem (lfs);
        datadir = FileUtil.toFileObject (f);
        assertNotNull ("no FileObject", datadir);
    }

    public void testProviders () throws Exception {
        Lookup.Result res = Lookup.getDefault ().lookup (new Lookup.Template (EjbJarProvider.class));
        assertEquals ("there should be 2 instances - one from j2ee/ejbapi and one from tests", 2, res.allInstances ().size ());
    }
    
    public void testGetEjbJar () throws Exception {
        FileObject foo = datadir.getFileObject ("a.foo");
        FileObject bar = datadir.getFileObject ("b.bar");
        EjbJar wm1 = EjbJar.getEjbJar (foo);
        assertNotNull ("found ejb module", wm1);
        EjbJar wm2 = EjbJar.getEjbJar (bar);
        assertNull ("no ejb module", wm2);
    }
}
