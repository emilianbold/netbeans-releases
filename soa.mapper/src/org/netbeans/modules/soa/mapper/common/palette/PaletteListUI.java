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

package org.netbeans.modules.soa.mapper.common.palette;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.ListSelectionListener;

/**
 * UI for PaletteList. Wraps rows/columns of list items.
 *
 *
 * @author Tientien Li
 */
public class PaletteListUI extends javax.swing.plaf.basic.BasicListUI {

    /** Field row Count           */
    private int mRowCount;

    /** Field column Count           */
    private int mColumnCount;

    /** Field lastDimension           */
    private int mLastDimension;

    /** Field CheckBoxSize           */
    private int mCheckBoxSize = 25;

    /**
     * Overwrite the paint method to paint the Palette list
     *
     * @param g the graphics context
     * @param c the component to be painted
     */
    public void paint(java.awt.Graphics g, JComponent c) {

        maybeUpdateLayoutState();

        javax.swing.ListCellRenderer   renderer  = list.getCellRenderer();
        javax.swing.ListModel          dataModel = list.getModel();
        javax.swing.ListSelectionModel selModel  = list.getSelectionModel();

        if ((renderer == null) || (dataModel.getSize() == 0)) {
            return;
        }

        Rectangle paintBounds = g.getClipBounds();
        int firstPaintRow    = convertYToRow(paintBounds.y);
        int lastPaintRow     =
            convertYToRow((paintBounds.y + paintBounds.height) - 1);
        int firstPaintColumn = convertXToColumn(paintBounds.x);
        int lastPaintColumn  =
            convertXToColumn((paintBounds.x + paintBounds.width) - 1);

        if (firstPaintRow == -1) {
            firstPaintRow = 0;
        }

        if (lastPaintRow == -1) {
            lastPaintRow = mRowCount - 1;
        }

        if (firstPaintColumn == -1) {
            firstPaintColumn = 0;
        }

        if (lastPaintColumn == -1) {
            lastPaintColumn = mColumnCount - 1;
        }

        int firstIndex = convertCellToIndex(firstPaintColumn, firstPaintRow);
        Rectangle rowBounds  = getCellBounds(list, firstIndex, firstIndex);

        if (rowBounds == null) {
            return;
        }

        int leadIndex = list.getLeadSelectionIndex();
        int rowStart  = rowBounds.y;

        for (int column = firstPaintColumn; column <= lastPaintColumn;
                column++) {
            for (int row = firstPaintRow; row <= lastPaintRow; row++) {
                int index = convertCellToIndex(column, row);

                if (index == -1) {
                    break;
                }

                rowBounds.height = getRowHeight(row);

                /* Set the clip rect to be the intersection of rowBounds
                 * and paintBounds and then paint the cell.
                 */
                g.setClip(rowBounds.x, rowBounds.y, rowBounds.width,
                          rowBounds.height);
                g.clipRect(paintBounds.x, paintBounds.y, paintBounds.width,
                           paintBounds.height);
                paintCell(g, index, rowBounds, renderer, dataModel, selModel,
                          leadIndex);

                rowBounds.y += rowBounds.height;
            }

            rowBounds.y     = rowStart;
            rowBounds.width = getColumnWidth(column);
            rowBounds.x     += rowBounds.width;
        }
    }

    /**
     * get the Preferred Size
     *
     *
     * @param c the compoent
     *
     * @return the perferred component size
     *
     */
    public Dimension getPreferredSize(JComponent c) {

        maybeUpdateLayoutState();

        int lastIndex = list.getModel().getSize() - 1;

        if (lastIndex < 0) {
            return new Dimension(0, 0);
        }

        java.awt.Insets insets = list.getInsets();

        return new Dimension((mColumnCount * cellWidth) + insets.left
                             + insets.right,
                             (mRowCount * cellHeight) + insets.top
                             + insets.bottom);
    }

    /**
     * create the palette list UI
     *
     *
     * @param list the component
     *
     * @return the palette list UI
     *
     */
    public static javax.swing.plaf.ComponentUI createUI(JComponent list) {
        return new PaletteListUI();
    }

    /**
     * map a screen location To the list item Index
     *
     *
     * @param list the palette list
     * @param location the screen location
     *
     * @return the item index
     *
     */
    public int locationToIndex(JList list, java.awt.Point location) {

        maybeUpdateLayoutState();

        return convertCellToIndex(convertXToColumn(location.x),
                                  convertYToRow(location.y));
    }

