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
package org.netbeans.modules.j2ee.jboss4;

import java.util.HashMap;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
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
    
//    private DeploymentFactory jbossFactory = null;
    /**
     * Mapping of a server installation directory to a deployment factory
     */
    private HashMap/*<String, DeploymentFactory*/ jbossFactories = new HashMap();
    
    public URLClassLoader getJBClassLoader(String serverRoot){
        try {

            URL urls[] = new URL[]{
                    new File(serverRoot + "/client/jbossall-client.jar").toURI().toURL(),      //NOI18N
                    new File(serverRoot + "/client/jboss-common-client.jar").toURI().toURL(),  //NOI18N
                    new File(serverRoot + "/client/jboss-deployment.jar").toURI().toURL(),     //NOI18N
                    new File(serverRoot + "/client/jnp-client.jar").toURI().toURL(),           //NOI18N
                    new File(serverRoot + "/lib/dom4j.jar").toURI().toURL()                    //NOI18N
            };
            URLClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader());
            return loader;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
        return null;
    }
    
    public DeploymentFactory getFactory(String instanceURL) {
        DeploymentFactory jbossFactory = null;
        try {
            String jbossRoot = InstanceProperties.getInstanceProperties(instanceURL).
                                    getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
            // if jbossRoot is null, then we are in a server instance registration process, thus this call
            // is made from InstanceProperties creation -> JBPluginProperties singleton contains 
            // install location of the instance being registered
            if (jbossRoot == null)
                jbossRoot = JBPluginProperties.getInstance().getInstallLocation();
            
            jbossFactory = (DeploymentFactory) jbossFactories.get(jbossRoot);
            if ( jbossFactory == null ) {
                URLClassLoader loader = getJBClassLoader(jbossRoot);
                jbossFactory = (DeploymentFactory) loader.loadClass("org.jboss.deployment.spi.factories.DeploymentFactoryImpl").newInstance();//NOI18N
                
                jbossFactories.put(jbossRoot, jbossFactory);
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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
            throw new DeploymentManagerCreationException(NbBundle.getMessage(JBDeploymentFactory.class, "MSG_INVALID_URI", uri)); // NOI18N
        }
        
        DeploymentFactory df = getFactory(uri);
        if (df == null)
            throw new DeploymentManagerCreationException(NbBundle.getMessage(JBDeploymentFactory.class, "MSG_ERROR_CREATING_DM", uri)); // NOI18N

        String jbURI = uri;
        try {
            jbURI = uri.substring(0, uri.indexOf("&")); // NOI18N
        }
        catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        
        return new JBDeploymentManager(df.getDeploymentManager(jbURI, uname, passwd), uri, uname, passwd);
    }
     
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException(NbBundle.getMessage(JBDeploymentFactory.class, "MSG_INVALID_URI", uri)); // NOI18N
        }
        
        DeploymentFactory df = getFactory(uri);
        if (df == null)
            throw new DeploymentManagerCreationException(NbBundle.getMessage(JBDeploymentFactory.class, "MSG_ERROR_CREATING_DM", uri)); // NOI18N

        String jbURI = uri;
        try {
            jbURI = uri.substring(0, uri.indexOf("&")); // NOI18N
        }
        catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        return new JBDeploymentManager(df.getDisconnectedDeploymentManager(jbURI), uri, null, null);
    }
    
    public String getProductVersion() {
       
        return NbBundle.getMessage (JBDeploymentFactory.class, "LBL_JBossFactoryVersion");
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(JBDeploymentFactory.class, "SERVER_NAME"); // NOI18N
    }
}
