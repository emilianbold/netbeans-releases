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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Base class for tests.
 * @author Jesse Glick
 */
abstract class TestBase extends NbTestCase {
    
    static {
        TestBase.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    protected TestBase(String name) {
        super(name);
    }
    
    protected File egdir;
    protected FreeformProject simple;
    protected void setUp() throws Exception {
        super.setUp();
        egdir = FileUtil.normalizeFile(new File(System.getProperty("test.eg.dir")));
        assertTrue("example dir exists", egdir.exists());
        Project _simple = ProjectManager.getDefault().findProject(FileUtil.toFileObject(egdir).getFileObject("simple"));
        assertNotNull("have a project", _simple);
        simple = (FreeformProject)_simple;
    }
    
}