    /**
     * map a list item index To its current Location
     *
     *
     * @param list the palette list
     * @param index the item index
     *
     * @return the screen location
     *
     */
    public java.awt.Point indexToLocation(JList list, int index) {

        maybeUpdateLayoutState();

        int x = convertColumnToX(convertIndexToColumn(index));
        int y = convertRowToY(convertIndexToRow(index));

        return ((y == -1) || (x == -1))
               ? null
               : new java.awt.Point(x, y);
    }

    /**
     * get the Cell Bounds for two selected items
     *
     *
     * @param list the palette list
     * @param index1 the first item index
     * @param index2 the second item index
     *
     * @return the cell bounds of the two items
     *
     */
    public Rectangle getCellBounds(JList list, int index1, int index2) {

        maybeUpdateLayoutState();

        int minIndex = Math.min(index1, index2);
        int maxIndex = Math.max(index1, index2);
        int minY     = convertRowToY(convertIndexToRow(minIndex));
        int maxY     = convertRowToY(convertIndexToRow(maxIndex));
        int minX     = convertColumnToX(convertIndexToColumn(minIndex));
        int maxX     = convertColumnToX(convertIndexToColumn(maxIndex));

        if ((minY == -1) || (maxY == -1) || (minX == -1) || (maxX == -1)) {
            return null;
        }

        java.awt.Insets insets = list.getInsets();
        int    x      = minX;
        int    y      = minY;
        int    w      =
            (maxX + getColumnWidth(convertIndexToColumn(maxIndex))) - minX;
        int    h      = (maxY + getRowHeight(convertIndexToRow(maxIndex)))
                        - minY;

        return new Rectangle(x, y, w, h);
    }

    /**
     * get the Row Height of a selected row
     *
     *
     * @param row the selected row index
     *
     * @return the row height
     *
     */
    protected int getRowHeight(int row) {

        if ((row < 0) || (row >= mRowCount)) {
            return -1;
        }

        return cellHeight;
    }

    /**
     * get the Column Width of a selected column
     *
     *
     * @param column the selected column
     *
     * @return the column width
     *
     */
    protected int getColumnWidth(int column) {

        if ((column < 0) || (column >= mColumnCount)) {
            return -1;
        }

        return cellWidth;
    }

    /**
     * convert an item Index To its Row number
     *
     *
     * @param index the item index
     *
     * @return row number
     *
     */
    protected int convertIndexToRow(int index) {

        if ((index < 0) || (index >= list.getModel().getSize())) {
            return -1;
        }

        if (((PaletteList) list).getAlignStyle()
                == PaletteList.VERTICAL_ALIGN_STYLE) {
            return (mRowCount <= 0)
                   ? -1
                   : index % mRowCount;
        } else {
            return (mColumnCount <= 0)
                   ? -1
                   : index / mColumnCount;
        }
    }

    /**
     * convert an item Index To its Column number
     *
     *
     * @param index the item index
     *
     * @return the column number
     *
     */
    protected int convertIndexToColumn(int index) {

        if ((index < 0) || (index >= list.getModel().getSize())) {
            return -1;
        }

        if (((PaletteList) list).getAlignStyle()
                == PaletteList.VERTICAL_ALIGN_STYLE) {
            return (mRowCount <= 0)
                   ? -1
                   : index / mRowCount;
        } else {
            return (mColumnCount <= 0)
                   ? -1
                   : index % mColumnCount;
        }
    }

    /**
     * convert a Cell To its Index
     *
     *
     * @param column the cell column number
     * @param row the cell column number
     *
     * @return the list index
     *
     */
    protected int convertCellToIndex(int column, int row) {

        if ((column < 0) || (column >= mColumnCount) || (row < 0)
                || (row >= mRowCount)) {
            return -1;
        }

        int index = (((PaletteList) list).getAlignStyle()
                     == PaletteList.VERTICAL_ALIGN_STYLE)
                    ? column * mRowCount + row
                    : row * mColumnCount + column;

        return (index < list.getModel().getSize())
               ? index
               : -1;
    }

    /**
     * convert a screen Y coordinate To the Row number
     *
     *
     * @param y0 the Y coordinate
     *
     * @return the row number
     *
     */
    protected int convertYToRow(int y0) {

        if (y0 < 0) {
            return -1;
        }

        int row = (cellHeight == 0)
                  ? -1
                  : (y0 - list.getInsets().top) / cellHeight;

        return ((row >= 0) && (row < mRowCount))
               ? row
               : -1;
    }

