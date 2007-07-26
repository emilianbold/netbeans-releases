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
package org.netbeans.modules.visualweb.css2;

import org.netbeans.modules.visualweb.designer.CssUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.ErrorManager;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.designer.ColorManager;
import org.netbeans.modules.visualweb.designer.DesignerPane;
import org.netbeans.modules.visualweb.designer.DesignerUtils;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


/**
 * Represents the page/document <body> tag - the initial containing block.
 * <b>Note: you must</b> call setViewport on
 * this component after it has been added to a JScrollPane; otherwise,
 * it will not relayout after a resize.
 * @todo This box is also used (as an ancestor class) for frames/iframes
 *  now. Rename it to DocumentBox - and put the page specific stuff into
 *  a subclass which doesn't share with FrameBox?
 *
 * @author Tor Norbye.
 */

public class PageBox extends DocumentBox implements ChangeListener {
    private boolean dark;
    private Color constraintsColor = null;
    private Color constraintsColorLight = null;

    /** True if this pagebox represents a top level box like a <body> or the
     * outermost <frameset> tag */
    protected boolean isTopLevel = true;
    private int componentVisibleWidth = Integer.MAX_VALUE;
    private int componentVisibleHeight = Integer.MAX_VALUE;
    private CssBox selected;

    /** Creates a new instance of PageBox suitable for a root level
     * document. Use Factory method instead.
     */
    private PageBox(DesignerPane pane, WebForm webform, Element body) {
        this(pane, webform, body, BoxType.STATIC, false, false);
    }

    /** Creates a new instance of PageBox */
    protected PageBox(DesignerPane pane, WebForm webform, Element body, BoxType boxType,
        boolean inline, boolean replaced) {
        // XXX What do we pass in as a containing block?
        super(pane, webform, body, boxType, inline, replaced);
        this.pane = pane;
        this.body = body;

        if (bg == null) {
            // Default to gray instead of white so that stylesheet switch
            // to white is a visual change
            //bg = new Color(240, 240, 240); // #f0f0f0
            //bg = new Color(245, 245, 245); // SVG "whitesmoke", #f5f5f5
            bg = Color.WHITE;
        }

        x = 0;
        y = 0;
        width = 0;
        height = 0;
    }

    /** Creates a new instance of PageBox suitable for a root level
     * document.
     */
    public static PageBox getPageBox(DesignerPane pane, WebForm webform, Element element) {
        if (element.getTagName().equals(HtmlTag.FRAMESET.name)) {
            return FrameSetBox.getFrameSetBox(pane, webform, element, BoxType.STATIC,
                HtmlTag.FRAMESET, false);
        }

        return new PageBox(pane, webform, element);
    }

    /** Called when the page box is added to a box hierarchy */
    public void boxAdded() {
    }

    /** Called when the page box is removed/getting unused */
    public void boxRemoved() {
        if (viewport != null) {
            viewport.removeChangeListener(this);
        }
    }

    /** Return the page background color */
    public Color getBackground() {
        return bg;
    }

    public void paint(Graphics g) {
        paint(g, 0, 0); // NOT getAbsoluteX()/getAbsoluteY(), since paint will call super which adds them in
    }

    // Get rid of selected box after a remove
    public void removed(Node node, Node parent) {
        super.removed(node, parent);
        selected = null;
    }

