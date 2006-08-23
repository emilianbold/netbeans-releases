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
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel just asking for basic info.
 */
public class PanelConfigureProject implements WizardDescriptor.Panel, NewMakeProjectWizardIterator.Name, WizardDescriptor.FinishablePanel {
    
    private WizardDescriptor wizardDescriptor;
    private String name;
    private PanelConfigureProjectVisual component;
    private String title;
    private String wizardTitle;
    private String wizardACSD;
    private boolean initialized = false;
    private boolean showMakefileTextField;
    
    /** Create the wizard panel descriptor. */
    public PanelConfigureProject(String name, String wizardTitle, String wizardACSD, boolean showMakefileTextField) {
        this.name = name;
        this.wizardTitle = wizardTitle;
        this.wizardACSD = wizardACSD;
        this.showMakefileTextField = showMakefileTextField;
	title = NbBundle.getMessage(PanelConfigureProject.class, "LAB_ConfigureProject"); // NOI18N
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new PanelConfigureProjectVisual(this, this.name, this.wizardTitle, this.wizardACSD, showMakefileTextField);
        }
        return component;
    }

    public String getName() {
	return title;
    }
    
    public HelpCtx getHelp() {
	/* FIXUP
        switch ( type ) {
            case ProjectDescriptor.TYPE_NEW_APP:
                return new HelpCtx( this.getClass().getName() + "_APP" ); // NOI18N
            case ProjectDescriptor.TYPE_NEW_LIB:
                return new HelpCtx( this.getClass().getName() + "_LIB" ); // NOI18N
            case ProjectDescriptor.TYPE_EXT_APP:
                return new HelpCtx( this.getClass().getName() + "_EXT_APP" ); // NOI18N
            case ProjectDescriptor.TYPE_EXT_LIB:
                return new HelpCtx( this.getClass().getName() + "_EXT_LIB" ); // NOI18N
        }        
	*/
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
        if (initialized)
            return;
        wizardDescriptor = (WizardDescriptor)settings;        
        component.read (wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent)component).getClientProperty ("NewProjectWizard_Title"); // NOI18N
        if (substitute != null) {
            wizardDescriptor.putProperty ("NewProjectWizard_Title", substitute); // NOI18N
        }
        initialized = true;
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor)settings;
        component.store(d);
    }

    public boolean isFinishPanel() {
	return false;
    }
}
