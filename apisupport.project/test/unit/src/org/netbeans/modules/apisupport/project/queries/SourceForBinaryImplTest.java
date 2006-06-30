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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.jar.Manifest;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test functionality of SourceForBinaryImpl.
 * @author Jesse Glick
 */
public class SourceForBinaryImplTest extends TestBase {
    
    public SourceForBinaryImplTest(String name) {
        super(name);
    }
    
    public void testFindSourceRootForCompiledClasses() throws Exception {
        doTestFindSourceRootForCompiledClasses("java/project/src", "java/project/build/classes");
        doTestFindSourceRootForCompiledClasses("java/project/test/unit/src", "java/project/build/test/unit/classes");
        doTestFindSourceRootForCompiledClasses("ant/freeform/src", "ant/freeform/build/classes");
        doTestFindSourceRootForCompiledClasses("ant/freeform/test/unit/src", "ant/freeform/build/test/unit/classes");
    }
    
    public void testExtraCompilationUnits() throws Exception {
        doTestFindSourceRootForCompiledClasses("ant/src-bridge", "ant/build/bridge-classes");
        // Have to load at least one module to get the scan going.
        ClassPath.getClassPath(FileUtil.toFileObject(file("beans/src")), ClassPath.COMPILE);
        check("ant/src-bridge", TestBase.CLUSTER_IDE + "/ant/nblib/bridge.jar");
    }
    
    public void testFindSourceRootForModuleJar() throws Exception {
        ClassPath.getClassPath(FileUtil.toFileObject(file("ant/src")), ClassPath.COMPILE);
        check("java/project/src", TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-java-project.jar");
        check("openide/loaders/src", TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar");
        check("core/bootstrap/src", TestBase.CLUSTER_PLATFORM + "/lib/boot.jar");
        check("diff/src", TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-diff.jar");
        check("editor/libsrc", TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-editor-lib.jar");
        check("xtest/nbjunit/src", "testtools/modules/org-netbeans-modules-nbjunit.jar");
    }
    
    public void testExternalModules() throws Exception {
        ClassPath.getClassPath(FileUtil.toFileObject(file(EEP + "/suite1/action-project/src")), ClassPath.COMPILE);
        check(EEP + "/suite1/action-project/src", file(EEP + "/suite1/build/cluster/modules/org-netbeans-examples-modules-action.jar"));
        ClassPath.getClassPath(FileUtil.toFileObject(file(EEP + "/suite3/dummy-project/src")), ClassPath.COMPILE);
        check(EEP + "/suite3/dummy-project/src",
              file(EEP + "/suite3/dummy-project/build/cluster/modules/org-netbeans-examples-modules-dummy.jar"));
    }
    
    public void testCompletionWorks_69735() throws Exception {
        SuiteProject suite = generateSuite("suite");
        NbModuleProject project = TestBase.generateSuiteComponent(suite, "module");
        File library = new File(getWorkDir(), "test-library-0.1_01.jar");
        createJar(library, Collections.EMPTY_MAP, new Manifest());
        FileObject libraryFO = FileUtil.toFileObject(library);
        FileObject yyJar = FileUtil.copyFile(libraryFO, FileUtil.toFileObject(getWorkDir()), "yy");
        
        // library wrapper
        File suiteDir = FileUtil.toFile(suite.getProjectDirectory());
        File wrapperDirF = new File(new File(getWorkDir(), "suite"), "wrapper");
        NbModuleProjectGenerator.createSuiteLibraryModule(
                wrapperDirF,
                "yy", // 69735 - the same name as jar
                "Testing Wrapper (yy)", // display name
                "org/example/wrapper/resources/Bundle.properties",
                suiteDir, // suite directory
                null,
                new File[] { FileUtil.toFile(yyJar)} );
        
        Util.addDependency(project, "yy");
        ProjectManager.getDefault().saveProject(project);
        
        URL wrappedJar = Util.urlForJar(new File(wrapperDirF, "release/modules/ext/yy.jar"));
        assertEquals("no sources for wrapper", 0, SourceForBinaryQuery.findSourceRoots(wrappedJar).getRoots().length);
    }
    
    private void check(String srcS, File jarF) throws Exception {
        File srcF = file(srcS);
        FileObject src = FileUtil.toFileObject(srcF);
        assertNotNull("have " + srcF, src);
        URL u = FileUtil.getArchiveRoot(jarF.toURI().toURL());
        assertEquals("right results for " + u,
            Collections.singletonList(src),
            Arrays.asList(SourceForBinaryQuery.findSourceRoots(u).getRoots()));
    }
    
    private void check(String srcS, String jarS) throws Exception {
        check(srcS, file("nbbuild/netbeans/" + jarS));
    }
    
    private void doTestFindSourceRootForCompiledClasses(String srcPath, String classesPath) throws Exception {
        File classesF = file(classesPath);
        File srcF = file(srcPath);
        FileObject src = FileUtil.toFileObject(srcF);
        assertNotNull("have " + srcF, src);
        URL u = Util.urlForDir(classesF);
        assertEquals("right source root for " + u,
            Collections.singletonList(src),
            Arrays.asList(SourceForBinaryQuery.findSourceRoots(u).getRoots()));
    }
    
}
