/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * FontFamilyEditorDialog.java
 *
 * Created on October 18, 2004, 3:47 PM
 */

package org.netbeans.modules.css.visual.ui;

import org.netbeans.modules.css.visual.model.FontModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Font Family Editor Dialog
 * @author  Winston Prakash
 * @version 1.0
 */
public class FontFamilyEditorDialog extends javax.swing.JPanel {
    private JDialog dialog;
    private DialogDescriptor dlg = null;
    private String closeButtonLabel =  NbBundle.getMessage(FontFamilyEditorDialog.class, "Close_Button_Label");
    
    private JButton closeButton = new JButton(closeButtonLabel);

    DefaultListModel fontFamilies = null;
    DefaultListModel selectedFonts = new DefaultListModel();
    DefaultListModel availableFonts = new DefaultListModel();

    FontModel fontModel = new FontModel();

    int currentIndex = 1;

    /** Creates new form TestPanel */
    public FontFamilyEditorDialog(DefaultListModel fontFamilies, int currIndex) {
        initComponents();
        currentIndex = currIndex;
        this.fontFamilies = fontFamilies;
        fontFaceList.setModel(fontFamilies);
        fontFaceList.setSelectedIndex(currentIndex);
        fontSelectionCombo.setModel(fontModel.getFontSelectionList());
        fontSelectionCombo.setSelectedIndex(0);
        availableFonts = fontModel.getFontList();
        availableFontList.setModel(availableFonts);
        selectedFontList.setModel(selectedFonts);
        availableFontList.setSelectedIndex(0);
    }

