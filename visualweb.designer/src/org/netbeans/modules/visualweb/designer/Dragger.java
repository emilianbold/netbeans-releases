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


import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import org.netbeans.modules.visualweb.css2.PageBox;
import org.openide.ErrorManager;

import org.w3c.dom.Element;
import org.w3c.dom.Node;



/**
 * Handle drawing (and "undrawing"!) a drag outline, as well as
 * communicating a drag to the GridHandler on completion
 * @todo Should I consult Toolkit.isDynamicLayoutSet() to decide
 *    if I should do live-image-dragging or just show outlines?
 *    If users have set outlines only I assume it's not a beefy
 *    system so could benefit from the simplification.
 *
 * @author Tor Norbye
 */
public class Dragger extends Interaction implements KeyListener {
    /** Pixel distance you must drag before dragging will actually
     * be performed.
     */
    private static final int THRESHOLD = 2;

    //private static final boolean DISPLAY_IMAGES = (System.getProperty("rave.enableImageDrags") != null);
    private static final boolean DISPLAY_IMAGES =
        !(System.getProperty("rave.disableImageDrags") != null);
    private static final int DRAG_GRID = 0;
    private static final int DRAG_FREE = 1;
    private static final int LINKING = 2;

    /* Restore previous cursor after operation. */
    protected transient Cursor previousCursor;
    private WebForm webform;
    private List<CssBox> boxes;
    private List<Rectangle> selections;
//    private List<MarkupDesignBean> beans;
    private Element[] componentRootElements;
    private List<Image> images;
    
//    private Position pos;
    private DomPosition pos;
    
    private int prevX = -500;
    private int prevY = -500;
    private int prevAction;
    private int startX;
    private int startY;
    private int prevMouseX = -500;
    private int prevMouseY = -500;
    private int action;
    private BasicStroke linkStroke;
    private boolean alreadyMoved = false;
    private AffineTransform transform;
    private AlphaComposite alpha;

    /**
     * Create a dragger which tracks a dragging outline offset by the
     * given amount from the mouse position, with the given dimension
     * @param webform The webform dragging is occurring on
     * @param boxes List of boxes being dragged.
     * @param selections List of Rectangle objects, where each rectangle
     *   represents a component getting dragged. The (x,y) position of
     *   the rectangle represents the relative distance to the rectangle
     *   from the mouse pointer. The (width,height) of the rectangle
     *   represents the size of the rectangle to draw.
     *   Therefore, { [-10,-10,20,30],[30,40,100,100] } means that
     *   the dragger will draw two selection rectangles as the mouse
     *   is moving; one of size (20,30), one of size (100,100).
     *   The first one will be positioned 10 pixels above and to the
     *   left of the current mouse position, the other one 30 pixels
     *   to the right and 40 pixels below the cursor.
     *   <b>Important note</b>: The offset rectangle indicates
     *   the <b>border</b> edge of the component (as is done in most places
     *   in the designer - e.g. see CssBox.getAbsoluteX(), getWidth, etc.)
     * @param beans List of DesignBean objects corresponding to components moved
     * <p>
     *   If one item in the selection is considered "primary", it should
     *   be the first item in the list. It is the coordinate of this
     *   (first) item that is reported in the status bar.
     * <p>
     *   The list of boxes and the list of selections should have
     *   identical size, and each view corresponds to the rectangle
     *   in the same position in the other list.
     *   <p>
     *   selections may not be null, and may not an empty list.
     */
//    public Dragger(WebForm webform, List<CssBox> boxes, List<Rectangle> selections, List<MarkupDesignBean> beans) {
    public Dragger(WebForm webform, List<CssBox> boxes, List<Rectangle> selections, Element[] componentRootElements) {
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + "()");
        }
        if(webform == null) {
            throw(new IllegalArgumentException("Null webform."));
        }
        if(boxes == null) {
            throw(new IllegalArgumentException("Null boxes list."));
        }
        if(selections == null) {
            throw(new IllegalArgumentException("Null selections list."));
        }
//        if(beans == null) {
//            throw(new IllegalArgumentException("Null beans list."));
//        }
        if (componentRootElements == null) {
            throw new IllegalArgumentException("Null componentRootElements list."); // NOI18N
        }
        this.webform = webform;
        this.boxes = boxes;
        this.selections = selections;
