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
import org.netbeans.modules.glassfish.cloud.data.GlassFishCloudInstance;
import org.netbeans.modules.glassfish.cloud.data.GlassFishCloudInstanceProvider;
import static org.openide.util.NbBundle.getMessage;

/**
 * GlassFish cloud GUI component.
 * <p/>
 * Allows editing cloud attributes in both add wizard and properties update
 * pop up.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishCloudWizardCpasComponent
        extends GlassFishWizardComponent {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize GlassFish cloud GUI component instance in constructor.
     * <p/>
     * Constructor helper containing shared code. do not use this method outside
     * constructors.
     * <p/>
     * @param instance GlassFish cloud GUI component instance to be initialized.
     */
    private static void initInstance(
            GlassFishCloudWizardCpasComponent instance) {
        instance.initComponents();
        instance.hostValid = instance.hostValid().isValid();
        instance.portValid = instance.portValid().isValid();
        instance.displayNameValid = instance.displayNameValid().isValid();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish cloud instance. */
    GlassFishCloudInstance instance;

    /** Validity of <code>displayName</code> field. */
    private boolean displayNameValid;

    /** Validity of <code>host</code> field. */
    private boolean hostValid;
    
    /** Validity of <code>port</code> field. */
    private boolean portValid;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates new instance of GlassFish cloud GUI component.
     * <p/>
     * Form field handlers are set to wizard mode.
     */
    @SuppressWarnings("LeakingThisInConstructor") // initInstance(this);
    public GlassFishCloudWizardCpasComponent() {
        this.instance = null;
        initInstance(this);
        hostTextField.getDocument()
                .addDocumentListener(initHostValidateListener());
        portTextField.getDocument()
                .addDocumentListener(initPortValidateListener());
    }

    /**
     * Creates new instance of GlassFish cloud GUI component.
     * <p/>
     * Form field handlers are set to edit mode and fields are initialized using
     * <code>instance</code> content.
     * <p/>
     * @param instance GlassFish cloud instance to be modified.
     */
    @SuppressWarnings("LeakingThisInConstructor") // initInstance(this);
    public GlassFishCloudWizardCpasComponent(GlassFishCloudInstance instance) {
        this.instance = instance;
        initInstance(this);
        nameLabel.setVisible(false);
        nameTextField.setVisible(false);
        hostTextField.getDocument()
                .addDocumentListener(initHostUpdateListener());
        portTextField.getDocument()
                .addDocumentListener(initPortUpdateListener());
    }

    ////////////////////////////////////////////////////////////////////////////
    // GUI Getters                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get CPAS display name.
     * <p/>
     * @return CPAS display name.
     */
    public String getDisplayName() {
        String text = nameTextField.getText();
        return text != null ? text.trim() : null;
    }

    /**
     * Get CPAS displayName name.
     * <p/>
     * @return CPAS displayName name.
     */
    public String getHost() {
        String text = hostTextField.getText();
        return text != null ? text.trim() : null;
    }

    /**
     * Get CPAS port number.
     * <p/>
     * @return CPAS port number, <code>0</code> for no value in text box
     *         or <code>-1</code> when number cannot be read.
     */
    public int getPort() {
        String text = portTextField.getText();
        try {
            return text != null ? Integer.parseInt(text.trim()) : 0;
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    /**
     * Get CPAS port number as <code>String</code>.
     * <p/>
     * @return CPAS port number, <code>0</code> for no value in text box
     *         or <code>-1</code> when number cannot be read.
     */
    public String getPortText() {
        String text = portTextField.getText();
        return text != null ? text.trim() : null;
    }

    /**
     * Get CPAS display name for GUI initialization.
     * <p/>
     * This method is used only in generated <code>initComponents</code> method.
     * <p/>
     * @return Value of name passed from cloud entity object
     *         or empty <code>String</code> when cloud entity object
     *         is <code>null</code>.
     */
    private String initDisplayName() {
        return instance != null ? instance.getName() : "";
    }

    /**
     * Get CPAS host for GUI initialization.
     * <p/>
     * This method is used only in generated <code>initComponents</code> method.
     * <p/>
     * @return Value of host passed from cloud entity object
     *         or empty <code>String</code> when cloud entity object
     *         is <code>null</code>.
     */
    private String initHost() {
        return instance != null ? instance.getHost() : "";
    }

    /**
     * Get CPAS port for GUI initialization.
     * <p/>
     * This method is used only in generated <code>initComponents</code> method.
     * <p/>
     * @return Value of port passed from cloud entity object
     *         or empty <code>String</code> when cloud entity object
     *         is <code>null</code>.
     */
    private String initPort() {
        return instance != null ? Integer.toString(instance.getPort()) : "";
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented abstract methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Enable modification of form elements.
     */
    @Override
    void enableModifications() {
        hostTextField.setEditable(true);
        portTextField.setEditable(true);
    }

    /**
     * Disable modification of form elements.
     */
    @Override
    void disableModifications() {
        hostTextField.setEditable(false);
        portTextField.setEditable(false);
    }

    /**
     * Validate component.
     */
    @Override
    boolean valid() {
        return displayNameValid && hostValid && portValid;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event helper metyhods                                                  //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Process host field validation event.
     * <p/>
     * This is internal event processing helper and should not be used outside
     * listeners <code>processEvent</code> method.
     */
    void processHostValidateEvent() {
        ValidationResult result = hostValid();
        hostValid = result.isValid();
        update(result);
    }
    
    /**
     * Process port field validation event.
     * <p/>
     * This is internal event processing helper and should not be used outside
     * listeners <code>processEvent</code> method.
     */
    void processPortValidateEvent() {
        ValidationResult result = portValid();
        portValid = result.isValid();
        update(result);
    }

    /**
     * Create event listener to validate host field on the fly.
     */
    private DocumentListener initHostValidateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                processHostValidateEvent();
            }
        };
    }

    /**
     * Create event listener to validate port field on the fly.
     */
    private DocumentListener initPortValidateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                processPortValidateEvent();
            }
        };
    }

    /**
     * Create event listener to update host field on the fly.
     */
    private DocumentListener initHostUpdateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                processHostValidateEvent();
                if (hostValid && instance != null) {
                    instance.setHost(getHost());
                    GlassFishCloudInstanceProvider.persist(instance);
                }
            }
        };
    }

    /**
     * Create event listener to update port field on the fly.
     */
    private DocumentListener initPortUpdateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                processPortValidateEvent();
                if (portValid && instance != null) {
                    instance.setPort(getPort());
                    GlassFishCloudInstanceProvider.persist(instance);
                }
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set value of display name text field.
     * <p/>
     * @param name Display name text field to be set.
     */
    ValidationResult setNameTextField(String name) {
        nameTextField.setText(name);
        ValidationResult result = displayNameValid();
        displayNameValid = result.isValid();
        return result;
    }

    /**
     * Validate display name field.
     * <p/>
     * Display name field should be non empty string value containing at least
     * one non-whitespace character. Display name must be unique among
     * all registered GlassFish cloud instances.
     * Value of <code>validationError</code> is set to inform about field
     * status.
     * <p/>
     * @return <code>true</code> when displayName field is valid
     *         or <code>false</code> otherwise.
     */
    final ValidationResult displayNameValid() {
        String displayName = getDisplayName();
        if (displayName != null && displayName.length() > 0) {
            if (GlassFishCloudInstanceProvider.getInstance().getCloudInstances()
                    .containsKey(displayName)) {
                return new ValidationResult(false,
                        getMessage(GlassFishCloudWizardCpasComponent.class,
                        Bundle.CLOUD_PANEL_ERROR_DISPLAY_NAME_DUPLICATED));
            }
            return new ValidationResult(true, null);
        } else {
            return new ValidationResult(false,
                    getMessage(GlassFishCloudWizardCpasComponent.class,
                    Bundle.CLOUD_PANEL_ERROR_DISPLAY_NAME_EMPTY));
        }
    }

    /**
     * Validate displayName field.
     * <p/>
     * Host field should be non empty string value containing at least one
     * non-whitespace character.
     * Value of <code>validationError</code> is set to inform about field
     * status.
     * <p/>
     * @return <code>true</code> when displayName field is valid or <code>false</code>
     *         otherwise.
     */
    final ValidationResult hostValid() {
        String host = getHost();
        if (host != null && host.length() > 0) {
            return new ValidationResult(true, null);
        } else {
            return new ValidationResult(false,
                    getMessage(GlassFishCloudWizardCpasComponent.class,
                    Bundle.CLOUD_PANEL_ERROR_HOST_EMPTY));
        }
    }

    /**
     * Validate port field.
     * <p/>
     * Port field should be non empty string value containing at least one
     * non-whitespace character. It's content must be also valid decimal
     * number.
     * <p/>
     * @return <code>true</code> when displayName field is valid or <code>false</code>
     *         otherwise.
     */
    final ValidationResult portValid() {
        String portText = getPortText();
        if (portText != null && portText.length() > 0) {
            try {
                Integer.parseInt(portText);
            } catch (NumberFormatException nfe) {
                return new ValidationResult(false,
                        getMessage(GlassFishCloudWizardCpasPanel.class,
                        Bundle.CLOUD_PANEL_ERROR_PORT_FORMAT));
            }
            return new ValidationResult(true, null);
        } else {
            return new ValidationResult(false,
                    getMessage(GlassFishCloudWizardCpasComponent.class,
                    Bundle.CLOUD_PANEL_ERROR_PORT_EMPTY));
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

        hostLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();

        hostLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishCloudWizardCpasComponent.class, "GlassFishCloudWizardCpasComponent.hostLabel.text")); // NOI18N

        hostTextField.setText(initHost());

        portLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishCloudWizardCpasComponent.class, "GlassFishCloudWizardCpasComponent.portLabel.text")); // NOI18N

        portTextField.setText(initPort());

        nameLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishCloudWizardCpasComponent.class, "GlassFishCloudWizardCpasComponent.nameLabel.text")); // NOI18N

        nameTextField.setBackground(new java.awt.Color(238, 238, 238));
        nameTextField.setEditable(false);
        nameTextField.setText(initDisplayName());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostLabel)
                    .addComponent(portLabel)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, Short.MAX_VALUE))
                    .addComponent(hostTextField)
                    .addComponent(nameTextField)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLabel)
                    .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portLabel)
                    .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    // End of variables declaration//GEN-END:variables
}
