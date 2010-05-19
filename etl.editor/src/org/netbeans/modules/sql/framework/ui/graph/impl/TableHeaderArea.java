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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TableHeaderArea extends CanvasArea {

    private ArrayList headerCellAreas;
    private JGoRectangle rect;
    private int headerGap = 1;
    private boolean drawBoundingRect = false;

    /** Creates a new instance of TableHeaderArea */
    public TableHeaderArea() {
        headerCellAreas = new ArrayList();
        this.setSelectable(false);
        this.setResizable(false);
        this.setDraggable(true);

        rect = new JGoRectangle();
        rect.setSelectable(false);
        rect.setResizable(false);
        rect.setPen(JGoPen.makeStockPen(Color.lightGray));
        this.addObjectAtHead(rect);
    }

    /**
     * set the headers in this area
     * 
     * @param columnNames name of the headers to set
     */
    public void setHeaders(String[] columnNames) {

        for (int i = 0; i < columnNames.length; i++) {
            addHeaderCell(columnNames[i]);
        }
    }

    /**
     * get the count of headers
     * 
     * @return count of headers
     */
    public int getHeaderCount() {
        return headerCellAreas.size();
    }

    /**
     * add a header cell to the collection of headers in this area
     * 
     * @param columnName name of the column header
     */
    public void addHeaderCell(String columnName) {
        HeaderCellArea headerCell = new HeaderCellArea(columnName);
        headerCellAreas.add(headerCell);
        this.addObjectAtTail(headerCell);
    }

    /**
     * set the width of header cell at an index in this TableHeaderArea
     * 
     * @param header index of header cell
     * @param width new width of header cell
     */
    public void setHeaderCellWidth(int header, int width) {
        HeaderCellArea headerCell = (HeaderCellArea) headerCellAreas.get(header);
        if (headerCell != null) {
            headerCell.setWidth(width);
        }
    }

    /**
     * get maximum width of a header cell at an index
     * 
     * @param header index of header cell
     * @return max width of header cell
     */
    public int getHeaderCellMaxWidth(int header) {
        HeaderCellArea headerCell = (HeaderCellArea) headerCellAreas.get(header);
        if (headerCell != null) {
            return headerCell.getMinimumSize().width;
        }
        return -1;
    }

    /**
     * get the maximum cell width of a cell in this area
     * 
     * @return maximum cell width
     */
    public int getMaximizeCellWidth() {
        Iterator it = headerCellAreas.iterator();
        //this is the width of widest cell
        int w = 0;

        while (it.hasNext()) {
            HeaderCellArea cell = (HeaderCellArea) it.next();
            //need to include the insets of cell also in the width
            int width = cell.getWidth();
            if (width > w) {
                w = width;
            }
        }

        return w;
    }

    /**
     * get the maximum cell height of a cell in this area
     * 
     * @return maximum cell height
     */
    public int getMaximizeCellHeight() {
        Iterator it = headerCellAreas.iterator();
        //this is the height of widest cell
        int h = 0;

        while (it.hasNext()) {
            HeaderCellArea cell = (HeaderCellArea) it.next();

            int height = cell.getHeight();
            if (height > h) {
                h = height;
            }
        }

        return h;
    }

    /**
     * get maximum width
     * 
     * @return max width
     */
    public int getMaximumWidth() {
        int maxWidth = getInsets().left + getInsets().right;
        maxWidth += getMaximizeCellWidth();

        return maxWidth;
    }

    /**
     * get the maximum height
     * 
     * @return max Height
     */
    public int getMaximumHeight() {
        int maxHeight = getInsets().top + getInsets().bottom;
        maxHeight += getMaximizeCellHeight();

        return maxHeight;
    }

    /**
     * calculate the minimum size for the columnRect , as determined by the maximum item
     * size plus the insets
     * 
     * @return the minimum size
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
     * capture the mouse move event
     * 
     * @param flags flags
     * @param dc document coordinates
     * @param vc view coordinates
     * @param view the view
     * @return bool
     */
    public boolean doUncapturedMouseMove(int flags, Point dc, Point vc, JGoView view) {
        if (showResizeCursor(dc, view)) {
            return true;
        }

        return super.doUncapturedMouseMove(flags, dc, vc, view);
    }

    private boolean showResizeCursor(Point dc, JGoView view) {
        int x = this.getLeft();
        int y = this.getTop();
        //calculate the next header cell left position
        int nextHeaderLeft = x;

        Iterator it = headerCellAreas.iterator();
        while (it.hasNext()) {
            HeaderCellArea header = (HeaderCellArea) it.next();
            Rectangle rect1 = new Rectangle(nextHeaderLeft + header.getWidth(), y, headerGap, header.getHeight());

            if (rect1.contains(dc)) {
                view.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                return true;
            }
        }
        return false;
    }

    /**
     * override this method to handle the changes in the geometry of this area we will lay
     * out all the header cell
     * 
     * @param prevRect the previous bounds rectangle
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
     * its child are resizeable
     * 
     * @param child child
     * @param prevRect child prev rect
     */

    protected boolean geometryChangeChild(JGoObject child, Rectangle prevRect) {
        //do nothing as we do not want to listen to changes in children
        return true;
    }

    /**
     * layout all the children of this table area
     */
    public void layoutChildren() {

        //set the bounding display rectangle
        if (drawBoundingRect) {
            rect.setBoundingRect(this.getBoundingRect());
        }
        //get the bounding rectangle of this table area
        int x = this.getLeft();
        int y = this.getTop();
        //calculate the next header cell left position
        int nextHeaderLeft = x;

        Iterator it = headerCellAreas.iterator();
        while (it.hasNext()) {
            HeaderCellArea header = (HeaderCellArea) it.next();
            header.setBoundingRect(nextHeaderLeft, y, header.getWidth(), header.getHeight());

            //calcualte next header cell left
            nextHeaderLeft += header.getWidth() + headerGap;

        }

    }
}