//        this.beans = beans;
        this.componentRootElements = componentRootElements;
    }

    private void cleanup(DesignerPane pane, int oldAction) {
        // Restore the cursor to normal
        pane.setCursor(previousCursor);

        if (webform.isGridMode()) {
            webform.getPane().hideCaret();
        }

        // Restore status line
//        StatusDisplayer_RAVE.getRaveDefault().clearPositionLabel();

//        GridHandler gm = GridHandler.getInstance();
//        GridHandler gm = webform.getGridHandler();
//        DesignerPane.clearDirty();
        pane.clearDirty();

        if (selections == null) {
            pane.repaint(); // XXX why are we doing this?

            return;
        }

        int n = selections.size();

        if (oldAction == LINKING) {
//            DesignerPane.addDirtyPoint(startX-1, startY-1);
//            DesignerPane.addDirtyPoint(startX+1, startY+1);
//            DesignerPane.addDirtyPoint(prevX-1, prevY-1);
//            DesignerPane.addDirtyPoint(prevX+1, prevY+1);
            pane.addDirtyPoint(startX-1, startY-1);
            pane.addDirtyPoint(startX+1, startY+1);
            pane.addDirtyPoint(prevX-1, prevY-1);
            pane.addDirtyPoint(prevX+1, prevY+1);
        } else {
            for (int i = 0; i < n; i++) {
                Rectangle r2 = (Rectangle)selections.get(i);
                int x;
                int y;

                if (oldAction == DRAG_FREE) {
                    x = r2.x + prevX;
                    y = r2.y + prevY;
                } else if (hasMoved(prevX, prevY)) {
//                    GridHandler gm = GridHandler.getDefault();
//                    x = gm.snapX(r2.x + prevX, getPositionedBy(i));
//                    y = gm.snapY(r2.y + prevY, getPositionedBy(i));
                    x = webform.snapX(r2.x + prevX, getPositionedBy(i));
                    y = webform.snapY(r2.y + prevY, getPositionedBy(i));
                } else {
                    x = r2.x + startX;
                    y = r2.y + startY;
                }

//                DesignerPane.addDirtyPoint(x, y);
//                DesignerPane.addDirtyPoint(x + r2.width + 1, y + r2.height + 1);
                pane.addDirtyPoint(x, y);
                pane.addDirtyPoint(x + r2.width + 1, y + r2.height + 1);
            }
        }

        pane.repaintDirty(false);
    }

    /** Cancel operation */
    public void cancel(DesignerPane pane) {
        pane.removeKeyListener(this);
        cleanup(pane, action);
    }

    /** When the mouse press is released, get rid of the drawn dragger,
     * and ask the selection manager to select all the components contained
     * within the dragger bounds.
     */
    public void mouseReleased(MouseEvent e) {
        try {
            DesignerPane pane = webform.getPane();
            pane.removeKeyListener(this);

            if ((e != null) && !e.isConsumed()) {
                int oldAction = action;
                updateSnapState(e);

                Point p = e.getPoint();
                // XXX #136373 Do not drop to negative canvas position.
                if (p.x < 0) {
                    p.x = 0;
                }
                if (p.y < 0) {
                    p.y = 0;
                }

                cleanup(pane, oldAction);

                if (action == LINKING) {
                    DndHandler handler = webform.getPane().getDndHandler();

//                    DesignBean candidate = getLinkParticipant(startX, startY);
//                    Element candidate = getLinkParticipant(startX, startY);
                    Element candidate = ModelViewMapper.findElement(webform.getPane().getPageBox(), startX, startY);

                    if (candidate == null) {
//                        if (beans.size() > 0) {
                        if (componentRootElements.length > 0) {
//                            candidate = (DesignBean)beans.get(0);
                            candidate = componentRootElements[0];
                        } else {
                            handler.clearDropMatch();

                            return;
                        }
                    }

//                    int dropType =
//                        handler.getDropTypeForClassNamesEx(p,
//                            new String[] { candidate.getInstance().getClass().getName() },
//                            new DesignBean[] { candidate }, true);
                    int dropType = handler.getDropTypeForComponent(p, candidate, true);

                    if (dropType == DndHandler.DROP_LINKED) {
//                        CssBox box = webform.getMapper().findBox(p.x, p.y);
                        CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), p.x, p.y);
                        Element element = box.getElement();

                        if (element != null) {
//                            ArrayList candidates = new ArrayList();
//                            candidates.add(candidate);
//                                handler.processLinks(element, null, candidates, true,
//                                    true, false);
//                            handler.processLinks(element, null, candidate, true, true, false);
                            webform.processLinks(element, candidate);
                        }
                    } else {
                        // Try reverse link
//                        candidate = getLinkParticipant(p.x, p.y);
                        candidate = ModelViewMapper.findElement(webform.getPane().getPageBox(), p.x, p.y);

                        if (candidate != null) {
//                            dropType =
//                                handler.getDropTypeForClassNamesEx(new Point(startX, startY),
//                                    new String[] { candidate.getInstance().getClass().getName() },
//                                    new DesignBean[] { candidate }, true);
                            dropType =
                                handler.getDropTypeForComponent(new Point(startX, startY), candidate, true);
                        }

                        if (dropType == DndHandler.DROP_LINKED) {
//                            CssBox box = webform.getMapper().findBox(startX, startY);
                            CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), startX, startY);
                            Element element = box.getElement();

                            if (element != null) {
//                                ArrayList candidates = new ArrayList();
//                                candidates.add(candidate);

//                                    handler.processLinks(element, null, candidates, true,
//                                        true, false);
//                                handler.processLinks(element, null, candidate, true, true, false);
                                webform.processLinks(element, candidate);
                            }
                        }
                    }

                    handler.clearDropMatch();
                } else if ((p.x != startX) || // Don't use snapped coords here!
                        (p.y != startY)) {
                    /*
                      How do I -actually- move a component in the
                      document?

                      (With CSS2 it's easy - I just update its style
                      attribute to contain the new coordinates.)

                      It must currently be positioned in a layout
                      table. Thus, this becomes a GridHandler update
                      call where it needs to recompute its positions.
                    */
                    prevX = p.x;
                    prevY = p.y;

//                    GridHandler gm = GridHandler.getInstance();
//                    GridHandler gm = webform.getGridHandler();
                    boolean grid = isOverGrid(prevX, prevY);

                    if (grid) {
//                        pos = Position.NONE;
                        pos = DomPosition.NONE;
                    } else {
                        pos = getPosition(prevX, prevY);

//                        if ((pos == Position.NONE) && webform.getDocument().isGridMode()) {
//                        if ((pos == Position.NONE) && webform.isGridModeDocument()) {
//                        if ((pos == DomPosition.NONE) && webform.isGridModeDocument()) {
                        if ((pos == DomPosition.NONE) && webform.isGridMode()) {
                            // Safety net: in page grid mode, if we
                            // can't find a valid position, position
                            // it at the absolute position instead
                            grid = true;
                        }
                    }

//                    if ((pos != Position.NONE) || (grid && hasMoved(prevX, prevY))) {
                    if ((pos != DomPosition.NONE) || (grid && hasMoved(prevX, prevY))) {
//                        gm.move(pane, /*beans,*/ selections, boxes, pos, prevX, prevY,
//                            action == DRAG_FREE);
                        List<Point> points = new ArrayList<Point>();
                        // XXX #118153 Possible NPE.
                        if (selections != null) {
                            for (Rectangle selection : selections) {
                                points.add(new Point(selection.x, selection.y));
                            }
    //                        gm.move(pane, /*beans,*/ points.toArray(new Point[points.size()]), boxes.toArray(new CssBox[boxes.size()]),
    //                                pos, prevX, prevY, action == DRAG_FREE);
                            webform.getDomDocument().moveComponents(
                                    webform,  boxes.toArray(new CssBox[boxes.size()]), points.toArray(new Point[points.size()]),
                                    pos, prevX, prevY, !(action == DRAG_FREE));
                        }
                    } // else: didn't really move ...
                }

                e.consume();
            }
        } finally {
            selections = null;
            previousCursor = null;
            images = null;
        }
    }

    /**
     * Moves the dragging rectangles
     */
    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();
        // XXX #136373 Do not drop to negative canvas position.
        if (p.x < 0) {
            p.x = 0;
        }
        if (p.y < 0) {
            p.y = 0;
        }
        prevMouseX = p.x;
        prevMouseY = p.y;
        update(e, p.x, p.y);
        webform.getPane().scrollRectToVisible(new Rectangle(p));
    }

    /** Return the positioning parent associated with a given dragged box.
     * This is the parent we should compute relative indices to.
     */
    private CssBox getPositionedBy(int index) {
        CssBox box = (CssBox)boxes.get(index);

        return box.getPositionedBy();
    }

    /** Draw the selection rectangles at the given position
     */
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        int x = prevX;
        int y = prevY;

        if (action == LINKING) {
            if (webform.getManager().isHighlighted()) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.BLACK);
            }

            if (linkStroke == null) {
                int width = 1;
                linkStroke =
                    new BasicStroke((float)width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                        10.0f, new float[] { 4 * width, (4 * width) + width }, 0.0f);
            }

            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(linkStroke);
            g.drawLine(startX, startY, x, y);
            g2d.setStroke(oldStroke);

            // TODO Arrowhead?  If you do, don't forget to update code
            // which produces the dirty rectangle either
            return;
        }

        if (!hasMoved(x, y)) {
            return;
        }

