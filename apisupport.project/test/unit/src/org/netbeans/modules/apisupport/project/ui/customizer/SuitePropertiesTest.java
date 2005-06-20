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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
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
    
    private SuiteProperties suiteProps;
    
    public SuitePropertiesTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        File suite1 = copyFolder(file(extexamplesF, "suite1"));
        FileObject suite1FO = FileUtil.toFileObject(suite1);
        FileObject actionFO = suite1FO.getFileObject("action-project");
        NbModuleProject ap = (NbModuleProject) ProjectManager.getDefault().findProject(actionFO);
        SubprojectProvider spp = (SubprojectProvider) ap.getLookup().lookup(SubprojectProvider.class);
        Set/*<Project>*/ subModules = spp.getSubprojects();
        this.suiteProps = new SuiteProperties(
                ap.getHelper(), ap.evaluator(), subModules);
    }

    public void testPropertiesAreLoaded() {
        assertNotNull(suiteProps.getActivePlatform());
        assertEquals("platform id: ", "default", suiteProps.getActivePlatform().getID());
        SuiteSubModulesListModel model = suiteProps.getModulesListModel();
        assertNotNull(model);
        assertEquals("number of sub-modules: ", 2, model.getSize());
        // to be continue
    }
}
