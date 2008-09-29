/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.web.core.palette.items;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.MsgHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author  Libor Kotouc, Petr Slechta
 */
public class SQLStmtCustomizer extends javax.swing.JPanel {

    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private boolean dialogOK = false;
    SQLStmt stmt;
    JTextComponent target;
    private String displayName;
    private String stmtLabel;
    private String stmtACSN;
    private String stmtACSD;
    private String helpID;
    private boolean mayVariableNameBeEmpty;
    private MsgHelper msgHelper;

    /**************************************************************************/
    public SQLStmtCustomizer(SQLStmt stmt, JTextComponent target,
                             String displayName, String stmtLabel, String stmtACSN, String stmtACSD,
                             String helpID)
    {
        this(stmt, target, displayName, stmtLabel, stmtACSN, stmtACSD, helpID, true);
    }
    
    /**************************************************************************/
    public SQLStmtCustomizer(SQLStmt stmt, JTextComponent target,
                             String displayName, String stmtLabel, String stmtACSN, String stmtACSD,
                             String helpID, boolean mayVariableNameBeEmpty)
    {
        this.stmt = stmt;
        this.target = target;
        this.displayName = displayName;
        this.stmtLabel = stmtLabel;
        this.stmtACSN = stmtACSN;
        this.stmtACSD = stmtACSD;
        this.helpID = helpID;
        this.mayVariableNameBeEmpty = mayVariableNameBeEmpty;
        
        initComponents();
        msgHelper = new MsgHelper(errorMessage, SQLStmtCustomizer.class);

        jTextField1.setText(stmt.getVariable());
        if (!mayVariableNameBeEmpty) {
            jTextField1.getDocument().addDocumentListener(new DocumentListener(){
                public void insertUpdate(DocumentEvent evt) {
                    validateInput();
                }
                public void removeUpdate(DocumentEvent evt) {
                    validateInput();
                }
                public void changedUpdate(DocumentEvent evt) {
                    validateInput();
                }
            });
        }
        
        jComboBox2.setModel(new DefaultComboBoxModel(SQLStmt.scopes));
        jComboBox2.setSelectedIndex(stmt.getScopeIndex());        
        jTextField2.setText(stmt.getDataSource());
        jTextArea1.setText(stmt.getStmt());
    }
    
    /**************************************************************************/
    private void validateInput() {
        if (jTextField1.getText().trim().length() < 1) {
            msgHelper.setInfoMsg("Error_Empty_VariableName"); // NOI18N
            descriptor.setValid(false);
            return;
        }

        msgHelper.setErrorMsg(null);
        descriptor.setValid(true);
    }
    
    /**************************************************************************/
    public boolean showDialog() {
        dialogOK = false;
        
        descriptor = new DialogDescriptor
                (this, NbBundle.getMessage(SQLStmtCustomizer.class, "LBL_Customizer_InsertPrefix") + " " + displayName, true,  // NOI18N
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            evaluateInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
		     }
		 } 
                );
        
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    /**************************************************************************/
    private void evaluateInput() {
        String variable = jTextField1.getText();
        stmt.setVariable(variable);
        
        int scopeIndex = jComboBox2.getSelectedIndex();
        stmt.setScopeIndex(scopeIndex);
        
        String dataSource = jTextField2.getText();
        stmt.setDataSource(dataSource);
        
        String stmtString = jTextArea1.getText();
        stmt.setStmt(stmtString);
    }
    
    /***************************************************************************
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jFileChooser1 = new javax.swing.JFileChooser();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        errorMessage = new javax.swing.JLabel();

        jFileChooser1.setCurrentDirectory(null);

        setLayout(new java.awt.GridBagLayout());

        jLabel4.setLabelFor(jComboBox2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SQLStmtCustomizer.class, "LBL_Stmt_Scope")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLStmtCustomizer.class, "ACSN_Stmt_Scope")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLStmtCustomizer.class, "ACSD_Stmt_Scope")); // NOI18N

        jLabel2.setLabelFor(jTextField1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SQLStmtCustomizer.class, "LBL_Stmt_Variable")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLStmtCustomizer.class, "ACSN_Stmt_Variable")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLStmtCustomizer.class, "ACSD_Stmt_Variable")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jComboBox2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jTextField1, gridBagConstraints);

        jLabel1.setLabelFor(jTextArea1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, stmtLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(stmtACSN);
        jLabel1.getAccessibleContext().setAccessibleDescription(stmtACSD);

        jTextArea1.setColumns(35);
        jTextArea1.setRows(10);
        jScrollPane1.setViewportView(jTextArea1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(jScrollPane1, gridBagConstraints);

        jLabel3.setLabelFor(jTextField2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SQLStmtCustomizer.class, "LBL_Stmt_DataSource")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLStmtCustomizer.class, "ACSN_Stmt_DataSource")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLStmtCustomizer.class, "ACSD_Stmt_DataSource")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jTextField2, gridBagConstraints);

        errorMessage.setForeground(new java.awt.Color(255, 0, 0));
        errorMessage.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        errorMessage.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 12, 12, 12);
        add(errorMessage, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SQLStmtCustomizer.class, "ACSD_Stmt_Dialog", displayName));
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorMessage;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
    
}
