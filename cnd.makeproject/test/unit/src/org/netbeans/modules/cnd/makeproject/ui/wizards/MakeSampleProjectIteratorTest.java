/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectType;
import org.netbeans.modules.cnd.test.CndTestIOProvider;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;

public class MakeSampleProjectIteratorTest extends CndBaseTestCase {
    private CompilerSet SunStudioSet = null;
    private CompilerSet GNUSet = null;
    private CompilerSet MinGWSet = null;
    private CompilerSet CygwinSet = null;

    List<CompilerSet> allAvailableCompilerSets = null;
    List<CompilerSet> SunStudioCompilerSet = null;
    List<CompilerSet> GNUCompilerSet = null;
    String[] defaultConfs = new String[] {"Debug", "Release"};

    public MakeSampleProjectIteratorTest(String name) {
        super(name);
    }

    @Before @Override
    public void setUp() throws Exception {
        super.setUp();
        List<CompilerSet> sets = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getCompilerSets();
        for (CompilerSet set : sets) {
            if (set.getName().equals("SunStudio")) {
                SunStudioSet = set;
            }
            if (set.getName().equals("GNU")) {
                GNUSet = set;
            }
            if (set.getName().equals("MinGW")) {
                MinGWSet = set;
            }
            if (set.getName().equals("Cygwin")) {
                CygwinSet = set;
            }
        }

        allAvailableCompilerSets = new ArrayList<CompilerSet>();
        allAvailableCompilerSets.add(SunStudioSet);
        allAvailableCompilerSets.add(GNUSet);
        allAvailableCompilerSets.add(MinGWSet);
        allAvailableCompilerSets.add(CygwinSet);

        SunStudioCompilerSet = new ArrayList<CompilerSet>();
        SunStudioCompilerSet.add(SunStudioSet);

        GNUCompilerSet = new ArrayList<CompilerSet>();
        GNUCompilerSet.add(GNUSet);
    }

    @Test
    public void testArguments() throws IOException, InterruptedException, InvocationTargetException {
        testSample(allAvailableCompilerSets, "Arguments", defaultConfs, "");
    }

    @Test
    public void testInputOutput() throws IOException, InterruptedException, InvocationTargetException {
        testSample(allAvailableCompilerSets, "InputOutput", defaultConfs, "");
    }

    @Test
    public void testWelcome() throws IOException, InterruptedException, InvocationTargetException {
        testSample(allAvailableCompilerSets, "Welcome", defaultConfs, "");
    }

    @Test
    public void testQuote() throws IOException, InterruptedException, InvocationTargetException {
        testSample(allAvailableCompilerSets, "Quote", defaultConfs, "");
    }

    @Test
    public void testSubProjects() throws IOException, InterruptedException, InvocationTargetException {
        testSample(allAvailableCompilerSets, "SubProjects", defaultConfs, "");
    }

