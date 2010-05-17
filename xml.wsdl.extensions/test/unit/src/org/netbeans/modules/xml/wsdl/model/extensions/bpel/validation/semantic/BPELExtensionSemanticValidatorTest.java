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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.semantic;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.ValidationHelper;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 *
 * @author radval
 */
public class BPELExtensionSemanticValidatorTest extends TestCase {
    
      private static final ResourceBundle mMessages =
        ResourceBundle.getBundle(BPELExtensionSemanticValidatorTest.class.getPackage().getName()+".Bundle");

    public BPELExtensionSemanticValidatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testPropertyAliasMissingProperty() throws Exception {
         HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("VAL_INVALID_PROPERTY_NAME"));
        
        String fileName = "/org/netbeans/modules/xml/wsdl/model/extensions/bpel/validation/semantic/resources/invalid/invalidPropertyAliasMissingProperty.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, expectedErrors);
    }
    
    public void testPropertyAlias1() throws Exception {
         HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("VAL_INVALID_PROPERTY_ALIAS_MESSAGE_TYPE"));
        
        String fileName = "/org/netbeans/modules/xml/wsdl/model/extensions/bpel/validation/semantic/resources/invalid/invalidPropertyAlias1.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, expectedErrors);
    }
    
    public void testPropertyAlias2() throws Exception {
         HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("VAL_INVALID_PROPERTY_ALIAS_ELEMENT"));
        
        String fileName = "/org/netbeans/modules/xml/wsdl/model/extensions/bpel/validation/semantic/resources/invalid/invalidPropertyAlias2.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, expectedErrors);
    }
    
    public void testPropertyAlias3() throws Exception {
         HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("VAL_INVALID_PROPERTY_ALIAS_TYPE"));
        
        String fileName = "/org/netbeans/modules/xml/wsdl/model/extensions/bpel/validation/semantic/resources/invalid/invalidPropertyAlias3.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, expectedErrors);
    }

    public void testPortType1() throws Exception {
         HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("VAL_INVALID_PORT_TYPE"));
        
        String fileName = "/org/netbeans/modules/xml/wsdl/model/extensions/bpel/validation/semantic/resources/invalid/invalidPortType1.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, expectedErrors);
    }
    
    public void testPortType2() throws Exception {
         HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("VAL_INVALID_PORT_TYPE"));
        
        String fileName = "/org/netbeans/modules/xml/wsdl/model/extensions/bpel/validation/semantic/resources/invalid/invalidPortType2.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, expectedErrors);
    }
    
    public void testPartnerLinkType1() throws Exception {
         HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("VAL_INVALID_PARTNERLINK_TYPE"));
        
        String fileName = "/org/netbeans/modules/xml/wsdl/model/extensions/bpel/validation/semantic/resources/invalid/invalidPartnerLinkType1.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, expectedErrors);
    }
    
    private ValidationResult validate(URI relativePath) throws Exception {
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(relativePath);
        Validation validation = new Validation();
        ValidationType validationType = Validation.ValidationType.COMPLETE;
        BPELExtensionSemanticValidator instance = new BPELExtensionSemanticValidator();
        ValidationResult result = 
            instance.validate(model, validation, validationType);
        return result;
    }
    
    private void validate(URI relativePath, HashSet<String> expectedErrors)
        throws Exception {
        System.out.println(relativePath);
        ValidationResult result = validate(relativePath);
        Iterator<ResultItem> it = result.getValidationResult().iterator();
        ValidationHelper.dumpExpecedErrors(expectedErrors);
        while (it.hasNext()) {
            ResultItem item = it.next();
//            System.out.println("    " + item.getDescription());
            assertTrue("Actual Error "+ item.getDescription() + "in " +relativePath, ValidationHelper.containsExpectedError(expectedErrors, item.getDescription()));
        }
        if (result.getValidationResult().size() == 0 && expectedErrors.size() > 0) {
            fail("Expected at least " + expectedErrors.size() + " error(s).  Got 0 errors instead");
        }
    }
    
}
