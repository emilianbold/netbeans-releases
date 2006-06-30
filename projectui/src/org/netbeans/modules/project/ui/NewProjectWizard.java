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

package org.netbeans.modules.project.ui;

import java.text.MessageFormat;
import javax.swing.JComponent;

import org.openide.WizardDescriptor;
//import org.netbeans.spi.project.ui.templates.support.InstantiatingIterator;
import org.openide.filesystems.FileObject;

import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

public final class NewProjectWizard extends TemplateWizard {

    private FileObject templatesFO;
    private MessageFormat format;

    public NewProjectWizard (FileObject fo) {
        this.templatesFO = fo;
        putProperty (TemplatesPanelGUI.TEMPLATES_FOLDER, templatesFO);
        format = new MessageFormat (NbBundle.getBundle (NewFileWizard.class).getString ("LBL_NewProjectWizard_MessageFormat"));
        //setTitleFormat( new MessageFormat( "{0}") );
    }
        
    public void updateState () {
        super.updateState ();
        String substitute = (String)getProperty ("NewProjectWizard_Title"); // NOI18N
        String title;
        if (substitute == null) {
            title = NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Title"); // NOI18N
        } else {
            Object[] args = new Object[] {
                    NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Subtitle"), // NOI18N
                    substitute};
            title = format.format (args);
        }
        super.setTitle (title);
    }
    
    public void setTitle (String ignore) {}
    
    protected WizardDescriptor.Panel createTemplateChooser() {
        WizardDescriptor.Panel panel = new TemplatesPanel ();
        JComponent jc = (JComponent)panel.getComponent ();
        jc.setPreferredSize( new java.awt.Dimension (500, 340) );
        jc.setName (NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Name")); // NOI18N
        jc.getAccessibleContext ().setAccessibleName (NbBundle.getBundle (NewProjectWizard.class).getString ("ACSN_NewProjectWizard")); // NOI18N
        jc.getAccessibleContext ().setAccessibleDescription (NbBundle.getBundle (NewProjectWizard.class).getString ("ACSD_NewProjectWizard")); // NOI18N
        jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (0)); // NOI18N
        jc.putClientProperty ("WizardPanel_contentData", new String[] { // NOI18N
                NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Name"), // NOI18N
                NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Dots")}); // NOI18N
                
        return panel;
    }          
    
}
