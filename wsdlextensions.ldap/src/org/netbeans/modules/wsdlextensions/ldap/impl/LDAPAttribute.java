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

import java.util.Collection;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * @author 
 *
 */
public enum LDAPAttribute implements Attribute {

    LDAP_OPERATIONTYPE_PROPERTY("operationType"),
    LDAP_RETPARTNAME_PROPERTY("returnPartName"),
    LDAP_ATTRIBUTES_PROPERTY("attributes"),
    LDAP_LOCATION_PROPERTY("location"),
    LDAP_PRINCIPAL_PROPERTY("principal"),
    LDAP_CREDENTIAL_PROPERTY("credential"),
    LDAP_SSLTYPE_PROPERTY("ssltype"),
    LDAP_AUTHENTICATION_PROPERTY("authentication"),
    LDAP_PROTOCOL_PROPERTY("protocol"),
    LDAP_TRUSTSTORE_PROPERTY("truststore"),
    LDAP_TRUSTSTOREPASSWORD_PROPERTY("truststorepassword"),
    LDAP_TRUSTSTORETYPE_PROPERTY("truststoretype"),
    LDAP_KEYSTORE_PROPERTY("keystore"),
    LDAP_KEYSTOREUSERNAME_PROPERTY("keystoreusername"),
    LDAP_KEYSTOREPASSWORD_PROPERTY("keystorepassword"),
    LDAP_TLSSECURITY_PROPERTY("tlssecurity"),
    LDAP_KEYSTORETYPE_PROPERTY("keystoretype");
    private String name;
    private Class type;
    private Class subtype;

    LDAPAttribute(String name) {
        this(name, String.class);
    }

    LDAPAttribute(String name, Class type) {
        this(name, type, null);
    }

    LDAPAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }

    public String toString() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Class getMemberType() {
        return subtype;
    }
}
