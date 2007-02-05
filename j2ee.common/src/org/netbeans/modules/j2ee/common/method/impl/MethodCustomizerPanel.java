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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.common.method.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.common.method.MethodModel;

/**
 *
 * @author Martin Adamek
 */
public final class MethodCustomizerPanel extends javax.swing.JPanel {
    
    public static final String NAME = "name";
    public static final String RETURN_TYPE = "returnType";
    public static final String INTERFACES = "interfaces";
    
    // immutable method prototype
    private final MethodModel methodModel;
    private final ParametersPanel parametersPanel;
    
    private MethodCustomizerPanel(
            MethodModel methodModel,
            boolean hasLocal,
            boolean hasRemote,
            boolean selectLocal,
            boolean selectRemote,
            boolean hasReturnType,
            String  ejbql,
            boolean hasFinderCardinality,
            boolean hasExceptions,
            boolean hasInterfaces) {
        initComponents();
        
        this.methodModel = methodModel;
        
        nameTextField.setText(methodModel.getName());
        returnTypeTextField.setText(methodModel.getReturnType());
        
        localCheckBox.setEnabled(hasLocal);
        remoteCheckBox.setEnabled(hasRemote);
        localCheckBox.setSelected(selectLocal);
        remoteCheckBox.setSelected(selectRemote);
        
        if (!hasReturnType) {
            disableReturnType();
        }
        if (ejbql == null) {
            disableEjbql();
        } else {
            ejbqlTextArea.setText(ejbql);
        }
        if (!hasFinderCardinality) {
            disableCardinality();;
        }
        if (!hasExceptions) {
            exceptionsPanel.setVisible(false);
        }
        if (!hasInterfaces) {
            disableInterfaces();
        }
        
        parametersPanel = new ParametersPanel(methodModel.getParameters());
        parametersContainerPanel.add(parametersPanel);
        
        // listeners
        nameTextField.getDocument().addDocumentListener(new SimpleListener(NAME));
        returnTypeTextField.getDocument().addDocumentListener(new SimpleListener(RETURN_TYPE));
        SimpleListener interfacesListener = new SimpleListener(INTERFACES);
        localCheckBox.addActionListener(interfacesListener);
        remoteCheckBox.addActionListener(interfacesListener);
    }
    
