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
package org.netbeans.modules.autoupdate.ui;

import java.awt.Component;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import org.openide.util.NbPreferences;

/**
 *
 * @author Radek Matous
 */
public class SortColumnHeaderRenderer implements TableCellRenderer{
    private UnitCategoryTableModel model;
    private TableCellRenderer textRenderer;
    private String sortColumn;
    private ImageIcon sortDescIcon;
    private static ImageIcon sortAscIcon;    
    private boolean sortAscending;
    
    public SortColumnHeaderRenderer (UnitCategoryTableModel model, TableCellRenderer textRenderer) {
        this.model = model;
        this.textRenderer = textRenderer;
        sortColumn = getPreferences().get(keyForType("SortingColumn"), getDefaultColumnSelected());// NOI18N
        sortAscending = getPreferences().getBoolean(keyForType("SortAscending"), true);// NOI18N
        this.model.sort (sortColumn, sortAscending);
    }
        
    public Component getTableCellRendererComponent (JTable table, Object value,
            boolean isSelected,
            boolean hasFocus, int row,
            int column) {
        Component text = textRenderer.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
        if( text instanceof JLabel ) {
            JLabel label = (JLabel)text;
            if (table.getColumnModel ().getColumn (column).getIdentifier ().equals (sortColumn)) {
                label.setIcon ( sortAscending ? getSortAscIcon () : getSortDescIcon ());
                label.setHorizontalTextPosition ( SwingConstants.LEFT );
            } else {
                label.setIcon ( null);
            }
        }
        return text;
    }
    
    public void setDefaultSorting () {
        setSorting(getDefaultColumnSelected());
    }
    
    private String getDefaultColumnSelected() {
        String retval = null;
        retval = this.model.getColumnName(2); // category
        return retval;
    }
        
    public void setSorting (Object column) {
        if (!column.equals (sortColumn)) {
            sortColumn = (String)column;
            sortAscending = true;
        } else {
            sortAscending = !sortAscending;
        }
        getPreferences().put(keyForType("SortingColumn"), sortColumn);// NOI18N
        getPreferences().putBoolean(keyForType("SortAscending"), sortAscending);// NOI18N
        this.model.sort (column, sortAscending);
    }
        
    private ImageIcon getSortAscIcon () {
        if (sortAscIcon == null) {
            sortAscIcon = new ImageIcon (org.openide.util.Utilities.loadImage (
                    "org/netbeans/modules/autoupdate/ui/resources/columnsSortedDesc.gif")); // NOI18N
        }
        return sortAscIcon;
    }
    
    private ImageIcon getSortDescIcon () {
        if (sortDescIcon == null) {
            sortDescIcon = new ImageIcon (org.openide.util.Utilities.loadImage (
                    "org/netbeans/modules/autoupdate/ui/resources/columnsSortedAsc.gif")); // NOI18N
        }
        return sortDescIcon;
    }
    
    private String keyForType(String key) {
        return key  + model.getType();
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(SortColumnHeaderRenderer.class);
    }    
}
