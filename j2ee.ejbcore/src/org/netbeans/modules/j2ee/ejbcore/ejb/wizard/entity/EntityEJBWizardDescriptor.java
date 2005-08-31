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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class EntityEJBWizardDescriptor implements WizardDescriptor.FinishablePanel, ChangeListener {

    private EntityEJBWizardPanel p;
    
    private List changeListeners = new ArrayList();
    
    private WizardDescriptor wizardDescriptor;
    
    public void addChangeListener(javax.swing.event.ChangeListener l) {
        changeListeners.add(l);
    }
    
    public java.awt.Component getComponent() {
        if (p == null) {
            p = new EntityEJBWizardPanel(this);
            // add listener to events which could cause valid status to change
        }
        return p;
    }
    
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(EntityEJBWizardDescriptor.class);
    }
    
    public boolean isValid() {
        // XXX add the following checks
        // p.getName = valid NmToken
        // p.getName not already in module
        if (wizardDescriptor == null) {
            return true;
        }
        boolean isLocalOrRemote = (p.isLocal() || p.isRemote());
        if (!isLocalOrRemote) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(EntityEJBWizardDescriptor.class,"ERR_RemoteOrLocal_MustBeSelected")); //NOI18N
            return false;
        }
        
        wizardDescriptor.putProperty("WizardPanel_errorMessage", " "); //NOI18N
        return true;
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
    }
    
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        changeListeners.remove(l);
    }
    
    public void storeSettings(Object settings) {
        
    }
    
    public boolean isCMP() {
        return p.isCMP();
    }
    
    public boolean hasRemote() {
        return p.isRemote();
    }

    public boolean hasLocal() {
        return p.isLocal();
    }

    public String getPrimaryKeyClassName() {
        return p.getPrimaryKeyClassName();
    }
    
    public boolean isFinishPanel() {
        return isValid();
    }
    
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (changeListeners) {
            it = new HashSet(changeListeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

}

