/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel just asking for basic info.
 */
final class ParserConfigurationDescriptorPanel implements WizardDescriptor.Panel, NewMakeProjectWizardIterator.Name, ChangeListener {

    private WizardDescriptor wizardDescriptor;
    private ParserConfigurationPanel component;
    private String name;
    
    /** Create the wizard panel descriptor. */
    public ParserConfigurationDescriptorPanel() {
	name = NbBundle.getMessage(ParserConfigurationDescriptorPanel.class, "ParserConfigurationName"); // NOI18N
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new ParserConfigurationPanel(this);
	    component.setName(name);
        }
        return component;
    }

    public String getName() {
	return name;
    }

    public WizardDescriptor getWizardDescriptor() {
	return wizardDescriptor;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("NewMakeWizardP3"); // NOI18N
    }
    
    public boolean isValid() {
	boolean valid = ((ParserConfigurationPanel)getComponent()).valid(wizardDescriptor);
	if (valid)
	    wizardDescriptor.putProperty( "WizardPanel_errorMessage", ""); // NOI18N
	return valid;
    }
    
    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
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
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    public void stateChanged(ChangeEvent e) {
	fireChangeEvent();
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        component.read (wizardDescriptor);
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor)settings;
        component.store(d);
    }
}
