/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.jms.configeditor;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSBinding;
import org.netbeans.modules.wsdlextensions.jms.JMSConstants;
import org.netbeans.modules.wsdlextensions.jms.JMSMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSOperation;
import org.netbeans.modules.wsdlextensions.jms.validator.JMSComponentValidator;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * JMSBindingConfigurationPanel - Panel that allows configuration of
 * properties specifically for JMS Binding component
 *
 * @author  jalmero
 */
public class JMSConnectionPanel extends javax.swing.JPanel {

    private WSDLComponent mComponent;

    /** QName **/
    private QName mQName;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.jms.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(JMSConnectionPanel.class.getName());

    private MyItemListener mItemListener = null;

    private MyActionListener mActionListener = null;
    private MyDocumentListener mDocumentListener = null;

    private DescriptionPanel mDescPanel = null;
    private DescriptionPanel descPanelArchivePanel = null;
    private DescriptionPanel descPanelTextPanel = null;

    private Dialog mDetailsDlg = null;
    private DialogDescriptor mDetailsDlgDesc = null;

    private Dialog mDetailsJNDIDlg = null;
    private DialogDescriptor mDetailsJNDIDlgDesc = null;

    private Dialog mJNDIEnvDlg = null;
    private DialogDescriptor mJNDIEnvDlgDesc = null;
    private JNDIEnvironmentPanel mJNDIEnvPanel = null;    

    /** Creates new form JMSBindingConfigurationPanel */
    public JMSConnectionPanel(QName qName, WSDLComponent component) {
        initComponents();
        populateView(qName, mComponent);
        setAccessibility();
    }

    @Override
    public String getName() {
        return "Request Configuration";
    }    

    /**
     * Return the connection URL
     * @return String connection url
     */
    String getConnectionURL() {
        return trimTextFieldInput(connectionURLTextField.getText());
    }

    String getUserName() {
        return trimTextFieldInput(userNameTextField.getText());
    }

    String getPassword() {
        char[] pass = passwordTextField.getPassword();
        if (pass.length > 0) {
            String passStr = new String(pass);
            Arrays.fill(pass, '0');
            return passStr;
        }
        return null;
    }

    String getConnectionFactoryName() {
        return trimTextFieldInput(connectionFactoryNameTextField.getText());
    }

    String getInitialContextFactory() {
        return trimTextFieldInput(initialContextFactoryTextField.getText());
    }

    String getProviderURL() {
        return trimTextFieldInput(providerURLTextField.getText());
    }

    String getSecurityPrincipal() {
        return trimTextFieldInput(securityPrincipalTextField.getText());
    }

    String getSecurityCredentials() {
        return trimTextFieldInput(securityCrendentialsTextField.getText());
    }

