/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
 * 
 * TODO implement "create personal domain" logic.
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
        // TODO get a help page?
        return HelpCtx.DEFAULT_HELP; // new HelpCtx(SampleWizardPanel1.class);
    }
    
    /** Is the directory usable.
     *
     * see Util.rootOfUsableDomain(File)
     * 
     */
    public boolean isValid() {
        File domainDir = new File(component.getInstanceDirectory());
        if (!creatingPersonalInstance) {
            if (!Util.rootOfUsableDomain(domainDir)) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_InValidDomainDir",                                 //NOI18N
                        component.getInstanceDirectory()));
                component.setDeploymentUri("");
                return false;
            }
            Util.fillDescriptorFromDomainXml(wiz, domainDir);
            component.setDeploymentUri(Util.getDeploymentUri(domainDir,
                    (File) wiz.getProperty(AddDomainWizardIterator.PLATFORM_LOCATION)));
            return true;
        } else {
            File parent = domainDir.getParentFile();
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
            if (domainDir.exists()) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_InValidDomainDir",                                 //NOI18N
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
