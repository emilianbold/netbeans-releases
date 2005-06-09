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

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.util.Iterator;

/*
Finding position procedure:
- find vertical position first - preferring aligned position
- find horizontal position - preferring derived (next to) position

finding position in a dimension:
- find best derived positions for each alignment (L, T):
  - go down from the root, looking for distances (opposite edges)
  - exclude elements not overlapping in the other dimension
  - if a distance is smaller than SNAP_DISTANCE, compare with the best position so far:
    - better position is that with better alignment in the opposite dimension
      - i.e. has smallest alignment distance (any)
    - otherwise the distance must be closer
    - otherwise the lower interval in hierarchy is preferred (given that
      it is visited only if the position is inside higher)
- find best aligned positions for each alignment (L, T, C, B):
  - go down from the root, looking for distances (same edges)
  - check only component elements
  - if a distance is smaller than SNAP_DISTANCE, compare with the best position so far:
    - closer distance is better
    - otherwise the distance in the other dimension must be closer
- choose the best position
  - the unpreferred position must have twice smaller distance, and be better
    by at least SNAP_DISTANCE/2
*/

class LayoutDragger implements LayoutConstants {

    private VisualMapper visualMapper;

    // fixed (initial) parameters of the operation ---

    // type of the operation, one of ADDING, MOVING, RESIZING
    private int operation;
    private static final int ADDING = 0;
    private static final int MOVING = 1;
    private static final int RESIZING = 2;

    // components being moved
    private LayoutComponent[] movingComponents;

    // for each dimension defines what component edges are to be moved:
    //   - LayoutRegion.ALL_POINTS means whole component is moved
    //   - LEADING or TRAILING means the component is resized
    //   - LayoutRegion.NO_POINT means the component is not changed in given dimension
    private int movingEdges[]; // array according to dimension (HORIZONTAL or VERTICAL)

    // indicates whether the operation is resizing - according to movingEdges
//    private boolean resizing;

    // initial components' positions and sizes (defines the formation to move)
    private LayoutRegion[] movingFormation;

    // initial mouse cursor position (when moving/resizing starts)
    private int[] startCursorPosition;

    // parameters changed with each move step ---

    // last mouse cursor position
    private int[] lastCursorPosition = new int[] { LayoutRegion.UNKNOWN, LayoutRegion.UNKNOWN };

    // direction of mouse move from last position
    private int[] moveDirection = new int[] { LEADING, LEADING };

    // dimension that is temporarily locked (kept unchanged)
    private int lockedDimension = -1;

    // container the components are being moved in/over
    private LayoutComponent targetContainer;

    // actual components' bounds of moving/resizing components
    private LayoutRegion[] movingBounds;

    // last found positions for the moving/resizing component
    private PositionDef[] bestPositions = new PositionDef[DIM_COUNT];

    // the following fields hold various parameters used during the procedure
    // of finding a suitable position for the moving/resizing components 
    // (fields are used not to need to pass the parameters through all the
    // methods again and again and to avoid repetitive creation of arrays) ---

    private LayoutRegion movingSpace;
    private int dimension;
    private boolean snapping;
    private LayoutInterval[] inside = new LayoutInterval[2]; // LEADING, TRAILING

    // arrays of position candidates for the moving/resizing component
    private PositionDef[][] findingsNextTo;
    private PositionDef[][] findingsAligned;

    // constants ---

    static final int[] ALL_EDGES = { LayoutRegion.ALL_POINTS,
                                     LayoutRegion.ALL_POINTS };

    // length of tips of painted guiding lines
    private static final int GL_TIP = 8;

    // distance in which components are drawn to guiding line
    private static final int SNAP_DISTANCE = 8;

    // max. orthogonal distance from a component to be still recognized as "next to"
    private static final int ORT_DISTANCE = 8;
    