    private void setAccessibility() {     
        connectionURLTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_connectionURL")); // NOI18N
        connectionURLTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_connectionURL"));
        userNameTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_username")); // NOI18N
        userNameTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_username"));
        passwordTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_password")); // NOI18N
        passwordTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_password"));        
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

        jndiSectionPanel = new javax.swing.JPanel();
        jndiLabel = new javax.swing.JLabel();
        connectionFactoryNameLab = new javax.swing.JLabel();
        initialContextFactoryLab = new javax.swing.JLabel();
        providerURLLab = new javax.swing.JLabel();
        securityPrincipalLab = new javax.swing.JLabel();
        securityCredentialLab = new javax.swing.JLabel();
        securityCrendentialsTextField = new javax.swing.JTextField();
        securityPrincipalTextField = new javax.swing.JTextField();
        providerURLTextField = new javax.swing.JTextField();
        initialContextFactoryTextField = new javax.swing.JTextField();
        connectionFactoryNameTextField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        QTopicBGroup = new javax.swing.ButtonGroup();
        topicSectionPanel = new javax.swing.JPanel();
        operationLab = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        topicPanel1 = new javax.swing.JPanel();
        subscriptionLabel = new javax.swing.JLabel();
        durableRadioButton = new javax.swing.JRadioButton();
        nondurableRadioButton = new javax.swing.JRadioButton();
        subscriptionNameTextField = new javax.swing.JTextField();
        clientIdLabel = new javax.swing.JLabel();
        clientIdTextField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        portBindingPanel = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        portTypeLabel = new javax.swing.JLabel();
        operationNameComboBox = new javax.swing.JComboBox();
        jLabel42 = new javax.swing.JLabel();
        bindingNameComboBox = new javax.swing.JComboBox();
        operationNameLabel = new javax.swing.JLabel();
        servicePortComboBox = new javax.swing.JComboBox();
        bindingNameLabel = new javax.swing.JLabel();
        portTypeComboBox = new javax.swing.JComboBox();
        jSeparator2 = new javax.swing.JSeparator();
        durableBtnGrp = new javax.swing.ButtonGroup();
        jndiBtn = new javax.swing.JButton();
        connectionPanel = new javax.swing.JPanel();
        connectionSectionLabel = new javax.swing.JLabel();
        connectionURLLabel = new javax.swing.JLabel();
        usernameLab = new javax.swing.JLabel();
        passwordLab = new javax.swing.JLabel();
        passwordTextField = new javax.swing.JPasswordField();
        userNameTextField = new javax.swing.JTextField();
        connectionURLTextField = new javax.swing.JTextField();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        jndiSectionPanel.setName("jndiSectionPanel"); // NOI18N
        jndiSectionPanel.setPreferredSize(new java.awt.Dimension(300, 300));

        org.openide.awt.Mnemonics.setLocalizedText(jndiLabel, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.jndiLabel.text")); // NOI18N
        jndiLabel.setName("jndiLabel"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(connectionFactoryNameLab, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.connectionFactoryNameLab.text")); // NOI18N
        connectionFactoryNameLab.setName("connectionFactoryNameLab"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(initialContextFactoryLab, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.initialContextFactoryLab.text")); // NOI18N
        initialContextFactoryLab.setName("initialContextFactoryLab"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(providerURLLab, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.providerURLLab.text")); // NOI18N
        providerURLLab.setName("providerURLLab"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(securityPrincipalLab, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.securityPrincipalLab.text")); // NOI18N
        securityPrincipalLab.setName("securityPrincipalLab"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(securityCredentialLab, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.securityCredentialLab.text")); // NOI18N
        securityCredentialLab.setName("securityCredentialLab"); // NOI18N

        securityCrendentialsTextField.setText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.securityCrendentialsTextField.text")); // NOI18N
        securityCrendentialsTextField.setToolTipText(mBundle.getString("DESC_Attribute_securityCredentials"));
        securityCrendentialsTextField.setName("securityCrendentialsTextField"); // NOI18N
        securityCrendentialsTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                securityCrendentialsTextFieldFocusGained(evt);
            }
        });

        securityPrincipalTextField.setText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.securityPrincipalTextField.text")); // NOI18N
        securityPrincipalTextField.setToolTipText(mBundle.getString("DESC_Attribute_securityPrincipal"));
        securityPrincipalTextField.setName("securityPrincipalTextField"); // NOI18N
        securityPrincipalTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                securityPrincipalTextFieldFocusGained(evt);
            }
        });

        providerURLTextField.setText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.providerURLTextField.text")); // NOI18N
        providerURLTextField.setToolTipText(mBundle.getString("DESC_Attribute_providerURL"));
        providerURLTextField.setName("providerURLTextField"); // NOI18N
        providerURLTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                providerURLTextFieldFocusGained(evt);
            }
        });

        initialContextFactoryTextField.setText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.initialContextFactoryTextField.text")); // NOI18N
        initialContextFactoryTextField.setToolTipText(mBundle.getString("DESC_Attribute_initialContextFactory"));
        initialContextFactoryTextField.setName("initialContextFactoryTextField"); // NOI18N
        initialContextFactoryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initialContextFactoryTextFieldActionPerformed(evt);
            }
        });
        initialContextFactoryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                initialContextFactoryTextFieldFocusGained(evt);
            }
        });

        connectionFactoryNameTextField.setText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.connectionFactoryNameTextField.text")); // NOI18N
        connectionFactoryNameTextField.setToolTipText(mBundle.getString("DESC_Attribute_connectionFactoryName"));
        connectionFactoryNameTextField.setName("connectionFactoryNameTextField"); // NOI18N
        connectionFactoryNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                connectionFactoryNameTextFieldFocusGained(evt);
            }
        });

        jSeparator1.setName("jSeparator1"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jndiSectionPanelLayout = new org.jdesktop.layout.GroupLayout(jndiSectionPanel);
        jndiSectionPanel.setLayout(jndiSectionPanelLayout);
        jndiSectionPanelLayout.setHorizontalGroup(
            jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jndiSectionPanelLayout.createSequentialGroup()
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jndiSectionPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jndiLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jndiSectionPanelLayout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(connectionFactoryNameLab)
                            .add(securityCredentialLab)
                            .add(initialContextFactoryLab)
                            .add(providerURLLab)
                            .add(securityPrincipalLab)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jndiSectionPanelLayout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, securityPrincipalTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, providerURLTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                                    .add(securityCrendentialsTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                                    .add(jButton1)))
                            .add(initialContextFactoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                            .add(connectionFactoryNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jndiSectionPanelLayout.setVerticalGroup(
            jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jndiSectionPanelLayout.createSequentialGroup()
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jndiSectionPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jndiLabel))
                    .add(jndiSectionPanelLayout.createSequentialGroup()
                        .add(18, 18, 18)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(connectionFactoryNameLab)
                    .add(connectionFactoryNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(initialContextFactoryLab)
                    .add(initialContextFactoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(providerURLTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(providerURLLab))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(securityPrincipalTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(securityPrincipalLab))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(securityCredentialLab)
                    .add(securityCrendentialsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jButton1))
                .addContainerGap(121, Short.MAX_VALUE))
        );

        topicSectionPanel.setMinimumSize(new java.awt.Dimension(279, 120));
        topicSectionPanel.setName("topicSectionPanel"); // NOI18N
        topicSectionPanel.setPreferredSize(new java.awt.Dimension(375, 230));
        topicSectionPanel.setLayout(new java.awt.GridBagLayout());

        operationLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(operationLab, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.operationLab.text")); // NOI18N
        operationLab.setName("operationLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        topicSectionPanel.add(operationLab, gridBagConstraints);

        jSeparator6.setName("jSeparator6"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 110, 0, 0);
        topicSectionPanel.add(jSeparator6, gridBagConstraints);

        topicPanel1.setName("topicPanel1"); // NOI18N
        topicPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(subscriptionLabel, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.subscriptionLabel.text")); // NOI18N
        subscriptionLabel.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.subscriptionLabel.toolTipText")); // NOI18N
        subscriptionLabel.setName("subscriptionLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 5);
        topicPanel1.add(subscriptionLabel, gridBagConstraints);

        durableBtnGrp.add(durableRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(durableRadioButton, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.durableRadioButton.text")); // NOI18N
        durableRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.durableRadioButton.toolTipText")); // NOI18N
        durableRadioButton.setName("durableRadioButton"); // NOI18N
        durableRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                durableRadioButtonFocusGained(evt);
            }
        });
        durableRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                durableRadioButtondurabilityChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        topicPanel1.add(durableRadioButton, gridBagConstraints);

        durableBtnGrp.add(nondurableRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(nondurableRadioButton, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.nondurableRadioButton.text")); // NOI18N
        nondurableRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.nondurableRadioButton.toolTipText")); // NOI18N
        nondurableRadioButton.setName("nondurableRadioButton"); // NOI18N
        nondurableRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nondurableRadioButtonFocusGained(evt);
            }
        });
        nondurableRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                nondurableRadioButtondurabilityChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        topicPanel1.add(nondurableRadioButton, gridBagConstraints);

        subscriptionNameTextField.setText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.subscriptionNameTextField.text")); // NOI18N
        subscriptionNameTextField.setName("subscriptionNameTextField"); // NOI18N
        subscriptionNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                subscriptionNameTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        topicPanel1.add(subscriptionNameTextField, gridBagConstraints);

        clientIdLabel.setLabelFor(clientIdTextField);
        org.openide.awt.Mnemonics.setLocalizedText(clientIdLabel, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.clientIdLabel.text")); // NOI18N
        clientIdLabel.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.clientIdLabel.toolTipText")); // NOI18N
        clientIdLabel.setName("clientIdLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 5);
        topicPanel1.add(clientIdLabel, gridBagConstraints);

        clientIdTextField.setText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.clientIdTextField.text")); // NOI18N
        clientIdTextField.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.clientIdTextField.toolTipText")); // NOI18N
        clientIdTextField.setName("clientIdTextField"); // NOI18N
        clientIdTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                clientIdTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        topicPanel1.add(clientIdTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        topicSectionPanel.add(topicPanel1, gridBagConstraints);

        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 0.5;
        topicSectionPanel.add(jPanel1, gridBagConstraints);

        portBindingPanel.setName("portBindingPanel"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel26, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.jLabel26.text")); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(portTypeLabel, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.portTypeLabel.text")); // NOI18N
        portTypeLabel.setName("portTypeLabel"); // NOI18N

        operationNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        operationNameComboBox.setName("operationNameComboBox"); // NOI18N
        operationNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                operationNameComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel42, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.jLabel42.text")); // NOI18N
        jLabel42.setName("jLabel42"); // NOI18N

        bindingNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bindingNameComboBox.setName("bindingNameComboBox"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(operationNameLabel, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.operationNameLabel.text")); // NOI18N
        operationNameLabel.setName("operationNameLabel"); // NOI18N

        servicePortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        servicePortComboBox.setName("servicePortComboBox"); // NOI18N
        servicePortComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                servicePortComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bindingNameLabel, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.bindingNameLabel.text")); // NOI18N
        bindingNameLabel.setName("bindingNameLabel"); // NOI18N

        portTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        portTypeComboBox.setName("portTypeComboBox"); // NOI18N

        jSeparator2.setName("jSeparator2"); // NOI18N

        org.jdesktop.layout.GroupLayout portBindingPanelLayout = new org.jdesktop.layout.GroupLayout(portBindingPanel);
        portBindingPanel.setLayout(portBindingPanelLayout);
        portBindingPanelLayout.setHorizontalGroup(
            portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 417, Short.MAX_VALUE)
            .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(portBindingPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(portBindingPanelLayout.createSequentialGroup()
                            .add(10, 10, 10)
                            .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jLabel26)
                                .add(portTypeLabel)
                                .add(operationNameLabel)
                                .add(bindingNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(servicePortComboBox, 0, 0, Short.MAX_VALUE)
                                .add(portBindingPanelLayout.createSequentialGroup()
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, portTypeComboBox, 0, 0, Short.MAX_VALUE)
                                        .add(operationNameComboBox, 0, 0, Short.MAX_VALUE)))
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, bindingNameComboBox, 0, 0, Short.MAX_VALUE)))
                        .add(portBindingPanelLayout.createSequentialGroup()
                            .add(jLabel42)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)))
                    .addContainerGap()))
        );
        portBindingPanelLayout.setVerticalGroup(
            portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 152, Short.MAX_VALUE)
            .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(portBindingPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(jLabel42)
                        .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel26)
                        .add(servicePortComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(bindingNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(bindingNameLabel))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(portTypeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(portTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(operationNameLabel)
                        .add(operationNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jndiBtn, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.jndiBtn.text_1")); // NOI18N
        jndiBtn.setName("jndiBtn"); // NOI18N

        setName("Form"); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        connectionPanel.setName("connectionPanel"); // NOI18N
        connectionPanel.setLayout(new java.awt.GridBagLayout());

        connectionSectionLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(connectionSectionLabel, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.connectionSectionLabel.text_1")); // NOI18N
        connectionSectionLabel.setName("connectionSectionLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        connectionPanel.add(connectionSectionLabel, gridBagConstraints);

        connectionURLLabel.setLabelFor(connectionURLTextField);
        org.openide.awt.Mnemonics.setLocalizedText(connectionURLLabel, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.connectionURLLabel.text_1")); // NOI18N
        connectionURLLabel.setName("connectionURLLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 10);
        connectionPanel.add(connectionURLLabel, gridBagConstraints);

        usernameLab.setLabelFor(userNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(usernameLab, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.usernameLab.text_1")); // NOI18N
        usernameLab.setName("usernameLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        connectionPanel.add(usernameLab, gridBagConstraints);

        passwordLab.setLabelFor(passwordTextField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLab, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.passwordLab.text_1")); // NOI18N
        passwordLab.setName("passwordLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        connectionPanel.add(passwordLab, gridBagConstraints);

        passwordTextField.setText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.passwordTextField.text_1")); // NOI18N
        passwordTextField.setToolTipText( mBundle.getString("DESC_Attribute_password"));
        passwordTextField.setName("passwordTextField"); // NOI18N
        passwordTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwordTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        connectionPanel.add(passwordTextField, gridBagConstraints);

        userNameTextField.setText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.userNameTextField.text_1")); // NOI18N
        userNameTextField.setToolTipText( mBundle.getString("DESC_Attribute_username"));
        userNameTextField.setName("userNameTextField"); // NOI18N
        userNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                userNameTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        connectionPanel.add(userNameTextField, gridBagConstraints);

        connectionURLTextField.setText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.connectionURLTextField.text_1")); // NOI18N
        connectionURLTextField.setToolTipText(mBundle.getString("DESC_Attribute_connectionURL"));
        connectionURLTextField.setName("connectionURLTextField"); // NOI18N
        connectionURLTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionURLTextFieldActionPerformed(evt);
            }
        });
        connectionURLTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                connectionURLTextFieldFocusGained(evt);
            }
        });
        connectionURLTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                connectionURLTextFieldKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                connectionURLTextFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        connectionPanel.add(connectionURLTextField, gridBagConstraints);

        jSeparator3.setName("jSeparator3"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 110, 0, 0);
        connectionPanel.add(jSeparator3, gridBagConstraints);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/jms/resources/service_composition_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.jLabel2.text")); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.jLabel2.toolTipText")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        connectionPanel.add(jLabel2, gridBagConstraints);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/jms/resources/service_composition_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.jLabel3.text")); // NOI18N
        jLabel3.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.jLabel3.toolTipText")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        connectionPanel.add(jLabel3, gridBagConstraints);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/jms/resources/service_composition_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.jLabel4.text")); // NOI18N
        jLabel4.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConnectionPanel.class, "JMSConnectionPanel.jLabel4.toolTipText")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        connectionPanel.add(jLabel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(connectionPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void initialContextFactoryTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initialContextFactoryTextFieldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_initialContextFactoryTextFieldActionPerformed

private void connectionURLTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_connectionURLTextFieldKeyTyped
    // TODO add your handling code here:
    //updateJNDISection();
}//GEN-LAST:event_connectionURLTextFieldKeyTyped

private void connectionURLTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_connectionURLTextFieldKeyPressed
    // TODO add your handling code here:
    //updateJNDISection();
}//GEN-LAST:event_connectionURLTextFieldKeyPressed

private void connectionURLTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionURLTextFieldActionPerformed
    // TODO add your handling code here:
    updateJNDISection();
}//GEN-LAST:event_connectionURLTextFieldActionPerformed

private void operationNameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_operationNameComboBoxItemStateChanged
    // TODO add your handling code here:
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        String selectedOperation = (String) operationNameComboBox.
                getSelectedItem();
        if (mComponent != null)  {
            Binding binding = null;
            if (mComponent instanceof JMSAddress) {
                Port port = (Port) ((JMSAddress) mComponent).getParent();
                binding = port.getBinding().get();

            } else if (mComponent instanceof JMSBinding) {
                binding = (Binding) ((JMSBinding) mComponent).getParent();
            } else if (mComponent instanceof Port) {
                if (((Port) mComponent).getBinding() != null) {
                    binding = ((Port) mComponent).getBinding().get();
                }
            }
            if (binding != null) {
                JMSMessage inputMessage = getInputJMSMessage(binding,
                        selectedOperation);
                updateInputMessageView(inputMessage);

            }
        }
    }
}//GEN-LAST:event_operationNameComboBoxItemStateChanged

