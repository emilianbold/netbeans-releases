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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.NbBundle;

/**
 *
 * @author nam
 */
public abstract class AbstractPanel implements ChangeListener, FinishablePanel, Panel {
    private final java.util.Set listeners = new HashSet(1);
    protected java.lang.String panelName;
    protected org.openide.WizardDescriptor wizardDescriptor;

    public AbstractPanel (String name, WizardDescriptor wizardDescriptor) {
        this.panelName = name;
        this.wizardDescriptor = wizardDescriptor;
    }
    
    public abstract java.awt.Component getComponent();

    public abstract boolean isFinishPanel();

    public static interface Settings {
        public void read(WizardDescriptor wizard);
        public void store(WizardDescriptor wizard);
        public boolean valid(WizardDescriptor wizard);
        public void addChangeListener(ChangeListener l);
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        ((Settings)getComponent()).read(wizardDescriptor);
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        ((Settings)getComponent()).store(wizardDescriptor);
    }

    public boolean isValid() {
        if (getComponent() instanceof Settings) {
            return ((Settings)getComponent()).valid(wizardDescriptor);
        }
        return false;
    }
    
    public final void addChangeListener(javax.swing.event.ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    protected final void fireChangeEvent(javax.swing.event.ChangeEvent ev) {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    public final void removeChangeListener(javax.swing.event.ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    static void clearErrorMessage(WizardDescriptor wizard) {
        setErrorMessage(wizard, (String) null);
    }
    
    static void setErrorMessage(WizardDescriptor wizard, Throwable t) {
        String message = "";
        if (t != null) {
            message = (t.getLocalizedMessage());
        }
        wizard.putProperty("WizardPanel_errorMessage", message);
    }
    static void setErrorMessage(WizardDescriptor wizard, String key) {
        String message = "";
        if (key != null) {
            message = (NbBundle.getMessage(EntitySelectionPanel.class, key));
        }
        wizard.putProperty("WizardPanel_errorMessage", message);
    }

    protected void setErrorMessage(java.lang.String key) {
        setErrorMessage(wizardDescriptor, key);
    }

    public void stateChanged(javax.swing.event.ChangeEvent e) {
        Component c = getComponent();
        if (c instanceof Settings) {
            ((Settings)c).valid(wizardDescriptor);
        }
        fireChangeEvent(e);
    }
    
    public String getName() {
        return panelName;
    }
}
