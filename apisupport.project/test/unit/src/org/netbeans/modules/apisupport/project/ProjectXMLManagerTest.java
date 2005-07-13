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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Tests ProjectXMLManager class.
 *
 * @author Martin Krauskopf
 */
public class ProjectXMLManagerTest extends TestBase {
    
    private FileObject suiteRepoFO;
    
    private ProjectXMLManager actionPXM;
    private ProjectXMLManager miscPXM;
    private NbModuleProject actionProject;
    private NbModuleProject miscProject;
    
    public ProjectXMLManagerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        suiteRepoFO = prepareSuiteRepo(extexamples);
        FileObject suite1FO = suiteRepoFO.getFileObject("suite1");
        FileObject suite2FO = suiteRepoFO.getFileObject("suite2");
        FileObject actionFO = suite1FO.getFileObject("action-project");
        FileObject miscFO = suite2FO.getFileObject("misc-project");
        this.actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(actionFO);
        this.miscProject = (NbModuleProject) ProjectManager.getDefault().findProject(miscFO);
        this.actionPXM = new ProjectXMLManager(actionProject.getHelper());
        this.miscPXM = getNewMiscPXM();
    }
    
    private ProjectXMLManager getNewMiscPXM() {
        return new ProjectXMLManager(miscProject.getHelper());
    }
    
    private ProjectXMLManager getNewActionPXM() {
        return new ProjectXMLManager(actionProject.getHelper());
    }
    
    private ProjectXMLManager createXercesPXM() throws IOException {
        NbModuleProject xercesPrj = (NbModuleProject) ProjectManager.getDefault().
                findProject(nbroot.getFileObject("libs/xerces"));
        return new ProjectXMLManager(xercesPrj.getHelper());
    }
    
    public void testGetCodeNameBase() throws Exception {
        assertEquals("action-project cnb", "org.netbeans.examples.modules.action", actionPXM.getCodeNameBase());
        assertEquals("misc-project cnb", "org.netbeans.examples.modules.misc", miscPXM.getCodeNameBase());
    }
    
    public void testGetDirectDependencies() throws Exception {
        Set deps = actionPXM.getDirectDependencies(null);
        assertEquals("number of dependencies", 2, deps.size());
        
        Set assumed = new HashSet();
        assumed.add("org.netbeans.examples.modules.lib");
        assumed.add("org.openide.dialogs");
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            if (md.getModuleEntry().getCodeNameBase().equals("org.openide.dialogs")) {
                assertNotNull("module entry", md.getModuleEntry());
                assertEquals("release version", null, md.getReleaseVersion());
                assertEquals("specification version", "6.2", md.getSpecificationVersion());
            }
            if (md.getModuleEntry().getCodeNameBase().equals("org.netbeans.examples.modules.lib")) {
                assertNotNull("module entry", md.getModuleEntry());
                assertNull("release version", md.getReleaseVersion());
                assertNull("specification version", md.getSpecificationVersion());
            }
            assertTrue("unknown dependency", assumed.remove(md.getModuleEntry().getCodeNameBase()));
        }
        assertTrue("following dependencies were found: " + assumed, assumed.isEmpty());
    }
    
    public void testRemoveDependency() throws Exception {
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                actionPXM.removeDependency("org.openide.dialogs");
                return Boolean.TRUE;
            }
        });
        assertTrue("removing dependency", result.booleanValue());
        ProjectManager.getDefault().saveProject(actionProject);
        
        final Set newDeps = actionPXM.getDirectDependencies(null);
        assertEquals("number of dependencies", 1, newDeps.size());
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
        final Set deps = actionPXM.getDirectDependencies(null);
        
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                boolean tested = false;
                for (Iterator it = deps.iterator(); it.hasNext(); ) {
                    ModuleDependency origDep = (ModuleDependency) it.next();
                    if ("org.openide.dialogs".equals(origDep.getModuleEntry().getCodeNameBase())) {
                        tested = true;
                        ModuleDependency newDep = new ModuleDependency(
                                origDep.getModuleEntry(),
                                "2",
                                origDep.getSpecificationVersion(),
                                origDep.hasCompileDependency(),
                                origDep.hasImplementationDepedendency());
                        actionPXM.editDependency(origDep, newDep);
                    }
                }
                assertTrue("org.openide.dialogs dependency tested", tested);
                return Boolean.TRUE;
            }
        });
        assertTrue("editing dependencies", result.booleanValue());
        ProjectManager.getDefault().saveProject(actionProject);
        // XXX this refresh shouldn't be needed
        this.actionPXM = new ProjectXMLManager(actionProject.getHelper());
        
        final Set newDeps = actionPXM.getDirectDependencies(null);
        boolean tested = false;
        for (Iterator it = newDeps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            if ("org.openide.dialogs".equals(md.getModuleEntry().getCodeNameBase())) {
                tested = true;
                assertEquals("edited release version", "2", md.getReleaseVersion());
                assertEquals("unedited specification version", "6.2", md.getSpecificationVersion());
                break;
            }
        }
        assertTrue("org.openide.dialogs dependency tested", tested);
    }
    
    public void testAddDependencies() throws Exception {
        final Set newDeps = new HashSet();
        ModuleEntry me = actionProject.getModuleList().getEntry(
                "org.netbeans.modules.java.project");
        assertNotNull("java/project must be built", me);
        String javaProjectRV = me.getReleaseVersion();
        String javaProjectSV = me.getSpecificationVersion();
        newDeps.add(new ModuleDependency(me));
        me = actionProject.getModuleList().getEntry("org.netbeans.modules.java.j2seplatform");
        assertNotNull("java/j2seplatform must be built", me);
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
        
        Set deps = actionPXM.getDirectDependencies(null);
        
        Set assumed = new HashSet();
        assumed.add("org.netbeans.examples.modules.lib");
        assumed.add("org.openide.dialogs");
        assumed.add("org.netbeans.modules.java.project");
        assumed.add("org.netbeans.modules.java.j2seplatform");
        
        assertEquals("number of dependencies", deps.size(), assumed.size());
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            assertTrue("unknown dependency",
                    assumed.remove(md.getModuleEntry().getCodeNameBase()));
            if ("org.netbeans.modules.java.project".equals(md.getModuleEntry().getCodeNameBase())) {
                assertEquals("initial release version", javaProjectRV, md.getReleaseVersion());
                assertEquals("initial specification version", javaProjectSV, md.getSpecificationVersion());
            }
            if ("org.netbeans.modules.java.j2seplatform".equals(md.getModuleEntry().getCodeNameBase())) {
                assertEquals("edited release version", "1", md.getReleaseVersion());
                assertFalse("has compile depedendency", md.hasCompileDependency());
                assertTrue("has implementation depedendency", md.hasImplementationDepedendency());
            }
        }
        assertTrue("following dependencies were found: " + assumed, assumed.isEmpty());
    }
    
    public void testFindPublicPackages() throws Exception {
        final File projectXML = new File(FileUtil.toFile(extexamples),
                "/suite2/misc-project/nbproject/project.xml");
        assert projectXML.exists();
        Element confData = (Element) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element data = null;
                try {
                    Document doc = XMLUtil.parse(new InputSource(projectXML.toURI().toString()),
                            false, true, null, null);
                    Element project = doc.getDocumentElement();
                    Element config = Util.findElement(project, "configuration", null); // NOI18N
                    data = Util.findElement(config, "data", NbModuleProjectType.NAMESPACE_SHARED);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                } catch (SAXException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                return data;
            }
        });
        assertNotNull("finding configuration data element", confData);
        ManifestManager.PackageExport[] pp = ProjectXMLManager.findPublicPackages(confData);
        assertEquals("number of public packages", 1, pp.length);
        assertEquals("public package", "org.netbeans.examples.modules.misc", pp[0].getPackage());
    }
    
    public void testReplaceDependencies() throws Exception {
        final Set deps = actionPXM.getDirectDependencies(null);
        assertEquals("number of dependencies", 2, deps.size());
        ModuleDependency newOO = null;
        ModuleDependency oldOO = null;
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            if ("org.openide.dialogs".equals(md.getModuleEntry().getCodeNameBase())) {
                oldOO = md;
                ModuleEntry me = md.getModuleEntry();
                newOO = new ModuleDependency(me,
                        "", // will be check if it is not written
                        me.getSpecificationVersion(),
                        md.hasCompileDependency(),
                        md.hasImplementationDepedendency());
                it.remove();
                break;
            }
        }
        deps.add(newOO);
        
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                actionPXM.replaceDependencies(deps);
                return Boolean.TRUE;
            }
        });
        
        final Set newDeps = getNewActionPXM().getDirectDependencies(null);
        for (Iterator it = newDeps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            if ("org.openide.dialogs".equals(md.getModuleEntry().getCodeNameBase())) {
                assertNull("empty(null) release version", md.getReleaseVersion());
                assertEquals("unedited specification version",
                        oldOO.getSpecificationVersion(),
                        md.getSpecificationVersion());
                break;
            }
        }
        
        assertTrue("replace dependencies", result.booleanValue());
        ProjectManager.getDefault().saveProject(actionProject);
    }
    
    public void testGetPublicPackages() throws Exception {
        assertEquals("number of public packages", 1, miscPXM.getPublicPackages().length);
        assertEquals("package name", "org.netbeans.examples.modules.misc", miscPXM.getPublicPackages()[0].getPackage());
        assertFalse("not recursive", miscPXM.getPublicPackages()[0].isRecursive());
        
        ProjectXMLManager xercesPXM = createXercesPXM();
        assertEquals("number of binary origins", 1, xercesPXM.getPublicPackages().length);
        assertEquals("package name", "org", xercesPXM.getPublicPackages()[0].getPackage());
        assertTrue("recursive", xercesPXM.getPublicPackages()[0].isRecursive());
    }
    
    public void testReplacePublicPackages() throws Exception {
        ManifestManager.PackageExport[] publicPackages = miscPXM.getPublicPackages();
        assertEquals("number of public packages", 1, publicPackages.length);
        final String[] newPP = new String[] { publicPackages[0].getPackage(), "org.netbeans.examples.modules" };
        
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                miscPXM.replacePublicPackages(newPP);
                return Boolean.TRUE;
            }
        });
        assertTrue("replace public packages", result.booleanValue());
        ProjectManager.getDefault().saveProject(miscProject);
        ManifestManager.PackageExport[] newPublicPackages = getNewMiscPXM().getPublicPackages();
        assertEquals("number of new public packages", 2, newPublicPackages.length);
