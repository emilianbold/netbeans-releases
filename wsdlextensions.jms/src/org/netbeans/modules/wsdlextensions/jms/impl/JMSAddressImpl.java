/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.wsdlextensions.jms.impl;

import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSConstants;
import org.netbeans.modules.wsdlextensions.jms.JMSQName;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * JMSAddressImpl
 */
public class JMSAddressImpl extends JMSComponentImpl implements JMSAddress {

    public JMSAddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public JMSAddressImpl(WSDLModel model){
        this(model, createPrefixedElement(JMSQName.ADDRESS.getQName(), model));
    }

    public void setConnectionURL(String val) {
        setAttribute(JMSAddress.ATTR_CONNECTION_URL, 
                     JMSAttribute.JMS_ADDRESS_CONNECTION_URL,
                     val);
    }
    
    public String getConnectionURL() {
        return getAttribute(JMSAttribute.JMS_ADDRESS_CONNECTION_URL);
    }
        
    public String getUsername() {
        return getAttribute(JMSAttribute.JMS_ADDRESS_USERNAME);        
    }
    
    public void setUsername(String val) {
        setAttribute(JMSAddress.ATTR_USERNAME, 
                     JMSAttribute.JMS_ADDRESS_USERNAME,
                     val);                
    }

    public String getPassword() {
        return getAttribute(JMSAttribute.JMS_ADDRESS_PASSWORD);        
    }
    
    public void setPassword(String val) {
        setAttribute(JMSAddress.ATTR_PASSWORD, 
                     JMSAttribute.JMS_ADDRESS_PASSWORD,
                     val);        
    }

    public String getConnectionFactoryName() {
        return getAttribute(JMSAttribute.JMS_ADDRESS_JNDI_CONNECTIONFACTORY_NAME);         
    }

    public void setConnectionFactoryName(String val) {
        setAttribute(JMSAddress.ATTR_JNDI_CONNECTION_FACTORY_NAME, 
                     JMSAttribute.JMS_ADDRESS_JNDI_CONNECTIONFACTORY_NAME,
                     val);                
    }

    public String getInitialContextFactory() {
        return getAttribute(JMSAttribute.JMS_ADDRESS_JNDI_INITIAL_CONTEXT_FACTORY);         
    }

    public void setInitialContextFactory(String val) {
        setAttribute(JMSAddress.ATTR_JNDI_INITIAL_CONTEXT_FACTORY, 
                     JMSAttribute.JMS_ADDRESS_JNDI_INITIAL_CONTEXT_FACTORY,
                     val);                
    }

    public String getProviderURL() {
        return getAttribute(JMSAttribute.JMS_ADDRESS_JNDI_PROVIDER_URL);         
    }

    public void setProviderURL(String val) {
        setAttribute(JMSAddress.ATTR_JNDI_PROVIDER_URL, 
                     JMSAttribute.JMS_ADDRESS_JNDI_PROVIDER_URL,
                     val);                
    }

    public String getSecurityPrincial() {
        return getAttribute(JMSAttribute.JMS_ADDRESS_JNDI_SECURITY_PRINCIPAL);         
    }

    public void setSecurityPrincipal(String val) {
        setAttribute(JMSAddress.ATTR_JNDI_SECURITY_PRINCIPAL, 
                     JMSAttribute.JMS_ADDRESS_JNDI_SECURITY_PRINCIPAL,
                     val);                
    }

    public String getSecurityCredentials() {
        return getAttribute(JMSAttribute.JMS_ADDRESS_JNDI_SECURITY_CREDENTIALS);         
    }

    public void setSecurityCredentials(String val) {
        setAttribute(JMSAddress.ATTR_JNDI_SECURITY_CRDENTIALS, 
                     JMSAttribute.JMS_ADDRESS_JNDI_SECURITY_CREDENTIALS,
                     val);                
    }
}
