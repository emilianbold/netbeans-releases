/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.form.layoutdesign;

import java.awt.*;
import java.util.*;
import java.util.List;
import org.openide.util.ImageUtilities;
import static org.netbeans.modules.form.layoutdesign.VisualState.GapInfo;

/**
 *
 */
public class LayoutPainter implements LayoutConstants {
    private LayoutModel layoutModel;
    private VisualState visualState;

    private Collection<GapInfo> paintedGaps;
    private Collection<LayoutComponent> componentsOfPaintedGaps;

    private Image linkBadgeBoth = null;
    private Image linkBadgeHorizontal = null;
    private Image linkBadgeVertical = null;

    private static final int BOTH_DIMENSIONS = 2;

    private Image warningImage;

    private static boolean PAINT_RES_GAP_MIN_SIZE;

    LayoutPainter(LayoutModel layoutModel, VisualState visualState) {
        this.layoutModel = layoutModel;
        this.visualState = visualState;
        layoutModel.addListener(new LayoutModel.Listener() {
            @Override
            public void layoutChanged(LayoutEvent ev) {
                int type = ev.getType();
                if (type != LayoutEvent.INTERVAL_SIZE_CHANGED
                        && type != LayoutEvent.INTERVAL_PADDING_TYPE_CHANGED) {
                    componentsOfPaintedGaps = null;
                    paintedGaps = null;
                }
            }
        });
    }

    /**
     * Paints layout information (anchor links and alignment in groups) for
     * given components.
     * @param selectedComponents Components selected in the designer.
     */
    void paintComponents(Graphics2D g, Collection<LayoutComponent> selectedComponents, boolean paintAlignment) {
        for (LayoutComponent comp : selectedComponents) {
            if (paintAlignment && comp.getParent() != null) {
                paintSelectedComponent(g, comp, HORIZONTAL);
                paintSelectedComponent(g, comp, VERTICAL);
            }
            if (LayoutComponent.isUnplacedComponent(comp)) {
                paintUnplacedWarningImage(g, comp);
            }
        }
    }

    private void paintSelectedComponent(Graphics2D g, LayoutComponent component, int dimension) {
        LayoutInterval interval = component.getLayoutInterval(dimension);
        if (component.isLinkSized(HORIZONTAL) || component.isLinkSized(VERTICAL)) {
            paintLinks(g, component);
        }
        // Paint baseline alignment
        if (interval.getAlignment() == BASELINE) {
            LayoutInterval alignedParent = interval.getParent();
            int oppDimension = (dimension == HORIZONTAL) ? VERTICAL : HORIZONTAL;
            LayoutRegion region = alignedParent.getCurrentSpace();
            int x = region.positions[dimension][BASELINE];
            int y1 = region.positions[oppDimension][LEADING];
            int y2 = region.positions[oppDimension][TRAILING];
            if ((y1 != LayoutRegion.UNKNOWN) && (y2 != LayoutRegion.UNKNOWN)) {
                if (dimension == HORIZONTAL) {
                    g.drawLine(x, y1, x, y2);
                } else {
                    g.drawLine(y1, x, y2, x);
                }
            }
        }
        int lastAlignment = -1;
        while (interval.getParent() != null) {
            LayoutInterval parent = interval.getParent();
            if (parent.getType() == SEQUENTIAL) {
                int alignment = LayoutInterval.getEffectiveAlignment(interval);
                int index = parent.indexOf(interval);
                int start, end;
                switch (alignment) {
                    case LEADING:
                        start = 0;
                        end = index;
                        lastAlignment = LEADING;
                        break;
                    case TRAILING:
                        start = index + 1;
                        end = parent.getSubIntervalCount();
                        lastAlignment = TRAILING;
                        break;
                    default: switch (lastAlignment) {
                        case LEADING: start = 0; end = index; break;
                        case TRAILING: start = index+1; end = parent.getSubIntervalCount(); break;
                        default: start = 0; end = parent.getSubIntervalCount(); break;
                    }
                }
                for (int i=start; i<end; i++) {
                    LayoutInterval candidate = parent.getSubInterval(i);
                    if (candidate.isEmptySpace()) {
                        paintAlignment(g, candidate, dimension, LayoutInterval.getEffectiveAlignment(candidate));
                    }
                }
            } else {
                int alignment = interval.getAlignment();
                if (!LayoutInterval.wantResizeInLayout(interval)) {
                    lastAlignment = alignment;
                }
                paintAlignment(g, interval, dimension, lastAlignment);
            }
            interval = interval.getParent();
        }
    }

