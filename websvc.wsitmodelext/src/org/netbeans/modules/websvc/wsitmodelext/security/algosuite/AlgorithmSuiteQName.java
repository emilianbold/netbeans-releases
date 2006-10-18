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


package org.netbeans.modules.websvc.wsitmodelext.security.algosuite;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Martin Grebac
 */
public enum AlgorithmSuiteQName {
    ALGORITHMSUITE(createSecurityPolicyQName("AlgorithmSuite")),  //NOI18N
    BASIC256(createSecurityPolicyQName("Basic256")),  //NOI18N
    BASIC192(createSecurityPolicyQName("Basic192")),  //NOI18N
    BASIC128(createSecurityPolicyQName("Basic128")),  //NOI18N
    TRIPLEDES(createSecurityPolicyQName("TripleDes")),  //NOI18N
    BASIC256RSA15(createSecurityPolicyQName("Basic256Rsa15")),  //NOI18N
    BASIC192RSA15(createSecurityPolicyQName("Basic192Rsa15")),  //NOI18N
    BASIC128RSA15(createSecurityPolicyQName("Basic128Rsa15")),  //NOI18N
    TRIPLEDESRSA15(createSecurityPolicyQName("TripleDesRsa15")),  //NOI18N
    BASIC256SHA256(createSecurityPolicyQName("Basic256Sha256")),  //NOI18N
    BASIC192SHA256(createSecurityPolicyQName("Basic192Sha256")),  //NOI18N
    BASIC128SHA256(createSecurityPolicyQName("Basic128Sha256")),  //NOI18N
    TRIPLEDESSHA256(createSecurityPolicyQName("TripleDesSha256")),  //NOI18N
    BASIC256SHA256RSA15(createSecurityPolicyQName("Basic256Sha256Rsa15")),  //NOI18N
    BASIC192SHA256RSA15(createSecurityPolicyQName("Basic192Sha256Rsa15")),  //NOI18N
    BASIC128SHA256RSA15(createSecurityPolicyQName("Basic128Sha256Rsa15")),  //NOI18N
    TRIPLEDESSHA256RSA15(createSecurityPolicyQName("TripleDesSha256Rsa15")),  //NOI18N
    INCLUSIVEC14N(createSecurityPolicyQName("InclusiveC14N")),  //NOI18N
    SOAPNORMALIZATION10(createSecurityPolicyQName("SOAPNormalization10")),  //NOI18N
    STRTRANSFORM10(createSecurityPolicyQName("STRTransform10")),  //NOI18N
    XPATH10(createSecurityPolicyQName("XPath10")),  //NOI18N
    XPATHFILTER20(createSecurityPolicyQName("XPathFilter20"));  //NOI18N

    public static final String SECPOLICY_UTILITY = 
            "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy"; //NOI18N
    public static final String SECPOLICY_UTILITY_NS_PREFIX = "sp";         //NOI18N
            
    public static QName createSecurityPolicyQName(String localName){
        return new QName(SECPOLICY_UTILITY, localName, SECPOLICY_UTILITY_NS_PREFIX);
    }
    
    AlgorithmSuiteQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (AlgorithmSuiteQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

}
