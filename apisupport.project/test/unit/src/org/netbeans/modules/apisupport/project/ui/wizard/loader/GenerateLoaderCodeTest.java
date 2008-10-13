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

package org.netbeans.modules.apisupport.project.ui.wizard.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.SuiteActions;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.netbeans.spi.project.ActionProvider;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;

/**
 * Checks loader wizard behaviour.
 * @author Jaroslav Tulach
 */
public class GenerateLoaderCodeTest extends TestBase {
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
//        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        //LayerTestBase.Lkp.setLookup(new Object[0]);
    }
    
    private SuiteProject suite;
    private Logger LOG;
    private TemplateWizard w;
    private NbModuleProject proj;
    
    public GenerateLoaderCodeTest(String name) {
        super(name);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    public static Test suite() {
        //return new GenerateLoaderCodeTest("testBuildJNLPWhenLocalizedFilesAreMissing");
        return new NbTestSuite(GenerateLoaderCodeTest.class);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("test." + getName());
        
        super.setUp();

        InstalledFileLocatorImpl.registerDestDir(destDirF);
        
        suite = TestBase.generateSuite(new File(getWorkDir(), "projects"), "suite");
        proj = TestBase.generateSuiteComponent(suite, "mod1");
        
        suite.open();
        proj.open();
        w = new TemplateWizard();
        w.putProperty( ProjectChooserFactory.WIZARD_KEY_PROJECT, proj );
    }
    
    public void testGenerateTheFileTypeCode() throws Exception {
        //    XXX: failing test, fix or delete
//        NewLoaderIterator.DataModel model = new NewLoaderIterator.DataModel(w);
//        model.setExtension("jarda");
//        model.setMimeType("text/x-jarda");
//        model.setPrefix("Jarda");
//        model.setPackageName("org.nb.test");
//        
//        NewLoaderIterator.generateFileChanges(model);
//        model.getCreatedModifiedFiles().run();
//        
//        FileObject src = proj.getProjectDirectory().getFileObject("src/org/nb/test/JardaDataObject.java");
//        assertNotNull("Loader created", src);
//        FileObject test = proj.getProjectDirectory().getFileObject("test/unit/src/org/nb/test/JardaDataObjectTest.java");
//        assertNotNull("Test created", test);
//                
//        
//        SuiteActions p = (SuiteActions)suite.getLookup().lookup(ActionProvider.class);
//        assertNotNull("Provider is here");
//        
//        List l = Arrays.asList(p.getSupportedActions());
//        assertTrue("We support test: " + l, l.contains("test"));
//        
//        LOG.info("invoking test");
//        ExecutorTask task = p.invokeActionImpl("test", suite.getLookup());
//        LOG.info("Invocation started");
//        
//        assertNotNull("Task was started", task);
//        LOG.info("Waiting for task to finish");
//        task.waitFinished();
//        LOG.info("Checking the result");
//        assertEquals("Finished ok", 0, task.result());
//        LOG.info("Testing the content of the directory");
//        
//        FileObject[] arr = suite.getProjectDirectory().getChildren();
//        List<FileObject> subobj = new ArrayList<FileObject>(Arrays.asList(arr));
//        subobj.remove(suite.getProjectDirectory().getFileObject("mod1"));
//        subobj.remove(suite.getProjectDirectory().getFileObject("nbproject"));
//        subobj.remove(suite.getProjectDirectory().getFileObject("build.xml"));
//        subobj.remove(suite.getProjectDirectory().getFileObject("build"));
//        
//        if (!subobj.isEmpty()) {
//            fail("There should be no created directories in the suite dir: " + subobj);
//        }   
//        
//        FileObject result = proj.getProjectDirectory().getFileObject("build/test/unit/results/TEST-org.nb.test.JardaDataObjectTest.xml");
//        assertNotNull("Test has been run", result);
//        
//        String resultTxt = readFile(result);
//        if (!resultTxt.contains("errors=\"0\" failures=\"0\"")) {
//            fail(resultTxt);
//        }
//        if (!resultTxt.contains("tests=\"1\"")) {
//            fail(resultTxt);
//        }
    }
    
    private static String readFile(final FileObject fo) throws IOException, FileNotFoundException {
        // write user modified version of the file
        byte[] arr = new byte[(int)fo.getSize()];
        InputStream is = fo.getInputStream();
        int len = is.read(arr);
        assertEquals("Read all", arr.length, len);
        String s = new String(arr);
        is.close();
        return s;
    }

    private void copyFiles(File from, File to) throws IOException {
        LOG.fine("Copy " + from + " to " + to);
        if (from.isDirectory()) {
            to.mkdirs();
            for (File f : from.listFiles()) {
                copyFiles(f, new File(to, f.getName()));
            }
        } else {
            byte[] arr = new byte[4096];
            FileInputStream is = new FileInputStream(from);
            FileOutputStream os = new FileOutputStream(to);
            for (;;) {
                int r = is.read(arr);
                if (r == -1) {
                    break;
                }
                os.write(arr, 0, r);
            }
            is.close();
            os.close();
        }
        
    }
    
    private File createNewJarFile (String prefix) throws IOException {
        if (prefix == null) {
            prefix = "modules";
        }
        
        File dir = new File(this.getWorkDir(), prefix);
        dir.mkdirs();
        
        int i = 0;
        for (;;) {
            File f = new File (dir, i++ + ".jar");
            if (!f.exists ()) {
                f.createNewFile();
                return f;
            }
        }
    }
}