    /**
     * convert a screen X coordinate To the Column number
     *
     *
     * @param x0 the X coordinate
     *
     * @return the column number
     *
     */
    protected int convertXToColumn(int x0) {

        if (x0 < 0) {
            return -1;
        }

        int column = (cellWidth == 0)
                     ? -1
                     : (x0 - list.getInsets().left) / cellWidth;

        return ((column >= 0) && (column < mColumnCount))
               ? column
               : -1;
    }

    /**
     * convert a Row index To the screen Y coordinate
     *
     *
     * @param row the row index
     *
     * @return the screen Y coordinate
     *
     */
    protected int convertRowToY(int row) {

        if ((row < 0) || (row >= mRowCount)) {
            return -1;
        }

        return list.getInsets().top + row * cellHeight;
    }

    /**
     * converta Column index To the screen X coordinate
     *
     *
     * @param column the column index
     *
     * @return the screen X coordinate
     *
     */
    protected int convertColumnToX(int column) {

        if ((column < 0) || (column >= mColumnCount)) {
            return -1;
        }

        return list.getInsets().left + column * cellWidth;
    }

    /**
     * is the x screen location On a CheckBox
     *
     *
     * @param x0 the screen location
     *
     * @return true if it is on a check box
     *
     */
    protected boolean isOnCheckBox(int x0) {

        if (x0 < 0) {
            return false;
        }

        int column = (cellWidth == 0)
                     ? -1
                     : (x0 - list.getInsets().left) % cellWidth;

        return ((column >= 0) && (column < mCheckBoxSize))
               ? true
               : false;
    }

    /**
     * maybe Update Layout State, it will update the layout if the
     * dimension has been changed.
     *
     *
     */
    protected void maybeUpdateLayoutState() {

        int newDimension = (((PaletteList) list).getAlignStyle()
                            == PaletteList.VERTICAL_ALIGN_STYLE)
                           ? list.getHeight()
                           : list.getWidth();

        if (mLastDimension != newDimension) {
            mLastDimension = newDimension;

            updateLayoutState();
        } else {
            super.maybeUpdateLayoutState();
        }
    }

    /**
     * update Layout State
     *
     *
     */
    protected void updateLayoutState() {

        int fixedCellHeight = list.getFixedCellHeight();
        int fixedCellWidth  = list.getFixedCellWidth();

        cellWidth  = (fixedCellWidth != -1)
                     ? fixedCellWidth
                     : -1;
        cellHeight = (fixedCellHeight != -1)
                     ? fixedCellHeight
                     : -1;

        if (fixedCellHeight != -1) {
            cellHeight = fixedCellHeight;
        } else {
            cellHeight = -1;
        }

        if (fixedCellWidth != -1) {
            cellHeight = fixedCellWidth;
        } else {
            cellHeight = -1;
        }

        if ((fixedCellWidth == -1) || (fixedCellHeight == -1)) {
            javax.swing.ListModel        dataModel     = list.getModel();
            int              dataModelSize = dataModel.getSize();
            javax.swing.ListCellRenderer renderer      = list.getCellRenderer();

            if (renderer != null) {
                for (int index = 0; index < dataModelSize; index++) {
                    Object    value = dataModel.getElementAt(index);
                    java.awt.Component c     =
                        renderer.getListCellRendererComponent(list, value,
                                                              index, false,
                                                              false);

                    rendererPane.add(c);

                    Dimension cellSize = c.getPreferredSize();

                    if (fixedCellWidth == -1) {
                        cellWidth = Math.max(cellSize.width, cellWidth);
                    }

                    if (fixedCellHeight == -1) {
                        cellHeight = Math.max(cellSize.height, cellHeight);
                    }
                }
            } else {
                if (cellWidth == -1) {
                    cellWidth = 0;
                }

                if (cellHeight == -1) {
                    cellHeight = 0;
                }
            }
        }

        if (updateAlignment()) {
            list.revalidate();
        } else {
            list.invalidate();
        }
    }

