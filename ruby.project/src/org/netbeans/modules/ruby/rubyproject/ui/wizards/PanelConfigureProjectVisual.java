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

package org.netbeans.modules.ruby.rubyproject.ui.wizards;

import java.io.File;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.NbBundle;

/** First panel in the NewProject wizard. Used for filling in
 * name, and directory of the project.
 *
 * @author Petr Hrebejk
 */
public class PanelConfigureProjectVisual extends JPanel {

    private PanelConfigureProject panel;
        
    private boolean ignoreProjectDirChanges;
    
    private boolean ignoreRakeProjectNameChanges;
    
    private boolean noDir = true;
    
    private SettingsPanel projectLocationPanel;
    
    private PanelOptionsVisual optionsPanel;
    
    private int type;
    
    /** Creates new form PanelInitProject */
    public PanelConfigureProjectVisual( PanelConfigureProject panel, int type ) {
        this.panel = panel;
        initComponents();                
        this.type = type;
        setName(NbBundle.getMessage(PanelConfigureProjectVisual.class,"TXT_NameAndLoc")); // NOI18N
        if (type == NewRubyProjectWizardIterator.TYPE_APP) {
            projectLocationPanel = new PanelProjectLocationVisual( panel, type );
            putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PanelConfigureProjectVisual.class,"TXT_NewJavaApp")); // NOI18N
            jSeparator1.setVisible(true);
            getAccessibleContext ().setAccessibleName (NbBundle.getMessage(PanelConfigureProjectVisual.class,"TXT_NewJavaApp")); // NOI18N
            getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(PanelConfigureProjectVisual.class,"ACSD_NewJavaApp")); // NOI18N
        }                       
//        else if (type == NewRubyProjectWizardIterator.TYPE_LIB) {
//            projectLocationPanel = new PanelProjectLocationVisual( panel, type );
//            jSeparator1.setVisible (false);
//            putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PanelConfigureProjectVisual.class,"TXT_NewJavaLib")); // NOI18N
//            getAccessibleContext ().setAccessibleName (NbBundle.getMessage(PanelConfigureProjectVisual.class,"TXT_NewJavaLib")); // NOI18N
//            getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(PanelConfigureProjectVisual.class,"ACSD_NewJavaLib")); // NOI18N
//        }
        else {
            projectLocationPanel = new PanelProjectLocationExtSrc ( panel );
            jSeparator1.setVisible(true);
            putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PanelConfigureProjectVisual.class,"TXT_JavaExtSourcesProjectLocation")); // NOI18N
            getAccessibleContext ().setAccessibleName (NbBundle.getMessage(PanelConfigureProjectVisual.class,"TXT_JavaExtSourcesProjectLocation")); // NOI18N
            getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(PanelConfigureProjectVisual.class,"ACSD_JavaExtSourcesProjectLocation")); // NOI18N
        }
        locationContainer.add( projectLocationPanel, java.awt.BorderLayout.CENTER );
        optionsPanel = new PanelOptionsVisual( panel, type );
        projectLocationPanel.addPropertyChangeListener(optionsPanel);
        optionsContainer.add( optionsPanel, java.awt.BorderLayout.CENTER );
    }
    
    boolean valid( WizardDescriptor wizardDescriptor ) {
        wizardDescriptor.putProperty( "WizardPanel_errorMessage", "" ); //NOI18N
        return projectLocationPanel.valid( wizardDescriptor ) && optionsPanel.valid(wizardDescriptor);
    }
    
    void read (WizardDescriptor d) {
        Integer lastType = (Integer) d.getProperty("ruby-wizard-type");  //NOI18N        
        if (lastType == null || lastType.intValue() != this.type) {
            //bugfix #46387 The type of project changed, reset values to defaults
            d.putProperty ("name", null); // NOI18N
            d.putProperty ("projdir",null); // NOI18N
        }
        projectLocationPanel.read (d);
        optionsPanel.read (d);
    }
    
    void store( WizardDescriptor d ) {
        d.putProperty("ruby-wizard-type", new Integer(this.type));   //NOI18N
        projectLocationPanel.store( d );
        optionsPanel.store( d );        
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        projectLocationPanel.validate (d);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        locationContainer = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        optionsContainer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        locationContainer.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(locationContainer, gridBagConstraints);
        locationContainer.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelConfigureProjectVisual.class).getString("ACSN_locationContainer"));
        locationContainer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelConfigureProjectVisual.class).getString("ACSD_locationContainer"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        add(jSeparator1, gridBagConstraints);

        optionsContainer.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(optionsContainer, gridBagConstraints);
        optionsContainer.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelConfigureProjectVisual.class).getString("ACSN_optionsContainer"));
        optionsContainer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelConfigureProjectVisual.class).getString("ACSD_optionsContainer"));

    }//GEN-END:initComponents

    /** Currently only handles the "Browse..." button
     */
           
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel locationContainer;
    private javax.swing.JPanel optionsContainer;
    // End of variables declaration//GEN-END:variables

    
}
