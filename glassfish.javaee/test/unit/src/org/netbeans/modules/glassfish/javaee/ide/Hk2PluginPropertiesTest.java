/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee.ide;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

/**
 *
 * @author vbk
 */
public class Hk2PluginPropertiesTest {

    public Hk2PluginPropertiesTest() {
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
     * Test of isRunning method, of class Hk2PluginProperties.
     */
    @Test
    public void testIsRunning() throws IOException {
        String host = "10.229.117.91";
        //localhost";
        if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
            System.out.println("isRunning");
            ServerSocket ss = new ServerSocket(0);
            String port = ss.getLocalPort() + "";
            boolean expResult = true;
            boolean result = Hk2PluginProperties.isRunning(host, port);
            assertEquals(expResult, result);
            ss.close();
            result = Hk2PluginProperties.isRunning(host, port);
            expResult = false;
            assertEquals(expResult, result);
            port = "4848";
            try {
                ss = new ServerSocket(Integer.parseInt(port));
            } catch (IOException ioe) {
                // it looks like there is an app server running... let's pound on it
                System.out.println("isRunning "+host+":4848");
                poundOnIt(host, port, expResult, result);
            }
        } else {
            System.out.println("isRunning "+host+":4848");
            poundOnIt(host, "4848", true, true);
        }
    }

    private void poundOnIt(String host, String port, boolean expResult, boolean result) {
        result = Hk2PluginProperties.isRunning(host, port);
        expResult = true;
        assertEquals(expResult, result);
        for (int i = 0; i < 4000; i++) {
            Hk2PluginProperties.isRunning(host, port);
        }
    }


}