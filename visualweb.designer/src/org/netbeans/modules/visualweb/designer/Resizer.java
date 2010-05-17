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

import java.util.Arrays;
import org.netbeans.modules.visualweb.api.designer.DomProviderService.ResizeConstraint;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;

import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


/**
 * Handle drawing (and "undrawing"!) a resize outline, as well as
 * communicating a resize event to the GridHandler on completion
 *
 * @todo For aspect-preserving resizing, rather than basing the
 *   proportion on the original size of the box when resizing begins,
 *   base it on the -intrinsic- size of the box. For an image for
 *   example, that should be the size of the image. That will avoid
 *   having repeated resizings of the image (resulting in rounding
 *   errors) gradually breaking the correct aspect of the image.
 *   It will also "restore" the aspect if it has been non-aspect
 *   resized in the other dimensions.
 *
 * @author Tor Norbye
 */
public class Resizer extends Interaction implements KeyListener {
    /* Restore previous cursor after operation. */
    protected transient Cursor previousCursor = null;
    private WebForm webform;
    private boolean snapDisabled = false;
//    private MarkupDesignBean component;
    private Element componentRootElement;
    private int origW = 0;
    private int origH = 0;
    private int origX = 0;
    private int origY = 0;
    private int direction = 0;
    private int prevX = -500;
    private int prevY = -500;
    private int prevMouseX = -500;
    private int prevMouseY = -500;
    private boolean preserveAspect;
    private final CssBox box;

    /** Current size of the rectangle being resized. */
    private Rectangle currentSize = null;

