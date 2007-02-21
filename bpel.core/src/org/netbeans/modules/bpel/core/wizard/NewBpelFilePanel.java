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



package org.netbeans.modules.bpel.core.wizard;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * from nb webservice module
 */
final class NewBpelFilePanel implements WizardDescriptor.Panel {

    NewBpelFilePanel(Project project, SourceGroup[] folders) {
        this.folders = folders;
        this.project = project;
    }
    
    TemplateWizard getTemplateWizard() {
        return templateWizard;
    }
    
    void setNameTF(JTextField nameTF) {
        gui.attachFileNameListener(nameTF);
    }

    public Component getComponent() {
        if (gui == null) {
            gui=new BpelOptionsPanel(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(NewBpelFilePanel.class);
    }

    public boolean isValid() {
        boolean valid = true;
        
        if(gui.getNamespaceTextField().contains(" "))
            valid = false;
        
        return valid;
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

    public void readSettings( Object settings ) {
        templateWizard = (TemplateWizard)settings;
    }

    public void storeSettings(Object settings) {
        if ( WizardDescriptor.PREVIOUS_OPTION.equals( 
                ((WizardDescriptor)settings).getValue() ) ) 
        {
            return;
        }
        if ( WizardDescriptor.CANCEL_OPTION.equals( 
                ((WizardDescriptor)settings).getValue() ) ) 
        {
            return;
        }
        
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
    }
    
    String getNS() {
        return gui.getNamespaceTextField();
    }
    
    String getWsName() {
        return gui.getWsName();
    }

    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private BpelOptionsPanel gui;

    private Project project;
    private SourceGroup[] folders;
    private TemplateWizard templateWizard;
   
}
