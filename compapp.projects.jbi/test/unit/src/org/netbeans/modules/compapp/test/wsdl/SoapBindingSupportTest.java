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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xdm.diff.Difference;
import org.netbeans.modules.xml.xdm.diff.XDMUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.junit.Assert.*;

/**
 *
 * @author jqian
 */
public class SoapBindingSupportTest {

    public SoapBindingSupportTest() {
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
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of buildRequest method, of class SoapBindingSupport.
     */
    @Test
    public void buildRequest_DocumentLiteral_MsgPartElement() throws Exception {
        System.out.println("buildRequest on BP 1.0 Compliant Document Literal");

        XDMUtil xdmUtil = new XDMUtil();

        String result = getBuildRequest("DocumentLiteral_MsgPartElement.wsdl");
        String expected = getFileContent("DocumentLiteral_MsgPartElement_ExpectedSoapMsg.xml");
        
        List<Difference> diffs = xdmUtil.compareXML(
                expected, result, XDMUtil.ComparisonCriteria.EQUAL);
        assertEquals(0, diffs.size());
    }

    /**
     * Test of buildRequest method, of class SoapBindingSupport.
     */
    @Test
    public void buildRequest_RPCLiteral_MsgPartType() throws Exception {
        System.out.println("buildRequest on BP 1.0 Compliant RPC Literal");

        XDMUtil xdmUtil = new XDMUtil();

        String result = getBuildRequest("RPCLiteral_MsgPartType.wsdl");
        String expected = getFileContent("RPCLiteral_MsgPartType_ExpectedSoapMsg.xml");
        
        List<Difference> diffs = xdmUtil.compareXML(
                expected, result, XDMUtil.ComparisonCriteria.EQUAL);
        assertEquals(0, diffs.size());
    }

    /**
     * Test of buildRequest method, of class SoapBindingSupport.
     */
    @Test
    public void buildRequest_RPCLiteral_MsgPartElement() throws Exception {
        System.out.println("buildRequest on non BP 1.0 Compliant RPC Literal");

        XDMUtil xdmUtil = new XDMUtil();

        String result = getBuildRequest("RPCLiteral_MsgPartElement.wsdl");
        String expected = getFileContent("RPCLiteral_MsgPartElement_ExpectedSoapMsg.xml");
        
        List<Difference> diffs = xdmUtil.compareXML(
                expected, result, XDMUtil.ComparisonCriteria.EQUAL);
        assertEquals(0, diffs.size());
    }

    private String getBuildRequest(String wsdlFileName)
            throws Exception {

        WsdlSupport wsdlSupport = getWsdlSupport(wsdlFileName);

        WSDLModel wsdlModel = wsdlSupport.getWsdlModel();
        SchemaTypeLoader schemaTypeLoader = wsdlSupport.getSchemaTypeLoader();
        Definitions definitions = wsdlModel.getDefinitions();
        Binding binding = Util.getSortedBindings(wsdlModel).get(0);
        BindingOperation bindingOp =
                Util.getSortedBindingOperations(binding).get(0);
        Map params = new HashMap();

        params.put(BindingSupport.BUILD_OPTIONAL, Boolean.TRUE);
        SoapBindingSupportFactory factory = new SoapBindingSupportFactory();
        BindingSupport bindingSupport = factory.createBindingSupport(
                binding,
                definitions,
                schemaTypeLoader);
        return bindingSupport.buildRequest(bindingOp, params);
    }

    private WsdlSupport getWsdlSupport(String wsdlFileName)
            throws URISyntaxException, CatalogModelException {
        URI uri = getClass().getResource("resources/" + wsdlFileName).toURI();
        File file = new File(uri);
        FileObject fo = FileUtil.toFileObject(file);
        ModelSource wsdlModelSource =
                TestCatalogModel.getDefault().createModelSource(fo, true);
        return new WsdlSupport(fo, wsdlModelSource);
    }

    private String getFileContent(String fileName) throws URISyntaxException {

        URI uri = getClass().getResource("resources/" + fileName).toURI();
        File file = new File(uri);

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
