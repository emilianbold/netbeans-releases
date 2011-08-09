/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.spi;

import java.net.ServerSocket;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import java.util.concurrent.Future;
import org.netbeans.modules.glassfish.common.Commands;
import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.glassfish.common.CommandRunner;
import java.util.HashMap;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;

/**
 *
 * @author vkraemer
 */
public class UtilsTest extends NbTestCase {

    public UtilsTest(String testName) {
        super(testName);
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


    /**
     * Test of getHttpListenerProtocol method, of class Utils.
     */
    @Test
    public void testGetHttpListenerProtocol() {
        System.out.println("getHttpListenerProtocol");
        String hostname = "glassfish.java.net";
        int port = 443;
        String expResult = "https";
        String result = Utils.getHttpListenerProtocol(hostname, port);
        assertEquals(expResult, result);
        port = 80;
        expResult = "http";
        result = Utils.getHttpListenerProtocol(hostname, port);
        assertEquals(expResult, result);
    }

    /**
     * Test of isSecurePort method, of class Utils.
     */
    @Test
    public void testIsSecurePort() throws Exception {
        System.out.println("isSecurePort");
        String hostname = "glassfish.java.net";
        int port = 443;
        boolean expResult = true;
        boolean result = Utils.isSecurePort(hostname, port);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetFileFromPattern() throws Exception {
        System.out.println("getFileFromPattern");
        File f;
        try {
            f = Utils.getFileFromPattern(null, null);
            assertNull(f);
        } catch (AssertionError ae) {
            // I expect this
        }
        try {
            f = Utils.getFileFromPattern("", null);
            assertNull(f);
        } catch (AssertionError ae) {
            // I expect this
        }
        File dataDir = getDataDir();
        try {
            f = Utils.getFileFromPattern(null, dataDir);
            assertNull(f);
        } catch (AssertionError ae) {
            // I expect this
        }
        f = Utils.getFileFromPattern("", dataDir);
        assertNull(f);
        f = Utils.getFileFromPattern("", new File(dataDir, "nottaDir"));
        assertNull(f);
        f = Utils.getFileFromPattern("nottaDir", dataDir);
        assertNotNull(f);
        f = Utils.getFileFromPattern("nottaDir"+Utils.VERSIONED_JAR_SUFFIX_MATCHER, dataDir);
        assertNotNull(f);
        f = Utils.getFileFromPattern("nottaDir.jar", dataDir);
        assertNull(f);
        f = Utils.getFileFromPattern("subdir/nottaDir"+Utils.VERSIONED_JAR_SUFFIX_MATCHER, dataDir);
        assertNotNull(f);
        f = Utils.getFileFromPattern("subdir/nottaDir.jar", dataDir);
        assertNull(f);
        f = Utils.getFileFromPattern("nottasubdir/nottaDir"+Utils.VERSIONED_JAR_SUFFIX_MATCHER, dataDir);
        assertNull(f);
    }
    /**
     * Test of sanitizeName method, of class Commands.
     */
    @Test
    public void testSanitizeName() {
        System.out.println("sanitizeName");
        String name = "aa";
        String expResult = "aa";
        String result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        name = "1a";
        expResult = "1a";
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        name = "_a";
        expResult = "_a";
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        name = ".a";
        expResult = "_.a";
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        name = "foo(bar)";
        expResult = "_foo_bar_";
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        name = "foo((bar)";
        expResult = "_foo__bar_";
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        name = ".a()";
        expResult = "_.a__";
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        name = null;
        expResult = null;
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
    }

    public static void main(String... args) throws InterruptedException, ExecutionException {
        for (int i = 0 ; i < 2000 ; i++) {
            String hostname =  //"127.0.0.1";
                 "10.229.117.91";
            int port = 4848;
            if (false)
                System.out.println(Utils.getHttpListenerProtocol(hostname, port));
            else {
                Map<String,String> ip = new HashMap<String,String>();
                ip.put(GlassfishModule.HOSTNAME_ATTR, hostname);
                ip.put(GlassfishModule.ADMINPORT_ATTR, port+"");
                CommandRunner cr = new CommandRunner(Utils.isLocalPortOccupied(port), null,
                        Collections.unmodifiableMap(ip), null);
                Future<OperationState> x = null;
                Commands.LocationCommand lc = new Commands.LocationCommand();
                x = cr.execute(lc);
                System.out.println(x.get() == OperationState.COMPLETED);
                System.out.println(lc.getDomainRoot()+":"+lc.getInstallRoot());
                System.out.println(lc.getServerMessage());
                System.out.println(lc.getSrc());

                cr = new CommandRunner(Utils.isLocalPortOccupied(port), null, Collections.unmodifiableMap(ip), null);
                ServerCommand.GetPropertyCommand gpc = new ServerCommand.GetPropertyCommand("*.server-config.*.http-listener-1.port");
                x = cr.execute(gpc);
                System.out.println(x.get() == OperationState.COMPLETED);
                System.out.println(gpc.getData());
                System.out.println(gpc.getServerMessage());
                System.out.println(gpc.getSrc());
            }
        }
        System.exit(0);
    }

    @Test
    public void testIsLocalPortOccupied() throws IOException {
        System.out.println("isLocalPortOccupied");
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        assert Utils.isLocalPortOccupied(port) : "the port is not occupied?";
        ss.close();
        assert !Utils.isLocalPortOccupied(port) : "the port is occupied?";
    }
}