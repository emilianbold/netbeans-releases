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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Martin Grebac
 */
public enum ProprietaryTrustServiceQName {
    STSCONFIGURATION(createTrustServiceQName("STSConfiguration")), //NOI18N
    CONTRACT(createTrustServiceQName("Contract")), //NOI18N
    SERVICEPROVIDER(createTrustServiceQName("ServiceProvider")), //NOI18N
    SERVICEPROVIDERS(createTrustServiceQName("ServiceProviders")), //NOI18N
    CERTALIAS(createTrustServiceQName("CertAlias")), //NOI18N
    TOKENTYPE(createTrustServiceQName("TokenType")), //NOI18N
    KEYTYPE(createTrustServiceQName("KeyType")), //NOI18N
    ISSUER(createTrustServiceQName("Issuer")), //NOI18N
    LIFETIME(createTrustServiceQName("LifeTime")); //NOI18N

    public static final String PROPRIETARY_TRUST_URI = 
            "http://schemas.sun.com/ws/2006/05/trust/server"; //NOI18N
    public static final String PROPRIETARY_TRUST_NS_PREFIX = "tc"; //NOI18N
            
    public static QName createTrustServiceQName(String localName){
        return new QName(PROPRIETARY_TRUST_URI, localName, PROPRIETARY_TRUST_NS_PREFIX);
    }
    
    ProprietaryTrustServiceQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (ProprietaryTrustServiceQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

}