    private static BasicStroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER, 5.0f, new float[]{5.0f, 2.0f}, 0.0f);

    // -----
    // setup

    LayoutDragger(LayoutComponent[] comps,
                  LayoutRegion[] compBounds,
                  int[] initialCursorPos,
                  int[] movingEdges,
                  VisualMapper mapper)
    {
        if (comps[0].getParent() == null) {
            operation = ADDING;
        }
        else {
            operation = MOVING;
            for (int i=0; i < DIM_COUNT; i++) {
                if (movingEdges[i] == LEADING || movingEdges[i] == TRAILING) {
                    operation = RESIZING;
                    break;
                }
            }
        }

        this.movingComponents = comps;
        this.movingFormation = compBounds;
        this.startCursorPosition = initialCursorPos;
        this.movingEdges = movingEdges;
        visualMapper = mapper;

        movingBounds = new LayoutRegion[compBounds.length];
        for (int i=0; i < compBounds.length; i++) {
            movingBounds[i] = new LayoutRegion();
        }

        findingsNextTo = new PositionDef[DIM_COUNT][];
        findingsAligned = new PositionDef[DIM_COUNT][];
        for (int i=0; i < DIM_COUNT; i++) {
            int n = LayoutRegion.POINT_COUNT[i];
            findingsNextTo[i] = new PositionDef[n];
            findingsAligned[i] = new PositionDef[n];
            for (int j=0; j < n; j++) {
                findingsNextTo[i][j] = new PositionDef();
                findingsAligned[i][j] = new PositionDef();
            }
        }
    }

    void setTargetContainer(LayoutComponent container) {
        targetContainer = container;
    }

    LayoutComponent getTargetContainer() {
        return targetContainer;
    }

    boolean isResizing() {
        return operation == RESIZING;
    }

    boolean isResizing(int dim) {
        return movingEdges[dim] == LEADING || movingEdges[dim] == TRAILING;
    }

    LayoutComponent[] getMovingComponents() {
        return movingComponents;
    }

    LayoutRegion[] getMovingBounds() {
        return movingBounds;
    }

    // -----
    // moving

    void move(int[] cursorPos, boolean autoPositioning, boolean lockDimension) {
        // translate mouse cursor position, compute move direction, ...
        int lockCandidate = -1; // dimension that might be locked if there's aligned position
        int minDelta = Integer.MAX_VALUE;
        for (int i=0; i < DIM_COUNT; i++) {
            cursorPos[i] -= startCursorPosition[i]; // translate to diff from the initial state
            int currentPos = cursorPos[i];
            int lastPos = lastCursorPosition[i];
            lastCursorPosition[i] = currentPos;
            if (lastPos == LayoutRegion.UNKNOWN) { // first move step, can't calc direction
                lockDimension = false; // can't lock yet
            }
            else {
                int delta = currentPos - lastPos;
                if (delta != 0) { // position changed in this direction
                    moveDirection[i] = delta > 0 ? TRAILING : LEADING;
                }
                if (movingEdges[i] != LayoutRegion.ALL_POINTS) {
                    lockDimension = false; // can't lock - this is not pure moving
                }
                else if (lockedDimension < 0) { // not locked yet
                    PositionDef pos = bestPositions[i];
                    if (pos != null && !pos.nextTo && delta < minDelta) {
                        lockCandidate = i;
                        minDelta = delta;
                    }
                }
            }
        }

        // check locked dimension
        if (lockDimension) {
            if (lockedDimension < 0) { // not set yet
                lockedDimension = lockCandidate;
            }
        }
        else lockedDimension = -1;

        // compute actual position of the moving components
        for (int i=0; i < movingBounds.length; i++) {
            for (int j=0; j < DIM_COUNT; j++) {
                if (j != lockedDimension) {
                    movingBounds[i].set(j, movingFormation[i]);
                    movingBounds[i].reshape(j, movingEdges[j], cursorPos[j]);
                }
                // for locked dimension the space is already set and not changing
            }
        }
        movingSpace = movingBounds[0]; // [limitation: only one component can be moved]

        // reset finding results
        for (int i=0; i < DIM_COUNT; i++) {
            if (i != lockedDimension) {
                bestPositions[i] = null;
                for (int j=0; j < LayoutRegion.POINT_COUNT[i]; j++) {
                    findingsNextTo[i][j].reset();
                    findingsAligned[i][j].reset();
                }
            }
        }

        // find position in the layout
        snapping = autoPositioning;
        if (autoPositioning) {
            // important: looking for vertical position first
            for (dimension=DIM_COUNT-1; dimension >= 0; dimension--) {
                if (dimension != lockedDimension
                    && movingEdges[dimension] != LayoutRegion.NO_POINT)
                {   // look for a suitable position in this dimension
                    PositionDef best = findBestPosition();
                    bestPositions[dimension] = best;
                    if (best != null) { // snap effect
                        cursorPos[dimension] -= best.distance;
                        movingSpace.reshape(dimension,
                                            movingEdges[dimension],
                                            -best.distance);
                    }
                }
            }
        }

        // translate mouse cursor position back to absolute coordinates
        for (int i=0; i < DIM_COUNT; i++) {
            cursorPos[i] += startCursorPosition[i];
        }
    }

    void paintMoveFeedback(Graphics2D g) {
        final int OVERLAP = 10;
        for (int i=0; i < DIM_COUNT; i++) {    
            LayoutDragger.PositionDef position = bestPositions[i];
            if (position != null) {
                boolean inRoot = (position.interval.getParent() == null);
                int dir = 1-i; // opposite direction
                int align = position.alignment;
                LayoutInterval interval = position.interval;
                LayoutInterval parent = interval.getParent();
                LayoutRegion posRegion = interval.getCurrentSpace();
                if (parent != null && parent.isParallel()) {
                    // check if the interval alignment coincides with parent
                    if (align == LEADING || align == TRAILING) {
                        if (!position.nextTo
                            && LayoutRegion.distance(posRegion, movingSpace, i, align, align) == 0)
                        {
                            interval = parent;
                        }
                    }
                    else if (align == parent.getGroupAlignment()) {
                        interval = parent;
                    }
                }
                LayoutRegion contRegion = targetContainer.getLayoutRoot(0).getCurrentSpace();
                int conty1 = contRegion.positions[dir][LayoutConstants.LEADING];
                int conty2 = contRegion.positions[dir][LayoutConstants.TRAILING];
                int posx = posRegion.positions[i][(inRoot || !position.nextTo) ? align : 1-align];
                int posy1 = posRegion.positions[dir][LayoutConstants.LEADING]-OVERLAP;
                posy1 = Math.max(posy1, conty1);
                int posy2 = posRegion.positions[dir][LayoutConstants.TRAILING]+OVERLAP;
                posy2 = Math.min(posy2, conty2);
                int x = movingBounds[0].positions[i][align];
                int y1 = movingBounds[0].positions[dir][LayoutConstants.LEADING]-OVERLAP;
                y1 = Math.max(y1, conty1);
                int y2 = movingBounds[0].positions[dir][LayoutConstants.TRAILING]+OVERLAP;
                y2 = Math.min(y2, conty2);
                Stroke oldStroke = g.getStroke();
                g.setStroke(dashedStroke);
                if (position.nextTo) { // adding next to
                    if (i == LayoutConstants.HORIZONTAL) {
                        g.drawLine(x, Math.min(y1, posy1), x, Math.max(y2, posy2));
                    } else {
                        g.drawLine(Math.min(y1, posy1), x, Math.max(y2, posy2), x);
                    }
                } else { // adding aligned
                    int ay1 = Math.min(y1, posy1);
                    int ay2 = Math.max(y2, posy2);
                    if (x == posx) {
                        if (i == LayoutConstants.HORIZONTAL) {
                            g.drawLine(posx, Math.min(y1, posy1), posx, Math.max(y2, posy2));
                        } else {
                            g.drawLine(Math.min(y1, posy1), posx, Math.max(y2, posy2), posx);
                        }
                    }
                    else { // indented position
                        if (i == LayoutConstants.HORIZONTAL) {
                            g.drawLine(posx, posy1, posx, posy2);
                            g.drawLine(x, y1, x, y2);
                        }
                        else {
                            g.drawLine(posy1, posx, posy2, posx);
                            g.drawLine(y1, x, y2, x);
                        }
                    }
                }
                g.setStroke(oldStroke);
            }
        }
    }

    PositionDef[] getResults() {
        for (dimension=0; dimension < DIM_COUNT; dimension++) {
            if (movingEdges[dimension] != LayoutRegion.NO_POINT) {
                PositionDef best = bestPositions[dimension];
                if (best == null) { // not found, retry without position restriction
                    snapping = false;
                    best = findBestPosition();
                    bestPositions[dimension] = best;
                }
            }
        }
        return bestPositions;
    }

    // -----
    // finding position in the layout

    /**
     * For the moving/resizing component represented by 'movingSpace' finds the
     * most suitable position the component could be placed to. Works in the
     * dimension defined by 'dimension' field.
     */
    private PositionDef findBestPosition() {
        PositionDef bestNextTo;
        PositionDef bestAligned;
        PositionDef best;

        LayoutInterval layoutRoot = targetContainer.getLayoutRoot(dimension);
        int edges = movingEdges[dimension];

        // [we could probably find the best position directly in scanning, not
        //  separately for each alignment point, choosing the best one additionally here
        //  (only issue is that BASELINE is preferred no matter the distance score)]
        // 1st go through the layout and find position candidates
        checkRootForNextTo(layoutRoot, edges);
        scanLayoutForNextTo(layoutRoot, edges);
        bestNextTo = chooseBestNextTo();

        if (snapping) { // finding aligned position makes sense only if we can snap to it
            checkRootForAligned(layoutRoot, edges);
            scanLayoutForAligned(layoutRoot, edges);
            bestAligned = chooseBestAligned();
        }
        else bestAligned = null;

        // 2nd choose the best position
        if (bestAligned == null) {
            best = bestNextTo;
        }
        else if (bestNextTo == null) {
            best = bestAligned;
        }
        else {
            boolean preferredNextTo = (dimension == HORIZONTAL);
            int nextToDst = smallestDistance(findingsNextTo[dimension]);
            int alignedOrtDst = Math.abs(LayoutRegion.nonOverlapDistance(
                    bestAligned.interval.getCurrentSpace(), movingSpace, dimension ^ 1));
            int alignedDst = getDistanceScore(smallestDistance(findingsAligned[dimension]), alignedOrtDst);
            if (preferredNextTo) {
                best = alignedDst*2 <= nextToDst && nextToDst - alignedDst >= SNAP_DISTANCE/2 ?
                       bestAligned : bestNextTo;
            }
            else {
                best = nextToDst*2 <= alignedDst && alignedDst - nextToDst >= SNAP_DISTANCE/2 ?
                       bestNextTo : bestAligned;
            }
        }
        return best;
    }

    /**
     * Checks distance of the leading/trailing edges of the moving component
     * to the padding positions in the root layout interval. (Distance in root
     * interval is checked differently than scanLayoutForNextTo method does.)
     * @param layoutRoot layout interval to be checked
     * @param alignment determines which edges of the moving space should be
     *        checked - can be LEADING or TRAILING, or ALL_POINTS for both
     */
    private void checkRootForNextTo(LayoutInterval layoutRoot, int alignment) {
        assert alignment == LayoutRegion.ALL_POINTS || alignment == LEADING || alignment == TRAILING;
        LayoutRegion rootSpace = layoutRoot.getCurrentSpace();

        for (int i = LEADING; i <= TRAILING; i++) {
            if (alignment == LayoutRegion.ALL_POINTS || alignment == i) {
                int distance = LayoutRegion.distance(rootSpace, movingSpace,
                                                     dimension, i, i);
                assert distance != LayoutRegion.UNKNOWN;
                if (snapping) {
                    int pad = findPadding(null, movingComponents[0], dimension, i); // [limitation: only one component can be moved]
                    distance += (i == LEADING ? -pad : pad);
                }

                if (!snapping || Math.abs(distance) < SNAP_DISTANCE) {
                    PositionDef bestSoFar = findingsNextTo[dimension][i];
                    assert !bestSoFar.isSet();
                    bestSoFar.interval = layoutRoot;
                    bestSoFar.alignment = i;
                    bestSoFar.distance = distance;
                    bestSoFar.nextTo = true;
                    bestSoFar.snapped = snapping && Math.abs(distance) < SNAP_DISTANCE;
                }
            }
        }
    }

    /**
     * Recursively scans given interval for suitable sub-intervals next to
     * which the moving component could be suitably positioned.
     * @param alignment determines which edges of the moving space should be
     *        checked - can be LEADING or TRAILING, or ALL_POINTS for both
     */
    private int scanLayoutForNextTo(LayoutInterval interval, int alignment) {
        assert alignment == LayoutRegion.ALL_POINTS || alignment == LEADING || alignment == TRAILING;

        int groupOuterAlignment = DEFAULT;

        for (int idx=0, count=interval.getSubIntervalCount(); idx < count; idx++) {
            LayoutInterval sub = interval.getSubInterval(idx);
            if (sub.isEmptySpace()) {
                continue;
            }

            LayoutRegion subSpace = sub.getCurrentSpace();

            boolean orthogonalOverlap = true;
            for (int i=0; i < DIM_COUNT; i++) {
                if (i != dimension
                    && !LayoutRegion.overlap(subSpace, movingSpace, i, 0)) // ORT_DISTANCE
                {   // the space coordinates do not overlap in the other dimension
                    orthogonalOverlap = false;
                    break;
                }
            }
            if (!orthogonalOverlap) {
                continue; // not overlapping orthogonally, can't be "next to"
            }

            int nextToAlignment = DEFAULT;

            if (sub.isComponent()) {
                if (isValidInterval(sub)
                    && (operation != RESIZING || isValidNextToResizing(sub, alignment)))
                {   // sub is a component not moved nor resized
                    nextToAlignment = checkNextToPosition(sub, alignment);
                }
            }
            else if (sub.isSequential()) {
                nextToAlignment = scanLayoutForNextTo(sub, alignment);
            }
            else { // parallel group
                assert sub.isParallel();

                // check if the group is not going to be dissolved (contains moving interval)
                boolean valid = isValidInterval(sub)
                    && (operation != RESIZING || isValidNextToResizing(sub, alignment));
                int subGroupOuterAlign;

                if (canGoInsideForNextTo(sub, valid)) {
                    int align = alignment;
                    for (int i = LEADING; i <= TRAILING; i++) {
                        if (alignment != LayoutRegion.ALL_POINTS && i != alignment) {
                            continue; // skip irrelevant alignment
                        }
                        int insideDst = LayoutRegion.distance(subSpace, movingSpace, dimension, i, i)
                                        * (i == LEADING ? 1 : -1);
                        if (insideDst < -SNAP_DISTANCE) {
                            // out of the subgroup - there is nothing "next to" inside
                            if (align == LayoutRegion.ALL_POINTS)
                                align = i ^ 1;
                            else
                                align = LayoutRegion.NO_POINT;
                        }
                    }
                    if (align != LayoutRegion.NO_POINT) {
                        subGroupOuterAlign = scanLayoutForNextTo(sub, align);
                    }
                    else subGroupOuterAlign = DEFAULT;
                }
                else subGroupOuterAlign = alignment;

                if (valid && subGroupOuterAlign != DEFAULT) {
                    nextToAlignment = checkNextToPosition(sub, subGroupOuterAlign);
                }
            }

            if (interval.isSequential() && nextToAlignment != DEFAULT) {
                // for sequence only first and last intervals can be used for outer alignment
                if (idx > 0 && idx+1 < count) {
                    nextToAlignment = DEFAULT;
                }
                if (idx != 0) {
                    nextToAlignment = nextToAlignment == TRAILING ? DEFAULT : LEADING;
                }
                else if (idx+1 != count) {
                    nextToAlignment = nextToAlignment == LEADING ? DEFAULT : TRAILING;
                }
            }

            if (nextToAlignment != DEFAULT) {
                // check if 'sub' is aligned at the corresponding border of the
                // group - to know if the whole group could not be next to
                if (LayoutInterval.wantResize(sub, false)) {
                    if (nextToAlignment == LayoutRegion.ALL_POINTS) {
                        groupOuterAlignment = LayoutRegion.ALL_POINTS; // both L and T can happen
                    }
                    else if (groupOuterAlignment == DEFAULT) {
                        groupOuterAlignment = nextToAlignment ^ 1; // "next to" side has 'sub' aligned
                    }
                }
                else if ((nextToAlignment == LayoutRegion.ALL_POINTS
                          || (nextToAlignment^1) == sub.getAlignment())
                         && groupOuterAlignment == DEFAULT)
                {   // 'sub' aligned at the "next to" side
                    groupOuterAlignment = sub.getAlignment();
                }
            }
        }

        return groupOuterAlignment;
    }

    private int checkNextToPosition(LayoutInterval sub, int alignment) {
        int nextToAlignment = DEFAULT;
        LayoutRegion subSpace = sub.getCurrentSpace();
        for (int i = LEADING; i <= TRAILING; i++) {
            if (alignment != LayoutRegion.ALL_POINTS && i != alignment)
                continue; // skip irrelevant edge

            boolean validDistance;
            int distance = LayoutRegion.distance(subSpace, movingSpace,
                                                 dimension, i^1, i);
            if (snapping) {
                int pad = findPadding(sub, movingComponents[0], dimension, i);// [limitation: only one component can be moved]
                distance += (i == LEADING ? -pad : pad);
                validDistance = Math.abs(distance) < SNAP_DISTANCE;
            }
            else {
                validDistance = (i == LEADING ? distance > 0 : distance < 0);
            }
            if (validDistance) {
                nextToAlignment = nextToAlignment == DEFAULT ? i : LayoutRegion.ALL_POINTS;

                PositionDef bestSoFar = findingsNextTo[dimension][i];
                if (!bestSoFar.isSet() || compareNextToPosition(sub, distance, bestSoFar) > 0) {
                    bestSoFar.interval = sub;
                    bestSoFar.alignment = i;
                    bestSoFar.distance = distance;
                    bestSoFar.nextTo = true;
                    bestSoFar.snapped = snapping;
                }
            }
        }
        return nextToAlignment;
    }

    /**
     * Compares given "next to" position with the best position found so far.
     * Cares about the visual aspect only, not the logical structure.
     * @return int as result of comparison: 1 - new position is better
     *                                     -1 - old position is better
     *                                      0 - the positions are equal
     */
    private int compareNextToPosition(LayoutInterval newInterval, int newDistance,
                                      PositionDef bestSoFar)
    {
        if (!bestSoFar.isSet())
            return 1; // best not set yet

        LayoutRegion newSpace = newInterval.getCurrentSpace();
        LayoutRegion oldSpace = bestSoFar.interval.getCurrentSpace();
        int oldDistance = Math.abs(bestSoFar.distance);

        // 1st compare the direct distance
        if (newDistance < 0)
            newDistance = -newDistance;
        if (newDistance != oldDistance) {
            return newDistance < oldDistance ? 1 : -1;
        }

        if (newInterval.isParentOf(bestSoFar.interval)) {
            return 1;
        }

        // 2nd compare the orthogonal distance
        int newOrtDst = Math.abs(
                LayoutRegion.minDistance(newSpace, movingSpace, dimension ^ 1));
        int oldOrtDst = Math.abs(
                LayoutRegion.minDistance(oldSpace, movingSpace, dimension ^ 1));
        if (newOrtDst != oldOrtDst) {
            return newOrtDst < oldOrtDst ? 1 : -1;
        }

        return 0;
    }

    private static boolean canGoInsideForNextTo(LayoutInterval subGroup, boolean valid) {
        // can't go inside a group if it has "closed" group alignment (center
        // or baseline) - only can if the group is not valid (i.e. going to be
        // removed so just one not-aligned interval might remain)
        return subGroup.isSequential()
               || (subGroup.isParallel()
                   && (!valid
                       || (subGroup.getGroupAlignment() != CENTER
                           && subGroup.getGroupAlignment() != BASELINE)));
    }

    /**
     * Checks distance of the leading/trailing edges of the moving component
     * to the border positions in the root layout interval. (Distance in root
     * interval is checked differently than scanLayoutForAligned method does.)
     * @param layoutRoot layout interval to be checked
     * @param alignment determines which edges of the moving space should be
     *        checked - can be LEADING or TRAILING, or ALL_POINTS for both
     */
    private void checkRootForAligned(LayoutInterval layoutRoot, int alignment) {
        assert alignment == LayoutRegion.ALL_POINTS || alignment == LEADING || alignment == TRAILING;
        LayoutRegion rootSpace = layoutRoot.getCurrentSpace();

        for (int i = LEADING; i <= TRAILING; i++) {
            if (alignment == LayoutRegion.ALL_POINTS || alignment == i) {
                int distance = LayoutRegion.distance(rootSpace, movingSpace,
                                                     dimension, i, i);
                if (distance != LayoutRegion.UNKNOWN
                    && Math.abs(distance) < SNAP_DISTANCE)
                {   // compare the actual distance with the best one
                    PositionDef bestSoFar = findingsAligned[dimension][i];
                    assert !bestSoFar.isSet();
                    bestSoFar.interval = layoutRoot;
                    bestSoFar.alignment = i;
                    bestSoFar.distance = distance;
                    bestSoFar.nextTo = false;
                    bestSoFar.snapped = true;
                }
            }
        }
    }

    /**
     * Recursively scans given interval for suitable sub-intervals which the
     * moving component could be aligned with.
     * @param alignment determines which edges of the moving space should be
     *        checked - can be LEADING or TRAILING, or ALL_POINTS for both
     */
    private void scanLayoutForAligned(LayoutInterval interval, int alignment) {
        assert alignment == LayoutRegion.ALL_POINTS || alignment == LEADING || alignment == TRAILING;

        Iterator it = interval.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval sub = (LayoutInterval) it.next();
            if (sub.isEmptySpace())
                continue;

            if (sub.isComponent()
                && isValidInterval(sub)
                && (operation != RESIZING || isValidAlignedResizing(sub, alignment)))
            {   // check distance of all alignment points of the moving
                // component to the examined interval space
                for (int i=0; i < LayoutRegion.POINT_COUNT[dimension]; i++) {
                    if (alignment == LayoutRegion.ALL_POINTS || i == alignment) {
                        int indentedDst = getIndentedDistance(sub, i);
                        int directDst = getDirectDistance(sub, i);
                        int distance = Math.abs(indentedDst) < Math.abs(directDst) ?
                                       indentedDst : directDst;
                        if (checkAlignedDistance(distance, sub.getCurrentSpace(), i)) {
                                //distance != LayoutRegion.UNKNOWN && Math.abs(distance) < SNAP_DISTANCE
                            // compare the actual distance with the best one
                            PositionDef bestSoFar = findingsAligned[dimension][i];
                            if (compareAlignedPosition(sub, distance, bestSoFar) >= 0) {
                                // >= 0 means we naturally prefer later components
                                bestSoFar.interval = sub;
                                bestSoFar.alignment = i;
                                bestSoFar.distance = distance;
                                bestSoFar.nextTo = false;
                                bestSoFar.snapped = true;
                            }
                        }
                    }
                }
            }

            if (sub.getSubIntervalCount() > 0
                && LayoutRegion.overlap(sub.getCurrentSpace(), movingSpace, dimension, SNAP_DISTANCE/2))
            {   // the group overlaps with the moving space so it makes sense to dive into it
                scanLayoutForAligned(sub, alignment);
            }
        }
    }

    private int getIndentedDistance(LayoutInterval interval, int alignment) {
        if (dimension == HORIZONTAL && alignment == LEADING) {
            // indented position is limited to horizontal dimension, left alignment
            LayoutRegion examinedSpace = interval.getCurrentSpace();
            int verticalDst = LayoutRegion.distance(examinedSpace, movingSpace,
                                                    VERTICAL, TRAILING, LEADING);
            if (verticalDst >= 0 && verticalDst < 2 * SNAP_DISTANCE) {
                int indent = findIndent(interval.getComponent(), movingComponents[0],
                                        dimension, alignment);
                if (indent > 0) {
                    return LayoutRegion.distance(examinedSpace, movingSpace,
                                                 dimension, alignment, alignment)
                           - indent;
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    private int getDirectDistance(LayoutInterval interval, int alignment) {
        return checkValidAlignment(interval, alignment) ?
               LayoutRegion.distance(interval.getCurrentSpace(), movingSpace,
                                     dimension, alignment, alignment) :
               Integer.MAX_VALUE;
    }

    private boolean checkValidAlignment(LayoutInterval interval, int alignment) {
        int presentAlign = interval.getAlignment();
        // check if the interval is not the last one to remain in parallel group
        if (presentAlign != DEFAULT) {
            boolean lastOne = true;
            Iterator it = interval.getParent().getSubIntervals();
            while (it.hasNext()) {
                LayoutInterval li = (LayoutInterval) it.next();
                if (li != interval && isValidInterval(li)) {
                    lastOne = false;
                    break;
                }
            }
            if (lastOne) {
                presentAlign = DEFAULT;
            }
        }

        if (alignment == LEADING || alignment == TRAILING) {
            // leading and trailing can't align with "closed" alignments
            if (presentAlign == CENTER || presentAlign == BASELINE)
                return false;
        }
        else if (alignment == CENTER) {
            // center alignment is allowed only with already centered intervals
            // (center alignment needs to be set up explicitly first)
            if (presentAlign != CENTER)
                return false;
        }
        else if (alignment == BASELINE) {
            // baseline can't go with other "closed" alignments
            if (presentAlign == CENTER)
                return false;
        }
        return true;
    }

    private boolean checkAlignedDistance(int distance, LayoutRegion examinedSpace, int alignment) {
        if (distance != LayoutRegion.UNKNOWN && Math.abs(distance) < SNAP_DISTANCE) {
            // check if there is nothing in the way along the line of aligned edges
            int x1, x2, y1, y2;
            int indent = movingSpace.positions[dimension][alignment]
                         - examinedSpace.positions[dimension][alignment] - distance;
            if (indent == 0) {
                x1 = examinedSpace.positions[dimension][alignment] - SNAP_DISTANCE/2;
                x2 = examinedSpace.positions[dimension][alignment] + SNAP_DISTANCE/2;
            }
            else {
                x1 = examinedSpace.positions[dimension][alignment];
                x2 = x1 + indent + SNAP_DISTANCE/2;
            }
            y1 = examinedSpace.positions[dimension^1][TRAILING];
            y2 = movingSpace.positions[dimension^1][LEADING];
            if (y1 > y2) {
                y1 = movingSpace.positions[dimension^1][TRAILING];
                y2 = examinedSpace.positions[dimension^1][LEADING];
                if (y1 > y2) { // orthogonally overlaps - so can see it
                    return true;
                }
            }
            return !contentOverlap(targetContainer.getLayoutRoot(dimension),
                                   x1, x2, y1, y2, dimension);
        }
        return false;
    }

    private static boolean contentOverlap(LayoutInterval group, int x1, int x2, int y1, int y2, int dim) {
        int[][] groupPos = group.getCurrentSpace().positions;
        for (int i=0, n=group.getSubIntervalCount(); i < n; i++) {
            LayoutInterval li = group.getSubInterval(i);
            int _x1, _x2, _y1, _y2;
            if (li.isEmptySpace()) {
                if (group.isParallel())
                    continue;
                _x1 = i == 0 ? groupPos[dim][LEADING] :
                               group.getSubInterval(i-1).getCurrentSpace().positions[dim][TRAILING];
                _x2 = i+1 == n ? groupPos[dim][TRAILING] :
                                 group.getSubInterval(i+1).getCurrentSpace().positions[dim][LEADING];
                _y1 = groupPos[dim^1][LEADING];
                _y2 = groupPos[dim^1][TRAILING];
                if (_y1 < y1) {
                    _y2 = _y1;
                }
                else if (_y2 > y2) {
                    _y1 = _y2;
                }
            }
            else {
                int[][] positions = li.getCurrentSpace().positions;
                _x1 = positions[dim][LEADING];
                _x2 = positions[dim][TRAILING];
                _y1 = positions[dim^1][LEADING];
                _y2 = positions[dim^1][TRAILING];
            }
            if (_x1 < x2 && _x2 > x1 && _y1 < y2 && _y2 > y1) { // overlap
                if (li.isComponent()) {
                    return true;
                }
                else if (li.isEmptySpace()) {
                    if (i > 0 // i == 0 indent space is not in the way
                        && (li.getMinimumSize() == NOT_EXPLICITLY_DEFINED || li.getMinimumSize() == USE_PREFERRED_SIZE)
                        && li.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                        && (li.getMaximumSize() == NOT_EXPLICITLY_DEFINED || li.getMaximumSize() == USE_PREFERRED_SIZE))
                        return true; // preferred padding in the way
                    if (_x1 >= x1 && _x2 <= x2)
                        return false; // goes over a gap in a sequence - so no overlap
                }
                else if (li.isGroup() && contentOverlap(li, x1, x2, y1, y2, dim)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return int as result of comparison: 1 - new position is better
     *                                     -1 - old position is better
     *                                      0 - the positions are equal
     */
    private int compareAlignedPosition(LayoutInterval newInterval,
                                       int newDistance,
                                       PositionDef bestSoFar)
    {
        if (!bestSoFar.isSet())
            return 1; // best not set yet

        // compute direct distance
        if (newDistance < 0)
            newDistance = -newDistance;
        int oldDistance = Math.abs(bestSoFar.distance);

        if (newInterval.getParent() == null) {
            return newDistance < oldDistance ? 1 : -1;
        }
        if (bestSoFar.interval.getParent() == null) {
            return oldDistance < newDistance ? -1 : 1;
        }

        // compute orthogonal distance
        LayoutRegion newSpace = newInterval.getCurrentSpace();
        LayoutRegion oldSpace = bestSoFar.interval.getCurrentSpace();
        int newOrtDst = Math.abs(
                LayoutRegion.nonOverlapDistance(newSpace, movingSpace, dimension ^ 1));
        int oldOrtDst = Math.abs(
                LayoutRegion.nonOverlapDistance(oldSpace, movingSpace, dimension ^ 1));

        // compute score
        int newScore = getDistanceScore(newDistance, newOrtDst);
        int oldScore = getDistanceScore(oldDistance, oldOrtDst);

        if (newScore != oldScore) {
            return newScore < oldScore ? 1 : -1;
        }
        return 0;
    }

    private static int getDistanceScore(int directDistance, int ortDistance) {
        // orthogonal distance >= SNAP_DISTANCE is penalized
        return directDistance + ortDistance / SNAP_DISTANCE;
    }

    private PositionDef chooseBestNextTo() {
        PositionDef[] positions = findingsNextTo[dimension];
        PositionDef bestPos = null;
        int bestDst = 0;
        for (int i=0; i < positions.length; i++) {
            PositionDef pos = positions[i];
            if (pos.isSet()) {
                int dst = Math.abs(pos.distance);
                if (bestPos == null || dst < bestDst
                        || (dst == bestDst && moveDirection[dimension] == i)) {
                    bestPos = pos;
                    bestDst = dst;
                }
            }
        }
        return bestPos;
    }

    private PositionDef chooseBestAligned() {
        PositionDef[] positions = findingsAligned[dimension];
        PositionDef bestPos = null;
        for (int i=positions.length-1; i >= 0; i--) {
            PositionDef pos = positions[i];
            if (pos.isSet()) {
                if (i == BASELINE || i == CENTER) {
                    return pos;
                }
                if (bestPos == null) {
                    bestPos = pos;
                }
                else {
                    int c = compareAlignedPosition(pos.interval, pos.distance, bestPos);
                    if (c == 0) {
                        c = compareAlignedDirection(pos, bestPos);
                    }
                    if (c > 0) {// || (c == 0 && moveDirection[dimension] != bestPos.alignment)) {
                        bestPos = pos;
                    }
                }
            }
        }
        return bestPos;
    }

    private int compareAlignedDirection(PositionDef pos1, PositionDef pos2) {
        boolean p1 = isSuitableAlignment(pos1);
        boolean p2 = isSuitableAlignment(pos2);
        if (p1 == p2) {
            p1 = (pos1.alignment == moveDirection[dimension]);
            p2 = (pos2.alignment == moveDirection[dimension]);
            if (p1 == p2)
                return 0;
        }
        return p1 ? 1 : -1;
    }

    private static boolean isSuitableAlignment(PositionDef pos) {
        assert pos.alignment == LEADING || pos.alignment == TRAILING;
        LayoutInterval parParent = LayoutInterval.getFirstParent(pos.interval, PARALLEL);
        return LayoutInterval.isAlignedAtBorder(pos.interval, parParent, pos.alignment)
               || !LayoutInterval.isAlignedAtBorder(pos.interval, parParent, pos.alignment^1);
    }

    private static int smallestDistance(PositionDef[] positions) {
        int bestDst = -1;
        for (int i=0; i < positions.length; i++) {
            PositionDef pos = positions[i];
            if (pos.isSet()) {
                int dst = Math.abs(pos.distance);
                if (bestDst < 0 || dst < bestDst) {
                    bestDst = dst;
                }
            }
        }
        return bestDst;
    }

    /**
     * Checks whether given interval can be used for the moving interval to
     * relate to. Returns false for other moving intervals, or for groups that
     * won't survive removal of the moving intervals from their original
     * positions.
     */
    private boolean isValidInterval(LayoutInterval interval) {
        if (operation == ADDING) {
            return true;
        }

        if (interval.isGroup()) {
            // as the moving intervals are going to be removed first, a valid
            // group must contain at least two other intervals - otherwise it
            // is dissolved before the moving intervals are re-added
            int count = 0;
            Iterator it = interval.getSubIntervals();
            while (it.hasNext()) {
                LayoutInterval li = (LayoutInterval) it.next();
                if ((!li.isEmptySpace() || interval.isSequential())
                    && isValidInterval(li))
                {   // offset gap in parallel group does not count
                    count++;
                    if (count > 1)
                        return true;
                }
            }
            return false;
        }
        else {
            for (int i=0; i < movingComponents.length; i++) {
                if (movingComponents[i].getLayoutInterval(dimension) == interval) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean isValidNextToResizing(LayoutInterval interval, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;
        LayoutInterval resizing = movingComponents[0].getLayoutInterval(dimension);
        LayoutInterval resizingParent = resizing.getParent();
        if (resizingParent.isSequential() && resizingParent.isParentOf(interval)) {
            boolean gapRequired;
            if (resizingParent == interval.getParent()) {
                gapRequired = true;
            }
            else {
                do {
                    if (!LayoutInterval.isBorderInterval(interval, alignment^1, false)) {
                        return false;
                    }
                    interval = interval.getParent();
                }
                while (interval.getParent() != resizingParent);
                gapRequired = false;
            }

            int index = resizingParent.indexOf(resizing) + (alignment == LEADING ? -1 : 1);
            while (index >= 0 && index < resizingParent.getSubIntervalCount()) {
                LayoutInterval li = resizingParent.getSubInterval(index);
                if (li == interval) {
                    return !gapRequired;
                }
                if (!li.isEmptySpace() || !gapRequired) {
                    return false;
                }
                gapRequired = false;
                index += alignment == LEADING ? -1 : 1;
            }
            return false;
        }
        return true; // [should not this be false??]
    }

    private boolean isValidAlignedResizing(LayoutInterval interval, int alignment) {
        int dst = LayoutRegion.distance(movingSpace, interval.getCurrentSpace(),
                                        dimension, alignment^1, alignment);
        if ((alignment == LEADING && dst <= 0) || (alignment == TRAILING && dst >= 0)) {
            // the examined interval position is reachable with positive size of the resizing interval
            LayoutInterval resizing = movingComponents[0].getLayoutInterval(dimension);
            return resizing.getParent().isParallel() || !resizing.getParent().isParentOf(interval);
        }
        else return false;
    }

    /**
     * Finds value of padding between a moving component and given layout
     * interval.
     * @param alignment edge of the component
     */
    static int findPadding(LayoutInterval interval, LayoutComponent comp,
                           int dimension, int alignment)
    {
        return interval == null ? 12 : 6;
        // TBD
    }

    /**
     * @return <= 0 if no indentation is recommended for given component pair
     */
    static int findIndent(LayoutComponent mainComp, LayoutComponent indentedComp,
                          int dimension, int alignemnt)
    {
        return 16;
    }

    // -----
    // innerclasses

    static class PositionDef {
        private int distance = LayoutRegion.UNKNOWN;

        LayoutInterval interval;
        int alignment = LayoutRegion.NO_POINT;
        boolean nextTo;
        boolean snapped;
//        boolean padding;

        private void reset() {
            distance = LayoutRegion.UNKNOWN;
            interval = null;
            alignment = LayoutRegion.NO_POINT;
        }

        private boolean isSet() {
            return interval != null;
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("distance=").append(distance); // NOI18N
            sb.append(",alignment=").append(alignment); // NOI18N
            sb.append(",nextTo=").append(nextTo); // NOI18N
            sb.append(",snapped=").append(snapped); // NOI18N
            return sb.toString();
        }
    }
}
