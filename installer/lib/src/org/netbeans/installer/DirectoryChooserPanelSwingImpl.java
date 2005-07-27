/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import com.installshield.util.Log;
import com.installshield.util.LocalizedStringResolver;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.swing.SwingWizardUI;
import com.installshield.wizard.swing.SwingWizardPanelImpl;

import com.installshield.wizardx.i18n.WizardXResourcesConst;
import com.installshield.product.i18n.ProductResourcesConst;

import com.installshield.util.MnemonicString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.basic.BasicFileChooserUI;

public class DirectoryChooserPanelSwingImpl
extends SwingWizardPanelImpl implements  ActionListener, ListSelectionListener
{
    private JTextField inputTextField;
    private JButton browseButton;
    private JList listList;
 
    private JTextArea infoTextArea;
    private JPanel inputPanel;    

    public void build(WizardBuilderSupport support) {
        super.build(support);
        try {
            support.putClass(Util.class.getName());
        }
        catch(Exception exception) {
            support.logEvent(this, Log.ERROR, exception);
        }
    }
    
    public void initialize(WizardBeanEvent event) {
        super.initialize(event);
        
        setLayout(new BorderLayout(0, 20));
        
        infoTextArea = new JTextArea();
        infoTextArea.setWrapStyleWord(true);
        infoTextArea.setLineWrap(true);
        infoTextArea.setEditable(false);
        infoTextArea.setBackground(getBackground());
        infoTextArea.setDisabledTextColor(getBackground());
                
        String description = getPanel().getDescription();
        infoTextArea.setText(resolveString(description));
        
        add(infoTextArea, BorderLayout.NORTH);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 20));
        add(panel, BorderLayout.CENTER);
        
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
                
        panel.add(inputPanel, BorderLayout.NORTH);
                                           
        JPanel inputLabelPanel = new JPanel();        
        inputLabelPanel.setLayout(new BorderLayout());
        
        JLabel inputLabel = new JLabel();
        
        MnemonicString inputLabelMn = 
            new MnemonicString(resolveString(getDirectoryChooserPanel().getDestinationCaption()));
        inputLabel.setText(inputLabelMn.toString());
        if (inputLabelMn.isMnemonicSpecified()) {
            inputLabel.setDisplayedMnemonic(inputLabelMn.getMnemonicChar());
        }
        inputLabelPanel.add(inputLabel);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 3, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        inputPanel.add(inputLabelPanel, gbc);
        
        // input entry
        inputTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 3, 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        inputLabel.setLabelFor(inputTextField);
        inputPanel.add(inputTextField, gbc);
        
        inputTextField.getAccessibleContext().setAccessibleName(resolveString(getDirectoryChooserPanel().getDescription()));


        browseButton = new JButton();
        MnemonicString browseMn = new MnemonicString(resolveString(getDirectoryChooserPanel().getBrowseCaption()));
        browseButton.setText(browseMn.toString());
        if (browseMn.isMnemonicSpecified()) {
            browseButton.setMnemonic(browseMn.getMnemonicChar());
        }
        
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 12, 3, 0);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(browseButton, gbc);
        browseButton.addActionListener(this);
        browseButton.getAccessibleContext().setAccessibleName(LocalizedStringResolver.resolve(ProductResourcesConst.NAME, "DestinationPanelSwingImpl.browse"));
        
        if (getDirectoryChooserPanel().getDestinations().size() != 0) {
            listList = new JList(getDirectoryChooserPanel().getDestinationDescriptions());
            listList.setSelectedIndex(Integer.parseInt(getDirectoryChooserPanel().getSelectedDestinationIndex()));            
            listList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listList.addListSelectionListener(this);

            JScrollPane listScrollPane = new JScrollPane();
            listScrollPane.setViewportView(listList);

            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BorderLayout(3, 3));
    
            JLabel listLabel = new JLabel();
            listLabel.setHorizontalAlignment(SwingConstants.LEFT);

            MnemonicString listLabelMn = new MnemonicString(resolveString(getDirectoryChooserPanel().getDestinationsCaption()));
            listLabel.setText(listLabelMn.toString());
            if (listLabelMn.isMnemonicSpecified()) {
                listLabel.setDisplayedMnemonic(listLabelMn.getMnemonicChar());
                listLabel.setLabelFor(listList);
            }
        
            listPanel.add(listLabel, java.awt.BorderLayout.NORTH);
            listPanel.add(listScrollPane, BorderLayout.CENTER);
                        
            panel.add(listPanel, BorderLayout.CENTER);

            inputTextField.setText(getDirectoryChooserPanel().getDestinationValue(Integer.parseInt(getDirectoryChooserPanel().getSelectedDestinationIndex())));
        }        
    }
        
    public void entered(WizardBeanEvent event){
        inputTextField.requestFocusInWindow();
    }

    public void exiting(WizardBeanEvent evt) {
        super.exiting(evt);
        getDirectoryChooserPanel().setDestination(inputTextField.getText());
    }
                    
    public void actionPerformed(ActionEvent event) {
            SwingWizardUI wizardUI = (SwingWizardUI)getPanel().getWizard().getUI();
            if (wizardUI != null) {
                wizardUI.restoreDefaultColors();
            }
            JFileChooser fc = new JFileChooser() {
                public boolean accept(java.io.File f) {
                    return f.isDirectory();
                    
                }
                public void setCurrentDirectory(java.io.File f) {
                    
                    super.setCurrentDirectory(f);
                    
                    FileChooserUI ui = getUI();
                    
                    if (ui instanceof BasicFileChooserUI) {
                        ((BasicFileChooserUI)ui).setFileName("");
                    }
                }
            };
            
            fc.setDialogTitle(LocalizedStringResolver.resolve(WizardXResourcesConst.NAME, "DirectoryInputComponent.selectDirectory"));
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            // remember pref size before displaying to restore after setting selected file (file name
            // can cause dialog to stretch horizontally)
            Dimension prefSize = fc.getPreferredSize();
            fc.setSelectedFile(new java.io.File(inputTextField.getText()));
            fc.setPreferredSize(prefSize);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    inputTextField.setText(fc.getSelectedFile().getCanonicalPath());
                } catch (java.io.IOException e) {
                    inputTextField.setText(fc.getSelectedFile().getPath());
                }
                inputTextField.requestFocus();
                inputTextField.selectAll();
            }
            if (wizardUI != null) {
                wizardUI.setWizardColors();
            }
    }
    
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        inputTextField.setText(getDirectoryChooserPanel().getDestinationValue(listList.getSelectedIndex()));
        inputTextField.selectAll();
    }
    
    private DirectoryChooserPanel getDirectoryChooserPanel() {
        return (DirectoryChooserPanel) getPanel();
    }
}
