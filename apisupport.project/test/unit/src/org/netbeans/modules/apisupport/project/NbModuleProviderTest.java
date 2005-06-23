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

package org.netbeans.modules.apisupport.project;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;

/**
 * Test if an NbModuleProject provides an NbModuleProvider in their lookup.
 *
 * @author Martin Krauskopf
 */
public class NbModuleProviderTest extends TestBase {
    
    public NbModuleProviderTest(String name) {
        super(name);
    }
    
    public void testNbModuleProvider() throws Exception {
        FileObject dir = nbroot.getFileObject("java/project");
        assertNotNull("have java/project checked out", dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        NbModuleTypeProvider nmtp = (NbModuleTypeProvider) p.getLookup().lookup(NbModuleTypeProvider.class);
        assertNotNull("has NbModuleProvider", nmtp);
        assertSame("is netbeans.org modules", NbModuleTypeProvider.NETBEANS_ORG, nmtp.getModuleType());
        
        FileObject suite1 = extexamples.getFileObject("suite1");
        FileObject action = suite1.getFileObject("action-project");
        p = ProjectManager.getDefault().findProject(action);
        nmtp = (NbModuleTypeProvider) p.getLookup().lookup(NbModuleTypeProvider.class);
        assertNotNull("has NbModuleProvider", nmtp);
        assertSame("is suite-component module", NbModuleTypeProvider.SUITE_COMPONENT, nmtp.getModuleType());
        
        FileObject suite3 = extexamples.getFileObject("suite3");
        FileObject dummy = suite3.getFileObject("dummy-project");
        p = ProjectManager.getDefault().findProject(dummy);
        nmtp = (NbModuleTypeProvider) p.getLookup().lookup(NbModuleTypeProvider.class);
        assertNotNull("has NbModuleProvider", nmtp);
        assertSame("is standalone modules", NbModuleTypeProvider.STANDALONE, nmtp.getModuleType());
    }
    
}
