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
 * InstancePropertiesImpl.java
 *
 * Created on December 4, 2003, 6:11 PM
 */

package org.netbeans.modules.j2ee.deployment.impl;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;

/**
 *
 * @author  nn136682
 */
public class InstancePropertiesImpl extends InstanceProperties implements InstanceListener {
    private final String url;
    private transient FileObject fo;
    
    /** Creates a new instance of InstancePropertiesImpl */
    public InstancePropertiesImpl(ServerInstance instance) {
        this(instance.getUrl());
    }

    /** Creates a new instance of InstancePropertiesImpl */
    public InstancePropertiesImpl(String url) {
        this.url = url;
    }
    
    private FileObject getFO() {
        if (fo == null) {
            ServerInstance instance = ServerRegistry.getInstance().getServerInstance(url);
            if (instance == null) 
                throw new IllegalStateException(
                (NbBundle.getMessage(InstancePropertiesImpl.class, "MSG_InstanceNotExists", url))); //NOI18N
            fo = ServerRegistry.getInstance().getInstanceFileObject(url);
            if (fo == null)
                throw new IllegalStateException(
                (NbBundle.getMessage(InstancePropertiesImpl.class, "MSG_InstanceNotExists", url))); //NOI18N
            
        }
        return fo;
    }
    
    // InstanceListener methods
    public void instanceRemoved(String instance) {
        if (instance != null && url.equals(instance))
            fo = null;
    }
    public void instanceAdded(String instance) {}
    public void changeDefaultInstance(String oldInstance, String newInstance){
    }
    
    public String getProperty(String propname) throws IllegalStateException {
        Object propValue = getFO().getAttribute(propname);
        return propValue == null ? null : propValue.toString();
    }

    public java.util.Enumeration propertyNames() throws IllegalStateException {
        return getFO().getAttributes();
    }
    
    public void setProperty(String propname, String value) throws IllegalStateException {
        try {
            String oldValue = getProperty(propname);
            getFO().setAttribute(propname, value);
            firePropertyChange(new PropertyChangeEvent(this, propname, oldValue, value));
        } catch (java.io.IOException ioe) {
            throw (IllegalStateException) org.openide.ErrorManager.getDefault().annotate(
            new IllegalStateException(NbBundle.getMessage(InstancePropertiesImpl.class, "MSG_InstanceNotExists", url)),ioe);
        }
    }
    
    public void setProperties(java.util.Properties props) throws IllegalStateException {
        java.util.Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            String propValue = props.getProperty(propName);
            setProperty(propName, propValue);
        }
    }    
    
    public javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager() {
        ServerRegistry registry = ServerRegistry.getInstance();
        ServerInstance inst = registry.getServerInstance(url);
        return inst.getDeploymentManager();
    }
    
    public javax.enterprise.deploy.spi.Target getDefaultTarget() {
        ServerRegistry registry = ServerRegistry.getInstance();
        ServerString ss = registry.getDefaultInstance();
        javax.enterprise.deploy.spi.Target[] targets = ss.toTargets();
        if (targets != null && targets.length > 0)
            return targets[0];
        return null;
    }
    
    public void setAsDefaultServer(String targetName) {
        ServerRegistry registry = ServerRegistry.getInstance();
        ServerInstance inst = registry.getServerInstance(url);
        ServerString server = new ServerString(inst, targetName);
        registry.setDefaultInstance(server);
    }
    
    public boolean isDefaultInstance() {
        ServerRegistry registry = ServerRegistry.getInstance();
        ServerString ss = registry.getDefaultInstance();
        return ss.getUrl().equals(url);
    }
    
    public void refreshServerInstance() {
        ServerRegistry registry = ServerRegistry.getInstance();
        ServerInstance inst = registry.getServerInstance(url);
        if (inst != null) {
            inst.refresh();
        }
    }
}
