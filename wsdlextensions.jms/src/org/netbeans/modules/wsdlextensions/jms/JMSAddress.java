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