    public void paint(Graphics g, int px, int py) {
        if (!isTopLevel) {
            super.paint(g, px, py);

            return;
        }

        //if (debugpaint) {
        //    Log.err.log("PageBox.paint - layotValid=" + layoutValid);
        //    Log.err.log("background=" + bg);
        //}
        layout();

        if (!layoutValid) {
            return;
        }

        super.paint(g, px, py);

        // FOR DEBUGGING PURPOSES ONLY
        if (org.netbeans.modules.visualweb.designer.InteractionManager.ENABLE_DOM_INSPECTOR && (selected != null) &&
                (selected != this)) {
            int bx = selected.getAbsoluteX();
            int by = selected.getAbsoluteY();
            int bw = selected.getWidth();
            int bh = selected.getHeight();

            if ((Math.abs(bx) < 50000) && (Math.abs(by) < 50000) && (Math.abs(bw) < 50000) &&
                    (Math.abs(bh) < 50000)) {
                g.setColor(Color.RED);
                g.drawRect(bx, by, bw, bh);
            }
        }

        // Constrain
//        DesignerSettings designerSettings = DesignerSettings.getInstance();
//        if (designerSettings.getPageSizeWidth() != -1) {
//            int x2 = designerSettings.getPageSizeWidth();
//            int y2 = designerSettings.getPageSizeHeight();
        if (webform.getPageSizeWidth() != -1) {
            int x2 = webform.getPageSizeWidth();
            int y2 = webform.getPageSizeHeight();

            if (constraintsColor == null) {
                constraintsColor = new Color(128, 128, 128, 208);
                constraintsColorLight = new Color(216, 216, 216, 96);
            }

            int w = width;
            int h = height;

            g.setColor(constraintsColor);

            if (w > x2) {
                g.fillRect(x2, 0, w - x2, h);
            }

            g.setColor(constraintsColorLight);

            if (h > y2) {
                g.fillRect(0, y2, x2, h - y2);
            }

            g.setColor(Color.WHITE);
            g.drawString("(" + x2 + "," + y2 + ")", x2 + 10, y2 + 10);
//        } else if (webform.isFragment() || webform.isPortlet()) {
        } else if (webform.isPaintSizeMask()) {
            // See if we should draw page-fragment/portlet size mask
            Element e = webform.getHtmlBody();

            if (e != null) {
//                int w = CssLookup.getLength(e, XhtmlCss.WIDTH_INDEX);
                int w = CssUtilities.getCssLength(e, XhtmlCss.WIDTH_INDEX);

                if (w != AUTO) {
                    int h = CssUtilities.getCssLength(e, XhtmlCss.HEIGHT_INDEX);

                    if (h != AUTO) {
                        int x2 = w;
                        int y2 = h;

                        if (constraintsColor == null) {
                            constraintsColor = new Color(128, 128, 128, 208);
                            constraintsColorLight = new Color(216, 216, 216, 96);
                        }

                        //g.setColor(constraintsColor);
                        g.setColor(constraintsColorLight);

                        if (width > x2) {
                            g.fillRect(x2, 0, width - x2, height);
                        }

                        if (height > y2) {
                            g.fillRect(0, y2, x2, height - y2);
                        }
                    }
                }
            }
        }
    }

    protected void paintBox(Graphics g, int x, int y, int w, int h) {
        webform.getColors().sync();

        if (!isTopLevel || (viewport == null)) {
            super.paintBox(g, x, y, w, h);

            return;
        }

        int originalX = x;
        int originalY = y;
        Dimension d = viewport.getExtentSize();
        Point p = viewport.getViewPosition();
        x = p.x;
        y = p.y;

        //        int originalWidth = w;
        //        int originalHeight = h;
        w = d.width;
        h = d.height;

        // XXX Shouldn't this border be painted like the background relative to the
        // box dimensions, not the viewport???
        if (border != null) {
            // TODO: The border should be painted using the original coordinate
            // system too! Otherwise if you set a border on the body, and
            // put a wide pre inside it, and move the scrollbar notice how
            // the border is moving with the viewport - it should not
            //border.paintBorder(g, originalX, originalY, originalWidth, originalHeight);
            border.paintBorder(g, x, y, w, h);
        }

        // NO! Don't do this if a particular border edge is dashed
        // or none - in that case the border should "shine through!
        x += leftBorderWidth;
        y += topBorderWidth;
        w -= leftBorderWidth;
        w -= rightBorderWidth;
        h -= topBorderWidth;
        h -= bottomBorderWidth;

        if (bg != null) {
            g.setColor(bg);
            g.fillRect(x, y, w, h);
        } // XXX should I do an else here??? Am I "double" painting the background?

        if (bgPainter != null) {
            // According to the CSS spec section 1.4.21, the background
            // image covers the padding rectangle.
            // Also, paint at the original position, not the viewport position since
            // the image should move when you drag the scrollbars
            bgPainter.paint(g, originalX, originalY, w + (x - originalX), h + (y - originalY));
        }

//        if (grid && GridHandler.getInstance().grid()) {
//        if (grid && getWebForm().getGridHandler().grid()) {
//        if (grid && GridHandler.getDefault().isGrid()) {
        if (grid && webform.isGridShow()) {
            if (hidden && !(this instanceof PageBox)) { // paint grid for the root pagebox!

                return;
            }

            paintGrid(g, x, y, w, h);
        }
    }

