/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available atou
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.util.Arrays;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectTest;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;

/**
 * Test ModuleOperations.
 *
 * @author Martin Krauskopf
 */
public class SuiteOperationsTest extends TestBase {
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
    }
    
    public SuiteOperationsTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
    }
    
    public void testDeleteOfEmptySuite() throws Exception {
        SuiteProject suite = TestBase.generateSuite(getWorkDir(), "suite");
        SuiteProjectTest.openSuite(suite);
        SuiteActions ap = (SuiteActions) suite.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        assertTrue("delete action is enabled", ap.isActionEnabled(ActionProvider.COMMAND_DELETE, null));
        
        FileObject prjDir = suite.getProjectDirectory();
        
        // build project
        ap.invokeActionImpl(ActionProvider.COMMAND_BUILD, suite.getLookup()).waitFinished();
        assertNotNull("suite was build", prjDir.getFileObject("build"));
        
        FileObject[] expectedMetadataFiles = new FileObject[] {
            prjDir.getFileObject(GeneratedFilesHelper.BUILD_XML_PATH),
            prjDir.getFileObject("nbproject"),
        };
        assertEquals("correct metadata files", Arrays.asList(expectedMetadataFiles), ProjectOperations.getMetadataFiles(suite));
        assertTrue("no data files", ProjectOperations.getDataFiles(suite).isEmpty());
        
        // It is hard to simulate exact scenario invoked by user. Let's test at least something.
        ProjectOperations.notifyDeleting(suite);
        prjDir.getFileSystem().refresh(true);
        assertNull(prjDir.getFileObject("build"));
    }
    
    public void testDeleteOfNonEmptySuite() throws Exception {
        SuiteProject suite = TestBase.generateSuite(getWorkDir(), "suite");
        NbModuleProject module1 = TestBase.generateSuiteComponent(suite, "module1");
        NbModuleProject module2 = TestBase.generateSuiteComponent(suite, "module2");
        assertEquals("module1 is suite component", NbModuleTypeProvider.SUITE_COMPONENT, Util.getModuleType(module1));
        assertEquals("module2 is suite component", NbModuleTypeProvider.SUITE_COMPONENT, Util.getModuleType(module2));
        module1.open();
        module2.open();
        
        SuiteProjectTest.openSuite(suite);
        SuiteActions ap = (SuiteActions) suite.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        assertTrue("delete action is enabled", ap.isActionEnabled(ActionProvider.COMMAND_DELETE, null));
        
        FileObject prjDir = suite.getProjectDirectory();
        
        // build project
        ap.invokeActionImpl(ActionProvider.COMMAND_BUILD, suite.getLookup()).waitFinished();
        assertNotNull("suite was build", prjDir.getFileObject("build"));
        
        FileObject[] expectedMetadataFiles = new FileObject[] {
            prjDir.getFileObject(GeneratedFilesHelper.BUILD_XML_PATH),
            prjDir.getFileObject("nbproject"),
        };
        assertEquals("correct metadata files", Arrays.asList(expectedMetadataFiles), ProjectOperations.getMetadataFiles(suite));
        assertTrue("no data files", ProjectOperations.getDataFiles(suite).isEmpty());
        
        // It is hard to simulate exact scenario invoked by user. Let's test at least something.
        ProjectOperations.notifyDeleting(suite);
        prjDir.getFileSystem().refresh(true);
        assertNull(prjDir.getFileObject("build"));
        
        assertEquals("module1 became standalone module", NbModuleTypeProvider.STANDALONE, Util.getModuleType(module1));
        assertEquals("module2 became standalone module", NbModuleTypeProvider.STANDALONE, Util.getModuleType(module2));
    }
    
}
