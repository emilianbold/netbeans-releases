/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.ui.graph.IHighlightConfigurator;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDrawable;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ColumnArea extends CanvasArea {
    protected ArrayList cells;
    private JGoRectangle columnRect;
    private int vSpacing = 1;
    private JGoPen linePen = JGoPen.makeStockPen(Color.WHITE);
    
    // Constants for first, last visible row
    private int firstVisibleRow = 0;
    private int lastVisibleRow = 0;
    
    private int preferredWidth = -1;
    
    private int tableType = -1;
    
    private int textAlignment = JGoText.ALIGN_CENTER;
    
    /** Creates a new instance of ColumnArea.
     * @param tableType
     */
    public ColumnArea(int tableType) {
        super();
        cells = new ArrayList();
        
        columnRect = new JGoRectangle();
        columnRect.setSelectable(false);
        columnRect.setPen(JGoPen.makeStockPen(Color.WHITE));
        this.tableType = tableType;
        //columnRect.setBrush(JGoBrush.makeStockBrush(IHighlightConfigurator.DEFAULT_BASIC_COLOR));
        if(tableType == TableConstants.OUTPUT_TABLE || tableType == TableConstants.NO_PORT_TABLE){
            columnRect.setBrush(JGoBrush.makeStockBrush(IHighlightConfigurator.DEFAULT_TARGET_COLOR));
        }else{
            columnRect.setBrush(JGoBrush.makeStockBrush(IHighlightConfigurator.DEFAULT_BASIC_COLOR));
        }
        addObjectAtHead(columnRect);
        
        this.setSelectable(false);
        this.setResizable(false);
        
        //set the insets around column
        this.insets = TableConstants.COLUMN_INSETS;
    }
    
    /**
     * Gets the bounding graph rectangle.
     *
     * @return graph rectangle
     */
    public JGoObject getRect() {
        return columnRect;
    }
    
    /**
     * Gets the number of rows in this column area.
     *
     * @return number of rows in this column
     */
    public int getRowCount() {
        if (cells == null) {
            return -1;
        }
        return cells.size();
    }
    
    /**
     * Adds a new cell in the column area with the given display name.
     *
     * @param row row in which to add a new cell
     * @param val value for the cell at <code>row</code>
     * @param toolTip tool tip to display for the cell
     */
    public void addItem(int row, String val, String toolTip) {
        BasicCellArea cell = new BasicCellArea(val);
        if (row == 0 || row == 1) {
            cell = new BasicCellArea(val);
        }
        
        cell.setToolTipText(toolTip);
        add(row, cell);
    }
    
    /**
     * Adds a new cell in the column area, using the given SQLDBColumn as a data object
     *
     * @param row row in which to add a new cell
     * @param data SQLDBColumn containing data for the cell at <code>row</code>
     * @param toolTip tool tip to display for the cell
     */
    public void addItem(int row, SQLDBColumn data, String toolTip) {
        BasicCellArea cell = new BasicCellArea(data.getName());
        cell.setToolTipText(toolTip);
        cell.setDataObject(data);
        
        add(row, cell);
    }
    
    private void add(int row, BasicCellArea cell) {
        cell.setTextAlignment(this.textAlignment);
        cell.setLocation(this.getLeft(), this.getTop() + this.getMaximumHeight());
        
        cell.setLinePen(JGoPen.makeStockPen(Color.WHITE));
        if(tableType == TableConstants.OUTPUT_TABLE || tableType == TableConstants.NO_PORT_TABLE){
            cell.setBrush(JGoBrush.makeStockBrush(IHighlightConfigurator.DEFAULT_TARGET_COLOR));
        }else{
            cell.setBrush(JGoBrush.makeStockBrush(IHighlightConfigurator.DEFAULT_BASIC_COLOR));
        } 
        cells.add(row, cell);
        this.addObjectAtTail(cell);
    }
    
    /**
     * Removes cell at given row.
     *
     * @param row row whose cell should be removed
     */
    public void removeItem(int row) {
        if (row < 0 || row >= cells.size()) {
            throw new IllegalArgumentException("Can not remove cell, specified row " + row + " does not exist.");
        }
        BasicCellArea cell = (BasicCellArea) cells.get(row);
        this.removeObject(cell);
        cells.remove(row);
    }
    
    /**
     * Gets the cell at an index.
     *
     * @param row row to get cell from
     * @return cell area
     */
    public BasicCellArea getCellAt(int row) {
        if (row <= cells.size() - 1) {
            return (BasicCellArea) cells.get(row);
        }
        
        return null;
    }
    
    /**
     * Sets the flag if this area updates scrollbar of its parent.
     *
     * @param manageScrollBar boolean
     */
    public void setManageHorizontalScrollBar(boolean manageScrollBar) {
        //this.manageHScrollBar = manageScrollBar;
    }
    
    /**
     * Sets the preferred width of this area.
     *
     * @param width preferred width
     */
    public void setPreferredWidth(int width) {
        this.preferredWidth = width;
    }
    
    /**
     * Gets the preferred width of this area.
     *
     * @return preferred width
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }
    
    /**
     * Gets the index of first visible row index.
     *
     * @return index of first visible row index
     */
    public int getFirstVisibleRow() {
        return firstVisibleRow;
    }
    
    /**
     * Sets the first visible row index.
     *
     * @param rowIdx first visible row index
     */
    public void setFirstVisibleRow(int rowIdx) {
        int oldIndex = firstVisibleRow;
        
        if (rowIdx >= 0 && rowIdx <= getRowCount() && oldIndex != rowIdx) {
            firstVisibleRow = rowIdx;
            layoutChildren();
        }
    }
    
    /**
     * Gets the index of last visible row in this area.
     *
     * @return last visible row index
     */
    public int getLastVisibleRow() {
        return lastVisibleRow;
    }
    
    /**
     * Gets the maximum cell width of a cell in this area.
     *
     * @return maximum cell width
     */
    public int getMaximizeCellWidth() {
        Iterator it = cells.iterator();
        //this is the width of widest cell
        int w = 0;
        
        while (it.hasNext()) {
            BasicCellArea cell = (BasicCellArea) it.next();
            //need to include the insets of cell also in the width
            int width = cell.getMaximumWidth();
            if (width > w) {
                w = width;
            }
        }
        
        return w;
    }
    
    /**
     * Gets the maximum cell height of a cell in this area.
     *
     * @return maximum cell height
     */
    public int getMaximizeCellHeight() {
        Iterator it = cells.iterator();
        //this is the height of widest cell
        int h = 0;
        
        while (it.hasNext()) {
            BasicCellArea cell = (BasicCellArea) it.next();
            
            int height = cell.getHeight();
            if (height > h) {
                h = height;
            }
        }
        
        return h;
    }
    
    /**
     * Gets the height of a cell at a given row index.
     *
     * @param row row
     * @return cell height
     */
    public int getCellHeight(int row) {
        int h = -1;
        if (cells.size() > row) {
            BasicCellArea cell = (BasicCellArea) cells.get(row);
            return cell.getHeight();
        }
        
        return h;
    }
    
    /**
     * Gets the height of all visible rows in this column.
     *
     * @return height of visible rows
     */
    public int getVisibleRowHeights() {
        int rowHeights = 0;
        //add insets
        for (int i = this.getFirstVisibleRow(); i <= this.getLastVisibleRow(); i++) {
            rowHeights += getCellHeight(i) + getVerticalSpacing();
        }
        rowHeights += insets.top + insets.bottom - getVerticalSpacing();
        
        return rowHeights;
    }
    
    /**
     * Gets maximum height of this area.
     *
     * @return maximum height
     */
    public int getMaximumHeight() {
        Iterator it = cells.iterator();
        //this is the height of widest cell
        int h = 0;
        
        while (it.hasNext()) {
            BasicCellArea cell = (BasicCellArea) it.next();
            
            int height = cell.getHeight();
            h += height + vSpacing;
        }
        // add insets
        h += insets.top + insets.bottom - vSpacing;
        return h;
    }
    
    /**
     * Gets minimum height of this area.
     *
     * @return minimum height
     */
    public int getMinimumHeight() {
        // show at least one cell
        return getMaximumHeight();
    }
    
    /**
     * Gets the maximum width of this area.
     *
     * @return maximum width
     */
    public int getMaximumWidth() {
        return getMaximizeCellWidth() + insets.left + insets.right;
    }
    
    /**
     * Gets the minimum width of this area.
     *
     * @return minimum width
     */
    public int getMinimumWidth() {
        int minWidth = getInsets().left + getInsets().right;
        
        //account for ... in each cell of this column
        minWidth += 20;
        return minWidth;
    }
    
    /**
     * Calculates the minimum size for the columnRect, as determined by the maximum item
     * size plus insets.
     *
     * @return min size
     */
    public Dimension getMinimumRectSize() {
        int maxW = getMaximizeCellWidth();
        int maxH = getMaximizeCellHeight();
        
        // now account for insets on all sides
        Insets insets1 = getInsets();
        
        int minw = maxW + insets1.left + insets1.right;
        int minh = maxH + insets1.top + insets1.bottom;
        return new Dimension(minw, minh);
    }
    
    /**
     * Gets the minimum size of this area.
     *
     * @return minimum size
     */
    public Dimension getMinimumSize() {
        // first account for the minimum ListAreaRect size
        Dimension minRect = getMinimumRectSize();
        return minRect;
    }
    
    /**
     * Gets the vertical spacing between cells in this column area.
     *
     * @return vertical spacing
     */
    public int getVerticalSpacing() {
        return vSpacing;
    }
    
    /**
     * Sets the vertical spacing between cells in this column area.
     *
     * @param newspace new space
     */
    public void setVerticalSpacing(int newspace) {
        int oldSpacing = vSpacing;
        if (oldSpacing != newspace) {
            vSpacing = newspace;
            layoutChildren();
        }
    }
    
    /**
     * Gets the line pen.
     *
     * @return line pen
     */
    public JGoPen getLinePen() {
        return linePen;
    }
    
    /**
     * Sets the line pen for border.
     *
     * @param pen pen
     */
    public void setLinePen(JGoPen pen) {
        JGoPen oldPen = linePen;
        if (oldPen != pen) {
            linePen = pen;
            layoutChildren();
        }
    }

    /**
     * Paints this area.
     * 
     * @param g Graphics2D
     * @param view view
     */
    public void paint(Graphics2D g, JGoView view) {
        super.paint(g, view);

        int penwidth = 0;
        if (getLinePen() != null) {
            penwidth = getLinePen().getWidth();
        }
        if (penwidth == 0) {
            return;
        }
        JGoObject r = getRect();
        if (r == null) {
            return; // not yet initialized
        }
        Insets insets1 = getInsets();

        int rectleft = r.getLeft();
        int recttop = r.getTop();
        int rectwidth = r.getWidth();
        int rectheight = r.getHeight();

        int top = recttop + insets1.top;
        int height = rectheight - insets1.top - insets1.bottom;

        int limit = 0;

        limit = height;

        int s = 0; // height/width of visible items so far
        for (int i = getFirstVisibleRow(); i < getLastVisibleRow(); i++) {
            BasicCellArea cell = (BasicCellArea) cells.get(i);
            int h = cell.getHeight();
            s += h;
            int sep = Math.max(penwidth, getVerticalSpacing());
            if (s + sep <= limit) {
                JGoDrawable.drawLine(g, getLinePen(), rectleft, top + s + sep / 2, rectleft + rectwidth, top + s + sep / 2);
            }
            s += sep;
        }

    }

    /**
     * Overrides parent to handle the changes in the geometry of this area. We will lay
     * out all the cell again.
     * 
     * @param prevRect previous rectangle bounds
     */
    protected void geometryChange(Rectangle prevRect) {
        // handle any size changes by repositioning all the items
        if (prevRect.width != getWidth() || prevRect.height != getHeight()) {
            // first set the new geometry for the rectangle
            if (getRect() != null) {
                Rectangle thisRect = getBoundingRect();
                getRect().setBoundingRect(thisRect);
            }

            // then we can lay out all the other parts
            layoutChildren();

        } else {
            super.geometryChange(prevRect);
        }

    }

    /**
     * Lays out all of this children of this column area.
     */
    public void layoutChildren() {
        JGoObject r = getRect();
        if (r == null) {
            return;
        } // not yet initialized

        //get the bounding rectangle of this column area
        int x = r.getLeft() + insets.left;
        int y = r.getTop() + insets.top;
        int width = r.getWidth() - insets.left - insets.right;
        int height = r.getHeight() - insets.top - insets.bottom;

        // remember last visible row index
        lastVisibleRow = getFirstVisibleRow();

        int cellWidth = width;

        // calculate the top of next cell
        int nextCellDeltaTop = 0;

        Iterator it = cells.iterator();

        // row count
        int cnt = 0;

        while (it.hasNext()) {
            BasicCellArea cell = (BasicCellArea) it.next();

            if (cnt < getFirstVisibleRow()) {
                cell.setVisible(false);

                cnt++;
                continue;
            }

            //if cell is going out of the height of this area then we
            //mark it invisible
            if (nextCellDeltaTop + cell.getHeight() > height) {
                cell.setVisible(false);

            } else {
                cell.setVisible(true);
                lastVisibleRow = cnt;
                cell.setBoundingRect(x, y + nextCellDeltaTop, cellWidth, cell.getHeight());
            }

            //calcualte the top for next cell
            nextCellDeltaTop += cell.getHeight() + getVerticalSpacing();

            cnt++;
        }
    }

    /**
     * Sets the alignment of text in this column area to the given characteristic.
     * 
     * @param align desired alignment characteristic; one of
     *        <UL>
     *        <LI>JGoText.ALIGN_LEFT
     *        <LI>JGoText.ALIGN_CENTER
     *        <LI>JGoText.ALIGN_RIGHT
     *        </UL>
     */
    public void setTextAlignment(int align) {
        this.textAlignment = align;
        if (cells != null) {
            Iterator it = cells.iterator();
            while (it.hasNext()) {
                BasicCellArea cell = (BasicCellArea) it.next();
                cell.setTextAlignment(align);
            }
        }
    }
}

