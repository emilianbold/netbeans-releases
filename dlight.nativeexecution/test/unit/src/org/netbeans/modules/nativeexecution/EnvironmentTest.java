/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Utilities;

/**
 *
 * @author ak119685
 */
public class EnvironmentTest extends NativeExecutionBaseTestCase {

    private final static String nonexistentVarName = "SOME_NEW_VAR";

    public EnvironmentTest(String name) {
        super(name);
    }

    public EnvironmentTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(EnvironmentTest.class);
    }

    @org.junit.Test
    public void testVarsLocal() throws Exception {
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();

//        _testVars(execEnv, true, true, null);
//        _testVars(execEnv, true, false, null);
        _testVars(execEnv, false, true, null);
        _testVars(execEnv, false, false, null);

        for (String terminalID : ExternalTerminalProvider.getSupportedTerminalIDs()) {
            ExternalTerminal terminal = ExternalTerminalProvider.getTerminal(execEnv, terminalID);
            if (terminal != null && terminal.isAvailable(execEnv)) {
                terminal = terminal.setPrompt("NO");
                _testVars(execEnv, false, true, terminal);

                // looks like starting in non-pty mode without
                // unbuffering doesn't work in this test..
                // TODO: investigate more...
//                _testVars(execEnv, false, false, terminal);
            }
        }
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testVars() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(execEnv);

//        _testVars(execEnv, true, true, null);
//        _testVars(execEnv, true, false, null);
        _testVars(execEnv, false, true, null);
        _testVars(execEnv, false, false, null);
    }

    public void _testVars(ExecutionEnvironment execEnv, boolean inPtyMode, boolean unbufferOutput, ExternalTerminal terminal) throws Exception {
        final String id = String.format("=== testVars [@ %s; ptyMode: %d, unbuffer: %d, term: %s] ===",
                execEnv.getDisplayName(),
                inPtyMode ? 1 : 0,
                unbufferOutput ? 1 : 0,
                terminal == null ? "null" : terminal.getID());

        System.out.println("=== START " + id);

        try {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            MacroMap env = npb.getEnvironment();

            String home = env.get("HOME");
            String path = env.get("PATH");

            assertNotNull("Initially environment should be filled with user's env", home);
            assertNotNull("Initially environment should be filled with user's env", path);

            assertNull("Will use var " + nonexistentVarName + " for testing. Should not exist in initial user's env", env.get(nonexistentVarName));
            env.put(nonexistentVarName, "test");

            NativeProcessBuilder npb2 = NativeProcessBuilder.newProcessBuilder(execEnv);
            assertNull("Adding env variabl " + nonexistentVarName + " to one builder should not have effect on other builders.", npb2.getEnvironment().get(nonexistentVarName));

            assertTrue("Touched variables must be marked for export", env.getExportVariablesSet().contains(nonexistentVarName));

            env.prependPathVariable("PATH", "additional path");
            assertTrue("PATH should start with 'additional path'", env.get("PATH").startsWith("additional path"));

            env.put(nonexistentVarName, "$" + nonexistentVarName + " value");

            String cmd = "echo " + nonexistentVarName + "=$" + nonexistentVarName + " && echo PATH=$PATH";

            String tmpFile = null;

            if (terminal != null) {
                File tmpFileFile = File.createTempFile("testVars", "result");
                tmpFile = tmpFileFile.getAbsolutePath();
                tmpFileFile.deleteOnExit();

                if (Utilities.isWindows()) {
                    tmpFile = WindowsSupport.getInstance().convertToShellPath(tmpFile);
                }

                System.out.println("Use tempfile: " + tmpFile);

                cmd = "(" + cmd + ") | tee " + tmpFile;
            }

            npb.setExecutable("/bin/sh").setArguments("-c", cmd);
            npb.setUsePty(inPtyMode);
            npb.unbufferOutput(unbufferOutput);
            if (terminal != null) {
                npb.useExternalTerminal(terminal);
            }

            Process p = npb.call();
            assertTrue(p.waitFor() == 0);

            List<String> result;

            if (terminal == null) {
                result = ProcessUtils.readProcessOutput(p);
            } else {
                // read result from tmpFile
                ExitStatus status = ProcessUtils.execute(execEnv, "cat", tmpFile);
                assertTrue(status.isOK());
                result = Arrays.asList(status.output.split("\n"));
            }

            Iterator<String> it = result.iterator();
            String line;

            line = it.next();
            System.out.println(line);
            assertEquals(nonexistentVarName + "=test value", line);
            line = it.next();
            System.out.println(line);
            assertEquals("PATH=" + env.get("PATH"), line);

        } finally {
            System.out.println("=== DONE " + id);
        }
    }
}
