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

import org.netbeans.modules.visualweb.propertyeditors.css.model.ClipData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.ClipModel;
import org.netbeans.modules.visualweb.propertyeditors.css.model.CssStyleData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.PositionData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.PositionModel;
import org.netbeans.modules.visualweb.propertyeditors.css.model.PropertyData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.PropertyWithUnitData;
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
 * Position Style editor.
 * @author  Winston Prakash
 *          Jeff Hoffman (HIE design)
 */
public class PositionStyleEditor extends StyleEditor {
    CssStyleData cssStyleData = null;
    
    /** Creates new form FontStyleEditor */
    public PositionStyleEditor(CssStyleData styleData) {
        cssStyleData = styleData;
        setName("positionStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(StyleBuilderDialog.class, "POSITION_EDITOR_DISPNAME"));
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
    
    private void initialize(){
        PositionModel positionModel = new PositionModel();
        PositionData positionData = new PositionData();
        
        // Set the Position Mode to the GUI
        DefaultComboBoxModel positionModeList = positionModel.getModeList();
        positionModeCombo.setModel(positionModeList);
        String  positionMode = cssStyleData.getProperty(CssStyleData.POSITION);
        if(positionMode != null){
            positionModeCombo.setSelectedItem(positionMode);
        }else{
            positionModeCombo.setSelectedIndex(0);
        }
        
        // Set the Position Top to the GUI
        DefaultComboBoxModel posTopList = positionModel.getPositionList();
        posTopComboBox.setModel(posTopList);
        posTopUnitComboBox.setModel(positionModel.getPositionUnitList());
        String  posTop = cssStyleData.getProperty(CssStyleData.TOP);
        if(posTop != null){
            positionData.setTop(posTop);
            posTopComboBox.setSelectedItem(positionData.getTopValue());
            posTopUnitComboBox.setSelectedItem(positionData.getTopUnit());
        }else{
            posTopComboBox.setSelectedIndex(0);
            posTopUnitComboBox.setSelectedIndex(0);
        }
        
        // Set the Position Bottom to the GUI
        DefaultComboBoxModel posBottomList = positionModel.getPositionList();
        posBottomComboBox.setModel(posBottomList);
        posBottomUnitComboBox.setModel(positionModel.getPositionUnitList());
        String  posBottom = cssStyleData.getProperty(CssStyleData.BOTTOM);
        if(posBottom != null){
            positionData.setBottom(posBottom);
            posBottomComboBox.setSelectedItem(positionData.getBottomValue());
            posBottomUnitComboBox.setSelectedItem(positionData.getBottomUnit());
        }else{
            posBottomComboBox.setSelectedIndex(0);
            posBottomUnitComboBox.setSelectedIndex(0);
        }
        
        // Set the Position Left to the GUI
        DefaultComboBoxModel posLeftList = positionModel.getPositionList();
        posLeftComboBox.setModel(posLeftList);
        posLeftUnitComboBox.setModel(positionModel.getPositionUnitList());
        String  posLeft = cssStyleData.getProperty(CssStyleData.LEFT);
        if(posLeft != null){
            positionData.setLeft(posLeft);
            posLeftComboBox.setSelectedItem(positionData.getLeftValue());
            posLeftUnitComboBox.setSelectedItem(positionData.getLeftUnit());
        }else{
            posLeftComboBox.setSelectedIndex(0);
            posLeftUnitComboBox.setSelectedIndex(0);
        }
        
        
        // Set the Width to the GUI
        DefaultComboBoxModel widthList = positionModel.getSizeList();
        widthComboBox.setModel(widthList);
        widthUnitComboBox.setModel(positionModel.getPositionUnitList());
        String  width = cssStyleData.getProperty(CssStyleData.WIDTH);
        if(width != null){
            positionData.setWidth(width);
            widthComboBox.setSelectedItem(positionData.getWidthValue());
            widthUnitComboBox.setSelectedItem(positionData.getWidthUnit());
        }else{
            widthComboBox.setSelectedIndex(0);
            widthUnitComboBox.setSelectedIndex(0);
        }
        
        // Set the Height to the GUI
        DefaultComboBoxModel heightList = positionModel.getSizeList();
        heightComboBox.setModel(heightList);
        heightUnitComboBox.setModel(positionModel.getPositionUnitList());
        String height = cssStyleData.getProperty(CssStyleData.HEIGHT);
        if(height != null){
            positionData.setHeight(height);
            heightComboBox.setSelectedItem(positionData.getHeightValue());
            heightUnitComboBox.setSelectedItem(positionData.getHeightUnit());
        }else{
            heightComboBox.setSelectedIndex(0);
            heightUnitComboBox.setSelectedIndex(0);
        }
        
        // Set the Visibility to the GUI
        DefaultComboBoxModel visibilityList = positionModel.getVisibilityList();
        visibleComboBox.setModel(visibilityList);
        String visibility = cssStyleData.getProperty(CssStyleData.VISIBILITY);
        if(visibility != null){
            visibleComboBox.setSelectedItem(visibility);
        }else{
            visibleComboBox.setSelectedIndex(0);
        }
        
        // Set the Visibility to the GUI
        DefaultComboBoxModel zindexList = positionModel.getZIndexList();
        zindexComboBox.setModel(zindexList);
        String zindex = cssStyleData.getProperty(CssStyleData.Z_INDEX);
        if(zindex != null){
            zindexComboBox.setSelectedItem(zindex);
        }else{
            zindexComboBox.setSelectedIndex(0);
        }
        
        // Set the Position Left to the GUI
        DefaultComboBoxModel posRightList = positionModel.getPositionList();
        posRightComboBox.setModel(posRightList);
        posRightUnitComboBox.setModel(positionModel.getPositionUnitList());
        String  posRight = cssStyleData.getProperty(CssStyleData.RIGHT);
        if(posRight != null){
            positionData.setRight(posRight);
            posRightComboBox.setSelectedItem(positionData.getRightValue());
            posRightUnitComboBox.setSelectedItem(positionData.getRightUnit());
        }else{
            posRightComboBox.setSelectedIndex(0);
            posRightUnitComboBox.setSelectedIndex(0);
        }
        
        ClipModel clipModel = new ClipModel();
        ClipData clipData = new ClipData();
        
        // Set the Position Top to the GUI
        clipTopComboBox.setModel(clipModel.getClipList());
        clipTopUnitComboBox.setModel(clipModel.getClipUnitList());
        clipBottomComboBox.setModel(clipModel.getClipList());
        clipBottomUnitComboBox.setModel(clipModel.getClipUnitList());
        clipLeftComboBox.setModel(clipModel.getClipList());
        clipLeftUnitComboBox.setModel(clipModel.getClipUnitList());
        clipRightComboBox.setModel(clipModel.getClipList());
        clipRightUnitComboBox.setModel(clipModel.getClipUnitList());
        
        String  clip = cssStyleData.getProperty(CssStyleData.CLIP);
        if(clip != null){
            clipData.setClip(clip);
            clipTopComboBox.setSelectedItem(clipData.getTopValue());
            clipTopUnitComboBox.setSelectedItem(clipData.getTopUnit());
            clipBottomComboBox.setSelectedItem(clipData.getBottomValue());
            clipBottomUnitComboBox.setSelectedItem(clipData.getBottomUnit());
            clipLeftComboBox.setSelectedItem(clipData.getLeftValue());
            clipLeftUnitComboBox.setSelectedItem(clipData.getLeftUnit());
            clipRightComboBox.setSelectedItem(clipData.getRightValue());
            clipRightUnitComboBox.setSelectedItem(clipData.getRightUnit());
        }else{
            clipTopComboBox.setSelectedIndex(0);
            clipTopUnitComboBox.setSelectedIndex(0);
            clipBottomComboBox.setSelectedIndex(0);
            clipBottomUnitComboBox.setSelectedIndex(0);
            clipLeftComboBox.setSelectedIndex(0);
            clipLeftUnitComboBox.setSelectedIndex(0);
            clipRightComboBox.setSelectedIndex(0);
            clipRightUnitComboBox.setSelectedIndex(0);
        }
        
        posTopComboBox.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        posBottomComboBox.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        posLeftComboBox.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        posRightComboBox.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        
        clipTopComboBox.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        clipBottomComboBox.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        clipLeftComboBox.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
            }
            public void focusLost(FocusEvent evt) {
                errorLabel.setText("");
            }
        });
        clipRightComboBox.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
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
        clipContainerPanel = new javax.swing.JPanel();
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
        errorPanel = new javax.swing.JPanel();
        errorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        mainPositionPanel.setLayout(new java.awt.GridBagLayout());

        positionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_LABEL"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.ABOVE_TOP)); // NOI18N
        positionPanel.setLayout(new java.awt.BorderLayout());

        positionModePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        positionModePanel.setLayout(new java.awt.BorderLayout());

        containerPanel.setLayout(new java.awt.BorderLayout(5, 5));

        positionModeLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_MODE")); // NOI18N
        containerPanel.add(positionModeLabel, java.awt.BorderLayout.WEST);

        positionModeCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                positionModeComboItemStateChanged(evt);
            }
        });
        containerPanel.add(positionModeCombo, java.awt.BorderLayout.CENTER);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle"); // NOI18N
        positionModeCombo.getAccessibleContext().setAccessibleName(bundle.getString("POSITION_MODE_ACCESS_NAME")); // NOI18N
        positionModeCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("POSITION_MODE_ACCESS_DESC")); // NOI18N

        positionModePanel.add(containerPanel, java.awt.BorderLayout.WEST);
        positionModePanel.add(fillPanel, java.awt.BorderLayout.CENTER);

        positionPanel.add(positionModePanel, java.awt.BorderLayout.NORTH);

        positionContainerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));
        positionContainerPanel.setLayout(new java.awt.GridBagLayout());

        posTopLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_TOP")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        positionContainerPanel.add(posTopLabel, gridBagConstraints);

        posTopComboBox.setEditable(true);
        posTopComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posTopComboBoxActionPerformed(evt);
            }
        });
        posTopComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posTopComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posTopComboBox, gridBagConstraints);
        posTopComboBox.getAccessibleContext().setAccessibleName(bundle.getString("POSITION_TOP_ACCESS_NAME")); // NOI18N
        posTopComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("POSITION_TOP_ACCESS_DESC")); // NOI18N

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
        posTopUnitComboBox.getAccessibleContext().setAccessibleName(bundle.getString("POSITION_TOP_UNIT_ACCESS_NAME")); // NOI18N
        posTopUnitComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("POSITION_TOP_UNIT_ACCESS_DESC")); // NOI18N

        posBottomLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_BOTTOM")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 0);
        positionContainerPanel.add(posBottomLabel1, gridBagConstraints);

        posBottomComboBox.setEditable(true);
        posBottomComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posBottomComboBoxActionPerformed(evt);
            }
        });
        posBottomComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posBottomComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posBottomComboBox, gridBagConstraints);
        posBottomComboBox.getAccessibleContext().setAccessibleName(bundle.getString("POSITION_BOTTOM_ACCESS_NAME")); // NOI18N
        posBottomComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("POSITION_BOTTOM_ACCESS_DESC")); // NOI18N

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
        posBottomUnitComboBox.getAccessibleContext().setAccessibleName(bundle.getString("POSITION_BOTTOM_UNIT_ACCESS_NAME")); // NOI18N
        posBottomUnitComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("POSITION_BOTTOM_UNIT_ACCESS_DESC")); // NOI18N

        posRightLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_RIGHT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        positionContainerPanel.add(posRightLabel, gridBagConstraints);

        posRightComboBox.setEditable(true);
        posRightComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posRightComboBoxActionPerformed(evt);
            }
        });
        posRightComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posRightComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        positionContainerPanel.add(posRightComboBox, gridBagConstraints);
        posRightComboBox.getAccessibleContext().setAccessibleName(bundle.getString("POSITION_RIGHT_ACCESS_NAME")); // NOI18N
        posRightComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("POSITION_Right_ACCESS_DESC")); // NOI18N

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
        posRightUnitComboBox.getAccessibleContext().setAccessibleName(bundle.getString("POSITION_RIGHT_UNIT_ACCESS_NAME")); // NOI18N
        posRightUnitComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("POSITION_Right_UNIT_ACCESS_DESC")); // NOI18N

        posLeftLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_LEFT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        positionContainerPanel.add(posLeftLabel1, gridBagConstraints);

        posLeftComboBox.setEditable(true);
        posLeftComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posLeftComboBoxActionPerformed(evt);
            }
        });
        posLeftComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posLeftComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        positionContainerPanel.add(posLeftComboBox, gridBagConstraints);
        posLeftComboBox.getAccessibleContext().setAccessibleName(bundle.getString("POSITION_LEFT_ACCESS_NAME")); // NOI18N
        posLeftComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("POSITION_LEFT_ACCESS_DESC")); // NOI18N

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
        posLeftUnitComboBox.getAccessibleContext().setAccessibleName(bundle.getString("POSITION_LEFT_UNIT_ACCESS_NAME")); // NOI18N
        posLeftUnitComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("POSITION_LEFT_UNIT_ACCESS_DESC")); // NOI18N

        positionPanel.add(positionContainerPanel, java.awt.BorderLayout.WEST);

        mainPositionPanel.add(positionPanel, new java.awt.GridBagConstraints());

        sizePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "SIZE_LABEL"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.ABOVE_TOP)); // NOI18N
        sizePanel.setLayout(new java.awt.BorderLayout());

        sizeContainerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        sizeContainerPanel.setLayout(new java.awt.GridBagLayout());

        heightLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_HEIGHT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        sizeContainerPanel.add(heightLabel, gridBagConstraints);

        heightComboBox.setEditable(true);
        heightComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                heightComboBoxActionPerformed(evt);
            }
        });
        heightComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                heightComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        sizeContainerPanel.add(heightComboBox, gridBagConstraints);
        heightComboBox.getAccessibleContext().setAccessibleName(bundle.getString("HEIGHT_ACCESS_NAME")); // NOI18N
        heightComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("HEIGHT_ACCESS_DESC")); // NOI18N

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
        heightUnitComboBox.getAccessibleContext().setAccessibleName(bundle.getString("HEIGHT_UNIT_ACCESS_NAME")); // NOI18N
        heightUnitComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("HEIGHT_UNIT_ACCESS_DESC")); // NOI18N

        widthLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_WIDTH")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        sizeContainerPanel.add(widthLabel, gridBagConstraints);

        widthComboBox.setEditable(true);
        widthComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                widthComboBoxActionPerformed(evt);
            }
        });
        widthComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                widthComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        sizeContainerPanel.add(widthComboBox, gridBagConstraints);
        widthComboBox.getAccessibleContext().setAccessibleName(bundle.getString("WIDTH_ACCESS_NAME")); // NOI18N
        widthComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("WIDTH_ACCESS_DESC")); // NOI18N

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
        widthUnitComboBox.getAccessibleContext().setAccessibleName(bundle.getString("WIDTH_UNIT_ACCESS_NAME")); // NOI18N
        widthUnitComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("WIDTH_UNIT_ACCESS_DESC")); // NOI18N

        visibleLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "VISIBILITY")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 0);
        sizeContainerPanel.add(visibleLabel1, gridBagConstraints);

        visibleComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                visibleComboBoxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                visibleComboBoxFocusLost(evt);
            }
        });
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
        visibleComboBox.getAccessibleContext().setAccessibleName(bundle.getString("VISIBILITY_ACCESS_NAME")); // NOI18N
        visibleComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("VISIBILITY_ACCESS_DESC")); // NOI18N

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
        zindexComboBox.getAccessibleContext().setAccessibleName(bundle.getString("ZINDEX_ACCESS_NAME")); // NOI18N
        zindexComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ZINDEX_ACCESS_DESC")); // NOI18N

        sizePanel.add(sizeContainerPanel, java.awt.BorderLayout.WEST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        mainPositionPanel.add(sizePanel, gridBagConstraints);

        clipPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_LABEL"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.ABOVE_TOP)); // NOI18N
        clipPanel.setLayout(new java.awt.BorderLayout());

        clipContainerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        clipContainerPanel.setLayout(new java.awt.GridBagLayout());

        clipLeftLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_LEFT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        clipContainerPanel.add(clipLeftLabel1, gridBagConstraints);

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
        clipContainerPanel.add(clipBottomUnitComboBox, gridBagConstraints);
        clipBottomUnitComboBox.getAccessibleContext().setAccessibleName(bundle.getString("CLIP_BOTTOM_UNIT_ACCESS_NAME")); // NOI18N
        clipBottomUnitComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("CLIP_BOTTOM_UNIT_ACCESS_DESC")); // NOI18N

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
        clipContainerPanel.add(clipLeftUnitComboBox, gridBagConstraints);
        clipLeftUnitComboBox.getAccessibleContext().setAccessibleName(bundle.getString("CLIP_LEFT_UNIT_ACCESS_NAME")); // NOI18N
        clipLeftUnitComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("CLIP_LEFT_UNIT_ACCESS_DESC")); // NOI18N

        clipTopLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_TOP")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        clipContainerPanel.add(clipTopLabel1, gridBagConstraints);

        clipLeftComboBox.setEditable(true);
        clipLeftComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clipLeftComboBoxActionPerformed(evt);
            }
        });
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
        clipContainerPanel.add(clipLeftComboBox, gridBagConstraints);
        clipLeftComboBox.getAccessibleContext().setAccessibleName(bundle.getString("CLIP_LEFT_ACCESS_NAME")); // NOI18N
        clipLeftComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("CLIP_LEFT_ACCESS_DESC")); // NOI18N

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
        clipContainerPanel.add(clipTopUnitComboBox, gridBagConstraints);
        clipTopUnitComboBox.getAccessibleContext().setAccessibleName(bundle.getString("CLIP_TOP_UNIT_ACCESS_NAME")); // NOI18N
        clipTopUnitComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("CLIP_TOP_UNIT_ACCESS_DESC")); // NOI18N

        clipRightLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_RIGHT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        clipContainerPanel.add(clipRightLabel1, gridBagConstraints);

        clipBottomComboBox.setEditable(true);
        clipBottomComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clipBottomComboBoxActionPerformed(evt);
            }
        });
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
        clipContainerPanel.add(clipBottomComboBox, gridBagConstraints);
        clipBottomComboBox.getAccessibleContext().setAccessibleName(bundle.getString("CLIP_BOTTOM_ACCESS_NAME")); // NOI18N
        clipBottomComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("CLIP_BOTTOM_ACCESS_DESC")); // NOI18N

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
        clipContainerPanel.add(clipRightUnitComboBox, gridBagConstraints);
        clipRightUnitComboBox.getAccessibleContext().setAccessibleName(bundle.getString("CLIP_RIGHT_UNIT_ACCESS_NAME")); // NOI18N
        clipRightUnitComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("CLIP_RIGHT_UNIT_ACCESS_DESC")); // NOI18N

        clipBottomLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_BOTTOM")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 0);
        clipContainerPanel.add(clipBottomLabel, gridBagConstraints);

        clipTopComboBox.setEditable(true);
        clipTopComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clipTopComboBoxActionPerformed(evt);
            }
        });
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
        clipContainerPanel.add(clipTopComboBox, gridBagConstraints);
        clipTopComboBox.getAccessibleContext().setAccessibleName(bundle.getString("CLIP_TOP_ACCESS_NAME")); // NOI18N
        clipTopComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("CLIP_TOP_ACCESS_DESC")); // NOI18N

        clipRightComboBox.setEditable(true);
        clipRightComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clipRightComboBoxActionPerformed(evt);
            }
        });
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
        clipContainerPanel.add(clipRightComboBox, gridBagConstraints);
        clipRightComboBox.getAccessibleContext().setAccessibleName(bundle.getString("CLIP_RIGHT_ACCESS_NAME")); // NOI18N
        clipRightComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("CLIP_RIGHT_ACCESS_DESC")); // NOI18N

        clipPanel.add(clipContainerPanel, java.awt.BorderLayout.WEST);

        clipErrorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        clipErrorPanel.setLayout(new java.awt.BorderLayout());

        clipErrorLabel.setForeground(new java.awt.Color(0, 0, 153));
        clipErrorLabel.setMinimumSize(new java.awt.Dimension(200, 20));
        clipErrorLabel.setPreferredSize(new java.awt.Dimension(200, 20));
        clipErrorPanel.add(clipErrorLabel, java.awt.BorderLayout.CENTER);

        clipPanel.add(clipErrorPanel, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        mainPositionPanel.add(clipPanel, gridBagConstraints);

        errorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        errorPanel.setLayout(new java.awt.BorderLayout());

        errorLabel.setForeground(new java.awt.Color(0, 0, 153));
        errorLabel.setMinimumSize(new java.awt.Dimension(200, 20));
        errorLabel.setPreferredSize(new java.awt.Dimension(200, 20));
        errorPanel.add(errorLabel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        mainPositionPanel.add(errorPanel, gridBagConstraints);

        add(mainPositionPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    
    private void visibleComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_visibleComboBoxFocusGained
        errorLabel.setText(CssStyleData.PREVIEW_NOT_SUPPORTED);
    }//GEN-LAST:event_visibleComboBoxFocusGained
    
    private void visibleComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_visibleComboBoxFocusLost
        errorLabel.setText("");
    }//GEN-LAST:event_visibleComboBoxFocusLost
    
    private void clipLeftUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipLeftUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setClip();
        }
    }//GEN-LAST:event_clipLeftUnitComboBoxItemStateChanged
    
    private void clipLeftComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipLeftComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setClip();
        }
    }//GEN-LAST:event_clipLeftComboBoxItemStateChanged
    
    private void clipLeftComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clipLeftComboBoxActionPerformed
        setClip();
    }//GEN-LAST:event_clipLeftComboBoxActionPerformed
    
    private void clipBottomUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipBottomUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setClip();
        }
    }//GEN-LAST:event_clipBottomUnitComboBoxItemStateChanged
    
    private void clipBottomComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipBottomComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setClip();
        }
    }//GEN-LAST:event_clipBottomComboBoxItemStateChanged
    
    private void clipBottomComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clipBottomComboBoxActionPerformed
        setClip();
    }//GEN-LAST:event_clipBottomComboBoxActionPerformed
    
    private void clipRightUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipRightUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setClip();
        }
    }//GEN-LAST:event_clipRightUnitComboBoxItemStateChanged
    
    private void clipRightComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipRightComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setClip();
        }
    }//GEN-LAST:event_clipRightComboBoxItemStateChanged
    
    private void clipRightComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clipRightComboBoxActionPerformed
        setClip();
    }//GEN-LAST:event_clipRightComboBoxActionPerformed
    
    private void clipTopUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipTopUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setClip();
        }
    }//GEN-LAST:event_clipTopUnitComboBoxItemStateChanged
    
    private void clipTopComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipTopComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setClip();
        }
    }//GEN-LAST:event_clipTopComboBoxItemStateChanged
    
    private void clipTopComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clipTopComboBoxActionPerformed
        setClip();
    }//GEN-LAST:event_clipTopComboBoxActionPerformed
    
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
    
    private void setClip(){
        ClipData clipData = new ClipData();
        clipData.setTop(clipTopComboBox.getSelectedItem().toString());
        clipData.setTopUnit(clipTopUnitComboBox.getSelectedItem().toString());
        clipTopUnitComboBox.setEnabled(clipData.isTopValueInteger());
        clipData.setRight(clipRightComboBox.getSelectedItem().toString());
        clipData.setRightUnit(clipRightUnitComboBox.getSelectedItem().toString());
        clipRightUnitComboBox.setEnabled(clipData.isRightValueInteger());
        clipData.setBottom(clipBottomComboBox.getSelectedItem().toString());
        clipData.setBottomUnit(clipBottomUnitComboBox.getSelectedItem().toString());
        clipBottomUnitComboBox.setEnabled(clipData.isBottomValueInteger());
        clipData.setLeft(clipLeftComboBox.getSelectedItem().toString());
        clipData.setLeftUnit(clipLeftUnitComboBox.getSelectedItem().toString());
        clipLeftUnitComboBox.setEnabled(clipData.isLeftValueInteger());
        cssStyleData.modifyProperty(CssStyleData.CLIP, clipData.toString());
        if(clipData.hasErros()){
            clipErrorLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_MESSAGE"));
        }else{
            clipErrorLabel.setText("");
        }
    }
    
    private void setZindex(){
        PropertyData zindexData = new PropertyData();
        zindexData.setValue(zindexComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.Z_INDEX, zindexData.toString());
    }
    
    public void setVisibility(){
        PropertyData visibleData = new PropertyData();
        visibleData.setValue(visibleComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.VISIBILITY, visibleData.toString());
    }
    
    private void setHeight(){
        PropertyWithUnitData heightData = new PropertyWithUnitData();
        heightData.setUnit(heightUnitComboBox.getSelectedItem().toString());
        heightData.setValue(heightComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.HEIGHT, heightData.toString());
        heightUnitComboBox.setEnabled(heightData.isValueInteger());
    }
    
    private void setWidth(){
        PropertyWithUnitData widthData = new PropertyWithUnitData();
        widthData.setUnit(widthUnitComboBox.getSelectedItem().toString());
        widthData.setValue(widthComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.WIDTH, widthData.toString());
        widthUnitComboBox.setEnabled(widthData.isValueInteger());
    }
    
    private void setRightPos(){
        PropertyWithUnitData posRightData = new PropertyWithUnitData();
        posRightData.setUnit(posRightUnitComboBox.getSelectedItem().toString());
        posRightData.setValue(posRightComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.RIGHT, posRightData.toString());
        posRightUnitComboBox.setEnabled(posRightData.isValueInteger());
    }
    
    private void setLeftPos(){
        PropertyWithUnitData posLeftData = new PropertyWithUnitData();
        posLeftData.setUnit(posLeftUnitComboBox.getSelectedItem().toString());
        posLeftData.setValue(posLeftComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.LEFT, posLeftData.toString());
        posLeftUnitComboBox.setEnabled(posLeftData.isValueInteger());
    }
    
    private void setBottomPos(){
        PropertyWithUnitData posBottomData = new PropertyWithUnitData();
        posBottomData.setUnit(posBottomUnitComboBox.getSelectedItem().toString());
        posBottomData.setValue(posBottomComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.BOTTOM, posBottomData.toString());
        posBottomUnitComboBox.setEnabled(posBottomData.isValueInteger());
    }
    
    private void setTopPos(){
        PropertyWithUnitData posTopData = new PropertyWithUnitData();
        posTopData.setUnit(posTopUnitComboBox.getSelectedItem().toString());
        posTopData.setValue(posTopComboBox.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.TOP, posTopData.toString());
        posTopUnitComboBox.setEnabled(posTopData.isValueInteger());
    }
    
    public void setPosition(){
        PropertyData positionData = new PropertyData();
        positionData.setValue(positionModeCombo.getSelectedItem().toString());
        cssStyleData.modifyProperty(CssStyleData.POSITION, positionData.toString());
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox clipBottomComboBox;
    private javax.swing.JLabel clipBottomLabel;
    private javax.swing.JComboBox clipBottomUnitComboBox;
    private javax.swing.JPanel clipContainerPanel;
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
    private javax.swing.JLabel errorLabel;
    private javax.swing.JPanel errorPanel;
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
