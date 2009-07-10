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
package org.netbeans.modules.nativeexecution.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import static org.junit.Assert.*;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.openide.util.Exceptions;

public class NativeProcessBuilderTest extends NativeExecutionBaseTestCase {

    public NativeProcessBuilderTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() {
    }

    @After
    @Override
    public void tearDown() {
    }

    /**
     * Test of newProcessBuilder method, of class NativeProcessBuilder.
     */
//    @Test
//    public void testNewProcessBuilder() {
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of newLocalProcessBuilder method, of class NativeProcessBuilder.
     */
    @Test
    public void testNewLocalProcessBuilder() throws IOException {
        System.out.println("newLocalProcessBuilder"); // NOI18N

        NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
        assertNotNull(npb);
    }

    /**
     * Test of setExecutable method, of class NativeProcessBuilder.
     */
    @Test
    public void testSetExecutable() throws CancellationException, IOException {
        System.out.println("setExecutable: ..."); // NOI18N
        ExecutionEnvironment ee;

        ee = ExecutionEnvironmentFactory.getLocal();
        System.out.println("... test on a localhost [" + ee.toString() + "] ..."); // NOI18N
        testSetExecutable(ee);

        String[] mspecs = new String[]{
            "intel-S2", // NOI18N
            "intel-Linux", // NOI18N
        };

        for (String mspec : mspecs) {
            ee = NativeExecutionTestSupport.getTestExecutionEnvironment(mspec); // NOI18N
            if (ee == null) {
                System.out.println("... skip testing on not configured " + mspec + " ... "); // NOI18N
            } else {
                System.out.println("... test on " + mspec + " [" + ee.toString() + "] ..."); // NOI18N
                testSetExecutable(ee);
            }
        }
    }