    protected void paintGrid(Graphics g, int x, int y, int w, int h) {
        if (!isTopLevel || (viewport == null)) {
            return;
        }

        Dimension d = viewport.getExtentSize();
        Point p = viewport.getViewPosition();

        // We're not painting the box' own boundaries, instead
        // we're painting the visible viewport. Because of that,
        // we have to make sure the grid is aligned with its
        // original origin, not the current scrollbar's origin,
        // so snap the viewport origin to the grid size.
        int px = p.x;
        int py = p.y;
        int width = d.width;
        int height = d.height;

        if (DesignerPane.INCREMENTAL_LAYOUT) {
            // Narrow region if possible
            // TODO: "clip" the beginning position too. Perhaps only
            // remove to a multiple of the starting position
            if (px < DesignerPane.clip.x) {
                width -= (DesignerPane.clip.x - px);
                px = DesignerPane.clip.x;
            }

            if (py < DesignerPane.clip.y) {
                height -= (DesignerPane.clip.y - py);
                py = DesignerPane.clip.y;
            }

            if ((px + width) > DesignerPane.clipBr.x) {
                width = DesignerPane.clipBr.x - px;
            }

            if ((py + height) > DesignerPane.clipBr.y) {
                height = DesignerPane.clipBr.y - py;
            }
        }

//        GridHandler gh = GridHandler.getInstance();
//        GridHandler gh = getWebForm().getGridHandler();
//        GridHandler gh = GridHandler.getDefault();
//        int gridWidth = gh.getGridWidth();
//        int gridHeight = gh.getGridHeight();
        int gridWidth = webform.getGridWidth();
        int gridHeight = webform.getGridHeight();
        int xOffset = (px % gridWidth);
        int yOffset = (py % gridHeight);
        super.paintGrid(g, px - xOffset, py - yOffset, width + xOffset, height + yOffset);
    }

    public void setViewport(JViewport viewport) {
        if (this.viewport != null) {
            this.viewport.removeChangeListener(this);
        }

        this.viewport = viewport;
    }

    public JViewport getViewport() {
        return viewport;
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == this.viewport) {
            boolean scrolled = false;
            Point p = viewport.getViewPosition();

            if ((p.x != viewportX) || (p.y != viewportY)) {
                viewportX = p.x;
                viewportY = p.y;
                scrolled = true;
                webform.getSelection().notifyScrolled();
            }

            if ((fixedBoxes != null) && scrolled) {
                updateFixedPositions();
                pane.repaint();

                return;
            } else if (viewport.getParent() instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)viewport.getParent();
                Dimension extentSize = scrollPane.getSize();

                if (componentVisibleWidth != extentSize.width) {
                    componentVisibleWidth = extentSize.width;
                    componentVisibleHeight = extentSize.height;

                    if (layoutValid) {
                        layoutValid = false;
                        pane.repaint();
                    }

                    return;
                } else if (componentVisibleHeight != extentSize.height) {
                    componentVisibleHeight = extentSize.height;

                    // we already know componentVisibleWidth is right
                    if (layoutValid) { // XXX should I force a recompute even if layoutValid? is ok?
                        layoutValid = false;
                        // XXX #110849 Fixing the issue with bad relayout.
                        // All this construct (layoutValid = false; repaint is very suspiscous
                        // investigate whether rather relayout shouldn't be used instead.
//                        currWidth = -1; // force recompute
                        pane.repaint();
                    }

                    return;
                }
            } else {
                ErrorManager.getDefault().log("Unexpected viewport parent");
            }
        }
    }

    /** Update all the positions of fixed boxes for the new position
     * of the viewport.
     */
    private void updateFixedPositions() {
        for (int i = 0, n = fixedBoxes.size(); i < n; i++) {
            CssBox box = fixedBoxes.get(i);
            box.setLocation(box.left + viewportX, box.top + viewportY);
        }
    }

    /**
     * Layout the page hierarchy.
     */
    public void layout() {
        if (layoutValid) {
            return;
        }

        //MarkupService.clearErrors(true);
        int initialWidth = 0;
        int initialHeight = 0;

        // See if we can find the viewport
        if (viewport != null) {
            // TODO - get rid of the viewport attribute of this bean;
            // switch to scrollbar entirely
            // I ran into this problem where a table which has a 5px margin
            // and a width: 100%, would start the system "oscillating":
            // the table width would get computed to 100% of the body width,
            // which of course is the viewport size. The margin would get tacked
            // on, which would make the width slightly wider than the viewport.
            // This would then become the new width - so the scrollbars are
            // added by the viewport when we set the new size. This of course
            // causes a resize event, and we now read the new viewport width.
            // This viewport width is ~20px smaller since the horizontal scrollbar
            // has been added. This means we end up computing a total width
            // which is less than the width required by the scrollpane, so it
            // removes the scrollbar - a new resize event comes in, and (start
            // reading this paragraph over again - oscillation.)
            // SOOOOOO, instead, we don't want to know the size of the viewport,
            // we want to know the full size of the scrollpane - without
            // scrollbars.
            if (viewport.getParent() instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)viewport.getParent();
                Dimension extentSize = scrollPane.getSize();
                initialWidth = extentSize.width;
                componentVisibleWidth = extentSize.width;
                componentVisibleHeight = extentSize.height;
                initialHeight = extentSize.height;

                Insets insets = scrollPane.getInsets();
                initialWidth -= insets.left;
                initialWidth -= insets.right;
                initialHeight -= insets.top;
                initialHeight -= insets.bottom;

                // Subtract 1 from each to avoid adding a scrollbar when we reach
                // exactly the width. - e.g. a <table width="100%"> shouldn't
                // cause a scrollbar, it should extend right up to the width.
                initialWidth--;
                initialHeight--;
            } else {
                Dimension extentSize = viewport.getExtentSize();
                initialWidth = extentSize.width;
                initialHeight = extentSize.height;
                componentVisibleWidth = initialWidth;
                componentVisibleHeight = initialHeight;
            }
        } else {
            initialWidth = maxWidth;
        }

        if (initialWidth <= 0) {
            return;
        }

        int wrapWidth = initialWidth;