//        if (pos != Position.NONE) {
        if (pos != DomPosition.NONE) {
            // We're over a text flow area - simply show the caret
            return;
        }

        // Draw a dashed line instead of a solid line?
        //  float[] dash = {1.0f, 7.0f}; // make a 1-pixel dot every 8th pixel
        //  g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
        //                              BasicStroke.JOIN_MITER,
        //                              10.0f, dash, 0.0f));
        if (selections == null) {
            return;
        }

        int n = selections.size();
//        GridHandler gm = GridHandler.getInstance();
//        GridHandler gm = webform.getGridHandler();
        Composite oldComposite = null;

        if (DISPLAY_IMAGES) {
            oldComposite = g2d.getComposite();

            if (alpha == null) {
                alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
            }

            g2d.setComposite(alpha);
        }

        for (int i = 0; i < n; i++) {
            Rectangle r = (Rectangle)selections.get(i);

            int xp;
            int yp;

            if (action == DRAG_FREE) {
                xp = r.x + x;
                yp = r.y + y;
            } else if (hasMoved(x, y)) {
//                GridHandler gm = GridHandler.getDefault();
//                xp = gm.snapX(r.x + x, getPositionedBy(i));
//                yp = gm.snapY(r.y + y, getPositionedBy(i));
                xp = webform.snapX(r.x + x, getPositionedBy(i));
                yp = webform.snapY(r.y + y, getPositionedBy(i));
            } else {
                xp = r.x + startX;
                yp = r.y + startY;
            }

            List<Image> images = null;

            if (DISPLAY_IMAGES) {
                images = getImages();
            }

            if ((images != null) && (images.get(i) != null)) {
                Image image = images.get(i);

                if (image != null) {
                    transform.setToTranslation((float)xp, (float)yp);
                    g2d.drawImage(image, transform, null);
                }
            } else {
                if (DesignerPane.useAlpha) {
                    g.setColor(webform.getColors().draggerColor);
                    g.fillRect(xp + 1, yp + 1, r.width - 1, r.height - 1);
                    g.setColor(webform.getColors().draggerColorBorder);
                } else {
                    g.setColor(Color.BLACK);
                }

                g.drawRect(xp, yp, r.width, r.height);
            }
        }

        if (oldComposite != null) {
            g2d.setComposite(oldComposite);
        }
    }

    // "Too much"; initiating a drag is a bit different than
    // adjusting the position of a component.
    //private static final int THRESHOLD = BasicDragGestureRecognizer.getMotionThreshold();

    /** Return true if the mouse pointer has moved (significantly)
     * since dragging was initiated. If not, we shouldn't snap to
     * grid for example - otherwise, simply clicking and releasing
     * on a component will move it to a grid location - not what we want.
     * If snap-to-grid is off, returns true if the coordinate has
     * moved any pixels since dragging began.
     * Once the mouse has moved from the original starting position,
     * this method will return true even over the original point.
     *
     * @param x The x coordinate of the reference point
     * @param y The y coordinate of the reference point
     * @return true iff we should snap the rectangle for the given reference
     *    (user mouse pointer) position.
     */
    private boolean hasMoved(int x, int y) {
        if (alreadyMoved) {
            return true;
        }

//        if (!GridHandler.getInstance().snap() || (action == DRAG_FREE)) {
//        if (!webform.getGridHandler().snap() || (action == DRAG_FREE)) {
//        if (!GridHandler.getDefault().isSnap() || (action == DRAG_FREE)) {
        if (!webform.isGridSnap() || (action == DRAG_FREE)) {
            return (x != startX) || (y != startY);
        }

        int distX = x - startX;

        if (distX < 0) {
            distX = -distX;
        }

        int distY = y - startY;

        if (distY < 0) {
            distY = -distY;
        }

        alreadyMoved = (distX > THRESHOLD) || (distY > THRESHOLD);

        return alreadyMoved;
    }

    /**
     * Start the dragger by setting the dragging cursor and
     * drawing dragging rectangles.
     */
    public void mousePressed(MouseEvent e) {
        if (!e.isConsumed()) {
            updateSnapState(e);

            Point p = e.getPoint();
            DesignerPane pane = webform.getPane();
            pane.addKeyListener(this);
            previousCursor = pane.getCursor();
            pane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            startX = p.x;
            startY = p.y;
            prevX = p.x;
            prevY = p.y;
            prevAction = getDragAction(e);
            prevMouseX = p.x;
            prevMouseY = p.y;

            ImageIcon imgIcon = // XXX Does this have any effect anymore?
                new ImageIcon(Dragger.class.getResource("/org/netbeans/modules/visualweb/designer/resources/drag_position.gif"));
//            StatusDisplayer_RAVE.getRaveDefault().setPositionLabelIcon(imgIcon);

            e.consume();
        }
    }

    /**
     * Report whether or not the components have been dragged from their
     * original positions.
     */
    public boolean hasMoved() {
        return hasMoved(prevX, prevY);
    }

    private List<Image> getImages() {
        if (images == null) {
            initializeImages();
        }

        return images;
    }

    private void initializeImages() {
        transform = new AffineTransform();

        int n = selections.size();
        images = new ArrayList<Image>(n);

        for (int i = 0; i < n; i++) {
            Image image = null;
            CssBox box = (CssBox)boxes.get(i);
            Rectangle r = (Rectangle)selections.get(i);

            if ((r.width > 0) && (r.height > 0)
            && ((float)r.width * r.height < Integer.MAX_VALUE)) { // XXX #6466015 Possible IAE from java.awt.image.SampleModel
                image = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_RGB);

                if (image != null) {
                    Graphics og = image.getGraphics(); // offscreen buffer

                    try {
                        og.setClip(0, 0, r.width, r.height);

                        //og.setColor(new Color(0, 255, 0, 0));
                        // Some components are transparent, so we have to
                        // paint the background. XXX I should ask them so
                        // I don't have to waste time here!!!
                        PageBox pageBox = box.getWebForm().getPane().getPageBox();
                        og.setColor(pageBox.getBackground());
                        og.fillRect(0, 0, r.width, r.height);
                        DesignerPane.clip.setBounds(0, 0, r.width, r.height);
                        DesignerPane.clipBr.x = r.width;
                        DesignerPane.clipBr.y = r.height;

                        // Turn off clip optimizations since they depend on box extents
                        // which we're not recomputing for this box
                        boolean oldClip = DesignerPane.INCREMENTAL_LAYOUT;
                        DesignerPane.INCREMENTAL_LAYOUT = false;

                        if (box.getX() == CssBox.UNINITIALIZED) {
                            // Some components paint into line boxes. These may be 
                            // broken up into non-rectangular boxes. (Imagine a StaticText
                            // consisting of two words, where the first word fits at the
                            // end of a line, and the second word fits at the beginning of
                            // the next). In this case we cannot simply paint the box,
                            // we have to paint its "context", e.g. the linebox. We've
                            // already computed the bounding box in the dragger code.
                            // This is set as the clip region. Paint the full page with
                            // a clip such that the component itself ends up at (0,0).
                            try {
                                pageBox.paint(og, -(startX + r.x), -(startY + r.y));
                            } finally {
                                DesignerPane.INCREMENTAL_LAYOUT = oldClip;
                            }
                        } else {
                            int oldX = box.getX();
                            int oldY = box.getY();
                            int oldLeftMargin = box.getLeftMargin();
                            int oldEffectiveTopMargin = box.getEffectiveTopMargin();

                            box.setLocation(0, 0);
                            box.setMargins(0, 0);

                            try {
                                box.paint(og, 0, 0);
                            } finally {
                                DesignerPane.INCREMENTAL_LAYOUT = oldClip;
                                box.setLocation(oldX, oldY);
                                box.setMargins(oldLeftMargin, oldEffectiveTopMargin);
                            }
                        }
                    } finally {
                        og.dispose();
                    }
                }
            }

            images.add(image); // even if image is null want it in the list
        }
    }

    // Release snap to grid with shift key
    private void updateSnapState(InputEvent e) {
        int oldAction = action;
        action = getDragAction(e);

        if ((oldAction == LINKING) && (action != LINKING)) {
            webform.getPane().getDndHandler().clearDropMatch();
        }
    }

    /** Compute a potential drop position under the pointer, if one
     * is allowed. Otherwise returns Position.NONE.
     */
