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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui.service.subpanels;

import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMMSModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMSunModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.TxModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;

import javax.swing.*;

/**
 *
 * @author Martin Grebac
 */
public class AdvancedRMPanel extends JPanel {

    private Node node;
    private Binding binding;
    private boolean inSync = false;
    
    public AdvancedRMPanel(Binding binding) {
        this.node = node;
        this.binding = binding;
        initComponents();
             
        sync();
    }

    private void sync() {
        inSync = true;
        
        String inactivityTimeout = RMModelHelper.getInactivityTimeout(binding);
        if (inactivityTimeout == null) { // no setup exists yet - set the default
            setInactivityTimeout(RMModelHelper.DEFAULT_TIMEOUT);
            RMModelHelper.setInactivityTimeout(binding, RMModelHelper.DEFAULT_TIMEOUT);
        } else {
            setInactivityTimeout(inactivityTimeout);
        } 

        String maxRcvBufferSize = RMMSModelHelper.getMaxReceiveBufferSize(binding);
        if (maxRcvBufferSize == null) { // no setup exists yet - set the default
            setMaxRcvBufferSize(RMModelHelper.DEFAULT_MAXRCVBUFFERSIZE);
            RMMSModelHelper.setMaxReceiveBufferSize(binding, RMModelHelper.DEFAULT_MAXRCVBUFFERSIZE);
        } else {
            setMaxRcvBufferSize(maxRcvBufferSize);
        } 

        setFlowControl(RMMSModelHelper.isFlowControlEnabled(binding));

        enableDisable();
        inSync = false;
    }

    // max receive buffer size
    private String getMaxRcvBufferSize() {
        return this.maxBufTextField.getText();
    }
    
    private void setMaxRcvBufferSize(String value) {
        this.maxBufTextField.setText(value);
    }

    // inactivity timeout
    private String getInactivityTimeout() {
        return this.inactivityTimeoutTextfield.getText();
    }
    
    private void setInactivityTimeout(String value) {
        this.inactivityTimeoutTextfield.setText(value);
    }

    // flow control
    private void setFlowControl(Boolean enable) {
        if (enable == null) {
            this.flowControlChBox.setSelected(false);
        } else {
            this.flowControlChBox.setSelected(enable);
        }
    }

    public Boolean getFlowControl() {
        if (flowControlChBox.isSelected()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
        
    public void setValue(javax.swing.JComponent source, Object value) {
        if (!inSync) {
            if (source.equals(flowControlChBox)) {
                if (flowControlChBox.isSelected()) {
                    if (!(RMMSModelHelper.isFlowControlEnabled(binding))) {
                        RMMSModelHelper.enableFlowControl(binding);
                    }
                } else {
                    if (RMMSModelHelper.isFlowControlEnabled(binding)) {
                        RMMSModelHelper.disableFlowControl(binding);
                    }
                }
                return;
            }
        
            if (source.equals(inactivityTimeoutTextfield)) {
                String timeout = getInactivityTimeout();
                if ((timeout != null) && (timeout.length() == 0)) {
                    RMModelHelper.setInactivityTimeout(binding, null);
                } else {
                    RMModelHelper.setInactivityTimeout(binding, timeout);
                }
                return;
            }

            if (source.equals(maxBufTextField)) {
                String bufSize = getMaxRcvBufferSize();
                if ((bufSize != null) && (bufSize.length() == 0)) {
                    RMMSModelHelper.setMaxReceiveBufferSize(binding, null);
                } else {
                    RMMSModelHelper.setMaxReceiveBufferSize(binding, bufSize);
                }
                return;
            }
            
        }
    }

    private void enableDisable() {
        boolean flowSelected = flowControlChBox.isSelected();
        maxBufLabel.setEnabled(flowSelected);
        maxBufTextField.setEnabled(flowSelected);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        flowControlChBox = new javax.swing.JCheckBox();
        maxBufLabel = new javax.swing.JLabel();
        maxBufTextField = new javax.swing.JTextField();
        inactivityTimeoutLabel = new javax.swing.JLabel();
        inactivityTimeoutTextfield = new javax.swing.JTextField();

        flowControlChBox.setText(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_FlowControlChBox")); // NOI18N
        flowControlChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        flowControlChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        flowControlChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flowControlChBoxActionPerformed(evt);
            }
        });

        maxBufLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_maxBufLabel")); // NOI18N

        maxBufTextField.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                maxBufTextFieldFocusLost(evt);
            }
        });
        maxBufTextField.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                maxBufTextFieldCaretUpdate(evt);
            }
        });

        inactivityTimeoutLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_InactivityTimeoutLabel")); // NOI18N

        inactivityTimeoutTextfield.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                inactivityTimeoutTextfieldFocusLost(evt);
            }
        });
        inactivityTimeoutTextfield.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                inactivityTimeoutTextfieldCaretUpdate(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(flowControlChBox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(maxBufLabel)
                            .add(inactivityTimeoutLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(inactivityTimeoutTextfield)
                            .add(maxBufTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(flowControlChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(maxBufLabel)
                    .add(maxBufTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(inactivityTimeoutLabel)
                    .add(inactivityTimeoutTextfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void maxBufTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_maxBufTextFieldFocusLost
        setValue(maxBufTextField, null);
    }//GEN-LAST:event_maxBufTextFieldFocusLost

    private void maxBufTextFieldCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_maxBufTextFieldCaretUpdate
        setValue(maxBufTextField, null);
    }//GEN-LAST:event_maxBufTextFieldCaretUpdate

    private void inactivityTimeoutTextfieldCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_inactivityTimeoutTextfieldCaretUpdate
        setValue(inactivityTimeoutTextfield, null);
    }//GEN-LAST:event_inactivityTimeoutTextfieldCaretUpdate

    private void inactivityTimeoutTextfieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inactivityTimeoutTextfieldFocusLost
        setValue(inactivityTimeoutTextfield, null);
    }//GEN-LAST:event_inactivityTimeoutTextfieldFocusLost

    private void flowControlChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flowControlChBoxActionPerformed
        enableDisable();
        setValue(flowControlChBox, null);
    }//GEN-LAST:event_flowControlChBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox flowControlChBox;
    private javax.swing.JLabel inactivityTimeoutLabel;
    private javax.swing.JTextField inactivityTimeoutTextfield;
    private javax.swing.JLabel maxBufLabel;
    private javax.swing.JTextField maxBufTextField;
    // End of variables declaration//GEN-END:variables
    
}