//        DesignerSettings designerSettings = DesignerSettings.getInstance();
//        if (designerSettings.getPageSizeWidth() != -1) {
//            int w = designerSettings.getPageSizeWidth();
        if (webform.getPageSizeWidth() != -1) {
            int w = webform.getPageSizeWidth();

            if (w < wrapWidth) {
                wrapWidth = w;
            }
        }

        relayout(viewport, initialWidth, initialHeight, wrapWidth);

        if (!webform.isGridMode()) {
            DesignerPane pane = webform.getPane();

//            if ((pane != null) &&
//                    ((pane.getCaret() == null) || (pane.getCaretPosition() == Position.NONE))) {
//            if ((pane != null) && ((pane.getCaret() == null) || (pane.getCaretPosition() == DomPosition.NONE))) {
            if ((pane != null) && (!pane.hasCaret() || (pane.getCaretDot() == DomPosition.NONE))) {
                pane.showCaretAtBeginning();
            }
        }
    }

    protected void updateSizeInfo() {
        // The <body> tag needs special handling of margins: unlike all
        // other boxes, the margin for the body should be filled in with
        // the color of the body box - e.g. act like padding
        //width += leftMargin + rightMargin;
        //height += effectiveTopMargin + effectiveBottomMargin;
        super.updateSizeInfo();

        if (viewport != null) {
            // Attempt to preserve the relative scroll position
            Point p = viewport.getViewPosition();
            Dimension d = viewport.getViewSize();

            // Avoid resize-notification of our own changes
            viewport.removeChangeListener(this);

            viewport.setViewSize(new Dimension(width, height));

            // Scroll the view such that it stays relatively in the same place
            // but accomodates the new view size
            if ((p.x > 0) || (p.y > 0)) {
                if (d.width > 0) {
                    p.x = (p.x * width) / d.width;
                } else {
                    p.x = 0;
                }

                if (d.height > 0) {
                    p.y = (p.y * height) / d.height;
                } else {
                    p.y = 0;
                }

                viewport.setViewPosition(p);
            }

            // Restore listener
            viewport.addChangeListener(this);
        }
    }

    public void setSize(int width, int height) {
        //Log.err.log("************************************************************************************\nPageBox.setSize - width= " + width + "\nLayoutValid was " + layoutValid + "  and contextwidth=" + (context != null ? Integer.toString(layoutWidth) : "null"));
        if (!layoutValid) {
            maxWidth = width;

            //setWidth(width);
            //setHeight(height);
            //layout();
        }
    }

    public void relayout(FormatContext context) {
        layout();
    }

    public void redoLayout(boolean immediate) {
        // This seems to be redundant (see only usages in designer/jsf (redoPaneLayout).
////        if (webform.getDomSynchronizer().isRefreshPending()) {
//        if (webform.isRefreshPending()) {
//            // Change will happen soon anyway
//            return;
//        }

        // XXX We are not sharing any output window from here.
//        InSyncService.getProvider().getRaveErrorHandler().clearErrors(true);
        initializeInvariants();
        super.redoLayout(immediate);

        if (pane != null) {
            pane.repaint();
        }
    }

    // XXX why is this necessary?
    public float getPreferredSpan(int axis) {
        if (!layoutValid) {
            if (context != null) {
                if (viewport != null) {
                    return (axis == X_AXIS) ? Math.max(layoutWidth, viewport.getExtentSize().width)
                                            : Math.max(layoutHeight, viewport.getExtentSize().height);
                } else {
                    return (axis == X_AXIS) ? layoutWidth : layoutHeight;
                }
            } else {
                return 1;
            }
        }

        if (layoutValid && (context != null)) {
            if (viewport != null) {
                return (axis == X_AXIS) ? Math.max(layoutWidth, viewport.getExtentSize().width)
                                        : Math.max(layoutHeight, viewport.getExtentSize().height);
            } else {
                return (axis == X_AXIS) ? layoutWidth : layoutHeight;
            }
        } else {
            return (axis == X_AXIS) ? getWidth() : getHeight();
        }
    }

    /**
     * Determines the minimum span for this view along an
     * axis.
     *
     * @param axis may be either <code>View.X_AXIS</code> or
     *          <code>View.Y_AXIS</code>
     * @return  the minimum span the view can be rendered into
     * @see Box#getPreferredSpan
     */
    public float getMinimumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    /**
     * Determines the maximum span for this view along an
     * axis.
     *
     * @param axis may be either <code>View.X_AXIS</code> or
     *          <code>View.Y_AXIS</code>
     * @return  the maximum span the view can be rendered into
     * @see Box#getPreferredSpan
     */
    public float getMaximumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    protected String paramString() {
        return "PageBox[" + super.paramString() + "]-element=" + getElement();
    }

    // FOR DEBUGGING PURPOSES ONLY!
    public void setSelected(CssBox box) {
        if (box != this.selected) {
            this.selected = box;
            pane.repaint();
        }
    }

    public CssBox getSelected() {
        return selected;
    }

    /** Paint a preview of the page into an image and return it, using
     * the specified width and height.
     * @todo Turn off the design-time grid, and selections, when painting
     *   the preview!
     * @todo What about the "rendered" fields in XhtmlText and XhtmlElement;
     *   will my relayout here cause new fragments to be created which
     *   will change the source pointers away from the designer page's own
     *   rendered fragments being shown, thus breaking model-to-view (caret)
     *   mapping etc?
     */
    public Image createPreviewImage(int width, int height) {
        // XXX rename method!!! how about drawPagePreview? Or perhaps drawThumbnail
        // since it uses anti aliasing etc.
        if (!layoutValid) {
            maxWidth = 1024;
        }

//        // XXX
//        CssBox.noBoxPersistence = true;

        try {
            layout();
        } catch (Exception e) {
            // XXX Why there were eaten all exceptions here?
            e.printStackTrace();
            return null;
        }
//        finally {
//            CssBox.noBoxPersistence = false;
//        }

        if (!layoutValid) {
            return null;
        }

        Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D og = (Graphics2D)image.getGraphics();

        try {
            og.setClip(0, 0, width, height);

            //og.setColor(new Color(0, 255, 0, 0));
            // Some components are transparent, so we have to
            // paint the background. XXX I should ask them so
            // I don't have to waste time here!!!
            og.setColor(getBackground());
            og.fillRect(0, 0, width, height);

            if (this.width > 0) {
                // XXX how do I set the clip?
                og.setClip(0, 0, this.width, (this.width * height) / width);

                AffineTransform aT = og.getTransform();

                //og.transform(...);
                double scale = width / (double)this.width;

                // TODO - scale such that a really tall page is shown correctly
                // too - e.g. pick max or the two scale factors horiz/vert
                og.scale(scale, scale);

                // Without antialiasing, the scaled down image looks
                // REALLY bad.
                og.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                og.getClipBounds(DesignerPane.clip);
                DesignerPane.clipBr.x = DesignerPane.clip.x + DesignerPane.clip.width;
                DesignerPane.clipBr.y = DesignerPane.clip.y + DesignerPane.clip.height;

                paint(og);
                og.setTransform(aT);
            }
        } finally {
            og.dispose();
        }

        return image;
    }

    /** Paint a preview of the given component, with the given CSS style
     * applied, and return it as an image. Use the preferred initial
     * width, unless the component is larger.
     */
    public BufferedImage paintCssPreview(Graphics2D g2d, String cssStyle,
    /*MarkupDesignBean bean,*/ Element componentRootElement, DocumentFragment df, Element element,
    int initialWidth, int initialHeight) {
//        if (initialWidth == 0) {
//            // Ensure that we don't force wrapping on components like a composite
//            // breadcrumbs by giving it some space to work with.
//            initialWidth = 600;
//        }
//
//        // Distinguish between the bean we're going to -render- and the one we're
//        // going to apply the differente properties to
//        MarkupDesignBean renderBean = bean;
//
////        // Handle hyperlinks. We really need to render its surrounding content
////        // to see the CS stylerules for <a> apply
////        if (renderBean.getInstance() instanceof HtmlOutputText) {
////            DesignBean parent = renderBean.getBeanParent();
////
////            if ((parent != null) && (parent.getChildBeanCount() == 1) &&
////                    (parent.getInstance() instanceof HtmlCommandLink ||
////                    parent.getInstance() instanceof HtmlOutputLink)) {
////                renderBean = (MarkupDesignBean)parent;
////            }
////        }
////
////        // Embedded table portions (rowgroups, columns) aren't happy being rendered
////        // without their surrounding table.
////        // It would be better to modify the preview code to actually go and -try- rendering
////        // components and then progressively retry on parents until it succeeds.
////        // But given that the code is freezing today I'm playing it safe
////        if (renderBean.getInstance() instanceof com.sun.rave.web.ui.component.TableColumn
////        || renderBean.getInstance() instanceof com.sun.webui.jsf.component.TableColumn) {
////            if (renderBean.getBeanParent() instanceof MarkupDesignBean) {
////                renderBean = (MarkupDesignBean)renderBean.getBeanParent();
////            } else {
////                return null;
////            }
////        } else if (renderBean.getBeanParent().getInstance() instanceof com.sun.rave.web.ui.component.TableColumn
////        || renderBean.getBeanParent().getInstance() instanceof com.sun.webui.jsf.component.TableColumn) {
////            // We also have to render components that are children of a TableColumn as part of the whole
////            // table as well, because their value binding expressions can involve data providers set up
////            // by the table. This is clearly not a clean solution. See comment above about trying arbitary
////            // rendering instead. This breaks once you nest components in a column inside a container
////            // component for example. Just doing a low risk, 90% fix now right before FCS.
////            if (renderBean.getBeanParent().getBeanParent() instanceof MarkupDesignBean) {
////                renderBean = (MarkupDesignBean)renderBean.getBeanParent().getBeanParent();
////            } else {
////                return null;
////            }
////        }
////
////        // Not else: a TableColumn can be inside a TableRowGroup so keep moving outwards if necessary:
////        if (renderBean.getInstance() instanceof com.sun.rave.web.ui.component.TableRowGroup
////        || renderBean.getInstance() instanceof com.sun.webui.jsf.component.TableRowGroup) {
////            if (renderBean.getBeanParent() instanceof MarkupDesignBean) {
////                renderBean = (MarkupDesignBean)renderBean.getBeanParent();
////            } else {
////                return null;
////            }
////        }
//        // XXX Hack, see the impl.
//        renderBean = WebForm.getDomProviderService().adjustRenderBeanHack(renderBean);
//
//        Element e = bean.getElement();
//        assert e != null;
        
        setGrid(false); // no grid painting here

        if (!layoutValid) {
            maxWidth = 1024;
        }

//        // XXX can I shut off errors in output window?
//        String oldStyleAttribute = null;
//        String oldStyleProperty = null;
//
//        if (e.hasAttribute(HtmlAttribute.STYLE)) {
//            oldStyleAttribute = e.getAttribute(HtmlAttribute.STYLE);
//        }
//
////        XhtmlCssEngine engine = webform.getMarkup().getCssEngine();

        try {
////            engine.setErrorHandler(XhtmlCssEngine.SILENT_ERROR_HANDLER);
////            CssProvider.getEngineService().setSilentErrorHandlerForDocument(webform.getMarkup().getSourceDom());
////            CssProvider.getEngineService().setSilentErrorHandlerForDocument(webform.getMarkup().getRenderedDom());
//            CssProvider.getEngineService().setSilentErrorHandlerForDocument(webform.getHtmlDom());
//            
////            CssBox.noBoxPersistence = true;
//
//            e.setAttribute(HtmlAttribute.STYLE, cssStyle);
//
//            DesignProperty prop = bean.getProperty("style");
//
//            if (prop != null) {
//                oldStyleProperty = (String)prop.getValue();
//
//                try {
//                    Method m = prop.getPropertyDescriptor().getWriteMethod();
//                    m.invoke(bean.getInstance(), new Object[] { cssStyle });
//                } catch (Exception ex) {
//                    ErrorManager.getDefault().notify(ex);
//                }
//            }
//
////            engine.clearComputedStyles(e, "");
//            CssProvider.getEngineService().clearComputedStylesForElement(e);

            CreateContext cc = new CreateContext();
            cc.pushPage(webform);

//            Font font = CssLookup.getFont(body, DesignerSettings.getInstance().getDefaultFontSize());
//            Font font = CssProvider.getValueService().getFontForElement(body, DesignerSettings.getInstance().getDefaultFontSize(), Font.PLAIN);
//            cc.metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
            // XXX Missing text.
            cc.metrics = CssUtilities.getDesignerFontMetricsForElement(body, null, webform.getDefaultFontSize());

//            // Try to render JSF so I can process the DF before proceeding
//            Element element = renderBean.getElement();
//            String tagName = element.getTagName();
//            HtmlTag tag = HtmlTag.getTag(tagName);
//
//            if (tag == null) {
//                // Possibly a Jsf component.
//                // Use getDocument() rather than doc directly since
//                // e.g. jsp includes may point to external documents here,
//                // not the document containing the jsp tag itself
//                
//                // XXX TODO There is not needed webform here.
////                FileObject markupFile = webform.getModel().getMarkupFile();
//////                DocumentFragment df = FacesSupport.renderHtml(markupFile, renderBean, !CssBox.noBoxPersistence);
////                DocumentFragment df = InSyncService.getProvider().renderHtml(markupFile, renderBean);
//                DocumentFragment df = webform.renderHtmlForMarkupDesignBean(renderBean);
            
                // XXX FIXME Is this correct here?
            // This was needless here:
            // 1) the previously called getHtmlBody has this side effect.
            // 2) there shouldn't be any component associated with this.
//                webform.updateErrorsInComponent();
                

                if (df != null) {
                    DesignerUtils.stripDesignStyleClasses(df);

                    // Yes - add its nodes into our box list
                    NodeList nl = df.getChildNodes();
                    int num = nl.getLength();
                    setProbableChildCount(num); // or addProbablyChildCount??

                    for (int i = 0, n = num; i < n; i++) {
                        Node nn = nl.item(i); // Recurse

                        if ((nn.getNodeType() == Node.TEXT_NODE) && COLLAPSE &&
                                DesignerUtils.onlyWhitespace(nn.getNodeValue())) {
                            continue;
                        }

                        addNode(cc, nn, element, null, null);
                    }
                } else {
                    // Not a JSF component -- normal processing
                    addNode(cc, element, element, null, null);
                }
//            } else {
//                // Not a JSF component -- normal processing
//                addNode(cc, element, element, null, null);
//            }

            int wrapWidth = initialWidth;
            relayout(null, initialWidth, initialHeight, wrapWidth);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);

            return null;
        }
