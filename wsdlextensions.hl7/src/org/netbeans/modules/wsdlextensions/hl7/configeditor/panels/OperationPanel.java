/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.hl7.configeditor.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.hl7.HL7Constants;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.ElementOrType;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.HL7Error;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.soa.wsdl.bindingsupport.ui.util.BindingComponentUtils;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */
public class OperationPanel extends javax.swing.JPanel {

    private DescriptionPanel descriptionPanel;
    private Project mProject;
    private WSDLModel mWSDLModel;
    private ElementOrType inputEOT = null;
    private ElementOrType outputEOT = null;

    /** Creates new form OperationPanel */
    public OperationPanel() {
        initComponents();
		//set Mnemonics
		setMnemonics();
		//set accessibility
		setAccessibility();
        addListeners();
    }

    public void setProject(Project mProject) {
        this.mProject = mProject;
    }

    public void setWSDLModel(WSDLModel wsdlModel) {
        this.mWSDLModel = wsdlModel;
    }

    public void setDescriptionPanel(DescriptionPanel descriptionPanel) {
        this.descriptionPanel = descriptionPanel;
    }

    private void addListeners() {
        FocusListener focusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateDescriptionArea(e);
            }
        };
        txtOpName.addFocusListener(focusListener);
        txtReqEOT.addFocusListener(focusListener);
        txtReqMsgType.addFocusListener(focusListener);
        txtResEOT.addFocusListener(focusListener);

        ActionListener actionListener = new BrowseButtonListener();
        btnReqBrowse.addActionListener(actionListener);
        btnResBrowse.addActionListener(actionListener);

//        ActionListener actionListener2 = new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                validateMe(true);
//            }
//        };
//        txtReqEOT.addActionListener(actionListener2);
//        txtResEOT.addActionListener(actionListener2);

        DocumentListener documentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validateMe(true);
            }
            public void removeUpdate(DocumentEvent e) {
                validateMe(true);
            }
            public void changedUpdate(DocumentEvent e) {
                validateMe(true);
            }
        };
        txtOpName.getDocument().addDocumentListener(documentListener);
//        txtReqEOT.getDocument().addDocumentListener(documentListener);
//        txtResEOT.getDocument().addDocumentListener(documentListener);
        txtReqMsgType.getDocument().addDocumentListener(documentListener);
    }

    public void setMessageExchangePattern(String templateConst, boolean twoWay) {
        changeResponseMessageVisibility(twoWay);

        if (templateConst.equals(HL7Constants.TEMPLATE_OUT)){
            changeRequestMessageTypeVisibility(false);
        }
    }

    public String getOperationName() {
        return txtOpName.getText();
    }

    public void setOperationName(String operationName) {
        txtOpName.setText(operationName);
    }

    public String getMessageType() {
        return txtReqMsgType.isVisible() ? txtReqMsgType.getText() : null;
    }

