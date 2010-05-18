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
package org.netbeans.modules.visualweb.designer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;

import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.css2.CssBox;


/**
 * Handle user interaction for a Table Resize operation; this refers
 * to resizing the internal rows and columns. The overall table size itself
 * is handled by the generic Resizer as for any other component.
 *
 * @todo Handle simultaneous row and column resizing?
 * @todo I need some kind of user gesture for removing/unconstraining the span size
 *
 * @author Tor Norbye
 */
public class TableResizer extends Interaction implements KeyListener {
    /* Restore previous cursor after operation. */
    protected transient Cursor previousCursor = null;
    private WebForm webform;
    private Element element;
    private int prevX = -500;
    private int prevY = -500;
    private int prevMouseX = -500;
    private int prevMouseY = -500;
//    private MarkupDesignBean bean;
//    private MarkupTableDesignInfo designInfo;
    private Element tableComponentRootElement;
    
    private int direction;
    private boolean leftTopSide;
    private int spanLeft;
    private int spanTop;
    private int originalSize;
    private int otherSize;
    private int minimum;
    private int maximum;
    private int row;
    private int column;

    /** Current size of the rectangle being resized. */
    private Rectangle currentSize = null;

    /**
     * Create a resizer which tracks resize mouse operations.
     * @param webform The webform associated with the resized table
     * @param bean The table component
     * @param designInfo The markup table design info which coordinates model updates to the table
     * @param direction CssBox.X_AXIS if we are resizing a row, CssBox.Y_AXIS if we
     *    are resizing a column.
     * @param leftTopSide If you're resizing the column by dragging on the left side
     *    or the row by dragging on the top side, pass in true, otherwise false.
     * @param spanLeft The x coordinate of the row or column (span) being resized
     * @param spanTop The y coordinate of the row or column (span) being resized
     * @param spanSize The current width or height of the row or column (span)
     * @param otherSize The size of the other dimension of the row/column. This is typically
     *   the table height (if resizing a column) or the table width (if resizing a row)
     * @param minimum The minimum size of the span. This value is not allowed to be negative.
     * @param maximum The maximum size of the span. Pass in Integer.MAX_VALUE for unconstrained size.
     */
    public TableResizer(WebForm webform, /*MarkupDesignBean bean, MarkupTableDesignInfo designInfo,*/ Element tableComponentRootElement,
        int direction, boolean leftTopSide, int spanLeft, int spanTop, int spanSize, int otherSize,
        int minimum, int maximum, int row, int column, Element element) {
        this.webform = webform;
//        this.bean = bean;
//        this.designInfo = designInfo;
        this.tableComponentRootElement = tableComponentRootElement;
        
        this.direction = direction;
        this.leftTopSide = leftTopSide;
        this.spanLeft = spanLeft;
        this.spanTop = spanTop;
        this.originalSize = spanSize;
        this.otherSize = otherSize;
        this.minimum = minimum;
        this.maximum = maximum;
        this.row = row;
        this.column = column;
        this.element = element;

        assert minimum >= 0;
        assert minimum <= maximum;
    }

    /** Cancel operation */
    public void cancel(DesignerPane pane) {
        pane.removeKeyListener(this);
        cleanup(pane);
        currentSize = null;
    }

    private void cleanup(DesignerPane pane) {
        // Restore the cursor to normal
        pane.setCursor(previousCursor);

        // Restore status line
//        StatusDisplayer_RAVE.getRaveDefault().clearPositionLabel();
//        StatusDisplayer.getDefault().setStatusText(""); // TEMP

        // Clear
        if (prevX != -500) {
            Rectangle dirty = new Rectangle();
            resize(dirty, prevX, prevY);
            dirty.width++;
            dirty.height++;
            pane.repaint(dirty);
        }
    }

    /** When the mouse press is released, get rid of the drawn resizer,
     * and ask the selection manager to select all the components contained
     * within the resizer bounds.
     */
    public void mouseReleased(MouseEvent e) {
        if ((e != null) && !e.isConsumed()) {
            Point p = e.getPoint();
            DesignerPane pane = webform.getPane();
            pane.removeKeyListener(this);

            int x = p.x;
            int y = p.y;

            Rectangle r = new Rectangle();
            resize(r, x, y);

            // Set the column size
            if (direction == CssBox.Y_AXIS) {
//                designInfo.resizeColumn(bean, column, r.width);
                webform.getDomProviderService().resizeColumn(tableComponentRootElement, column, r.width);
            } else {
                assert direction == CssBox.X_AXIS;
//                designInfo.resizeRow(bean, row, r.height);
                webform.getDomProviderService().resizeRow(tableComponentRootElement, row, r.height);
            }

            cleanup(pane);

            e.consume();
        }
    }

