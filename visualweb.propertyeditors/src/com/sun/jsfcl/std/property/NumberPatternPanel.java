/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.sun.jsfcl.std.property;

import com.sun.rave.designtime.DesignProperty;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

/**
 * Panel for editing the number pattern
 *
 * @author  cao
 */
public class NumberPatternPanel extends JPanel {
    
    private DesignProperty designProperty;
    private NumberPatternPropertyEditor propertyEditor;
    
    /** Creates new form NumberPatternPanel */
    public NumberPatternPanel(NumberPatternPropertyEditor propertyEditor, DesignProperty liveProperty) {
       
        this.propertyEditor = propertyEditor;
        this.designProperty = liveProperty;
        
        initComponents();
        
        // Populate the pattern combo box with some sample patterns
        NumberFormat form1, form2, form3, form4;
        form1 = NumberFormat.getInstance();
        form2 = NumberFormat.getIntegerInstance();
        form3 = NumberFormat.getCurrencyInstance();
        form4 = NumberFormat.getPercentInstance();
        cmbPattern.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", ((DecimalFormat)form1).toPattern(), ((DecimalFormat)form2).toPattern(), ((DecimalFormat)form3).toPattern(), ((DecimalFormat)form4).toPattern() }));
        
        // The Example part of the panel
        cmbExample.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"1234.56", "-1234.56", "123.4567", "0.123", "01234"}));
        
        if( this.designProperty != null ) {
            cmbPattern.setSelectedItem( designProperty.getValue() );
        }
    }
    
    public void addPatternListener( ActionListener listener ) {
        cmbPattern.addActionListener( listener );
    }
    
    // The sample result will be updated based on the selected type or pattern
    private boolean upateSampleResult() {
        
        double sampleNumber = 0;
        
        try {
            if( cmbExample.getSelectedItem() == null ||
                    ((String)cmbExample.getSelectedItem()).trim().length() == 0 )
                return true;
            else
                sampleNumber = Double.parseDouble( (String)cmbExample.getSelectedItem() );
        } catch( NumberFormatException ne ) {
            
            txtResults.setText( NbBundle.getMessage(NumberPatternPanel.class, "notANumber", (String)cmbExample.getSelectedItem()) );
            return false;
        }
        
        // Pattern should be used to format the number
        try {
            if( cmbPattern.getSelectedItem() == null || ((String)cmbPattern.getSelectedItem()).trim().length() == 0 )
                return true;
            else {
                DecimalFormat decimalFormat = (DecimalFormat)DecimalFormat.getInstance();
                decimalFormat.applyPattern( ((String)cmbPattern.getSelectedItem()).trim() );
                txtResults.setText( decimalFormat.format( sampleNumber ) );
            }
        } catch( IllegalArgumentException ie ) {
            txtResults.setText( NbBundle.getMessage(NumberPatternPanel.class, "badPattern", (String)cmbPattern.getSelectedItem() ) );
            return false;
        }
        
        return true;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblPattern = new javax.swing.JLabel();
        cmbPattern = new javax.swing.JComboBox();
        pnlExample = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        lblExample = new javax.swing.JLabel();
        txtResults = new javax.swing.JTextField();
        lblResults = new javax.swing.JLabel();
        cmbExample = new javax.swing.JComboBox();
        btnTest = new javax.swing.JButton();
        txtExampleInstructions = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        lblPattern.setLabelFor(cmbPattern);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/sun/jsfcl/std/property/Bundle"); // NOI18N
        lblPattern.setText(bundle.getString("pattern")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        add(lblPattern, gridBagConstraints);
        lblPattern.getAccessibleContext().setAccessibleDescription(bundle.getString("pattern")); // NOI18N

        cmbPattern.setEditable(true);
        cmbPattern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPatternActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(cmbPattern, gridBagConstraints);
        cmbPattern.getAccessibleContext().setAccessibleDescription(bundle.getString("pattern")); // NOI18N

        pnlExample.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        pnlExample.add(jSeparator1, gridBagConstraints);

        lblExample.setLabelFor(cmbExample);
        org.openide.awt.Mnemonics.setLocalizedText(lblExample, org.openide.util.NbBundle.getMessage(NumberPatternPanel.class, "example_label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        pnlExample.add(lblExample, gridBagConstraints);
        lblExample.getAccessibleContext().setAccessibleDescription(bundle.getString("example_label")); // NOI18N

        txtResults.setEditable(false);
        txtResults.setText("1234.56");
        txtResults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtResultsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        pnlExample.add(txtResults, gridBagConstraints);
        txtResults.getAccessibleContext().setAccessibleDescription(bundle.getString("results_label")); // NOI18N

        lblResults.setLabelFor(txtResults);
        org.openide.awt.Mnemonics.setLocalizedText(lblResults, org.openide.util.NbBundle.getMessage(NumberPatternPanel.class, "results_label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        pnlExample.add(lblResults, gridBagConstraints);
        lblResults.getAccessibleContext().setAccessibleDescription(bundle.getString("results_label")); // NOI18N

        cmbExample.setEditable(true);
        cmbExample.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbExampleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        pnlExample.add(cmbExample, gridBagConstraints);
        cmbExample.getAccessibleContext().setAccessibleDescription(bundle.getString("example_label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnTest, org.openide.util.NbBundle.getMessage(NumberPatternPanel.class, "testButton_label")); // NOI18N
        btnTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        pnlExample.add(btnTest, gridBagConstraints);
        btnTest.getAccessibleContext().setAccessibleDescription(bundle.getString("test")); // NOI18N

        txtExampleInstructions.setBackground(getBackground());
        txtExampleInstructions.setBorder(null);
        txtExampleInstructions.setEditable(false);
        txtExampleInstructions.setText(bundle.getString("exampleInstruction")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 11);
        pnlExample.add(txtExampleInstructions, gridBagConstraints);
        txtExampleInstructions.getAccessibleContext().setAccessibleName(bundle.getString("exampleInstruction")); // NOI18N
        txtExampleInstructions.getAccessibleContext().setAccessibleDescription(bundle.getString("exampleInstruction")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(pnlExample, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void txtResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtResultsActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_txtResultsActionPerformed
    
    private void cmbExampleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbExampleActionPerformed
        upateSampleResult();
    }//GEN-LAST:event_cmbExampleActionPerformed
    
    private void btnTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestActionPerformed
        upateSampleResult();
    }//GEN-LAST:event_btnTestActionPerformed
    
    private void cmbPatternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPatternActionPerformed

        if( upateSampleResult() )
            propertyEditor.setAsText( ((String)cmbPattern.getSelectedItem() ));
    }//GEN-LAST:event_cmbPatternActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnTest;
    private javax.swing.JComboBox cmbExample;
    private javax.swing.JComboBox cmbPattern;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblExample;
    private javax.swing.JLabel lblPattern;
    private javax.swing.JLabel lblResults;
    private javax.swing.JPanel pnlExample;
    private javax.swing.JTextPane txtExampleInstructions;
    private javax.swing.JTextField txtResults;
    // End of variables declaration//GEN-END:variables
    
}
