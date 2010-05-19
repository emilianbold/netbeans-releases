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

package org.netbeans.modules.sun.manager.jbi.management.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author jqian
 */
public class JBIComponentConfigurationDescriptorTest {

    private static JBIComponentConfigurationDescriptor threadCount;
    private static JBIComponentConfigurationDescriptor appConfig;
        
    public JBIComponentConfigurationDescriptorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws URISyntaxException, 
            ParserConfigurationException, IOException, SAXException {
        
        URI xmlURI = JBIComponentConfigurationDescriptorTest.class.
                getResource("resources/sun-jms-binding-jbi.xml").toURI();
        File xmlFile = new File(xmlURI);
        String xmlText = getContent(xmlFile);
        
        JBIComponentConfigurationDescriptor result =
                JBIComponentConfigurationParser.parse(xmlText);
        
        threadCount = result.getChild("ThreadCount");
        appConfig = result.getChild("ApplicationConfiguration");        
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
     * Test of getName method, of class JBIComponentConfigurationDescriptor.
     */
    @Test
    public void getName() {
        System.out.println("getName");
        
        assertEquals("ThreadCount", threadCount.getName());
        assertEquals("ApplicationConfiguration", appConfig.getName());
    }

    /**
     * Test of getDisplayName method, of class JBIComponentConfigurationDescriptor.
     */
    @Test
    public void getDisplayName() {
        System.out.println("getDisplayName");
        
        assertEquals("Number of Threads", threadCount.getDisplayName());
        assertEquals("Application Configuration", appConfig.getDisplayName());
    }

    /**
     * Test of getDescription method, of class JBIComponentConfigurationDescriptor.
     */
    @Test
    public void getDescription() {
        System.out.println("getDescription");
        
        assertEquals("# of threads to proccess outbound JMS requests and Message Exchange responses concurrentlty", threadCount.getDescription());
        assertEquals("Application Configuration", appConfig.getDescription());
    }

    /**
     * Test of getTypeQName method, of class JBIComponentConfigurationDescriptor.
     */
    @Test
    public void getTypeQname() {
        System.out.println("getTypeQname");
        
        assertEquals(JBIComponentConfigurationDescriptor.XSD_POSITIVE_INTEGER, 
                threadCount.getTypeQName());        
    }

    /**
     * Test of isEncrypted method, of class JBIComponentConfigurationDescriptor.
     */
    @Test
    public void isEncrypted() {
        System.out.println("isEncrypted");
        
        assertFalse(threadCount.isEncrypted());
        assertFalse(appConfig.isEncrypted());
    }

    /**
     * Test of needsApplicationRestart method, of class JBIComponentConfigurationDescriptor.
     */
    @Test
    public void isApplicationRestartRequired() {
        System.out.println("needsApplicationRestart");
        
        assertFalse(threadCount.isApplicationRestartRequired());
        assertTrue(appConfig.isApplicationRestartRequired());
    }

    /**
     * Test of isComponentRestartRequired method, of class JBIComponentConfigurationDescriptor.
     */
    @Test
    public void isComponentRestartRequired() {
        System.out.println("isComponentRestartRequired");
        
        assertTrue(threadCount.isComponentRestartRequired());
        assertFalse(appConfig.isComponentRestartRequired());
    }

    /**
     * Test of isServerRestartRequired method, of class JBIComponentConfigurationDescriptor.
     */
    @Test
    public void isServerRestartRequired() {
        System.out.println("isServerRestartRequired");
        
        assertFalse(threadCount.isServerRestartRequired());
        assertFalse(appConfig.isServerRestartRequired());
    }

    /**
     * Test of isRequired method, of class JBIComponentConfigurationDescriptor.
     */
    @Test
    public void isRequired() {
        System.out.println("isRequired");
        
        assertFalse(threadCount.isRequired());
        assertFalse(appConfig.isRequired());
    }

    /**
     * Test of getChildNames method, of class JBIComponentConfigurationDescriptor.
     */
    @Test
    public void getChildNames() {
        System.out.println("getChildNames");
        
        Set<String> childrenNames = appConfig.getChildNames();
        assertTrue(childrenNames.contains("ApplicationConfigurationName"));
        assertTrue(childrenNames.contains("UserName"));
        assertTrue(childrenNames.contains("Password"));
        
        // not supporting compound property yet
//        assertTrue(childrenNames.contains("JndiEnv"));
    }

    /**
     * Test of getChild method, of class JBIComponentConfigurationDescriptor.
     */
    // not supporting compound property yet
//    @Test
//    public void getChild() {
//        System.out.println("getChild");
//        
//        JBIComponentConfigurationDescriptor jndiEnv = appConfig.getChild("JndiEnv");
//        Set<String> childrenNames = jndiEnv.getChildNames();
//        assertTrue(childrenNames.contains("JndiName"));
//        assertTrue(childrenNames.contains("JndiValue"));        
//    }
    
    private static String getContent(File file) {
        String ret = "";

        BufferedReader is = null;
        try {
            is = new BufferedReader(new FileReader(file));
            String inputLine;
            while ((inputLine = is.readLine()) != null) {
                ret += inputLine;
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }

        return ret;
    }
    
}