    @Test
    public void testPi() throws IOException, InterruptedException, InvocationTargetException {
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            testSample(SunStudioCompilerSet, "Pi", new String[] {"Serial", "Pthreads", "Pthreads_safe", "Pthread_Hot", "OpenMP"}, "");
            testSample(GNUCompilerSet, "Pi", new String[] {"Serial"}, "");
        }
        else {
            testSample(allAvailableCompilerSets, "Pi", new String[] {"Serial"}, "");
        }
    }

    @Test
    public void testFreeway() throws IOException, InterruptedException, InvocationTargetException {
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS || Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
            testSample(allAvailableCompilerSets, "Freeway", defaultConfs, "");
        }
    }

    @Test
    public void testFractal() throws IOException, InterruptedException, InvocationTargetException {
        testSample(allAvailableCompilerSets, "Fractal", new String[] {"FastBuild", "Debug", "PerformanceDebug", "DianogsableRelease", "Release", "PerformanceRelease"}, "");
    }

    @Test
    public void testLexYacc() throws IOException, InterruptedException, InvocationTargetException {
        if (!Utilities.isWindows()) {
            testSample(allAvailableCompilerSets, "LexYacc", defaultConfs, "");
        }
    }

    @Test
    public void testMP() throws IOException, InterruptedException, InvocationTargetException {
        if (!Utilities.isWindows()) {
            testSample(allAvailableCompilerSets, "MP", new String[] {"Debug", "Debug_mp", "Release", "Release_mp"}, "");
        }
    }

    @Test
    public void testHello() throws IOException, InterruptedException, InvocationTargetException {
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            testSample(SunStudioCompilerSet, "Hello", defaultConfs, "");
        }
    }

    @Test
    public void testHelloQtWorld() throws IOException, InterruptedException, InvocationTargetException {
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            testSample(SunStudioCompilerSet, "HelloQtWorld", defaultConfs, "-j 1");
        }
        if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
            testSample(GNUCompilerSet, "HelloQtWorld", defaultConfs, "");
        }
    }

    @Test
    public void testProfilingDemo() throws IOException, InterruptedException, InvocationTargetException {
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS || Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
            testSample(SunStudioCompilerSet, "ProfilingDemo", defaultConfs, "");
        }
    }

    @Override
    protected List<Class<?>> getServices() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(MakeProjectType.class);
        list.addAll(super.getServices());
        return list;
    }

    protected static Set<DataObject>  instantiateSample(String name, final File destdir) throws IOException, InterruptedException, InvocationTargetException {
        if(destdir.exists()) {
            assertTrue("Can not remove directory " + destdir.getAbsolutePath(), removeDirectoryContent(destdir));
        }
        final FileObject templateFO = FileUtil.getConfigFile("Templates/Project/Samples/Native/" + name);
        assertNotNull("FileObject for " + name + " sample not found", templateFO);
        final DataObject templateDO = DataObject.find(templateFO);
        assertNotNull("DataObject for " + name + " sample not found", templateDO);
        final AtomicReference<IOException> exRef = new AtomicReference<IOException>();
        final AtomicReference<Set<DataObject>> setRef = new AtomicReference<Set<DataObject>>();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                MakeSampleProjectIterator projectCreator = new MakeSampleProjectIterator();
                TemplateWizard wiz = new TemplateWizard();
                wiz.setTemplate(templateDO);
                projectCreator.initialize(wiz);
                wiz.putProperty("name", destdir.getName());
                wiz.putProperty("projdir", destdir);
                try {
                    setRef.set(projectCreator.instantiate(wiz));
                } catch (IOException ex) {
                    exRef.set(ex);
                }
            }
        });
        if (exRef.get() != null) {
            throw exRef.get();
        }
        return setRef.get();
    }

    public void testSample(List<CompilerSet> sets, String sample, String[] confs, String makeOptions) throws IOException, InterruptedException, InvocationTargetException {
        for (CompilerSet set : sets) {
            if (set != null) {
                for (String conf : confs) {
                    testSample(set, sample, conf, makeOptions);
                }
            }
        }
    }

    public void testSample(CompilerSet set, String sample, String conf, String makeOptions) throws IOException, InterruptedException, InvocationTargetException {
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicInteger build_rc = new AtomicInteger(-1);

        CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).setDefault(set);
        MakeOptions.setDefaultMakeOptions(makeOptions);

        File workDir = getWorkDir();//new File("/tmp");
        File projectDir = new File(workDir, sample + set.getName() + conf);
        File mainProjectDir = null;
        FileObject mainProjectDirFO = null;
        Set<DataObject> projectDataObjects;

        projectDataObjects = instantiateSample(sample, projectDir);
        assertTrue(projectDataObjects.size()>0);

        for (DataObject projectDataObject : projectDataObjects) {
            FileObject projectDirFO = projectDataObject.getPrimaryFile();
            if (mainProjectDir == null) {
                mainProjectDirFO = projectDirFO;
                mainProjectDir = FileUtil.toFile(projectDirFO);
            }
            ConfigurationDescriptorProvider descriptorProvider = new ConfigurationDescriptorProvider(projectDirFO);
            MakeConfigurationDescriptor descriptor = descriptorProvider.getConfigurationDescriptor(true);
            descriptor.getConfs().setActive(conf);
            descriptor.save(); // make sure all necessary configuration files in nbproject/ are written
        }

        final String successLine = "BUILD SUCCESSFUL";
        final String failureLine = "BUILD FAILED";

        IOProvider iop = IOProvider.getDefault();
        assert iop instanceof CndTestIOProvider : "found " + iop.getClass();
        ((CndTestIOProvider) iop).addListener(new CndTestIOProvider.Listener() {
            @Override
            public void linePrinted(String line) {
                if(line != null) {
                    if (line.startsWith(successLine)) {
                        build_rc.set(0);
                        done.countDown();
                    }
                    else if (line.startsWith(failureLine)) {
                        // message is:
                        // BUILD FAILED (exit value 1, total time: 326ms)
                        int rc = -1;
                        String[] tokens = line.split("[ ,]");
                        if (tokens.length > 4) {
                            try {
                                rc = Integer.parseInt(tokens[4]);
                            } catch(NumberFormatException nfe) {
                                nfe.printStackTrace();
                            }
                        }
                        build_rc.set(rc);
                        done.countDown();
                    }
                }
            }
        });

        MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(mainProjectDirFO);
        assertNotNull(makeProject);
        MakeActionProvider makeActionProvider = new MakeActionProvider(makeProject);
        makeActionProvider.invokeAction("build", null);

//        File makefile = new File(mainProjectDir, "Makefile");
//        FileObject makefileFileObject = FileUtil.toFileObject(makefile);
//        assertTrue("makefileFileObject == null", makefileFileObject != null);
//        DataObject dObj = null;
//        try {
//            dObj = DataObject.find(makefileFileObject);
//        } catch (DataObjectNotFoundException ex) {
//        }
//        assertTrue("DataObjectNotFoundException", dObj != null);
//        Node node = dObj.getNodeDelegate();
//        assertTrue("node == null", node != null);
//
//        MakeExecSupport ses = node.getCookie(MakeExecSupport.class);
//        assertTrue("ses == null", ses != null);
//
//        MakeAction.execute(node, target, new MyExecutionListener(), null, null, null);
//
        try {
            done.await();
        } catch (InterruptedException ir) {
        }

        assertTrue("build failed - rc = " + build_rc.intValue(), build_rc.intValue() == 0);
    }
}
