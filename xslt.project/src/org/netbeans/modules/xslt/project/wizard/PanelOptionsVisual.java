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
package org.netbeans.modules.xslt.project.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.compapp.projects.base.ui.wizards.SettingsPanel;
import org.netbeans.modules.compapp.projects.base.ui.wizards.PanelConfigureProject;
import org.netbeans.modules.compapp.projects.base.ui.wizards.ProjectNameAware;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class PanelOptionsVisual extends SettingsPanel implements ProjectNameAware {
    private PanelConfigureProject panel;
    private org.netbeans.modules.compapp.projects.base.ui.wizards.PanelOptionsVisual parentOptions;
    private javax.swing.JLabel targetNsLabel;
    private javax.swing.JTextField targetNsTextField;
    private JTextField projectNameTextField;
    
    
    public PanelOptionsVisual(PanelConfigureProject panel) {
        initComponents();
        this.panel = panel;
    }
    
    public void attachProjectNameListener(JTextField projectNameTextFiled) {
        this.projectNameTextField = projectNameTextFiled;
        if (projectNameTextFiled == null) {
            targetNsTextField.setText(NewXsltproProjectWizardIterator.TRANSFORMMAP_NS_PREFIX);
        } else {
            targetNsTextField.setText(NewXsltproProjectWizardIterator.TRANSFORMMAP_NS_PREFIX+projectNameTextFiled.getName());
            Document doc = projectNameTextFiled.getDocument();
            doc.addDocumentListener(new ProjectNameDocumentListener());
        }
    }
    
    private void initComponents() {
        targetNsLabel = new JLabel();
        targetNsTextField = new JTextField();

        setLayout(new GridBagLayout());
               
        targetNsLabel.setLabelFor(targetNsTextField);
        Mnemonics.setLocalizedText(targetNsLabel, 
                NbBundle.getMessage(PanelOptionsVisual.class, "LBL_targetNamespace")); // NOI18N
        targetNsLabel.setToolTipText(
                NbBundle.getMessage(PanelOptionsVisual.class, "HINT_targetNamespace")); // NOI18N
        targetNsLabel.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_targetNamespace")); // NOI18N
        targetNsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
                PanelOptionsVisual.class, "ACSD_targetNamespace")); // NOI18N

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 0, 2, 0);
        add(targetNsLabel, gridBagConstraints);


        targetNsTextField.setToolTipText(
                NbBundle.getMessage(PanelOptionsVisual.class, "HINT_targetNamespace")); // NOI18N
        targetNsTextField.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_targetNamespace")); // NOI18N
        targetNsTextField.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_targetNamespace")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 2, 0);
        add(targetNsTextField, gridBagConstraints);
        
        parentOptions = new org.netbeans.modules.compapp.projects.base.ui.wizards.PanelOptionsVisual(panel);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
//        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
//        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(5, 0, 2, 0);
        add(parentOptions, gridBagConstraints);
    }

    @Override
    public void store(WizardDescriptor settings) {
        assert parentOptions != null;
        parentOptions.store(settings);
        settings.putProperty(NewXsltproProjectWizardIterator.TMAP_NS_PROPERTY, 
                targetNsTextField.getText());
    }

    @Override
    public void read(WizardDescriptor settings) {
        assert parentOptions != null;
        parentOptions.read(settings);
    }

    @Override
    public boolean valid(WizardDescriptor settings) {
        assert parentOptions != null;
        if (!parentOptions.valid(settings)) {
            return false;
        }
        
        String ns = targetNsTextField != null ? targetNsTextField.getText() : null;
        if (!isValidNS(ns)) {
            settings.putProperty("WizardPanel_errorMessage", 
                    NbBundle.getMessage(PanelOptionsVisual.class,"MSG_IllegalNamespace", ns)); //NOI18N
            return false;
        }
        return true;
    }
    
    // TODO m
    private boolean isValidNS(String ns) {
        if (ns == null) {
            return false;
        }
        if (
                ns.indexOf('&') >= 0
           )
        {
            return false;
        }
        return true;
    }

    private class ProjectNameDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            documentChanged(e);
        }

        public void removeUpdate(DocumentEvent e) {
            documentChanged(e);
        }

        public void changedUpdate(DocumentEvent e) {
            documentChanged(e);
        }
        
        private void documentChanged(DocumentEvent e) {
            if (projectNameTextField == null) {
                targetNsTextField.setText(NewXsltproProjectWizardIterator.TRANSFORMMAP_NS_PREFIX);
            } else {
                targetNsTextField.setText(
                        NewXsltproProjectWizardIterator.TRANSFORMMAP_NS_PREFIX
                        +projectNameTextField.getText());
            }
        }
    }
    
}
