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

package org.netbeans.modules.apisupport.project.queries;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test that project association by module inclusion list works.
 * @author Jesse Glick
 */
public class UpdateTrackingFileOwnerQueryTest extends TestBase {

    public UpdateTrackingFileOwnerQueryTest(String name) {
        super(name);
    }
    
    public void testOwnershipNetBeansOrg() throws Exception {
        // Basic module:
        assertOwnership("ant", "nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-apache-tools-ant-module.jar");
        // Explicitly listed additions:
        assertOwnership("ant", "nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/ant/nblib/bridge.jar");
        // Pattern matches (here "ant/lib/"):
        assertTrue("ant module built (cannot scan by pattern unless files exist)", file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/ant/lib/ant.jar").isFile());
        assertOwnership("ant", "nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/ant/lib/ant.jar");
        // These two always included:
        assertOwnership("ant", "nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/config/Modules/org-apache-tools-ant-module.xml");
        assertOwnership("ant", "nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/update_tracking/org-apache-tools-ant-module.xml");
        // Different pattern match ("modules/ext/jh*.jar"):
        assertOwnership("core/javahelp", "nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/ext/jh-2.0_04.jar");
        // Use of release dir:
        assertOwnership("extbrowser", "nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/lib/extbrowser.dll");
    }
    
    public void testOwnershipExternal() throws Exception {
        // Will not normally exist when test is run:
        assertOwnership(EEP + "/suite1/action-project", EEP + "/suite1/build/cluster/modules/org-netbeans-examples-modules-action.jar");
        assertOwnership(EEP + "/suite1/action-project", EEP + "/suite1/build/cluster/update_tracking/org-netbeans-examples-modules-action.xml");
    }
    
    private void assertOwnership(String project, String file) throws Exception {
        FileObject projectFO = FileUtil.toFileObject(file(project));
        assertNotNull("have project " + project, projectFO);
        Project p = ProjectManager.getDefault().findProject(projectFO);
        assertNotNull("have a project in " + project, p);
        // This has the side effect of forcing a scan of the module universe:
        ClassPath.getClassPath(projectFO.getFileObject("src"), ClassPath.COMPILE);
        FileObject fileFO = FileUtil.toFileObject(file(file));
        if (fileFO != null) { // OK if not currently built
            assertEquals("correct owner by FileObject of " + file, p, FileOwnerQuery.getOwner(fileFO));
        }
        assertEquals("correct owner by URI of " + file, p, FileOwnerQuery.getOwner(file(file).toURI()));
    }
    
}
