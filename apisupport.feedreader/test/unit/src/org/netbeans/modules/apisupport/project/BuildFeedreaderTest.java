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

import java.io.File;
import java.io.InputStream;
import javax.swing.JOptionPane;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Tests Feedreader sample.
 * Invokes various Ant targets over Feedreader sample.
 *
 * @author Tomas Musil
 */
public class BuildFeedreaderTest extends TestBase {
    
    private File feedFolder = null;
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
    }
    
    public BuildFeedreaderTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        noDataDir = true;
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
        TestAntLogger.getDefault().setEnabled(true);
    }
    
    protected void tearDown() throws Exception {
        TestAntLogger.getDefault().setEnabled(false);
    }
    
    /**
     * Extracts feedreader to workdir, then platform properties are copied and ant task(s) is called. Build status is returned
     */
    public int runAntTargetsOnFeedreader(String[] targets) throws Exception{
        InputStream is = getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/apisupport/feedreader/FeedReaderProject.zip");
        assertNotNull(is);
        feedFolder = new File(getWorkDir(),"feedreader");
        feedFolder.mkdir();
        FileObject fo = FileUtil.toFileObject(feedFolder);
        assertNotNull(fo);
        
        
        try {
            FileUtil.extractJar(fo,is);
        } finally {
            is.close();
        }
        
        File buildProps = new File(getWorkDir(), "build.properties");
        File privateFolder = new File(new File(feedFolder, "nbproject"),"private");
        privateFolder.mkdir();
        
        FileObject platfPrivateProps = FileUtil.copyFile(FileUtil.toFileObject(buildProps), FileUtil.toFileObject(privateFolder), "platform-private");
        assertNotNull(platfPrivateProps);
        SuiteProject feedreaderSuite = (SuiteProject) ProjectManager.getDefault().findProject(fo);
        assertNotNull(feedreaderSuite);
        FileObject buildScript = feedreaderSuite.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        assertNotNull(buildScript);
        assertTrue(buildScript.isValid());
        
        System.out.println("------------- BUILD OUTPUT --------------");
        ExecutorTask et = ActionUtils.runTarget(buildScript, targets, null);
        et.waitFinished();
        System.out.println("-----------------------------------------");
        // ant task executor returns 0 on win and jdk 1.5.0_xxx
        boolean win15 = Utilities.isWindows() && System.getProperty("java.version").startsWith("1.5.0_");
        
        return (win15)? 0: et.result();
    }
    
    /**
     * Invokes build-jnlp target on feedreader
     */
    public void testBuildJNLP() throws Exception {
        int ret = runAntTargetsOnFeedreader(new String[] {"build-jnlp"});
        assertFileExists("dist/feedreader.war");
        assertEquals("build-jnlp ant target should return zero - build successful", 0 , ret);
    }
    
    /**
     * Invokes build-zip target on feedreader
     */
    public void testBuildZip() throws Exception {
        int ret = runAntTargetsOnFeedreader(new String[] {"build-zip"});
        assertFileExists("dist/feedreader.zip");
        assertEquals("build-zipant target should return zero - build successful", 0 , ret);
    }
    
    /**
     * Invokes build target on feedreader
     */
    public void testBuild() throws Exception {
        int ret = runAntTargetsOnFeedreader(new String[] {"build"});
        assertEquals("build ant target should return zero - build successful", 0 , ret);
    }
    
    /**
     * Invokes nbms target on feedreader
     */
    public void testBuildNBMs() throws Exception {
        int ret = runAntTargetsOnFeedreader(new String[] {"nbms"});
        assertFileExists("build/updates/com-sun-syndication-fetcher.nbm");
        assertFileExists("build/updates/com-sun-syndication.nbm");
        assertFileExists("build/updates/org-jdom.nbm");
        assertFileExists("build/updates/org-myorg-feedreader.nbm");
        assertFileExists("build/updates/updates.xml");
        assertEquals("build ant target should return zero - build successful", 0 , ret);
    }
    
    /**
     * Invokes clean target on feedreader
     */
    public void testClean() throws Exception {
        int ret = runAntTargetsOnFeedreader(new String[] {"clean"});
        assertFalse("Empty build",new File(feedFolder,"build").exists());
        assertFalse("Empty dist",new File(feedFolder,"dist").exists());
        
        assertEquals("clean ant target should return zero - build successful", 0 , ret);
    }

    private void assertFileExists(String relPath) {
        assertTrue("Feed reader folder exists",feedFolder.exists());
        File f = new File (feedFolder,relPath);
        assertTrue("File ${feedreader}/" + relPath,f.exists());
    }
    
}

