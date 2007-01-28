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

import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FilterCriteria;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableDataProvider;

/**
 * <p>The NestedFilterCriteria class defines a set of nested filter criteria
 * for a {@link com.sun.data.provider.TableDataFilter}.</p>
 */
public class NestedFilterCriteria extends FilterCriteria {

    /**
     *
     */
    public NestedFilterCriteria() {}

    /**
     * @param displayName The desired display name for this nested filter criteria
     */
    public NestedFilterCriteria(String displayName) {
        super(displayName);
    }

    /**
     * @param displayName The desired display name for this filter criteria
     * @param include <code>true</code> matches for this criteria, or
     *        exclude <code>false</code> matches for this criteria.
     */
    public NestedFilterCriteria(String displayName, boolean include) {
        super(displayName, include);
    }

    /**
     * @param displayName The desired display name for this filter criteria
     * @param include <code>true</code> matches for this criteria, or
     *        exclude <code>false</code> matches for this criteria.
     * @param nested An array of nested filter criteria
     */
    public NestedFilterCriteria(String displayName, boolean include, FilterCriteria[] nested) {
        super(displayName, include);
        this.filterCriteria = nested;
    }

    /**
     * storage for the nested filter criteria
     */
    protected FilterCriteria[] filterCriteria;

    /**
     * @param filterCriteria FilterCriteria[]
     */
    public void setFilterCriteria(FilterCriteria[] filterCriteria) {
        this.filterCriteria = filterCriteria;
    }

    /**
     * @return FilterCriteria[]
     */
    public FilterCriteria[] getFilterCriteria() {
        return filterCriteria;
    }

    /**
     * storage for the matchAllCriteria property (default is <code>true</code>)
     */
    protected boolean matchAll = true;

    /**
     * <p>Sets the matchAllCriteria property.  When set to <code>true</code>,
     * the <code>match(...)</code> method will only match rows that are
     * matched by all nested include filter criteria and not mached by all
     * nested exclude filter criteria.  When set to <code>false</code>, the
     * <code>match(...)</code> method will match a row if it is matched
     * by any nested include filter criteria or not matched by any nested
     * exclude filter criteria.</p>
     *
     * @param matchAll The desired setting for the matchAllCriteria property
     */
    public void setMatchAllCriteria(boolean matchAll) {
        this.matchAll = matchAll;
    }

    /**
     * <p>Gets the matchAllCriteria property.  When set to <code>true</code>,
     * the <code>match(...)</code> method will only match rows that are
     * matched by all nested include filter criteria and not mached by all
     * nested exclude filter criteria.  When set to <code>false</code>, the
     * <code>match(...)</code> method will match a row if it is matched
     * by any nested include filter criteria or not matched by any nested
     * exclude filter criteria.</p>
     *
     * @return The current setting for the matchAllCriteria property
     */
    public boolean isMatchAllCriteria() {
        return matchAll;
    }

    /**
     * <p>This method iterates the nested
     * {@link com.sun.data.provider.FilterCriteria} and invokes the
     * <code>match()</code> method on each.  If the
     * <code>matchAllCriteria</code> property is set to <code>true</code>, then
     * all of the nested include filter criteria must match a row, and all of
     * the nested exclude filter criteria must not match a row for this method
     * to return <code>true</code>.  If the <code>matchAllCriteria</code>
     * property is set to <code>false</code> (match any), then any nested
     * include criteria that matches a row or nested exclude criteria that does
     * not match a row will cause this method to return <code>true</code>.  If
     * there is no nested filter criteria defined, then this method returns
     * <code>true</code>.</p>
     *
     * {@inheritDoc}
     */
    public boolean match(TableDataProvider provider, RowKey row)
        throws DataProviderException {

        if (filterCriteria == null || filterCriteria.length == 0) {
            return true;
        }
        boolean anyAccepted = false;
        for (int i = 0; i < filterCriteria.length; i++) {
            boolean match = filterCriteria[i].match(provider, row);
            boolean accept = filterCriteria[i].isInclude() ? match : !match;
            if (accept) {
                anyAccepted = true;
            }
            if (matchAll && !accept) {
                return false;
            }
            if (!matchAll && accept) {
                return true;
            }
        }
        return anyAccepted;
    }
}
