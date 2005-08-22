/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutdesign;

import java.util.*;

/**
 * This class collects various static methods for examining the layout.
 * For modifying methods see LayoutOperations class.
 *
 * @author Tomas Pavek
 */

public class LayoutUtils implements LayoutConstants {
    
    private LayoutUtils() {
    }

    public static LayoutInterval getAdjacentEmptySpace(LayoutComponent comp, int dimension, int direction) {
        LayoutInterval interval = comp.getLayoutInterval(dimension);
        LayoutInterval parent;
        while ((parent = interval.getParent()) != null) {
            if (parent.isSequential()) {
                int index = parent.indexOf(interval);
                if (direction == LEADING) {
                    if (index == 0) {
                        interval = parent;
                    } else {
                        LayoutInterval candidate = parent.getSubInterval(index-1);
                        return candidate.isEmptySpace() ? candidate : null;
                    }
                } else {
                    if (index == parent.getSubIntervalCount()-1) {
                        interval = parent;
                    } else {
                        LayoutInterval candidate = parent.getSubInterval(index+1);
                        return candidate.isEmptySpace() ? candidate : null;                        
                    }
                }
            } else {
                // PENDING how should we determine the space: isAlignedAtBorder, isPlacedAtBorder, any?
                if (LayoutInterval.isPlacedAtBorder(interval, dimension, direction)) {
                    interval = parent;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    // -----
    // package private utils

    static LayoutInterval getOutermostComponent(LayoutInterval interval, int dimension, int alignment) {
        if (interval.isComponent()) {
            return interval;
        }

        assert alignment == LEADING || alignment == TRAILING;

        if (interval.isSequential()) {
            int d = alignment == LEADING ? 1 : -1;
            int i = alignment == LEADING ? 0 : interval.getSubIntervalCount()-1;
            while (i >= 0 && i < interval.getSubIntervalCount()) {
                LayoutInterval li = interval.getSubInterval(i);
                if (li.isEmptySpace()) {
                    i += d;
                }
//                else if (li.isComponent()) {
//                    return li;
//                }
                else {
                    return getOutermostComponent(li, dimension, alignment);
                }
            }
//            return null;
        }
        else if (interval.isParallel()) {
            LayoutInterval best = null;
            int pos = Integer.MAX_VALUE;
            for (int i=0, n=interval.getSubIntervalCount(); i < n; i++) {
                LayoutInterval li = getOutermostComponent(interval.getSubInterval(i), dimension, alignment);
                if (li != null) {
                    if (LayoutInterval.isAlignedAtBorder(li, interval, alignment)) {
                        return li;
                    }
                    int p = li.getCurrentSpace().positions[dimension][alignment]
                            * (alignment == LEADING ? 1 : -1);
                    if (p < pos) {
                        best = li;
                        pos = p;
                    }
                }
            }
            return best;
        }
//        else if (interval.isComponent()) {
//            return interval;
//        }
        return null;
    }

    /**
     * Returns size of the empty space represented by the given layout interval.
     *
     * @param interval layout interval that represents padding.
     * @return size of the padding.
     */
    static int getSizeOfDefaultGap(LayoutInterval interval, VisualMapper visualMapper) {
        assert interval.isEmptySpace();
        LayoutInterval parent = interval.getParent();
        if (parent.isParallel())
            return interval.getPreferredSize();
        
        // Find intervals that contain sources and targets
        LayoutInterval candidate = interval;
        LayoutInterval srcInt = null;
        LayoutInterval targetInt = null;
        while ((parent != null) && ((srcInt == null) || (targetInt == null))) {
            int index = parent.indexOf(candidate);
            if ((srcInt == null) && (index > 0)) {
                srcInt = parent.getSubInterval(index-1);
            }
            if ((targetInt == null) && (index < parent.getSubIntervalCount()-1)) {
                targetInt = parent.getSubInterval(index+1);
            }
            if ((srcInt == null) || (targetInt == null)) {
                do {
                    candidate = parent;
                    parent = parent.getParent();
                } while ((parent != null) && parent.isParallel());
            }
        }
        
        // Find sources and targets inside srcInt and targetInt
        List sources = edgeSubComponents(srcInt, TRAILING);
        List targets = edgeSubComponents(targetInt, LEADING);        

        // Calculate size of gap from sources and targets and their positions
        return getSizeOfDefaultGap(sources, targets, visualMapper, null, Collections.EMPTY_MAP);
    }

    static int getSizeOfDefaultGap(List sources, List targets, VisualMapper visualMapper,
                                   String contId, Map boundsMap) {
        int size = 0;
        boolean containerGap = false;
        int containerGapAlignment = -1;
        LayoutInterval temp = null;
        if (sources.isEmpty()) {
            if (targets.isEmpty()) {
                return 0;
            } else {
                // Leading container gap
                containerGap = true;
                containerGapAlignment = LEADING;
                temp = (LayoutInterval)targets.get(0);
            }
        } else {
            temp = (LayoutInterval)sources.get(0);
            if (targets.isEmpty()) {
                // Trailing container gap
                containerGap = true;
                containerGapAlignment = TRAILING;
            }
        }
        int dimension = (temp == temp.getComponent().getLayoutInterval(HORIZONTAL)) ? HORIZONTAL : VERTICAL;
        // Calculate max of sources and min of targets
        int max = Short.MIN_VALUE;
        int min = Short.MAX_VALUE;
        boolean positionsNotUpdated = false;
        Iterator iter = sources.iterator();
        while (iter.hasNext()) {
            LayoutInterval source = (LayoutInterval)iter.next();
            LayoutRegion region = sizeOfEmptySpaceHelper(source, boundsMap);
            int trailing = region.positions[dimension][TRAILING];
            if (trailing == LayoutRegion.UNKNOWN) {
                positionsNotUpdated = true; break;
            } else {
                max = Math.max(max, trailing);
            }
        }
        iter = targets.iterator();
        while (iter.hasNext()) {
            LayoutInterval target = (LayoutInterval)iter.next();
            LayoutRegion region = sizeOfEmptySpaceHelper(target, boundsMap);
            int leading = region.positions[dimension][LEADING];
            if (leading == LayoutRegion.UNKNOWN) {
                positionsNotUpdated = true; break;
            } else {
                min = Math.min(min, leading);
            }
        }
        if (containerGap) {
            iter = sources.isEmpty() ? targets.iterator() : sources.iterator();
            while (iter.hasNext()) {
                LayoutInterval interval = (LayoutInterval)iter.next();
                LayoutComponent component = interval.getComponent();
                LayoutRegion region = sizeOfEmptySpaceHelper(interval, boundsMap);
                String parentId = (contId == null) ? component.getParent().getId() : contId; 
                int padding = visualMapper.getPreferredPaddingInParent(parentId, component.getId(), dimension, containerGapAlignment);
                int position = region.positions[dimension][containerGapAlignment];
                int delta = (containerGapAlignment == LEADING) ? (position - min) : (max - position);
                if (!positionsNotUpdated) padding -= delta;
                size = Math.max(size, padding);
            }            
        } else {
            Iterator srcIter = sources.iterator();
            while (srcIter.hasNext()) {
                LayoutInterval srcCandidate = (LayoutInterval)srcIter.next();                
                String srcId = srcCandidate.getComponent().getId();
                LayoutRegion srcRegion = sizeOfEmptySpaceHelper(srcCandidate, boundsMap);
                int srcDelta = max - srcRegion.positions[dimension][TRAILING];
                Iterator targetIter = targets.iterator();
                while (targetIter.hasNext()) {
                    LayoutInterval targetCandidate = (LayoutInterval)targetIter.next();
                    String targetId = targetCandidate.getComponent().getId();
                    LayoutRegion targetRegion = sizeOfEmptySpaceHelper(targetCandidate, boundsMap);
                    int targetDelta = targetRegion.positions[dimension][LEADING] - min;
                    int padding = visualMapper.getPreferredPadding(srcId,
                        targetId, dimension, LEADING, VisualMapper.PADDING_RELATED);
                    if (!positionsNotUpdated) padding -= srcDelta + targetDelta;
                    size = Math.max(size, padding);
                }
            }
        }
        return size;        
    }

    private static LayoutRegion sizeOfEmptySpaceHelper(LayoutInterval interval, Map boundsMap) {
        LayoutComponent component = interval.getComponent();
        String compId = component.getId();
        if (boundsMap.containsKey(compId)) {
            return (LayoutRegion)boundsMap.get(compId);
        } else {
            return interval.getCurrentSpace();
        }
    }

    /**
     * Returns list of components that reside in the <code>root</code>
     * layout interval - the list contains only components whose layout
     * intervals lie at the specified edge (<code>LEADING</code>
     * or <code>TRAILING</code>) of the <code>root</code> layout interval.
     *
     * @param root layout interval that will be scanned.
     * @param edge the requested edge the components shoul be next to.
     * @return <code>List</code> of <code>LayoutInterval</code>s that
     * represent <code>LayoutComponent</code>s.
     */
    static List edgeSubComponents(LayoutInterval root, int edge) {
        List components = new LinkedList();
        List candidates = new LinkedList();
        if (root != null) {
            candidates.add(root);
        }
        while (!candidates.isEmpty()) {
            LayoutInterval candidate = (LayoutInterval)candidates.get(0);
            candidates.remove(candidate);
            if (candidate.isGroup()) {
                if (candidate.isSequential()) {
                    int index = (edge == LEADING) ? 0 : candidate.getSubIntervalCount()-1;
                    candidates.add(candidate.getSubInterval(index));
                } else {
                    Iterator subs = candidate.getSubIntervals();
                    while (subs.hasNext()) {
                        candidates.add(subs.next());
                    }
                }
            } else if (candidate.isComponent()) {
                components.add(candidate);
            }
        }
        return components;
    }

    /**
     * Computes whether a space overlaps with content of given interval.
     * The difference from LayoutRegion.overlap(...) is that this method goes
     * recursivelly down to components in case of a group - does not use the
     * union space for whole group (which might be inaccurate).
     */
    static boolean contentOverlap(LayoutRegion space, LayoutInterval interval, int dimension) {
        return contentOverlap(space, interval, -1, -1, dimension);
    }

    static boolean contentOverlap(LayoutRegion space, LayoutInterval interval, int fromIndex, int toIndex, int dimension) {
        LayoutRegion examinedSpace = interval.getCurrentSpace();
        if (!interval.isGroup()) {
            return LayoutRegion.overlap(space, examinedSpace, dimension, 0);
        }
        boolean overlap = !examinedSpace.isSet(dimension)
                          || LayoutRegion.overlap(space, examinedSpace, dimension, 0);
        if (overlap) {
            if (fromIndex < 0)
                fromIndex = 0;
            if (toIndex < 0)
                toIndex = interval.getSubIntervalCount()-1;
            assert fromIndex <= toIndex;

            overlap = false;
            for (int i=fromIndex; i <= toIndex; i++) {
                LayoutInterval li = interval.getSubInterval(i);
                if (!li.isEmptySpace() && contentOverlap(space, li, dimension)) {
                    overlap = true;
                    break;
                }
            }
        }
        return overlap;
    }
}
