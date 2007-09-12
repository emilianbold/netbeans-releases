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
package org.netbeans.modules.bpel.properties.editors;

import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.bpel.properties.editors.controls.MessageConfigurationController;
import org.netbeans.modules.bpel.properties.editors.controls.MessageExchangeController;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;

/**
 *
 * @author  nk160297
 */
public class ReceiveMainPanel extends EditorLifeCycleAdapter {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor<Receive> myEditor;
    
    private MessageConfigurationController mcc;
    private MessageExchangeController mec;
    
    public ReceiveMainPanel(CustomNodeEditor<Receive> anEditor) {
        this.myEditor = anEditor;
        createContent();
    }
    
    public void createContent() {
        mcc = new MessageConfigurationController(myEditor);
        mcc.createContent();
        mcc.setVisibleVariables(true, false, false);
        mcc.useMyRole();
        //
        mec = new MessageExchangeController(myEditor);
        mec.createContent();
        //
        initComponents();
        bindControls2PropertyNames();
        
        // Issue 85553 start
        lblMessageExchange.setVisible(false);
        fldMessageExchange.setVisible(false);
        btnChooseMessEx.setVisible(false);
        // Issue 85553 end
        
        
        //
        myEditor.getValidStateManager(true).addValidStateListener(
                new ValidStateListener() {
            public void stateChanged(ValidStateManager source, boolean isValid) {
                if (source.isValid()) {
                    lblErrorMessage.setText("");
                } else {
                    lblErrorMessage.setText(source.getHtmlReasons());
                }
            }
        });
    }
    
    /**
     * Binds simple controls to names of properties.
     * This is necessary for automatic value inicialization and value inquiry.
     */
    private void bindControls2PropertyNames() {
        fldName.putClientProperty(
                CustomNodeEditor.PROPERTY_BINDER, PropertyType.NAME);
        chbxCreateInstance.putClientProperty(
                CustomNodeEditor.PROPERTY_BINDER, PropertyType.CREATE_INSTANCE);
    }
    
    public boolean initControls() {
        mcc.initControls();
        mec.initControls();
        return true;
    }
    
    public boolean subscribeListeners() {
        mcc.subscribeListeners();
        mec.subscribeListeners();
        return true;
    }
    
    public boolean unsubscribeListeners() {
        mcc.unsubscribeListeners();
        mec.unsubscribeListeners();
        return true;
    }
    
    public boolean applyNewValues() {
        mcc.applyNewValues();
        mec.applyNewValues();
        //
        return true;
    }
    
    public boolean afterClose() {
        mcc.afterClose();
        mec.afterClose();
        return true;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblName = new javax.swing.JLabel();
        fldName = new javax.swing.JTextField();
        lblPartnerLink = new javax.swing.JLabel();
        cbxPartnerLink = mcc.getCbxPartnerLink();
        lblOperation = new javax.swing.JLabel();
        cbxOperation = mcc.getCbxOperation();
        lblInputVariable = new javax.swing.JLabel();
        fldInputVariable = mcc.getFldInputVariable();
        btnNewInputVariable = mcc.getBtnNewInputVariable();
        btnChooseInputVariable = mcc.getBtnChooseInputVariable();
        fldMessageExchange = mec.getFldMessageExchange();
        btnChooseMessEx = mec.getBtnChooseMsgEx();
        chbxCreateInstance = new javax.swing.JCheckBox();
        lblErrorMessage = new javax.swing.JLabel();
        lblMessageExchange = new javax.swing.JLabel();

        lblName.setLabelFor(fldName);
        lblName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Name"));
        lblName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Name"));

        fldName.setColumns(40);
        fldName.setName("");

        lblPartnerLink.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_PartnerLink"));
        lblPartnerLink.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_PartnerLink"));
        lblPartnerLink.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_PartnerLink"));

        lblOperation.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Operation"));
        lblOperation.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Operation"));
        lblOperation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Operation"));

        lblInputVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_InputVariable"));
        lblInputVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_InputVariable"));
        lblInputVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_InputVariable"));

        fldInputVariable.setColumns(30);
        fldInputVariable.setEditable(false);

        btnNewInputVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "BTN_CreateInputVariable"));
        btnNewInputVariable.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnNewInputVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_CreateInputVariable"));
        btnNewInputVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_CreateInputVariable"));

        btnChooseInputVariable.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "BTN_BrowseInputVarible"));
        btnChooseInputVariable.setMargin(new java.awt.Insets(0, 2, 0, 2));
        btnChooseInputVariable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_BrowseInputVarible"));
        btnChooseInputVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_BrowseInputVarible"));

        btnChooseMessEx.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"BTN_ChooseMessageExchange"));
        btnChooseMessEx.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnChooseMessEx.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_BTN_ChooseMessageExchange"));
        btnChooseMessEx.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_BTN_ChooseMessageExchange"));

        chbxCreateInstance.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"CHBX_CreateInstance"));
        chbxCreateInstance.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chbxCreateInstance.setMargin(new java.awt.Insets(0, 0, 0, 0));
        chbxCreateInstance.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_CHBX_CreateInstance"));
        chbxCreateInstance.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_CHBX_CreateInstance"));

        lblErrorMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblErrorMessage.setAlignmentX(0.5F);

        lblMessageExchange.setLabelFor(fldMessageExchange);
        lblMessageExchange.setText(org.openide.util.NbBundle.getMessage(FormBundle.class,"LBL_MessageExchange"));
        lblMessageExchange.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_MessageExchange"));
        lblMessageExchange.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_MessageExchange"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblInputVariable)
                            .add(lblOperation)
                            .add(lblPartnerLink)
                            .add(lblName))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, cbxPartnerLink, 0, 351, Short.MAX_VALUE)
                            .add(cbxOperation, 0, 351, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(fldInputVariable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnNewInputVariable)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnChooseInputVariable))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, fldName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)))
                    .add(chbxCreateInstance)
                    .add(layout.createSequentialGroup()
                        .add(lblMessageExchange)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fldMessageExchange, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnChooseMessEx)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblName)
                    .add(fldName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPartnerLink)
                    .add(cbxPartnerLink, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblOperation)
                    .add(cbxOperation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblInputVariable)
                    .add(btnChooseInputVariable)
                    .add(btnNewInputVariable)
                    .add(fldInputVariable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnChooseMessEx)
                    .add(lblMessageExchange)
                    .add(fldMessageExchange, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbxCreateInstance)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChooseInputVariable;
    private javax.swing.JButton btnChooseMessEx;
    private javax.swing.JButton btnNewInputVariable;
    private javax.swing.JComboBox cbxOperation;
    private javax.swing.JComboBox cbxPartnerLink;
    private javax.swing.JCheckBox chbxCreateInstance;
    private javax.swing.JTextField fldInputVariable;
    private javax.swing.JTextField fldMessageExchange;
    private javax.swing.JTextField fldName;
    private javax.swing.JLabel lblErrorMessage;
    private javax.swing.JLabel lblInputVariable;
    private javax.swing.JLabel lblMessageExchange;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblOperation;
    private javax.swing.JLabel lblPartnerLink;
    // End of variables declaration//GEN-END:variables
}
