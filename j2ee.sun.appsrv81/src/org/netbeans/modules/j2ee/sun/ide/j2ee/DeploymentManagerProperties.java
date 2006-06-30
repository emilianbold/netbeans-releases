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
import org.netbeans.modules.j2ee.sun.api.SunURIManager;

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
    
    /* AVK Instrumentation on or off
     *
     **/
    public static final String AVK_INSTRUMENTED_ATTR = "AVKTurnedOn";
    
    private InstanceProperties instanceProperties;
    private SunDeploymentManagerInterface SunDM;
    
    /** Creates a new instance of DeploymentManagerProperties */
    public DeploymentManagerProperties(DeploymentManager deploymentManager) {
        SunDM = (SunDeploymentManagerInterface)deploymentManager;
        instanceProperties = SunURIManager.getInstanceProperties(SunDM.getPlatformRoot(),SunDM.getHost(),SunDM.getPort());
        String httpport =  SunDM.getNonAdminPortNumber();
        if (httpport==null){
            httpport ="8080";//hard code //NOI18N
        }
        if (instanceProperties==null){
            try {
                
                instanceProperties = SunURIManager.createInstanceProperties(SunDM.getPlatformRoot(),SunDM.getHost(),""+SunDM.getPort(), SunDM.getUserName(),SunDM.getPassword() , SunDM.getHost()+":"+SunDM.getPort() );
                setHttpPortNumber( httpport);
                
                
            } catch (InstanceCreationException e){
                
            }
        }
        if (instanceProperties.getProperty(HTTP_PORT_NUMBER_ATTR)==null){
            instanceProperties.setProperty(HTTP_PORT_NUMBER_ATTR, httpport);
            
        }
    }
    
    /** Creates a new instance of DeploymentManagerProperties via a URI for deployment MAnager */
//    public DeploymentManagerProperties(String uri) {
//        instanceProperties = InstanceProperties.getInstanceProperties(uri);
//
//    }
    /**
     * Getter for property domainName. Should never be null
     * @return Value of property domainName.
     */
    public java.lang.String getDomainName() {
        String retVal = ""; //NOI18N
        if (instanceProperties==null)
            return retVal;
        retVal = instanceProperties.getProperty(DOMAIN_ATTR) ;
        if (null == retVal || (retVal.trim().length() == 0)) {
            retVal = ""; //NOI18N
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
        java.io.File irf = SunDM.getPlatformRoot();
        String installRoot = null;
        if (null != irf && irf.exists())
            installRoot = irf.getAbsolutePath(); // System.getProperty("com.sun.aas.installRoot");
        if (instanceProperties==null)
            return installRoot;
        String ret= instanceProperties.getProperty(LOCATION_ATTR) ;
        if (ret==null){
            return installRoot;
        }
        if (ret.equals( installRoot )){
            //upgrade from previous semantic of this field in EA2: it was the app server location
            //not the domain location...
            ret = ret +File.separator+"domains";
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
    
    public String getUrl() {
        if (instanceProperties == null) {
            return null;
        }
        return instanceProperties.getProperty(InstanceProperties.URL_ATTR);
    }
    
    public InstanceProperties getInstanceProperties() {
        return instanceProperties;
    }
    
    
    public boolean getAVKOn() {
        if (instanceProperties == null)
            return false;
        String s = instanceProperties.getProperty(AVK_INSTRUMENTED_ATTR);
        if (s == null)
            return false;
        return Boolean.valueOf(s).booleanValue();
    }
    
    public void setAVKOn(boolean AVKOn) {
        instanceProperties.setProperty(AVK_INSTRUMENTED_ATTR, Boolean.toString(AVKOn));
    }
}
