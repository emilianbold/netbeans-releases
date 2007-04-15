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

package org.netbeans.modules.soa.mapper.common.palette;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;


/**
 * List which wraps rows of items in HORIZONTAL_ALIGN_STYLE or
 * columns in VERTICAL_ALIGN_STYLE.
 *
 * @author Tientien Li
 * @version 
 */
public class PaletteList extends javax.swing.JList {

    /**
     * Field HORIZONTAL_ALIGN_STYLE
     */
    public static final int HORIZONTAL_ALIGN_STYLE = 0;

    /**
     * Field VERTICAL_ALIGN_STYLE
     */
    public static final int VERTICAL_ALIGN_STYLE = 1;

    /**
     *  Field rolloverIndex
     */
    private int rolloverIndex = -1;

    /** Field curSelectedIndex           */
    private int curSelectedIndex = -1;

    /** Field alignStyle           */
    private int alignStyle = VERTICAL_ALIGN_STYLE;

    /** Field visibleColumnCount           */
    private int visibleColumnCount = 8;

    /** Palette Manager           */
    private PaletteManager mManager = null;

    /**
     * Constructor for creating a PaletteList
     *
     *
     */
    public PaletteList() {

    }

    /**
     * Constructor for creating a PaletteList with an associated
     * Palette manager
     *
     *
     * @param m the palette manager
     *
     */
    public PaletteList(PaletteManager m) {
        mManager = m;
    }

    /**
     * Constructor for creating a PaletteList from a list of items
     *
     *
     * @param items the initial list of items
     *
     */
    public PaletteList(Object[] items) {
        super(items);
    }

    /**
     * update the palette list UI
     *
     *
     */
    public void updateUI() {
        setUI(new PaletteListUI());
        invalidate();
    }

    /**
     * Setter for mouse rollover index.
     * @param index index of item
     */
    public void setRolloverIndex(int index) {

        int oldValue = rolloverIndex;

        rolloverIndex = index;

        firePropertyChange("rolloverIndex", oldValue, rolloverIndex);
    }

    /**
     * set the current Selected Index
     *
     *
     * @param index the current selected index
     *
     */
    public void setCurSelectedIndex(int index) {

        curSelectedIndex = (curSelectedIndex == index)
                           ? -1
                           : index;

        if (mManager != null) {
            mManager.setSelectedItem((curSelectedIndex == -1)
                                     ? null
                                     : ((PaletteItemNode) (getModel()
                                         .getElementAt(curSelectedIndex))));
        }
    }

    /**
     * get the current Selected Index
     *
     *
     * @return the current selected index
     *
     */
    public int getCurSelectedIndex() {
        return curSelectedIndex;
    }

    /**
     * get the index of item which contains mouse cursor
     *
     * @return index of item which contains mouse cursor
     */
    public int getRolloverIndex() {
        return rolloverIndex;
    }

    /**
     * Setter for align style of the list. It can be
     * <code>HORIZONTAL_ALIGN_STYLE</code>
     * or <code>VERTICAL_ALIGN_STYLE</code>
     *
     * @param style the align style
     */
    public void setAlignStyle(int style) {

        switch (style) {

        case HORIZONTAL_ALIGN_STYLE :
        case VERTICAL_ALIGN_STYLE :
            break;

        default :
            throw new IllegalArgumentException("invalid alignStyle");
        }

        int oldValue = alignStyle;

        alignStyle = style;

        firePropertyChange("alignStyle", oldValue, alignStyle);
    }

    /**
     * Getter for the current align style
     *
     * @return the current align style
     */
    public int getAlignStyle() {
        return alignStyle;
    }

    /**
     * Sets preferred visible column count.
     * Used when list is added to <code>JViewport</code>
     *
     * @param columnCount preferred visible column count
     */
    public void setVisibleColumnCount(int columnCount) {

        int oldValue = visibleColumnCount;

        visibleColumnCount = Math.max(0, columnCount);

        firePropertyChange("visibleColumnCount", oldValue,
                           visibleColumnCount);    // NOI18N
    }

    /**
     * get the visible column count.
     *
     * @return preferred visible column count
     */
    public int getVisibleColumnCount() {
        return visibleColumnCount;
    }

