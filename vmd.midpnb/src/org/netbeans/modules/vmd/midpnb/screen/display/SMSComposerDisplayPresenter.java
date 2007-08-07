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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.screen.display;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.screen.display.DisplayableDisplayPresenter;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenTextAreaPropertyEditor;
import org.netbeans.modules.vmd.midpnb.components.displayables.SMSComposerCD;

/**
 *
 * @author Karol Harezlak
 */
public class SMSComposerDisplayPresenter extends DisplayableDisplayPresenter {

    private JComponent view;
    private SMSView smsView;

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        if (smsView != null) {
            smsView.updateView();
        }
    }

    @Override
    public JComponent getView() {
        if (view == null) {
            view = super.getView();
            smsView = new SMSView();
            super.getView().add(smsView);
        }
        return view;
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        Collection<ScreenPropertyDescriptor> desciptors = new ArrayList<ScreenPropertyDescriptor>(super.getPropertyDescriptors());
        desciptors.addAll(Arrays.asList(new ScreenPropertyDescriptor(getComponent(), smsView.messageLabel, new ScreenStringPropertyEditor(SMSComposerCD.PROP_MESSAGE_LABEL, JTextField.CENTER)), new ScreenPropertyDescriptor(getComponent(), smsView.messageTextrArea, new ScreenTextAreaPropertyEditor(SMSComposerCD.PROP_MESSAGE)), new ScreenPropertyDescriptor(getComponent(), smsView.phoneNumberLabel, new ScreenStringPropertyEditor(SMSComposerCD.PROP_PHONE_NUMEBR_LABEL, JTextField.CENTER)), new ScreenPropertyDescriptor(getComponent(), smsView.phoneNumberTextField, new ScreenStringPropertyEditor(SMSComposerCD.PROP_PHONE_NUMBER, JTextField.CENTER))));
        return desciptors;
    }

    private class SMSView extends JPanel {

        private javax.swing.JTextArea messageTextrArea;
        private javax.swing.JLabel messageLabel;
        private javax.swing.JScrollPane messageScrollPane;
        private javax.swing.JTextField phoneNumberTextField;
        private javax.swing.JLabel phoneNumberLabel;

        SMSView() {
            initComponents();
        }

        private void initComponents() {
            java.awt.GridBagConstraints gridBagConstraints;
            phoneNumberLabel = new javax.swing.JLabel();
            messageLabel = new javax.swing.JLabel();
            phoneNumberTextField = new javax.swing.JTextField();
            messageScrollPane = new javax.swing.JScrollPane();
            messageTextrArea = new javax.swing.JTextArea();

            setLayout(new java.awt.GridBagLayout());

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.insets = new java.awt.Insets(50, 20, 0, 0);
            add(phoneNumberLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
            add(messageLabel, gridBagConstraints);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.insets = new java.awt.Insets(50, 4, 0, 30);
            add(phoneNumberTextField, gridBagConstraints);

            messageTextrArea.setColumns(20);
            messageTextrArea.setRows(5);
            messageScrollPane.setViewportView(messageTextrArea);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
            add(messageScrollPane, gridBagConstraints);
            updateView();
        }

        void updateView() {
            final DesignComponent component = SMSComposerDisplayPresenter.this.getComponent();
            component.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    String phoneNumberLabelText = (String) component.readProperty(SMSComposerCD.PROP_PHONE_NUMEBR_LABEL).getPrimitiveValue();
                    phoneNumberLabel.setText(phoneNumberLabelText);
                    String phoneNumberText = (String) component.readProperty(SMSComposerCD.PROP_PHONE_NUMBER).getPrimitiveValue();
                    phoneNumberTextField.setText(phoneNumberText);
                    String messageLabelString = (String) component.readProperty(SMSComposerCD.PROP_MESSAGE_LABEL).getPrimitiveValue();
                    messageLabel.setText(messageLabelString);
                    String messageText = (String) component.readProperty(SMSComposerCD.PROP_MESSAGE).getPrimitiveValue();
                    messageTextrArea.setText(messageText);
                    Integer bkgColor = (Integer) component.readProperty(SMSComposerCD.PROP_BGK_COLOR).getPrimitiveValue();
                    if (bkgColor != null) {
                        SMSView.this.setBackground(new Color(bkgColor));
                    }
                    Integer frgColor = (Integer) component.readProperty(SMSComposerCD.PROP_FRG_COLOR).getPrimitiveValue();
                    if (frgColor != null) {
                        Color color = new Color(frgColor);
                        SMSView.this.setForeground(color);
                        phoneNumberLabel.setForeground(color);
                        messageLabel.setForeground(color);
                        phoneNumberTextField.setForeground(color);
                        messageTextrArea.setForeground(color);
                    }
                    
                }
            });
        }
    }
}
