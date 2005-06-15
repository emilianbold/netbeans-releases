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
package org.netbeans.modules.j2ee.jboss4;

import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;


/**
 *
 * @author Kirill Sorokin
 */
public class JBDeploymentFactory implements DeploymentFactory {
    
    public static final String URI_PREFIX = "jboss-deployer:";//NOI18N
    
    private static JBDeploymentFactory instance;
    
    public static synchronized DeploymentFactory create() {
        if (instance == null) {
            instance = new JBDeploymentFactory();
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
        }
        return instance;
    }
    
    private DeploymentFactory jbossFactory = null;
    
    public JBDeploymentFactory() {
        // init();
    }
    
    public URLClassLoader getJBClassLoader(){
            String jbossRoot = JBPluginProperties.getInstance().getInstallLocation();
            try {
                URL urls[] = new URL[]{
                        new File(jbossRoot + "/client/jboss-common-client.jar").toURL(),  //NOI18N
                        new File(jbossRoot + "/client/jboss-deployment.jar").toURL(),     //NOI18N
                        new File(jbossRoot + "/client/jnp-client.jar").toURL(),           //NOI18N 
                        new File(jbossRoot + "/lib/dom4j.jar").toURL()                    //NOI18N
                };
                URLClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader());
                return loader;
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
            return null;
    }
    
    public DeploymentFactory getFactory() {
        if ( jbossFactory == null ){
            String jbossRoot = JBPluginProperties.getInstance().getInstallLocation();
     
            
            try {
//                URL urls[] = new URL[]{
//                        new File(jbossRoot + "/client/jboss-common-client.jar").toURL(),  //NOI18N
//                        new File(jbossRoot + "/client/jboss-deployment.jar").toURL(),     //NOI18N
//                        new File(jbossRoot + "/client/jnp-client.jar").toURL(),           //NOI18N 
//                        new File(jbossRoot + "/lib/dom4j.jar").toURL()                    //NOI18N
//                                
//                };
//                
//                URLClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader());
                URLClassLoader loader = getJBClassLoader();
                jbossFactory = (DeploymentFactory) loader.loadClass("org.jboss.deployment.spi.factories.DeploymentFactoryImpl").newInstance();//NOI18N
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
        }
        return jbossFactory;
    }
    public boolean handlesURI(String uri) {
        if (uri != null && uri.startsWith(URI_PREFIX)) {
            return true;
        }
        
        return false;
    }
    
    public DeploymentManager getDeploymentManager(String uri, String uname, String passwd) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI:" + uri); // NOI18N
        }
        
        return new JBDeploymentManager(getFactory().getDeploymentManager(uri, uname, passwd), uri, uname, passwd);
    }
     
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
//         alter the uri
        if (uri.indexOf("#") != -1) {//NOI18N
            uri = uri.substring(0, uri.indexOf("#"));//NOI18N
        }
        return new JBDeploymentManager(getFactory().getDisconnectedDeploymentManager(uri), uri, null, null);

    }
    
    public String getProductVersion() {
       
        return getFactory().getProductVersion();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(JBDeploymentFactory.class, "SERVER_NAME"); // NOI18N
    }
}
