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
package org.netbeans.modules.vmd.midpnb.screen.display;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.screen.display.DisplayableDisplayPresenter;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenTextAreaPropertyEditor;
import org.netbeans.modules.vmd.midpnb.components.displayables.SMSComposerCD;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class SMSComposerDisplayPresenter extends DisplayableDisplayPresenter {

    private JComponent view;
    private SMSView smsView;
    private static final String USER_CODE = NbBundle.getMessage(LoginScreenDisplayPresenter.class, "LBL_UserCode"); //NOI18N
    private static final String NULL_TEXT = NbBundle.getMessage(LoginScreenDisplayPresenter.class, "LBL_NULL"); //NOI18N

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
        desciptors.addAll(Arrays.asList(new ScreenPropertyDescriptor(getComponent(),
                                        smsView.messageLabel,
                                        new ScreenStringPropertyEditor(SMSComposerCD.PROP_MESSAGE_LABEL, JTextField.CENTER)),
                                        new ScreenPropertyDescriptor(getComponent(),
                                        smsView.messageTextrArea,
                                        new ScreenTextAreaPropertyEditor(SMSComposerCD.PROP_MESSAGE)),
                                        new ScreenPropertyDescriptor(getComponent(),
                                        smsView.phoneNumberLabel,
                                        new ScreenStringPropertyEditor(SMSComposerCD.PROP_PHONE_NUMEBR_LABEL, JTextField.CENTER)),
                                        new ScreenPropertyDescriptor(getComponent(),
                                        smsView.phoneNumberTextField,
                                        new ScreenStringPropertyEditor(SMSComposerCD.PROP_PHONE_NUMBER, JTextField.CENTER))));
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
                    PropertyValue phoneNumberLabelTextPV = component.readProperty(SMSComposerCD.PROP_PHONE_NUMEBR_LABEL);
                    String phoneNumberLabelText;
                    if (phoneNumberLabelTextPV.getKind() == PropertyValue.Kind.VALUE) {
                        phoneNumberLabelText = (String) phoneNumberLabelTextPV.getPrimitiveValue();
                    } else if (phoneNumberLabelTextPV.getKind() == PropertyValue.Kind.USERCODE) {
                        phoneNumberLabelText = USER_CODE;
                    } else {
                        phoneNumberLabelText = NULL_TEXT;
                    }
                    phoneNumberLabel.setText(phoneNumberLabelText);

                    PropertyValue phoneNumberTextFieldPV = component.readProperty(SMSComposerCD.PROP_PHONE_NUMBER);
                    String phoneNumberText = null;
                    if (phoneNumberTextFieldPV.getKind() == PropertyValue.Kind.VALUE) {
                        phoneNumberTextField.setEditable(true);
                        phoneNumberText = (String) phoneNumberTextFieldPV.getPrimitiveValue();
                    } else if (phoneNumberTextFieldPV.getKind() == PropertyValue.Kind.USERCODE) {
                        phoneNumberText = USER_CODE;
                        phoneNumberTextField.setEnabled(false);
                    } 
                    phoneNumberTextField.setText(phoneNumberText);
                    
                    PropertyValue messageLabelStringPV = component.readProperty(SMSComposerCD.PROP_MESSAGE_LABEL);
                    String messageLabelString = null;
                    if (messageLabelStringPV.getKind() == PropertyValue.Kind.VALUE) {
                        messageLabelString = (String) messageLabelStringPV.getPrimitiveValue();
                    } else if (messageLabelStringPV.getKind() == PropertyValue.Kind.USERCODE) {
                        messageLabelString = USER_CODE;
                    } else {
                        phoneNumberText = NULL_TEXT;
                    }
                    messageLabel.setText(messageLabelString);
                    
                    PropertyValue messageTextAreaPV = component.readProperty(SMSComposerCD.PROP_MESSAGE);
                    String messageText = null;
                    if (messageTextAreaPV.getKind() == PropertyValue.Kind.VALUE) {
                        messageTextrArea.setEnabled(true);
                        messageText = (String) messageTextAreaPV.getPrimitiveValue();
                    } else if (messageTextAreaPV.getKind() == PropertyValue.Kind.USERCODE) {
                        messageText = USER_CODE;
                        messageTextrArea.setEnabled(false);
                    } 
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