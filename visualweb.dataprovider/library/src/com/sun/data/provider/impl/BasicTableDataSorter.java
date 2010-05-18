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

package com.sun.data.provider.impl;

import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.SortCriteria;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.TableDataSorter;

/**
 * The BasicTableDataSorter utilizes the {@link Comparable} interface on
 * data objects in the specified {@link TableDataProvider} to provide a
 * sorted version of the data based on the specified sort criteria.
 *
 * @author Joe Nuxoll, Dan Labracque
 */
public class BasicTableDataSorter implements TableDataSorter {

    /**
     * Constructs a BasicTableDataSorter with no sort criteria or locale
     * setting.
     */
    public BasicTableDataSorter() {}

    /**
     * Constructs a BasicTableDataSorter with the specified initial sort
     * criteria and no locale setting.
     *
     * @param sortCriteria The desired initial sort criteria
     */
    public BasicTableDataSorter(SortCriteria[] sortCriteria) {
        this.sortCriteria = sortCriteria;
    }

    /**
     * Constructs a BasicTableDataSorter with the specified locale with no
     * initial sort criteria.
     *
     * @param sortLocale The desired sort locale
     */
    public BasicTableDataSorter(Locale sortLocale) {
        this.sortLocale = sortLocale;
    }

    /**
     * Constructs a BasicTableDataSorter with the specified initial sort
     * criteria and sort locale.
     *
     * @param sortCriteria The desired initial sort criteria
     * @param sortLocale The desired sort locale
     */
    public BasicTableDataSorter(SortCriteria[] sortCriteria, Locale sortLocale) {
        this.sortCriteria = sortCriteria;
        this.sortLocale = sortLocale;
    }

    /**
     * Storage for the sort criteria
     */
    protected SortCriteria[] sortCriteria;

    /**
     * Storage for the sort locale
     */
    protected Locale sortLocale;

    /** {@inheritDoc} */
    public void setSortCriteria(SortCriteria[] sortCriteria) {
        this.sortCriteria = sortCriteria;
    }

    /** {@inheritDoc} */
    public SortCriteria[] getSortCriteria() {
        return sortCriteria;
    }

    /** {@inheritDoc} */
    public void setSortLocale(Locale sortLocale) {
        this.sortLocale = sortLocale;
    }

    /** {@inheritDoc} */
    public Locale getSortLocale() {
        return sortLocale;
    }

    /**
     * <p>Get an array containing an row of sorted rows. This method does not
     * modify the model, but creates an rowed array of sorted rows. The
     * returned array is typically used to row through model rows to obtain
     * the next sorted object.</p>
     *
     * <p>The sorting algorithms selected for this method are based on the
     * merge sort and radix sort. The radix sort, always sorts on the least
     * significant object. For example, we could sort information three times
     * with a stable sort: first on day, next on month, and finally on year.
     * The analysis of the running time depends on the stable sort used as the
     * intermediate sorting algorithm. If there are m passes, and m is
     * constant, the radix sort runs in linear time. The total time for the
     * merge sort algorithm is O(n log n) in the worst case.</p>
     *
     * {@inheritDoc}
     */
    public RowKey[] sort(TableDataProvider provider, RowKey[] rows)
        throws DataProviderException {

        if (rows == null || rows.length == 0) {
            return RowKey.EMPTY_ARRAY;
        }

        // Initialize an array to contain the row of sorted rows.
        int[] sortIndex = new int[rows.length];
        for (int i = 0; i < rows.length; i++) {
            sortIndex[i] = i;
        }

        if (sortCriteria != null) {
            for (int i = sortCriteria.length - 1; i >= 0; i--) {
                mergeSort(sortIndex, 0, sortIndex.length - 1, sortCriteria[i], provider, rows);
            }
        }

        RowKey[] sortedRows = new RowKey[sortIndex.length];
        for (int i = 0; i < sortedRows.length; i++) {
            sortedRows[i] = rows[sortIndex[i]];
        }

        return sortedRows;
    }

    // ---------------------------------------------------- Private Sort Methods

