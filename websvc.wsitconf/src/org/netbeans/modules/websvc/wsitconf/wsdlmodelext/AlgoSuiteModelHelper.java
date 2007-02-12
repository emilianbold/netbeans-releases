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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.AlgorithmSuite;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.AlgorithmSuiteQName;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic128;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic192;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic256;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic256Sha256;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic256Sha256Rsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.TripleDes;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.TripleDesRsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.TripleDesSha256;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.TripleDesSha256Rsa15;
import org.netbeans.modules.xml.wsdl.model.*;

import javax.xml.namespace.QName;
import java.util.List;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic128Rsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic128Sha256;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic128Sha256Rsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic192Rsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic192Sha256;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic192Sha256Rsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic256Rsa15;

/**
 *
 * @author Martin Grebac
 */
public class AlgoSuiteModelHelper {
    
    /**
     * Creates a new instance of AlgoSuiteModelHelper
     */
    public AlgoSuiteModelHelper() {
    }
    
    public static String getAlgorithmSuite(WSDLComponent comp) {
        WSDLComponent layout = getAlgorithmSuiteElement(comp);
        if (layout != null) {
            if (layout instanceof Basic128) return ComboConstants.BASIC128;
            if (layout instanceof Basic192) return ComboConstants.BASIC192;
            if (layout instanceof Basic256) return ComboConstants.BASIC256;
            if (layout instanceof TripleDes) return ComboConstants.TRIPLEDES;
            if (layout instanceof Basic256Rsa15) return ComboConstants.BASIC256RSA15;
            if (layout instanceof Basic192Rsa15) return ComboConstants.BASIC192RSA15;
            if (layout instanceof Basic128Rsa15) return ComboConstants.BASIC128RSA15;
            if (layout instanceof TripleDesRsa15) return ComboConstants.TRIPLEDESRSA15;
            if (layout instanceof Basic256Sha256) return ComboConstants.BASIC256SHA256;
            if (layout instanceof Basic192Sha256) return ComboConstants.BASIC192SHA256;
            if (layout instanceof Basic128Sha256) return ComboConstants.BASIC128SHA256;
            if (layout instanceof TripleDesSha256) return ComboConstants.TRIPLEDESSHA256;
            if (layout instanceof Basic256Sha256Rsa15) return ComboConstants.BASIC256SHA256RSA15;
            if (layout instanceof Basic192Sha256Rsa15) return ComboConstants.BASIC192SHA256RSA15;
            if (layout instanceof Basic128Sha256Rsa15) return ComboConstants.BASIC128SHA256RSA15;
            if (layout instanceof TripleDesSha256Rsa15) return ComboConstants.TRIPLEDESSHA256RSA15;
        }
        return null;
    }
    
    public static WSDLComponent getAlgorithmSuiteElement(WSDLComponent comp) {
        if ((comp instanceof Binding) || (comp instanceof BindingOperation)) {
            comp = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
        }
        if (comp == null) return null;
        Policy p = PolicyModelHelper.getTopLevelElement(comp, Policy.class);
        AlgorithmSuite as = PolicyModelHelper.getTopLevelElement(p, AlgorithmSuite.class);
        p = PolicyModelHelper.getTopLevelElement(as, Policy.class);
        if (p != null) {
            List<ExtensibilityElement> elements = p.getExtensibilityElements();
            if ((elements != null) && !(elements.isEmpty())) {
                ExtensibilityElement e = elements.get(0);
                return e;
            }
        }
        return null;
    }
 
    public static void setAlgorithmSuite(WSDLComponent c, String algoSuite) {
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        
        WSDLComponent topElem = c;
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            QName qnameToCreate = null;

            if (ComboConstants.BASIC128.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC128.getQName();
            } else if (ComboConstants.BASIC192.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC192.getQName();
            } else if (ComboConstants.BASIC256.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC256.getQName();
            } else if (ComboConstants.TRIPLEDES.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.TRIPLEDES.getQName();
            } else if (ComboConstants.BASIC128RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC128RSA15.getQName();
            } else if (ComboConstants.BASIC192RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC192RSA15.getQName();
            } else if (ComboConstants.BASIC256RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC256RSA15.getQName();
            } else if (ComboConstants.TRIPLEDESRSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.TRIPLEDESRSA15.getQName();
            } else if (ComboConstants.BASIC128SHA256.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC128SHA256.getQName();
            } else if (ComboConstants.BASIC192SHA256.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC192SHA256.getQName();
            } else if (ComboConstants.BASIC256SHA256.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC256SHA256.getQName();
            } else if (ComboConstants.TRIPLEDESSHA256.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.TRIPLEDESSHA256.getQName();
            } else if (ComboConstants.BASIC128SHA256RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC128SHA256RSA15.getQName();
            } else if (ComboConstants.BASIC192SHA256RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC192SHA256RSA15.getQName();
            } else if (ComboConstants.BASIC256SHA256RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC256SHA256RSA15.getQName();
            } else if (ComboConstants.TRIPLEDESSHA256RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.TRIPLEDESSHA256RSA15.getQName();
            }

            AlgorithmSuite suite = PolicyModelHelper.createElement(topElem, 
                    AlgorithmSuiteQName.ALGORITHMSUITE.getQName(), 
                    AlgorithmSuite.class, 
                    !(topElem instanceof Policy));

            List<Policy> policies = suite.getExtensibilityElements(Policy.class);
            if ((policies != null) && (!policies.isEmpty())) {
                for (Policy pol : policies) {
                    suite.removeExtensibilityElement(pol);
                }
            }
            Policy p = PolicyModelHelper.createElement(suite, PolicyQName.POLICY.getQName(), Policy.class, false);
            ExtensibilityElement e = (ExtensibilityElement) wcf.create(p, qnameToCreate);
            p.addExtensibilityElement(e);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
}