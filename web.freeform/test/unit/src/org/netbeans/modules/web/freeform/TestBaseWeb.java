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

package org.netbeans.modules.web.freeform;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Base class for web module project tests.
 * @author Pavel Buzek
 */
abstract class TestBaseWeb extends NbTestCase {
    
    static {
        TestBaseWeb.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    protected TestBaseWeb (String name) {
        super(name);
    }
    
    protected File egdir;
    protected FileObject buildProperties;
    protected FreeformProject jakarta;
    protected FileObject helloWorldServlet;
    protected FileObject helloWorldJsp;
    protected FileObject jakartaIndex;
    
    protected void setUp() throws Exception {
        super.setUp();
        egdir = FileUtil.normalizeFile(new File(System.getProperty("test.data.dir"), "example-projects"));
        assertTrue("example dir " + egdir + " exists", egdir.exists());
        FileObject jakartaDir = FileUtil.toFileObject(egdir).getFileObject("web_jakarta");
        assertNotNull("found projdir", jakartaDir);
        Project _jakarta = ProjectManager.getDefault().findProject(jakartaDir);
        assertNotNull("have a project", _jakarta);
        jakarta = (FreeformProject)_jakarta;
        helloWorldServlet = jakartaDir.getFileObject("src/mypackage/HelloWorld.java");
        assertNotNull("found HelloWorld.java", helloWorldServlet);
        helloWorldJsp = jakartaDir.getFileObject("web/hello.jsp");
        assertNotNull("found hello.jsp", helloWorldJsp);
        jakartaIndex = jakartaDir.getFileObject("web/index.html");
        assertNotNull("found index.html", jakartaIndex);
        buildProperties = jakartaDir.getFileObject("build.properties");
        assertNotNull("found build.properties", buildProperties);
    }
    
}
