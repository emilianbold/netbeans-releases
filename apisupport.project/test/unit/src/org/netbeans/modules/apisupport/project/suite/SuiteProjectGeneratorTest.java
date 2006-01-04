/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
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
    
    public void testCreateSuiteProject() throws Exception {
        File targetPrjDir = new File(getWorkDir(), "testSuite");
        SuiteProjectGenerator.createSuiteProject(targetPrjDir, NbPlatform.PLATFORM_ID_DEFAULT);
        FileObject fo = FileUtil.toFileObject(targetPrjDir);
        // Make sure generated files are created too - simulate project opening.
        Project p = ProjectManager.getDefault().findProject(fo);
        assertNotNull("have a project in " + targetPrjDir, p);
        SuiteProjectTest.openSuite(p);
        // check generated module
        for (int i=0; i < SUITE_CREATED_FILES.length; i++) {
            assertNotNull(SUITE_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(SUITE_CREATED_FILES[i]));
        }
    }
    
    public void testSuiteProjectWithDotInName() throws Exception {
        File targetPrjDir = new File(getWorkDir(), "testSuite 1.0");
        SuiteProjectGenerator.createSuiteProject(targetPrjDir, NbPlatform.PLATFORM_ID_DEFAULT);
        FileObject fo = FileUtil.toFileObject(targetPrjDir);
        // Make sure generated files are created too - simulate project opening.
        Project p = ProjectManager.getDefault().findProject(fo);
        assertEquals("#66080: right display name", "testSuite 1.0", ProjectUtils.getInformation(p).getDisplayName());
    }
    
}
