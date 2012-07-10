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
public class GlassFishCloudWizardCpasComponent
        extends GlassFishWizardComponent {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////
    
    /** Validity of host field. */
    private boolean hostValid;
    
    /** Validity of port field. */
    private boolean portValid;

    /** Event listener to validate host field on the fly. */
    private DocumentListener hostEventListener = new ComponentFieldListener() {

        /**
         * Process received notification.
         */
        @Override
        void processEvent() {
            ValidationResult result = hostValid();
            hostValid = result.isValid();
            update(result);            
        }

    };

    /** Event listener to validate port field on the fly. */
    private DocumentListener portEventListener = new ComponentFieldListener() {

        /**
         * Process received notification.
         */
        @Override
        void processEvent() {
            ValidationResult result = portValid();
            portValid = result.isValid();
            update(result);            
        }

    };

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates new form GlassFishCloudWizardCpasComponent
     */
    public GlassFishCloudWizardCpasComponent() {
        initComponents();
        hostValid = hostValid().isValid();
        portValid = portValid().isValid();
        hostTextField.getDocument().addDocumentListener(hostEventListener);
        portTextField.getDocument().addDocumentListener(portEventListener);
    }

    ////////////////////////////////////////////////////////////////////////////
    // GUI Getters                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get CPAS host name.
     * <p/>
     * @return CPAS host name.
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

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Enable modification of form elements.
     */
    void enableModifications() {
        hostTextField.setEditable(true);
        portTextField.setEditable(true);
    }

    /**
     * Disable modification of form elements.
     */
    void disableModifications() {
        hostTextField.setEditable(false);
        portTextField.setEditable(false);
    }

    /**
     * Validate host field.
     * <p/>
     * Host field should be non empty string value containing at least one
     * non-whitespace character.
     * Value of <code>validationError</code> is set to inform about field
     * status.
     * <p/>
     * @return <code>true</code> when host field is valid or <code>false</code>
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
     * @return <code>true</code> when host field is valid or <code>false</code>
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

    /**
     * Validate component.
     */
    boolean valid() {
        return hostValid && portValid; 
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

        hostLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishCloudWizardCpasComponent.class, "GlassFishCloudWizardCpasComponent.hostLabel.text")); // NOI18N

        hostTextField.setText(org.openide.util.NbBundle.getMessage(GlassFishCloudWizardCpasComponent.class, "GlassFishCloudWizardCpasComponent.hostTextField.text")); // NOI18N

        portLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishCloudWizardCpasComponent.class, "GlassFishCloudWizardCpasComponent.portLabel.text")); // NOI18N

        portTextField.setText(org.openide.util.NbBundle.getMessage(GlassFishCloudWizardCpasComponent.class, "GlassFishCloudWizardCpasComponent.portTextField.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostLabel)
                    .addComponent(portLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 266, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
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
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    // End of variables declaration//GEN-END:variables
}
