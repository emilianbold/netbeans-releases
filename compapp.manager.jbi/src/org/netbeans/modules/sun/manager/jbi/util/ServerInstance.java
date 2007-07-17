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

package org.netbeans.modules.sun.manager.jbi.util;

import java.io.Serializable;
import java.util.Properties;


/**
 * DOCUMENT ME!
 *
 * @author Graj TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
/* 
<attributes version="1.0">
    <fileobject name="instance">
        <attr name="DOMAIN" stringvalue="domain1"/>
        <attr name="HttpMonitorOn" stringvalue="true"/>
        <attr name="HttpProxySynced" stringvalue="true"/>
        <attr name="LOCATION" stringvalue="C:\Glassfish-v2-b54\glassfish\domains"/>
        <attr name="PROP_INSTANCE_ID" stringvalue="glassfish"/>
        <attr name="displayName" stringvalue="GlassFish V2 (b54)"/>
        <attr name="httpportnumber" stringvalue="8080"/>
        <attr name="password" stringvalue="adminadmin"/>
        <attr name="url" stringvalue="[C:\Glassfish-v2-b54\glassfish]deployer:Sun:AppServer::localhost:4848"/>
        <attr name="username" stringvalue="admin"/>
    </fileobject>
    <fileobject name="instance_1">
        <attr name="DOMAIN" stringvalue="GFb54_PersonalDomain2"/>
        <attr name="HttpMonitorOn" stringvalue="true"/>
        <attr name="HttpProxySynced" stringvalue="true"/>
        <attr name="LOCATION" stringvalue="C:\tmp"/>
        <attr name="PROP_INSTANCE_ID" stringvalue="glassfish_1"/>
        <attr name="displayName" stringvalue="GlassFish V2 (b54 personal domain 2)"/>
        <attr name="httpportnumber" stringvalue="8105"/>
        <attr name="password" stringvalue="adminadmin"/>
        <attr name="url" stringvalue="[C:\Glassfish-v2-b54\glassfish]deployer:Sun:AppServer::localhost:4873"/>
        <attr name="username" stringvalue="admin"/>
    </fileobject>
    <fileobject name="instance_2">
        <attr name="DOMAIN" stringvalue=""/>
        <attr name="LOCATION" stringvalue=""/>
        <attr name="displayName" stringvalue="GlassFish V2"/>
        <attr name="httpportnumber" stringvalue="2848"/>
        <attr name="password" stringvalue="adminadmin"/>
        <attr name="url" stringvalue="[C:\Glassfish-v2-b54\glassfish]deployer:Sun:AppServer::cordova.stc.com:2848"/>
        <attr name="username" stringvalue="admin"/>
    </fileobject>
</attributes>
*/
public class ServerInstance implements Serializable {
    
    static final String DISPLAY_NAME = "displayName"; // NOI18N
    static final String DOMAIN = "DOMAIN"; // NOI18N
    static final String HTTP_MONITOR_ON = "HttpMonitorOn"; // NOI18N
    static final String HTTP_PORT_NUMBER = "httpportnumber"; // NOI18N
    static final String LOCATION = "LOCATION"; // NOI18N
    static final String PASSWORD = "password"; // NOI18N
    static final String URL = "url"; // NOI18N
    static final String USER_NAME = "username"; // NOI18N
    
    /*
    private static final String DISPLAY_NAME_PROPERTY_KEY = "com.sun.appserver.instance.displayName"; // NOI18N
    private static final String HOST_NAME_PROPERTY_KEY = "com.sun.appserver.instance.hostName"; // NOI18N
    private static final String ADMINISTRATION_PORT_PROPERTY_KEY = "com.sun.appserver.instance.administrationPort"; // NOI18N
    private static final String DOMAIN_PROPERTY_KEY = "com.sun.appserver.instance.domain"; // NOI18N
    private static final String HTTP_MONITOR_ON_PROPERTY_KEY = "com.sun.appserver.instance.httpMonitorOn"; // NOI18N
    private static final String HTTP_PORT_NUMBER_PROPERTY_KEY = "com.sun.appserver.instance.httpPortNumber"; // NOI18N
    private static final String LOCATION_PROPERTY_KEY = "com.sun.appserver.instance.location"; // NOI18N
    private static final String PASSWORD_PROPERTY_KEY = "com.sun.appserver.instance.password"; // NOI18N
    private static final String URL_PROPERTY_KEY = "com.sun.appserver.instance.url"; // NOI18N
    private static final String USER_NAME_PROPERTY_KEY = "com.sun.appserver.instance.userName"; // NOI18N
    */
    
