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
