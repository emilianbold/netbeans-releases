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

package org.netbeans.modules.iep.editor.ps;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * SelectPanelTableCellRenderer.java
 *Created on November 22, 2006, 11:44 AM
 *
 * @author Rahul Dwivedi
 */
public class SelectPanelTableCellRenderer extends DefaultTableCellRenderer{
    
    
    public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
            
           super.getTableCellRendererComponent(table,value,isSelected,hasFocus,rowIndex,colIndex);
//            if (isSelected) {
//                
//            }
//    
//            if (hasFocus) {
//                super.setForeground(table.getForeground());
//                super.setBackground(table.getBackground());
//            }
            
            boolean editable = table.getModel().isCellEditable(rowIndex, colIndex);
            super.setEnabled(editable);
            return  this;
        }
   
    
}
