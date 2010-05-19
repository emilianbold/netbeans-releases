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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.ui.graph.ICanvasInterface;
import org.netbeans.modules.sql.framework.ui.graph.IGraphInterface;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDocumentChangedEdit;
import com.nwoods.jgo.JGoDocumentEvent;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoScrollBar;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TableArea extends CanvasArea implements IGraphInterface, ICanvasInterface {

    /**
     * Table header for this table.
     */
    protected TableHeaderArea headerArea;

    /**
     * Left port area.
     */
    protected ColumnPortArea leftPortArea;

    /**
     * Right port area.
     */
    protected ColumnPortArea rightPortArea;

    /**
     * Table type.
     */
    protected int tableType = TableConstants.INPUT_OUTPUT_TABLE;

    /**
     * Represents number of columns in the table
     */
    protected ArrayList columnAreas;

    // this is the column being resized.
    private ColumnArea resizeColumn;

    // these are for what is visible in this table area
    private int firstVisibleRow = 0;
    private int firstVisibleColumn = 0;
    private int lastVisibleColumn = -1;

    // some constant for gaps
    private int portAreaColumnAreaGap = 0;
    private int headerColumnGap = 0;

    // vertical scrollbar
    private BasicScrollBar vScrollBar;

    // horizontal scrollbar
    private BasicScrollBar hScrollBar;

    // this is the size of the scrollbar

    private JGoRectangle topLeftCornerRect;

    private JGoRectangle topRightCornerRect;

    private JGoRectangle rect;

    private boolean expanded = true;

    private ColumnArea lastColumn;

    private boolean showHeader = true;

    private boolean disableLayout = false;

    // EventHints
    /**
     * Event fired when first visible row in the table is changed.
     */
    public static final int FIRST_VISIBLE_ROW_CHANGED = JGoDocumentEvent.LAST + 40001;

    /**
     * Event fired when last visible row in the table is changed.
     */
    public static final int LAST_VISIBLE_ROW_CHANGED = JGoDocumentEvent.LAST + 40002;

    /**
     * Event fired when first visible column in the table is changed.
     */
    public static final int FIRST_VISIBLE_COLUMN_CHANGED = JGoDocumentEvent.LAST + 40003;

    /**
     * Event fired when last visible column in the table is changed.
     */
    public static final int LAST_VISIBLE_COLUMN_CHANGED = JGoDocumentEvent.LAST + 40004;

    // AutoResizeMode
    /**
     * When a column is adjusted in the UI, adjust the next column the opposite way.
     */
    public static final int AUTO_RESIZE_NEXT_COLUMN = 1;

    /**
     * During UI adjustment, change subsequent columns to preserve the total width; this
     * is the default behavior.
     */
    public static final int AUTO_RESIZE_SUBSEQUENT_COLUMNS = 2;

    /**
     * During all resize operations, apply adjustments to the last column only.
     */
    public static final int AUTO_RESIZE_LAST_COLUMN = 3;

    /** During all resize operations, proportionately resize all columns. */
    public static final int AUTO_RESIZE_ALL_COLUMNS = 4;

    /** Creates a new instance of TableArea */
    public TableArea() {
        super();
        initGui();
    }

    /**
     * Initializes GUI components.
     */
    protected void initGui() {
        headerArea = new TableHeaderArea();
        this.addObjectAtTail(headerArea);
        columnAreas = new ArrayList();

        this.setDraggable(true);
        // for now do not allow it to be resizeable
        this.setResizable(false);

        // initialize top left and right corner display rectangle
        topLeftCornerRect = new JGoRectangle();
        topLeftCornerRect.setSelectable(false);
        topLeftCornerRect.setResizable(false);
        topLeftCornerRect.set4ResizeHandles(false);
        topLeftCornerRect.setPen(JGoPen.makeStockPen(Color.lightGray));
        topLeftCornerRect.setBrush(JGoBrush.makeStockBrush(new Color(221, 235, 246)));
        addObjectAtHead(topLeftCornerRect);

        topRightCornerRect = new JGoRectangle();
        topRightCornerRect.setSelectable(false);
        topRightCornerRect.setResizable(false);
        topRightCornerRect.set4ResizeHandles(false);
        topRightCornerRect.setPen(JGoPen.makeStockPen(Color.lightGray));
        topRightCornerRect.setBrush(JGoBrush.makeStockBrush(new Color(221, 235, 246)));//(254,253,235)
        addObjectAtHead(topRightCornerRect);

        rect = new JGoRectangle();
        rect.setSelectable(false);
        rect.setResizable(false);
        rect.set4ResizeHandles(false);
        rect.setPen(JGoPen.makeStockPen(Color.lightGray));
        addObjectAtHead(rect);
    }

    /**
     * Creates new instance of table.
     * 
     * @param tType the table type
     */
    protected TableArea(int tType) {
        this();
        this.tableType = tType;
    }

    /**
     * Creates an instance of table area based on row data and column name.
     * 
     * @param rowData the row data to be displayed in the table
     * @param columnNames the name of the column headers
     */
    public TableArea(String[][] rowData, String[] columnNames) {
        this();
        setHeaders(columnNames);
        addItems(rowData);
        initialize(getTableType());
    }

    /**
     * Creates an instance of table area based on row data and column name and table type.
     * 
     * @param rowData the row data to be displayed in the table
     * @param columnNames the name of the column headers
     * @param tType the table type
     */
    public TableArea(String[][] rowData, String[] columnNames, int tType) {
        this(tType);
        setHeaders(columnNames);
        addItems(rowData);

        initialize(tType);
    }

    /**
     * Initialize the table.
     * 
     * @param tType the table type
     */
    public void initialize(int tType) {
        this.tableType = tType;

        // if table is out we only need to provide port on left side
        // if it is both input and output , then we need ports on both sides
        if (tType == TableConstants.OUTPUT_TABLE) {
            leftPortArea = new ColumnPortArea(ColumnPortArea.LEFT_PORT_AREA, getRowCount());
            this.addObjectAtTail(leftPortArea);
        } else if (tType == TableConstants.INPUT_TABLE) {
            // if it is input we need to provide port on the right side
            rightPortArea = new ColumnPortArea(ColumnPortArea.RIGHT_PORT_AREA, getRowCount());
            this.addObjectAtTail(rightPortArea);
        } else if (tType == TableConstants.INPUT_OUTPUT_TABLE) {
            leftPortArea = new ColumnPortArea(ColumnPortArea.LEFT_PORT_AREA, getRowCount());
            this.addObjectAtTail(leftPortArea);
            rightPortArea = new ColumnPortArea(ColumnPortArea.RIGHT_PORT_AREA, getRowCount());
            this.addObjectAtTail(rightPortArea);
        } else if(tType == TableConstants.NO_PORT_TABLE) {
            // do nothing
        }

    }

    /**
     * Removes all of the child objects in this area.
     */
    public void removeAll() {
        super.removeAll();
        columnAreas.clear();
    }

    /**
     * Gets table type.
     * 
     * @return current table type
     */
    public int getTableType() {
        return this.tableType;
    }

    /**
     * Sets table type.
     * 
     * @param tType new table type
     */
    public void setTableType(int tType) {
        this.tableType = tType;
    }

    /**
     * Indicates whether table is expanded.
     * 
     * @return true if table is expanded; false otherwise
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Sets table mode as expanded.
     * 
     * @param sExpanded true to expand the table, false otherwise.
     */
    public void setExpanded(boolean sExpanded) {
        this.expanded = sExpanded;
    }

    /**
     * Gets the left port area of the table.
     * 
     * @return left port area
     */
    public ColumnPortArea getLeftPortArea() {
        return leftPortArea;
    }

    /**
     * Gets the right port area of the table.
     * 
     * @return right port area
     */
    public ColumnPortArea getRightPortArea() {
        return rightPortArea;
    }

    /**
     * Gets the row count in this table area.
     * 
     * @return number of rows in this table area
     */
    public int getRowCount() {
        if (columnAreas == null) {
            return -1;
        }
        Iterator it = columnAreas.iterator();
        if (it.hasNext()) {
            ColumnArea column = (ColumnArea) it.next();
            return column.getRowCount();
        }

        return -1;
    }

    /**
     * Gets column count in this area.
     * 
     * @return number of column in this area
     */
    public int getColumnCount() {
        return headerArea.getHeaderCount();
    }

    // build the table with the provided set of row data
    private void addItems(String[][] rowData) {
        for (int i = 0; i < rowData.length; i++) {
            String[] columns = rowData[i];

            for (int j = 0; j < columns.length; j++) {
                String cellVal = columns[j];
                addItem(i, j, cellVal, "");
            }
        }
    }

    /**
     * Sets the header on this table.
     * 
     * @param columnNames names of the headers
     */
    public void setHeaders(String[] columnNames) {
        headerArea.setHeaders(columnNames);
    }

    /**
     * Adds the column cell in the table header.
     * 
     * @param columnName name of table column
     */
    public void addColumn(String columnName) {
        headerArea.addHeaderCell(columnName);
    }

    /**
     * Adds item to this table.
     * 
     * @param row row where item needs to be added
     * @param col column in row row where item needs to be added
     * @param val the string value of the item
     */
    public void addItem(int row, int col, String val, String toolTip) {
        ColumnArea column = null;

        if (columnAreas.size() != 0 && col < columnAreas.size()) {
            column = (ColumnArea) columnAreas.get(col);
        }
        if (column == null) {
            column = new ColumnArea(getTableType());

            columnAreas.add(col, column);
            this.addObjectAtTail(column);
        }
        column.addItem(row, val, toolTip);
        addPort(row);
        // remember the last column added in the table
        lastColumn = column;

        this.setHeight(this.getMaximumHeight());
    }

    /**
     * Adds item to this table.
     * 
     * @param row row where item needs to be added
     * @param col column in row row where item needs to be added
     * @param val the string value of the item
     */
    public void addItem(int row, int col, SQLDBColumn data, String toolTip) {
        ColumnArea column = null;

        if (columnAreas.size() != 0 && col < columnAreas.size()) {
            column = (ColumnArea) columnAreas.get(col);
        }
        if (column == null) {
            column = new ColumnArea(getTableType());

            columnAreas.add(col, column);
            this.addObjectAtTail(column);
        }
        column.addItem(row, data, toolTip);
        addPort(row);
        // remember the last column added in the table
        lastColumn = column;

        this.setHeight(this.getMaximumHeight());
    }

    private void addPort(int row) {
        // if table is out we only need to provide port on left side
        // if it is both input and output , then we need ports on both sides
        if (this.tableType == TableConstants.OUTPUT_TABLE) {
            leftPortArea = this.getLeftPortArea();
            if (leftPortArea != null && leftPortArea.getRowCount() != this.getRowCount()) {
                leftPortArea.addPort(row);
            }
        }
        // if it is input we need to provide port on the right side
        if (this.tableType == TableConstants.INPUT_TABLE) {
            rightPortArea = this.getRightPortArea();
            if (rightPortArea != null && rightPortArea.getRowCount() != this.getRowCount()) {
                rightPortArea.addPort(row);
            }
        }
        if (this.tableType == TableConstants.INPUT_OUTPUT_TABLE) {

            leftPortArea = this.getLeftPortArea();
            if (leftPortArea != null && leftPortArea.getRowCount() != this.getRowCount()) {
                leftPortArea.addPort();
            }

            rightPortArea = this.getRightPortArea();
            if (rightPortArea != null && rightPortArea.getRowCount() != this.getRowCount()) {
                rightPortArea.addPort();
            }
        }
    }

    public void removeItem(int row, int col) {
        ColumnArea column = null;

        if (columnAreas.size() != 0 && col < columnAreas.size()) {
            column = (ColumnArea) columnAreas.get(col);
        }

        if (column == null) {
            throw new IllegalArgumentException("Cannot delete table item for row " + row + ", column " + col);
        }

        column.removeItem(row);

        if (leftPortArea != null) {
            leftPortArea.removePort(row);
        }

        if (rightPortArea != null) {
            rightPortArea.removePort(row);
        }

        this.setHeight(this.getMaximumHeight());
    }

    public IGraphPort getLeftGraphPort(int row) {
        if (leftPortArea != null) {
            PortArea pArea = leftPortArea.getPortAreaAt(row);
            if (pArea != null) {
                return pArea.getGraphPort();
            }
        }

        return null;
    }

    public IGraphPort getRightGraphPort(int row) {
        if (rightPortArea != null) {
            PortArea pArea = rightPortArea.getPortAreaAt(row);
            if (pArea != null) {
                return pArea.getGraphPort();
            }
        }

        return null;
    }

    /**
     * Gets the vertical scrollbar.
     * 
     * @return vertical scrollbar
     */
    public JGoScrollBar getVerticalScrollBar() {
        return vScrollBar;
    }

    /**
     * Gets the horizontal scrollbar.
     * 
     * @return horizontal scrollbar
     */
    public JGoScrollBar getHorizontalScrollBar() {
        return hScrollBar;
    }

    /**
     * Gets the first visible row of this table.
     * 
     * @return first visible row
     */
    public int getFirstVisibleRow() {
        return lastColumn != null ? lastColumn.getFirstVisibleRow() : -1;
    }

    /**
     * Sets the first visible row of the table.
     * 
     * @param rowIdx the first visible row of this table
     */
    public void setFirstVisibleRow(int rowIdx) {
        int oldIndex = firstVisibleRow;

        if (rowIdx >= 0 && rowIdx <= getRowCount() && oldIndex != rowIdx) {
            firstVisibleRow = rowIdx;
            layoutAllColumns(rowIdx);
            update(FIRST_VISIBLE_ROW_CHANGED, oldIndex, null);
        }
    }

    /**
     * Gets the last visible row in this table.
     * 
     * @return last visible row
     */
    public int getLastVisibleRow() {
        // return lastVisibleRow;
        return lastColumn != null ? lastColumn.getLastVisibleRow() : -1;
    }

    /**
     * Gets the first visible column of the table.
     * 
     * @return first visible column
     */
    public int getFirstVisibleColumn() {
        return firstVisibleColumn;
    }

    /**
     * Sets the first visible column in this table.
     * 
     * @param colIdx the column to set as the first visible column
     */
    public void setFirstVisibleColumn(int colIdx) {
        int oldIndex = firstVisibleColumn;

        if (colIdx >= 0 && colIdx <= getColumnCount() && oldIndex != colIdx) {
            firstVisibleRow = colIdx;
            layoutChildren();
            update(FIRST_VISIBLE_COLUMN_CHANGED, oldIndex, null);
        }
    }

    /**
     * Gets the last visible column in this table.
     * 
     * @return the last visible column in this table
     */
    public int getLastVisibleColumn() {
        return lastVisibleColumn;
    }

    private void layoutAllColumns(int rowIdx) {
        if (leftPortArea != null) {
            leftPortArea.setFirstVisibleRow(rowIdx);
        }

        if (rightPortArea != null) {
            rightPortArea.setFirstVisibleRow(rowIdx);
        }

        Iterator it = columnAreas.iterator();
        while (it.hasNext()) {
            ColumnArea columnArea = (ColumnArea) it.next();
            columnArea.setFirstVisibleRow(rowIdx);
        }

    }

    /**
     * Sets the value of a particular cell.
     * 
     * @param row row to look the cell
     * @param col column to look for
     * @param val new value of the cell
     */
    public void setValueAt(int row, int col, String val) {
    }

    /**
     * Gets the maximum width which will allow us to show all the columns properly with no
     * excess whitespace surrounding the column with the longest name.
     * 
     * @return maximum width of this table
     */
    public int getMaximumWidth() {
        Iterator it = columnAreas.iterator();
        int maxWidth = 0;

        // take the largest maximum width of all column areas into account
        while (it.hasNext()) {
            ColumnArea column = (ColumnArea) it.next();
            maxWidth = Math.max(maxWidth, column.getMaximumWidth());
        }

        maxWidth += getPortAreaWidth();

        // Always account for horizontal insets.
        maxWidth += getInsets().left + getInsets().right;
        return maxWidth;
    }

    /**
     * Gets the minimum width of this tables
     * 
     * @return minimum width of this table
     */
    public int getMinimumWidth() {
        int minWidth = 0;

        // take the largest maximum width of all columns into account
        Iterator it = columnAreas.iterator();
        while (it.hasNext()) {
            ColumnArea column = (ColumnArea) it.next();
            minWidth = Math.max(minWidth, column.getMaximumWidth());
        }

        minWidth += getPortAreaWidth();

        // Always account for horizontal insets.
        minWidth += getInsets().left + getInsets().right;
        return minWidth;
    }

    /**
     * @return
     */
    private int getPortAreaWidth() {
        int portAreaWidth = 0;

        // we at least want to show port areas
        // now take the width of port areas into account
        if (getTableType() == TableConstants.INPUT_TABLE || getTableType() == TableConstants.INPUT_OUTPUT_TABLE) {
            portAreaWidth += leftPortArea != null ? leftPortArea.getMaximizePortAreaWidth() : 0;
            // account for gap between PortArea and ColumnArea
            portAreaWidth += portAreaColumnAreaGap;
        }

        if (getTableType() == TableConstants.OUTPUT_TABLE || getTableType() == TableConstants.INPUT_OUTPUT_TABLE) {
            portAreaWidth += rightPortArea != null ? rightPortArea.getMaximizePortAreaWidth() : 0;
            // account for gap between PortArea and ColumnArea
            portAreaWidth += portAreaColumnAreaGap;
        }

        return portAreaWidth;
    }

    /**
     * Gets the maximum height of this table.
     * 
     * @return maximum height of table
     */
    public int getMaximumHeight() {
        Iterator it = columnAreas.iterator();
        // int maxWidth = 0;
        int maxHeight = 0;

        while (it.hasNext()) {
            ColumnArea column = (ColumnArea) it.next();
            int height = column.getMaximumHeight();
            if (height > maxHeight) {
                maxHeight = height;
            }
        }

        if (showHeader && getRowCount() != 0) {
            // accounts for height of header
            maxHeight += headerArea.getMaximumHeight();
            // account for gap between header and column
            maxHeight += headerColumnGap;
        }

        // account for insets
        maxHeight += getInsets().top + getInsets().bottom;

        return maxHeight;
    }

    /**
     * Gets the minimum height of this table.
     * 
     * @return minimum height of table
     */
    public int getMinimumHeight() {
        // account for insets
        int minHeight = getInsets().top + getInsets().bottom;

        // account for header. we atleast want to show title always
        if (showHeader && getRowCount() != 0) {
            minHeight += headerArea.getMaximumHeight();
        }

        // account for first cell in a column we atleast want show first cell
        // we should add vertical spacing also + ;
        minHeight += getMaximumCellHeight(0);
        return minHeight;
    }

    /**
     * Gets height of the tallest column cell for a given row.
     * 
     * @param row the row to look cell
     * @return maximum cell height in a row
     */
    public int getMaximumCellHeight(int row) {
        Iterator it = columnAreas.iterator();
        int maxHeight = 0;

        while (it.hasNext()) {
            ColumnArea column = (ColumnArea) it.next();
            int height = column.getCellHeight(row);
            if (height > maxHeight) {
                maxHeight = height;
            }
        }
        return maxHeight;
    }

    /**
     * Gets the height of all visible rows in this table.
     * 
     * @return height of visible rows
     */
    public int getVisibleRowTableHeights() {
        int height = lastColumn != null ? lastColumn.getVisibleRowHeights() : 0 + insets.top + insets.bottom
            + ((headerArea != null && showHeader && getRowCount() != 0) ? headerArea.getMaximumHeight() : 0) + headerColumnGap;

        return height;
    }

    private void adjustPortAreaHeight(ColumnPortArea cPortArea) {
        // go through the column and find out the maximum height of each row
        for (int i = 0; i < columnAreas.size(); i++) {
            int cellHeight = getMaximumCellHeight(i);
            cPortArea.setPortAreaHeight(i, cellHeight);
        }
    }

    /**
     * Overrides parent method to handle the changes in the geometry of this area. We will
     * lay out all the columns and headers again.
     * 
     * @param prevRect previous bound rectangle
     */
    protected void geometryChange(Rectangle prevRect) {
        // handle any size changes by repositioning all the items
        if (!disableLayout && prevRect.width != getWidth() || prevRect.height != getHeight()) {
            layoutChildren();
        } else {
            super.geometryChange(prevRect);
        }

    }

    /**
     * Lays out all the children of this table area.
     */
    public void layoutChildren() {
        // rect.setBoundingRect(this.getBoundingRect());
        Insets insets1 = getInsets();

        // get the bounding rectangle of this table area
        int x = getLeft() + insets1.left;
        int y = getTop() + insets1.top;
        int width = getWidth() - insets1.left - insets1.right;
        int height = getHeight() - insets1.top - insets1.bottom;

        // calcualte the left for next column
        int nextColumnLeft = x;

        int headerHeight = 0;

        if (showHeader && getRowCount() != 0) {
            headerHeight = headerArea.getMaximumHeight();
        }

        int leftPortAreaWidth = 0;
        int rightPortAreaWidth = 0;

        // position leftportarea if it is available
        if (leftPortArea != null) {
            // this adjustment can be done in the following while loop also
            // do that if we see performance issues
            adjustPortAreaHeight(leftPortArea);

            // if (isExpanded()) {
            if (height - headerHeight - headerColumnGap > 0) {
                leftPortArea.setVisible(true);
                leftPortArea.setBoundingRect(x, y + headerHeight + headerColumnGap, leftPortArea.getWidth(), height - headerHeight - headerColumnGap);

                // set the top left and right corner rect bounds
                if (showHeader && getRowCount() != 0) {
                    topLeftCornerRect.setVisible(true);
                    topLeftCornerRect.setBoundingRect(x, y, leftPortArea.getWidth(), headerHeight);
                } else {
                    topLeftCornerRect.setVisible(false);
                }
            } else {
                leftPortArea.setVisible(false);
                leftPortArea.setBoundingRect(x, y, leftPortArea.getWidth(), headerHeight);
            }

            leftPortAreaWidth = leftPortArea.getWidth();
            nextColumnLeft += leftPortAreaWidth + portAreaColumnAreaGap;
        }

        // position rightportarea if it is available
        if (rightPortArea != null) {
            adjustPortAreaHeight(rightPortArea);
            if (height - headerHeight - headerColumnGap > 0) {
                rightPortArea.setVisible(true);
                Rectangle rPortRect = new Rectangle(getLeft() + getWidth() - rightPortArea.getWidth() - portAreaColumnAreaGap, y + headerHeight
                    + headerColumnGap, rightPortArea.getWidth(), height - headerHeight - headerColumnGap);
                rightPortArea.setBoundingRect(rPortRect);
            } else {
                rightPortArea.setVisible(false);
                rightPortArea.setBoundingRect(getLeft() + getWidth() - rightPortArea.getWidth() - portAreaColumnAreaGap, y, rightPortArea.getWidth(),
                    headerHeight);
            }

            rightPortAreaWidth = rightPortArea.getWidth();
        }

        int columnWidth = 0;

        // divide the width among all the column equally when resizing
        // table externally
        if (resizeColumn == null && columnAreas.size() != 0) {
            int allColumnWidth = width - leftPortAreaWidth - rightPortAreaWidth;
            columnWidth = allColumnWidth / columnAreas.size();
        }

        // what is old last visible row
        // int oldLastVisibleRow = this.getLastVisibleRow();

        // go through the column and find out the maximum width of
        // each one of them
        Iterator it = columnAreas.iterator();

        int cumulativeWidth = getCumulativeColumnPreferredWidth();

        int cnt = 0;
        while (it.hasNext()) {
            ColumnArea column = (ColumnArea) it.next();
            columnWidth = getColumnWidth(cnt, cumulativeWidth);
            headerArea.setHeaderCellWidth(cnt, columnWidth);

            // set the column position and height, width will be column width
            column.setBoundingRect(nextColumnLeft, y + headerHeight + headerColumnGap, columnWidth, height - headerHeight - headerColumnGap);
            nextColumnLeft += columnWidth;
            cnt++;
        }

        if (showHeader && getRowCount() != 0) {
            headerArea.setBoundingRect(x + leftPortAreaWidth + portAreaColumnAreaGap, y, width - leftPortAreaWidth - rightPortAreaWidth - cnt
                * portAreaColumnAreaGap, headerHeight);
        }

        if (topRightCornerRect != null && showHeader && getRowCount() != 0) {
            topRightCornerRect.setVisible(true);
            topRightCornerRect.setBoundingRect(getLeft() + getWidth() - rightPortAreaWidth - portAreaColumnAreaGap, y, rightPortAreaWidth,
                headerHeight);
        } else {
            topRightCornerRect.setVisible(false);
        }
    }

    private int getColumnWidth(int columnIndex, int cumulativeWidth) {
        Insets insets1 = getInsets();

        // get the bounding rectangle of this table area
        int width = getWidth() - insets1.left - insets1.right - (leftPortArea != null ? leftPortArea.getWidth() : 0)
            - (rightPortArea != null ? rightPortArea.getWidth() : 0) - 2 * portAreaColumnAreaGap;

        ColumnArea column = (ColumnArea) columnAreas.get(columnIndex);

        // if column has preferred width then return it
        int preferredWidth = column.getPreferredWidth();

        // if we have more than one column with preffered width
        // we want to make sure we have enough width to accomodate
        // all preffered width if not we will ignore preffered width
        // and divide it equally among all the columns.
        // also if all column have preffered size set then we will
        // ignore it
        if (preferredWidth != -1 && cumulativeWidth < width && getColumnCountWithPreferredWidth() != columnAreas.size()) {
            return preferredWidth;
        }

        int columnWidth = 0;
        // divide the width among all the column equally when resizing
        // table externally
        if (resizeColumn == null) {
            int preferredWidthColumnCount = getColumnCountWithPreferredWidth();
            int columnCount = columnAreas.size() - preferredWidthColumnCount;
            int allColumnWidth = width - cumulativeWidth;

            // check for divide by zero error
            // also if all column have preffered size set then we will ignore it
            if (columnCount == 0) {
                columnCount = columnAreas.size();
                allColumnWidth = width;
            }

            columnWidth = allColumnWidth / columnCount;
            // is there a 1 pixel excess width that needs to be allocated
            int excessWidth = allColumnWidth - columnWidth * columnCount;
            // if last column allocate any 1 pixel excess width also this
            // will manage scroll bar
            // updates
            if (columnIndex == columnAreas.size() - 1) {
                column.setManageHorizontalScrollBar(true);
                columnWidth += excessWidth;
            }
        }

        return columnWidth;
    }

    private int getCumulativeColumnPreferredWidth() {
        int width = 0;

        Iterator it = columnAreas.iterator();

        while (it.hasNext()) {
            ColumnArea column = (ColumnArea) it.next();
            // if column has preffered width then add it
            int preferredWidth = column.getPreferredWidth();
            if (preferredWidth != -1) {
                width += preferredWidth;
            }
        }

        return width;
    }

    /**
     * get the column area for this table at a particular index
     * 
     * @param index column area at index
     * @return column area
     */
    public ColumnArea getColumnArea(int index) {
        if (index <= columnAreas.size() - 1) {
            return (ColumnArea) columnAreas.get(index);
        }

        return null;
    }

    /**
     * get the input port area
     * 
     * @return column port area
     */
    public ColumnPortArea getInputPortArea() {
        return leftPortArea;
    }

    /**
     * get the output port area
     * 
     * @return column port area
     */
    public ColumnPortArea getOutputPortArea() {
        return rightPortArea;
    }

    private int getColumnCountWithPreferredWidth() {
        int count = 0;

        Iterator it = columnAreas.iterator();

        while (it.hasNext()) {
            ColumnArea column = (ColumnArea) it.next();
            // if column has preffered width then add it
            int preferredWidth = column.getPreferredWidth();
            if (preferredWidth != -1) {
                count++;
            }
        }
        return count;
    }

    /**
     * get the bounds of title area this table contains
     * 
     * @return the rectangle bounds of title area
     */
    public Rectangle getTitleAreaBounds() {
        JGoObject parent = this.getParent();
        if (parent != null && parent instanceof IGraphInterface) {
            return ((IGraphInterface) parent).getTitleAreaBounds();
        }

        return headerArea.getBoundingRect();
    }

    /**
     * copy new values when redo occurs
     * 
     * @param e JGoDocumentChangedEdit
     */
    public void copyNewValueForRedo(JGoDocumentChangedEdit e) {
        switch (e.getFlags()) {
            case FIRST_VISIBLE_ROW_CHANGED:
                e.setNewValueInt(this.getFirstVisibleRow());
                return;
            default:
                super.copyNewValueForRedo(e);
                return;
        }
    }

    public void setShowHeader(boolean show) {
        this.showHeader = show;
        this.headerArea.setVisible(show);
    }

    /**
     * Updates the vertical scrollbar.
     */
    public void updateVerticalScrollBar() {
        // Empty as we no longer support a vertical scroll bar for TableAreas.
    }

    public void setBackgroundColor(Color c) {
        if (this.leftPortArea != null) {
            this.leftPortArea.setBackgroundColor(c);
        }

        if (this.rightPortArea != null) {
            this.rightPortArea.setBackgroundColor(c);
        }

        if (this.topLeftCornerRect != null) {
            this.topLeftCornerRect.setBrush(JGoBrush.makeStockBrush(c));
        }

        if (this.topRightCornerRect != null) {
            this.topRightCornerRect.setBrush(JGoBrush.makeStockBrush(c));
        }
    }

}

