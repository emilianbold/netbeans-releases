/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

/*
 * FontStyleEditor.java
 *
 * Created on October 13, 2004, 12:23 PM
 */

package org.netbeans.modules.css.visual.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.netbeans.modules.css.visual.model.CssProperties;
import org.netbeans.modules.css.editor.model.CssRuleContent;
import org.netbeans.modules.css.visual.model.FontModel;
import org.netbeans.modules.css.visual.model.PropertyWithUnitData;
import org.netbeans.modules.css.visual.model.TextDecorationData;
import org.netbeans.modules.css.visual.model.PropertyData;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.css.visual.model.Utils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Font Style editor.
 * @author  Winston Prakash
 * @version 1.0
 */
public class FontStyleEditor extends StyleEditor {
    
    ColorSelectionField colorField =  new ColorSelectionField();
    TextDecorationData textDecorationData = new TextDecorationData();

    //allow to GC the request processor and its thread when possible
    private static WeakReference<RequestProcessor> RP_WR;
    private static RequestProcessor createRP() {
        return new RequestProcessor(FontStyleEditor.class.getName(), 1);
    }
    private static synchronized RequestProcessor getRequestProcessor() {
        if(RP_WR != null) {
            RequestProcessor rp = RP_WR.get();
            if(rp == null) {
                rp = createRP();
                RP_WR = new WeakReference<RequestProcessor>(rp);
                return rp;
            } else {
                return rp;
            }
        } else {
            RequestProcessor rp = createRP();
            RP_WR = new WeakReference<RequestProcessor>(rp);
            return rp;
        }
    }

    FontModel fontModel;
    DefaultListModel fontFamilies;
    
    DefaultListModel fontSizes;
    DefaultComboBoxModel fontSizeUnits;
    DefaultComboBoxModel fontStyles;
    DefaultComboBoxModel fontWeights;
    DefaultComboBoxModel fontVariants;
    
    String temporaryFontSet = null;
    
    private String currentFontFamily = null;
    private String currentFontSize = null;
    private String currentFontStyle = null;
    private String currentFontWeight = null;
    private String currentFontVariant = null;
    private String currentFontColor = null;
    private String currentFontDecoration = null;
    
    /** Creates new form FontStyleEditor */
    public FontStyleEditor() {
        setName("fontStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(FontStyleEditor.class, "FONT_EDITOR_DISPNAME"));
        initComponents();
        colorSelectionPanel.add(colorField,BorderLayout.CENTER);
        colorField.addPropertyChangeListener("color", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                setFontColor();
            }
        });//NOI18N

