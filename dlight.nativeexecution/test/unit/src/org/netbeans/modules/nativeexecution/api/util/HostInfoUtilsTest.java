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

import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.Platform;
import org.openide.util.Utilities;

/**
 *
 * @author Vladimir Kvashin
 */
public class HostInfoUtilsTest extends NbTestCase {

    public HostInfoUtilsTest(String name) {
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

    @Test
    public void testGetPlatformLocal() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        Platform platform = HostInfoUtils.getPlatform(env);
        String os_name = System.getProperty("os.name").toLowerCase();
        switch(platform.getOSType()) {
            case GENUNIX:
                assertTrue(Utilities.isUnix());
                break;
            case LINUX:
                assertTrue(os_name.contains("linux"));
                break;
            case MACOSX:
                assertTrue(os_name.contains("mac") && Utilities.isMac());
                break;
            case SOLARIS:
                assertTrue(os_name.contains("sunos"));
                break;
            case WINDOWS:
                assertTrue(os_name.contains("windows") && Utilities.isWindows());
                break;
        }
        String os_arch = System.getProperty("os.arch");
        switch (platform.getHardwareType()) {
            case SPARC:
                assertTrue(os_arch.contains("spark"));
                break;
            case X86:
                assertTrue(os_arch.contains("86"));
                break;
        }
    }

//    @Test
//    public void testGetPlatformRemote() throws Exception {
//        testGetPlatform("my_host", "my_login", 22, "pwd", "SOLARIS", "86");
//    }

    private void testGetPlatform(String host, String user, int port, String passwd, 
            String osTypeShouldContain, String hardwareTypeShouldContain) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironmentFactory.createNew(user, host, port);
        ConnectionManager.getInstance().connectTo(env, passwd.toCharArray(), true);
        Platform platform = HostInfoUtils.getPlatform(env);
        assertTrue(platform.getHardwareType().toString().contains(hardwareTypeShouldContain));
        assertTrue(platform.getOSType().toString().contains(osTypeShouldContain));
    }

    @Test
    public void testSearchFile() throws Exception {
        System.out.println("Test searchFile()"); // NOI18N
        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        String result = null;

        result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "/bin", "/usr/bin"), "rm", true); // NOI18N
        assertNotNull(result);

        result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "/bin", "/usr/bin"), "rm", false); // NOI18N
        assertNotNull(result);

        result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath"), "ls", true); // NOI18N
        assertNotNull(result);

        result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath"), "ls", false); // NOI18N
        assertNull(result);
    }

    
//    private void testLoggers() {
//        Logger logger = Logger.getAnonymousLogger();
//        logger.setLevel(Level.FINEST);
//        logger.fine("fff");
//        logger.finest("FFFFfff");
//    }
}
