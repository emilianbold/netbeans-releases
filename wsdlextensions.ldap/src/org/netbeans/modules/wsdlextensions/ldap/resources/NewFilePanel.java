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
package org.netbeans.modules.wsdlextensions.ldap.resources;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

final class NewFilePanel implements WizardDescriptor.Panel<WizardDescriptor> {

    NewFilePanel(Project aProject, SourceGroup[] aFolders) {
        this.folders = aFolders;
        this.project = aProject;
    }
    
    TemplateWizard getTemplateWizard() {
        return templateWizard;
    }
    
    public Component getComponent() {
        if (gui == null) {
            gui=new NewOptionsPanel(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(NewFilePanel.class);
    }

    public boolean isValid() {
        return true;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    protected void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    public void readSettings( WizardDescriptor settings ) {
        templateWizard = (TemplateWizard)settings;
    }

    public void storeSettings(WizardDescriptor settings) {
        if ( WizardDescriptor.PREVIOUS_OPTION.equals( settings.getValue() ) ) {
            return;
        }
        if ( WizardDescriptor.CANCEL_OPTION.equals( settings.getValue() ) ) {
            return;
        }
        
        settings.putProperty ("NewFileWizard_Title", null); // NOI18N
    }
    
    Project getProject() {
        return project;
    }

    private NewOptionsPanel gui;
    private Project project;
    private SourceGroup[] folders;
    private TemplateWizard templateWizard;
    private final List listeners = new ArrayList();
}
