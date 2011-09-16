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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        LayoutInterval gap = LayoutInterval.getNeighbor(interval, direction, false, true, false);
        if (gap != null && gap.isEmptySpace()) {
            LayoutInterval gapNeighbor = LayoutInterval.getDirectNeighbor(gap, direction^1, true);
            if (gapNeighbor == interval || LayoutInterval.isPlacedAtBorder(interval, gapNeighbor, dimension, direction)) {
                return gap;
            }
        }
        return null;
//        LayoutInterval parent;
//        while ((parent = interval.getParent()) != null) {
//            if (parent.isSequential()) {
//                int index = parent.indexOf(interval);
//                if (direction == LEADING) {
//                    if (index == 0) {
//                        interval = parent;
//                    } else {
//                        LayoutInterval candidate = parent.getSubInterval(index-1);
//                        return candidate.isEmptySpace() ? candidate : null;
//                    }
//                } else {
//                    if (index == parent.getSubIntervalCount()-1) {
//                        interval = parent;
//                    } else {
//                        LayoutInterval candidate = parent.getSubInterval(index+1);
//                        return candidate.isEmptySpace() ? candidate : null;                        
//                    }
//                }
//            } else {
//                // PENDING how should we determine the space: isAlignedAtBorder, isPlacedAtBorder, any?
//                if (LayoutInterval.isPlacedAtBorder(interval, dimension, direction)) {
//                    interval = parent;
//                } else {
//                    return null;
//                }
//            }
//        }
//        return null;
    }

    public static boolean hasAdjacentComponent(LayoutComponent comp, int dimension, int direction) {
        return LayoutInterval.getNeighbor(comp.getLayoutInterval(dimension), direction, true, true, false)
                != null;
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
                } else {
                    return getOutermostComponent(li, dimension, alignment);
                }
            }
        } else if (interval.isParallel()) {
            LayoutInterval best = null;
            int pos = Integer.MAX_VALUE;
            for (int i=0, n=interval.getSubIntervalCount(); i < n; i++) {
                LayoutInterval li = interval.getSubInterval(i);
                li = getOutermostComponent(li, dimension, alignment);
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
        return null;
    }

    /**
     * @param exclude except this one
     * @return true if there is a component placed directly or with default gap
     *         at the group edge (does not have to be aligned)
     */
    static boolean anythingAtGroupEdge(LayoutInterval group, LayoutInterval exclude, int dimension, int alignment) {
        List<LayoutInterval> list = new LinkedList<LayoutInterval>();
        list.add(group);
        while (!list.isEmpty()) {
            LayoutInterval interval = list.remove(0);
            if (interval.isParallel()) {
                for (Iterator<LayoutInterval> it = interval.getSubIntervals(); it.hasNext(); ) {
                    LayoutInterval li = it.next();
                    if (li != exclude
                        && (LayoutInterval.isAlignedAtBorder(li, interval, alignment)
                            || LayoutInterval.isPlacedAtBorder(li, interval, dimension, alignment))) {
                        if (li.isComponent()) {
                            return true;
                        } else if (li.isGroup()) {
                            list.add(li);
                        }
                    }
                }
            } else if (interval.isSequential()) {
                LayoutInterval li = interval.getSubInterval(
                        alignment==LEADING ? 0 : interval.getSubIntervalCount()-1);
                if (li.isComponent()
                    || (li.isEmptySpace() && li.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                        && !LayoutInterval.canResize(li))) {
                    return true;
                } else if (li.isParallel()) {
                    list.add(li);
                }
            }
        }
        return false;
    }

    static int getPositionWithoutGap(Collection<LayoutInterval> intervals, int dimension, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;
        int outermostPos = Integer.MIN_VALUE;
        for (LayoutInterval li : intervals) {
            assert !li.isEmptySpace();
            LayoutInterval interval = null;
            if (li.isSequential()) {
                int idx = (alignment == LEADING ? 0 : li.getSubIntervalCount()-1);
                while (idx >= 0 && idx < li.getSubIntervalCount()) {
                    interval = li.getSubInterval(idx);
                    if (!interval.isEmptySpace()) {
                        break;
                    } else {
                        interval = null;
                        idx += (alignment == LEADING ? 1 : -1);
                    }
                }
            } else {
                interval = li;
            }
            if (interval != null) {
                int pos = interval.getCurrentSpace().positions[dimension][alignment];
                if (LayoutRegion.isValidCoordinate(pos)
                        && (outermostPos == Integer.MIN_VALUE
                            || (alignment == LEADING && pos < outermostPos)
                            || (alignment == TRAILING && pos > outermostPos))) {
                    outermostPos = pos;
                }
            }
        }
        return outermostPos;
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
        List sources = edgeSubComponents(srcInt, TRAILING, false);
        List targets = edgeSubComponents(targetInt, LEADING, false);

        // Calculate size of gap from sources and targets and their positions
        return getSizesOfDefaultGap(sources, targets, interval.getPaddingType(),
                                    visualMapper, null, Collections.EMPTY_MAP)[0];
    }

    /**
     * Finds out the sizes of given types of default gaps between the trailing
     * edge of a set of "source" components and the leading edge of a set of
     * "target" components.
     * @param gapType the padding type whose size should be returned, or null
     *        if all types should be determined
     * @return array of sizes - one element if specific padding type is asked or
     *         four elements if null is provided
     */
    static int[] getSizesOfDefaultGap(List sources, List targets, PaddingType gapType,
                VisualMapper visualMapper, String contId, Map<String,LayoutRegion> boundsMap) {
        if (((sources != null) && (sources.isEmpty()))
            || ((targets != null) && (targets.isEmpty()))) {
            return new int[] { 0 }; // Preferred gap not between components
        }
        sources = (sources == null) ? Collections.EMPTY_LIST : sources;
        targets = (targets == null) ? Collections.EMPTY_LIST : targets;
        boolean containerGap = false;
        int containerGapAlignment = -1;
        LayoutInterval temp = null;
        if (sources.isEmpty()) {
            if (targets.isEmpty()) {
                return new int[] { 0 };
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

        int[] sizes;
        if (containerGap) {
            sizes = new int[1];
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
                sizes[0] = Math.max(sizes[0], padding);
            }            
        } else {
            PaddingType[] paddingTypes = // just one, or all types of gaps
                    gapType != null ? new PaddingType[] { gapType } : PADDINGS;
            sizes = new int[paddingTypes.length]; 
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
                    for (int i=0; i < paddingTypes.length; i++) {
                        PaddingType type = paddingTypes[i];
                        int padding = visualMapper.getPreferredPadding(srcId,
                            targetId, dimension, LEADING, type);
                        if (!positionsNotUpdated) padding -= srcDelta + targetDelta;
                        sizes[i] = Math.max(sizes[i], padding);
                    }
                }
            }
        }
        return sizes;
    }

    private static LayoutRegion sizeOfEmptySpaceHelper(LayoutInterval interval, Map<String,LayoutRegion> boundsMap) {
        LayoutComponent component = interval.getComponent();
        String compId = component.getId();
        if (boundsMap.containsKey(compId)) {
            return boundsMap.get(compId);
        } else {
            return interval.getCurrentSpace();
        }
    }

    static int getVisualPosition(LayoutInterval interval, int dimension, int alignment) {
        if (interval.isEmptySpace()) {
            assert alignment == LEADING || alignment == TRAILING;
            LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
            if (neighbor != null) {
                interval = neighbor;
                alignment ^= 1;
            }
            else interval = LayoutInterval.getFirstParent(interval, PARALLEL);
        }
        return interval.getCurrentSpace().positions[dimension][alignment];
    }

    static int determineDimension(LayoutInterval interval) {
        Iterator<LayoutInterval> it = getComponentIterator(interval);
        if (it.hasNext()) {
            LayoutInterval comp = it.next();
            return comp == comp.getComponent().getLayoutInterval(HORIZONTAL)
                    ? HORIZONTAL : VERTICAL;
        }
        return -1;
    }

    /**
     * Lists components under given interval that lie at given side of the
     * interval.
     * edge.in the <code>root</code>
     * layout interval - the list contains only components whose layout
     * intervals lie at the specified edge (<code>LEADING</code>
     * or <code>TRAILING</code>) of the <code>root</code> layout interval.
     *
     * @param root layout interval that will be scanned.
     * @param edge the requested edge the components shoul be next to.
     * @return <code>List</code> of <code>LayoutInterval</code>s that
     * represent <code>LayoutComponent</code>s.
     */
    static List<LayoutInterval> edgeSubComponents(LayoutInterval root, int edge, boolean aligned) {
        assert edge == LEADING || edge == TRAILING;
        List<LayoutInterval> components = null;
        List<LayoutInterval> candidates = new LinkedList<LayoutInterval>();
        if (root != null) {
            components = new LinkedList<LayoutInterval>();
            candidates.add(root);
        }
        while (!candidates.isEmpty()) {
            LayoutInterval candidate = candidates.get(0);
            candidates.remove(candidate);
            if (candidate.isGroup()) {
                if (candidate.isSequential()) {
                    int index = (edge == LEADING) ? 0 : candidate.getSubIntervalCount()-1;
                    candidates.add(candidate.getSubInterval(index));
                } else {
                    Iterator<LayoutInterval> subs = candidate.getSubIntervals();
                    while (subs.hasNext()) {
                        LayoutInterval li = subs.next();
                        if (!aligned || LayoutInterval.isAlignedAtBorder(li, edge)) {
                            candidates.add(li);
                        }
                    }
                }
            } else if (candidate.isComponent()) {
                components.add(candidate);
            }
        }
        return components;
    }

    static boolean alignedIntervals(LayoutInterval interval1, LayoutInterval interval2, int alignment) {
        LayoutInterval parent = LayoutInterval.getCommonParent(interval1, interval2);
        return parent != null && parent.isParallel()
               && LayoutInterval.isAlignedAtBorder(interval1, parent, alignment)
               && LayoutInterval.isAlignedAtBorder(interval2, parent, alignment);
    }

    /**
     * Computes whether a space overlaps with content of given interval.
     * The difference from LayoutRegion.overlap(...) is that this method goes
     * recursivelly down to components in case interval is a group - does not
     * use the union space for whole group (which might be inaccurate).
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

    /**
     * Finds out whether components under one interval overlap with components
     * under another interval (in given dimension).
     */
    static boolean contentOverlap(LayoutInterval interval1, LayoutInterval interval2, int dimension) {
        return contentOverlap(interval1, interval2, 0, interval2.getSubIntervalCount()-1, dimension);
    }

    /**
     * @param fromIndex initial index of sub-interval in interval2
     * @param toIndex last index to consider under interval2
     */
    static boolean contentOverlap(LayoutInterval interval1, LayoutInterval interval2,
                                  int fromIndex, int toIndex, int dimension)
    {
        if (!interval2.isGroup()) {
            if (!interval1.isGroup()) {
                return LayoutRegion.overlap(interval1.getCurrentSpace(),
                                            interval2.getCurrentSpace(), dimension, 0);
            }
            LayoutInterval temp = interval1;
            interval1 = interval2;
            interval2 = temp;
        }

        // [more efficient algorithm based on region merging and ordering could be found...]
        List<LayoutInterval> int2list = null;
        List<LayoutInterval> addList = null;
        Iterator it1 = getComponentIterator(interval1);
        while (it1.hasNext()) {
            LayoutRegion space1 = ((LayoutInterval)it1.next()).getCurrentSpace();
            Iterator it2 = int2list != null ?
                           int2list.iterator() :
                           getComponentIterator(interval2, fromIndex, toIndex);
            if (int2list == null && it1.hasNext()) {
                int2list = new LinkedList<LayoutInterval>();
                addList = int2list;
            }
            while (it2.hasNext()) {
                LayoutInterval li2 = (LayoutInterval) it2.next();
                if (LayoutRegion.overlap(space1, li2.getCurrentSpace(), dimension, 0))
                    return true;
                if (addList != null)
                    addList.add(li2);
            }
            addList = null;
        }
        return false;
    }

    /**
     * Finds out whether given space overlaps with some component under given
     * interval. Unlike other contentOverlap methods that are for one dimension
     * only, here we look for a full overlap. It can be that a space overlaps
     * some components (intersects in coordinates) in each dimension, but none
     * in both dimensions together.
     */
    static boolean contentOverlap(LayoutRegion space, LayoutInterval interval) {
        for (Iterator<LayoutInterval> it=getComponentIterator(interval); it.hasNext(); ) {
            LayoutRegion compSpace = it.next().getCurrentSpace();
            if (LayoutRegion.overlap(space, compSpace, HORIZONTAL, 0)
                    && LayoutRegion.overlap(space, compSpace, VERTICAL, 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the layout structure of the orthogonal dimension whether
     * an overlap of a component interval with another interval (or its
     * subintervals) is prevented - i.e. if in the orthogonal dimension the
     * intervals of the given components are placed sequentially.
     */
    static boolean isOverlapPreventedInOtherDimension(LayoutInterval compInterval,
                                                      LayoutInterval interval,
                                                      int dimension)
    {
        int otherDim = dimension^1;
        compInterval = (LayoutInterval) getComponentIterator(compInterval).next();
        LayoutComponent component = compInterval.getComponent();
        LayoutInterval otherCompInterval = component.getLayoutInterval(otherDim);
        Iterator it = getComponentIterator(interval);
        assert it.hasNext();
        do {
            LayoutComponent comp = ((LayoutInterval)it.next()).getComponent();
            LayoutInterval otherInterval = comp.getLayoutInterval(otherDim);
            LayoutInterval parent = LayoutInterval.getCommonParent(otherCompInterval, otherInterval);
            if (parent == null || parent.isParallel())
                return false;
        }
        while (it.hasNext());
        return true;
    }

    static Iterator<LayoutInterval> getComponentIterator(LayoutInterval interval) {
        return new ComponentIterator(interval, 0, interval.getSubIntervalCount()-1);
    }

    static Iterator<LayoutInterval> getComponentIterator(LayoutInterval interval, int startIndex, int endIndex) {
        return new ComponentIterator(interval, startIndex, endIndex);
    }

    private static class ComponentIterator implements Iterator {
        private LayoutInterval root;
        private int startIndex, endIndex;
        private boolean initialized;
        private int index;
        private LayoutInterval next;

        ComponentIterator(LayoutInterval interval, int startIndex, int endIndex) {
            root = interval;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            findNext();
            initialized = true;
        }

        private void findNext() {
            LayoutInterval parent;
            int idx;
            if (next == null) {
                if (initialized)
                    return;
                if (!root.isGroup()) {
                    if (root.isComponent()) {
                        next = root;
                    }
                    return;
                }
                parent = root; // let's start from root
                idx = startIndex;
            }
            else if (next != root) { // somewhere in the structure
                parent = next.getParent();
                idx = index + 1;
            }
            else { // root is component, already used
                next = null;
                return;
            }

            next = null;
            do {
                while (idx < parent.getSubIntervalCount()) {
                    if (parent == root && idx > endIndex)
                        return; // out of the root set
                    LayoutInterval sub = parent.getSubInterval(idx);
                    if (sub.isComponent()) { // that's it
                        next = sub;
                        index = idx;
                        return;
                    }
                    if (sub.isGroup()) { // go down
                        parent = sub;
                        idx = 0;
                    }
                    else idx++;
                }
                if (parent != root) { // go up
                    idx = parent.getParent().indexOf(parent) + 1;
                    parent = parent.getParent();
                }
                else break; // all scanned
            }
            while (true);
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Object next() {
            if (next == null)
                throw new NoSuchElementException();

            Object ret = next;
            findNext();
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