//    public void setMessageType(String msgType) {
//        txtReqMsgType.setText(msgType);
//    }

    private void setRequestEOT(String eot) {
        txtReqEOT.setText(eot);
    }

    public ElementOrType getRequestEOT() {
        return inputEOT;
    }

    public ElementOrType getResponseEOT() {
        return outputEOT;
    }

    private void setResponseEOT(String eot) {
        txtResEOT.setText(eot);
    }

    public HL7Error validateMe(boolean fireEvent) {
        HL7Error hl7Error = new HL7Error();

        if (getOperationName() == null || getOperationName().length() == 0) {
            hl7Error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            hl7Error.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                    "OperationPanel.OperationNameEmpty")); // NOI18N
        } else if (!isNCName(getOperationName())) {
            hl7Error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            hl7Error.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                    "OperationPanel.OperationNameNotNCName", getOperationName())); // NOI18N
        } else if (getRequestEOT() == null) {
            hl7Error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            hl7Error.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                    "OperationPanel.RequestEOTEmpty", getOperationName())); // NOI18N
        } else if ( txtReqMsgType.isVisible() &&
                (getMessageType() == null || getMessageType().trim().length() == 0)) {
            hl7Error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            hl7Error.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                    "OperationPanel.MessageTypeEmpty", getOperationName())); // NOI18N
        } else if (txtResEOT.isVisible() && getResponseEOT() == null) {
            hl7Error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            hl7Error.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                    "OperationPanel.ResponseEOTEmpty", getOperationName())); // NOI18N
        }

        if (fireEvent && hl7Error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, hl7Error.getErrorMessage());
        } else {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }
        
        return hl7Error;
    }

    /**
     * Checks whether the given name is a valid NCName.
     * (http://www.w3.org/TR/REC-xml-names/#NT-NCName)
     */
    private static boolean isNCName(String name) {
        if (name == null) {
            return false;
        } else {
            String regex = "[_A-Za-z][-._A-Za-z0-9]*"; // NOI18N
            return name.matches(regex);
        }
    }

    private class BrowseButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            final JButton browseButton = (JButton) e.getSource();

            if ("input".equals(browseButton.getActionCommand())) {
                boolean ok = BindingComponentUtils.browseForElementOrType(mProject,
                        mWSDLModel, null);
                if (ok) {
                    setRequestEOT(BindingComponentUtils.getPrefixNameSpace());
                    inputEOT = new ElementOrType(BindingComponentUtils.getSchemaComponent(),
                            BindingComponentUtils.getElementOrType());
                    validateMe(true);
                }

            } else if ("output".equals(browseButton.getActionCommand())) {
                boolean ok = BindingComponentUtils.browseForElementOrType(mProject,
                        mWSDLModel, null);
                if (ok) {
                    setResponseEOT(BindingComponentUtils.getPrefixNameSpace());
                    outputEOT = new ElementOrType(BindingComponentUtils.getSchemaComponent(),
                            BindingComponentUtils.getElementOrType());
                    validateMe(true);
                }
            }
        }
    }

    private void updateDescriptionArea(FocusEvent evt){
        if (descriptionPanel == null) {
            return;
        }
        
        descriptionPanel.setText("");

        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == txtOpName) {
            desc = new String[]{"Operation Name\n\n", txtOpName.getToolTipText()};
        } else if (evt.getSource() == txtReqEOT) {
            desc = new String[]{"Request Message\n\n", txtReqEOT.getToolTipText()};
            casaEdited = true;
        } else if(evt.getSource() == txtResEOT){
            desc = new String[]{"Response Message\n\n", txtResEOT.getToolTipText()};
        } else if(evt.getSource() == txtReqMsgType){
            desc = new String[]{"Message Type\n\n", txtReqMsgType.getToolTipText()};
        }

        if (desc != null) {
            // Insert the image
            if (casaEdited) {
            }
            this.descriptionPanel.setText(desc[0], desc[1]);
            return;
        }
    }

    private void changeResponseMessageVisibility(boolean visible) {
        lblResMsg.setVisible(visible);
        txtResEOT.setVisible(visible);
        btnResBrowse.setVisible(visible);
        revalidate();
    }

    private void changeRequestMessageTypeVisibility(boolean visible) {
        lblReqMsgType.setVisible(visible);
        txtReqMsgType.setVisible(visible);
        revalidate();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblOpName = new javax.swing.JLabel();
        txtOpName = new javax.swing.JTextField();
        lblReqMsg = new javax.swing.JLabel();
        lblReqMsgType = new javax.swing.JLabel();
        lblResMsg = new javax.swing.JLabel();
        txtReqEOT = new javax.swing.JTextField();
        txtReqMsgType = new javax.swing.JTextField();
        txtResEOT = new javax.swing.JTextField();
        btnReqBrowse = new javax.swing.JButton();
        btnResBrowse = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setLayout(new java.awt.GridBagLayout());

        lblOpName.setLabelFor(txtOpName);
        lblOpName.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.lblOpName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(lblOpName, gridBagConstraints);

        txtOpName.setToolTipText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtOpName.toolTipText")); // NOI18N
        txtOpName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtOpNameActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(txtOpName, gridBagConstraints);

        lblReqMsg.setLabelFor(txtReqEOT);
        lblReqMsg.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.lblReqMsg.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(lblReqMsg, gridBagConstraints);

        lblReqMsgType.setLabelFor(txtReqMsgType);
        lblReqMsgType.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.lblReqMsgType.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(lblReqMsgType, gridBagConstraints);

        lblResMsg.setLabelFor(txtResEOT);
        lblResMsg.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.lblResMsg.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(lblResMsg, gridBagConstraints);

        txtReqEOT.setEditable(false);
        txtReqEOT.setToolTipText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtReqMsg.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(txtReqEOT, gridBagConstraints);

        txtReqMsgType.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtReqMsgType.text")); // NOI18N
        txtReqMsgType.setToolTipText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtReqMsgType.toolTipText")); // NOI18N
        txtReqMsgType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtReqMsgTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(txtReqMsgType, gridBagConstraints);

        txtResEOT.setEditable(false);
        txtResEOT.setToolTipText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtResMsg.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(txtResEOT, gridBagConstraints);

        btnReqBrowse.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnReqBrowse.text")); // NOI18N
        btnReqBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnReqBrowse.toolTipText")); // NOI18N
        btnReqBrowse.setActionCommand(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnReqBrowse.actionCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(btnReqBrowse, gridBagConstraints);

        btnResBrowse.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnResBrowse.text")); // NOI18N
        btnResBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnResBrowse.toolTipText")); // NOI18N
        btnResBrowse.setActionCommand(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnResBrowse.actionCommand")); // NOI18N
        btnResBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(btnResBrowse, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void txtOpNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtOpNameActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_txtOpNameActionPerformed

    private void txtReqMsgTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtReqMsgTypeActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_txtReqMsgTypeActionPerformed

    private void btnResBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResBrowseActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_btnResBrowseActionPerformed

	private void setMnemonics(){
		org.openide.awt.Mnemonics.setLocalizedText(lblOpName, org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.lblOpName.text")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblReqMsg, org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.lblReqMsg.text")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblReqMsgType, org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.lblReqMsgType.text")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(lblResMsg, org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.lblResMsg.text")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(btnReqBrowse, org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnReqBrowse.text")); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(btnResBrowse, org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnResBrowse.text")); // NOI18N
	}

	private void setAccessibility() {
		btnReqBrowse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnReqBrowse.toolTipText")); // NOI18N
		btnReqBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnReqBrowse.toolTipText")); // NOI18N
		btnResBrowse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnResBrowse.toolTipText")); // NOI18N
		btnResBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnResBrowse.toolTipText")); // NOI18N
        txtOpName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtOpName.toolTipText")); // NOI18N
        txtOpName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtOpName.toolTipText")); // NOI18N
        txtReqEOT.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtReqMsg.toolTipText")); // NOI18N
        txtReqEOT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtReqMsg.toolTipText")); // NOI18N
        txtReqMsgType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtReqMsgType.toolTipText")); // NOI18N
        txtReqMsgType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtReqMsgType.toolTipText")); // NOI18N
        txtResEOT.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtResMsg.toolTipText")); // NOI18N
        txtResEOT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtResMsg.toolTipText")); // NOI18N
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton btnReqBrowse;
    protected javax.swing.JButton btnResBrowse;
    private javax.swing.JLabel lblOpName;
    private javax.swing.JLabel lblReqMsg;
    private javax.swing.JLabel lblReqMsgType;
    private javax.swing.JLabel lblResMsg;
    protected javax.swing.JTextField txtOpName;
    protected javax.swing.JTextField txtReqEOT;
    protected javax.swing.JTextField txtReqMsgType;
    protected javax.swing.JTextField txtResEOT;
    // End of variables declaration//GEN-END:variables

}
