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
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Panel to query for a domain directory.
 * Used to query the user for a local instance's domain directory.
 */
class AddDomainDirectoryPanel implements WizardDescriptor.FinishablePanel,
        ChangeListener {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private AddInstanceVisualDirectoryPanel component;
    private WizardDescriptor wiz;
    private boolean creatingPersonalInstance;
    
    AddDomainDirectoryPanel(boolean creatingPersonalInstance) {
        this.creatingPersonalInstance = creatingPersonalInstance;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new AddInstanceVisualDirectoryPanel(creatingPersonalInstance);
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        if (creatingPersonalInstance)
            return new HelpCtx("AS_RegServ_EnterPIDir"); //NOI18N
        else 
            return new HelpCtx("AS_RegServ_EnterDomainDir");
    }
        
    /** Is the directory usable.
     *
     * see Util.rootOfUsableDomain(File)
     * 
     */
    public boolean isValid() {
        File domainDir = new File(component.getInstanceDirectory());
        if (!creatingPersonalInstance) {
            if (component.getInstanceDirectory().length() < 1) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_EneterValidDomainDir",                                 //NOI18N
                        component.getInstanceDirectory()));
                component.setAdminPort("");
                return false;                
            }
            if (!Util.rootOfUsableDomain(domainDir)) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_InValidDomainDir",                                 //NOI18N
                        component.getInstanceDirectory()));
                component.setAdminPort("");                                     // NOI18N
                return false;
            }
            Util.fillDescriptorFromDomainXml(wiz, domainDir);
            String port = (String)wiz.getProperty(AddDomainWizardIterator.PORT);
            component.setAdminPort(port);
            if ("".equals(port)) {                                              // NOI18N
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_UnsupportedDomain",                                 //NOI18N
                        component.getInstanceDirectory()));   
                return false;
            }
            return true;
        } else {
            File parent = domainDir.getParentFile();
            if (domainDir.exists()) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_ExistingDomainDir",                                //NOI18N
                        domainDir.getAbsolutePath()));
                return false;                
            }
            if (null == parent) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_InValidDomainDir",                                 //NOI18N
                        component.getInstanceDirectory()));
                return false;
            }
            if (!parent.exists()) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_InValidDomainDirParent",                           //NOI18N
                        parent.getAbsolutePath()));
                return false;
            }
            if (!parent.canWrite()) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_InValidDomainDirParent",                                 //NOI18N
                        parent.getAbsolutePath()));
                return false;
            }
            wiz.putProperty(AddDomainWizardIterator.DOMAIN,domainDir.getName());
            wiz.putProperty(AddDomainWizardIterator.INSTALL_LOCATION,
                    domainDir.getParentFile().getAbsolutePath());
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,null);
            return true;
        }
    }
    
    // Event handling
    //
    private final Set/*<ChangeListener>*/ listeners = 
            new HashSet/*<ChangeListener>*/(1);
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

    /** This panel is a finishable panel for registering an existing instance.
     *
     * If the user is trying to create an instance we may be in trouble
     */
    public boolean isFinishPanel() {
        return !creatingPersonalInstance;
    }    
}
