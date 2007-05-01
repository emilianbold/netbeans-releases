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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class HeaderCellArea extends CanvasArea {

    private BasicText text;

    private JGoRectangle rect;

    /**
     * Creates a new instance of HeaderCellArea
     * 
     * @param columnName name of the column
     */
    public HeaderCellArea(String columnName) {
        super();
        this.setSelectable(false);
        this.setResizable(false);
        this.setGrabChildSelection(false);

        text = new BasicText(columnName);
        text.setSelectable(false);
        text.setResizable(false);
        text.setDraggable(false);

        //make it transparent so that we can see the effect of JGo3DRect
        text.setTransparent(true);
        //align the text to center
        text.setAlignment(JGoText.ALIGN_CENTER);
        //make the text bold
        text.setBold(true);
        this.addObjectAtTail(text);

        rect = new JGoRectangle();
        rect.setSelectable(false);
        rect.setResizable(false);

        rect.setPen(JGoPen.makeStockPen(Color.lightGray));
        rect.setBrush(JGoBrush.makeStockBrush(new Color(254, 253, 235)));
        addObjectAtHead(rect);

        //set the insets on header cell
        layoutChildren();
    }

    /**
     * get the minimum size of the area
     * 
     * @return minimum size
     */
    public Dimension getMinimumSize() {

        int w = text.getWidth() + insets.left + insets.right;
        int h = text.getHeight() + insets.top + insets.bottom;
        return new Dimension(w, h);
    }

    /**
     * paint this area
     * 
     * @param g Graphics2D
     * @param view view
     */
    public void paint(java.awt.Graphics2D g, JGoView view) {
        super.paint(g, view);

        Insets insets1 = getInsets();

        //get the bounding rectangle of this table area
        int width = getWidth() - insets1.left - insets1.right;

        int textWidth = text.getWidth();
        if (width < textWidth) {

        }
    }

    /**
     * override this method to handle the changes in the geometry of this area we will lay
     * out all the cell again
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
     * layout the children of this cell area
     */
    public void layoutChildren() {
        Rectangle rectangle = this.getBoundingRect();
        text.setBoundingRect(rectangle);
        rect.setBoundingRect(rectangle);

    }

}

