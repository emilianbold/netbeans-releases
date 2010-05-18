/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.j2ee.sun.util;

import java.io.IOException;
import junit.framework.*;
import com.sun.appserv.management.client.ConnectionSource;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.DomainRoot;
import org.netbeans.modules.j2ee.sun.util.AppserverConnectionFactory;

/**
 *
 * 
 */
public class AppserverConnectionFactoryTest extends TestCase {
    
    private static final String HOST = "localhost";
    private static final int PORT = 4848;
    private static final String USER_NAME = "admin";
    private static final String PASSWORD = "adminadmin";
    
    
    public AppserverConnectionFactoryTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testGetHTTPAppserverConnection() {
        AppserverConnectionSource conn = 
            AppserverConnectionFactory.getHTTPAppserverConnectionSource(HOST, 
                PORT, USER_NAME, PASSWORD, null);
        assertNotNull(conn);
        try {
            conn.getDomainRoot();
        } catch(IOException e) {
            fail("Error connecting to the DAS!!!");
            e.printStackTrace();
        }
    }
    
    public void testIsAppserverConnectionSecurityEnabled() {
        try {
            System.out.println("Is Security Enabled? " + 
            AppserverConnectionFactory.isAppserverConnectionSecurityEnabled(
                    AppserverConnectionFactory.getHTTPAppserverConnectionSource(
                        HOST, PORT, USER_NAME, PASSWORD, null)));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public void testGetAppserverConnection() {
        try {
            AppserverConnectionSource conn = 
                AppserverConnectionFactory.getAppserverConnection(HOST, PORT, 
                    USER_NAME, PASSWORD, null, false);
            System.out.println(conn.getDomainRoot().getAppserverDomainName());
            System.out.println(conn.getJMXConnector(false).getConnectionId());
            AppserverConnectionSource conn2 = 
                AppserverConnectionFactory.getAppserverConnection(HOST, PORT, 
                    USER_NAME, PASSWORD, null, true);
            AppserverConnectionSource conn3 = 
                AppserverConnectionFactory.getAppserverConnection(HOST, PORT, 
                    USER_NAME, PASSWORD, null, false);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AppserverConnectionFactoryTest.class);
        return suite;
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
}
