/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitmodelext;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10QName;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10WsdlQName;
import org.netbeans.modules.websvc.wsitmodelext.addressing.AddressingQName;
import org.netbeans.modules.websvc.wsitmodelext.mtom.MtomQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMMSQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMSunClientQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMSunQName;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityQName;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.AlgorithmSuiteQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySCClientQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryTrustClientQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ProprietarySCServiceQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ProprietarySecurityPolicyServiceQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ProprietaryTrustServiceQName;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.TokensQName;
import org.netbeans.modules.websvc.wsitmodelext.transport.FIQName;
import org.netbeans.modules.websvc.wsitmodelext.transport.TCPQName;
import org.netbeans.modules.websvc.wsitmodelext.trust.TrustQName;
import org.netbeans.modules.websvc.wsitmodelext.tx.TxQName;
import org.netbeans.modules.websvc.wsitmodelext.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitmodelext.util.Util;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class PolicyTest extends NbTestCase {
    
    public PolicyTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testWrite() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = Util.loadWSDLModel("../resources/policy.xml");
        WSDLComponentFactory fact = model.getFactory();
        
        model.startTransaction();

        Definitions d = model.getDefinitions();
        ExtensibilityElement e = null;
        Set<QName> qnames = new HashSet<QName>();
        
        qnames.addAll(PolicyQName.getQNames());
        qnames.addAll(MtomQName.getQNames());
        qnames.addAll(FIQName.getQNames());
        qnames.addAll(TCPQName.getQNames());
        qnames.addAll(AddressingQName.getQNames());
        qnames.addAll(Addressing10QName.getQNames());
        qnames.addAll(Addressing10WsdlQName.getQNames());
        qnames.addAll(RMQName.getQNames());
        qnames.addAll(RMSunQName.getQNames());
        qnames.addAll(RMSunClientQName.getQNames());
        qnames.addAll(RMMSQName.getQNames());
        qnames.addAll(SecurityQName.getQNames());
        qnames.addAll(SecurityPolicyQName.getQNames());
        qnames.addAll(AlgorithmSuiteQName.getQNames());
        qnames.addAll(ProprietaryPolicyQName.getQNames());
        qnames.addAll(ProprietarySCClientQName.getQNames());
        qnames.addAll(ProprietarySecurityPolicyQName.getQNames());
        qnames.addAll(ProprietarySecurityPolicyServiceQName.getQNames());
        qnames.addAll(ProprietaryTrustClientQName.getQNames());
        qnames.addAll(ProprietaryTrustServiceQName.getQNames());
        qnames.addAll(ProprietarySCServiceQName.getQNames());
        qnames.addAll(TokensQName.getQNames());
        qnames.addAll(TrustQName.getQNames());
        qnames.addAll(TxQName.getQNames());

        for (QName qname : qnames) {
            e = (ExtensibilityElement)fact.create(d, qname);
            d.addExtensibilityElement(e);
        }
        
        model.endTransaction();

        File output = new File("WSITModelTestOutput.wsdl");
        
        System.out.println("Generating file: " + output.getAbsolutePath());
        
        File golden = new File(getDataDir().getAbsolutePath() + "/goldenfiles/" + "WSITModelTestOutput.wsdl");

        Util.dumpToFile(model.getBaseDocument(), output);
        
        System.out.println(getWorkDir().getAbsolutePath());
        assertFile(output, golden, getWorkDir());
    }

    public String getTestResourcePath() {
        return "../resources/policy.xml";
    }
    
}
