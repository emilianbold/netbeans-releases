/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ldap.properties;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author anjeleevich
 */
public class ConnectionProperties implements Cloneable {
    private String connectionName;

    private String host;

    private int port; // 1, 65535 // 0 - default
    private boolean useSSL;

    private AuthenticationType authenticationType;
    private String user;
    private char[] password;
    private boolean savePassword;

    private String baseDN;

    private int searchCountLimit = 0; // 0 - unlimited
    private int browseCountLimit = 0; // 0 - unlimited

    public ConnectionProperties() {
        
    }

    public ConnectionProperties(Document document) throws Exception {
        NodeList nodes = document.getElementsByTagName(CONNECTION_NAME_ELEMENT);
        connectionName = nodes.item(0).getTextContent();

        nodes = document.getElementsByTagName(HOST_ELEMENT);
        Element hostElement = (Element) nodes.item(0);

        host = hostElement.getTextContent();
        
        useSSL = Boolean.parseBoolean(hostElement.getAttribute(USE_SSL_ATTR));

        String portAttributeValue = hostElement.getAttribute(PORT_ATTR);
        if (DEFAULT_PORT_ATTR_VALUE.equalsIgnoreCase(portAttributeValue)) {
            port = 0;
        } else {
            port = Integer.parseInt(portAttributeValue);
        }

        nodes = document.getElementsByTagName(SECURITY_ELEMENT);
        Element securityElement = (Element) nodes.item(0);

        authenticationType = AuthenticationType.fromXMLValue(securityElement
                .getAttribute(SECURITY_METHOD_ATTR));

        if (authenticationType == AuthenticationType.SIMPLE_AUTHENTICATION) {
            nodes = securityElement.getElementsByTagName(USER_ELEMENT);
            user = nodes.item(0).getTextContent();

            nodes = securityElement.getElementsByTagName(PASSWORD_ELEMENT);
            if (nodes.getLength() == 0) {
                password = null;
                savePassword = false;
            } else {
                password = ((Element) nodes.item(0)).getTextContent()
                        .toCharArray();
                savePassword = true;
            }
        }

        nodes = document.getElementsByTagName(OPTIONS_ELEMENT);
        Element optionsElement = (Element) nodes.item(0);

        nodes = optionsElement.getElementsByTagName(BASE_DN_ELEMENT);
        if (nodes.getLength() > 0) {
            baseDN = ((Element) nodes.item(0)).getTextContent();
        }

        nodes = optionsElement.getElementsByTagName(LIMITS_ELEMENT);
        Element limitsElement = (Element) nodes.item(0);

        searchCountLimit = Integer.parseInt(limitsElement
                .getAttribute(SEARCH_COUNT_LIMIT_ATTR));
        browseCountLimit = Integer.parseInt(limitsElement
                .getAttribute(BROWSE_COUNT_LIMIT_ATTR));
    }