//        assertEquals("added package", "org.netbeans.examples.modules", newPublicPackages[0].getPackage());
//        assertEquals("added package", "org.netbeans.examples.modules.misc", newPublicPackages[1].getPackage());
        assertNull("there must not be friend", getNewMiscPXM().getFriends());
    }
    
    public void testReplaceFriendPackages() throws Exception {
        assertNull("none friend packages", miscPXM.getFriends());
        final String[] friends = new String[] { "org.exampleorg.somefriend" };
        
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                ManifestManager.PackageExport pkgs[] = miscPXM.getPublicPackages();
                String[] packages = new String[pkgs.length];
                for (int i = 0; i < pkgs.length; i++) {
                    packages[i] = pkgs[i].getPackage();
                }
                miscPXM.replaceFriendPackages(friends, packages);
                return Boolean.TRUE;
            }
        });
        assertTrue("replace friend packages", result.booleanValue());
        ProjectManager.getDefault().saveProject(miscProject);
        String[] newFriendPackages = getNewMiscPXM().getFriends();
        assertEquals("number of new friend", 1, newFriendPackages.length);
        assertEquals("stored friend", "org.exampleorg.somefriend", newFriendPackages[0]);
        assertEquals("public packages", 1, getNewMiscPXM().getPublicPackages().length);
    }
    
    public void testGetBinaryOrigins() throws Exception {
        ProjectXMLManager xercesPXM = createXercesPXM();
        assertEquals("number of binary origins", 2, xercesPXM.getBinaryOrigins().length);
    }
    
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
