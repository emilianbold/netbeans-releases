/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package com.sun.data.provider;

import java.io.Serializable;

/**
 * <p>The abstract SortCriteria class defines a single sort criteria for a
 * {@link TableDataFilter}.  This includes a boolean to indicate an ascending
 * (<code>true</code>) or descending (<code>false</code>) sort critiera.
 * Subclasses override the {@link #getSortValue(TableDataProvider, RowKey)}
 * method to provide a sortable value for the particular row in the table.
 * An array of these SortCriteria objects are used to define the sort order
 * for a {@link TableDataProvider}.</p>
 */
public abstract class SortCriteria implements Serializable {

    /**
     * <p>Constructs a new SortCriteria object with no display name and the
     * default state for include (<code>true</code>).</p>
     */
    public SortCriteria() {}

    /**
     * <p>Constructs a new SortCriteria object with the specified display name
     * and the default state for the include/exclude (<code>true == </code>
     * include).</p>
     *
     * @param displayName The desired display name for this filter criteria
     */
    public SortCriteria(String displayName) {
        this.displayName = displayName;
    }

    /**
     * <p>Constructs a new SortCriteria object with the specified display name
     * and state for include/exclude (<code>true</code> == include).</p>
     *
     * @param displayName The desired display name for this filter criteria
     * @param ascending <code>true</code> for ascending sort, or
     *        <code>false</code> for descending.
     */
    public SortCriteria(String displayName, boolean ascending) {
        this.displayName = displayName;
        this.ascending = ascending;
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
     * <p>Returns the display name for this sort criteria.</p>
     *
     * @return The display name of this sort criteria
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the ascending sort state for this SortCriteria
     *
     * @param ascending <code>true</code> for ascending, <code>false</code>
     *        for desscending
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Returns the ascending sort state for this SortCriteria
     *
     * @return <code>true</code> for ascending, <code>false</code> for
     *         descending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Returns a String uniquely identifying this sort criteria.  This is used
     * to match up a sort criteria object with a user gesture in UI.
     *
     * @return A String that uniquely identifies this sort criteria
     */
    abstract public String getCriteriaKey();

    /**
     * <p>Provides the data value to use while sorting a particular row.
     * Implementations may perform whatever logic is desired to provide the
     * data object to represent this row in a sort.</p>
     *
     * @param provider {@link TableDataProvider} containing the data on display
     * @param rowKey The {@link RowKey} of the row to be sorted
     * @return the data object representing this row for this sort criteria
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning null.  A DPE may also indicate that the
     *         passed TableDataProvider or RowKey is not valid.  Consult the
     *         documentation of the specific SortCriteria implementation for
     *         details on what exceptions might be wrapped by a DPE.
     */
    abstract public Object getSortValue(TableDataProvider provider, RowKey rowKey)
        throws DataProviderException;

    private String displayName;
    private boolean ascending = true;
}
