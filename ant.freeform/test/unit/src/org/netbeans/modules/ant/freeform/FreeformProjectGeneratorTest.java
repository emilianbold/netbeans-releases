/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import junit.framework.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// XXX: part of the testSourceFoldersAndSourceViews test is commented out
// becasue implementation of Source interface does not refresh automatically.

/**
 * Tests for FreeformProjectGenerator.
 *
 * @author David Konecny
 */
public class FreeformProjectGeneratorTest extends NbTestCase {

    private File lib1;
    private File lib2;
    private File src;
    private File test;
    
    public FreeformProjectGeneratorTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    private AntProjectHelper createEmptyProject(String projectFolder, String projectName) throws Exception {
        File base = new File(getWorkDir(), projectFolder);
        base.mkdir();
        File antScript = new File(base, "build.xml");
        antScript.createNewFile();
        src = new File(base, "src");
        src.mkdir();
        test = new File(base, "test");
        test.mkdir();
        File libs = new File(base, "libs");
        libs.mkdir();
        lib1 = new File(libs, "some.jar");
        createRealJarFile(lib1);
        lib2 = new File(libs, "some2.jar");
        createRealJarFile(lib2);
        
// XXX: might need to call refresh here??
//        FileObject fo = FileUtil.toFileObject(getWorkDir());
//        fo.refresh();
        
        ArrayList sources = new ArrayList();
        FreeformProjectGenerator.SourceFolder sf = new FreeformProjectGenerator.SourceFolder();
        sf.label = "src";
        sf.type = "java";
        sf.location = src.getAbsolutePath();
        sources.add(sf);
        ArrayList compUnits = new ArrayList();
        FreeformProjectGenerator.JavaCompilationUnit cu = new FreeformProjectGenerator.JavaCompilationUnit();
        cu.classpath = lib1.getAbsolutePath();
        cu.sourceLevel = "1.4";
        cu.packageRoot = src.getAbsolutePath();
        compUnits.add(cu);
        AntProjectHelper helper = FreeformProjectGenerator.createProject(base, projectName, null, new HashMap(), sources, compUnits);
        return helper;
    }
    
    public void testCreateProject() throws Exception {
        clearWorkDir();
        AntProjectHelper helper = createEmptyProject("proj1", "proj-1");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        ProjectInformation pi = (ProjectInformation)p.getLookup().lookup(ProjectInformation.class);
        assertEquals("Project name was not set", "proj-1", pi.getName());
    }
    
    public void testTargetMappings() throws Exception {
        clearWorkDir();
        AntProjectHelper helper = createEmptyProject("proj2", "proj-2");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        ActionProvider ap = (ActionProvider)p.getLookup().lookup(ActionProvider.class);
        assertNotNull("Project does not have ActionProvider", ap);
        assertEquals("Project cannot have any action", 0, ap.getSupportedActions().length);
        
        Map map = FreeformProjectGenerator.getTargetMappings(helper);
        assertNotNull("getTargetMappings() cannot return null", map);
        assertEquals("Project cannot have any action", 0, map.keySet().size());
        
        map = new HashMap();
        map.put("clean", Collections.singletonList("clean-target"));
        map.put("build", Collections.singletonList("build-target"));
        map.put("rebuild", Arrays.asList(new String[]{"clean-target", "build-target"}));
        FreeformProjectGenerator.putTargetMappings(helper, map);
        Map map2 = FreeformProjectGenerator.getTargetMappings(helper);
        assertNotNull("getTargetMappings() cannot return null", map);
        assertEquals("Project must have 3 actions", 3, map.keySet().size());
        assertEquals("Project must have 3 actions", 3, ap.getSupportedActions().length);
        assertTrue("Action clean must be enabled", ap.isActionEnabled("clean", Lookup.EMPTY));
        assertTrue("Action build must be enabled", ap.isActionEnabled("build", Lookup.EMPTY));
        assertTrue("Action rebuild must be enabled", ap.isActionEnabled("rebuild", Lookup.EMPTY));
        boolean ok = false;
        try {
            assertFalse("Action javadoc must be disabled", ap.isActionEnabled("javadoc", Lookup.EMPTY));
        } catch (IllegalArgumentException ex) {
            ok = true;
        }
        assertTrue("Exception must be thrown for non-existing actions", ok);
        ProjectManager.getDefault().saveAllProjects();
    }
    
