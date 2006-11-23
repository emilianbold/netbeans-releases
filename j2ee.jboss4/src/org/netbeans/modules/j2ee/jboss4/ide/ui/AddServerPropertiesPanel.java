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
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Ivan Sidorkin
 */
public class AddServerPropertiesPanel implements WizardDescriptor.Panel, ChangeListener {

    private final static String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N
    private WizardDescriptor wizard;
    private AddServerPropertiesVisualPanel component;
    private JBInstantiatingIterator instantiatingIterator;
    
    /** Creates a new instance of AddServerPropertiesPanel */
    public AddServerPropertiesPanel(JBInstantiatingIterator instantiatingIterator) {
        this.instantiatingIterator = instantiatingIterator;
    }
    
    public boolean isValid() {
        AddServerPropertiesVisualPanel panel = (AddServerPropertiesVisualPanel)getComponent();
        
        String host = panel.getHost();
        String port = panel.getPort();
        
        if(panel.isLocalServer()){
            // wrong domain path
            String path = panel.getDomainPath();
            if (!JBPluginUtils.isGoodJBInstanceLocation(new File(path))){
                wizard.putProperty(PROP_ERROR_MESSAGE,NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_WrongDomainPath"));
                return false;
            }
            
            ServerInstance[] si = ServerRegistry.getInstance().getServerInstances();
            
            for(int i=0;i<si.length;i++) {
                try {
                    String property = si[i].getInstanceProperties().getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
                    
                    if(property == null)
                        continue;
                    
                    String root = new File(property).getCanonicalPath();
                    
                    if(root.equals(new File(path).getCanonicalPath())) {
                        wizard.putProperty(PROP_ERROR_MESSAGE,NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_InstanceExists"));
                        return false;
                    }
                } catch (MissingResourceException ex) {
                    // It's normal behaviour when si[i] is something else then jboss instance
                    continue;
                } catch (IOException ex) {
                    // It's normal behaviour when si[i] is something else then jboss instance
                    continue;
                }
            }
            
            try{
                new Integer(port);
            }catch(Exception e){
                wizard.putProperty(PROP_ERROR_MESSAGE,NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_InvalidPort"));
                return false;
            }
            
            
        }else{ //remote
            if ( (host.equals("")) ){
                wizard.putProperty(PROP_ERROR_MESSAGE,NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_EnterHost"));
                return false;
            }
            if (port.equals("")) {
                wizard.putProperty(PROP_ERROR_MESSAGE,NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_EnterPort"));
                return false;
            }
        }
        
        wizard.putProperty(PROP_ERROR_MESSAGE,null);
        
        //   JBTargetServerData ts = JBPluginProperties.getInstance().getTargetServerData();
        instantiatingIterator.setHost(host);
        instantiatingIterator.setPort(port);
        instantiatingIterator.setServer(panel.getDomain());
        instantiatingIterator.setServerPath(panel.getDomainPath());
        instantiatingIterator.setDeployDir(JBPluginUtils.getDeployDir( panel.getDomainPath()));
        
        JBPluginProperties.getInstance().setDomainLocation(panel.getDomainPath());
        
        return true;
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new AddServerPropertiesVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }
    
    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }
    
    private void fireChangeEvent(ChangeEvent ev) {
        //@todo implement it
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    private transient Set listeners = new HashSet(1);
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void readSettings(Object settings) {
        if (wizard == null)
            wizard = (WizardDescriptor)settings;
    }
    
    public void storeSettings(Object settings) {
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_server_jboss_properties"); //NOI18N
    }
    
    void installLocationChanged() {
        if (component != null)
            component.installLocationChanged();
    }
}
