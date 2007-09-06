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
package org.netbeans.modules.visualweb.propertyeditors.css;

import org.netbeans.modules.visualweb.propertyeditors.css.model.BackgroundModel;
import org.netbeans.modules.visualweb.propertyeditors.css.model.BackgroundPositionData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.CssStyleData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.PropertyData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.Utils;
import com.sun.rave.designtime.DesignProperty;
import java.awt.BorderLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;

/**
 * Background Style editor.
 * @author  Winston Prakash
 *          Jeff Hoffman (HIE design)
 */
public class BackgroundStyleEditor extends StyleEditor implements PropertyChangeListener{
    
    CssStyleData cssStyleData = null;
    static File currentFile = null;
    
    BackgroundModel backgroundModel = new BackgroundModel();
    
    ColorSelectionField colorField =  new ColorSelectionField();
    BackgroundPositionData bgPositionData = new BackgroundPositionData();
    
    DesignProperty designProperty = null;
    
    /** Creates new form FontStyleEditor */
    public BackgroundStyleEditor(CssStyleData styleData) {
        cssStyleData = styleData;
        setName("backgroundStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(StyleBuilderDialog.class, "BACKGROUND_EDITOR_DISPNAME"));
        initComponents();
        colorSelectionPanel.add(colorField,BorderLayout.CENTER);
        colorField.addCssPropertyChangeListener(this);
        initialize();
        
        // Add editor listeners to the horizontal position combobox
        final JTextField horizontalPosComboBoxEditor = (JTextField) horizontalPosComboBox.getEditor().getEditorComponent();
        horizontalPosComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        horizontalUnitComboBox.setEnabled(Utils.isInteger(horizontalPosComboBoxEditor.getText()));
                        enablePositionCombo();
                    }
                });
            }
        });
        
        // Add editor listeners to the vertical position combobox
        final JTextField verticalPosComboBoxEditor = (JTextField) verticalPosComboBox.getEditor().getEditorComponent();
        verticalPosComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        verticalUnitComboBox.setEnabled(Utils.isInteger(verticalPosComboBoxEditor.getText()));
                    }
                });
                
            }
        });
    }
    
    public void setDesignProperty(DesignProperty liveProperty){
        this.designProperty = liveProperty;
    }
    
    private void initialize(){
        // Set the Bckground Color to the GUI
        String backGroundColor = cssStyleData.getProperty(CssStyleData.BACKGROUND_COLOR);
        if(backGroundColor != null){
            colorField.setColorString(backGroundColor);
        }
        
        // Set the Bckground Image name to the GUI
        String backGroundImage = cssStyleData.getProperty(CssStyleData.BACKGROUND_IMAGE);
        if(backGroundImage != null){
            String imgString = backGroundImage.substring(backGroundImage.indexOf("(")+1,backGroundImage.indexOf(")")); //NOI18N
            imageFileField.setText(imgString);
        }else{
            imageFileField.setText(CssStyleData.NOT_SET);
        }
        
        // Set the background repeat info to the GUI
        DefaultComboBoxModel backgroundRepeatList = backgroundModel.getBackgroundRepeatList();
        repeatComboBox.setModel(backgroundRepeatList);
        String backGroundRepeat = cssStyleData.getProperty(CssStyleData.BACKGROUND_REPEAT);
        if(backGroundRepeat != null){
            repeatComboBox.setSelectedItem(backGroundRepeat);
        }else{
            repeatComboBox.setSelectedIndex(0);
        }
        
        // Set the background scroll to the GUI
        DefaultComboBoxModel backgroundScrollList = backgroundModel.getBackgroundScrollList();
        scrollComboBox.setModel(backgroundScrollList);
        String backGroundScroll = cssStyleData.getProperty(CssStyleData.BACKGROUND_ATTACHMENT);
        if(backGroundScroll != null){
            scrollComboBox.setSelectedItem(backGroundScroll);
        }else{
            scrollComboBox.setSelectedIndex(0);
        }
        
        // Set the background poistion data to the GUI
        horizontalPosComboBox.setModel(backgroundModel.getBackgroundPositionList());
        verticalPosComboBox.setModel(backgroundModel.getBackgroundPositionList());
        horizontalUnitComboBox.setModel(backgroundModel.getBackgroundPositionUnitList());
        verticalUnitComboBox.setModel(backgroundModel.getBackgroundPositionUnitList());
        
        String backgroundPosition = cssStyleData.getProperty(CssStyleData.BACKGROUND_POSITION);
        if(backgroundPosition != null){
            bgPositionData.setBackgroundPosition(backgroundPosition);
            horizontalPosComboBox.setSelectedItem(bgPositionData.getHorizontalValue());
            horizontalUnitComboBox.setSelectedItem(bgPositionData.getHorizontalUnit());
            verticalPosComboBox.setSelectedItem(bgPositionData.getVerticalValue());
            verticalUnitComboBox.setSelectedItem(bgPositionData.getVerticalUnit());
        }else{
            horizontalPosComboBox.setSelectedIndex(0);
            verticalPosComboBox.setSelectedIndex(0);
        }
        
        verticalPosComboBox.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        
        horizontalPosComboBox.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        
        scrollComboBox.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        
    }
    
    /** Listens to the color property change in the color chooser filed */
    public void propertyChange(PropertyChangeEvent evt) {
        setBackgroundColor();
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        colorPanel = new javax.swing.JPanel();
        colorLabel = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        imageFileField = new javax.swing.JTextField();
        lineHeightLabel = new javax.swing.JLabel();
        imageTileLabel = new javax.swing.JLabel();
        imageScrollLabel = new javax.swing.JLabel();
        scrollComboBox = new javax.swing.JComboBox();
        repeatComboBox = new javax.swing.JComboBox();
        horizontalPosLabel = new javax.swing.JLabel();
        verticalPosLabel = new javax.swing.JLabel();
        horizontalPosComboBox = new javax.swing.JComboBox();
        verticalPosComboBox = new javax.swing.JComboBox();
        horizontalUnitComboBox = new javax.swing.JComboBox();
        verticalUnitComboBox = new javax.swing.JComboBox();
        colorSelectionPanel = new javax.swing.JPanel();
        errorPanel = new javax.swing.JPanel();
        errorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        colorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        colorPanel.setLayout(new java.awt.GridBagLayout());

        colorLabel.setLabelFor(colorPanel);
        colorLabel.setText(org.openide.util.NbBundle.getMessage(BackgroundStyleEditor.class, "BACKGROUND_COLOR")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        colorPanel.add(colorLabel, gridBagConstraints);

        browseButton.setText("...");
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle"); // NOI18N
        browseButton.setToolTipText(bundle.getString("BG_SELECTION_BUTTON_TOOLTIP")); // NOI18N
        browseButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        browseButton.setPreferredSize(new java.awt.Dimension(20, 20));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 3, 0, 0);
        colorPanel.add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleName(bundle.getString("BG_SELECTION_BUTTON_ACCESSIBLE_NAME")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(bundle.getString("BG_SELECTION_BUTTON_ACCESSIBLE_DESC")); // NOI18N

        imageFileField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageFileFieldActionPerformed(evt);
            }
        });
        imageFileField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                imageFileFieldFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        colorPanel.add(imageFileField, gridBagConstraints);
        imageFileField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BackgroundStyleEditor.class, "BG_IMAGE_FIELD_ACCESS_DESC")); // NOI18N

        lineHeightLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("BACKGROUND_IMAGE_MNEMONIC").charAt(0));
        lineHeightLabel.setLabelFor(imageFileField);
        lineHeightLabel.setText(org.openide.util.NbBundle.getMessage(BackgroundStyleEditor.class, "BACKGROUND_IMAGE")); // NOI18N
        lineHeightLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        colorPanel.add(lineHeightLabel, gridBagConstraints);

        imageTileLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("BACKGROUNDTILE_MNEMONIC").charAt(0));
        imageTileLabel.setLabelFor(repeatComboBox);
        imageTileLabel.setText(org.openide.util.NbBundle.getMessage(BackgroundStyleEditor.class, "BACKGROUNDTILE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        colorPanel.add(imageTileLabel, gridBagConstraints);

        imageScrollLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("BACKGROUND_SCROLL_MNEMONIC").charAt(0));
        imageScrollLabel.setLabelFor(scrollComboBox);
        imageScrollLabel.setText(org.openide.util.NbBundle.getMessage(BackgroundStyleEditor.class, "BACKGROUND_SCROLL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        colorPanel.add(imageScrollLabel, gridBagConstraints);

        scrollComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                scrollComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        colorPanel.add(scrollComboBox, gridBagConstraints);
        scrollComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("SCROLL_COMBO_ACCESSIBLE_DESC")); // NOI18N

        repeatComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                repeatComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        colorPanel.add(repeatComboBox, gridBagConstraints);
        repeatComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("TILE_COMBO_ACCESSIBLE_DESC")); // NOI18N

        horizontalPosLabel.setLabelFor(horizontalPosComboBox);
        horizontalPosLabel.setText(org.openide.util.NbBundle.getMessage(BackgroundStyleEditor.class, "BG_HORIZONTAL_POS")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        colorPanel.add(horizontalPosLabel, gridBagConstraints);

        verticalPosLabel.setLabelFor(verticalPosComboBox);
        verticalPosLabel.setText(org.openide.util.NbBundle.getMessage(BackgroundStyleEditor.class, "BG_VERTICAL_POS")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        colorPanel.add(verticalPosLabel, gridBagConstraints);

        horizontalPosComboBox.setEditable(true);
        horizontalPosComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                horizontalPosComboBoxItemStateChanged(evt);
            }
        });
        horizontalPosComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                horizontalPosComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        colorPanel.add(horizontalPosComboBox, gridBagConstraints);
        horizontalPosComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("HORIZPOS_COMBO_ACCESSIBLE_DESC")); // NOI18N

        verticalPosComboBox.setEditable(true);
        verticalPosComboBox.setEnabled(false);
        verticalPosComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                verticalPosComboBoxItemStateChanged(evt);
            }
        });
        verticalPosComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verticalPosComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        colorPanel.add(verticalPosComboBox, gridBagConstraints);
        verticalPosComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("VERTPOS_COMBO_ACCESSIBLE_DESC")); // NOI18N

        horizontalUnitComboBox.setEnabled(false);
        horizontalUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                horizontalUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        colorPanel.add(horizontalUnitComboBox, gridBagConstraints);
        horizontalUnitComboBox.getAccessibleContext().setAccessibleName(bundle.getString("HORIZPOS_UNIT_COMBO_ACCESSIBLE_NAME")); // NOI18N
        horizontalUnitComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("HORIZPOS_UNIT_COMBO_ACCESSIBLE_DESC")); // NOI18N

        verticalUnitComboBox.setEnabled(false);
        verticalUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                verticalUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        colorPanel.add(verticalUnitComboBox, gridBagConstraints);
        verticalUnitComboBox.getAccessibleContext().setAccessibleName(bundle.getString("VERTPOS_UNIT_COMBO_ACCESSIBLE_NAME")); // NOI18N
        verticalUnitComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("VERTPOS_UNIT_COMBO_ACCESSIBLE_DESC")); // NOI18N

        colorSelectionPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        colorPanel.add(colorSelectionPanel, gridBagConstraints);

        add(colorPanel, java.awt.BorderLayout.NORTH);

        errorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        errorPanel.setLayout(new java.awt.BorderLayout());

        errorLabel.setForeground(new java.awt.Color(0, 0, 153));
        errorLabel.setMinimumSize(new java.awt.Dimension(200, 20));
        errorLabel.setPreferredSize(new java.awt.Dimension(200, 20));
        errorPanel.add(errorLabel, java.awt.BorderLayout.CENTER);

        add(errorPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
    
    private void verticalUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_verticalUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            bgPositionData.setVerticalUnit((String)verticalUnitComboBox.getSelectedItem());
            setBackgroundPosition();
        }
    }//GEN-LAST:event_verticalUnitComboBoxItemStateChanged
    
    private void horizontalUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_horizontalUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            bgPositionData.setHorizontalUnit((String)horizontalUnitComboBox.getSelectedItem());
            setBackgroundPosition();
        }
    }//GEN-LAST:event_horizontalUnitComboBoxItemStateChanged
    
    private void verticalPosComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_verticalPosComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            bgPositionData.setVerticalValue((String)verticalPosComboBox.getSelectedItem());
            setBackgroundPosition();
        }
    }//GEN-LAST:event_verticalPosComboBoxItemStateChanged
    
    private void verticalPosComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verticalPosComboBoxActionPerformed
        bgPositionData.setVerticalValue((String)verticalPosComboBox.getSelectedItem());
        setBackgroundPosition();
    }//GEN-LAST:event_verticalPosComboBoxActionPerformed
    
    private void horizontalPosComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_horizontalPosComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            bgPositionData.setHorizontalValue((String)horizontalPosComboBox.getSelectedItem());
            setBackgroundPosition();
        }
    }//GEN-LAST:event_horizontalPosComboBoxItemStateChanged
    
    private void horizontalPosComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_horizontalPosComboBoxActionPerformed
        bgPositionData.setHorizontalValue((String)horizontalPosComboBox.getSelectedItem());
        setBackgroundPosition();
    }//GEN-LAST:event_horizontalPosComboBoxActionPerformed
    
    private void scrollComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_scrollComboBoxItemStateChanged
        setBackgroundAttachment();
    }//GEN-LAST:event_scrollComboBoxItemStateChanged
    
    private void repeatComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_repeatComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setBackgroundRepeat();
        }
    }//GEN-LAST:event_repeatComboBoxItemStateChanged
    
    private void imageFileFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_imageFileFieldFocusLost
        setBackgroundImage();
    }//GEN-LAST:event_imageFileFieldFocusLost
    
    private void imageFileFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageFileFieldActionPerformed
        setBackgroundImage();
    }//GEN-LAST:event_imageFileFieldActionPerformed
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        
        BackgroundImageUrlDialog imageUrlDialog = new BackgroundImageUrlDialog();
        if(designProperty != null){
            imageUrlDialog.setDesignContext(designProperty.getDesignBean().getDesignContext());
        }
        imageUrlDialog.initialize();
        imageUrlDialog.showDialog();
        String imgResource = (String)imageUrlDialog.getPropertyValue();
        if(imgResource.startsWith("/")){
            imgResource = "." + imgResource;
        }
        StringBuffer sb = new StringBuffer();
        int len = imgResource.length();
        for (int i = 0; i < len; i++) {
            char chr =  imgResource.charAt(i);
            if (chr == ' '){
                sb.append("%20");
            }else{
               sb.append(chr); 
            }
        }
        imgResource = sb.toString();
        imageFileField.setText(imgResource);
        setBackgroundImage();
    }//GEN-LAST:event_browseButtonActionPerformed
    
    class ImageFileFilter extends FileFilter {
        
        //Accept all directories and all ".jar" files.
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');
            
            if (i > 0 &&  i < s.length() - 1) {
                extension = s.substring(i+1).toLowerCase();
            }
            
            if (extension != null) {
                if (extension.toLowerCase().equals("gif") || //NOI18N
                extension.toLowerCase().equals("jpg") || //NOI18N
                extension.toLowerCase().equals("png") ) { //NOI18N
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }
        
        //The description of this filter
        public String getDescription() {
            return NbBundle.getMessage(BackgroundStyleEditor.class, "IMAGE_FILE_FILTER");
        }
    }
    
    private void  setBackgroundColor(){
        PropertyData backgroundColorData = new PropertyData();
        backgroundColorData.setValue(colorField.getColorString());
        cssStyleData.modifyProperty(CssStyleData.BACKGROUND_COLOR, backgroundColorData.toString());
    }
    
    private void  setBackgroundImage(){
        PropertyData backgroundImageData = new PropertyData();
        String imgPath = imageFileField.getText();
        if((imgPath == null) || (imgPath.equals(""))) {
            imgPath = CssStyleData.NOT_SET;
            imageFileField.setText(imgPath);
        }
        backgroundImageData.setValue("url(" + imgPath + ")"); //NOI18N
        cssStyleData.modifyProperty(CssStyleData.BACKGROUND_IMAGE, backgroundImageData.toString());
    }
    
    private void  setBackgroundRepeat(){
        PropertyData backgroundRepeatData = new PropertyData();
        backgroundRepeatData.setValue(repeatComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.BACKGROUND_REPEAT, backgroundRepeatData.toString());
    }
    
    private void  setBackgroundAttachment(){
        PropertyData backgroundAttachmentData = new PropertyData();
        backgroundAttachmentData.setValue(scrollComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.BACKGROUND_ATTACHMENT, backgroundAttachmentData.toString());
    }
    
    private void  setBackgroundPosition(){
        String backgroundPosition = bgPositionData.toString();
        cssStyleData.modifyProperty(CssStyleData.BACKGROUND_POSITION, backgroundPosition);
        enablePositionCombo();
    }
    
    private void enablePositionCombo(){
        String horizontalPos = bgPositionData.getHorizontalValue();
        if (Utils.isInteger(horizontalPos)){
            horizontalUnitComboBox.setEnabled(true);
        }else{
            horizontalUnitComboBox.setEnabled(false);
        }
        if((horizontalPos.equals("") || horizontalPos.startsWith(CssStyleData.NOT_SET) || horizontalPos.startsWith(CssStyleData.VALUE))){
            verticalPosComboBox.setEnabled(false);
            verticalUnitComboBox.setEnabled(false);
        }else{
            verticalPosComboBox.setEnabled(true);
            verticalUnitComboBox.setEnabled(true);
        }
        String verticalPos = bgPositionData.getVerticalValue();
        if (Utils.isInteger(verticalPos)){
            verticalUnitComboBox.setEnabled(true);
        }else{
            verticalUnitComboBox.setEnabled(false);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel colorLabel;
    private javax.swing.JPanel colorPanel;
    private javax.swing.JPanel colorSelectionPanel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JPanel errorPanel;
    private javax.swing.JComboBox horizontalPosComboBox;
    private javax.swing.JLabel horizontalPosLabel;
    private javax.swing.JComboBox horizontalUnitComboBox;
    private javax.swing.JTextField imageFileField;
    private javax.swing.JLabel imageScrollLabel;
    private javax.swing.JLabel imageTileLabel;
    private javax.swing.JLabel lineHeightLabel;
    private javax.swing.JComboBox repeatComboBox;
    private javax.swing.JComboBox scrollComboBox;
    private javax.swing.JComboBox verticalPosComboBox;
    private javax.swing.JLabel verticalPosLabel;
    private javax.swing.JComboBox verticalUnitComboBox;
    // End of variables declaration//GEN-END:variables
    
}
