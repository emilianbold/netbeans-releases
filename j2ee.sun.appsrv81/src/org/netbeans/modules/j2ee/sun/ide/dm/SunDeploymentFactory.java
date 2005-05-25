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

package org.netbeans.modules.j2ee.sun.ide.dm;

import java.util.ResourceBundle;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;

/** This deploymenmt factory can creates an alternate deployment manager for
 * S1AS8.
 * @author vkraemer,ludo
 * @version prototype
 */
public class SunDeploymentFactory implements Constants, DeploymentFactory {
    
    // VBK hack the innerDF is used to support configuration
    // prototype in J2EE 1.4 RI beta 1 deploytool
    //
    // this whole class should probably be a subclass of the
    // com.sun.enterprise.deployapi.SunDeploymentFactory...
    private DeploymentFactory innerDF = null;
    
    /** resource bundle
     */
    protected static final ResourceBundle bundle = ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.ide.dm.Bundle");	// NOI18N
    
    public SunDeploymentFactory() {
        try{
            
             org.netbeans.modules.j2ee.sun.ide.ExtendedClassLoader loader =  org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader();
            Object o = loader.loadClass("com.sun.enterprise.deployapi.SunDeploymentFactory").newInstance();//NOI18N
            innerDF = (DeploymentFactory)o;
            //innerDF =   new com.sun.enterprise.deployapi.SunDeploymentFactory();
            //
            //turn off severe loggin which is not needed in plugins:
            java.util.logging.Logger.getLogger("javax.enterprise.system.tools.deployment").setLevel(java.util.logging.Level.OFF);
        } catch (Throwable e){
            //nothin to report there. The node name will indicate the config issue 
            e.printStackTrace();
            System.out.println("  WARNING: cannot create a good SunDeploymentFactory:to correct, set com.sun.aas.installRoot to the correct App Server 8 PE Location and restart.");
            
        }
        
    }
    
    
    
    /** This method returns a connected deployment manager.
     *
     * @param uri
     * @param userName
     * @param password
     * @throws DeploymentManagerCreationException
     * @return a deployment manager for a particular server instance
     */
    public DeploymentManager getDeploymentManager(String uri, String userName,String password) throws DeploymentManagerCreationException {
        
        
        try {
            return  new SunDeploymentManager(innerDF, uri, userName, password);
        } catch (NoClassDefFoundError e) {
            throw new DeploymentManagerCreationException(bundle.getString("MSG_WrongInstallDir"));
        }
    }
    
    /** This method returns a disconnected deployment manager.
     *
     * Should a disconnected deployment manager be able to become connected?
     *
     * @param uri
     * @throws DeploymentManagerCreationException
     * @return a deployment manager for doing configuration.
     */
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        
        try {
            return new SunDeploymentManager(innerDF,uri,null,null);
        } catch (NoClassDefFoundError e) {
            throw new DeploymentManagerCreationException(bundle.getString("MSG_WrongInstallDir"));
        }
        
    }
    
    /** Determines whether this URI is handled by the Deployment factory
     * Iniitally we need to test the prefix. If the factory will support
     * multiple managers, we need to extend this test to catch those cases.
     *
     *The tests should also be extended to deteremine if the URI is "complete"
     * for this factory.  It has to have a machine name (that can be resolved
     * to an IP address) and a port. Whether the server is "up" may be an open
     * question.
     *
     * @param uri
     * @return boolean value
     */
    public boolean handlesURI(String uri) {
        if (uri==null)
            return false;
        if (null != innerDF) {
            boolean retval = false;
            try {
                retval = innerDF.handlesURI(uri);
            } catch (Exception t) {
            }
            
            return retval;
        } else
            return uri.startsWith("deployer:Sun:AppServer::");//NOI18N
    }
    
    public String getDisplayName() {
        return bundle.getString("FACTORY_DISPLAYNAME");//NOI18N
    }
    
    public String getProductVersion() {
        if (null != innerDF)
            return innerDF.getProductVersion();
        return "1.0";//NOI18N
    }
    
    
    
    
    
}
