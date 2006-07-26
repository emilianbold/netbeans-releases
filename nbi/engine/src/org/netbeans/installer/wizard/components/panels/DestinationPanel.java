/*
 * DestinationPanel.java
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
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class DestinationPanel extends DefaultWizardPanel {
    private JTextPane textPane;
    private JLabel destinationLabel;
    private JTextField destinationField;
    private JButton destinationButton;
    
    private JPanel spacer;
    
    private JFileChooser fileChooser;
    
    public DestinationPanel() {
        setProperty(TEXT_PROPERTY, DEFAULT_TEXT);
        setProperty(DESTINATION_LABEL_PROPERTY, DEFAULT_DESTINATION_LABEL);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY, DEFAULT_DESTINATION_BUTTON_TEXT);
    }
    
    public void initialize() {
        getBackButton().setEnabled(false);
        
        textPane.setText(getProperty(TEXT_PROPERTY));
        destinationLabel.setText(getProperty(DESTINATION_LABEL_PROPERTY));
        destinationButton.setText(getProperty(DESTINATION_BUTTON_TEXT_PROPERTY));
        
        String location = getWizard().getProductComponent().getProperty(ProductComponent.INSTALLATION_LOCATION_PROPERTY);
        if (location == null) {
            String defaultLocation = getWizard().getProductComponent().getProperty(ProductComponent.DEFAULT_INSTALLATION_LOCATION_PROPERTY);
            if (defaultLocation == null) {
                destinationField.setText(DEFAULT_INSTALLATION_LOCATION);
            } else {
                destinationField.setText(SystemUtils.parsePath(defaultLocation));
            }
        } else {
            destinationField.setText(location);
        }
    }

    public void initComponents() {
        setLayout(new GridBagLayout());
        
        textPane = new JTextPane();
        textPane.setContentType("text/plain");
        textPane.setOpaque(false);
        
        destinationField = new JTextField();
        
        destinationLabel = new JLabel();
        destinationLabel.setLabelFor(destinationField);
        
        destinationButton = new JButton();
        if (SystemUtils.isMacOS()) {
            destinationButton.setOpaque(false);
        }
        destinationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                destinationButtonPressed();
            }
        });
        
        spacer = new JPanel();
        spacer.setOpaque(false);
        
        add(textPane, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
        add(destinationLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 11, 0, 11), 0, 0));
        add(destinationField, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 11, 0, 4), 0, 0));
        add(destinationButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 0, 0, 11), 0, 0));
        add(spacer, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.7, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 11, 11, 11), 0, 0));
        
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
    }
    
    public void evaluateNextButtonClick() {
        getWizard().getProductComponent().setProperty(ProductComponent.INSTALLATION_LOCATION_PROPERTY, destinationField.getText());
        getWizard().next();
    }
    
    private void destinationButtonPressed() {
        fileChooser.setSelectedFile(new File(destinationField.getText()));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            destinationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    public static final String TEXT_PROPERTY = "text";
    public static final String DESTINATION_LABEL_PROPERTY = "destination.label";
    public static final String DESTINATION_BUTTON_TEXT_PROPERTY = "destination.button";
    
    public static final String DEFAULT_TEXT = "";
    public static final String DEFAULT_DESTINATION_LABEL = "";
    public static final String DEFAULT_DESTINATION_BUTTON_TEXT = "";
    
    public static final String DEFAULT_INSTALLATION_LOCATION = "";
}
