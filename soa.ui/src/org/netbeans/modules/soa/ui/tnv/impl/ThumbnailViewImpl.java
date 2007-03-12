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

package org.netbeans.modules.soa.ui.tnv.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Timer;
import org.netbeans.modules.soa.ui.tnv.api.ThumbnailPaintable;
import org.netbeans.modules.soa.ui.tnv.api.ThumbnailView;
import org.openide.util.NbBundle;

/**
 * The implementation of the Thumbnail view which uses the double coordinates.
 * @author nk160297
 */
public class ThumbnailViewImpl extends JPanel implements ThumbnailView {
    
    protected JScrollPane myScrollPane;
    
    protected JComponent myContent;
    
    protected MyContainerListener myContListener;
    
    protected double myZoom = 0d;
    
    /**
     * Determines what times the covering space of the TNV view
     * more then covering space of the main view.
     */
    protected double relativeZoom = 3.5d;
    
    /*
     * Rectangle which is visible in the thumbnail view.
     * It is defined in the main view coordinates.
     */
    protected Rectangle2D.Double myTnvVisibleRect;
    
    /*
     * Rectangle which is visible in the main view.
     * It is defined in the main view coordinates.
     */
    protected Rectangle2D.Double myMainVisibleRect;
    
    private DndProcessor yDndProcessor = new DndProcessor();
    private DndProcessor xDndProcessor = new DndProcessor();
    //
    // used only when the JComponent is used as content
    private ComponentChangeTracker compChangeTracker = null;
    
    protected boolean repaintMainViewRightAway = true;
    protected boolean repaintThumbnailViewRightAway = true;
    
    // indicates if the TNV is now in the DnD mode
    protected boolean dndMode = false;
    
    protected transient Dimension prevMainVisibleDimension;
    protected transient Dimension prevMainDimension;
    protected transient Dimension prevTnvDimension;
    
    private Timer changeUpdateDelayTimer;
    
