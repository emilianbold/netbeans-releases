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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import static org.netbeans.installer.product.ProductComponent.INSTALLATION_LOCATION_PROPERTY;
import static org.netbeans.installer.product.ProductComponent.DEFAULT_INSTALLATION_LOCATION_PROPERTY;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextField;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;

/**
 *
 * @author Kirill Sorokin
 */
public class DestinationPanel extends ErrorMessagePanel {
    private NbiTextPane    messagePane;
    private NbiLabel       destinationLabel;
    private NbiTextField   destinationField;
    private NbiButton      destinationButton;
    
    private NbiPanel       spacer;
    
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
        messagePane.setContentType(getProperty(MESSAGE_CONTENT_TYPE_PROPERTY));
        
        messagePane.setText(getProperty(MESSAGE_TEXT_PROPERTY));
        
        destinationLabel.setText(getProperty(DESTINATION_LABEL_TEXT_PROPERTY));
        
        destinationButton.setText(getProperty(DESTINATION_BUTTON_TEXT_PROPERTY));
        
        String destination = getWizard().getProductComponent().getProperty(INSTALLATION_LOCATION_PROPERTY);
        if (destination == null) {
            final String defaultDestination = getWizard().getProductComponent().getProperty(DEFAULT_INSTALLATION_LOCATION_PROPERTY);
            
            if (defaultDestination != null) {
                destination = parsePath(defaultDestination).getAbsolutePath();
            } else {
                destination = parsePath(DEFAULT_DESTINATION).getAbsolutePath();
            }
        }
        
        destinationField.setText(destination);
    }
    
    public void initComponents() {
        messagePane = new NbiTextPane();
        
        destinationField = new NbiTextField();
        destinationField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateErrorMessage();
            }
            public void insertUpdate(DocumentEvent e) {
                updateErrorMessage();
            }
            public void removeUpdate(DocumentEvent e) {
                updateErrorMessage();
            }
        });
        
        destinationLabel = new NbiLabel();
        destinationLabel.setLabelFor(destinationField);
        
        destinationButton = new NbiButton();
        destinationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                browseButtonPressed();
            }
        });
        
        spacer = new NbiPanel();
        
        add(messagePane, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
        add(destinationLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 11, 0, 11), 0, 0));
        add(destinationField, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 11, 0, 4), 0, 0));
        add(destinationButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 0, 0, 11), 0, 0));
        add(spacer, new GridBagConstraints(0, 3, 2, 1, 1.0, 0.7, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 11, 0, 11), 0, 0));
        
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
    }
    
    public void evaluateNextButtonClick() {
        String errorMessage = validateInput();
        String path = new File(destinationField.getText().trim()).getAbsolutePath();
        
        if (errorMessage == null) {
            getWizard().getProductComponent().setProperty(INSTALLATION_LOCATION_PROPERTY, path);
            super.evaluateNextButtonClick();
        } else {
            ErrorManager.notify(ErrorLevel.ERROR, errorMessage);
        }
    }
    
    private void browseButtonPressed() {
        fileChooser.setSelectedFile(new File(destinationField.getText()));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            destinationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    public String validateInput() {
        final String string = destinationField.getText().trim();
        final File   file   = new File(string);
        final String path   = file.getAbsolutePath();
        
        if (string.equals("")) {
            return StringUtils.format(getProperty(ERROR_NULL_PROPERTY), path);
        }
        
        if (!SystemUtils.isPathValid(path)) {
            return StringUtils.format(getProperty(ERROR_NOT_VALID_PROPERTY), path);
        }
        
        if (file.exists() && !file.isDirectory()) {
            return StringUtils.format(getProperty(ERROR_NOT_DIRECTORY_PROPERTY), path);
        }
        
        if (!FileUtils.canRead(file)) {
            return StringUtils.format(getProperty(ERROR_NOT_READABLE_PROPERTY), path);
        }
        
        if (!FileUtils.canWrite(file)) {
            return StringUtils.format(getProperty(ERROR_NOT_WRITABLE_PROPERTY), path);
        }
        
        if (!FileUtils.isEmpty(file)) {
            return StringUtils.format(getProperty(ERROR_NOT_EMPTY_PROPERTY), path);
        }
        
        return null;
    }
    
    private static final String MESSAGE_TEXT_PROPERTY = "message.text";
    private static final String MESSAGE_CONTENT_TYPE_PROPERTY = "message.content.type";
    private static final String DESTINATION_LABEL_TEXT_PROPERTY = "destination.label.text";
    private static final String DESTINATION_BUTTON_TEXT_PROPERTY = "destination.button.text";
    
    private static final String DEFAULT_MESSAGE_TEXT = ResourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.message.text");
    private static final String DEFAULT_MESSAGE_CONTENT_TYPE = ResourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.message.content.type");
    private static final String DEFAULT_DESTINATION_LABEL_TEXT = ResourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.destination.label.text");
    private static final String DEFAULT_DESTINATION_BUTTON_TEXT = ResourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.destination.button.text");
    
    private static final String ERROR_NULL_PROPERTY = "error.null";
    private static final String ERROR_NOT_VALID_PROPERTY = "error.not.valid";
    private static final String ERROR_NOT_DIRECTORY_PROPERTY = "error.not.directory";
    private static final String ERROR_NOT_READABLE_PROPERTY = "error.not.readable";
    private static final String ERROR_NOT_WRITABLE_PROPERTY = "error.not.writable";
    private static final String ERROR_NOT_EMPTY_PROPERTY = "error.not.empty";
    
    private static final String DEFAULT_ERROR_NULL = ResourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.error.null");
    private static final String DEFAULT_ERROR_NOT_VALID = ResourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.error.not.valid");
    private static final String DEFAULT_ERROR_NOT_DIRECTORY = ResourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.error.not.directory");
    private static final String DEFAULT_ERROR_NOT_READABLE = ResourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.error.not.readable");
    private static final String DEFAULT_ERROR_NOT_WRITABLE = ResourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.error.not.writable");
    private static final String DEFAULT_ERROR_NOT_EMPTY = ResourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.error.not.empty");
    
    private static final String DEFAULT_DESTINATION = ResourceUtils.getString(DestinationPanel.class, "DestinationPanel.default.destination");
}
