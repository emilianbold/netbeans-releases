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

package org.netbeans.modules.wsdlextensions.jms;

/**
 *
 * JMSAddress
 */
public interface JMSAddress extends JMSComponent {

    public static final String ATTR_CONNECTION_URL = "connectionURL";
    public static final String ATTR_USERNAME = "username";
    public static final String ATTR_PASSWORD = "password";
    public static final String ATTR_JNDI_CONNECTION_FACTORY_NAME = "connectionFactoryName";
    public static final String ATTR_JNDI_INITIAL_CONTEXT_FACTORY = "initialContextFactory";
    public static final String ATTR_JNDI_PROVIDER_URL            = "providerURL";
    public static final String ATTR_JNDI_SECURITY_PRINCIPAL      = "securityPrincipal";
    public static final String ATTR_JNDI_SECURITY_CRDENTIALS     = "securityCredentials";


    public String getConnectionURL();
    public void setConnectionURL(String val);
    
    public String getUsername();
    public void setUsername(String val);

    public String getPassword();
    public void setPassword(String val);    
    
    public String getConnectionFactoryName();
    public void setConnectionFactoryName(String val);    
    
    public String getInitialContextFactory();
    public void setInitialContextFactory(String val);

    public String getProviderURL();
    public void setProviderURL(String val);

    public String getSecurityPrincial();
    public void setSecurityPrincipal(String val);

    public String getSecurityCredentials();
    public void setSecurityCredentials(String val);
    
}
