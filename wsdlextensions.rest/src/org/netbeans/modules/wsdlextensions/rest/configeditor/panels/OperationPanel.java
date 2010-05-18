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
package org.netbeans.modules.wsdlextensions.rest.configeditor.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.rest.RESTConstants;
import org.netbeans.modules.wsdlextensions.rest.configeditor.ElementOrType;
import org.netbeans.modules.wsdlextensions.rest.configeditor.InboundProperties;
import org.netbeans.modules.wsdlextensions.rest.configeditor.OutboundProperties;
import org.netbeans.modules.wsdlextensions.rest.configeditor.RESTError;
import org.netbeans.modules.wsdlextensions.rest.configeditor.ValidatableProperties;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
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
    private ElementOrType requestEOT;
    private ElementOrType responseEOT;
    private ValidatableProperties properties;
    private String templateConst;

    /** Creates new form OperationPanel */
    public OperationPanel(String templateConst, boolean twoWay,
            DescriptionPanel descriptionPanel) {
        initComponents();

        this.templateConst = templateConst;
        this.descriptionPanel = descriptionPanel;
        addListeners();
        changeResponseMessageVisibility(twoWay);

        requestEOT = new ElementOrType(null, getPrimitiveType("anyType"));
        responseEOT = requestEOT;
    }

    public void setProject(Project mProject) {
        this.mProject = mProject;
    }

    public void setWSDLModel(WSDLModel wsdlModel) {
        this.mWSDLModel = wsdlModel;
    }

    public ValidatableProperties getValidatableProperties() {
        if (properties == null) {
            if (templateConst.equals(RESTConstants.TEMPLATE_IN)) {
                return new InboundProperties();
            } else {
                return new OutboundProperties();
            }
        } else {
            return properties;
        }
    }

    public void setValidatableProperties(ValidatableProperties properties) {
        this.properties = properties;
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
    }

    public String getOperationName() {
        return txtOpName.getText();
    }

    public void setOperationName(String operationName) {
        txtOpName.setText(operationName);
    }

    private void setRequestEOT(String eot) {
        txtReqEOT.setText(eot);
    }

    public ElementOrType getRequestEOT() {
        return requestEOT;
    }

    public ElementOrType getResponseEOT() {
        return responseEOT;
    }

    private void setResponseEOT(String eot) {
        txtResEOT.setText(eot);
    }

    public RESTError validateMe(boolean fireEvent) {
        RESTError restError = new RESTError();

        if (getOperationName() == null || getOperationName().length() == 0) {
            restError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            restError.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                    "OperationPanel.OperationNameEmpty")); // NOI18N
        } else if (!isNCName(getOperationName())) {
            restError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            restError.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                    "OperationPanel.OperationNameNotNCName", getOperationName())); // NOI18N
        } else if (getRequestEOT() == null) {
            restError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            restError.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                    "OperationPanel.RequestEOTEmpty", getOperationName())); // NOI18N
        } else if (txtResEOT.isVisible() && getResponseEOT() == null) {
            restError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            restError.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                    "OperationPanel.ResponseEOTEmpty", getOperationName())); // NOI18N
        } else if (!getValidatableProperties().isValid()) {
            restError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            restError.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                    "OperationPanel.InvalidProperties", getOperationName())); // NOI18N
        }

        if (fireEvent && restError.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, restError.getErrorMessage());
        } else {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }
        
        return restError;
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

            if ("input".equals(browseButton.getActionCommand())) { // NOI18N
                boolean ok = BindingComponentUtils.browseForElementOrType(mProject,
                        mWSDLModel, null);
                if (ok) {
                    setRequestEOT(BindingComponentUtils.getPrefixNameSpace());
                    requestEOT = new ElementOrType(BindingComponentUtils.getSchemaComponent(),
                            BindingComponentUtils.getElementOrType());
                    validateMe(true);
                }

            } else if ("output".equals(browseButton.getActionCommand())) { // NOI18N
                boolean ok = BindingComponentUtils.browseForElementOrType(mProject,
                        mWSDLModel, null);
                if (ok) {
                    setResponseEOT(BindingComponentUtils.getPrefixNameSpace());
                    responseEOT = new ElementOrType(BindingComponentUtils.getSchemaComponent(),
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
        
        descriptionPanel.setText(""); // NOI18N

        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == txtOpName) {
            desc = new String[]{"Operation Name\n\n", txtOpName.getToolTipText()};
        } else if (evt.getSource() == txtReqEOT) {
            desc = new String[]{"Request Message\n\n", txtReqEOT.getToolTipText()};
            casaEdited = true;
        } else if(evt.getSource() == txtResEOT){
            desc = new String[]{"Response Message\n\n", txtResEOT.getToolTipText()};
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

    private static GlobalSimpleType getPrimitiveType(String typeName) {
        SchemaModel primitiveModel =
                SchemaModelFactory.getDefault().getPrimitiveTypesModel();
        Collection<GlobalSimpleType> primitives =
                primitiveModel.getSchema().getSimpleTypes();
        for (GlobalSimpleType ptype : primitives) {
            if (ptype.getName().equals(typeName)) {
                return ptype;
            }
        }
        return null;
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
        lblResMsg = new javax.swing.JLabel();
        txtReqEOT = new javax.swing.JTextField();
        txtResEOT = new javax.swing.JTextField();
        btnReqBrowse = new javax.swing.JButton();
        btnResBrowse = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setLayout(new java.awt.GridBagLayout());

        lblOpName.setDisplayedMnemonic('N');
        lblOpName.setLabelFor(txtOpName);
        lblOpName.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.lblOpName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(lblOpName, gridBagConstraints);

        txtOpName.setToolTipText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtOpName.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(txtOpName, gridBagConstraints);

        lblReqMsg.setDisplayedMnemonic('q');
        lblReqMsg.setLabelFor(txtReqEOT);
        lblReqMsg.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.lblReqMsg.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(lblReqMsg, gridBagConstraints);

        lblResMsg.setDisplayedMnemonic('p');
        lblResMsg.setLabelFor(txtResEOT);
        lblResMsg.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.lblResMsg.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(lblResMsg, gridBagConstraints);

        txtReqEOT.setEditable(false);
        txtReqEOT.setText("xsd:anyType"); // NOI18N
        txtReqEOT.setToolTipText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtReqMsg.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(txtReqEOT, gridBagConstraints);

        txtResEOT.setEditable(false);
        txtResEOT.setText("xsd:anyType"); // NOI18N
        txtResEOT.setToolTipText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.txtResMsg.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(txtResEOT, gridBagConstraints);

        btnReqBrowse.setMnemonic('o');
        btnReqBrowse.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnReqBrowse.text")); // NOI18N
        btnReqBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnReqBrowse.toolTipText")); // NOI18N
        btnReqBrowse.setActionCommand(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnReqBrowse.actionCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(btnReqBrowse, gridBagConstraints);

        btnResBrowse.setMnemonic('w');
        btnResBrowse.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnResBrowse.text")); // NOI18N
        btnResBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnResBrowse.toolTipText")); // NOI18N
        btnResBrowse.setActionCommand(org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.btnResBrowse.actionCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(btnResBrowse, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton btnReqBrowse;
    protected javax.swing.JButton btnResBrowse;
    private javax.swing.JLabel lblOpName;
    private javax.swing.JLabel lblReqMsg;
    private javax.swing.JLabel lblResMsg;
    protected javax.swing.JTextField txtOpName;
    protected javax.swing.JTextField txtReqEOT;
    protected javax.swing.JTextField txtResEOT;
    // End of variables declaration//GEN-END:variables

}
