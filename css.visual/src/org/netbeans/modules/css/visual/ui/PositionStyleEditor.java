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
 * PositionStyleEditor.java
 *
 * Created on October 13, 2004, 12:23 PM
 */

package org.netbeans.modules.css.visual.ui;

import org.netbeans.modules.css.visual.model.ClipData;
import org.netbeans.modules.css.visual.model.ClipModel;
import org.netbeans.modules.css.visual.model.CssProperties;
import org.netbeans.modules.css.model.CssRuleContent;
import org.netbeans.modules.css.visual.model.PositionData;
import org.netbeans.modules.css.visual.model.PositionModel;
import org.netbeans.modules.css.visual.model.PropertyData;
import org.netbeans.modules.css.visual.model.PropertyWithUnitData;
import org.netbeans.modules.css.visual.model.PropertyData;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.netbeans.modules.css.visual.model.Utils;
import org.openide.util.NbBundle;

/**
 * Position Style editor.
 * @author  Winston Prakash
 * @version 1.0
 */
public class PositionStyleEditor extends StyleEditor {

    DefaultComboBoxModel positionModeList;
    PositionData positionData = new PositionData();
    ClipData clipData = new ClipData();

    /** Creates new form FontStyleEditor */
    public PositionStyleEditor() {
        setName("positionStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(PositionStyleEditor.class, "POSITION_EDITOR_DISPNAME"));
        initComponents();
        initialize();

        // Add editor listeners to the width & height combobox
        final JTextField widthComboBoxEditor = (JTextField) widthComboBox.getEditor().getEditorComponent();
        widthComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        widthUnitComboBox.setEnabled(Utils.isInteger(widthComboBoxEditor.getText()));
                    }
                });
            }
        });
        final JTextField heightComboBoxEditor = (JTextField) heightComboBox.getEditor().getEditorComponent();
        heightComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        heightUnitComboBox.setEnabled(Utils.isInteger(heightComboBoxEditor.getText()));
                    }
                });
            }
        });

        // Add editor listeners to the top, right, bottom & left combobox

        final JTextField posTopComboBoxEditor = (JTextField) posTopComboBox.getEditor().getEditorComponent();
        posTopComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                         posTopUnitComboBox.setEnabled(Utils.isInteger(posTopComboBoxEditor.getText()));
                    }
                });
            }
        });
        final JTextField posRightComboBoxEditor = (JTextField) posRightComboBox.getEditor().getEditorComponent();
        posRightComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                         posRightUnitComboBox.setEnabled(Utils.isInteger(posRightComboBoxEditor.getText()));
                    }
                });
            }
        });
        final JTextField posBottomComboBoxEditor = (JTextField) posBottomComboBox.getEditor().getEditorComponent();
        posBottomComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                         posBottomUnitComboBox.setEnabled(Utils.isInteger(posBottomComboBoxEditor.getText()));
                    }
                });   
            }
        });
        final JTextField posLeftComboBoxEditor = (JTextField) posLeftComboBox.getEditor().getEditorComponent();
        posLeftComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                         posLeftUnitComboBox.setEnabled(Utils.isInteger(posLeftComboBoxEditor.getText()));
                    }
                });   
                 
            }
        });
        
        // Add editor listeners to the top, right, bottom & left Clip combobox
        
        final JTextField clipTopComboBoxEditor = (JTextField) clipTopComboBox.getEditor().getEditorComponent();
        clipTopComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        clipTopUnitComboBox.setEnabled(Utils.isInteger(clipTopComboBoxEditor.getText()));
                    }
                });       
            }
        });
        final JTextField clipRightComboBoxEditor = (JTextField) clipRightComboBox.getEditor().getEditorComponent();
        clipRightComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        clipRightUnitComboBox.setEnabled(Utils.isInteger(clipRightComboBoxEditor.getText()));
                    }
                });         
            }
        });
        final JTextField clipBottomComboBoxEditor = (JTextField) clipBottomComboBox.getEditor().getEditorComponent();
        clipBottomComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        clipBottomUnitComboBox.setEnabled(Utils.isInteger(clipBottomComboBoxEditor.getText()));
                    }
                });          
            }
        });
        final JTextField clipLeftComboBoxEditor = (JTextField) clipLeftComboBox.getEditor().getEditorComponent();
        clipLeftComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        clipLeftComboBoxEditor.setEnabled(Utils.isInteger(clipLeftComboBoxEditor.getText()));
                    }
                });     
            }
        });
    }
    
    /**
     * Set the CSS Properties Values from the CssStyleData data structure
     * to the GUI components.
     */
    protected void setCssPropertyValues(CssRuleContent cssStyleData){
        removeCssPropertyChangeListener();
        String  positionMode = cssStyleData.getProperty(CssProperties.POSITION);
        if(positionMode != null){
            positionModeCombo.setSelectedItem(positionMode);
        }else{
            positionModeCombo.setSelectedIndex(0);
        }
        String  posTop = cssStyleData.getProperty(CssProperties.TOP);
        if(posTop != null){
            positionData.setTop(posTop);
            posTopComboBox.setSelectedItem(positionData.getTopValue());
            posTopUnitComboBox.setSelectedItem(positionData.getTopUnit());
        }else{
            posTopComboBox.setSelectedIndex(0);
            posTopUnitComboBox.setSelectedItem("px"); //NOI18N
        }
        
        String  posBottom = cssStyleData.getProperty(CssProperties.BOTTOM);
        if(posBottom != null){
            positionData.setBottom(posBottom);
            posBottomComboBox.setSelectedItem(positionData.getBottomValue());
            posBottomUnitComboBox.setSelectedItem(positionData.getBottomUnit());
        }else{
            posBottomComboBox.setSelectedIndex(0);
            posBottomUnitComboBox.setSelectedItem("px"); //NOI18N
        }
        
        String  posLeft = cssStyleData.getProperty(CssProperties.LEFT);
        if(posLeft != null){
            positionData.setLeft(posLeft);
            posLeftComboBox.setSelectedItem(positionData.getLeftValue());
            posLeftUnitComboBox.setSelectedItem(positionData.getLeftUnit());
        }else{
            posLeftComboBox.setSelectedIndex(0);
            posLeftUnitComboBox.setSelectedItem("px");  //NOI18N
        }
        
        String  width = cssStyleData.getProperty(CssProperties.WIDTH);
        if(width != null){
            positionData.setWidth(width);
            widthComboBox.setSelectedItem(positionData.getWidthValue());
            widthUnitComboBox.setSelectedItem(positionData.getWidthUnit());
        }else{
            widthComboBox.setSelectedIndex(0); 
            widthUnitComboBox.setSelectedItem("px");  //NOI18N
        }
        
        String height = cssStyleData.getProperty(CssProperties.HEIGHT);
        if(height != null){
            positionData.setHeight(height);
            heightComboBox.setSelectedItem(positionData.getHeightValue());
            heightUnitComboBox.setSelectedItem(positionData.getHeightUnit());
        }else{
            heightComboBox.setSelectedIndex(0);
            heightUnitComboBox.setSelectedItem("px");  //NOI18N
        }
        
        String visibility = cssStyleData.getProperty(CssProperties.VISIBILITY);
        if(visibility != null){
            visibleComboBox.setSelectedItem(visibility);
        }else{
            visibleComboBox.setSelectedIndex(0);
        }
        
        String zindex = cssStyleData.getProperty(CssProperties.Z_INDEX);
        if(zindex != null){
            zindexComboBox.setSelectedItem(zindex);
        }else{
            zindexComboBox.setSelectedIndex(0);
        }
        
        String  posRight = cssStyleData.getProperty(CssProperties.RIGHT);
        if(posRight != null){
            positionData.setRight(posRight);
            posRightComboBox.setSelectedItem(positionData.getRightValue());
            posRightUnitComboBox.setSelectedItem(positionData.getRightUnit());
        }else{
            posRightComboBox.setSelectedIndex(0);
            posRightUnitComboBox.setSelectedItem("px");  //NOI18N
        }
        
        String  clip = cssStyleData.getProperty(CssProperties.CLIP);
        clipData.setClip(clip);
        clipTopComboBox.setSelectedItem(clipData.getTopValue());
        clipTopUnitComboBox.setSelectedItem(clipData.getTopUnit());
        clipBottomComboBox.setSelectedItem(clipData.getBottomValue());
        clipBottomUnitComboBox.setSelectedItem(clipData.getBottomUnit());
        clipLeftComboBox.setSelectedItem(clipData.getLeftValue());
        clipLeftUnitComboBox.setSelectedItem(clipData.getLeftUnit());
        clipRightComboBox.setSelectedItem(clipData.getRightValue());
        clipRightUnitComboBox.setSelectedItem(clipData.getRightUnit());
        
        setCssPropertyChangeListener(cssStyleData);
    }
    
    public void initialize(){
        PositionModel positionModel = new PositionModel();
        
        // Set the Position Mode to the GUI
        positionModeList = positionModel.getModeList();
        positionModeCombo.setModel(positionModeList);
        
        // Set the Position Top to the GUI
        DefaultComboBoxModel posTopList = positionModel.getPositionList();
        posTopComboBox.setModel(posTopList);
        posTopUnitComboBox.setModel(positionModel.getPositionUnitList());

        // Set the Position Bottom to the GUI
        DefaultComboBoxModel posBottomList = positionModel.getPositionList();
        posBottomComboBox.setModel(posBottomList);
        posBottomUnitComboBox.setModel(positionModel.getPositionUnitList());

        // Set the Position Left to the GUI
        DefaultComboBoxModel posLeftList = positionModel.getPositionList();
        posLeftComboBox.setModel(posLeftList);
        posLeftUnitComboBox.setModel(positionModel.getPositionUnitList());

        // Set the Width to the GUI
        DefaultComboBoxModel widthList = positionModel.getSizeList();
        widthComboBox.setModel(widthList);
        widthUnitComboBox.setModel(positionModel.getPositionUnitList());

        // Set the Height to the GUI
        DefaultComboBoxModel heightList = positionModel.getSizeList();
        heightComboBox.setModel(heightList);
        heightUnitComboBox.setModel(positionModel.getPositionUnitList());
 
        // Set the Visibility to the GUI
        DefaultComboBoxModel visibilityList = positionModel.getVisibilityList();
        visibleComboBox.setModel(visibilityList);

        // Set the Visibility to the GUI
        DefaultComboBoxModel zindexList = positionModel.getZIndexList();
        zindexComboBox.setModel(zindexList);

        // Set the Position Left to the GUI
        DefaultComboBoxModel posRightList = positionModel.getPositionList();
        posRightComboBox.setModel(posRightList);
        posRightUnitComboBox.setModel(positionModel.getPositionUnitList());

        ClipModel clipModel = new ClipModel();
        
        // Set the Position Top to the GUI
        clipTopComboBox.setModel(clipModel.getClipList());
        clipTopUnitComboBox.setModel(clipModel.getClipUnitList());
        clipBottomComboBox.setModel(clipModel.getClipList());
        clipBottomUnitComboBox.setModel(clipModel.getClipUnitList());
        clipLeftComboBox.setModel(clipModel.getClipList());
        clipLeftUnitComboBox.setModel(clipModel.getClipUnitList());
        clipRightComboBox.setModel(clipModel.getClipList());
        clipRightUnitComboBox.setModel(clipModel.getClipUnitList());
        
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPositionPanel = new javax.swing.JPanel();
        positionPanel = new javax.swing.JPanel();
        positionModePanel = new javax.swing.JPanel();
        containerPanel = new javax.swing.JPanel();
        positionModeLabel = new javax.swing.JLabel();
        positionModeCombo = new javax.swing.JComboBox();
        fillPanel = new javax.swing.JPanel();
        positionContainerPanel = new javax.swing.JPanel();
        posTopLabel = new javax.swing.JLabel();
        posTopComboBox = new javax.swing.JComboBox();
        posTopUnitComboBox = new javax.swing.JComboBox();
        posBottomLabel1 = new javax.swing.JLabel();
        posBottomComboBox = new javax.swing.JComboBox();
        posBottomUnitComboBox = new javax.swing.JComboBox();
        posRightLabel = new javax.swing.JLabel();
        posRightComboBox = new javax.swing.JComboBox();
        posRightUnitComboBox = new javax.swing.JComboBox();
        posLeftLabel1 = new javax.swing.JLabel();
        posLeftComboBox = new javax.swing.JComboBox();
        posLeftUnitComboBox = new javax.swing.JComboBox();
        sizePanel = new javax.swing.JPanel();
        sizeContainerPanel = new javax.swing.JPanel();
        heightLabel = new javax.swing.JLabel();
        heightComboBox = new javax.swing.JComboBox();
        heightUnitComboBox = new javax.swing.JComboBox();
        widthLabel = new javax.swing.JLabel();
        widthComboBox = new javax.swing.JComboBox();
        widthUnitComboBox = new javax.swing.JComboBox();
        visibleLabel1 = new javax.swing.JLabel();
        visibleComboBox = new javax.swing.JComboBox();
        zIndexLabel1 = new javax.swing.JLabel();
        zindexComboBox = new javax.swing.JComboBox();
        clipPanel = new javax.swing.JPanel();
        clipLeftLabel1 = new javax.swing.JLabel();
        clipBottomUnitComboBox = new javax.swing.JComboBox();
        clipLeftUnitComboBox = new javax.swing.JComboBox();
        clipTopLabel1 = new javax.swing.JLabel();
        clipLeftComboBox = new javax.swing.JComboBox();
        clipTopUnitComboBox = new javax.swing.JComboBox();
        clipRightLabel1 = new javax.swing.JLabel();
        clipBottomComboBox = new javax.swing.JComboBox();
        clipRightUnitComboBox = new javax.swing.JComboBox();
        clipBottomLabel = new javax.swing.JLabel();
        clipTopComboBox = new javax.swing.JComboBox();
        clipRightComboBox = new javax.swing.JComboBox();
        clipErrorPanel = new javax.swing.JPanel();
        clipErrorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        mainPositionPanel.setLayout(new java.awt.GridBagLayout());

        positionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_EDITOR_DISPNAME"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.ABOVE_TOP, new java.awt.Font("Dialog", 1, 14))); // NOI18N
        positionPanel.setLayout(new java.awt.BorderLayout());

        positionModePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        positionModePanel.setLayout(new java.awt.BorderLayout());

        containerPanel.setLayout(new java.awt.BorderLayout(5, 5));

        positionModeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_MODE").charAt(0));
        positionModeLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_MODE")); // NOI18N
        containerPanel.add(positionModeLabel, java.awt.BorderLayout.WEST);

        positionModeCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                positionModeComboItemStateChanged(evt);
            }
        });
        containerPanel.add(positionModeCombo, java.awt.BorderLayout.CENTER);
        positionModeCombo.getAccessibleContext().setAccessibleName(null);
        positionModeCombo.getAccessibleContext().setAccessibleDescription(null);

        positionModePanel.add(containerPanel, java.awt.BorderLayout.WEST);
        positionModePanel.add(fillPanel, java.awt.BorderLayout.CENTER);

        positionPanel.add(positionModePanel, java.awt.BorderLayout.NORTH);

        positionContainerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));
        positionContainerPanel.setLayout(new java.awt.GridBagLayout());

        posTopLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_TOP").charAt(0));
        posTopLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_TOP")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        positionContainerPanel.add(posTopLabel, gridBagConstraints);

        posTopComboBox.setEditable(true);
        posTopComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posTopComboBoxItemStateChanged(evt);
            }
        });
        posTopComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posTopComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posTopComboBox, gridBagConstraints);
        posTopComboBox.getAccessibleContext().setAccessibleName(null);
        posTopComboBox.getAccessibleContext().setAccessibleDescription(null);

        posTopUnitComboBox.setEnabled(false);
        posTopUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posTopUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posTopUnitComboBox, gridBagConstraints);
        posTopUnitComboBox.getAccessibleContext().setAccessibleName(null);
        posTopUnitComboBox.getAccessibleContext().setAccessibleDescription(null);

        posBottomLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_BOTTOM").charAt(0));
        posBottomLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_BOTTOM")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 0);
        positionContainerPanel.add(posBottomLabel1, gridBagConstraints);

        posBottomComboBox.setEditable(true);
        posBottomComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posBottomComboBoxItemStateChanged(evt);
            }
        });
        posBottomComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posBottomComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posBottomComboBox, gridBagConstraints);
        posBottomComboBox.getAccessibleContext().setAccessibleName(null);
        posBottomComboBox.getAccessibleContext().setAccessibleDescription(null);

        posBottomUnitComboBox.setEnabled(false);
        posBottomUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posBottomUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posBottomUnitComboBox, gridBagConstraints);
        posBottomUnitComboBox.getAccessibleContext().setAccessibleName(null);
        posBottomUnitComboBox.getAccessibleContext().setAccessibleDescription(null);

        posRightLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_RIGHT").charAt(0));
        posRightLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_RIGHT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        positionContainerPanel.add(posRightLabel, gridBagConstraints);

        posRightComboBox.setEditable(true);
        posRightComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posRightComboBoxItemStateChanged(evt);
            }
        });
        posRightComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posRightComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        positionContainerPanel.add(posRightComboBox, gridBagConstraints);
        posRightComboBox.getAccessibleContext().setAccessibleName(null);
        posRightComboBox.getAccessibleContext().setAccessibleDescription(null);

        posRightUnitComboBox.setEnabled(false);
        posRightUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posRightUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        positionContainerPanel.add(posRightUnitComboBox, gridBagConstraints);
        posRightUnitComboBox.getAccessibleContext().setAccessibleName(null);
        posRightUnitComboBox.getAccessibleContext().setAccessibleDescription(null);

        posLeftLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_LEFT").charAt(0));
        posLeftLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_LEFT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        positionContainerPanel.add(posLeftLabel1, gridBagConstraints);

        posLeftComboBox.setEditable(true);
        posLeftComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posLeftComboBoxItemStateChanged(evt);
            }
        });
        posLeftComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posLeftComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        positionContainerPanel.add(posLeftComboBox, gridBagConstraints);
        posLeftComboBox.getAccessibleContext().setAccessibleName(null);
        posLeftComboBox.getAccessibleContext().setAccessibleDescription(null);

        posLeftUnitComboBox.setEnabled(false);
        posLeftUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posLeftUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        positionContainerPanel.add(posLeftUnitComboBox, gridBagConstraints);
        posLeftUnitComboBox.getAccessibleContext().setAccessibleName(null);
        posLeftUnitComboBox.getAccessibleContext().setAccessibleDescription(null);

        positionPanel.add(positionContainerPanel, java.awt.BorderLayout.WEST);

        mainPositionPanel.add(positionPanel, new java.awt.GridBagConstraints());

        sizePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "SIZE_TITLE"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.ABOVE_TOP, new java.awt.Font("Dialog", 1, 14))); // NOI18N
        sizePanel.setLayout(new java.awt.BorderLayout());

        sizeContainerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        sizeContainerPanel.setLayout(new java.awt.GridBagLayout());

        heightLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_HEIGHT").charAt(0));
        heightLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_HEIGHT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        sizeContainerPanel.add(heightLabel, gridBagConstraints);

        heightComboBox.setEditable(true);
        heightComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                heightComboBoxItemStateChanged(evt);
            }
        });
        heightComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                heightComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        sizeContainerPanel.add(heightComboBox, gridBagConstraints);
        heightComboBox.getAccessibleContext().setAccessibleName(null);
        heightComboBox.getAccessibleContext().setAccessibleDescription(null);

        heightUnitComboBox.setEnabled(false);
        heightUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                heightUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        sizeContainerPanel.add(heightUnitComboBox, gridBagConstraints);
        heightUnitComboBox.getAccessibleContext().setAccessibleName(null);
        heightUnitComboBox.getAccessibleContext().setAccessibleDescription(null);

        widthLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_WIDTH").charAt(0));
        widthLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_WIDTH")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        sizeContainerPanel.add(widthLabel, gridBagConstraints);

        widthComboBox.setEditable(true);
        widthComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                widthComboBoxItemStateChanged(evt);
            }
        });
        widthComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                widthComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        sizeContainerPanel.add(widthComboBox, gridBagConstraints);
        widthComboBox.getAccessibleContext().setAccessibleName(null);
        widthComboBox.getAccessibleContext().setAccessibleDescription(null);

        widthUnitComboBox.setEnabled(false);
        widthUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                widthUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        sizeContainerPanel.add(widthUnitComboBox, gridBagConstraints);
        widthUnitComboBox.getAccessibleContext().setAccessibleName(null);
        widthUnitComboBox.getAccessibleContext().setAccessibleDescription(null);

        visibleLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_VISIBILITY").charAt(0));
        visibleLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "VISIBILITY")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 0);
        sizeContainerPanel.add(visibleLabel1, gridBagConstraints);

        visibleComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                visibleComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        sizeContainerPanel.add(visibleComboBox, gridBagConstraints);
        visibleComboBox.getAccessibleContext().setAccessibleName(null);
        visibleComboBox.getAccessibleContext().setAccessibleDescription(null);

        zIndexLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_Z_INDEX").charAt(0));
        zIndexLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "Z_INDEX")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        sizeContainerPanel.add(zIndexLabel1, gridBagConstraints);

        zindexComboBox.setEditable(true);
        zindexComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zindexComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        sizeContainerPanel.add(zindexComboBox, gridBagConstraints);
        zindexComboBox.getAccessibleContext().setAccessibleName(null);
        zindexComboBox.getAccessibleContext().setAccessibleDescription(null);

        sizePanel.add(sizeContainerPanel, java.awt.BorderLayout.WEST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        mainPositionPanel.add(sizePanel, gridBagConstraints);

        clipPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_TITLE"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.ABOVE_TOP, new java.awt.Font("Dialog", 1, 14))); // NOI18N
        clipPanel.setLayout(new java.awt.GridBagLayout());

        clipLeftLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("CLIP_LEFT").charAt(0));
        clipLeftLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_LEFT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        clipPanel.add(clipLeftLabel1, gridBagConstraints);

        clipBottomUnitComboBox.setEnabled(false);
        clipBottomUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipBottomUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        clipPanel.add(clipBottomUnitComboBox, gridBagConstraints);
        clipBottomUnitComboBox.getAccessibleContext().setAccessibleName(null);
        clipBottomUnitComboBox.getAccessibleContext().setAccessibleDescription(null);

        clipLeftUnitComboBox.setEnabled(false);
        clipLeftUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipLeftUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        clipPanel.add(clipLeftUnitComboBox, gridBagConstraints);
        clipLeftUnitComboBox.getAccessibleContext().setAccessibleName(null);
        clipLeftUnitComboBox.getAccessibleContext().setAccessibleDescription(null);

        clipTopLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_CLIP_TOP").charAt(0));
        clipTopLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_TOP")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        clipPanel.add(clipTopLabel1, gridBagConstraints);

        clipLeftComboBox.setEditable(true);
        clipLeftComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipLeftComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        clipPanel.add(clipLeftComboBox, gridBagConstraints);
        clipLeftComboBox.getAccessibleContext().setAccessibleName(null);
        clipLeftComboBox.getAccessibleContext().setAccessibleDescription(null);

        clipTopUnitComboBox.setEnabled(false);
        clipTopUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipTopUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        clipPanel.add(clipTopUnitComboBox, gridBagConstraints);
        clipTopUnitComboBox.getAccessibleContext().setAccessibleName(null);
        clipTopUnitComboBox.getAccessibleContext().setAccessibleDescription(null);

        clipRightLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_CLIP_RIGHT").charAt(0));
        clipRightLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_RIGHT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        clipPanel.add(clipRightLabel1, gridBagConstraints);

        clipBottomComboBox.setEditable(true);
        clipBottomComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipBottomComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        clipPanel.add(clipBottomComboBox, gridBagConstraints);
        clipBottomComboBox.getAccessibleContext().setAccessibleName(null);
        clipBottomComboBox.getAccessibleContext().setAccessibleDescription(null);

        clipRightUnitComboBox.setEnabled(false);
        clipRightUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipRightUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        clipPanel.add(clipRightUnitComboBox, gridBagConstraints);
        clipRightUnitComboBox.getAccessibleContext().setAccessibleName(null);
        clipRightUnitComboBox.getAccessibleContext().setAccessibleDescription(null);

        clipBottomLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("CLIP_BOTTOM").charAt(0));
        clipBottomLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_BOTTOM")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 0);
        clipPanel.add(clipBottomLabel, gridBagConstraints);

        clipTopComboBox.setEditable(true);
        clipTopComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipTopComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        clipPanel.add(clipTopComboBox, gridBagConstraints);
        clipTopComboBox.getAccessibleContext().setAccessibleName(null);
        clipTopComboBox.getAccessibleContext().setAccessibleDescription(null);

        clipRightComboBox.setEditable(true);
        clipRightComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipRightComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        clipPanel.add(clipRightComboBox, gridBagConstraints);
        clipRightComboBox.getAccessibleContext().setAccessibleName(null);
        clipRightComboBox.getAccessibleContext().setAccessibleDescription(null);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        mainPositionPanel.add(clipPanel, gridBagConstraints);

        add(mainPositionPanel, java.awt.BorderLayout.NORTH);

        clipErrorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        clipErrorPanel.setLayout(new java.awt.BorderLayout());

        clipErrorLabel.setForeground(new java.awt.Color(0, 0, 153));
        clipErrorPanel.add(clipErrorLabel, java.awt.BorderLayout.CENTER);

        add(clipErrorPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    
    private void clipLeftUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipLeftUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setLeftUnit(clipLeftUnitComboBox.getSelectedItem().toString());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipLeftUnitComboBoxItemStateChanged
    
    private void clipLeftComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipLeftComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setLeft(clipLeftComboBox.getSelectedItem().toString());
            clipLeftUnitComboBox.setEnabled(clipData.isLeftValueInteger());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipLeftComboBoxItemStateChanged
        
    private void clipBottomUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipBottomUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setBottomUnit(clipBottomUnitComboBox.getSelectedItem().toString());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipBottomUnitComboBoxItemStateChanged
    
    private void clipBottomComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipBottomComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setBottom(clipBottomComboBox.getSelectedItem().toString());
            clipBottomUnitComboBox.setEnabled(clipData.isBottomValueInteger());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipBottomComboBoxItemStateChanged
        
    private void clipRightUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipRightUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setRightUnit(clipRightUnitComboBox.getSelectedItem().toString());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipRightUnitComboBoxItemStateChanged
    
    private void clipRightComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipRightComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setRight(clipRightComboBox.getSelectedItem().toString());
            clipRightUnitComboBox.setEnabled(clipData.isRightValueInteger());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipRightComboBoxItemStateChanged
        
    private void clipTopUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipTopUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setTopUnit(clipTopUnitComboBox.getSelectedItem().toString());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipTopUnitComboBoxItemStateChanged
    
    private void clipTopComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipTopComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setTop(clipTopComboBox.getSelectedItem().toString());
            clipTopUnitComboBox.setEnabled(clipData.isTopValueInteger());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipTopComboBoxItemStateChanged
        
    private void zindexComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zindexComboBoxItemStateChanged
        setZindex();
    }//GEN-LAST:event_zindexComboBoxItemStateChanged
    
    private void visibleComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_visibleComboBoxItemStateChanged
        setVisibility();
    }//GEN-LAST:event_visibleComboBoxItemStateChanged
    
    private void heightUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_heightUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setHeight();
        }
    }//GEN-LAST:event_heightUnitComboBoxItemStateChanged
    
    private void heightComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_heightComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setHeight();
        }
    }//GEN-LAST:event_heightComboBoxItemStateChanged
    
    private void heightComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heightComboBoxActionPerformed
        setHeight();
    }//GEN-LAST:event_heightComboBoxActionPerformed
    
    private void widthComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_widthComboBoxActionPerformed
        setWidth();
    }//GEN-LAST:event_widthComboBoxActionPerformed
    
    private void widthComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_widthComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setWidth();
        }
    }//GEN-LAST:event_widthComboBoxItemStateChanged
    
    private void widthUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_widthUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setWidth();
        }
    }//GEN-LAST:event_widthUnitComboBoxItemStateChanged
    
    private void posRightUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posRightUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setRightPos();
        }
    }//GEN-LAST:event_posRightUnitComboBoxItemStateChanged
    
    private void posRightComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posRightComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setRightPos();
        }
    }//GEN-LAST:event_posRightComboBoxItemStateChanged
    
    private void posRightComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posRightComboBoxActionPerformed
        setRightPos();
    }//GEN-LAST:event_posRightComboBoxActionPerformed
    
    private void posLeftUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posLeftUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setLeftPos();
        }
    }//GEN-LAST:event_posLeftUnitComboBoxItemStateChanged
    
    private void posLeftComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posLeftComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setLeftPos();
        }
    }//GEN-LAST:event_posLeftComboBoxItemStateChanged
    
    private void posLeftComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posLeftComboBoxActionPerformed
        setLeftPos();
    }//GEN-LAST:event_posLeftComboBoxActionPerformed
    
    private void posBottomUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posBottomUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setBottomPos();
        }
    }//GEN-LAST:event_posBottomUnitComboBoxItemStateChanged
    
    private void posBottomComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posBottomComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setBottomPos();
        }
    }//GEN-LAST:event_posBottomComboBoxItemStateChanged
    
    private void posBottomComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posBottomComboBoxActionPerformed
        setBottomPos();
    }//GEN-LAST:event_posBottomComboBoxActionPerformed
    
    private void posTopUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posTopUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setTopPos();
        }
    }//GEN-LAST:event_posTopUnitComboBoxItemStateChanged
    
    private void posTopComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posTopComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setTopPos();
        }
    }//GEN-LAST:event_posTopComboBoxItemStateChanged
    
    private void posTopComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posTopComboBoxActionPerformed
        setTopPos();
    }//GEN-LAST:event_posTopComboBoxActionPerformed
    
    private void positionModeComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_positionModeComboItemStateChanged
        setPosition();
    }//GEN-LAST:event_positionModeComboItemStateChanged
    
    private void setZindex(){
        PropertyData zindexData = new PropertyData();
        zindexData.setValue(zindexComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.Z_INDEX, null, zindexData.toString());
    }
    
    public void setVisibility(){
        PropertyData visibleData = new PropertyData();
        visibleData.setValue(visibleComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.VISIBILITY, null, visibleData.toString());
    }
    
    private void setHeight(){
        PropertyWithUnitData heightData = new PropertyWithUnitData();
        heightData.setUnit(heightUnitComboBox.getSelectedItem().toString());
        heightData.setValue(heightComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.HEIGHT, null, heightData.toString());
        heightUnitComboBox.setEnabled(heightData.isValueInteger());
    }
    
    private void setWidth(){
        PropertyWithUnitData widthData = new PropertyWithUnitData();
        widthData.setUnit(widthUnitComboBox.getSelectedItem().toString());
        widthData.setValue(widthComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.WIDTH, null, widthData.toString());
        widthUnitComboBox.setEnabled(widthData.isValueInteger());
    }
    
    private void setRightPos(){
        PropertyWithUnitData posRightData = new PropertyWithUnitData();
        posRightData.setUnit(posRightUnitComboBox.getSelectedItem().toString());
        posRightData.setValue(posRightComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.RIGHT, null, posRightData.toString());
        posRightUnitComboBox.setEnabled(posRightData.isValueInteger());
    }
    
    private void setLeftPos(){
        PropertyWithUnitData posLeftData = new PropertyWithUnitData();
        posLeftData.setUnit(posLeftUnitComboBox.getSelectedItem().toString());
        posLeftData.setValue(posLeftComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.LEFT, null, posLeftData.toString());
        posLeftUnitComboBox.setEnabled(posLeftData.isValueInteger());
    }
    
    private void setBottomPos(){
        PropertyWithUnitData posBottomData = new PropertyWithUnitData();
        posBottomData.setUnit(posBottomUnitComboBox.getSelectedItem().toString());
        posBottomData.setValue(posBottomComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.BOTTOM, null, posBottomData.toString());
        posBottomUnitComboBox.setEnabled(posBottomData.isValueInteger());
    }
    
    private void setTopPos(){
        PropertyWithUnitData posTopData = new PropertyWithUnitData();
        posTopData.setUnit(posTopUnitComboBox.getSelectedItem().toString());
        posTopData.setValue(posTopComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.TOP, null, posTopData.toString());
        posTopUnitComboBox.setEnabled(posTopData.isValueInteger());
    }
    
    public void setPosition(){
        PropertyData positionData = new PropertyData();
        positionData.setValue(positionModeCombo.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.POSITION, null, positionData.toString());
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox clipBottomComboBox;
    private javax.swing.JLabel clipBottomLabel;
    private javax.swing.JComboBox clipBottomUnitComboBox;
    private javax.swing.JLabel clipErrorLabel;
    private javax.swing.JPanel clipErrorPanel;
    private javax.swing.JComboBox clipLeftComboBox;
    private javax.swing.JLabel clipLeftLabel1;
    private javax.swing.JComboBox clipLeftUnitComboBox;
    private javax.swing.JPanel clipPanel;
    private javax.swing.JComboBox clipRightComboBox;
    private javax.swing.JLabel clipRightLabel1;
    private javax.swing.JComboBox clipRightUnitComboBox;
    private javax.swing.JComboBox clipTopComboBox;
    private javax.swing.JLabel clipTopLabel1;
    private javax.swing.JComboBox clipTopUnitComboBox;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JComboBox heightComboBox;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JComboBox heightUnitComboBox;
    private javax.swing.JPanel mainPositionPanel;
    private javax.swing.JComboBox posBottomComboBox;
    private javax.swing.JLabel posBottomLabel1;
    private javax.swing.JComboBox posBottomUnitComboBox;
    private javax.swing.JComboBox posLeftComboBox;
    private javax.swing.JLabel posLeftLabel1;
    private javax.swing.JComboBox posLeftUnitComboBox;
    private javax.swing.JComboBox posRightComboBox;
    private javax.swing.JLabel posRightLabel;
    private javax.swing.JComboBox posRightUnitComboBox;
    private javax.swing.JComboBox posTopComboBox;
    private javax.swing.JLabel posTopLabel;
    private javax.swing.JComboBox posTopUnitComboBox;
    private javax.swing.JPanel positionContainerPanel;
    private javax.swing.JComboBox positionModeCombo;
    private javax.swing.JLabel positionModeLabel;
    private javax.swing.JPanel positionModePanel;
    private javax.swing.JPanel positionPanel;
    private javax.swing.JPanel sizeContainerPanel;
    private javax.swing.JPanel sizePanel;
    private javax.swing.JComboBox visibleComboBox;
    private javax.swing.JLabel visibleLabel1;
    private javax.swing.JComboBox widthComboBox;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JComboBox widthUnitComboBox;
    private javax.swing.JLabel zIndexLabel1;
    private javax.swing.JComboBox zindexComboBox;
    // End of variables declaration//GEN-END:variables
    
}