    /**
     * Create a resizer which tracks resize mouse operations.
     * @param element The element to be resized.
     * @param direction The direction to resize the view.
     * @param alloc The original shape of the view/component being resized
     * @param preserveAspect Whether to preserve aspect on two-dimension resizing
     *   (e.g. dragging the corners - NE, NW, SE, SW)
     */
    public Resizer(WebForm webform, Element componentRootElement, /*MarkupDesignBean component,*/ CssBox box, int direction,
        Shape alloc, boolean preserveAspect) {
        this.webform = webform;
//        this.component = component;
        this.componentRootElement = componentRootElement;
        this.direction = direction;
        this.box = box;

        Rectangle r = (alloc instanceof Rectangle) ? (Rectangle)alloc : alloc.getBounds();

        // We copy out the attributes since I've got a really bad experience
        // with allocations getting mutated by other code in the View code.
        // (For performance reasons they're probably reusing the same Rectangles
        // over and over.)
        origX = r.x;
        origY = r.y;
        origW = r.width;
        origH = r.height;

        // Handle <img> tags. All components that render to a top-level
        // <img> tag should be resized proportionally. We can't easily
        // do this with metadata in all cases since for example
        // a button should have aspect-preserving resizing only when it's
        // an image button
//        RaveElement element = (RaveElement)component.getElement();
//        RaveElement rendered = element.getRendered();
//        Element element = component.getElement();
//        Element rendered = MarkupService.getRenderedElementForElement(element);
//
//        if (rendered != null) {
//            element = rendered;
//        }
        Element element = componentRootElement;

        String tagName = element.getTagName();

        if (HtmlTag.IMG.name.equals(tagName) ||
                (HtmlTag.INPUT.name.equals(tagName) &&
                element.getAttribute(HtmlAttribute.TYPE).equals("image"))) { // NOI18N
            preserveAspect = true;
        }

        this.preserveAspect = preserveAspect;

        if ((origW == 0) || (origH == 0)) {
            this.preserveAspect = false;
        }
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
        try {
            if ((e != null) && !e.isConsumed()) {
                updateSnapState(e);

                Point p = e.getPoint();
                DesignerPane pane = webform.getPane();
                pane.removeKeyListener(this);

//                GridHandler gm = GridHandler.getInstance();
//                GridHandler gm = webform.getGridHandler();
                Rectangle r = new Rectangle();

                int x;
                int y;

                if (snapDisabled) {
                    x = p.x;
                    y = p.y;
                } else {
//                    GridHandler gm = GridHandler.getDefault();
//                    x = gm.snapX(p.x, box.getPositionedBy());
//                    y = gm.snapY(p.y, box.getPositionedBy());
                    x = webform.snapX(p.x, box.getPositionedBy());
                    y = webform.snapY(p.y, box.getPositionedBy());
                }

                resize(r, x, y);

//                if ((component != null) && (r.width > 0) && (r.height > 0)) {
                if ((componentRootElement != null) && (r.width > 0) && (r.height > 0)) {
                    boolean resizeHorizontally = origW != r.width;
                    boolean resizeVertically = origH != r.height;

                    // The width/height used in the Resizer includes the size of the borders.
                    // However, the width we set on the component should only be the
                    // size of the content box (see CSS box model).                    
                    Insets insets = box.getCssSizeInsets();
                    r.width -= insets.left;
                    r.width -= insets.right;
                    r.height -= insets.top;
                    r.height -= insets.bottom;

                    if (!preserveAspect &&
                            ((direction == Cursor.S_RESIZE_CURSOR) ||
                            (direction == Cursor.N_RESIZE_CURSOR))) {
                        resizeHorizontally = false;
                    }

                    if (!preserveAspect &&
                            ((direction == Cursor.E_RESIZE_CURSOR) ||
                            (direction == Cursor.W_RESIZE_CURSOR))) {
                        resizeVertically = false;
                    }

//                    gm.resize(pane, component, r.x, origX != r.x, r.y, origY != r.y, r.width,
//                        resizeHorizontally, r.height, resizeVertically, box, snapDisabled);
//                    gm.resize(pane, componentRootElement, r.x, origX != r.x, r.y, origY != r.y, r.width,
//                        resizeHorizontally, r.height, resizeVertically, box, snapDisabled);
                    webform.getDomDocument().resizeComponent(webform, componentRootElement, r.x, origX != r.x, r.y, origY != r.y, r.width,
                        resizeHorizontally, r.height, resizeVertically, box, !snapDisabled);
                }

                /*
                  TODO:
                  We should have cursor feedback (error-cursor)
                  showing that releasing the cursor will not
                  resize the rectangle to a negative/zero length
                */
                cleanup(pane);

                e.consume();
            }
        } finally {
//            component = null;
            componentRootElement = null;
            previousCursor = null;
            currentSize = null;
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
        webform.getPane().scrollRectToVisible(new Rectangle(p));
    }

    /** Set the given rectangle to contain the new bounds for
     * the view (originally anchored at origX,origY,origW,origH) constrained
     * by resize direction to the new coordinate x,y. */
    private void resize(Rectangle r, int x, int y) {
        switch (direction) {
        case Cursor.S_RESIZE_CURSOR:
            r.x = origX;
            r.y = origY;
            r.width = origW;
            r.height = (y - origY);

            break;

        case Cursor.E_RESIZE_CURSOR:
            r.x = origX;
            r.y = origY;
            r.width = (x - origX);
            r.height = origH;

            break;

        case Cursor.SE_RESIZE_CURSOR:

            if (preserveAspect) {
                r.x = origX;
                r.y = origY;

                // We know origW and origH is nonzero when preserveAspect is set
                // (enforced in constructor)
                int dx = origW;
                int dy = origH;

                // Signed distance from x,y to the line (origX,origY,origW,origH)
                // (((x - origX) * dy) - ((y - origY) * dx)) / sqrt(dx*dx+dy*dy)
                // However, I only care about the sign of the distance to determine
                // if I should use x or y as the chosen dimension to assign
                // (and compute the aspect ratio for the other dimension)
                int x0 = origX;
                int y0 = origY;
                int d = ((x - x0) * dy) - ((y - y0) * dx);

                if (d > 0) {
                    r.width = x - origX;
                    r.height = (r.width * origH) / origW;
                } else {
                    r.height = y - origY;
                    r.width = (r.height * origW) / origH;
                }
            } else {
                r.x = origX;
                r.y = origY;
                r.width = (x - origX);
                r.height = (y - origY);
            }

            break;

        case Cursor.W_RESIZE_CURSOR:
            r.x = x;
            r.y = origY;
            r.width = (origX - x) + origW;
            r.height = origH;

            break;

        case Cursor.SW_RESIZE_CURSOR:

            if (preserveAspect) {
                int dx = origW;
                int dy = -origH;
                int x0 = origX;
                int y0 = origY + origH;
                int d = ((x - x0) * dy) - ((y - y0) * dx);

                if (d > 0) {
                    r.width = origX - x + origW;
                    r.height = (r.width * origH) / origW;
                } else {
                    r.height = y - origY;
                    r.width = (r.height * origW) / origH;
                }

                r.x = (origX + origW) - r.width;
                r.y = origY;
            } else {
                r.x = x;
                r.y = origY;
                r.width = (origX - x) + origW;
                r.height = (y - origY);
            }

            break;

        case Cursor.N_RESIZE_CURSOR:
            r.x = origX;
            r.y = y;
            r.width = origW;
            r.height = (origY - y) + origH;

            break;

        case Cursor.NW_RESIZE_CURSOR:

            if (preserveAspect) {
                int dx = origW;
                int dy = origH;
                int x0 = origX;
                int y0 = origY;
                int d = ((x - x0) * dy) - ((y - y0) * dx);

                if (d < 0) {
                    r.width = origX - x + origW;
                    r.height = (r.width * origH) / origW;
                } else {
                    r.height = origY - y + origH;
                    r.width = (r.height * origW) / origH;
                }

                r.x = (origX + origW) - r.width;
                r.y = (origY + origH) - r.height;
            } else {
                r.x = x;
                r.y = y;
                r.width = (origX - x) + origW;
                r.height = (origY - y) + origH;
            }

            break;

        case Cursor.NE_RESIZE_CURSOR:

            if (preserveAspect) {
                int dx = origW;
                int dy = -origH;
                int x0 = origX;
                int y0 = origY + origH;
                int d = ((x - x0) * dy) - ((y - y0) * dx);

                if (d < 0) {
                    r.width = x - origX;
                    r.height = (r.width * origH) / origW;
                } else {
                    r.height = origY - y + origH;
                    r.width = (r.height * origW) / origH;
                }

                r.x = origX;
                r.y = (origY + origH) - r.height;
            } else {
                r.x = origX;
                r.y = y;
                r.width = (x - origX);
                r.height = (origY - y) + origH;
            }

            break;

        default:
            Thread.dumpStack();
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
            updateSnapState(e);

            Point p = e.getPoint();
            prevMouseX = p.x;
            prevMouseY = p.y;

            DesignerPane pane = webform.getPane();
            pane.addKeyListener(this);

            previousCursor = pane.getCursor();
            pane.setCursor(Cursor.getPredefinedCursor(direction));

            ImageIcon imgIcon =
                new ImageIcon(Resizer.class.getResource("/org/netbeans/modules/visualweb/designer/resources/drag_resize.gif"));
//            StatusDisplayer_RAVE.getRaveDefault().setPositionLabelIcon(imgIcon);

            e.consume();
        }
    }

    private void updateSnapState(InputEvent e) {
        snapDisabled = e.isShiftDown();
    }

    private void update(InputEvent e, int px, int py) {
        if (!e.isConsumed()) {
            updateSnapState(e);

            DesignerPane pane = webform.getPane();

            Rectangle dirty;

            if (currentSize != null) {
                dirty = currentSize;
                dirty.width++;
                dirty.height++;
            } else {
                dirty = new Rectangle();
            }

//            GridHandler gm = GridHandler.getInstance();
//            GridHandler gm = webform.getGridHandler();

            if (snapDisabled) {
                prevX = px;
                prevY = py;
            } else {
//                GridHandler gm = GridHandler.getDefault();
//                prevX = gm.snapX(px, box.getPositionedBy());
//                prevY = gm.snapY(py, box.getPositionedBy());
                prevX = webform.snapX(px, box.getPositionedBy());
                prevY = webform.snapY(py, box.getPositionedBy());
            }

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

//            StatusDisplayer_RAVE.getRaveDefault().setPositionLabelText(w + "," + h);

            e.consume();
        }
    }

    // --- implements KeyListener ---
    public void keyPressed(KeyEvent e) {
        if (snapDisabled != e.isShiftDown()) {
            update(e, prevMouseX, prevMouseY);
        }
    }

    public void keyReleased(KeyEvent e) {
        if (snapDisabled != e.isShiftDown()) {
            update(e, prevMouseX, prevMouseY);
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    /** Look up the resize constraints for the given component */
    public static /*int*/ResizeConstraint[] getResizeConstraints(WebForm webForm, /*MarkupDesignBean component*/ Element componentRootElement) {
//        Element element = component.getElement();
        Element element = componentRootElement;

        if (element != null) {
//            CssBox box = CssBox.getBox(element);
            CssBox box = webForm.findCssBoxForElement(element);

            // Non-replaced inline formatted components are not resizable!
            // See CSS2.1 spec, section 10.2
            if ((box != null) && box.isInlineBox() && !box.isReplacedBox() &&
                    box.getBoxType().isNormalFlow()) {
//                return Constants.ResizeConstraints.NONE;
                return new ResizeConstraint[0];
            }
        }

//        int constraints = Constants.ResizeConstraints.ANY;
//
//        // Special case: The Jsp Include box is not resizable.
//        // If I build a BeanDescriptor for it I can inject
//        // this value right on it, but I also want to make it
//        // as NOT POSITIONABLE.
//        if (component.getInstance() instanceof Jsp_Directive_Include) {
//            return Constants.ResizeConstraints.NONE;
//        }
//
//        BeanInfo bi = component.getBeanInfo();
//
//        if (bi != null) {
//            BeanDescriptor bd = bi.getBeanDescriptor();
//            Object o = bd.getValue(Constants.BeanDescriptor.RESIZE_CONSTRAINTS);
//
//            if ((o != null) && o instanceof Integer) {
//                constraints = ((Integer)o).intValue();
//            }
//        }
//
//        return constraints;
        return webForm.getDomProviderService().getResizeConstraintsForComponent(componentRootElement);
    }
    
    public static boolean hasMaintainAspectRatioResizeConstraint(ResizeConstraint[] resizeConstraints) {
        if (resizeConstraints == null) {
            return false;
        }
        return Arrays.asList(resizeConstraints).contains(ResizeConstraint.MAINTAIN_ASPECT_RATIO);
    }
    
    public static boolean hasTopResizeConstraint(ResizeConstraint[] resizeConstraints) {
        if (resizeConstraints == null) {
            return false;
        }
        return Arrays.asList(resizeConstraints).contains(ResizeConstraint.TOP);
    }
    
    public static boolean hasLeftResizeConstraint(ResizeConstraint[] resizeConstraints) {
        if (resizeConstraints == null) {
            return false;
        }
        return Arrays.asList(resizeConstraints).contains(ResizeConstraint.LEFT);
    }
    
    public static boolean hasRightResizeConstraint(ResizeConstraint[] resizeConstraints) {
        if (resizeConstraints == null) {
            return false;
        }
        return Arrays.asList(resizeConstraints).contains(ResizeConstraint.RIGHT);
    }
    
    public static boolean hasBottomResizeConstraint(ResizeConstraint[] resizeConstraints) {
        if (resizeConstraints == null) {
            return false;
        }
        return Arrays.asList(resizeConstraints).contains(ResizeConstraint.BOTTOM);
    }
    
}
