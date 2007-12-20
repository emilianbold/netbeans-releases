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
    
    
