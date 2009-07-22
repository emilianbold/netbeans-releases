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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
public class HostInfoUtilsTest extends NativeExecutionBaseTestCase {

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(HostInfoUtilsTest.class);
    }

    public HostInfoUtilsTest(String name) {
        super(name);
    }

    public HostInfoUtilsTest(String name, ExecutionEnvironment testEnv) {
        super(name, testEnv);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setHandlers();
    }

    private static void setHandlers() {
        // FIXIP: [VK] a dirty hack that allows to get all messages during test run;
        // otherwise FINE and FINEST are never shown, even if correspondent loggers
        // has Level.FINEST or Level.ALL
        Handler handler = new Handler() {

            @Override
            public void close() throws SecurityException {
            }

            @Override
            public void flush() {
            }

            @Override
            public void publish(LogRecord record) {
                System.err.printf("%s [%s]: %s\n", record.getLevel(), record.getLoggerName(), record.getMessage());
            }
        };
        Logger parent = Logger.getAnonymousLogger().getParent();
        final Handler[] handlers = parent.getHandlers();
        for (Handler h : handlers) {
            parent.removeHandler(h);
        }
        parent.addHandler(handler);
    }

    @After
    @Override
    public void tearDown() {
    }

//    @ForAllEnvironments(section = "dlight.nativeexecution.hostinfo")
//    public void testGetOS() {
//        try {
//            System.out.println(HostInfoUtils.getHostInfo(getTestExecutionEnvironment()).getOS().getName());
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (CancellationException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
    /**
     * This test assures that only first call to getHostInfo does the job.
     * So any subsequent call should return cached result.
     * Also it assures that HostInfoUtils.isHostInfoAvailable() works fast and
     * correct.
     *
     * Note: in case of some error during getHostInfo() the result should not be
     * stored in a cache. So subsequent calls WILL initiate data re-fetching.
     */
    public void testMultipleGetInfo() {
        System.setProperty("dlight.nativeexecution.SlowHostInfoProviderEnabled", "true"); // NOI18N

        final ExecutionEnvironment local = ExecutionEnvironmentFactory.getLocal();

        try {
            // Reset hosts data - it may be already collected in previous tests

            HostInfoUtils.resetHostsData();

            assertFalse(HostInfoUtils.isHostInfoAvailable(local));

            Thread fetchingThread = new Thread(new Runnable() {

                public void run() {
                    try {
                        HostInfoUtils.getHostInfo(local);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (CancellationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });

            fetchingThread.start();

            // As SlowHostInfoProviderEnabled we know that fetching info will
            // take at least 3 seconds. From the other hand it should not take
            // more than 10 seconds (as we are on localhost). 

            int count = 20;

            while (!HostInfoUtils.isHostInfoAvailable(local)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }

                if (--count == 0) {
                    break;
                }
            }

            try {
                fetchingThread.interrupt();
                fetchingThread.join();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }

            assertTrue("HostInfo MUST be available already (10 seconds passed)", count > 0); // NOI18N
            assertTrue("HostInfo cannot be available already (only " + ((20 - count) * 2) + " seconds passed)", count < 15); // NOI18N

            long startTime = System.currentTimeMillis();
            HostInfoUtils.dumpInfo(HostInfoUtils.getHostInfo(local), System.out);
            long endTime = System.currentTimeMillis();

            assertTrue((endTime - startTime) / 1000 < 2);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            System.setProperty("dlight.nativeexecution.SlowHostInfoProviderEnabled", "false"); // NOI18N
        }
    }

//    public void testGetInfoInterrupting() {
//        System.setProperty("dlight.nativeexecution.SlowHostInfoProviderEnabled", "true"); // NOI18N
//
//        final ExecutionEnvironment local = ExecutionEnvironmentFactory.getLocal();
//        try {
//            HostInfoUtils.resetHostsData();
//
//            assertFalse(HostInfoUtils.isHostInfoAvailable(local));
//
//            Thread fetchingThread = new Thread(new Runnable() {
//
//                public void run() {
//                    try {
//                        HostInfoUtils.getHostInfo(local);
//                    } catch (IOException ex) {
//                        Exceptions.printStackTrace(ex);
//                    } catch (CancellationException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//                }
//            });
//
//            fetchingThread.start();
//
//            fetchingThread.interrupt();
//
//            try {
//                fetchingThread.join();
//            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//
//            System.out.println(HostInfoUtils.isHostInfoAvailable(local));
//            try {
//                HostInfoUtils.dumpInfo(HostInfoUtils.getHostInfo(local), System.out);
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            } catch (CancellationException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//
//        } finally {
//            System.setProperty("dlight.nativeexecution.SlowHostInfoProviderEnabled", "false"); // NOI18N
//        }
//    }

//    @Test
//    public void testGetPlatformLocal() throws Exception {
//        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
//        HostInfo info = HostInfoUtils.getHostInfo(env);
//        String os_name = System.getProperty("os.name").toLowerCase();
//
//        switch(platform.getOSType()) {
//            case GENUNIX:
//                assertTrue(Utilities.isUnix());
//                break;
//            case LINUX:
//                assertTrue(os_name.contains("linux"));
//                break;
//            case MACOSX:
//                assertTrue(os_name.contains("mac") && Utilities.isMac());
//                break;
//            case SOLARIS:
//                assertTrue(os_name.contains("sunos"));
//                break;
//            case WINDOWS:
//                assertTrue(os_name.contains("windows") && Utilities.isWindows());
//                break;
//        }
//        String os_arch = System.getProperty("os.arch");
//        switch (platform.getHardwareType()) {
//            case SPARC:
//                assertTrue(os_arch.contains("sparc"));
//                break;
//            case X86:
//                assertTrue(os_arch.contains("86"));
//                break;
//        }
//    }

//    @Test
//    public void testGetPlatformRemote() throws Exception {
//        testGetPlatform("my_host", "my_login", 22, "pwd", "SOLARIS", "86");
//    }

//    private void testGetPlatform(String host, String user, int port, String passwd,
//            String osTypeShouldContain, String hardwareTypeShouldContain) throws Exception {
//        ExecutionEnvironment env = ExecutionEnvironmentFactory.createNew(user, host, port);
//        ConnectionManager.getInstance().connectTo(env, passwd.toCharArray(), true);
//        Platform platform = HostInfoUtils.getPlatform(env);
//        assertTrue(platform.getHardwareType().toString().contains(hardwareTypeShouldContain));
//        assertTrue(platform.getOSType().toString().contains(osTypeShouldContain));
//    }
    @ForAllEnvironments(section = "dlight.nativeexecution.hostinfo")
    public void testRemoteSearchFile() throws Exception {

        System.out.println("Test testRemoteSearchFile()"); // NOI18N
        ExecutionEnvironment env = getTestExecutionEnvironment();
        HostInfo info = HostInfoUtils.getHostInfo(env);

        assertNotNull(info);

        HostInfoUtils.dumpInfo(info, System.out);
        assertNotNull(info.getShell());

        String testDir = info.getTempDir() + "/some dir"; // NOI18N
        String testFileName = "some (" + new Random().nextInt() + ") file"; // NOI18N
        CommonTasksSupport.mkDir(env, testDir, null);
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.addEnvironmentVariable("PATH", "$PATH:/bin:/usr/bin"); // NOI18N
        npb.setExecutable("touch").setArguments(testDir + "/" + testFileName); // NOI18N
        npb.call().waitFor();

        System.out.println("Use file '" + testFileName + "' in '" + testDir + "' for testing"); // NOI18N
        try {
            String result = null;
            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrong Path", testDir, "/usr/bin"), testFileName, true); // NOI18N
            assertNotNull(result);
            assertEquals(result, testDir + "/" + testFileName); // NOI18N

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "/bin", "/usr/bin"), "rm", true); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "/bin", "/usr/bin"), "rm", false); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath"), "ls", true); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath"), "ls", false); // NOI18N
            assertNull(result);
        } finally {
            CommonTasksSupport.rmDir(env, testDir, true, null);
        }

    }

    public void testUnixSearchFile() throws Exception {
        System.out.println("Test testUnixSearchFile()"); // NOI18N

        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        HostInfo info = HostInfoUtils.getHostInfo(env);

        assertNotNull(info);

        if (info.getShell() == null) {
            return;
        }

        File testDir = new File(info.getTempDirFile(), "some dir"); // NOI18N
        testDir.mkdir();
        File testFile = File.createTempFile("some (", ") file", testDir); // NOI18N
        testFile.createNewFile();

        System.out.println("Use file '" + testFile.getAbsolutePath() + "' for testing"); // NOI18N

        try {
            String result = null;
            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrong Path", testDir.getAbsolutePath(), "/usr/bin"), testFile.getName(), true); // NOI18N
            assertNotNull(result);

            String expectedPath = testFile.getAbsolutePath();

            if (info.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                expectedPath = WindowsSupport.getInstance().convertToShellPath(result);
            }

            assertEquals(result, expectedPath);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "/bin", "/usr/bin"), "rm", true); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "/bin", "/usr/bin"), "rm", false); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath"), "ls", true); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath"), "ls", false); // NOI18N
            assertNull(result);
        } finally {
            testFile.delete();
            testDir.delete();
        }
    }

    public void testWindowsSearchFile() throws Exception {
        System.out.println("Test testWindowsSearchFile()"); // NOI18N

        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        HostInfo info = HostInfoUtils.getHostInfo(env);

        assertNotNull(info);

        if (info.getOSFamily() != HostInfo.OSFamily.WINDOWS) {
            return;
        }

        File testDir = new File(info.getTempDirFile(), "some dir"); // NOI18N
        testDir.mkdir();
        File testFile = File.createTempFile("some (", ") file", testDir); // NOI18N
        testFile.createNewFile();

        System.out.println("Use file '" + testFile.getAbsolutePath() + "' for testing"); // NOI18N
        try {

            String result;

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "c:\\Windows", testDir.getAbsolutePath()), testFile.getName(), true); // NOI18N
            assertNotNull(result);

            if (info.getShell() != null) {
                assertEquals(result, WindowsSupport.getInstance().convertToShellPath(testFile.getCanonicalPath()));
            } else {
                assertEquals(result, testFile.getCanonicalPath());
            }

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "c:\\Windows", "c:\\Windows\\system32"), "cmd.exe", true); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "c:\\Windows"), "cmd.exe", true); // NOI18N
            assertNotNull(result);

            if (info.getShell() != null) {
                result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", WindowsSupport.getInstance().convertToShellPath("c:\\Windows\\system32")), "cmd.exe", false); // NOI18N
                assertNotNull(result);
            }

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "c:\\Windows"), "cmd.exe", false); // NOI18N
            assertNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath"), "wrongFile", true); // NOI18N
            assertNull(result);
        } finally {
            testFile.delete();
            testDir.delete();
        }
    }

//    private void testLoggers() {
//        Logger logger = Logger.getAnonymousLogger();
//        logger.setLevel(Level.FINEST);
//        logger.fine("fff");
//        logger.finest("FFFFfff");
//    }
}