//        } finally {
////            CssBox.noBoxPersistence = false;
//
//            if (oldStyleAttribute != null) {
//                e.setAttribute(HtmlAttribute.STYLE, oldStyleAttribute);
//            } else {
//                e.removeAttribute(HtmlAttribute.STYLE);
//            }
//
//            DesignProperty prop = bean.getProperty("style");
//
//            if (prop != null) {
//                try {
//                    Method m = prop.getPropertyDescriptor().getWriteMethod();
//                    m.invoke(bean.getInstance(), new Object[] { oldStyleProperty });
//                } catch (Exception ex) {
//                    ErrorManager.getDefault().notify(ex);
//                }
//            }
//
////            engine.clearComputedStyles(e, null);
//            CssEngineService cssEngineService = CssProvider.getEngineService();
//            cssEngineService.clearComputedStylesForElement(e);
//
//            if (renderBean != bean) {
////                engine.clearComputedStyles(renderBean.getElement(), null);
//                cssEngineService.clearComputedStylesForElement(renderBean.getElement());
//            }
//
////            engine.setErrorHandler(null);
////            cssEngineService.setNullErrorHandlerForDocument(webform.getMarkup().getSourceDom());
////            cssEngineService.setNullErrorHandlerForDocument(webform.getMarkup().getRenderedDom());
//            cssEngineService.setNullErrorHandlerForDocument(webform.getHtmlDom());
//        }

        if (!layoutValid) {
            return null;
        }

