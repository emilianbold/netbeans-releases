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

package org.netbeans.modules.j2ee.sun.api;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import org.openide.nodes.Node;

/**
 * Extensions specific to our sun deployment manager
 * @author  vkraemer
 */
public interface SunDeploymentManagerInterface extends Node.Cookie{

    /* return the user name used for this deploymment manager*/
    String getUserName();

    /* return the user password used for this deploymment manager*/
    String getPassword();
    
    /* set the user name used for this deploymment manager*/
    void setUserName(String name);
    
    /* set the user password used for this deploymment manager*/
    void  setPassword(String pw);
    
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
    /* return true is this  deploymment manager is running, 
     * if forced is true, no caching of the value is done, so the latest status is available in real time
     */
    boolean isRunning(boolean forced);
    
        /* return true is this  deploymment manager needs a restart, because of changes in admin configuration*/
    public boolean isRestartNeeded();
    
    /* return true is this  deploymment manager is secure, i.e is using https instead of http protocol*/
    boolean isSecure();
    
   ServerInterface/* ServerMEJB*/ getManagement();
    
///    MBeanServerConnection getMBeanServerConnection() throws RemoteException, ServerException;
   /*
    * necessary to fix some jpda bug due to dt_socket in Windows only platform
    */
   void fixJVMDebugOptions() throws java.rmi.RemoteException;
   String getDebugAddressValue() throws java.rmi.RemoteException;
   boolean isDebugSharedMemory() throws java.rmi.RemoteException;
   
   ResourceConfiguratorInterface getResourceConfigurator();
   CmpMappingProvider getSunCmpMapper();
   
   boolean isSuspended();
   /*
    * force a refresh of the internal Deployment manager. 
    * Sometimes useful to reset a few calculated values.
    *
    **/
   void refreshDeploymentManager();
   
   /*
    * return the App Server installation root used for getting the extra jar for this Deployment
    * manager
    * might return null if not a valid directory
    * usually, this is not stored within the DM URI and correctly calculated when ytou create the DM.
    
    */
   File  getPlatformRoot();
   
   HashMap getSunDatasourcesFromXml();
   
   HashMap getConnPoolsFromXml();

    HashMap getAdminObjectResourcesFromXml();
    
       /** Registers new listener. */
    void addPropertyChangeListener(PropertyChangeListener l);
    
    /** Unregister the listener. */
    void removePropertyChangeListener(PropertyChangeListener l);
    
    boolean grabInnerDM(boolean returnInsteadOfWaiting);
    
    void releaseInnerDM();
    
    int getAppserverVersion(); 
}
