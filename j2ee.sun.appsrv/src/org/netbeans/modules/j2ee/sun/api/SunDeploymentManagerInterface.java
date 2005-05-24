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

package org.netbeans.modules.j2ee.sun.api;

import java.rmi.ServerException;
import java.rmi.RemoteException;
import org.openide.nodes.Node;

///import org.netbeans.modules.j2ee.sun.share.management.ServerMEJB;
///import javax.management.MBeanServerConnection;
/**
 * Extensions specific to our sun deployment manager
 * @author  vkraemer
 */
public interface SunDeploymentManagerInterface extends Node.Cookie{
    
    /* return the user name used for this deploymment manager*/
    String getUserName();
    
    /* return the user password used for this deploymment manager*/
    String getPassword();
    
    /* return the hostname name used for this deploymment manager*/
    String getHost();
    
    /* return the port used for this deploymment manager*/
    int getPort();
    
    /*
     * return the real http port for the server. Usually, it is "8080", or null if the server is not running
     *
     **/
     String getNonAdminPortNumber() ;
     
    /* tells if  deploymment manager is this local machine or not*/
    boolean isLocal();
    
    /* return true is this  deploymment manager is running*/
    boolean isRunning();
    
        /* return true is this  deploymment manager needs a restart, because of changes in admin configuration*/
    public boolean isRestartNeeded();
    
    /* return true is this  deploymment manager is secure, i.e is using https instead of http protocol*/
    boolean isSecure();
    
    /* may return null
     * or returns a netbeans specific class that implements the StartServer interface from j2eeserver
     * need to keep the mapping between a DM and a StartServer object
     */
    SunServerStateInterface getStartServerInterface();
    
    /* used by a netbeans extension to associatate a StartServer with this DM
     */
    void setStartServerInterface (SunServerStateInterface o);
    
    
   ServerInterface/* ServerMEJB*/ getManagement();
    
///    MBeanServerConnection getMBeanServerConnection() throws RemoteException, ServerException;
   /*
    * necessary to fix some jpda bug due to dt_socket in Windows only platform
    */
   void fixJVMDebugOptions() throws java.rmi.RemoteException;
   String getDebugAddressValue() throws java.rmi.RemoteException;
   boolean isDebugSharedMemory() throws java.rmi.RemoteException;
   
}
