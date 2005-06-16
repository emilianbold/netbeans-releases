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

package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * SuiteProjectGenerator tests.
 *
 * @author Martin Krauskopf, Jesse Glick
 */
public class SuiteProjectGeneratorTest extends TestBase {
    // XXX also should test content of created files (XMLs, properties)
    
    public SuiteProjectGeneratorTest(String testName) {
        super(testName);
    }
    
    private static final String[] SUITE_CREATED_FILES = {
        "build.xml",
        "nbproject/project.xml",
        "nbproject/build-impl.xml",
        "nbproject/platform.properties",
        "nbproject/project.properties",
    };
    
    public void testCreateSuiteModule() throws Exception {
        File targetPrjDir = new File(getWorkDir(), "testSuite");
        SuiteProjectGenerator.createSuiteModule(targetPrjDir, "default");
        FileObject fo = FileUtil.toFileObject(targetPrjDir);
        // Make sure generated files are created too - simulate project opening.
        Project p = ProjectManager.getDefault().findProject(fo);
        assertNotNull("have a project in " + targetPrjDir, p);
        SuiteProject.OpenedHook hook = (SuiteProject.OpenedHook) p.getLookup().lookup(SuiteProject.OpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        hook.projectOpened(); // protected but can use package-private access
        // check generated module
        for (int i=0; i < SUITE_CREATED_FILES.length; i++) {
            assertNotNull(SUITE_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(SUITE_CREATED_FILES[i]));
        }
    }
}