private void servicePortComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_servicePortComboBoxItemStateChanged
    // TODO add your handling code here:
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        Object selObj = servicePortComboBox.getSelectedItem();
        String selBindingName = "";
        if (bindingNameComboBox.getSelectedItem() != null) {
            selBindingName = bindingNameComboBox.getSelectedItem().toString();
        }
        if ((selObj != null) && (mComponent != null)) {
            Port selServicePort = (Port) selObj;
            if (selServicePort.getBinding() != null) {
                Binding binding = selServicePort.getBinding().get();

                if ((binding != null) && (binding.getName().
                        equals(selBindingName))) {
                    Iterator<JMSAddress> jmsAddresses = selServicePort.
                            getExtensibilityElements(JMSAddress.class).
                            iterator();
                    // 1 fileaddress for 1 binding
                    while (jmsAddresses.hasNext()) {
                        updateServiceView(jmsAddresses.next());
                        break;
                    }
                }
            }
        }
    }
}//GEN-LAST:event_servicePortComboBoxItemStateChanged

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    // TODO add your handling code here:
    showJNDIEnvironment();
}//GEN-LAST:event_jButton1ActionPerformed

private void durableRadioButtondurabilityChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_durableRadioButtondurabilityChanged
    if (durableRadioButton.isEnabled() && durableRadioButton.isSelected()) {
        subscriptionNameTextField.setEnabled(true);
    }
    if (nondurableRadioButton.isEnabled() && nondurableRadioButton.isSelected()) {
        subscriptionNameTextField.setEnabled(false);
    }
}//GEN-LAST:event_durableRadioButtondurabilityChanged

private void nondurableRadioButtondurabilityChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_nondurableRadioButtondurabilityChanged
    if (durableRadioButton.isEnabled() && durableRadioButton.isSelected()) {
        subscriptionNameTextField.setEnabled(true);
    }
    if (nondurableRadioButton.isEnabled() && nondurableRadioButton.isSelected()) {
        subscriptionNameTextField.setEnabled(false);
    }
}//GEN-LAST:event_nondurableRadioButtondurabilityChanged

private void connectionURLTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_connectionURLTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_connectionURLTextFieldFocusGained

private void userNameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userNameTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_userNameTextFieldFocusGained

private void passwordTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwordTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_passwordTextFieldFocusGained

private void connectionFactoryNameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_connectionFactoryNameTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_connectionFactoryNameTextFieldFocusGained

private void initialContextFactoryTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_initialContextFactoryTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_initialContextFactoryTextFieldFocusGained

private void providerURLTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_providerURLTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_providerURLTextFieldFocusGained

private void securityPrincipalTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_securityPrincipalTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_securityPrincipalTextFieldFocusGained

private void securityCrendentialsTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_securityCrendentialsTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_securityCrendentialsTextFieldFocusGained

private void clientIdTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_clientIdTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_clientIdTextFieldFocusGained

private void durableRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_durableRadioButtonFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_durableRadioButtonFocusGained

private void subscriptionNameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_subscriptionNameTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_subscriptionNameTextFieldFocusGained

