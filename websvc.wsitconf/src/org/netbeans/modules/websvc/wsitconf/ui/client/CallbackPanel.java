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

import java.awt.Color;
import java.util.Set;
import javax.swing.JPanel;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityCheckerRegistry;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.client.subpanels.DynamicCredsPanel;
import org.netbeans.modules.websvc.wsitconf.ui.client.subpanels.StaticCredsPanel;
import org.netbeans.modules.xml.multiview.ui.NodeSectionPanel;

/**
 *
 * @author Martin Grebac
 */
public class CallbackPanel extends SectionInnerPanel {

    private WSDLModel model;
    private Node node;
    private Binding binding;

    private boolean inSync = false;
    
    private Project project;

    private SectionView view;
    private JaxWsModel jaxwsmodel;
    
    public CallbackPanel(SectionView view, WSDLModel model, Node node, Binding binding, JaxWsModel jaxWsModel) {
        super(view);
        this.view = view;
        this.model = model;
        this.node = node;
        this.binding = binding;
        this.jaxwsmodel = jaxWsModel;

        FileObject fo = (FileObject) node.getLookup().lookup(FileObject.class);
        if (fo != null) project = FileOwnerQuery.getOwner(fo);
        
        initComponents();

        samlHandlerField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        samlHandlerLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        credTypeCombo.removeAllItems();
        credTypeCombo.addItem(ComboConstants.STATIC);
        credTypeCombo.addItem(ComboConstants.DYNAMIC);

        addImmediateModifier(samlHandlerField);
        addImmediateModifier(credTypeCombo);

        sync();
    }

    public void sync() {
        inSync = true;

        String samlCallback = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.SAML_CBHANDLER);
        if (samlCallback != null) {
            setCallbackHandler(samlCallback);
        }
        
        String usernameCBH = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER);
        if ((usernameCBH != null) && (usernameCBH.length() > 0)) {
            setCredType(ComboConstants.DYNAMIC);
            credTypeCombo.setSelectedItem(ComboConstants.DYNAMIC);
        } else {
            setCredType(ComboConstants.STATIC);
            credTypeCombo.setSelectedItem(ComboConstants.STATIC);
        }
        
        enableDisable();
        
        inSync = false;
    }
    
    private JPanel getPanel(String type) {
        boolean amSec = SecurityCheckerRegistry.getDefault().isNonWsitSecurityEnabled(node, jaxwsmodel);

        if (ComboConstants.DYNAMIC.equals(type)) {
            return new DynamicCredsPanel(binding, project, !amSec);            
        }
        return new StaticCredsPanel(binding, project, !amSec);
    }
    
    private void setCredType(String credType) {
        this.remove(credPanel);
        credPanel = getPanel(credType);
        NodeSectionPanel panel = view.getActivePanel();
        boolean active = (panel == null) ? false : panel.isActive();
        Color c = active ? SectionVisualTheme.getSectionActiveBackgroundColor() : SectionVisualTheme.getDocumentBackgroundColor();
        credPanel.setBackground(c);
        refreshLayout();
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) return;
        
        if (source.equals(credTypeCombo)) {
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, null, null, true);
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER, null, null, true);
            setCredType((String)credTypeCombo.getSelectedItem());
        }
        
        if (source.equals(samlHandlerField)) {
            String classname = getCallbackHandler();
            if ((classname != null) && (classname.length() == 0)) {
                classname = null;
            }
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.SAML_CBHANDLER, classname, null, true);
            return;
        }
        
        enableDisable();
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

        credTypeLabel.setEnabled(!amSec);
        credTypeCombo.setEnabled(!amSec);
        samlBrowseButton.setEnabled(!amSec);
        samlHandlerField.setEnabled(!amSec);
        samlHandlerLabel.setEnabled(!amSec);
    }

    private void setCallbackHandler(String classname) {
        this.samlHandlerField.setText(classname);
    }    

    private String getCallbackHandler() {
        return samlHandlerField.getText();
    }
    
    private void refreshLayout() {
        org.jdesktop.layout.GroupLayout layout = (GroupLayout) this.getLayout();
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(credTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(credTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, credPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(samlHandlerLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(samlHandlerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(samlBrowseButton)))
                        .add(35, 35, 35)))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(credTypeLabel)
                    .add(credTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(credPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(samlHandlerLabel)
                    .add(samlHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(samlBrowseButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        samlHandlerLabel = new javax.swing.JLabel();
        samlHandlerField = new javax.swing.JTextField();
        samlBrowseButton = new javax.swing.JButton();
        credTypeCombo = new javax.swing.JComboBox();
        credTypeLabel = new javax.swing.JLabel();
        credPanel = new javax.swing.JPanel();

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

        samlHandlerLabel.setLabelFor(samlHandlerField);
        org.openide.awt.Mnemonics.setLocalizedText(samlHandlerLabel, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_SamlLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(samlBrowseButton, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_AuthPanel_SCHBrowseButton")); // NOI18N
        samlBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                samlBrowseButtonActionPerformed(evt);
            }
        });

        credTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Static", "Dynamic" }));

        org.openide.awt.Mnemonics.setLocalizedText(credTypeLabel, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_AuthTypeLabel")); // NOI18N

        org.jdesktop.layout.GroupLayout credPanelLayout = new org.jdesktop.layout.GroupLayout(credPanel);
        credPanel.setLayout(credPanelLayout);
        credPanelLayout.setHorizontalGroup(
            credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 516, Short.MAX_VALUE)
        );
        credPanelLayout.setVerticalGroup(
            credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 64, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(credTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(credTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, credPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(samlHandlerLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(samlHandlerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(samlBrowseButton)))
                        .add(35, 35, 35)))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(credTypeLabel)
                    .add(credTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(credPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(samlHandlerLabel)
                    .add(samlHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(samlBrowseButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
    enableDisable();
}//GEN-LAST:event_formFocusGained

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    enableDisable();
}//GEN-LAST:event_formAncestorAdded

    private void samlBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_samlBrowseButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "javax.security.auth.callback.CallbackHandler"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setCallbackHandler(selectedClass);
                    ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.SAML_CBHANDLER, selectedClass, null, true);          
                    break;
                }
            }
        }
    }//GEN-LAST:event_samlBrowseButtonActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel credPanel;
    private javax.swing.JComboBox credTypeCombo;
    private javax.swing.JLabel credTypeLabel;
    private javax.swing.JButton samlBrowseButton;
    private javax.swing.JTextField samlHandlerField;
    private javax.swing.JLabel samlHandlerLabel;
    // End of variables declaration//GEN-END:variables
    
}
