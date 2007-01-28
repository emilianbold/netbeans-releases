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


package com.sun.data.provider.impl;

import java.util.ArrayList;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FilterCriteria;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableDataFilter;
import com.sun.data.provider.TableDataProvider;

/**
 * <p>This basic implementation of TableDataFilter iterates rows and the
 * contained {@link FilterCriteria} and invokes the <code>match()</code> method
 * on each.  If the <code>matchAllCriteria</code> property is set to
 * <code>true</code>, then all of the filter criteria must report either a
 * positive match for an include criteria, or a negative match for an exclude
 * criteria for a row to be accepted by the filter.  If the
 * <code>matchAllCriteria</code> property is set to <code>false</code> (match
 * any), then any include criteria that matches a row or exclude criteria
 * that does not match a row will cause the row to be accepted by the filter.
 * If there is no filter criteria defined, all rows will be accepted.</p>
 *
 * @author Joe Nuxoll
 */
public class BasicTableDataFilter implements TableDataFilter {

    /**
     * storage for the filter criteria
     */
    protected FilterCriteria[] filterCriteria;

    /** {@inheritDoc} */
    public void setFilterCriteria(FilterCriteria[] filterCriteria) {
        this.filterCriteria = filterCriteria;
    }

    /** {@inheritDoc} */
    public FilterCriteria[] getFilterCriteria() {
        return filterCriteria;
    }

    /**
     * storage for the matchAllCriteria property (default is <code>true</code>)
     */
    protected boolean matchAll = true;

    /**
     * <p>Sets the matchAllCriteria property.  When set to <code>true</code>,
     * the <code>accept(...)</code> method to only accept rows that are
     * matched by all include filter criteria and not mached by all exclude
     * filter criteria.  When set to <code>false</code>, the
     * <code>accept(...)</code> method will accept an row if it is matched
     * by any include filter criteria or not matched by any exclude filter
     * criteria.</p>
     *
     * @param matchAll The desired setting for the matchAllCriteria property
     */
    public void setMatchAllCriteria(boolean matchAll) {
        this.matchAll = matchAll;
    }

    /**
     * <p>Gets the matchAllCriteria property.  When set to <code>true</code>,
     * the <code>accept(...)</code> method to only accept rows that are
     * matched by all include filter criteria and not mached by all exclude
     * filter criteria.  When set to <code>false</code>, the
     * <code>accept(...)</code> method will accept an row if it is matched
     * by any include filter criteria or not matched by any exclude filter
     * criteria.</p>
     *
     * @return The current setting for the matchAllCriteria property
     */
    public boolean isMatchAllCriteria() {
        return matchAll;
    }

    /**
     * <p>This method iterates the passed rows and contained
     * {@link FilterCriteria} and invokes the <code>match()</code> method on
     * each.  If the <code>matchAllCriteria</code> property is set to
     * <code>true</code>, then all of the include filter criteria must match a
     * row, and all of the exclude filter criteria must not match a row for that
     * row to be accepted by the filter.  If the <code>matchAllCriteria</code>
     * property is set to <code>false</code> (match any), then any include
     * criteria that matches a row or exclude criteria that does not match a row
     * will cause that row to be excluded.  If there is no filter criteria
     * defined, then this method returns the passed-in rows, effectively
     * accepting all rows.</p>
     *
     * {@inheritDoc}
     */
    public RowKey[] filter(TableDataProvider provider, RowKey[] rows)
        throws DataProviderException {

        if (filterCriteria == null || filterCriteria.length == 0) {
            return rows;
        }
        ArrayList acceptRows = new ArrayList();
        for (int r = 0; rows != null && r < rows.length; r++) {
            RowKey row = rows[r];
            int acceptCount = 0;
            boolean handled = false;
            for (int i = 0; i < filterCriteria.length; i++) {
                boolean match = filterCriteria[i].match(provider, row);
                boolean accept = filterCriteria[i].isInclude() ? match : !match;
                if (!matchAll && accept) {
                    acceptRows.add(row);
                    handled = true;
                    break; // out of filterCriteria for loop
                }
                if (matchAll && !accept) {
                    handled = true;
                    break; // out of filterCriteria for loop
                }
                if (accept) {
                    acceptCount++;
                }
            }
            if (!handled) {
                if (matchAll && filterCriteria.length == acceptCount) {
                    acceptRows.add(row);
                }
                else if (!matchAll && acceptCount > 0) {
                    acceptRows.add(row);
                }
            }
        }
        return (RowKey[])acceptRows.toArray(new RowKey[acceptRows.size()]);
    }
}