//        CssBox box = findCssBox(bean);
//        CssBox box = findCssBoxForComponentRootElement(
//                WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean(bean));
        CssBox box = findCssBoxForComponentRootElement(componentRootElement);

        Rectangle bounds;

        if (box == null) {
//            bounds = computeBounds(bean, null);
//            bounds = computeBounds(WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean(bean), null);
            bounds = computeBounds(componentRootElement, null);
            // XXX #6389428 Possible NPE. Probably just a consequence of some other issue.
            if (bounds == null) {
                // Log it?
                return null;
            }
            
            width = bounds.width;
            height = bounds.height;
            box = this;
        } else {
            width = box.getWidth();
            height = box.getHeight();
            bounds = new Rectangle(0, 0, width, height);
        }

        // See what the computed size is
        if ((width <= 0) || (width >= 3000)) {
            return null;
        }

        if ((height <= 0) || (height >= 3000)) {
            return null;
        }

        // Restore?
        BufferedImage image = null;

        if (g2d != null) {
            GraphicsConfiguration config = g2d.getDeviceConfiguration();
            image = config.createCompatibleImage(width, height);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        if (image != null) {
            Graphics2D og = (Graphics2D)image.getGraphics();

            try {
                // I have to set a clip rectangle here. That's usually done by
                // Swing (when we're painting the designer surface), but not
                // when I'm obtaining a Graphics object as shown above.
                // And this matters because the BackgroundImagePainter looks
                // to see if a clip is in effect, and if there's no clip, it
                // doesn't use its own clipping rectangle - with the net result
                // that the background painting can overflow its background area
                // (and cover up borders etc.)
                og.setClip(0, 0, width, height);

                //og.setColor(new Color(0, 255, 0, 0));
                // Some components are transparent, so we have to
                // paint the background. XXX I should ask them so
                // I don't have to waste time here!!!
                og.setColor(getBackground());
                og.fillRect(0, 0, width, height);

                if (this.width > 0) {
                    // Turn off clip optimizations since they depend on box extents
                    // which we're not recomputing for this box
                    boolean oldClip = DesignerPane.INCREMENTAL_LAYOUT;
                    DesignerPane.INCREMENTAL_LAYOUT = false;

                    int oldX = box.getX();
                    int oldY = box.getY();
                    int oldLeftMargin = box.getLeftMargin();
                    int oldEffectiveTopMargin = box.getEffectiveTopMargin();

                    box.setLocation(0, 0);
                    box.setMargins(0, 0);

                    if (box == this) {
                        context.initialCB.x = -bounds.x;
                        context.initialCB.y = -bounds.y;
                    }

                    try {
                        box.paint(og, -bounds.x, -bounds.y);
                    } finally {
                        DesignerPane.INCREMENTAL_LAYOUT = oldClip;
                        box.setLocation(oldX, oldY);
                        box.setMargins(oldLeftMargin, oldEffectiveTopMargin);
                    }

                    //og.setTransform(aT);
                }
            } finally {
                og.dispose();
            }
        }

        return image;
    }