    private void testSetCommandLine(ExecutionEnvironment ee) throws IOException {
        if (ee == null) {
            System.out.println("null ExecutionEnvironment - skip"); // NOI18N
            return;
        }

        String tmpDir = HostInfoUtils.getHostInfo(ee).getTempDir();
        String testDir = tmpDir + "/path with a space"; // NOI18N

        System.out.println("Do remove directory " + testDir); // NOI18N

        try {
            CommonTasksSupport.rmDir(ee, testDir, true, null).get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println("Do create directory " + testDir); // NOI18N

        // as we use setCommandLine we need to take care of escaping,
        // quoting, etc...

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(ee);
        npb.setCommandLine("mkdir \"" + testDir + "\""); // NOI18N
        NativeProcess process = npb.call();

        int result = -1;

        try {
            result = process.waitFor();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        assertEquals(0, result);

        if (ee.isLocal()) {
            File dir = new File(testDir);
            assertTrue(dir.exists() && dir.isDirectory());
        } else {
            assertTrue(HostInfoUtils.fileExists(ee, testDir));
        }

        System.out.println("Copy file /bin/ls to '" + testDir + "' with name 'copied ls'"); // NOI18N

        npb = NativeProcessBuilder.newProcessBuilder(ee);
        npb.setCommandLine("cp /bin/ls \"" + testDir + "/copied ls\""); // NOI18N

        try {
            result = npb.call().waitFor();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        assertEquals(0, result);

        System.out.println("Invoke copied ls and ensure that it works"); // NOI18N

        npb = NativeProcessBuilder.newProcessBuilder(ee);
        npb.setCommandLine("\"" + testDir + "/copied ls\" \"" + testDir + "\""); // NOI18N

        NativeProcess ls = npb.call();
        List<String> out = ProcessUtils.readProcessOutput(ls);

        boolean found = false;

        for (String line : out) {
            if ("copied ls".equals(line)) { // NOI18N
                found = true;
                break;
            }
        }

        assertTrue(found);

        System.out.println("... OK ..."); // NOI18N
    }

    private void testSetExecutable(ExecutionEnvironment ee) throws IOException {
        if (ee == null) {
            System.out.println("null ExecutionEnvironment - skip"); // NOI18N
            return;
        }

        String tmpDir = HostInfoUtils.getHostInfo(ee).getTempDir();
        String testDir = tmpDir + "/path with a space"; // NOI18N

        System.out.println("Do remove directory " + testDir); // NOI18N

        try {
            CommonTasksSupport.rmDir(ee, testDir, true, null).get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println("Do create directory " + testDir); // NOI18N

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(ee);
        npb.setExecutable("mkdir").setArguments(testDir); // NOI18N
        NativeProcess process = npb.call();

        int result = -1;

        try {
            result = process.waitFor();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        assertEquals(0, result);

        if (ee.isLocal()) {
            File dir = new File(testDir);
            assertTrue(dir.exists() && dir.isDirectory());
        } else {
            assertTrue(HostInfoUtils.fileExists(ee, testDir));
        }

        System.out.println("Copy file /bin/ls to '" + testDir + "' with name 'copied ls'"); // NOI18N

        npb = NativeProcessBuilder.newProcessBuilder(ee);
        npb.setExecutable("cp").setArguments("/bin/ls", testDir + "/copied ls"); // NOI18N

        try {
            result = npb.call().waitFor();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        assertEquals(0, result);

        System.out.println("Invoke copied ls and ensure that it works"); // NOI18N

        npb = NativeProcessBuilder.newProcessBuilder(ee);
        npb.setExecutable(testDir + "/copied ls"); // NOI18N
        npb.setArguments(testDir);

        NativeProcess ls = npb.call();
        List<String> out = ProcessUtils.readProcessOutput(ls);
        boolean found = false;

        for (String line : out) {
            if ("copied ls".equals(line)) { // NOI18N
                found = true;
                break;
            }
        }

        assertTrue(found);

        System.out.println("... OK ..."); // NOI18N
    }

    /**
     * Test of setCommandLine method, of class NativeProcessBuilder.
     */
    @Test
    public void testSetCommandLine() throws IOException {
        System.out.println("setCommandLine: ..."); // NOI18N
        ExecutionEnvironment ee;

        ee = ExecutionEnvironmentFactory.getLocal();
        System.out.println("... test on localhost [" + ee.toString() + "] ..."); // NOI18N
        testSetCommandLine(ee);

        String[] mspecs = new String[]{
            "intel-S2", // NOI18N
            "intel-Linux", // NOI18N
        };

        for (String mspec : mspecs) {
            ee = NativeExecutionTestSupport.getTestExecutionEnvironment(mspec); // NOI18N
            if (ee == null) {
                System.out.println("... skip testing on not configured " + mspec + " ... "); // NOI18N
            } else {
                System.out.println("... test on " + mspec + " [" + ee.toString() + "] ..."); // NOI18N
                testSetCommandLine(ee);
            }
        }
    }
//
//    /**
//     * Test of addNativeProcessListener method, of class NativeProcessBuilder.
//     */
////    @Test
//    public void testAddNativeProcessListener() {
//        System.out.println("addNativeProcessListener");
//        ChangeListener listener = null;
//        NativeProcessBuilder instance = null;
//        NativeProcessBuilder expResult = null;
//        NativeProcessBuilder result = instance.addNativeProcessListener(listener);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of call method, of class NativeProcessBuilder.
//     */
////    @Test
//    public void testCall() throws Exception {
//        System.out.println("call");
//        NativeProcessBuilder instance = null;
//        NativeProcess expResult = null;
//        NativeProcess result = instance.call();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setWorkingDirectory method, of class NativeProcessBuilder.
//     */
////    @Test
//    public void testSetWorkingDirectory() {
//        System.out.println("setWorkingDirectory");
//        String workingDirectory = "";
//        NativeProcessBuilder instance = null;
//        NativeProcessBuilder expResult = null;
//        NativeProcessBuilder result = instance.setWorkingDirectory(workingDirectory);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addEnvironmentVariable method, of class NativeProcessBuilder.
//     */
////    @Test
//    public void testAddEnvironmentVariable() {
//        System.out.println("addEnvironmentVariable");
//        String name = "";
//        String value = "";
//        NativeProcessBuilder instance = null;
//        NativeProcessBuilder expResult = null;
//        NativeProcessBuilder result = instance.addEnvironmentVariable(name, value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addEnvironmentVariables method, of class NativeProcessBuilder.
//     */
////    @Test
//    public void testAddEnvironmentVariables() {
//        System.out.println("addEnvironmentVariables");
//        Map<String, String> envs = null;
//        NativeProcessBuilder instance = null;
//        NativeProcessBuilder expResult = null;
//        NativeProcessBuilder result = instance.addEnvironmentVariables(envs);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setArguments method, of class NativeProcessBuilder.
//     */
////    @Test
//    public void testSetArguments() {
//        System.out.println("setArguments");
//        String[] arguments = null;
//        NativeProcessBuilder instance = null;
//        NativeProcessBuilder expResult = null;
//        NativeProcessBuilder result = instance.setArguments(arguments);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of useExternalTerminal method, of class NativeProcessBuilder.
//     */
////    @Test
//    public void testUseExternalTerminal() {
//        System.out.println("useExternalTerminal");
//        ExternalTerminal terminal = null;
//        NativeProcessBuilder instance = null;
//        NativeProcessBuilder expResult = null;
//        NativeProcessBuilder result = instance.useExternalTerminal(terminal);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of unbufferOutput method, of class NativeProcessBuilder.
//     */
////    @Test
//    public void testUnbufferOutput() {
//        System.out.println("unbufferOutput");
//        boolean unbuffer = false;
//        NativeProcessBuilder instance = null;
//        NativeProcessBuilder expResult = null;
//        NativeProcessBuilder result = instance.unbufferOutput(unbuffer);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}