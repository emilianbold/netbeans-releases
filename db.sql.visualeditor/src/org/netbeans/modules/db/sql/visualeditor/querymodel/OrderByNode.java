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
package org.netbeans.modules.db.sql.visualeditor.querymodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

/**
 * Represents a SQL ORDER BY clause
 */
public class OrderByNode implements OrderBy {

    // Fields

    // A vector of generalized column objects (JoinTables)

    ArrayList _sortSpecificationList;


    // Constructors

    public OrderByNode() {
    }

    public OrderByNode(ArrayList sortSpecificationList) {
        _sortSpecificationList = sortSpecificationList;
    }


    // Methods

    // Return the SQL string that corresponds to this From clause
    public String genText() {
        String res = "";    // NOI18N
        if (_sortSpecificationList != null && _sortSpecificationList.size() > 0) {

            res = "\nORDER BY " + ((SortSpecification)_sortSpecificationList.get(0)).genText();  // NOI18N

            for (int i=1; i<_sortSpecificationList.size(); i++) {
                res += ", " + "\n                    " +    // NOI18N
                  ((SortSpecification)_sortSpecificationList.get(i)).genText();
            }
        }

        return res;
    }



    // Methods

    // Accessors/Mutators

    void renameTableSpec(String oldTableSpec, String corrName) {
        if (_sortSpecificationList != null) {
            for (int i=0; i<_sortSpecificationList.size(); i++)
                ((SortSpecification)_sortSpecificationList.get(i)).renameTableSpec(oldTableSpec, corrName);
        }
    }

    public void removeSortSpecification(String tableSpec) {
        if (_sortSpecificationList != null) {
            for (int i=0; i<_sortSpecificationList.size(); i++) {
                ColumnNode col = (ColumnNode)((SortSpecification)_sortSpecificationList.get(i)).getColumn();
                if (col.getTableSpec().equals(tableSpec))
                {
                    _sortSpecificationList.remove(i);
                    // item from arraylist is removed, reset index value
                    // as remove shifts any subsequent elements to the left
                    // (subtracts one from their indices).
                    i=i-1;
                }
            }
        }
    }

    public void removeSortSpecification(String tableSpec, String columnName) {
        if (_sortSpecificationList != null) {
            for (int i=0; i<_sortSpecificationList.size(); i++) {
                ColumnNode col = (ColumnNode)((SortSpecification)_sortSpecificationList.get(i)).getColumn();
                if (col.matches(tableSpec, columnName))
                {
                    _sortSpecificationList.remove(i);
                    // item from arraylist is removed, reset index value
                    // as remove shifts any subsequent elements to the left
                    // (subtracts one from their indices).
                    i=i-1;
                }
            }
        }
    }

    public void addSortSpecification(String tableSpec, String columnName, String direction, int order) {
        SortSpecification sortSpec = new SortSpecification(new ColumnNode(tableSpec, columnName), direction);
        // Insert the new one in an appropriate place
        if (_sortSpecificationList == null)
            _sortSpecificationList = new ArrayList();
        _sortSpecificationList.add(order-1, sortSpec);
    }

    public int getSortSpecificationCount() {
        return (_sortSpecificationList != null) ? _sortSpecificationList.size() : 0;
    }

    public SortSpecification getSortSpecification(int i) {
        return (_sortSpecificationList != null) ? ((SortSpecification)_sortSpecificationList.get(i)) : null;
    }

    public void  getReferencedColumns (Collection columns) {
        if (_sortSpecificationList != null) {
            for (int i = 0; i < _sortSpecificationList.size(); i++)
                ((SortSpecification)_sortSpecificationList.get(i)).getReferencedColumns(columns);
        }
    }

    public void getQueryItems(Collection items) {
        if (_sortSpecificationList != null) 
            items.addAll(_sortSpecificationList);
    }
}
