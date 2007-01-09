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
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextField;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public class DestinationPanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String MESSAGE_TEXT_PROPERTY = "message.text";
    private static final String MESSAGE_CONTENT_TYPE_PROPERTY = "message.content.type";
    private static final String DESTINATION_LABEL_TEXT_PROPERTY = "destination.label.text";
    private static final String DESTINATION_BUTTON_TEXT_PROPERTY = "destination.button.text";
    
    private static final String DEFAULT_MESSAGE_TEXT = ResourceUtils.getString(DestinationPanel.class, "DP.message.text");
    private static final String DEFAULT_MESSAGE_CONTENT_TYPE = ResourceUtils.getString(DestinationPanel.class, "DP.message.content.type");
    private static final String DEFAULT_DESTINATION_LABEL_TEXT = ResourceUtils.getString(DestinationPanel.class, "DP.destination.label.text");
    private static final String DEFAULT_DESTINATION_BUTTON_TEXT = ResourceUtils.getString(DestinationPanel.class, "DP.destination.button.text");
    
    private static final String ERROR_NULL_PROPERTY = "error.null";
    private static final String ERROR_NOT_VALID_PROPERTY = "error.not.valid";
    private static final String ERROR_NOT_DIRECTORY_PROPERTY = "error.not.directory";
    private static final String ERROR_NOT_READABLE_PROPERTY = "error.not.readable";
    private static final String ERROR_NOT_WRITABLE_PROPERTY = "error.not.writable";
    private static final String ERROR_NOT_EMPTY_PROPERTY = "error.not.empty";
    
    private static final String DEFAULT_ERROR_NULL = ResourceUtils.getString(DestinationPanel.class, "DP.error.null");
    private static final String DEFAULT_ERROR_NOT_VALID = ResourceUtils.getString(DestinationPanel.class, "DP.error.not.valid");
    private static final String DEFAULT_ERROR_NOT_DIRECTORY = ResourceUtils.getString(DestinationPanel.class, "DP.error.not.directory");
    private static final String DEFAULT_ERROR_NOT_READABLE = ResourceUtils.getString(DestinationPanel.class, "DP.error.not.readable");
    private static final String DEFAULT_ERROR_NOT_WRITABLE = ResourceUtils.getString(DestinationPanel.class, "DP.error.not.writable");
    private static final String DEFAULT_ERROR_NOT_EMPTY = ResourceUtils.getString(DestinationPanel.class, "DP.error.not.empty");
    
    private static final String DEFAULT_DESTINATION = ResourceUtils.getString(DestinationPanel.class, "DP.default.destination");
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
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
    
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new DestinationPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class DestinationPanelUi extends ErrorMessagePanelUi {
        protected DestinationPanel        component;
        
        public DestinationPanelUi(DestinationPanel component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new DestinationPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class DestinationPanelSwingUi extends ErrorMessagePanelSwingUi {
        /////////////////////////////////////////////////////////////////////////////
        // Constants
        public static final String ERROR_ICON =
                "org/netbeans/installer/wizard/components/panels/error.png";
        public static final String EMPTY_ICON =
                "org/netbeans/installer/wizard/components/panels/empty.png";
        
        /////////////////////////////////////////////////////////////////////////////
        // Instance
        protected DestinationPanel component;
        
        private NbiTextPane    messagePane;
        private NbiLabel       destinationLabel;
        private NbiTextField   destinationField;
        private NbiButton      destinationButton;
        
        private NbiPanel       spacer;
        
        private JFileChooser fileChooser;
        
        public DestinationPanelSwingUi(
                final DestinationPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        protected void initialize() {
            super.initialize();
            
            messagePane.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_PROPERTY));
            messagePane.setText(component.getProperty(MESSAGE_TEXT_PROPERTY));
            
            destinationLabel.setText(component.getProperty(DESTINATION_LABEL_TEXT_PROPERTY));
            
            destinationButton.setText(component.getProperty(DESTINATION_BUTTON_TEXT_PROPERTY));
            
            String destination = component.getWizard().getProduct().getProperty(Product.INSTALLATION_LOCATION_PROPERTY);
            if (destination == null) {
                destination = DEFAULT_DESTINATION;
            }
            
            destinationField.setText(component.parsePath(destination).getAbsolutePath());
        }
        
        protected void saveInput() {
            component.getWizard().getProduct().setProperty(
                    Product.INSTALLATION_LOCATION_PROPERTY, 
                    new File(destinationField.getText().trim()).getAbsolutePath());
        }
        
        protected String validateInput() {
            final String string = destinationField.getText().trim();
            final File   file   = new File(string);
            final String path   = file.getAbsolutePath();
            
            if (string.equals("")) {
                return StringUtils.format(component.getProperty(ERROR_NULL_PROPERTY), path);
            }
            
            if (!SystemUtils.isPathValid(path)) {
                return StringUtils.format(component.getProperty(ERROR_NOT_VALID_PROPERTY), path);
            }
            
            if (file.exists() && !file.isDirectory()) {
                return StringUtils.format(component.getProperty(ERROR_NOT_DIRECTORY_PROPERTY), path);
            }
            
            if (!FileUtils.canRead(file)) {
                return StringUtils.format(component.getProperty(ERROR_NOT_READABLE_PROPERTY), path);
            }
            
            if (!FileUtils.canWrite(file)) {
                return StringUtils.format(component.getProperty(ERROR_NOT_WRITABLE_PROPERTY), path);
            }
            
            if (!FileUtils.isEmpty(file)) {
                return StringUtils.format(component.getProperty(ERROR_NOT_EMPTY_PROPERTY), path);
            }
            
            return null;
        }
        
        private void browseButtonPressed() {
            fileChooser.setSelectedFile(new File(destinationField.getText()));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                destinationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
        
        private void initComponents() {
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
    }
}
