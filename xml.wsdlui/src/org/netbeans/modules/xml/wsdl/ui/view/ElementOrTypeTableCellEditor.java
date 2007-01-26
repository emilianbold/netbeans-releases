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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ElementOrTypeTableCellEditor.java
 *
 * Created on August 30, 2006, 5:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view;

import java.awt.Component;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.netbeans.api.project.Project;

/**
 *
 * @author radval
 */
class ElementOrTypeTableCellEditor extends AbstractCellEditor
                                                implements TableCellEditor {
        
        private ElementOrTypeChooserEditorPanel mPanel;
        private DefaultCellEditor mEditor = new DefaultCellEditor(new JTextField());
        
        public ElementOrTypeTableCellEditor(JTable partsTable, Map<String, String> namespaceToPrefixMap, Project project) {
            mPanel = new ElementOrTypeChooserEditorPanel(partsTable, namespaceToPrefixMap, project);
            
        }
        
        public Component getTableCellEditorComponent(JTable table, 
                                                     Object value,
                                                      boolean isSelected,
                                                      int row, 
                                                      int column) {
            if(column == 1) {
                mPanel.getJTextField().setText(value.toString());
                
                 return mPanel;                                                      
            }
            
            return mEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        public Object getCellEditorValue() {
            return mPanel.getJTextField().getText();
        }
        

    }
