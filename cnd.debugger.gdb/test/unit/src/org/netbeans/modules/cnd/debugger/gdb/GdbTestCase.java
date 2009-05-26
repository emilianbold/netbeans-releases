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

package org.netbeans.modules.cnd.debugger.gdb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger.State;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.proxy.GdbProxy;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.modules.cnd.test.BaseTestCase;

/**
 * Base class for each gdb test case should extend this class. It provides a handle
 * to a GdbDebugger as well as the test apps.
 *
 * @author gordonp
 */
public abstract class GdbTestCase extends BaseTestCase implements ContextProvider {

    protected ProjectActionEvent pae = null;
    protected Project project = null;
    protected Configuration conf = null;
    protected String testapp = null;
    protected String testproj = null;
    private String testapp_dir = null;
    private String executable = "";
    protected GdbDebugger debugger;
    protected GdbProxy gdb;
    protected static final Logger tlog = Logger.getLogger("gdb.testlogger"); // NOI18N
    private final StateListener stateListener = new StateListener();
    private final StackListener stackListener = new StackListener();

    public GdbTestCase(String name) {
        super(name);
        System.setProperty("gdb.testsuite", "true");
        tlog.setLevel(Level.FINE);
        // TODO: need to get test apps dir from the environment
        String workdir = System.getProperty("nbjunit.workdir"); // NOI18N
        if (workdir != null && workdir.endsWith("/build/test/unit/work")) { // NOI18N
            testapp_dir = workdir.substring(0, workdir.length() - 21) + "/build/testapps"; // NOI18N
            File dir = new File(testapp_dir);
            if (!dir.exists()) {
                assert false : "Missing testapps directory";
            }
        }
    }

    protected File getProjectDir(String project) {
        return new File(testapp_dir, project);
    }

    protected void startTest(String testapp) {
        this.testapp = testapp;
        System.out.println("\n" + testapp); // NOI18N
    }

    protected void tlog(String msg) {
        System.out.println("    " + testapp + ": " + msg); // NOI18N
    }

    protected void startDebugger(String testproj, String executable) {
        this.testproj = testproj;
        this.executable = testapp_dir + '/' + executable;
        debugger = new GdbDebugger(this);
        debugger.addPropertyChangeListener(GdbDebugger.PROP_STATE, stateListener);
        debugger.addPropertyChangeListener(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME, stackListener);
        String[] denv = new String[] { "GDBUnitTest=True" };
        try {
            // TODO: need to get gdb command from the toolchain or environment
            gdb = new GdbProxy(debugger, "/opt/csw/bin/gdb", denv, testapp_dir, null, "");
        } catch (Exception ex) {
            gdb = null;
        }
        assert gdb != null;

        try {
            debugger.testSuiteInit(gdb);
            gdb.gdb_show("language"); // NOI18N
            gdb.gdb_set("print repeat", "10"); // NOI18N
            gdb.file_exec_and_symbols(executable);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doFinish() {
        project = null;
        pae = null;
        debugger = null;
        testapp = null;
        executable = null;
    }

    private final Object STATE_WAIT_LOCK = new String("State Wait Lock");
    private final long WAIT_TIMEOUT = 5000;

    protected void waitForStateChange(State state) {
        long timeout = WAIT_TIMEOUT;
        long start = System.currentTimeMillis();

        System.out.println("    waitForStateChange: Waiting for state " + state + " [current is " + debugger.getState() + "]");
        synchronized (STATE_WAIT_LOCK) {
            for (;;) {
                try {
                    STATE_WAIT_LOCK.wait(timeout);
                } catch (InterruptedException ie) {
                }

                timeout = timeout - (System.currentTimeMillis() - start);

                if (debugger.getState() == state) {
                    System.out.println("    waitForStateChange: Got expected state " + state);
                    return;
                } else if (timeout < 0) {
                    System.out.println("    waitForStateChange: Timeout exceeded");
                    fail("Timeout while waiting for State " + state);
                    return;
                }
            }
        }
    }

    private final Object BP_WAIT_LOCK = new String("Breakpoint Wait Lock");

    protected void waitForBreakpoint(LineBreakpoint lb) {
        long timeout = WAIT_TIMEOUT;
        long start = System.currentTimeMillis();

        System.out.println("    waitForBreakpoint: Waiting for breakpoint" + lb);
        synchronized (BP_WAIT_LOCK) {
            for (;;) {
                try {
                    BP_WAIT_LOCK.wait(timeout);
                } catch (InterruptedException ie) {
                }

                timeout = timeout - (System.currentTimeMillis() - start);
                CallStackFrame csf = debugger.getCurrentCallStackFrame();
                if (csf != null && lb.getPath().equals(csf.getFullname()) && lb.getLineNumber() == csf.getLineNumber()) {
                    System.out.println("    waitForBreakpoint: Got expected stop position");
                    return;
                } else if (timeout < 0) {
                    System.out.println("    waitForBreakpoint: Timeout exceeded");
                    fail("Timeout while waiting for breakpoint");
                    return;
                }
            }
        }
    }

    public <T> List<? extends T> lookup(String folder, Class<T> service) {
        return DebuggerManager.getDebuggerManager().lookup(folder, service);
    }

    @SuppressWarnings("unchecked")
    public <T> T lookupFirst(String folder, Class<T> service) {
        if (service == ProjectActionEvent.class) {
            if (pae == null) {
                conf = new TestConfiguration();
                pae = new ProjectActionEvent(project, ProjectActionEvent.Type.DEBUG, testapp, executable, null, null, false);
            }
            return (T) pae;
        } else {
            return DebuggerManager.getDebuggerManager().lookupFirst(folder, service);
        }
    }

    class TestConfiguration extends MakeConfiguration {
        public TestConfiguration() {
            super(testapp_dir, testproj, MakeConfiguration.TYPE_APPLICATION, CompilerSetManager.LOCALHOST);
        }
    }

    private class StateListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (STATE_WAIT_LOCK) {
                STATE_WAIT_LOCK.notifyAll();
            }
        }
    }

    private class StackListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (BP_WAIT_LOCK) {
                BP_WAIT_LOCK.notifyAll();
            }
        }
    }
}
