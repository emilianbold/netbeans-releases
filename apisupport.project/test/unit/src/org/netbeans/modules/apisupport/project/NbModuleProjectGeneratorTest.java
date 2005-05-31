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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * NbModuleProjectGenerator tests.
 *
 * @author Martin Krauskopf, Jesse Glick
 */
public class NbModuleProjectGeneratorTest extends TestBase {
    // TODO test suite module and also NetBeans CVS tree modules
    // XXX also should test content of created files (XMLs, properties)
    
    public NbModuleProjectGeneratorTest(String testName) {
        super(testName);
    }
    
    private static final String[] BASIC_CREATED_FILES = {
        "build.xml",
        "manifest.mf",
        "nbproject/project.xml",
        "nbproject/build-impl.xml",
        "src/org/example/testModule/resources/Bundle.properties",
        "src/org/example/testModule/resources/layer.xml",
        "test/unit/src",
    };
    
    private static final String[] STANDALONE_CREATED_FILES = {
        "nbproject/platform.properties",
    };
    
    private static final String[] SUITE_COMP_REL_CREATED_FILES = {
        "nbproject/suite.properties",
    };
    
    private static final String[] SUITE_COMP_ABS_CREATED_FILES = {
        "nbproject/private/suite-private.properties",
    };
    
    public void testCreateStandAloneModule() throws Exception {
        File targetPrjDir = new File(getWorkDir(), "testModule");
        NbModuleProjectGenerator.createStandAloneModule(
                targetPrjDir,
                "org.example.testModule", // cnb
                "Testing Module", // display name
                "org/example/testModule/resources/Bundle.properties",
                "org/example/testModule/resources/layer.xml",
                "default"); // platform id
        FileObject fo = FileUtil.toFileObject(targetPrjDir);
        // Make sure generated files are created too - simulate project opening.
        Project p = ProjectManager.getDefault().findProject(fo);
        assertNotNull("have a project in " + targetPrjDir, p);
        NbModuleProject.OpenedHook hook = (NbModuleProject.OpenedHook) p.getLookup().lookup(NbModuleProject.OpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        hook.projectOpened(); // protected but can use package-private access
        // check generated module
        for (int i=0; i < BASIC_CREATED_FILES.length; i++) {
            assertNotNull(BASIC_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(BASIC_CREATED_FILES[i]));
        }
        for (int i=0; i < STANDALONE_CREATED_FILES.length; i++) {
            assertNotNull(STANDALONE_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(STANDALONE_CREATED_FILES[i]));
        }
    }
    
    public void testCreateSuiteComponentModule() throws Exception {
        // create suite for the module being tested
        File suiteDir = new File(getWorkDir(), "testSuite");
        SuiteProjectGenerator.createSuiteModule(suiteDir, "default");
        
        // create "relative" module in suite
        File targetPrjDir = new File(suiteDir, "testModuleRel");
        NbModuleProjectGenerator.createSuiteComponentModule(
                targetPrjDir,
                "org.example.testModule", // cnb
                "Testing Module", // display name
                "org/example/testModule/resources/Bundle.properties",
                "org/example/testModule/resources/layer.xml",
                suiteDir); // platform id
        FileObject fo = FileUtil.toFileObject(targetPrjDir);
        // Make sure generated files are created too - simulate project opening.
        Project p = ProjectManager.getDefault().findProject(fo);
        assertNotNull("have a project in " + targetPrjDir, p);
        NbModuleProject.OpenedHook hook = (NbModuleProject.OpenedHook) p.getLookup().lookup(NbModuleProject.OpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        hook.projectOpened(); // protected but can use package-private access
        // check generated module
        for (int i=0; i < BASIC_CREATED_FILES.length; i++) {
            assertNotNull(BASIC_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(BASIC_CREATED_FILES[i]));
        }
        for (int i=0; i < SUITE_COMP_REL_CREATED_FILES.length; i++) {
            assertNotNull(SUITE_COMP_REL_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(SUITE_COMP_REL_CREATED_FILES[i]));
        }
        
        // create "absolute" module in suite
        targetPrjDir = new File(getWorkDir(), "testModuleAbs");
        NbModuleProjectGenerator.createSuiteComponentModule(
                targetPrjDir,
                "org.example.testModule", // cnb
                "Testing Module", // display name
                "org/example/testModule/resources/Bundle.properties",
                "org/example/testModule/resources/layer.xml",
                suiteDir); // platform id
        fo = FileUtil.toFileObject(targetPrjDir);
        // Make sure generated files are created too - simulate project opening.
        p = ProjectManager.getDefault().findProject(fo);
        assertNotNull("have a project in " + targetPrjDir, p);
        hook = (NbModuleProject.OpenedHook) p.getLookup().lookup(NbModuleProject.OpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        hook.projectOpened(); // protected but can use package-private access
        // check generated module
        for (int i=0; i < BASIC_CREATED_FILES.length; i++) {
            assertNotNull(BASIC_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(BASIC_CREATED_FILES[i]));
        }
        for (int i=0; i < SUITE_COMP_ABS_CREATED_FILES.length; i++) {
            assertNotNull(SUITE_COMP_ABS_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(SUITE_COMP_ABS_CREATED_FILES[i]));
        }
    }
    
}
