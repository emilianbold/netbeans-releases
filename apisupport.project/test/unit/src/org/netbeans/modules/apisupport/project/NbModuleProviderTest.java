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

import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
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
        FileObject dir = nbCVSRoot().getFileObject("java/project");
        assertNotNull("have java/project checked out", dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        NbModuleProvider nmtp = (NbModuleProvider) p.getLookup().lookup(NbModuleProvider.class);
        assertNotNull("has NbModuleProvider", nmtp);
        assertSame("is netbeans.org modules", NbModuleProvider.NETBEANS_ORG, nmtp.getModuleType());
        
        FileObject suite1 = resolveEEP("suite1");
        FileObject action = suite1.getFileObject("action-project");
        p = ProjectManager.getDefault().findProject(action);
        nmtp = (NbModuleProvider) p.getLookup().lookup(NbModuleProvider.class);
        assertNotNull("has NbModuleProvider", nmtp);
        assertSame("is suite-component module", NbModuleProvider.SUITE_COMPONENT, nmtp.getModuleType());
        
        FileObject suite3 = resolveEEP("suite3");
        FileObject dummy = suite3.getFileObject("dummy-project");
        p = ProjectManager.getDefault().findProject(dummy);
        nmtp = (NbModuleProvider) p.getLookup().lookup(NbModuleProvider.class);
        assertNotNull("has NbModuleProvider", nmtp);
        assertSame("is standalone modules", NbModuleProvider.STANDALONE, nmtp.getModuleType());
    }
    
}
