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

package org.netbeans.modules.apisupport.project;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.openide.filesystems.FileObject;

/**
 * Verify that loading modules does not automatically do a module list scan.
 * @author Jesse Glick
 * @see "issue #59550"
 */
public class AvoidModuleListInProjectConstructorTest extends TestBase {

    public AvoidModuleListInProjectConstructorTest(String name) {
        super(name);
    }
    
    public void testNetBeansOrgModules() throws Exception {
        assertEquals("no scans of netbeans.org initially", 0, ModuleList.getKnownEntries(file("nbbuild/netbeans/ide6/org-apache-tools-ant-module.jar")).size());
        FileObject fo = nbroot.getFileObject("ant");
        Project p = ProjectManager.getDefault().findProject(fo);
        assertNotNull(p);
        assertEquals("still no scans", 0, ModuleList.getKnownEntries(file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-apache-tools-ant-module.jar")).size());
        assertEquals("org.apache.tools.ant.module", ProjectUtils.getInformation(p).getName());
        assertEquals("still no scans", 0, ModuleList.getKnownEntries(file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-apache-tools-ant-module.jar")).size());
        ClassPath.getClassPath(fo.getFileObject("src"), ClassPath.COMPILE);
        assertEquals("now have scanned something", 1, ModuleList.getKnownEntries(file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-apache-tools-ant-module.jar")).size());
    }
    
}
