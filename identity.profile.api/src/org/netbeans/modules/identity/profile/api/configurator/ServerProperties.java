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
package org.netbeans.modules.identity.profile.api.configurator;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import org.openide.util.NbBundle;

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
  
    private static final String NAMING_SERVICE = "/namingservice";  //NOI18N
    
    private static final String IS_ALIVE_JSP = "/isAlive.jsp";      //NOI18N
    
    private static final String LIBERTY_DISCO_SERVICE = "/Liberty/disco";   //NOI18N
    
    private static final String AM_CONFIG_FILE = "/domains/domain1/config/AMConfig.properties"; //NOI18N
    
    private static HashMap<String, ServerProperties> instanceMap = new HashMap<String, ServerProperties>();
    
    public synchronized static ServerProperties getInstance(String id) {
        ServerProperties instance = instanceMap.get(id);
        
        if (instance == null) {
            instance = new ServerProperties(id);
            instanceMap.put(id, instance);
        }
        
        return instance;
    }
    
    /** Creates a new instance of ServerProperties */
    private ServerProperties(String id) throws ConfiguratorException {
        super();
        
        init(id);
    }
    
    private void init(String id) {
        String asRoot = id.substring(1, id.indexOf(']'));    
        String amConfigFile = asRoot + AM_CONFIG_FILE;       
        String[] segments = id.substring(id.indexOf(']')+1).split(":");     //NOI18N
        String host = segments[4];
        
        // RESOLVE:
        // Need to figure a way to report errors.
        // close the input stream?
        // should cache
        try {
            FileInputStream fis = new FileInputStream(amConfigFile.trim());
            try {
                load(fis);
                setProperty(PROP_AM_CONFIG_FILE, amConfigFile);
                setProperty(PROP_AS_ROOT, asRoot);
                setProperty(PROP_ID, id);
                setProperty(PROP_CONTEXT_ROOT, getContextRoot());
                setProperty(PROP_HOST, host);
                updateURLs();
            } finally {
                fis.close();
            }
        } catch (Exception ex) {
            throw new ConfiguratorException(NbBundle.getMessage(ServerProperties.class,
                    "TXT_InvalidAMConfigFile"));
        }
        
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
        } else if (key.equals(PROP_USERNAME) || key.equals(PROP_PASSWORD)) {
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
