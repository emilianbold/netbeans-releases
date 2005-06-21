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

package org.netbeans.modules.apisupport.project.ui.customizer;
import java.io.File;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.SuiteSubModulesListModel;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests {@link SuiteProperties}
 *
 * @author Martin Krauskopf
 */
public class SuitePropertiesTest extends TestBase {
    
    private SuiteProperties suite1Props;
    
    public SuitePropertiesTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        File suite1 = copyFolder(file(extexamplesF, "suite1"));
        FileObject suite1FO = FileUtil.toFileObject(suite1);
        SuiteProject suite1Prj = (SuiteProject) ProjectManager.getDefault().findProject(suite1FO);
        SubprojectProvider spp = (SubprojectProvider) suite1Prj.getLookup().lookup(SubprojectProvider.class);
        Set/*<Project>*/ subModules = spp.getSubprojects();
        this.suite1Props = new SuiteProperties(
                suite1Prj.getHelper(), suite1Prj.getEvaluator(), subModules);
    }

    public void testPropertiesAreLoaded() throws Exception {
        assertNotNull(suite1Props.getActivePlatform());
        assertEquals("platform id: ", "default", suite1Props.getActivePlatform().getID());
        SuiteSubModulesListModel model = suite1Props.getModulesListModel();
        assertNotNull(model);
        assertEquals("number of sub-modules: ", 2, model.getSize());
        // assert order by display name ("Demo Action", "Demo Library")
        NbModuleProject p = (NbModuleProject) model.getElementAt(0);
        assertEquals("action project first", p.getCodeNameBase(), "org.netbeans.examples.modules.action");
        p = (NbModuleProject) model.getElementAt(1);
        assertEquals("lib project first", p.getCodeNameBase(), "org.netbeans.examples.modules.lib");
    }
    
}
