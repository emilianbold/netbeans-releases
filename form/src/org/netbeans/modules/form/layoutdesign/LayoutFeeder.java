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
 * This class is responsible for adding layout intervals to model based on
 * mouse actions done by the user (input provided from LayoutDragger). When an
 * instance is created, it analyzes the original positions - before the adding
 * operation is performed (this is needed in case of resizing). Then 'add'
 * method is called to add the intervals on desired place. It is responsibility
 * of the caller to remove the intervals/components from original locations
 * before calling 'add'.
 * Note this class does not add LayoutComponent instances to model.
 *
 * @author Tomas Pavek
 */

class LayoutFeeder implements LayoutConstants {

    private LayoutModel layoutModel;
    private LayoutOperations operations;

    private LayoutDragger dragger;
    private IncludeDesc[] originalPositions1 = new IncludeDesc[DIM_COUNT];
    private IncludeDesc[] originalPositions2 = new IncludeDesc[DIM_COUNT];
    private boolean[] originalLPositionsFixed = new boolean[DIM_COUNT];
    private boolean[] originalTPositionsFixed = new boolean[DIM_COUNT];
    private LayoutDragger.PositionDef[] newPositions = new LayoutDragger.PositionDef[DIM_COUNT];
    private LayoutInterval[] addingIntervals; // horizontal, vertical

    // working context (actual dimension)
    private int dimension;
    private LayoutInterval addingInterval;
    private LayoutInterval toAdd;
    private LayoutRegion addingSpace;
    private boolean solveOverlap;
    private boolean originalLPosFixed;
    private boolean originalTPosFixed;

    // params used when searching for the right place (inclusion)
    private int aEdge;
    private LayoutInterval aSnappedParallel;
    private LayoutInterval aSnappedNextTo;

    private static class IncludeDesc {
        LayoutInterval parent;
        int index = -1; // if adding next to
        boolean newSubGroup; // can be true if parent is sequential (parallel subgroup for part of the sequence is to be created)
        LayoutInterval neighbor; // if included in a sequence with single interval (which is not in sequence yet)
        LayoutInterval snappedParallel; // not null if aligning in parallel
        LayoutInterval snappedNextTo; // not null if snapped next to (can but need not be 'neighbor')
        int alignment; // the edge this object defines (leading or trailing or default)
        boolean fixedPosition; // whether distance from the neighbor is definitely fixed
        int distance = Integer.MAX_VALUE;
        int ortDistance = Integer.MAX_VALUE;
    }

    private static boolean sameInclusionGroup(IncludeDesc iDesc1, IncludeDesc iDesc2) {
        LayoutInterval parent1 = iDesc1.parent.isSequential() && !iDesc1.newSubGroup ?
                                 iDesc1.parent.getParent() : iDesc1.parent;
        LayoutInterval parent2 = iDesc2.parent.isSequential() && !iDesc2.newSubGroup ?
                                 iDesc2.parent.getParent() : iDesc2.parent;
        return parent1 == parent2;
    }

    // -----

    LayoutFeeder(LayoutOperations operations, LayoutDragger dragger, LayoutInterval[] addingIntervals) {
        this.layoutModel = operations.getModel();
        this.operations = operations;
        this.dragger = dragger;
        this.addingIntervals = addingIntervals;

        LayoutDragger.PositionDef[] positions = dragger.getPositions();
        for (int dim=0; dim < DIM_COUNT; dim++) {
            if (dragger.isResizing()) {
                if (dragger.isResizing(dim)) {
                    originalPositions1[dim] = findOutCurrentPosition(
                            addingIntervals[dim], dim, dragger.getResizingEdge(dim)^1);
                    newPositions[dim] = dragger.getPositions()[dim];
                }
                else { // this dimension has not been resized
                    int alignment = DEFAULT;
                    originalPositions1[dim] = findOutCurrentPosition(addingIntervals[dim], dim, alignment);
                    alignment = originalPositions1[dim].alignment;
                    if (alignment == LEADING || alignment == TRAILING) {
                        originalPositions2[dim] = findOutCurrentPosition(addingIntervals[dim], dim, alignment^1);
                    }
                }
                originalLPositionsFixed[dim] = isFixedRelativePosition(addingIntervals[dim], LEADING);
                originalTPositionsFixed[dim] = isFixedRelativePosition(addingIntervals[dim], TRAILING);
            }
            else newPositions[dim] = dragger.getPositions()[dim];
        }
    }

    void add() {
        int overlapDim = getDimensionSolvingOverlap(newPositions);

        for (int dim=0; dim < DIM_COUNT; dim++) {
            dimension = dim;
            addingInterval = toAdd = addingIntervals[dim];
            addingSpace = dragger.getMovingSpace();
            addingInterval.setCurrentSpace(addingSpace);
            solveOverlap = overlapDim == dim;
            IncludeDesc originalPos1 = originalPositions1[dim];
            IncludeDesc originalPos2 = originalPositions2[dim];

            if (dragger.isResizing()) {
                originalLPosFixed = originalLPositionsFixed[dim];
                originalTPosFixed = originalTPositionsFixed[dim];
                if (dragger.isResizing(dim))
                    checkResizing();
            }

//            if (dragger.isResizing(dim)) {
//                checkResizing();
//            }
//            else {
//                for (int i=0; i<components.length; i++) {
//                    LayoutInterval compInt = components[i].getLayoutInterval(dim);
//                    if (compInt.getPreferredSize() != NOT_EXPLICITLY_DEFINED) {
//                        resizeInterval(compInt, movingBounds[i].size(dim));
//                    }
//                }
//            }

            LayoutDragger.PositionDef newPos = newPositions[dim];
            if (newPos != null && (newPos.alignment == CENTER || newPos.alignment == BASELINE)) {
                // hack: simplified adding to a closed group
                aEdge = newPos.alignment;
                aSnappedParallel = newPos.interval;
                addSimplyAligned();
                continue;
            }
            if (dragger.isResizing() && (originalPos1.alignment == CENTER || originalPos1.alignment == BASELINE)) {
                aEdge = originalPos1.alignment;
                aSnappedParallel = originalPos1.snappedParallel;
                addSimplyAligned();
                continue;
            }

            // prepare task for searching the position
            List inclusions = new LinkedList();
            IncludeDesc inclusion1 = null;
            IncludeDesc inclusion2 = null;
            LayoutInterval root = dragger.getTargetContainer().getLayoutRoot(dim);

            if (dragger.isResizing(dim^1)) { // need to renew the original position
                aEdge = originalPos1.alignment;
                aSnappedParallel = originalPos1.snappedParallel;
                aSnappedNextTo = originalPos1.snappedNextTo;
            }
            else if (newPos != null) {
                aEdge = newPos.alignment;
                aSnappedParallel = !newPos.nextTo ? newPos.interval : null;
                aSnappedNextTo = newPos.snapped && newPos.nextTo ? newPos.interval : null;
            }
            else {
                aEdge = dragger.isResizing(dim) ? dragger.getResizingEdge(dim) : DEFAULT;
                aSnappedParallel = aSnappedNextTo = null;
            }
            analyzeParallel(root, inclusions);

            boolean preserveOriginal = dragger.isResizing(dim) && !dragger.isResizing(dim^1);
            if (inclusions.size() > 1) {
                mergeParallelInclusions(inclusions, originalPos1, preserveOriginal);
                assert inclusions.size() == 1;
            }
            IncludeDesc found = (IncludeDesc) inclusions.get(0);
            inclusions.clear();
            if (preserveOriginal) { // resized in this dimension only
                inclusion1 = originalPos1;
                inclusion2 = found;
            }
            else {
                inclusion1 = found;
                if (dragger.isResizing(dim^1)
                    && (originalPos1.alignment == LEADING || originalPos1.alignment == TRAILING))
                {   // second search needed if resizing in the other dimension only or in both
                    if (originalPos2 != null) {
                        assert !dragger.isResizing(dim);
                        aEdge = originalPos2.alignment;
                        aSnappedParallel = originalPos2.snappedParallel;
                        aSnappedNextTo = originalPos2.snappedNextTo;
                    }
                    else {
                        assert dragger.isResizing(dim);
                        if (newPos != null) {
                            aEdge = newPos.alignment;
                            aSnappedParallel = !newPos.nextTo ? newPos.interval : null;
                            aSnappedNextTo = newPos.snapped && newPos.nextTo ? newPos.interval : null;
                        }
                        else {
                            aEdge = dragger.getResizingEdge(dim);
                            aSnappedParallel = aSnappedNextTo = null;
                        }
                    }
                     // second round searching
                    analyzeParallel(root, inclusions);

                    if (inclusions.size() > 1) {
                        mergeParallelInclusions(inclusions, originalPos2 != null ? originalPos2 : originalPos1, false);
                        assert inclusions.size() == 1;
                    }
                    inclusion2 = (IncludeDesc) inclusions.get(0);
                    inclusions.clear();
                }
            }

            if (!mergeSequentialInclusions(inclusion1, inclusion2))
                inclusion2 = null;

            addInterval(inclusion1, inclusion2);

//            if (pos2 == null && dragger.isResizing() && !dragger.isResizing(dim)) {
//                LayoutDragger.PositionDef pos = getCurrentPosition(adding, false, dim, DEFAULT);
//                if (pos != null) {
//                    pos1 = pos;
//                    // [maybe dragger should rather provide positions for both dimensions]
//                }
//            }
//
//            if (components.length == 1) { // PENDING
//                checkResizing(adding, pos1, pos2, dim);
//            }
        }
    }

    LayoutInterval getAddedInterval(int dim) {
        return addingIntervals[dim];
    }

    private static IncludeDesc findOutCurrentPosition(LayoutInterval interval, int dimension, int alignment) {
        LayoutInterval parent = interval.getParent();
        int nonEmptyCount = LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true);

        IncludeDesc iDesc = new IncludeDesc();

