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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.identity.profile.api.configurator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Custom Properties class for loading and manipulating the AMConfig.properties
 * file.
 *
 * Created on July 5, 2006, 5:39 PM
 *
 * @author ptliu
 */
public class ServerProperties extends Properties implements Cloneable {
    
    public static final String PROP_ID = "org.netbeans.modules.identity.profile.api.configurator.id";   //NOI18N
    
    public static final String PROP_HOST = "com.iplanet.am.server.host";  //NOI18N
    
    public static final String PROP_PORT = "com.iplanet.am.server.port";  //NOI18N
    
    public static final String PROP_CONTEXT_ROOT = "org.netbeans.modules.identity.profile.api.configurator.contextroot";   //NOI18N
    
    public static final String PROP_USERNAME = "com.sun.identity.agents.app.username";  //NOI18N
    
    public static final String PROP_PASSWORD = "com.iplanet.am.service.password";  //NOI18N
    
    private static final String PROP_PROTOCOL = "com.iplanet.am.server.protocol";   //NOI18N
    
    public static final String PROP_AM_CONFIG_FILE = "org.netbeans.modules.identity.profile.api.configurator.amconfigfile"; //NOI18N
    
    public static final String PROP_AM_CONSOLE_URL = "org.netbeans.modules.identity.profile.api.configurator.amconsoleurl";    //NOI18N
    
    private static final String PROP_NAMING_SERVICE_URL = "com.iplanet.am.naming.url";  //NOI18N
    
    public static final String PROP_IS_ALIVE_URL = "org.netbeans.modules.identity.profile.api.configurator.isaliveurl";    //NOI18N
    
    public static final String PROP_LIBERTY_DISCO_SERVICE_URL =
            "org.netbeans.modules.identity.profile.api.configurator.libertydiscoserviceurl"; //NOI18N
    
    public static final String PROP_AS_ROOT = "org.netbeans.modules.identity.profile.api.configurator.asroot";  //NOI18N
    
    private static final String DEFAULT_ID = "Default";         //NOI18N
    
    private static final String DEFAULT_PROTOCOL = "http";      //NOI18N
    
    private static final String DEFAULT_HOST = "localhost";     //NOI18N
    
    private static final String DEFAULT_PORT = "8080";      //NOI18N
    
    private static final String DEFAULT_USERNAME = "amadmin";   //NOI18N
    
    private static final String DEFAULT_PASSWORD = "admin123";  //NOI18N
    
    private static final String AM_CONFIG_FILE = "AM_CONFIG_FILE";   //NOI18N
    
    private static final String AS_DOMAINS = "/domains";  //NOI18N
    
    private static final String NAMING_SERVICE = "/namingservice";  //NOI18N
    
    private static final String IS_ALIVE_JSP = "/isAlive.jsp";      //NOI18N
    
    private static final String LIBERTY_DISCO_SERVICE = "/Liberty/disco";   //NOI18N
    
    
    private boolean isDefault;
    
