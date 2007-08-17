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
import java.awt.Component;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
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
import org.netbeans.modules.websvc.wsitconf.ui.service.ServicePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.TruststorePanel;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.X509Token;
import org.netbeans.modules.xml.multiview.ui.NodeSectionPanel;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class CallbackPanel extends SectionInnerPanel {

    private Node node;
    private Binding binding;

    private boolean inSync = false;

    private Project project;

    private SectionView view;
    private JaxWsModel jaxwsmodel;
    private WSDLModel serviceModel;

    private String profile;

    public CallbackPanel(SectionView view, Node node, Binding binding, JaxWsModel jaxWsModel, WSDLModel serviceModel) {
        super(view);
        this.view = view;
        this.node = node;
        this.binding = binding;
        this.jaxwsmodel = jaxWsModel;
        this.serviceModel = serviceModel;

        FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo != null) {
            project = FileOwnerQuery.getOwner(fo);
        }
        initComponents();

        samlHandlerField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        samlHandlerLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        devDefaultsChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        credTypeCombo.removeAllItems();
        credTypeCombo.addItem(ComboConstants.STATIC);
        credTypeCombo.addItem(ComboConstants.DYNAMIC);

        addImmediateModifier(samlHandlerField);
        addImmediateModifier(credTypeCombo);
        addImmediateModifier(devDefaultsChBox);

        sync();
    }

    public void sync() {
        inSync = true;

        Binding serviceBinding = PolicyModelHelper.getBinding(serviceModel, binding.getName());
        profile = ProfilesModelHelper.getWSITSecurityProfile(serviceBinding);

        boolean defaults = ProfilesModelHelper.isClientDefaultSetupUsed(profile, binding, serviceBinding, project);
        setChBox(devDefaultsChBox, defaults);

        String samlCallback = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.SAML_CBHANDLER);
        if (samlCallback != null) {
            setCallbackHandler(samlCallback);
        }

        String usernameCBH = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER);
        if ((usernameCBH != null) && (usernameCBH.length() > 0)) {
            setCredType(ComboConstants.DYNAMIC, defaults);
            credTypeCombo.setSelectedItem(ComboConstants.DYNAMIC);
        } else {
            setCredType(ComboConstants.STATIC, defaults);
            credTypeCombo.setSelectedItem(ComboConstants.STATIC);
        }

        enableDisable();

        inSync = false;
    }

    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }

    private JPanel getPanel(String type, boolean defaults) {
        boolean amSec = SecurityCheckerRegistry.getDefault().isNonWsitSecurityEnabled(node, jaxwsmodel);

        if (ComboConstants.DYNAMIC.equals(type)) {
            return new DynamicCredsPanel(binding, project, !amSec && !defaults);
        }
        return new StaticCredsPanel(binding, !amSec && !defaults);
    }

    private void setCredType(String credType, boolean defaults) {
        this.remove(credPanel);
        credPanel = getPanel(credType, defaults);

        boolean active = true;
        if (view != null) {
            NodeSectionPanel panel = view.getActivePanel();
            active = (panel == null) ? false : panel.isActive();
        }

        Color c = active ? SectionVisualTheme.getSectionActiveBackgroundColor() : SectionVisualTheme.getDocumentBackgroundColor();
        credPanel.setBackground(c);
        refreshLayout();
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) {
            return;
        }
        if (source.equals(credTypeCombo)) {
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, null, null, true);
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER, null, null, true);
            setCredType((String) credTypeCombo.getSelectedItem(), devDefaultsChBox.isSelected());
        }

        if (source.equals(samlHandlerField)) {
            String classname = getCallbackHandler();
            if ((classname != null) && (classname.length() == 0)) {
                classname = null;
            }
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.SAML_CBHANDLER, classname, null, true);
            return;
        }

        if (source.equals(devDefaultsChBox)) {
            if (devDefaultsChBox.isSelected()) {
                Util.fillDefaults(project, true,true);
                Binding serviceBinding = PolicyModelHelper.getBinding(serviceModel, binding.getName());
                ProfilesModelHelper.setClientDefaults(profile, binding, serviceBinding, project);
                sync();
                refreshLayout();
                ((PanelEnabler)credPanel).enablePanel(false);
                credPanel.revalidate();
                credPanel.repaint();
            } else {
                ((PanelEnabler)credPanel).enablePanel(true);
            }
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
        boolean samlRequired = true;
        boolean authRequired = true;

        if (!amSec) {
            devDefaultsChBox.setEnabled(true);
            boolean defaults = devDefaultsChBox.isSelected();

            boolean trustStoreConfigRequired = true;
            boolean keyStoreConfigRequired = true;
            if (ComboConstants.PROF_USERNAME.equals(profile)) {
                keyStoreConfigRequired = false;
            }
            if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
                trustStoreConfigRequired = false;
            }
            keyStoreButton.setEnabled(!amSec && keyStoreConfigRequired && !defaults);
            trustStoreButton.setEnabled(!amSec && trustStoreConfigRequired && !defaults);

            if (ComboConstants.PROF_USERNAME.equals(profile) || ComboConstants.PROF_STSISSUED.equals(profile) || ComboConstants.PROF_STSISSUEDENDORSE.equals(profile) || ComboConstants.PROF_STSISSUEDCERT.equals(profile) || ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
                samlRequired = false;
            }

            if (ComboConstants.PROF_SAMLSSL.equals(profile) || ComboConstants.PROF_SAMLHOLDER.equals(profile) || ComboConstants.PROF_SAMLSENDER.equals(profile)) {
                authRequired = false;
            }

            credTypeLabel.setEnabled(!amSec && authRequired && !defaults);
            credTypeCombo.setEnabled(!amSec && authRequired && !defaults);

            credPanel.setEnabled(!amSec && authRequired && !defaults);
            Component[] comps = credPanel.getComponents();
            for (Component c : comps) {
                c.setEnabled(!amSec && authRequired && !defaults);
            }

            samlBrowseButton.setEnabled(!amSec && samlRequired && !defaults);
            samlHandlerField.setEnabled(!amSec && samlRequired && !defaults);
            samlHandlerLabel.setEnabled(!amSec && samlRequired && !defaults);
        } else {
            credPanel.setEnabled(false);
            Component[] comps = credPanel.getComponents();
            for (Component c : comps) {
                c.setEnabled(false);
            }
            credTypeCombo.setEnabled(false);
            credTypeLabel.setEnabled(false);
            devDefaultsChBox.setEnabled(false);
            jSeparator1.setEnabled(false);
            keyStoreButton.setEnabled(false);
            samlBrowseButton.setEnabled(false);
            samlHandlerField.setEnabled(false);
            samlHandlerLabel.setEnabled(false);
            trustStoreButton.setEnabled(false);
        }
        refreshLayout();
    }

    public static boolean isStoreConfigRequired(String profile, boolean trust, Binding binding) {
        ArrayList<WSDLComponent> compsToTry = new ArrayList<WSDLComponent>();
        compsToTry.add(binding);
        Collection<BindingOperation> ops = binding.getBindingOperations();
        for (BindingOperation op : ops) {
            BindingInput bi = op.getBindingInput();
            if (bi != null) {
                compsToTry.add(bi);
            }
            BindingOutput bo = op.getBindingOutput();
            if (bo != null) {
                compsToTry.add(bo);
            }
            Collection<BindingFault> bfs = op.getBindingFaults();
            for (BindingFault bf : bfs) {
                if (bf != null) {
                    compsToTry.add(bf);
                }
            }
        }

        for (WSDLComponent wc : compsToTry) {
            List<WSDLComponent> suppTokens = SecurityTokensModelHelper.getSupportingTokens(wc);
            if (suppTokens != null) {
                for (WSDLComponent suppToken : suppTokens) {
                    WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(suppToken);
                    if (token instanceof X509Token) {
                        return true;
                    }
                }
            }
        }

        if ((ComboConstants.PROF_TRANSPORT.equals(profile)) || (ComboConstants.PROF_SAMLSSL.equals(profile))) {
            return false;
        }
        if (!trust) {
            if (ComboConstants.PROF_USERNAME.equals(profile)) {
                return false;
            }
            if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
                // TODO - depends on other config
            }
        } else {
            if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
                return false;
            }
        }
        return true;
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
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(12, 12, 12).add(keyStoreButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(trustStoreButton)).add(devDefaultsChBox)).add(284, 284, 284)).add(layout.createSequentialGroup().add(24, 24, 24).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(credTypeLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(credTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup().add(samlHandlerLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(samlHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 209, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(samlBrowseButton)).add(org.jdesktop.layout.GroupLayout.LEADING, credPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(devDefaultsChBox).addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(keyStoreButton).add(trustStoreButton)).addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(credTypeLabel).add(credTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(credPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(samlHandlerLabel).add(samlHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(samlBrowseButton)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
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
        keyStoreButton = new javax.swing.JButton();
        trustStoreButton = new javax.swing.JButton();
        devDefaultsChBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();

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

        credTypeLabel.setLabelFor(credTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(credTypeLabel, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_AuthTypeLabel")); // NOI18N

        org.jdesktop.layout.GroupLayout credPanelLayout = new org.jdesktop.layout.GroupLayout(credPanel);
        credPanel.setLayout(credPanelLayout);
        credPanelLayout.setHorizontalGroup(
            credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 424, Short.MAX_VALUE)
        );
        credPanelLayout.setVerticalGroup(
            credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 64, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(keyStoreButton, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStore")); // NOI18N
        keyStoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyStoreButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(trustStoreButton, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_Truststore")); // NOI18N
        trustStoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trustStoreButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(devDefaultsChBox, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_DevDefaults")); // NOI18N
        devDefaultsChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(keyStoreButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(trustStoreButton))
                    .add(devDefaultsChBox))
                .add(284, 284, 284))
            .add(layout.createSequentialGroup()
                .add(24, 24, 24)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(credTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(credTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(samlHandlerLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(samlHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 209, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(samlBrowseButton))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, credPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(devDefaultsChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyStoreButton)
                    .add(trustStoreButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
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

        samlHandlerLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_SamlLabel_ACSN")); // NOI18N
        samlHandlerLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_SamlLabel_ACSD")); // NOI18N
        samlBrowseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_AuthPanel_SCHBrowseButton_ACSN")); // NOI18N
        samlBrowseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_AuthPanel_SCHBrowseButton_ACSD")); // NOI18N
        credTypeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_AuthTypeLabel_ACSN")); // NOI18N
        credTypeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_AuthTypeLabel_ACSD")); // NOI18N
        keyStoreButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStore_ACSN")); // NOI18N
        keyStoreButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStore_ACSD")); // NOI18N
        trustStoreButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_Truststore_ACSN")); // NOI18N
        trustStoreButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_Truststore_ACSD")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_DevDefaults_ACSN")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_DevDefaults_ACSD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void trustStoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trustStoreButtonActionPerformed
    boolean jsr109 = isJsr109Supported();
    TruststorePanel storePanel = new TruststorePanel(binding, project, jsr109, profile, true);
    DialogDescriptor dlgDesc = new DialogDescriptor(storePanel, 
            NbBundle.getMessage(ServicePanel.class, "LBL_Truststore_Panel_Title")); //NOI18N
    Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

    dlg.setVisible(true); 
    if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
        storePanel.storeState();
    }
}//GEN-LAST:event_trustStoreButtonActionPerformed

private void keyStoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyStoreButtonActionPerformed
    boolean jsr109 = isJsr109Supported();
    KeystorePanel storePanel = new KeystorePanel(binding, project, jsr109, true);
    DialogDescriptor dlgDesc = new DialogDescriptor(storePanel, 
            NbBundle.getMessage(ServicePanel.class, "LBL_Keystore_Panel_Title")); //NOI18N
    Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

    dlg.setVisible(true); 

    if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
        storePanel.storeState();
    }
}//GEN-LAST:event_keyStoreButtonActionPerformed

private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
    enableDisable();
}//GEN-LAST:event_formFocusGained

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    enableDisable();
}//GEN-LAST:event_formAncestorAdded

    private boolean isJsr109Supported(){
        J2eePlatform j2eePlatform = getJ2eePlatform();
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
        }
        return false;
    }
    
    private J2eePlatform getJ2eePlatform(){
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if(provider != null){
            String serverInstanceID = provider.getServerInstanceID();
            if(serverInstanceID != null && serverInstanceID.length() > 0) {
                return Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            }
        }
        return null;
    }    

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
    private javax.swing.JCheckBox devDefaultsChBox;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton keyStoreButton;
    private javax.swing.JButton samlBrowseButton;
    private javax.swing.JTextField samlHandlerField;
    private javax.swing.JLabel samlHandlerLabel;
    private javax.swing.JButton trustStoreButton;
    // End of variables declaration//GEN-END:variables
    
}
