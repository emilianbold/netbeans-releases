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