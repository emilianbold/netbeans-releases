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