    /**
     * get the ToolTip Text
     *
     *
     * @param event mouse event
     *
     * @return the tool tip text
     *
     */
    public String getToolTipText(MouseEvent event) {

        if (event != null) {
            java.awt.Point   p        = event.getPoint();
            int              index    = locationToIndex(p);
            javax.swing.ListCellRenderer renderer = getCellRenderer();

            if ((index != -1) && (renderer != null)) {
                java.awt.Component rendererComp =
                    renderer.getListCellRendererComponent(
                        this, getModel().getElementAt(index), index,
                        isSelectedIndex(index),
                        hasFocus() && (index == getLeadSelectionIndex()));

                if (rendererComp instanceof javax.swing.JComponent) {
                    MouseEvent newEvent;
                    Rectangle  cellBounds = getCellBounds(index, index);

                    p.translate(-cellBounds.x, -cellBounds.y);

                    newEvent = new MouseEvent(rendererComp, event.getID(),
                                              event.getWhen(),
                                              event.getModifiers(), p.x, p.y,
                                              event.getClickCount(),
                                              event.isPopupTrigger());

                    return ((javax.swing.JComponent) rendererComp)
                        .getToolTipText(newEvent);
                }
            }
        }

        return null;
    }

// Scrollable

    /**
     * get the tracking flag for scrollabel Viewport Width
     *
     *
     * @return true if align style is horizontal
     *
     */
    public boolean getScrollableTracksViewportWidth() {

        if (alignStyle == HORIZONTAL_ALIGN_STYLE) {
            return true;
        }

        return super.getScrollableTracksViewportWidth();
    }

    /**
     * get the tracking flag for scrollabel Viewport Height
     *
     *
     * @return true if align style is vertical
     *
     */
    public boolean getScrollableTracksViewportHeight() {

        if (alignStyle == VERTICAL_ALIGN_STYLE) {
            return true;
        }

        return super.getScrollableTracksViewportHeight();
    }

    /**
     * get the Preferred Scrollable Viewport Size
     *
     *
     * @return the perferred scrollable viewport size
     *
     */
    public Dimension getPreferredScrollableViewportSize() {

        java.awt.Insets insets             = getInsets();
        int    dx                 = insets.left + insets.right;
        int    dy                 = insets.top + insets.bottom;
        int    visibleRowCount    = getVisibleRowCount();
        int    visibleColumnCount = getVisibleColumnCount();
        int    fixedCellWidth     = getFixedCellWidth();
        int    fixedCellHeight    = getFixedCellHeight();

        if ((fixedCellWidth > 0) && (fixedCellHeight > 0)) {
            int width  = (visibleColumnCount * fixedCellWidth) + dx;
            int height = (visibleRowCount * fixedCellHeight) + dy;

            return new Dimension(width, height);
        } else if (getModel().getSize() > 0) {
            Rectangle r      = getCellBounds(0, 0);
            int       width  = (visibleColumnCount * r.width) + dx;
            int       height = (visibleRowCount * r.height) + dy;

            return new Dimension(width, height);
        } else {
            fixedCellWidth  = (fixedCellWidth > 0)
                              ? fixedCellWidth
                              : 256;
            fixedCellHeight = (fixedCellHeight > 0)
                              ? fixedCellHeight
                              : 16;

            return new Dimension(fixedCellWidth * visibleColumnCount,
                                 fixedCellHeight * visibleRowCount);
        }
    }

    /**
     * get the Scrollable Unit Increment
     *
     *
     * @param visibleRect the visible rectangle area
     * @param orientation the orientation
     * @param direction the direction of scrolling
     *
     * @return the scrollable unit increment
     *
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation, int direction) {

        int       index = getFirstVisibleIndex();
        Rectangle r     = getCellBounds(index, index);

        if (r == null) {
            return 0;
        }

        int increment = 0;

        if (orientation == javax.swing.SwingConstants.HORIZONTAL) {
            increment = r.width;
        } else {
            increment = r.height;
        }

        return increment;
    }


    /**
     * set Checked Item of the palette list
     *
     * @param index the index of checked item
     *
     */
    public void setCheckedItem(int index) {
        boolean oldState = isSelectedIndex(index);
        if (oldState) {
            removeSelectionInterval(index, index);
        } else {
            addSelectionInterval(index, index);
        }

        mManager.setCheckedItem(
                (PaletteItemNode) getModel().getElementAt(index), !oldState);
        //mManager.setCheckedItem(index, !oldState);
    }
}
