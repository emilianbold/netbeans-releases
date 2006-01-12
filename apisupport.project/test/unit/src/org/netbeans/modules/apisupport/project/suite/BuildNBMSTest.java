/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.apisupport.project.DialogDisplayerImpl;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.ui.SuiteActions;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;

/**
 * Checks building of NBM files.
 * @author Petr Zajac
 */
public class BuildNBMSTest extends TestBase {
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
    }
    
    private SuiteProject suite;
    
    public BuildNBMSTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        super.setUp();

        InstalledFileLocatorImpl.registerDestDir(destDirF);
        
        suite = TestBase.generateSuite(new File(getWorkDir(), "projects"), "suite");
        NbModuleProject proj = TestBase.generateSuiteComponent(suite, "mod1");
        
        SuiteProjectTest.openSuite(suite);
        proj.open();
    }
    
    public void testBuildNBMS() throws Exception {
        SuiteActions p = (SuiteActions) suite.getLookup().lookup(ActionProvider.class);
        assertNotNull("Provider is here", p);
        
        List l = Arrays.asList(p.getSupportedActions());
        assertTrue("We support nbms: " + l, l.contains("nbms"));
        
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
        ExecutorTask task = p.invokeActionImpl("nbms", suite.getLookup());
        assertNotNull("did not even run task", task);
        task.waitFinished();
        FileObject nbmFo = suite.getProjectDirectory().getFileObject("build/updates/org-example-mod1.nbm");
        FileObject updatesXml = suite.getProjectDirectory().getFileObject("build/updates/updates.xml");
        assertNotNull("Nbm build/updates/org-example-mod1.nbm doesn't exist",nbmFo);
        assertNotNull("build/updates/updates.xml doesn't exist",updatesXml);
    }
    
}

    