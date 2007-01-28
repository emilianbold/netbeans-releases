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

import java.util.Locale;

/**
 * <p>The TableDataSorter interface defines a sort criteria and mechanism for
 * sorting the contents of a {@link TableDataProvider}.  The sort criteria
 * is stored in an array of {@link SortCriteria} objects, and the sort operation
 * is invoked via the {@link #sort(TableDataProvider,RowKey[])} method.
 * </p>
 *
 * <p>NOTE: If a component is bound to a {@link TableDataProvider} and a
 * TableDataSorter which are the *same* instance, the <code>sort(...)</code>
 * method is never called.  The sort order is assumed to be intrinsic in the
 * row order of the {@link TableDataProvider}, based on the currently set sort
 * criteria.</p>
 *
 * @see TableDataProvider
 * @see SortCriteria
 *
 * @author Joe Nuxoll
 */
public interface TableDataSorter {

    /**
     * <p>Sets the sort criteria on this data sorter.</p>
     *
     * @param sortCriteria An array of {@link SortCriteria} objects defining
     *        the sort order on this TableDataSorter
     */
    public void setSortCriteria(SortCriteria[] sortCriteria);

    /**
     * <p>Returns the current sort criteria on this data sorter.</p>
     *
     * @return SortCriteria[] defining the sort order on this TableDataSorter
     */
    public SortCriteria[] getSortCriteria();

    /**
     * <p>Sets the sort locale to use when comparing objects.  If none is set,
     * the default locale should be used via <code>Locale.getDefault()</code>.
     * </p>
     *
     * @param locale The desired Locale to use for sort comparisons
     */
    public void setSortLocale(Locale locale);

    /**
     * <p>Gets the sort locale used when comparing objects.  If none is set,
     * the default locale should be used via <code>Locale.getDefault()</code>.
     * </p>
     *
     * @return The Locale used for comparing objects, or null if the default
     *         Locale is used.
     */
    public Locale getSortLocale();

    /**
     * <p>Sorts the rows from the specified {@link TableDataProvider} based on
     * the current sort criteria, and returns an array of {@link RowKey}s
     * representing the sorted row order.  Any excluded rows from the return
     * value of <code>sort(...)</code> should be considered unsortable, and thus
     * should be displayed at the end of whatever UI is presenting these items.
     * </p>
     *
     * <p>NOTE: If a component is bound to a {@link TableDataProvider} and
     * an TableDataSorter, which are the *same* instance, this method is never
     * called.  The sort order is assumed to be intrinsic in the row key order
     * of the {@link TableDataProvider}, based on the currently set sort
     * criteria.</p>
     *
     * @param provider The {@link TableDataProvider} to sort
     * @param rows The array of {@link RowKey}s to sort.  If this is
     *        <code>null</code> then the return value should be
     *        <code>null</code> or an empty array of RowKeys.
     * @return An array of {@link RowKey}s representing the sorted rows from
     *         the specified {@link TableDataProvider}.  Any excluded rows
     *         from the return value of this method should be considered
     *         unsortable, and thus should be displayed at the end of whatever
     *         UI is presenting these items.  <code>null</code> may be returned
     *         to indicate that sorting is not possible.
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning null or an empty array.  A DPE may also
     *         indicate that the passed TableDataProvider or RowKeys are not
     *         valid.  Consult the documentation of the specific TableDataSorter
     *         implementation for details on what exceptions might be wrapped by
     *         a DPE.
     */
    public RowKey[] sort(TableDataProvider provider, RowKey[] rows)
        throws DataProviderException;
}
