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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemListener;

import org.netbeans.modules.sql.framework.ui.graph.impl.BasicText;
import org.netbeans.modules.sql.framework.ui.graph.impl.CanvasArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.CheckBoxArea;
import org.openide.util.NbBundle;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;

/**
 * Extension of CanvasArea to show toggle control for parentheses surrounding a given operator.
 * 
 * @author Ritesh Adval
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ParenthesisCheckBoxArea extends CanvasArea {
    private JGoRectangle rect;

    private CheckBoxArea cbArea;

    private BasicText title;

    private int checkboxTextGap = 1;

    /**
     * Constructs a default instance of ParenthesisCheckBoxArea.
     */
    ParenthesisCheckBoxArea() {
        this.setSelectable(false);
        this.setResizable(false);
        this.setGrabChildSelection(false);

        //add the bounding display rectangle
        rect = new JGoRectangle();

        rect.setPen(JGoPen.makeStockPen(Color.WHITE));
        rect.setBrush(JGoBrush.makeStockBrush(new Color(241, 249, 253)));
        rect.setSelectable(false);
        rect.setResizable(false);
        addObjectAtHead(rect);

        // Add check box
        cbArea = new CheckBoxArea();
        String toolTipText = NbBundle.getMessage(ParenthesisCheckBoxArea.class, "LBL_hide_display_parentheses");
        cbArea.setToolTipText(toolTipText);
        cbArea.setBackground(new Color(221, 235, 246));
        addObjectAtTail(cbArea);

        //add text of title
        String titleText = NbBundle.getMessage(ParenthesisCheckBoxArea.class, "LBL_use_parentheses");
        title = new BasicText(titleText);
        title.setEditable(false);
        title.setSelectable(false);
        title.setResizable(false);
        title.setTransparent(true);
        title.setBold(false);

        title.setShowDot(false);
        addObjectAtTail(title);

        this.insets = new Insets(1, 3, 1, 3);
    }

    /**
     * layout the children of this cell area
     */
    public void layoutChildren() {
        Rectangle rectangle = this.getBoundingRect();
        rect.setBoundingRect(rectangle);

        Insets insets1 = getInsets();

        int x = this.getLeft() + insets1.left;
        int y = this.getTop() + insets1.top;
        int width = this.getWidth() - insets1.left - insets1.right;
        int height = this.getHeight() - insets1.top - insets1.bottom;

        cbArea.setBoundingRect(x, y, cbArea.getWidth(), height);
        int yCenteringAdjustment = Math.max(0, (cbArea.getHeight() - title.getHeight()) / 2);
        title.setBoundingRect(x + cbArea.getWidth() + checkboxTextGap, y + yCenteringAdjustment, width - cbArea.getWidth() - checkboxTextGap, height
            - (yCenteringAdjustment * 2));
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.ICanvasInterface#getMinimumWidth()
     */
    public int getMinimumWidth() {
        Insets insets1 = getInsets();
        int minWidth = insets1.left + insets1.right;
        minWidth += cbArea.getWidth();
        minWidth += checkboxTextGap;
        minWidth += title.getMaximumWidth();

        return minWidth;
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.ICanvasInterface#getMinimumHeight()
     */
    public int getMinimumHeight() {
        int minHeight = cbArea.getHeight();

        if (title.getHeight() > minHeight) {
            minHeight = title.getHeight();
        }

        minHeight += getInsets().top + getInsets().bottom;
        return minHeight;
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.ICanvasInterface#getMaximumHeight()
     */
    public int getMaximumHeight() {
        // For now, fix this check box area to be uniform in height regardless of the
        // whether its parent JGoArea is resized.
        return getMinimumHeight();
    }

    /**
     * Change the cursor at the port
     * 
     * @param flags
     */
    public boolean doUncapturedMouseMove(int flags, Point dc, Point vc, JGoView view) {
        if (getLayer() != null && getLayer().isModifiable()) {
            view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return true;
        }
        return false;
    }

    /**
     * Adds the given item listener to this control.
     * 
     * @param l ItemListener to be added
     */
    public void addItemListener(ItemListener l) {
        this.cbArea.addItemListener(l);
    }

    /**
     * Removes the given item listener from this control.
     * 
     * @param l ItemListener to be removed
     */
    public void removeItemListener(ItemListener l) {
        this.cbArea.removeItemListener(l);
    }

    /**
     * whether to select or deselect check box ui
     */
    public void setShowParenthesis(boolean select) {
        this.cbArea.setSelected(select);
    }
    
    /**
     * Sets background color. 
     * 
     * @param c new background color
     */
    public void setBackgroundColor(Color c) {
        rect.setBrush(JGoBrush.makeStockBrush(c));
        cbArea.setBackground(c);
        title.setTransparent(true);
    }
    
    /**
     * Sets color of text label associated with the check box.
     * 
     * @param c new text color
     */
    public void setTextColor(Color c) {
        title.setTextColor(c);
    }
}
