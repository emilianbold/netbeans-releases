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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.ui.wizard.HostValidatorImpl;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.cnd.test.CndTestIOProvider;
import org.netbeans.modules.cnd.ui.options.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.spi.project.ActionProvider;
import org.openide.windows.IOProvider;

/**
 * A common base class for remote "unit" tests
 * @author Sergey Grinev
 */
public abstract class RemoteTestBase extends CndBaseTestCase {

    protected static final Logger log = RemoteUtil.LOGGER;

    public static enum Sync {
        FTP("ftp"),
        RFS("rfs"),
        ZIP("scp");
        public final String ID;
        Sync(String id) {
            this.ID = id;
        }
    }

    public static enum Toolchain {
        GNU("GNU"),
        SUN("SunStudio");
        public final String ID;
        Toolchain(String id) {
            this.ID = id;
        }
    }

    private final static String successLine = "BUILD SUCCESSFUL";
    private final static String failureLine = "BUILD FAILED";
    private final static String[] errorLines = new String[] {
            "Error copying project files",
            "CLEAN FAILED"
        };

    static {
        System.setProperty("jsch.connection.timeout", "30000");
        System.setProperty("socket.connection.timeout", "30000");
    }

    static {
        log.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                // Log if parent cannot log the message ONLY.
                if (!log.getParent().isLoggable(record.getLevel())) {
                    System.err.printf("%s: %s\n", record.getLevel(), record.getMessage()); // NOI18N
                    if (record.getThrown() != null) {
                        record.getThrown().printStackTrace(System.err);
                    }
                }
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        });
    }


    // we need this for tests which should run NOT for all environments
    public RemoteTestBase(String testName) {
        super(testName);
        setSysProps();
    }

    protected RemoteTestBase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
        setSysProps();
    }

    private void setSysProps() {
        try {
            addPropertyFromRcFile(RemoteDevelopmentTest.DEFAULT_SECTION, "cnd.remote.logger.level");
            addPropertyFromRcFile(RemoteDevelopmentTest.DEFAULT_SECTION, "nativeexecution.support.logger.level");
            addPropertyFromRcFile(RemoteDevelopmentTest.DEFAULT_SECTION, "cnd.remote.force.setup", "true");
            addPropertyFromRcFile(RemoteDevelopmentTest.DEFAULT_SECTION, "socket.connection.timeout", "10000");
            if (NativeExecutionTestSupport.getBoolean(RemoteDevelopmentTest.DEFAULT_SECTION, "logging.finest")) {
                Logger.getLogger("cnd.remote.logger").setLevel(Level.ALL);
                Logger.getLogger("nativeexecution.support.logger.level").setLevel(Level.FINEST);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (FormatException ex) {
            ex.printStackTrace();
        }
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

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ConnectionManager.getInstance().disconnect(getTestExecutionEnvironment());
    }

    protected static void setupHost(ExecutionEnvironment execEnv) {
        ToolsCacheManager tcm = new ToolsCacheManager();
        HostValidatorImpl validator = new HostValidatorImpl(tcm);
        boolean ok = validator.validate(execEnv, null, false, new PrintWriter(System.out));
        assertTrue(ok);
        tcm.applyChanges();
    }

    protected void rebuildProject(MakeProject makeProject, long timeout, TimeUnit unit) throws InterruptedException, IllegalArgumentException {
        buildProject(makeProject, ActionProvider.COMMAND_REBUILD, timeout, unit);
    }

    protected void buildProject(MakeProject makeProject, long timeout, TimeUnit unit) throws InterruptedException, IllegalArgumentException {
        buildProject(makeProject, ActionProvider.COMMAND_BUILD, timeout, unit);
    }

    protected void buildProject(MakeProject makeProject, String command, long timeout, TimeUnit unit) throws InterruptedException, IllegalArgumentException {

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
                    } else if (isFailureLine(line)) {
                        int rc = -1;
                        if (line.startsWith(failureLine)) {
                            // message is:
                            // BUILD FAILED (exit value 1, total time: 326ms)
                            String[] tokens = line.split("[ ,]");
                            if (tokens.length > 4) {
                                try {
                                    rc = Integer.parseInt(tokens[4]);
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace();
                                }
                            }
                        }
                        build_rc.set(rc);
                        done.countDown();
                    }
                }
            }

            private boolean isFailureLine(String line) {
                if (line.startsWith(failureLine)) {
                    return true;
                }
                for (int i = 0; i < errorLines.length; i++) {
                    if (line.startsWith(errorLines[i])) {
                        return true;
                    }
                }
                return false;
            }
        });
        MakeActionProvider makeActionProvider = new MakeActionProvider(makeProject);
        makeActionProvider.invokeAction(command, null);
        if (timeout <= 0) {
            done.await();
        } else {
            if (!done.await(timeout, unit)) {
                assertTrue("Timeout: could not build within " + timeout + " " + unit.toString().toLowerCase(), false);
            }
        }
        assertTrue("build failed: RC=" + build_rc.get(), build_rc.get() == 0);
    }

    protected void removeRemoteHome() {
        String cmd = "rm -rf ${HOME}/.netbeans/remote/*";
        int rc = RemoteCommandSupport.run(getTestExecutionEnvironment(), cmd);
        assertEquals("Failed to run " + cmd, 0, rc);
    }

    protected void removeRemoteHomeSubdir(String subdir) {
        String cmd = "rm -rf ${HOME}/.netbeans/remote/" + subdir;
        int rc = RemoteCommandSupport.run(getTestExecutionEnvironment(), cmd);
        assertEquals("Failed to run " + cmd, 0, rc);
    }

    protected void addPropertyFromRcFile(String section, String varName) throws IOException, FormatException {
        addPropertyFromRcFile(section, varName, null);
    }

    protected void addPropertyFromRcFile(String section, String varName, String defaultValue) throws IOException, FormatException {
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String value = rcFile.get(section, varName, defaultValue);
        if (value != null && value.length() > 0) {
            System.setProperty(varName, value);
        }
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
