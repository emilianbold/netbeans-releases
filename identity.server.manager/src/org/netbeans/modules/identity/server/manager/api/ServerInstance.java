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

package org.netbeans.modules.identity.server.manager.api;

import java.beans.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;

/**
 * This class represents an instance of the AM server.
 *
 * Created on June 14, 2006, 11:10 AM
 *
 * @author ptliu
 */
public class ServerInstance extends Object implements Serializable {
    
    public static final String PROP_ID = "id";                      //NOI18N
    
    public static final String PROP_DISPLAY_NAME = "displayName";   //NOI18N
    
    public static final String PROP_HOST = "host";                  //NOI18N
    
    public static final String PROP_PORT = "port";                  //NOI18N
    
    public static final String PROP_CONTEXT_ROOT = "contextRoot";   //NOI18N
    
    public static final String PROP_USERNAME = "userName";          //NOI18N
    
    public static final String PROP_PASSWORD = "password";          //NOI18N
    
    private String id;
    private String displayName;
    private String host;
    private String port;
    private String contextRoot;
    private String userName;
    private String password;
    private boolean isDefault;
    private ServerProperties properties;
    
    private PropertyChangeSupport propertySupport;
    
    public ServerInstance() {
        propertySupport = new PropertyChangeSupport(this);
    }
    
    public String getID() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getHost() {
        return host;
    }
    
    public String getPort() {
        return port;
    }
    
    public String getContextRoot() {
        return contextRoot;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getPassword() {
        return password;
    }
    
    void setID(String id) {
        this.id = id;
    }
    
    public void setDisplayName(String value) throws PropertyVetoException {
        String oldValue = displayName;
        displayName = value;
        propertySupport.firePropertyChange(PROP_DISPLAY_NAME, oldValue, displayName);
    }
    
    public void setHost(String value) {
        String oldValue = host;
        host = value;
        propertySupport.firePropertyChange(PROP_HOST, oldValue, host);
    }
    
    public void setPort(String value) {
        String oldValue = port;
        port = value;
        propertySupport.firePropertyChange(PROP_PORT, oldValue, host);
    }
    
    public void setContextRoot(String value) {
        String oldValue = contextRoot;
        contextRoot = value;
        propertySupport.firePropertyChange(PROP_CONTEXT_ROOT, oldValue, contextRoot);
    }
    
    public void setUserName(String value) {
        String oldValue = userName;
        userName = value;
        propertySupport.firePropertyChange(PROP_USERNAME, oldValue, userName);
    }
    
    public void setPassword(String value) {
        String oldValue = password;
        password = value;
        propertySupport.firePropertyChange(PROP_PASSWORD, oldValue, password);
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    void setIsDefault(boolean flag) {
        isDefault = flag;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    public ServerProperties getServerProperties() {
        if (properties == null) {
            properties = new ServerProperties();
        }
        
        // Update the ServerProperties with the current configuration data.
        
        if (!isDefault()) {
            // Use the displayName to identity the ServerProperties.
            properties.setProperty(ServerProperties.PROP_ID, displayName);
            properties.setProperty(ServerProperties.PROP_HOST, host);
            properties.setProperty(ServerProperties.PROP_PORT, port);
            properties.setProperty(ServerProperties.PROP_CONTEXT_ROOT, contextRoot);
            properties.setProperty(ServerProperties.PROP_USERNAME, userName);
            properties.setProperty(ServerProperties.PROP_PASSWORD, password);
        }
        
        return properties;
        
    }
    
    public String getAdminURL() {
        return getServerProperties().getProperty(ServerProperties.PROP_AM_CONSOLE_URL);
    }
    
    public void remove() {
        ServerManager.getDefault().removeServerInstance(this);
    }
    
    public boolean isRunning() {
        HttpURLConnection conn = null;
        
        try {
            URL isAliveURL = new URL(getServerProperties().getProperty(ServerProperties.PROP_IS_ALIVE_URL));
            conn = (HttpURLConnection) isAliveURL.openConnection();
            
            if (conn.getResponseCode() == 200) {
                return true;
            }
        } catch (MalformedURLException ex) {
            //ex.printStackTrace();
        } catch (IOException ex) {
            //ex.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        
        return false;
    }
    
    public String toString() {
        return "ServerInstance: id = " + id + " host = " + host + " port = "  //NOI18N
                + port + " contextRoot = " + contextRoot + " userName = "   //NOI18N
                + userName + " password = " + password;                     //NOI18N
    }
    
}
