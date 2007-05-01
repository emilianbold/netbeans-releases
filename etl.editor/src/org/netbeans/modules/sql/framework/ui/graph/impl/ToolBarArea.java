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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ToolBarArea extends CanvasArea {

    private JGoRectangle rect;
    private ArrayList items;

    private int hGap = 2;

    /** Creates a new instance of TableToolBarArea */
    public ToolBarArea() {
        items = new ArrayList();

        rect = new JGoRectangle();
        rect = new JGoRectangle();
        rect.setSelectable(false);
        rect.setPen(JGoPen.lightGray);
        rect.setBrush(JGoBrush.makeStockBrush(new Color(254, 253, 235)));
        addObjectAtHead(rect);

        this.insets = new Insets(4, 2, 2, 4);

        this.setResizable(false);
        this.setSelectable(false);
        this.setGrabChildSelection(false);
    }

    /**
     * add a toolbar item
     * 
     * @param item toolbar item
     */
    public void addToolBarItem(ToolBarItemArea item) {
        this.addObjectAtTail(item);
        items.add(item);
    }

    /**
     * remove a tool bar item
     * 
     * @param item toolbar item
     */
    public void removeToolBarItem(ToolBarItemArea item) {
        this.removeObject(item);
        items.remove(item);
    }

    /**
     * initialize the tool bar area
     * 
     * @param loc the location of toolbar
     */
    public void initialize(Point loc) {

        this.setBoundingRect(loc.x, loc.y, getMaximumWidth(), getMaximumHeight());

    }

    /**
     * get the maximum width
     * 
     * @return maximum width
     */
    public int getMaximumWidth() {
        int maxWidth = insets.left + insets.right;

        Iterator it = items.iterator();
        while (it.hasNext()) {
            ToolBarItemArea item = (ToolBarItemArea) it.next();
            maxWidth += item.getWidth();
        }

        return maxWidth;
    }

    /**
     * get the maximum height
     * 
     * @return maximum height
     */
    public int getMaximumHeight() {
        int maxHeight = insets.top + insets.bottom;

        Iterator it = items.iterator();
        while (it.hasNext()) {
            ToolBarItemArea item = (ToolBarItemArea) it.next();
            maxHeight += item.getHeight();
            break;
        }

        return maxHeight;
    }

    /**
     * override this method to handle the changes in the geometry of this area we will lay
     * out all toolbar items
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
     * layout all the children of this toolbar area
     */
    public void layoutChildren() {

        rect.setBoundingRect(this.getBoundingRect());
        Insets insets1 = getInsets();

        //get the bounding rectangle of this table area
        int x = getLeft() + insets1.left;
        int y = getTop() + insets1.top;
        int nextLeft = x;

        Iterator it = items.iterator();
        while (it.hasNext()) {
            ToolBarItemArea item = (ToolBarItemArea) it.next();
            item.setBoundingRect(nextLeft, y, item.getWidth(), item.getHeight());

            nextLeft += item.getWidth() + hGap;
        }
    }

}

