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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006s Sun
 * Microsystems, Inc. All Rights Reserved.
 */



package projects.apitest;

import java.io.File;
import junit.framework.*;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import projects.apitest.Utilities;

public class CreateProjectTest extends JellyTestCase {
    private String projName1 = "TestAppAPI_1"; // NOI18N
    private String projName2 = "TestAppAPI_2"; // NOI18N
    
    public CreateProjectTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(CreateProjectTest.class);
        return suite;
    }
    
    public void setUp() {
        System.out.println("########  " + getName() + "  #######"); // NOI18N
    }
    
    public void testCreateAndOpenProject_API_1() throws Exception {
        String mainClass = "MyMain" + projName1; // NOI18N
        File projectDir = new File(getWorkDir(), projName1);
        projectDir.mkdir();
        AntProjectHelper project = org.netbeans.modules.java.j2seproject.J2SEProjectGenerator.createProject(projectDir, projName1, mainClass, null);
        Utilities.waitScanFinished();
        assertNotNull(Utilities.openProject(projectDir));
    }
    
    public void testCloseProject_API_1() throws Exception {
        assertTrue(Utilities.closeProject(projName1));
    }
    
   
     public void testReopenAndCloseProject_API_1() throws Exception {
        String mainClass = "MyMain" + projName1; // NOI18N
        File projectDir = new File(getWorkDir(), projName1);
        projectDir.mkdir();
        AntProjectHelper project = org.netbeans.modules.java.j2seproject.J2SEProjectGenerator.createProject(projectDir, projName1, mainClass, null);
        Utilities.waitScanFinished();
        Utilities.openProject(projectDir);
        assertNotNull(Utilities.closeProject(projName1));
    }
     
     public void testReopenAndDeleteProjectFolder_API_1() throws Exception {
        String mainClass = "MyMain" + projName1; // NOI18N
        File projectDir = new File(getWorkDir(), projName1);
        projectDir.mkdir();
        AntProjectHelper project = org.netbeans.modules.java.j2seproject.J2SEProjectGenerator.createProject(projectDir, projName1, mainClass, null);
        Utilities.waitScanFinished();
        Utilities.openProject(projectDir);
        assertTrue(Utilities.deleteProjectFolder(project.getProjectDirectory().getPath()));
    }
 
     
    public void testCreateAndOpenProject_API_2() throws Exception {
        File projectDir = new File(getWorkDir(), projName2);
        projectDir.mkdir();
        
        File[] sourceFolders = new File[2];
        File src1 = new File(projectDir, "src1");
        src1.mkdirs();
        File src2 = new File(projectDir, "src2");
        src2.mkdirs();
        sourceFolders[0] = src1;
        sourceFolders[1] = src2;
        
        File[] testFolders = new File[2];
        File test1 = new File(projectDir, "test1");
        test1.mkdirs();
        File test2 = new File(projectDir, "test2");
        test2.mkdirs();
        testFolders[0] = test1;
        testFolders[1] = test2;
        
        AntProjectHelper project = org.netbeans.modules.java.j2seproject.J2SEProjectGenerator.createProject(projectDir, projName2, sourceFolders, testFolders, null);
        Utilities.waitScanFinished();
        assertNotNull(Utilities.openProject(projectDir));
    }
    
    public void testCloseProject_API_2() throws Exception {
        assertTrue(Utilities.closeProject(projName2));
    }

    public void testReopenAndCloseProject_API_2() throws Exception {
            File projectDir = new File(getWorkDir(), projName2);
        projectDir.mkdir();
        
        File[] sourceFolders = new File[2];
        File src1 = new File(projectDir, "src1");
        src1.mkdirs();
        File src2 = new File(projectDir, "src2");
        src2.mkdirs();
        sourceFolders[0] = src1;
        sourceFolders[1] = src2;
        
        File[] testFolders = new File[2];
        File test1 = new File(projectDir, "test1");
        test1.mkdirs();
        File test2 = new File(projectDir, "test2");
        test2.mkdirs();
        testFolders[0] = test1;
        testFolders[1] = test2;
        
        AntProjectHelper project = org.netbeans.modules.java.j2seproject.J2SEProjectGenerator.createProject(projectDir, projName2, sourceFolders, testFolders, null);
        Utilities.waitScanFinished();
        Utilities.openProject(projectDir);
        assertTrue(Utilities.closeProject(projName2));

    }
    public void testReopenAndDeleteProjectFolder_API_2() throws Exception {
        File projectDir = new File(getWorkDir(), projName2);
        projectDir.mkdir();
        
        File[] sourceFolders = new File[2];
        File src1 = new File(projectDir, "src1");
        src1.mkdirs();
        File src2 = new File(projectDir, "src2");
        src2.mkdirs();
        sourceFolders[0] = src1;
        sourceFolders[1] = src2;
        
        File[] testFolders = new File[2];
        File test1 = new File(projectDir, "test1");
        test1.mkdirs();
        File test2 = new File(projectDir, "test2");
        test2.mkdirs();
        testFolders[0] = test1;
        testFolders[1] = test2;
        
        AntProjectHelper project = org.netbeans.modules.java.j2seproject.J2SEProjectGenerator.createProject(projectDir, projName2, sourceFolders, testFolders, null);
        Utilities.waitScanFinished();
        Utilities.openProject(projectDir);
        assertTrue(Utilities.deleteProjectFolder(project.getProjectDirectory().getPath()));
    }
}