    /**
     * update the Alignment of palette list
     *
     *
     * @return true if alignment changes required
     *
     */
    private boolean updateAlignment() {

        int    size   = list.getModel().getSize();
        java.awt.Insets insets = list.getInsets();

        if (((PaletteList) list).getAlignStyle()
                == PaletteList.VERTICAL_ALIGN_STYLE) {
            int listHeight = list.getHeight() - (insets.top + insets.bottom);

            if (cellHeight <= 0) {
                mRowCount = 0;
            } else if (listHeight < cellHeight) {
                mRowCount = 1;
            } else {
                mRowCount = listHeight / cellHeight;
            }

            mRowCount = Math.min(size, mRowCount);

            int oldColumnCount = mColumnCount;

            mColumnCount = (mRowCount == 0)
                          ? 0
                          : (size + mRowCount - 1) / mRowCount;

            return oldColumnCount != mColumnCount;
        } else {
            int listWidth = list.getWidth() - (insets.left + insets.right);

            if (cellWidth <= 0) {
                mColumnCount = 0;
            } else if (listWidth < cellWidth) {
                mColumnCount = 1;
            } else {
                mColumnCount = listWidth / cellWidth;
            }

            mColumnCount = Math.min(size, mColumnCount);

            int oldRowCount = mRowCount;

            mRowCount = (mColumnCount == 0)
                       ? 0
                       : (size + mColumnCount - 1) / mColumnCount;

            return oldRowCount != mRowCount;
        }
    }

    /**
     * This is a listener class used for handling Mouse events
     * on the palette list
     *
     */
    public class AlignedListMouseInputHandler
            extends javax.swing.plaf.basic.BasicListUI.MouseInputHandler {

        /**
         * handle a mouse Pressed event. If an item is selected, set the
         * ValueIsAdjusting flag to true.
         *
         *
         * @param e a mouse event
         *
         */
        public void mousePressed(MouseEvent e) {

            if (!javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                return;
            }

            if (!list.isEnabled()) {
                return;
            }

            if (!list.hasFocus()) {
                list.requestFocus();
            }

            int index = convertCellToIndex(convertXToColumn(e.getX()),
                                           convertYToRow(e.getY()));
            if (index != -1) {
                list.setValueIsAdjusting(true);
            }
        }

        /**
         * handle a mouse Dragged event
         *
         *
         * @param e a mouse event
         *
         */
        public void mouseDragged(MouseEvent e) {
        }

        /**
         * handle a mouse Moved event
         *
         *
         * @param e a mouse event
         *
         */
        public void mouseMoved(MouseEvent e) {
            mouseEntered(e);
        }

        /**
         * handle a mouse Entered event
         *
         *
         * @param e a mouse event
         *
         */
        public void mouseEntered(MouseEvent e) {

            if (!list.isEnabled()) {
                return;
            }

            ((PaletteList) list).setRolloverIndex(locationToIndex(list,
                    e.getPoint()));
        }

        /**
         * handle a mouse Exited event
         *
         *
         * @param e a mouse event
         *
         */
        public void mouseExited(MouseEvent e) {

            if (!list.isEnabled()) {
                return;
            }

            ((PaletteList) list).setRolloverIndex(-1);
        }

        /**
         * handle a mouse Released event. Update the selected item status
         * according to the selection state
         *
         * @param e a mouse event
         *
         */
        public void mouseReleased(MouseEvent e) {

            if (!javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                return;
            }

            int index = convertCellToIndex(convertXToColumn(e.getX()),
                                           convertYToRow(e.getY()));
            if (index < 0) {
                return;
            }
            if (isOnCheckBox(e.getX())) {
                /*
                if (list.isSelectedIndex(index)) {
                    list.removeSelectionInterval(index, index);
                } else {
                    list.addSelectionInterval(index, index);
                }
                 */
                ((PaletteList) list).setCheckedItem(index);
            } else {

                // highlite the selected button...
                int last = ((PaletteList) list).getCurSelectedIndex();

                ((PaletteList) list).setCurSelectedIndex(index);

                if (last != index) {
                    repaintList(last, last);
                }

                repaintList(index, index);
            }

            list.setValueIsAdjusting(false);
        }
    }

    /**
     * create a Mouse Input Listener class
     *
     *
     * @return  a mouse input listener
     *
     */
    protected javax.swing.event.MouseInputListener createMouseInputListener() {
        return new AlignedListMouseInputHandler();
    }

    /**
     * This class handles the selection events of the palette list
     *
     */
    public class AlignedListSelectionHandler implements ListSelectionListener {

        /**
         * Handle a value Changed selection event. Update the selection status
         * if needed.
         * 
         * 
         * @param e
         *            a list selection event
         *  
         */
        public void valueChanged(javax.swing.event.ListSelectionEvent e) {
            maybeUpdateLayoutState();
            repaintList(e.getFirstIndex(), e.getLastIndex());
        }
    }

