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
import java.awt.Insets;
import java.awt.Point;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import com.nwoods.jgo.JGoObject;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BasicListArea extends BasicCanvasArea {

    protected static final GradientBrush BRUSH_TITLE = new GradientBrush(new Color(221, 221, 255), // light
                                                                                                    // magenta
        new Color(160, 186, 213)); // navy

    /**
     * list area in this Basic List Area
     */
    protected ListArea listArea;

    /**
     * Creates a new instance of BasicListArea
     * 
     * @param title title
     */
    public BasicListArea(String title) {
        this.setPickableBackground(true);
        this.setSelectable(true);
        titleArea = new TitleArea(title);
        titleArea.setBrush(BRUSH_TITLE);

        this.addObjectAtTail(titleArea);
    }

    /**
     * create an instance of list area
     * 
     * @param title title
     * @param data list cell data array
     */
    public BasicListArea(String title, Object[] data) {
        this(title);

        listArea = new ListArea();
        // don not draw lines in the list area
        // line drawing occurs in paint method
        listArea.setDrawLines(false);
        listArea.setVerticalSpacing(0);

        this.addObjectAtTail(listArea);

        DefaultListModel model = new DefaultListModel();

        for (int i = 0; i < data.length; i++) {
            model.addElement(data[i]);
        }
        setModel(model);

    }

    /**
     * add an object in list area
     * 
     * @param val value
     */
    public void add(Object val) {
        DefaultListModel dListModel = (DefaultListModel) getModel();
        dListModel.addElement(val);
    }

    /**
     * add the list cell at a specfic row index
     * 
     * @param row row
     * @param val value
     */
    public void add(int row, Object val) {
        DefaultListModel dListModel = (DefaultListModel) getModel();
        dListModel.add(row, val);
    }

    /**
     * set the first visible row of the list
     * 
     * @param rowIdx row index
     */
    public void setFirstVisibleRow(int rowIdx) {
        listArea.setFirstVisibleRow(rowIdx);
    }

    /**
     * set the last visible row of the list
     * 
     * @param rowIdx row index
     */
    public void setLastVisibleRow(int rowIdx) {
        listArea.setLastVisibleRow(rowIdx);
    }

    /**
     * get the index of child object
     * 
     * @param val value
     * @return index of child object
     */
    public int getIndexOf(Object val) {
        DefaultListModel dListModel = (DefaultListModel) getModel();
        return dListModel.indexOf(val);
    }

    /**
     * get the value at a particular point
     * 
     * @param loc location
     * @return value at a point
     */
    public Object getValueAt(Point loc) {
        return listArea.getValueAt(loc);
    }

    /**
     * get the title area of this list
     * 
     * @return title area
     */
    public TitleArea getTitleArea() {
        return titleArea;
    }

    /**
     * get maximum width of this area
     * 
     * @return max width
     */
    public int getMaximumWidth() {
        int maxWidth = getInsets().left + getInsets().right;

        int w = 0;

        w = titleArea.getMaximumWidth();

        if (listArea.getMaximumWidth() > w) {
            w = listArea.getMaximumWidth();
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

        maxHeight += titleArea.getMaximumHeight();
        maxHeight += listArea.getMaximumHeight();

        return maxHeight;
    }

    /**
     * get the minimum width of this area
     * 
     * @return minimum width
     */
    public int getMinimumWidth() {
        int minWidth = 0;
        minWidth = getInsets().left + getInsets().right;
        minWidth += titleArea.getMinimumWidth();

        return minWidth;
    }

    /**
     * get the minimum height of this area
     * 
     * @return minimum height
     */
    public int getMinimumHeight() {
        int minHeight = 0;
        minHeight = getInsets().top + getInsets().bottom;
        minHeight += titleArea.getMinimumHeight();

        return minHeight;
    }

    // override this to get the visible row height.
    // subclass must override this if they have any custom areas
    protected int getVisibleRowHeights() {
        int visHeight = getInsets().top + getInsets().bottom;
        visHeight += titleArea.getMaximumHeight();
        visHeight += listArea.getVisibleRowHeights();

        return visHeight;
    }

    /**
     * get the vertical spacing
     * 
     * @return vertical spacing
     */
    public int getVerticalSpacing() {
        return listArea.getVerticalSpacing();
    }

    /**
     * set the vertical spacing between cells of this list area
     * 
     * @param newspace new vertical space
     */
    public void setVerticalSpacing(int newspace) {
        listArea.setVerticalSpacing(0);
    }

    /**
     * set the vertical scrollbar gap from the edge of the list area
     * 
     * @param gap gap
     */
    public void setVerticalScrollBarGapFromEdge(int gap) {
        listArea.setVerticalScrollBarGapFromEdge(gap);
    }

    /**
     * this gets the notification from the TableTitleArea when it expansion or collapse
     * image is clicked
     * 
     * @param hint event hint
     * @param prevInt previous integer value
     * @param prevVal previous object val
     */
    public void update(int hint, int prevInt, Object prevVal) {
        if (hint == TitleArea.EXPANSION_STATE_CHANGED) {
            // optimization: assume area doesn't change when scrolling items
            if (titleArea.getState() == TitleArea.EXPANDED) {
                setExpanded(true);
            } else {
                setExpanded(false);
            }
            JGoObject parent = this.getParent();
            if (parent != null) {
                parent.update(hint, prevInt, prevVal);
            }
        } else {
            super.update(hint, prevInt, prevVal);
        }
    }

    public boolean adjustHeight(JGoObject child) {
        // uncomment this once tested properly with scrolling
        if (child.equals(listArea) && this.isExpandedState()) {

            this.setHeight(getVisibleRowHeights());
            return true;
        }

        return false;
    }

    /**
     * layout the children of this list area
     */
    public void layoutChildren() {
        Insets insets1 = getInsets();

        // get the bounding rectangle of this table area
        int x = getLeft() + insets1.left;
        int y = getTop() + insets1.top;
        int width = getWidth() - insets1.left - insets1.right;
        int height = getHeight() - insets1.top - insets1.bottom;

        titleArea.setBoundingRect(x, y, width, titleArea.getMinimumHeight());

        if (height - titleArea.getHeight() > 0) {
            listArea.setVisible(true);
            listArea.setOutOfScrollCellBounds(titleArea.getBoundingRect());
            listArea.setBoundingRect(x, y + titleArea.getHeight(), width, height - titleArea.getHeight());
        } else {
            listArea.setVisible(false);
            listArea.setOutOfScrollCellBounds(titleArea.getBoundingRect());
            listArea.setBoundingRect(titleArea.getBoundingRect());
        }
    }

    /**
     * set the model for this list area
     * 
     * @param model list model
     */
    public void setModel(ListModel model) {
        listArea.setModel(model);
    }

    /**
     * get the list model of this list area
     * 
     * @return list model
     */
    public ListModel getModel() {
        return listArea.getModel();
    }

}

