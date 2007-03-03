/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import java.io.File;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test functionality of NbModuleProject.
 * @author Jesse Glick
 */
public class NbModuleProjectTest extends TestBase {
    
    public NbModuleProjectTest(String name) {
        super(name);
    }
    
    private NbModuleProject javaProjectProject;
    private NbModuleProject loadersProject;
    private File userPropertiesFile;
    
    protected void setUp() throws Exception {
        super.setUp();
        userPropertiesFile = TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
        FileObject dir = nbCVSRoot().getFileObject("java/project");
        assertNotNull("have java/project checked out", dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        javaProjectProject = (NbModuleProject)p;
        dir = nbCVSRoot().getFileObject("openide/loaders");
        assertNotNull("have openide/loaders checked out", dir);
        p = ProjectManager.getDefault().findProject(dir);
        loadersProject = (NbModuleProject)p;
    }

    /** #56457 */
    public void testExternalSourceRoots() throws Exception {
        FileObject documentFinderJava = nbCVSRoot().getFileObject("editor/libsrc/org/netbeans/editor/DocumentFinder.java");
        assertNotNull("have DocumentFinder.java", documentFinderJava);
        FileObject editorLib = nbCVSRoot().getFileObject("editor/lib");
        assertNotNull("have editor/lib", editorLib);
        Project editorLibProject = ProjectManager.getDefault().findProject(editorLib);
        assertNotNull("have editor/lib project", editorLibProject);
        Thread.sleep(1000); // XXX why?
        assertEquals("correct owner of DocumentFinder.java", editorLibProject, FileOwnerQuery.getOwner(documentFinderJava));
    }

    public void testExternalModules() throws Exception {
        FileObject suite1 = resolveEEP("suite1");
        FileObject action = suite1.getFileObject("action-project");
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(action);
        PropertyEvaluator eval = actionProject.evaluator();
        String nbdestdir = eval.getProperty("netbeans.dest.dir");
        assertNotNull("defined netbeans.dest.dir", nbdestdir);
        assertEquals("right netbeans.dest.dir", file("nbbuild/netbeans"), PropertyUtils.resolveFile(FileUtil.toFile(action), nbdestdir));
        FileObject suite3 = resolveEEP("suite3");
        FileObject dummy = suite3.getFileObject("dummy-project");
        NbModuleProject dummyProject = (NbModuleProject) ProjectManager.getDefault().findProject(dummy);
        eval = dummyProject.evaluator();
        assertEquals("right netbeans.dest.dir", resolveEEPFile("suite3/nbplatform"), PropertyUtils.resolveFile(FileUtil.toFile(dummy), eval.getProperty("netbeans.dest.dir")));
        // XXX more...
    }

    public void testGetType() throws Exception {
        assertEquals(NbModuleProvider.NETBEANS_ORG, Util.getModuleType(javaProjectProject));
        FileObject suite1 = resolveEEP("suite1");
        FileObject action = suite1.getFileObject("action-project");
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(action);
        assertEquals(NbModuleProvider.SUITE_COMPONENT, Util.getModuleType(actionProject));
        FileObject suite3 = resolveEEP("suite3");
        FileObject dummy = suite3.getFileObject("dummy-project");
        NbModuleProject dummyProject = (NbModuleProject) ProjectManager.getDefault().findProject(dummy);
        assertEquals(NbModuleProvider.STANDALONE, Util.getModuleType(dummyProject));
    }

    public void testSupportsJavadoc() throws Exception {
        assertTrue(javaProjectProject.supportsJavadoc());
        FileObject dir = nbCVSRoot().getFileObject("beans");
        assertNotNull("have beans checked out", dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        NbModuleProject beansProject = (NbModuleProject) p;
        assertFalse(beansProject.supportsJavadoc());
    }

    public void testGetNbrootFile() throws Exception {
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(resolveEEP("suite1/action-project"));
        assertEquals(file("xtest/lib/insanelib.jar"), actionProject.getNbrootFile("xtest/lib/insanelib.jar"));
    }

    public void testThatModuleWithOverriddenSrcDirPropertyDoesNotThrowNPE() throws Exception {
        FileObject prjFO = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "module1");
        FileObject srcFO = prjFO.getFileObject("src");
        FileUtil.moveFile(srcFO, prjFO, "src2");
        ProjectManager.getDefault().findProject(prjFO);
    }
    
    public void testGenericSourceGroupForExternalUnitTests() throws Exception {
        FileObject prjFO = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "module1");
        FileUtil.createData(prjFO, "../myunitsrc/a/b/c/Dummy.java");
        FileObject propsFO = FileUtil.createData(prjFO, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties ep = Util.loadProperties(propsFO);
        ep.setProperty("test.unit.src.dir", "../myunitsrc");
        Util.storeProperties(propsFO, ep);
        Project module = ProjectManager.getDefault().findProject(prjFO);
        Sources sources = ProjectUtils.getSources(module);
        SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("two generic source group", 2, sourceGroups.length); // prjFolder and unitFolder
    }

}
