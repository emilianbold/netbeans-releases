/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import org.openide.util.ImageUtilities;
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
    public static final int HORIZONTAL_RIGHT_WIDGET_GAP = 10;
    
    // The number of pixels of extra space that should exist between
    // a widget and it's region's left border.
    public static final int HORIZONTAL_LEFT_WIDGET_GAP = 30;

    // The number of pixels of extra space that should exist between
    // a widget and the bottom of the scene.
    public static final int VERTICAL_EXPANSION_GAP   = 60;
    

    public static final Image IMAGE_DELETE_16_ICON = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/palette/resources/delete16.png");      // NOI18N
    public static final Image IMAGE_EDIT_16_ICON = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/palette/resources/edit16.png");        // NOI18N
    public static final Image IMAGE_HIDE_16_ICON = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/palette/resources/hide16.png");        // NOI18N
    public static final Image IMAGE_WS_POLICY_16_ICON = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/palette/resources/WSpolicy16.png");    // NOI18N
    public static final Image IMAGE_ERROR_BADGE_ICON = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/palette/resources/badge_error.png");    // NOI18N
    public static final Image IMAGE_WARNING_BADGE_ICON = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/palette/resources/badge_warning.png");    // NOI18N
    
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
        boolean isAdjusting = scene.isAdjusting();
        try {
            scene.setIsAdjusting(true);
            doStretchSceneHeight(scene);
            doStretchSceneWidth(scene);
            scene.revalidate();
            scene.validate();
            scene.getView().repaint();
        } finally {
            scene.setIsAdjusting(isAdjusting);
        }
    }
    
    public static void stretchSceneHeightOnly(CasaModelGraphScene scene) {
        boolean isAdjusting = scene.isAdjusting();
        try {
            scene.setIsAdjusting(true);
            doStretchSceneHeight(scene);
            scene.revalidate();
            scene.validate();
            scene.getView().repaint();
        } finally {
            scene.setIsAdjusting(isAdjusting);
        }
    }

    public static void stretchSceneWidthOnly(CasaModelGraphScene scene) {
        boolean isAdjusting = scene.isAdjusting();
        try {
            scene.setIsAdjusting(true);
            doStretchSceneWidth(scene);
            scene.revalidate();
            scene.validate();
            scene.getView().repaint();
        } finally {
            scene.setIsAdjusting(isAdjusting);
        }
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
            
            CasaRegionWidget leftRegion = getRegionWidget(scene, CasaRegion.Name.WSDL_ENDPOINTS);
            CasaRegionWidget middleRegion = getRegionWidget(scene, CasaRegion.Name.JBI_MODULES);
            CasaRegionWidget rightRegion  = getRegionWidget(scene, CasaRegion.Name.EXTERNAL_MODULES);
            
            if (leftRegion == null || middleRegion == null || rightRegion == null) {
                return;
            }
            
            int maxRightRegionXSpan  = findMaximumWidgetXSpan(HORIZONTAL_RIGHT_WIDGET_GAP, rightRegion);
            
            // The left and middle regions expand only enough to fit their widgets.
            int leftWidth   = leftRegion.getPreferredBounds().width;
            int middleWidth = middleRegion.getPreferredBounds().width;
            
            // The right region expands automatically to fill the remaining scene width.
            maxRightRegionXSpan = Math.max(
                    maxRightRegionXSpan, 
                    sceneRect.width - (leftWidth + middleWidth));
            
            int rightWidth  = adjustWidth(maxRightRegionXSpan,  rightRegion);
            
            rightRegion.setPreferredLocation( new Point(leftWidth + middleWidth, 0));
            
            scene.getLeftResizer().setPreferredLocation(new Point(
                middleRegion.getPreferredLocation().x - RegionUtilities.RESIZER_HALF_WIDTH, 
                0));
            scene.getMiddleResizer().setPreferredLocation(new Point(
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
    
    private static int findMaximumWidgetXSpan(int gap, CasaRegionWidget regionWidget) {
        int maxWidth = 0;
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
    public static Layout createHorizontalFlowLayoutWithJustifications() {
        return createHorizontalFlowLayoutWithJustifications(null, 0);
    }
    
    
    /**
     * Creates a horizontal box layout with a specific style where widgets are placed horizontally one to the right from another.
     * The instance can be shared by multiple widgets.
     * @param alignment the alignment
     * @param gap the gap between widgets
     * @return the horizontal box layout
     */
    public static Layout createHorizontalFlowLayoutWithJustifications(SerialAlignment alignment, int gap) {
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