    public void showDialog(){
        // Add a listener to the dialog's buttons
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dialog.setVisible(false);
            }
        };
        dlg = new DialogDescriptor(this, NbBundle.getMessage(FontFamilyEditorDialog.class, "FONT_FAMILY_EDITOR_TITLE"), true, listener);
        dlg.setOptions(new Object[] { closeButton });
        dlg.setClosingOptions(new Object[] {closeButton});
        //dlg.setHelpCtx(new HelpCtx("projrave_ui_elements_server_nav_add_datasourcedb")); // NOI18N

        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setResizable(true);
        dialog.pack();
        dialog.setVisible(true);
    }

    public int getSelectedIndex(){
        return currentIndex;
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        currentFontFamilyPanel = new javax.swing.JPanel();
        fontFamilyScroll = new javax.swing.JScrollPane();
        fontFaceList = new javax.swing.JList();
        newDeleteButtonPanel = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        currentFontFamiliesLabel = new javax.swing.JLabel();
        fontFamilyEditor = new javax.swing.JPanel();
        availableFontScroll = new javax.swing.JScrollPane();
        availableFontList = new javax.swing.JList();
        selectedFontScroll = new javax.swing.JScrollPane();
        selectedFontList = new javax.swing.JList();
        availableLabel = new javax.swing.JLabel();
        selectedLabel = new javax.swing.JLabel();
        addRemoveButtonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        upDownPanel = new javax.swing.JPanel();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        fontSelectionCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout(5, 5));

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        currentFontFamilyPanel.setLayout(new java.awt.GridBagLayout());

        fontFaceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fontFaceList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fontFaceListValueChanged(evt);
            }
        });

        fontFamilyScroll.setViewportView(fontFaceList);
        fontFaceList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "CURRENT_FONTS"));
        fontFaceList.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("FONT_FAMILY_LIST_ACCESS_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        currentFontFamilyPanel.add(fontFamilyScroll, gridBagConstraints);

        newDeleteButtonPanel.setLayout(new java.awt.GridLayout(0, 1, 0, 5));

        newButton.setMnemonic(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "FONT_FAMILY_NEW_BUTTON_MNEMONIC").charAt(0));
        newButton.setText(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "NEW"));
        newButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        newDeleteButtonPanel.add(newButton);
        newButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("ADD_FONT_FAMILY_ACCESS_DESC"));

        deleteButton.setMnemonic(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "FONT_FAMILY_DELETE_BUTTON_MNEMONIC").charAt(0));
        deleteButton.setText(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "DELETE"));
        deleteButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        newDeleteButtonPanel.add(deleteButton);
        deleteButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("DELETE_FONT_FAMILY_ACCESS_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        currentFontFamilyPanel.add(newDeleteButtonPanel, gridBagConstraints);

        currentFontFamiliesLabel.setLabelFor(fontFaceList);
        currentFontFamiliesLabel.setText(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "CURRENT_FONTS"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        currentFontFamilyPanel.add(currentFontFamiliesLabel, gridBagConstraints);

        add(currentFontFamilyPanel, java.awt.BorderLayout.CENTER);

        fontFamilyEditor.setLayout(new java.awt.GridBagLayout());

        availableFontScroll.setPreferredSize(new java.awt.Dimension(200, 138));
        availableFontList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                availableFontListValueChanged(evt);
            }
        });

        availableFontScroll.setViewportView(availableFontList);
        availableFontList.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("AVAILABLE_FONT_LIST_ACCESS_NAME"));
        availableFontList.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("AVAILABLE_FONT_LIST_ACCESS_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 10);
        fontFamilyEditor.add(availableFontScroll, gridBagConstraints);

        selectedFontScroll.setPreferredSize(new java.awt.Dimension(200, 130));
        selectedFontList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                selectedFontListValueChanged(evt);
            }
        });

        selectedFontScroll.setViewportView(selectedFontList);
        selectedFontList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "SELECTED"));
        selectedFontList.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("SELECTED_FONT_LIST_ACCESS_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        fontFamilyEditor.add(selectedFontScroll, gridBagConstraints);

        availableLabel.setLabelFor(fontSelectionCombo);
        availableLabel.setText(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "AVAILABLE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        fontFamilyEditor.add(availableLabel, gridBagConstraints);

        selectedLabel.setLabelFor(selectedFontList);
        selectedLabel.setText(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "SELECTED"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        fontFamilyEditor.add(selectedLabel, gridBagConstraints);
        selectedLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("SELECTED_FONT_LIST_ACCESS_DESC"));

        addRemoveButtonPanel.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        addButton.setText(">");
        addButton.setToolTipText(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "Add_selected_fonts"));
        addButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        addRemoveButtonPanel.add(addButton);
        addButton.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("ADD_FONT_ACCESS_NAME"));

        removeButton.setText("<");
        removeButton.setToolTipText(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "Remove_selected_fonts"));
        removeButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        addRemoveButtonPanel.add(removeButton);
        removeButton.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("REMOVE_FONT_ACCESS_NAME"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        fontFamilyEditor.add(addRemoveButtonPanel, gridBagConstraints);

        upDownPanel.setLayout(new java.awt.GridLayout(0, 1, 0, 5));

        upButton.setMnemonic(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "FONT_FAMILY_UP_BUTTON_MNEMONIC").charAt(0));
        upButton.setText(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "UP"));
        upButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        upDownPanel.add(upButton);
        upButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MOVE_FONT_UP_ACCESS_NAME"));

        downButton.setMnemonic(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "FONT_FAMILY_DOWN_BUTTON_MNEMONIC").charAt(0));
        downButton.setText(org.openide.util.NbBundle.getMessage(FontFamilyEditorDialog.class, "DOWN"));
        downButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        upDownPanel.add(downButton);
        downButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MOVE_FONT_DOWN_ACCESS_NAME"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        fontFamilyEditor.add(upDownPanel, gridBagConstraints);

        fontSelectionCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fontSelectionComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        fontFamilyEditor.add(fontSelectionCombo, gridBagConstraints);
        fontSelectionCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("AVAILABLE_FONT_COMBO_ACCESS_DESC"));

        add(fontFamilyEditor, java.awt.BorderLayout.SOUTH);

    }// </editor-fold>//GEN-END:initComponents
    
    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        if (selectedFonts.getSize() > 0){
            int index = selectedFontList.getSelectedIndex();
            if((index >= 0) && index < selectedFonts.getSize()){
                Object currentObject = selectedFonts.get(index);
                Object prevObject = selectedFonts.get(index+1);
                selectedFonts.setElementAt(currentObject, index+1);
                selectedFonts.setElementAt(prevObject, index);
                selectedFontList.setSelectedIndex(index+1);
                resetFontFamilySet();
            }
        }
    }//GEN-LAST:event_downButtonActionPerformed
    
    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        if (selectedFonts.getSize() > 0){
            int index = selectedFontList.getSelectedIndex();
            if(index > 0){
                Object currentObject = selectedFonts.get(index);
                Object prevObject = selectedFonts.get(index-1);
                selectedFonts.setElementAt(currentObject, index-1);
                selectedFonts.setElementAt(prevObject, index);
                selectedFontList.setSelectedIndex(index-1);
                resetFontFamilySet();
            }
        }
    }//GEN-LAST:event_upButtonActionPerformed
    
    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int index = fontFaceList.getSelectedIndex();
        if (index <= 0)
            return;
        fontFamilies.remove(index);
        if (!fontFamilies.isEmpty()) {
            if (index == fontFamilies.getSize())
                index--;
            fontFaceList.setSelectedIndex(index);
        }
    }//GEN-LAST:event_deleteButtonActionPerformed
    
    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        fontFamilies.insertElementAt(" ", 1);
        fontFaceList.setSelectedIndex(1);
    }//GEN-LAST:event_newButtonActionPerformed
    
    private void selectedFontListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_selectedFontListValueChanged
        if (!evt.getValueIsAdjusting()) {
            //selectedFont = (String)selectedFontList.getSelectedValue();
            int index = selectedFontList.getSelectedIndex();
            if(index == 0) {
                upButton.setEnabled(false);
            }else{
                upButton.setEnabled(true);
            }
            if(index == selectedFonts.getSize()-1) {
                downButton.setEnabled(false);
            }else{
                downButton.setEnabled(true);
            }
        }
    }//GEN-LAST:event_selectedFontListValueChanged
    
    private void availableFontListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_availableFontListValueChanged
        if (!evt.getValueIsAdjusting()) {
            //selectedAvailableFont = (String)availableFontList.getSelectedValue();
        }
    }//GEN-LAST:event_availableFontListValueChanged
    
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Object[] selections = selectedFontList.getSelectedValues();
        for(int i=0; i< selections.length ;i++){
            if (selectedFonts.contains(selections[i])){
                selectedFonts.removeElement(selections[i]);
            }
        }
        if(!selectedFonts.isEmpty()) {
            selectedFontList.setSelectedIndex(0);
        }else{
            removeButton.setEnabled(false);
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }
        resetFontFamilySet();
    }//GEN-LAST:event_removeButtonActionPerformed
    
    private void fontFaceListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fontFaceListValueChanged
        if (!evt.getValueIsAdjusting()) {
            currentIndex = fontFaceList.getSelectedIndex();
            selectedFonts.removeAllElements();
            // Do not show the <NOT SET> in the selected fonts
            if (currentIndex > 0) {
                addButton.setEnabled(true);
                deleteButton.setEnabled(true);
                upButton.setEnabled(true);
                downButton.setEnabled(true);
                String selectedFontSet = (String)fontFaceList.getSelectedValue();
                if((selectedFontSet != null) && !selectedFontSet.trim().equals("")) {
                    StringTokenizer st = new StringTokenizer(selectedFontSet,",");
                    while(st.hasMoreTokens()){
                        String fontName = st.nextToken();
                        if(!fontName.trim().equals("")){
                            fontName = fontName.replaceAll("'","");
                            selectedFonts.addElement(fontName);
                        }
                    }
                }
                if (!selectedFonts.isEmpty()) {
                    removeButton.setEnabled(true);
                    selectedFontList.setSelectedIndex(0);
                }
            } else {
                addButton.setEnabled(false);
                removeButton.setEnabled(false);
                deleteButton.setEnabled(false);
                upButton.setEnabled(false);
                downButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_fontFaceListValueChanged
    
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        Object[] selections = availableFontList.getSelectedValues();
        for(int i=0; i< selections.length ;i++){
            if (!selectedFonts.contains(selections[i])){
                selectedFonts.addElement(selections[i]);
                selectedFontList.setSelectedValue(selections[i],true);
            }
        }
        removeButton.setEnabled(true);
        resetFontFamilySet();
    }//GEN-LAST:event_addButtonActionPerformed
    
    private void fontSelectionComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fontSelectionComboItemStateChanged
        fontSelectionCombo.getSelectedIndex();
        switch(fontSelectionCombo.getSelectedIndex()){
            case 0:
                availableFonts = fontModel.getFontList();
                //availableFontList.setModel(fontModel.getFontList());
                break;
            case 1:
                availableFonts = fontModel.getFontFamilyList();
                //availableFontList.setModel(fontModel.getFontFamilyList());
                break;
            case 2:
                availableFonts = fontModel.getWebFontList();
                //availableFontList.setModel(fontModel.getWebFontList());
                break;
        }
        availableFontList.setModel(availableFonts);
        availableFontList.setSelectedIndex(0);
    }//GEN-LAST:event_fontSelectionComboItemStateChanged
    
    private void resetFontFamilySet() {
        StringBuffer fontSetBuf = new StringBuffer();
        for(int i = 0; i < selectedFonts.size(); i++){
            String fontName = ((String) selectedFonts.get(i)).trim();
            // If the font name has spaces then quote it as per CSS spec.
            if (fontName.indexOf(" ") != -1){
                fontName = "\'" + fontName + "\'";
            }
            fontSetBuf.append(fontName);
            if(i < selectedFonts.size()-1 )fontSetBuf.append(",");
        }
        // Otherwise List does not display the empty line
        if(fontSetBuf.length() == 0) fontSetBuf.append(" ");
        fontFamilies.setElementAt(fontSetBuf.toString(), currentIndex);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel addRemoveButtonPanel;
    private javax.swing.JList availableFontList;
    private javax.swing.JScrollPane availableFontScroll;
    private javax.swing.JLabel availableLabel;
    private javax.swing.JLabel currentFontFamiliesLabel;
    private javax.swing.JPanel currentFontFamilyPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton downButton;
    private javax.swing.JList fontFaceList;
    private javax.swing.JPanel fontFamilyEditor;
    private javax.swing.JScrollPane fontFamilyScroll;
    private javax.swing.JComboBox fontSelectionCombo;
    private javax.swing.JButton newButton;
    private javax.swing.JPanel newDeleteButtonPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JList selectedFontList;
    private javax.swing.JScrollPane selectedFontScroll;
    private javax.swing.JLabel selectedLabel;
    private javax.swing.JButton upButton;
    private javax.swing.JPanel upDownPanel;
    // End of variables declaration//GEN-END:variables
    
}
