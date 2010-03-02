/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.apisupport.crudsample;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.DialogDisplayerImpl;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.TestAntLogger;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests crudsample sample.
 * Invokes various Ant targets over crudsample sample.
 *
 * @author Tomas Musil
 */
public class BuildCRUDSampleApplicationTest extends TestBase {

    private File crudSampleFolder = null;

    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
    }

    public BuildCRUDSampleApplicationTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        noDataDir = true;
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
        TestAntLogger.getDefault().setEnabled(true);
    }

    @Override
    protected void tearDown() throws Exception {
        TestAntLogger.getDefault().setEnabled(false);
    }

    /**
     * Extracts crudsample to workdir, then platform properties are copied and ant task(s) is called. Build status is returned
     */
    public int runAntTargetsOncrudsample(String[] targets) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/apisupport/crudsample/SampleCRUDAppProject.zip");
        assertNotNull(is);
        crudSampleFolder = new File(getWorkDir(), "crudsample");
        crudSampleFolder.mkdir();
        FileObject fo = FileUtil.toFileObject(crudSampleFolder);
        assertNotNull(fo);


        try {
            FileUtil.extractJar(fo, is);
        } finally {
            is.close();
        }

        File buildProps = new File(getWorkDir(), "build.properties");
        File privateFolder = new File(new File(crudSampleFolder, "nbproject"), "private");
        privateFolder.mkdir();

        FileObject platfPrivateProps = FileUtil.copyFile(FileUtil.toFileObject(buildProps), FileUtil.toFileObject(privateFolder), "platform-private");
        assertNotNull(platfPrivateProps);
        SuiteProject crudSampleSuite = (SuiteProject) ProjectManager.getDefault().findProject(fo);
        assertNotNull(crudSampleSuite);
        FileObject buildScript = crudSampleSuite.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        assertNotNull(buildScript);
        assertTrue(buildScript.isValid());
        Properties props = new Properties();
        File toplinkJar1 = new File(destDirF, "java/modules/ext/toplink/toplink-essentials.jar");
        assertTrue(toplinkJar1.getAbsolutePath(), toplinkJar1.isFile());
        assert toplinkJar1.exists() : "toplinkJar1 exists";
        File toplinkJar2 = new File(destDirF, "java/modules/ext/toplink/toplink-essentials-agent.jar");
        assertTrue(toplinkJar2.getAbsolutePath(), toplinkJar2.isFile());
        assert toplinkJar2.exists() : "toplinkJar2 exists";
        props.setProperty("persistence-library1.jar", toplinkJar1.getAbsolutePath());
        props.setProperty("persistence-library2.jar", toplinkJar2.getAbsolutePath());
        props.setProperty("libs.toplink.classpath", "" + toplinkJar1 + File.pathSeparator + toplinkJar2);

        System.out.println("------------- BUILD OUTPUT --------------");
        ExecutorTask et = ActionUtils.runTarget(buildScript, targets, props);
        et.waitFinished();
        System.out.println("-----------------------------------------");
        return et.result();
    }

    /**
     * Invokes build-jnlp target on crudsample
     */
    public void testBuildJNLP() throws Exception {
        int ret = runAntTargetsOncrudsample(new String[] {"build-jnlp"});
        assertEquals("build-jnlp ant target should return zero - build successful", 0 , ret);
        File dist = new File(crudSampleFolder,"dist");
        File warFile = new File(dist,"crud_sample_application.war");
        assertTrue("crud_sample_application.war file should be in dist folder", warFile.exists());
    }

    /**
     * Invokes build-zip target on crudsample
     */
    public void testBuildZip() throws Exception {
        int ret = runAntTargetsOncrudsample(new String[]{"build-zip"});
        assertEquals("build-zipant target should return zero - build successful", 0, ret);
        File dist = new File(crudSampleFolder, "dist");
        File zipFile = new File(dist, "crud_sample_application.zip");
        assertTrue("crud_sample_application.zip file should be in dist folder", zipFile.exists());
    }

    /**
     * Invokes build target on crudsample
     */
    public void testBuild() throws Exception {
        int ret = runAntTargetsOncrudsample(new String[] {"build"});
        assertEquals("build ant target should return zero - build successful", 0 , ret);
        File buildFolder = new File(crudSampleFolder,"build");
        assertTrue("build folder should exist", buildFolder.exists() && buildFolder.isDirectory());
        String[] children = buildFolder.list();
        assertTrue("build folder is not empty", children.length>0);

    }

    /**
     * Invokes nbms target on crudsample
     */
    public void testBuildNBMs() throws Exception {
        int ret = runAntTargetsOncrudsample(new String[] {"nbms"});
        assertEquals("build ant target should return zero - build successful", 0 , ret);
        File buildFolder = new File(crudSampleFolder,"build");
        File updatesFolder = new File(buildFolder,"updates");
        assertTrue("build/update folder exists", updatesFolder.exists() && updatesFolder.isDirectory());
        File viewerNbm = new File(updatesFolder, "org-netbeans-modules-customerviewer.nbm");
        File editorNbm = new File(updatesFolder, "org-netbeans-modules-customereditor.nbm");
        File customerDbNbm = new File(updatesFolder, "org-netbeans-modules-customerdb.nbm");
        File derbyNbm = new File(updatesFolder, "org-netbeans-modules-derbyclientlibrary.nbm");
        File persistenceLibNbm = new File(updatesFolder, "org-netbeans-modules-persistencelibrary.nbm");
        assertTrue("Viewer NBM is in build/updates folder", viewerNbm.exists());
        assertTrue("Editor NBM is in build/updates folder", editorNbm.exists());
        assertTrue("Customer DB NBM is in build/updates folder", customerDbNbm.exists());
        assertTrue("Derby NBM is in build/updates folder", derbyNbm.exists());
        assertTrue("Persistence Library NBM is in build/updates folder", persistenceLibNbm.exists());
        assertEquals("5 nbms are in build/updates folder", 5, updatesFolder.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.indexOf("nbm") != -1;
            }
        }).length);
    }

    /**
     * Invokes clean target on crudsample
     */
    public void testClean() throws Exception {
        int ret = runAntTargetsOncrudsample(new String[]{"clean"});
        assertEquals("clean ant target should return zero - build successful", 0, ret);
        File buildFolder = new File(crudSampleFolder, "build");
        File distFolder = new File(crudSampleFolder, "dist");
        assertTrue("build and dist folders are deleted", !distFolder.exists() && !buildFolder.exists());
    }
}

