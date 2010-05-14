/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.sun.manager.jbi.management.model;

import java.io.File;
import java.net.URI;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jqian
 */
public class ComponentInformationParserTest {

    public ComponentInformationParserTest() {
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
     * Test of parse method, of class ComponentInformationParser.
     */
    @Test
    public void parse_BindingComponentInformation() throws Exception {
        System.out.println("parse BindingComponentInformation.xml");
        
        URI xmlURI = getClass().getResource("resources/BindingComponentInformation.xml").toURI();
        File xmlFile = new File(xmlURI);
        List<JBIComponentStatus> compList = ComponentInformationParser.parse(xmlFile);
        assertEquals(5, compList.size());
        
        // check one BC
        JBIComponentStatus httpBC = compList.get(1);
        assertEquals("sun-http-binding", httpBC.getName());
        assertTrue(httpBC.isBindingComponent());
        assertEquals(2, httpBC.getNamespaces().size());
        assertTrue(httpBC.getNamespaces().contains("http://schemas.xmlsoap.org/wsdl/http/"));
        assertTrue(httpBC.getNamespaces().contains("http://schemas.xmlsoap.org/wsdl/soap/"));
        
        // check another BC 
        JBIComponentStatus ftpBC = compList.get(4);
        assertEquals("sun-ftp-binding", ftpBC.getName());
        assertTrue(ftpBC.isBindingComponent());  // case insensitive
    }
    
     /**
     * Test of parse method, of class ComponentInformationParser.
     */
    @Test
    public void parse_ComponentInformation() throws Exception {
        System.out.println("parse ComponentInformation.xml");        
        
        URI xmlURI = getClass().getResource("resources/ComponentInformation.xml").toURI();
        File xmlFile = new File(xmlURI);
        List<JBIComponentStatus> compList = ComponentInformationParser.parse(xmlFile);
        assertEquals(11, compList.size());
        
        // check one SE
        JBIComponentStatus bpelSE = compList.get(3);
        assertEquals("sun-bpel-engine", bpelSE.getName());
        assertTrue(bpelSE.isServiceEngine());
        
        // check another SE
        JBIComponentStatus etlSE = compList.get(9);
        assertEquals("sun-etl-engine", etlSE.getName());
        assertTrue(etlSE.isServiceEngine()); // case insensitive
    }
}