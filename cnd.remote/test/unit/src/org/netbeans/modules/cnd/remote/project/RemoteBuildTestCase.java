/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.project;

import org.netbeans.modules.cnd.test.CndTestIOProvider;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.Test;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.wizards.MakeSampleProjectIterator;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.support.RemoteTestBase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Node;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectType;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.openide.windows.IOProvider;
/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteBuildTestCase extends RemoteTestBase {

    public RemoteBuildTestCase(String testName) {
        super(testName);
    }

    public RemoteBuildTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);       
    }

    private static void instantiateSample(String name, File destdir) throws IOException {
        FileObject templateFO = FileUtil.getConfigFile("Templates/Project/Samples/Native/" + name);
        assertNotNull("FileObject for " + name + " sample not found", templateFO);
        DataObject templateDO = DataObject.find(templateFO);
        assertNotNull("DataObject for " + name + " sample not found", templateDO);
        MakeSampleProjectIterator projectCreator = new MakeSampleProjectIterator();
        TemplateWizard wiz = new TemplateWizard();
        wiz.setTemplate(templateDO);
        projectCreator.initialize(wiz);
        wiz.putProperty("name", destdir.getName());
        wiz.putProperty("projdir", destdir);
        projectCreator.instantiate(wiz);
        return;
    }

    @Override
    protected List<Class> getServises() {
        List<Class> list = new ArrayList<Class>();
        list.add(MakeProjectType.class);
        list.addAll(super.getServises());
        return list;
    }

    @ForAllEnvironments
    public void testBuildSampleArguments() throws Exception {

        ExecutionEnvironment env = getTestExecutionEnvironment();
        setupHost(env);

        RemoteSyncFactory syncFactory = RemoteSyncFactory.fromID("scp");
        if (syncFactory == null) {
            syncFactory = RemoteSyncFactory.getDefault();
        }

        ServerRecord rec = ServerList.addServer(env, env.getDisplayName(), syncFactory, true, true);
        assertNotNull("Null ServerRecord for " + env, rec);

        File projectDir = new File(getWorkDir(), "Args_01");
        instantiateSample("Arguments", projectDir);

        FileObject projectDirFO = FileUtil.toFileObject(projectDir);
        ConfigurationDescriptorProvider descriptorProvider = new ConfigurationDescriptorProvider(projectDirFO);
        MakeConfigurationDescriptor descriptor = descriptorProvider.getConfigurationDescriptor(true);
        descriptor.save(); // make sure all necessary configuration files in nbproject/ are written


        File makefile = new File(projectDir, "Makefile");
        FileObject makefileFileObject = FileUtil.toFileObject(makefile);
        assertTrue("makefileFileObject == null", makefileFileObject != null);
        DataObject dObj = null;
        try {
            dObj = DataObject.find(makefileFileObject);
        } catch (DataObjectNotFoundException ex) {
        }
        assertTrue("DataObjectNotFoundException", dObj != null);
        Node node = dObj.getNodeDelegate();
        assertTrue("node == null", node != null);

        MakeExecSupport ses = node.getCookie(MakeExecSupport.class);
        assertTrue("ses == null", ses != null);

        final CountDownLatch done = new CountDownLatch(1);
        final AtomicInteger build_rc = new AtomicInteger(-1);

//        ExecutionListener listener = new ExecutionListener() {
//            public void executionFinished(int rc) {
//                //System.err.printf("EXECUTION FINISHED\n");
//                build_rc.set(rc);
//                done.countDown();
//            }
//            public void executionStarted() {
//                //System.err.printf("EXECUTION STARTED\n");
//            }
//        };

        final String successLine = "BUILD SUCCESSFUL";
        final String failureLine = "BUILD FAILED";

        IOProvider iop = IOProvider.getDefault();
        assert iop instanceof CndTestIOProvider;
        ((CndTestIOProvider) iop).addListener(new CndTestIOProvider.Listener() {
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

        MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(projectDirFO);
        MakeActionProvider makeActionProvider = new MakeActionProvider(makeProject);
        makeActionProvider.invokeAction("build", null);


        done.await();

        assertTrue("build failed: RC=" + build_rc.get(), build_rc.get() == 0);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteBuildTestCase.class);
    }
}
