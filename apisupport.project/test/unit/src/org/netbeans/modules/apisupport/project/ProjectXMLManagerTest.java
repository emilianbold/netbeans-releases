/*/*
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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;

/**
 * Tests ProjectXMLManager class.
 *
 * @author Martin Krauskopf
 */
public class ProjectXMLManagerTest extends TestBase {
    
    private FileObject suiteRepoFO;
    
    private ProjectXMLManager actionPXM;
    private NbModuleProject actionProject;
    
    public ProjectXMLManagerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        suiteRepoFO = prepareSuiteRepo(extexamples);
        FileObject suite1FO = suiteRepoFO.getFileObject("suite1");
        FileObject actionFO = suite1FO.getFileObject("action-project");
        this.actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(actionFO);
        this.actionPXM = new ProjectXMLManager(actionProject.getHelper(), actionProject);
    }
    
    public void testGetDirectDependencies() throws Exception {
        Set deps = actionPXM.getDirectDependencies();
        assertEquals("number of dependencies", new Integer(deps.size()), new Integer(2));
        
        Set assumed = new HashSet();
        assumed.add("org.netbeans.examples.modules.lib");
        assumed.add("org.openide");
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            if (md.getModuleEntry().getCodeNameBase().equals("org.openide")) {
                assertEquals("release version", "1", md.getReleaseVersion());
                assertEquals("specification version", "5.9", md.getSpecificationVersion());
            }
            assertTrue("unknown dependency", assumed.remove(md.getModuleEntry().getCodeNameBase()));
        }
        assertTrue("following dependencies were found: " + assumed, assumed.isEmpty());
    }
    
    public void testRemoveDependency() throws Exception {
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                actionPXM.removeDependency("org.openide");
                return Boolean.TRUE;
            }
        });
        assertTrue("removing dependency", result.booleanValue());
        ProjectManager.getDefault().saveProject(actionProject);
        
        final Set newDeps = actionPXM.getDirectDependencies();
        assertEquals("number of dependencies", new Integer(1), new Integer(newDeps.size()));
        Set assumed = new HashSet();
        assumed.add("org.netbeans.examples.modules.lib");
        for (Iterator it = newDeps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            assertTrue("unknown dependency", assumed.remove(md.getModuleEntry().getCodeNameBase()));
        }
        assertTrue("following dependencies were found: " + assumed, assumed.isEmpty());
    }
    
//    /** TODO */
//    public void testRemoveDependencies() {
//    }
    
    public void testEditDependency() throws Exception {
        final Set deps = actionPXM.getDirectDependencies();
        
        ModuleDependency origDep;
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                for (Iterator it = deps.iterator(); it.hasNext(); ) {
                    ModuleDependency origDep = (ModuleDependency) it.next();
                    if ("org.openide".equals(origDep.getModuleEntry().getCodeNameBase())) {
                        ModuleDependency newDep = new ModuleDependency(
                                origDep.getModuleEntry(),
                                "2",
                                origDep.getSpecificationVersion(),
                                origDep.hasCompileDependency(),
                                origDep.hasImplementationDepedendency());
                        actionPXM.editDependency(origDep, newDep);
                    }
                }
                return Boolean.TRUE;
            }
        });
        assertTrue("editing dependencies", result.booleanValue());
        ProjectManager.getDefault().saveProject(actionProject);
        // XXX this refresh shouldn't be needed
        this.actionPXM = new ProjectXMLManager(actionProject.getHelper(), actionProject);
        
        final Set newDeps = actionPXM.getDirectDependencies();
        for (Iterator it = newDeps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            if ("org.openide".equals(md.getModuleEntry().getCodeNameBase())) {
                assertEquals("edited release version", "2", md.getReleaseVersion());
                assertEquals("unedited specification version", "5.9", md.getSpecificationVersion());
                break;
            }
        }
    }
    
    public void testAddDependencies() throws Exception {
        final Set newDeps = new HashSet();
        ModuleList.Entry me =
                actionProject.getModuleList().getEntry("org.netbeans.modules.java.project");
        newDeps.add(new ModuleDependency(me));
        me = actionProject.getModuleList().getEntry("org.netbeans.modules.java.j2seplatform");
        newDeps.add(new ModuleDependency(me, "1", null, false, true));
        
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                actionPXM.addDependencies(newDeps);
                return Boolean.TRUE;
            }
        });
        assertTrue("adding dependencies", result.booleanValue());
        ProjectManager.getDefault().saveProject(actionProject);
        
        Set deps = actionPXM.getDirectDependencies();
        
        Set assumed = new HashSet();
        assumed.add("org.netbeans.examples.modules.lib");
        assumed.add("org.openide");
        assumed.add("org.netbeans.modules.java.project");
        assumed.add("org.netbeans.modules.java.j2seplatform");
        
        assertEquals("number of dependencies",
                new Integer(deps.size()), new Integer(assumed.size()));
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            assertTrue("unknown dependency",
                    assumed.remove(md.getModuleEntry().getCodeNameBase()));
            if ("org.netbeans.modules.java.j2seplatform".equals(md.getModuleEntry().getCodeNameBase())) {
                assertEquals("edited release version", "1", md.getReleaseVersion());
                assertFalse("has compile depedendency", md.hasCompileDependency());
                assertTrue("has implementation depedendency", md.hasImplementationDepedendency());
            }
        }
        assertTrue("following dependencies were found: " + assumed, assumed.isEmpty());
    }
    
    public void testFindPublicPackages() throws Exception {
        File projectXML = new File(FileUtil.toFile(extexamples),
                "/suite2/misc-project/nbproject/project.xml");
        assert projectXML.exists();
        ManifestManager.PackageExport[] pp = ProjectXMLManager.findPublicPackages(projectXML);
        assertEquals("number of public packages", new Integer(pp.length), new Integer(1));
        assertEquals("public package", "org.netbeans.examples.modules.misc", pp[0].getPackage());
    }
    
    private FileObject prepareSuiteRepo(FileObject what) throws Exception {
        int srcFolderLen = what.getPath().length();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
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
