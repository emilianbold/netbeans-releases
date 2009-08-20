/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.support;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.remote.ui.wizard.HostValidatorImpl;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.cnd.test.CndTestIOProvider;
import org.netbeans.modules.cnd.ui.options.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.windows.IOProvider;

/**
 * A common base class for remote "unit" tests
 * @author Sergey Grinev
 */
public abstract class RemoteTestBase extends CndBaseTestCase {

    protected final Logger log = Logger.getLogger("cnd.remote.logger");

    protected final static String successLine = "BUILD SUCCESSFUL";
    protected final static String failureLine = "BUILD FAILED";

    // we need this for tests which should run NOT for all environments
    public RemoteTestBase(String testName) {
        super(testName);
    }

    protected RemoteTestBase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final ExecutionEnvironment env = getTestExecutionEnvironment();
        if (env != null) {
            // the password should be stored in the initialization phase
            ConnectionManager.getInstance().connectTo(env);
        }
    }

    protected static void setupHost(ExecutionEnvironment execEnv) {
        ToolsCacheManager tcm = new ToolsCacheManager();
        HostValidatorImpl validator = new HostValidatorImpl(tcm);
        boolean ok = validator.validate(execEnv, null, false, new PrintWriter(System.out));
        assertTrue(ok);
        tcm.applyChanges();
    }

    protected void buildProject(MakeProject makeProject) throws InterruptedException, IllegalArgumentException {

        final CountDownLatch done = new CountDownLatch(1);
        final AtomicInteger build_rc = new AtomicInteger(-1);
        IOProvider iop = IOProvider.getDefault();
        assert iop instanceof CndTestIOProvider;
        ((CndTestIOProvider) iop).addListener(new CndTestIOProvider.Listener() {

            public void linePrinted(String line) {
                if (line != null) {
                    if (line.startsWith(successLine)) {
                        build_rc.set(0);
                        done.countDown();
                    } else if (line.startsWith(failureLine)) {
                        // message is:
                        // BUILD FAILED (exit value 1, total time: 326ms)
                        int rc = -1;
                        String[] tokens = line.split("[ ,]");
                        if (tokens.length > 4) {
                            try {
                                rc = Integer.parseInt(tokens[4]);
                            } catch (NumberFormatException nfe) {
                                nfe.printStackTrace();
                            }
                        }
                        build_rc.set(rc);
                        done.countDown();
                    }
                }
            }
        });
        MakeActionProvider makeActionProvider = new MakeActionProvider(makeProject);
        makeActionProvider.invokeAction("build", null);
        done.await();
        assertTrue("build failed: RC=" + build_rc.get(), build_rc.get() == 0);
    }

    public static class FakeCompilerSet extends CompilerSet {

        private List<Tool> tools = Collections.<Tool>singletonList(new FakeTool());

        public FakeCompilerSet() {
            super(PlatformTypes.getDefaultPlatform());
        }

        @Override
        public List<Tool> getTools() {
            return tools;
        }

        private static class FakeTool extends BasicCompiler {

            private List<String> fakeIncludes = new ArrayList<String>();

            private FakeTool() {
                super(ExecutionEnvironmentFactory.fromUniqueID("fake"), CompilerFlavor.getUnknown(PlatformTypes.getDefaultPlatform()), 0, "fakeTool", "fakeTool", "/usr/sfw/bin");
                fakeIncludes.add("/usr/include"); //NOI18N
                fakeIncludes.add("/usr/local/include"); //NOI18N
                fakeIncludes.add("/usr/sfw/include"); //NOI18N
                //fakeIncludes.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/include");
            }

            @Override
            public List<String> getSystemIncludeDirectories() {
                return fakeIncludes;
            }

            @Override
            public CompilerDescriptor getDescriptor() {
                return null;
            }
        }
    }
}
