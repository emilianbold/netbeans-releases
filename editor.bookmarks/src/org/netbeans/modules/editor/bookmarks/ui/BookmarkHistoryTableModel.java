/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.bookmarks.ui;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;

/**
 * Model for JTable in Popup switcher.
 * <br/>
 * It has either one or more columns depending on vertical space available.
 *
 * @author Miloslav Metelka
 */
public class BookmarkHistoryTableModel  extends AbstractTableModel {
    
    private final List<BookmarkInfo> entries;
    
    /**
     * Number of entries in a single column (in possibly multi-column model).
     */
    private int columnEntryCount = Integer.MAX_VALUE;
    
    private int columnCount = 1;

    public BookmarkHistoryTableModel(List<BookmarkInfo> entries) {
        this.entries = entries;
    }
    
    /**
     * Set maximum number of entries in a single column.
     *
     * @param columnEntryCount entry count per column.
     * @return total number of columns based on current entry count.
     */
    int setColumnEntryCount(int columnEntryCount) {
        assert (columnEntryCount > 0) : "columnEntryCount=" + columnEntryCount + " <= 0"; // NOI18N
        this.columnEntryCount = columnEntryCount;
        this.columnCount = Math.min(1, (entries.size() + columnEntryCount - 1) / columnEntryCount);
        fireTableDataChanged();
        return this.columnCount;
    }
    
    public BookmarkInfo getEntry(int entryIndex) {
        return entries.get(entryIndex);
    }
    
    public BookmarkInfo getEntry(int rowIndex, int columnIndex) {
        return entries.get(entryIndex(rowIndex, columnIndex));
    }
    
    public int getEntryCount() {
        return entries.size();
    }

    public int entryIndex(int rowIndex, int columnIndex) {
        return columnIndex * columnEntryCount + rowIndex;
    }
    
    public void entryIndex2rowColumn(int entryIndex, int[] rowColumn) {
        rowColumn[1] = entryIndex / columnEntryCount;
        rowColumn[0] = entryIndex - (rowColumn[1] * columnEntryCount);
    }
    
    @Override
    public int getRowCount() {
        return Math.min(entries.size(), columnEntryCount);
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    public int getColumnEntryCount() {
        return columnEntryCount;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        BookmarkInfo bookmark = getEntry(rowIndex, columnIndex);
        return bookmark.getDisplayName();
    }
    
    public String getDescription(int rowIndex, int columnIndex) {
        BookmarkInfo bookmark = getEntry(rowIndex, columnIndex);
        return bookmark.getFullPathDescription();
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        throw new IllegalStateException("Cannot modify any entries"); // NOI18N
    }
    
}
