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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
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
