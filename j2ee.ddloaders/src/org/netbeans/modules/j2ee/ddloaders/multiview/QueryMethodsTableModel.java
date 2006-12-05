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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;

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
