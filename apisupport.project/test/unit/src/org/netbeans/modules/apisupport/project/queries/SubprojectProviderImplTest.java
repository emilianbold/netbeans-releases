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

import java.io.File;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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
            "libs/swing-layout",
            "core/progress",
            "projects/projectapi",
            "projects/projectuiapi",
        });
    }
    
    public void testExternalSubprojects() throws Exception {
        checkSubprojects(resolveEEPPath("/suite1/action-project"), new String[] {
            resolveEEPPath("/suite1/support/lib-project"),
            file("openide/dialogs").getAbsolutePath(),
        });
        checkSubprojects(resolveEEPPath("/suite1/support/lib-project"), new String[0]);
        // No sources for beans available, so no subprojects reported:
        checkSubprojects(resolveEEPPath("/suite3/dummy-project"), new String[0]);
    }
    
    /** @see "#63824" */
    public void testAdHocSubprojects() throws Exception {
        assertDepends("mdr/module", "mdr");
        assertDepends("ide/applemenu", "ide/applemenu/eawtstub");
    }
    
    /** @see "#77533" */
    public void testSelfRefWithClassPathExts() throws Exception {
        checkSubprojects("apisupport/samples/PaintApp-suite/ColorChooser", new String[0]);
    }
    
    /** @see "#81878" */
    public void testInclusionOfHigherBin() throws Exception {
        checkSubprojects("httpserver/servletapi", new String[0]);
    }
    
    private void checkSubprojects(String project, String[] subprojects) throws Exception {
        Project p = project(project);
        SubprojectProvider spp = (SubprojectProvider) p.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("have SPP in " + p, spp);
        SortedSet/*<String>*/ expected = new TreeSet();
        for (int i = 0; i < subprojects.length; i++) {
            File f = new File(subprojects[i]);
            if (!f.isAbsolute()) {
                f = file(subprojects[i]);
            }
            expected.add(f.toURI().toString());
        }
        SortedSet/*<String>*/ actual = new TreeSet();
        Iterator it = spp.getSubprojects().iterator();
        while (it.hasNext()) {
            actual.add(((Project) it.next()).getProjectDirectory().getURL().toExternalForm());
        }
        assertEquals("correct subprojects for " + project, expected.toString(), actual.toString());
    }
    
    private Project project(String path) throws Exception {
        FileObject dir = FileUtil.toFileObject(PropertyUtils.resolveFile(nbCVSRootFile(), path));
//        FileObject dir = nbCVSRoot().getFileObject(path);
        assertNotNull("have " + path, dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        assertNotNull("have project in " + path, p);
        return p;
    }
    
    private void assertDepends(String parent, String child) throws Exception {
        Project p1 = project(parent);
        Project p2 = project(child);
        SubprojectProvider spp = (SubprojectProvider) p1.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("have SPP in " + p1, spp);
        assertTrue(parent + " includes " + child, spp.getSubprojects().contains(p2));
    }
    
}
