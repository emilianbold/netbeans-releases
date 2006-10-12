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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Panel to query for the host and port when registering a remote instance.
 *
 * TODO add additional sanity testing for the port
 */
class AddDomainHostPortPanel implements WizardDescriptor.FinishablePanel,
        ChangeListener {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private AddInstanceVisualHostPortPanel component;
    private WizardDescriptor wiz;
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new AddInstanceVisualHostPortPanel();
            component.addChangeListener(this);
            
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("AS_RegServ_EnterRemoteInfo"); //NOI18N
    }
        
    /** Determine if the page has acceptable values.
     *
     * Fill in the WizardDescriptor properties
     *
     * Using a host name which is "unknown" at registration time is probably wrong.
     * Using an invalid port number is totally wrong.
     */
    public boolean isValid() {
        String h = component.getHost();
        if (h.length() < 1) {
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddDomainHostPortPanel.class, 
                    "MSG_EnterHost",h));                                     //NOI18N
            return false;            
        }
        if (h.indexOf("://") > -1) {
            // IZ 77187
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddDomainHostPortPanel.class, 
                    "MSG_exclude_protocol",h));                                 //NOI18N
            return false;                        
        }
        int p = component.getPort();
        try {
            InetAddress ia = InetAddress.getByName(h);
            new InetSocketAddress(ia,p);
        } catch (UnknownHostException uhe) {
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddDomainHostPortPanel.class, 
                    "MSG_UnknownHost2",h));                                     //NOI18N
            return false;
        } catch (IllegalArgumentException iae) {
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddDomainHostPortPanel.class, 
                    "Msg_ValidPortNumber"));                                    //NOI18N
            return false;
        }
        // TODO verify no listener OR listener is an admin instance
        wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,null);
        wiz.putProperty(AddDomainWizardIterator.HOST,h);
        wiz.putProperty(AddDomainWizardIterator.PORT,p+"");
        return true;
    }
    
    // Event handling
    //
    private final Set/*<ChangeListener> */listeners = new HashSet/*<ChangeListener>*/(1);
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
        Iterator/*<ChangeListener>*/ it;
        synchronized (listeners) {
            it = new HashSet/*<ChangeListener>*/(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        wiz = (WizardDescriptor) settings;
    }
    public void storeSettings(Object settings) {
        // TODO implement?
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

    /** this can be a finish page
     */
    public boolean isFinishPanel() {
        return true;
    }    
}
