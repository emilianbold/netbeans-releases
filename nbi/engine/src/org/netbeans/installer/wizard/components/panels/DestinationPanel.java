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
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components.panels;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

import static org.netbeans.installer.product.ProductComponent.INSTALLATION_LOCATION_PROPERTY;
import static org.netbeans.installer.product.ProductComponent.DEFAULT_INSTALLATION_LOCATION_PROPERTY;
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class DestinationPanel extends DefaultWizardPanel {
    private JTextPane    messagePane;
    private JLabel       destinationLabel;
    private JTextField   destinationField;
    private JButton      destinationButton;
    
    private JLabel       errorLabel;
    
    private JPanel       spacer;
    
    private JFileChooser fileChooser;
    
    public DestinationPanel() {
        setProperty(MESSAGE_TEXT_PROPERTY, DEFAULT_MESSAGE_TEXT);
        setProperty(MESSAGE_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_CONTENT_TYPE);
        setProperty(DESTINATION_LABEL_TEXT_PROPERTY, DEFAULT_DESTINATION_LABEL_TEXT);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY, DEFAULT_DESTINATION_BUTTON_TEXT);
        
        setProperty(ERROR_NULL_PROPERTY, DEFAULT_ERROR_NULL);
        setProperty(ERROR_NOT_VALID_PROPERTY, DEFAULT_ERROR_NOT_VALID);
        setProperty(ERROR_NOT_DIRECTORY_PROPERTY, DEFAULT_ERROR_NOT_DIRECTORY);
        setProperty(ERROR_NOT_READABLE_PROPERTY, DEFAULT_ERROR_NOT_READABLE);
        setProperty(ERROR_NOT_WRITABLE_PROPERTY, DEFAULT_ERROR_NOT_WRITABLE);
        setProperty(ERROR_NOT_EMPTY_PROPERTY, DEFAULT_ERROR_NOT_EMPTY);
    }
    
    public void initialize() {
        final String messageContentType = systemUtils.parseString(getProperty(MESSAGE_CONTENT_TYPE_PROPERTY), getClassLoader());
        messagePane.setContentType(messageContentType);
        
        final String messageText = systemUtils.parseString(getProperty(MESSAGE_TEXT_PROPERTY), getClassLoader());
        messagePane.setText(messageText);
        
        final String destinationLabelText = systemUtils.parseString(getProperty(DESTINATION_LABEL_TEXT_PROPERTY), getClassLoader());
        destinationLabel.setText(stringUtils.stripMnemonic(destinationLabelText));
        destinationLabel.setDisplayedMnemonic(stringUtils.fetchMnemonic(destinationLabelText));
        
        final String destinationButtonText = systemUtils.parseString(getProperty(DESTINATION_BUTTON_TEXT_PROPERTY), getClassLoader());
        destinationButton.setText(stringUtils.stripMnemonic(destinationButtonText));
        destinationButton.setMnemonic(stringUtils.fetchMnemonic(destinationButtonText));
        
        String destination = getWizard().getProductComponent().getProperty(INSTALLATION_LOCATION_PROPERTY);
        if (destination == null) {
            String defaultDestination = getWizard().getProductComponent().getProperty(DEFAULT_INSTALLATION_LOCATION_PROPERTY);
            if (defaultDestination != null) {
                destination = SystemUtils.getInstance().parsePath(defaultDestination);
            } else {
                destination = SystemUtils.getInstance().parsePath(DEFAULT_DESTINATION);
            }
        }
        
        destinationField.setText(destination);
    }
    
    public void initComponents() {
        setLayout(new GridBagLayout());
        
        messagePane = new JTextPane();
        messagePane.setOpaque(false);
        
        destinationField = new JTextField();
        destinationField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                destinationFieldChanged();
            }
            public void insertUpdate(DocumentEvent e) {
                destinationFieldChanged();
            }
            public void removeUpdate(DocumentEvent e) {
                destinationFieldChanged();
            }
        });
        
        destinationLabel = new JLabel();
        destinationLabel.setLabelFor(destinationField);
        
        destinationButton = new JButton();
        if (SystemUtils.Platform.isMacOS()) {
            destinationButton.setOpaque(false);
        }
        destinationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                browseButtonPressed();
            }
        });
        
        spacer = new JPanel();
        spacer.setOpaque(false);
        
        errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);
        errorLabel.setIcon(emptyIcon);
        errorLabel.setText(" ");
        
        add(messagePane, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
        add(destinationLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 11, 0, 11), 0, 0));
        add(destinationField, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 11, 0, 4), 0, 0));
        add(destinationButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 0, 0, 11), 0, 0));
        add(spacer, new GridBagConstraints(0, 3, 2, 1, 1.0, 0.7, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 11, 0, 11), 0, 0));
        add(errorLabel, new GridBagConstraints(0, 4, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(7, 11, 11, 11), 0, 0));
        
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
    }
    
    public void evaluateNextButtonClick() {
        String errorMessage = validateLocation();
        
        if (errorMessage == null) {
            getWizard().getProductComponent().setProperty(INSTALLATION_LOCATION_PROPERTY, destinationField.getText());
            super.evaluateNextButtonClick();
        } else {
            ErrorManager.getInstance().notify(ErrorLevel.ERROR, errorMessage);
        }
    }
    
    private void browseButtonPressed() {
        fileChooser.setSelectedFile(new File(destinationField.getText()));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            destinationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void destinationFieldChanged() {
        String errorMessage = validateLocation();
        if (errorMessage != null) {
            errorLabel.setIcon(errorIcon);
            errorLabel.setText(errorMessage);
            getNextButton().setEnabled(false);
        } else {
            errorLabel.setIcon(emptyIcon);
            errorLabel.setText(" ");
            getNextButton().setEnabled(true);
        }
    }
    
    private String validateLocation() {
        final String string = destinationField.getText().trim();
        final File   file   = new File(string);
        final String path   = file.getAbsolutePath();
        
        if (string.equals("")) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_NULL_PROPERTY), getClassLoader()), path);
        }
        
        if (!systemUtils.isPathValid(path)) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_NOT_VALID_PROPERTY), getClassLoader()), path);
        }
        
        if (file.exists() && !file.isDirectory()) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_NOT_DIRECTORY_PROPERTY), getClassLoader()), path);
        }
        
        if (!fileUtils.canRead(file)) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_NOT_READABLE_PROPERTY), getClassLoader()), path);
        }
        
        if (!fileUtils.canWrite(file)) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_NOT_WRITABLE_PROPERTY), getClassLoader()), path);
        }
        
        if (!fileUtils.isEmpty(file)) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_NOT_EMPTY_PROPERTY), getClassLoader()), path);
        }
        
        return null;
    }
    
    private static SystemUtils   systemUtils   = SystemUtils.getInstance();
    private static StringUtils   stringUtils   = StringUtils.getInstance();
    private static ResourceUtils resourceUtils = ResourceUtils.getInstance();
    private static FileUtils     fileUtils     = FileUtils.getInstance();
    
    private static final String MESSAGE_TEXT_PROPERTY = "message.text";
    private static final String MESSAGE_CONTENT_TYPE_PROPERTY = "message.content.type";
    private static final String DESTINATION_LABEL_TEXT_PROPERTY = "destination.label.text";
    private static final String DESTINATION_BUTTON_TEXT_PROPERTY = "destination.button.text";
    
    private static final String DEFAULT_MESSAGE_TEXT = resourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.message.text");
    private static final String DEFAULT_MESSAGE_CONTENT_TYPE = resourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.message.content.type");
    private static final String DEFAULT_DESTINATION_LABEL_TEXT = resourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.destination.label.text");
    private static final String DEFAULT_DESTINATION_BUTTON_TEXT = resourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.destination.button.text");
    
    private static final String ERROR_NULL_PROPERTY = "error.null";
    private static final String ERROR_NOT_VALID_PROPERTY = "error.not.valid";
    private static final String ERROR_NOT_DIRECTORY_PROPERTY = "error.not.directory";
    private static final String ERROR_NOT_READABLE_PROPERTY = "error.not.readable";
    private static final String ERROR_NOT_WRITABLE_PROPERTY = "error.not.writable";
    private static final String ERROR_NOT_EMPTY_PROPERTY = "error.not.empty";
    
    private static final String DEFAULT_ERROR_NULL = resourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.error.null");
    private static final String DEFAULT_ERROR_NOT_VALID = resourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.error.not.valid");
    private static final String DEFAULT_ERROR_NOT_DIRECTORY = resourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.error.not.directory");
    private static final String DEFAULT_ERROR_NOT_READABLE = resourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.error.not.readable");
    private static final String DEFAULT_ERROR_NOT_WRITABLE = resourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.error.not.writable");
    private static final String DEFAULT_ERROR_NOT_EMPTY = resourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.error.not.empty");
    
    private static final String DEFAULT_DESTINATION = resourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.destination");
}