//    private Position getPosition(int px, int py) {
    private DomPosition getPosition(int px, int py) {
        // Update caret if over a text area. But not if it's over
        // one of the being-dragged areas!
//        CssBox box = webform.getMapper().findBox(px, py);
        CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), px, py);

        if (box.getWebForm() != webform) {
            // XXX #6366584 The box is from different DOM, e.g. it is over fragment.
//            return Position.NONE;
            return DomPosition.NONE;
        }
        
        if (isBelowDragged(box)) {
//            return Position.NONE;
            return DomPosition.NONE;

            /*
            } else if (!(box instanceof TextBox) && box.isReplacedBox()) {
            System.out.println("Over non-text replaced box");
            return Position.NONE;
             */
        } else {
            if (box.isGrid()) {
//                return Position.NONE;
                return DomPosition.NONE;
            }

//            Position pos = webform.getManager().findTextPosition(px, py);
            DomPosition pos = webform.getManager().findTextPosition(px, py);

//            if ((pos == Position.NONE) || canDropAt(pos)) {
            if ((pos == DomPosition.NONE) || canDropAt(pos)) {
                return pos;
            }

//            return Position.NONE;
            return DomPosition.NONE;
        }
    }

//    private boolean canDropAt(Position pos) {
    private boolean canDropAt(DomPosition pos) {
        // Look up the nearest parent bean at the position
//        assert pos != Position.NONE;
        if (pos == DomPosition.NONE) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("Invalid position, pos=" + pos)); // NOI18N
            return false;
        }
