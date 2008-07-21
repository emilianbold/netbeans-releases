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

package org.netbeans.modules.j2ee.sun.ide.j2ee.jsps;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vkraemer
 */
public class FindJSPServletImplTest {

    public FindJSPServletImplTest() {
    }

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.junit.AfterClass
    public static void tearDownClass() throws Exception {
    }

    @org.junit.Before
    public void setUp() throws Exception {
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    /**
     * Test of getServletTempDirectory method, of class FindJSPServletImpl.
     *
    @org.junit.Test
    public void testGetServletTempDirectory() {
        System.out.println("getServletTempDirectory");
        String moduleContextPath = "";
        FindJSPServletImpl instance = null;
        File expResult = null;
        File result = instance.getServletTempDirectory(moduleContextPath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getServletResourcePath method, of class FindJSPServletImpl.
     */
    @org.junit.Test
    public void testGetServletResourcePath() {
        System.out.println("getServletResourcePath");
        String moduleContextPath = "";
        String jspResourcePath = "/test/index.jsp";
        FindJSPServletImpl instance = new FindJSPServletImpl(null);
        String expResult = "org/apache/jsp/test/index_jsp.java";
        String result = instance.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        jspResourcePath = "//test///index.jsp";
        instance = new FindJSPServletImpl(null);
        expResult = "org/apache/jsp/test/index_jsp.java";
        result = instance.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        jspResourcePath = "/index.jsp";
        expResult = "org/apache/jsp/index_jsp.java";
        result = instance.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        jspResourcePath = "index.jsp";
        expResult = "org/apache/jsp/index_jsp.java";
        result = instance.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        jspResourcePath = "a";
        expResult = "org/apache/jsp/a.java";
        result = instance.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        try {
            jspResourcePath = "";
            expResult = "";
            result = instance.getServletResourcePath(moduleContextPath, jspResourcePath);
            fail("should have triggered an exception");            
        } catch (IllegalArgumentException iae) {
            
        }
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getServletPackageName method, of class FindJSPServletImpl.
     *
    @org.junit.Test
    public void testGetServletPackageName() {
        System.out.println("getServletPackageName");
        String jspUri = "";
        FindJSPServletImpl instance = null;
        String expResult = "";
        String result = instance.getServletPackageName(jspUri);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getServletClassName method, of class FindJSPServletImpl.
     *
    @org.junit.Test
    public void testGetServletClassName() {
        System.out.println("getServletClassName");
        String jspUri = "";
        FindJSPServletImpl instance = null;
        String expResult = "";
        String result = instance.getServletClassName(jspUri);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getServletEncoding method, of class FindJSPServletImpl.
     */
    @org.junit.Test
    public void testGetServletEncoding() {
        System.out.println("getServletEncoding");
        String moduleContextPath = "";
        String jspResourcePath = "";
        FindJSPServletImpl instance = new FindJSPServletImpl(null);
        String expResult = "UTF8";
        String result = instance.getServletEncoding(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of setDeploymentManager method, of class FindJSPServletImpl.
     *
    @org.junit.Test
    public void testSetDeploymentManager() {
        System.out.println("setDeploymentManager");
        DeploymentManager manager = null;
        FindJSPServletImpl instance = null;
        instance.setDeploymentManager(manager);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
}