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

package org.netbeans.modules.j2ee.sun.ide.dm;

import java.io.File;
import java.net.Authenticator;
import java.util.ResourceBundle;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;

import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunURIManager;
import org.netbeans.modules.j2ee.sun.ide.editors.AdminAuthenticator;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties;
import org.openide.filesystems.FileObject;
/** This deploymenmt factory can creates an alternate deployment manager for
 * S1AS.
 * @author vkraemer,ludo
 */
public class SunDeploymentFactory implements Constants, DeploymentFactory {
    

    //
    // this whole class should probably be a subclass of the
    // com.sun.enterprise.deployapi.SunDeploymentFactory...
    private DeploymentFactory innerDF = null;
    
    
    /** resource bundle
     */
    protected static final ResourceBundle bundle = ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.ide.dm.Bundle");	// NOI18N
    
    public SunDeploymentFactory() {
        
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
            Authenticator.setDefault(new AdminAuthenticator());
            innerDF = ServerLocationManager.getDeploymentFactory(getServerLocationFromURI(uri));
            if (innerDF==null){
                throw new DeploymentManagerCreationException(getRealURI(uri)+getServerLocationFromURI( uri)+bundle.getString("MSG_WrongInstallDir"));
                
            }
            return  new SunDeploymentManager(innerDF, getRealURI(uri), userName, password,getServerLocationFromURI( uri));
        } catch (Exception e) {
            throw new DeploymentManagerCreationException(getRealURI(uri)+getServerLocationFromURI( uri)+bundle.getString("MSG_WrongInstallDir"));
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
            Authenticator.setDefault(new AdminAuthenticator());
            innerDF = ServerLocationManager.getDeploymentFactory(getServerLocationFromURI(uri));

            return new SunDeploymentManager(innerDF,getRealURI(uri),null,null,getServerLocationFromURI( uri));
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentManagerCreationException(getRealURI(uri)+getServerLocationFromURI( uri)+bundle.getString("MSG_WrongInstallDir"));
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
        if(uri.startsWith("[")){//NOI18N
            if (uri.indexOf(SunURIManager.SUNSERVERSURI)!=-1)
            return true;
        }


        
        return false;
    }
    
    public String getDisplayName() {
        return bundle.getString("FACTORY_DISPLAYNAME");//NOI18N
    }
    
    public String getProductVersion() {
        if (null != innerDF)
            return innerDF.getProductVersion();
        return "1.0";//NOI18N
    }
    
    private static File getServerLocationFromURI(String uri) throws DeploymentManagerCreationException{
        
        if(uri.startsWith("[")){//NOI18N
            String loc = uri.substring(1,uri.indexOf("]"));
            return  new File(loc);
        }
        
        throw new DeploymentManagerCreationException(uri+bundle.getString("MSG_WrongInstallDir"));
    }
    private static String getRealURI(String uri) throws DeploymentManagerCreationException{
        
        if(uri.startsWith("[")){//NOI18N
            return uri.substring(uri.indexOf("]")+1,uri.length());
        } 
        return uri;// the old one.
        
        
    }
}