    private String displayName;
    private String domain;
    private String httpMonitorOn;
    private String httpPortNumber;
    private String location;
    private String password;
    private String url;
    private String userName;
    private String hostName;
    private String adminPort;
    
  
    public ServerInstance() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param displayName
     * @param domain
     * @param httpMonitorOn
     * @param httpPortNumber
     * @param location
     * @param password
     * @param url
     * @param userName
     */
    public ServerInstance(
            String displayName, String domain, String httpMonitorOn, String httpPortNumber,
            String location, String password, String url, String userName
            ) {
        super();
        this.setDisplayName(displayName);
        this.domain = domain;
        this.httpMonitorOn = httpMonitorOn;
        this.httpPortNumber = httpPortNumber;
        this.location = location;
        this.password = password;
        this.url = url;
        this.userName = userName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the displayName.
     */
    public String getDisplayName() {
        return this.displayName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param displayName The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the domain.
     */
    public String getDomain() {
        return this.domain;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param domain The domain to set.
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the httpMonitorOn.
     */
    public String getHttpMonitorOn() {
        return this.httpMonitorOn;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param httpMonitorOn The httpMonitorOn to set.
     */
    public void setHttpMonitorOn(String httpMonitorOn) {
        this.httpMonitorOn = httpMonitorOn;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the httpPortNumber.
     */
    public String getHttpPortNumber() {
        return this.httpPortNumber;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param httpPortNumber The httpPortNumber to set.
     */
    public void setHttpPortNumber(String httpPortNumber) {
        this.httpPortNumber = httpPortNumber;
    }
    
    /**
     * Gets local domain's parent location. For remote domain, this location
     * is empty.
     *
     * @return Returns the location.
     */
    public String getLocation() {
        return this.location;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param location The location to set.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the url.
     */
    public String getUrl() {
        return this.url;
    }
    
    /**
     * Gets the location part from the URL. For remote server, the location 
     * is undefined. The location part from the URL gives the location of 
     * local server installation.
     */
    public String getUrlLocation() {
        String url = getUrl();
        int index = url.indexOf(']');   // NOI18N
        if (index != -1) {
            return url.substring(1, index);
        } else {
            return null;    // unconfigured Tomcat, for example
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param url The url to set.
     */
    public void setUrl(String url) {
        this.url = url;
        
        String[] separator = this.url.split(":"); // NOI18N
        
        int k = separator.length -1;
        
        /* NB5.0 Format changed...
           [C:\alaska\root\jbi\runtime\Sun\AppServer]deployer:Sun:AppServer::localhost:4848
           [/home/tli/SUNWappserver]deployer:Sun:AppServer::localhost:24848"
         */
        this.hostName = separator[k-1];
        this.adminPort = separator[k];
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the userName.
     */
    public String getUserName() {
        return this.userName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the adminPort.
     */
    public String getAdminPort() {
        return this.adminPort;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the hostName.
     */
    public String getHostName() {
        return this.hostName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
    public Properties getProperties() {
        Properties properties = new Properties();
        
        if (this.displayName != null) {
            properties.put(ServerInstance.DISPLAY_NAME_PROPERTY_KEY, this.displayName);
        }
        
        if (this.hostName != null) {
            properties.put(ServerInstance.HOST_NAME_PROPERTY_KEY, this.hostName);
        }
        
        if (this.adminPort != null) {
            properties.put(ServerInstance.ADMINISTRATION_PORT_PROPERTY_KEY, this.adminPort);
        }
        
        if (this.domain != null) {
            properties.put(ServerInstance.DOMAIN_PROPERTY_KEY, this.domain);
        }
        
        if (this.httpMonitorOn != null) {
            properties.put(ServerInstance.HTTP_MONITOR_ON_PROPERTY_KEY, this.httpMonitorOn);
        }
        
        if (this.httpPortNumber != null) {
            properties.put(ServerInstance.HTTP_PORT_NUMBER_PROPERTY_KEY, this.httpPortNumber);
        }
        
        if (this.location != null) {
            properties.put(ServerInstance.LOCATION_PROPERTY_KEY, this.location);
        }
        
        if (this.password != null) {
            properties.put(ServerInstance.PASSWORD_PROPERTY_KEY, this.password);
        }
        
        if (this.url != null) {
            properties.put(ServerInstance.URL_PROPERTY_KEY, this.url);
        }
        
        if (this.userName != null) {
            properties.put(ServerInstance.USER_NAME_PROPERTY_KEY, this.userName);
        }
        
        return properties;
    }
    */
    
    /**
     * DOCUMENT ME!
     */
    public void printOut() {
        System.out.println("/////////////////////////////////////////////////"); // NOI18N
        System.out.println("//   -- Instance values --"); // NOI18N
        System.out.println("/////////////////////////////////////////////////"); // NOI18N
        System.out.println("// displayName is :" + this.displayName); // NOI18N
        System.out.println("// domain is :" + this.domain); // NOI18N
        System.out.println("// httpMonitorOn is :" + this.httpMonitorOn); // NOI18N
        System.out.println("// httpPortNumber is :" + this.httpPortNumber); // NOI18N
        System.out.println("// location is :" + this.location); // NOI18N
        System.out.println("// password is :" + this.password); // NOI18N
        System.out.println("// url is :" + this.url); // NOI18N
        System.out.println("// userName is :" + this.userName); // NOI18N
        System.out.println("// hostName is :" + this.hostName); // NOI18N
        System.out.println("// adminPort is :" + this.adminPort); // NOI18N
        System.out.println("/////////////////////////////////////////////////"); // NOI18N
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
    }
}
