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
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
        String user = panel.getUser();
        String password = panel.getPassword();
        
        if(panel.isLocalServer()){
            // wrong domain path
            String path = panel.getDomainPath();
            if (!JBPluginUtils.isGoodJBInstanceLocation(new File(path))){
                wizard.putProperty(PROP_ERROR_MESSAGE,NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_WrongDomainPath"));
                return false;
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
        return new HelpCtx("j2eeplugins_wizard_new_server_instance_properties"); //NOI18N
    }
}
