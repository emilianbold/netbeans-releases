/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.openide.src.ClassElement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author pfiala
 */
public abstract class QueryMethodsTableModel extends InnerTableModel {

    protected final EntityHelper.Queries queries;

    public QueryMethodsTableModel(String[] columnNames, int[] columnWidths, final EntityHelper.Queries queries) {
        super(null, columnNames, columnWidths);
        this.queries = queries;
        queries.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Object source = evt.getSource();
                if (source instanceof Entity) {
                    tableChanged();
                } else if (source instanceof ClassElement) {
                    fireTableDataChanged();
                } else if (source instanceof Query) {
                    for (int i = 0, n = getRowCount(); i < n; i++) {
                        QueryMethodHelper queryMethodHelper = getQueryMethodHelper(i);
                        if (queryMethodHelper.query == source) {
                            fireTableRowsUpdated(i, i);
                            return;
                        }
                    }
                } else {
                    fireTableDataChanged();
                }
            }
        });
    }

    public void removeRow(int row) {
        getQueryMethodHelper(row).removeQuery();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public abstract QueryMethodHelper getQueryMethodHelper(int row);

}
