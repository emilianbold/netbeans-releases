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

/**
 * <p>The TableDataFilter interface performs a filtering operation on a
 * {@link TableDataProvider}.  If a component is using an TableDataFilter, it
 * will invoke the <code>filter(...)</code> method to determine which rows
 * should be displayed in the visual (or non-visual) component.</p>
 *
 * <p>NOTE: If a component is bound to a {@link TableDataProvider} and a
 * TableDataFilter which are the *same* instance, the <code>filter(...)</code>
 * method is never called.  The filter is assumed to be intrinsic in the row
 * data of the {@link TableDataProvider}, based on the currently set filter
 * criteria.</p>
 *
 * <p>Typically, an TableDataFilter implementation will iterate the contained
 * set of FilterCriteria objects and call the <code>match(...)</code> method on
 * each for each row of data in the TableDataProvider.  The combination of
 * include and/or exclude match criteria then determines wether a row is
 * included or excluded from the filter.</p>
 *
 * @see TableDataProvider
 * @see FilterCriteria
 *
 * @author Joe Nuxoll
 */
public interface TableDataFilter {

    /**
     * <p>Sets the filter criteria on this data filter.</p>
     *
     * @param filterCriteria An array of {@link FilterCriteria} objects defining
     *        the filter order on this TableDataFilter
     */
    public void setFilterCriteria(FilterCriteria[] filterCriteria);

    /**
     * <p>Returns the current filter criteria on this data filterer.</p>
     *
     * @return FilterCriteria[] defining the filter order on this TableDataFilter
     */
    public FilterCriteria[] getFilterCriteria();

    /**
     * <p>Performs a filter operation on the passed set of {@link RowKey}
     * objects.  Typically, the contained FilterCriteria objects will be
     * iterated for each row and their <code>match(...)</code> method will be
     * called in conjunction with their <code>isInclude()</code> method to
     * determine if a row should be displayed or not.</p>
     *
     * <p>NOTE: If a component is bound to an {@link TableDataProvider} and
     * an TableDataFilter, which are the *same* instance, this method is never
     * called.  The filter is assumed to be intrinsic in the row data of the
     * {@link TableDataProvider}, based on the currently set filter criteria.
     * </p>
     *
     * @param provider {@link TableDataProvider} containing the data on display
     * @param rows An array of {@link RowKey} to be filtered.  If this is
     *        <code>null</code>, then the return value should be
     *        <code>null</code> or an empty array of RowKeys.
     * @return An array of {@link RowKey} representing the result of the filter
     *         operation
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning null.  A DPE may also indicate that the
     *         passed TableDataProvider or RowKeys are not valid.  Consult the
     *         documentation of the specific TableDataFilter implementation for
     *         details on what exceptions might be wrapped by a DPE.
     */
    public RowKey[] filter(TableDataProvider provider, RowKey[] rows)
        throws DataProviderException;
}
