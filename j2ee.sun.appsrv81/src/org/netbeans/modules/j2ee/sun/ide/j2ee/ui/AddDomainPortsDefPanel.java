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

package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.awt.Component;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author vkraemer
 */
public class AddDomainPortsDefPanel implements WizardDescriptor.Panel,
        ChangeListener {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private CreateServerVisualPanel component;
    private WizardDescriptor wiz;
//    private TargetServerData targetData;
    
    /** Create the wizard panel descriptor. */
    public AddDomainPortsDefPanel() { //TargetServerData data) {
//        targetData = data;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
//            targetData.setHost("localhost"); //NOI18N
            component = new CreateServerVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("AS_RegServ_DefinePorts"); //NOI18N
    }
    
    public boolean isValid() {
        //msgLabel.setText(""); //NOI18N
        // make sure the values are unique
        Set portsUsed = new HashSet(7);
        portsUsed.add(component.getAdminPort());
        
        if (isPortReused(portsUsed, component.getAdminJmxPort(), "ERR_AdminJmxPort"))
            return false;
        
        if (isPortReused(portsUsed, component.getInstanceHttpPort(), "ERR_InstancePort"))
            return false;
        
        
        if (isPortReused(portsUsed, component.getJmsPort(), "ERR_JmsPort"))
            return false;
        
        if (isPortReused(portsUsed,component.getOrbPort(), "ERR_OrbListenerPort"))
            return false;
        
        
        if (isPortReused(portsUsed,component.getHttpSslPort(), "ERR_HttpSslPort"))
            return false;
        
        if (isPortReused(portsUsed,component.getOrbSslPort(), "ERR_OrbSslPort"))
            return false;
        if (isPortReused(portsUsed,component.getOrbMutualAuthPort(), "ERR_OrbMutualAutPort"))
            return false;
        
        wiz.putProperty(AddDomainWizardIterator.ADMIN_JMX_PORT,
                component.getAdminJmxPort().toString());
        wiz.putProperty(AddDomainWizardIterator.HTTP_SSL_PORT,
                component.getHttpSslPort().toString());
        wiz.putProperty(AddDomainWizardIterator.INSTANCE_PORT,
                component.getInstanceHttpPort().toString());
        wiz.putProperty(AddDomainWizardIterator.JMS_PORT,
                component.getJmsPort().toString());
        wiz.putProperty(AddDomainWizardIterator.ORB_LISTENER_PORT,
                component.getOrbPort().toString());
        wiz.putProperty(AddDomainWizardIterator.ORB_SSL_PORT,
                component.getOrbSslPort().toString());
        wiz.putProperty(AddDomainWizardIterator.ORB_MUTUAL_AUTH_PORT,
                component.getOrbMutualAuthPort().toString());
        wiz.putProperty(AddDomainWizardIterator.PORT,
                component.getAdminPort().toString());
        
        wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,null);
        wiz.putProperty(AddDomainWizardIterator.HOST,"localhost");            //NOI18N
        return true;
    }


    private boolean isPortReused(Set portsUsed, Object newVal, String id) {
        if (portsUsed.contains(newVal)) {
            // create the error message
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainPortsDefPanel.class, id));
            return true;
        } else {
            portsUsed.add(newVal);
            return false;
        }

    }
 
    private final Set listeners = new HashSet(1);
    
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
    
    // You can use a settings object to keep track of state.
    // Normally the settings object will be the WizardDescriptor,
    // so you can use WizardDescriptor.getProperty & putProperty
    // to store information entered by the user.
    public void readSettings(Object settings) {
        wiz = (WizardDescriptor) settings;
    }
    public void storeSettings(Object settings) {
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
    
}
