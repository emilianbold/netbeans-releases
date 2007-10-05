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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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
package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.io.File;
import junit.framework.*;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.websvc.wsitmodelext.security.TrustElement;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class SecurityPolicyTest extends TestCase {
    
    public SecurityPolicyTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception { }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testSecurityPolicy() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");
        WSDLComponentFactory fact = model.getFactory();
        
        model.startTransaction();

        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        assertFalse("WSS10 enabled indicated on empty WSDL", SecurityPolicyModelHelper.isWss10(b));
        assertFalse("WSS11 enabled indicated on empty WSDL", SecurityPolicyModelHelper.isWss11(b));
        assertFalse("Trust10 enabled indicated on empty WSDL", SecurityPolicyModelHelper.isTrust10(b));

        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isMustSupportClientChallenge(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isMustSupportServerChallenge(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isMustSupportIssuedTokens(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isMustSupportRefEmbeddedToken(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isMustSupportRefEncryptedKey(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isMustSupportRefExternalURI(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isMustSupportRefIssuerSerial(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isMustSupportRefKeyIdentifier(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isMustSupportRefThumbprint(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isMustSupportServerChallenge(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isRequireClientEntropy(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isRequireServerEntropy(b));
        
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isEncryptSignature(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isIncludeTimestamp(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isRequireSignatureConfirmation(b));
        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isSignEntireHeadersAndBody(b));

        assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isEncryptBeforeSigning(b));
        
        String secBindingType = SecurityPolicyModelHelper.getSecurityBindingType(b);
        assertEquals("SecurityBinding type indicated on empty wsdl", ComboConstants.NOSECURITY, secBindingType);
        
        //WSS10
        WssElement wss = SecurityPolicyModelHelper.enableWss(b, false);
        assertTrue("WSS10 Not enabled correctly", SecurityPolicyModelHelper.isWss10(b));

            //REF EMBEDDED WSS10
            SecurityPolicyModelHelper.enableMustSupportRefEmbeddedToken(wss, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportRefEmbeddedToken(b));
            SecurityPolicyModelHelper.enableMustSupportRefEmbeddedToken(wss, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportRefEmbeddedToken(b));

            //REF EXTERNAL URI WSS10
            SecurityPolicyModelHelper.enableMustSupportRefExternalURI(wss, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportRefExternalURI(b));
            SecurityPolicyModelHelper.enableMustSupportRefExternalURI(wss, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportRefExternalURI(b));
        
            //REF ISSUER SERIAL WSS10
            SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportRefIssuerSerial(b));
            SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportRefIssuerSerial(b));

            //REF KEY IDENTIFIER WSS10
            SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportRefKeyIdentifier(b));
            SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportRefKeyIdentifier(b));

        SecurityPolicyModelHelper.disableWss(b);
        assertFalse("WSS10 enabled indicated", SecurityPolicyModelHelper.isWss10(b));
        
        //WSS11
        wss = SecurityPolicyModelHelper.enableWss(b, true);
        assertTrue("WSS11 Not enabled correctly", SecurityPolicyModelHelper.isWss11(b));

            //REF EMBEDDED WSS11
            SecurityPolicyModelHelper.enableMustSupportRefEmbeddedToken(wss, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportRefEmbeddedToken(b));
            SecurityPolicyModelHelper.enableMustSupportRefEmbeddedToken(wss, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportRefEmbeddedToken(b));

            //REF EXTERNAL URI WSS11
            SecurityPolicyModelHelper.enableMustSupportRefExternalURI(wss, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportRefExternalURI(b));
            SecurityPolicyModelHelper.enableMustSupportRefExternalURI(wss, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportRefExternalURI(b));
        
            //REF KEY IDENTIFIER WSS11
            SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportRefKeyIdentifier(b));
            SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportRefKeyIdentifier(b));

            //REF ISSUER SERIAL WSS11
            SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportRefIssuerSerial(b));
            SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportRefIssuerSerial(b));

            //Must Support Ref Encrypted Key
            SecurityPolicyModelHelper.enableMustSupportRefEncryptedKey(wss, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportRefEncryptedKey(b));
            SecurityPolicyModelHelper.enableMustSupportRefEncryptedKey(wss, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportRefEncryptedKey(b));
            
            //Must Support Ref Thumbprint
            SecurityPolicyModelHelper.enableMustSupportRefThumbprint(wss, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportRefThumbprint(b));
            SecurityPolicyModelHelper.enableMustSupportRefThumbprint(wss, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportRefThumbprint(b));

            //Require Signature Confirmation
            SecurityPolicyModelHelper.enableRequireSignatureConfirmation(wss, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isRequireSignatureConfirmation(b));
            SecurityPolicyModelHelper.enableRequireSignatureConfirmation(wss, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isRequireSignatureConfirmation(b));

        SecurityPolicyModelHelper.disableWss(b);
        assertFalse("WSS11 enabled indicated", SecurityPolicyModelHelper.isWss11(b));

        //TRUST10
        TrustElement trust = SecurityPolicyModelHelper.enableTrust10(b);
        assertTrue("Trust10 Not enabled correctly", SecurityPolicyModelHelper.isTrust10(b));

            //MUST SUPPORT CLIENT CHALLENGE
            SecurityPolicyModelHelper.enableMustSupportClientChallenge(trust, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportClientChallenge(b));
            SecurityPolicyModelHelper.enableMustSupportClientChallenge(trust, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportClientChallenge(b));

            //MUST SUPPORT SERVER CHALLENGE
            SecurityPolicyModelHelper.enableMustSupportServerChallenge(trust, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportServerChallenge(b));
            SecurityPolicyModelHelper.enableMustSupportServerChallenge(trust, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportServerChallenge(b));

            //MUST SUPPORT Issued Tokens
            SecurityPolicyModelHelper.enableMustSupportIssuedTokens(trust, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isMustSupportIssuedTokens(b));
            SecurityPolicyModelHelper.enableMustSupportIssuedTokens(trust, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isMustSupportIssuedTokens(b));

            //Require Client Entropy
            SecurityPolicyModelHelper.enableRequireClientEntropy(trust, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isRequireClientEntropy(b));
            SecurityPolicyModelHelper.enableRequireClientEntropy(trust, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isRequireClientEntropy(b));

            //Require Server Entropy
            SecurityPolicyModelHelper.enableRequireServerEntropy(trust, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isRequireServerEntropy(b));
            SecurityPolicyModelHelper.enableRequireServerEntropy(trust, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isRequireServerEntropy(b));

        SecurityPolicyModelHelper.disableTrust10(b);
        assertFalse("Trust10 enabled indicated", SecurityPolicyModelHelper.isTrust10(b));

        SecurityPolicyModelHelper.setSecurityBindingType(b, ComboConstants.TRANSPORT);
        secBindingType = SecurityPolicyModelHelper.getSecurityBindingType(b);
        assertEquals("Wrong SecurityBinding type indicated", ComboConstants.TRANSPORT, secBindingType);

        SecurityPolicyModelHelper.setSecurityBindingType(b, ComboConstants.SYMMETRIC);
        secBindingType = SecurityPolicyModelHelper.getSecurityBindingType(b);
        assertEquals("Wrong SecurityBinding type indicated", ComboConstants.SYMMETRIC, secBindingType);
        
            WSDLComponent bindingType = SecurityPolicyModelHelper.getSecurityBindingTypeElement(b);
            
            // Encrypt Signature
            SecurityPolicyModelHelper.enableEncryptSignature(bindingType, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isEncryptSignature(b));
            SecurityPolicyModelHelper.enableEncryptSignature(bindingType, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isEncryptSignature(b));

            // Include Timestamp
            SecurityPolicyModelHelper.enableIncludeTimestamp(bindingType, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isIncludeTimestamp(b));
            SecurityPolicyModelHelper.enableIncludeTimestamp(bindingType, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isIncludeTimestamp(b));

            // Sign Entire Headers And Body
            SecurityPolicyModelHelper.enableSignEntireHeadersAndBody(bindingType, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isSignEntireHeadersAndBody(b));
            SecurityPolicyModelHelper.enableSignEntireHeadersAndBody(bindingType, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isSignEntireHeadersAndBody(b));

            // Encrypt Before Signing
            SecurityPolicyModelHelper.enableEncryptBeforeSigning(bindingType, true);
            assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isEncryptBeforeSigning(b));
            SecurityPolicyModelHelper.enableEncryptBeforeSigning(bindingType, false);
            assertFalse("enabled indicated", SecurityPolicyModelHelper.isEncryptBeforeSigning(b));

            // Message Layout
            SecurityPolicyModelHelper.setLayout(bindingType, ComboConstants.STRICT);
            assertEquals("Message Layout", ComboConstants.STRICT, SecurityPolicyModelHelper.getMessageLayout(b));
            SecurityPolicyModelHelper.setLayout(bindingType, ComboConstants.LAX);
            assertEquals("Message Layout", ComboConstants.LAX, SecurityPolicyModelHelper.getMessageLayout(b));
            SecurityPolicyModelHelper.setLayout(bindingType, ComboConstants.LAXTSFIRST);
            assertEquals("Message Layout", ComboConstants.LAXTSFIRST, SecurityPolicyModelHelper.getMessageLayout(b));
            SecurityPolicyModelHelper.setLayout(bindingType, ComboConstants.LAXTSLAST);
            assertEquals("Message Layout", ComboConstants.LAXTSLAST, SecurityPolicyModelHelper.getMessageLayout(b));
        
            // Algorithm Suite
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.BASIC128);
            assertEquals("Algorithm Suite", ComboConstants.BASIC128, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.BASIC192);
            assertEquals("Algorithm Suite", ComboConstants.BASIC192, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.BASIC256);
            assertEquals("Algorithm Suite", ComboConstants.BASIC256, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.TRIPLEDES);
            assertEquals("Algorithm Suite", ComboConstants.TRIPLEDES, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.BASIC256RSA15);
            assertEquals("Algorithm Suite", ComboConstants.BASIC256RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.BASIC192RSA15);
            assertEquals("Algorithm Suite", ComboConstants.BASIC192RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.BASIC128RSA15);
            assertEquals("Algorithm Suite", ComboConstants.BASIC128RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.TRIPLEDESRSA15);
            assertEquals("Algorithm Suite", ComboConstants.TRIPLEDESRSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.BASIC256SHA256);
            assertEquals("Algorithm Suite", ComboConstants.BASIC256SHA256, AlgoSuiteModelHelper.getAlgorithmSuite(b));

            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.BASIC192SHA256);
            assertEquals("Algorithm Suite", ComboConstants.BASIC192SHA256, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.BASIC128SHA256);
            assertEquals("Algorithm Suite", ComboConstants.BASIC128SHA256, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.TRIPLEDESSHA256);
            assertEquals("Algorithm Suite", ComboConstants.TRIPLEDESSHA256, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.BASIC256SHA256RSA15);
            assertEquals("Algorithm Suite", ComboConstants.BASIC256SHA256RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.BASIC192SHA256RSA15);
            assertEquals("Algorithm Suite", ComboConstants.BASIC192SHA256RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.BASIC128SHA256RSA15);
            assertEquals("Algorithm Suite", ComboConstants.BASIC128SHA256RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            AlgoSuiteModelHelper.setAlgorithmSuite(bindingType, ComboConstants.TRIPLEDESSHA256RSA15);
            assertEquals("Algorithm Suite", ComboConstants.TRIPLEDESSHA256RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
            
        SecurityPolicyModelHelper.setSecurityBindingType(b, ComboConstants.ASYMMETRIC);
        secBindingType = SecurityPolicyModelHelper.getSecurityBindingType(b);
        assertEquals("Wrong SecurityBinding type indicated", ComboConstants.ASYMMETRIC, secBindingType);

        SecurityPolicyModelHelper.setSecurityBindingType(b, ComboConstants.NOSECURITY);
        secBindingType = SecurityPolicyModelHelper.getSecurityBindingType(b);
        assertEquals("Wrong SecurityBinding type indicated", ComboConstants.NOSECURITY, secBindingType);
        
            // FIRST CHECK DEFAULTS - those should be set when binding is switched to this value
            assertNull("Default Algorithm Suite", AlgoSuiteModelHelper.getAlgorithmSuite(b));
            assertNull("Default Message Layout", SecurityPolicyModelHelper.getMessageLayout(b));
            assertFalse("Default Include Timestamp", SecurityPolicyModelHelper.isIncludeTimestamp(b));
            assertFalse("Default WSS", SecurityPolicyModelHelper.isWss10(b));
            assertFalse("Default WSS", SecurityPolicyModelHelper.isWss11(b));
            assertFalse("Default Trust", SecurityPolicyModelHelper.isTrust10(b));

        model.endTransaction();

        TestUtil.dumpToFile(model.getBaseDocument(), new File("C:\\SecurityPolicyService.wsdl"));
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}
