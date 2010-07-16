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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.ldap.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.ldap.LDAPAddress;
import org.netbeans.modules.wsdlextensions.ldap.LDAPComponent;
import org.netbeans.modules.wsdlextensions.ldap.LDAPQName;
import org.w3c.dom.Element;

/**
 */
public class LDAPAddressImpl extends LDAPComponentImpl implements LDAPAddress {
    public LDAPAddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public LDAPAddressImpl(WSDLModel model){
        this(model, createPrefixedElement(LDAPQName.ADDRESS.getQName(), model));
    }
    
    public void accept(LDAPComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public void setLocation(String loc) {
        setAttribute(LDAPAddress.LDAP_LOCATION_PROPERTY, LDAPAttribute.LDAP_LOCATION_PROPERTY, loc);
    }

    public String getLocation() {
        return getAttribute(LDAPAttribute.LDAP_LOCATION_PROPERTY);
    }

    public void setPrincipal(String str) {
        setAttribute(LDAPAddress.LDAP_PRINCIPAL_PROPERTY, LDAPAttribute.LDAP_PRINCIPAL_PROPERTY, str);
    }

    public String getPrincipal() {
        return getAttribute(LDAPAttribute.LDAP_PRINCIPAL_PROPERTY);
    }

    public void setCredential(String str) {
        setAttribute(LDAPAddress.LDAP_CREDENTIAL_PROPERTY, LDAPAttribute.LDAP_CREDENTIAL_PROPERTY, str);
    }

    public String getCredential() {
        return getAttribute(LDAPAttribute.LDAP_CREDENTIAL_PROPERTY);
    }

    public void setSsltype(String str) {
        setAttribute(LDAPAddress.LDAP_SSLTYPE_PROPERTY, LDAPAttribute.LDAP_SSLTYPE_PROPERTY, str);
    }

    public String getSsltype() {
        return getAttribute(LDAPAttribute.LDAP_SSLTYPE_PROPERTY);
    }

    public void setAuthentication(String str) {
        setAttribute(LDAPAddress.LDAP_AUTHENTICATION_PROPERTY, LDAPAttribute.LDAP_AUTHENTICATION_PROPERTY, str);
    }

    public String getAuthentication() {
        return getAttribute(LDAPAttribute.LDAP_AUTHENTICATION_PROPERTY);
    }

    public void setProtocol(String str) {
        setAttribute(LDAPAddress.LDAP_PROTOCOL_PROPERTY, LDAPAttribute.LDAP_PROTOCOL_PROPERTY, str);
    }

    public String getProtocol() {
        return getAttribute(LDAPAttribute.LDAP_PROTOCOL_PROPERTY);
    }

    public void setTruststore(String str) {
        setAttribute(LDAPAddress.LDAP_TRUSTSTORE_PROPERTY, LDAPAttribute.LDAP_TRUSTSTORE_PROPERTY, str);
    }

    public String getTruststore() {
        return getAttribute(LDAPAttribute.LDAP_TRUSTSTORE_PROPERTY);
    }

    public void setTruststorepassword(String str) {
        setAttribute(LDAPAddress.LDAP_TRUSTSTOREPASSWORD_PROPERTY, LDAPAttribute.LDAP_TRUSTSTOREPASSWORD_PROPERTY, str);
    }

    public String getTruststorepassword() {
        return getAttribute(LDAPAttribute.LDAP_TRUSTSTOREPASSWORD_PROPERTY);
    }

    public void setTruststoretype(String str) {
        setAttribute(LDAPAddress.LDAP_TRUSTSTORETYPE_PROPERTY, LDAPAttribute.LDAP_TRUSTSTORETYPE_PROPERTY, str);
    }

    public String getTruststoretype() {
        return getAttribute(LDAPAttribute.LDAP_TRUSTSTORETYPE_PROPERTY);
    }

    public void setKeystore(String str) {
        setAttribute(LDAPAddress.LDAP_KEYSTORE_PROPERTY, LDAPAttribute.LDAP_KEYSTORE_PROPERTY, str);
    }

    public String getKeystore() {
        return getAttribute(LDAPAttribute.LDAP_KEYSTORE_PROPERTY);
    }

    public void setKeystoreusername(String str) {
        setAttribute(LDAPAddress.LDAP_KEYSTOREUSERNAME_PROPERTY, LDAPAttribute.LDAP_KEYSTOREUSERNAME_PROPERTY, str);
    }

    public String getKeystoreusername() {
        return getAttribute(LDAPAttribute.LDAP_KEYSTOREUSERNAME_PROPERTY);
    }

    public void setKeystorepassword(String str) {
        setAttribute(LDAPAddress.LDAP_KEYSTOREPASSWORD_PROPERTY, LDAPAttribute.LDAP_KEYSTOREPASSWORD_PROPERTY, str);
    }

    public String getKeystorepassword() {
        return getAttribute(LDAPAttribute.LDAP_KEYSTOREPASSWORD_PROPERTY);
    }
    
    public void setKeystoretype(String str) {
        setAttribute(LDAPAddress.LDAP_KEYSTORETYPE_PROPERTY, LDAPAttribute.LDAP_KEYSTORETYPE_PROPERTY, str);
    }

    public String getKeystoretype() {
        return getAttribute(LDAPAttribute.LDAP_KEYSTORETYPE_PROPERTY);
    }

    public String getTlssecurity() {
        return getAttribute(LDAPAttribute.LDAP_TLSSECURITY_PROPERTY);
    }

    public void setTlssecurity(String tlssecurity) {
        setAttribute(LDAPAddress.LDAP_TLSSECURITY_PROPERTY, LDAPAttribute.LDAP_TLSSECURITY_PROPERTY, tlssecurity);
    }
}