    public ConnectionProperties(ConnectionProperties properties) {
        connectionName = properties.getConnectionName();
        
        host = properties.getHost();
        port = properties.getPort();
        useSSL = properties.isUseSSL();

        authenticationType = properties.getAuthenticationType();
        user = properties.getUser();
        password = properties.getPassword();
        savePassword = properties.isSavePassword();

        baseDN = properties.getBaseDN();
        searchCountLimit = properties.getSearchCountLimit();
        browseCountLimit = properties.getBrowseCountLimit();
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public boolean hasBaseDN() {
        return (baseDN != null) && (baseDN.trim().length() > 0);
    }

    public String getBaseDN() {
        return baseDN;
    }

    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    public int getBrowseCountLimit() {
        return browseCountLimit;
    }

    public void setBrowseCountLimit(int browseCountLimit) {
        this.browseCountLimit = browseCountLimit;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public char[] getPassword() {
        if (password == null) {
            return null;
        }

        char[] result = new char[password.length];
        System.arraycopy(password, 0, result, 0, password.length);

        return result;
    }

    public void setPassword(char[] password) {
        if (password == null) {
            this.password = null;
        } else {
            this.password = new char[password.length];
            System.arraycopy(password, 0, this.password, 0, password.length);
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSavePassword() {
        return savePassword;
    }

    public void setSavePassword(boolean savePassword) {
        this.savePassword = savePassword;
    }

    public int getSearchCountLimit() {
        return searchCountLimit;
    }

    public void setSearchCountLimit(int searchCountLimit) {
        this.searchCountLimit = searchCountLimit;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public ConnectionProperties clone() throws CloneNotSupportedException {
        return (ConnectionProperties) super.clone();
    }

    public Document toXMLDocument() throws ParserConfigurationException, 
            DOMException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document result = builder.newDocument();

        // root
        Element rootElement = result.createElement(ROOT_ELEMENT);
        result.appendChild(rootElement);

        // connection name
        Element nameElement = result.createElement(CONNECTION_NAME_ELEMENT);
        nameElement.setTextContent(connectionName);
        rootElement.appendChild(nameElement);

        // host
        Element hostElement = result.createElement(HOST_ELEMENT);
        hostElement.setTextContent(host);
        rootElement.appendChild(hostElement);

        // use ssl
        Attr useSSLAttribute = result.createAttribute(USE_SSL_ATTR);
        useSSLAttribute.setValue(Boolean.toString(useSSL));
        hostElement.setAttributeNode(useSSLAttribute);

        // port
        Attr portAttribute = result.createAttribute(PORT_ATTR);
        portAttribute.setValue((port == 0) ? DEFAULT_PORT_ATTR_VALUE
                : Integer.toString(port));
        hostElement.setAttributeNode(portAttribute);

        // security
        Element securityElement = result.createElement(SECURITY_ELEMENT);
        rootElement.appendChild(securityElement);

        // method
        Attr methodAttribute = result.createAttribute(SECURITY_METHOD_ATTR);
        methodAttribute.setValue(authenticationType.getXMLValue());
        securityElement.setAttributeNode(methodAttribute);

        if (authenticationType != AuthenticationType.NO_AUTHENTICATION) {
            Element userElement = result.createElement(USER_ELEMENT);
            userElement.setTextContent(user);
            securityElement.appendChild(userElement);

            if (savePassword) {
                Element passwordElement = result.createElement(PASSWORD_ELEMENT);
                passwordElement.setTextContent(new String(password));
                securityElement.appendChild(passwordElement);
            }
        }

        Element optionsElement = result.createElement(OPTIONS_ELEMENT);
        rootElement.appendChild(optionsElement);

        if (baseDN != null && baseDN.trim().length() > 0) {
            Element baseDNElement = result.createElement(BASE_DN_ELEMENT);
            baseDNElement.setTextContent(baseDN);
            optionsElement.appendChild(baseDNElement);
        }

        Element limitsElement = result.createElement(LIMITS_ELEMENT);
        optionsElement.appendChild(limitsElement);

        Attr searchCountLimitAttribute = result.createAttribute(
                SEARCH_COUNT_LIMIT_ATTR);
        searchCountLimitAttribute.setValue(Integer.toString(searchCountLimit));
        limitsElement.setAttributeNode(searchCountLimitAttribute);

        Attr browseCountLimitAttribute = result.createAttribute(
                BROWSE_COUNT_LIMIT_ATTR);
        browseCountLimitAttribute.setValue(Integer.toString(browseCountLimit));
        limitsElement.setAttributeNode(browseCountLimitAttribute);

        return result;
    }

    public String getURL() {
        StringBuilder builder = new StringBuilder();
        if (isUseSSL()) {
            builder.append("ldaps://");
        } else {
            builder.append("ldap://");
        }

        builder.append(getHost());

        if (getPort() != 0) {
            builder.append(":");
            builder.append(getPort());
        }

        String baseDName = getBaseDN();
        if (baseDName != null) {
            baseDName = baseDName.trim();
            if (baseDName.length() > 0) {
                builder.append("/");
                builder.append(baseDName);
            }
        }

        return builder.toString();
    }

    public DirContext createDirContext() throws NamingException {
        Hashtable<String, Object> environment = new Hashtable<String, Object>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory"); // NOI18N

        environment.put(Context.PROVIDER_URL, getURL());

        if (getAuthenticationType() == AuthenticationType.SIMPLE_AUTHENTICATION)
        {
            environment.put(Context.SECURITY_AUTHENTICATION, "simple"); // NOI18N
            environment.put(Context.SECURITY_PRINCIPAL, getUser());
            environment.put(Context.SECURITY_CREDENTIALS, new String(getPassword()));
        }

        return new InitialDirContext(environment);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ConnectionProperties)) {
            return false;
        }

        ConnectionProperties properties = (ConnectionProperties) obj;

        return getChangedProperties(properties).isEmpty();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 53 * hash + this.port;
        hash = 53 * hash + (this.useSSL ? 1 : 0);
        hash = 53 * hash + (this.baseDN != null ? this.baseDN.hashCode() : 0);
        return hash;
    }

    public Set<ConnectionPropertyType> getChangedProperties(
            ConnectionProperties properties)
    {
        Set<ConnectionPropertyType> result
                = new HashSet<ConnectionPropertyType>();

        if (!equal(this.connectionName, properties.connectionName)) {
            result.add(ConnectionPropertyType.CONNECTION_NAME);
        }

        if (!equal(this.host, properties.host)) {
            result.add(ConnectionPropertyType.HOST);
        }

        if (this.useSSL != properties.useSSL) {
            result.add(ConnectionPropertyType.USE_SSL);
        }

        if (this.port != properties.port) {
            result.add(ConnectionPropertyType.PORT);
        }

        if (!equal(this.baseDN, properties.baseDN)) {
            result.add(ConnectionPropertyType.BASE_DN);
        }

        if (this.searchCountLimit != properties.searchCountLimit) {
            result.add(ConnectionPropertyType.SEARCH_COUNT_LIMIT);
        }

        if (this.browseCountLimit != properties.browseCountLimit) {
            result.add(ConnectionPropertyType.BROWSE_COUNT_LIMIT);
        }

        if (this.authenticationType != properties.authenticationType) {
            result.add(ConnectionPropertyType.AUTHENTICATION_TYPE);
        } else if (this.authenticationType == AuthenticationType
                .SIMPLE_AUTHENTICATION)
        {
            if (!equal(this.user, properties.user)) {
                result.add(ConnectionPropertyType.USER);
            }

            if (this.savePassword != properties.savePassword) {
                result.add(ConnectionPropertyType.SAVE_PASSWORD);
            }

            if (!equalPassword(this.password, properties.password)) {
                result.add(ConnectionPropertyType.PASSWORD);
            }
        }

        return result;
    }

    private static boolean equal(String s1, String s2) {
        return (s1 == null) ? (s2 == null) : s1.equals(s2);
    }

    private static boolean equalPassword(char[] password1, char[] password2) {
        int length1 = (password1 == null) ? 0 : password1.length;
        int length2 = (password2 == null) ? 0 : password2.length;

        if (length1 != length2) {
            return false;
        }

        for (int i = 0; i < length1; i++) {
            if (password1[i] != password2[i]) {
                return false;
            }
        }

        return true;
    }

    private static final String ROOT_ELEMENT = "ldap-connection"; // NOI18N

    private static final String CONNECTION_NAME_ELEMENT = "connection-name"; // NOI18N

    private static final String HOST_ELEMENT = "host"; // NOI18N
    private static final String PORT_ATTR = "port"; // NOI18N
    private static final String DEFAULT_PORT_ATTR_VALUE = "default"; // NOI18N
    private static final String USE_SSL_ATTR = "use-ssl"; // NOI18N

    private static final String SECURITY_ELEMENT = "security"; // NOI18N
    private static final String SECURITY_METHOD_ATTR = "method"; // NOI18N
    private static final String USER_ELEMENT = "user"; // NOI18N
    private static final String PASSWORD_ELEMENT = "password"; // NOI18N

    private static final String OPTIONS_ELEMENT = "options"; // NOI18N
    private static final String BASE_DN_ELEMENT = "base-dn"; // NOI18N
    private static final String LIMITS_ELEMENT = "limits"; // NOI18N
    private static final String SEARCH_COUNT_LIMIT_ATTR = "search-count"; // NOI18N
    private static final String BROWSE_COUNT_LIMIT_ATTR = "browse-count"; // NOI18N
}