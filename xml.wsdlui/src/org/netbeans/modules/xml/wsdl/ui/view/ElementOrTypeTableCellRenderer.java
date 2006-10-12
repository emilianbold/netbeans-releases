/*
 * ElementOrTypeTableCellRenderer.java
 *
 * Created on August 30, 2006, 5:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author radval
 */
public class ElementOrTypeTableCellRenderer extends DefaultTableCellRenderer {
        private ElementOrTypeChooserRendererPanel mPanel = new ElementOrTypeChooserRendererPanel();                                       
        
        public Component getTableCellRendererComponent(JTable table, 
                                                       Object value,
                                                       boolean isSelected, 
                                                       boolean hasFocus, 
                                                       int row, 
                                                       int column) {
            
                boolean isEnabled = table == null || table.isEnabled();
                if(column == 1) {                                       
                    JLabel label = (JLabel) mPanel.getDefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    mPanel.setNewLabel(label);
                    mPanel.setEnabled(isEnabled);
                    return mPanel;
                } 
                
                
                Component comp =  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                comp.setEnabled(isEnabled);
                return comp;
        }
    }
    
    
