/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.config;

import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.projects.J2eeDeploymentLookup;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.spi.*;
import org.netbeans.modules.j2ee.deployment.config.ui.ConfigUtils;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.WebContextRoot;
import org.openide.ErrorManager;

/*
 * ConfigSupportImpl.java
 *
 * Created on December 12, 2003, 11:47 PM
 * @author  nn136682
 */
public class ConfigSupportImpl implements J2eeModuleProvider.ConfigSupport {
    private J2eeDeploymentLookup deployment;
    private ServerString server;
    private String webContextRootXpath;
    private String webContextRootPropName;
    
    /** Creates a new instance of ConfigSupportImpl */
    public ConfigSupportImpl(J2eeDeploymentLookup deployment) {
        this.deployment = deployment;
        server = deployment.getJ2eeProfileSettings().getServerString();
        WebContextRoot webContextRoot = server.getServer().getWebContextRoot();
        webContextRootXpath = webContextRoot.getXpath();
        webContextRootPropName = webContextRoot.getPropName();
    }
    
    private DConfigBean getWebContextDConfigBean() {
        try {
            DeploymentConfiguration dc = deployment.getStorage().getDeploymentConfiguration();
            DeployableObject deployable = dc.getDeployableObject();
            //PENDIND: do we need if (deployable instanceof J2eeApplicationObject) ...
            DDBeanRoot ddBeanRoot = deployable.getDDBeanRoot();
            DConfigBeanRoot configBeanRoot = dc.getDConfigBeanRoot(ddBeanRoot);
            DDBean[] ddBeans = ddBeanRoot.getChildBean(webContextRootXpath);
            if (ddBeans == null || ddBeans.length != 1) {
                ErrorManager.getDefault ().log ("DDBeans not found");
                return null; //better than throw exception
            }
            return configBeanRoot.getDConfigBean(ddBeans[0]);
            
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, e.getMessage());
        }
        return null;
    }
    
    /*private java.beans.PropertyDescriptor getBeanProp(DConfigBean configBean) {
        java.beans.BeanInfo info = ConfigUtils.createBeanInfo(configBean);
        java.beans.PropertyDescriptor[] descs = info.getPropertyDescriptors();
        for (int i=0; i<descs.length; i++) {
            if (webContextRootPropName.equals(descs[i].getName()))
                return descs[i];
        }
        return null;
    }*/
    
    /**
     * Get context root
     * @return string value, null if not set or could not find
     */
    public String getWebContextRoot() {
        DConfigBean configBean = getWebContextDConfigBean();
        if (configBean == null) {
            ErrorManager.getDefault ().log ("Configuration not found");
            return null;
        }
        return (String) ConfigUtils.getBeanPropertyValue(configBean, webContextRootPropName);
    }
    /**
     * Set context root
     */
    public void setWebContextRoot(String contextRoot) {
        DConfigBean configBean = getWebContextDConfigBean();
        if (configBean == null) {
            ErrorManager.getDefault ().log ("Configuration not found");
            return;
        }
        ConfigUtils.setBeanPropertyValue(configBean, webContextRootPropName, contextRoot);
    }
}