        if (parent.isSequential() && nonEmptyCount > 1) {
            if (alignment < 0)
                alignment = LEADING;
            if (nonEmptyCount == 2) { // the sequence may not survive when the interval is removed
                iDesc.parent = parent.getParent();
                boolean after = true;
                for (int i=0,n=parent.getSubIntervalCount(); i < n; i++) {
                    LayoutInterval li = parent.getSubInterval(i);
                    if (li == interval) {
                        after = false;
                    }
                    else if (!li.isEmptySpace()) {
                        iDesc.neighbor = li; // next to a single interval in parallel group
                        iDesc.index = after ? i + 1 : 0;
                        break;
                    }
                }
            }
            else { // simply goes to the sequence
                iDesc.parent = parent;
                iDesc.index = parent.indexOf(interval);
            }
        }
        else { // parallel parent
            if (parent.isSequential()) {
                parent = parent.getParent(); // alone in sequence, take parent
                if (alignment < 0)
                    alignment = LEADING;
            }
            else if (alignment < 0) {
                alignment = interval.getAlignment();
            }
            nonEmptyCount = LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true);
            if (nonEmptyCount <= 2 && parent.getParent() != null) {
                // parallel group will not survive when the interval is removed
                parent = parent.getParent();
                if (parent.isSequential()) {
                    boolean ortOverlap = false;
                    for (Iterator it=parent.getSubIntervals(); it.hasNext(); ) {
                        LayoutInterval li = (LayoutInterval) it.next();
                        if (!li.isEmptySpace() && !li.isParentOf(interval)
                            && LayoutRegion.overlap(interval.getCurrentSpace(), li.getCurrentSpace(), dimension^1, 0))
                        {   // orthogonal overlap - need to stay within the sequence
                            ortOverlap = true;
                            break;
                        }
                    }
                    if (ortOverlap) // parallel with part of the sequence
                        iDesc.newSubGroup = true;
                    else // parallel with whole sequence
                        parent = parent.getParent();
                }
                iDesc.parent = parent;
            }
            else iDesc.parent = parent; // simply goes to the parallel group
        }

        parent = LayoutInterval.getFirstParent(interval, PARALLEL);
        boolean aligned;
        if (alignment == LEADING || alignment == TRAILING) {
            aligned = LayoutInterval.isAlignedAtBorder(interval, parent, alignment);
            iDesc.fixedPosition = isFixedRelativePosition(interval, alignment);
        }
        else aligned = (alignment == interval.getAlignment());

        if (aligned) { // check for parallel aligning
            nonEmptyCount = LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true);
            if (nonEmptyCount > 2 || parent.getParent() == null) { // aligned to the whole group
                iDesc.snappedParallel = parent;
            }
            else { // aligned with the other subinterval only
                for (Iterator it=parent.getSubIntervals(); it.hasNext(); ) {
                    LayoutInterval sub = (LayoutInterval) it.next();
                    if (!sub.isEmptySpace() && sub != interval && !sub.isParentOf(interval)) {
                        if (alignment == LEADING || alignment == TRAILING) {
                            LayoutInterval li = LayoutUtils.getOutermostComponent(sub, dimension, alignment);
                            if (LayoutInterval.isAlignedAtBorder(li, parent, alignment)
                                || LayoutInterval.isPlacedAtBorder(li, parent, dimension, alignment))
                            {   // this is it
                                iDesc.snappedParallel = li;
                            }
                        }
                        else iDesc.snappedParallel = sub;
                        break;
                    }
                }
            }
        }
        else if (alignment == LEADING || alignment == TRAILING) {
            LayoutInterval li = LayoutInterval.getNeighbor(interval, alignment, false, true, true);
            if (li != null && LayoutInterval.isFixedDefaultPadding(li)) {
                li = LayoutInterval.getDirectNeighbor(interval, alignment, true);
                iDesc.snappedNextTo = li != null ? li : LayoutInterval.getRoot(interval);
            }
        }

        iDesc.alignment = alignment;
        return iDesc;
    }

    private static boolean isFixedRelativePosition(LayoutInterval interval, int edge) {
        assert edge == LEADING || edge == TRAILING;
        LayoutInterval parent = interval.getParent();
        if (parent == null)
            return true;
        if (parent.isSequential()) {
            LayoutInterval li = LayoutInterval.getDirectNeighbor(interval, edge, false);
            if (li != null)
                return !LayoutInterval.wantResize(li);
            else {
                interval = parent;
                parent = interval.getParent();
            }
        }
        if (!LayoutInterval.isAlignedAtBorder(interval, parent, edge)
                && LayoutInterval.contentWantResize(parent))
            return false;

        return isFixedRelativePosition(parent, edge);
    }

    // -----
    // overlap analysis

    private int getDimensionSolvingOverlap(LayoutDragger.PositionDef[] positions) {
        if ((dragger.isResizing(VERTICAL) && !dragger.isResizing(HORIZONTAL))
            || (positions[HORIZONTAL] != null && positions[HORIZONTAL].snapped && (positions[VERTICAL] == null || !positions[VERTICAL].snapped))
            || (positions[VERTICAL] != null && !positions[VERTICAL].nextTo && positions[VERTICAL].snapped
                && (positions[VERTICAL].interval.getParent() == null)
                && !existsComponentPlacedAtBorder(positions[VERTICAL].interval, VERTICAL, positions[VERTICAL].alignment))) {
            return VERTICAL;
        }
        if (positions[VERTICAL] != null && positions[VERTICAL].nextTo && positions[VERTICAL].snapped
            && (positions[VERTICAL].interval.getParent() == null)) {
            int alignment = positions[VERTICAL].alignment;
            int[][] overlapSides = overlappingGapSides(dragger.getTargetContainer().getLayoutRoot(HORIZONTAL),
                                                       dragger.getMovingSpace());
            if (((alignment == LEADING) || (alignment == TRAILING))
                && (overlapSides[VERTICAL][1-alignment] != 0)
                && (overlapSides[VERTICAL][alignment] == 0)) {
                return VERTICAL;
            }
        }
        if ((positions[HORIZONTAL] == null || !positions[HORIZONTAL].snapped)
            && (positions[VERTICAL] == null || !positions[VERTICAL].snapped)) {
            boolean[] overlapDim = overlappingGapDimensions(dragger.getTargetContainer().getLayoutRoot(HORIZONTAL),
                                                            dragger.getMovingSpace());
            if (overlapDim[VERTICAL] && !overlapDim[HORIZONTAL]) {
                return VERTICAL;
            }
        }
        return HORIZONTAL;
    }
    
    /**
     * Checks whether there is a component placed at the border
     * of the specified interval.
     *
     * @param interval interval to check.
     * @param dimension dimension that should be considered.
     * @param alignment alignment that should be considered.
     */
    private static boolean existsComponentPlacedAtBorder(LayoutInterval interval, int dimension, int alignment) {
        Iterator iter = interval.getSubIntervals();
        while (iter.hasNext()) {
            LayoutInterval subInterval = (LayoutInterval)iter.next();
            if (LayoutInterval.isPlacedAtBorder(interval, dimension, alignment)) {
                if (subInterval.isComponent()) {
                    return true;
                } else if (subInterval.isGroup()) {
                    if (existsComponentPlacedAtBorder(subInterval, dimension, alignment)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Fills the given list by components that overlap with the <code>region</code>.
     * 
     * @param overlaps list that should be filled by overlapping components.
     * @param group layout group that is scanned by this method.
     * @param region region to check.
     */
    private static void fillOverlappingComponents(List overlaps, LayoutInterval group, LayoutRegion region) {
        Iterator iter = group.getSubIntervals();
        while (iter.hasNext()) {
            LayoutInterval subInterval = (LayoutInterval)iter.next();
            if (subInterval.isGroup()) {
                fillOverlappingComponents(overlaps, subInterval, region);
            } else if (subInterval.isComponent()) {                
                LayoutComponent component = subInterval.getComponent();
                LayoutRegion compRegion = subInterval.getCurrentSpace();
                if (LayoutRegion.overlap(compRegion, region, HORIZONTAL, 0)
                    && LayoutRegion.overlap(compRegion, region, VERTICAL, 0)) {
                    overlaps.add(component);
                }
            }
        }
    }

    // Helper method for getDimensionSolvingOverlap() method
    private static boolean[] overlappingGapDimensions(LayoutInterval layoutRoot, LayoutRegion region) {
        boolean[] result = new boolean[2];
        int[][] overlapSides = overlappingGapSides(layoutRoot, region);
        for (int i=0; i<DIM_COUNT; i++) {
            result[i] = (overlapSides[i][0] == 1) && (overlapSides[i][1] == 1);
        }
        return result;        
    }

    // Helper method for getDimensionSolvingOverlap() method
    private static int[][] overlappingGapSides(LayoutInterval layoutRoot, LayoutRegion region) {
        int[][] overlapSides = new int[][] {{0,0},{0,0}};
        List overlaps = new LinkedList();
//        LayoutInterval layoutRoot = LayoutInterval.getRoot(positions[HORIZONTAL].interval);
        fillOverlappingComponents(overlaps, layoutRoot, region);
        Iterator iter = overlaps.iterator();
        while (iter.hasNext()) {
            LayoutComponent component = (LayoutComponent)iter.next();
            LayoutRegion compRegion = component.getLayoutInterval(HORIZONTAL).getCurrentSpace();
            for (int i=0; i<DIM_COUNT; i++) {
                int[] edges = overlappingSides(compRegion, region, i);
                for (int j=0; j<2; j++) {
                    if (edges[j] == 1) {
                        overlapSides[i][j] = 1;
                    } else if (edges[j] == -1) {
                        if (overlapSides[i][j] == -1) {
                            overlapSides[i][j] = 1;
                        } else if (overlapSides[i][j] == 0) {
                            overlapSides[i][j] = -1;
                        }
                    }
                }
            }
        }
        return overlapSides;
    }

    // Helper method for overlappingGapSides() method
    private static int[] overlappingSides(LayoutRegion compRegion, LayoutRegion region, int dimension) {
        int[] sides = new int[2];
        int compLeading = compRegion.positions[dimension][LEADING];
        int compTrailing = compRegion.positions[dimension][TRAILING];
        int regLeading = region.positions[dimension][LEADING];
        int regTrailing = region.positions[dimension][TRAILING];
        if ((regLeading < compTrailing) && (compTrailing < regTrailing)) {
            sides[0] = 1;
        }
        if ((regLeading < compLeading) && (compLeading < regTrailing)) {
            sides[1] = 1;
        }
        if ((sides[0] == 1) &&  (sides[1] == 1)) {
            sides[0] = sides[1] = -1;
        }
        return sides;
    }

    // -----
    // the following methods work in context of adding to actual dimension

    private void checkResizing() {
        IncludeDesc orig = originalPositions1[dimension];
        LayoutDragger.PositionDef newPos = newPositions[dimension];
        boolean resizing = (orig.snappedNextTo != null || orig.snappedParallel != null)
                            && (newPos != null && newPos.snapped);
/*        if (pos2 != null) {
            resizing = pos1.snapped && pos2.snapped;
        }
        else if (pos1.snapped) {
            LayoutInterval parent = interval.getParent();
            if (parent == null || parent.isParallel()) {
                resizing = false;
                // cannot decide this as we can't be sure about the actual visual position of the group
                // [unless LayoutDragger provides two positions also for moving,
                //  the resizing flag is always reset when the interval is moved]
            }
            else { // in sequence
                int align = pos1.alignment ^ 1;
                LayoutInterval gap = LayoutInterval.getNeighbor(interval, align, false, true, true);
                resizing = gap != null && gap.isDefaultPadding() && !LayoutInterval.canResize(gap);
                // [not catching situation when touching the root without gap]
            }
        }
        else {
            resizing = false;
        } */

        if (!resizing) {
            layoutModel.setIntervalSize(addingInterval,
                    USE_PREFERRED_SIZE, addingInterval.getPreferredSize(), USE_PREFERRED_SIZE);
        }
        else { //if (dragger.isResizing(dimension)) {
            // [maybe should act according to potential resizability of the component,
            //  not only on resizing operation - the condition should be:
            //  isComponentResizable(interval.getComponent(), dimension)  ]
            layoutModel.setIntervalSize(addingInterval,
                    NOT_EXPLICITLY_DEFINED, addingInterval.getPreferredSize(), Short.MAX_VALUE);
        }
    }

    /**
     * Adds aligned with an interval to existing group, or creates new.
     * (Now used only to a limited extent for closed groups only.)
     */
    private void addSimplyAligned() {
        int alignment = aEdge;
        assert alignment == CENTER || alignment == BASELINE;
        layoutModel.setIntervalAlignment(toAdd, alignment);

        if (aSnappedParallel.isParallel() && aSnappedParallel.getGroupAlignment() == alignment) {
            layoutModel.addInterval(toAdd, aSnappedParallel, -1);
            return;
        }
        LayoutInterval parent = aSnappedParallel.getParent();
        if (parent.isParallel() && parent.getGroupAlignment() == alignment) {
            layoutModel.addInterval(toAdd, parent, -1);
            return;
        }

        int alignIndex = layoutModel.removeInterval(aSnappedParallel);
        LayoutInterval subGroup = new LayoutInterval(PARALLEL);
        subGroup.setGroupAlignment(alignment);
        if (parent.isParallel()) {
            subGroup.setAlignment(aSnappedParallel.getAlignment());
        }
        layoutModel.setIntervalAlignment(aSnappedParallel, alignment);
        layoutModel.addInterval(aSnappedParallel, subGroup, -1);
        layoutModel.addInterval(toAdd, subGroup, -1);
        layoutModel.addInterval(subGroup, parent, alignIndex);
    }

    void addInterval(IncludeDesc iDesc1, IncludeDesc iDesc2) {
        addToGroup(iDesc1, iDesc2);

        // align in parallel if required
        boolean alignedInParallel = false;
        if (iDesc2 != null && iDesc2.snappedParallel != null) {
            alignInParallel(iDesc2.snappedParallel, iDesc2.alignment);
            alignedInParallel = true;
        }
        if (iDesc1.snappedParallel != null) {
            alignInParallel(iDesc1.snappedParallel, iDesc1.alignment);
            alignedInParallel = true;
        }

        // post processing
        LayoutInterval parent = addingInterval.getParent();
        int accAlign = DEFAULT;
        if (parent.isSequential()) {
            if (LayoutInterval.getDirectNeighbor(addingInterval, LEADING, false) == null)
                accAlign = LEADING;
            else if (LayoutInterval.getDirectNeighbor(addingInterval, TRAILING, false) == null)
                accAlign = TRAILING;
        }
        else {
            accAlign = addingInterval.getAlignment() ^ 1;
        }
        if (accAlign != DEFAULT) {
            accommodateOutPosition(addingInterval, accAlign); // adapt size of parent/neighbor
        }

        if (parent.isSequential()) {// && !alignedInParallel)
            int nonEmptyCount = LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true);
            if (nonEmptyCount == 1) { // this is a newly created sequence
                operations.optimizeGaps(parent.getParent(), dimension);
                parent = addingInterval.getParent(); // the parent might have changed
                if (parent.isParallel()) { // the sequence was eliminated
                    operations.mergeParallelGroups(parent); // the added interval might go to parallel subgroup
                }
            }
            else if (dimension == HORIZONTAL) {
                // check whether the added interval could not be rather placed
                // in a neighbor parallel group
                operations.moveInsideSequential(parent, dimension);
            }
        }
        else operations.optimizeGaps(parent, dimension); // also removes supporting gap in container
    }

    private void addToGroup(IncludeDesc iDesc1, IncludeDesc iDesc2) {
        assert iDesc2 == null || (iDesc1.parent == iDesc2.parent
                && iDesc1.newSubGroup == iDesc2.newSubGroup && iDesc1.neighbor == iDesc2.neighbor
                && iDesc1.index == iDesc2.index);

        LayoutInterval parent = iDesc1.parent;
        LayoutInterval seq;
        int index;
        if (parent.isSequential()) {
            if (iDesc1.newSubGroup) {
                seq = new LayoutInterval(SEQUENTIAL);
                LayoutRegion space = addingInterval.getCurrentSpace();
                if (dimension == VERTICAL) { // count in a margin in vertical direction
                    // [because analyzeAdding uses it - maybe we should get rid of it completely]
                    space = new LayoutRegion(space);
                    space.reshape(VERTICAL, LEADING, -4);
                    space.reshape(VERTICAL, TRAILING, 4);
                }
                LayoutInterval subgroup = extractParallelSequence(
                        parent, space, false, iDesc1.alignment); // dimension == VERTICAL
                parent = subgroup;
                index = 0;
            }
            else {
                seq = parent;
                parent = seq.getParent();
                index = iDesc1.index;
            }
        }
        else { // parallel parent
            LayoutInterval neighbor = iDesc1.neighbor;
            if (neighbor != null) {
                if (neighbor.getParent().isSequential()) {
                    assert neighbor.getParent().getParent() == parent;
                    seq = neighbor.getParent();
                }
                else {
                    assert neighbor.getParent() == parent;
                    seq = new LayoutInterval(SEQUENTIAL);
                    layoutModel.addInterval(seq, parent, layoutModel.removeInterval(neighbor));
                    seq.setAlignment(neighbor.getAlignment());
                    layoutModel.setIntervalAlignment(neighbor, DEFAULT);
                    layoutModel.addInterval(neighbor, seq, 0);
                }
                index = iDesc1.index;
            }
            else {
                seq = new LayoutInterval(SEQUENTIAL);
                seq.setAlignment(iDesc1.alignment);
                index = 0;
            }
        }

        assert iDesc1.alignment >= 0 || iDesc2 == null;
        assert iDesc2 == null || iDesc2.alignment == (iDesc1.alignment^1);

        LayoutInterval[] neighbors = new LayoutInterval[2]; // LEADING, TRAILING
        LayoutInterval[] gaps = new LayoutInterval[2]; // LEADING, TRAILING
        LayoutInterval originalGap = null;
        int[] centerDst = new int[2]; // LEADING, TRAILING

        // find the neighbors for the adding interval
        int count = seq.getSubIntervalCount();
        if (index > count)
            index = count;
        for (int i = LEADING; i <= TRAILING; i++) {
            int idx1 = i == LEADING ? index - 1 : index;
            int idx2 = i == LEADING ? index - 2 : index + 1;
            if (idx1 >= 0 && idx1 < count) {
                LayoutInterval li = seq.getSubInterval(idx1);
                if (li.isEmptySpace()) {
                    originalGap = li;
                    if (idx2 >= 0 && idx2 < count)
                        neighbors[i] = seq.getSubInterval(idx2);
                }
                else neighbors[i] = li;
            }
            if (iDesc1.alignment < 0) { // no alignment known
                centerDst[i] = neighbors[i] != null ?
                    LayoutRegion.distance(neighbors[i].getCurrentSpace(), addingSpace, dimension, i^1, CENTER) :
                    LayoutRegion.distance(parent.getCurrentSpace(), addingSpace, dimension, i, CENTER);
                if (i == TRAILING)
                    centerDst[i] *= -1;
            }
        }

        // compute leading and trailing gaps
        for (int i = LEADING; i <= TRAILING; i++) {
            IncludeDesc iiDesc = iDesc1.alignment < 0 || iDesc1.alignment == i ? iDesc1 : iDesc2;
            if (neighbors[i] == null && iiDesc != null && iiDesc.snappedParallel != null
                && (iiDesc.snappedParallel.getParent() != seq || originalGap == null))
            {   // starting/ending edge aligned in parallel - does not need a gap
                continue;
            }

            boolean aligned;
            if (iDesc1.alignment < 0) { // no specific alignment - decide based on distance
                aligned = centerDst[i] < centerDst[i^1]
                          || (centerDst[i] == centerDst[i^1] && i == LEADING);
            }
            else if (iDesc2 != null) { // both positions defined
                aligned = iiDesc.fixedPosition
                          || (i == LEADING && originalLPosFixed)
                          || (i == TRAILING && originalTPosFixed);
            }
            else { // single position only (either next to or parallel)
                if (iDesc1.snappedParallel == null || iDesc1.snappedParallel.getParent() != seq)
                    aligned = i == iDesc1.alignment;
                else // special case - aligning with interval in the same sequence - to subst. its position
                    aligned = i == (iDesc1.alignment^1);
            }

            boolean minorGap = false;
            if (!aligned && neighbors[i] == null && originalGap == null) {
                LayoutInterval outerNeighbor = LayoutInterval.getNeighbor(parent, i, false, true, false);
                if (outerNeighbor != null && outerNeighbor.isEmptySpace())
                    continue; // unaligned ending gap not needed
                else // minor gap if the other edge is going to align in parallel
                    minorGap = iiDesc == null && iDesc1.snappedParallel != null  && parent.isParentOf(iDesc1.snappedParallel);
            }

            boolean fixedGap = aligned;

            if (!fixedGap
                && (minorGap
                    || LayoutInterval.wantResize(addingInterval)
                    || (originalGap != null && !LayoutInterval.canResize(originalGap))
                    || (originalGap == null
                        && (LayoutInterval.wantResize(seq)
                            || (neighbors[i] == null && !LayoutInterval.contentWantResize(parent))))))
            {   // can't introduce resizing gap
                fixedGap = true;
            }

            LayoutInterval gap = new LayoutInterval(SINGLE);
            if (iiDesc == null || iiDesc.snappedNextTo == null) {//&& !minorGap
                // the gap possibly needs an explicit size
                int distance = neighbors[i] != null ?
                    LayoutRegion.distance(neighbors[i].getCurrentSpace(), addingSpace, dimension, i^1, i) :
                    LayoutRegion.distance(parent.getCurrentSpace(), addingSpace, dimension, i, i);
                if (i == TRAILING)
                    distance *= -1;

                if (distance > 0) {
                    int pad = determineExpectingPadding(addingInterval, neighbors[i], seq, i);
                    if (distance > pad || (fixedGap && distance != pad)) {
                        gap.setPreferredSize(distance);
                        if (fixedGap) {
                            gap.setMinimumSize(USE_PREFERRED_SIZE);
                            gap.setMaximumSize(USE_PREFERRED_SIZE);
                        }
                    }
                }
            }
            if (!fixedGap) {
                gap.setMaximumSize(Short.MAX_VALUE);
            }
            gaps[i] = gap;
        }

        if (seq.getParent() == null) { // newly created sequence
            assert seq.getSubIntervalCount() == 0;
            if (gaps[LEADING] == null && gaps[TRAILING] == null) { // after all, the sequence is not needed
                layoutModel.setIntervalAlignment(addingInterval, seq.getAlignment());
                layoutModel.addInterval(toAdd, parent, -1);
                return;
            }
            else layoutModel.addInterval(seq, parent, -1);
        }

        // finally add the surrounding gaps and the interval
        if (originalGap != null) {
            index = layoutModel.removeInterval(originalGap);
        }
        else if (neighbors[TRAILING] != null) {
            index = seq.indexOf(neighbors[TRAILING]);
        }
        else if (neighbors[LEADING] != null) {
            index = seq.getSubIntervalCount();
        }
        else index = 0;

        if (gaps[LEADING] != null) {
            layoutModel.addInterval(gaps[LEADING], seq, index++);
        }
        layoutModel.setIntervalAlignment(addingInterval, DEFAULT);
        layoutModel.addInterval(toAdd, seq, index++);
        if (gaps[TRAILING] != null) {
            layoutModel.addInterval(gaps[TRAILING], seq, index);
        }
    }

    private LayoutInterval extractParallelSequence(LayoutInterval seq,
                                                   LayoutRegion space,
                                                   boolean close,
                                                   int alignment)
    {
        int count = seq.getSubIntervalCount();
        int startIndex = 0;
        int endIndex = count - 1;
        int startPos = seq.getCurrentSpace().positions[dimension][LEADING];
        int endPos = seq.getCurrentSpace().positions[dimension][TRAILING];
        int point = alignment < 0 ? CENTER : alignment;

        for (int i=0; i < count; i++) {
            LayoutInterval li = seq.getSubInterval(i);
            if (li.isEmptySpace())
                continue;

            LayoutRegion subSpace = li.getCurrentSpace();

            if (contentOverlap(space, li, dimension^1)) { // orthogonal overlap
                // this interval makes a hard boundary
                if (getAddDirection(space, subSpace, dimension, point) == LEADING) {
                    // given interval is positioned before this subinterval (trailing boundary)
                    endIndex = i - 1;
                    endPos = subSpace.positions[dimension][LEADING];
                    break;
                }
                else { // given interval points behind this one (leading boundary)
                    startIndex = i + 1;
                    startPos = subSpace.positions[dimension][TRAILING];
                }
            }
            else if (close) { // go for smallest parallel part possible
                int[] detPos = space.positions[dimension];
                int[] subPos = subSpace.positions[dimension];
                if (detPos[LEADING] >= subPos[TRAILING]) {
                    startIndex = i + 1;
                    startPos = subPos[TRAILING];
                }
                else if (detPos[LEADING] >= subPos[LEADING]) {
                    startIndex = i;
                    startPos = subPos[LEADING];
                }
                else if (detPos[TRAILING] <= subPos[TRAILING]) {
                    if (detPos[TRAILING] > subPos[LEADING]) {
                        endIndex = i;
                        endPos = subPos[TRAILING];
                        break;
                    }
                    else { // detPos[TRAILING] <= subPos[LEADING]
                        endIndex = i - 1;
                        endPos = subPos[LEADING];
                        break;
                    }
                }
            }
        }

        if (startIndex > endIndex) {
            return null; // no part of the sequence can be parallel to the given space
        }
        if (startIndex == 0 && endIndex == count-1) { // whole sequence is parallel
            return seq.getParent();
        }

        LayoutInterval group = new LayoutInterval(PARALLEL);
//        int effAlign1 = getEffectiveAlignment(seq.getSubInterval(startIndex));
//        int effAlign2 = getEffectiveAlignment(seq.getSubInterval(endIndex));
//        int groupAlign = (effAlign1 == effAlign2 || effAlign2 < 0) ? effAlign1 : effAlign2;
        if (alignment != DEFAULT) {
            group.setGroupAlignment(/*groupAlign == LEADING || groupAlign == TRAILING ?
                                groupAlign :*/ alignment);
        }
        if (startIndex == endIndex) {
            LayoutInterval li = layoutModel.removeInterval(seq, startIndex);
            layoutModel.addInterval(li, group, 0);
        }
        else {
            LayoutInterval interSeq = new LayoutInterval(SEQUENTIAL);
            group.add(interSeq, 0);
            int i = startIndex;
            while (i <= endIndex) {
                LayoutInterval li = layoutModel.removeInterval(seq, i);
                endIndex--;
                layoutModel.addInterval(li, interSeq, -1);
            }
        }
        layoutModel.addInterval(group, seq, startIndex);

        group.getCurrentSpace().set(dimension, startPos, endPos);

        return group;
    }

    /**
     * When an interval is added or resized out of current boundaries of its
     * parent, this method tries to accommodate the size increment in the parent
     * (and its parents). It acts according to the current visual position of
     * the interval - how it exceeds the current parent border. In the simplest
     * form the method tries to shorten the nearest gap in the parent sequence.
     */
    private void accommodateOutPosition(LayoutInterval interval, /*int dimension,*/ int alignment/*, boolean snapped*/) {
        if (alignment == CENTER || alignment == BASELINE) {
            return; // [but should consider these too...]
        }

        int pos = interval.getCurrentSpace().positions[dimension][alignment];
        assert pos != LayoutRegion.UNKNOWN;
        int sizeIncrement = Integer.MIN_VALUE;
        int[] groupPos = null;
        LayoutInterval parent = interval.getParent();

        do {
            if (parent.isSequential()) {
                if (sizeIncrement > 0) {
                    int accommodated = accommodateSizeInSequence(
                            interval, sizeIncrement, /*dimension,*/ alignment/*, snapped*/);
                    sizeIncrement -= accommodated;
                    if (groupPos != null) {
                        if (alignment == LEADING)
                            accommodated = -accommodated;
                        groupPos[alignment] += accommodated;
                    }
                }
                if (parent.getSubInterval(alignment == LEADING ? 0 : parent.getSubIntervalCount()-1) != interval) {
                    return; // not a border interval in the sequence, can't go up
                }
            }
            else {
                groupPos = parent.getCurrentSpace().positions[dimension];
                if (groupPos[alignment] != LayoutRegion.UNKNOWN) {
                    sizeIncrement = alignment == LEADING ?
                        groupPos[alignment] - pos : pos - groupPos[alignment];
                }
                else groupPos = null;
            }
            interval = parent;
            parent = interval.getParent();
        }
        while ((sizeIncrement > 0 || sizeIncrement == Integer.MIN_VALUE)
               && parent != null
               && (!parent.isParallel() || interval.getAlignment() != alignment));
               // can't accommodate at the aligned side [but could probably turn to other side - update 'pos', etc]
    }

    private int accommodateSizeInSequence(LayoutInterval interval, int sizeIncrement, /*int dimension,*/ int alignment/*, boolean snapped*/) {
        LayoutInterval parent = interval.getParent();
        assert parent.isSequential();
        int increment = sizeIncrement;
        int d = alignment == LEADING ? -1 : 1;
        int i = parent.indexOf(interval) + d;
        int n = parent.getSubIntervalCount();
        while (i >= 0 && i < n) {
            LayoutInterval li = parent.getSubInterval(i);
            if (li.isEmptySpace() && li.getPreferredSize() != NOT_EXPLICITLY_DEFINED) {
                int pad = determinePadding(interval, dimension, alignment);
                int currentSize = LayoutInterval.getIntervalCurrentSize(li, dimension);

                int size = currentSize - increment;
                if (size <= pad) {
//                    if (size > 0 || !snapped || (i+d >= 0 && i+d < n)) {
                        size = NOT_EXPLICITLY_DEFINED;
                        increment -= currentSize - pad;
//                    }
//                    else { // remove gap
//                        layoutModel.removeInterval(parent, i);
//                        break;
//                    }
                }
                else increment = 0;

                operations.resizeInterval(li, size);
                if (LayoutInterval.wantResize(li) && LayoutInterval.wantResize(interval)) {
                    // cancel gap resizing if the neighbor is also resizing
                    layoutModel.setIntervalSize(li, li.getMinimumSize(), li.getPreferredSize(), USE_PREFERRED_SIZE);
                }
                break;
            }
            else {
                interval = li;
                i += d;
            }
        }
        return sizeIncrement - increment;
    }

    /**
     * This method aligns an interval (just simply added to the layout - so it
     * is already placed correctly where it should appear) in parallel with
     * another interval.
     */
    private void alignInParallel(/*LayoutInterval interval,*/ LayoutInterval toAlignWith, /*int dimension,*/ int alignment) {
        if (toAlignWith.getParent() == null)
            return; // aligning with root - nothing to do (the interval must be already aligned)

        LayoutInterval interval = addingInterval;
        boolean resizing = dragger.isResizing(dimension);

        LayoutInterval parParent = LayoutInterval.getFirstParent(interval, PARALLEL);
        while (!parParent.isParentOf(toAlignWith)) {
            if (LayoutInterval.isAlignedAtBorder(interval, parParent, alignment)) {
                interval = parParent;
                parParent = LayoutInterval.getFirstParent(interval, PARALLEL);
            }
            else parParent = null;
            if (parParent == null)
                return; // not parent of toAlignWith - can't align with interval from different branch
        }

        // hack: remove the aligning interval temporarily not to influence the follwoing analysis
        LayoutInterval tempRemoved = interval.getParent().isSequential() ? interval.getParent() : interval;
        int removedIndex = parParent.remove(tempRemoved);

        // check if we shouldn't rather align with a whole group (parent of toAlignWith)
        boolean alignWithParent = false;
        LayoutInterval alignParent;
        do {
            alignParent = LayoutInterval.getFirstParent(toAlignWith, PARALLEL);
            if (alignParent == null)
                return; // aligning with parent (the interval must be already aligned)
            if (canSubstAlignWithParent(toAlignWith, dimension, alignment, resizing)) { // toAlignWith is at border and we use parent instead
                if (alignParent == parParent)
                    alignWithParent = true;
                else
                    toAlignWith = alignParent;
            }
        }
        while (toAlignWith == alignParent);

        parParent.add(tempRemoved, removedIndex); // add back temporarily removed

        if (alignParent != parParent)
            return; // can't align (toAlignWith is too deep)

        List aligned = new ArrayList(2);
        List remainder = new ArrayList(2);
        int originalCount = parParent.getSubIntervalCount();

        int effAlign = extract(toAlignWith, aligned, remainder, alignment);
        extract(interval, aligned, remainder, alignment);

        assert !alignWithParent || remainder.isEmpty();

        // add indent if needed
        int indent = LayoutRegion.distance(toAlignWith.getCurrentSpace(), interval.getCurrentSpace(),
                                           dimension, alignment, alignment);
        assert indent == 0 || alignment == LEADING; // currently support indent only at the LEADING side
        if (indent != 0) {
            LayoutInterval indentGap = new LayoutInterval(SINGLE);
            indentGap.setSize(Math.abs(indent));
            // [need to use default padding for indent gap]
            LayoutInterval li = (LayoutInterval) aligned.get(aligned.size()-1);
            if (!li.isSequential()) {
                LayoutInterval seq;
                LayoutInterval parent = li.getParent();
                if (parent == null || !parent.isSequential()) {
                    if (parent != null) {
                        layoutModel.removeInterval(li);
                    }
                    seq = new LayoutInterval(SEQUENTIAL);
                    layoutModel.addInterval(li, seq, 0);
                }
                else seq = parent;
                
                aligned.set(aligned.size()-1, seq);
                li = seq;
            }
            layoutModel.addInterval(indentGap, li, alignment == LEADING ? 0 : -1);
        }

        // prepare the group where the aligned intervals will be placed
        LayoutInterval group;
        LayoutInterval commonSeq;
        if (alignWithParent || (originalCount == 2 && parParent.getParent() != null)) {
            // reuse the original group - avoid unnecessary nesting
            group = parParent;
            if (!remainder.isEmpty()) { // need a sequence for the remainder group
                LayoutInterval groupParent = group.getParent();
                if (groupParent.isSequential()) {
                    commonSeq = groupParent;
                }
                else { // insert a new one
                    int index = layoutModel.removeInterval(group);
                    commonSeq = new LayoutInterval(SEQUENTIAL);
                    commonSeq.setAlignment(group.getAlignment());
                    layoutModel.addInterval(commonSeq, groupParent, index);
//                    commonSeq.getCurrentSpace().set(dimension, groupParent.getCurrentSpace());
                    layoutModel.setIntervalAlignment(group, DEFAULT);
                    layoutModel.addInterval(group, commonSeq, -1);
                }
            }
            else commonSeq = null;
        }
        else { // need to create a new group
            group = new LayoutInterval(PARALLEL);
            group.setGroupAlignment(alignment);
            if (!remainder.isEmpty()) { // need a new sequence for the remainder group
                commonSeq = new LayoutInterval(SEQUENTIAL);
                commonSeq.add(group, 0);
                if (effAlign == LEADING || effAlign == TRAILING) {
                    commonSeq.setAlignment(effAlign);
                }
                layoutModel.addInterval(commonSeq, parParent, -1);
//                commonSeq.getCurrentSpace().set(dimension, parParent.getCurrentSpace());
            }
            else {
                commonSeq = null;
                if (effAlign == LEADING || effAlign == TRAILING) {
                    group.setAlignment(effAlign);
                }
                layoutModel.addInterval(group, parParent, -1);
            }
            if (alignment == LEADING || alignment == TRAILING) {
                int alignPos = toAlignWith.getCurrentSpace().positions[dimension][alignment];
                int outerPos = parParent.getCurrentSpace().positions[dimension][alignment^1];
                group.getCurrentSpace().set(dimension,
                                            alignment == LEADING ? alignPos : outerPos,
                                            alignment == LEADING ? outerPos : alignPos);
            }
        }

        // add the intervals and their separated neighbors to the aligned group
        toAlignWith = (LayoutInterval) aligned.get(0);
//        boolean resizing1 = LayoutInterval.wantResize(toAlignWith, false); // [should rather check all group content except interval]
        if (toAlignWith.getParent() != group) {
            if (toAlignWith.getParent() != null) {
                layoutModel.setIntervalAlignment(toAlignWith, toAlignWith.getAlignment()); // remember explicit alignment
                layoutModel.removeInterval(toAlignWith);
            }
//            if (effAlign== LEADING || effAlign == TRAILING) {
//                layoutModel.setIntervalAlignment(toAlignWith, effAlign); // keeps its alignment
//            }
//            addContent(toAlignWith, group, -1);
            layoutModel.addInterval(toAlignWith, group, -1);
        }

        interval = (LayoutInterval) aligned.get(1);
//        boolean resizing2 = LayoutInterval.wantResize(interval, false);
        if (interval.getParent() != group) {
            if (interval.getParent() != null) {
                layoutModel.removeInterval(interval);
            }
            layoutModel.addInterval(interval, group, -1);
//            addContent(interval, group, -1);
        }
//        else {
//            layoutModel.setIntervalAlignment(interval, alignment);
//        }
        if (!LayoutInterval.isAlignedAtBorder(interval, alignment)) {
            layoutModel.setIntervalAlignment(interval, alignment);
        }

        if (!resizing && group.getSubIntervalCount() == 2 && !LayoutInterval.isAlignedAtBorder(toAlignWith, alignment)) {
            layoutModel.setIntervalAlignment(toAlignWith, alignment);
        }

        if (/*resizing &&*/ LayoutInterval.wantResize(interval)) {
            boolean groupResizing = false;
            for (Iterator it=group.getSubIntervals(); it.hasNext(); ) {
                LayoutInterval li = (LayoutInterval) it.next();
                if (li != interval && LayoutInterval.wantResize(li)) {
                    groupResizing = true;
                    break;
                }
            }
            if (!groupResizing) { // interval resized according to non-resizing content
                operations.suppressGroupResizing(group);
                layoutModel.changeIntervalAttribute(interval, LayoutInterval.ATTRIBUTE_FILL, true);
            }
        }

        // create the remainder group next to the aligned group
        if (!remainder.isEmpty()) {
            LayoutInterval sideGroup = operations.addGroupContent(
                    remainder, commonSeq, commonSeq.indexOf(group), alignment, dimension/*, effAlign*/);
            if (sideGroup != null) {
                int pos1 = parParent.getCurrentSpace().positions[dimension][alignment];
                int pos2 = toAlignWith.getCurrentSpace().positions[dimension][alignment];
                sideGroup.getCurrentSpace().set(dimension,
                                                alignment == LEADING ? pos1 : pos2,
                                                alignment == LEADING ? pos2 : pos1);
                operations.optimizeGaps(sideGroup, dimension);
            }
        }
    }

    private int extract(LayoutInterval interval, List toAlign, List toRemain, int alignment) {
        int effAlign = LayoutInterval.getEffectiveAlignment(interval);
        LayoutInterval parent = interval.getParent();
        if (parent.isSequential()) {
            int extractCount = operations.extract(interval, alignment, false,
                                                  alignment == LEADING ? toRemain : null,
                                                  alignment == LEADING ? null : toRemain);
            if (extractCount == 1) { // the parent won't be reused
                layoutModel.setIntervalAlignment(interval, parent.getAlignment());
                layoutModel.removeInterval(parent);
                toAlign.add(interval);
            }
            else { // we'll reuse the parent sequence in the new group
                toAlign.add(parent);
            }
        }
        else {
            toAlign.add(interval);
        }
        return effAlign;
    }

    private int determinePadding(LayoutInterval interval, int dimension, int alignment) {
        LayoutInterval neighbor = LayoutInterval.getNeighbor(interval, alignment, true, true, false);
        return dragger.findPadding(neighbor, interval, dimension, alignment);
        // need to go through dragger as the component of 'interval' is not in model yet
    }

    /**
     * Finds padding for an interval (yet to be added) in relation to a base
     * interval or a parent interval border (base interval null)
     * @param alignment LEADING or TRAILING point of addingInt
     */ 
    private int determineExpectingPadding(LayoutInterval addingInt,
                                          LayoutInterval baseInt,
                                          LayoutInterval baseParent,
                                          int alignment)
    {
        if (baseInt == null) {
            baseInt = LayoutInterval.getNeighbor(baseParent, SEQUENTIAL, alignment);
        }
        return dragger.findPadding(baseInt, addingInt, dimension, alignment);
    }

    // -----

    private void analyzeParallel(LayoutInterval group, List inclusions) {
        int point = aEdge < 0 ? CENTER : aEdge;

        boolean usable = canUseGroup(group);

        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval sub = (LayoutInterval) it.next();
            if (sub.isEmptySpace())
                continue;

            LayoutRegion subSpace = sub.getCurrentSpace();

            if (sub.isParallel()
                && LayoutRegion.pointInside(addingSpace, point, subSpace, dimension)
                && shouldEnterGroup(sub))
            {   // group space contains significant edge
                analyzeParallel(sub, inclusions);
            }
            else if (sub.isSequential()) {
                // always analyze sequence - it may be valid even if there is no
                // overlap (not required in vertical dimension)
                analyzeSequential(sub, inclusions);
            }
            else if (usable && contentOverlap(addingSpace, sub, dimension^1)) {
                boolean dimOverlap = LayoutRegion.overlap(addingSpace, subSpace, dimension, 0);
                if (dimOverlap && !solveOverlap) {
                    continue; // don't want to deal with the overlap here
                }
                int distance;
                if (!dimOverlap) { // determine distance from the interval
                    if (aSnappedNextTo != null && (sub == aSnappedNextTo || sub.isParentOf(aSnappedNextTo))) {
                        // preferred distance
                        distance = -1;
                    }
                    else { // explicit distance
                        int dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                         dimension, TRAILING, LEADING);
                        int dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                         dimension, TRAILING, LEADING);
                        distance = dstL >= 0 ? dstL : dstT;
                    }
                }
                else distance = 0; // overlapping

                IncludeDesc iDesc = addInclusion(group, false, distance, 0, inclusions);
                if (iDesc != null) {
                    iDesc.neighbor = sub;
                    iDesc.index = getAddDirection(addingSpace, subSpace, dimension, point) == LEADING ? 0 : 1;
                }
            }
        }

        if (inclusions.isEmpty()) { // no inclusion found yet
            if (usable
                && (group.getParent() == null
                    || !canUseGroup(LayoutInterval.getFirstParent(group, PARALLEL))))
            {   // this is the last (top) valid group
                int distance = aSnappedNextTo == group ? -1 : Integer.MAX_VALUE;
                addInclusion(group, false, distance, Integer.MAX_VALUE, inclusions);
            }
        }
    }

    private void analyzeSequential(LayoutInterval group, List inclusions) {
        int point = aEdge < 0 ? CENTER : aEdge;

        boolean inSequence = false;
        boolean parallelWithSequence = false;
        int index = -1;
        int distance = Integer.MAX_VALUE;
        int ortDistance = Integer.MAX_VALUE;

        for (int i=0,n=group.getSubIntervalCount(); i < n; i++) {
            LayoutInterval sub = group.getSubInterval(i);
            if (sub.isEmptySpace())
                continue;

            LayoutRegion subSpace = sub.getCurrentSpace();

            // first analyze the interval as a possible sub-group
            if (sub.isParallel()
                && LayoutRegion.pointInside(addingSpace, point, subSpace, dimension)
                && shouldEnterGroup(sub))
            {   // group space contains significant edge
                int count = inclusions.size();
                analyzeParallel(sub, inclusions);
                if (inclusions.size() > count)
                    return;
            }

            // second analyze the interval as a single element for "next to" placement
            boolean ortOverlap = contentOverlap(addingSpace, sub, dimension^1);
            int margin = (dimension == VERTICAL && !ortOverlap ? 4 : 0);
            boolean dimOverlap = LayoutRegion.overlap(addingSpace, subSpace, dimension, margin);
            // in vertical dimension always pretend orthogonal overlap if there
            // is no overlap in horizontal dimension (i.e. force inserting into sequence)
            if (ortOverlap || (dimension == VERTICAL && !dimOverlap && !parallelWithSequence)) {
                if (dimOverlap) { // overlaps in both dimensions
                    if (!solveOverlap) {
                        return; // don't want to solve the overlap in this sequence
                    }
                    inSequence = true;
                    distance = ortDistance = 0;
                }
                else { // determine distance from the interval
                    int dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                     dimension, TRAILING, LEADING);
                    int dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                     dimension, TRAILING, LEADING);
                    if (dstL >= 0 && dstL < distance)
                        distance = dstL;
                    if (dstT >= 0 && dstT < distance)
                        distance = dstT;

                    if (ortOverlap) {
                        ortDistance = 0;
                        inSequence = true;
                    }
                    else { // remember also the orthogonal distance
                        dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                     dimension^1, TRAILING, LEADING);
                        dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                     dimension^1, TRAILING, LEADING);
                        if (dstL > 0 && dstL < ortDistance)
                            ortDistance = dstL;
                        if (dstT > 0 && dstT < ortDistance)
                            ortDistance = dstT;
                    }
                }
                if (getAddDirection(addingSpace, subSpace, dimension, point) == LEADING) {
                    index = i;
                    break; // this interval is already after the adding one, no need to continue
                }
                else { // intervals before this one are irrelevant
                    parallelWithSequence = false;
                }
            }
            else { // no orthogonal overlap, moreover in vertical dimension located parallelly
                parallelWithSequence = true;
            }
        }

        if ((inSequence || (dimension == VERTICAL && !parallelWithSequence))
            && canUseGroup(parallelWithSequence ? group : group.getParent()))
        {   // so it make sense to add the interval to this sequence
            if (index < 0) {
                index = group.getSubIntervalCount();
            }
            if (aSnappedNextTo != null
                && (group.isParentOf(aSnappedNextTo) || aSnappedNextTo.getParent() == null))
            {   // snapped interval is in this sequence, or it is the root group
                distance = -1; // preferred distance
            }
            IncludeDesc iDesc = addInclusion(group, parallelWithSequence, distance, ortDistance, inclusions);
            if (iDesc != null) {
                iDesc.index = index < 0 ? group.getSubIntervalCount() : index;
            }
        }
    }

    private IncludeDesc addInclusion(LayoutInterval parent,
                                     boolean subgroup,
                                     int distance,
                                     int ortDistance,
                                     List inclusions)
    {
        if (!inclusions.isEmpty()) {
            int index = inclusions.size() - 1;
            IncludeDesc last = (IncludeDesc) inclusions.get(index);
            boolean useLast = false;
            boolean useNew = false;

            boolean ortOverlap1 = last.ortDistance == 0;
            boolean ortOverlap2 = ortDistance == 0;
            if (ortOverlap1 != ortOverlap2) {
                useLast = ortOverlap1;
                useNew = ortOverlap2;
            }
            else if (ortOverlap1) { // both having orthogonal overlap
                useLast = useNew = true;
            }
            else { // none having orthogonal overlap (could happen in vertical dimension)
                if (last.ortDistance != ortDistance) {
                    useLast = last.ortDistance < ortDistance;
                    useNew = ortDistance < last.ortDistance;
                }
                else if (last.distance != distance) {
                    useLast = last.distance < distance;
                    useNew = distance < last.distance;
                }
            }
            if (!useLast && !useNew) { // could not choose according to distance, so prefer deeper position
                LayoutInterval parParent = last.parent.isParallel() ?
                                           last.parent : last.parent.getParent();
                useNew = parParent.isParentOf(parent);
                useLast = !useNew;
            }

            if (!useLast)
                inclusions.remove(index);
            if (!useNew)
                return null;
        }

        IncludeDesc iDesc = new IncludeDesc();
        iDesc.parent = parent;
        iDesc.newSubGroup = subgroup;
        iDesc.alignment = aEdge;
        iDesc.snappedParallel = aSnappedParallel;
        if (distance == -1) {
            iDesc.snappedNextTo = aSnappedNextTo;
            iDesc.fixedPosition = true;
        }
        iDesc.distance = distance;
        iDesc.ortDistance = ortDistance;
        inclusions.add(iDesc);

        return iDesc;
    }

    private static boolean sameSubGroupAlignment(LayoutInterval group, LayoutInterval subgroup) {
        assert group.isParallel() && subgroup.isParallel();

        int align = subgroup.getAlignment();
        boolean sameAlign = true;
        Iterator it = subgroup.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (LayoutInterval.wantResize(li)) { // will span over whole group
                sameAlign = true;
                break;
            }
            if (li.getAlignment() != align) {
                sameAlign = false;
            }
        }

        return sameAlign && LayoutInterval.canResize(subgroup) == LayoutInterval.canResize(group);
    }

    /**
     * @param preserveOriginal if true, original inclusion needs to be preserved,
     *        will be merged with new inclusion sequentially; if false, original
     *        inclusion is just consulted when choosing best inclusion
     */
    private void mergeParallelInclusions(List inclusions, IncludeDesc original, boolean preserveOriginal) {
        // 1st step - find representative (best) inclusion
        IncludeDesc best = null;
        boolean bestOriginal = false;
        for (Iterator it=inclusions.iterator(); it.hasNext(); ) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (original == null || !preserveOriginal || canCombine(iDesc, original)) {
                if (best != null) {
                    boolean originalCompatible = original != null && !preserveOriginal
                                                 && iDesc.parent == original.parent;
                    if (!bestOriginal && originalCompatible) {
                        best = iDesc;
                        bestOriginal = true;
                    }
                    else if (bestOriginal == originalCompatible) {
                        LayoutInterval group1 = best.parent.isSequential() ?
                                                best.parent.getParent() : best.parent;
                        LayoutInterval group2 = iDesc.parent.isSequential() ?
                                                iDesc.parent.getParent() : iDesc.parent;
                        if (group1.isParentOf(group2)) {
                            best = iDesc; // deeper is better
                        }
                        else if (!group2.isParentOf(group1) && iDesc.distance < best.distance) {
                            best = iDesc;
                        }
                    }
                }
                else best = iDesc;
            }
        }

        // 2nd remove incompatible inclusions, move compatible ones to same level
        LayoutInterval commonGroup = best.parent.isSequential() ? best.parent.getParent() : best.parent;
        for (Iterator it=inclusions.iterator(); it.hasNext(); ) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (iDesc != best) {
                if (!compatibleInclusions(iDesc, best, dimension)) {
                    it.remove();
                }
                else {
                    LayoutInterval group = iDesc.parent.isSequential() ?
                                           iDesc.parent.getParent() : iDesc.parent;
                    if (group.isParentOf(commonGroup)) {
                        LayoutInterval neighbor = iDesc.parent.isSequential() ?
                                                  iDesc.parent : iDesc.neighbor;
                        layoutModel.removeInterval(neighbor);
                        // [what about the alignment?]
                        layoutModel.addInterval(neighbor, commonGroup, -1);
                        if (group.getSubIntervalCount() == 1) {
                            LayoutInterval last = layoutModel.removeInterval(group, 0);
                            operations.addContent(last, group.getParent(), layoutModel.removeInterval(group));
                        }
                        if (iDesc.parent == group)
                            iDesc.parent = commonGroup;
                    }
                }
            }
        }

        // 3rd analyse inclusions requiring a subgroup (parallel with part of sequence)
        LayoutInterval subGroup = null;
        int subAlign = DEFAULT;
        boolean defaultPadding = false;
        List separatedLeading = new LinkedList();
        List separatedTrailing = new LinkedList();

        for (Iterator it=inclusions.iterator(); it.hasNext(); ) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (iDesc.parent.isSequential() && iDesc.newSubGroup) {
                addToGroup(iDesc, null);
                LayoutInterval addedGroup = LayoutInterval.getFirstParent(addingInterval, PARALLEL);
                if (subGroup == null) {
                    subAlign = (addingInterval.getParent().isParallel() ?
                                addingInterval : addingInterval.getParent()).getAlignment();
                }
                operations.extract(addedGroup, DEFAULT, true, separatedLeading, separatedTrailing);
                layoutModel.removeInterval(addingInterval);
                layoutModel.removeInterval(addedGroup);
                layoutModel.removeInterval(iDesc.parent);
                if (subGroup == null)
                    subGroup = addedGroup;
                else
                    operations.addContent(addedGroup, subGroup, -1); // [or something special to ensure the group is dismantled?]
            }
            if (iDesc.snappedNextTo != null)
                defaultPadding = true;
        }

        int extractAlign = DEFAULT;
        if (subGroup != null) {
            if (separatedLeading.isEmpty())
                extractAlign = TRAILING;
            if (separatedTrailing.isEmpty())
                extractAlign = LEADING;
        }
        LayoutInterval subsubGroup;
        if (extractAlign != DEFAULT) { // intervals from one side will be grouped
                // with the adding interval - instead of going into a side group
            subsubGroup = new LayoutInterval(PARALLEL);
            subsubGroup.setGroupAlignment(extractAlign);
        }
        else subsubGroup = null;

        // 4th collect surroundings of adding component
        for (Iterator it=inclusions.iterator(); it.hasNext(); ) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (iDesc.parent.isParallel() || !iDesc.newSubGroup) {
                addToGroup(iDesc, null);
//                mainEffectiveAlign = getEffectiveAlignment(interval);
                operations.extract(addingInterval, extractAlign, extractAlign == DEFAULT,
                                   separatedLeading, separatedTrailing);
                LayoutInterval parent = addingInterval.getParent();
                layoutModel.removeInterval(addingInterval);
                layoutModel.removeInterval(parent);
                if (subsubGroup != null && LayoutInterval.getCount(parent, DEFAULT, true) >= 1) {
                    layoutModel.addInterval(parent, subsubGroup, -1);
                }
            }
            if (iDesc != best)
                it.remove();
        }

        // 5th create groups of merged content around the adding component
        int[] borderPos = commonGroup.getCurrentSpace().positions[dimension];
        LayoutInterval commonSeq;
        int index;
        if (commonGroup.getSubIntervalCount() == 0 && commonGroup.getParent() != null) {
            // the common group got empty - eliminate it to avoid unncessary nesting
            LayoutInterval parent = commonGroup.getParent();
            index = layoutModel.removeInterval(commonGroup);
            if (parent.isSequential()) {
                commonSeq = parent;
                commonGroup = parent.getParent();
            }
            else { // parallel parent
                commonSeq = new LayoutInterval(SEQUENTIAL);
                commonSeq.setAlignment(commonGroup.getAlignment());
                layoutModel.addInterval(commonSeq, parent, index);
                commonGroup = parent;
                index = 0;
            }
        }
        else {
            commonSeq = new LayoutInterval(SEQUENTIAL);
            layoutModel.addInterval(commonSeq, commonGroup, -1);
            index = 0;
        }
        if (commonSeq.getSubIntervalCount() == 0) {
            commonSeq.getCurrentSpace().set(dimension, commonGroup.getCurrentSpace());
        }
        if (!separatedLeading.isEmpty()) {
            int checkCount = commonSeq.getSubIntervalCount(); // remember ...
            LayoutInterval sideGroup = operations.addGroupContent(
                    separatedLeading, commonSeq, index, dimension, LEADING); //, mainEffectiveAlign
            if (sideGroup != null) {
                sideGroup.getCurrentSpace().set(
                        dimension, borderPos[LEADING], addingSpace.positions[dimension][LEADING]);
                operations.optimizeGaps(sideGroup, dimension);
            }
            index += commonSeq.getSubIntervalCount() - checkCount;
        }
        if (!separatedTrailing.isEmpty()) {
            LayoutInterval sideGroup = operations.addGroupContent(
                    separatedTrailing, commonSeq, index, dimension, TRAILING); //, mainEffectiveAlign
            if (sideGroup != null) {
                sideGroup.getCurrentSpace().set(
                        dimension, addingSpace.positions[dimension][TRAILING], borderPos[TRAILING]);
                operations.optimizeGaps(sideGroup, dimension);
            }
        }

        // resolve subgroup
        if (subGroup != null) {
            if (subsubGroup != null && subGroup.getSubIntervalCount() > 0) {
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                layoutModel.addInterval(addingInterval, seq, 0);
                operations.addContent(subsubGroup, seq, extractAlign == LEADING ? 1 : 0);
                layoutModel.addInterval(seq, subGroup, -1);
            }
            else {
                layoutModel.addInterval(addingInterval, subGroup, subAlign);
            }
            toAdd = subGroup;
        }

        // 6th merge the inclusions
        best.parent = commonSeq;
        best.newSubGroup = false;
        best.neighbor = null;
        best.index = index;
        if (defaultPadding) {
            LayoutInterval li = commonSeq.getSubInterval(index);
            if (li.isEmptySpace())
                li = LayoutInterval.getDirectNeighbor(li, best.alignment, true);
            best.snappedNextTo = li;
            best.fixedPosition = true;
        }
    }

    private static boolean compatibleInclusions(IncludeDesc iDesc1, IncludeDesc iDesc2, int dimension) {
        LayoutInterval group1 = iDesc1.parent.isSequential() ?
                                iDesc1.parent.getParent() : iDesc1.parent;
        LayoutInterval group2 = iDesc2.parent.isSequential() ?
                                iDesc2.parent.getParent() : iDesc2.parent;
        if (group1 == group2)
            return true;

        if (group1.isParentOf(group2)) {
            LayoutInterval temp = group1;
            group1 = group2;
            group2 = temp;
            IncludeDesc itemp = iDesc1;
            iDesc1 = iDesc2;
            iDesc2 = itemp;
        }
        else if (!group2.isParentOf(group1)) {
            return false;
        }

        // group2 is parent of group1
        LayoutInterval neighbor = iDesc2.parent.isSequential() ? iDesc2.parent : iDesc2.neighbor;
        if (neighbor == null)
            return false;
        LayoutRegion spaceToHold = neighbor.getCurrentSpace();
        LayoutRegion spaceAvailable = group1.getCurrentSpace();
        return LayoutRegion.pointInside(spaceToHold, LEADING, spaceAvailable, dimension)
               && LayoutRegion.pointInside(spaceToHold, TRAILING, spaceAvailable, dimension);
    }

    private boolean mergeSequentialInclusions(IncludeDesc iDesc1, IncludeDesc iDesc2) {
        if (iDesc2 == null || !canCombine(iDesc1, iDesc2))
            return false;

        assert (iDesc1.alignment == LEADING || iDesc1.alignment == TRAILING)
                && (iDesc2.alignment == LEADING || iDesc2.alignment == TRAILING)
                && iDesc1.alignment == (iDesc2.alignment^1);

        if (iDesc1.parent == iDesc2.parent)
            return true;

        if (iDesc1.alignment == TRAILING) {
            IncludeDesc temp = iDesc1;
            iDesc1 = iDesc2;
            iDesc2 = temp;
        }

        LayoutInterval commonGroup;
        if (iDesc1.parent.isParentOf(iDesc2.parent))
            commonGroup = iDesc1.parent;
        else if (iDesc2.parent.isParentOf(iDesc1.parent))
            commonGroup = iDesc2.parent;
        else
            commonGroup = LayoutInterval.getFirstParent(iDesc1.parent, SEQUENTIAL);

        assert commonGroup.isSequential();

        int startIndex = 0;
        LayoutInterval ext1;
        boolean startGap = false;
        if (commonGroup.isParentOf(iDesc1.parent)) {
            ext1 = iDesc1.parent.isSequential() ? iDesc1.parent : iDesc1.neighbor;
            if (ext1 != null)
                startIndex = commonGroup.indexOf(ext1.getParent());
        }
        else {
            ext1 = null;
            startGap = commonGroup.getSubInterval(startIndex).isEmptySpace();
        }

        int endIndex = commonGroup.getSubIntervalCount() - 1;
        LayoutInterval ext2;
        boolean endGap = false;
        if (commonGroup.isParentOf(iDesc2.parent)) {
            ext2 = iDesc2.parent.isSequential() ? iDesc2.parent : iDesc2.neighbor;
            if (ext2 != null)
                endIndex = commonGroup.indexOf(ext2.getParent());
        }
        else {
            ext2 = null;
            endGap = commonGroup.getSubInterval(endIndex).isEmptySpace();
        }

        if (endIndex > startIndex + 1
            || (endIndex == startIndex+1 && !startGap && !endGap))
        {   // there is a significant part of the common sequence to be parallelized
            LayoutInterval parGroup;
            if (startIndex == 0 && endIndex == commonGroup.getSubIntervalCount()-1) {
                // parallel with whole sequence
                parGroup = commonGroup.getParent();
            }
            else { // separate part of the original sequence
                parGroup = new LayoutInterval(PARALLEL);
                LayoutInterval parSeq = new LayoutInterval(SEQUENTIAL);
                layoutModel.addInterval(parSeq, parGroup, 0);
                int i = startIndex;
                while (i <= endIndex) {
                    LayoutInterval li = layoutModel.removeInterval(commonGroup, i);
                    endIndex--;
                    layoutModel.addInterval(li, parSeq, -1);
                }
                layoutModel.addInterval(parGroup, commonGroup, startIndex);
            }
            LayoutInterval extSeq = new LayoutInterval(SEQUENTIAL); // sequence for the extracted inclusion targets
            layoutModel.addInterval(extSeq, parGroup, -1);
            if (ext1 != null) {
                LayoutInterval parent = ext1.getParent();
                layoutModel.removeInterval(ext1);
                if (parent.getSubIntervalCount() == 1) {
                    LayoutInterval last = layoutModel.removeInterval(parent, 0);
                    operations.addContent(last, parent.getParent(), layoutModel.removeInterval(parent));
                }
                operations.addContent(ext1, extSeq, 0);
                if (ext2 != null) {
                    LayoutInterval gap = new LayoutInterval(SINGLE);
                    int size = LayoutRegion.distance(ext1.getCurrentSpace(), ext2.getCurrentSpace(), dimension, LEADING, TRAILING);
                    gap.setSize(size);
                    layoutModel.addInterval(gap, extSeq, -1);

                    iDesc1.index = iDesc2.index = extSeq.indexOf(gap);
                }
                else iDesc2.index = iDesc1.index;
            }
            else iDesc1.index = iDesc2.index;
            if (ext2 != null) {
                LayoutInterval parent = ext2.getParent();
                layoutModel.removeInterval(ext2);
                if (parent.getSubIntervalCount() == 1) {
                    LayoutInterval last = layoutModel.removeInterval(parent, 0);
                    operations.addContent(last, parent.getParent(), layoutModel.removeInterval(parent));
                }
                operations.addContent(ext2, extSeq, -1);
            }

            iDesc1.parent = iDesc2.parent = extSeq;
            iDesc1.newSubGroup = iDesc2.newSubGroup = false;
            iDesc1.neighbor = iDesc2.neighbor = null;
        }
        else { // end position, stay in subgroup
            if (iDesc2.parent.isParentOf(iDesc1.parent)) {
                iDesc2.parent = iDesc1.parent;
                iDesc2.index = iDesc1.index;
                iDesc2.neighbor = iDesc1.neighbor;
                if (endGap) // there's an outer gap
                    iDesc2.fixedPosition = false;
            }
            else if (iDesc1.parent.isParentOf(iDesc2.parent)) {
                iDesc1.parent = iDesc2.parent;
                iDesc1.index = iDesc2.index;
                iDesc1.neighbor = iDesc2.neighbor;
                if (startGap) // there's an outer gap
                    iDesc1.fixedPosition = false;
            }
        }

        return true;
    }

    private boolean shouldEnterGroup(LayoutInterval group) {
        assert group.isParallel();

        int alignment = aEdge;
        int groupAlign = group.getGroupAlignment();
        if (groupAlign != alignment
            && ((groupAlign != LEADING && groupAlign != TRAILING)
                 || (alignment != LEADING && alignment != TRAILING && alignment != DEFAULT)))
            return false; // incompatible group alignment

        if (aSnappedParallel != null && !allowsSubAlignWith(aSnappedParallel, group, alignment))
            return false; // could not align with position.interval

        return true;
    }

    private boolean canUseGroup(LayoutInterval group) {
        // allows align with aSnappedParallel?
        return aSnappedParallel == null
               || (group.isSequential() && group.isParentOf(aSnappedParallel)) // subgroup to be created
               || (group.isParallel() && canAlignWith(aSnappedParallel, group, aEdge));
    }

    /**
     * Computes whether a space overlaps with content of given interval.
     * The difference from LayoutRegion.overlap(...) is that this method goes
     * recursivelly to sub-intervals (in case of parallel groups).
     */
    private static boolean contentOverlap(LayoutRegion space, LayoutInterval interval, int dimension) {
        boolean overlap = LayoutRegion.overlap(space, interval.getCurrentSpace(), dimension, 0);
        if (overlap && interval.isGroup()) {
            overlap = false;
            Iterator it = interval.getSubIntervals();
            while (it.hasNext()) {
                LayoutInterval li = (LayoutInterval) it.next();
                if (!li.isEmptySpace() && contentOverlap(space, li, dimension)) {
                    overlap = true;
                    break;
                }
            }
        }
        return overlap;
    }

    /**
     * @return whether within or under 'group' one might align in parallel
     *         with 'interval'; so it return false if content of 'group' is
     *         in an incompatible branch
     */
    private boolean allowsSubAlignWith(LayoutInterval interval, LayoutInterval group, int alignment) {
        if (group.isParentOf(interval))
            return true;

        LayoutInterval parent = LayoutInterval.getFirstParent(interval, PARALLEL);
        while (parent != null) {
            if (LayoutInterval.isAlignedAtBorder(interval, parent, alignment)) {
                if (parent.isParentOf(group) && LayoutInterval.isAlignedAtBorder(group, parent, alignment)) {
                    return true;
                }
                interval = parent;
                parent = LayoutInterval.getFirstParent(interval, PARALLEL);
            }
            else parent = null;
        }

        return false;
    }

    /**
     * @return whether being in 'group' (having it as first parallel parent)
     *         allows parallel align with 'interval'
     */
    private boolean canAlignWith(LayoutInterval interval, LayoutInterval group, int alignment) {
        LayoutInterval parent = interval.getParent();
        if (parent == null)
            parent = interval;
        else if (parent.isSequential())
            parent = parent.getParent();

        while (parent != null && parent != group && !parent.isParentOf(group)) {
            if (canSubstAlignWithParent(interval, dimension, alignment, dragger.isResizing(dimension))) {
                interval = parent;
                parent = LayoutInterval.getFirstParent(interval, PARALLEL);
            }
            else parent = null;
        }
        if (parent == null)
            return false;
        if (parent == group)
            return true;
        // otherwise parent.isParentOf(group)
        return LayoutInterval.isAlignedAtBorder(group, parent, alignment);
        // we silently assume that addingInterval will end up aligned in 'group'
    }

    private static boolean canSubstAlignWithParent(LayoutInterval interval, int dimension, int alignment, boolean placedAtBorderEnough) {
        LayoutInterval parent = LayoutInterval.getFirstParent(interval, PARALLEL);
        boolean aligned = LayoutInterval.isAlignedAtBorder(interval, parent, alignment);
        if (!aligned && LayoutInterval.isPlacedAtBorder(interval, parent, dimension, alignment)) {
            aligned = placedAtBorderEnough
                      || LayoutInterval.getDirectNeighbor(parent, alignment, true) != null
                      || LayoutInterval.isClosedGroup(parent, alignment);
            if (!aligned) { // check if the group can be considered "closed" at alignment edge
                boolean allTouching = true;
                for (Iterator it=parent.getSubIntervals(); it.hasNext(); ) {
                    LayoutInterval li = (LayoutInterval) it.next();
                    if (li.getAlignment() == alignment || LayoutInterval.wantResize(li)) {
                        aligned = true;
                        break;
                    }
                    else if (allTouching && !LayoutInterval.isPlacedAtBorder(li, dimension, alignment)) {
                        allTouching = false;
                    }
                }
                if (allTouching)
                    aligned = true;
            }
        }
        return aligned;
    }

    private boolean canCombine(IncludeDesc iDesc1, IncludeDesc iDesc2) {
        if (iDesc1.parent == iDesc2.parent)
            return true;

        LayoutInterval commonGroup;
        if (iDesc1.parent.isParentOf(iDesc2.parent))
            return isBorderInclusion(iDesc2);
        else if (iDesc2.parent.isParentOf(iDesc1.parent))
            return isBorderInclusion(iDesc1);
        else {
            LayoutInterval parParent1 = iDesc1.parent.isParallel() ? iDesc1.parent : iDesc1.parent.getParent();
            LayoutInterval parParent2 = iDesc2.parent.isParallel() ? iDesc2.parent : iDesc2.parent.getParent();
            return parParent1.getParent() == parParent2.getParent()
                   && isBorderInclusion(iDesc1)
                   && isBorderInclusion(iDesc2)
                   && LayoutInterval.getDirectNeighbor(parParent1, iDesc1.alignment^1, true) == parParent2;
        }
    }

    private static boolean isBorderInclusion(IncludeDesc iDesc) {
        if (iDesc.alignment != LEADING && iDesc.alignment != TRAILING)
            return false;

        LayoutInterval neighbor = iDesc.parent.isSequential() ? iDesc.parent : iDesc.neighbor;
        if (neighbor == null)
            return true;
        if (iDesc.alignment == TRAILING)
            return iDesc.index == 0;
        else { // LEADING
            int count = neighbor.isSequential() ? neighbor.getSubIntervalCount() : 1;
            return iDesc.index >= count;
        }
    }

    private static int getAddDirection(LayoutRegion adding,
                                       LayoutRegion existing,
                                       int dimension,
                                       int alignment)
    {
        return LayoutRegion.distance(adding, existing, dimension, alignment, CENTER) > 0 ?
               LEADING : TRAILING;
    }
}
