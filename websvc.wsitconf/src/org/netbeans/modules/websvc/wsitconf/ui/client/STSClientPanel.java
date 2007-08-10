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

package org.netbeans.modules.websvc.wsitconf.ui.client;

import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityCheckerRegistry;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class STSClientPanel extends SectionInnerPanel {

    private Node node;
    private Binding binding;
    private boolean inSync = false;
    private JaxWsModel jaxwsmodel;

    public STSClientPanel(SectionView view, Node node, Binding binding, JaxWsModel jaxWsModel) {
        super(view);
        this.node = node;
        this.binding = binding;
        this.jaxwsmodel = jaxWsModel;
        
        initComponents();

        endpointLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        endpointTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        metadataLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        metadataField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        namespaceLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        namespaceTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        portNameLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        portNameTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        serviceNameLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        serviceNameTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        wsdlLocationLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        wsdlLocationTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        addImmediateModifier(endpointTextField);
        addImmediateModifier(namespaceTextField);
        addImmediateModifier(portNameTextField);
        addImmediateModifier(serviceNameTextField);
        addImmediateModifier(wsdlLocationTextField);
        addImmediateModifier(metadataField);

        sync();
    }

    public void sync() {
        inSync = true;

        String endpoint = ProprietarySecurityPolicyModelHelper.getPreSTSEndpoint(binding);
        if (endpoint != null) {
            setEndpoint(endpoint);
        }

        String metadata = ProprietarySecurityPolicyModelHelper.getPreSTSMetadata(binding);
        if (metadata != null) {
            setMetadata(metadata);
        }
        
        String namespace = ProprietarySecurityPolicyModelHelper.getPreSTSNamespace(binding);
        if (namespace != null) {
            setNamespace(namespace);
        } 

        String portName = ProprietarySecurityPolicyModelHelper.getPreSTSPortName(binding);
        if (portName != null) {
            setPortName(portName);
        } 

        String serviceName = ProprietarySecurityPolicyModelHelper.getPreSTSServiceName(binding);
        if (serviceName != null) {
            setServiceName(serviceName);
        } 

        String wsdlLocation = ProprietarySecurityPolicyModelHelper.getPreSTSWsdlLocation(binding);
        if (wsdlLocation != null) {
            setWsdlLocation(wsdlLocation);
        } 
        
        inSync = false;
    }

    private String getEndpoint() {
        return this.endpointTextField.getText();
    }

    private void setEndpoint(String url) {
        this.endpointTextField.setText(url);
    }

    private String getMetadata() {
        return this.metadataField.getText();
    }

    private void setMetadata(String url) {
        this.metadataField.setText(url);
    }
    
    private String getNamespace() {
        return this.namespaceTextField.getText();
    }

    private void setNamespace(String ns) {
        this.namespaceTextField.setText(ns);
    }
    
    private String getServiceName() {
        return this.serviceNameTextField.getText();
    }

    private void setServiceName(String sname) {
        this.serviceNameTextField.setText(sname);
    }
    
    private String getPortName() {
        return this.portNameTextField.getText();
    }

    private void setPortName(String pname) {
        this.portNameTextField.setText(pname);
    }

    private String getWsdlLocation() {
        return this.wsdlLocationTextField.getText();
    }

    private void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocationTextField.setText(wsdlLocation);
    }
    
    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (!inSync) {
            if (source.equals(endpointTextField)) {
                String endpoint = getEndpoint();
                if ((endpoint != null) && (endpoint.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSEndpoint(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSEndpoint(binding, endpoint);
                }
                return;
            }

            if (source.equals(metadataField)) {
                String metad = getMetadata();
                if ((metad != null) && (metad.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSMetadata(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSMetadata(binding, metad);
                }
                return;
            }

            if (source.equals(namespaceTextField)) {
                String ns = getNamespace();
                if ((ns != null) && (ns.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSNamespace(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSNamespace(binding, ns);
                }
                return;
            }

            if (source.equals(serviceNameTextField)) {
                String sname = getServiceName();
                if ((sname != null) && (sname.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSServiceName(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSServiceName(binding, sname);
                }
                return;
            }

            if (source.equals(portNameTextField)) {
                String pname = getPortName();
                if ((pname != null) && (pname.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSPortName(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSPortName(binding, pname);
                }
                return;
            }

            if (source.equals(wsdlLocationTextField)) {
                String wsdlLoc = getWsdlLocation();
                if ((wsdlLoc != null) && (wsdlLoc.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSWsdlLocation(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSWsdlLocation(binding, wsdlLoc);
                }
                return;
            }
            enableDisable();
        }
    }
    
    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        enableDisable();
    }

    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }
    
    @Override
    protected void endUIChange() {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public javax.swing.JComponent getErrorComponent(String errorId) {
        return null;
    }

    private void enableDisable() {
        
        boolean amSec = SecurityCheckerRegistry.getDefault().isNonWsitSecurityEnabled(node, jaxwsmodel);

        endpointLabel.setEnabled(!amSec);
        endpointTextField.setEnabled(!amSec);
        metadataField.setEnabled(!amSec);
        metadataLabel.setEnabled(!amSec);
        namespaceLabel.setEnabled(!amSec);
        namespaceLabel.setEnabled(!amSec);
        portNameLabel.setEnabled(!amSec);
        portNameTextField.setEnabled(!amSec);
        serviceNameLabel.setEnabled(!amSec);
        serviceNameTextField.setEnabled(!amSec);
        wsdlLocationLabel.setEnabled(!amSec);
        wsdlLocationTextField.setEnabled(!amSec);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        endpointLabel = new javax.swing.JLabel();
        wsdlLocationLabel = new javax.swing.JLabel();
        endpointTextField = new javax.swing.JTextField();
        wsdlLocationTextField = new javax.swing.JTextField();
        serviceNameLabel = new javax.swing.JLabel();
        serviceNameTextField = new javax.swing.JTextField();
        portNameLabel = new javax.swing.JLabel();
        namespaceLabel = new javax.swing.JLabel();
        portNameTextField = new javax.swing.JTextField();
        namespaceTextField = new javax.swing.JTextField();
        metadataLabel = new javax.swing.JLabel();
        metadataField = new javax.swing.JTextField();

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        endpointLabel.setLabelFor(endpointTextField);
        org.openide.awt.Mnemonics.setLocalizedText(endpointLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Endpoint")); // NOI18N
        endpointLabel.setToolTipText("The maximum number of seconds the time stamp remains valid.");

        wsdlLocationLabel.setLabelFor(wsdlLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(wsdlLocationLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_WsdlLocation")); // NOI18N
        wsdlLocationLabel.setToolTipText("The maximum number of seconds the sending clock can deviate from the receiving clock.");

        endpointTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        wsdlLocationTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        serviceNameLabel.setLabelFor(serviceNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(serviceNameLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_ServiceName")); // NOI18N

        serviceNameTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        portNameLabel.setLabelFor(portNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(portNameLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_PortName")); // NOI18N

        namespaceLabel.setLabelFor(namespaceTextField);
        org.openide.awt.Mnemonics.setLocalizedText(namespaceLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Namespace")); // NOI18N

        portNameTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        namespaceTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        metadataLabel.setLabelFor(metadataField);
        org.openide.awt.Mnemonics.setLocalizedText(metadataLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Metadata")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(namespaceLabel)
                    .add(endpointLabel)
                    .add(wsdlLocationLabel)
                    .add(metadataLabel)
                    .add(serviceNameLabel)
                    .add(portNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(namespaceTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(portNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(serviceNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(wsdlLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(endpointTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(metadataField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 383, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {endpointTextField, metadataField, namespaceTextField, portNameTextField, serviceNameTextField, wsdlLocationTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(endpointLabel)
                    .add(endpointTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(wsdlLocationLabel)
                    .add(wsdlLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(metadataLabel)
                    .add(metadataField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serviceNameLabel)
                    .add(serviceNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(portNameLabel)
                    .add(portNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(namespaceLabel)
                    .add(namespaceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        endpointLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Endpoint_ACSN")); // NOI18N
        endpointLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Endpoint_ACSD")); // NOI18N
        wsdlLocationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_WsdlLocation_ACSN")); // NOI18N
        wsdlLocationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_WsdlLocation_ACSD")); // NOI18N
        serviceNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_ServiceName_ACSN")); // NOI18N
        serviceNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_ServiceName_ACSD")); // NOI18N
        portNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_PortName_ACSN")); // NOI18N
        portNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_PortName_ACSD")); // NOI18N
        namespaceLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Namespace_ACSN")); // NOI18N
        namespaceLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Namespace_ACSD")); // NOI18N
        metadataLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Metadata_ACSN")); // NOI18N
        metadataLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Metadata_ACSD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
    enableDisable();
}//GEN-LAST:event_formFocusGained

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    enableDisable();
}//GEN-LAST:event_formAncestorAdded
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel endpointLabel;
    private javax.swing.JTextField endpointTextField;
    private javax.swing.JTextField metadataField;
    private javax.swing.JLabel metadataLabel;
    private javax.swing.JLabel namespaceLabel;
    private javax.swing.JTextField namespaceTextField;
    private javax.swing.JLabel portNameLabel;
    private javax.swing.JTextField portNameTextField;
    private javax.swing.JLabel serviceNameLabel;
    private javax.swing.JTextField serviceNameTextField;
    private javax.swing.JLabel wsdlLocationLabel;
    private javax.swing.JTextField wsdlLocationTextField;
    // End of variables declaration//GEN-END:variables
    
}
