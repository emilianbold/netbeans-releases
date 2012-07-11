/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.wizards;

import javax.swing.event.DocumentListener;
import static org.openide.util.NbBundle.getMessage;

/**
 *
 * @author kratz
 */
public class GlassFishAccountWizardUserComponent
        extends GlassFishWizardComponent {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////
    
    /** Validity of <code>account</code> field. */
    private boolean accountValid;

    /** Validity of <code>userName</code> field. */
    private boolean userNameValid;

    /** Validity of <code>userPassword</code> field. */
    private boolean userPasswordValid;

    /** Event listener to validate host field on the fly. */
    private DocumentListener accountEventListener
            = new ComponentFieldListener() {

        /**
         * Process received notification.
         */
        @Override
        void processEvent() {
            ValidationResult result = accountValid();
            accountValid = result.isValid();
            update(result);            
        }

    };

    /** Event listener to validate user name field on the fly. */
    private DocumentListener userNameEventListener
            = new ComponentFieldListener() {

        /**
         * Process received notification.
         */
        @Override
        void processEvent() {
            ValidationResult result = userNameValid();
            userNameValid = result.isValid();
            update(result);            
        }

    };

    /** Event listener to validate user password field on the fly. */
    private DocumentListener userPasswordEventListener
            = new ComponentFieldListener() {

        /**
         * Process received notification.
         */
        @Override
        void processEvent() {
            ValidationResult result = userPasswordValid();
            userPasswordValid = result.isValid();
            update(result);            
        }

    };

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates new form GlassFishAccountWizardUserComponent
     */
    public GlassFishAccountWizardUserComponent() {
        initComponents();
        accountValid = accountValid().isValid();
        userNameValid = userNameValid().isValid();
        userPasswordValid = userPasswordValid().isValid();
        accountTextField.getDocument()
                .addDocumentListener(accountEventListener);
        userNameTextField.getDocument()
                .addDocumentListener(userNameEventListener);
        userPasswordTextField.getDocument()
                .addDocumentListener(userPasswordEventListener);
    }

    ////////////////////////////////////////////////////////////////////////////
    // GUI Getters                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get account name.
     * <p/>
     * @return Account name.
     */
    public String getAccount() {
        String text = accountTextField.getText();
        return text != null ? text.trim() : null;
    }

    /**
     * Get user name.
     * <p/>
     * @return User name.
     */
    public String getUserName() {
        String text = userNameTextField.getText();
        return text != null ? text.trim() : null;
    }

    /**
     * Get user password.
     * <p/>
     * Password processing should not remove leading and trailing spaces.
     * <p/>
     * @return User password.
     */
    public String getUserPassword() {
        return userPasswordTextField.getText();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented abstract methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Enable modification of form elements.
     */
    @Override
    void enableModifications() {
        accountTextField.setEditable(true);
        userNameTextField.setEditable(true);
        userPasswordTextField.setEditable(true);
    }

    /**
     * Disable modification of form elements.
     */
    @Override
    void disableModifications() {
        accountTextField.setEditable(false);
        userNameTextField.setEditable(false);
        userPasswordTextField.setEditable(false);
    }

    /**
     * Validate component.
     */
    @Override
    boolean valid() {
        return accountValid && userNameValid && userPasswordValid; 
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Validate account field.
     * <p/>
     * Account field should be non empty string value containing at least one
     * non-whitespace character.
     * Value of <code>validationError</code> is set to inform about field
     * status.
     * <p/>
     * @return <code>true</code> when account field is valid
     *         or <code>false</code> otherwise.
     */
    final ValidationResult accountValid() {
        String account = getAccount();
        if (account != null && account.length() > 0) {
            return new ValidationResult(true, null);
        } else {
            return new ValidationResult(false,
                    getMessage(GlassFishCloudWizardCpasComponent.class,
                    Bundle.USER_PANEL_ERROR_ACCOUNT_EMPTY));
        }
    }

    /**
     * Validate userName field.
     * <p/>
     * User name field should be non empty string value containing at least one
     * non-whitespace character.
     * Value of <code>validationError</code> is set to inform about field
     * status.
     * <p/>
     * @return <code>true</code> when userName field is valid
     *         or <code>false</code> otherwise.
     */
    final ValidationResult userNameValid() {
        String userName = getUserName();
        if (userName != null && userName.length() > 0) {
            return new ValidationResult(true, null);
        } else {
            return new ValidationResult(false,
                    getMessage(GlassFishCloudWizardCpasComponent.class,
                    Bundle.USER_PANEL_ERROR_USER_NAME_EMPTY));
        }
    }

    /**
     * Validate userPassword field.
     * <p/>
     * User password  field should be non empty string value containing at least
     * one character.
     * Value of <code>validationError</code> is set to inform about field
     * status.
     * <p/>
     * @return <code>true</code> when userPassworde field is valid
     *         or <code>false</code> otherwise.
     */
    final ValidationResult userPasswordValid() {
        String userPassword = getUserPassword();
        if (userPassword != null && userPassword.length() > 0) {
            return new ValidationResult(true, null);
        } else {
            return new ValidationResult(false,
                    getMessage(GlassFishCloudWizardCpasComponent.class,
                    Bundle.USER_PANEL_ERROR_USER_PASSWORD_EMPTY));
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Generated GUI code                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        accountLabel = new javax.swing.JLabel();
        userNameLabel = new javax.swing.JLabel();
        userPasswordLabel = new javax.swing.JLabel();
        userPasswordTextField = new javax.swing.JTextField();
        userNameTextField = new javax.swing.JTextField();
        accountTextField = new javax.swing.JTextField();

        accountLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.accountLabel.text")); // NOI18N

        userNameLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.userNameLabel.text")); // NOI18N

        userPasswordLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.userPasswordLabel.text")); // NOI18N

        userPasswordTextField.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.userPasswordTextField.text")); // NOI18N

        userNameTextField.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.userNameTextField.text")); // NOI18N

        accountTextField.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.accountTextField.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userPasswordLabel)
                    .addComponent(accountLabel)
                    .addComponent(userNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userPasswordTextField)
                    .addComponent(userNameTextField)
                    .addComponent(accountTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(accountLabel)
                    .addComponent(accountTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameLabel)
                    .addComponent(userNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userPasswordLabel)
                    .addComponent(userPasswordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel accountLabel;
    private javax.swing.JTextField accountTextField;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JTextField userNameTextField;
    private javax.swing.JLabel userPasswordLabel;
    private javax.swing.JTextField userPasswordTextField;
    // End of variables declaration//GEN-END:variables
}
