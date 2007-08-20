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
package org.netbeans.modules.mercurial.ui.properties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;


/**
 *
 * @author Peter Pis
 */
public class PropertiesTableModel extends AbstractTableModel {
    
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_VALUE = "value";
    
    private HgPropertiesNode[] nodes;
    private String[] columns;
    
    private static final Map<String, String[]> columnLabels = new HashMap<String, String[]>(2); 
    
    {
        ResourceBundle loc = NbBundle.getBundle(PropertiesTableModel.class);    
        columnLabels.put(COLUMN_NAME_NAME, new String[] {loc.getString("CTL_PropertiesTable_Column_Name"), loc.getString("CTL_PropertiesTable_Column_Name")});
        columnLabels.put(COLUMN_NAME_VALUE, new String[] {loc.getString("CTL_PropertiesTable_Column_Value"), loc.getString("CTL_PropertiesTable_Column_Value")});
    }
    
    /** Creates a new instance of PropertiesTableModel */
    public PropertiesTableModel(String[] clms) {
        if (Arrays.equals(columns, clms))
            return;
        setColumns(clms);
        setNodes(new HgPropertiesNode[0]);
    }
    
    public void setColumns(String[] clms) {
        this.columns = clms;
        fireTableStructureChanged();
    }
    
    public void setNodes(HgPropertiesNode[] nodes) {
        this.nodes = nodes;
        fireTableDataChanged();
    }
    
    public HgPropertiesNode[] getNodes() {
        return nodes;
    }
    
    public HgPropertiesNode getNode(int row) {
        return nodes[row];
    }
    
    public int getRowCount() {
        return nodes.length;
    }

    public String getColumnName(int column) {
        return columnLabels.get(columns[column])[0];
    }
    
    public int getColumnCount() {
        return columns.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        String clm = columns[columnIndex];
        if (clm.equals(COLUMN_NAME_NAME)) {
            return nodes[rowIndex].getName();
        } else if (clm.equals(COLUMN_NAME_VALUE)) {
            return nodes[rowIndex].getValue();
        }
        throw new IllegalArgumentException("The column index is out of index: " + columnIndex);
    }

    
}
