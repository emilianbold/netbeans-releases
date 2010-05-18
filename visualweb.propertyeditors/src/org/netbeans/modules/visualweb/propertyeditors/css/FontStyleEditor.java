/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.propertyeditors.css;

import org.netbeans.modules.visualweb.propertyeditors.css.model.CssStyleData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.FontModel;
import org.netbeans.modules.visualweb.propertyeditors.css.model.PropertyData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.PropertyWithUnitData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.TextDecorationData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.Utils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;

/**
 * Font Style editor.
 * @author  Winston Prakash
 *          Jeff Hoffman (HIE design)
 */
public class FontStyleEditor extends StyleEditor implements PropertyChangeListener {
    
    CssStyleData cssStyleData = null;
    
    ColorSelectionField colorField =  new ColorSelectionField();
    TextDecorationData textDecorationData = new TextDecorationData();
    
    FontModel fontModel = new FontModel();
    DefaultListModel fontFamilies = fontModel.getFontFamilySetList();
    
    /** Creates new form FontStyleEditor */
    public FontStyleEditor(CssStyleData styleData) {
        cssStyleData = styleData;
        setName("fontStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(StyleBuilderDialog.class, "FONT_EDITOR_DISPNAME"));
        initComponents();
        colorSelectionPanel.add(colorField,BorderLayout.CENTER);
        colorField.addCssPropertyChangeListener(this);
        initialize();
    }
    
    private void initialize(){
        // Set the font family to the GUI
        fontFamilies = fontModel.getFontFamilySetList();
        fontFaceList.setModel(fontFamilies);
        String fontFamily = cssStyleData.getProperty(CssStyleData.FONT_FAMILY);
        if(fontFamily != null){
            // This is a work around. Since batik parser puts the quotes around
            // fonts with out spaces, remove the quotes from those fonts before
            // adding it to the list, else the font set will appear doubly - one
            // with quotes and one with out quotes.
            String fontSet = "";
            StringTokenizer st = new StringTokenizer(fontFamily.trim(),",");
            while(st.hasMoreTokens()){
                String fontName = st.nextToken();
                if(new StringTokenizer(fontName.trim()).countTokens() == 1){
                    fontName = fontName.replaceAll("'","");
                }
                fontSet += fontName;
                if(st.hasMoreTokens()) fontSet += ",";
            }
            
            if(!fontFamilies.contains(fontSet)){
                fontFamilies.add(1,fontSet);
            }
            fontFaceList.setSelectedIndex(fontFamilies.indexOf(fontSet));
        }else{
            fontFaceList.setSelectedIndex(0);
        }
        
        // Set the font size to the GUI
        DefaultListModel fontSizes= fontModel.getFontSizeList();
        fontSizeList.setModel(fontSizes);
        
        DefaultComboBoxModel fontSizeUnits= fontModel.getFontSizeUnitList();
        fontSizeUnitCombo.setModel(fontSizeUnits);
        
        String fontSizeStr = cssStyleData.getProperty(CssStyleData.FONT_SIZE);
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
        
        // Set the font Style to the GUI
        DefaultComboBoxModel fontStyles = fontModel.getFontStyleList();
        fontStyleComboBox.setModel(fontStyles);
        String fontStyle = cssStyleData.getProperty(CssStyleData.FONT_STYLE);
        if(fontStyle != null){
            if(fontStyles.getIndexOf(fontStyle) != -1){
                fontStyleComboBox.setSelectedIndex(fontStyles.getIndexOf(fontStyle));
            }
        }else{
            fontStyleComboBox.setSelectedIndex(0);
        }
        
        // Set the font Weight to the GUI
        DefaultComboBoxModel fontWeights = fontModel.getFontWeightList();
        fontWeightComboBox.setModel(fontWeights);
        String fontWeight = cssStyleData.getProperty(CssStyleData.FONT_WEIGHT);
        if(fontWeight != null){
            if(fontWeights.getIndexOf(fontWeight) != -1){
                fontWeightComboBox.setSelectedIndex(fontWeights.getIndexOf(fontWeight));
            }
        }else{
            fontWeightComboBox.setSelectedIndex(0);
        }
        
        // Set the font Variant to the GUI
        DefaultComboBoxModel fontVariants = fontModel.getFontVariantList();
        fontVariantComboBox.setModel(fontVariants);
        String fontVariant = cssStyleData.getProperty(CssStyleData.FONT_VARIANT);
        if(fontVariant != null){
            if(fontVariants.getIndexOf(fontVariant) != -1){
                fontVariantComboBox.setSelectedIndex(fontVariants.getIndexOf(fontVariant));
            }
        }else{
            fontVariantComboBox.setSelectedIndex(0);
        }
        
        // Set the Text Decoration the GUI
        String textDecoration = cssStyleData.getProperty(CssStyleData.TEXT_DECORATION);
        if(textDecoration != null){
            textDecorationData.setDecoration(textDecoration);
            underlineCheckbox.setSelected(textDecorationData.underlineEnabled());
            overlineCheckbox.setSelected(textDecorationData.overlineEnabled());
            strikethroughCheckbox.setSelected(textDecorationData.lineThroughEnabled());
        }
        
        // Set the Bckground Color the GUI
        String textColor = cssStyleData.getProperty(CssStyleData.COLOR);
        if(textColor != null){
            colorField.setColorString(textColor);
        }
        
        FontMetrics fontMetrics = fontSizeField.getFontMetrics(fontSizeField.getFont());
        int width = fontMetrics.stringWidth((String) fontSizes.get(0)) + 10;
        int height = (fontMetrics.getHeight() + 10) > 25 ? fontMetrics.getHeight() + 10 : 25;
        fontSizeField.setPreferredSize(new Dimension(width, height));
    }
    
    /** Listens to the color property change in the color chooser filed */
    public void propertyChange(PropertyChangeEvent evt) {
        setFontColor();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

        fontFamilyPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fontFamilyPanel.setLayout(new java.awt.GridBagLayout());

        fontLabel.setLabelFor(fontChosenField);
        org.openide.awt.Mnemonics.setLocalizedText(fontLabel, org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "Font_Family")); // NOI18N
        fontLabel.setMinimumSize(new java.awt.Dimension(200, 15));
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
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle"); // NOI18N
        fontFaceList.getAccessibleContext().setAccessibleName(bundle.getString("FontFamilyList")); // NOI18N
        fontFaceList.getAccessibleContext().setAccessibleDescription(bundle.getString("FontFamilyListAccessibleDescription")); // NOI18N

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
        fontChosenField.getAccessibleContext().setAccessibleName(bundle.getString("chosenFontsAccessibleName")); // NOI18N
        fontChosenField.getAccessibleContext().setAccessibleDescription(bundle.getString("ChosenFontAccessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fontFamilyButton, org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "EDIT")); // NOI18N
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
        fontFamilyButton.getAccessibleContext().setAccessibleDescription(bundle.getString("EditChosenFontsAccessibleDescription")); // NOI18N

        mainPanel.add(fontFamilyPanel, java.awt.BorderLayout.CENTER);

        fontSizePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fontSizePanel.setLayout(new java.awt.GridBagLayout());

        sizeLabel.setLabelFor(fontSizeField);
        org.openide.awt.Mnemonics.setLocalizedText(sizeLabel, org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_SIZE")); // NOI18N
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
        fontSizeField.getAccessibleContext().setAccessibleName(bundle.getString("ChosenFontSizeAccessibleName")); // NOI18N
        fontSizeField.getAccessibleContext().setAccessibleDescription(bundle.getString("ChosenFontSizeAccessibleDescription")); // NOI18N

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
        fontSizeUnitCombo.getAccessibleContext().setAccessibleName(bundle.getString("FontSizeUnitListAccessibleName")); // NOI18N
        fontSizeUnitCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("FontSizeUnitListAccessibleDescription")); // NOI18N

        fontSizeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fontSizeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fontSizeListValueChanged(evt);
            }
        });
        fontSizeScroll.setViewportView(fontSizeList);
        fontSizeList.getAccessibleContext().setAccessibleName(bundle.getString("FontSizeListAccessibleName")); // NOI18N
        fontSizeList.getAccessibleContext().setAccessibleDescription(bundle.getString("FontSizeListAccessibleDesription")); // NOI18N

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

        stylePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        stylePanel.setLayout(new java.awt.GridBagLayout());

        styleLabel.setLabelFor(fontStyleComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(styleLabel, org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_STYLE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        stylePanel.add(styleLabel, gridBagConstraints);

        fontStyleComboBox.setMinimumSize(new java.awt.Dimension(150, 20));
        fontStyleComboBox.setPreferredSize(new java.awt.Dimension(150, 20));
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
        fontStyleComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("FontStyleSelectionAccessibleDescription")); // NOI18N

        weightLabel.setLabelFor(fontWeightComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(weightLabel, org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_WEIGHT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        stylePanel.add(weightLabel, gridBagConstraints);

        fontWeightComboBox.setMinimumSize(new java.awt.Dimension(150, 20));
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
        fontWeightComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fontWeightComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        stylePanel.add(fontWeightComboBox, gridBagConstraints);
        fontWeightComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("FontWeightSelectionAccessibleDescription")); // NOI18N

        variantLabel.setLabelFor(fontVariantComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(variantLabel, org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_VARIANT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        stylePanel.add(variantLabel, gridBagConstraints);
        variantLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("FontVariantSelectionAccessibleDescription")); // NOI18N

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
        fontVariantComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("FontVariantSelectionAccessibleDescription")); // NOI18N

        colorLabel.setLabelFor(colorSelectionPanel);
        org.openide.awt.Mnemonics.setLocalizedText(colorLabel, org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_COLOR")); // NOI18N
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

        decorationPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        decorationPanel.setLayout(new java.awt.GridBagLayout());

        decorationLabel.setText(bundle.getString("FONT_DECORATION")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        decorationPanel.add(decorationLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(underlineCheckbox, org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_UNDERLINE")); // NOI18N
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
        underlineCheckbox.getAccessibleContext().setAccessibleDescription(bundle.getString("FontUnderlineAccessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(strikethroughCheckbox, org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_STRIKETHROUGH")); // NOI18N
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
        strikethroughCheckbox.getAccessibleContext().setAccessibleDescription(bundle.getString("FontStrikeThroughAccessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(overlineCheckbox, org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_OVERLINE")); // NOI18N
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
        overlineCheckbox.getAccessibleContext().setAccessibleDescription(bundle.getString("FontOverlineAccessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(noDecorationCheckbox, org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "NO_DECORATION")); // NOI18N
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
        noDecorationCheckbox.getAccessibleContext().setAccessibleDescription(bundle.getString("FontNoDecorationAccessibleDescription")); // NOI18N

        styleMainPanel.add(decorationPanel, java.awt.BorderLayout.CENTER);

        mainPanel.add(styleMainPanel, java.awt.BorderLayout.SOUTH);

        add(mainPanel, java.awt.BorderLayout.NORTH);

        errorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        errorPanel.setLayout(new java.awt.BorderLayout());

        errorLabel.setForeground(new java.awt.Color(0, 0, 153));
        errorLabel.setMinimumSize(new java.awt.Dimension(200, 20));
        errorLabel.setPreferredSize(new java.awt.Dimension(200, 20));
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
        fontChosenField.setText(fontFaceList.getSelectedValue().toString());
        setFontFamily();
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
        cssStyleData.modifyProperty(CssStyleData.FONT_FAMILY, fontFamilyData.toString());
    }
    
    private void setFontSize(){
        //XXX Do we need to put some constraints?
        PropertyWithUnitData fontSizeData = new PropertyWithUnitData();
        fontSizeData.setUnit(fontSizeUnitCombo.getSelectedItem().toString());
        fontSizeData.setValue(fontSizeField.getText());
        cssStyleData.modifyProperty(CssStyleData.FONT_SIZE, fontSizeData.toString());
    }
    
    private void setFontStyle(){
        PropertyData fontStyleData = new PropertyData();
        fontStyleData.setValue(fontStyleComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.FONT_STYLE, fontStyleData.toString());
    }
    
    private void setFontWeight(){
        PropertyData fontWeightData = new PropertyData();
        fontWeightData.setValue(fontWeightComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.FONT_WEIGHT, fontWeightData.toString());
    }
    
    private void setFontVariant(){
        PropertyData fontVariantData = new PropertyData();
        fontVariantData.setValue(fontVariantComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.FONT_VARIANT, fontVariantData.toString());
    }
    
    private void  setFontColor(){
        PropertyData fontColorData = new PropertyData();
        fontColorData.setValue(colorField.getColorString());
        cssStyleData.modifyProperty(CssStyleData.COLOR, fontColorData.toString());
    }
    
    private void  setTextDecoration(){
        cssStyleData.modifyProperty(CssStyleData.TEXT_DECORATION, textDecorationData.toString());
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