    /**
     * Moves the dragging rectangles
     */
    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();
        prevMouseX = p.x;
        prevMouseY = p.y;
        update(e, p.x, p.y);
    }

    /** Compute the visual dimensions of the resize rectangle anchored
     *  from (spanLeft, spanTop) to the given coordinate, constrained by
     *  the minimum and maximum allowed size for the table.
     */
    private void resize(Rectangle r, int x, int y) {
        r.x = spanLeft;
        r.y = spanTop;

        int size = 0;

        switch (direction) {
        case CssBox.X_AXIS:

            if (leftTopSide) {
                size = originalSize - (y - spanTop);
                r.y = (spanTop + originalSize) - size;
            } else {
                size = y - spanTop;
            }

            break;

        case CssBox.Y_AXIS:

            if (leftTopSide) {
                size = originalSize - (x - spanLeft);
                r.x = (spanLeft + originalSize) - size;
            } else {
                size = x - spanLeft;
            }

            break;
        }

        if (size < minimum) {
            size = minimum;
        }

        if (size > maximum) {
            size = maximum;
        }

        // Assign sizes, but allow table info to supply constraints too
        switch (direction) {
        case CssBox.X_AXIS:
            r.width = otherSize;
//            size = designInfo.testResizeRow(bean, row, column, size);
            size = webform.getDomProviderService().testResizeRow(tableComponentRootElement, row, column, size);

            if (size == -1) { // VERY surprising behavior by the table design info
                size = 0;
            }

            r.height = size;

            break;

        case CssBox.Y_AXIS:
//            size = designInfo.testResizeColumn(bean, row, column, size);
            size = webform.getDomProviderService().testResizeColumn(tableComponentRootElement, row, column, size);

            if (size == -1) { // VERY surprising behavior by the table design info
                size = 0;
            }

            r.width = size;
            r.height = otherSize;

            break;
        }
    }

    /** Draw the resize rectangle */
    public void paint(Graphics g) {
        if (currentSize != null) {
            if (DesignerPane.useAlpha) {
                g.setColor(webform.getColors().resizerColor);
                g.fillRect(currentSize.x + 1, currentSize.y + 1, currentSize.width - 1,
                    currentSize.height - 1);
                g.setColor(webform.getColors().resizerColorBorder);
            } else {
                g.setColor(Color.BLACK);
            }

            g.drawRect(currentSize.x, currentSize.y, currentSize.width, currentSize.height);
        }
    }

    /**
     * Start the resizer by setting the dragging cursor and
     * drawing dragging rectangles.
     */
    public void mousePressed(MouseEvent e) {
        if (!e.isConsumed()) {
            Point p = e.getPoint();
            prevMouseX = p.x;
            prevMouseY = p.y;

            DesignerPane pane = webform.getPane();
            pane.addKeyListener(this);

            previousCursor = pane.getCursor();
            pane.setCursor(Cursor.getPredefinedCursor(direction));

            update(e, prevMouseX, prevMouseY);

            ImageIcon imgIcon =
                new ImageIcon(TableResizer.class.getResource(
                        "/org/netbeans/modules/visualweb/designer/resources/drag_resize.gif"));
//            StatusDisplayer_RAVE.getRaveDefault().setPositionLabelIcon(imgIcon);

            e.consume();
        }
    }

    private void update(InputEvent e, int px, int py) {
        if (!e.isConsumed()) {
            DesignerPane pane = webform.getPane();

            Rectangle dirty;

            if (currentSize != null) {
                dirty = currentSize;
                dirty.width++;
                dirty.height++;
            } else {
                dirty = new Rectangle();
            }

            prevX = px;
            prevY = py;
            currentSize = new Rectangle();
            resize(currentSize, prevX, prevY);
            dirty.add(currentSize.x, currentSize.y);
            dirty.add(currentSize.x + currentSize.width, currentSize.y + currentSize.height);
            dirty.width++;
            dirty.height++;
            pane.repaint(dirty);

            int w = currentSize.width;
            int h = currentSize.height;

            if (w < 0) {
                w = 0;
            }

            if (h < 0) {
                h = 0;
            }

            int size = (direction == CssBox.X_AXIS) ? currentSize.height : currentSize.width;

//            StatusDisplayer_RAVE.getRaveDefault().setPositionLabelText(Integer.toString(size));
//            StatusDisplayer.getDefault().setStatusText(Integer.toString(size));

            e.consume();
        }
    }

    // --- implements KeyListener ---
    public void keyPressed(KeyEvent e) {
        // Not yet used
        //        if (snapDisabled != e.isShiftDown()) {
        //            update(e, prevMouseX, prevMouseY);
        //        }
    }

    public void keyReleased(KeyEvent e) {
        // Not yet used
        //        if (snapDisabled != e.isShiftDown()) {
        //            update(e, prevMouseX, prevMouseY);
        //        }
    }

    public void keyTyped(KeyEvent e) {
    }
}
