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

package org.netbeans.modules.xml.xam.ui.column;

/**
 * A ColumnView contains one or more Column instances, and manages their
 * display in terms of providing a scrollable view.
 *
 * @author  Nathan Fiedler
 */
public interface ColumnView {

    /**
     * Appends the given column to the end of the column list.
     *
     * @param  column  Column to be added to view.
     */
    void appendColumn(Column column);

    /**
     * Appends the array of columns to the end of the column list.
     * Any scrolling will be done once, making the last column visible.
     *
     * @param  columns  array of columns to be added to view.
     */
    void appendColumns(Column[] columns);

    /**
     * Remove all of the columns from the view.
     */
    void clearColumns();

    /**
     * Returns the number of columns contained in this view.
     *
     * @return  column count.
     */
    int getColumnCount();

    /**
     * Get the index into the list for the given column.
     *
     * @param  column  Column to locate in view.
     * @return  index of the column, or -1 if not found.
     */
    int getColumnIndex(Column column);

    /**
     * Retrieves the first column in the view.
     *
     * @return  first Column, or null if no columns exist.
     */
    Column getFirstColumn();

    /**
     * Retrieves the column following the one given.
     *
     * @param  column  Column for which to find sibling.
     * @return  next Column, or null if none.
     */
    Column getNextColumn(Column column);

    /**
     * Removes the columns after the one given.
     *
     * @param  column  Column that will be the new right-most column.
     */
    void removeColumnsAfter(Column column);

    /**
     * Scrolls the viewport to make the specified column visible. If the
     * synchronous parameter is false, this method will return before the
     * viewport has finished scrolling. If the synchronous parameter is
     * true, this method will scroll the viewport on the event thread
     * (if the invoking thread is not the event thread, this method will
     * block on the event thread until the scrolling is complete).
     *
     * @param  column       Column to make visible.
     * @param  synchronous  true to scroll now, on event thread, false to
     *                      scroll some time later, on event thread.
     */
    void scrollToColumn(Column column, boolean synchronous);
}
