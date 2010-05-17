/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.test.helpers;

/**
 * Used to check JMX Manager wizard values.
 */
public class Manager {

    public static String DEFAULT_PROTOCOL = "RMI JVM Agent";
    public static String DEFAULT_HOST = "localhost";
    public static String DEFAULT_PORT = "1099";
    public static String DEFAULT_PATH = "/jndi/rmi://localhost:1099/jmxrmi";
    
    private String name = "";
    
    // Variables initialized with wizard default values
    private String agentURL = "";
    private String protocol = DEFAULT_PROTOCOL;
    private String host = DEFAULT_HOST;
    private String port = DEFAULT_PORT;
    private String path = DEFAULT_PATH;
    private String userName = "";
    private String password = "";
    private boolean generateMainMethod = true;
    private boolean projectMainClass = true;
    private boolean generateSampleDiscoveryCode = true;
    private boolean authenticatedConnection = true;
    private boolean generateSampleConnectionCode = true;
    
    /** Creates a new instance of Manager with default values */
    public Manager() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String s) {
        this.name = s;
    }
    
    public String getAgentURL() {
        return agentURL;
    }
    
    /**
     * The agent URL text field is not editable.
     * It is updated using the agent URL popup values.
     */
    public void updateAgentURL() {
        if (this.protocol.equals(DEFAULT_PROTOCOL)) {
            this.agentURL = "service:jmx:rmi://" + host + ":" + port + path;
        } else {
            this.agentURL = "service:jmx:" + protocol + "://" + host + ":" + port + path;
        }
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String s) {
        this.protocol = s;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String s) {
        this.host = s;
    }
    
    public String getPort() {
        return port;
    }
    
    public void setPort(String s) {
        this.port = s;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String s) {
        this.path = s;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String s) {
        this.userName = s;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String s) {
        this.password = s;
    }
    
    public boolean getGenerateMainMethod() {
        return generateMainMethod;
    }
    
    public void setGenerateMainMethod(boolean b) {
        this.generateMainMethod = b;
        if (this.generateMainMethod == false) {
            this.projectMainClass = false;
            this.generateSampleDiscoveryCode = false;
        }
    }
    
    public boolean getProjectMainClass() {
        return projectMainClass;
    }
    
    public void setProjectMainClass(boolean b) {
        this.projectMainClass = b;
    }
    
    public boolean getGenerateSampleDiscoveryCode() {
        return generateSampleDiscoveryCode;
    }
    
    public void setGenerateSampleDiscoveryCode(boolean b) {
        this.generateSampleDiscoveryCode = b;
    }
    
    public boolean getAuthenticatedConnection() {
        return authenticatedConnection;
    }
    
    public void setAuthenticatedConnection(boolean b) {
        this.authenticatedConnection = b;
    }
    
    public boolean getGenerateSampleConnectionCode() {
        return generateSampleConnectionCode;
    }
    
    public void setGenerateSampleConnectionCode(boolean b) {
        this.generateSampleConnectionCode = b;
    }
    
    public boolean getGenerateCredentialsConnectionCode() {
        return !generateSampleConnectionCode;
    }
    
    public void setGenerateCredentialsConnectionCode(boolean b) {
        this.generateSampleConnectionCode = !b;
    }
}
