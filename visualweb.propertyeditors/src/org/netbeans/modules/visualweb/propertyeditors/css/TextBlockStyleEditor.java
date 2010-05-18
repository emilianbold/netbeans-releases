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
import org.netbeans.modules.visualweb.propertyeditors.css.model.PropertyData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.PropertyWithUnitData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.TextBlockData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.TextBlockModel;
import org.netbeans.modules.visualweb.propertyeditors.css.model.Utils;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;

/**
 * Text Block Style editor.
 * @author  Winston Prakash
 *          Jeff Hoffman (HIE design)
 */
public class TextBlockStyleEditor extends StyleEditor {
    
    CssStyleData cssStyleData = null;
    PropertyData directionData = new PropertyData();
    PropertyData textAlignData = new PropertyData();
    PropertyData verticalAlignData = new PropertyWithUnitData();
    PropertyWithUnitData wordSpacingData = new PropertyWithUnitData();
    PropertyWithUnitData letterSpacingData = new PropertyWithUnitData();
    PropertyWithUnitData lineHeightData = new PropertyWithUnitData();
    PropertyWithUnitData textIndentData = new PropertyWithUnitData();
    
    /** Creates new form FontStyleEditor */
    public TextBlockStyleEditor(CssStyleData styleData) {
        cssStyleData = styleData;
        setName("textBlockStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(StyleBuilderDialog.class, "TEXTBLOCK_EDITOR_DISPNAME"));
        initComponents();
        initialize();
        
        // Add editor listeners to the border width combobox
        final JTextField textIndentComboEditor = (JTextField) textIndentCombo.getEditor().getEditorComponent();
        textIndentComboEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        textIndentUnitCombo.setEnabled(Utils.isInteger(textIndentComboEditor.getText()));
                    }
                });
            }
        });
        
        // Add editor listeners to the border width combobox
        final JTextField wordSpacingComboEditor = (JTextField) wordSpacingCombo.getEditor().getEditorComponent();
        wordSpacingComboEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        wordSpacingUnitCombo.setEnabled(Utils.isInteger(wordSpacingComboEditor.getText()));
                    }
                });
            }
        });
        
        // Add editor listeners to the border width combobox
        final JTextField letterSpacingComboEditor = (JTextField) letterSpacingCombo.getEditor().getEditorComponent();
        letterSpacingComboEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        letterSpacingUnitCombo.setEnabled(Utils.isInteger(letterSpacingComboEditor.getText()));
                    }
                });
            }
        });
        
        // Add editor listeners to the border width combobox
        final JTextField lineHeightComboEditor = (JTextField) lineHeightCombo.getEditor().getEditorComponent();
        lineHeightComboEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        lineHeightUnitCombo.setEnabled(Utils.isInteger(lineHeightComboEditor.getText()));
                    }
                });
            }
        });
    }
    
    private void initialize(){
        TextBlockModel textBlockModel = new TextBlockModel();
        TextBlockData textBlockData = new TextBlockData();
        
        // Set the Horizontal Alignment to the GUI
        DefaultComboBoxModel horizontalAlignList = textBlockModel.getHorizontalAlignmentList();
        horizontalAlignCombo.setModel(horizontalAlignList);
        String  horizontalAlign = cssStyleData.getProperty(CssStyleData.TEXT_ALIGN);
        if(horizontalAlign != null){
            horizontalAlignCombo.setSelectedItem(horizontalAlign);
        }else{
            horizontalAlignCombo.setSelectedIndex(0);
        }
        
        // Set the Vertical Alignment to the GUI
        DefaultComboBoxModel verticalAlignList = textBlockModel.getVerticalAlignmentList();
        verticalAlignCombo.setModel(verticalAlignList);
        String  verticalAlign = cssStyleData.getProperty(CssStyleData.VERTICAL_ALIGN);
        if(verticalAlign != null){
            textBlockData.setVerticalAlign(verticalAlign);
            verticalAlignCombo.setSelectedItem(textBlockData.getVerticalAlignValue());
        }else{
            verticalAlignCombo.setSelectedIndex(0);
        }
        
        // Set the Indentation to the GUI
        DefaultComboBoxModel indentationList = textBlockModel.getIndentationList();
        textIndentCombo.setModel(indentationList);
        textIndentUnitCombo.setModel(textBlockModel.getTextBlockUnitList());
        String  indentation = cssStyleData.getProperty(CssStyleData.TEXT_INDENT);
        if(indentation != null){
            textBlockData.setIndentation(indentation);
            textIndentCombo.setSelectedItem(textBlockData.getIndentationValue());
            textIndentUnitCombo.setSelectedItem(textBlockData.getIndentationUnit());
        }else{
            textIndentCombo.setSelectedIndex(0);
            textIndentUnitCombo.setSelectedIndex(0);
        }
        
        // Set the Text Direction to the GUI
        DefaultComboBoxModel textDirectionList = textBlockModel.getTextDirectionList();
        directionCombo.setModel(textDirectionList);
        String  textDirection = cssStyleData.getProperty(CssStyleData.DIRECTION);
        if(textDirection != null){
            directionCombo.setSelectedItem(textDirection);
        }else{
            directionCombo.setSelectedIndex(0);
        }
        
        // Set the Word Spacing to the GUI
        DefaultComboBoxModel wordSpacingList = textBlockModel.getWordSpacingList();
        wordSpacingCombo.setModel(wordSpacingList);
        wordSpacingUnitCombo.setModel(textBlockModel.getTextBlockUnitList());
        String  wordSpacing = cssStyleData.getProperty(CssStyleData.WORD_SPACING);
        if(wordSpacing != null){
            textBlockData.setWordSpacing(wordSpacing);
            wordSpacingCombo.setSelectedItem(textBlockData.getWordSpacingValue());
            wordSpacingUnitCombo.setSelectedItem(textBlockData.getWordSpacingUnit());
        }else{
            wordSpacingCombo.setSelectedIndex(0);
            wordSpacingUnitCombo.setSelectedIndex(0);
        }
        
        // Set the Letter Spacing to the GUI
        DefaultComboBoxModel letterSpacingList = textBlockModel.getLetterSpacingList();
        letterSpacingCombo.setModel(letterSpacingList);
        letterSpacingUnitCombo.setModel(textBlockModel.getTextBlockUnitList());
        String  letterSpacing = cssStyleData.getProperty(CssStyleData.LETTER_SPACING);
        if(letterSpacing != null){
            textBlockData.setLetterSpacing(letterSpacing);
            letterSpacingCombo.setSelectedItem(textBlockData.getLetterSpacingValue());
            letterSpacingUnitCombo.setSelectedItem(textBlockData.getLetterSpacingUnit());
        }else{
            letterSpacingCombo.setSelectedIndex(0);
            letterSpacingUnitCombo.setSelectedIndex(0);
        }
        
        // Set the Letter Spacing to the GUI
        DefaultComboBoxModel lineHeightList = textBlockModel.getLineHeightList();
        lineHeightCombo.setModel(lineHeightList);
        lineHeightUnitCombo.setModel(textBlockModel.getTextBlockUnitList());
        String  lineHeight = cssStyleData.getProperty(CssStyleData.LINE_HEIGHT);
        if(lineHeight != null){
            textBlockData.setLineHeight(lineHeight);
            lineHeightCombo.setSelectedItem(textBlockData.getLineHeightValue());
            lineHeightUnitCombo.setSelectedItem(textBlockData.getLineHeightUnit());
        }else{
            lineHeightCombo.setSelectedIndex(0);
            lineHeightUnitCombo.setSelectedIndex(0);
        }
        
        textIndentCombo.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        
        directionCombo.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        
        letterSpacingCombo.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        
        wordSpacingCombo.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        textBlockPanel = new javax.swing.JPanel();
        decorationLabel = new javax.swing.JLabel();
        imageScroll = new javax.swing.JLabel();
        directionCombo = new javax.swing.JComboBox();
        horizontalAlignCombo = new javax.swing.JComboBox();
        textAlignLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        textIndentCombo = new javax.swing.JComboBox();
        verticalAlignCombo = new javax.swing.JComboBox();
        wordSpacingUnitCombo = new javax.swing.JComboBox();
        imageScroll1 = new javax.swing.JLabel();
        wordSpacingCombo = new javax.swing.JComboBox();
        imageScroll2 = new javax.swing.JLabel();
        letterSpacingCombo = new javax.swing.JComboBox();
        letterSpacingUnitCombo = new javax.swing.JComboBox();
        textIndentUnitCombo = new javax.swing.JComboBox();
        lineHeightLabel = new javax.swing.JLabel();
        lineHeightCombo = new javax.swing.JComboBox();
        lineHeightUnitCombo = new javax.swing.JComboBox();
        errorPanel = new javax.swing.JPanel();
        errorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        textBlockPanel.setLayout(new java.awt.GridBagLayout());

        textBlockPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        decorationLabel.setText(org.openide.util.NbBundle.getMessage(TextBlockStyleEditor.class, "TEXT_INDENTATION"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        textBlockPanel.add(decorationLabel, gridBagConstraints);

        imageScroll.setText(org.openide.util.NbBundle.getMessage(TextBlockStyleEditor.class, "TEXT_DIRECTION"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        textBlockPanel.add(imageScroll, gridBagConstraints);

        directionCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                directionComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        textBlockPanel.add(directionCombo, gridBagConstraints);
        directionCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("DIRECTION_ACCESS_NAME"));
        directionCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("DIRECTION_ACCESS_DESC"));

        horizontalAlignCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                horizontalAlignComboFocusLost(evt);
            }
        });
        horizontalAlignCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                horizontalAlignComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        textBlockPanel.add(horizontalAlignCombo, gridBagConstraints);
        horizontalAlignCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("HORIZONTAL_ALIGNMENT_COMBOBOX_ACCESS_NAME"));
        horizontalAlignCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("HORIZONTAL_ALIGNMENT_COMBOBOX_ACCESS_DESC"));

        textAlignLabel.setText(org.openide.util.NbBundle.getMessage(TextBlockStyleEditor.class, "TEXT_HORIZ_ALIGN"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 10);
        textBlockPanel.add(textAlignLabel, gridBagConstraints);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(TextBlockStyleEditor.class, "TEXT_VERTICAL_ALIGN"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        textBlockPanel.add(jLabel2, gridBagConstraints);

        textIndentCombo.setEditable(true);
        textIndentCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textIndentComboActionPerformed(evt);
            }
        });
        textIndentCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                textIndentComboFocusLost(evt);
            }
        });
        textIndentCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                textIndentComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        textBlockPanel.add(textIndentCombo, gridBagConstraints);
        textIndentCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("INDENT_ACCESS_NAME"));
        textIndentCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("INDENT_ACCESS_DESC"));

        verticalAlignCombo.setEditable(true);
        verticalAlignCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verticalAlignComboActionPerformed(evt);
            }
        });
        verticalAlignCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                verticalAlignComboFocusLost(evt);
            }
        });
        verticalAlignCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                verticalAlignComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        textBlockPanel.add(verticalAlignCombo, gridBagConstraints);
        verticalAlignCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("VERTICAL_ALIGN_ACCESS_NAME"));
        verticalAlignCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("VERTICAL_ALIGN_ACCESS_DESC"));

        wordSpacingUnitCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                wordSpacingUnitComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        textBlockPanel.add(wordSpacingUnitCombo, gridBagConstraints);
        wordSpacingUnitCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("LETTER_SPACING_UNIT_ACCESS_NAME"));
        wordSpacingUnitCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("WORD_SPACING_UNIT_ACCESS_DESC"));

        imageScroll1.setText(org.openide.util.NbBundle.getMessage(TextBlockStyleEditor.class, "WORD_SPACING"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        textBlockPanel.add(imageScroll1, gridBagConstraints);

        wordSpacingCombo.setEditable(true);
        wordSpacingCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wordSpacingComboActionPerformed(evt);
            }
        });
        wordSpacingCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                wordSpacingComboFocusLost(evt);
            }
        });
        wordSpacingCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                wordSpacingComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        textBlockPanel.add(wordSpacingCombo, gridBagConstraints);
        wordSpacingCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("WORD_SPACING_ACCESS_NAME"));
        wordSpacingCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("WORD_SPACING_ACCESS_DESC"));

        imageScroll2.setText(org.openide.util.NbBundle.getMessage(TextBlockStyleEditor.class, "LETTER_SPACING"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        textBlockPanel.add(imageScroll2, gridBagConstraints);

        letterSpacingCombo.setEditable(true);
        letterSpacingCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                letterSpacingComboActionPerformed(evt);
            }
        });
        letterSpacingCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                letterSpacingComboFocusLost(evt);
            }
        });
        letterSpacingCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                letterSpacingComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        textBlockPanel.add(letterSpacingCombo, gridBagConstraints);
        letterSpacingCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("LETTER_SPACING_ACCESS_NAME"));
        letterSpacingCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("LETTER_SPACING_ACCESS_DESC"));

        letterSpacingUnitCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                letterSpacingUnitComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        textBlockPanel.add(letterSpacingUnitCombo, gridBagConstraints);
        letterSpacingUnitCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("LETTER_SPACING_UNIT_ACCESS_NAME"));
        letterSpacingUnitCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("LETTER_SPACING_UNIT_ACCESS_DESC"));

        textIndentUnitCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                textIndentUnitComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        textBlockPanel.add(textIndentUnitCombo, gridBagConstraints);
        textIndentUnitCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("INDENT_UNIT_ACCESS_NAME"));
        textIndentUnitCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("INDENT_UNIT_ACCESS_DESC"));

        lineHeightLabel.setText(org.openide.util.NbBundle.getMessage(TextBlockStyleEditor.class, "LINE_HEIGHT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 10);
        textBlockPanel.add(lineHeightLabel, gridBagConstraints);

        lineHeightCombo.setEditable(true);
        lineHeightCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineHeightComboActionPerformed(evt);
            }
        });
        lineHeightCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                lineHeightComboFocusLost(evt);
            }
        });
        lineHeightCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                lineHeightComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        textBlockPanel.add(lineHeightCombo, gridBagConstraints);
        lineHeightCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("LINE_HEIGHT_ACCESS_NAME"));
        lineHeightCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("LINE_HEIGHT_ACCESS_DESC"));

        lineHeightUnitCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                lineHeightUnitComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        textBlockPanel.add(lineHeightUnitCombo, gridBagConstraints);
        lineHeightUnitCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("LINE_HEIGHT_UNIT_ACCESS_NAME"));
        lineHeightUnitCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle").getString("LINE_HEIGHT_UNIT_ACCESS_DESC"));

        add(textBlockPanel, java.awt.BorderLayout.NORTH);

        errorPanel.setLayout(new java.awt.BorderLayout());

        errorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        errorLabel.setForeground(new java.awt.Color(0, 0, 153));
        errorLabel.setMinimumSize(new java.awt.Dimension(200, 20));
        errorLabel.setPreferredSize(new java.awt.Dimension(200, 20));
        errorPanel.add(errorLabel, java.awt.BorderLayout.CENTER);

        add(errorPanel, java.awt.BorderLayout.SOUTH);

    }// </editor-fold>//GEN-END:initComponents
    
    private void letterSpacingUnitComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_letterSpacingUnitComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setLetterSpacing();
        }
    }//GEN-LAST:event_letterSpacingUnitComboItemStateChanged
    
    private void letterSpacingComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_letterSpacingComboFocusLost
        setLetterSpacing();
    }//GEN-LAST:event_letterSpacingComboFocusLost
    
    private void letterSpacingComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_letterSpacingComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setLetterSpacing();
        }
    }//GEN-LAST:event_letterSpacingComboItemStateChanged
    
    private void letterSpacingComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_letterSpacingComboActionPerformed
        setLetterSpacing();
    }//GEN-LAST:event_letterSpacingComboActionPerformed
    
    private void wordSpacingUnitComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_wordSpacingUnitComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setWordSpacing();
        }
    }//GEN-LAST:event_wordSpacingUnitComboItemStateChanged
    
    private void wordSpacingComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wordSpacingComboFocusLost
        setWordSpacing();
    }//GEN-LAST:event_wordSpacingComboFocusLost
    
    private void wordSpacingComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_wordSpacingComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setWordSpacing();
        }
    }//GEN-LAST:event_wordSpacingComboItemStateChanged
    
    private void wordSpacingComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wordSpacingComboActionPerformed
        setWordSpacing();
    }//GEN-LAST:event_wordSpacingComboActionPerformed
    
    private void directionComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_directionComboItemStateChanged
        setDirection();
    }//GEN-LAST:event_directionComboItemStateChanged
    
    private void textIndentUnitComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_textIndentUnitComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setTextIndent();
        }
    }//GEN-LAST:event_textIndentUnitComboItemStateChanged
    
    private void textIndentComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textIndentComboFocusLost
        setTextIndent();
    }//GEN-LAST:event_textIndentComboFocusLost
    
    private void textIndentComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_textIndentComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setTextIndent();
        }
    }//GEN-LAST:event_textIndentComboItemStateChanged
    
    private void textIndentComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textIndentComboActionPerformed
        setTextIndent();
    }//GEN-LAST:event_textIndentComboActionPerformed
    
    private void verticalAlignComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_verticalAlignComboFocusLost
        setVerticalAlign();
    }//GEN-LAST:event_verticalAlignComboFocusLost
    
    private void horizontalAlignComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_horizontalAlignComboFocusLost
        setTextAlign();
    }//GEN-LAST:event_horizontalAlignComboFocusLost
    
    private void verticalAlignComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_verticalAlignComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setVerticalAlign();
        }
    }//GEN-LAST:event_verticalAlignComboItemStateChanged
    
    private void verticalAlignComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verticalAlignComboActionPerformed
        setVerticalAlign();
    }//GEN-LAST:event_verticalAlignComboActionPerformed
    
    private void horizontalAlignComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_horizontalAlignComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setTextAlign();
        }
    }//GEN-LAST:event_horizontalAlignComboItemStateChanged
    
    private void lineHeightUnitComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_lineHeightUnitComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setLineHeight();
        }
    }//GEN-LAST:event_lineHeightUnitComboItemStateChanged
    
    private void lineHeightComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_lineHeightComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setLineHeight();
        }
    }//GEN-LAST:event_lineHeightComboItemStateChanged
    
    private void lineHeightComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_lineHeightComboFocusLost
        setLineHeight();
    }//GEN-LAST:event_lineHeightComboFocusLost
    
    private void lineHeightComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineHeightComboActionPerformed
        setLineHeight();
    }//GEN-LAST:event_lineHeightComboActionPerformed
    
    private void setLineHeight(){
        lineHeightData.setUnit(lineHeightUnitCombo.getSelectedItem().toString());
        lineHeightData.setValue(lineHeightCombo.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.LINE_HEIGHT, lineHeightData.toString());
        lineHeightUnitCombo.setEnabled(lineHeightData.isValueInteger());
    }
    
    private void setLetterSpacing(){
        letterSpacingData.setUnit(letterSpacingUnitCombo.getSelectedItem().toString());
        letterSpacingData.setValue(letterSpacingCombo.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.LETTER_SPACING, letterSpacingData.toString());
        letterSpacingUnitCombo.setEnabled(letterSpacingData.isValueInteger());
    }
    
    private void setWordSpacing(){
        wordSpacingData.setUnit(wordSpacingUnitCombo.getSelectedItem().toString());
        wordSpacingData.setValue(wordSpacingCombo.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.WORD_SPACING, wordSpacingData.toString());
        wordSpacingUnitCombo.setEnabled(wordSpacingData.isValueInteger());
    }
    
    private void setDirection(){
        directionData.setValue(directionCombo.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.DIRECTION, directionData.toString());
    }
    
    private void setTextIndent(){
        textIndentData.setUnit(textIndentUnitCombo.getSelectedItem().toString());
        textIndentData.setValue(textIndentCombo.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.TEXT_INDENT, textIndentData.toString());
        textIndentUnitCombo.setEnabled(textIndentData.isValueInteger());
    }
    
    private void setVerticalAlign(){
        verticalAlignData.setValue(verticalAlignCombo.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.VERTICAL_ALIGN, verticalAlignData.toString());
    }
    
    private void setTextAlign(){
        textAlignData.setValue(horizontalAlignCombo.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.TEXT_ALIGN, textAlignData.toString());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel decorationLabel;
    private javax.swing.JComboBox directionCombo;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JPanel errorPanel;
    private javax.swing.JComboBox horizontalAlignCombo;
    private javax.swing.JLabel imageScroll;
    private javax.swing.JLabel imageScroll1;
    private javax.swing.JLabel imageScroll2;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox letterSpacingCombo;
    private javax.swing.JComboBox letterSpacingUnitCombo;
    private javax.swing.JComboBox lineHeightCombo;
    private javax.swing.JLabel lineHeightLabel;
    private javax.swing.JComboBox lineHeightUnitCombo;
    private javax.swing.JLabel textAlignLabel;
    private javax.swing.JPanel textBlockPanel;
    private javax.swing.JComboBox textIndentCombo;
    private javax.swing.JComboBox textIndentUnitCombo;
    private javax.swing.JComboBox verticalAlignCombo;
    private javax.swing.JComboBox wordSpacingCombo;
    private javax.swing.JComboBox wordSpacingUnitCombo;
    // End of variables declaration//GEN-END:variables
    
}
