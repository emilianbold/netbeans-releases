/*
 * WSURIManager.java
 *
 * Created on 23 январь 2006 г., 17:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.websphere6;
import java.io.File;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;


/**
 *
 * @author dlm198383
 */
public class WSURIManager {
    public static final String WSURI = "deployer:WebSphere:"; //NOI18N
           
    /**
     * Returns instance properties for the server instance.
     *
     * @param url the url connection string to get the instance deployment manager.
     * @return the InstanceProperties object, null if instance does not exists.
     */
    
    public static InstanceProperties getInstanceProperties(String host, String port){
        InstanceProperties  instanceProperties =
                InstanceProperties.getInstanceProperties(WSURI+host+":"+port);
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
    public static InstanceProperties createInstanceProperties(String host, String port, String user, String password, String displayName) throws InstanceCreationException {
        InstanceProperties  instanceProperties =  InstanceProperties.createInstanceProperties(WSURI+host+":"+port,user,password,displayName);
       
        return instanceProperties;
    }
    
    
}