    public void testSourceFoldersAndSourceViews() throws Exception {
        clearWorkDir();
        AntProjectHelper helper = createEmptyProject("proj3", "proj-3");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        Sources ss = (Sources)p.getLookup().lookup(Sources.class);
        assertNotNull("Project does not have Sources instance in lookup", ss);
        assertEquals("Project must have one java source group", 1, ss.getSourceGroups("java").length);
        assertEquals("Project cannot have csharp source group", 0, ss.getSourceGroups("csharp").length);

        LogicalViewProvider lvp = (LogicalViewProvider)p.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("Project does not have LogicalViewProvider", lvp);
        Node n = lvp.createLogicalView();
        // expected subnodes: #1) src folder and #2) build.xml
        assertEquals("There must be two subnodes in logical view", 2, n.getChildren().getNodesCount());
        
        Listener l = new Listener();
        ss.addChangeListener(l);
        
        List sfs = FreeformProjectGenerator.getSourceFolders(helper);
        assertEquals("There must be one source folder", 1, sfs.size());
        FreeformProjectGenerator.SourceFolder sf = new FreeformProjectGenerator.SourceFolder();
        sf.label = "test";
        sf.type = "java";
        sf.location = test.getAbsolutePath();
        sfs.add(sf);
        FreeformProjectGenerator.putSourceFolders(helper, sfs);
        // XXX: updating not implement in Freeform project:
//        assertEquals("Project must have one java source group", 2, ss.getSourceGroups("java").length);
//        assertEquals("Project cannot have csharp source group", 0, ss.getSourceGroups("csharp").length);
//        assertEquals("Number of fired events does not match", 1, l.count);
//        l.reset();
        
        n = lvp.createLogicalView();
        // expected subnodes: #1) src folder and #2) build.xml and #3) tests
//        assertEquals("There must be three subnodes in logical view", 3, n.getChildren().getNodesCount());
        
        ProjectManager.getDefault().saveAllProjects();
    }
    
    public void testAuxiliaryConfiguration() throws Exception {
        clearWorkDir();
        AntProjectHelper helper = createEmptyProject("proj4", "proj-4");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        AuxiliaryConfiguration au = FreeformProjectGenerator.getAuxiliaryConfiguration(helper);
        assertNotNull("Project does not have AuxiliaryConfiguration", au);
        Element el = au.getConfigurationFragment("java-data", FreeformProjectType.NS_JAVA, true);
        assertNotNull("Project does not have correct aux data", el);
    }

    public void testJavaCompilationUnits() throws Exception {
        clearWorkDir();
        AntProjectHelper helper = createEmptyProject("proj5", "proj-5");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        ClassPathProvider cpp = (ClassPathProvider)p.getLookup().lookup(ClassPathProvider.class);
        assertNotNull("Project does not have ClassPathProvider", cpp);
        ClassPath cp = cpp.findClassPath(FileUtil.toFileObject(src), ClassPath.COMPILE);
        assertEquals("Project must have one classpath root", 1, cp.getRoots().length);
        assertEquals("Classpath root does not match", "jar:"+lib1.toURI().toURL()+"!/", (cp.getRoots()[0]).getURL().toExternalForm());
        cp = cpp.findClassPath(FileUtil.toFileObject(src).getParent(), ClassPath.COMPILE);
        assertEquals("There is no classpath for this file", null, cp);
        
        AuxiliaryConfiguration aux = FreeformProjectGenerator.getAuxiliaryConfiguration(helper);
        List cus = FreeformProjectGenerator.getJavaCompilationUnits(helper, aux);
        assertEquals("There must be one compilation unit", 1, cus.size());
        FreeformProjectGenerator.JavaCompilationUnit cu = new FreeformProjectGenerator.JavaCompilationUnit();
        cu.sourceLevel = "1.4";
        cu.classpath = lib2.getAbsolutePath();
        cu.packageRoot = test.getAbsolutePath();
        cus.add(cu);
        FreeformProjectGenerator.putJavaCompilationUnits(helper, aux, cus);
        cus = FreeformProjectGenerator.getJavaCompilationUnits(helper, aux);
        assertEquals("There must be two compilation units", 2, cus.size());
        cp = cpp.findClassPath(FileUtil.toFileObject(src), ClassPath.COMPILE);
        assertEquals("Project must have one classpath root", 1, cp.getRoots().length);
        assertEquals("Classpath root does not match", "jar:"+lib1.toURI().toURL()+"!/", (cp.getRoots()[0]).getURL().toExternalForm());
        cp = cpp.findClassPath(FileUtil.toFileObject(src).getParent(), ClassPath.COMPILE);
        assertEquals("There is no classpath for this file", null, cp);
        cp = cpp.findClassPath(FileUtil.toFileObject(test), ClassPath.COMPILE);
        assertEquals("Project must have one classpath root", 1, cp.getRoots().length);
        assertEquals("Classpath root does not match", "jar:"+lib2.toURI().toURL()+"!/", (cp.getRoots()[0]).getURL().toExternalForm());
        ProjectManager.getDefault().saveAllProjects();
    }
    
        
    private static class Listener implements ChangeListener {
        int count = 0;
        public void stateChanged(ChangeEvent ev) {
            count++;
        }
        public void reset() {
            count = 0;
        }
    }

    // create real Jar otherwise FileUtil.isArchiveFile returns false for it
    public void createRealJarFile(File f) throws Exception {
        OutputStream os = new FileOutputStream(f);
        try {
            JarOutputStream jos = new JarOutputStream(os);
//            jos.setMethod(ZipEntry.STORED);
            JarEntry entry = new JarEntry("foo.txt");
//            entry.setSize(0L);
//            entry.setTime(System.currentTimeMillis());
//            entry.setCrc(new CRC32().getValue());
            jos.putNextEntry(entry);
            jos.flush();
            jos.close();
        } finally {
            os.close();
        }
    }
    
}
