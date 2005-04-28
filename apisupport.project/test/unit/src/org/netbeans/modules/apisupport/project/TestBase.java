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
package org.netbeans.modules.apisupport.project;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Basic setup for all the tests.
 * @author Jesse Glick
 */
abstract class TestBase extends NbTestCase {
    
    protected TestBase(String name) {
        super(name);
    }
    
    protected File nbrootF;
    protected FileObject nbroot;
    protected void setUp() throws Exception {
        super.setUp();
        nbrootF = new File(System.getProperty("test.nbroot"));
        assertTrue("there is a dir " + nbrootF, nbrootF.isDirectory());
        assertTrue("nbbuild exists", new File(nbrootF, "nbbuild").isDirectory());
        nbroot = FileUtil.toFileObject(nbrootF);
        assertNotNull("have a file object for nbroot", nbroot);
    }
    
    protected File file(File root, String path) {
        return new File(root, path.replace('/', File.separatorChar));
    }
    
    protected File file(String path) {
        return file(nbrootF, path);
    }
    
}
