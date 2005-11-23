/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * SunURIManager
 *
 */

package org.netbeans.modules.j2ee.sun.api;


import java.io.File;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;



public class SunURIManager  {
    
    public static final String SUNSERVERSURI = "deployer:Sun:AppServer::"; //NOI18N
    
    /**
     * Returns instance properties for the server instance.
     *
     * @param url the url connection string to get the instance deployment manager.
     * @return the InstanceProperties object, null if instance does not exists.
     */
    
    public static InstanceProperties getInstanceProperties(File serverLocation, String host, int port){
        InstanceProperties  instanceProperties =
                InstanceProperties.getInstanceProperties("["+serverLocation.getAbsolutePath()+"]"+SUNSERVERSURI+host+":"+port);

        return instanceProperties;
    }
    /**
     * Create new instance and returns instance properties for the server instance.
     * 
     * @param url the url connection string to get the instance deployment manager.
     * @param username username which is used by the deployment manager.
     * @param password password which is used by the deployment manager.
     * @param displayName display name which is used by IDE to represent this 
     *        server instance.
     * @return the <code>InstanceProperties</code> object, <code>null</code> if 
     *         instance does not exists.
     * @exception InstanceCreationException when instance with same url already 
     *            registered.
     */
    public static InstanceProperties createInstanceProperties(File serverLocation, String host, String port, String user, String password, String displayName) throws InstanceCreationException {
        InstanceProperties  instanceProperties =  InstanceProperties.createInstanceProperties(
                "["+serverLocation.getAbsolutePath()+"]"+SUNSERVERSURI+host+":"+port,
                user,
                password,displayName);
        
        return instanceProperties;
    }
    
    
}