    private void paintUnplacedWarningImage(Graphics2D g, LayoutComponent comp) {
        LayoutRegion region = comp.getCurrentSpace();
        Rectangle rect = region.toRectangle(new Rectangle());
        Image image = getWarningImage();
        g.drawImage(image, rect.x+rect.width-image.getWidth(null), rect.y, null);
    }

    private Image getWarningImage() {
        if (warningImage == null) {
            warningImage = ImageUtilities.loadImage("org/netbeans/modules/form/layoutsupport/resources/warning.png"); // NOI18N
        }
        return warningImage;
    }
    
    private void paintLinks(Graphics2D g, LayoutComponent component) {
        if ((component.isLinkSized(HORIZONTAL)) && (component.isLinkSized(VERTICAL))) {
            Map<Integer,List<String>> linkGroupsH = layoutModel.getLinkSizeGroups(HORIZONTAL);            
            Map<Integer,List<String>> linkGroupsV = layoutModel.getLinkSizeGroups(VERTICAL);
            Integer linkIdH = new Integer(component.getLinkSizeId(HORIZONTAL));
            Integer linkIdV = new Integer(component.getLinkSizeId(VERTICAL));
            
            List<String> lH = linkGroupsH.get(linkIdH);
            List<String> lV = linkGroupsV.get(linkIdV);

            Set<String> merged = new HashSet<String>(); 
            for (int i=0; i < lH.size(); i++) {
                merged.add(lH.get(i));
            }
            for (int i=0; i < lV.size(); i++) {
                merged.add(lV.get(i));
            }

            Iterator<String> mergedIt = merged.iterator();
            while (mergedIt.hasNext()) {
                String id = mergedIt.next();
                LayoutComponent lc = layoutModel.getLayoutComponent(id);
                LayoutInterval interval = lc.getLayoutInterval(HORIZONTAL);
                LayoutRegion region = interval.getCurrentSpace();
                Image badge = null;
                if ((lV.contains(id)) && (lH.contains(id))) {
                    badge = getLinkBadge(BOTH_DIMENSIONS);
                } else {
                    if (lH.contains(lc.getId())) {
                        badge = getLinkBadge(HORIZONTAL);
                    }
                    if (lV.contains(lc.getId())) {
                        badge = getLinkBadge(VERTICAL);
                    }
                }
                int x = region.positions[HORIZONTAL][TRAILING] - region.size(HORIZONTAL) / 4  - (badge.getWidth(null) / 2);
                int y = region.positions[VERTICAL][LEADING] - (badge.getHeight(null));
                g.drawImage(badge, x, y, null);
            }
        } else {
            int dimension = (component.isLinkSized(HORIZONTAL)) ? HORIZONTAL : VERTICAL;
            Map map =  layoutModel.getLinkSizeGroups(dimension);
            
            Integer linkId = new Integer(component.getLinkSizeId(dimension));
            List l = (List)map.get(linkId);
            Iterator mergedIt = l.iterator();
            
            while (mergedIt.hasNext()) {
                String id = (String)mergedIt.next();
                LayoutComponent lc = layoutModel.getLayoutComponent(id);
                LayoutInterval interval = lc.getLayoutInterval(dimension);
                LayoutRegion region = interval.getCurrentSpace();
                Image badge = getLinkBadge(dimension);
                int x = region.positions[HORIZONTAL][TRAILING] - region.size(HORIZONTAL) / 4 - (badge.getWidth(null) / 2);
                int y = region.positions[VERTICAL][LEADING] - (badge.getHeight(null));
                g.drawImage(badge, x, y, null);
            }
        }
    }
    