    public static MethodCustomizerPanel create(MethodModel methodModel, boolean hasLocal, boolean hasRemote, boolean selectLocal, boolean selectRemote,
            boolean hasReturnType, String  ejbql, boolean hasFinderCardinality, boolean hasExceptions, boolean hasInterfaces) {
        return new MethodCustomizerPanel(methodModel, hasLocal, hasRemote, selectLocal, selectRemote,
                hasReturnType, ejbql, hasFinderCardinality, hasExceptions, hasInterfaces);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	super.addPropertyChangeListener(listener);
         // first validation before any real event is send
        firePropertyChange(NAME, null, null);
        firePropertyChange(RETURN_TYPE, null, null);
        firePropertyChange(INTERFACES, null, null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        finderCardinalityButtonGroup = new javax.swing.ButtonGroup();
        ejbqlScrollPane = new javax.swing.JScrollPane();
        ejbqlTextArea = new javax.swing.JTextArea();
        parExcLabel = new javax.swing.JLabel();
        exceptionAndParameterPane = new javax.swing.JTabbedPane();
        parametersContainerPanel = new javax.swing.JPanel();
        exceptionsPanel = new javax.swing.JPanel();
        errorTextField = new javax.swing.JTextField();
        returnTypeLabel = new javax.swing.JLabel();
        returnTypeTextField = new javax.swing.JTextField();
        nameTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        methodLabel = new javax.swing.JLabel();
        methodSeparator = new javax.swing.JSeparator();
        oneRadioButton = new javax.swing.JRadioButton();
        manyRadioButton = new javax.swing.JRadioButton();
        cardinalityLabel = new javax.swing.JLabel();
        cardinalitySeparator = new javax.swing.JSeparator();
        ejbqlLabel = new javax.swing.JLabel();
        ejbqlSeparator = new javax.swing.JSeparator();
        remoteCheckBox = new javax.swing.JCheckBox();
        localCheckBox = new javax.swing.JCheckBox();
        interfaceLabel = new javax.swing.JLabel();
        interfaceSeparator = new javax.swing.JSeparator();
        parExcSeparator = new javax.swing.JSeparator();

        ejbqlScrollPane.setBorder(null);

        ejbqlTextArea.setColumns(20);
        ejbqlTextArea.setRows(5);
        ejbqlTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ejbqlScrollPane.setViewportView(ejbqlTextArea);

        org.openide.awt.Mnemonics.setLocalizedText(parExcLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.parExcLabel.text")); // NOI18N

        parametersContainerPanel.setLayout(new java.awt.BorderLayout());
        exceptionAndParameterPane.addTab(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.parametersContainerPanel.TabConstraints.tabTitle"), parametersContainerPanel); // NOI18N

        org.jdesktop.layout.GroupLayout exceptionsPanelLayout = new org.jdesktop.layout.GroupLayout(exceptionsPanel);
        exceptionsPanel.setLayout(exceptionsPanelLayout);
        exceptionsPanelLayout.setHorizontalGroup(
            exceptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 362, Short.MAX_VALUE)
        );
        exceptionsPanelLayout.setVerticalGroup(
            exceptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 73, Short.MAX_VALUE)
        );

        exceptionAndParameterPane.addTab(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.exceptionsPanel.TabConstraints.tabTitle"), exceptionsPanel); // NOI18N

        errorTextField.setEditable(false);
        errorTextField.setText(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.errorTextField.text")); // NOI18N
        errorTextField.setBorder(null);

        returnTypeLabel.setLabelFor(returnTypeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(returnTypeLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.returnTypeLabel.text")); // NOI18N

        returnTypeTextField.setText(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.returnTypeTextField.text")); // NOI18N

        nameTextField.setText(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.nameTextField.text")); // NOI18N

        jLabel1.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(methodLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.methodLabel.text")); // NOI18N

        finderCardinalityButtonGroup.add(oneRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(oneRadioButton, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.oneRadioButton.text")); // NOI18N
        oneRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        oneRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        finderCardinalityButtonGroup.add(manyRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(manyRadioButton, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.manyRadioButton.text")); // NOI18N
        manyRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        manyRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(cardinalityLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.cardinalityLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ejbqlLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.ejbqlLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(remoteCheckBox, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.remoteCheckBox.text")); // NOI18N
        remoteCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        remoteCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(localCheckBox, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.localCheckBox.text")); // NOI18N
        localCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        localCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(interfaceLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.interfaceLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(parExcLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(parExcSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE))
                    .add(exceptionAndParameterPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                    .add(errorTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(methodLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(methodSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel1)
                            .add(returnTypeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                            .add(returnTypeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(cardinalityLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cardinalitySeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE))
                    .add(ejbqlScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(interfaceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(interfaceSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(remoteCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(localCheckBox))
                    .add(layout.createSequentialGroup()
                        .add(ejbqlLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ejbqlSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(oneRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(manyRadioButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(methodLabel)
                    .add(methodSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(returnTypeLabel)
                    .add(returnTypeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(cardinalityLabel)
                    .add(cardinalitySeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(oneRadioButton)
                    .add(manyRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(ejbqlLabel)
                    .add(ejbqlSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ejbqlScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(interfaceLabel)
                    .add(interfaceSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(remoteCheckBox)
                            .add(localCheckBox))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(parExcLabel))
                    .add(parExcSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(exceptionAndParameterPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cardinalityLabel;
    private javax.swing.JSeparator cardinalitySeparator;
    private javax.swing.JLabel ejbqlLabel;
    private javax.swing.JScrollPane ejbqlScrollPane;
    private javax.swing.JSeparator ejbqlSeparator;
    private javax.swing.JTextArea ejbqlTextArea;
    private javax.swing.JTextField errorTextField;
    private javax.swing.JTabbedPane exceptionAndParameterPane;
    private javax.swing.JPanel exceptionsPanel;
    private javax.swing.ButtonGroup finderCardinalityButtonGroup;
    private javax.swing.JLabel interfaceLabel;
    private javax.swing.JSeparator interfaceSeparator;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JCheckBox localCheckBox;
    private javax.swing.JRadioButton manyRadioButton;
    private javax.swing.JLabel methodLabel;
    private javax.swing.JSeparator methodSeparator;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JRadioButton oneRadioButton;
    private javax.swing.JLabel parExcLabel;
    private javax.swing.JSeparator parExcSeparator;
    private javax.swing.JPanel parametersContainerPanel;
    private javax.swing.JCheckBox remoteCheckBox;
    private javax.swing.JLabel returnTypeLabel;
    private javax.swing.JTextField returnTypeTextField;
    // End of variables declaration//GEN-END:variables
    
    public void setError(String message) {
        errorTextField.setText(message);
    }
    
    public String getMethodName() {
            return nameTextField.getText().trim();
    }
    
    public String getReturnType() {
        return returnTypeTextField.getText().trim();
    }

    public List<MethodModel.Variable> getParameters() {
        return parametersPanel.getParameters();
    }
    
    public List<String> getExceptions() {
        return Collections.<String>emptyList();
    }
    
    public Set<Modifier> getModifiers() {
        // not changing?
        return methodModel.getModifiers();
    }
    
    public String getMethodBody() {
        // not changing?
        return methodModel.getBody();
    }
    
    public boolean hasLocal() {
        return localCheckBox.isEnabled() & localCheckBox.isSelected();
    }
    
    public boolean hasRemote() {
        return remoteCheckBox.isEnabled() && remoteCheckBox.isSelected();
    }
    
    private void disableReturnType() {
        returnTypeLabel.setVisible(false);
        returnTypeTextField.setVisible(false);
    }
    
    private void disableCardinality() {
        cardinalityLabel.setVisible(false);
        cardinalitySeparator.setVisible(false);
        oneRadioButton.setVisible(false);
        manyRadioButton.setVisible(false);
    }
    
    private void disableInterfaces() {
        interfaceLabel.setVisible(false);
        interfaceSeparator.setVisible(false);
        remoteCheckBox.setVisible(false);
        localCheckBox.setVisible(false);
    }
    
    private void disableEjbql() {
        ejbqlLabel.setVisible(false);
        ejbqlSeparator.setVisible(false);
        ejbqlScrollPane.setVisible(false);
    }
    
    /**
     * Listener on text fields. 
     * Fires change event for specified property of this JPanel, 
     * old and new value of event is null. 
     * After receiving event, client can get property value by 
     * calling {@link #getProperty(String)}
     */
    private class SimpleListener implements DocumentListener, ActionListener {
        
        private final String propertyName;
        
        public SimpleListener(String propertyName) {
            this.propertyName = propertyName;
        }
        
        public void insertUpdate(DocumentEvent documentEvent) { fire(); }
        
        public void removeUpdate(DocumentEvent documentEvent) { fire(); }
        
        public void changedUpdate(DocumentEvent documentEvent) {}
        
        public void actionPerformed(ActionEvent actionEvent) { fire(); }

        private void fire() {
            firePropertyChange(propertyName, null, null);
        }
        
    }
    
}
