/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 * Panel just asking for basic info.
 * @author Jesse Glick
 */
final class PanelConfigureProject implements WizardDescriptor.Panel, WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {
    
    private WizardDescriptor wizardDescriptor;
    private int type;
    private PanelConfigureProjectVisual component;
    
    /** Create the wizard panel descriptor. */
    public PanelConfigureProject( int type ) {
        this.type = type;
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new PanelConfigureProjectVisual(this, this.type);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        
        switch ( type ) {
            case NewJ2SEProjectWizardIterator.TYPE_APP:
                return new HelpCtx( this.getClass().getName() + "_APP" ); // NOI18N
            case NewJ2SEProjectWizardIterator.TYPE_LIB:
                return new HelpCtx( this.getClass().getName() + "_LIB" ); // NOI18N
            case NewJ2SEProjectWizardIterator.TYPE_EXT:
                return new HelpCtx( this.getClass().getName() + "_EXT" ); // NOI18N
        }        
        return new HelpCtx( PanelConfigureProject.class );
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid( wizardDescriptor );
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
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        component.read (wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent)component).getClientProperty ("NewProjectWizard_Title"); // NOI18N
        if (substitute != null) {
            wizardDescriptor.putProperty ("NewProjectWizard_Title", substitute); // NOI18N
        }
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor)settings;
        component.store(d);
        ((WizardDescriptor)d).putProperty ("NewProjectWizard_Title", null); // NOI18N
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    public void validate () throws WizardValidationException {
        getComponent ();
        component.validate (wizardDescriptor);
    }

}
