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
 * DeploymentManagerProperties.java
 *
 * Created on January 5, 2004, 3:47 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;
import java.io.File;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;

/**
 *
 * @author  ludo
 */
public class DeploymentManagerProperties {
    /**
     * Domain property, 
     */
    public static final String DOMAIN_ATTR = "DOMAIN"; //NOI18N

    /**
     * Username property, its value is used by the deployment manager.
     */    
    public static final String HTTP_MONITOR_ATTR = "HttpMonitorOn"; //NOI18N
    
    /**
     * Password property, its value is used by the deployment manager.
     */
    public static final String PASSWORD_ATTR = "password"; //NOI18N
    
    /**
     * Display name property, its value is used by IDE to represent server instance.
     */
    public static final String DISPLAY_NAME_ATTR = "displayName"; //NOI18N
    /**
     * Location of the app server instance property, its value is used by IDE to represent server instance.
     */
    public static final String LOCATION_ATTR = "LOCATION"; //NOI18N
    
    
    /**
     * HTTP port property, The port where the instance runs
     */
    public static final String HTTP_PORT_NUMBER_ATTR = "httpportnumber";
    
    private InstanceProperties instanceProperties;
    
    /** Creates a new instance of DeploymentManagerProperties */
    public DeploymentManagerProperties(DeploymentManager deploymentManager) {
        SunDeploymentManagerInterface s = (SunDeploymentManagerInterface)deploymentManager;
        instanceProperties = InstanceProperties.getInstanceProperties("deployer:Sun:AppServer::"+s.getHost()+":"+s.getPort());
        String httpport =  s.getNonAdminPortNumber();
        if (httpport==null){
            httpport ="8080";//hard code //NOI18N
        }
        if (instanceProperties==null){
            try {
                
                instanceProperties = InstanceProperties.createInstanceProperties("deployer:Sun:AppServer::"+s.getHost()+":"+s.getPort(), s.getUserName(),s.getPassword() , s.getHost()+":"+s.getPort());
                setHttpPortNumber( httpport);
                
            } catch (InstanceCreationException e){
                
            }
        } else{
            if (instanceProperties.getProperty(HTTP_PORT_NUMBER_ATTR)==null){
                instanceProperties.setProperty(HTTP_PORT_NUMBER_ATTR, httpport);
            }
        }
    }
    
    /** Creates a new instance of DeploymentManagerProperties via a URI for deployment MAnager */
    public DeploymentManagerProperties(String uri) {
        instanceProperties = InstanceProperties.getInstanceProperties(uri);

    }    
    /**
     * Getter for property domainName. Should never be null
     * @return Value of property domainName.
     */
    public java.lang.String getDomainName() {
        String retVal = Constants.DEFAULT_DOMAIN_NAME;
        if (instanceProperties==null)
            return retVal;
        retVal = instanceProperties.getProperty(DOMAIN_ATTR) ;
        if (null == retVal || (retVal.trim().length() == 0)) {
            retVal = Constants.DEFAULT_DOMAIN_NAME;
        }
        return retVal;
    }
    
    /**
     * Setter for property domainName.
     * @param domainName New value of property domainName.
     */
    public void setDomainName(java.lang.String domainName) {
        instanceProperties.setProperty(DOMAIN_ATTR, domainName);
    }
    
    /**
     * Getter for property location. can be null if the dm is remote
     * @return Value of property location.
     */
    public java.lang.String getLocation() {
        String installRoot = System.getProperty("com.sun.aas.installRoot");
        if (instanceProperties==null)
            return installRoot;
        String ret= instanceProperties.getProperty(LOCATION_ATTR) ;
        if (ret==null){
            return installRoot;
        }
        if (ret.equals ( PluginProperties.getDefault().getInstallRoot().getAbsolutePath())){
            //upgrade from previous semantic of this field in EA2: it was the app server location
            //not the domain location...
            ret = ret +File.separator+"domains"+File.separator;
            instanceProperties.setProperty(LOCATION_ATTR, ret);
        
        }
        return ret;
    }
    
    /**
     * Setter for property location.
     * @param location New value of property location.
     */
    public void setLocation(java.lang.String location) {
        if (instanceProperties==null)
            return;
        instanceProperties.setProperty(LOCATION_ATTR, location);
    }
    
    /**
     * Getter for property password. can be null if the DM is a disconnected DM
     * @return Value of property password.
     */
    public java.lang.String getPassword() {
        if (instanceProperties==null)
            return null;
        return instanceProperties.getProperty(InstanceProperties.PASSWORD_ATTR) ;
    }
    
    /**
     * Setter for property password.
     * @param password New value of property password.
     */
    public void setPassword(java.lang.String password) {
        instanceProperties.setProperty(InstanceProperties.PASSWORD_ATTR, password);
        
    }
    
    /**
     * Getter for property port.
     * @return Value of property port.
     */
    //   public int getPort() {
    //       return 0;
    //   }
    
    /**
     * Setter for property port.
     * @param port New value of property port.
     */
    //  public void setPort(int port) {
    //  }
    
    /**
     * Getter for property UserName. can be null for a disconnected DM.
     * @return Value of property UserName.
     */
    public java.lang.String getUserName() {
        if (instanceProperties==null)
            return null;
        return instanceProperties.getProperty(InstanceProperties.USERNAME_ATTR) ;
    }
    
    /**
     * Setter for property UserName.
     * @param UserName New value of property UserName.
     */
    public void setUserName(java.lang.String UserName) {
        instanceProperties.setProperty(InstanceProperties.USERNAME_ATTR, UserName);
        
    }
    /**
     * Ask the server instance to reset cached deployment manager, J2EE
     * management objects and refresh it UI elements.
     */
    public  void refreshServerInstance(){
        instanceProperties.refreshServerInstance();
    }
    
    
    public String getHttpMonitorOn() {
        if (instanceProperties==null)
            return "false";
        String s = instanceProperties.getProperty(HTTP_MONITOR_ATTR);
        if (s==null)
            return "false";
            
        return s;
    }
    
    public void setHttpMonitorOn(String HttpMonitorOn) {
            instanceProperties.setProperty(HTTP_MONITOR_ATTR, HttpMonitorOn);
    }
    public String getHttpPortNumber() {
        if (instanceProperties==null)
            return "8080";
        return instanceProperties.getProperty(HTTP_PORT_NUMBER_ATTR) ;
    }
    
    public void setHttpPortNumber(String port) {
            instanceProperties.setProperty(HTTP_PORT_NUMBER_ATTR, port);
    }
    
    public String getDisplayName() {
        if (instanceProperties == null) {
            return null;
        }
        return instanceProperties.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
    }
    
    public InstanceProperties getInstanceProperties () {
        return instanceProperties;
    }
}
