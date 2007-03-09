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
package org.netbeans.modules.visualweb.designer;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;

import org.netbeans.modules.visualweb.css2.CssBox;


/**
 * Handle drawing (and "undrawing"!) a Marquee - a selection
 * rectangle (aka "marching ants", aka animated rubber-banding line).
 *
 * @todo Provide shift key snap-release here too?
 *
 * @author Tor Norbye
 */
public class Marquee extends Interaction {
    private WebForm webform = null;

    /* Restore previous cursor after operation. */
    protected transient Cursor previousCursor = null;

    /* The rectangle that defines the current marquee selection. */
    protected Rectangle marqueeBounds; // TODO look up!

    /* The start start and current point of the marquee session. */
    protected Point startPoint;
    protected Point currentPoint;
    protected Point unsnappedStartPoint;
    private boolean snap = false;
    private boolean select = true;
    private boolean insertCursor = false;

    /** The box potentially containing the marquee selection. Grid snapping will
     * be performed with respect to its coordinate system. */
    private CssBox gridBox;

    public Marquee(WebForm webform, CssBox gridBox) {
        this.webform = webform;
        this.gridBox = gridBox;
    }

    /** Cancel operation */
    public void cancel(DesignerPane pane) {
        cleanup(pane);
        marqueeBounds = null;
    }

    private void cleanup(DesignerPane pane) {
        // Restore the cursor to normal
        pane.setCursor(previousCursor);

        // Restore status line
//        StatusDisplayer_RAVE.getRaveDefault().clearPositionLabel();

        // Clean out the selection rectangle
        Rectangle dirty = new Rectangle(marqueeBounds);
        dirty.width++;
        dirty.height++;
        pane.repaint(dirty);
    }

    /** When the mouse press is released, get rid of the drawn marquee,
     * and ask the selection manager to select all the components contained
     * within the marquee bounds.
     */
    public void mouseReleased(MouseEvent e) {
        try {
            if ((e != null) && !e.isConsumed() && (marqueeBounds != null)) {
                // XXX shouldn't I get the final coordinates here, rather
                // than relying on the previous ones from mouseDragged?
                // TODO Should I pass in the event (e) so that the manager
                // can say use the control keys and alt keys
                if (select) {
                    // We should call selectViews even on size (0,0) since we need
                    // to cause an unselection in this case
                    if ((marqueeBounds.width != 0) || (marqueeBounds.height != 0)) {
                        webform.getSelection().selectComponentRectangle(marqueeBounds, true);
                    } else {
                        webform.getManager().getMouseHandler().selectAt(e, false);
                    }
                }

                //}
                cleanup(webform.getPane());

                e.consume();
            }
        } finally {
            currentPoint = null;
            startPoint = null;
            marqueeBounds = null;
            previousCursor = null;
        }
    }

    /**
     * Resizes the selection rectangle to extend from startpoint to the
     * new mouse cursor position.
     */
    public void mouseDragged(MouseEvent e) {
        if (!e.isConsumed() && (startPoint != null)) {
            currentPoint = e.getPoint();

            DesignerPane pane = webform.getPane();
            Rectangle old = marqueeBounds;

            if (snap) {
//                GridHandler gm = GridHandler.getInstance();
                GridHandler gm = webform.getGridHandler();
                currentPoint.x = gm.snapX(currentPoint.x, gridBox);
                currentPoint.y = gm.snapY(currentPoint.y, gridBox);
            }

            marqueeBounds = new Rectangle(startPoint);
            marqueeBounds.add(currentPoint);

            Rectangle dirty;

            if (old != null) {
                // compute "union" of the two rectangles so I can erase old "paint"
                dirty = marqueeBounds.union(old);
            } else {
                dirty = new Rectangle(marqueeBounds);
            }

            dirty.width++;
            dirty.height++;
            pane.repaint(dirty);

            e.consume();

            // Show position of component in the status line
            int x = marqueeBounds.x;
            int y = marqueeBounds.y;

            if (x < 0) {
                x = 0;
            }

            if (y < 0) {
                y = 0;
            }

            pane.scrollRectToVisible(new Rectangle(currentPoint));

//            StatusDisplayer_RAVE.getRaveDefault().setPositionLabelText(x + "," + y);
        }
    }

    public void paint(Graphics g) {
        // Draw a dashed line instead of a solid line?
        //  float[] dash = {1.0f, 7.0f}; // make a 1-pixel dot every 8th pixel
        //  g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
        //                              BasicStroke.JOIN_MITER,
        //                              10.0f, dash, 0.0f));
        if (marqueeBounds != null) {
            ColorManager colors = webform.getColors();
            g.setColor(colors.marqueeColor);
            g.fillRect(marqueeBounds.x + 1, marqueeBounds.y + 1, marqueeBounds.width - 1,
                marqueeBounds.height - 1);
            g.setColor(colors.marqueeColorBorder);
            g.drawRect(marqueeBounds.x, marqueeBounds.y, marqueeBounds.width, marqueeBounds.height);
        }
    }

    /**
     * Start the marquee at the specified startPoint.
     */
    public void mousePressed(MouseEvent e) {
        if (!e.isConsumed()) {
            startPoint = e.getPoint();

            DesignerPane pane = webform.getPane();

            if (snap) {
                unsnappedStartPoint = new Point(startPoint.x, startPoint.y);

//                GridHandler gm = GridHandler.getInstance();
                GridHandler gm = webform.getGridHandler();
                startPoint.x = gm.snapX(startPoint.x, gridBox);
                startPoint.y = gm.snapY(startPoint.y, gridBox);
            } else {
                unsnappedStartPoint = startPoint;
            }

            marqueeBounds = new Rectangle(startPoint);
            previousCursor = pane.getCursor();

            // XXX Why new Cursor? I should use predefined cursor here!!!
            if (insertCursor) {
                pane.setCursor(webform.getManager().getInsertCursor());
            } else {
                pane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }

            ImageIcon imgIcon =
                new ImageIcon(Marquee.class.getResource("/org/netbeans/modules/visualweb/designer/drag_select.gif")); // TODO get marquee icon
//            StatusDisplayer_RAVE.getRaveDefault().setPositionLabelIcon(imgIcon);

            e.consume();
        }
    }

    Rectangle getBounds() {
        return marqueeBounds;
    }

    Point getUnsnappedPosition() {
        return unsnappedStartPoint;
    }

    /** Should the coordinates of the rectangle be snapped to grid? */
    public void setSnapToGrid(boolean snap) {
        this.snap = snap;
    }

    /** Should a selection operation in the selection manager be attempted
     * when mouse is released ?*/
    public void setSelect(boolean select) {
        this.select = select;
    }

    public void setInsertCursor(boolean insertCursor) {
        this.insertCursor = true;
    }
}
