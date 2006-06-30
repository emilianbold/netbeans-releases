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
* TestSuiteTargetPanel.java
*
* Created on April 10, 2002, 1:46 PM
*/

import java.net.URL;
import java.awt.Component;
import java.awt.CardLayout;
import java.util.StringTokenizer;
import javax.swing.JPanel;
import javax.swing.text.Document;
import javax.swing.SwingUtilities;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

import org.netbeans.modules.java.JavaDataObject;
import org.openide.util.NbBundle;

/** Wizard Panel with Test Suite Target selection
* @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
*/
public class TestSuiteTargetPanel extends JPanel {

    static final long serialVersionUID = 7320115283955775732L;
    private ChangeListener listener=null;
    private static final String DEFAULT_NAME=NbBundle.getMessage(TestSuiteTargetPanel.class, "CTL_DefaultName"); // NOI18N
    private boolean modified=true;
    
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
            return TestSuiteTargetPanel.this;
        }    

        /** returns Help Context
         * @return HelpCtx */    
        public HelpCtx getHelp() {
            return new HelpCtx(TestSuiteTargetPanel.class);
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
            set.suiteName=name;
            set.suitePackage=packageField.getText().replace('.','/');
            set.suiteTemplate=(DataObject)templateCombo.getSelectedItem();
            if (modified) {
                set.templateMethods=WizardIterator.getTemplateMethods((JavaDataObject)set.suiteTemplate);
                set.methods=null;
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
            StringTokenizer st=new StringTokenizer(packageField.getText().replace('.','/'),"/"); // NOI18N
            while (st.hasMoreTokens())
                if (!Utilities.isJavaIdentifier(st.nextToken()))
                    return false;
            return DEFAULT_NAME.equals(nameField.getText())||Utilities.isJavaIdentifier(nameField.getText());
        }
    }
    
    /** Creates new form TestSuiteTargetPanel */
    public TestSuiteTargetPanel() {
        setName(NbBundle.getMessage(TestSuiteTargetPanel.class, "LBL_TestSuitePanelDescription")); // NOI18N
        initComponents();
        templateCombo.setRenderer(new WizardIterator.MyCellRenderer());
        templateCombo.setModel(new DefaultComboBoxModel(WizardIterator.getSuiteTemplates()));
        templateComboActionPerformed(null);
        DocumentListener list=new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {panel.fireStateChanged();}
            public void removeUpdate(DocumentEvent e) {panel.fireStateChanged();}
            public void changedUpdate(DocumentEvent e) {panel.fireStateChanged();}
        };
        nameField.getDocument().addDocumentListener(list);
        packageField.getDocument().addDocumentListener(list);
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
        packageLabel = new javax.swing.JLabel();
        packageField = new javax.swing.JTextField();
        templateLabel = new javax.swing.JLabel();
        templateCombo = new javax.swing.JComboBox();
        descriptionLabel = new javax.swing.JLabel();
        descriptionPanel = new javax.swing.JPanel();
        noDescription = new javax.swing.JLabel();
        htmlBrowser = new org.openide.awt.HtmlBrowser();

        setLayout(new java.awt.GridBagLayout());

        setMinimumSize(new java.awt.Dimension(135, 156));
        setPreferredSize(new java.awt.Dimension(408, 334));
        nameLabel.setDisplayedMnemonic(NbBundle.getMessage(TestSuiteTargetPanel.class, "MNM_TestSuiteName").charAt(0) );
        nameLabel.setLabelFor(nameField);
        nameLabel.setText(org.openide.util.NbBundle.getMessage(TestSuiteTargetPanel.class, "LBL_TestSuiteName"));
        nameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestSuiteTargetPanel.class, "TTT_TestSuiteName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 11, 0, 11);
        add(nameLabel, gridBagConstraints);

        nameField.setToolTipText(org.openide.util.NbBundle.getMessage(TestSuiteTargetPanel.class, "TTT_TestSuiteName"));
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
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 11);
        add(nameField, gridBagConstraints);

        packageLabel.setDisplayedMnemonic(NbBundle.getMessage(TestSuiteTargetPanel.class, "MNM_TestSuitePackage").charAt(0) );
        packageLabel.setLabelFor(packageField);
        packageLabel.setText(org.openide.util.NbBundle.getMessage(TestSuiteTargetPanel.class, "LBL_TestSuitePackage"));
        packageLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestSuiteTargetPanel.class, "TTT_TestSuitePackage"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        add(packageLabel, gridBagConstraints);

        packageField.setToolTipText(org.openide.util.NbBundle.getMessage(TestSuiteTargetPanel.class, "TTT_TestSuitePackage"));
        packageField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                packageFieldFocusGained(evt);
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
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 11);
        add(packageField, gridBagConstraints);

        templateLabel.setDisplayedMnemonic(NbBundle.getMessage(TestSuiteTargetPanel.class, "MNM_TestSuiteTemplate").charAt(0) );
        templateLabel.setLabelFor(templateCombo);
        templateLabel.setText(org.openide.util.NbBundle.getMessage(TestSuiteTargetPanel.class, "LBL_TestSuiteTemplate"));
        templateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestSuiteTargetPanel.class, "TTT_TestSuiteTemplate"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        add(templateLabel, gridBagConstraints);

        templateCombo.setToolTipText(org.openide.util.NbBundle.getMessage(TestSuiteTargetPanel.class, "TTT_TestSuiteTemplate"));
        templateCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                templateComboActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 11);
        add(templateCombo, gridBagConstraints);

        descriptionLabel.setDisplayedMnemonic(NbBundle.getMessage(TestSuiteTargetPanel.class, "MNM_TestSuiteDescription").charAt(0) );
        descriptionLabel.setLabelFor(htmlBrowser);
        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(TestSuiteTargetPanel.class, "LBL_TestSuiteDescription"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(17, 11, 0, 11);
        add(descriptionLabel, gridBagConstraints);

        descriptionPanel.setLayout(new java.awt.CardLayout());

        descriptionPanel.setPreferredSize(new java.awt.Dimension(400, 200));
        noDescription.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noDescription.setText(org.openide.util.NbBundle.getMessage(TestSuiteTargetPanel.class, "LBL_TestSuiteNoDescription"));
        descriptionPanel.add(noDescription, "no");

        htmlBrowser.setEnableHome(false);
        htmlBrowser.setEnableLocation(false);
        htmlBrowser.setPreferredSize(new java.awt.Dimension(400, 200));
        htmlBrowser.setStatusLineVisible(false);
        htmlBrowser.setToolbarVisible(false);
        htmlBrowser.setAutoscrolls(true);
        descriptionPanel.add(htmlBrowser, "yes");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 11, 11);
        add(descriptionPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private void templateComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templateComboActionPerformed
        modified=true;
        URL url=null;
        DataObject dob=(DataObject)templateCombo.getSelectedItem();
        if (dob!=null)
            url=TemplateWizard.getDescription(dob);
        if (url==null) {
            ((CardLayout)descriptionPanel.getLayout()).show(descriptionPanel, "no"); // NOI18N
        } else {
            htmlBrowser.setURL(url);
            ((CardLayout)descriptionPanel.getLayout()).show(descriptionPanel, "yes"); // NOI18N
        }
    }//GEN-LAST:event_templateComboActionPerformed

    private void packageFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_packageFieldFocusGained
        packageField.selectAll();
    }//GEN-LAST:event_packageFieldFocusGained

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
    private javax.swing.JTextField packageField;
    private javax.swing.JLabel packageLabel;
    // End of variables declaration//GEN-END:variables
     
}