//    /**
//      * Provides a mapping, for a given character,
//      * from the document model coordinate space
//      * to the view coordinate space.
//      *
//      * @todo Find a better home for this method
//      * @param pos the position of the desired character (>=0)
//      * @return the bounding box, in view coordinate space,
//      *          of the character at the specified position
//      * @see View#viewToModel
//     * @todo Replace with mapper usage!
//      */
//    public Rectangle modelToView(Position pos) {
//        return ModelViewMapper.modelToView(this, pos);
//    }

    /** Return the deepest box containing the given point. */
    public CssBox findCssBox(int x, int y) { // XXX return Box instead?

        CssBox box = findCssBox(x, y, leftMargin, effectiveTopMargin, 0);

        if (box == null) {
            box = this;
        }

        return box;
    }

    /** Initialize whether this box should show a visual grid and should receive
     * grid mode handling from mouse operations */
    protected void initializeGrid() {
//        if (webform.getDocument().isGridMode()) {
//        if (webform.isGridModeDocument()) {
        if (webform.isGridMode()) {
            setGrid(true);
        }
    }

    protected void initializeBackground() {
        super.initializeBackground();

        if (bg == null) {
            bg = Color.white;
        }

        dark = ColorManager.isDark(bg);
    }

    /** Report whether the background on this page is "dark".
     * In this case for example the selection markers should
     * not be painted in black but in some light/reverse video colors.
     */
    public boolean isDarkBackground() {
        return dark;
    }
}
