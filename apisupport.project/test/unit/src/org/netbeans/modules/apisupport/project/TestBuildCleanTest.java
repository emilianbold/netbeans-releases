/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test building and cleaning tests
 * @author pzajac
 */
public class TestBuildCleanTest extends TestBase {
    private FileObject nbAll;
    
    public TestBuildCleanTest(java.lang.String testName) {
        super(testName);
    }
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
        nbAll = FileUtil.toFileObject(FileUtil.normalizeFile(new File(destDirF, "../..")));
    }

    protected void tearDown() throws Exception {
        // restore jars
        super.tearDown();
    }

    public void testNBORGProject() throws Exception {
        FileObject fsbuild = nbAll.getFileObject("openide.filesystems/build.xml");
        FileObject msfsbuild = nbAll.getFileObject("masterfs/build.xml");
//        FileObject loadersBuild = nbAll.getFileObject("openide.loaders/build.xml");
        try {
            // Check unit tests
            //
            runTask(fsbuild,"test-build");
            checkTest("org-openide-filesystems",CLUSTER_PLATFORM,"unit",true);
            // masterfs tests depends on fs tests
            runTask(msfsbuild,"test-build");
            checkTest("org-netbeans-modules-masterfs",CLUSTER_PLATFORM,"unit",true);

            deleteTests("org-openide-filesystems",CLUSTER_PLATFORM,"unit");
//            checkTest("org-openide-filesystems",CLUSTER_PLATFORM,"unit",false);
            runTask(fsbuild,"test-build");
            checkTest("org-openide-filesystems",CLUSTER_PLATFORM,"unit",true);

            // no more qa-functional tests in openide.*
//            runTask(loadersBuild,"test-build-qa-functional");
//            checkTest("org-openide-loaders",CLUSTER_PLATFORM,"qa-functional",true);
//            deleteTests("org-openide-loaders",CLUSTER_PLATFORM,"qa-functional");
//            checkTest("org-openide-loaders",CLUSTER_PLATFORM,"qa-functional",false);
        } finally {        
            String pathfs = "nbbuild/build/testdist/unit/" + CLUSTER_PLATFORM + "/org-openide-fs/tests.jar";
            String pathjava = "nbbuild/build/testdist/unit/" + CLUSTER_IDE + "/org-netbeans-modules-java-project/tests.jar";

            if (!(new File(destDirF, pathfs).exists())) {
                runTask(fsbuild,"test-build");
            }
//            if (!(new File(destDirF,pathjava).exists())) {
//                runTask(loadersBuild,"test-build-qa-functional");
//            }
        }
    }
    
    public void testExternalProject() throws Exception {
        FileObject module1build = resolveEEP("/suite4/module1/build.xml");
        FileObject module2build = resolveEEP("/suite4/module2/build.xml");
        runTask(module1build,"test-build");
        checkTestExternal("module1",true);
        runTask(module2build,"test-build");
        checkTestExternal("module2",true);
        runTask(module1build,"clean");
        checkTestExternal("module1",false);
    } 

    private void checkTest(String cnb, String cluster, String testtype, boolean exist) {
        String path = "nbbuild/build/testdist/" + testtype + "/" + cluster + "/" + cnb + "/tests.jar";
        FileObject testsFo = nbAll.getFileObject(path);
        if (exist) {
            assertTrue("test.jar for " + path + " doesn't exist.", testsFo != null && testsFo.isValid());
        } else {
            assertTrue("test.jar for " + path + " exists.", testsFo == null || !testsFo.isValid());
        }
    }

    private void deleteTests(String cnb,String cluster,String testtype) throws IOException {
        String path = "nbbuild/build/testdist/" + testtype + "/" + cluster + "/" + cnb + "/tests.jar";
        FileObject testsFo = nbAll.getFileObject(path);
        if (testsFo.isValid()) {
  //          testsFo.delete();
        }
    }
    
    private void runTask(FileObject fo, String target) throws IOException {
        Properties p = new Properties();
        p.setProperty("harness.dir", new File(destDirF, "harness").getAbsolutePath());
        ActionUtils.runTarget(fo,new String[]{target}, p).waitFinished();
    }

    private void checkTestExternal(String cnd, boolean exist) {
        String path = "/suite4/build/testdist/unit/cluster/" + cnd + "/tests.jar";
        File tests = resolveEEPFile(path);
        if (exist) {
            assertTrue("test.jar for " + path + " doesn't exist.",tests.exists());
        } else {
            assertTrue("test.jar for " + path + " exists.", !tests.exists());
        }
    }
}
