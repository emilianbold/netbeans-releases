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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTestSuite;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.ui.wizard.HostValidatorImpl;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.cnd.test.CndTestIOProvider;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;

/**
 * A common base class for remote "unit" tests
 * @author Sergey Grinev
 */
public abstract class RemoteTestBase extends CndBaseTestCase {

    protected static final Logger log = RemoteUtil.LOGGER;
    private String remoteTmpDir;

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
        cleanUserDir();
        setSysProps();        
    }

    protected RemoteTestBase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
        cleanUserDir();
        setSysProps();        
    }

    protected void cleanUserDir()  {
        File userDir = getUserDir();
        if (userDir.exists()) {
            if (!removeDirectoryContent(userDir)) {
                assertTrue("Can not remove the content of " +  userDir.getAbsolutePath(), false);
            }
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("LG")
    private void setSysProps() {
        try {
            addPropertyFromRcFile(RemoteDevelopmentTestSuite.DEFAULT_SECTION, "cnd.remote.logger.level");
            addPropertyFromRcFile(RemoteDevelopmentTestSuite.DEFAULT_SECTION, "nativeexecution.support.logger.level");
            addPropertyFromRcFile(RemoteDevelopmentTestSuite.DEFAULT_SECTION, "cnd.remote.force.setup", "true");
            addPropertyFromRcFile(RemoteDevelopmentTestSuite.DEFAULT_SECTION, "socket.connection.timeout", "10000");
            if (NativeExecutionTestSupport.getBoolean(RemoteDevelopmentTestSuite.DEFAULT_SECTION, "logging.finest")) {
                Logger.getLogger("nativeexecution.support.logger.level").setLevel(Level.FINEST);
                Logger.getLogger("cnd.remote.logger").setLevel(Level.ALL);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (FormatException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void setUp() throws Exception {
        System.err.printf("\n###> setUp    %s\n", getClass().getName() + '.' + getName());
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
        System.err.printf("\n###< tearDown %s\n", getClass().getName() + '.' + getName());
    }

    protected void createRemoteTmpDir() throws Exception {
        String dir = getRemoteTmpDir();
        int rc = CommonTasksSupport.mkDir(getTestExecutionEnvironment(), dir, new PrintWriter(System.err)).get().intValue();
        assertEquals("Can not create directory " + dir, 0, rc);
    }

    protected void clearRemoteTmpDir() throws Exception {
        String dir = getRemoteTmpDir();
        int rc = CommonTasksSupport.rmDir(getTestExecutionEnvironment(), dir, true, new PrintWriter(System.err)).get().intValue();
        if (rc != 0) {
            System.err.printf("Can not delete directory %s\n", dir);
        }
    }

    protected synchronized  String getRemoteTmpDir() {
        if (remoteTmpDir == null) {
            final ExecutionEnvironment local = ExecutionEnvironmentFactory.getLocal();
            MacroExpander expander = MacroExpanderFactory.getExpander(local);
            String id;
            try {
                id = expander.expandPredefinedMacros("${hostname}-${osname}-${platform}${_isa}"); // NOI18N
            } catch (ParseException ex) {
                id = local.getHost();
                Exceptions.printStackTrace(ex);
            }
            remoteTmpDir = "/tmp/" + id + "-" + System.getProperty("user.name") + "-" + getTestExecutionEnvironment().getUser();
        }
        return remoteTmpDir;
    }

    protected static void setupHost(ExecutionEnvironment execEnv) {
        ToolsCacheManager tcm = ToolsCacheManager.createInstance(true);
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

            @Override
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
        Thread.sleep(3000); // give building thread time to finish and to kill rfs_controller
        assertTrue("build failed: RC=" + build_rc.get(), build_rc.get() == 0);
    }

    protected void clearRemoteSyncRoot() {
        String dirToRemove = RemotePathMap.getRemoteSyncRoot(getTestExecutionEnvironment()) + "/*";
        boolean isOk = ProcessUtils.execute(getTestExecutionEnvironment(), "rm", "-rf", dirToRemove).isOK();
        assertTrue("Failed to remove " + dirToRemove, isOk);
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
}
