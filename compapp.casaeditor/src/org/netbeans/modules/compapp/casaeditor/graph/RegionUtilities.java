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

/*
 * RegionUtilities.java
 *
 * Created on November 9, 2006, 5:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaRegion;
import org.openide.util.Utilities;

/**
 *
 * @author jsandusky
 */
public class RegionUtilities {
    
    public static final int MIN_REGION_WIDTH   = 50;
    public static final int RESIZER_WIDTH      = 4; // must be an even number
    public static final int RESIZER_HALF_WIDTH = RESIZER_WIDTH / 2;
    public static final int DEFAULT_HEIGHT  = 500;
    
    public static final Color RESIZER_COLOR = new Color(80, 80, 80, 60);
    
    // The number of pixels of extra space that should exist between
    // a widget and it's region's right border.
    public static final int HORIZONTAL_EXPANSION_GAP = 10;
    
    // The number of pixels of extra space that should exist between
    // a widget and the bottom of the scene.
    public static final int VERTICAL_EXPANSION_GAP   = 60;
    

    public static final Image IMAGE_DELETE_16_ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/palette/resources/delete16.png");      // NOI18N
    public static final Image IMAGE_EDIT_16_ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/palette/resources/edit16.png");        // NOI18N
    public static final Image IMAGE_HIDE_16_ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/palette/resources/hide16.png");        // NOI18N
    public static final Image IMAGE_WS_POLICY_16_ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/palette/resources/WSpolicy16.png");    // NOI18N
    
    public enum Directions {
        LEFT, TOP, RIGHT, BOTTOM
    }
    
    
    public static CasaRegionWidget getRegionWidget(CasaModelGraphScene scene, CasaRegion.Name regionID) {
        CasaRegion region = scene.getModel().getCasaRegion(regionID);
        if (region != null) {
            return (CasaRegionWidget) scene.findWidget(region);
        }
        return null;
    }
    
    
    public static void stretchScene(CasaModelGraphScene scene) {
        doStretchSceneHeight(scene);
        doStretchSceneWidth(scene);
        scene.revalidate();
        scene.validate();
        scene.getView().repaint();
    }
    
    public static void stretchSceneHeightOnly(CasaModelGraphScene scene) {
        doStretchSceneHeight(scene);
        scene.revalidate();
        scene.validate();
        scene.getView().repaint();
    }

    public static void stretchSceneWidthOnly(CasaModelGraphScene scene) {
        doStretchSceneWidth(scene);
        scene.revalidate();
        scene.validate();
        scene.getView().repaint();
    }

    
    
    private static void doStretchSceneHeight(CasaModelGraphScene scene) {
        if (scene.getBounds() != null && scene.getView() != null) {
            JScrollPane scroller = (JScrollPane) SwingUtilities.getAncestorOfClass(
                    JScrollPane.class,
                    scene.getView());
            if (scroller == null) {
                return;
            }
            
            Rectangle sceneRect = scroller.getViewportBorderBounds();
            
            Widget leftResizer   = scene.getLeftResizer();
            Widget middleResizer = scene.getMiddleResizer();
            
            CasaRegionWidget leftRegion   = getRegionWidget(scene, CasaRegion.Name.WSDL_ENDPOINTS);
            CasaRegionWidget middleRegion = getRegionWidget(scene, CasaRegion.Name.JBI_MODULES);
            CasaRegionWidget rightRegion  = getRegionWidget(scene, CasaRegion.Name.EXTERNAL_MODULES);
            
            if (leftRegion == null || middleRegion == null || rightRegion == null) {
                return;
            }
            
            // All regions expand automatically to fill the remaining scene height.
            int maxYSpan = findMaximumWidgetYSpan(leftRegion, middleRegion, rightRegion);
            maxYSpan = Math.max(maxYSpan, sceneRect.height);

            adjustHeights(maxYSpan, leftResizer, leftRegion, middleResizer, middleRegion, rightRegion);
        }
    }
    
    
    private static void doStretchSceneWidth(CasaModelGraphScene scene) {
        if (scene.getBounds() != null && scene.getView() != null) {
            JScrollPane scroller = (JScrollPane) SwingUtilities.getAncestorOfClass(
                    JScrollPane.class,
                    scene.getView());
            if (scroller == null) {
                return;
            }
            
            Rectangle sceneRect = scroller.getViewportBorderBounds();
            
            Widget leftResizer   = scene.getLeftResizer();
            Widget middleResizer = scene.getMiddleResizer();
            
            CasaRegionWidget leftRegion   = getRegionWidget(scene, CasaRegion.Name.WSDL_ENDPOINTS);
            CasaRegionWidget middleRegion = getRegionWidget(scene, CasaRegion.Name.JBI_MODULES);
            CasaRegionWidget rightRegion  = getRegionWidget(scene, CasaRegion.Name.EXTERNAL_MODULES);
            
            if (leftRegion == null || middleRegion == null || rightRegion == null) {
                return;
            }

            int maxLeftRegionXSpan   = findMaximumWidgetXSpan(
                    0, // cannot have a gap, we are right-aligning our widgets to the edge
                    leftRegion);
            int maxMiddleRegionXSpan = findMaximumWidgetXSpan(
                    HORIZONTAL_EXPANSION_GAP, 
                    middleRegion);
            int maxRightRegionXSpan  = findMaximumWidgetXSpan(
                    HORIZONTAL_EXPANSION_GAP, 
                    rightRegion);
            
            maxLeftRegionXSpan   = Math.max(maxLeftRegionXSpan,   leftRegion.getPreferredBounds().width);
            maxMiddleRegionXSpan = Math.max(maxMiddleRegionXSpan, middleRegion.getPreferredBounds().width);
            
            // The left and middle regions expand only enough to fit their widgets.
            int leftWidth   = adjustWidth(maxLeftRegionXSpan,   leftRegion);
            int middleWidth = adjustWidth(maxMiddleRegionXSpan, middleRegion);
            
            // The right region expands automatically to fill the remaining scene width.
            maxRightRegionXSpan = Math.max(
                    maxRightRegionXSpan, 
                    sceneRect.width - (leftWidth + middleWidth));
            
            int rightWidth  = adjustWidth(maxRightRegionXSpan,  rightRegion);
            
            leftRegion.setPreferredLocation(  new Point(0, 0));
            middleRegion.setPreferredLocation(new Point(leftWidth, 0));
            rightRegion.setPreferredLocation( new Point(leftWidth + middleWidth, 0));
            
            leftResizer.setPreferredLocation(new Point(
                middleRegion.getPreferredLocation().x - RegionUtilities.RESIZER_HALF_WIDTH, 
                0));
            middleResizer.setPreferredLocation(new Point(
                rightRegion.getPreferredLocation().x - RegionUtilities.RESIZER_HALF_WIDTH, 
                0));
        }
    }