//
//        DesignBean parent = null;
        Node curr = pos.getNode();
//
//        while (curr != null) {
////            if (curr instanceof RaveElement) {
////                parent = ((RaveElement)curr).getDesignBean();
//            if (curr instanceof Element) {
////                parent = InSyncService.getProvider().getMarkupDesignBeanForElement((Element)curr);
//                parent = WebForm.getDomProviderService().getMarkupDesignBeanForElement((Element)curr);
//
//                if (parent != null) {
//                    break;
//                }
//            }
//
//            curr = curr.getParentNode();
//        }
//
//        if (parent == null) {
//            return true;
//        }
//
//        // See if ALL the beans being dragged can be dropped here
//        LiveUnit unit = webform.getModel().getLiveUnit();
//
//        for (int i = 0, n = beans.size(); i < n; i++) {
//            DesignBean bean = (DesignBean)beans.get(i);
//            String className = bean.getInstance().getClass().getName();
//
//            if (!unit.canCreateBean(className, parent, null)) {
//                return false;
//            }
//
//            // Ensure that we're not trying to drop a html bean on a
//            // renders-children parent
//            boolean isHtmlBean = className.startsWith(HtmlBean.PACKAGE);
//
//            if (isHtmlBean) {
//                // We can't drop anywhere below a "renders children" JSF
//                // component
////                if (parent != FacesSupport.findHtmlContainer(webform, parent)) {
//                if (parent != webform.findHtmlContainer(parent)) {
//                    return false;
//                }
//            }
//        }
//
//        return true;
        
