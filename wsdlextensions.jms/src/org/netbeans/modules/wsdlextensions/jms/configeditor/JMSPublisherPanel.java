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

import java.awt.event.FocusAdapter;
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
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
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
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * JMSBindingConfigurationPanel - Panel that allows configuration of
 * properties specifically for JMS Binding component
 *
 * @author  jalmero
 */
public class JMSPublisherPanel extends javax.swing.JPanel {

    private WSDLComponent mComponent;

    /** QName **/
    private QName mQName;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.jms.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(JMSPublisherPanel.class.getName());
    
    private DescriptionPanel mDescPanel = null;

    /** Creates new form JMSBindingConfigurationPanel */
    public JMSPublisherPanel(QName qName, WSDLComponent component) {
        initComponents();
        populateView(qName, component);
        setAccessibility();
    }

    /** Creates new form JMSBindingConfigurationPanel */
    public JMSPublisherPanel() {
        this(null, null);
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
        //initListeners();
    }

    /**
     * Return the operation name
     * @return String operation name
     */
    String getOperationName() {
        if ((operationNameComboBox.getSelectedItem() != null) &&
                (!operationNameComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return operationNameComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return deliveriy mode
     * @return String delivery mode
     */
    String getDeliverMode() {
        if (persistenceRBtn.isSelected()) {
            return JMSConstants.DELIVERYMODE_PERSISTENT;
        } else {
            return JMSConstants.DELIVERYMODE_NON_PERSISTENT;
        }
    }

    /**
     * Return value for time to live
     * @return String
     */
    int getTimeToLive() {
        if (timeToLiveSpinner.getValue() != null) {
            return new Integer(timeToLiveSpinner.getValue().toString()).
                    intValue();
        } else {
            return 0;
        }
    }

    /**
     * Return priority value
     * @return
     */
    int getPriority() {
        if (prioritySpinner.getValue() != null) {
            return new Integer(prioritySpinner.getValue().toString()).
                    intValue();
        } else {
            return 0;
        }
    }

    /**
     * Return timeout value
     * @return
     */
    long getTimeout() {
        if (timeoutTextField.getText().length() == 0) {
            return 0;
        } else {
            return new Long(timeoutTextField.getText()).longValue();
        }
    }

    void showTimeOut(boolean mode) {
        timeoutTextField.setVisible(mode);
        timeoutLab.setVisible(mode);
        timeoutMSLab.setVisible(mode);
    }

    public void setDescriptionPanel(DescriptionPanel descPanel) {
         mDescPanel = descPanel;
    }
    
   /**
     * Set the operation name to be configured
     * @param opName
     */
    void setOperationName(String opName) {
        if (opName != null) {
            operationNameComboBox.setSelectedItem(opName);
        }
    }    
    
    private void setAccessibility() {     
        ((JSpinner.DefaultEditor)timeToLiveSpinner.getEditor()).getTextField().getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_timeToLive")); // NOI18N
        ((JSpinner.DefaultEditor)timeToLiveSpinner.getEditor()).getTextField().getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_timeToLive")); // NOI18N       
        timeToLiveLab.setLabelFor(((JSpinner.DefaultEditor)timeToLiveSpinner.getEditor()).getTextField());
        ((JSpinner.DefaultEditor)prioritySpinner.getEditor()).getTextField().getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_priority")); // NOI18N
        ((JSpinner.DefaultEditor)prioritySpinner.getEditor()).getTextField().getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_priority")); // NOI18N               
        priorityLab.setLabelFor(((JSpinner.DefaultEditor)prioritySpinner.getEditor()).getTextField());
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
        deliverModeBtnGrp = new javax.swing.ButtonGroup();
        producerPanel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel36 = new javax.swing.JLabel();
        priorityLab = new javax.swing.JLabel();
        timeToLiveLab = new javax.swing.JLabel();
        timeoutLab = new javax.swing.JLabel();
        timeoutTextField = new javax.swing.JTextField();
        timeToLiveSpinner = new javax.swing.JSpinner();
        persistenceRBtn = new javax.swing.JRadioButton();
        nonPersistenceRtn = new javax.swing.JRadioButton();
        prioritySpinner = new javax.swing.JSpinner();
        secondLab = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        timeoutMSLab = new javax.swing.JLabel();

        portBindingPanel.setName("portBindingPanel"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel26, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.jLabel26.text")); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(portTypeLabel, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.portTypeLabel.text")); // NOI18N
        portTypeLabel.setName("portTypeLabel"); // NOI18N

        operationNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        operationNameComboBox.setName("operationNameComboBox"); // NOI18N
        operationNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                operationNameComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel42, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.jLabel42.text")); // NOI18N
        jLabel42.setName("jLabel42"); // NOI18N

        bindingNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bindingNameComboBox.setName("bindingNameComboBox"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(operationNameLabel, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.operationNameLabel.text_1")); // NOI18N
        operationNameLabel.setName("operationNameLabel"); // NOI18N

        servicePortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        servicePortComboBox.setName("servicePortComboBox"); // NOI18N
        servicePortComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                servicePortComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bindingNameLabel, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.bindingNameLabel.text")); // NOI18N
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

        setName("Form"); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        producerPanel.setName("producerPanel"); // NOI18N
        producerPanel.setLayout(new java.awt.GridBagLayout());

        jLabel47.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel47, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.jLabel47.text")); // NOI18N
        jLabel47.setName("jLabel47"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        producerPanel.add(jLabel47, gridBagConstraints);

        jSeparator8.setName("jSeparator8"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 160, 0, 0);
        producerPanel.add(jSeparator8, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel36, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.jLabel36.text")); // NOI18N
        jLabel36.setName("jLabel36"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        producerPanel.add(jLabel36, gridBagConstraints);

        priorityLab.setLabelFor(prioritySpinner);
        org.openide.awt.Mnemonics.setLocalizedText(priorityLab, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.priorityLab.text")); // NOI18N
        priorityLab.setName("priorityLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        producerPanel.add(priorityLab, gridBagConstraints);

        timeToLiveLab.setLabelFor(timeToLiveSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(timeToLiveLab, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.timeToLiveLab.text")); // NOI18N
        timeToLiveLab.setName("timeToLiveLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        producerPanel.add(timeToLiveLab, gridBagConstraints);

        timeoutLab.setLabelFor(timeoutTextField);
        org.openide.awt.Mnemonics.setLocalizedText(timeoutLab, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.timeoutLab.text")); // NOI18N
        timeoutLab.setName("timeoutLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 70, 0, 0);
        producerPanel.add(timeoutLab, gridBagConstraints);

        timeoutTextField.setText(org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.timeoutTextField.text")); // NOI18N
        timeoutTextField.setToolTipText(mBundle.getString("DESC_Attribute_timeout"));
        timeoutTextField.setName("timeoutTextField"); // NOI18N
        timeoutTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                timeoutTextFieldFocusGained(evt);
            }
        });
        timeoutTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                timeoutTextFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        producerPanel.add(timeoutTextField, gridBagConstraints);

        timeToLiveSpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), null, Long.valueOf(1L)));
        timeToLiveSpinner.setToolTipText(mBundle.getString("DESC_Attribute_timeToLive"));
        timeToLiveSpinner.setName("timeToLiveSpinner"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 75;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        producerPanel.add(timeToLiveSpinner, gridBagConstraints);
        JTextField timeToLiveTF = ((JSpinner.DefaultEditor)timeToLiveSpinner.getEditor()).getTextField();
        timeToLiveTF.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                updateDescriptionArea(evt);
            }
        });

        deliverModeBtnGrp.add(persistenceRBtn);
        org.openide.awt.Mnemonics.setLocalizedText(persistenceRBtn, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.persistenceRBtn.text")); // NOI18N
        persistenceRBtn.setToolTipText(mBundle.getString("DESC_Attribute_deliveryMode"));
        persistenceRBtn.setMargin(new java.awt.Insets(2, 0, 2, 2));
        persistenceRBtn.setName("persistenceRBtn"); // NOI18N
        persistenceRBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                persistenceRBtnFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        producerPanel.add(persistenceRBtn, gridBagConstraints);

        deliverModeBtnGrp.add(nonPersistenceRtn);
        org.openide.awt.Mnemonics.setLocalizedText(nonPersistenceRtn, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.nonPersistenceRtn.text")); // NOI18N
        nonPersistenceRtn.setToolTipText(mBundle.getString("DESC_Attribute_deliveryMode"));
        nonPersistenceRtn.setName("nonPersistenceRtn"); // NOI18N
        nonPersistenceRtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nonPersistenceRtnFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        producerPanel.add(nonPersistenceRtn, gridBagConstraints);

        prioritySpinner.setModel(new javax.swing.SpinnerNumberModel(4, 0, 9, 1));
        prioritySpinner.setToolTipText(mBundle.getString("DESC_Attribute_priority"));
        prioritySpinner.setName("prioritySpinner"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        producerPanel.add(prioritySpinner, gridBagConstraints);
        JTextField prioritySpinnerTF = ((JSpinner.DefaultEditor)prioritySpinner.getEditor()).getTextField();
        prioritySpinnerTF.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                updateDescriptionArea(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(secondLab, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.secondLab.text")); // NOI18N
        secondLab.setName("secondLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        producerPanel.add(secondLab, gridBagConstraints);

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
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 0.5;
        producerPanel.add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(timeoutMSLab, org.openide.util.NbBundle.getMessage(JMSPublisherPanel.class, "JMSPublisherPanel.timeoutMSLab.text")); // NOI18N
        timeoutMSLab.setName("timeoutMSLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        producerPanel.add(timeoutMSLab, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        add(producerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void timeoutTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_timeoutTextFieldKeyTyped
// TODO add your handling code here:
    if (!Character.isDigit(evt.getKeyChar())) {
        evt.consume();
    }
}//GEN-LAST:event_timeoutTextFieldKeyTyped

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

                JMSMessage outputMessage = getOutputJMSMessage(binding,
                        selectedOperation);
                updateOutputMessageView(outputMessage);
            }
        }
    }
}//GEN-LAST:event_operationNameComboBoxItemStateChanged

private void persistenceRBtnFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_persistenceRBtnFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);    
}//GEN-LAST:event_persistenceRBtnFocusGained

private void nonPersistenceRtnFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nonPersistenceRtnFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);    
}//GEN-LAST:event_nonPersistenceRtnFocusGained

private void timeoutTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_timeoutTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);    
}//GEN-LAST:event_timeoutTextFieldFocusGained



    private void resetView() {
        servicePortComboBox.setEnabled(false);
        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
        portTypeComboBox.removeAllItems();
        operationNameComboBox.removeAllItems();
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

                    JMSMessage outputMessage = getOutputJMSMessage(binding,
                            operationNameComboBox.getSelectedItem().toString());
                    updateOutputMessageView(outputMessage);
                }
            }
            updateGeneralView(jmsAddress, bindingOperations);
        }
    }

    private void updateInputMessageView(JMSMessage inputJMSMessage) {
        if (inputJMSMessage != null) {

        }
    }

    private void updateOutputMessageView(JMSMessage outputJMSMessage) {
        if (outputJMSMessage != null) {

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
                    if (bi != null) {
                        List<JMSMessage> inputJMSMessages =
                                bi.getExtensibilityElements(JMSMessage.class);
                        if (inputJMSMessages.size() > 0) {
                            inputJMSMessage = inputJMSMessages.get(0);
                            break;
                        }
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
                    if (bo != null) {
                        List<JMSMessage> outputJMSMessages =
                                bo.getExtensibilityElements(JMSMessage.class);
                        if (outputJMSMessages.size() > 0) {
                            outputJMSMessage = outputJMSMessages.get(0);
                            break;
                        }
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
                        if (JMSConstants.DELIVERYMODE_NON_PERSISTENT.equals(jmsOp.getDeliveryMode())) {
                            nonPersistenceRtn.setSelected(true);
                        } else {
                            persistenceRBtn.setSelected(true);
                        }
                        
                        timeToLiveSpinner.setValue(jmsOp.getTimeToLive());

                        prioritySpinner.setValue(jmsOp.getPriority());
                        timeoutTextField.setText(String.valueOf(jmsOp.getTimeout()));

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

                // get the output text part
                outputTextParts = getOutputParts(binding, bop.getName());

                // populate text part
                populateInputPartComboBox(inputTextParts);

                // populate text part
                populateOutputPartComboBox(outputTextParts);

                BindingInput bi = bop.getBindingInput();
                if (bi != null) {
                    List<JMSMessage> inputJMSMessages =
                            bi.getExtensibilityElements(JMSMessage.class);
                    if ((inputJMSMessages != null) &&
                            (inputJMSMessages.size() > 0)) {
                        JMSMessage inputJMSMessage = inputJMSMessages.get(0);
    //                    // get input message type
    //                    inputTypeComboBox.setSelectedItem(inputJMSMessage.
    //                            getMessageType());
    //                    // get input text
    //                    inputTextComboBox.setSelectedItem(inputJMSMessage.
    //                            getTextPart());
                    }
                }

                BindingOutput bo = bop.getBindingOutput();
                if (bo != null) {
                    List<JMSMessage> outputJMSMessages =
                            bo.getExtensibilityElements(JMSMessage.class);
                    if ((outputJMSMessages != null) &&
                            (outputJMSMessages.size() > 0)) {
                        JMSMessage outputJMSMessage = outputJMSMessages.get(0);
    //                    // get output message type
    //                    outputTypeComboBox.setSelectedItem(outputJMSMessage.
    //                            getMessageType());
    //
    //                    // get output text
    //                    outputTextComboBox.setSelectedItem(outputJMSMessage.
    //                            getTextPart());
                    }
                }
            }
        }
    }

    private void updateServiceView(JMSAddress jmsAddress) {
        if (jmsAddress != null) {
//            connectionURLTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_CONNECTION_URL));
//            updateJNDISection();
//            userNameTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_USERNAME));
//            passwordTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_PASSWORD));
//            connectionFactoryNameTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_JNDI_CONNECTION_FACTORY_NAME));
//            initialContextFactoryTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_JNDI_INITIAL_CONTEXT_FACTORY));
//            providerURLTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_JNDI_PROVIDER_URL));
//            securityPrincipalTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_JNDI_SECURITY_PRINCIPAL));
//            securityCrendentialsTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_JNDI_SECURITY_CRDENTIALS));
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
                    if ((input != null) && (input.getMessage() != null) &&
                            (input.getMessage().get() != null)) {
                        NamedComponentReference<Message> messageIn = input.getMessage();
                        Message msgIn = messageIn.get();
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
                    if ((output != null) && (output.getMessage() != null) &&
                            (output.getMessage().get() != null)) {
                        NamedComponentReference<Message> messageOut = output.getMessage();
                        Message msgOut = messageOut.get();
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
//                inputTextComboBox.addItem(partName);
//                inputCorrelationPartComboBox.addItem(partName);
//                inputDeliveryModePartComboBox.addItem(partName);
//                inputPriorityPartComboBox.addItem(partName);
//                inputTypePartComboBox.addItem(partName);
//                inputMsgIDPartComboBox.addItem(partName);
//                inputRedeliveredPartComboBox.addItem(partName);
//                inputTimestampPartComboBox.addItem(partName);
            }
        }
    }

    private void populateOutputPartComboBox(Collection textItems) {
        if ((textItems != null) && (textItems.size() > 0)) {
            Iterator iter = textItems.iterator();
            while(iter.hasNext()) {
                String partName = (String) iter.next();
//                outputTextComboBox.addItem(partName);
//                outputCorrelationPartComboBox.addItem(partName);
//                outputDeliveryModePartComboBox.addItem(partName);
//                outputPriorityPartComboBox.addItem(partName);
//                outputTypePartComboBox.addItem(partName);
//                outputMsgIDPartComboBox.addItem(partName);
//                outputRedeliveredPartComboBox.addItem(partName);
//                outputTimestampPartComboBox.addItem(partName);
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

    private void updateDescriptionArea(FocusEvent evt) {
        if (mDescPanel != null) {
            mDescPanel.setText("");
        }

        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == persistenceRBtn) {
            desc = new String[]{"Delivery Mode \n\n",
                   persistenceRBtn.getToolTipText()}; 
        } else if (evt.getSource() == nonPersistenceRtn) {
            desc = new String[]{"Delivery Mode \n\n",
                   nonPersistenceRtn.getToolTipText()}; 
        } else if (evt.getSource() == ((JSpinner.DefaultEditor)timeToLiveSpinner.getEditor()).getTextField()) {
            desc = new String[]{"Time To Live \n\n",
                   timeToLiveSpinner.getToolTipText()}; 
        } else if (evt.getSource() == ((JSpinner.DefaultEditor)prioritySpinner.getEditor()).getTextField()) {
            desc = new String[]{"Priority \n\n",
                   prioritySpinner.getToolTipText()}; 
        } else if (evt.getSource() == timeoutTextField) {
            desc = new String[]{"Time Out\n\n",
                   timeoutTextField.getToolTipText()}; 
        }
        if (desc != null) {
            if (mDescPanel != null) {
                mDescPanel.setText(desc[0], desc[1]);
            }
            return;
        }
    }     
    
    private void cleanUp() {
        // clean up listeners TODO
        // null out data TODO
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JLabel bindingNameLabel;
    private javax.swing.ButtonGroup deliverModeBtnGrp;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JRadioButton nonPersistenceRtn;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JLabel operationNameLabel;
    private javax.swing.JRadioButton persistenceRBtn;
    private javax.swing.JPanel portBindingPanel;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JLabel priorityLab;
    private javax.swing.JSpinner prioritySpinner;
    private javax.swing.JPanel producerPanel;
    private javax.swing.JLabel secondLab;
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JLabel timeToLiveLab;
    private javax.swing.JSpinner timeToLiveSpinner;
    private javax.swing.JLabel timeoutLab;
    private javax.swing.JLabel timeoutMSLab;
    private javax.swing.JTextField timeoutTextField;
    // End of variables declaration//GEN-END:variables

}
