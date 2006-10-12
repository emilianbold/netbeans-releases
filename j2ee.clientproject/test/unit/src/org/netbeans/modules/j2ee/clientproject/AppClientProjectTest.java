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

package org.netbeans.modules.j2ee.clientproject;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.clientproject.AppClientProject.ProjectOpenedHookImpl;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.clientproject.test.TestUtil;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;

/**
 * @author Martin Krauskopf
 */
public class AppClientProjectTest extends NbTestCase {
    
    private String serverID;
    
    public AppClientProjectTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
    }
    
    public void testBrokenAppClientOpening_73710() throws Exception {
        doTestBrokenAppClientOpening_73710(generateApplicationClient(
                "TestCreateACProject_14", J2eeModule.J2EE_14));
        doTestBrokenAppClientOpening_73710(generateApplicationClient(
                "TestCreateACProject_15", J2eeModule.JAVA_EE_5));
    }
    
    private void doTestBrokenAppClientOpening_73710(final File prjDirF) throws IOException, IllegalArgumentException {
        File dirCopy = TestUtil.copyFolder(getWorkDir(), prjDirF);
        File ddF = new File(dirCopy, "src/conf/application-client.xml");
        assertTrue("has deployment descriptor", ddF.isFile());
        ddF.delete(); // one of #73710 scenario
        FileObject fo = FileUtil.toFileObject(dirCopy);
        Project project = ProjectManager.getDefault().findProject(fo);
        assertNotNull("project is found", project);
        // tests #73710
        AppClientProjectTest.openProject((AppClientProject) project);
    }
    
    private File generateApplicationClient(String prjDir, String version) throws IOException, SAXException {
        File prjDirF = new File(getWorkDir(), prjDir);
        AppClientProjectGenerator.createProject(prjDirF, "test-project",
                "test.MyMain", version, serverID);
        return prjDirF;
    }
    
    /**
     * Accessor method for those who wish to simulate open of a project and in
     * case of suite for example generate the build.xml.
     */
    public static void openProject(final Project p) {
        ProjectOpenedHookImpl hook = (ProjectOpenedHookImpl) p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        hook.projectOpened(); // protected but can use package-private access
    }
    
}