    public ThumbnailViewImpl() {
        //
        changeUpdateDelayTimer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processObservableChange();
            }
        });
        changeUpdateDelayTimer.setRepeats(false);
        //
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        //
        //        addComponentListener(new ComponentAdapter() {
        //            public void componentResized(ComponentEvent e) {
        //                processTnvResize();
        //                repaint();
        //            }
        //        });
        //
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (myContent == null) {
                    return;
                }
                if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                    dndMode = true;
                    processDrag(e);
                    repaint();
                }
            }
        });
        //
        addMouseListener(new MouseAdapter() {
            
            public void mouseClicked(MouseEvent e) {
                if (myContent == null) {
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON1) {
                    processClick(e);
                    scrollMainView();
                    repaint();
                }
            }
            
            public void mousePressed(MouseEvent e) {
                if (myContent == null) {
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dndMode = false;
                    //
                    double zoom = getZoom();
                    ThumbnailPositionState xTnState = new ThumbnailPositionState();
                    ThumbnailPositionState yTnState = new ThumbnailPositionState();
                    recalculateTnPositionStates(xTnState, yTnState);
                    //
                    xDndProcessor.startDnd(e.getX(), xTnState, zoom);
                    yDndProcessor.startDnd(e.getY(), yTnState, zoom);
                }
                if (e.isPopupTrigger()) {
                    constructPopupMenu().show(
                            ThumbnailViewImpl.this, e.getX(), e.getY());
                }
            }
            
            public void mouseReleased(MouseEvent e) {
                if (myContent == null) {
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Clear temporary variables for other case
                    xDndProcessor.stopDnd();
                    yDndProcessor.stopDnd();
                    //
                    if (dndMode && !repaintMainViewRightAway) {
                        scrollMainView();
                    }
                    //
                    dndMode = false;
                }
                if (e.isPopupTrigger()) {
                    constructPopupMenu().show(
                            ThumbnailViewImpl.this, e.getX(), e.getY());
                }
            }
            
        });
        //
    }
    
    private JPopupMenu constructPopupMenu() {
        JPopupMenu result = new JPopupMenu();
        //
        String text = NbBundle.getMessage(
                ThumbnailView.class, "REPAINT_MAIN_VIEW_RIGHT_AWAY");
        JMenuItem item = new JCheckBoxMenuItem(text, isRepaintMainViewRightAway());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaintMainViewRightAway(!isRepaintMainViewRightAway());
            }
        });
        result.add(item);
        //
        text = NbBundle.getMessage(
                ThumbnailView.class, "REPAINT_THUMBNAIL_VIEW_RIGHT_AWAY");
        item = new JCheckBoxMenuItem(text, isRepaintThumbnailViewRightAway());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaintThumbnailViewRightAway(!isRepaintThumbnailViewRightAway());
            }
        });
        result.add(item);
        //
        return result;
    }
    
    public void repaintMainViewRightAway(boolean newValue) {
        repaintMainViewRightAway = newValue;
    }
    
    public boolean isRepaintMainViewRightAway() {
        return repaintMainViewRightAway;
    }
    
    public void repaintThumbnailViewRightAway(boolean newValue) {
        repaintThumbnailViewRightAway = newValue;
    }
    
    public boolean isRepaintThumbnailViewRightAway() {
        return repaintThumbnailViewRightAway;
    }
    
    private void recalculateTnPositionStates(ThumbnailPositionState xTnState,
            ThumbnailPositionState yTnState) {
        Rectangle2D.Double tnvVisibleRect = getTnvVisibleRect();
        Rectangle currVisiblePart = myContent.getVisibleRect();
        //
        xTnState.mvSize = myContent.getWidth();
        xTnState.tnvPosition = tnvVisibleRect.x;
        xTnState.tnvSize = tnvVisibleRect.width;
        xTnState.vaPosition = currVisiblePart.x;
        xTnState.vaHalfSize = currVisiblePart.width / 2d;
        //
        yTnState.mvSize = myContent.getHeight();
        yTnState.tnvPosition = tnvVisibleRect.y;
        yTnState.tnvSize = tnvVisibleRect.height;
        yTnState.vaPosition = currVisiblePart.y;
        yTnState.vaHalfSize = currVisiblePart.height / 2d;
    }
    
    private void applyTnPositionStates(ThumbnailPositionState xTnState,
            ThumbnailPositionState yTnState) {
        Rectangle2D.Double tnvVisibleRect = getTnvVisibleRect();
        tnvVisibleRect.x = xTnState.tnvPosition;
        tnvVisibleRect.y = yTnState.tnvPosition;
        tnvVisibleRect.width = xTnState.tnvSize;
        tnvVisibleRect.height = yTnState.tnvSize;
        //
        Rectangle2D.Double newVisiblePart = new Rectangle2D.Double(
                xTnState.vaPosition,
                yTnState.vaPosition,
                xTnState.vaHalfSize * 2d,
                yTnState.vaHalfSize * 2d);
        setVisiblePart(newVisiblePart);
    }
    
    public void doLayout() {
        super.doLayout();
        processTnvResize();
    }
    
    public JScrollPane getScrollPane() {
        return myScrollPane;
    }
    
    public JComponent getContent() {
        return myContent;
    }
    
    public void setScrollPane(JScrollPane newValue) {
        if (myScrollPane != null) {
            if (compChangeTracker != null) {
                compChangeTracker.removeUpdateListener(this);
                compChangeTracker.dispose();
                compChangeTracker = null;
            }
        }
        //
        myScrollPane = newValue;
        //
        if (myScrollPane != null) {
            myScrollPane.getContainerListeners();
            startListenContainer(myScrollPane);
            //
            compChangeTracker = new ComponentChangeTracker(myScrollPane);
            compChangeTracker.addUpdateListener(this);
        }
        //
        updateContent();
    }
    
    private void startListenContainer(Container container) {
        if (myContListener == null) {
            myContListener = new MyContainerListener();
            container.addContainerListener(myContListener);
        } else {
            ContainerListener[] listenerArr = container.getContainerListeners();
            boolean found = false;
            for (ContainerListener listener : listenerArr) {
                if (listener == myContListener) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                container.addContainerListener(myContListener);
            }
        }
    }
    
    /**
     * Looks for changes in the content of TNV.
     * If content is changed, then reinitialize TNV and repaint it.
     */
    private void updateContent() {
        JComponent newContent = null;
        //
        if (myScrollPane != null) {
            JViewport viewport = myScrollPane.getViewport();
            if (viewport != null) {
                Component comp = viewport.getView();
                if (comp != null && comp instanceof JComponent) {
                    newContent = (JComponent)comp;
                }
            }
        }
        if (!(newContent instanceof ThumbnailPaintable)) {
            // Only components which implements the ThumbnailPaintable are allowed
            newContent = null;
        }
        //
        if (myContent == null && newContent == null) {
            // Nothing to update
            return;
        }
        //
        if (myContent == null  || !myContent.equals(newContent)) {
            myContent = newContent;
            //
            recalculateZoomAndTnv();
            centerSmallView();
            revalidate();
            repaint();
        }
    }
    
    private class MyContainerListener implements ContainerListener {
        
        public void componentAdded(ContainerEvent e) {
            Container cont = e.getContainer();
            Component child = e.getChild();
            if (child instanceof JViewport) {
                startListenContainer((JViewport)child);
            }
            updateContent();
        }
        
        public void componentRemoved(ContainerEvent e) {
            Container cont = e.getContainer();
            Component child = e.getChild();
            if (child instanceof JComponent) {
                ((JComponent)child).removeContainerListener(myContListener);
            }
            updateContent();
        }
        
    }
    
    /**
     * Returns rectangle which is visible in the thumbnail view.
     * It is defined in the main view coordinates.
     */
    private Rectangle2D.Double getTnvVisibleRect() {
        if (myTnvVisibleRect == null) {
            recalculateZoomAndTnv();
        }
        return myTnvVisibleRect;
    }
    
    public void setVisiblePart(Rectangle2D.Double newVisiblePart) {
        if (myContent == null) {
            return;
        }
        //
        myMainVisibleRect = newVisiblePart;
        //
        if (repaintMainViewRightAway) {
            scrollMainView();
        }
    }
    
    protected void scrollMainView() {
        myContent.scrollRectToVisible(myMainVisibleRect.getBounds());
    }
    
    /**
     * Obtains the visible part rectangle from the observed component.
     */
    public Rectangle2D.Double getVisiblePart() {
        if (myContent == null) {
            return null;
        }
        //
        if (myMainVisibleRect == null) {
            Rectangle rect = myContent.getVisibleRect();
            myMainVisibleRect = new Rectangle2D.Double(
                    rect.getX(),
                    rect.getY(),
                    rect.getWidth(),
                    rect.getHeight());
            //
            accommodateTnvTo(myMainVisibleRect);
        }
        return myMainVisibleRect;
        // return myContent.getVisibleRect();
    }
    
    public void observableChanged() {
        if (repaintThumbnailViewRightAway) {
            processObservableChange();
        } else {
            changeUpdateDelayTimer.restart();
        }
    }
    
    private void processObservableChange() {
        if (myContent != null) {
            recalculateZoomAndTnv();
            centerSmallView();
            Rectangle2D currVisiblePart = myContent.getVisibleRect().getBounds2D();
            myMainVisibleRect = new Rectangle2D.Double(
                    currVisiblePart.getX(),
                    currVisiblePart.getY(),
                    currVisiblePart.getWidth(),
                    currVisiblePart.getHeight());
            accommodateTnvTo(currVisiblePart);
            repaint();
        }
    }
    
    public JComponent getUIComponent() {
        return this;
    }
    
    public double getZoom() {
        if (myZoom == 0d) {
            recalculateZoomAndTnv();
        }
        return myZoom;
    }
    
    public void setZoom(double newValue) {
        if (myZoom != newValue) {
            myZoom = newValue;
            // System.out.println("new ZOOM: " + myZoom);
            //
            repaint();
        }
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // draw background
        //
        if (myContent != null) {
            ThumbnailGraphics g2 = new ThumbnailGraphics((Graphics2D)g.create());
            try {
                //
                // Paint main content
                double zoom = getZoom();
                g2.scale(zoom, zoom);
                //
                Rectangle2D.Double tnvVisibleRect = getTnvVisibleRect();
                g2.translate(-tnvVisibleRect.x, -tnvVisibleRect.y);
                //
                assert myContent instanceof ThumbnailPaintable :
                    "The observable component has to implement " +
                    "the ThumbnailPaintable interface"; // NOI18N
                ((ThumbnailPaintable)myContent).paintThumbnail(g2);
                //
                // Paint view rectangle
                Rectangle2D.Double visiblePart = getVisiblePart();
                //
                g2.setColor(new Color(200, 200, 200, 128));
                g2.fill(visiblePart);
                g2.setColor(Color.BLACK);
                g2.draw(visiblePart);
                //
            } finally {
                g2.dispose();
            }
        }
    }
    
    /**
     * Converts a point from TN view coordinates according the current zoom
     * and ths shift specified.
     */
    protected Point2D.Double fromTnvToMain(Point2D point, Point2D.Double shift) {
        double zoom = getZoom();
        double x = point.getX() / zoom + shift.x;
        double y = point.getY() / zoom + shift.y;
        Point2D.Double result = new Point2D.Double(x, y);
        return result;
    }
    
    /**
     * Converts a point from TN view coordinates to the Main view coordinates.
     */
    protected Point2D.Double fromTnvToMain(Point2D point) {
        Rectangle2D.Double tnvRect = getTnvVisibleRect();
        return fromTnvToMain(point, new Point2D.Double(tnvRect.x, tnvRect.y));
    }
    
    /**
     * Converts a point from Main view coordinates to the TN view coordinates.
     */
    protected Point2D.Double fromMainToTnv(Point2D point) {
        double zoom = getZoom();
        Rectangle2D.Double tnvRect = getTnvVisibleRect();
        double x = (point.getX() - tnvRect.x) * zoom;
        double y = (point.getY() - tnvRect.y) * zoom;
        Point2D.Double result = new Point2D.Double(x, y);
        return result;
    }
    
    /**
     * Track the mouse drag event to change the position of visible part.
     */
    protected void processDrag(MouseEvent e) {
        double zoom = getZoom();
        ThumbnailPositionState xTnState = new ThumbnailPositionState();
        ThumbnailPositionState yTnState = new ThumbnailPositionState();
        recalculateTnPositionStates(xTnState, yTnState);
        //
        xTnState = xDndProcessor.processDnD(e.getX(), xTnState, zoom);
        yTnState = yDndProcessor.processDnD(e.getY(), yTnState, zoom);
        //
        applyTnPositionStates(xTnState, yTnState);
        // setVisiblePart(newVisiblePart);
    }
    
    /**
     * Process mouse click. Move the view to the new point.
     */
    protected void processClick(MouseEvent event) {
        double zoom = getZoom();
        Rectangle2D.Double tnvVisibleRect = getTnvVisibleRect();
        //
        // Convert to the main view coordinates
        double x = event.getX() / zoom + tnvVisibleRect.x;
        double y = event.getY() / zoom + tnvVisibleRect.y;
        Point2D.Double newCenter = new Point2D.Double(x, y);
        //
        Rectangle2D currVisiblePart = myContent.getVisibleRect().getBounds2D();
        Dimension fullAreaSize = myContent.getSize();
        //
        //        System.out.println("centerX: " + x);
        //        System.out.println("event.getX(): " + event.getX());
        //        System.out.println("newCenter: " + newCenter);
        //        System.out.println("currVisiblePart: " + currVisiblePart);
        //        System.out.println("fullArea: " + fullArea);
        //
        double halfWidth = currVisiblePart.getWidth() / 2d;
        double halfHeight = currVisiblePart.getHeight() / 2d;
        //
        double centerX = newCenter.x;
        double centerY = newCenter.y;
        //
        // Determine edges of the full area and correct the new location of the center
        // such way so the main visible area remains inside of the full area.
        double leftEdge = halfWidth;
        double rightEdge = fullAreaSize.getWidth() - halfWidth;
        double topEdge = halfHeight;
        double bottomEdge = fullAreaSize.getHeight() - halfHeight;
        //
        if (centerX < leftEdge) {
            centerX = leftEdge;
        }
        if (centerX > rightEdge) {
            centerX = rightEdge;
        }
        if (centerY < topEdge) {
            centerY = topEdge;
        }
        if (centerY > bottomEdge) {
            centerY = bottomEdge;
        }
        //
        Rectangle2D.Double newVisiblePart = new Rectangle2D.Double(
                centerX - halfWidth,
                centerY - halfHeight,
                currVisiblePart.getWidth(),
                currVisiblePart.getHeight());
        //
        setVisiblePart(newVisiblePart);
    }
    
    /**
     * Accommodate the TNV visible part to the main view visible part.
     * The main view visible part has to be shown inside of thumbnail view.
     * If the main view rectangle is out of the TN view, then the TN view has to be shifted.
     */
    protected void accommodateTnvTo(Rectangle2D newVisiblePart) {
        Rectangle2D.Double tnvVisibleRect = getTnvVisibleRect();
        double xShift = 0;
        double yShift = 0;
        //
        double main = newVisiblePart.getX(); // X coordinate of the main view visible part
        double tnv = tnvVisibleRect.x; // X coordinate of the TN view visible part
        xShift = main - tnv;
        if (xShift >= 0) {
            xShift = (main + newVisiblePart.getWidth()) - (tnv + tnvVisibleRect.width);
            if (xShift <= 0) {
                xShift = 0;
            }
        }
        //
        main = newVisiblePart.getY(); // Y coordinate of the main view visible part
        tnv = tnvVisibleRect.y; // Y coordinate of the TN view visible part
        yShift = main - tnv;
        if (yShift >= 0) {
            yShift = (main + newVisiblePart.getHeight()) - (tnv + tnvVisibleRect.height);
            if (yShift <= 0) {
                yShift = 0;
            }
        }
        //
        // Shift TN view visible rectangle
        if (yShift != 0 || xShift != 0) {
            tnvVisibleRect.x += xShift;
            tnvVisibleRect.y += yShift;
        }
        //        System.out.println("tnvVisibleRect: " + tnvVisibleRect);
    }
    
    /**
     * Calculates current ratio of thumbnail view size relative to main view size.
     * In usual case it has to be less then 1.
     * It also corrects the thumbnail view location.
     */
    protected double recalculateZoomAndTnv() {
        double newScale = 0.1d;
        //
        if (myContent != null) {
            Rectangle visibleRect = myContent.getVisibleRect();
            Dimension mainVisibleDimension = visibleRect.getSize();
            Dimension mainDimension = myContent.getSize();
            Dimension tnvDimension = this.getSize();
            if (tnvDimension.width == 0 || tnvDimension.height == 0) {
                tnvDimension = this.getPreferredSize();
            }
            //
            boolean needRecalculateTnv = false;
            if (prevMainDimension == null ||
                    !prevMainDimension.equals(mainDimension)) {
                // The main view was resized
                needRecalculateTnv = true;
                prevMainDimension = mainDimension;
            }
            if (prevMainVisibleDimension == null ||
                    !prevMainVisibleDimension.equals(mainVisibleDimension)) {
                // The main view visible size was changed
                needRecalculateTnv = true;
                prevMainVisibleDimension = mainVisibleDimension;
            }
            //
            if (needRecalculateTnv) {
                //
                double xScale = tnvDimension.width / (visibleRect.width * relativeZoom);
                double yScale = tnvDimension.height / (visibleRect.height * relativeZoom);
                //
                newScale = Math.min(xScale, yScale);
                //
                Dimension mainSize = myContent.getSize();
                if (mainSize.width == 0 || mainSize.height == 0) {
                    mainSize = myContent.getPreferredSize();
                }
                //
                // Correct scale if the observable component takes only part of space on the TNV.
                if ((tnvDimension.width / newScale) > mainSize.width * 1.001 ) {
                    xScale = tnvDimension.width / (double)mainSize.width;
                }
                if ((tnvDimension.height / newScale) > mainSize.height * 1.001) {
                    yScale = tnvDimension.height / (double)mainSize.height;
                }
                //
                newScale = Math.min(xScale, yScale);
                //                System.out.println("new ZOOM: " + newScale);
                //
                if (myZoom != newScale) {
                    //
                    // Try retain the position (center point) of visible rectangle
                    // at the TNV at previous position.
                    Point2D.Double mainViewCenter = getCenter(visibleRect);
                    //
                    Point2D.Double resizeFixedPoint;
                    if (myTnvVisibleRect != null) {
                        resizeFixedPoint = fromMainToTnv(mainViewCenter);
                    } else {
                        // If the previous position isn't specified, then use center of TNV
                        Rectangle tnvBounds = new Rectangle(
                                0, 0, tnvDimension.width, tnvDimension.height);
                        resizeFixedPoint = getCenter(tnvBounds);
                        myTnvVisibleRect = new Rectangle2D.Double();
                    }
                    //
                    myZoom = newScale;
                    //
                    myTnvVisibleRect.x = mainViewCenter.x - resizeFixedPoint.x / newScale;
                    myTnvVisibleRect.y = mainViewCenter.y - resizeFixedPoint.y / newScale;
                    myTnvVisibleRect.width = tnvDimension.width / newScale;
                    myTnvVisibleRect.height = tnvDimension.height / newScale;
                }
                //
                // Correct the TNV visible rectangle position
                // to prevent it's moveint out of full area border
                Rectangle mainBounds = myContent.getBounds();
                if (myTnvVisibleRect.x < 0) {
                    myTnvVisibleRect.x = 0;
                } else if (myTnvVisibleRect.x + myTnvVisibleRect.width >
                        mainBounds.width) {
                    myTnvVisibleRect.x =
                            mainBounds.width - myTnvVisibleRect.width;
                }
                if (myTnvVisibleRect.y < 0) {
                    myTnvVisibleRect.y = 0;
                } else if (myTnvVisibleRect.y + myTnvVisibleRect.height >
                        mainBounds.height) {
                    myTnvVisibleRect.y =
                            mainBounds.height - myTnvVisibleRect.height;
                }
            }
        }
        return newScale;
    }
    
    protected double processTnvResize() {
        double newScale = 0.1d;
        //
        if (myContent != null) {
            Rectangle visibleRect = myContent.getVisibleRect();
            Dimension tnvDimension = this.getSize();
            if (tnvDimension.width == 0 || tnvDimension.height == 0) {
                tnvDimension = this.getPreferredSize();
            }
            //
            boolean needRecalculateTnv = false;
            if (prevTnvDimension == null || !prevTnvDimension.equals(tnvDimension)) {
                // The TNV was resized
                needRecalculateTnv = true;
                prevTnvDimension = tnvDimension;
            }
            //
            if (needRecalculateTnv) {
                //
                double xScale = tnvDimension.width / (visibleRect.width * relativeZoom);
                double yScale = tnvDimension.height / (visibleRect.height * relativeZoom);
                //
                newScale = Math.min(xScale, yScale);
                //
                Dimension mainSize = myContent.getSize();
                if (mainSize.width == 0 || mainSize.height == 0) {
                    mainSize = myContent.getPreferredSize();
                }
                //
                // Correct scale if the observable component takes only part of space on the TNV.
                if ((tnvDimension.width / newScale) > mainSize.width * 1.001) {
                    xScale = tnvDimension.width / (double)mainSize.width;
                }
                if ((tnvDimension.height / newScale) > mainSize.height * 1.001) {
                    yScale = tnvDimension.height / (double)mainSize.height;
                }
                //
                newScale = Math.min(xScale, yScale);
                //                System.out.println("new ZOOM: " + newScale);
                //
                if (myZoom != newScale) {
                    myZoom = newScale;
                }
                //
                // Put the visible rectangle to the center of the TNV.
                Point2D.Double mainViewCenter = getCenter(visibleRect);
                //
                myTnvVisibleRect.x = mainViewCenter.x - (tnvDimension.width / newScale) / 2d;
                myTnvVisibleRect.y = mainViewCenter.y - (tnvDimension.height / newScale) / 2d;
                myTnvVisibleRect.width = tnvDimension.width / newScale;
                myTnvVisibleRect.height = tnvDimension.height / newScale;
                //
                // Correct the TNV visible rectangle position
                // to prevent it's moveint out of full area border
                Rectangle mainBounds = myContent.getBounds();
                if (myTnvVisibleRect.x < 0) {
                    myTnvVisibleRect.x = 0;
                }
                if (myTnvVisibleRect.x + myTnvVisibleRect.width >
                        mainBounds.width) {
                    myTnvVisibleRect.x =
                            mainBounds.width - myTnvVisibleRect.width;
                }
                if (myTnvVisibleRect.y < 0) {
                    myTnvVisibleRect.y = 0;
                }
                if (myTnvVisibleRect.y + myTnvVisibleRect.height >
                        mainBounds.height) {
                    myTnvVisibleRect.y =
                            mainBounds.height - myTnvVisibleRect.height;
                }
            }
        }
        //
        centerSmallView();
        //
        return newScale;
    }
    
    /**
     * Centers the view inside of TNV if it less then TNV visible rectangle.
     */
    protected void centerSmallView() {
        if (myContent != null) {
            Rectangle fullBounds = myContent.getBounds();
            double zoom = getZoom();
            Dimension tnvSize = this.getSize();
            double tnvWidth = tnvSize.width / zoom;
            double tnvHeight = tnvSize.height / zoom;
            Rectangle2D.Double tnvVisibleRect = getTnvVisibleRect();
            //
            if (tnvWidth > fullBounds.width * 1.001) {
                tnvVisibleRect.x = (fullBounds.width - tnvWidth) / 2;
                // System.out.println("tnvVisibleRect.x " + tnvVisibleRect.x);
            }
            //
            if (tnvHeight > fullBounds.height * 1.001) {
                tnvVisibleRect.y = (fullBounds.height - tnvHeight) / 2;
                // System.out.println("tnvVisibleRect.y " + tnvVisibleRect.y);
            }
        }
    }
    
    public static Point2D.Double getCenter(Rectangle2D.Double rect) {
        double xCenter = rect.x + rect.width / 2;
        double yCenter = rect.y + rect.height / 2;
        return new Point2D.Double(xCenter, yCenter);
    }
    
    public static Point2D.Double getCenter(Rectangle rect) {
        double xCenter = rect.x + rect.width / 2;
        double yCenter = rect.y + rect.height / 2;
        return new Point2D.Double(xCenter, yCenter);
    }
    
}
