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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.openide.util.NbBundle;

/**
 * Panel for adding message destination.
 * @author Tomas Mysik
 */
public class MessageDestinationPanel extends javax.swing.JPanel {
    
    public static final String IS_VALID = MessageDestinationPanel.class.getName() + ".IS_VALID";
    
    // map because of faster searching
    private final Map<String, MessageDestination.Type> destinationMap;
    
    // private because correct initialization is needed
    private MessageDestinationPanel(Map<String, MessageDestination.Type> destinationMap) {
        initComponents();
        this.destinationMap = destinationMap;
    }
    
    /**
     * Factory method for creating new instance.
     * @param destinationMap the names and the types of project message destinations.
     * @return MessageDestinationPanel instance.
     */
    public static MessageDestinationPanel newInstance(final Map<String, MessageDestination.Type> destinationMap) {
        MessageDestinationPanel mdp = new MessageDestinationPanel(destinationMap);
        mdp.initialize();
        return mdp;
    }
    
    /**
     * Get the name of the message destination.
     * @return message destination name.
     */
    public String getDestinationName() {
        return destinationNameText.getText().trim();
    }
    
    /**
     * Get the type of the message destination.
     * @return message destination type.
     * @see MessageDestination.Type
     */
    public MessageDestination.Type getDestinationType() {
        if (queueTypeRadio.isSelected()) {
            return MessageDestination.Type.QUEUE;
        }
        return MessageDestination.Type.TOPIC;
    }
    
    private void initialize() {
        registerListeners();
        setupErrorLabel();
        verifyAndFire();
    }
    
    private void registerListeners() {
        // text field
        destinationNameText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                verifyAndFire();
            }

            public void removeUpdate(DocumentEvent event) {
                verifyAndFire();
            }

            public void changedUpdate(DocumentEvent event) {
                verifyAndFire();
            }
        });
        
        // radio buttons
        queueTypeRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                verifyAndFire();
            }
        });
        topicTypeRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                verifyAndFire();
            }
        });
    }
    
    private void setupErrorLabel() {
        setError(null);
        errorLabel.setForeground(getErrorColor());
    }
    
    private void setError(String key) {
        if (key == null) {
            errorLabel.setText("");
            return;
        }
        errorLabel.setText(NbBundle.getMessage(MessageDestinationPanel.class, key));
    }
    
    private Color getErrorColor() {
        Color errorColor = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (errorColor != null) {
            return errorColor;
        }
        return new Color(255, 0, 0);
    }
    
    private void verifyAndFire() {
        boolean isValid = verifyComponents();
        firePropertyChange(IS_VALID, !isValid, isValid);
    }
    
    private boolean verifyComponents() {
        // destination name - form & duplicity
        String destinationName = destinationNameText.getText();
        if (destinationName == null || destinationName.trim().length() == 0) {
            setError("ERR_NoDestinationName"); // NOI18N
            return false;
        } else {
            destinationName = destinationName.trim();
            MessageDestination.Type type = destinationMap.get(destinationName);
            if (type != null && type.equals(getDestinationType())) {
                setError("ERR_DuplicateDestination"); // NOI18N
                return false;
            }
        }
        
        // destination type (radio)
        if (destinationTypeGroup.getSelection() == null) {
            setError("ERR_NoDestinationType"); // NOI18N
            return false;
        }
        
        // no errors
        setError(null);
        return true;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        destinationTypeGroup = new javax.swing.ButtonGroup();
        destinationNameLabel = new javax.swing.JLabel();
        destinationNameText = new javax.swing.JTextField();
        destinationTypeLabel = new javax.swing.JLabel();
        queueTypeRadio = new javax.swing.JRadioButton();
        topicTypeRadio = new javax.swing.JRadioButton();
        errorLabel = new javax.swing.JLabel();

        destinationNameLabel.setLabelFor(destinationNameText);
        org.openide.awt.Mnemonics.setLocalizedText(destinationNameLabel, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "LBL_DestinationName")); // NOI18N

        destinationTypeLabel.setLabelFor(queueTypeRadio);
        org.openide.awt.Mnemonics.setLocalizedText(destinationTypeLabel, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "LBL_DestinationType")); // NOI18N

        destinationTypeGroup.add(queueTypeRadio);
        queueTypeRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(queueTypeRadio, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "LBL_Queue")); // NOI18N
        queueTypeRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        queueTypeRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        destinationTypeGroup.add(topicTypeRadio);
        org.openide.awt.Mnemonics.setLocalizedText(topicTypeRadio, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "LBL_Topic")); // NOI18N
        topicTypeRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        topicTypeRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "ERR_NoDestinationName")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(destinationTypeLabel)
                            .add(destinationNameLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(topicTypeRadio)
                            .add(queueTypeRadio)
                            .add(destinationNameText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(destinationNameLabel)
                    .add(destinationNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(destinationTypeLabel)
                    .add(queueTypeRadio))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(topicTypeRadio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel destinationNameLabel;
    private javax.swing.JTextField destinationNameText;
    private javax.swing.ButtonGroup destinationTypeGroup;
    private javax.swing.JLabel destinationTypeLabel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JRadioButton queueTypeRadio;
    private javax.swing.JRadioButton topicTypeRadio;
    // End of variables declaration//GEN-END:variables
    
}
