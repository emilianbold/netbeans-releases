/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.screen.display.DisplayableDisplayPresenter;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;
import org.netbeans.modules.vmd.midpnb.components.displayables.LoginScreenCD;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class LoginScreenDisplayPresenter extends DisplayableDisplayPresenter {

    private JComponent view;
    private LoginView loginView;
    private static final String USER_CODE = NbBundle.getMessage(LoginScreenDisplayPresenter.class, "LBL_UserCode"); //NOI18N
    private static final String NULL_TEXT = NbBundle.getMessage(LoginScreenDisplayPresenter.class, "LBL_NULL"); //NOI18N

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        if (loginView != null) {
            loginView.updateView();
        }
    }

    @Override
    public JComponent getView() {
        if (view == null) {
            view = super.getView();
            loginView = new LoginView();
            super.getView().add(loginView);
        }
        return view;
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        Collection<ScreenPropertyDescriptor> desciptors = new ArrayList<ScreenPropertyDescriptor>(super.getPropertyDescriptors());
        desciptors.addAll(Arrays.asList(
                new ScreenPropertyDescriptor(getComponent(), loginView.passwordTextField, new ScreenStringPropertyEditor(LoginScreenCD.PROP_PASSWORD, JTextField.CENTER)),
                new ScreenPropertyDescriptor(getComponent(), loginView.passwordLabel, new ScreenStringPropertyEditor(LoginScreenCD.PROP_PASSWORD_LABEL, JTextField.CENTER)),
                new ScreenPropertyDescriptor(getComponent(), loginView.usernameLabel, new ScreenStringPropertyEditor(LoginScreenCD.PROP_USERNAME_LABEL, JTextField.CENTER)),
                new ScreenPropertyDescriptor(getComponent(), loginView.usernameTextField, new ScreenStringPropertyEditor(LoginScreenCD.PROP_USERNAME, JTextField.CENTER))
                ));
        return desciptors;
    }

    private class LoginView extends JPanel {

        JLabel loginTitleLabel;
        JButton loginButton;
        JLabel passwordLabel;
        JPasswordField passwordTextField;
        JLabel usernameLabel;
        JTextField usernameTextField;
        GridBagConstraints gridBagConstraints;

        LoginView() {
            initComponents();
        }

        private void initComponents() {
            loginTitleLabel = new javax.swing.JLabel();
            usernameLabel = new javax.swing.JLabel();
            passwordLabel = new javax.swing.JLabel();
            usernameTextField = new javax.swing.JTextField();
            passwordTextField = new javax.swing.JPasswordField();
            loginButton = new javax.swing.JButton();

            setLayout(new java.awt.GridBagLayout());

            addLoginTitle();
            
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
            add(usernameLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
            add(passwordLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            add(usernameTextField, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.ipadx = 100;
            add(passwordTextField, gridBagConstraints);

            addLoginButton();

            updateView();
        }

        private void addLoginButton() {
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.ipadx = 10;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
            gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
            add(loginButton, gridBagConstraints);
        }

        private void addLoginTitle() {
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 4;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
            add(loginTitleLabel, gridBagConstraints);
        }

        void updateView() {
            final DesignComponent component = LoginScreenDisplayPresenter.this.getComponent();
            if (component == null) {
                return;
            }
            component.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    PropertyValue usernameLabelPV = component.readProperty(LoginScreenCD.PROP_USERNAME_LABEL);
                    if (usernameLabelPV.getKind() == PropertyValue.Kind.VALUE) {
                        usernameLabel.setText((String) usernameLabelPV.getPrimitiveValue());
                    } else if (usernameLabelPV.getKind() == PropertyValue.Kind.USERCODE) {
                        usernameLabel.setText(USER_CODE); 
                    } else if (usernameLabelPV.getKind() == PropertyValue.Kind.NULL) {
                        usernameLabel.setText(NULL_TEXT); 
                    }
                    PropertyValue passwordLabelPV = component.readProperty(LoginScreenCD.PROP_PASSWORD_LABEL);
                    if (passwordLabelPV.getKind() == PropertyValue.Kind.VALUE) {
                        passwordLabel.setText((String) passwordLabelPV.getPrimitiveValue());
                    } else if (passwordLabelPV.getKind() == PropertyValue.Kind.USERCODE) {
                        passwordLabel.setText(USER_CODE); 
                    } else {
                        passwordLabel.setText(NULL_TEXT); 
                    }
                    PropertyValue usernameTextFieldPV = component.readProperty(LoginScreenCD.PROP_USERNAME);
                    if (usernameTextFieldPV.getKind() == PropertyValue.Kind.VALUE) {
                        usernameTextField.setEnabled(true);
                        usernameTextField.setText((String) usernameTextFieldPV.getPrimitiveValue());
                    } else if (usernameTextFieldPV.getKind() == PropertyValue.Kind.USERCODE) {
                        usernameTextField.setText(USER_CODE); 
                        usernameTextField.setEnabled(false);
                    } 
                    
                    PropertyValue passwordTextFieldPV = component.readProperty(LoginScreenCD.PROP_PASSWORD);
                    if (passwordTextFieldPV.getKind() == PropertyValue.Kind.VALUE) {
                        passwordTextField.setEnabled(true);
                        passwordTextField.setText((String) passwordTextFieldPV.getPrimitiveValue());
                    } else if (passwordTextFieldPV.getKind() == PropertyValue.Kind.USERCODE) {
                        passwordTextField.setText(USER_CODE); 
                        passwordTextField.setEnabled(false);
                    } 
                    
                    Integer bckColor = (Integer) component.readProperty(LoginScreenCD.PROP_BGK_COLOR).getPrimitiveValue();
                    Integer frgColor = (Integer) component.readProperty(LoginScreenCD.PROP_FRG_COLOR).getPrimitiveValue();
                    if (bckColor != null) {
                        Color color = new Color(bckColor);
                        setBackground(color);
                        loginButton.setBackground(color);
                    }
                    if (frgColor != null) {
                        Color color = new Color(frgColor);
                        usernameLabel.setForeground(color);
                        usernameTextField.setForeground(color);
                        passwordLabel.setForeground(color);
                        passwordTextField.setForeground(color);
                        loginButton.setForeground(color);
                    }
                    
                    Boolean buttonUsed = (Boolean) component.readProperty(LoginScreenCD.PROP_USE_LOGIN_BUTTON).getPrimitiveValue();
                    if (buttonUsed == null) {
                        throw new IllegalArgumentException();
                    }
                    if (buttonUsed) {
                        addLoginButton();
                        PropertyValue loginButtonPV = component.readProperty(LoginScreenCD.PROP_LOGIN_BUTTON_TEXT);
                        if (loginButtonPV.getKind() == PropertyValue.Kind.VALUE) {
                            loginButton.setText((String) loginButtonPV.getPrimitiveValue());
                        } else if (loginButtonPV.getKind() == PropertyValue.Kind.USERCODE) {
                            loginButton.setText(USER_CODE);
                        } else {
                            loginButton.setText(NULL_TEXT);
                        }
                    } else {
                        remove(loginButton);
                    }

                    PropertyValue loginTitlePV = component.readProperty(LoginScreenCD.PROP_LOGIN_TITLE);
                    if (loginTitlePV.getKind() == PropertyValue.Kind.NULL) {
                        remove(loginTitleLabel);
                    } else {
                        addLoginTitle();
                        if (loginTitlePV.getKind() == PropertyValue.Kind.VALUE) {
                            loginTitleLabel.setText((String) loginTitlePV.getPrimitiveValue());
                        } else if (loginTitlePV.getKind() == PropertyValue.Kind.USERCODE) {
                            loginTitleLabel.setText(USER_CODE);
                        }
                    }
                }
            });
        }
    }
}
