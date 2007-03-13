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
 * FontStyleEditor.java
 *
 * Created on October 13, 2004, 12:23 PM
 */

package org.netbeans.modules.css.visual.ui;

import org.netbeans.modules.css.visual.model.CssProperties;
import org.netbeans.modules.css.visual.model.CssStyleData;
import org.netbeans.modules.css.visual.model.FontModel;
import org.netbeans.modules.css.visual.model.PropertyData;
import org.netbeans.modules.css.visual.model.PropertyWithUnitData;
import org.netbeans.modules.css.visual.model.TextDecorationData;
import org.netbeans.modules.css.visual.model.PropertyData;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.css.visual.model.Utils;
import org.openide.util.NbBundle;

/**
 * Font Style editor.
 * @author  Winston Prakash
 * @version 1.0
 */
public class FontStyleEditor extends StyleEditor implements PropertyChangeListener {

    ColorSelectionField colorField =  new ColorSelectionField();
    TextDecorationData textDecorationData = new TextDecorationData();

    FontModel fontModel = new FontModel();
    DefaultListModel fontFamilies = fontModel.getFontFamilySetList();

    DefaultListModel fontSizes;
    DefaultComboBoxModel fontSizeUnits;
    DefaultComboBoxModel fontStyles;
    DefaultComboBoxModel fontWeights;
    DefaultComboBoxModel fontVariants;

    String temporaryFontSet = null;

