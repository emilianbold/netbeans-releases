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

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;

/**
 * Test subprojects.
 * @author Jesse Glick
 */
public class SubprojectProviderImplTest extends TestBase {
    
    public SubprojectProviderImplTest(String name) {
        super(name);
    }
    
    public void testNetBeansOrgSubprojects() throws Exception {
        // Keep in synch with ant/nbproject/project.xml:
        checkSubprojects("ant", new String[] {
            "openide/fs",
            "openide/util",
            "openide/modules",
            "openide/nodes",
            "openide/awt",
            "openide/dialogs",
            "openide/options",
            "openide/windows",
            "openide/text",
            "openide/actions",
            "openide/execution",
            "openide/io",
            "openide/loaders",
            "xml/api",
            "core/navigator",
            "openide/explorer",
            "core/options",
            "libs/form-layout",
            "core/progress",
        });
    }
    
    public void testExternalSubprojects() throws Exception {
        checkSubprojects(EEP + "/suite1/action-project", new String[] {
            EEP + "/suite1/support/lib-project",
            "openide/dialogs",
        });
        checkSubprojects(EEP + "/suite1/support/lib-project", new String[0]);
        // No sources for beans available, so no subprojects reported:
        checkSubprojects(EEP + "/suite3/dummy-project", new String[0]);
    }
    
    private void checkSubprojects(String project, String[] subprojects) throws Exception {
        Project p = project(project);
        SubprojectProvider spp = (SubprojectProvider) p.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("have SPP in " + p, spp);
        SortedSet/*<String>*/ expected = new TreeSet();
        for (int i = 0; i < subprojects.length; i++) {
            expected.add(file(subprojects[i]).toURI().toString());
        }
        SortedSet/*<String>*/ actual = new TreeSet();
        Iterator it = spp.getSubprojects().iterator();
        while (it.hasNext()) {
            actual.add(((Project) it.next()).getProjectDirectory().getURL().toExternalForm());
        }
        assertEquals("correct subprojects for " + project, expected.toString(), actual.toString());
    }
    private Project project(String path) throws Exception {
        FileObject dir = nbroot.getFileObject(path);
        assertNotNull("have " + path, dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        assertNotNull("have project in " + path, p);
        return p;
    }
    
}