    /** Creates a new instance of ServerProperties */
    public ServerProperties() {
        super();
        
        String amConfigFile = System.getProperty(AM_CONFIG_FILE);
        //System.out.println("amConfigFile = " + amConfigFile);
        
        // RESOLVE:
        // Need to figure a way to report errors.
        // close the input stream?
        // should cache
        if (amConfigFile != null && amConfigFile.trim().length() != 0) {
            try {
                load(new FileInputStream(amConfigFile.trim()));
                setProperty(PROP_AM_CONFIG_FILE, amConfigFile);
                setProperty(PROP_AS_ROOT, getASRoot(amConfigFile));
                setProperty(PROP_ID, DEFAULT_ID);
                setProperty(PROP_CONTEXT_ROOT, getContextRoot());
                updateURLs();
                isDefault = true;
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public Object setProperty(String key, String value) {
        Object prevValue = super.setProperty(key, value);
        
        updateInternalProperties(key);
        
        return prevValue;
    }
    
    private void updateInternalProperties(String key) {
        if (key.equals(PROP_PORT) || key.equals(PROP_HOST) ||
                key.equals(PROP_PROTOCOL) || key.equals(PROP_CONTEXT_ROOT)) {
            updateURLs();
            isDefault = false;
        } else if (key.equals(PROP_USERNAME) || key.equals(PROP_PASSWORD)) {
            isDefault = false;
        }
    }
    
    private void updateURLs() {
        String port = getProperty(PROP_PORT);
        String host = getProperty(PROP_HOST);
        String protocol = getProperty(PROP_PROTOCOL);
        String contextRoot = getProperty(PROP_CONTEXT_ROOT);
        
        String amConsoleURL = protocol + "://" + host + ":" + port + "/" + contextRoot; //NOI18N
        String namingServiceURL = amConsoleURL + NAMING_SERVICE;
        String isAliveURL = amConsoleURL + IS_ALIVE_JSP;
        String discoURL = amConsoleURL + LIBERTY_DISCO_SERVICE;
        
        setProperty(PROP_AM_CONSOLE_URL, amConsoleURL);
        setProperty(PROP_NAMING_SERVICE_URL, namingServiceURL);
        setProperty(PROP_IS_ALIVE_URL, isAliveURL);
        setProperty(PROP_LIBERTY_DISCO_SERVICE_URL, discoURL);
    }
    
    
    private String getContextRoot() {
        //
        // We get the context root by parsing the naming url.
        // Is there a better way to do this?
        //
        String namingURL = getProperty(PROP_NAMING_SERVICE_URL);
        String adminURL = namingURL.substring(0, namingURL.length() - NAMING_SERVICE.length());
        String contextRoot = adminURL.substring(adminURL.lastIndexOf('/') + 1);
        
        return contextRoot;
    }
    
    private String getASRoot(String amConfigFile) {
        amConfigFile = amConfigFile.replace('\\', '/');
        int index = amConfigFile.indexOf(AS_DOMAINS);
        
        if (index != -1) {
            return amConfigFile.substring(0, index);
        }
        
        return null;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof ServerProperties) {
            ServerProperties properties = (ServerProperties) obj;
            
            if (!getProperty(PROP_HOST).equals(properties.getProperty(PROP_HOST)))
                return false;
            
            if (!getProperty(PROP_PORT).equals(properties.getProperty(PROP_PORT)))
                return false;
            
            if (!getProperty(PROP_USERNAME).equals(properties.getProperty(PROP_USERNAME)))
                return false;
            
            if (!getProperty(PROP_PASSWORD).equals(properties.getProperty(PROP_PASSWORD)))
                return false;
            
            if (!getProperty(PROP_CONTEXT_ROOT).equals(properties.getProperty(PROP_CONTEXT_ROOT)))
                return false;
            
            return true;
            
            /* RESOLVE:  Should we check all the properties
            if (properties.size() != this.size())
                return false;
             
            Enumeration propNames = this.propertyNames();
             
            while (propNames.hasMoreElements()) {
                String propName = (String) propNames.nextElement();
                String value = properties.getProperty(propName);
             
                if (value == null) return false;
             
                if (!value.equals(this.getProperty(propName)))
                    return false;
            }
             
            return true;
             */
        }
        
        return false;
    }
    
    public int hashCode() {
        int hashCode = 0;
        Object value = null;
        
        if ((value = getProperty(PROP_HOST)) != null) {
            hashCode += value.hashCode();
        }
        
        if ((value = getProperty(PROP_PORT)) != null) {
            hashCode += value.hashCode();
        }
        
        if ((value = getProperty(PROP_USERNAME)) != null) {
            hashCode += value.hashCode();
        }
        
        if ((value = getProperty(PROP_PASSWORD)) != null) {
            hashCode += value.hashCode();
        }
        
        if ((value = getProperty(PROP_CONTEXT_ROOT)) != null) {
            hashCode += value.hashCode();
        }
        
        return hashCode;
    }
    
    
    public Object clone() {
        Object clone = super.clone();
        
        Enumeration propNames = this.propertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            
            ((ServerProperties) clone).setProperty(name, this.getProperty(name));
        }
        
        return clone;
    }
    
    public String toString() {
        return getProperty(PROP_ID) + " (" + getProperty(PROP_HOST) + //NOI18N
                ":" + getProperty(PROP_PORT) + ")";     //NOI18N
    };
}