    /*
     * The mergesort algorithm execution is illustrated as:
     *
     * 1. Partition the input sequence into two halves.
     * 2. Sort the two subsequences using the same algorithm.
     * 3. Merge the two sorted subsequences.
     *
     * The merge operation employed in step (3) combines two sorted
     * subsequences to produce a single sorted array.
     */
    private void mergeSort(int a[], int first, int last,
        SortCriteria sc, TableDataProvider tdp, RowKey[] rows)
        throws DataProviderException {

        if (first < last) {
            int mid = (first + last) / 2;               // Index of midpoint
            mergeSort(a, first, mid, sc, tdp, rows);    // Sort left half
            mergeSort(a, mid + 1, last, sc, tdp, rows); // Sort right half
            merge(a, first, mid, last, sc, tdp, rows);  // Merge both halves
        }
    }

    /*
     * Merge 2 sorted array segments a[first...mid] and
     * a[mid+1...last] into one sorted array.
     */
    private void merge(int a[], int first, int mid, int last,
        SortCriteria sc, TableDataProvider tdp, RowKey[] rows)
        throws DataProviderException {

        int length = last - first + 1; // Length of auxilary array.
        int tmp[] = new int[length];   // Auxilary array.
        int row1 = 0;                // Index of first subarray.
        int row2 = mid - first + 1;  // Index of second subarray.

        // Initialize auxilary array.
        for (int i = 0; i < length; i++) {
            tmp[i] = a[first + i];
        }

        // Combine subarrays.
        for (int i = 0; i < length; i++) {
            if (row2 <= last - first) {
                if (row1 <= mid - first) {
                    if (sc.isAscending()
                        ? compare(sc, tdp, rows[tmp[row1]], rows[tmp[row2]]) > 0
                        : compare(sc, tdp, rows[tmp[row1]], rows[tmp[row2]]) < 0)
                        a[first + i] = tmp[row2++];
                    else {
                        a[first + i] = tmp[row1++];
                    }
                }
                else {
                    a[first + i] = tmp[row2++];
                }
            }
            else {
                a[first + i] = tmp[row1++];
            }
        }
    }

    /*
     * Helper method to compare two objects. This method compares the following
     * types:
     *
     * Boolean
     * Character
     * Comparator
     * Date
     * Number
     * String
     *
     * If the object type is not identified, the object value is compared as a
     * string.
     */
    private int compare(SortCriteria sc, TableDataProvider tdp, RowKey row1, RowKey row2)
        throws DataProviderException {
        // Get objects for the current row index
        Object o1 = sc.getSortValue(tdp, row1);
        Object o2 = sc.getSortValue(tdp, row2);

        // Ensure objects are not null
        if (o1 == null && o2 == null) {
            return 0;
        }
        else if (o1 == o2) {
            return 0;
        }
        else if (o1 == null) {
            return 1;
        }
        else if (o2 == null) {
            return -1; // Null values should appear first for descending sorts.
        }

        // Test object values
        if (o1 instanceof Comparator && o2 instanceof Comparator) {
            return ((Comparator)o1).compare(o1, o2);
        }
        else if (o1 instanceof Character && o2 instanceof Character) {
            return ((Character)o1).compareTo(o2);
        }
        else if (o1 instanceof Date && o2 instanceof Date) {
            return ((Date)o1).compareTo(o2);
        }
        else if (o1 instanceof Number && o2 instanceof Number) {
            Double d1 = new Double(((Number)o1).doubleValue());
            Double d2 = new Double(((Number)o2).doubleValue());
            return d1.compareTo(d2);
        }
        else if (o1 instanceof Boolean && o2 instanceof Boolean) {
            boolean b1 = ((Boolean)o1).booleanValue();
            boolean b2 = ((Boolean)o2).booleanValue();

            if (b1 == b2)
                return 0;
            else if (b1)
                return -1;
            else
                return 1;
        }
        else if (o1 instanceof String && o2 instanceof String) {
            String s1 = (String)o1;
            String s2 = (String)o2;

            // The String.compareTo method performs a binary comparison of the
            // Unicode characters within the two strings. For most languages;
            // however, this binary comparison cannot be relied on to sort
            // strings because the Unicode values do not correspond to the
            // relative order of the characters.
            Collator collator = Collator.getInstance(
                sortLocale != null ? sortLocale : Locale.getDefault());
            collator.setStrength(Collator.IDENTICAL);

            return collator.compare(s1, s2);
        }
        else {
            String s1 = o1.toString();
            String s2 = o2.toString();
            return s1.compareTo(s2);
        }
    }
}