    private static int adjustWidth(int width, CasaRegionWidget region) {
        if (width < CasaRegionWidget.MINIMUM_WIDTH) {
            width = CasaRegionWidget.MINIMUM_WIDTH;
        }
        Rectangle bounds = region.getPreferredBounds();
        region.setPreferredBounds(new Rectangle(
                width, 
                bounds.height));
        return width;
    }
    
    
    private static void adjustHeights(int height, Widget ... widgets) {
        for (Widget widget : widgets) {
            Rectangle bounds = widget.getPreferredBounds();
            widget.setPreferredBounds(new Rectangle(
                    bounds.width,
                    height));
        }
    }
    
    
    private static int findMaximumWidgetYSpan(CasaRegionWidget ... regionWidgets) {
        int maxHeight = 0;
        for (CasaRegionWidget regionWidget : regionWidgets) {
            for (Widget childWidget : regionWidget.getChildren()) {
                if (childWidget instanceof CasaNodeWidget) {
                    if (
                            childWidget.getPreferredLocation() != null &&
                            childWidget.getBounds() != null)
                    {
                        maxHeight = Math.max(
                                maxHeight, 
                                childWidget.getPreferredLocation().y + 
                                    childWidget.getBounds().height + 
                                    VERTICAL_EXPANSION_GAP);
                    }
                }
            }
        }
        return maxHeight;
    }
    
    private static int findMaximumWidgetXSpan(int gap, CasaRegionWidget ... regionWidgets) {
        int maxWidth = 0;
        for (CasaRegionWidget regionWidget : regionWidgets) {
            for (Widget childWidget : regionWidget.getChildren()) {
                if (childWidget instanceof CasaNodeWidget) {
                    if (
                            childWidget.getPreferredLocation() != null &&
                            childWidget.getBounds() != null)
                    {
                        maxWidth = Math.max(
                                maxWidth, 
                                childWidget.getPreferredLocation().x + 
                                    childWidget.getBounds().width + 
                                    gap);
                    }
                }
            }
        }
        return maxWidth;
    }
    
    
    public static MoveStrategy createPinsRestrictedMoveStrategy() {
        return new PinsRestrictedMoveStrategy();
    }
    
    
    /**
     * Creates a directional anchor with computes a point as the one in the middle of the boundary side of specified widget.
     * The side is the closest one to the opposite anchor.
     * @param widget the widget
     * @param kind the kind of directional anchor
     * @param gap the gap between the widget and the anchor location
     * @return the anchor
     */
    public static Anchor createFixedDirectionalAnchor(Widget widget, Directions kind, int gap) {
        return widget != null && kind != null ? new FixedDirectionalAnchor(widget, kind, gap) : null;
    }
    
    
    /**
     * Creates a horizontal box layout with default style where widgets are placed horizontally one to the right from another.
     * The instance can be shared by multiple widgets.
     * @return the horizontal box layout
     */
    public static Layout createHorizontalLayoutWithJustifications() {
        return createHorizontalLayoutWithJustifications(null, 0);
    }
    
    
    /**
     * Creates a horizontal box layout with a specific style where widgets are placed horizontally one to the right from another.
     * The instance can be shared by multiple widgets.
     * @param alignment the alignment
     * @param gap the gap between widgets
     * @return the horizontal box layout
     */
    public static Layout createHorizontalLayoutWithJustifications(SerialAlignment alignment, int gap) {
        return new SerialLayoutWithJustifications(false, alignment != null ? alignment : SerialAlignment.JUSTIFY, gap);
    }
    
    
    public static LayerWidget getRegionForScenePoint(CasaModelGraphScene scene, Point scenePoint) {
        LayerWidget region = null;
        region = scene.getBindingRegion();
        if (region.getBounds().contains(region.convertSceneToLocal(scenePoint))) {
            return region;
        }
        region = scene.getEngineRegion();
        if (region.getBounds().contains(region.convertSceneToLocal(scenePoint))) {
            return region;
        }
        region = scene.getExternalRegion();
        if (region.getBounds().contains(region.convertSceneToLocal(scenePoint))) {
            return region;
        }
        return null;
    }
    
    
    
    private static class PinsRestrictedMoveStrategy implements MoveStrategy {
        public PinsRestrictedMoveStrategy() {
        }
        public Point locationSuggested(Widget widget, Point originalLocation, Point suggestedLocation) {
            return suggestedLocation;
        }
    };
}
