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
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import javax.swing.JList;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.SuiteSubModulesListModel;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Tests {@link SuiteProperties}
 *
 * @author Martin Krauskopf
 */
public class SuitePropertiesTest extends TestBase {
    
    private SuiteProject suite1Prj;
    private SuiteProperties suite1Props;
    private FileObject suite1FO;
    
    public SuitePropertiesTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        File suite1 = copyFolder(file(extexamplesF, "suite1"));
        suite1FO = FileUtil.toFileObject(suite1);
        suite1Prj = (SuiteProject) ProjectManager.getDefault().findProject(suite1FO);
        SubprojectProvider spp = (SubprojectProvider) suite1Prj.getLookup().lookup(SubprojectProvider.class);
        Set/*<Project>*/ subModules = spp.getSubprojects();
        this.suite1Props = new SuiteProperties(
                suite1Prj.getHelper(), suite1Prj.getEvaluator(), subModules);
    }
    
    private SuiteProject getSuite1Project(boolean reload) throws IOException {
        if (reload) {
            suite1Prj = (SuiteProject) ProjectManager.getDefault().findProject(suite1FO);
        }
        return suite1Prj;
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
    
    public void testRemoveAllSubModules() throws Exception {
        SuiteSubModulesListModel model = suite1Props.getModulesListModel();
        assertNotNull(model);
        
        // simulate removing all items from the list
        JList moduleList = new JList(model);
        moduleList.setSelectedIndices(new int[] {0, model.getSize() - 1});
        model.removeModules(Arrays.asList(moduleList.getSelectedValues()));
        assertEquals("none subModule should left", 0, model.getSize());
        
        saveProperties(suite1Props);
        
        SubprojectProvider spp = (SubprojectProvider) getSuite1Project(true).
                getLookup().lookup(SubprojectProvider.class);
        assertEquals("none module should be left", 0, spp.getSubprojects().size());
    }
    
    public void testRemoveOneSubModule() throws Exception {
        SuiteSubModulesListModel model = suite1Props.getModulesListModel();
        assertNotNull(model);
        
        // simulate removing all items from the list
        JList moduleList = new JList(model);
        moduleList.setSelectedIndex(0);
        model.removeModules(Arrays.asList(moduleList.getSelectedValues()));
        assertEquals("one subModule should left", 1, model.getSize());
        
        saveProperties(suite1Props);
        
        SubprojectProvider spp = (SubprojectProvider) getSuite1Project(true).
                getLookup().lookup(SubprojectProvider.class);
        assertEquals("one module should be left", 1, spp.getSubprojects().size());
        assertEquals("lib module should be the one", "org.netbeans.examples.modules.lib",
                ((NbModuleProject) spp.getSubprojects().toArray()[0]).getCodeNameBase());
    }
    
    public void testAddSubModule() throws Exception {
        SuiteSubModulesListModel model = suite1Props.getModulesListModel();
        assertNotNull(model);
        
        // simulate removing all items from the list
        Project antProject = ProjectManager.getDefault().findProject(FileUtil.toFileObject(file("ant/project")));
        assertNotNull(antProject);
        model.addModule(antProject);
        assertEquals("one project should be added", 3, model.getSize());
        
        saveProperties(suite1Props);
        
        SubprojectProvider spp = (SubprojectProvider) getSuite1Project(true).
                getLookup().lookup(SubprojectProvider.class);
        assertEquals("one module should be left", 3, spp.getSubprojects().size());
        assertTrue("ant/project is there", spp.getSubprojects().contains(antProject));
    }
    
    private void saveProperties(final SuiteProperties props) {
        try {
            // Store properties
            Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    props.storeProperties();
                    return Boolean.TRUE;
                }
            });
            // and save the project
            if (result == Boolean.TRUE) {
                ProjectManager.getDefault().saveProject(getSuite1Project(false));
            }
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
}
