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
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.autoupdate.ui;

import java.awt.Component;
import java.util.Comparator;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Radek Matous
 */
public class SortColumnHeaderRenderer implements TableCellRenderer{
    private UnitCategoryTableModel model;
    private TableCellRenderer textRenderer;
    private Object sortColumn = null;
    private static ImageIcon SORT_DESC_ICON =
        new ImageIcon(org.openide.util.Utilities.loadImage(
        "org/netbeans/modules/autoupdate/ui/columnsSortedDesc.gif")); // NOI18N
    private static ImageIcon SORT_ASC_ICON = 
        new ImageIcon(org.openide.util.Utilities.loadImage(
        "org/netbeans/modules/autoupdate/ui/columnsSortedAsc.gif")); // NOI18N
    
    private boolean sortAscending = false;
    public SortColumnHeaderRenderer(UnitCategoryTableModel model, TableCellRenderer textRenderer) {
        this.model = model;
        this.textRenderer = textRenderer;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus, int row,
                                                   int column) {
        Component text = textRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if( text instanceof JLabel ) {
            JLabel label = (JLabel)text;
            if (table.getColumnModel().getColumn(column).getIdentifier().equals(sortColumn)) {
                label.setIcon( sortAscending ? SORT_ASC_ICON : SORT_DESC_ICON);
                label.setHorizontalTextPosition( SwingConstants.LEFT );
            } else {
                label.setIcon( null);
            }
        }        
        return text;
    }
    
    public void columnSelected(Object column) {
        if (!column.equals(sortColumn)) {
            sortColumn = column;
            sortAscending = true;
        } else {
            sortAscending = !sortAscending;
            if (sortAscending) {
                sortColumn = null;
                this.model.sort(null, sortAscending);
                return;
            }
        }
        this.model.sort(column, sortAscending);
    }
}
