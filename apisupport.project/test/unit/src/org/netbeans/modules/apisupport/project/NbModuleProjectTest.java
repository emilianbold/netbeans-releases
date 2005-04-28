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
import java.util.Arrays;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
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
    
    protected void setUp() throws Exception {
        super.setUp();
        FileObject dir = nbroot.getFileObject("java/project");
        assertNotNull("have java/project checked out", dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        javaProjectProject = (NbModuleProject)p;
        dir = nbroot.getFileObject("openide/loaders");
        assertNotNull("have openide/loaders checked out", dir);
        p = ProjectManager.getDefault().findProject(dir);
        loadersProject = (NbModuleProject)p;
    }
    
    public void testEvaluator() throws Exception {
        PropertyEvaluator eval = javaProjectProject.evaluator();
        assertEquals("right basedir", file("java/project"),
            javaProjectProject.getHelper().resolveFile(eval.getProperty("basedir")));
        assertEquals("right nb_all", nbrootF,
            javaProjectProject.getHelper().resolveFile(eval.getProperty("nb_all")));
        assertEquals("right code.name.base.dashes", "org-netbeans-modules-java-project", eval.getProperty("code.name.base.dashes"));
        assertEquals("right is.autoload", "true", eval.getProperty("is.autoload"));
        assertEquals("right manifest.mf", "manifest.mf", eval.getProperty("manifest.mf"));
        // Keep the following in synch with java/project/nbproject/project.xml etc.:
        String[] cp = {
            "ide5/modules/org-apache-tools-ant-module.jar",
            "platform5/core/openide.jar",
            "platform5/modules/org-openide-execution.jar",
            "platform5/modules/org-openide-io.jar",
            "platform5/core/openide-loaders.jar",
            "ide5/modules/org-netbeans-modules-java-platform.jar",
            "ide5/modules/org-netbeans-modules-project-ant.jar",
            "ide5/modules/org-netbeans-modules-project-libraries.jar",
            "ide5/modules/org-openidex-util.jar",
            "ide5/modules/org-netbeans-modules-projectapi.jar",
            "ide5/modules/org-netbeans-modules-projectuiapi.jar",
            "platform5/modules/org-netbeans-modules-queries.jar",
            "ide5/modules/org-netbeans-api-java.jar",
        };
        StringBuffer cpS = new StringBuffer();
        for (int i = 0; i < cp.length; i++) {
            if (i > 0) {
                cpS.append(File.pathSeparatorChar);
            }
            cpS.append(file("nbbuild/netbeans/" + cp[i]).getAbsolutePath());
        }
        assertEquals("right module.classpath", cpS.toString(), eval.getProperty("module.classpath"));
        assertEquals("right core.dir", file("nbbuild/netbeans/platform5"),
            javaProjectProject.getHelper().resolveFile(eval.getProperty("core.dir")));
        /* Will not work in branches:
        assertEquals("right apisupport/project.dir", file("nbbuild/netbeans/ide5"),
            javaProjectProject.getHelper().resolveFile(eval.getProperty("apisupport/project.dir")));
         */
        assertEquals("right module JAR", file("nbbuild/netbeans/ide5/modules/org-netbeans-modules-java-project.jar"),
            javaProjectProject.getHelper().resolveFile(eval.evaluate("${netbeans.dest.dir}/${cluster.dir}/${module.jar}")));
        eval = loadersProject.evaluator();
        assertEquals("right module JAR", file("nbbuild/netbeans/platform5/core/openide-loaders.jar"),
            loadersProject.getHelper().resolveFile(eval.evaluate("${netbeans.dest.dir}/${cluster.dir}/${module.jar}")));
    }
    
    /** #56457 */
    public void testExternalSourceRoots() throws Exception {
        FileObject documentFinderJava = nbroot.getFileObject("editor/libsrc/org/netbeans/editor/DocumentFinder.java");
        assertNotNull("have DocumentFinder.java", documentFinderJava);
        FileObject editorLib = nbroot.getFileObject("editor/lib");
        assertNotNull("have editor/lib", editorLib);
        Project editorLibProject = ProjectManager.getDefault().findProject(editorLib);
        assertNotNull("have editor/lib project", editorLibProject);
        Thread.sleep(1000);
        assertEquals("correct owner of DocumentFinder.java", editorLibProject, FileOwnerQuery.getOwner(documentFinderJava));
    }
    
    public void testExternalModules() throws Exception {
        FileObject examples = nbroot.getFileObject("apisupport/project/test/unit/data/example-external-projects");
        FileObject suite1 = examples.getFileObject("suite1");
        FileObject action = suite1.getFileObject("action-project");
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(action);
        PropertyEvaluator eval = actionProject.evaluator();
        String[] cp = {
            "platform5/core/openide.jar",
            "extra/modules/org-netbeans-examples-modules-lib.jar",
        };
        StringBuffer cpS = new StringBuffer();
        for (int i = 0; i < cp.length; i++) {
            if (i > 0) {
                cpS.append(File.pathSeparatorChar);
            }
            cpS.append(file("nbbuild/netbeans/" + cp[i]).getAbsolutePath());
        }
        assertEquals("right module.classpath", cpS.toString(), eval.getProperty("module.classpath"));
        String nbsources = eval.getProperty("netbeans.sources");
        assertNotNull("netbeans.sources defined", nbsources);
        String[] pieces = PropertyUtils.tokenizePath(nbsources);
        File[] piecesF = new File[pieces.length];
        for (int i = 0; i < pieces.length; i++) {
            piecesF[i] = PropertyUtils.resolveFile(FileUtil.toFile(action), pieces[i]);
        }
        assertEquals("correct netbeans.sources",
            Arrays.asList(new File[] {
                nbrootF,
                FileUtil.toFile(examples.getFileObject("suite2")),
            }), Arrays.asList(piecesF));
        FileObject suite3 = examples.getFileObject("suite3");
        FileObject dummy = suite3.getFileObject("dummy-project");
        NbModuleProject dummyProject = (NbModuleProject) ProjectManager.getDefault().findProject(dummy);
        eval = dummyProject.evaluator();
        cp = new String[] {
            "random/modules/random.jar",
            "random/modules/ext/stuff.jar",
        };
        cpS = new StringBuffer();
        for (int i = 0; i < cp.length; i++) {
            if (i > 0) {
                cpS.append(File.pathSeparatorChar);
            }
            cpS.append(file("apisupport/project/test/unit/data/example-external-projects/suite3/nbplatform/" + cp[i]).getAbsolutePath());
        }
        assertEquals("right module.classpath", cpS.toString(), eval.getProperty("module.classpath"));
        // XXX more...
    }
    
}
