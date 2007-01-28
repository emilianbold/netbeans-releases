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

package com.sun.data.provider;

import java.io.Serializable;

/**
 * <p>The FilterCriteria class defines a single filter criteria for a {@link
 * TableDataFilter}.  This includes a boolean to indicate an include
 * (<code>true</code>) or exclude (<code>false</code>) filter critiera.
 * An array of these FilterCriteria objects are used to define the display
 * filter for an {@link TableDataProvider}.</p>
 */
public abstract class FilterCriteria implements Serializable {

    /**
     * <p>Constructs a new FilterCriteria object with no display name and the
     * default state for include (<code>true</code>).</p>
     */
    public FilterCriteria() {}

    /**
     * <p>Constructs a new FilterCriteria object with the specified display name
     * and the default state for the include/exclude (<code>true == </code>
     * include).</p>
     *
     * @param displayName The desired display name for this filter criteria
     */
    public FilterCriteria(String displayName) {
        this.displayName = displayName;
    }

    /**
     * <p>Constructs a new FilterCriteria object with the specified display name
     * and state for include/exclude (<code>true</code> == include).</p>
     *
     * @param displayName The desired display name for this filter criteria
     * @param include <code>true</code> matches for this criteria, or
     *        exclude <code>false</code> matches for this criteria.
     */
    public FilterCriteria(String displayName, boolean include) {
        this.displayName = displayName;
        this.include = include;
    }

    /**
     * <p>Sets the display name for this filter criteria.</p>
     *
     * @param displayName The desired display name for this filter criteria
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * <p>Returns the display name for this filter criteria.</p>
     *
     * @return The display name of this filter criteria
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * <p>Sets the include/exclude setting for this filter criteria.
     * <code>true</code> represents include (include filter matches in the
     * row list), and <code>false</code> represents exclude (exclude filter
     * matches in the row list).</p>
     *
     * @param include <code>true</code> matches for this criteria, or exclude
     *        <code>false</code> matches for this criteria.
     */
    public void setInclude(boolean include) {
        this.include = include;
    }

    /**
     * <p>Returns the include/exclude setting for this FilterCriteria.
     * <code>true</code> represents include (include filter matches in the
     * row list), and a <code>false</code> represents exclude (exclude
     * filter matches in the row list).</p>
     *
     * @return <code>true</code> represents include (include filter matches
     *         in the row list), and a <code>false</code> represents exclude
     *         (exclude filter matches in the row list).
     */
    public boolean isInclude() {
        return include;
    }

    /**
     * <p>Determines wether or not a particular row matches this filter
     * criteria.  Implementations may perform whatever logic is desired to
     * make this determination.  The combination of a positive match and
     * the state of the include property determines if a row is included or
     * excluded from the filter.</p>
     *
     * @param provider {@link TableDataProvider} containing the data on
     *        display
     * @param rowKey The {@link RowKey} of the data to be checked for a match
     * @return <code>true</code> if a match was detected, or
     *         <code>false</code> if not.
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning null.  A DPE may also indicate that the
     *         passed TableDataProvider or RowKey is not valid.  Consult the
     *         documentation of the specific FilterCriteria implementation for
     *         details on what exceptions might be wrapped by a DPE.
     */
    abstract public boolean match(TableDataProvider provider, RowKey rowKey)
        throws DataProviderException;

    private String displayName;
    private boolean include = true;
}
