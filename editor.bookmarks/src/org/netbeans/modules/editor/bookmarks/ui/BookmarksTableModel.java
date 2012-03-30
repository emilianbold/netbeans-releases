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

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 * Container for bookmark nodes.
 *
 * @author Miloslav Metelka
 */
public class BookmarksTableModel extends AbstractTableModel {
    
    static final int NAME_COLUMN = 0;
    
    static final int KEY_COLUMN = 1;
    
    static final int LOCATION_COLUMN = 2;

    static final int COLUMN_COUNT = LOCATION_COLUMN + 1;
    
    private static final boolean[] COLUMN_EDITABLE = new boolean[] {
        true,
        true,
        false,
    };
    
    private List<BookmarkNode> entries;

    public BookmarksTableModel() {
        entries = new ArrayList<BookmarkNode>();
    }
    
    public void setEntries(List<BookmarkNode> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
        fireTableDataChanged();
    }
    
    public void addEntry(BookmarkNode entry) {
        entries.add(entry);
        int index = getRowCount() - 1;
        fireTableRowsInserted(index, index);
    }

    public BookmarkNode getEntry(int rowIndex) {
        return entries.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return COLUMN_EDITABLE[columnIndex];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        BookmarkNode entry = entries.get(rowIndex);
        switch (columnIndex) {
            case NAME_COLUMN:
                return entry.getBookmarkName();
            case KEY_COLUMN:
                return entry.getBookmarkKey();
            case LOCATION_COLUMN:
                return entry.getBookmarkLocation();
            default:
                throw new IllegalStateException("Invalid columnIndex=" + columnIndex); // NOI18N
        }
    }
    
    public String getToolTipText(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case NAME_COLUMN:
                String nameToolTip = (String) getValueAt(rowIndex, columnIndex);
                if (nameToolTip == null || nameToolTip.length() == 0) {
                    nameToolTip = NbBundle.getMessage(BookmarksTable.class, "LBL_BookmarkNameEmpty");
                }
                return nameToolTip;
            case KEY_COLUMN:
                String keyToolTip = (String) getValueAt(rowIndex, columnIndex);
                if (keyToolTip == null || keyToolTip.length() == 0) {
                    keyToolTip = NbBundle.getMessage(BookmarksTable.class, "LBL_BookmarkKeyEmpty");
                }
                return keyToolTip;
            case LOCATION_COLUMN:
                return entries.get(rowIndex).getBookmarkFullLocation();
            default:
                throw new IllegalStateException("Invalid columnIndex=" + columnIndex); // NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (!COLUMN_EDITABLE[columnIndex]) {
            return;
        }
        BookmarkNode entry = entries.get(rowIndex);
        switch (columnIndex) {
            case NAME_COLUMN:
                entry.setBookmarkName((String)value);
                break;
            case KEY_COLUMN:
                entry.setBookmarkKey((String)value);
                break;
            case LOCATION_COLUMN:
                throw new IllegalStateException("Should never get here"); // NOI18N
            default:
                throw new IllegalStateException("Invalid columnIndex=" + columnIndex); // NOI18N
        }
    }

}
