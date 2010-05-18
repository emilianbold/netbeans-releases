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
    
    public static final String PROP_HOST = "host";                  //NOI18N
    
    public static final String PROP_PORT = "port";                  //NOI18N
    
    public static final String PROP_CONTEXT_ROOT = "contextRoot";   //NOI18N
    
    public static final String PROP_USERNAME = "userName";          //NOI18N
    
    public static final String PROP_PASSWORD = "password";          //NOI18N
    
    public static final String LOCAL_HOST = "localhost";            //NOI18N
    
    private String id;
    private String host;
    private String port;
    private String contextRoot;
    private String userName;
    private String password;
    private ServerProperties properties;
    private PropertyChangeSupport propertySupport;
    
    public ServerInstance() {
        propertySupport = new PropertyChangeSupport(this);
    }
    
    public String getID() {
        return id;
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
    
    public boolean isLocal() {
        return (getHost().equals(LOCAL_HOST));
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    public ServerProperties getServerProperties() {
        if (properties == null) {
            properties = ServerProperties.getInstance(getID());
        }
        
        //if (!isLocal()) {           
            properties.setProperty(ServerProperties.PROP_HOST, host);
            properties.setProperty(ServerProperties.PROP_PORT, port);
            properties.setProperty(ServerProperties.PROP_CONTEXT_ROOT, contextRoot);
            properties.setProperty(ServerProperties.PROP_USERNAME, userName);
            properties.setProperty(ServerProperties.PROP_PASSWORD, password);
        //}
        
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
