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

import org.netbeans.modules.websvc.wsitconf.ui.security.SecurityBindingOtherPanel;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.algosuite.*;
import org.netbeans.modules.xml.wsdl.model.*;
import org.openide.ErrorManager;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;

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
    
    public static String getAlgorithmSuite(Binding b, WSDLModel model) {
        WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(b, model);
        if (secBinding != null) {
            WSDLComponent wc = getAlgorithmSuiteForSecurityBinding(b, model, secBinding);
            if (wc != null) {
                return SecurityPolicyModelHelper.getComboItemForElement(wc);
            }
        }
        return null;
    }

    public static WSDLComponent getAlgorithmSuiteForSecurityBinding(Binding b, WSDLModel model, WSDLComponent secBinding) {
        List<Policy> policies = secBinding.getExtensibilityElements(Policy.class);
        if ((policies != null) && !(policies.isEmpty())) {
            Policy p = policies.get(0);
            List<AlgorithmSuite> asuites = p.getExtensibilityElements(AlgorithmSuite.class);
            if ((asuites != null) && !(asuites.isEmpty())) {
                AlgorithmSuite as = asuites.get(0);
                policies = as.getExtensibilityElements(Policy.class);
                if ((policies != null) && !(policies.isEmpty())) {
                    p = policies.get(0);
                    if (p != null) {
                        List<ExtensibilityElement> elements = p.getExtensibilityElements();
                        if ((elements != null) && !(elements.isEmpty())) {
                            ExtensibilityElement e = elements.get(0);
                            return e;
                        }
                    }
                }
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

            if (SecurityBindingOtherPanel.BASIC128.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC128.getQName();
            } else if (SecurityBindingOtherPanel.BASIC192.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC192.getQName();
            } else if (SecurityBindingOtherPanel.BASIC256.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC256.getQName();
            } else if (SecurityBindingOtherPanel.TRIPLEDES.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.TRIPLEDES.getQName();
            } else if (SecurityBindingOtherPanel.BASIC128RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC128RSA15.getQName();
            } else if (SecurityBindingOtherPanel.BASIC192RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC192RSA15.getQName();
            } else if (SecurityBindingOtherPanel.BASIC256RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC256RSA15.getQName();
            } else if (SecurityBindingOtherPanel.TRIPLEDESRSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.TRIPLEDESRSA15.getQName();
            } else if (SecurityBindingOtherPanel.BASIC128SHA256.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC128SHA256.getQName();
            } else if (SecurityBindingOtherPanel.BASIC192SHA256.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC192SHA256.getQName();
            } else if (SecurityBindingOtherPanel.BASIC256SHA256.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC256SHA256.getQName();
            } else if (SecurityBindingOtherPanel.TRIPLEDESSHA256.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.TRIPLEDESSHA256.getQName();
            } else if (SecurityBindingOtherPanel.BASIC128SHA256RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC128SHA256RSA15.getQName();
            } else if (SecurityBindingOtherPanel.BASIC192SHA256RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC192SHA256RSA15.getQName();
            } else if (SecurityBindingOtherPanel.BASIC256SHA256RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.BASIC256SHA256RSA15.getQName();
            } else if (SecurityBindingOtherPanel.TRIPLEDESSHA256RSA15.equals(algoSuite)) {
                qnameToCreate = AlgorithmSuiteQName.TRIPLEDESSHA256RSA15.getQName();
            }

            if (!(topElem instanceof Policy)) {   
                List<Policy> policies = topElem.getExtensibilityElements(Policy.class);
                Policy p = null;
                if ((policies == null) || (policies.isEmpty())) {
                    p = (Policy) wcf.create(topElem, PolicyQName.POLICY.getQName());
                    topElem.addExtensibilityElement(p);
                } else {            
                    p = policies.get(0);
                }
                topElem = p;
            }

            List<AlgorithmSuite> asuites = topElem.getExtensibilityElements(AlgorithmSuite.class);

            AlgorithmSuite suite = null;
            if ((asuites == null) || (asuites.isEmpty())) {
                suite = (AlgorithmSuite) wcf.create(topElem, AlgorithmSuiteQName.ALGORITHMSUITE.getQName());
                topElem.addExtensibilityElement(suite);
            } else {
                suite = asuites.get(0);
            }

            List<Policy> policies = suite.getExtensibilityElements(Policy.class);
            if ((policies != null) && (!policies.isEmpty())) {
                for (Policy pol : policies) {
                    suite.removeExtensibilityElement(pol);
                }
            }
            Policy p = (Policy) wcf.create(suite, PolicyQName.POLICY.getQName());
            suite.addExtensibilityElement(p);
            ExtensibilityElement e = (ExtensibilityElement) wcf.create(p, qnameToCreate);
            p.addExtensibilityElement(e);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
    
}