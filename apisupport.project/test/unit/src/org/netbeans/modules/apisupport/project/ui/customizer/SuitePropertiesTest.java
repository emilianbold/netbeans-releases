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

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import javax.swing.JList;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.SuiteProvider;
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
 * Tests {@link SuiteProperties}. Actually also for some classes which
 * SuiteProperties utilizes - which doesn't mean they shouldn't be tested
 * individually :)
 *
 * @author Martin Krauskopf
 */
public class SuitePropertiesTest extends TestBase {
    
    private FileObject suiteRepoFO;
    private SuiteProject suite1Prj;
    private SuiteProject suite2Prj;
    private SuiteProperties suite1Props;
    private FileObject suite1FO;
    private FileObject suite2FO;
    
    public SuitePropertiesTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        suiteRepoFO = FileUtil.toFileObject(copyFolder(extexamplesF));
        suite1FO = suiteRepoFO.getFileObject("suite1");
        suite2FO = suiteRepoFO.getFileObject("suite2");
        suite1Prj = (SuiteProject) ProjectManager.getDefault().findProject(suite1FO);
        suite2Prj = (SuiteProject) ProjectManager.getDefault().findProject(suite1FO);
        SubprojectProvider suite1spp = (SubprojectProvider) suite1Prj.getLookup().lookup(SubprojectProvider.class);
        Set/*<Project>*/ suite1subModules = suite1spp.getSubprojects();
        this.suite1Props = new SuiteProperties(suite1Prj, suite1Prj.getHelper(),
                suite1Prj.getEvaluator(), suite1subModules);
    }
    
    private Project findSuite1Project() throws IOException {
        return ProjectManager.getDefault().findProject(suite1FO);
    }
    
    private SubprojectProvider getSubProjectProvider(Project project) throws IOException {
        return (SubprojectProvider) project.getLookup().lookup(SubprojectProvider.class);
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

        SubprojectProvider spp = getSubProjectProvider(findSuite1Project());
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

        SubprojectProvider spp = getSubProjectProvider(findSuite1Project());
        assertEquals("one module should be left", 1, spp.getSubprojects().size());
        NbModuleProject libProject = (NbModuleProject) spp.getSubprojects().toArray()[0];
        assertEquals("lib module should be the one", "org.netbeans.examples.modules.lib",
                libProject.getCodeNameBase());
        NbModuleTypeProvider libProjectNmtp = (NbModuleTypeProvider) libProject.
                getLookup().lookup(NbModuleTypeProvider.class);
        assertSame("lib module is still suite component module", NbModuleTypeProvider.SUITE_COMPONENT,
                libProjectNmtp.getModuleType());

        // assert that the remove module (action-project) is standalone
        FileObject actionFO = suite1FO.getFileObject("action-project");
        Project actionProject = ProjectManager.getDefault().findProject(actionFO);
        NbModuleTypeProvider actionNmtp = (NbModuleTypeProvider) actionProject.
                getLookup().lookup(NbModuleTypeProvider.class);
        assertNotNull(actionNmtp);
        assertSame("action-project module is standalone module now", NbModuleTypeProvider.STANDALONE,
                actionNmtp.getModuleType());
    }

    public void testMoveSubModuleBetweenSuites() throws Exception {
        FileObject miscFO = suite2FO.getFileObject("misc-project");
        Project miscProject = ProjectManager.getDefault().findProject(miscFO);
        assertNotNull(miscProject);
        SuiteProvider sp = (SuiteProvider) miscProject.getLookup().lookup(SuiteProvider.class);
        assertNotNull(sp);
        assertEquals("is in suite2", FileUtil.toFile(suite2FO), sp.getSuiteDirectory());

        // simulate addition of miscProject to the suite1
        SuiteSubModulesListModel model = suite1Props.getModulesListModel();
        assertNotNull(model);
        model.addModule(miscProject);
        assertEquals("one project should be added", 3, model.getSize());

        // saves all changes
        saveProperties(suite1Props);

        // assert miscProject is part of suite1 (has moved from suite2)....
        SubprojectProvider suite1spp = getSubProjectProvider(findSuite1Project());
        assertEquals("one module should be left", 3, suite1spp.getSubprojects().size());
        assertTrue("misc-project has moved to suite1", suite1spp.getSubprojects().contains(miscProject));

        // ....and as such has correctly set suite provider
        sp = (SuiteProvider) miscProject.getLookup().lookup(SuiteProvider.class);
        assertNotNull(sp);
        assertNotNull(sp.getSuiteDirectory());
        assertEquals("misc-project was moved from suite2 to suite1", FileUtil.toFile(suite1FO), sp.getSuiteDirectory());

        // assert miscProject is not part of suite2
        SubprojectProvider suite2spp = getSubProjectProvider(miscProject);
        assertFalse("misc-project is not part of suite2 anymore",
                suite2spp.getSubprojects().contains(miscProject));

    }
    
    public void testRemovingOneModuleFromThree_63307() throws Exception {
        SuiteProject suite1 = TestBase.generateSuite(getWorkDir(), "suite1");
        assert suite1 != null;
        NbModuleProject module1 = TestBase.generateSuiteComponent(suite1, "module1");
        assert module1 != null;
        NbModuleProject module2 = TestBase.generateSuiteComponent(suite1, "module2");
        assert module2 != null;
        NbModuleProject module3 = TestBase.generateSuiteComponent(suite1, "module3");
        assert module3 != null;
        
        SubprojectProvider spp = (SubprojectProvider) suite1.getLookup().lookup(SubprojectProvider.class);
        Set/*<Project>*/ subModules = spp.getSubprojects();
        SuiteProperties suiteProps = new SuiteProperties(suite1, suite1.getHelper(),
                suite1.getEvaluator(), subModules);
        
        SuiteSubModulesListModel model = suiteProps.getModulesListModel();
        assertEquals("three module suite components", 3, model.getSize());
        model.removeModules(Arrays.asList(new Object[] { module2 }));
        
        saveProperties(suiteProps);
        
        suiteProps.refresh(spp.getSubprojects());
        assertEquals("two module suite components", 2, suiteProps.getModulesListModel().getSize());
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
                ProjectManager.getDefault().saveProject(props.getProject());
            }
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
}
