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
import java.io.IOException;
import junit.framework.*;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.junit.*;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test building and cleaning tests
 * @author pzajac
 */
public class TestBuildCleanTest extends TestBase {
    
    public TestBuildCleanTest(java.lang.String testName) {
        super(testName);
    }
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
    }
    
    FileObject fsbuild;
    FileObject msfsbuild;
    FileObject javaBuild;
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
         
        fsbuild = nbroot.getFileObject("openide/fs/build.xml"); 
        msfsbuild = nbroot.getFileObject("openide/masterfs/build.xml"); 
        javaBuild = nbroot.getFileObject("java/project/build.xml");
    }

    protected void tearDown() throws Exception {
        // restore jars
        String pathfs = "nbbuild/build/testdist/unit/" + CLUSTER_PLATFORM + "/org-openide-fs/tests.jar";
        String pathjava = "nbbuild/build/testdist/unit/" + CLUSTER_IDE + "/org-netbeans-modules-java-project/tests.jar";
        if (!(new File(nbrootF,pathfs).exists())) {
            runTask(fsbuild,"test-build");
        }
        if (!(new File(nbrootF,pathjava).exists())) {
            runTask(javaBuild,"test-build-qa-functional");
        }
        super.tearDown();
    }


    public void testNBCVSProject() throws Exception {
        // Check unit tests
        //
        runTask(fsbuild,"test-build");
        checkTest("org-openide-filesystems",CLUSTER_PLATFORM,"unit",true);
        // masterfs tests depends on fs tests
        runTask(msfsbuild,"test-build");
        checkTest("org-netbeans-modules-masterfs",CLUSTER_PLATFORM,"unit",true);
        
        runTask(fsbuild,"clean");
        checkTest("org-openide-filesystems",CLUSTER_PLATFORM,"unit",false);
        runTask(fsbuild,"test-build");
        checkTest("org-openide-filesystems",CLUSTER_PLATFORM,"unit",true);
        
        // check qa-functional tests
        runTask(javaBuild,"test-build-qa-functional");
        checkTest("org-netbeans-modules-java-project",CLUSTER_IDE,"qa-functional",true);
        runTask(javaBuild,"clean");
        checkTest("org-netbeans-modules-java-project",CLUSTER_IDE,"qa-functional",false);
        
    }
    public void testExternalProject() throws Exception {
        FileObject module1build = FileUtil.toFileObject(file(EEP + "/suite4/module1/build.xml"));
        FileObject module2build = FileUtil.toFileObject(file(EEP + "/suite4/module2/build.xml"));
        runTask(module1build,"test-build");
        checkTestExternal("module1",true);
        runTask(module2build,"test-build");
        checkTestExternal("module2",true);
        runTask(module1build,"clean");
        checkTestExternal("module1",false);
 
    } 
    
    private void checkTest(String cnb, String cluster, String testtype, boolean exist) {
        String path = "nbbuild/build/testdist/" + testtype + "/" + cluster + "/" + cnb + "/tests.jar";
        FileObject testsFo = nbroot.getFileObject(path);
        if (exist) {
            assertTrue("test.jar for " + path + " doesn't exist.", testsFo != null && testsFo.isValid());
        } else {
            assertTrue("test.jar for " + path + " exists.", testsFo == null || !testsFo.isValid());
        }
    }

    private void runTask(FileObject fo, String target) throws IOException {
        ActionUtils.runTarget(fo,new String[]{target},null).waitFinished(); 
    }

    private void checkTestExternal(String cnd, boolean exist) {
        String path = "/suite4/build/testdist/unit/cluster/" + cnd + "/tests.jar";
        File tests = file(EEP + path);
        if (exist) {
            assertTrue("test.jar for " + path + " doesn't exist.",tests.exists());
        } else {
            assertTrue("test.jar for " + path + " exists.", !tests.exists());
        }
    }
}