//        return webform.canDropDesignBeansAtNode(
//                beans == null
//                    ? new DesignBean[0]
//                    : (DesignBean[])beans.toArray(new DesignBean[beans.size()]),
//                curr);
        return webform.canDropComponentsAtNode(componentRootElements.clone(), curr);
    }

    /** Determine whether the given box is inside one of the boxes we're dragging */
    private boolean isBelowDragged(CssBox box) {
        for (int i = 0, n = boxes.size(); i < n; i++) {
            CssBox curr = box;
            CssBox draggedBox = (CssBox)boxes.get(i);

            while (curr != null) {
                if (curr == draggedBox) {
                    return true;
                }

                curr = curr.getParent();
            }
        }

        return false;
    }

    /** Determine whether the given x,y position is over a grid area.
     * If the x,y is over one of the objects being dragged, we look "under" it.
     */
    private boolean isOverGrid(int px, int py) {
//        CssBox box = webform.getMapper().findBox(px, py);
        CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), px, py);

        CssBox curr = box;

        while (curr != null) {
//            MarkupDesignBean currMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(curr);
            Element currComponentRootElement = CssBox.getElementForComponentRootCssBox(curr);
//            if (curr.getDesignBean() != null) {
//            if (currMarkupDesignBean != null) {
            if (currComponentRootElement != null) {
                // If we're within a component being dragged, look "under it".
                // No - that's no good -- there might be another absolutely positioned
                // component at this position that is NOT the parent; we should use
                // it instead. I guess findBox should take an ignore-list?
//                if (isDragged(curr.getDesignBean())) {
//                if (isDragged(currMarkupDesignBean)) {
                if (isDraggedComponent(currComponentRootElement)) {
                    box = curr.getParent();
                } else {
                    // If we're within a component that is not a container, look "under it".
//                    BeanInfo bi = curr.getDesignBean().getBeanInfo();
//                    BeanInfo bi = currMarkupDesignBean.getBeanInfo();
//
//                    if (bi != null) {
//                        BeanDescriptor bd = bi.getBeanDescriptor();
//                        Object o = bd.getValue(Constants.BeanDescriptor.IS_CONTAINER);
//                        boolean notContainer = o == Boolean.FALSE;
//
//                        if (notContainer) {
//                            box = curr.getParent();
//                        }
//                    }
                    if (!webform.getDomProviderService().isContainerComponent(currComponentRootElement)) {
                        box = curr.getParent();
                    }
                }
            }

            curr = curr.getParent();
        }

        return (box == null) || box.isGrid();
    }

    /** Return true iff the given DesignBean is being dragged, or if one of its
     * ancestors are being dragged.
     */
