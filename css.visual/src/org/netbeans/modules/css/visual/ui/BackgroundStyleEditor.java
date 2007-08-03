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

/*
 * BackgroundStyleEditor.java
 *
 * Created on October 13, 2004, 12:23 PM
 */

package org.netbeans.modules.css.visual.ui;

import org.netbeans.modules.css.visual.model.BackgroundModel;
import org.netbeans.modules.css.visual.model.BackgroundPositionData;
import org.netbeans.modules.css.visual.model.CssProperties;
import org.netbeans.modules.css.model.CssRuleContent;
import org.netbeans.modules.css.visual.model.PropertyData;
import org.netbeans.modules.css.visual.model.PropertyData;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.netbeans.modules.css.visual.model.Utils;
import org.openide.util.NbBundle;


/**
 * Background Style editor.
 * @author  Winston Prakash
 * @version 1.0
 */
public class BackgroundStyleEditor extends StyleEditor {

    ColorSelectionField colorField =  new ColorSelectionField();
    BackgroundPositionData bgPositionData = new BackgroundPositionData();

    /** Creates new form FontStyleEditor */
    public BackgroundStyleEditor() {
        setName("backgroundStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(BackgroundStyleEditor.class, "BACKGROUND_EDITOR_DISPNAME"));
        initComponents();
        colorSelectionPanel.add(colorField,BorderLayout.CENTER);
        colorField.addPropertyChangeListener("color", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                setBackgroundColor();
            }
        });
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

    protected void initialize(){
        // Set the background repeat info to the GUI
        BackgroundModel backgroundModel = new BackgroundModel();
        DefaultComboBoxModel backgroundRepeatList = backgroundModel.getBackgroundRepeatList();
        repeatComboBox.setModel(backgroundRepeatList);

        // Set the background scroll to the GUI
        DefaultComboBoxModel backgroundScrollList = backgroundModel.getBackgroundScrollList();
        scrollComboBox.setModel(backgroundScrollList);

        // Set the background poistion data to the GUI

        horizontalPosComboBox.setModel(backgroundModel.getBackgroundPositionList());
        verticalPosComboBox.setModel(backgroundModel.getBackgroundPositionList());
        horizontalUnitComboBox.setModel(backgroundModel.getBackgroundPositionUnitList());
        verticalUnitComboBox.setModel(backgroundModel.getBackgroundPositionUnitList());
    }

    /**
     * Set the CSS Properties Values from the CssStyleData data structure
     * to the GUI components.
     */
    protected void setCssPropertyValues(CssRuleContent cssStyleData){
        removeCssPropertyChangeListener();
        
        // Set the Bckground Color to the GUI
        String backGroundColor = cssStyleData.getProperty(CssProperties.BACKGROUND_COLOR);
        if(backGroundColor != null){
            colorField.setColorString(backGroundColor);
        }else{
            imageFileField.setText(CssRuleContent.NOT_SET);
        }
        
        // Set the Bckground Image name to the GUI
        String backGroundImage = cssStyleData.getProperty(CssProperties.BACKGROUND_IMAGE);
        if(backGroundImage != null && !backGroundImage.trim().equals("")){
            int openBracketPos = backGroundImage.indexOf("(");
            int endBracketPos = backGroundImage.indexOf(")");
            if((openBracketPos >= 0) && (endBracketPos >= 0)){
                String imgString = backGroundImage.substring(openBracketPos + 1, endBracketPos);
                imageFileField.setText(imgString);
            }else{
                imageFileField.setText(backGroundImage);
            }
        }else{
            imageFileField.setText(CssRuleContent.NOT_SET);
        }
        
        String backGroundRepeat = cssStyleData.getProperty(CssProperties.BACKGROUND_REPEAT);
        if(backGroundRepeat != null){
            repeatComboBox.setSelectedItem(backGroundRepeat);
        }else{
            repeatComboBox.setSelectedIndex(0);
        }
        
        String backGroundScroll = cssStyleData.getProperty(CssProperties.BACKGROUND_ATTACHMENT);
        if(backGroundScroll != null){
            scrollComboBox.setSelectedItem(backGroundScroll);
        }else{
            scrollComboBox.setSelectedIndex(0);
        }
        
        String backgroundPosition = cssStyleData.getProperty(CssProperties.BACKGROUND_POSITION);
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
        setCssPropertyChangeListener(cssStyleData);
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

        colorLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_BACKGROUND_COLOR").charAt(0));
        colorLabel.setLabelFor(colorPanel);
        colorLabel.setText(org.openide.util.NbBundle.getMessage(BackgroundStyleEditor.class, "BACKGROUND_COLOR")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        colorPanel.add(colorLabel, gridBagConstraints);

        browseButton.setText("...");
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
        browseButton.getAccessibleContext().setAccessibleName(null);
        browseButton.getAccessibleContext().setAccessibleDescription(null);

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
        imageFileField.getAccessibleContext().setAccessibleName(null);
        imageFileField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BackgroundStyleEditor.class, "BG_IMG_TEXTFIELD_ACCESS_DESC")); // NOI18N

        lineHeightLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_BACKGROUND_IMAGE").charAt(0));
        lineHeightLabel.setLabelFor(imageFileField);
        lineHeightLabel.setText(org.openide.util.NbBundle.getMessage(BackgroundStyleEditor.class, "BACKGROUND_IMAGE")); // NOI18N
        lineHeightLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        colorPanel.add(lineHeightLabel, gridBagConstraints);

        imageTileLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_BACKGROUNDTILE").charAt(0));
        imageTileLabel.setLabelFor(repeatComboBox);
        imageTileLabel.setText(org.openide.util.NbBundle.getMessage(BackgroundStyleEditor.class, "BACKGROUNDTILE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        colorPanel.add(imageTileLabel, gridBagConstraints);

        imageScrollLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_BACKGROUND_SCROLL").charAt(0));
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
        scrollComboBox.getAccessibleContext().setAccessibleName(null);
        scrollComboBox.getAccessibleContext().setAccessibleDescription(null);

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
        repeatComboBox.getAccessibleContext().setAccessibleName(null);
        repeatComboBox.getAccessibleContext().setAccessibleDescription(null);

        horizontalPosLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_BG_HORIZONTAL_POS").charAt(0));
        horizontalPosLabel.setLabelFor(horizontalPosComboBox);
        horizontalPosLabel.setText(org.openide.util.NbBundle.getMessage(BackgroundStyleEditor.class, "BG_HORIZONTAL_POS")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        colorPanel.add(horizontalPosLabel, gridBagConstraints);

        verticalPosLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_BG_VERTICAL_POS").charAt(0));
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
        horizontalPosComboBox.getAccessibleContext().setAccessibleName(null);
        horizontalPosComboBox.getAccessibleContext().setAccessibleDescription(null);

        verticalPosComboBox.setEditable(true);
        verticalPosComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verticalPosComboBoxActionPerformed(evt);
            }
        });
        verticalPosComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                verticalPosComboBoxItemStateChanged(evt);
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
        verticalPosComboBox.getAccessibleContext().setAccessibleName(null);
        verticalPosComboBox.getAccessibleContext().setAccessibleDescription(null);

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
        horizontalUnitComboBox.getAccessibleContext().setAccessibleName(null);
        horizontalUnitComboBox.getAccessibleContext().setAccessibleDescription(null);

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
        verticalUnitComboBox.getAccessibleContext().setAccessibleName(null);
        verticalUnitComboBox.getAccessibleContext().setAccessibleDescription(null);

        colorSelectionPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        colorPanel.add(colorSelectionPanel, gridBagConstraints);

        add(colorPanel, java.awt.BorderLayout.NORTH);

        errorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        errorPanel.setLayout(new java.awt.BorderLayout());

        errorLabel.setForeground(new java.awt.Color(0, 0, 153));
        errorPanel.add(errorLabel, java.awt.BorderLayout.NORTH);

        add(errorPanel, java.awt.BorderLayout.CENTER);
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
        BackgroundImageUrlDialog imageUrlDialog = new BackgroundImageUrlDialog(content().fileObject());
        if(imageUrlDialog.show(this)){
            imageFileField.setText(imageUrlDialog.getImageUrl());
        }
        setBackgroundImage();
    }//GEN-LAST:event_browseButtonActionPerformed
    
    private void  setBackgroundColor(){
        PropertyData backgroundColorData = new PropertyData();
        backgroundColorData.setValue(colorField.getColorString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.BACKGROUND_COLOR, null, backgroundColorData.toString());
    }
    
    private void  setBackgroundImage(){
        PropertyData backgroundImageData = new PropertyData();
        String imgPath = imageFileField.getText();
        if((imgPath == null) || (imgPath.equals(""))) {
            imgPath = CssRuleContent.NOT_SET;
            imageFileField.setText(imgPath);
        }
        if(!imgPath.equals(CssRuleContent.NOT_SET)){
            backgroundImageData.setValue("url(" + imgPath + ")"); //NOI18N
        }else{
            backgroundImageData.setValue(CssRuleContent.NOT_SET);
        }
        cssPropertyChangeSupport().firePropertyChange(CssProperties.BACKGROUND_IMAGE, null, backgroundImageData.toString());
    }
    
    private void  setBackgroundRepeat(){
        PropertyData backgroundRepeatData = new PropertyData();
        backgroundRepeatData.setValue(repeatComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.BACKGROUND_REPEAT, null, backgroundRepeatData.toString());
    }
    
    private void  setBackgroundAttachment(){
        PropertyData backgroundAttachmentData = new PropertyData();
        backgroundAttachmentData.setValue(scrollComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.BACKGROUND_ATTACHMENT, null, backgroundAttachmentData.toString());
    }
    
    private void  setBackgroundPosition(){
        cssPropertyChangeSupport().firePropertyChange(CssProperties.BACKGROUND_POSITION, null, bgPositionData.toString());
        enablePositionCombo();
    }
    
    private void enablePositionCombo(){
        String horizontalPos = bgPositionData.getHorizontalValue();
        if (Utils.isInteger(horizontalPos)){
            horizontalUnitComboBox.setEnabled(true);
        }else{
            horizontalUnitComboBox.setEnabled(false);
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
