/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.queries;

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
        assertOwnership("ant", "nbbuild/netbeans/ide6/modules/org-apache-tools-ant-module.jar");
        // Explicitly listed additions:
        assertOwnership("ant", "nbbuild/netbeans/ide6/ant/nblib/bridge.jar");
        // Pattern matches (here "ant/lib/"):
        assertTrue("ant module built (cannot scan by pattern unless files exist)", file("nbbuild/netbeans/ide6/ant/lib/ant.jar").isFile());
        assertOwnership("ant", "nbbuild/netbeans/ide6/ant/lib/ant.jar");
        // These two always included:
        assertOwnership("ant", "nbbuild/netbeans/ide6/config/Modules/org-apache-tools-ant-module.xml");
        assertOwnership("ant", "nbbuild/netbeans/ide6/update_tracking/org-apache-tools-ant-module.xml");
        // Different pattern match ("modules/ext/jh*.jar"):
        assertOwnership("core/javahelp", "nbbuild/netbeans/platform6/modules/ext/jh-2.0_02.jar");
        // Use of release dir:
        assertOwnership("extbrowser", "nbbuild/netbeans/ide6/modules/lib/extbrowser.dll");
    }
    
    public void testOwnershipExternal() throws Exception {
        // Will not normally exist when test is run:
        assertOwnership(EEP + "/suite1/action-project", EEP + "/suite1/build/cluster/modules/org-netbeans-examples-modules-action.jar");
        assertOwnership(EEP + "/suite1/action-project", EEP + "/suite1/build/cluster/update_tracking/org-netbeans-examples-modules-action.xml");
    }
    
    private void assertOwnership(String project, String file) throws Exception {
        FileObject projectFO = FileUtil.toFileObject(file(project));
        assertNotNull("have project " + project, projectFO);
        // This has the side effect of forcing a scan of the module universe:
        Project p = ProjectManager.getDefault().findProject(projectFO);
        assertNotNull("have a project in " + project, p);
        FileObject fileFO = FileUtil.toFileObject(file(file));
        if (fileFO != null) { // OK if not currently built
            assertEquals("correct owner by FileObject of " + file, p, FileOwnerQuery.getOwner(fileFO));
        }
        assertEquals("correct owner by URI of " + file, p, FileOwnerQuery.getOwner(file(file).toURI()));
    }
    
}
