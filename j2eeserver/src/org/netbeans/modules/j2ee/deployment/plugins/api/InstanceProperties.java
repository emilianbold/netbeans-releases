/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * InstanceProperties.java
 *
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.impl.InstancePropertiesImpl;

/**
 *  A way for the IDE to store customized information about a server instance
 *  and make it available to a plugin.
 *
 *  Typical usage for create new instance would be like this:
 *      InstanceProperties props = InstanceProperties.getInstanceProperties(url);
 *      if (props == null)
 *          props = InstanceProperties.createInstanceProperties(url, user, password);
 *      props.setProperty(prop1, value1);
 *      . . .
 *
 * @author George FinKlang
 * @author nn136682
 * @version 0.1
 */

public abstract class InstanceProperties {

    public static final String URL_ATTR = "url"; //NOI18N
    public static final String USERNAME_ATTR = "username"; //NOI18N
    public static final String PASSWORD_ATTR = "password"; //NOI18N
    public static final String NAME_ATTR = "name"; //NOI18N

    /**
     * Returns instance properties for the server instance
     * @param url the url connection string to get the instance deployment manager
     * @return the InstanceProperties object, null if instance does not exists
     */
    public static InstanceProperties getInstanceProperties(String url) {
        ServerInstance inst = ServerRegistry.getInstance().getServerInstance(url);
        if (inst == null)
            return null;
        return new InstancePropertiesImpl(inst);
    }
    
    /**
     * Create new instance and returns instance properties for the server instance
     * @param url the url connection string to get the instance deployment manager
     * @return the InstanceProperties object, null if instance does not exists
     * @exception InstanceCreationException when instance with same url already registered.
     */
    public static InstanceProperties createInstanceProperties(
        String url, String username, String password) throws InstanceCreationException {
        
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.checkInstanceAlreadyExists(url);
        registry.addInstance(url, username, password);
        ServerInstance inst = registry.getServerInstance(url);
        return new InstancePropertiesImpl(inst);
    }

    /**
     * Returns list of URL strings of all registered instances
     * @return array of URL strings
     */
    public static String[] getInstanceList() {
        return ServerRegistry.getInstance().getInstanceURLs();
    }

    /**
     * Return default instance properties.
     */
    public static InstanceProperties getDefaultInstance() {
        return new InstancePropertiesImpl(ServerRegistry.getInstance().getDefaultInstance().getServerInstance());
    }
    
    /**
     * Set instance property
     * @propname name of property
     * @value property string value
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract void setProperties(java.util.Properties props) throws IllegalStateException;

    /**
     * Set instance property
     * @propname name of property
     * @value property string value
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract void setProperty(String propname, String value) throws IllegalStateException;
    
    /**
     * Get instance property
     * @propname name of property
     * @return property string value
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract String getProperty(String propname) throws IllegalStateException;
    
    /**
     * Get instance property keys
     * @propname name of property
     * @return property key enunmeration
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract java.util.Enumeration propertyNames() throws IllegalStateException;
    
    /**
     * Is the target server the default J2EE server for deployment?
     * @param url the URL identifying the admin server
     * @param targetName name of the target server; null if not target-specific or admin server is also single target.
     * @return true if the target server or admin server is the default.
     */
    public abstract boolean isDefaultInstance();
    
    /**
     * Return DeploymentManager associated with this instance.
     */
    public abstract DeploymentManager getDeploymentManager();
    
    /**
     * Return default Target object for the target server from this instance, if any.
     */
    public abstract Target getDefaultTarget();
    
    /**
     * Set the target server the default server.
     * @param url the url of the admin server
     * @param targetName name of the target server; null if admin server is also single target.
     */
    public abstract void setAsDefaultServer(String targetName);
    
}
