/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import org.netbeans.modules.sql.framework.ui.graph.IGraphInterface;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDrawable;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ColumnPortArea extends CanvasArea {
    /**
     * constant that describe that this area has ports on left side of it
     */
    public static final int LEFT_PORT_AREA = 0;

    /**
     * constant that describe that this area has ports on right side of it
     */
    public static final int RIGHT_PORT_AREA = 1;

    private static final JGoPen DEFAULT_PEN = JGoPen.makeStockPen(Color.WHITE);
    
    private ArrayList columnPorts;
    private JGoRectangle rect;
    //default --> a ColumnPortArea can be both source and destination of a link
    private int columnPortAlignment = 2;
    private int vSpace = 1;
    private JGoPen linePen = null;
    private boolean expanded = true;

    //constants for first , last visible row
    private int firstVisibleRow = 0;
    private int lastVisibleRow = -1;

    private boolean drawBoundingRect = true;

    /**
     * Creates a new instance of ColumnPortArea
     * 
     * @param alignment alignment
     * @param rows number of rows
     */
    public ColumnPortArea(int alignment, int rows) {
        super();
        this.setSelectable(false);
        this.setResizable(false);

        this.rect = new JGoRectangle();
        rect.setSelectable(false);
        rect.setResizable(false);

        rect.setPen(DEFAULT_PEN);
        rect.setBrush(JGoBrush.makeStockBrush(new Color(254, 253, 235)));
        this.addObjectAtTail(rect);

        this.columnPortAlignment = alignment;
        columnPorts = new ArrayList();
        addPorts(rows);

        this.insets = new Insets(1, 0, 0, 0);

    }

    /**
     * add port areas to this area
     * 
     * @param rows number of port areas to be added
     */
    public void addPorts(int rows) {
        for (int i = 0; i < rows; i++) {
            PortArea pArea = new PortArea();
            if (columnPortAlignment == LEFT_PORT_AREA) {
                pArea.setValidDestination(true);
                pArea.setValidSource(false);
            } else if (columnPortAlignment == RIGHT_PORT_AREA) {
                pArea.setValidSource(true);
                pArea.setValidDestination(false);
            }

            this.addObjectAtTail(pArea);
            columnPorts.add(pArea);
            layoutChildren();
        }
    }

    public void addPort() {
        PortArea pArea = new PortArea();
        pArea.setLocation(this.getLeft(), this.getTop() + this.getMaximumHeight());
        if (columnPortAlignment == LEFT_PORT_AREA) {
            pArea.setValidDestination(true);
            pArea.setValidSource(false);
        } else if (columnPortAlignment == RIGHT_PORT_AREA) {
            pArea.setValidSource(true);
            pArea.setValidDestination(false);
        }

        this.addObjectAtTail(pArea);
        columnPorts.add(pArea);
    }

    public void addPort(int row) {
        if (row < 0 || row > columnPorts.size()) {
            throw new IllegalArgumentException("Can not add port, specified row " + row + " does not exist.");
        }

        PortArea pArea = new PortArea();
        pArea.setLocation(this.getLeft(), this.getTop() + this.getMaximumHeight());
        if (columnPortAlignment == LEFT_PORT_AREA) {
            pArea.setValidDestination(true);
            pArea.setValidSource(false);
        } else if (columnPortAlignment == RIGHT_PORT_AREA) {
            pArea.setValidSource(true);
            pArea.setValidDestination(false);
        }

        this.addObjectAtTail(pArea);
        columnPorts.add(row, pArea);
    }

    public void removePort(int row) {
        if (row < 0 || row >= columnPorts.size()) {
            throw new IllegalArgumentException("Can not remove port, specified row " + row + " does not exist.");
        }
        PortArea pArea = (PortArea) columnPorts.get(row);
        this.removeObject(pArea);
        columnPorts.remove(row);
    }

    /**
     * get the index of a port area
     * 
     * @param pArea port area
     * @return port area index
     */
    public int getIndexOf(PortArea pArea) {
        for (int i = 0; i < columnPorts.size(); i++) {
            PortArea portArea = (PortArea) columnPorts.get(i);
            if (portArea.equals(pArea)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * get the port area at an index
     * 
     * @param row row index
     * @return port area
     */
    public PortArea getPortAreaAt(int row) {
        if (row < columnPorts.size()) {
            return (PortArea) columnPorts.get(row);
        }
        return null;
    }

    /**
     * get the graphical bound rectangle
     * 
     * @return graphical bound rectangle
     */
    public JGoRectangle getRect() {
        return rect;
    }

    /**
     * get the number of rows in this column area
     * 
     * @return number of rows in this column
     */
    public int getRowCount() {
        if (columnPorts == null) {
            return -1;
        }
        return columnPorts.size();
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
     * set the first visible row index
     * 
     * @param rowIdx row index
     */
    public void setFirstVisibleRow(int rowIdx) {
        int oldIndex = firstVisibleRow;

        if (rowIdx >= 0 && rowIdx <= getRowCount() && oldIndex != rowIdx) {
            firstVisibleRow = rowIdx;
            layoutChildren();
        }
    }

    /**
     * get the last visible row index
     * 
     * @return last visible row index
     */
    public int getLastVisibleRow() {
        return lastVisibleRow;
    }

    /**
     * is area expanded
     * 
     * @return whether area is expanded
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * set whether container table is expanded
     * 
     * @param sExpanded expanded or collapsed
     */
    public void setExpanded(boolean sExpanded) {
        this.expanded = sExpanded;
    }

    /**
     * get the line pen
     * 
     * @return pen
     */
    public JGoPen getLinePen() {
        return (linePen != null) ? linePen : DEFAULT_PEN;
    }

    /**
     * set the line pen for drawing border
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

    public int getMaximumHeight() {
        Iterator it = columnPorts.iterator();
        //this is the width of widest PortArea
        int h = 0;

        while (it.hasNext()) {
            PortArea pArea = (PortArea) it.next();
            int height = pArea.getHeight();
            h += height + getVerticalSpacing();

        }
        //remove one extra vertical space
        if (columnPorts.size() > 0) {
            h -= getVerticalSpacing();
        }

        return h;
    }

    /**
     * get the maximum width of a PortArea in this area
     * 
     * @return maximum PortArea width
     */
    public int getMaximizePortAreaWidth() {
        Iterator it = columnPorts.iterator();
        //this is the width of widest PortArea
        int w = 0;

        while (it.hasNext()) {
            PortArea pArea = (PortArea) it.next();
            int width = pArea.getWidth();
            if (width > w) {
                w = width;
            }
        }

        return w;
    }

    /**
     * get the maximum height of a PortArea in this area
     * 
     * @return maximum PortArea height
     */
    public int getMaximizePortAreaHeight() {
        Iterator it = columnPorts.iterator();
        //this is the height of tallest PortArea
        int h = 0;

        while (it.hasNext()) {
            PortArea pArea = (PortArea) it.next();

            int height = pArea.getHeight();
            if (height > h) {
                h = height;
            }
        }

        return h;
    }

    /**
     * calculate the minimum size for the columnRect , as determined by the maximum item
     * size plus the insets
     * 
     * @return minimum size
     */
    public Dimension getMinimumRectSize() {
        int maxW = getMaximizePortAreaWidth();
        int maxH = getMaximizePortAreaHeight();

        // now account for insets on all sides
        Insets insets1 = getInsets();

        int minw = maxW + insets1.left + insets1.right;
        int minh = maxH + insets1.top + insets1.bottom;
        return new Dimension(minw, minh);
    }

    /**
     * get the minimum size
     * 
     * @return minimum size
     */
    public Dimension getMinimumSize() {
        // first account for the minimum ListAreaRect size
        Dimension minRect = getMinimumRectSize();
        return minRect;
    }

    /**
     * set the port area height at an index
     * 
     * @param row row index
     * @param height height
     */
    public void setPortAreaHeight(int row, int height) {
        if (columnPorts.size() > row) {
            PortArea pArea = (PortArea) columnPorts.get(row);
            pArea.setHeight(height);
        }
    }

    /**
     * paint this area
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
        // do not allow to draw a line for last item...
        // it will be taken care by bounding rectangle
        for (int i = 0; i < columnPorts.size() - 1; i++) {

            PortArea cell = (PortArea) columnPorts.get(i);
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
     * override this method to handle the changes in the geometry of this area we will lay
     * out all the columns and headers again
     * 
     * @param prevRect previous bounds rectangle
     */
    protected void geometryChange(Rectangle prevRect) {
        // handle any size changes by repositioning all the items
        if (prevRect.width != getWidth() || prevRect.height != getHeight()) {
            layoutChildren();
        } else {
            super.geometryChange(prevRect);
        }
    }

    /**
     * handle child geometry change. This will ignore changes in child geometry as none if
     * its children are resizable
     * 
     * @param child child
     * @param prevRect previous rectangle
     */

    protected boolean geometryChangeChild(JGoObject child, Rectangle prevRect) {
        //do nothing as we do not want to listen to changes in children
        return true;
    }

    /**
     * layout all of this children of this column area
     */
    public void layoutChildren() {
        rect.setPen(getLinePen());
        
        if (drawBoundingRect) {
            rect.setBoundingRect(this.getBoundingRect());
        }
        Insets insets1 = getInsets();

        int x = this.getLeft() + insets1.left;
        int y = this.getTop() + insets1.top;
        int height = this.getHeight() - insets1.top - insets1.bottom;

        // remember last visible row index
        lastVisibleRow = getFirstVisibleRow();

        //get the cell with maximum width, this will be the width of the column
        int cellWidth = this.getMaximizePortAreaWidth();
        
        //calculate the top of next cell
        int nextCellDeltaTop = 0;
        Iterator it = columnPorts.iterator();

        //row count
        int cnt = 0;

        while (it.hasNext()) {
            PortArea pArea = (PortArea) it.next();

            if (cnt < getFirstVisibleRow()) {
                pArea.setVisible(false);
                JGoObject parent = this.getParent();
                if (parent != null && parent instanceof IGraphInterface) {
                    Rectangle cellBounds = ((IGraphInterface) parent).getTitleAreaBounds();
                    if (columnPortAlignment == TableConstants.LEFT_PORT_AREA) {
                        pArea.setLocation(cellBounds.x, cellBounds.y);
                    } else {
                        pArea.setLocation(cellBounds.x + cellBounds.width - pArea.getWidth(), cellBounds.y);
                    }
                }
                cnt++;
                continue;
            }

            //if cell is going out of the height of this area
            //then we mark it invisible
            if (nextCellDeltaTop + pArea.getHeight() > height) {
                pArea.setVisible(false);
                JGoObject parent = this.getParent();
                if (parent != null && parent instanceof IGraphInterface) {
                    Rectangle cellBounds = ((IGraphInterface) parent).getTitleAreaBounds();
                    if (columnPortAlignment == TableConstants.LEFT_PORT_AREA) {
                        pArea.setLocation(cellBounds.x, cellBounds.y);
                    } else {
                        pArea.setLocation(cellBounds.x + cellBounds.width - pArea.getWidth(), cellBounds.y);
                    }
                }
                continue;
            }
            
            pArea.setVisible(true);
            pArea.setBoundingRect(x, y + nextCellDeltaTop, cellWidth, pArea.getHeight());

            nextCellDeltaTop += pArea.getHeight() + getVerticalSpacing();
            cnt++;
        }
    }

    /**
     * get the vertical space between port area of this area
     * 
     * @return vertical space
     */
    int getVerticalSpacing() {
        return vSpace;
    }

    public void setBackgroundColor(Color c) {
        if (this.rect != null) {
            this.rect.setBrush(JGoBrush.makeStockBrush(c));
        }

        Iterator it = this.columnPorts.iterator();
        while (it.hasNext()) {
            PortArea p = (PortArea) it.next();
            p.setBackgroundColor(c);
        }
    }
}

