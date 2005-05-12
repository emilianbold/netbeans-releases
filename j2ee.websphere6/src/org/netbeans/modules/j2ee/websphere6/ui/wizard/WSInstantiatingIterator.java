/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.ui.wizard;

import org.netbeans.modules.j2ee.websphere6.WSDeploymentFactory;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;


/**
 *
 * @author Kirill Sorokin
 */
public class WSInstantiatingIterator  implements WizardDescriptor.InstantiatingIterator {
    
    private static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N
    
    private static final String DEFAULT_DEBUGGER_PORT = "8787";
    
    private WizardDescriptor wizardDescriptor;
    
    public void uninitialize(WizardDescriptor wizardDescriptor) {
        // do nothing as we do not need to release any resources
    }

    public void initialize(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
    }
    
    public String name() {
        return ""; // NOI18N
    }
    
    public Set instantiate() throws IOException {
        Set result = new HashSet();
        
        String displayName = NbBundle.getMessage(ServerPropertiesPanel.class, "INSTANCE_DISPLAY_NAME_PREFIX") + " [" + this.host + ":" + this.port +"]"; // NOI18N
        String url = "deployer:WebSphere:" + this.host + ":" + this.port; // NOI18N
        String username = this.username;
        String password = this.password;
        
        if (getDisplayName() != null && !getDisplayName().equals(NbBundle.getMessage(WSDeploymentFactory.class, "TXT_displayName"))) { // NOI18N
            displayName = getDisplayName();
        }
        
        String serverRoot = this.serverRoot;
        String domainRoot = this.domainRoot;
        String isLocal = this.isLocal;
        String serverName = this.serverName;
        
        if (validate()) {
            InstanceProperties ip = InstanceProperties.createInstanceProperties(url, username, password, displayName);
            ip.setProperty(WSDeploymentFactory.SERVER_ROOT_ATTR, serverRoot);
            ip.setProperty(WSDeploymentFactory.DOMAIN_ROOT_ATTR, domainRoot);
            ip.setProperty(WSDeploymentFactory.IS_LOCAL_ATTR, isLocal);
            ip.setProperty(WSDeploymentFactory.SERVER_NAME_ATTR, serverName);
            ip.setProperty(WSDeploymentFactory.DEBUGGER_PORT_ATTR, DEFAULT_DEBUGGER_PORT);
            
            result.add(ip);
            
            return result;
        } else {
            return null;
        }
    }
    
    private boolean validate() {
        try {
            new Socket(getHost(), new Integer(getPort()).intValue());
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(serverPropertiesPanel, NbBundle.getMessage(WSInstantiatingIterator.class, "MSG_unknownHost", getHost()), NbBundle.getMessage(WSInstantiatingIterator.class, "MSG_instanceCreationFailed"), JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (IOException e) {
            // in this cse we do nothing because it could simply mean that the 
            // server is not started
        }
        
        return true;
    }
    
    private String getDisplayName() {
        return (String) wizardDescriptor.getProperty(PROP_DISPLAY_NAME);
    }
    
    private String serverRoot;
    private String domainRoot;
    private String isLocal;
    private String host;
    private String port;
    private String username;
    private String password;
    private String serverName;
    
    public void setServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;
        
        // reinit the instances list
        serverPropertiesPanel.updateInstancesList();
    }
    
    public String getServerRoot() {
        return this.serverRoot;
    }
    
    public String getDomainRoot() {
        return domainRoot;
    }

    public void setDomainRoot(String domainRoot) {
        this.domainRoot = domainRoot;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getIsLocal() {
        return this.isLocal;
    }
    
    public void setIsLocal(String isLocal) {
        this.isLocal = isLocal;
    }
    
    public String getServerName() {
        return this.serverName;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Panels section
    ////////////////////////////////////////////////////////////////////////////
    private Vector steps = new Vector();
    {
        steps.add(NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_LOCATION_STEP")); // NOI18N
        steps.add(NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_PROPERTIES_STEP")); // NOI18N
    }
    
    private Vector panels = new Vector();
    private ServerLocationPanel serverLocationPanel = new ServerLocationPanel((String[]) steps.toArray(new String[steps.size()]), 0, new IteratorListener(), this);
    private ServerPropertiesPanel serverPropertiesPanel = new ServerPropertiesPanel((String[]) steps.toArray(new String[steps.size()]), 1, new IteratorListener(), this);
    {
        panels.add(serverLocationPanel);
        panels.add(serverPropertiesPanel);
    }
    
    private int index;
    
    public boolean hasPrevious() {
        return index > 0;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public boolean hasNext() {
        return index < panels.size() - 1;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public WizardDescriptor.Panel current() {
        return (WizardDescriptor.Panel) panels.get(index);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Listeners section
    ////////////////////////////////////////////////////////////////////////////
    private Vector listeners = new Vector();
    
    public void removeChangeListener(ChangeListener listener) {
        if (listeners != null) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
    }

    public void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    private void fireChangeEvent(ChangeEvent event) {
        Vector targetListeners;
        synchronized (listeners) {
            targetListeners = (Vector) listeners.clone();
        }
        
        for (int i = 0; i < targetListeners.size(); i++) {
            ChangeListener listener = (ChangeListener) targetListeners.elementAt(i);
            listener.stateChanged(event);
        }
    }
    
    private class IteratorListener implements ChangeListener {
        public void stateChanged(ChangeEvent event) {
            fireChangeEvent(event);
        }
    }
    
}