//    private boolean isDragged(DesignBean bean) {
    private boolean isDraggedComponent(Element componentRootElement) {
//        for (int i = 0, n = beans.size(); i < n; i++) {
////            DesignBean lb = (DesignBean)beans.get(i);
//            MarkupDesignBean lb = beans.get(i);
        for (Element lbElement : componentRootElements) {
        
//            if (bean == lb) {
//                return true;
//            }
            if (lbElement == componentRootElement) {
                return true;
            }
        }

//        if (bean.getBeanParent() == null) {
//            return false;
//        } else {
//            return isDragged(bean.getBeanParent());
//        }
        Element parentComponentRootElement = webform.getDomProviderService().getParentComponent(componentRootElement);
        return parentComponentRootElement == null ? false : isDraggedComponent(parentComponentRootElement);
    }

    private void update(InputEvent e, int px, int py) {
        if (!e.isConsumed()) {
            int oldAction = action;
            updateSnapState(e);

            DesignerPane pane = webform.getPane();

//            GridHandler gm = GridHandler.getInstance();
//            GridHandler gm = webform.getGridHandler();
//            GridHandler gm = GridHandler.getDefault();
//            DesignerPane.clearDirty();
            pane.clearDirty();

            // XXX #94643 Avoiding possible NPE.
            if (selections == null) {
                return;
            }
            
            if (!DesignerPane.INCREMENTAL_LAYOUT) {
//                DesignerPane.addDirtyPoint(0, 0);
                pane.addDirtyPoint(0, 0);
            }

            if (prevAction == LINKING) {
//                DesignerPane.addDirtyPoint(startX-1, startY-1);
//                DesignerPane.addDirtyPoint(startX+1, startY+1);
//                DesignerPane.addDirtyPoint(prevX-1, prevY-1);
//                DesignerPane.addDirtyPoint(prevX+1, prevY+1);
                pane.addDirtyPoint(startX-1, startY-1);
                pane.addDirtyPoint(startX+1, startY+1);
                pane.addDirtyPoint(prevX-1, prevY-1);
                pane.addDirtyPoint(prevX+1, prevY+1);
            } else if (prevX != -500) {
                int n = selections.size();

                for (int i = 0; i < n; i++) {
                    Rectangle r2 = (Rectangle)selections.get(i);
                    int x;
                    int y;

                    if (oldAction == DRAG_FREE) {
                        x = r2.x + prevX;
                        y = r2.y + prevY;
                    } else if (hasMoved(prevX, prevY)) {
//                        x = gm.snapX(r2.x + prevX, getPositionedBy(i));
//                        y = gm.snapY(r2.y + prevY, getPositionedBy(i));
                        x = webform.snapX(r2.x + prevX, getPositionedBy(i));
                        y = webform.snapY(r2.y + prevY, getPositionedBy(i));
                    } else {
                        x = r2.x + startX;
                        y = r2.y + startY;
                    }

//                    DesignerPane.addDirtyPoint(x, y);
//                    DesignerPane.addDirtyPoint(x + r2.width + 1, y + r2.height + 1);
                    pane.addDirtyPoint(x, y);
                    pane.addDirtyPoint(x + r2.width + 1, y + r2.height + 1);
                }
            }

            prevX = px;
            prevY = py;
            prevAction = action;

            int n = selections.size();

            if (action == LINKING) {
//                DesignerPane.addDirtyPoint(startX-1, startY-1);
//                DesignerPane.addDirtyPoint(startX+1, startY+1);
//                DesignerPane.addDirtyPoint(px-1, py-1);
//                DesignerPane.addDirtyPoint(px+1, py+1);
                pane.addDirtyPoint(startX-1, startY-1);
                pane.addDirtyPoint(startX+1, startY+1);
                pane.addDirtyPoint(px-1, py-1);
                pane.addDirtyPoint(px+1, py+1);
            } else {
                for (int i = 0; i < n; i++) {
                    Rectangle r2 = (Rectangle)selections.get(i);
                    int x;
                    int y;

                    if (action == DRAG_FREE) {
                        x = r2.x + prevX;
                        y = r2.y + prevY;
                    } else if (hasMoved(prevX, prevY)) {
//                        x = gm.snapX(r2.x + prevX, getPositionedBy(i));
//                        y = gm.snapY(r2.y + prevY, getPositionedBy(i));
                        x = webform.snapX(r2.x + prevX, getPositionedBy(i));
                        y = webform.snapY(r2.y + prevY, getPositionedBy(i));
                    } else {
                        x = r2.x + startX;
                        y = r2.y + startY;
                    }

//                    DesignerPane.addDirtyPoint(x, y);
//                    DesignerPane.addDirtyPoint(x + r2.width + 1, y + r2.height + 1);
                    pane.addDirtyPoint(x, y);
                    pane.addDirtyPoint(x + r2.width + 1, y + r2.height + 1);
                }
            }

            // Update caret if over a text area. But not if it's over
            // one of the being-dragged areas!
            boolean grid = isOverGrid(px, py);

            if (grid) {
//                pos = Position.NONE;
                pos = DomPosition.NONE;
            } else {
                pos = getPosition(px, py);

//                if ((pos == Position.NONE) && webform.getDocument().isGridMode()) {
//                if ((pos == Position.NONE) && webform.isGridModeDocument()) {
//                if ((pos == DomPosition.NONE) && webform.isGridModeDocument()) {
                if ((pos == DomPosition.NONE) && webform.isGridMode()) {
                    // Safety net: in page grid mode, if we can't find a valid
                    // position, position it at the absolute position instead
                    grid = true;
                }
            }

            if (action == LINKING) {
                webform.getPane().hideCaret();

//                DesignBean candidate = getLinkParticipant(startX, startY);
//                Element candidate = getLinkParticipant(startX, startY);
                Element candidate = ModelViewMapper.findElement(webform.getPane().getPageBox(), startX, startY);

                if ((candidate == null) ||
//                        (webform.getPane().getDndHandler().getDropTypeForClassNamesEx(new Point(px, py),
//                            new String[] { candidate.getInstance().getClass().getName() },
//                            new DesignBean[] { candidate }, true) != DndHandler.DROP_LINKED)) {
                        (webform.getPane().getDndHandler().getDropTypeForComponent(new Point(px, py),
                            candidate, true) != DndHandler.DROP_LINKED)) {
                    // Try reverse link
//                    candidate = getLinkParticipant(px, py);
                    candidate = ModelViewMapper.findElement(webform.getPane().getPageBox(), px, py);

                    if ((candidate == null) ||
//                            (webform.getPane().getDndHandler().getDropTypeForClassNamesEx(new Point(startX, startY),
//                                new String[] { candidate.getInstance().getClass().getName() },
//                                new DesignBean[] { candidate }, true) != DndHandler.DROP_LINKED)) {
                            (webform.getPane().getDndHandler().getDropTypeForComponent(new Point(startX, startY),
                                candidate, true) != DndHandler.DROP_LINKED)) {
                        webform.getPane().getDndHandler().clearDropMatch();
                        pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    } else {
                        pane.setCursor(webform.getManager().getLinkedCursor());
                    }
                } else {
                    pane.setCursor(webform.getManager().getLinkedCursor());
                }
            } else if (!grid) {
//                Position pos = ModelViewMapper.viewToModel(webform, px, py);
                DomPosition pos = ModelViewMapper.viewToModel(webform, px, py);

//                if (pos != Position.NONE) {
                if (pos != DomPosition.NONE) {
                    webform.getPane().showCaret(pos);
                    pane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                } else {
                    webform.getPane().hideCaret();
                    pane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            } else {
                webform.getPane().hideCaret();
                pane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            pane.repaintDirty(false);

            e.consume();

            // Show position of component in the status line
            if ((n > 0) && (action != LINKING)) {
                int i = 0;
                Rectangle r = (Rectangle)selections.get(i);
                int x;
                int y;

                if (action == DRAG_FREE) {
                    x = r.x + prevX;
                    y = r.y + prevY;
                } else if (hasMoved(prevX, prevY)) {
//                    x = gm.snapX(r.x + prevX, getPositionedBy(i));
//                    y = gm.snapY(r.y + prevY, getPositionedBy(i));
                    x = webform.snapX(r.x + prevX, getPositionedBy(i));
                    y = webform.snapY(r.y + prevY, getPositionedBy(i));
                } else {
                    x = r.x + startX;
                    y = r.y + startY;
                }

//                StatusDisplayer_RAVE.getRaveDefault().setPositionLabelText(x + "," + y);
            }
        }
    }

    // --- implements KeyListener ---
    public void keyPressed(KeyEvent e) {
        if (action != getDragAction(e)) {
            update(e, prevMouseX, prevMouseY);
        }
    }

    public void keyReleased(KeyEvent e) {
        if (action != getDragAction(e)) {
            update(e, prevMouseX, prevMouseY);
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    private int getDragAction(InputEvent e) {
        if (e.isShiftDown()) {
            if (e.isControlDown()) {
                return LINKING;
            } else {
                return DRAG_FREE;
            }
        } else {
            return DRAG_GRID;
        }
    }

////    private DesignBean getLinkParticipant(int x, int y) {
//    private Element getLinkParticipant(int x, int y) {
////        Object droppedUpon = webform.getMapper().findComponent(x, y);
////
////        if (droppedUpon instanceof DesignBean) {
////            return (DesignBean)droppedUpon;
////        }
////
////        return null;
////        return ModelViewMapper.findComponent(webform.getPane().getPageBox(), x, y);
////        return ModelViewMapper.findMarkupDesignBean(webform.getPane().getPageBox(), x, y);
//        return ModelViewMapper.findElement(webform.getPane().getPageBox(), x, y);
//    }
}
