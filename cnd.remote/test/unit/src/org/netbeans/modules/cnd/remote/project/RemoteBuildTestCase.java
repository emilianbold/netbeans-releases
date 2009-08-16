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

import java.io.IOException;
import org.netbeans.modules.cnd.test.CndTestIOProvider;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.Test;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.remote.mapper.RemoteBuildTestBase;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.openide.windows.IOProvider;
/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteBuildTestCase extends RemoteBuildTestBase {

    public RemoteBuildTestCase(String testName) {
        super(testName);
    }

    public RemoteBuildTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);       
    }


    @ForAllEnvironments
    public void testBuildSampleArguments() throws Exception {
        setupHost("scp");

        FileObject projectDirFO = prepareSampleProject("Arguments", "Args_01");

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