    /**
     * create a List Selection Listener to handle selection events
     * 
     * 
     * @return a list selection listener
     *  
     */
    protected ListSelectionListener createListSelectionListener() {
        return new AlignedListSelectionHandler();
    }

    /**
     * redraw the palette List
     *
     *
     */
    private void redrawList() {
        list.revalidate();
        list.repaint();
    }

    /**
     * This class handles data events of the palette list
     *
     */
    public class AlignedListDataHandler
            extends javax.swing.plaf.basic.BasicListUI.ListDataHandler {

        /**
         * handle the addittion of an interval to the the palette list.
         *
         *
         * @param e the list data event
         *
         */
            public void intervalAdded(javax.swing.event.ListDataEvent e) {

            updateLayoutStateNeeded = modelChanged;

            int                minIndex = Math.min(e.getIndex0(),
                                                   e.getIndex1());
            int                maxIndex = Math.max(e.getIndex0(),
                                                   e.getIndex1());
            javax.swing.ListSelectionModel sm       = list.getSelectionModel();

            if (sm != null) {
                sm.insertIndexInterval(minIndex, maxIndex - minIndex, true);
            }

            repaintList(minIndex, list.getModel().getSize() - 1);
        }

        /**
         * Handle the removal of an interval from the palette list
         *
         *
         * @param e the data list event
         *
         */
            public void intervalRemoved(javax.swing.event.ListDataEvent e) {

            updateLayoutStateNeeded = modelChanged;

            javax.swing.ListSelectionModel sm = list.getSelectionModel();

            if (sm != null) {
                sm.removeIndexInterval(e.getIndex0(), e.getIndex1());
            }

            repaintList(Math.min(e.getIndex0(), e.getIndex1()),
                        list.getModel().getSize() - 1);
        }
    }

    /**
     * create a new List Data Listener to handle list data events
     *
     *
     * @return a new list data listener
     *
     */
    protected javax.swing.event.ListDataListener createListDataListener() {
        return new AlignedListDataHandler();
    }

    /**
     * repaint the palette List for a selected interval
     *
     *
     * @param minIndex from index
     * @param maxIndex end index
     *
     */
    private void repaintList(int minIndex, int maxIndex) {

        int x = 0, y = 0, w = 0, h = 0;

        minIndex = (minIndex == -1)
                   ? 0
                   : minIndex;
        maxIndex = (maxIndex == -1)
                   ? list.getModel().getSize() - 1
                   : maxIndex;

        if (((PaletteList) list).getAlignStyle()
                == PaletteList.VERTICAL_ALIGN_STYLE) {
            x = Math.max(0, convertColumnToX(convertIndexToColumn(minIndex)));

            int maxColumn = convertIndexToColumn(maxIndex);

            w = Math
                .min(list.getWidth(),
                     convertColumnToX(maxColumn)
                     + getColumnWidth(maxColumn)) - x;
            h = list.getHeight();
        } else {
            y = Math.max(0, convertRowToY(convertIndexToRow(minIndex)));

            int maxRow = convertIndexToRow(maxIndex);

            h = Math
                .min(list.getHeight(),
                     convertRowToY(maxRow) + getRowHeight(maxRow)) - y;
            w = list.getWidth();
        }

        list.revalidate();
        list.repaint(x, y, w, h);
    }

    /**
     * This class handles the property change events of the palette list
     *
     *
     */
    public class AlignedListPropertyChangeHandler
            extends javax.swing.plaf.basic.BasicListUI.PropertyChangeHandler {

        /**
         * handle a property Change event.
         *
         *
         * @param e a property change event
         *
         */
            public void propertyChange(java.beans.PropertyChangeEvent e) {

            String propertyName = e.getPropertyName();

            if (propertyName.equals("rolloverIndex")) {        // NOI18N
                int index0 = ((Integer) e.getNewValue()).intValue();
                int index1 = ((Integer) e.getOldValue()).intValue();

                repaintList(Math.min(index0, index1),
                            Math.max(index0, index1));
            } else if (propertyName.equals("alignStyle")) {    // NOI18N
                redrawList();
            } else {
                super.propertyChange(e);
            }
        }
    }

    /**
     * create a new Property Change Listener to handle property change events
     *
     *
     * @return a new property change listener
     *
     */
    protected java.beans.PropertyChangeListener createPropertyChangeListener() {
        return new AlignedListPropertyChangeHandler();
    }
}

