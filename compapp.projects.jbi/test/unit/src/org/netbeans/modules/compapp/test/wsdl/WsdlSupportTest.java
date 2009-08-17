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

package org.netbeans.modules.compapp.test.wsdl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jqian
 */
public class WsdlSupportTest {

    private WsdlSupport wsdlSupport;
    
    public WsdlSupportTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws URISyntaxException, CatalogModelException {
        // #114295 Disable warning message for NB 6.0
        Logger.getLogger("org.netbeans.modules.editor.impl.KitsTracker").setLevel(Level.SEVERE);
        
        URI uri = getClass().
                getResource("resources/DocumentLiteral_MsgPartElement.wsdl").
                toURI();
        File file = new File(uri);
        FileObject fo = FileUtil.toFileObject(file); 
        ModelSource wsdlModelSource = 
                TestCatalogModel.getDefault().createModelSource(fo, true);
        wsdlSupport = new WsdlSupport(fo, wsdlModelSource);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getWsdlModel method, of class WsdlSupport.
     */
    @Test
    public void getWsdlModel() {
        System.out.println("getWsdlModel");   
        WSDLModel result = wsdlSupport.getWsdlModel();
        assertNotNull(result);
    }

    /**
     * Test of getSchemaTypeLoader method, of class WsdlSupport.
     */
    @Test
    public void getSchemaTypeLoader() {
        System.out.println("getSchemaTypeLoader");
        SchemaTypeLoader result = wsdlSupport.getSchemaTypeLoader();
        assertNotNull(result);
    }
}