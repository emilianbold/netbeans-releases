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

package org.netbeans.modules.compapp.projects.jbi.api;

import java.net.URL;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.XMLFileSystem;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author jqian
 */
public class JbiExtensionInfoTest {
    
    private static final String CONFIG_EXTENSION = "ConfigExtension";
    
    private JbiExtensionInfo configExtensionInfo; 
    
    public JbiExtensionInfoTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws SAXException {
        URL fsURLDef = this.getClass().getResource("resources/fs.xml");
        assertTrue("Cannot create XML FS for testing purposes", fsURLDef != null);
        
        FileSystem fs = new XMLFileSystem(fsURLDef);   
        JbiInstalledExtensionInfo installedExtensionInfo = 
                JbiInstalledExtensionInfo.getInstalledExtensionInfo(fs);   
        
        configExtensionInfo = installedExtensionInfo.getExtensionInfo(CONFIG_EXTENSION);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getIcon method, of class JbiExtensionInfo.
     */
    @Test
    public void getIcon() {
        System.out.println("getIcon");
        String expResult = "nbresloc:/org/namebeans/modules/compapp/configextension/resources/config-ext.png";
        String result = configExtensionInfo.getIcon().toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getName method, of class JbiExtensionInfo.
     */
    @Test
    public void getName() {
        System.out.println("getName");
        String expResult = "ConfigExtension";
        String result = configExtensionInfo.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getType method, of class JbiExtensionInfo.
     */
    @Test
    public void getType() {
        System.out.println("getType");
        String expResult = "endpoint";
        String result = configExtensionInfo.getType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTarget method, of class JbiExtensionInfo.
     */
    @Test
    public void getTarget() {
        System.out.println("getTarget");
        String expResult = "sun-http-binding";
        String result = configExtensionInfo.getTarget();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFile method, of class JbiExtensionInfo.
     */
    @Test
    public void getFile() {
        System.out.println("getFile");
        String expResult = "config-ext.xsd";
        String result = configExtensionInfo.getFile();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDescription method, of class JbiExtensionInfo.
     */
    @Test
    public void getDescription() {
        System.out.println("getDescription");
        String expResult = "";
        String result = configExtensionInfo.getDescription();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNameSpace method, of class JbiExtensionInfo.
     */
    @Test
    public void getNameSpace() {
        System.out.println("getNameSpace");
        String expResult = "http://www.sun.com/jbi/descriptor/configuration";
        String result = configExtensionInfo.getNameSpace();
        assertEquals(expResult, result);
    }

    /**
     * Test of getElements method, of class JbiExtensionInfo.
     */
    @Test
    public void getElements() {
        System.out.println("getElements");
        List<JbiExtensionElement> result = configExtensionInfo.getElements();
        assertEquals(1, result.size());
    }

}