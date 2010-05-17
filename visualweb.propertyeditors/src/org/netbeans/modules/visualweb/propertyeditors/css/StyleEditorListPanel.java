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
