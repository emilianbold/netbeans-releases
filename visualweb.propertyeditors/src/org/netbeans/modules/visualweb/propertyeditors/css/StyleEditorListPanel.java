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

import org.netbeans.modules.visualweb.propertyeditors.css.model.CssStyleData;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.LineBorder;

/**
 * This is the left hand side Editor list panel
 * @author  Winston Prakash
 */
public class StyleEditorListPanel extends javax.swing.JPanel {
    StyleBuilderPanel styleBuilderPanel = null;
    CssStyleData cssStyleData = null;
    
    List editorList = new ArrayList();
    
    /** Creates new form EditorListPanel */
    public StyleEditorListPanel(StyleBuilderPanel mainPanel, CssStyleData styleData) {
        cssStyleData = styleData;
        styleBuilderPanel = mainPanel;
        initComponents();
        // Set the background of the List to panel background
        // (light grey) instead of white.
        styleEditorList.setBackground(getBackground());
        styleEditorList.setModel(new StyleEditorListModel());
        styleEditorList.setCellRenderer(new StyleEditorListRenderer());
    }
    
    public void addEditor(StyleEditor editor){
        editorList.add(editor);
    }
    
    public void setSelectedEditor(StyleEditor editor){
        int index = editorList.indexOf(editor);
        if( index >= 0){
            styleEditorList.setSelectedIndex(index);
        }
    }
    
    public Dimension getPreferredSize(){
         Dimension dim = super.getPreferredSize();
         dim.setSize(dim.getWidth()+20,dim.getHeight());
         return dim;
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        styleEditorList = new javax.swing.JList();

        setLayout(new java.awt.BorderLayout());

        setMinimumSize(new java.awt.Dimension(80, 200));
        setOpaque(false);
        styleEditorList.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        styleEditorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        styleEditorList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                styleEditorListValueChanged(evt);
            }
        });

        add(styleEditorList, java.awt.BorderLayout.NORTH);
        styleEditorList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(StyleEditorListPanel.class, "STYLE_EDITOR_LIST_ACCESSIBLE_NAME"));
        styleEditorList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(StyleEditorListPanel.class, "STYLE_EDITOR_LIST_ACCESSIBLE_DESC"));

    }// </editor-fold>//GEN-END:initComponents
    
    private void styleEditorListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_styleEditorListValueChanged
        if (evt.getValueIsAdjusting()) return;
        JList list = (JList) evt.getSource();
        StyleEditor styleEditor = (StyleEditor) list.getSelectedValue();
        styleBuilderPanel.setEditorPanel((JPanel)styleEditor);
    }//GEN-LAST:event_styleEditorListValueChanged
    
    class StyleEditorListModel extends AbstractListModel{
        
        public int getSize() {
            return editorList.size();
        }
        
        public Object getElementAt(int i) {
            return editorList.get(i);
        }
    }
    
    class StyleEditorListRenderer extends DefaultListCellRenderer{
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                Color lineColor = list.getSelectionBackground().darker();
                setBorder(new LineBorder(lineColor, 1, true));
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                setBorder(null);
            }
            StyleEditor styleEditor = (StyleEditor) value;
            setText(styleEditor.getDisplayName());
            return this;
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList styleEditorList;
    // End of variables declaration//GEN-END:variables
    
}
