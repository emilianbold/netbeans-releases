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

package org.netbeans.modules.websvc.core.dev.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 *
 * @author radko
 */
public class WebServiceFromWSDL implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel, WizardDescriptor.ValidatingPanel, ChangeListener {

    private WebServiceFromWSDLPanel component;
    private WizardDescriptor wizardDescriptor;
    private Project project;
    
    /** Creates a new instance of WebServiceType */
    public WebServiceFromWSDL(WizardDescriptor wizardDescriptor, Project project) {
        this.wizardDescriptor = wizardDescriptor;
        this.project = project;
    }

    public Component getComponent() {
        if (component == null) {
            component = new WebServiceFromWSDLPanel(project);
            component.addChangeListener(this);
        }
        
        return component;
    }

    public HelpCtx getHelp() {
        HelpCtx helpCtx = null;
        if (getComponent() != null && (helpCtx = component.getHelpCtx()) != null)
            return helpCtx;
        
        return new HelpCtx(WebServiceFromWSDL.class);
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read (wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }

    public boolean isValid() {
        getComponent();
        return component.isValid(wizardDescriptor);
    }

    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public boolean isFinishPanel() {
        return isValid();
    }

    public void validate() throws WizardValidationException {
        component.validate(wizardDescriptor);
    }

    public void stateChanged(ChangeEvent e) {
        fireChange();
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

}
