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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoImage;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ToolBarItemArea extends CanvasArea {

    private JGoImage itemImg;

    private JGoRectangle rect;

    private Action action;

    private String actionCommand;

    /** Creates a new instance of ToolBarItemArea */
    public ToolBarItemArea() {

        this.setGrabChildSelection(true);
        rect = new JGoRectangle();
        rect = new JGoRectangle();
        rect.setSelectable(false);
        rect.setResizable(false);

        rect.setPen(JGoPen.lightGray);
        rect.setBrush(JGoBrush.makeStockBrush(Color.white));
        addObjectAtHead(rect);

        this.insets = new Insets(1, 1, 1, 1);
    }

    /**
     * initialize the toolbar item
     * 
     * @param icon icon
     */
    public void initialize(Icon icon) {
        //add title image
        itemImg = new JGoImage();
        ImageIcon imgIcon = (ImageIcon) icon;
        itemImg.loadImage(imgIcon.getImage(), false);

        itemImg.setSize(imgIcon.getImage().getWidth(null), imgIcon.getImage().getHeight(null));
        itemImg.setSelectable(false);
        itemImg.setResizable(false);
        addObjectAtTail(itemImg);

    }

    /**
     * create a new instance of toolbar item
     * 
     * @param action action
     */
    public ToolBarItemArea(Action action) {
        this();
        this.action = action;
        ItemPropertyChangeListener listener = new ItemPropertyChangeListener();
        action.addPropertyChangeListener(listener);

        Icon icon = (Icon) action.getValue(Action.SMALL_ICON);
        //String toolTip = (String) action.getValue(Action.SHORT_DESCRIPTION);
        actionCommand = (String) action.getValue(Action.ACTION_COMMAND_KEY);
        initialize(icon);
    }

    /**
     * handle mouse click
     * 
     * @param modifiers mouse event modifiers
     * @param dc document point
     * @param vc view point
     * @param view view
     * @return bool
     */
    public boolean doMouseClick(int modifiers, Point dc, Point vc, JGoView view) {

        ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand);

        action.actionPerformed(actionEvent);
        return false;
    }

    /**
     * override this method to handle the changes in the geometry of this area we will lay
     * out all images again
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
     * layout all the children of this table area
     */
    public void layoutChildren() {

        rect.setBoundingRect(this.getBoundingRect());
        Insets insets1 = getInsets();

        //get the bounding rectangle of this table area
        int x = getLeft() + insets1.left;
        int y = getTop() + insets1.top;
        int width = getWidth() - insets1.left - insets1.right;
        int height = getHeight() - insets1.top - insets1.bottom;

        itemImg.setBoundingRect(x, y, width, height);
    }

    class ItemPropertyChangeListener implements PropertyChangeListener {

        /**
         * listen for property change event
         * 
         * @param evt PropertyChangeEvent
         */
        public void propertyChange(PropertyChangeEvent evt) {

        }

    }
}

