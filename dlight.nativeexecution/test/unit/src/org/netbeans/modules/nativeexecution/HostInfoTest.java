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
package org.netbeans.modules.nativeexecution;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.util.Exceptions;
import static org.junit.Assert.*;

public class HostInfoTest extends NativeExecutionTest {

    private final static String password = ""; // NOI18N
    public HostInfoTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetHostInfo() {
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.createNew("ak119685", "tulos", 22); // NOI18N
        ConnectionManager cm = ConnectionManager.getInstance();
        
        try {
            cm.connectTo(execEnv, password.toCharArray(), false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            Exceptions.printStackTrace(ex);
        }

        HostInfo hi = HostInfoUtils.getHostInfo(execEnv, true);
        System.out.println("Tempdir is " + hi.getTempDir()); // NOI18N
    }

    /**
     * Test of getOS method, of class HostInfo.
     */
//    @Test
    public void testGetOS() {
        System.out.println("getOS"); // NOI18N
        String expResult = "SunOS"; // NOI18N
        String result;
        expResult = "SunOS"; // NOI18N
        HostInfo hi = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.createNew("ak119685", "127.0.0.1"), false); // NOI18N
        result = hi.getOS().getName();
        System.out.printf("Expected result is %s, actual result is %s\n", expResult, result); // NOI18N
        assertEquals(expResult, result);
//
//        try {
//            result = HostInfo.getOS(new ExecutionEnvironment("ak119685", "129.159.127.252"));
//            assertEquals(expResult, result);
//        } catch (HostNotConnectedException ex) {
//            Exceptions.printStackTrace(ex);
//            fail("Wrong exception");
//        }
//
//        try {
//            result = HostInfo.getOS(new ExecutionEnvironment("ak119685", "129.159.127.13"));
//            fail("Exception expected");
//        } catch (HostNotConnectedException ex) {
//            System.out.println("Expected exception");
//        }
//
//        try {
////    NativeTask nt = new NativeTask(host, "ak119685", "/bin/ls", null);
////    nt.submit();
////    System.out.println("ls exit status is " + nt.exitValue());
////    System.out.println("ls output is " + nt.getOutput());
////      host = "129.159.127.13";
////    allowUserInteraction = false;
//            expResult = "SunOS";
//            result = HostInfo.getOS(new ExecutionEnvironment("ak119685", "129.159.127.13"));
//            assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
////    fail("The test case is a prototype.");
//        } catch (HostNotConnectedException ex) {
//            Exceptions.printStackTrace(ex);
//            fail("Wrong exception");
//        }

    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
    }

//    @Test
    public void testFileExists() {
        String fname = "/etc/passwd"; // NOI18N
        boolean result;
        boolean expResult;

        ExecutionEnvironment ee = ExecutionEnvironmentFactory.getLocal();
//        try {
//            CharArrayWriter writer = new CharArrayWriter();
//
//            NativeTaskConfig ntc = new NativeTaskConfig(ee, "/bin/ls").setArguments("-la");
//            NativeProcessBuilder npb = new NativeProcessBuilder(ntc, null);
//            ExecutionDescriptor descr = new ExecutionDescriptor().outLineBased(true).outProcessorFactory(new InputRedirectorFactory(writer));
//            ExecutionService service = ExecutionService.newService(npb, descr, "xxx");
//
//            Future<Integer> fr = service.run();
//            fr.get();
//            System.out.println(writer.toString());
//
//        } catch (Exception ex) {
//            Exceptions.printStackTrace(ex);
//        }

        try {
            fname = "/etc/passwd1"; // NOI18N
            result = HostInfoUtils.fileExists(ee, fname);
            expResult = false;
            System.out.println(fname + (result == false ? " doesn't exist" : " exists")); // NOI18N
            assertEquals(expResult, result);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            fail("Wrong exception"); // NOI18N
        }

        try {
            fname = "/etc/passwd"; // NOI18N
            result = HostInfoUtils.fileExists(ee, fname);
            expResult = true;
            System.out.println(fname + (result == false ? " doesn't exist" : " exists")); // NOI18N
            assertEquals(expResult, result);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            fail("Wrong exception"); // NOI18N
        }
//
//        try {
//            fname = "/etc/passwd";
//            result = HostInfo.fileExists(new ExecutionEnvironment("ak119685", "129.159.127.13"), fname);
//            fail("Exception expected");
//        } catch (HostNotConnectedException ex) {
//            System.out.println("Expected exception");
//        }
//
//        CharArrayWriter taskOutput = new CharArrayWriter();
//        NativeTask nt = new NativeTask(new ExecutionEnvironment("ak119685", "localhost"), "/bin/ls", null);
//        nt.redirectOutTo(taskOutput);
//        nt.submit(true, false);
//
//        Integer taskResult = -1;
//
//        try {
//            taskResult = nt.get();
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (ExecutionException ex) {
//            Exceptions.printStackTrace(ex);
//        }

//        System.out.println("ls exit status is " + taskResult);
//        System.out.println("ls output is " + taskOutput.toString());
//
//        HostInfo.getPlatformPath(new ExecutionEnvironment(null, null, 22));
//
//        for (int i = 0; i < 10; i++) {
//            System.out.println(".. " + i + " ..");
//            try {
//                result = HostInfo.fileExists(new ExecutionEnvironment(null, null, 22), fname);
//                expResult = true;
//                assertEquals(expResult, result);
//            } catch (HostNotConnectedException ex) {
//                Exceptions.printStackTrace(ex);
//                fail("Wrong exception");
//            }
//        }
//
//        System.out.println("");
//
//        for (int i = 0; i < 10; i++) {
//            System.out.println(".. " + i + " ..");
//            try {
//                fname = "/etc/passwd1";
//                result = HostInfo.fileExists(new ExecutionEnvironment("ak119685", "localhost"), fname);
//                expResult = false;
//                assertEquals(expResult, result);
//            } catch (HostNotConnectedException ex) {
//                Exceptions.printStackTrace(ex);
//                fail("Wrong exception");
//            }
//        }

    }

    /**
     * Test of isLocalhost method, of class HostInfo.
     */
//    @Test
    public void testIsLocalhost() {
        System.out.println("isLocalhost"); // NOI18N
        String host = "localhost"; // NOI18N
        boolean expResult = true;
        boolean result = HostInfoUtils.isLocalhost(host);
        assertEquals(expResult, result);
    }

}

