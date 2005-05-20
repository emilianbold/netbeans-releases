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

import java.io.File;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;

/**
 *
 * @author Ivan Sidorkin
 */
public class AddServerLocationPanel implements WizardDescriptor.Panel, ChangeListener {
    private final static String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N
    
    private JBInstantiatingIterator instantiatingIterator;
    
    private AddServerLocationVisualPanel component;
    private WizardDescriptor wizard;
    private transient Set listeners = new HashSet(1);
    
    
    
    
    public AddServerLocationPanel(JBInstantiatingIterator instantiatingIterator){
        this.instantiatingIterator = instantiatingIterator;
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
    
    public Component getComponent() {
        if (component == null) {
            component = new AddServerLocationVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_wizard_local_server_location"); //NOI18N
    }
    
    public boolean isValid() {
        String locationStr = ((AddServerLocationVisualPanel)getComponent()).getInstallLocation();
        File location = new File(locationStr);
        if (!JBPluginUtils.isGoodJBServerLocation(location)) {
            wizard.putProperty(PROP_ERROR_MESSAGE,  NbBundle.getMessage(AddServerLocationPanel.class, "MSG_InvalidServerLocation")); // NOI18N
            return false;
        } else {
            wizard.putProperty(PROP_ERROR_MESSAGE, null);
            JBPluginProperties.getInstance().setInstallLocation(((AddServerLocationVisualPanel)getComponent()).getInstallLocation());
            JBPluginProperties.getInstance().saveProperties();
            instantiatingIterator.setInstallLocation(locationStr);
            return true;
        }
    }

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
}
