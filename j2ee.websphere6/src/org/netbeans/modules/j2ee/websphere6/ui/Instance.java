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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.websphere6.ui;

/**
 * A model for the server instance. It contains all the critical properties
 * for the plugin: name, host, port, profile path.
 *
 * @author Kirill Sorokin
 * @author Dmitry Lipin
 */
public final class Instance {
    /**
     * Instance's name, it is used a the parameter to the startup/shutdown
     * scripts
     */
    private String name;

    /**
     * Instance's host
     */
    private String host;

    /**
     * Instance's port
     */
    private String port;

    /**
     * Instance's profile directory
     */
    private String domainPath;
    
    /**
     * Path to the server.xml file
     */
    private String configXmlPath;
    
    private String adminPort;
    
    private String httpPort;
    
    private String defaultHostPort;
    
    private final boolean securityEnabled;
    
    /**
     * Creates a new instance of Instance
     *
     * @param name the instance's name
     * @param host the instance's host
     * @param port the instance's port
     * @param domainPath the instance's profile path
     * @param configXmlPath path to the server.xml file
     */
    public Instance(String name, String host, String port,
            String domainPath, String configXmlPath, String adminPort,
            String httpPort, String defaultHostPort, boolean securityEnabled) {
        // save the properties
        this.name = name;
        this.host = host;
        this.port = port;
        this.domainPath = domainPath;
        this.configXmlPath = configXmlPath;
        this.adminPort = adminPort;
        this.httpPort = httpPort;
        this.defaultHostPort = defaultHostPort;
        this.securityEnabled = securityEnabled;
    }
    
    /**
     * Getter for the instance's name
     *
     * @return the instance's name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Setter for the instance's name
     *
     * @param the new instance's name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Getter for the instance's host
     *
     * @return the instance's host
     */
    public String getHost() {
        return this.host;
    }
    
    /**
     * Setter for the instance's host
     *
     * @param the new instance's host
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * Getter for the instance's port
     *
     * @return the instance's port
     */
    public String getPort() {
        return this.port;
    }
    
    /**
     * Setter for the instance's port
     *
     * @param the new instance's port
     */
    public void setPort(String port) {
        this.port = port;
    }
    
    /**
     * Getter for the instance's profile path
     *
     * @return the instance's profile path
     */
    public String getDomainPath() {
        return this.domainPath;
    }
    
    /**
     * Setter for the instance's profile path
     *
     * @param the new instance's profile path
     */
    public void setDomainPath(String domainPath) {
        this.domainPath = domainPath;
    }
    
    /**
     * Getter for the path to server's config xml file
     *
     * @return the server's config xml file
     */
    public String getConfigXmlPath() {
        return this.configXmlPath;
    }
    
    /**
     * Setter for the server's config xml file
     *
     * @param the new server's config xml file
     */
    public void setConfigXmlPath(String configXmlPath) {
        this.configXmlPath = configXmlPath;
    }
    /**
     * Getter for the server's admin port
     *
     * @return the server's admin port
     */
    public String getAdminPort() {
        return adminPort;
    }
    /**
     * Setter for the server's admin port
     *
     * @param the server's admin port
     */
    public void setAdminPort(String adminPort) {
        this.adminPort = adminPort;
    }
    
    /**
     * Getter for the server's default host port
     *
     * @return the server's default host port
     */
    public String getDefaultHostPort() {
        return defaultHostPort;
    }
    /**
     * Setter for the server's deafult host port
     *
     * @param the server's default host port
     */
    public void setDefaultHostPort(String defaultHostPort) {
        this.defaultHostPort = defaultHostPort;
    }
    
    
    
     public String getHttpPort() {
        return httpPort;
    }
    
    public void setHttpPort(String adminPort) {
        this.httpPort = adminPort;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }
        
    /**
     * An overriden version of the Object's toString() so that the
     * instance is displayed properly in the combobox
     */
    public String toString() {
        return name + " [" + host + ":" + port + "]"; // NOI18N
    }
}

