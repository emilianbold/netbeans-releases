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
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class DestinationPanel extends DefaultWizardPanel {
    private JTextPane    textPane;
    private JLabel       destinationFieldLabel;
    private JTextField   destinationField;
    private JButton      browseButton;
    
    private JPanel       spacer;
    
    private JFileChooser fileChooser;
    
    public DestinationPanel() {
        setProperty(TEXT_PROPERTY, DEFAULT_TEXT);
        setProperty(DESTINATION_FIELD_LABEL_PROPERTY, DEFAULT_DESTINATION_FIELD_LABEL);
        setProperty(BROWSE_BUTTON_LABEL_PROPERTY, DEFAULT_BROWSE_BUTTON_LABEL);
    }
    
    public void initialize() {
        StringUtils stringUtils = StringUtils.getInstance();
        
        textPane.setText(getProperty(TEXT_PROPERTY));
        
        destinationFieldLabel.setText(stringUtils.stripMnemonic(getProperty(DESTINATION_FIELD_LABEL_PROPERTY)));
        destinationFieldLabel.setDisplayedMnemonic(stringUtils.fetchMnemonic(getProperty(DESTINATION_FIELD_LABEL_PROPERTY)));
        
        browseButton.setText(stringUtils.stripMnemonic(getProperty(BROWSE_BUTTON_LABEL_PROPERTY)));
        browseButton.setMnemonic(stringUtils.fetchMnemonic(getProperty(BROWSE_BUTTON_LABEL_PROPERTY)));
        
        String location = getWizard().getProductComponent().getProperty(ProductComponent.INSTALLATION_LOCATION_PROPERTY);
        if (location == null) {
            String defaultLocation = getWizard().getProductComponent().getProperty(ProductComponent.DEFAULT_INSTALLATION_LOCATION_PROPERTY);
            if (defaultLocation != null) {
                location = SystemUtils.getInstance().parsePath(defaultLocation);
            } else {
                location = DEFAULT_LOCATION;
            }
        }
        
        destinationField.setText(location);
    }

    public void initComponents() {
        setLayout(new GridBagLayout());
        
        textPane = new JTextPane();
        textPane.setContentType("text/plain");
        textPane.setOpaque(false);
        
        destinationField = new JTextField();
        
        destinationFieldLabel = new JLabel();
        destinationFieldLabel.setLabelFor(destinationField);
        
        browseButton = new JButton();
        if (SystemUtils.Platform.isMacOS()) {
            browseButton.setOpaque(false);
        }
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                browseButtonPressed();
            }
        });
        
        spacer = new JPanel();
        spacer.setOpaque(false);
        
        add(textPane, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
        add(destinationFieldLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 11, 0, 11), 0, 0));
        add(destinationField, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 11, 0, 4), 0, 0));
        add(browseButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 0, 0, 11), 0, 0));
        add(spacer, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.7, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 11, 11, 11), 0, 0));
        
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
    }
    
    public void evaluateNextButtonClick() {
        getWizard().getProductComponent().setProperty(ProductComponent.INSTALLATION_LOCATION_PROPERTY, destinationField.getText());
        getWizard().next();
    }
    
    private void browseButtonPressed() {
        fileChooser.setSelectedFile(new File(destinationField.getText()));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            destinationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    public static final String TEXT_PROPERTY = "text";
    public static final String DESTINATION_FIELD_LABEL_PROPERTY = "destination.field.label";
    public static final String BROWSE_BUTTON_LABEL_PROPERTY = "browse.button.label";
    
    public static final String DEFAULT_TEXT = "";
    public static final String DEFAULT_DESTINATION_FIELD_LABEL = "";
    public static final String DEFAULT_BROWSE_BUTTON_LABEL = "";
    
    public static final String DEFAULT_LOCATION = "";
}