private void nondurableRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nondurableRadioButtonFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_nondurableRadioButtonFocusGained


    private void initListeners() {
        if (mItemListener == null)  {
            mItemListener = new MyItemListener();
        }

        if (mActionListener == null) {
            mActionListener = new MyActionListener();
        }
        
        if (mDocumentListener == null) {
            mDocumentListener = new MyDocumentListener();
        }
        
        jndiBtn.addActionListener(mActionListener);
        userNameTextField.getDocument().addDocumentListener(mDocumentListener);
        passwordTextField.getDocument().addDocumentListener(mDocumentListener);
    }

    private void resetView() {
        jndiBtn.removeActionListener(mActionListener);
        userNameTextField.getDocument().removeDocumentListener(mDocumentListener);
        passwordTextField.getDocument().removeDocumentListener(mDocumentListener);
     
        servicePortComboBox.setEnabled(false);
        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
        portTypeComboBox.removeAllItems();
        operationNameComboBox.removeAllItems();
    }

    /**
     * Populate the view with the given the model component
     * @param qName
     * @param component
     */
    public void populateView(QName qName, WSDLComponent component) {
        cleanUp();
        mQName = qName;
        mComponent = component;
        resetView();
        populateView(mComponent);        
        initListeners();        
    }

    public void setDescriptionPanel(DescriptionPanel descPanel) {
        mDescPanel = descPanel;
    }
    
    private void populateView(WSDLComponent component) {
        if (component != null) {
            if (component instanceof JMSAddress) {
                populateJMSAddress((JMSAddress) component);
            } else if (component instanceof JMSBinding) {
                populateJMSBinding((JMSBinding) component, null);
            } else if (component instanceof Port) {
                Collection<JMSAddress> address = ((Port) component).
                        getExtensibilityElements(JMSAddress.class);
                if (!address.isEmpty()) {
                    populateJMSAddress(address.iterator().next());
                }
            } else if (component instanceof JMSMessage) {
                Object obj = ((JMSMessage)component).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                } else if (obj instanceof BindingOutput) {
                    BindingOperation parentOp = (BindingOperation)
                            ((BindingOutput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                }
                if (parentBinding != null) {
                    Collection<JMSBinding> bindings = parentBinding.
                            getExtensibilityElements(JMSBinding.class);
                    if (!bindings.isEmpty()) {
                        populateJMSBinding(bindings.iterator().next(), null);
                        bindingNameComboBox.
                                setSelectedItem(parentBinding.getName());
                    }
                }

            } else if (component instanceof JMSOperation) {
                Object obj = ((JMSOperation)component).getParent();
                if (obj instanceof BindingOperation) {
                    Binding parentBinding = (Binding)
                            ((BindingOperation)obj).getParent();
                    Collection<JMSBinding> bindings = parentBinding.
                            getExtensibilityElements(JMSBinding.class);
                    if (!bindings.isEmpty()) {
                        populateJMSBinding(bindings.iterator().next(), null);
                        bindingNameComboBox.setSelectedItem(parentBinding.getName());
                    }
                }
            }
        }
    }

    private void populateJMSAddress(JMSAddress jmsAddress) {
        updateServiceView(jmsAddress);
        Port port = (Port) jmsAddress.getParent();
        Binding binding = port.getBinding().get();
        Collection<JMSBinding> bindings = binding.
                getExtensibilityElements(JMSBinding.class);
        if (!bindings.isEmpty()) {
            populateJMSBinding(bindings.iterator().next(), jmsAddress);
        }
        bindingNameComboBox.setSelectedItem(binding.getName());
        portTypeComboBox.addItem(port.getName());
    }

    private void populateJMSBinding(JMSBinding jmsBinding,
            JMSAddress jmsAddress) {
        if (jmsAddress == null) {
            servicePortComboBox.setEnabled(true);
            jmsAddress = getJMSAddress(jmsBinding);
        }
        if (jmsAddress == null) {
            return;
        }
        Port port = (Port) jmsAddress.getParent();

        // need to populate with all service ports that uses this binding
        populateListOfPorts(jmsBinding);
        servicePortComboBox.setSelectedItem(port);

        // from Binding, need to allow changing of Port
        bindingNameComboBox.setEditable(false);
        bindingNameComboBox.setEnabled(false);

        updateServiceView(jmsAddress);
        if (jmsBinding != null) {
            populateListOfBindings(jmsBinding);
            populateListOfPortTypes(jmsBinding);
            Binding binding = (Binding) jmsBinding.getParent();
            bindingNameComboBox.setSelectedItem(binding.getName());
            NamedComponentReference<PortType> pType = binding.getType();
            PortType portType = pType.get();
            portTypeComboBox.addItem(portType.getName());

            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            populateOperationBox(bindingOperations);

            operationNameComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent evt) {
                    // based on selected operation, populate messages
                    operationNameComboBoxItemStateChanged(evt);
                }
            });
            populatePartBoxes(binding, bindingOperations);
            // select the 1st item since this is not a configurable param
            operationNameComboBox.setSelectedIndex(0);
            if (operationNameComboBox.getItemCount() == 1) {
                // need to implicitly call update on messages because above
                // listener will not change selection if only 1 item
                if (binding != null) {
                    JMSMessage inputMessage = getInputJMSMessage(binding,
                            operationNameComboBox.getSelectedItem().toString());
                    updateInputMessageView(inputMessage);

                }
            }
            updateGeneralView(jmsAddress, bindingOperations);
        }
    }

    private void updateInputMessageView(JMSMessage inputJMSMessage) {
        if (inputJMSMessage != null) {
//TODO
//            inputTypeComboBox.setSelectedItem(inputJMSMessage.getMessageType());
//            inputTextComboBox.setSelectedItem(inputJMSMessage.getTextPart());

            // TODO
//            if (inputJMSMessage.getUse() != null) {
//                if (inputJMSMessage.getUse().equals(JMSConstants.ENCODED)) {
//                    inputEncodingRBtn.setSelected(true);
//                } else {
//                    inputEncodingRBtn.setSelected(false);
//                }
//            } else {
//                inputEncodingRBtn.setSelected(false);
//            }
//            inputEncodingStyleTextField.setText(
//                    inputJMSMessage.getJMSEncodingStyle());
        }
    }

    private void populateOperationBox(Collection bindingOps) {
        Iterator iter = bindingOps.iterator();
        while (iter.hasNext()) {
            BindingOperation bop = (BindingOperation) iter.next();
            operationNameComboBox.addItem(bop.getName());
        }
    }

    JMSAddress getJMSAddress(JMSBinding jmsBinding) {
        JMSAddress jmsAddress = null;
        if ((jmsBinding != null) && (jmsBinding.getParent() != null)) {
            Binding parentBinding = (Binding) jmsBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if(port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                        if ((binding != null) && (binding.getName().
                                equals(bindingName))) {
                            Iterator<JMSAddress> jmsAddresses = port.
                                    getExtensibilityElements(JMSAddress.class).
                                    iterator();
                            // 1 jmsaddress for 1 binding
                            while (jmsAddresses.hasNext()) {
                                return jmsAddresses.next();
                            }
                        }
                    }
                }
            }
        }
        return jmsAddress;
    }

    private JMSMessage getInputJMSMessage(Binding binding,
            String selectedOperation) {
        JMSMessage inputJMSMessage = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    BindingInput bi = bop.getBindingInput();
                    List<JMSMessage> inputJMSMessages =
                            bi.getExtensibilityElements(JMSMessage.class);
                    if (inputJMSMessages.size() > 0) {
                        inputJMSMessage = inputJMSMessages.get(0);
                        break;
                    }
                }
            }
        }
        return inputJMSMessage;
    }

    private JMSMessage getOutputJMSMessage(Binding binding,
            String selectedOperation) {
        JMSMessage outputJMSMessage = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    BindingOutput bo = bop.getBindingOutput();
                    List<JMSMessage> outputJMSMessages =
                            bo.getExtensibilityElements(JMSMessage.class);
                    if (outputJMSMessages.size() > 0) {
                        outputJMSMessage = outputJMSMessages.get(0);
                        break;
                    }
                }
            }
        }
        return outputJMSMessage;
    }

    private void updateGeneralView(JMSAddress jmsAddress,
            Collection<BindingOperation> bindingOperations) {
        if (jmsAddress != null) {
        }
        if (bindingOperations != null) {
            for (BindingOperation bop : bindingOperations) {
                List<JMSOperation> jmsOpsList = bop.
                        getExtensibilityElements(JMSOperation.class);
                Iterator<JMSOperation> jmsOps =
                        jmsOpsList.iterator();
                // there should only be one jms:operation for the binding op
                if (jmsOpsList.size() > 0) {
                    JMSOperation jmsOp = jmsOps.next();
                    if (jmsOp != null) {
                        // taken care in destination panel now
                    }
                }
            }
        }
    }

    private void populatePartBoxes(Binding binding,
            Collection<BindingOperation> bindingOperations) {
        if (bindingOperations != null) {
            Collection inputTextParts = new ArrayList();
            Collection outputTextParts = new ArrayList();
            for (BindingOperation bop : bindingOperations) {

                // get the input text part
                inputTextParts = getInputParts(binding, bop.getName());

//                // get the output text part
//                outputTextParts = getOutputParts(binding, bop.getName());

                // populate text part
                populateInputPartComboBox(inputTextParts);

//                // populate text part
//                populateOutputPartComboBox(outputTextParts);

                BindingInput bi = bop.getBindingInput();
                List<JMSMessage> inputJMSMessages =
                        bi.getExtensibilityElements(JMSMessage.class);
                if ((inputJMSMessages != null) &&
                        (inputJMSMessages.size() > 0)) {
                    JMSMessage inputJMSMessage = inputJMSMessages.get(0);
// TODO
//                    // get input message type
//                    inputTypeComboBox.setSelectedItem(inputJMSMessage.
//                            getMessageType());
//                    // get input text
//                    inputTextComboBox.setSelectedItem(inputJMSMessage.
//                            getTextPart());
                }

//                BindingOutput bo = bop.getBindingOutput();
//                List<JMSMessage> outputJMSMessages =
//                        bo.getExtensibilityElements(JMSMessage.class);
//                if ((outputJMSMessages != null) &&
//                        (outputJMSMessages.size() > 0)) {
//                    JMSMessage outputJMSMessage = outputJMSMessages.get(0);
//// TODO
////                    // get output message type
////                    outputTypeComboBox.setSelectedItem(outputJMSMessage.
////                            getMessageType());
////
////                    // get output text
////                    outputTextComboBox.setSelectedItem(outputJMSMessage.
////                            getTextPart());
//                }
            }
        }
    }

    private void updateServiceView(JMSAddress jmsAddress) {
        if (jmsAddress != null) {
            connectionURLTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_CONNECTION_URL));
            updateJNDISection();
            userNameTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_USERNAME));
            passwordTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_PASSWORD));
            connectionFactoryNameTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_JNDI_CONNECTION_FACTORY_NAME));
            initialContextFactoryTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_JNDI_INITIAL_CONTEXT_FACTORY));
            providerURLTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_JNDI_PROVIDER_URL));
            securityPrincipalTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_JNDI_SECURITY_PRINCIPAL));
            securityCrendentialsTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_JNDI_SECURITY_CRDENTIALS));
        }
    }
    private Collection getInputParts(Binding binding, String opName) {
        Collection<String> inputParts = new ArrayList<String>();
        if (binding != null) {
            NamedComponentReference<PortType> pType = binding.getType();
            PortType type = pType.get();
            Collection ops = type.getOperations();
            Iterator iter = ops.iterator();
            inputParts.add(JMSConstants.NOT_SET);
            while(iter.hasNext()) {
                Operation op = (Operation) iter.next();
                if ((op != null) && (op.getName().equals(opName))) {
                    Input input = op.getInput();
                    NamedComponentReference<Message> messageIn = input.getMessage();
                    Message msgIn = messageIn.get();
                    if (msgIn != null) {
                        Collection parts = msgIn.getParts();
                        Iterator partIter = parts.iterator();
                        while (partIter.hasNext()) {
                            Part part = (Part) partIter.next();
                            inputParts.add(part.getName());
                        }
                    }
                }
            }
        }
        return inputParts;
    }

    private Collection getOutputParts(Binding binding, String opName) {
        Collection<String> outputParts = new ArrayList<String>();
        if (binding != null) {
            NamedComponentReference<PortType> pType = binding.getType();
            PortType type = pType.get();
            Collection ops = type.getOperations();
            Iterator iter = ops.iterator();
            outputParts.add(JMSConstants.NOT_SET);
            while(iter.hasNext()) {
                Operation op = (Operation) iter.next();
                if ((op != null) && (op.getName().equals(opName))) {
                    Output output = op.getOutput();
                    NamedComponentReference<Message> messageOut = output.getMessage();
                    Message msgOut = messageOut.get();
                    if (msgOut != null) {
                        Collection parts = msgOut.getParts();
                        Iterator partIter = parts.iterator();
                        while (partIter.hasNext()) {
                            Part part = (Part) partIter.next();
                            outputParts.add(part.getName());
                        }
                    }
                }
            }
        }
        return outputParts;
    }

    private void populateInputPartComboBox(Collection textItems) {
        if ((textItems != null) && (textItems.size() > 0)) {
            Iterator iter = textItems.iterator();
            while(iter.hasNext()) {
                String partName = (String) iter.next();
// TODO
//                inputTextComboBox.addItem(partName);
 
            }
        }
    }

    private void populateOutputPartComboBox(Collection textItems) {
        if ((textItems != null) && (textItems.size() > 0)) {
            Iterator iter = textItems.iterator();
            while(iter.hasNext()) {
                String partName = (String) iter.next();
// TODO                
//                outputTextComboBox.addItem(partName);

            }
        }
    }

    private void populateListOfPortTypes(JMSBinding jmsBinding) {
        if ((jmsBinding != null) && (jmsBinding.getParent() != null)) {
            Binding parentBinding = (Binding) jmsBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<PortType> portTypes = defs.getPortTypes().iterator();
            List<PortType> filePortTypes = null;
            while (portTypes.hasNext()) {
                PortType portType = portTypes.next();
                portTypeComboBox.addItem(portType.getName());
            }
        }
    }

    private void populateListOfPorts(JMSBinding jmsBinding) {
            Vector<Port> portV = new Vector<Port>();

        if ((jmsBinding != null) && (jmsBinding.getParent() != null)) {
            Binding parentBinding = (Binding) jmsBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            boolean found = false;
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if(port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                        if ((binding != null) && (binding.getName().
                                equals(bindingName))) {
                            portV.add(port);
                        }
                    }
                }
            }
        }
        servicePortComboBox.setModel(new DefaultComboBoxModel(portV));
        servicePortComboBox.setRenderer(new PortCellRenderer());

    }

   private void populateListOfBindings(JMSBinding jmsBinding) {
        if ((jmsBinding != null) && (jmsBinding.getParent() != null)) {
            Binding parentBinding = (Binding) jmsBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            List<JMSBinding> jmsBindings = null;

            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null
                        || binding.getType().get() == null) {
                    continue;
                }

                jmsBindings = binding.
                        getExtensibilityElements(JMSBinding.class);
                if (jmsBindings != null) {
                    Iterator iter = jmsBindings.iterator();
                    while (iter.hasNext()) {
                        JMSBinding b = (JMSBinding) iter.next();
                        Binding fBinding = (Binding) b.getParent();
                        bindingNameComboBox.addItem(fBinding.getName());
                    }
                }
            }
        }
    }

    JMSAddress getJMSAddressPerSelectedPort() {
        JMSAddress address = null;
        Port selectedServicePort = (Port) servicePortComboBox.getSelectedItem();
        if (selectedServicePort != null) {
            Binding binding = selectedServicePort.getBinding().get();
            String selBindingName = bindingNameComboBox.
                    getSelectedItem().toString();
            if ((binding != null) && (binding.getName().
                    equals(selBindingName))) {
                Iterator<JMSAddress> jmsAddresses = selectedServicePort.
                        getExtensibilityElements(JMSAddress.class).
                        iterator();
                // 1 fileaddress for 1 binding
                while (jmsAddresses.hasNext()) {
                    return jmsAddresses.next();
                }
            }
        }
        return address;
    }

    private void cleanUp() {
        // clean up listeners TODO
        // null out data TODO
    }

    private void updateDescriptionArea(FocusEvent evt) {
        if (mDescPanel != null) {
            mDescPanel.setText("");
        }

        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == connectionURLTextField) {
            desc = new String[]{"Connection URL\n\n",
                   connectionURLTextField.getToolTipText()}; 
        } else if (evt.getSource() == userNameTextField) {
            desc = new String[]{"User Name\n\n",
                   userNameTextField.getToolTipText()}; 
        } else if (evt.getSource() == passwordTextField) {
            desc = new String[]{"Password\n\n",
                   passwordTextField.getToolTipText()}; 
        } else if (evt.getSource() == connectionFactoryNameTextField) {
            desc = new String[]{"Connection Factory Name\n\n",
                   connectionFactoryNameTextField.getToolTipText()}; 
        } else if (evt.getSource() == initialContextFactoryTextField) {
            desc = new String[]{"Initial Context Factory\n\n",
                   initialContextFactoryTextField.getToolTipText()}; 
        } else if (evt.getSource() == providerURLTextField) {
            desc = new String[]{"Provider URL\n\n",
                   providerURLTextField.getToolTipText()}; 
        } else if (evt.getSource() == securityPrincipalTextField) {
            desc = new String[]{"Security Principal\n\n",
                   securityPrincipalTextField.getToolTipText()}; 
        } else if (evt.getSource() == securityCrendentialsTextField) {
            desc = new String[]{"Security Credentials\n\n",
                   securityCrendentialsTextField.getToolTipText()}; 
        } else if (evt.getSource() == subscriptionNameTextField) {
            desc = new String[]{"Subcription Name\n\n",
                   subscriptionNameTextField.getToolTipText()}; 
        } else if (evt.getSource() == durableRadioButton) {
            desc = new String[]{"Subscription Durability\n\n",
                   durableRadioButton.getToolTipText()}; 
        } else if (evt.getSource() == nondurableRadioButton) {
            desc = new String[]{"Subscription Durability\n\n",
                   nondurableRadioButton.getToolTipText()}; 
        } else if (evt.getSource() == clientIdTextField) {
            desc = new String[]{"Client ID\n\n",
                   clientIdTextField.getToolTipText()}; 
        }    
        if (desc != null) {
            if (mDescPanel != null) {
                mDescPanel.setText(desc[0], desc[1]);
            }
            return;
        }
    }    
    
    public FileError validateMe() {  
        return validateMe(false);
    }
    
    public FileError validateMe(boolean fireEvent) {  
        // validate user/password
        FileError fileError = new FileError();
        if ((getUserName() != null) && (getPassword() == null)) {
            fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT);
            fileError.setErrorMessage(NbBundle.getMessage(JMSConnectionPanel.class, 
                    "JMSConnectionPanel.PasswordEmpty"));
        }
        
        if (fireEvent) {
            ErrorPropagator.doFirePropertyChange(fileError.getErrorMode(), null,
                    fileError.getErrorMessage(), this);
        }      
        return fileError;        
    }
    
    protected boolean validateContent() {
        ValidationResult results = new JMSComponentValidator().
                validate(mComponent.getModel(), null, ValidationType.COMPLETE);
        Collection<ResultItem> resultItems = results.getValidationResult();
        ResultItem firstResult = null;
        String type = ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT;
        boolean result = true;
        if (resultItems != null && !resultItems.isEmpty()) {
            for (ResultItem item : resultItems) {
                if (item.getType() == ResultType.ERROR) {
                    firstResult = item;
                    type = ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_ERROR_EVT;
                    result = false;
                    break;
                } else if (firstResult == null) {
                    firstResult = item;
                    type = ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_WARNING_EVT;
                }
            }
        }
        if (firstResult != null) {
            firePropertyChange(type, null, firstResult.getDescription());
            return result;
        } else {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_CLEAR_MESSAGES_EVT, null, null);
            return true;
        }
    }

    private void updateJNDISection() {
        // enable JNDI Section only if ConnectionURL starts out with jndi
        String val = getConnectionURL();
        if ((val != null) && (val.toLowerCase().startsWith("jndi://"))) {
            jndiBtn.setEnabled(true);
        } else {
            jndiBtn.setEnabled(false);
        }
    }
    
    private void updateUserPasswordSection() {
        // disable username and password if connection url starts with lookup://
        String val = getConnectionURL();
        if ((val != null) && (val.toLowerCase().startsWith("lookup://"))) {
            userNameTextField.setEnabled(false);
            passwordTextField.setEnabled(false);
        } else {
            userNameTextField.setEnabled(true);
            passwordTextField.setEnabled(true);
        }
    }    
    
    public class PortCellRenderer extends JLabel
            implements javax.swing.ListCellRenderer {

        public PortCellRenderer() {
            super();
            setOpaque(true);
        }

        public java.awt.Component getListCellRendererComponent(javax.swing.JList list,
                Object value, int index, boolean isSelected,
                boolean isFocused) {
            if ((value != null) && (value instanceof Port)) {
                setText(((Port) value).getName());
                setBackground(isSelected ?
                    list.getSelectionBackground() : list.getBackground());
                setForeground(isSelected ?
                    list.getSelectionForeground() : list.getForeground());
            }
            return this;
        }
    }

    /**
     * Trims input and returns null, if blank.
     *
     * @param text
     * @return trimmed text, if blank returns null.
     */
    private String trimTextFieldInput(String text) {
        if (text == null) {
            return text;
        }
        String trimmedText = text.trim();
        if (trimmedText.length() == 0) {
            return null;
        }
        return text.trim();
    }

    private void showTopicDetails() {
        if (mDetailsDlg == null) {
            mDetailsDlgDesc = new DialogDescriptor(
                    topicSectionPanel,
                    NbBundle.getMessage(JMSConnectionPanel.class,
                    "JMSBindingConfigurationPanel.TopicsOnlyTitle"),
                    true, null);
            mDetailsDlg = DialogDisplayer.getDefault().
                    createDialog(mDetailsDlgDesc);
        }
        mDetailsDlg.setSize(topicSectionPanel.getPreferredSize());
        mDetailsDlg.setVisible(true);
        if (mDetailsDlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
        } else {
        }
        mDetailsDlg.setVisible(false);
    }

    private void showJNDIDetails() {
        if (mDetailsJNDIDlg == null) {
            mDetailsJNDIDlgDesc = new DialogDescriptor(
                    jndiSectionPanel,
                    NbBundle.getMessage(JMSConnectionPanel.class,
                    "JMSBindingConfigurationPanel.JNDIInfoTitle"),
                    true, null);
            mDetailsJNDIDlg = DialogDisplayer.getDefault().
                    createDialog(mDetailsJNDIDlgDesc);
        }
        mDetailsJNDIDlg.setSize(jndiSectionPanel.getPreferredSize());
        mDetailsJNDIDlg.setVisible(true);
        if (mDetailsJNDIDlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
        } else {
        }
        mDetailsJNDIDlg.setVisible(false);
    }

    private void showJNDIEnvironment() {
        if (mJNDIEnvPanel == null) {
            mJNDIEnvPanel = new JNDIEnvironmentPanel(mQName, mComponent);
        }
        if (mJNDIEnvDlgDesc == null) {
            mJNDIEnvDlgDesc = new DialogDescriptor(
                    mJNDIEnvPanel,
                    NbBundle.getMessage(JMSConnectionPanel.class,
                    "JMSBindingConfigurationPanel.JNDIEnvTitle"),
                    true, null);
            mJNDIEnvDlg = DialogDisplayer.getDefault().
                    createDialog(mJNDIEnvDlgDesc);
        }
        mJNDIEnvDlg.setSize(mJNDIEnvPanel.getPreferredSize());
        mJNDIEnvDlg.setVisible(true);
        if (mJNDIEnvDlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
        } else {
        }
        mJNDIEnvDlg.setVisible(false);
    }

    private void handleItemStateChanged(ItemEvent evt) {

    }

    private void handleActionPerformed(ActionEvent evt) {
        if (evt.getSource() == jndiBtn) {            
            showJNDIDetails();
        }
    }

    public class MyItemListener implements ItemListener {
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            handleItemStateChanged(evt);
        }
    }

    public class MyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            handleActionPerformed(evt);
        }
    }
    
    
    public class MyDocumentListener implements DocumentListener {
	// Handle insertions into the text field
        public void insertUpdate(DocumentEvent event) {
            validateMe(true);
        }

	// Handle deletions	from the text field
        public void removeUpdate(DocumentEvent event) {
            validateMe(true);
        }

	// Handle changes to the text field
        public void changedUpdate(DocumentEvent event) {
            // empty
        }
             
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup QTopicBGroup;
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JLabel bindingNameLabel;
    private javax.swing.JLabel clientIdLabel;
    private javax.swing.JTextField clientIdTextField;
    private javax.swing.JLabel connectionFactoryNameLab;
    private javax.swing.JTextField connectionFactoryNameTextField;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JLabel connectionSectionLabel;
    private javax.swing.JLabel connectionURLLabel;
    private javax.swing.JTextField connectionURLTextField;
    private javax.swing.ButtonGroup durableBtnGrp;
    private javax.swing.JRadioButton durableRadioButton;
    private javax.swing.JLabel initialContextFactoryLab;
    private javax.swing.JTextField initialContextFactoryTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JButton jndiBtn;
    private javax.swing.JLabel jndiLabel;
    private javax.swing.JPanel jndiSectionPanel;
    private javax.swing.JRadioButton nondurableRadioButton;
    private javax.swing.JLabel operationLab;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JLabel operationNameLabel;
    private javax.swing.JLabel passwordLab;
    private javax.swing.JPasswordField passwordTextField;
    private javax.swing.JPanel portBindingPanel;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JLabel providerURLLab;
    private javax.swing.JTextField providerURLTextField;
    private javax.swing.JLabel securityCredentialLab;
    private javax.swing.JTextField securityCrendentialsTextField;
    private javax.swing.JLabel securityPrincipalLab;
    private javax.swing.JTextField securityPrincipalTextField;
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JLabel subscriptionLabel;
    private javax.swing.JTextField subscriptionNameTextField;
    private javax.swing.JPanel topicPanel1;
    private javax.swing.JPanel topicSectionPanel;
    private javax.swing.JTextField userNameTextField;
    private javax.swing.JLabel usernameLab;
    // End of variables declaration//GEN-END:variables

}
