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

package org.netbeans.modules.testtools.wizards;

/*
 * TestTypeTemplatePanel.java
 *
 * Created on April 10, 2002, 1:46 PM
 */

import java.net.URL;
import java.awt.Component;
import java.awt.CardLayout;
import java.util.StringTokenizer;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.text.Document;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.DefaultComboBoxModel;

import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/** Wizard Panel with Test Type Template selection
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TestTypeTemplatePanel extends JPanel {
    
    static final long serialVersionUID = 2893559646017815470L;
    
    private ChangeListener listener=null;
    private static final String DEFAULT_NAME=NbBundle.getMessage(TestTypeTemplatePanel.class, "CTL_DefaultName"); // NOI18N
    boolean modified=true;
    
    public final Panel panel = new Panel();
    
    private class Panel extends Object implements WizardDescriptor.Panel {
        
        /** adds ChangeListener of current Panel
         * @param changeListener ChangeListener */    
        public void addChangeListener(ChangeListener changeListener) {
            if (listener != null) throw new IllegalStateException ();
            listener = changeListener;
        }    

        /** returns current Panel
         * @return Component */    
        public Component getComponent() {
            return TestTypeTemplatePanel.this;
        }    

        /** returns Help Context
         * @return HelpCtx */    
        public HelpCtx getHelp() {
            return new HelpCtx(TestTypeTemplatePanel.class);
        }

        /** read settings from given Object
         * @param obj TemplateWizard with settings */    
        public void readSettings(Object obj) {}

        /** removes Change Listener of current Panel
         * @param changeListener ChangeListener */    
        public void removeChangeListener(ChangeListener changeListener) {
            listener = null;
        }

        /** stores settings to given Object
         * @param obj TemplateWizard with settings */    
        public void storeSettings(Object obj) {
            WizardSettings set=WizardSettings.get(obj);
            String name=nameField.getText();
            if (DEFAULT_NAME.equals(name))
                name=null;
            set.typeName=name;
            set.typeTemplate=(DataObject)templateCombo.getSelectedItem();
            if (modified) {
                set.readTypeSettings();
                modified=false;
            }
        }

        private void fireStateChanged() {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    if (listener != null) {
                        listener.stateChanged (new ChangeEvent (this));
                    }
                    if (nameField.getText().equals ("")) { // NOI18N
                        nameField.setText(DEFAULT_NAME);
                        nameField.selectAll();
                    }
                }
            });            
        }

        /** test current Panel state for data validity
         * @return boolean true if data are valid and Wizard can continue */    
        public boolean isValid() {
            return DEFAULT_NAME.equals(nameField.getText())||nameField.getText().indexOf(' ')<0;
        }
    }
    
    /** Creates new form TestTypeTemplatePanel */
    public TestTypeTemplatePanel() {
        setName(NbBundle.getMessage(TestTypeTemplatePanel.class, "LBL_TestTypePanelName")); // NOI18N
        initComponents();
        templateCombo.setRenderer(new WizardIterator.MyCellRenderer());
        templateCombo.setModel(new DefaultComboBoxModel(WizardIterator.getTestTypeTemplates()));
        templateComboActionPerformed(null);
        DocumentListener list=new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {panel.fireStateChanged();}
            public void removeUpdate(DocumentEvent e) {panel.fireStateChanged();}
            public void changedUpdate(DocumentEvent e) {panel.fireStateChanged();}
        };
        nameField.getDocument().addDocumentListener(list);
        panel.fireStateChanged();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        templateLabel = new javax.swing.JLabel();
        templateCombo = new javax.swing.JComboBox();
        descriptionLabel = new javax.swing.JLabel();
        descriptionPanel = new javax.swing.JPanel();
        noDescription = new javax.swing.JLabel();
        htmlBrowser = new org.openide.awt.HtmlBrowser();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setDisplayedMnemonic(NbBundle.getMessage(TestTypeTemplatePanel.class, "MNM_TestTypeName").charAt(0) );
        nameLabel.setLabelFor(nameField);
        nameLabel.setText(org.openide.util.NbBundle.getMessage(TestTypeTemplatePanel.class, "LBL_TestTypeName"));
        nameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeTemplatePanel.class, "TTT_TestTypeName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(nameLabel, gridBagConstraints);

        nameField.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeTemplatePanel.class, "TTT_TestTypeName"));
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        add(nameField, gridBagConstraints);

        templateLabel.setDisplayedMnemonic(NbBundle.getMessage(TestTypeTemplatePanel.class, "MNM_TestTypeTemplate").charAt(0) );
        templateLabel.setLabelFor(templateCombo);
        templateLabel.setText(org.openide.util.NbBundle.getMessage(TestTypeTemplatePanel.class, "LBL_TestTypeTemplate"));
        templateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeTemplatePanel.class, "TTT_TestTypeTemplate"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(templateLabel, gridBagConstraints);

        templateCombo.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeTemplatePanel.class, "TTT_TestTypeTemplate"));
        templateCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                templateComboActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        add(templateCombo, gridBagConstraints);

        descriptionLabel.setDisplayedMnemonic(NbBundle.getMessage(TestTypeTemplatePanel.class, "MNM_TestTypeDescription").charAt(0) );
        descriptionLabel.setLabelFor(htmlBrowser);
        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(TestTypeTemplatePanel.class, "LBL_TestTypeTemplateDescription"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(17, 12, 0, 11);
        add(descriptionLabel, gridBagConstraints);

        descriptionPanel.setLayout(new java.awt.CardLayout());

        descriptionPanel.setPreferredSize(new java.awt.Dimension(400, 200));
        noDescription.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noDescription.setText(org.openide.util.NbBundle.getMessage(TestTypeTemplatePanel.class, "MSG_NoDescription"));
        descriptionPanel.add(noDescription, "no");

        htmlBrowser.setEnableHome(false);
        htmlBrowser.setEnableLocation(false);
        htmlBrowser.setPreferredSize(new java.awt.Dimension(400, 200));
        htmlBrowser.setStatusLineVisible(false);
        htmlBrowser.setToolbarVisible(false);
        htmlBrowser.setAutoscrolls(true);
        descriptionPanel.add(htmlBrowser, "yes");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 11, 11);
        add(descriptionPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private void templateComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templateComboActionPerformed
        modified=true;
        URL url=null;
        DataObject dob=(DataObject)templateCombo.getSelectedItem();
        if (dob!=null) {
            url=TemplateWizard.getDescription(dob);
        }
        if (url==null) {
            ((CardLayout)descriptionPanel.getLayout()).show(descriptionPanel, "no"); // NOI18N
        } else {
            htmlBrowser.setURL(url);
            ((CardLayout)descriptionPanel.getLayout()).show(descriptionPanel, "yes"); // NOI18N
        }
    }//GEN-LAST:event_templateComboActionPerformed

    private void nameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusGained
        nameField.selectAll();
    }//GEN-LAST:event_nameFieldFocusGained

            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.openide.awt.HtmlBrowser htmlBrowser;
    private javax.swing.JLabel noDescription;
    private javax.swing.JComboBox templateCombo;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JLabel descriptionLabel;
    // End of variables declaration//GEN-END:variables
    
}