        //lazy initialize the font model, its creation can be very slow, cannot happen in AWT
        FutureTask<FontModel> modelTask = new FutureTask<FontModel>(FontModel.getFontModel()) {
          @Override
            protected void done() {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            try {
                                initialize(get());
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        getRequestProcessor().execute(modelTask);
    }
    
    private void initialize(FontModel model){
        // Set the font family to the GUI
        fontModel = model;
        fontFamilies = fontModel.getFontFamilySetList();

        fontFaceList.setModel(fontFamilies);

        // Set the font size to the GUI
        fontSizes = fontModel.getFontSizeList();
        fontSizeList.setModel(fontSizes);

        fontSizeUnits = fontModel.getFontSizeUnitList();
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
        fontSizeField.setPreferredSize(new Dimension(width, height));

    }

    protected void setCssPropertyValues(CssRuleContent cssStyleData){
        //set the values lazily
        //since a request processor with throughput one is used, this taks
        //will run after the fond model initialization is done
        getRequestProcessor().execute(new LazyInitCssPropertyValues(cssStyleData));
    }

    private class LazyInitCssPropertyValues implements Runnable {
        private CssRuleContent cssStyleData;
        private LazyInitCssPropertyValues(CssRuleContent cssStyleData) {
            this.cssStyleData = cssStyleData;
        }
        public void run() {
            _setCssPropertyValues(cssStyleData);
        }
    }

    /**
     * Set the CSS Properties Values from the CssStyleData data structure
     * to the GUI components.
     */
    private void _setCssPropertyValues(CssRuleContent cssStyleData){
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

        boolean thereIsDecoration = textDecoration != null;
        if(thereIsDecoration){
            textDecorationData.setDecoration(textDecoration);
            currentFontDecoration = textDecorationData.toString();
        }
        boolean nodecoration = thereIsDecoration && textDecorationData.noDecorationEnabled();

        noDecorationCheckbox.setSelected(nodecoration);
        underlineCheckbox.setSelected(thereIsDecoration && !nodecoration && textDecorationData.underlineEnabled());
        overlineCheckbox.setSelected(thereIsDecoration && !nodecoration && textDecorationData.overlineEnabled());
        strikethroughCheckbox.setSelected(thereIsDecoration && !nodecoration && textDecorationData.lineThroughEnabled());
        
        
        // Set the Bckground Color the GUI
        String textColor = cssStyleData.getProperty(CssProperties.COLOR);
        if(textColor != null){
            colorField.setColorString(textColor);
        }else{
            colorField.setColorString(Utils.NOT_SET);
        }
        
        setCssPropertyChangeListener(cssStyleData);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        fontFamilyPanel = new javax.swing.JPanel();
        fontFaceScroll = new javax.swing.JScrollPane();
        fontFaceList = new javax.swing.JList();
        fontChosenField = new javax.swing.JTextField();
        fontFamilyButton = new javax.swing.JButton();
        fontSizePanel = new javax.swing.JPanel();
        fontSizeField = new javax.swing.JTextField();
        fontSizeUnitCombo = new javax.swing.JComboBox();
        fontSizeScroll = new javax.swing.JScrollPane();
        fontSizeList = new javax.swing.JList();
        styleMainPanel = new javax.swing.JPanel();
        decorationPanel = new javax.swing.JPanel();
        underlineCheckbox = new javax.swing.JCheckBox();
        strikethroughCheckbox = new javax.swing.JCheckBox();
        overlineCheckbox = new javax.swing.JCheckBox();
        noDecorationCheckbox = new javax.swing.JCheckBox();
        styleLabel = new javax.swing.JLabel();
        fontStyleComboBox = new javax.swing.JComboBox();
        weightLabel = new javax.swing.JLabel();
        fontWeightComboBox = new javax.swing.JComboBox();
        variantLabel = new javax.swing.JLabel();
        fontVariantComboBox = new javax.swing.JComboBox();
        colorLabel = new javax.swing.JLabel();
        colorSelectionPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.BorderLayout());

        fontFamilyPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fontFamilyPanel.setLayout(new java.awt.GridBagLayout());

        fontFaceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fontFaceList.setVisibleRowCount(5);
        fontFaceList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fontFaceListValueChanged(evt);
            }
        });
        fontFaceScroll.setViewportView(fontFaceList);
        fontFaceList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontFamilyList")); // NOI18N
        fontFaceList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontFamilyListAccessibleDescription")); // NOI18N

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
        fontChosenField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "chosenFontsAccessibleName")); // NOI18N
        fontChosenField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "ChosenFontAccessibleDescription")); // NOI18N

        fontFamilyButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_EDIT").charAt(0));
        fontFamilyButton.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "EDIT")); // NOI18N
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
        fontFamilyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "EditChosenFontsAccessibleDescription")); // NOI18N

        mainPanel.add(fontFamilyPanel, java.awt.BorderLayout.CENTER);

        fontSizePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fontSizePanel.setLayout(new java.awt.GridBagLayout());

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
        fontSizeField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "ChosenFontSizeAccessibleName")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle"); // NOI18N
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
        fontSizeUnitCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontSizeUnitListAccessibleName")); // NOI18N
        fontSizeUnitCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontSizeUnitListAccessibleDescription")); // NOI18N

        fontSizeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fontSizeList.setVisibleRowCount(5);
        fontSizeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fontSizeListValueChanged(evt);
            }
        });
        fontSizeScroll.setViewportView(fontSizeList);
        fontSizeList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontSizeListAccessibleName")); // NOI18N
        fontSizeList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontSizeListAccessibleDesription")); // NOI18N

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

        decorationPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        underlineCheckbox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_FONT_UNDERLINE").charAt(0));
        underlineCheckbox.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_UNDERLINE")); // NOI18N
        underlineCheckbox.setMargin(new java.awt.Insets(0, 2, 0, 2));
        underlineCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                underlineCheckboxItemStateChanged(evt);
            }
        });

        strikethroughCheckbox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_FONT_STRIKETHROUGH").charAt(0));
        strikethroughCheckbox.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_STRIKETHROUGH")); // NOI18N
        strikethroughCheckbox.setMargin(new java.awt.Insets(0, 2, 0, 2));
        strikethroughCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                strikethroughCheckboxItemStateChanged(evt);
            }
        });

        overlineCheckbox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_FONT_OVERLINE").charAt(0));
        overlineCheckbox.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_OVERLINE")); // NOI18N
        overlineCheckbox.setMargin(new java.awt.Insets(0, 2, 0, 2));
        overlineCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                overlineCheckboxItemStateChanged(evt);
            }
        });

        noDecorationCheckbox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_NO_DECORATION_1").charAt(0));
        noDecorationCheckbox.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "NO_DECORATION_1")); // NOI18N
        noDecorationCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                noDecorationCheckboxItemStateChanged(evt);
            }
        });

        styleLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_FONT_STYLE").charAt(0));
        styleLabel.setLabelFor(fontStyleComboBox);
        styleLabel.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_STYLE")); // NOI18N

        fontStyleComboBox.setMinimumSize(new java.awt.Dimension(150, 20));
        fontStyleComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fontStyleComboBoxItemStateChanged(evt);
            }
        });
        fontStyleComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fontStyleComboBoxFocusLost(evt);
            }
        });

        weightLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_FONT_WEIGHT").charAt(0));
        weightLabel.setLabelFor(fontWeightComboBox);
        weightLabel.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_WEIGHT")); // NOI18N

        fontWeightComboBox.setMinimumSize(new java.awt.Dimension(150, 20));
        fontWeightComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fontWeightComboBoxItemStateChanged(evt);
            }
        });
        fontWeightComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fontWeightComboBoxFocusLost(evt);
            }
        });

        variantLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_FONT_VARIANT").charAt(0));
        variantLabel.setLabelFor(fontVariantComboBox);
        variantLabel.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_VARIANT")); // NOI18N

        fontVariantComboBox.setMinimumSize(new java.awt.Dimension(150, 20));
        fontVariantComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fontVariantComboBoxItemStateChanged(evt);
            }
        });
        fontVariantComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fontVariantComboBoxFocusLost(evt);
            }
        });

        colorLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_FONT_COLOR").charAt(0));
        colorLabel.setLabelFor(colorSelectionPanel);
        colorLabel.setText(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FONT_COLOR")); // NOI18N

        colorSelectionPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout decorationPanelLayout = new javax.swing.GroupLayout(decorationPanel);
        decorationPanel.setLayout(decorationPanelLayout);
        decorationPanelLayout.setHorizontalGroup(
            decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(decorationPanelLayout.createSequentialGroup()
                .addGroup(decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(decorationPanelLayout.createSequentialGroup()
                        .addGroup(decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(styleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fontStyleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fontWeightComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(weightLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(variantLabel)
                            .addComponent(fontVariantComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(colorSelectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(colorLabel)))
                    .addGroup(decorationPanelLayout.createSequentialGroup()
                        .addComponent(underlineCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(overlineCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(strikethroughCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(noDecorationCheckbox)))
                .addContainerGap(51, Short.MAX_VALUE))
        );
        decorationPanelLayout.setVerticalGroup(
            decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(decorationPanelLayout.createSequentialGroup()
                .addGroup(decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(styleLabel)
                    .addComponent(weightLabel)
                    .addComponent(variantLabel)
                    .addComponent(colorLabel))
                .addGroup(decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(decorationPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fontWeightComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fontVariantComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fontStyleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(decorationPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(colorSelectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(underlineCheckbox)
                    .addComponent(overlineCheckbox)
                    .addComponent(noDecorationCheckbox)
                    .addComponent(strikethroughCheckbox)))
        );

        underlineCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontUnderlineAccessibleDescription")); // NOI18N
        strikethroughCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontStrikeThroughAccessibleDescription")); // NOI18N
        overlineCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontOverlineAccessibleDescription")); // NOI18N
        noDecorationCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontNoDecorationAccessibleDescription")); // NOI18N
        fontStyleComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "STYLE_LIST_ACCES_NAME")); // NOI18N
        fontStyleComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontStyleSelectionAccessibleDescription")); // NOI18N
        fontWeightComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontWeightSelectionAccessibleName")); // NOI18N
        fontWeightComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontWeightSelectionAccessibleDescription")); // NOI18N
        variantLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontVariantSelectionAccessibleDescription")); // NOI18N
        fontVariantComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontVariantSelectionAccessibleName")); // NOI18N
        fontVariantComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FontStyleEditor.class, "FontVariantSelectionAccessibleDescription")); // NOI18N

        styleMainPanel.add(decorationPanel, java.awt.BorderLayout.CENTER);

        mainPanel.add(styleMainPanel, java.awt.BorderLayout.SOUTH);

        add(mainPanel, java.awt.BorderLayout.NORTH);
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
        
    private void strikethroughCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_strikethroughCheckboxItemStateChanged
        if(textDecorationData.enableLineThrough((evt.getStateChange() == evt.SELECTED))) {
            setTextDecoration();
        }
    }//GEN-LAST:event_strikethroughCheckboxItemStateChanged
    
    private void overlineCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_overlineCheckboxItemStateChanged
        if(textDecorationData.enableOverline((evt.getStateChange() == evt.SELECTED))) {
            setTextDecoration();
        }
    }//GEN-LAST:event_overlineCheckboxItemStateChanged
    
    private void underlineCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_underlineCheckboxItemStateChanged
        if(textDecorationData.enableUnderline((evt.getStateChange() == evt.SELECTED))) {
            setTextDecoration();
        }
    }//GEN-LAST:event_underlineCheckboxItemStateChanged
    
    private void fontVariantComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fontVariantComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setFontVariant();
        }
    }//GEN-LAST:event_fontVariantComboBoxItemStateChanged
    
    private void fontVariantComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fontVariantComboBoxFocusLost
        setFontVariant();
    }//GEN-LAST:event_fontVariantComboBoxFocusLost
        
    private void fontWeightComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fontWeightComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setFontWeight();
        }
    }//GEN-LAST:event_fontWeightComboBoxItemStateChanged
        
    private void fontWeightComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fontWeightComboBoxFocusLost
        setFontWeight();
    }//GEN-LAST:event_fontWeightComboBoxFocusLost
        
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
                enableFontSizeUnitCombo(Utils.isFloat(fontSizeField.getText()));
            }
        });
    }//GEN-LAST:event_fontSizeFieldKeyTyped
    
    private void fontSizeListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fontSizeListValueChanged
        if (evt.getValueIsAdjusting()) return;
        String selectedFontSize = (String) fontSizeList.getSelectedValue();
        fontSizeField.setText(selectedFontSize);
        enableFontSizeUnitCombo(Utils.isFloat(selectedFontSize));
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
        String newValue = fontFamilyData.toString();
        cssPropertyChangeSupport().firePropertyChange(CssProperties.FONT_FAMILY, currentFontFamily, newValue);
        currentFontFamily = newValue;
    }
    
    private void setFontSize(){
        //XXX Do we need to put some constraints?
        PropertyWithUnitData fontSizeData = new PropertyWithUnitData();
        fontSizeData.setUnit(fontSizeUnitCombo.getSelectedItem().toString());
        fontSizeData.setValue(fontSizeField.getText());
        String newValue = fontSizeData.toString();
        cssPropertyChangeSupport().firePropertyChange(CssProperties.FONT_SIZE, currentFontSize, newValue);
        currentFontSize = newValue;
    }
    
    private void setFontStyle(){
        PropertyData fontStyleData = new PropertyData();
        fontStyleData.setValue(fontStyleComboBox.getSelectedItem().toString());
        String newValue = fontStyleData.toString();
        cssPropertyChangeSupport().firePropertyChange(CssProperties.FONT_STYLE, currentFontStyle, newValue);
        currentFontStyle = newValue;
    }
    
    private void setFontWeight(){
        PropertyData fontWeightData = new PropertyData();
        fontWeightData.setValue(fontWeightComboBox.getSelectedItem().toString());
        String newValue = fontWeightData.toString();
        cssPropertyChangeSupport().firePropertyChange(CssProperties.FONT_WEIGHT, currentFontWeight, newValue);
        currentFontWeight = newValue;
    }
    
    private void setFontVariant(){
        PropertyData fontVariantData = new PropertyData();
        fontVariantData.setValue(fontVariantComboBox.getSelectedItem().toString());
        String newValue = fontVariantData.toString();
        cssPropertyChangeSupport().firePropertyChange(CssProperties.FONT_VARIANT, currentFontVariant, newValue);
        currentFontVariant = newValue;
    }
    
    private void  setFontColor(){
        PropertyData fontColorData = new PropertyData();
        fontColorData.setValue(colorField.getColorString());
        String newValue = fontColorData.toString();
        cssPropertyChangeSupport().firePropertyChange(CssProperties.COLOR, currentFontColor, newValue);
        currentFontColor = newValue;
    }
    
    private void  setTextDecoration(){
        String newValue = textDecorationData.toString();
        cssPropertyChangeSupport().firePropertyChange(CssProperties.TEXT_DECORATION, currentFontDecoration, newValue);
        currentFontDecoration = newValue;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel colorLabel;
    private javax.swing.JPanel colorSelectionPanel;
    private javax.swing.JPanel decorationPanel;
    private javax.swing.JTextField fontChosenField;
    private javax.swing.JList fontFaceList;
    private javax.swing.JScrollPane fontFaceScroll;
    private javax.swing.JButton fontFamilyButton;
    private javax.swing.JPanel fontFamilyPanel;
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
    private javax.swing.JCheckBox strikethroughCheckbox;
    private javax.swing.JLabel styleLabel;
    private javax.swing.JPanel styleMainPanel;
    private javax.swing.JCheckBox underlineCheckbox;
    private javax.swing.JLabel variantLabel;
    private javax.swing.JLabel weightLabel;
    // End of variables declaration//GEN-END:variables
}
