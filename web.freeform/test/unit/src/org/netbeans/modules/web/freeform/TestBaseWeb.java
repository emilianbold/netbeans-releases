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
        egdir = FileUtil.normalizeFile(new File(getDataDir(), "example-projects"));
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
