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

package org.netbeans.modules.web.struts.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import org.netbeans.api.project.Project;

/**
 *
 * @author radko
 */
public class ActionPanel1 implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    private WizardDescriptor wizardDescriptor;
    private ActionPanel1Visual component;
    private Project project;

    /** Creates a new instance of ActionPanel */
    public ActionPanel1(Project project) {
        this.project=project;
    }
    
    Project getProject() {
        return project;
    }
    
    public Component getComponent() {
        if (component == null)
            component = new ActionPanel1Visual(this);

        return component;
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((javax.swing.JComponent) component).getClientProperty("NewFileWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewFileWizard_Title", substitute); // NOI18N
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor desc = (WizardDescriptor) settings;
        component.store(desc);
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ActionPanel1.class);
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
    
    public boolean isFinishPanel() {
        return isValid();
    }

}
