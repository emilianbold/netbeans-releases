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
package org.netbeans.modules.wsdlextensions.ldap;

/**
 *
 * Represents the address element under the wsdl port for LDAP binding
 * @author
 */
public interface LDAPAddress extends LDAPComponent {

    static final String LDAP_LOCATION_PROPERTY = "location";
    static final String LDAP_PRINCIPAL_PROPERTY = "principal";
    static final String LDAP_CREDENTIAL_PROPERTY = "credential";
    static final String LDAP_SSLTYPE_PROPERTY = "ssltype";
    static final String LDAP_AUTHENTICATION_PROPERTY = "authentication";
    static final String LDAP_PROTOCOL_PROPERTY = "protocol";
    static final String LDAP_TRUSTSTORE_PROPERTY = "truststore";
    static final String LDAP_TRUSTSTOREPASSWORD_PROPERTY = "truststorepassword";
    static final String LDAP_TRUSTSTORETYPE_PROPERTY = "truststoretype";
    static final String LDAP_KEYSTORE_PROPERTY = "keystore";
    static final String LDAP_KEYSTOREUSERNAME_PROPERTY = "keystoreusername";
    static final String LDAP_KEYSTOREPASSWORD_PROPERTY = "keystorepassword";
    static final String LDAP_KEYSTORETYPE_PROPERTY = "keystoretype";
    static final String LDAP_TLSSECURITY_PROPERTY = "tlssecurity";

    public String getTlssecurity();

    public void setTlssecurity(String tlssecurity);

    public void setLocation(String loc);

    public String getLocation();

    public void setPrincipal(String str);

    public String getPrincipal();

    public void setCredential(String str);

    public String getCredential();

    public void setSsltype(String str);

    public String getSsltype();

    public void setAuthentication(String str);

    public String getAuthentication();

    public void setProtocol(String str);

    public String getProtocol();

    public void setTruststore(String str);

    public String getTruststore();

    public void setTruststorepassword(String str);

    public String getTruststorepassword();

    public void setTruststoretype(String str);

    public String getTruststoretype();

    public void setKeystore(String str);

    public String getKeystore();

    public void setKeystoreusername(String str);

    public String getKeystoreusername();

    public void setKeystorepassword(String str);

    public String getKeystorepassword();

    public void setKeystoretype(String str);

    public String getKeystoretype();
}