    /** Creates new form FontStyleEditor */
    public FontStyleEditor() {
        setName("fontStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(FontStyleEditor.class, "FONT_EDITOR_DISPNAME"));
        initComponents();
        colorSelectionPanel.add(colorField,BorderLayout.CENTER);
        colorField.addPropertyChangeListener(this);
        initialize();
    }

    private void initialize(){
        // Set the font family to the GUI
        fontModel = new FontModel();
        fontFamilies = fontModel.getFontFamilySetList();
        fontFaceList.setModel(fontFamilies);

        // Set the font size to the GUI
        fontSizes= fontModel.getFontSizeList();
        fontSizeList.setModel(fontSizes);

        fontSizeUnits= fontModel.getFontSizeUnitList();
        fontSizeUnitCombo.setModel(fontSizeUnits);
        fontSizeUnitCombo.setSelectedItem("px");

        // Set the font Style to the GUI
        fontStyles = fontModel.getFontStyleList();
        fontStyleComboBox.setModel(fontStyles);

        // Set the font Weight to the GUI
        fontWeights = fontModel.getFontWeightList();
        fontWeightComboBox.setModel(fontWeights);

        // Set the font Variant to the GUI
        fontVariants = fontModel.getFontVariantList();
        fontVariantComboBox.setModel(fontVariants);

        FontMetrics fontMetrics = fontSizeField.getFontMetrics(fontSizeField.getFont());
        int width = fontMetrics.stringWidth((String) fontSizes.get(0)) + 10;
        int height = (fontMetrics.getHeight() + 10) > 25 ? fontMetrics.getHeight() + 10 : 25;
        fontSizeField.setPreferredSize(new Dimension (width, height));
    }

    /**
     * Set the CSS Properties Values from the CssStyleData data structure
     * to the GUI components.
     */
    protected void setCssPropertyValues(CssStyleData cssStyleData){
        removeCssPropertyChangeListener();

        String fontFamily = cssStyleData.getProperty(CssProperties.FONT_FAMILY);
        if(fontFamily != null){
            // This is a work around. Since batik parser puts the quotes around
            // fonts with out spaces, remove the quotes from those fonts before
            // adding it to the list, else the font set will appear doubly - one
            // with quotes and one with out quotes.
            StringBuffer fontSetBuf = new StringBuffer();
            StringTokenizer st = new StringTokenizer(fontFamily.trim(),",");
            while(st.hasMoreTokens()){
                String fontName = st.nextToken();
                if(new StringTokenizer(fontName.trim()).countTokens() == 1){
                    fontName = fontName.replaceAll("'","");
                }
                fontSetBuf.append(fontName);
                if(st.hasMoreTokens()) fontSetBuf.append(",");
            }
            
            if(!fontFamilies.contains(fontSetBuf.toString())){
                // Work around to avoid adding the fonts to the list
                // when user edit the font family in the editor
                if (temporaryFontSet != null){
                    fontFamilies.removeElement(temporaryFontSet);
                }
                fontFamilies.add(1,fontSetBuf.toString());
                temporaryFontSet = fontSetBuf.toString();
            }
            fontFaceList.setSelectedIndex(fontFamilies.indexOf(fontSetBuf.toString()));
        }else{
            fontFaceList.setSelectedIndex(0);
        }
        
        String fontSizeStr = cssStyleData.getProperty(CssProperties.FONT_SIZE);
        if(fontSizeStr != null){
            // First check if absolute relative (large largere etc) font-size identifiers
            if(fontSizes.contains(fontSizeStr)){
                fontSizeList.setSelectedIndex(fontSizes.indexOf(fontSizeStr));
            }else{
                // Try splitiing in to numerical font size and unit
                FontModel.FontSize fontSize = fontModel.getFontSize(fontSizeStr);
                if(fontSize.getValue()!= null){
                    if(fontSizes.contains(fontSize.getValue())){
                        fontSizeList.setSelectedIndex(fontSizes.indexOf(fontSize.getValue()));
                    }else{
                        fontSizeField.setText(fontSize.getValue());
                    }
                }else{
                    fontSizeList.setSelectedIndex(0);
                }
                if(fontSizeUnits.getIndexOf(fontSize.getUnit()) != -1){
                    fontSizeUnitCombo.setSelectedIndex(fontSizeUnits.getIndexOf(fontSize.getUnit()));
                }
            }
        }else{
            fontSizeList.setSelectedIndex(0);
        }
        
        String fontStyle = cssStyleData.getProperty(CssProperties.FONT_STYLE);
        if(fontStyle != null){
            if(fontStyles.getIndexOf(fontStyle) != -1){
                fontStyleComboBox.setSelectedIndex(fontStyles.getIndexOf(fontStyle));
            } else{
                fontStyleComboBox.setSelectedIndex(0);
            }
        }else{
            fontStyleComboBox.setSelectedIndex(0);
        }
        
        String fontWeight = cssStyleData.getProperty(CssProperties.FONT_WEIGHT);
        if(fontWeight != null){
            if(fontWeights.getIndexOf(fontWeight) != -1){
                fontWeightComboBox.setSelectedIndex(fontWeights.getIndexOf(fontWeight));
            }else{
                fontWeightComboBox.setSelectedIndex(0);
            }
        }else{
            fontWeightComboBox.setSelectedIndex(0);
        }
        
        String fontVariant = cssStyleData.getProperty(CssProperties.FONT_VARIANT);
        if(fontVariant != null){
            if(fontVariants.getIndexOf(fontVariant) != -1){
                fontVariantComboBox.setSelectedIndex(fontVariants.getIndexOf(fontVariant));
            } else{
                fontVariantComboBox.setSelectedIndex(0);
            }
        }else{
            fontVariantComboBox.setSelectedIndex(0);
        }
        
        // Set the Text Decoration the GUI
        String textDecoration = cssStyleData.getProperty(CssProperties.TEXT_DECORATION);
        noDecorationCheckbox.setSelected(false);
        underlineCheckbox.setSelected(false);
        overlineCheckbox.setSelected(false);
        strikethroughCheckbox.setSelected(false);
        if(textDecoration != null){
            textDecorationData.setDecoration(textDecoration);
            if (textDecorationData.noDecorationEnabled()){
                noDecorationCheckbox.setSelected(true);
            }else{
                underlineCheckbox.setSelected(textDecorationData.underlineEnabled());
                overlineCheckbox.setSelected(textDecorationData.overlineEnabled());
                strikethroughCheckbox.setSelected(textDecorationData.lineThroughEnabled());
            }
        }
        
        // Set the Bckground Color the GUI
        String textColor = cssStyleData.getProperty(CssProperties.COLOR);
        if(textColor != null){
            colorField.setColorString(textColor);
        }else{
            colorField.setColorString(CssStyleData.NOT_SET);
        }
        
        setCssPropertyChangeListener(cssStyleData);
    }
    
    /** Listens to the color property change in the color chooser filed */
    public void propertyChange(PropertyChangeEvent evt) {
        setFontColor();
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        fontFamilyPanel = new javax.swing.JPanel();
        fontLabel = new javax.swing.JLabel();
        fontFaceScroll = new javax.swing.JScrollPane();
        fontFaceList = new javax.swing.JList();
        fontChosenField = new javax.swing.JTextField();
        fontFamilyButton = new javax.swing.JButton();
        fontSizePanel = new javax.swing.JPanel();
        sizeLabel = new javax.swing.JLabel();
        fontSizeField = new javax.swing.JTextField();
        fontSizeUnitCombo = new javax.swing.JComboBox();
        fontSizeScroll = new javax.swing.JScrollPane();
        fontSizeList = new javax.swing.JList();
        styleMainPanel = new javax.swing.JPanel();
        stylePanel = new javax.swing.JPanel();
        styleLabel = new javax.swing.JLabel();
        fontStyleComboBox = new javax.swing.JComboBox();
        weightLabel = new javax.swing.JLabel();
        fontWeightComboBox = new javax.swing.JComboBox();
        variantLabel = new javax.swing.JLabel();
        fontVariantComboBox = new javax.swing.JComboBox();
        colorLabel = new javax.swing.JLabel();
        colorSelectionPanel = new javax.swing.JPanel();
        decorationPanel = new javax.swing.JPanel();
        decorationLabel = new javax.swing.JLabel();
        underlineCheckbox = new javax.swing.JCheckBox();
        strikethroughCheckbox = new javax.swing.JCheckBox();
        overlineCheckbox = new javax.swing.JCheckBox();
        noDecorationCheckbox = new javax.swing.JCheckBox();
        errorPanel = new javax.swing.JPanel();
        errorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.BorderLayout());

        fontFamilyPanel.setLayout(new java.awt.GridBagLayout());

        fontFamilyPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fontLabel.setLabelFor(fontChosenField);
        fontLabel.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "Font_Family"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        fontFamilyPanel.add(fontLabel, gridBagConstraints);

        fontFaceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fontFaceList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fontFaceListValueChanged(evt);
            }
        });

        fontFaceScroll.setViewportView(fontFaceList);
        fontFaceList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontFamilyList"));
        fontFaceList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontFamilyListAccessibleDescription"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        fontFamilyPanel.add(fontFaceScroll, gridBagConstraints);

        fontChosenField.setEditable(false);
        fontChosenField.setMargin(new java.awt.Insets(1, 2, 2, 4));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        fontFamilyPanel.add(fontChosenField, gridBagConstraints);
        fontChosenField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "chosenFontsAccessibleName"));
        fontChosenField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "ChosenFontAccessibleDescription"));

        fontFamilyButton.setMnemonic(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_FAMILY_EDIT_BTN_MNEMONIC").charAt(0));
        fontFamilyButton.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "EDIT"));
        fontFamilyButton.setMargin(new java.awt.Insets(4, 4, 4, 4));
        fontFamilyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontFamilyButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        fontFamilyPanel.add(fontFamilyButton, gridBagConstraints);
        fontFamilyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "EditChosenFontsAccessibleDescription"));

        mainPanel.add(fontFamilyPanel, java.awt.BorderLayout.CENTER);

        fontSizePanel.setLayout(new java.awt.GridBagLayout());

        fontSizePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        sizeLabel.setLabelFor(fontSizeField);
        sizeLabel.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_SIZE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        fontSizePanel.add(sizeLabel, gridBagConstraints);

        fontSizeField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        fontSizeField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeFieldActionPerformed(evt);
            }
        });
        fontSizeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fontSizeFieldFocusLost(evt);
            }
        });
        fontSizeField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fontSizeFieldKeyTyped(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        fontSizePanel.add(fontSizeField, gridBagConstraints);
        fontSizeField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "ChosenFontSizeAccessibleName"));
        fontSizeField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("ChosenFontSizeAccessibleDescription"));

        fontSizeUnitCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fontSizeUnitComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        fontSizePanel.add(fontSizeUnitCombo, gridBagConstraints);
        fontSizeUnitCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontSizeUnitListAccessibleName"));
        fontSizeUnitCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontSizeUnitListAccessibleDescription"));

        fontSizeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fontSizeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fontSizeListValueChanged(evt);
            }
        });

        fontSizeScroll.setViewportView(fontSizeList);
        fontSizeList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontSizeListAccessibleName"));
        fontSizeList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontSizeListAccessibleDesription"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        fontSizePanel.add(fontSizeScroll, gridBagConstraints);

        mainPanel.add(fontSizePanel, java.awt.BorderLayout.EAST);

        styleMainPanel.setLayout(new java.awt.BorderLayout(20, 0));

        stylePanel.setLayout(new java.awt.GridBagLayout());

        stylePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        styleLabel.setLabelFor(fontStyleComboBox);
        styleLabel.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_STYLE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        stylePanel.add(styleLabel, gridBagConstraints);

        fontStyleComboBox.setMinimumSize(new java.awt.Dimension(150, 20));
        fontStyleComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fontStyleComboBoxItemStateChanged(evt);
            }
        });
        fontStyleComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontStyleComboBoxActionPerformed(evt);
            }
        });
        fontStyleComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fontStyleComboBoxFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        stylePanel.add(fontStyleComboBox, gridBagConstraints);
        fontStyleComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "STYLE_LIST_ACCES_NAME"));
        fontStyleComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontStyleSelectionAccessibleDescription"));

        weightLabel.setLabelFor(fontWeightComboBox);
        weightLabel.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_WEIGHT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        stylePanel.add(weightLabel, gridBagConstraints);

        fontWeightComboBox.setMinimumSize(new java.awt.Dimension(150, 20));
        fontWeightComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fontWeightComboBoxItemStateChanged(evt);
            }
        });
        fontWeightComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontWeightComboBoxActionPerformed(evt);
            }
        });
        fontWeightComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fontWeightComboBoxFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        stylePanel.add(fontWeightComboBox, gridBagConstraints);
        fontWeightComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontWeightSelectionAccessibleName"));
        fontWeightComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontWeightSelectionAccessibleDescription"));

        variantLabel.setLabelFor(fontVariantComboBox);
        variantLabel.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_VARIANT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        stylePanel.add(variantLabel, gridBagConstraints);
        variantLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontVariantSelectionAccessibleDescription"));

        fontVariantComboBox.setMinimumSize(new java.awt.Dimension(150, 20));
        fontVariantComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fontVariantComboBoxItemStateChanged(evt);
            }
        });
        fontVariantComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontVariantComboBoxActionPerformed(evt);
            }
        });
        fontVariantComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fontVariantComboBoxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fontVariantComboBoxFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        stylePanel.add(fontVariantComboBox, gridBagConstraints);
        fontVariantComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontVariantSelectionAccessibleName"));
        fontVariantComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontVariantSelectionAccessibleDescription"));

        colorLabel.setLabelFor(colorSelectionPanel);
        colorLabel.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_COLOR"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        stylePanel.add(colorLabel, gridBagConstraints);

        colorSelectionPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        stylePanel.add(colorSelectionPanel, gridBagConstraints);

        styleMainPanel.add(stylePanel, java.awt.BorderLayout.WEST);

        decorationPanel.setLayout(new java.awt.GridBagLayout());

        decorationPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        decorationLabel.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_DECORATION"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        decorationPanel.add(decorationLabel, gridBagConstraints);

        underlineCheckbox.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_UNDERLINE"));
        underlineCheckbox.setMargin(new java.awt.Insets(0, 2, 0, 2));
        underlineCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                underlineCheckboxItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        decorationPanel.add(underlineCheckbox, gridBagConstraints);
        underlineCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontUnderlineAccessibleDescription"));

        strikethroughCheckbox.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_STRIKETHROUGH"));
        strikethroughCheckbox.setMargin(new java.awt.Insets(0, 2, 0, 2));
        strikethroughCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                strikethroughCheckboxItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        decorationPanel.add(strikethroughCheckbox, gridBagConstraints);
        strikethroughCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontStrikeThroughAccessibleDescription"));

        overlineCheckbox.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_OVERLINE"));
        overlineCheckbox.setMargin(new java.awt.Insets(0, 2, 0, 2));
        overlineCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                overlineCheckboxItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        decorationPanel.add(overlineCheckbox, gridBagConstraints);
        overlineCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontOverlineAccessibleDescription"));

        noDecorationCheckbox.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "NO_DECORATION"));
        noDecorationCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                noDecorationCheckboxItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        decorationPanel.add(noDecorationCheckbox, gridBagConstraints);
        noDecorationCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontNoDecorationAccessibleDescription"));

        styleMainPanel.add(decorationPanel, java.awt.BorderLayout.CENTER);

        mainPanel.add(styleMainPanel, java.awt.BorderLayout.SOUTH);

        add(mainPanel, java.awt.BorderLayout.NORTH);

        errorPanel.setLayout(new java.awt.BorderLayout());

        errorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        errorLabel.setForeground(new java.awt.Color(0, 0, 153));
        errorPanel.add(errorLabel, java.awt.BorderLayout.CENTER);

        add(errorPanel, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents
    
    private void noDecorationCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_noDecorationCheckboxItemStateChanged
        textDecorationData.enableNoDecoration((evt.getStateChange() == evt.SELECTED));
        if(evt.getStateChange() == evt.SELECTED){
            strikethroughCheckbox.setSelected(false);
            overlineCheckbox.setSelected(false);
            underlineCheckbox.setSelected(false);
            strikethroughCheckbox.setEnabled(false);
            overlineCheckbox.setEnabled(false);
            underlineCheckbox.setEnabled(false);
        }else{
            strikethroughCheckbox.setEnabled(true);
            overlineCheckbox.setEnabled(true);
            underlineCheckbox.setEnabled(true);
        }
        setTextDecoration();
    }//GEN-LAST:event_noDecorationCheckboxItemStateChanged
    
    private void fontVariantComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fontVariantComboBoxFocusGained
        errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
    }//GEN-LAST:event_fontVariantComboBoxFocusGained
    
    private void strikethroughCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_strikethroughCheckboxItemStateChanged
        textDecorationData.enableLineThrough((evt.getStateChange() == evt.SELECTED));
        setTextDecoration();
    }//GEN-LAST:event_strikethroughCheckboxItemStateChanged
    
    private void overlineCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_overlineCheckboxItemStateChanged
        textDecorationData.enableOverline((evt.getStateChange() == evt.SELECTED));
        setTextDecoration();
    }//GEN-LAST:event_overlineCheckboxItemStateChanged
    
    private void underlineCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_underlineCheckboxItemStateChanged
        textDecorationData.enableUnderline((evt.getStateChange() == evt.SELECTED));
        setTextDecoration();
    }//GEN-LAST:event_underlineCheckboxItemStateChanged
    
    private void fontVariantComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fontVariantComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setFontVariant();
        }
    }//GEN-LAST:event_fontVariantComboBoxItemStateChanged
    
    private void fontVariantComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fontVariantComboBoxFocusLost
        errorLabel.setText("");
        setFontVariant();
    }//GEN-LAST:event_fontVariantComboBoxFocusLost
    
    private void fontVariantComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontVariantComboBoxActionPerformed
        setFontVariant();
    }//GEN-LAST:event_fontVariantComboBoxActionPerformed
    
    private void fontWeightComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fontWeightComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setFontWeight();
        }
    }//GEN-LAST:event_fontWeightComboBoxItemStateChanged
    
    private void fontWeightComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontWeightComboBoxActionPerformed
        setFontWeight();
    }//GEN-LAST:event_fontWeightComboBoxActionPerformed
    
    private void fontWeightComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fontWeightComboBoxFocusLost
        setFontWeight();
    }//GEN-LAST:event_fontWeightComboBoxFocusLost
    
    private void fontStyleComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontStyleComboBoxActionPerformed
        setFontStyle();
    }//GEN-LAST:event_fontStyleComboBoxActionPerformed
    
    private void fontStyleComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fontStyleComboBoxFocusLost
        setFontStyle();
    }//GEN-LAST:event_fontStyleComboBoxFocusLost
    
    private void fontStyleComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fontStyleComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setFontStyle();
        }
    }//GEN-LAST:event_fontStyleComboBoxItemStateChanged
    
    private void fontSizeUnitComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fontSizeUnitComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setFontSize();
        }
    }//GEN-LAST:event_fontSizeUnitComboItemStateChanged
    
    private void fontSizeFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeFieldActionPerformed
        setFontSize();
    }//GEN-LAST:event_fontSizeFieldActionPerformed
    
    private void fontSizeFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fontSizeFieldFocusLost
        setFontSize();
    }//GEN-LAST:event_fontSizeFieldFocusLost
    
    private void fontSizeFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fontSizeFieldKeyTyped
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                enableFontSizeUnitCombo(Utils.isInteger(fontSizeField.getText()));
            }
        });
    }//GEN-LAST:event_fontSizeFieldKeyTyped
    
    private void fontSizeListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fontSizeListValueChanged
        if (evt.getValueIsAdjusting()) return;
        String selectedFontSize = (String) fontSizeList.getSelectedValue();
        fontSizeField.setText(selectedFontSize);
        enableFontSizeUnitCombo(Utils.isInteger(selectedFontSize));
        setFontSize();
    }//GEN-LAST:event_fontSizeListValueChanged
    
    private void enableFontSizeUnitCombo(final boolean enable){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                fontSizeUnitCombo.setEnabled(enable);
            }
        });
    }
    
    private void fontFaceListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fontFaceListValueChanged
        if (evt.getValueIsAdjusting()) return;
        if(fontFaceList.getSelectedValue()!= null){
            fontChosenField.setText(fontFaceList.getSelectedValue().toString());
            setFontFamily();
        }
    }//GEN-LAST:event_fontFaceListValueChanged
    
    private void fontFamilyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontFamilyButtonActionPerformed
        FontFamilyEditorDialog fontFamilyEditor = new FontFamilyEditorDialog(fontFamilies, fontFaceList.getSelectedIndex());
        fontFamilyEditor.showDialog();
        fontFaceList.setSelectedIndex(fontFamilyEditor.getSelectedIndex());
        fontChosenField.setText(fontFaceList.getSelectedValue().toString());
        setFontFamily();
    }//GEN-LAST:event_fontFamilyButtonActionPerformed
    
    
    private void setFontFamily(){
        PropertyData fontFamilyData = new PropertyData();
        fontFamilyData.setValue(fontChosenField.getText());
        cssPropertyChangeSupport.firePropertyChange(CssProperties.FONT_FAMILY, null, fontFamilyData.toString());
    }
    
    private void setFontSize(){
        //XXX Do we need to put some constraints?
        PropertyWithUnitData fontSizeData = new PropertyWithUnitData();
        fontSizeData.setUnit(fontSizeUnitCombo.getSelectedItem().toString());
        fontSizeData.setValue(fontSizeField.getText());
        cssPropertyChangeSupport.firePropertyChange(CssProperties.FONT_SIZE, null, fontSizeData.toString());
    }
    
    private void setFontStyle(){
        PropertyData fontStyleData = new PropertyData();
        fontStyleData.setValue(fontStyleComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport.firePropertyChange(CssProperties.FONT_STYLE, null, fontStyleData.toString());
    }
    
    private void setFontWeight(){
        PropertyData fontWeightData = new PropertyData();
        fontWeightData.setValue(fontWeightComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport.firePropertyChange(CssProperties.FONT_WEIGHT, null, fontWeightData.toString());
    }
    
    private void setFontVariant(){
        PropertyData fontVariantData = new PropertyData();
        fontVariantData.setValue(fontVariantComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport.firePropertyChange(CssProperties.FONT_VARIANT, null, fontVariantData.toString());
    }
    
    private void  setFontColor(){
        PropertyData fontColorData = new PropertyData();
        fontColorData.setValue(colorField.getColorString());
        cssPropertyChangeSupport.firePropertyChange(CssProperties.COLOR, null, fontColorData.toString());
    }
    
    private void  setTextDecoration(){
        cssPropertyChangeSupport.firePropertyChange(CssProperties.TEXT_DECORATION, null, textDecorationData.toString());
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel colorLabel;
    private javax.swing.JPanel colorSelectionPanel;
    private javax.swing.JLabel decorationLabel;
    private javax.swing.JPanel decorationPanel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JPanel errorPanel;
    private javax.swing.JTextField fontChosenField;
    private javax.swing.JList fontFaceList;
    private javax.swing.JScrollPane fontFaceScroll;
    private javax.swing.JButton fontFamilyButton;
    private javax.swing.JPanel fontFamilyPanel;
    private javax.swing.JLabel fontLabel;
    private javax.swing.JTextField fontSizeField;
    private javax.swing.JList fontSizeList;
    private javax.swing.JPanel fontSizePanel;
    private javax.swing.JScrollPane fontSizeScroll;
    private javax.swing.JComboBox fontSizeUnitCombo;
    private javax.swing.JComboBox fontStyleComboBox;
    private javax.swing.JComboBox fontVariantComboBox;
    private javax.swing.JComboBox fontWeightComboBox;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JCheckBox noDecorationCheckbox;
    private javax.swing.JCheckBox overlineCheckbox;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JCheckBox strikethroughCheckbox;
    private javax.swing.JLabel styleLabel;
    private javax.swing.JPanel styleMainPanel;
    private javax.swing.JPanel stylePanel;
    private javax.swing.JCheckBox underlineCheckbox;
    private javax.swing.JLabel variantLabel;
    private javax.swing.JLabel weightLabel;
    // End of variables declaration//GEN-END:variables
}
