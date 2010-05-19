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
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;

import org.netbeans.modules.sql.framework.ui.graph.IGraphInterface;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;

/**
 * @author Ritesh Adval
 * @author Jonathan Giron
 * @version $Revision$
 */
public class BasicTableArea extends BasicCanvasArea implements IGraphInterface {

    /**
     * table area
     */
    protected TableArea tableArea;

    /**
     * toolbar area
     */
    protected ToolBarArea toolBarArea;

    private JGoRectangle rect;

    private int titleToolBarGap = 2;
    private int toolBarTableGap = 2;

    private boolean drawBoundingRect = false;

    /** Creates a new instance of SQLTableArea */
    public BasicTableArea() {
        super();
        if (drawBoundingRect) {
            this.insets = new Insets(5, 5, 5, 5);

            rect = new JGoRectangle();
            rect.setPen(JGoPen.makeStockPen(Color.lightGray));
            rect.setBrush(JGoBrush.makeStockBrush(new Color(241, 249, 253)));
            rect.setResizable(false);
            addObjectAtTail(rect);
        }
    }

    /**
     * create a new instance of table
     * 
     * @param nTitleArea titleArea
     * @param toolBarArea toolBar Area
     * @param nTableArea table area
     */
    public BasicTableArea(TitleArea nTitleArea, ToolBarArea toolBarArea, TableArea nTableArea) {
        this();
        this.titleArea = nTitleArea;
        addObjectAtTail(titleArea);

        this.toolBarArea = toolBarArea;
        addObjectAtTail(toolBarArea);

        this.tableArea = nTableArea;
        addObjectAtTail(tableArea);

    }

    /**
     * initialize the table
     * 
     * @param loc location
     * @param nTitleArea titleArea
     * @param nTableArea nTableArea
     */
    public void initialize(Point loc, TitleArea nTitleArea, TableArea nTableArea) {
        this.titleArea = nTitleArea;
        addObjectAtTail(titleArea);

        this.tableArea = nTableArea;
        addObjectAtTail(tableArea);

        this.setBoundingRect(loc.x, loc.y, getMaximumWidth(), getMaximumHeight());

    }

    /**
     * set the title of the table
     * 
     * @param title title
     */
    public void setTitle(String title) {
        titleArea.setTitle(title);
    }

    /**
     * get the table area
     * 
     * @return table area
     */
    public TableArea getTableArea() {
        return tableArea;
    }

    /**
     * set the gap between title and toolbar
     * 
     * @param gap gap between title and tool bar
     */
    public void setTitleToolBarGap(int gap) {
        this.titleToolBarGap = gap;
    }

    /**
     * set the gap between toolbar and table
     * 
     * @param gap gap
     */
    public void setToolBarTableGap(int gap) {
        this.toolBarTableGap = gap;
    }

    /**
     * set the state
     * 
     * @param sExpanded whether table is expanded
     */
    public void setExpanded(boolean sExpanded) {
        tableArea.setExpanded(sExpanded);
        //make this table resizeable only in expanded mode
        this.setResizable(sExpanded);

        super.setExpanded(sExpanded);
    }

    /**
     * draw the bounding rectangle
     * 
     * @param draw whether to draw a bounding rect
     */
    public void setDrawBoundingRect(boolean draw) {
        drawBoundingRect = draw;
    }

    /**
     * Is a bounding rectangle is drawn over this area
     * 
     * @return whether there is a bounding rectangle drawn
     */
    public boolean isDrawBoundingRect() {
        return drawBoundingRect;
    }

    /**
     * get the maximum width
     * 
     * @return maximum width
     */
    public int getMaximumWidth() {
        int maxWidth = 0;

        //always take the width of TableArea..
        if (tableArea != null) {
            maxWidth = tableArea.getMaximumWidth();
        }

        //TableTitleArea may be longer than TableArea due to title name
        if (titleArea != null) {
            maxWidth = Math.max(maxWidth, titleArea.getMaximumWidth());
        }

        // Always account for horizontal insets.
        maxWidth += getInsets().left + getInsets().right;
        return maxWidth;
    }

    /**
     * get the minimum width
     * 
     * @return minimum width
     */
    public int getMinimumWidth() {
        // Default value.
        int minWidth = 150;

        if (titleArea != null) {
            minWidth = Math.max(minWidth, titleArea.getMinimumWidth());
        }

        if (tableArea != null) {
            minWidth = Math.max(minWidth, tableArea.getMinimumWidth());
        }

        // Always adjust minimum width to account for horizontal insets.
        minWidth += getInsets().left + getInsets().right;
        return minWidth;
    }

    /**
     * get the maximum height
     * 
     * @return maximum height
     */
    public int getMaximumHeight() {
        int maxHeight = getInsets().top + getInsets().bottom;

        if (titleArea != null) {
            maxHeight += titleArea.getMaximumHeight();
            maxHeight += titleToolBarGap;
        }
        if (toolBarArea != null) {
            maxHeight += toolBarArea.getMaximumHeight();
            maxHeight += toolBarTableGap;
        }
        if (tableArea != null) {
            maxHeight += tableArea.getMaximumHeight();
        }

        return maxHeight;
    }

    /**
     * get the minimum height
     * 
     * @return minimum height
     */
    public int getMinimumHeight() {
        int minHeight = getInsets().top + getInsets().bottom;

        //take min height of title into account this much height
        //we want to show always
        if (titleArea != null) {
            minHeight += titleArea.getMinimumHeight();
            minHeight += titleToolBarGap;
        }

        return minHeight;
    }

