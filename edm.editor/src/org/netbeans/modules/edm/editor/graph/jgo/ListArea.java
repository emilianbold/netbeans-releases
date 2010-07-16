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

package org.netbeans.modules.edm.editor.graph.jgo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.netbeans.modules.edm.editor.graph.jgo.ListAreaCellRenderer;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDrawable;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoScrollBar;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ListArea extends CanvasArea {

    private static final JGoPen DEFAULT_PEN = JGoPen.makeStockPen(Color.lightGray);
    
    private ListModel dataModel;

    private ListAreaCellRenderer cellRenderer;

    private ListDataAdapter listDataAdapter;

    private ArrayList cells;

    private JGoRectangle columnRect;

    private int vSpacing = 1;
    
    private JGoPen linePen = null;

    //constants for first , last visible row
    //by default set to 0 so that first row is visible
    private int firstVisibleRow = 0;
    private int lastVisibleRow = -1;
    //vertical scrollbar
    private JGoScrollBar vScrollBar;
    private int vScrollBarGap = 0;

    //this is the size of the scrollbar
    private static int scrollBarSize = 14;
    private int prefferedWidth = -1;

    private ArrayList areaList = new ArrayList();

    private boolean drawBoundingRect = true;

    private boolean drawLines = true;

    private boolean showScrollBar = true;

    /** Creates a new instance of BasicListArea */
    public ListArea() {

        listDataAdapter = new ListDataAdapter();
        if (drawBoundingRect) {
            columnRect = new JGoRectangle();
            columnRect.setSelectable(false);
            columnRect.setResizable(false);
            columnRect.setPen(JGoPen.lightGray);
            columnRect.setBrush(JGoBrush.makeStockBrush(Color.WHITE));
            addObjectAtTail(columnRect);
        }
        //add scrollbar
        vScrollBar = new JGoScrollBar();
        vScrollBar.setVertical(true);
        vScrollBar.setSelectable(false);
        addObjectAtTail(vScrollBar);

        this.setSelectable(false);
        this.setResizable(false);
        this.setDraggable(true);

        //set the insets around column
        //this.insets = new Insets(1, 5, 0, 10);

        // have a default cell renderer
        cellRenderer = new DefaultListAreaRenderer();

    }

    /**
     * create an instance of list area
     * 
     * @param listData list data values
     */
    public ListArea(Object[] listData) {
        this();

        DefaultListModel model = new DefaultListModel();

        for (int i = 0; i < listData.length; i++) {
            model.addElement(listData[i]);
        }
        setModel(model);

    }

    /**
     * set the list model
     * 
     * @param model list model
     */
    public void setModel(ListModel model) {
        ListModel oldModel = dataModel;
        this.dataModel = model;
        if (oldModel != null) {
            oldModel.removeListDataListener(listDataAdapter);
        }
        dataModel.addListDataListener(listDataAdapter);

        //by setting the model we need to also set the renderer for objects
        //in the model
        for (int i = 0; i < dataModel.getSize(); i++) {
            Object val = dataModel.getElementAt(i);
            ListAreaCellRenderer listCellRenderer = getCellRenderer(i);
            JGoObject cell = listCellRenderer.getListAreaCellRenderer(this, val, i, false, false);

            areaList.add(cell);
            this.addObjectAtTail(cell);
        }

    }

    /**
     * get the list model
     * 
     * @return list model
     */
    public ListModel getModel() {
        return dataModel;
    }

    /**
     * set the default cell renderer
     * 
     * @param cellRenderer cell renderer
     */
    public void setCellRenderer(ListAreaCellRenderer cellRenderer) {
        this.cellRenderer = cellRenderer;
    }

    /**
     * get the default cell renderer
     * 
     * @return cell renderer
     */
    public ListAreaCellRenderer getCellRenderer() {
        return cellRenderer;
    }

    /**
     * get cell renderer at an index
     * 
     * @param row row index
     * @return cell renderer
     */
    protected ListAreaCellRenderer getCellRenderer(int row) {
        return getCellRenderer();
    }

    /**
     * get the cell renderer component at an index
     * 
     * @param row row index
     * @return cell renderer
     */
    public JGoObject getCellRendererComponent(int row) {
        if ((row >= areaList.size()) || row < 0) {
            return null;
        }
        return (JGoObject) areaList.get(row);
    }

    /**
     * set whether to draw lines after each cell in this list
     * 
     * @param drawLines whether to draw lines
     */
    public void setDrawLines(boolean drawLines) {
        this.drawLines = drawLines;
    }

    /**
     * get whether list draws line after each cell
     * 
     * @return drawlines
     */
    public boolean isDrawLines() {
        return this.drawLines;
    }

    /**
     * get value at a particular point
     * 
     * @param loc location
     * @return cell value
     */
    public Object getValueAt(Point loc) {
        ListModel model = getModel();

        if (model == null) {
            return null;
        }

        for (int i = 0; i < model.getSize(); i++) {
            Object val = model.getElementAt(i);
            JGoObject renderer = getCellRendererComponent(i);
            if (renderer != null && renderer.getBoundingRect().contains(loc)) {
                return val;
            }
        }

        return null;
    }

    /**
     * get the vertical scrollbar
     * 
     * @return vertical scroll bar
     */
    public JGoScrollBar getVerticalScrollBar() {
        return vScrollBar;
    }

    /**
     * set the gap of vertical scrollbar from the edge of this area
     * 
     * @param gap gap
     */
    public void setVerticalScrollBarGapFromEdge(int gap) {
        vScrollBarGap = gap;
    }

    public void setShowScrollBar(boolean showScrollBar) {
        this.showScrollBar = showScrollBar;
    }

    public boolean isShowScrollBar() {
        return this.showScrollBar;
    }

    /**
     * since setVisible() doesn't automatically call setVisible() on all the children, we
     * need to do this manually to handle the scroll bar
     * 
     * @param bFlag boolean
     */
    public void setVisible(boolean bFlag) {
        super.setVisible(bFlag);
        if (getVerticalScrollBar() != null && isShowScrollBar()) {
            getVerticalScrollBar().setVisible(bFlag);
            if (bFlag) {
                this.addObjectAtTail(getVerticalScrollBar());
            } else {
                this.removeObject(getVerticalScrollBar());
            }
        }
    }

    /**
     * updates the vertical scroll bar
     */
    public void updateVerticalScrollBar() {
        JGoScrollBar bar = getVerticalScrollBar();
        if (bar != null) {

            if (!this.isShowScrollBar()) {
                bar.setVisible(false);
                return;
            }

            if (getFirstVisibleRow() == 0 && getLastVisibleRow() == getRowCount() - 1) {
                bar.setVisible(false);
                scrollBarSize = 0;
            } else {
                bar.setVisible(true);
                bar.setValues(getFirstVisibleRow(), getLastVisibleRow() - getFirstVisibleRow() + 1, 0, getRowCount(), 1, Math.max(getLastVisibleRow()
                    - getFirstVisibleRow(), 1));

                scrollBarSize = 14;
                //set the height so that only visible rows can be dispalced
                //this avoids extra space at the end of scroll bar
                //any parent should listen to change in child geometry
                if (this.getParent() instanceof BasicListArea) {
                    ((BasicListArea) this.getParent()).adjustHeight(this);
                }
            }
        }
    }

    /**
     * this gets the notification from the JGoScrollBar when the scroll bar value,
     * representing the first visible index, has changed
     * 
     * @param hint event hint
     * @param prevInt previous integer value
     * @param prevVal previous object value
     */
    public void update(int hint, int prevInt, Object prevVal) {
        if (hint == JGoScrollBar.ChangedScrollBarValue) {
            if (getVerticalScrollBar() != null) {
                // optimization: assume area doesn't change when scrolling items
                setFirstVisibleRow(getVerticalScrollBar().getValue());
            }
        } else {
            super.update(hint, prevInt, prevVal);
        }
    }

    /**
     * get the graph rectangle
     * 
     * @return graph rectangle
     */
    public JGoObject getRect() {
        return columnRect;
    }

    /**
     * get the number of rows in this column area
     * 
     * @return number of rows in this column
     */
    public int getRowCount() {
        if (dataModel == null) {
            return -1;
        }
        return dataModel.getSize();
    }

    /**
     * add a new cell in the column area int val value to represent in a cell
     * 
     * @param row row
     * @param val value
     */
    public void addItem(int row, Object val) {
    }

    /**
     * get the cell at an index
     * 
     * @param row row
     * @return cell area
     */
    public CellArea getCellAt(int row) {
        if (row <= cells.size() - 1) {
            return (CellArea) cells.get(row);
        }

        return null;
    }

    /**
     * set the preffered width
     * 
     * @param width preffered width
     */
    public void setPrefferedWidth(int width) {
        this.prefferedWidth = width;
    }

    /**
     * get the preffered width
     * 
     * @return preffered width
     */
    public int getPrefferedWidth() {
        return prefferedWidth;
    }

    /**
     * get maximum width of this area
     * 
     * @return max width
     */
    public int getMaximumWidth() {
        int maxWidth = getInsets().left + getInsets().right;

        int w = 0;

        for (int i = 0; i < getModel().getSize(); i++) {
            JGoObject renderer = getCellRendererComponent(i);
            if (renderer != null) {
                int rendererWidth = renderer.getWidth();
                if (w < rendererWidth) {
                    w = rendererWidth;
                }
            }
        }

        maxWidth += w;

        return maxWidth;
    }

    /**
     * get the maximum height of this area
     * 
     * @return max height
     */
    public int getMaximumHeight() {
        int maxHeight = getInsets().top + getInsets().bottom;

        for (int i = 0; i < getModel().getSize(); i++) {
            JGoObject renderer = getCellRendererComponent(i);
            if (renderer != null) {
                int rendererHeight = renderer.getHeight();
                maxHeight += rendererHeight;
                maxHeight += vSpacing;
            }
        }
        //remove one extra vspace
        maxHeight -= vSpacing;

        return maxHeight;
    }

    /**
     * get first visible row index
     * 
     * @return first visible row index
     */
    public int getFirstVisibleRow() {
        return firstVisibleRow;
    }

    /**
     * set the first visible row
     * 
     * @param rowIdx index of first visible row
     */
    public void setFirstVisibleRow(int rowIdx) {
        int oldIndex = firstVisibleRow;

        if (rowIdx >= 0 && rowIdx <= getRowCount() && oldIndex != rowIdx) {
            firstVisibleRow = rowIdx;
        }
    }

    /**
     * get the height of all visible rows in this column
     * 
     * @return height of visible rows
     */
    public int getVisibleRowHeights() {
        int rowHeights = 0;
        for (int i = this.getFirstVisibleRow(); i <= this.getLastVisibleRow(); i++) {
            JGoObject renderer = getCellRendererComponent(i);
            if (renderer != null) {
                rowHeights += renderer.getHeight() + getVerticalSpacing();
            }
        }

        rowHeights += insets.top + insets.bottom - getVerticalSpacing();

        return rowHeights;
    }

    /**
     * get the last visible row of this list area
     * 
     * @return list visible row
     */
    public int getLastVisibleRow() {
        return lastVisibleRow;
    }

    /**
     * set the last visible row
     * 
     * @param rowIdx index of first visible row
     */
    public void setLastVisibleRow(int rowIdx) {
        int oldIndex = lastVisibleRow;

        if (rowIdx >= 0 && rowIdx <= getRowCount() && oldIndex != rowIdx) {
            lastVisibleRow = rowIdx;
        }
    }

    /**
     * get the vertical spacing
     * 
     * @return vertical spacing
     */
    public int getVerticalSpacing() {
        return vSpacing;
    }

    /**
     * set the vertical spacing between cells of this list area
     * 
     * @param newspace new vertical space
     */
    public void setVerticalSpacing(int newspace) {
        int oldSpacing = vSpacing;
        if (oldSpacing != newspace) {
            vSpacing = newspace;
        }
    }

    /**
     * get the line pen
     * 
     * @return line pen
     */
    public JGoPen getLinePen() {
        return (linePen != null) ? linePen : DEFAULT_PEN;
    }

    /**
     * set line pen for drawing border
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
     * set the out of scroll cell bounds
     * 
     * @param rect rect
     */
    public void setOutOfScrollCellBounds(Rectangle rect) {
    }

    /**
     * paint this area
     * 
     * @param g Graphics2D
     * @param view view
     */
    public void paint(Graphics2D g, JGoView view) {
        super.paint(g, view);

        if (!drawLines) {
            return;
        }

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

            JGoObject cell = this.getCellRendererComponent(i);
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
     * layout all of this children of this column area
     */
    public void layoutChildren() {
        columnRect.setPen(getLinePen());
        
        if (drawBoundingRect) {
            columnRect.setBoundingRect(this.getBoundingRect());
        }

        //get the bounding rectangle of this column area
        int x = getLeft() + insets.left;
        int y = getTop() + insets.top;
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;

        int oldLastVisibleRow = lastVisibleRow;

        int cellWidth = width;

        int nextCellDeltaTop = 0;

        //row count
        int cnt = 0;
        for (int i = 0; i < dataModel.getSize(); i++) {
            JGoObject cell = (JGoObject) areaList.get(i);

            if (cnt < getFirstVisibleRow()) {
                cell.setVisible(false);

                cell.setBoundingRect(x, y + nextCellDeltaTop, cellWidth, cell.getHeight());
                cnt++;
                continue;
            }

            //if cell is going out of the height of this
            //area then we mark it invisible
            //if (nextCellTop > (y + height)) {
            if (nextCellDeltaTop + cell.getHeight() > height) {

                cell.setVisible(false);
                cell.setBoundingRect(x, y + nextCellDeltaTop, cellWidth, cell.getHeight());
            } else {
                cell.setVisible(true);
                lastVisibleRow = cnt;
                cell.setBoundingRect(x, y + nextCellDeltaTop, cellWidth, cell.getHeight());
                nextCellDeltaTop += cell.getHeight() + getVerticalSpacing();

            }
            cnt++;
        }

        //layout vertical scrollbar
        JGoScrollBar sbar = getVerticalScrollBar();

        if (sbar != null) {
            sbar.setBoundingRect(x + width - scrollBarSize - vScrollBarGap, y, scrollBarSize, height);

            if (oldLastVisibleRow != lastVisibleRow) {
                updateVerticalScrollBar();
            }
        }
    }

    class ListDataAdapter implements ListDataListener {

        /**
         * Sent when the contents of the list has changed in a way that's too complex to
         * characterize with the previous methods. For example, this is sent when an item
         * has been replaced. Index0 and index1 bracket the change.
         * 
         * @param e a <code>ListDataEvent</code> encapsulating the event information
         */
        public void contentsChanged(ListDataEvent e) {
        }

        /**
         * Sent after the indices in the index0,index1 interval have been inserted in the
         * data model. The new interval includes both index0 and index1.
         * 
         * @param e a <code>ListDataEvent</code> encapsulating the event information
         */
        public void intervalAdded(ListDataEvent e) {
            //list model has new items so get renderer for them and add it
            for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                Object val = getModel().getElementAt(i);

                ListAreaCellRenderer listCellRenderer = getCellRenderer(i);
                JGoObject cell = listCellRenderer.getListAreaCellRenderer(ListArea.this, val, i, false, false);

                //set location of the cell otherwise it will be 0 0 causing
                //width and height of listarea to start from 0,0 till current list
                //are x, y position see computeBoundingRect() for how it is calculated

                cell.setLocation(ListArea.this.getLocation());
                areaList.add(cell);
                addObjectAtTail(cell);
            }
        }

        /**
         * Sent after the indices in the index0,index1 interval have been removed from the
         * data model. The interval includes both index0 and index1.
         * 
         * @param e a <code>ListDataEvent</code> encapsulating the event information
         */
        public void intervalRemoved(ListDataEvent e) {
            //list model has some items removed so remove renderer for them
            for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                JGoObject obj = (JGoObject) areaList.get(i);
                areaList.remove(i);
                removeObject(obj);
            }

        }

    }
}

