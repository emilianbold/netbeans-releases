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

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger.State;
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
    private String path = "";
    protected GdbDebugger debugger;
    protected GdbProxy gdb;
    protected static final Logger tlog = Logger.getLogger("gdb.testlogger"); // NOI18N

    public GdbTestCase(String name) {
        super(name);
        System.setProperty("gdb.testsuite", "true");
        tlog.setLevel(Level.FINE);
        String workdir = System.getProperty("nbjunit.workdir"); // NOI18N
        if (workdir != null && workdir.endsWith("/build/test/unit/work")) { // NOI18N
            testapp_dir = workdir.substring(0, workdir.length() - 21) + "/build/testapps"; // NOI18N
            File dir = new File(testapp_dir);
            if (!dir.exists()) {
                assert false : "Missing testapps directory";
            }
        }
    }

    protected void startTest(String testapp) {
        this.testapp = testapp;
        System.out.println("\n" + testapp); // NOI18N
    }

    protected void tlog(String msg) {
        System.out.println("    " + testapp + ": " + msg); // NOI18N
    }

    protected void startDebugger(String testproj, String path) {
        this.testproj = testproj;
        this.path = testapp_dir + '/' + path;
        debugger = new GdbDebugger(this);
        String[] denv = new String[] { "GDBUnitTest=True" };
        try {
            gdb = new GdbProxy(debugger, "gdb", denv, testapp_dir, null, "");
        } catch (Exception ex) {
            gdb = null;
        }
        assert gdb != null;

        try {
            debugger.testSuiteInit(gdb);
            gdb.gdb_show("language"); // NOI18N
            gdb.gdb_set("print repeat", "10"); // NOI18N
            gdb.file_exec_and_symbols(path);
            gdb.break_insert_temporary("main"); // NOI18N
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doFinish() {
        project = null;
        pae = null;
        debugger = null;
        testapp = null;
        path = null;
    }

    protected void waitForStateChange(State state, int max) {
        int i = 0;

        do {
            System.out.println("    waitForStateChange: State is " + debugger.getState() + " [waiting for " + state + "]");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
            }
        } while (debugger.getState() != state && i++ < max);
        if (debugger.getState() == state) {
            System.out.println("    waitForStateChange: Got expected change to " + state);
        } else {
            System.out.println("    waitForStateChange: Timeout exceeded");
        }
    }

    protected void waitForStateChange(State state) {
        waitForStateChange(state, 5);
    }

    public <T> List<? extends T> lookup(String folder, Class<T> service) {
        return DebuggerManager.getDebuggerManager().lookup(folder, service);
    }

    @SuppressWarnings("unchecked")
    public <T> T lookupFirst(String folder, Class<T> service) {
        if (service == ProjectActionEvent.class) {
            if (pae == null) {
                conf = new TestConfiguration();
                pae = new ProjectActionEvent(project, ProjectActionEvent.Type.DEBUG, testapp, path, null, null, false);
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
}