    /**
     * set the bounding rectangle. This also will constrain table to its maximum possible
     * height.
     * 
     * @param left left
     * @param top top
     * @param width width
     * @param height height
     */
    public void setBoundingRect(int left, int top, int width, int height) {
        //int maxWidth = this.getMaximumWidth();
        int maxHeight = this.getMaximumHeight();
        super.setBoundingRect(left, top, width, Math.min(height, maxHeight));
    }

    /**
     * handle child geometry change. This handle change in TableArea. when a resize occurs
     * table area sets its height to the height of all visible column. this avoids extra
     * space in vertical scroll bar
     * 
     * @param child child
     * @param prevRect child prev rect
     */
    protected boolean geometryChangeChild(JGoObject child, java.awt.Rectangle prevRect) {
        if (child == tableArea && this.isExpandedState()) {
            int height = tableArea.getVisibleRowTableHeights() + toolBarTableGap;
            if (titleArea != null) {
                height += titleArea.getMaximumHeight();
            }

            if (toolBarArea != null) {

                height += titleToolBarGap + toolBarArea.getMaximumHeight();
            }
            if (this.getHeight() != height) {
                setHeight(height);
            } else {
                layoutChildren();
            }

            return true;
        }

        return super.geometryChangeChild(child, prevRect);
    }

    /**
     * layout all the children of this table area
     */
    public void layoutChildren() {
        if (drawBoundingRect) {
            rect.setBoundingRect(this.getBoundingRect());
        }

        Insets insets1 = getInsets();

        //get the bounding rectangle of this table area
        int x = getLeft() + insets1.left;
        int y = getTop() + insets1.top;
        int width = getWidth() - insets1.left - insets1.right;
        int height = getHeight() - insets1.top - insets1.bottom;

        titleArea.setBoundingRect(x, y, width, titleArea.getMaximumHeight());

        if (toolBarArea != null) {
            //if there is some height left allocate it to tool bar area
            if (height - titleArea.getHeight() - titleToolBarGap > 0) {
                toolBarArea.setVisible(true);
                toolBarArea.setBoundingRect(x, y + titleArea.getHeight() + titleToolBarGap, width, toolBarArea.getMaximumHeight());

            } else {
                toolBarArea.setVisible(false);
                toolBarArea.setBoundingRect(titleArea.getBoundingRect());
            }

            if (tableArea != null) {
                //if there is some height left allocate it to table area
                if (height - toolBarArea.getHeight() - toolBarTableGap > 0) {
                    tableArea.setVisible(true);

                    Rectangle tAreaRect = new Rectangle(x, toolBarArea.getTop() + toolBarArea.getHeight() + toolBarTableGap, width, height
                        - titleArea.getHeight() - titleToolBarGap - toolBarArea.getHeight() - toolBarTableGap);

                    //if table area height did not change (this happens when table
                    // is first minimized and then maximized) then we still want
                    //to layout children one more time so that vertical scrollbar
                    //will not have extra space
                    if (tableArea.getBoundingRect() != tAreaRect) {
                        tableArea.setBoundingRect(tAreaRect);
                    } else {
                        tableArea.layoutChildren();
                    }

                    return;
                }
                tableArea.setVisible(false);
                tableArea.setBoundingRect(titleArea.getBoundingRect());
            }

        } else { //end toolBar != null
            if (tableArea != null) {
                //if there is some height left allocate it to table area
                if (height - titleArea.getHeight() - titleToolBarGap > 0) {
                    tableArea.setVisible(true);
                    Rectangle tAreaRect = new Rectangle(x, y + titleArea.getHeight() + titleToolBarGap, width, height - titleArea.getHeight()
                        - titleToolBarGap);

                    //if table area height did not change (this happens when table
                    // is first minimized and then maximized) then we still want
                    //to layout children one more time so that vertical scrollbar
                    //will not have extra space

                    if (tableArea.getBoundingRect() != tAreaRect) {
                        tableArea.setBoundingRect(tAreaRect);
                    } else {
                        tableArea.layoutChildren();
                    }
                    return;
                }
                tableArea.setVisible(false);
                tableArea.setBoundingRect(titleArea.getBoundingRect());
            }
        }
    }

    /**
     * get the title area bounds
     * 
     * @return title area bounds
     */
    public Rectangle getTitleAreaBounds() {
        return titleArea.getBoundingRect();
    }

    /**
     * Sets brush for table title to the given instance.
     *  
     * @param newBrush JGoBrush to use for title area
     */
    public void setTitleBrush(JGoBrush newBrush) {
        if (this.titleArea != null) {
            this.titleArea.setBrush(newBrush);
        }
    }    

    /**
     * update the vertical scrollbar
     */
    public void updateVerticalScrollBar() {
    }

    public void setShowHeader(boolean show) {
        this.tableArea.setShowHeader(show);
    }

    /**
     * Sets background color for this instance.
     * 
     * @param c new background color
     */
    public void setBackgroundColor(Color c) {
        if (this.tableArea != null) {
            this.tableArea.setBackgroundColor(c);
        }
    }

    /**
     * Sets background paint for this instance.
     * 
     * @param p new Paint instance
     */
    public void setBackgroundPaint(Paint p) {
        this.titleArea.setBackgroundPaint(p);
    }
}

