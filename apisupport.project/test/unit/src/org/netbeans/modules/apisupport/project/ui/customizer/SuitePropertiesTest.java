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
import java.util.Enumeration;
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
        suiteRepoFO = prepareSuiteRepo(extexamples);
        suite1FO = suiteRepoFO.getFileObject("suite1");
        suite2FO = suiteRepoFO.getFileObject("suite2");
        suite1Prj = (SuiteProject) ProjectManager.getDefault().findProject(suite1FO);
        suite2Prj = (SuiteProject) ProjectManager.getDefault().findProject(suite1FO);
        SubprojectProvider suite1spp = (SubprojectProvider) suite1Prj.getLookup().lookup(SubprojectProvider.class);
        Set/*<Project>*/ suite1subModules = suite1spp.getSubprojects();
        this.suite1Props = new SuiteProperties(suite1Prj, suite1Prj.getHelper(),
                suite1Prj.getEvaluator(), suite1subModules);
    }
    
    private SuiteProject getSuite1Project(boolean reload) throws IOException {
        if (reload) {
            suite1Prj = (SuiteProject) ProjectManager.getDefault().findProject(suite1FO);
        }
        return suite1Prj;
    }
    
    private SuiteProject getSuite2Project(boolean reload) throws IOException {
        if (reload) {
            suite2Prj = (SuiteProject) ProjectManager.getDefault().findProject(suite2FO);
        }
        return suite2Prj;
    }
    
    private Project getMiscProject() throws IOException {
        FileObject miscFO = suite2FO.getFileObject("misc-project");
        return ProjectManager.getDefault().findProject(miscFO);
    }
    
    private SubprojectProvider getSuite1SPP(boolean reloadSuite1) throws IOException {
        return (SubprojectProvider) getSuite1Project(reloadSuite1).
                getLookup().lookup(SubprojectProvider.class);
    }
    
    private SubprojectProvider getSuite2SPP(boolean reloadSuite2) throws IOException {
        return (SubprojectProvider) getSuite2Project(reloadSuite2).
                getLookup().lookup(SubprojectProvider.class);
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
        NbModuleProject libProject = (NbModuleProject) spp.getSubprojects().toArray()[0];
        assertEquals("lib module should be the one", "org.netbeans.examples.modules.lib",
                libProject.getCodeNameBase());
        assertSame("lib module is still suite component module", NbModuleTypeProvider.SUITE_COMPONENT,
                libProject.getNbModuleTypeProvider().getModuleType());
        
        // assert that the remove module (action-project) is standalone
        FileObject actionFO = suite1FO.getFileObject("action-project");
        Project actionProject = ProjectManager.getDefault().findProject(actionFO);
        NbModuleTypeProvider actionNmtp = (NbModuleTypeProvider) actionProject.
                getLookup().lookup(NbModuleTypeProvider.class);
        assertNotNull(actionNmtp);
        assertSame("action-project module is standalone module now", NbModuleTypeProvider.STANDALONE,
                actionNmtp.getModuleType());
    }
    
    public void testAddSubModule() throws Exception {
        SuiteSubModulesListModel model = suite1Props.getModulesListModel();
        assertNotNull(model);
        
        Project miscProject = getMiscProject();
        assertNotNull(miscProject);
        model.addModule(miscProject);
        assertEquals("one project should be added", 3, model.getSize());
        
        saveProperties(suite1Props);
        
        // assert miscProject is part of suite1
        SubprojectProvider suite1spp = getSuite1SPP(true);
        assertEquals("one module should be left", 3, suite1spp.getSubprojects().size());
        assertTrue("misc-project has moved to suite1", suite1spp.getSubprojects().contains(miscProject));
        
        
        // assert miscProject is not part of suite2
        SubprojectProvider suite2spp = getSuite2SPP(true);
        assertFalse("misc-project is not part of suite2 anymore",
                suite2spp.getSubprojects().contains(miscProject));
        
        // assert miscProject has correctly set suite provider
        SuiteProvider sp = (SuiteProvider) getMiscProject().getLookup().lookup(SuiteProvider.class);
        assertNotNull(sp);
        assertEquals("misc-project was moved to suite1",
                FileUtil.toFile(getSuite1Project(false).getProjectDirectory()),
                sp.getSuiteDirectory());
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
    
    // XXX fastly copied from ProjectXMLManagerTest!!!
    private FileObject prepareSuiteRepo(FileObject what) throws Exception {
        int srcFolderLen = what.getPath().length();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        // XXX this should be probably be using (TestBase.this.)copyFolder
        for (Enumeration en = what.getFolders(true); en.hasMoreElements(); ) {
            FileObject src = (FileObject) en.nextElement();
            if (src.getName().equals("CVS")) {
                continue;
            }
            FileObject dest = FileUtil.createFolder(workDir, src.getPath().substring(srcFolderLen));
            for (Enumeration en2 = src.getData(false); en2.hasMoreElements(); ) {
                FileObject fo = (FileObject) en2.nextElement();
                FileUtil.copyFile(fo, dest, fo.getName());
            }
        }
        return workDir;
    }
}
