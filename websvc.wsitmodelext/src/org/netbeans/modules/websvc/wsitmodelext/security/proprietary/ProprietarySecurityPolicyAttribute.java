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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary;

import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author Martin Grebac
 */
public enum ProprietarySecurityPolicyAttribute implements Attribute {
        VISIBILITY("visibility"),       //NOI18N
        DEFAULT("default"),       //NOI18N
        ENCRYPTISSUEDKEY("encryptIssuedKey"),       //NOI18N
        ENCRYPTISSUEDTOKEN("encryptIssuedToken"),   //NOI18N
        ENDPOINT("endPoint"),                       //NOI18N
        METADATA("metadata"),                       //NOI18N
        WSDLLOCATION("wsdlLocation"),               //NOI18N
        SERVICENAME("serviceName"),                 //NOI18N
        PORTNAME("portName"),                       //NOI18N
        NAMESPACE("namespace"),                     //NOI18N
        TIMEOUT("timeout"),                         //NOI18N
        REQUIRECANCELSCT("requireCancelSCT"),       //NOI18N
        RENEWEXPIREDSCT("renewExpiredSCT"),         //NOI18N
        LOCATION("location"),               //NOI18N
        ALIAS("alias"),                     //NOI18N
        STSALIAS("stsalias"),               //NOI18N
        PEERALIAS("peeralias"),             //NOI18N
        TYPE("type"),                       //NOI18N
        KEYPASS("keypass"),                 //NOI18N
        STOREPASS("storepass"),             //NOI18N
        NAME("name"),                       //NOI18N
        CLASSNAME("classname");             //NOI18N
    
    private String name;
    private Class type;
    private Class subtype;
    
    /**
     * Creates a new instance of ProprietarySecurityPolicyAttribute
     */
    ProprietarySecurityPolicyAttribute(String name) {
        this(name, String.class);
    }
    ProprietarySecurityPolicyAttribute(String name, Class type) {
        this(name, type, null);
    }
    ProprietarySecurityPolicyAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    @Override
    public String toString() { return name; }

    public Class getType() {
        return type;
    }

    public String getName() { return name; }

    public Class getMemberType() { return subtype; }
}