    private Image getLinkBadge(int dimension) {
        if (dimension == (BOTH_DIMENSIONS)) {
            if (linkBadgeBoth == null) {
                linkBadgeBoth = ImageUtilities.loadImage("org/netbeans/modules/form/resources/sameboth.png"); //NOI18N
            }
            return linkBadgeBoth;
        }
        if (dimension == HORIZONTAL) {
            if (linkBadgeHorizontal == null) {
                linkBadgeHorizontal = ImageUtilities.loadImage("org/netbeans/modules/form/resources/samewidth.png"); //NOI18N
            }
            return linkBadgeHorizontal;
        }
        if (dimension == VERTICAL) {
            if (linkBadgeVertical == null) {
                linkBadgeVertical = ImageUtilities.loadImage("org/netbeans/modules/form/resources/sameheight.png"); //NOI18N
            }
            return linkBadgeVertical;
        }
        return null;
    }
    
    private void paintAlignment(Graphics2D g, LayoutInterval interval, int dimension, int alignment) {
        LayoutInterval parent = interval.getParent();
        boolean baseline = parent.isParallel() && (parent.getGroupAlignment() == BASELINE);
        LayoutRegion group = parent.getCurrentSpace();
        int opposite = (dimension == HORIZONTAL) ? VERTICAL : HORIZONTAL;
        int x1, x2, y;
        if (interval.isEmptySpace()) {
            int index = parent.indexOf(interval);
            int[] ya, yb;
            boolean x1group, x2group;
            if (index == 0) {
                x1 = group.positions[dimension][baseline ? BASELINE : LEADING];
                ya = visualIntervalPosition(parent, opposite, LEADING);
                x1group = LayoutInterval.getFirstParent(interval, PARALLEL).getParent() != null;
            } else {
                LayoutInterval x1int = parent.getSubInterval(index-1);
                if (x1int.isParallel() && (x1int.getGroupAlignment() == BASELINE)) {
                    x1 = x1int.getCurrentSpace().positions[dimension][BASELINE];
                } else {
                    if (x1int.isEmptySpace()) return;
                    x1 = x1int.getCurrentSpace().positions[dimension][TRAILING];
                }
                ya = visualIntervalPosition(x1int, opposite, TRAILING);
                x1group = x1int.isGroup();
            }
            if (index + 1 == parent.getSubIntervalCount()) {
                x2 = group.positions[dimension][baseline ? BASELINE : TRAILING];
                yb = visualIntervalPosition(parent, opposite, TRAILING);
                x2group = LayoutInterval.getFirstParent(interval, PARALLEL).getParent() != null;
            } else {
                LayoutInterval x2int = parent.getSubInterval(index+1);
                if (x2int.isParallel() && (x2int.getGroupAlignment() == BASELINE)) {
                    x2 = x2int.getCurrentSpace().positions[dimension][BASELINE];
                } else {
                    if (x2int.isEmptySpace()) return;
                    x2 = x2int.getCurrentSpace().positions[dimension][LEADING];
                }
                yb = visualIntervalPosition(x2int, opposite, LEADING);
                x2group = x2int.isGroup();
            }
            if ((x1 == LayoutRegion.UNKNOWN) || (x2 == LayoutRegion.UNKNOWN)) return;
            int y1 = Math.min(ya[1], yb[1]);
            int y2 = Math.max(ya[0], yb[0]);
            y = (y1 + y2)/2;
            if ((ya[1] < yb[0]) || (yb[1] < ya[0])) {
                // no intersection
                if (dimension == HORIZONTAL) {
                    g.drawLine(x1, ya[0], x1, y);
                    g.drawLine(x1, ya[0], x1, ya[1]);
                    g.drawLine(x2, yb[0], x2, y);
                    g.drawLine(x2, yb[0], x2, yb[1]);
                } else {
                    g.drawLine(ya[0], x1, y, x1);
                    g.drawLine(ya[0], x1, ya[1], x1);
                    g.drawLine(yb[0], x2, y, x2);
                    g.drawLine(yb[0], x2, yb[1], x2);
                }
            } else {
                if (dimension == HORIZONTAL) {
                    if (x1group) g.drawLine(x1, ya[0], x1, ya[1]);
                    if (x2group) g.drawLine(x2, yb[0], x2, yb[1]);
                } else {
                    if (x1group) g.drawLine(ya[0], x1, ya[1], x1);
                    if (x2group) g.drawLine(yb[0], x2, yb[1], x2);
                }
            }
        } else {
            LayoutRegion child = interval.getCurrentSpace();
            if ((alignment == LEADING) || (alignment == TRAILING)) {
                x1 = group.positions[dimension][baseline ? BASELINE : alignment];
                if (interval.isParallel() && (interval.getAlignment() == BASELINE)) {
                    x2 = child.positions[dimension][BASELINE];
                } else {
                    x2 = child.positions[dimension][alignment];
                }
            } else {
                return;
            }
            if ((x1 == LayoutRegion.UNKNOWN) || (x2 == LayoutRegion.UNKNOWN)) return;
            int[] pos = visualIntervalPosition(parent, opposite, alignment);
            y = (pos[0] + pos[1])/2;
            int xa = group.positions[dimension][LEADING];
            int xb = group.positions[dimension][TRAILING];
            if (parent.getParent() != null) {
                if (dimension == HORIZONTAL) {
                    if (alignment == LEADING) {
                        g.drawLine(xa, pos[0], xa, pos[1]);
                    } else if (alignment == TRAILING) {
                        g.drawLine(xb, pos[0], xb, pos[1]);
                    }
                } else {
                    if (alignment == LEADING) {
                        g.drawLine(pos[0], xa, pos[1], xa);
                    } else if (alignment == TRAILING) {
                        g.drawLine(pos[0], xb, pos[1], xb);
                    }
                }
            }
        }
        // Avoid overload of EQ when current space is incorrectly calculated.
        if ((x2 - x1 > 1) && (Math.abs(y) <= Short.MAX_VALUE)
            && (Math.abs(x1) <= Short.MAX_VALUE) && (Math.abs(x2) <= Short.MAX_VALUE)) {
            int x, angle;            
            if (alignment == LEADING) {
                x = x1;
                angle = 180;
            } else {
                x = x2;
                angle = 0;
            }
            x2--;
            int diam = Math.min(4, x2-x1);
            Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_BEVEL, 0, new float[] {1, 1}, 0);
            Stroke oldStroke = g.getStroke();
            g.setStroke(stroke);
            if (dimension == HORIZONTAL) {
                g.drawLine(x1, y, x2, y);
                angle += 90;
            } else {
                g.drawLine(y, x1, y, x2);
                int temp = x; x = y; y = temp;
            }
            g.setStroke(oldStroke);
            if ((alignment == LEADING) || (alignment == TRAILING)) {
                Object hint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.fillArc(x-diam, y-diam, 2*diam, 2*diam, angle, 180);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, hint);
            }
        }
    }
    
    private int[] visualIntervalPosition(LayoutInterval interval, int dimension, int alignment) {
        int min = Short.MAX_VALUE;
        int max = Short.MIN_VALUE;
        if (interval.isParallel() && (interval.getGroupAlignment() != BASELINE)) {
            Iterator iter = interval.getSubIntervals();
            while (iter.hasNext()) {
                LayoutInterval subInterval = (LayoutInterval)iter.next();
                int imin, imax;
                int oppDim = (dimension == HORIZONTAL) ? VERTICAL : HORIZONTAL;
                if (LayoutInterval.isPlacedAtBorder(subInterval, oppDim, alignment)) {
                    if (subInterval.isParallel()) {
                        int[] ipos = visualIntervalPosition(subInterval, dimension, alignment);
                        imin = ipos[0]; imax = ipos[1];
                    } else if (!subInterval.isEmptySpace()) {
                        LayoutRegion region = subInterval.getCurrentSpace();
                        imin = region.positions[dimension][LEADING];
                        imax = region.positions[dimension][TRAILING];                        
                    } else {
                        imin = min; imax = max;
                    }
                } else {
                    imin = min; imax = max;
                }
                if (min > imin) min = imin;
                if (max < imax) max = imax;
            }
        }
        if (!interval.isParallel() || (min == Short.MAX_VALUE)) {
            LayoutRegion region = interval.getCurrentSpace();
            min = region.positions[dimension][LEADING];
            max = region.positions[dimension][TRAILING];
        }
        return new int[] {min, max};
    }

    // -----

    Collection<GapInfo> getPaintedGaps() {
        return paintedGaps;
    }

    void paintGaps(Graphics2D g, Collection<LayoutComponent> selectedComponents, Collection<GapInfo> selectedGaps) {
        if (selectedComponents == null || selectedComponents.isEmpty()) {
            componentsOfPaintedGaps = null;
            paintedGaps = null;
            return;
        }

        List<GapInfo> newGaps = (paintedGaps == null || newSelectionForGaps(selectedComponents, selectedGaps))
                ? new ArrayList<GapInfo>(100) : null;
        Map<LayoutInterval, GapInfo> gapMap = new HashMap<LayoutInterval, GapInfo>();
        for (LayoutComponent component : (newGaps != null ? selectedComponents : componentsOfPaintedGaps)) {
            for (GapInfo gapInfo : visualState.getComponentGaps(component)) {
                if (newGaps == null && gapInfo.paintRect != null) {
                    break; // everything is up-to-date
                }
                setPaintRectForGap(gapInfo);
                // There can be multiple GapInfo objects for one gap if multiple
                // components are selected. Compute their union rectangle for visualization.
                GapInfo paintRep = gapMap.get(gapInfo.gap);
                if (paintRep != null) {
                    expandOrtPaintRect(paintRep, gapInfo);
                } else {
                    gapMap.put(gapInfo.gap, gapInfo);
                    if (newGaps != null) {
                        newGaps.add(gapInfo);
                    }
                }
            }
        }
        if (newGaps != null) {
            componentsOfPaintedGaps = new ArrayList<LayoutComponent>(selectedComponents);
            paintedGaps = newGaps;
        }

        if (paintedGaps != null && !paintedGaps.isEmpty()) {
            Color oldColor = g.getColor();
            for (GapInfo gapInfo : paintedGaps) {
                if (selectedGaps.contains(gapInfo)) {
                    continue;
                }
                paintGap(g, gapInfo, false);
                if (gapInfo.overlappingComponents != null) {
                    for (String compId : gapInfo.overlappingComponents) {
                        visualState.repaintComponent(compId, g);
                    }
                }
            }
            for (GapInfo gapInfo : selectedGaps) {
                paintGap(g, gapInfo, true);
                if (selectedGaps.size() == 1) {
                    paintGapResizeHandles(g, gapInfo);
                }
                if (gapInfo.overlappingComponents != null) {
                    for (String compId : gapInfo.overlappingComponents) {
                        visualState.repaintComponent(compId, g);
                    }
                }
            }
            g.setColor(oldColor);
        }
    }

    private boolean newSelectionForGaps(Collection<LayoutComponent> selectedComponents,
                                        Collection<GapInfo> selectedGaps) {
        if (componentsOfPaintedGaps == null || componentsOfPaintedGaps.isEmpty()) {
            return true;
        }
        if (selectedComponents.size() == componentsOfPaintedGaps.size()
                && selectedComponents.containsAll(componentsOfPaintedGaps)) {
            return false;
        }
        if (selectedGaps.isEmpty() || selectedComponents.size() != 1) {
            return true;
        }
        // Check for special case when clicked on a gap next to a selected
        // component. Even though the container gets selected in such case,
        // we don't want to paint all container gaps, but only the gaps of
        // previously selected components.
        LayoutComponent maybeContainer = selectedComponents.toArray(new LayoutComponent[1])[0];
        for (LayoutComponent comp : componentsOfPaintedGaps) {
            if (comp.getParent() != maybeContainer) {
                return true;
            }
        }
        for (GapInfo gapInfo : selectedGaps) {
            if (!paintedGaps.contains(gapInfo)) {
                return true;
            }
        }
        return false;
    }

    private static void setPaintRectForGap(GapInfo gapInfo) {
        Rectangle r;
        if (gapInfo.dimension == HORIZONTAL) {
            r = new Rectangle(gapInfo.position, gapInfo.ortPositions[LEADING],
                    gapInfo.currentSize, gapInfo.ortPositions[TRAILING] - gapInfo.ortPositions[LEADING]);
        } else {
            r = new Rectangle(gapInfo.ortPositions[LEADING], gapInfo.position,
                    gapInfo.ortPositions[TRAILING] - gapInfo.ortPositions[LEADING], gapInfo.currentSize);
        }
        gapInfo.paintRect = r;
    }

    private static void expandOrtPaintRect(GapInfo gapInfo, GapInfo exp) {
        Rectangle r = gapInfo.paintRect;
        Rectangle er = exp.paintRect;
        if (exp.dimension == HORIZONTAL) {
            if (er.y < r.y) {
                r.y = er.y;
            }
            if (er.height > r.height) {
                r.height = er.height;
            }
        } else {
            if (er.x < r.x) {
                r.x = er.x;
            }
            if (er.width > r.width) {
                r.width = er.width;
            }
        }
    }

    // called from dragger
    void paintGapResizing(Graphics2D g, GapInfo resGap, Rectangle resRect, boolean defaultSize) {
        if (paintedGaps != null && !paintedGaps.isEmpty()) {
            Color originalColor = g.getColor();
            for (GapInfo gapInfo : paintedGaps) {
                if (gapInfo != resGap) {
                    paintGap(g, gapInfo, false);
                    if (gapInfo.overlappingComponents != null) {
                        for (String compId : gapInfo.overlappingComponents) {
                            visualState.repaintComponent(compId, g);
                        }
                    }
                }
            }
            paintDraggedGap(g, resRect, resGap.dimension, LayoutInterval.canResize(resGap.gap));
            g.setColor(originalColor);
        }
    }

    private static void paintGap(Graphics2D g, GapInfo gapInfo, boolean selected) {
        int x1 = gapInfo.paintRect.x;
        int y1 = gapInfo.paintRect.y;
        int w1 = gapInfo.paintRect.width;
        int h1 = gapInfo.paintRect.height;
        int x2, y2, w2, h2;
        if (gapInfo.dimension == HORIZONTAL) {
            w2 = gapInfo.minSize;
            x2 = x1 + ((w1 - w2) / 2);
            if (h1 >= 4) {
                h1 -= 2;
                y1 += 1;
            }
            y2 = y1;
            h2 = h1;
        } else {
            h2 = gapInfo.minSize;
            y2 = y1 + ((h1 - h2) / 2);
            if (w1 >= 4) {
                w1 -= 2;
                x1 += 1;
            }
            x2 = x1;
            w2 = w1;
        }
        boolean differentMinSize = (w1 != w2 || h1 != h2);
        boolean resizing = LayoutInterval.canResize(gapInfo.gap);
        if (differentMinSize || (resizing && !PAINT_RES_GAP_MIN_SIZE)) {
            g.setColor(getResizingGapColor(selected));
            g.fillRect(x1+1, y1+1, w1-1, h1-1);
        }
        if (w2 > 0 && h2 > 0 && (!resizing || PAINT_RES_GAP_MIN_SIZE)) {
            g.setColor(differentMinSize ? getMinGapColor(selected) : getFixedGapColor(selected));
            g.fillRect(x2+1, y2+1, w2-1, h2-1);
        }
        if (resizing) {
            g.setColor(getSawColor(selected));
            if (gapInfo.dimension == HORIZONTAL && h1 > 4) { // paint a horizontal "saw"
                int count = h1 / 120 + 1;
                int step = h1 / count;
                for (int by=y1+step/2; count > 0; by+=step, count--) {
                    int d = h1 > 40 ? 4 : (h1 > 12 ? 3 : 2);
                    int d1 = -d;
                    int d2 = d;
                    for (int x=PAINT_RES_GAP_MIN_SIZE ? x2 : x1+w1/2;
                            x-4 >= x1;
                            x-=4, d1*=-1, d2*=-1) {
                        g.drawLine(x, by+d1, x-4, by+d2);
                    }
                    d1 = -d;
                    d2 = d;
                    for (int x=PAINT_RES_GAP_MIN_SIZE ? x2+w2-1 : x1+w1/2, xx=x1+w1;
                            x+4 < xx;
                            x+=4, d1*=-1, d2*=-1) {
                        g.drawLine(x, by+d1, x+4, by+d2);
                    }
            }
            } else if (w1 > 4) { // paint a vertical "saw"
                int count = w1 / 120 + 1;
                int step = w1 / count;
                for (int bx=x1+step/2; count > 0; bx+=step, count--) {
                    int d = w1 > 40 ? 4 : (w1 > 8 ? 3 : 2);
                    int d1 = -d;
                    int d2 = d;
                    for (int y=PAINT_RES_GAP_MIN_SIZE ? y2 : y1+h1/2;
                            y-4 >= y1;
                            y-=4, d1*=-1, d2*=-1) {
                        g.drawLine(bx+d1, y, bx+d2, y-4);
                    }
                    d1 = -d;
                    d2 = d;
                    for (int y=PAINT_RES_GAP_MIN_SIZE ? y2+h2-1 : y1+h1/2, yy=y1+h1;
                            y+4 < yy;
                            y+=4, d1*=-1, d2*=-1) {
                        g.drawLine(bx+d1, y, bx+d2, y+4);
                    }
                }
            }
        }
        g.setColor(getGapBorderColor(selected));
        g.drawRect(x1, y1, w1-1, h1-1);
    }

    private static void paintDraggedGap(Graphics2D g, Rectangle gapRect, int dimension, boolean resizing) {
        int x = gapRect.x;
        int y = gapRect.y;
        int w = gapRect.width;
        int h = gapRect.height;
        if (dimension == HORIZONTAL) {
            if (h >= 4) {
                h -= 2;
                y += 1;
            }
        } else {
            if (w >= 4) {
                w -= 2;
                x += 1;
            }
        }
        Composite originalComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g.setColor(resizing ? getResizingGapColor(true) : getFixedGapColor(true));
        g.fillRect(x+1, y+1, w-1, h-1);
        g.setColor(getGapBorderColor(true));
        g.drawRect(x, y, w-1, h-1);
        g.setComposite(originalComposite);
    }

    private static final Color[] HANDLE_COLORS = { new Color(255, 255, 243), //new Color(253, 253, 252),
                                                   new Color(242, 238, 230), //new Color(244, 247, 246),
                                                   new Color(221, 217, 209) }; // new Color(223, 230, 234)
    private static final int HANDLE_WIDTH = HANDLE_COLORS.length;

    private void paintGapResizeHandles(Graphics2D g, GapInfo gapInfo) {
        Rectangle r = gapInfo.paintRect;
        if (gapInfo.dimension == HORIZONTAL && r.width > 5) { // horizontal gap - paint vertical handles
            if (gapInfo.resizeLeading) {
                paintGapResizeHandle(g, r.x+1, r.y, r.height, VERTICAL, LEADING);
            }
            if (gapInfo.resizeTrailing) {
                paintGapResizeHandle(g, r.x+r.width-HANDLE_WIDTH-1, r.y, r.height, VERTICAL, TRAILING);
            }
        } else if (gapInfo.dimension == VERTICAL && r.height > 5) { // vertical gap - paint horizontal handles
            if (gapInfo.resizeLeading) {
                paintGapResizeHandle(g, r.x, r.y+1, r.width, HORIZONTAL, LEADING);
            }
            if (gapInfo.resizeTrailing) {
                paintGapResizeHandle(g, r.x, r.y+r.height-HANDLE_WIDTH-1, r.width, HORIZONTAL, TRAILING);
            }
        }
    }

    static int pointOnResizeHandler(GapInfo gapInfo, Point p) {
        Rectangle r = gapInfo.paintRect;
        if (r != null) {
            if (gapInfo.dimension == HORIZONTAL) { // horizontal gap - vertical handle
                int ll = r.x-1; // TODO subtract more if the width is really small
                int lt = r.x+HANDLE_WIDTH+2;
                int tl = r.x+r.width-HANDLE_WIDTH-3;
                int tt = r.x+r.width; // TODO add more if the width is really small
                if (gapInfo.resizeTrailing) {
                    if (tl < r.x + r.width/2) {
                        if (gapInfo.resizeLeading) {
                            tl = r.x + r.width/2;
                        } else if (tl < r.x) {
                            tl = r.x;
                        }
                    }
                    if (pointInArea(p, tl, tt, r.y-1, r.y+r.height+1)) {
                        return TRAILING;
                    }
                }
                if (gapInfo.resizeLeading) {
                    if (lt > r.x + r.width/2) {
                        if (gapInfo.resizeTrailing) {
                            lt = r.x + r.width/2;
                        } else if (lt > tt) {
                            lt = tt;
                        }
                    }
                    if (pointInArea(p, ll, lt, r.y-1, r.y+r.height+1)) {
                        return LEADING;
                    }
                }
            } else { // vertical gap - horizontal handle
                int ll = r.y-1; // TODO subtract more if the height is really small
                int lt = r.y+HANDLE_WIDTH+2;
                int tl = r.y+r.height-HANDLE_WIDTH-3;
                int tt = r.y+r.height; // TODO add more if the height is really small
                if (gapInfo.resizeTrailing) {
                    if (tl < r.y + r.height/2) {
                        if (gapInfo.resizeLeading) {
                            tl = r.y + r.height/2;
                        } else if (tl < ll) {
                            tl = ll;
                        }
                    }
                    if (pointInArea(p, r.x-1, r.x+r.width+1, tl, tt)) {
                        return TRAILING;
                    }
                }
                if (gapInfo.resizeLeading) {
                    if (lt > r.y + r.height/2) {
                        if (gapInfo.resizeTrailing) {
                            lt = r.y + r.height/2;
                        } else if (lt > tt) {
                            lt = tt;
                        }
                    }
                    if (pointInArea(p, r.x-1, r.x+r.width+1, ll, lt)) {
                        return LEADING;
                    }
                }
            }
        }
        return -1;
    }

    private static boolean pointInArea(Point p, int x1, int x2, int y1, int y2) {
        return p.x >= x1 && p.x < x2 && p.y >= y1 && p.y < y2;
    }

    private void paintGapResizeHandle(Graphics2D g, int x, int y, int length, int dimension, int alignment) {
        int correction;
        if (length < 6) {
            correction = 6 - length;
        } else if (length < 10) {
            correction = length - 6;
        } else {
            correction = -4;
        }
        length += correction;
        int dx, dy;
        if (dimension == HORIZONTAL) {
            dx = 0; dy = 1;
            x -= correction / 2;
        } else {
            dx = 1; dy = 0;
            y -= correction / 2;
        }

        for (int i=0; i < HANDLE_WIDTH; i++) {
            g.setColor(HANDLE_COLORS[alignment == LEADING ? i : HANDLE_WIDTH-i-1]);
            int px = x + i*dx;
            int py = y + i*dy;
            g.drawLine(px, py, px + length*dy - dy, py + length*dx - dx);
        }
    }

    private static Color fixedGapColor = new Color(220, 220, 220); // 208, 208, 208
    private static Color getFixedGapColor(boolean selected) {
        return selected ? selectedColor(fixedGapColor) : fixedGapColor;
    }

    private static Color resGapColor = new Color(224, 224, 224);
    private static Color getResizingGapColor(boolean selected) {
        return selected ? selectedColor(resGapColor) : resGapColor;
    }

    private static Color minGapColor = new Color(212, 212, 212); // 204, 204, 204
    private static Color getMinGapColor(boolean selected) {
        return selected ? selectedColor(minGapColor) : minGapColor;
    }

    private static Color gapBorderColor = new Color(200, 200, 200); // 192, 192, 192
    private static Color getGapBorderColor(boolean selected) {
        return selected ? selectedColor(gapBorderColor) : gapBorderColor;
    }

    private static Color sawColor = new Color(208, 208, 208);
    private static Color getSawColor(boolean selected) {
        return selected ? selectedColor(sawColor) : sawColor;
    }

    private static Color selectedColor(Color c) {
        return transColor(c, 210, 270, 190); // 322, 256, 176
    }

    private static Color transColor(Color c, int tr, int tg, int tb) {
        return new Color(transColor(c.getRed(), tr),
                         transColor(c.getGreen(), tg),
                         transColor(c.getBlue(), tb));
    }

    private static int transColor(int c, int t) {
        double change = t / 256.0;
//        double changing = 1.0;
//        int result = (int) Math.round(((c * changing * change) + (c * (1.0 - changing))) * brighter);
        int result = (int) Math.round(c * change);
        if (result > 255) {
            result = 255;
        } else if (result < 0) {
            result = 0;
        }
        return result;
    }
}
