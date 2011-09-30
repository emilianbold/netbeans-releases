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

import java.awt.Toolkit;
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

    boolean imposeSize;
    boolean optimizeStructure;

    private LayoutModel layoutModel;
    private LayoutOperations operations;

    private LayoutDragger dragger;
    private OriginalPosition[] originalPositions = new OriginalPosition[DIM_COUNT];
//    private IncludeDesc[] originalPositions1 = new IncludeDesc[DIM_COUNT];
//    private IncludeDesc[] originalPositions2 = new IncludeDesc[DIM_COUNT];
    private List[] undoMarks = new List[DIM_COUNT];
    private LayoutRegion originalSpace;
//    private boolean[] originalLPositionsFixed = new boolean[DIM_COUNT];
//    private boolean[] originalTPositionsFixed = new boolean[DIM_COUNT];
    private LayoutDragger.PositionDef[] newPositions = new LayoutDragger.PositionDef[DIM_COUNT];
    private LayoutInterval[][] selectedComponentIntervals = new LayoutInterval[DIM_COUNT][]; // horizontal, vertical // [get rid of]
    private Boolean[] becomeResizing = new Boolean[DIM_COUNT];
    private Collection<LayoutInterval>[] unresizedOnRemove;

    // working context (actual dimension)
    private int dimension;
    private LayoutInterval addingInterval;
    private LayoutRegion addingSpace;
    private boolean solveOverlap;
//    private boolean originalLPosFixed;
//    private boolean originalTPosFixed;
    private LayoutRegion closedSpace;
    private OriginalPosition originalPosition;
    private Object undoCheckMark;

    // params used when searching for the right place (inclusion)
    private int aEdge;
    private LayoutInterval aSnappedParallel;
    private LayoutInterval aSnappedNextTo;
    private PaddingType aPaddingType;

    private static class IncludeDesc {
        LayoutInterval parent;
        int index = -1; // if adding next to
        boolean newSubGroup; // can be true if parent is sequential (parallel subgroup for part of the sequence is to be created)
        LayoutInterval neighbor; // if included in a sequence with single interval (which is not in sequence yet)
        LayoutInterval snappedParallel; // not null if aligning in parallel
        LayoutInterval snappedNextTo; // not null if snapped next to (can but need not be 'neighbor')
        PaddingType paddingType; // type of padding if snapped (next to)
        int alignment; // the edge this object defines (leading or trailing or default)
        boolean fixedPosition; // whether distance from the neighbor is definitely fixed
//        boolean closedPosition;
//        LayoutInterval closedIn;
//        LayoutRegion closedSpace;
        int distance = Integer.MAX_VALUE;
        int ortDistance = Integer.MAX_VALUE;

        boolean snapped() {
            return snappedNextTo != null || snappedParallel != null;
        }
    }

    private static class OriginalPosition {
//        int parentType;
        IncludeDesc desc1, desc2;
        LayoutRegion closedSpace;
        boolean lPosFixed;
        boolean tPosFixed;
        boolean suppressedResizing; // at least one component resizing, in suppressed group
        boolean wholeResizing; // one resizing, or multiple in parallel all resizing
    }

    // -----

//    LayoutFeeder(LayoutOperations operations, LayoutDragger dragger, LayoutInterval[] addingIntervals) {
    LayoutFeeder(LayoutComponent[] selectedComponents, LayoutComponent targetContainer, //LayoutInterval[] addingInts,
                 LayoutOperations operations, LayoutDragger dragger) {
        // [combine into one add method - once there is method for deleting in operations]
        this.layoutModel = operations.getModel();
        this.operations = operations;
        this.dragger = dragger;
//        this.addingIntervals = addingInts;
//        this.originalSpace = addingInts[HORIZONTAL].getCurrentSpace();

        boolean stayInContainer = true;
        for (LayoutComponent c : selectedComponents) {
            if (c.getParent() == null || c.getParent() != targetContainer) {
                stayInContainer = false;
                break;
            }
        }

        for (int dim=0; dim < DIM_COUNT; dim++) {
//            LayoutInterval adding = addingInts[dim];
            LayoutInterval[] compIntervals = new LayoutInterval[selectedComponents.length];
            for (int i=0; i < selectedComponents.length; i++) {
                compIntervals[i] = selectedComponents[i].getLayoutInterval(dim);
            }
            selectedComponentIntervals[dim] = compIntervals;
            List<LayoutInterval> selCompList = Arrays.asList(compIntervals);
            List<LayoutInterval> inCommonParent = getIntervalsInCommonParent(compIntervals);
            OriginalPosition originalPos;
            if (inCommonParent != null && !inCommonParent.isEmpty()) {
                originalPos = new OriginalPosition();
                LayoutInterval first = inCommonParent.get(0);
                LayoutInterval parent = LayoutInterval.getFirstParent(first, PARALLEL);
                if (parent != null
                    && (parent.getGroupAlignment() == CENTER || parent.getGroupAlignment() == BASELINE
                        || (LayoutInterval.isAlignedAtBorder(first, parent, LEADING) && isSignificantGroupEdge(parent, LEADING, false))
                        || (LayoutInterval.isAlignedAtBorder(first, parent, TRAILING) && isSignificantGroupEdge(parent, TRAILING, false)))) {
                    originalPos.closedSpace = new LayoutRegion();
                    originalPos.closedSpace.set(dim, parent.getCurrentSpace());
                }
            } else {
                originalPos = null;
            }
//            if (dragger.isResizing()) {
//                LayoutInterval resInt = compIntervals[0]; // only one component can be resized
            if (dragger.isResizing(dim)) {
                IncludeDesc pos = findOutCurrentPosition(selCompList, inCommonParent,
                        originalPos != null && originalPos.closedSpace != null,
                        dim, dragger.getResizingEdge(dim)^1);
                LayoutDragger.PositionDef newPos = dragger.getPositions()[dim];
                if ((newPos == null || !newPos.snapped) && !pos.snapped()) {
                    pos.alignment = LayoutInterval.getEffectiveAlignment(compIntervals[0]);
                }
//                originalPositions1[dim] = pos;
                originalPos.desc1 = pos;
                newPositions[dim] = newPos;
                becomeResizing[dim] = checkResizing(compIntervals[0], dragger, dim); // make the interval resizing?
                if (layoutModel.isChangeRecording()) {
                    undoMarks[dim] = new ArrayList();
                }
            }
//                originalLPositionsFixed[dim] = isFixedRelativePosition(resInt, LEADING);
//                originalTPositionsFixed[dim] = isFixedRelativePosition(resInt, TRAILING);
//            }
            else { //if (!dragger.isResizing(dim)) { // not resizing in this dimension
                if (!dragger.isResizing()) { // adding or moving
                    newPositions[dim] = dragger.getPositions()[dim];
                }
                if (stayInContainer && originalPos != null) {
                    int alignment = DEFAULT;
                    IncludeDesc pos1 = findOutCurrentPosition(selCompList, inCommonParent,
                                         originalPos.closedSpace != null, dim, alignment);
//                    if (pos1 != null) {
//                    originalPositions1[dim] = pos1;
                    originalPos.desc1 = pos1;
                    alignment = pos1.alignment;
                    if (alignment == LEADING || alignment == TRAILING) {
                        IncludeDesc pos2 = findOutCurrentPosition(selCompList, inCommonParent,
                                             originalPos.closedSpace != null, dim, alignment^1);
                        if (pos2.snapped() || !pos1.snapped()) {
//                            originalPositions2[dim] = pos2;
                            originalPos.desc2 = pos2;
                        } // don't remember second position if not snapped, one is enough
                    }
                    if (layoutModel.isChangeRecording()) {
                        undoMarks[dim] = new ArrayList();
                    }
//                    }
                }
            }
            if (originalPos != null) {
                if (dragger.isResizing() && inCommonParent.size() == 1) { // for resizing operation
                    originalPos.lPosFixed = isFixedRelativePosition(inCommonParent.get(0), LEADING);
                    originalPos.tPosFixed = isFixedRelativePosition(inCommonParent.get(0), TRAILING);
                }
                saveResizingState(inCommonParent, originalPos);
            }
            originalPositions[dim] = originalPos;
        }
//
//        if (selectedComponents.length > 1 && selectedComponents[0].getParent() != null) {
//            return; // [HACK]
//        }
//
//        for (LayoutComponent comp : selectedComponents) {
//            if (comp.getParent() != null) {
//                if (originalSpace == null) {
//                    originalSpace = new LayoutRegion();
//                    originalSpace.set(comp.getCurrentSpace());
//                } else {
//                    originalSpace.expand(comp.getCurrentSpace());
//                }
//                for (int dim=0; dim < DIM_COUNT; dim++) {
//                    LayoutInterval compInt = comp.getLayoutInterval(dim);
//                    if (compInt.getParent() != null) {
//                        List undoList = undoMarks[dim];
//                        if (undoList != null) {
//                            undoList.add(layoutModel.getChangeMark());
//                        }
//                        layoutModel.removeInterval(compInt);
//                        if (undoList != null) {
//                            undoList.add(layoutModel.getChangeMark());
//                        }
//                        if (dragger.isResizing(dim)) {
//                            layoutModel.removeComponentFromLinkSizedGroup(comp, dim);
//                        }
//                    }
//                }
//                if (/*comp.getParent() != null &&*/ comp.getParent() != targetContainer) {
//                    layoutModel.removeComponent(comp, false);
//                }
//            }
//            if (comp.getParent() == null) {
//                layoutModel.addComponent(comp, targetContainer, -1);
//            }
//        }
    }

    void setUndo(Object start, Object end, int dim) {
        if (undoMarks[dim] != null && start != null && !start.equals(end)) {
            undoMarks[dim].add(start);
            undoMarks[dim].add(end);
        }
    }

    void setUp(LayoutRegion origSpace, Collection<LayoutInterval>[] unresizedOnRemove) {
        this.originalSpace = origSpace;
        this.unresizedOnRemove = unresizedOnRemove;
    }

    void add(LayoutInterval[] addingInts) {
        addingSpace = dragger.getMovingSpace();

        int overlapDim = getDimensionSolvingOverlap(newPositions);
        for (int dim=overlapDim, dc=0; dc < DIM_COUNT; dim^=1, dc++) {
            dimension = dim;
            addingInterval = addingInts[dim];
//            addingInterval.setCurrentSpace(addingSpace);
            solveOverlap = overlapDim == dim;
            LayoutInterval root = dragger.getTargetRoots()[dim];
            originalPosition = originalPositions[dim];
            IncludeDesc originalPos1;
            IncludeDesc originalPos2;
            if (originalPosition != null) {
                originalPos1 = correctOriginalInclusion(originalPosition.desc1, root);
                originalPosition.desc1 = originalPos1;
                originalPos2 = correctOriginalInclusion(originalPosition.desc2, root);
                originalPosition.desc2 = originalPos2;
            } else {
                originalPos1 = null;
                originalPos2 = null;
            }
            undoCheckMark = layoutModel.getChangeMark();
            closedSpace = null;
//            boolean closed = originalPos1 != null && originalPos1.closedPosition;

            if (dragger.isResizing()) {
//                originalLPosFixed = originalLPositionsFixed[dim];
//                originalTPosFixed = originalTPositionsFixed[dim];
                if (dragger.isResizing(dim)) {
                    boolean res = Boolean.TRUE.equals(becomeResizing[dim]);
                    layoutModel.setIntervalSize(addingInterval,
//                        becomeResizing[dim] ? NOT_EXPLICITLY_DEFINED : USE_PREFERRED_SIZE,
                        res ? NOT_EXPLICITLY_DEFINED : USE_PREFERRED_SIZE,
                        addingSpace.size(dim),
                        res ? Short.MAX_VALUE : USE_PREFERRED_SIZE);
//                        becomeResizing[dim] ? Short.MAX_VALUE : USE_PREFERRED_SIZE);
                    layoutModel.changeIntervalAttribute(addingInterval, LayoutInterval.ATTR_FLEX_SIZEDEF, true);
                }
//            } else if (originalPosition != null) {
//                suppressMovingResizing();
            }

            LayoutDragger.PositionDef newPos = newPositions[dim];
            if (!dragger.isResizing(dim) && newPos != null
                    && (newPos.alignment == CENTER || newPos.alignment == BASELINE)) {
                // simplified adding/moving to closed group
                if (originalPos1 == null || originalPos1.alignment != newPos.alignment
                        || !equalSnap(originalPos1.snappedParallel, newPos.interval, newPos.alignment)
                        || !restoreDimension()) { // if not staying the same...
                    aEdge = newPos.alignment;
                    aSnappedParallel = newPos.interval;
                    addSimplyAligned();
                }
                continue;
            }
            if (dragger.isResizing() && originalPos1 != null
                    && (originalPos1.alignment == CENTER || originalPos1.alignment == BASELINE)) {
                // simplified resizing in closed group
                if (/*dragger.isResizing(dim) || */!restoreDimension()) { // if not staying the same...
                    aEdge = originalPos1.alignment;
                    aSnappedParallel = originalPos1.snappedParallel;
                    addSimplyAligned();
                }
                continue;
            }

            // prepare task for searching the position
            IncludeDesc inclusion1 = null;
            IncludeDesc inclusion2 = null;

            List<IncludeDesc> inclusions = new LinkedList<IncludeDesc>();
            boolean preserveOriginal;
            boolean originalSignificant = dragger.isResizing(dimension) && originalPos1 != null
                    && (originalPos1.snapped()
                        || newPos == null || newPos.nextTo || newPos.interval == null // no new snap in parallel
                        || originalPos1.neighbor != null // something is next to
                        || (originalPos1.parent.isSequential() // something is next to
                            && (!originalPos1.newSubGroup
                                || !originalPos1.parent.isParentOf(newPos.interval))));

            if (dragger.isResizing(dim^1)
                    && (originalSignificant || (newPos == null && originalPos1 != null))) {
                // resizing in the other dimension, renew the original position
                aEdge = originalPos1.alignment;
                aSnappedParallel = originalPos1.snappedParallel;
                aSnappedNextTo = originalPos1.snappedNextTo;
                aPaddingType = originalPos1.paddingType;
                preserveOriginal = false;
            } else if (newPos != null) {
                // snapped in dragger, always find the position
                aEdge = newPos.alignment;
                aSnappedParallel = newPos.snapped && !newPos.nextTo ? newPos.interval : null;
                aSnappedNextTo = newPos.snapped && newPos.nextTo ? newPos.interval : null;
                aPaddingType = newPos.paddingType;
                preserveOriginal = originalSignificant;
            } else if (dragger.isResizing(dim) && originalPos1 != null) {
                // resizing only in this dimension and without snap, check for
                // possible growing in parallel with part of its own parent sequence
                aEdge = originalPos1.alignment;
                aSnappedParallel = originalPos1.snappedParallel;
                aSnappedNextTo = originalPos1.snappedNextTo;
                aPaddingType = originalPos1.paddingType;
                preserveOriginal = true;
            } else { // otherwise plain moving without snap
                aEdge = DEFAULT;
                aSnappedParallel = aSnappedNextTo = null;
                aPaddingType = null;
                preserveOriginal = false;
            }

            analyzeParallel(root, inclusions);

            if (inclusions.isEmpty()) { // no suitable inclusion found (nothing in sequence)
                assert aSnappedParallel != null;
                if (dragger.isResizing() && originalPos1 != null && originalPos1.alignment == aEdge) {
                    inclusions.add(originalPos1);
                } else {
                    addAligningInclusion(inclusions);
                }
            } else {
                IncludeDesc preferred = addAligningInclusion(inclusions); // make sure it is there...
                if (inclusions.size() > 1) {
                    if ((preferred == null || (preserveOriginal && originalPos1.alignment == aEdge))
                            && dragger.isResizing()) {
                        preferred = originalPos1;
                    }
                    mergeParallelInclusions(inclusions, preferred, preserveOriginal);
                    assert inclusions.size() == 1;
                }
            }

            IncludeDesc found = inclusions.get(0);
            inclusions.clear();
            if (preserveOriginal) { // i.e. resizing in this dimension
                inclusion1 = originalPos1;
                if (found != originalPos1) {
                    if (newPos != null) {
                        inclusion2 = found;
                    }
                    LayoutInterval foundP = found.parent;
                    LayoutInterval origP = originalPos1.parent;
                    if ((foundP == origP && found.newSubGroup)
                          || (origP.isSequential() && foundP.isParallel() && foundP.isParentOf(origP)
                              && LayoutUtils.contentOverlap(addingInterval, origP, dim))) {
                        inclusion1.newSubGroup = true;
                    }
                }
            } else {
                inclusion1 = found;

                boolean secondRound;
                if (newPos != null) {
                    if (dragger.isResizing(dim^1) && originalSignificant) {
                        // find inclusion based on the position from dragger
                        // (first round was for renewing the original position)
                        assert dragger.isResizing(dim);
                        aEdge = newPos.alignment;
                        aSnappedParallel = !newPos.nextTo ? newPos.interval : null;
                        aSnappedNextTo = newPos.snapped && newPos.nextTo ? newPos.interval : null;
                        aPaddingType = newPos.paddingType;
                        secondRound = true;
                    } else if (inclusion1.parent.isSequential()) {
                        // compute inclusion for the other than snapped edge,
                        // it might want to go into a neighbor parallel group
                        assert !dragger.isResizing();
                        aEdge = newPos.alignment ^ 1;
                        aSnappedParallel = null;
                        aSnappedNextTo = null;
                        aPaddingType = null;
                        secondRound = true;
                    } else {
                        secondRound = false;
                    }
                } else if (dragger.isResizing(dim^1) && originalPos2 != null) {
                     // renew the second original position
                    assert !dragger.isResizing(dim);
                    secondRound = true;
                    aEdge = originalPos2.alignment;
                    aSnappedParallel = originalPos2.snappedParallel;
                    aSnappedNextTo = originalPos2.snappedNextTo;
                    aPaddingType = originalPos2.paddingType;
                } else {
                    secondRound = false;
                }

                if (secondRound) {
                    // second round searching
                    analyzeParallel(root, inclusions);

                    if (inclusions.isEmpty()) { // no suitable inclusion found
                        assert aSnappedParallel != null;
                        if (originalPos2 != null && originalPos2.alignment == aEdge) {
                            inclusions.add(originalPos2);
                        } else {
                            addAligningInclusion(inclusions);
                        }
                    } else {
                        IncludeDesc preferred = addAligningInclusion(inclusions);
                        if (inclusions.size() > 1) {
                            if (preferred == null) { // [ && dragger.isResizing() ??? ]
                                preferred = originalPos2 != null ? originalPos2 : originalPos1;
                            }
                            mergeParallelInclusions(inclusions, preferred, false);
                            assert inclusions.size() == 1;
                        }
                    }
                    inclusion2 = inclusions.get(0);
                    inclusions.clear();

                    if (!dragger.isResizing() && newPos != null
                            && !inclusion1.parent.isParentOf(inclusion2.parent)) {
                        // secondary inclusion for the other than snapped edge not relevant
                        inclusion2 = null;
                    }
                }
            }

//            preserveOriginalClosedPosition(inclusion1, originalPos1);
            if (!preferClosedPosition(inclusion1)) {
                cancelResizingOfMovingComponent();
            }

            if (!mergeSequentialInclusions(inclusion1, inclusion2)) {
                inclusion2 = null;
            }

            // now may detect more cases when the component needs to be set as resizing
            if (dragger.isResizing(dim)) {
                checkResizing2(inclusion1, inclusion2);
            }

            if (!unchangeDimension(inclusion1, inclusion2)) { // if not staying the same...
                addInterval(inclusion1, inclusion2,
                        !dragger.isResizing() && !LayoutUtils.getComponentIterator(root).hasNext());
            }
        }
    }

    private static IncludeDesc findOutCurrentPosition(List<LayoutInterval> components,
              List<LayoutInterval> inParent, boolean inClosedSpace,
              int dimension, int alignment) {
//        List<LayoutInterval> inParent = getIntervalsInCommonParent(components);
//        if (inParent == null || inParent.isEmpty()) {
//            return null;
//        }
        LayoutInterval firstParent = inParent.get(0).getParent();
        LayoutInterval parent = firstParent;
        int remainingCount = LayoutUtils.getRemainingCount(parent, components, true);

        IncludeDesc iDesc = new IncludeDesc();

        if (parent.isSequential() && remainingCount > 0) {
            if (alignment < 0) {
                alignment = LEADING;
            }
            if (remainingCount == 1) { // the sequence may not survive when the interval is removed
                // (if it survives, the inclusion gets corrected by 'correctNeighborInSequence' method)
                iDesc.parent = parent.getParent();
                int index = 0;
                for (int i=parent.getSubIntervalCount()-1; i >= 0; i--) {
                    LayoutInterval li = parent.getSubInterval(i);
                    if (!li.isEmptySpace()) {
                        if (inParent.contains(li)) {
                            index = i;
                        } else  {
                            iDesc.neighbor = li; // next to a single interval in parallel group
                            iDesc.index = index;
                            break;
                        }
                    }
                }
            } else { // simply goes to the sequence
                iDesc.parent = parent;
                iDesc.index = parent.indexOf(inParent.get(0));
                // multiple components can individually be in parallel with the
                // sequence (even though their first common parent is the sequence)
                for (LayoutInterval li : components) {
                    if (li.getParent() != parent) {
                        iDesc.newSubGroup = true;
                        break;
                    }
                }
            }
        } else { // parallel parent
            int currentAlign;
            if (parent.isSequential()) {
                currentAlign = parent.getAlignment();
                parent = parent.getParent(); // alone in sequence, take parent
                remainingCount = LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true) - 1;
//                if (alignment < 0) {
//                    alignment = LEADING;
//                }
            } else {
                currentAlign = inParent.get(0).getAlignment();
            }
            if (alignment < 0 || (currentAlign != LEADING && currentAlign != TRAILING)) {
                alignment = currentAlign;
            }

            // placed parallel in a closed position?
//            if (LayoutInterval.isClosedGroup(parent, alignment^1)) {
//            if (alignment == CENTER || alignment == BASELINE
//                    || isSignificantGroupEdge(parent, alignment, false)
//                    || isSignificantGroupEdge(parent, alignment^1, false)) {
//                iDesc.closedSpace = new LayoutRegion();
//                iDesc.closedSpace.set(dimension, parent.getCurrentSpace());
//            }

            if (remainingCount <= 1 && parent.getParent() != null) {
                // parallel group will not survive when the interval is removed
                LayoutInterval subGroup = parent;
                parent = parent.getParent();
                if (parent.isSequential()) {
                    boolean inSequence = inClosedSpace; // iDesc.closedSpace != null;
                    if (!inSequence) {
                        LayoutRegion selSpace = new LayoutRegion();
                        for (LayoutInterval li : components) {
                            selSpace.expand(li.getCurrentSpace());
                        }
                        for (Iterator it=parent.getSubIntervals(); it.hasNext(); ) {
                            LayoutInterval li = (LayoutInterval) it.next();
                            if (!li.isEmptySpace() && li != subGroup
                                && LayoutRegion.overlap(selSpace, li.getCurrentSpace(), dimension^1, 0))
                            {   // orthogonal overlap - need to stay within the sequence
                                inSequence = true;
                                break;
                            }
                        }
                    }
                    if (inSequence) { // parallel with part of the sequence
                        iDesc.newSubGroup = true;
                        iDesc.index = parent.indexOf(subGroup);
                    } else { // parallel with whole sequence
                        parent = parent.getParent();
                    }
                }
            } // else parallel group will survive
            iDesc.parent = parent;
        }

        // check for parallel aligning (sets iDesc.snappedParallel)
        alignment = findAlignedInterval(components, inParent, dimension, alignment, iDesc);

        LayoutInterval borderInterval = null; // representative of multi-selection
        if (alignment == LEADING || alignment == TRAILING) {
            if (firstParent.isSequential()) {
                borderInterval = inParent.get(alignment == LEADING ? 0 : inParent.size()-1);
            } else {
                for (LayoutInterval li : inParent) {
                    if (LayoutInterval.isAlignedAtBorder(li, alignment)) {
                        borderInterval = li;
                        break;
                    }
                    if (borderInterval == null
                            && LayoutInterval.isPlacedAtBorder(li, firstParent, dimension, alignment)) {
                        borderInterval = li; // if not aligned
                    }
                }
                if (borderInterval == null) {
                    borderInterval = inParent.get(0);
                }
            }

            iDesc.fixedPosition = isFixedRelativePosition(borderInterval, alignment);
        }

//        if (iDesc.snappedParallel != null) {
//            LayoutInterval group = iDesc.snappedParallel;
//            if (!group.isParallel()) {
//                group = group.getParent();
//            }
//            if (group.isParallel() && LayoutInterval.isClosedGroup(group, alignment ^ 1)) {
//                iDesc.closedPosition = true;
//            }
//        }

        // check for next to aligning
        if (iDesc.snappedParallel == null && (alignment == LEADING || alignment == TRAILING)) {
            LayoutInterval gap = LayoutInterval.getNeighbor(borderInterval, alignment, false, true, false);
            if (gap != null && LayoutInterval.isFixedDefaultPadding(gap)) {
                LayoutInterval prev = LayoutInterval.getDirectNeighbor(gap, alignment^1, true);
                if (prev == borderInterval || LayoutInterval.isPlacedAtBorder(borderInterval, prev, dimension, alignment)) {
                    LayoutInterval next = LayoutInterval.getNeighbor(gap, alignment, true, true, false);
                    if (next != null) {
                        if (next.getParent() == gap.getParent()
                            || next.getCurrentSpace().positions[dimension][alignment^1]
                               == gap.getParent().getCurrentSpace().positions[dimension][alignment])
                        {   // the next interval is really at preferred distance
                            iDesc.snappedNextTo = next;
                            iDesc.paddingType = gap.getPaddingType();
                        }
                    } else { // likely next to the root group border
                        next = LayoutInterval.getRoot(firstParent);
                        if (LayoutInterval.isPlacedAtBorder(gap.getParent(), next, dimension, alignment)) {
                            iDesc.snappedNextTo = next;
                        }
                    }
                }
            }
        }

        iDesc.alignment = alignment;
        return iDesc;
    }

    /**
     * For selected components finds the intervals that represent them in a
     * common parent. If a representing interval contains other than selected
     * components, then the multiple components cannot be represented by a set
     * of intervals under one parent (selection does not make a coherent block).
     * @param compIntervals
     * @return list of intervals under one parent representing the components,
     *         or empty list if the components can't be represented
     */
    private static List<LayoutInterval> getIntervalsInCommonParent(LayoutInterval[] compIntervals) {
        List<LayoutInterval> inParent = null;
        if (compIntervals.length == 1) {
            inParent = Collections.singletonList(compIntervals[0]);
        } else {
            LayoutInterval commonParent = LayoutInterval.getCommonParent(compIntervals);
            Set<LayoutInterval> comps = new HashSet<LayoutInterval>();
            Collections.addAll(comps, compIntervals);
            if (commonParent != null) {
                boolean found = false;
                boolean previous = false;
                boolean all = true;
                for (int i=0; i < commonParent.getSubIntervalCount(); i++) {
                    LayoutInterval li = commonParent.getSubInterval(i);
                    if (!li.isEmptySpace()) {
                        int sel = isInSelectedComponents(li, compIntervals, true);
                        if (sel > 0) { // 'li' contains selected components and nothing else
                            if (!found) {
                                found = true;
                                inParent = new ArrayList<LayoutInterval>();
                            } else if (!previous && commonParent.isSequential()) {
                                inParent.clear();
                                break; // not a continuous sub-sequence
                            }
                            // [TODO alignment in parallel matters?]
    //                        if (commonParent.isParallel()) {
    //                            int align = LayoutInterval.wantResize(li) ? LayoutRegion.ALL_POINTS : li.getAlignment();
    //                            if (commonAlign != LEADING && commonAlign != TRAILING && commonAlign != CENTER && commonAlign != BASELINE) {
    //                                commonAlign = align;
    //                            } else if (commonAlign != align && align != LayoutRegion.ALL_POINTS) {
    //                                inParent.clear();
    //                                break; // alignment does not much
    //                            }
    //                        }
                            previous = true;
                            inParent.add(li);
                        } else if (sel < 0) { // 'li' does not contain any selected component
                            previous = false;
                            all = false;
                        } else { // 'li' contains both, not continuous
                            if (inParent != null) {
                                inParent.clear();
                            }
                            break;
                        }
                    }
                }
                if (commonParent.isParallel() && commonParent.getParent() != null && found && all) {
                    // entire parallel group selected, use it as a whole
                    inParent.clear();
                    inParent.add(commonParent);
                    commonParent = commonParent.getParent();
                }
            }
        }
        if (inParent == null) {
            inParent = Collections.emptyList();
        }
        return inParent;
    }

    private static int isInSelectedComponents(LayoutInterval interval,
                         LayoutInterval[] selectedComps, boolean firstLevel) {
        if (interval.isComponent()) {
            for (LayoutInterval li : selectedComps) {
                if (li == interval) {
                    return 1;
                }
            }
        } else if (interval.isGroup()) { // in subgroup all must be in selected
            boolean oneEnough = firstLevel && interval.isParallel();
            boolean inSelected = false;
            boolean notInSelected = false;
            for (int i=0; i < interval.getSubIntervalCount(); i++) {
                LayoutInterval li = interval.getSubInterval(i);
                if (!li.isEmptySpace()) {
                    int sel = isInSelectedComponents(li, selectedComps, false);
                    if (sel > 0) { // all from 'li' in selected
                        if (oneEnough) {
                            return 1;
                        }
                        inSelected = true;
                    } else if (sel < 0) { // nothing from 'li' in selected
                        notInSelected = true;
                    } else { // partially selected, not continuous
                        return 0;
                    }
                }
            }
            if (inSelected && !notInSelected) { // contains continuous block of selected
                return 1;
            } else if (!inSelected) { // does not contain any selected
                return -1;
            } else { // contains some selected, but not continuous
                return 0;
            }
        }
        return -1;
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

    private static int findAlignedInterval(List<LayoutInterval> components,
                                           List<LayoutInterval> inParent,
                                           int dimension, int alignment,
                                           IncludeDesc iDesc) {
        LayoutInterval interval = inParent.get(0);
        LayoutInterval parent = interval.getParent(); // all selected intervals have the same parent
        if (parent.isSequential() && alignment == TRAILING) {
            interval = inParent.get(inParent.size()-1);
        }

        // need to force parallel alignment in case of indented position whose
        // parent parallel group won't survive removing of the resizing component
        // (the resizing interval will target a higher group where the resulting
        // indent gap might be different)
        boolean indent = false;
        if ((alignment == LEADING || alignment == TRAILING)
            && parent.isSequential()
            && LayoutInterval.getCount(parent, -1, true) == 1)
        {   // alone in sequence
            LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
            if (neighbor != null && neighbor.isEmptySpace() && !LayoutInterval.canResize(neighbor)
                && LayoutInterval.getCount(parent.getParent(), LayoutRegion.ALL_POINTS, true) == 2
                && !LayoutInterval.isAlignedAtBorder(neighbor, LayoutInterval.getRoot(parent), alignment))
            {   // only one sibling in parallel group - candidate for aligned interval
                indent = true;
            }
        }

        LayoutInterval closedAlignRep = null; // multiple components on baseline or center
        if (components.size() > 1 && !indent && (alignment == LEADING || alignment == TRAILING)) {
            // can consider the selected components actually aligned on BASELINE
            // or CENTER even if they don't form a single parallel group
            for (LayoutInterval comp : components) {
                if (comp.getAlignment() == CENTER || comp.getAlignment() == BASELINE) {
                    for (Iterator<LayoutInterval> it=comp.getParent().getSubIntervals(); it.hasNext(); ) {
                        LayoutInterval sub = it.next();
                        if (sub != comp && !components.contains(sub)) {
                            closedAlignRep = comp;
                            break;
                        }
                    }
                    if (closedAlignRep != null) {
                        break;
                    }
                }
            }
        }

        int overallAlignment = alignment;
        if (closedAlignRep != null) {
            alignment = closedAlignRep.getAlignment();
            interval = closedAlignRep;
            parent = closedAlignRep.getParent();
        }
        LayoutInterval alignedInterval = null;
        LayoutInterval directParent = parent;
        do {
            parent = LayoutInterval.getFirstParent(interval, PARALLEL);

            if (!indent) {
                boolean aligned;
                if (alignment == LEADING || alignment == TRAILING) {
                    if (parent != directParent) {
                        aligned = LayoutInterval.isAlignedAtBorder(interval, parent, alignment);
                    } else {
                        aligned = false;
                        for (LayoutInterval li : inParent) {
                            li = LayoutUtils.getOutermostComponent(li, dimension, alignment);
                            if (LayoutInterval.isAlignedAtBorder(li, alignment)) {
                                aligned = true; // some is aligned, ok
                                break;
                            }
                        }
                    }
                } else { // CENTER or BASELINE
                    aligned = interval.getParent() == parent && interval.getAlignment() == alignment;
                }
                if (!aligned) {
                    break;
                }
                if (parent.getParent() == null) {
                    alignedInterval = parent; // aligned with root group
                    break;
                }
            }

            for (Iterator it=parent.getSubIntervals(); it.hasNext(); ) {
                LayoutInterval sub = (LayoutInterval) it.next();
                if (!sub.isEmptySpace() && !components.contains(sub)
                        && ((parent != directParent && sub != interval && !sub.isParentOf(interval))
                            || (parent == directParent && !inParent.contains(sub)))) {
                    // can align with sub (sub is not in selected)
                    if (alignment == LEADING || alignment == TRAILING) {
                        LayoutInterval li = LayoutUtils.getOutermostComponent(sub, dimension, alignment);
                        if (LayoutInterval.isAlignedAtBorder(li, parent, alignment)
                            || LayoutInterval.isPlacedAtBorder(li, parent, dimension, alignment))
                        {   // here we have an aligned component
                            alignedInterval = li;
                            break;
                        }
                    } else {
                        alignedInterval = sub;
                        break;
                    }
                }
            }

            interval = parent;
        } while (!indent && alignedInterval == null);

        if (alignedInterval != null) {
            overallAlignment = alignment; // confirming that closedAlignRep aligns with something
            if (alignedInterval.getParent() == parent) {
                int remainingCount = LayoutUtils.getRemainingCount(parent, components, true);
                if (remainingCount >= 2) { // if parent is going to survive, it's prefered to align with it
                    alignedInterval = parent;
                }
            }
            iDesc.snappedParallel = alignedInterval;
        }
        return overallAlignment;
    }

    private static void saveResizingState(List<LayoutInterval> inParent, OriginalPosition pos) {
        LayoutInterval parent = inParent.get(0).getParent();
        boolean wholeResizing = inParent.size() == 1 || parent.isParallel();
        boolean suppResChecked = false;
        boolean suppressedResizing = false;
        for (LayoutInterval li : inParent) {
            if (LayoutInterval.wantResize(li)) {
                if (!suppResChecked) {
                    suppressedResizing = !LayoutInterval.canResizeInLayout(li);
                    suppResChecked = true;
                }
            } else {
                wholeResizing = false;
            }
        }
        pos.wholeResizing = wholeResizing;
        pos.suppressedResizing = suppressedResizing;
    }

    /**
     * Determines whether addingInterval that is being resized should be set to
     * auto-resizing. The analysis is based on current position (before the
     * interval is removed for new placement) and new position from dragger.
     * There's also checkResizing2 called later for situations this method can't detect.
     * @return true if the interval should be made resizing
     */
    private static Boolean checkResizing(LayoutInterval interval, LayoutDragger dragger, int dim) {
        int resizingEdge = dragger.getResizingEdge(dim);
        int fixedEdge = resizingEdge^1;
        LayoutDragger.PositionDef newPos = dragger.getPositions()[dim]; //newPositions[dimension];
        Boolean resizing = null;

        if (newPos != null && newPos.snapped && newPos.interval != null) {
            int align1, align2;
            LayoutInterval parent;
            if (newPos.interval.isParentOf(interval)) {
                parent = newPos.interval;
                align1 = LayoutInterval.getEffectiveAlignmentInParent(interval, newPos.interval, fixedEdge);
                align2 = resizingEdge;
            }
            else {
                parent = LayoutInterval.getCommonParent(interval, newPos.interval);
                align1 = determineFixedEdgeAlignemnt(interval, parent, fixedEdge, dim); //LayoutInterval.getEffectiveAlignmentInParent(interval, parent, fixedEdge);
                align2 = align1 == resizingEdge ?
                        resizingEdge : // whole component tied to resizing edge
                        LayoutInterval.getEffectiveAlignmentInParent(newPos.interval, parent, newPos.nextTo ? fixedEdge : resizingEdge);
            }
            if (align1 == resizingEdge && LayoutInterval.wantResize(interval.getParent())) {
                resizing = Boolean.FALSE;
            } else if (align1 != align2
                    && (align1 == LEADING || align1 == TRAILING) && (align2 == LEADING || align2 == TRAILING)
                    && (parent.getParent() == null || LayoutInterval.wantResize(parent))) {
                resizing = Boolean.TRUE;
            }
        }
        // [maybe we should consider also potential resizability of the component,
        //  not only on resizing operation - the condition should be:
        //  isComponentResizable(interval.getComponent(), dimension)  ]
        return resizing;
    }

    private static int determineFixedEdgeAlignemnt(LayoutInterval resizingInterval, LayoutInterval parent, int fixedEdge, int dimension) {
        if (parent.isSequential() && LayoutInterval.wantResize(resizingInterval)) {
            LayoutInterval parParent = LayoutInterval.getFirstParent(resizingInterval, PARALLEL);
            if (parent.isParentOf(parParent)
                    && LayoutInterval.isPlacedAtBorder(resizingInterval, parParent, dimension, fixedEdge)
                    && LayoutUtils.anythingAtGroupEdge(parParent, resizingInterval, dimension, fixedEdge)) {
                return fixedEdge;
            }
        }
        return LayoutInterval.getEffectiveAlignmentInParent(resizingInterval, parent, fixedEdge);
    }

    /**
     * After the moving/resizing interval is temporarily removed, the related
     * optimizations may alter the parent group or neighbor recorded in the
     * original position. This methods tries to correct that.
     */
    private static IncludeDesc correctOriginalInclusion(IncludeDesc iDesc, LayoutInterval targetRoot) {
        if (iDesc != null) {
            if (iDesc.parent.getParent() == null && iDesc.parent.getSubIntervalCount() == 0
                    && iDesc.parent != targetRoot) {
                // Original parallel parent did not survive - probably because of
                // border gaps optimization after removing the resizing interval.
                LayoutInterval interval;
                if (iDesc.snappedParallel != null) {
                    interval = iDesc.snappedParallel;
                } else if (iDesc.snappedNextTo != null) {
                    interval = iDesc.snappedNextTo;
                } else if (iDesc.neighbor != null) {
                    interval = iDesc.neighbor;
                } else {
                    return null; // bad luck, we don't know where it is
                }
                if (interval != null) {
                    iDesc.parent = interval.getParent() != null
                            ? LayoutInterval.getFirstParent(interval, PARALLEL) : interval;
                }
            }
            if (iDesc.neighbor != null) {
                if (iDesc.neighbor.isParallel() && iDesc.neighbor.getParent() == null) {
                    // As expected, the sequence where 'neighbor' was originally
                    // placed with the moving interval did not survive, but
                    // moreover the content of 'neigbor' itself (as a parallel
                    // group) was dissolved into a parallel parent.
                    iDesc.neighbor = null;
                } else if (iDesc.neighbor.getParent().isSequential()) {
                    // In contrary, here the sequential parent of 'neighbor'
                    // unexpectedly survived removal of the moving interval (some
                    // gaps stayed around it), so we should use the sequence directly.
                    iDesc.parent = iDesc.neighbor.getParent();
                    iDesc.neighbor = null;
                    if (iDesc.index > iDesc.parent.getSubIntervalCount()) {
                        iDesc.index = iDesc.parent.getSubIntervalCount();
                    }
                }
            }
        }
        return iDesc;
    }

    /**
     * Recognize situation when the new position in given dimension looks same
     * as the original one and so would be better not to change it. In such case
     * the original state in this dimension is restored.
     * @param ndesc1 description of new position
     * @param ndesc2 description of original position
     * @return true if the actual dimension should stay in original state that is
     *         also successfully restored
     */
    private boolean unchangeDimension(IncludeDesc ndesc1, IncludeDesc ndesc2) {
        if (undoMarks[dimension] == null || !layoutModel.getChangeMark().equals(undoCheckMark)) {
            return false; // also when a new components is being added
        }
        if (dragger.isResizing(dimension)) {
            return false;
        }
        IncludeDesc odesc1 = originalPosition != null ? originalPosition.desc1 : null; //originalPositions1[dimension];
        if (odesc1 == null || ndesc1 == null || odesc1.parent.getSubIntervalCount() == 0) {
            return false;
        }
        IncludeDesc odesc2 = originalPosition != null ? originalPosition.desc2 : null; //originalPositions2[dimension];
        if (ndesc2 != null && odesc2 == null) {
            return false;
        }

        if ((ndesc1.alignment == LEADING || ndesc1.alignment == TRAILING)
                && ndesc1.alignment != odesc1.alignment
                && odesc2 != null
                && ndesc1.alignment == odesc2.alignment) {
            IncludeDesc tmp = odesc1;
            odesc1 = odesc2;
            odesc2 = tmp;
        }

        int align = odesc1.alignment;
        if (ndesc1.alignment != DEFAULT && ndesc1.alignment != odesc1.alignment) {
            align = ndesc1.alignment;
        }
        boolean multi = selectedComponentIntervals[dimension].length > 1;
        boolean originalClosedAlignment = multi && (odesc1.alignment == CENTER || odesc1.alignment == BASELINE);

        int dst = LayoutRegion.distance(originalSpace, addingSpace, dimension, CENTER, CENTER);
        if (dst >= -5 && dst <= 5 && originalClosedAlignment) {
            dst = 0; // hack for multiple components separate on baseline
        }
        if (dst != 0 && !equalNextTo(ndesc1, odesc1, align)) {
            return false;
        }
        if (!originalClosedAlignment && align != odesc1.alignment) {
            return plainAlignmentChange(ndesc1, odesc1);
        }

        boolean equalToOriginal = false;
        LayoutInterval np = ndesc1.parent; // new parent
        LayoutInterval op = odesc1.parent; // old parent
        if (np != op) {
            if (np.isParentOf(op)) { // moving to "wider" position
                if (np.isParallel()) {
                    if (ndesc1.neighbor == null) {
                        if (op.isParallel()) {
                            if (LayoutInterval.isClosedGroup(op, align^1)) {
                                equalToOriginal = true;
                            } else {
                                LayoutInterval neighbor = LayoutInterval.getNeighbor(op, align^1, true, true, false);
                                if (neighbor == null || !np.isParentOf(neighbor)) {
                                    equalToOriginal = true; // no neighbor that would make the wider position different
                                }
                            }
                        } else if (multi && odesc1.newSubGroup) {
                            equalToOriginal = true;
                        }
                    }
                } else if (op.isParallel()
                        && (equalNextTo(ndesc1, odesc1, align)
                           || equalSnap(ndesc1.snappedParallel, odesc1.snappedParallel, align))) {
                    equalToOriginal = true;
                }
            }
        } else if ((np.isParallel() && ndesc1.neighbor == odesc1.neighbor)
                || (np.isSequential() && ndesc1.newSubGroup == odesc1.newSubGroup)) {
            equalToOriginal = true;
        }
        if (equalToOriginal) {
            return restoreDimension();
        }
        return false;
    }

    private boolean restoreDimension() {
        boolean undone = false;
        List undoList = undoMarks[dimension];
        if (undoList != null) {
            for (int n=undoList.size()-1; n > 0; n-=2) {
                Object startMark = undoList.get(n-1);
                Object endMark = undoList.get(n);
                undone |= layoutModel.revert(startMark, endMark);
            }
            undoList.clear();
//            if (undone) {
//                Toolkit.getDefaultToolkit().beep();
//            }
        }
        return undone;
    }

    private static boolean equalNextTo(IncludeDesc iDesc1, IncludeDesc iDesc2, int alignment) {
        if (iDesc1.parent.isSequential() && iDesc1.snappedNextTo != null) {
            if (iDesc1.parent == iDesc2.parent && iDesc1.snappedNextTo == iDesc2.snappedNextTo) {
                return iDesc1.paddingType == iDesc2.paddingType;
            } else if (iDesc2.snappedParallel != null
                    && (iDesc1.parent == iDesc2.parent
                        || (iDesc1.parent.isParentOf(iDesc2.parent) && iDesc1.newSubGroup && (iDesc2.parent.isParallel() || iDesc2.newSubGroup)))) {
                // snap next to is equal to align in parallel with something that is next to the same thing
                LayoutInterval neighbor = LayoutInterval.getNeighbor(iDesc2.snappedParallel, alignment, false, true, true);
                if (neighbor != null && neighbor.isEmptySpace() // && neighbor.getPaddingType() == iDesc1.paddingType
                        && neighbor.getPreferredSize() == NOT_EXPLICITLY_DEFINED) {
                    neighbor = LayoutInterval.getDirectNeighbor(neighbor, alignment, false);
                    if (neighbor == null) {
                        neighbor = LayoutInterval.getRoot(iDesc2.snappedParallel);
                    }
                    if (equalSnap(neighbor, iDesc1.snappedNextTo, alignment)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean equalSnap(LayoutInterval interval1, LayoutInterval interval2, int alignment) {
        if (interval1 != null && interval2 != null) {
            if (interval1 == interval2) {
                return true;
            }
            if (alignment == LEADING || alignment == TRAILING) {
                if (interval1.isParentOf(interval2)) {
                    return LayoutInterval.isAlignedAtBorder(interval2, interval1, alignment);
                } else if (interval2.isParentOf(interval1)) {
                    return LayoutInterval.isAlignedAtBorder(interval1, interval2, alignment);
                } else {
                    return LayoutUtils.alignedIntervals(interval1, interval2, alignment);
//                    LayoutInterval parent = LayoutInterval.getCommonParent(interval1, interval2);
//                    return parent != null && parent.isParallel()
//                           && LayoutInterval.isAlignedAtBorder(interval1, parent, alignment)
//                           && LayoutInterval.isAlignedAtBorder(interval2, parent, alignment);
                }
            } else if (alignment == CENTER || alignment == BASELINE) {
                return (interval1 == interval2.getParent() && interval1.getGroupAlignment() == alignment && interval2.getAlignment() == alignment)
                        || (interval2 == interval1.getParent() && interval2.getGroupAlignment() == alignment && interval1.getAlignment() == alignment)
                        || (interval1.getParent() == interval2.getParent() && interval1.getAlignment() == alignment && interval2.getAlignment() == alignment);
            }
        }
        return false;
    }

    private boolean plainAlignmentChange(IncludeDesc ndesc, IncludeDesc odesc) {
        if (/*ndesc.*/closedSpace != null && odesc.snappedParallel != null) {
            LayoutInterval ogroup = (odesc.snappedParallel == odesc.parent) ? odesc.parent : null;
            if (restoreDimension()) {
                if (ogroup == null) {
                    ogroup = odesc.snappedParallel.getParent();
                }
                LayoutInterval moved = dragger.getMovingComponents()[0].getLayoutInterval(dimension);
                for (Iterator<LayoutInterval> it=ogroup.getSubIntervals(); it.hasNext(); ) {
                    LayoutInterval li = it.next();
                    if (li == moved || li.isParentOf(moved)) {
                        layoutModel.setIntervalAlignment(li, ndesc.alignment);
                        return true;
                    }
                }
            }
        }
        return false;
    }

//    private static void setIDesc(IncludeDesc source, IncludeDesc target) {
//        target.parent = source.parent;
//        target.alignment = source.alignment;
//        target.index = source.index;
//        target.neighbor = source.neighbor;
//        target.newSubGroup = source.newSubGroup;
//        target.snappedParallel = source.snappedParallel;
//        target.snappedNextTo = source.snappedNextTo;
//        target.paddingType = source.paddingType;
//    }

    // -----
    // overlap analysis

    private int getDimensionSolvingOverlap(LayoutDragger.PositionDef[] positions) {
        if (dragger.isResizing(HORIZONTAL) && !dragger.isResizing(VERTICAL)) {
            return HORIZONTAL;
        }
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
            int[][] overlapSides = overlappingGapSides(dragger.getTargetRoots()[HORIZONTAL],
                                                       dragger.getMovingSpace());
            if (((alignment == LEADING) || (alignment == TRAILING))
                && (overlapSides[VERTICAL][1-alignment] != 0)
                && (overlapSides[VERTICAL][alignment] == 0)) {
                return VERTICAL;
            }
        }
        if ((positions[HORIZONTAL] == null || !positions[HORIZONTAL].snapped)
            && (positions[VERTICAL] == null || !positions[VERTICAL].snapped)) {
            boolean[] overlapDim = overlappingGapDimensions(dragger.getTargetRoots()[HORIZONTAL],
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
    private static void fillOverlappingComponents(List<LayoutComponent> overlaps, LayoutInterval group, LayoutRegion region) {
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
        List<LayoutComponent> overlaps = new LinkedList<LayoutComponent>();
//        LayoutInterval layoutRoot = LayoutInterval.getRoot(positions[HORIZONTAL].interval);
        fillOverlappingComponents(overlaps, layoutRoot, region);
        Iterator<LayoutComponent> iter = overlaps.iterator();
        while (iter.hasNext()) {
            LayoutComponent component = iter.next();
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

    /**
     * An auto-resizing component that is being moved needs to be set to fixed
     * under certain conditions not to grow on the new location. Depending on
     * where it is placed in the end, its resizing may be re-enabled.
     */
    private void cancelResizingOfMovingComponent() {
        if (!dragger.isResizing() && originalPosition != null && originalPosition.suppressedResizing) {
            Object start = layoutModel.getChangeMark();
            if (addingInterval.isComponent()) {
                assert LayoutInterval.wantResize(addingInterval);
                operations.eliminateResizing(addingInterval, dimension, null);
            } else if (addingInterval.isParallel()) {
                addingInterval.setMaximumSize(USE_PREFERRED_SIZE);
            }
            Object end = layoutModel.getChangeMark();
            setUndo(start, end, dimension); // this change should be undone if doing restoreDimension()
            if (start.equals(undoCheckMark)) {
                undoCheckMark = end;
            }
        }
    }

    /**
     * Completes check for auto-resizing when exact inclusions are known for the
     * adding interval (that has been resized). Here we detect situations when
     * it should be made resizing just to accommodate to fixed size of some other
     * components, in result forming a parallel group with suppressed resizing.
     */
    private void checkResizing2(IncludeDesc iDesc1, IncludeDesc iDesc2) {
        // The checkResizing method detected resizing when a component spans
        // over resizing space (edges have opposite anchors). 
        if (!LayoutInterval.wantResize(addingInterval)
                && becomeResizing[dimension] == null
                && (iDesc1.snapped() || iDesc1.fixedPosition)
                && iDesc2 != null && iDesc2.snapped()) {
            assert dragger.getResizingEdge(dimension) == iDesc2.alignment;
            // the fixed edge
            LayoutInterval snap1;
            if (iDesc1.snapped()) {
                snap1 = iDesc1.snappedParallel != null ? iDesc1.snappedParallel : iDesc1.snappedNextTo;
            } else if (iDesc1.neighbor != null) {
                snap1 = iDesc1.neighbor;
            } else if (iDesc1.parent.isParallel()) {
                snap1 = iDesc1.parent;
            } else { // in a sequence - find the neighbor
                snap1 = null;
                LayoutInterval p = iDesc1.parent;
                int i = iDesc1.index;
                if (iDesc1.alignment == LEADING && i >= p.getSubIntervalCount()) {
                    i = p.getSubIntervalCount() - 1;
                }
                while (i >= 0 && i < p.getSubIntervalCount()) {
                    LayoutInterval li = p.getSubInterval(i);
                    if (!li.isEmptySpace()) {
                        snap1 = li;
                        break;
                    }
                    i += (iDesc1.alignment == TRAILING ? 1 : -1);
                }
                if (snap1 == null) {
                    snap1 = iDesc1.parent.getParent();
                }
            }
            // the resizing edge snap
            LayoutInterval snap2 = iDesc2.snappedParallel != null
                                   ? iDesc2.snappedParallel : iDesc2.snappedNextTo;
//            boolean snapsRelated = true;
            // The resizing edge must be snapped directly to something parallel
            // or equal. The fixed edge can also be snapped next to something
            // that has a parallel snap or equal (i.e. indirectly).
//            if (snap1 != snap2 && (iDesc1.newSubGroup || iDesc2.newSubGroup)
//                    && snap1.getParent() != null && snap2.getParent() != null) {
//                // check for some hypothetical situation of resizing out of sequence...
//                LayoutInterval commonParent = LayoutInterval.getCommonParent(snap1, snap2);
//                if (commonParent == null || !commonParent.isSequential()) {
//                    snapsRelated = false;
//                }
//            }
            if (snapEqualToParallelSnap(snap2, snap2==iDesc2.snappedParallel, iDesc2.alignment, snap1)
                    && (snapEqualToParallelSnap(snap1, snap1==iDesc1.snappedParallel, iDesc1.alignment, snap2)
                        || tiedToParallelSnap(snap1, iDesc1.alignment, snap2))) {
                // have parallel snap on both sides
                operations.setIntervalResizing(addingInterval, true);
            }
        }
        // Unlike in checkResizing method, here we can detect e.g. situation
        // when a component is resized in parallel with part of a sequence
        // (and not just within the sequence) - which should make the component
        // resizing (though maybe in parallel group that will be fixed).
//        if (!LayoutInterval.wantResize(addingInterval)
//                && iDesc1 != null && iDesc2 != null
//                && iDesc1.snapped() && iDesc2.snapped()
//                && (iDesc1.newSubGroup || iDesc2.newSubGroup)) {
//            // snapping at both sides and new parallel sub-group created
//            int resizingEdge = dragger.getResizingEdge(dimension);
//            if (iDesc1.alignment == resizingEdge) {
//                IncludeDesc d = iDesc1;
//                iDesc1 = iDesc2;
//                iDesc2 = d;
//            }
//            boolean snapsRelated = true;
//            if (iDesc2.snappedParallel != null) {
//                // resized to snap in parallel with something out of the sequence?
//                LayoutInterval origSnap = (iDesc1.snappedParallel != null)
//                        ? iDesc1.snappedParallel : iDesc1.snappedNextTo;
//                LayoutInterval commonParent = LayoutInterval.getCommonParent(origSnap, iDesc2.snappedParallel);
//                if (commonParent == null || !commonParent.isSequential()) {
//                    snapsRelated = false;
//                }
//            } else { // is snap "next to" equal to some parallel snap?
//                assert iDesc2.parent.isSequential();
//                LayoutInterval neighborGap;
//                if (iDesc2.snappedNextTo.getParent() != null) {
//                    neighborGap = LayoutInterval.getDirectNeighbor(iDesc2.snappedNextTo, resizingEdge^1, false);
//                } else {
//                    neighborGap = iDesc2.parent.getSubInterval(
//                            resizingEdge==LEADING ? 0 : iDesc2.parent.getSubIntervalCount()-1);
//                }
//                if (neighborGap == null
//                        || !neighborGap.isEmptySpace()
//                        || neighborGap.getPreferredSize() != NOT_EXPLICITLY_DEFINED
//                        || neighborGap.getDiffToDefaultSize() != 0) {
//                    snapsRelated = false;
//                }
//            }
//            if (snapsRelated) {
//                operations.setIntervalResizing(addingInterval, true);
//            }
//        }
    }

    /**
     * @return true if given inclusion snaps to some component in parallel, or
     *         there is a component to which it could equally snap to sequentially
     */
    private boolean snapEqualToParallelSnap(LayoutInterval snapped, boolean parallel, int alignment, LayoutInterval otherSnapped) {
        if (snapped.getParent() == null) { // snapped to root
            LayoutInterval group = otherSnapped.isGroup() ? otherSnapped : otherSnapped.getParent();
            while (group != snapped) {
                if (LayoutInterval.isPlacedAtBorder(group, snapped, dimension, alignment)) {
                    break;
                }
                group = group.getParent();
            }
            return LayoutUtils.anythingAtGroupEdge(group, null, dimension, alignment);
        }
        if (parallel) {
            return true;
        }
        LayoutInterval neighborGap = LayoutInterval.getDirectNeighbor(snapped, alignment^1, false);
        return neighborGap != null && neighborGap.isEmptySpace()
                && neighborGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                && neighborGap.getDiffToDefaultSize() == 0;
//        if (parallel) {
//            return snapped.getParent() != null || rootSnapEqualToParallel(otherSnapped, dimension, alignment);
//        } else {
//            return nextToSnapEqualToParallel(snapped, alignment, otherSnapped);
//        }

//        return (desc.snappedParallel != null
//                 && (desc.snappedParallel.getParent() != null
//                    || rootSnapEqualToParallel(otherSnapped, dimension, desc.alignment)))
//            || (desc.snappedParallel == null && desc.snappedNextTo != null
//                && nextToSnapEqualToParallel(desc.snappedNextTo, otherSnapped, desc.alignment));
    }

    private boolean tiedToParallelSnap(LayoutInterval snapped, int alignment, LayoutInterval otherSnapped) {
        LayoutInterval tieParent;
        if (otherSnapped == null || otherSnapped.getParent() == null) {
            tieParent = LayoutInterval.getFirstParent(snapped, PARALLEL);
        } else if (otherSnapped.isParallel() && otherSnapped.isParentOf(snapped)) {
            tieParent = otherSnapped;
        } else {
            tieParent = LayoutInterval.getCommonParent(snapped, otherSnapped);
            if (tieParent != null && tieParent.isSequential()) {
                LayoutInterval p = snapped;
                if (p != tieParent) {
                    while (p.getParent() != tieParent) {
                        p = p.getParent();
                    }
                }
                tieParent = p.isParallel() ? p : null;
            }
        }
        if (tieParent != null) { // && !LayoutInterval.contentWantResize(tiedParent)
            if (tieParent.isParentOf(snapped)) {
                snapped = tiedToParent(snapped, tieParent, false, dimension, alignment);
            } // otherwise snapped is likely root
            if (snapped != null && LayoutUtils.anythingAtGroupEdge(tieParent, snapped, dimension, alignment)) {
                return true;
            }
        }
        return false;
    }

    private static LayoutInterval tiedToParent(LayoutInterval interval, LayoutInterval tieParent, boolean defGap, int dimension, int alignment) {
        if (LayoutInterval.wantResize(interval)) {
            return null;
        }
        LayoutInterval parParent = interval;
        while (parParent != tieParent) {
            interval = parParent;
            LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
            while (neighbor != null) {
                if (LayoutInterval.canResize(neighbor)
                    || (neighbor.isEmptySpace() && defGap
                        && neighbor.getPreferredSize() != NOT_EXPLICITLY_DEFINED)) {
                    return null;
                }
                interval = neighbor;
                neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
            }
            if (interval.isEmptySpace()) {
                interval = interval.getParent();
                assert interval.isSequential();
            }
            parParent = LayoutInterval.getFirstParent(interval, PARALLEL);
            if (!LayoutInterval.isPlacedAtBorder(interval, parParent, dimension, alignment)) {
                return null;
            }
        }

        while (interval != tieParent && interval != null && interval.getParent() != tieParent) {
            interval = interval.getParent();
        }
        return interval;
    }

//    private static boolean nextToSnapEqualToParallel(LayoutInterval nextTo, int alignment, LayoutInterval otherSnapped) {
//        if (nextTo.getParent() != null) {
//            LayoutInterval neighborGap = LayoutInterval.getDirectNeighbor(nextTo, alignment^1, false);
//            if (neighborGap != null && neighborGap.isEmptySpace()
//                    && neighborGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED
//                    && neighborGap.getDiffToDefaultSize() == 0) {
//                return true;
//            }
//        } else {
//            List<LayoutInterval> list = new LinkedList<LayoutInterval>();
//            if (otherSnapped != null && otherSnapped.getParent() != null) {
//                list.add(LayoutInterval.getFirstParent(otherSnapped, SEQUENTIAL));
//            } else {
//                list.add(nextTo);
//            }
//            while (!list.isEmpty()) {
//                LayoutInterval interval = list.remove(0);
//                if (interval.isParallel()) {
//                    for (Iterator<LayoutInterval> it = interval.getSubIntervals(); it.hasNext(); ) {
//                        LayoutInterval li = it.next();
//                        if (li.isSequential()) {
//                            list.add(li);
//                        }
//                    }
//                } else if (interval.isSequential()) {
//                    LayoutInterval li = interval.getSubInterval(alignment==LEADING ? 0 : interval.getSubIntervalCount()-1);
//                    if (li.isEmptySpace() && li.getPreferredSize() == NOT_EXPLICITLY_DEFINED
//                        && li.getDiffToDefaultSize() == 0
//                        && LayoutInterval.getNeighbor(li, alignment, false, true, false) == null
//                        && (LayoutInterval.wantResize(li)
//                            || LayoutInterval.isAlignedAtBorder(li, nextTo, alignment))) { // at root
//                        return true;
//                    } else if (li.isGroup()) {
//                        list.add(li);
//                    }
//                }
//            }
//        }
//        return false;
//    }

//    private static boolean rootSnapEqualToParallel(LayoutInterval otherSnapped, int dimension, int alignment) {
////        if (!otherSnapped.isParallel()) {
////            otherSnapped = LayoutInterval.getFirstParent(otherSnapped, PARALLEL);
////        }
//        LayoutInterval root = LayoutInterval.getRoot(otherSnapped);
//        while (otherSnapped != root) {
//            if (LayoutInterval.isPlacedAtBorder(otherSnapped, root, dimension, alignment)) {
//                break;
//            }
//            otherSnapped = otherSnapped.getParent();
//        }
////        if (otherSnapped == root || LayoutInterval.isPlacedAtBorder(otherSnapped, root, dimension, alignment)) {
//            List<LayoutInterval> list = new LinkedList<LayoutInterval>();
//            list.add(otherSnapped);
//            while (!list.isEmpty()) {
//                LayoutInterval interval = list.remove(0);
//                if (interval.isParallel()) {
//                    for (Iterator<LayoutInterval> it = interval.getSubIntervals(); it.hasNext(); ) {
//                        LayoutInterval li = it.next();
//                        if (LayoutInterval.isPlacedAtBorder(li, interval, dimension, alignment)) {
//                            if (li.isComponent()) {
//                                return true;
//                            } else if (li.isSequential()) {
//                                list.add(li);
//                            }
//                        }
//                    }
//                } else if (interval.isSequential()) {
//                    LayoutInterval li = interval.getSubInterval(
//                            alignment==LEADING ? 0 : interval.getSubIntervalCount()-1);
//                    if (li.isComponent()
//                        || (li.isEmptySpace() && li.getPreferredSize() == NOT_EXPLICITLY_DEFINED
//                            && !LayoutInterval.canResize(li))) {
//                        return true;
//                    } else if (li.isParallel()) {
//                        list.add(li);
//                    }
//                }
//            }
////        }
//        return false;
//    }

    /**
     * Adds aligned with an interval to existing group, or creates new.
     * (Now used only to a limited extent for closed groups only.)
     */
    private void addSimplyAligned() {
        int alignment = aEdge;
        assert alignment == CENTER || alignment == BASELINE;
        layoutModel.setIntervalAlignment(addingInterval, alignment);

//        if (addingInterval.getParent() != null) {
//            // hack: resized interval in center/baseline has not been removed
//            return;
//        }

        LayoutInterval group;
        LayoutRegion currentSpace;
        if (aSnappedParallel.isParallel() && aSnappedParallel.getGroupAlignment() == alignment) {
//            layoutModel.addInterval(addingInterval, aSnappedParallel, -1);
            group = aSnappedParallel;
            currentSpace = aSnappedParallel.getCurrentSpace();
        } else {
            group = aSnappedParallel.getParent();
            if (group.isParallel() && group.getGroupAlignment() == alignment) {
//                layoutModel.addInterval(addingInterval, parent, -1);
                currentSpace = group.getCurrentSpace();
            } else {
                int alignIndex = layoutModel.removeInterval(aSnappedParallel);
                LayoutInterval subGroup = new LayoutInterval(PARALLEL);
                subGroup.setGroupAlignment(alignment);
                if (group.isParallel()) {
                    subGroup.setAlignment(aSnappedParallel.getAlignment());
                }
                layoutModel.setIntervalAlignment(aSnappedParallel, alignment);
                layoutModel.addInterval(aSnappedParallel, subGroup, -1);
//                layoutModel.addInterval(addingInterval, subGroup, -1);
                layoutModel.addInterval(subGroup, group, alignIndex);
                group = subGroup;
                currentSpace = aSnappedParallel.getCurrentSpace();
            }
        }
        layoutModel.addInterval(addingInterval, group, -1);

        // adjusting surrounding gaps if growing
        for (int e=LEADING; e <= TRAILING; e++) {
            LayoutInterval gap = LayoutInterval.getNeighbor(group, e, false, true, false);
            if (gap == null || !gap.isEmptySpace() || LayoutInterval.isDefaultPadding(gap)) {
                continue;
            }
            int growth;
            if (gap.getParent() == group.getParent()) { // direct gap
                growth = LayoutRegion.distance(addingSpace, currentSpace, dimension, e, e);
            } else {
                LayoutInterval superGroup = LayoutInterval.getDirectNeighbor(gap, e^1, true);
                growth = LayoutRegion.distance(addingSpace, superGroup.getCurrentSpace(), dimension, e, e);
            }
            if (e == TRAILING) {
                growth *= -1;
            }
            if (growth > 0) {
                LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(gap, e, true);
                int npos = (neighbor != null) ?
                    neighbor.getCurrentSpace().positions[dimension][e^1] :
                    LayoutInterval.getFirstParent(gap, PARALLEL).getCurrentSpace().positions[dimension][e];
                int dist = addingSpace.positions[dimension][e] - npos;
                if (e == TRAILING) {
                    dist *= -1;
                }
                if (dist < LayoutInterval.getCurrentSize(gap, dimension) && dist > 0) {
                    operations.resizeInterval(gap, dist);
                }
            }
        }
    }

    private void addInterval(IncludeDesc iDesc1, IncludeDesc iDesc2, boolean newRoot) {
        addToGroup(iDesc1, iDesc2, true);

        List<LayoutInterval> added = getAddedIntervals();

        // align in parallel if required
        if (iDesc1.snappedParallel != null || (iDesc2 != null && iDesc2.snappedParallel != null)) {
            if (iDesc2 != null && iDesc2.snappedParallel != null) {
//                alignInParallel(addingInterval, iDesc2.snappedParallel, iDesc2.alignment);
                alignInParallel(getAlignRep(added, iDesc2.alignment), iDesc2.snappedParallel, iDesc2.alignment);
            }
            if (iDesc1.snappedParallel != null) {
//                alignInParallel(addingInterval, iDesc1.snappedParallel, iDesc1.alignment);
                alignInParallel(getAlignRep(added, iDesc1.alignment), iDesc1.snappedParallel, iDesc1.alignment);
            }
        }

        // may want to disable resizing of parallel parent group
        checkParallelResizing(added, iDesc1, iDesc2);

        if (!newRoot) { // adjust to grown content
            accommodateOutPosition(added);
        }
//        // post processing
//        LayoutInterval parent = addingInterval.getParent();
//        int accAlign = DEFAULT;
//        if (parent.isSequential()) {
//            int tryAlign = parent.getAlignment() != TRAILING ? TRAILING : LEADING;
//            if (LayoutInterval.getDirectNeighbor(addingInterval, tryAlign, true) == null) {
//                accAlign = tryAlign;
//            }
//            else {
//                tryAlign ^= 1;
//                if (LayoutInterval.getDirectNeighbor(addingInterval, tryAlign, true) == null)
//                    accAlign = tryAlign;
//            }
//        }
//        else {
//            accAlign = addingInterval.getAlignment() ^ 1;
//        }
//        if (accAlign != DEFAULT) {
//            accommodateOutPosition(addingInterval, accAlign); // adapt size of parent/neighbor
//        }

        LayoutInterval interval = getAddedIntervals().get(0); // get again, groups might have changed

        if (dragger.isResizing(dimension) && LayoutInterval.wantResize(interval)) {
            operations.suppressResizingOfSurroundingGaps(interval);
        }

        // avoid unnecessary parallel group nesting
        operations.mergeParallelGroups(LayoutInterval.getRoot(selectedComponentIntervals[dimension][0]));

        // optimize repeated gaps at the edges of parallel parent group
        LayoutInterval parent = null;
        do {
            interval = getAddedIntervals().get(0); // get again, groups might have changed
            LayoutInterval p = LayoutInterval.getFirstParent(interval, PARALLEL);
            if (p != parent) { // repeat at the same level if it re-arranges groups
                parent = p;
                operations.optimizeGaps(parent, dimension);
            } else { // then go up
                parent = LayoutInterval.getFirstParent(parent, PARALLEL);
                while (parent != null) {
                    operations.optimizeGaps(parent, dimension);
                    parent = LayoutInterval.getFirstParent(parent, PARALLEL);
                }
            }
        } while (parent != null);

        // check if intervals added to a sequence should not be rather placed
        // inside an open neighbor parallel group
        interval = getAddedIntervals().get(0); // get again, groups might have changed
        parent = interval.getParent();
        if (parent.isSequential()) {// && !alignedInParallel)
            int nonEmptyCount = LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true);
            if (nonEmptyCount > 1 && dimension == HORIZONTAL) {
//                operations.moveInsideSequential(parent, dimension);
//                interval = getAddedIntervals().get(0); // get again, groups might have changed
            }
        }

//        operations.mergeParallelGroups(LayoutInterval.getFirstParent(interval, PARALLEL));
    }

    private List<LayoutInterval> getAddedIntervals() {
        return getIntervalsInCommonParent(selectedComponentIntervals[dimension]);
    }

    private LayoutInterval getAlignRep(List<LayoutInterval> added, int alignment) {
        LayoutInterval first = added.get(0);
        if (added.size() == 1) {
            return first;
        } else {
            LayoutInterval parent = first.getParent();
            if (parent.isSequential()) {
                return alignment != TRAILING ? first : added.get(added.size()-1);
            } else {
                return parent;
            }
        }
    }

    private void addToGroup(IncludeDesc iDesc1, IncludeDesc iDesc2, boolean definite) {
        assert iDesc2 == null || (iDesc1.parent == iDesc2.parent
                                  && iDesc1.newSubGroup == iDesc2.newSubGroup
                                  && iDesc1.neighbor == iDesc2.neighbor);

        LayoutInterval parent = iDesc1.parent;
        LayoutInterval seq = null;
        boolean subseq = false;
        int index = 0;

        if (parent.isSequential()) {
            if (iDesc1.newSubGroup) {
                LayoutRegion space = /*iDesc1.*/closedSpace == null ? addingSpace : /*iDesc1.*/closedSpace;
//                if (dimension == VERTICAL) { // count in a margin in vertical direction
//                    // [because analyzeAdding uses it - maybe we should get rid of it completely]
//                    space = new LayoutRegion(space);
//                    space.reshape(VERTICAL, LEADING, -4);
//                    space.reshape(VERTICAL, TRAILING, 4);
//                }
                int closeAlign1;
                int closeAlign2;
                if (/*iDesc1.*/closedSpace != null) {
                    closeAlign1 = LEADING;
                    closeAlign2 = TRAILING;
                } else {
                    closeAlign1 = getExtractCloseAlign(iDesc1);
                    closeAlign2 = getExtractCloseAlign(iDesc2);
                }
                LayoutInterval subgroup = extractParallelSequence(
                        parent, space, closeAlign1, closeAlign2, iDesc1.alignment);
                if (subgroup != null) { // just for robustness - null only if something got screwed up
//                    if (subgroup.getGroupAlignment() != iDesc1.alignment
//                            && iDesc1.snappedParallel != null
//                            && subgroup.isParentOf(iDesc1.snappedParallel)) {
//                        subgroup.setGroupAlignment(iDesc1.alignment);
//                    }
                    seq = new LayoutInterval(SEQUENTIAL);
                    parent = subgroup;
                    subseq = true;
                }
            }
            if (seq == null) {
                seq = parent;
                parent = seq.getParent();
                index = iDesc1.index;
            }
            if (iDesc2 != null && iDesc2.alignment == dragger.getResizingEdge(dimension)) {
                alignWithResizingInSubgroup(seq, parent, iDesc2);
            }
        }
        else { // parallel parent
            LayoutInterval neighbor = iDesc1.neighbor;
            if (neighbor != null) {
                assert neighbor.getParent() == parent;
                seq = new LayoutInterval(SEQUENTIAL);
                layoutModel.addInterval(seq, parent, layoutModel.removeInterval(neighbor));
                seq.setAlignment(neighbor.getAlignment());
                layoutModel.setIntervalAlignment(neighbor, DEFAULT);
                layoutModel.addInterval(neighbor, seq, 0);
                index = iDesc1.index;
            }
            else {
                seq = new LayoutInterval(SEQUENTIAL);
                if (iDesc1.snapped()) {
                    seq.setAlignment(iDesc1.alignment);
                }
            }
        }

        assert iDesc1.alignment >= 0 || iDesc2 == null;
        assert iDesc2 == null || iDesc2.alignment == (iDesc1.alignment^1);
        assert parent.isParallel();

        LayoutInterval[] outBounds = new LayoutInterval[2]; // outermost boundary intervals (not gaps)
//        int[] outEdges = new int[2]; // edges of the boundary intervals
        boolean[] span = new boolean[2]; // [what it really is??]
        boolean[] outOfGroup = new boolean[2];
        LayoutInterval[] neighbors = new LayoutInterval[2]; // direct neighbors in the sequence (not gaps)
//        LayoutInterval[] outNeighbors = new LayoutInterval[2]; // neighbors of parent if no neighbor in sequence (also gaps)
        LayoutInterval[] gaps = new LayoutInterval[2]; // new gaps to create
        LayoutInterval originalGap = null;
        boolean minorOriginalGap;
//        int[] boundaryPos = { LayoutRegion.UNKNOWN, LayoutRegion.UNKNOWN }; // LEADING, TRAILING
        int[] centerDst = new int[2]; // LEADING, TRAILING

        // find the neighbors for the adding interval and determine the original gap
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
                    if (idx2 >= 0 && idx2 < count) {
                        neighbors[i] = seq.getSubInterval(idx2);
                    }
                } else  {
                    neighbors[i] = li;
                }
            }
//            if (neighbors[i] == null) {
//                outNeighbors[i] = LayoutInterval.getNeighbor(parent, i, false, true, false);
//            }
            IncludeDesc iiDesc = (iDesc1.alignment < 0 || iDesc1.alignment == i) ? iDesc1 : iDesc2;

            if (iiDesc != null && iiDesc.snappedParallel != null) {
                span[i] = true;
//                outBounds[i] = iiDesc.snappedParallel;
//                outEdges[i] = i;
//            } if (neighbors[i] != null) {
//                outBounds[i] = neighbors[i];
//                outEdges[i] = i^1;
//            } else if (iDesc1.closedSpace != null) {
//                outBounds[i] = parent;
//                outEdges[i] = i;
            } else if (iiDesc != null && iiDesc.snappedNextTo != null) {
                if (!parent.isParentOf(iiDesc.snappedNextTo)) {
                    LayoutInterval li = parent;
                    LayoutInterval gap = null;
                    do {
                        li = LayoutInterval.getNeighbor(li, i, false, true, false);
                        if (li != null) {
                            if (li.isEmptySpace()) {
                                gap = li;
                            }
                        }
                    } while (li != null && li != iiDesc.snappedNextTo && !li.isParentOf(iiDesc.snappedNextTo));
                    if (gap != null && (li != null || iiDesc.snappedNextTo.getParent() == null)
                            && LayoutInterval.isDefaultPadding(gap)) {
                        span[i] = true;
                    }
                }
//                    outBounds[i] = iiDesc.snappedNextTo;
//                    outEdges[i] = i^1;
            }
            outOfGroup[i] = stickingOutOfGroup(parent, i);
            if (neighbors[i] != null) {
                outBounds[i] = neighbors[i];
//                outEdges[i] = i^1;
            } else {
                outBounds[i] = getPerceivedParentNeighbor(parent, addingSpace, outOfGroup[i], dimension, i);
//                outEdges[i] = outBounds[i] == parent || outBounds[i].isParentOf(parent) ? i : i^1;
            }
            if (definite && /*(iiDesc == null || iiDesc.alignment < 0) &&*/ neighbors[i] == null) {
                if (!subseq && seq.getParent() != null && parent.getParent() != null
                        && outOfGroup[i]//stickingOutOfGroup(parent, i)
                        && shouldExpandOverGroupEdge(parent, outBounds[i], i)) {
                    // adding over group edge that is not apparent to the user
                    parent = separateSequence(seq, i);
                } else if (subseq && iDesc1.parent.getParent().getParent() != null
                        && stickingOutOfGroup(iDesc1.parent.getParent(), i)
                        && shouldExpandOverGroupEdge(iDesc1.parent.getParent(), outBounds[i], i)) {
                    // adding over group edge that is not apparent to the user
                    LayoutInterval p = separateSequence(iDesc1.parent, i);
                    setCurrentPositionToParent(parent, p, dimension, i);
//                    parent.getCurrentSpace().setPos(dimension, i,
//                            p.getCurrentSpace().positions[dimension][i]);
                }
            }
//            outBounds[i] = neighbors[i] != null ? neighbors[i] : parent;;
            if (iDesc1.alignment < 0) { // no alignment known
//                boundaryPos[i] = getPerceivedParentNeighborPosition(parent, outBounds[i], addingSpace, dimension, i);
                centerDst[i] = addingSpace.positions[dimension][CENTER]
                        - (outBounds[i] == parent || outBounds[i].isParentOf(parent) ?//outEdges[i] == i ?
                              outBounds[i].getCurrentSpace().positions[dimension][i] : 
                              getPerceivedNeighborPosition(outBounds[i], addingSpace, dimension, i));
//                    (neighbors[i] != null ?
//                        getPerceivedNeighborPosition(neighbors[i], addingSpace, dimension, i^1) :
//                        getPerceivedParentPosition(seq, parent, addingSpace, dimension, i));
                if (i == TRAILING) {
                    centerDst[i] *= -1;
                }
            }
        }
        minorOriginalGap = originalGap != null && !LayoutInterval.canResize(originalGap)
                && ((neighbors[LEADING] == null && LayoutInterval.getEffectiveAlignment(neighbors[TRAILING], LEADING) == TRAILING)
                  || (neighbors[TRAILING] == null && LayoutInterval.getEffectiveAlignment(neighbors[LEADING], TRAILING) == LEADING));

        // compute leading and trailing gaps
        int edges = 2;
        for (int i=LEADING; edges > 0; i^=1, edges--) {
            gaps[i] = null;
            LayoutInterval outerNeighbor = neighbors[i] == null ?
                    LayoutInterval.getNeighbor(parent, i, false, true, false) : null;
            IncludeDesc iiDesc, otherDesc;
            if (iDesc1.alignment < 0 || iDesc1.alignment == i) {
                iiDesc = iDesc1;
                otherDesc = iDesc2;
            } else {
                iiDesc = iDesc2;
                otherDesc = iDesc1;
            }

            if (neighbors[i] == null && iiDesc != null) { // at the start/end of the sequence
                if (iiDesc.snappedNextTo != null
                    && outerNeighbor != null && LayoutInterval.isDefaultPadding(outerNeighbor))
                {   // the padding is outside of the parent already
                    continue;
                }
                if (iiDesc.snappedParallel != null
                    && (!seq.isParentOf(iiDesc.snappedParallel) || originalGap == null))
                {   // starting/ending edge aligned in parallel - does not need a gap
                    continue;
                }
            }

            boolean aligned;
            if (iDesc1.alignment < 0) { // no specific alignment - decide based on distance
                aligned = centerDst[i] < centerDst[i^1]
                          || (centerDst[i] == centerDst[i^1] && i == LEADING);
            } else if (iDesc2 != null) { // both positions defined
//                if (iiDesc.newSubGroup && dragger.getResizingEdge(dimension) == (i^1)) {
//                    // resizing to new subgroup - only the resizing edge should be considered aligned
//                    aligned = LayoutInterval.wantResize(addingInterval);
                if (dragger.isResizing(dimension) && LayoutInterval.wantResize(addingInterval)) {
                    aligned = true;
                } else {
                    aligned = iiDesc.fixedPosition
                              || (i == LEADING && originalPosition != null && originalPosition.lPosFixed)
                              || (i == TRAILING && originalPosition != null && originalPosition.tPosFixed);
                }
            } else { // single position only (either next to or parallel)
                if (iDesc1.snappedParallel == null || !seq.isParentOf(iDesc1.snappedParallel))
                    aligned = i == iDesc1.alignment;
                else // special case - aligning with interval in the same sequence - to subst. its position
                    aligned = i == (iDesc1.alignment^1);
            }

            boolean minorGap = false;
            boolean noMinPadding = false;
            LayoutInterval otherPar = otherDesc != null ? otherDesc.snappedParallel : null;
            if (!aligned && neighbors[i] == null) { // at the end of the sequence
                if (originalGap == null) {
//                    LayoutInterval otherPar = otherDesc != null ? otherDesc.snappedParallel : null;
                    // make sure new sequence has appropriate explicit alignment
//                    if ((otherPar == null || !parent.isParentOf(otherPar))
//                            && seq.getSubIntervalCount() == 0 && seq.getAlignment() != (i^1)) {
//                        layoutModel.setIntervalAlignment(seq, i^1);
//                    }
                    if (outerNeighbor != null && outerNeighbor.isEmptySpace()) {
                        //continue; // unaligned ending gap not needed - there's a gap outside the parent
                        minorGap = true;
                        noMinPadding = true;
                    } else if (otherPar != null && otherPar.getParent() != null) {
                        minorGap = parent.isParentOf(otherPar)
                            || LayoutInterval.getCount(parent, i^1, true) > 0
                            || (neighbors[i^1] == null && LayoutUtils.alignedIntervals(parent, otherPar, i^1));
                    } else {
//                        boolean withinParent;// = true; // tohle je proste naky divny
//                        int boundaryPos = outBounds[i^1].getCurrentSpace().positions[dimension][outBounds[i^1] == parent || outBounds[i^1].isParentOf(parent) ? i^1 : i];//outEdges[i]];
////                        if (boundaryPos[i^1] != LayoutRegion.UNKNOWN) {
//                            int[] parentPos = parent.getCurrentSpace().positions[dimension];
////                            assert parentPos[LEADING] != LayoutRegion.UNKNOWN && parentPos[TRAILING] != LayoutRegion.UNKNOWN;
//                            withinParent = (i == LEADING && boundaryPos <= parentPos[TRAILING])
//                                        || (i == TRAILING && boundaryPos >= parentPos[LEADING]);
////                            withinParent = (i == LEADING && boundaryPos[i^1] <= parentPos[TRAILING])
////                                        || (i == TRAILING && boundaryPos[i^1] >= parentPos[LEADING]);
////                        }

                        if (!outOfGroup[i^1]//withinParent
                            && (LayoutInterval.getCount(parent, i^1, true) > 0
                                || (LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true) > 0
                                    && !LayoutInterval.contentWantResize(parent)))) {
                            minorGap = true;
                        }
//                    } else if (LayoutRegion.pointInside(addingSpace, i^1, parent.getCurrentSpace(), dimension)) {
//                        // minor gap if it does not need to define the parent size
//                        minorGap = (parallel != null && parallel.getParent() != null)
//                                   || LayoutInterval.getCount(parent, i^1, true) > 0
//                                   || (LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true) > 0
//                                       && !LayoutInterval.contentWantResize(parent));
//    //                               || (parent.getParent() != null && LayoutInterval.getCount(parent, i^1, true) > 0);
                    }
                    if (outerNeighbor == null) {
//                        boolean parallelInParent = parallel != null && parent.isParentOf(parallel);
                        if (otherPar != null && (!otherPar.isParallel() || parent.isParentOf(otherPar))) {
                            // aligned directly with some component or group as a whole (not from inside)
                            noMinPadding = !endsWithNonZeroGap(otherPar, i, parent);
                        } else {
                            boolean wantMinPadding = (otherPar == null)
                                    && (seq.getSubIntervalCount() == 0 || endsWithNonZeroGap(seq, i^1, null));
                                      // [or just neighbors[i^1] != null?]
                            noMinPadding = followEndingPaddingFromNeighbors(
                                        (otherPar != null ? otherPar : parent), seq, i, wantMinPadding)
                                    ^ wantMinPadding;
                        }
                    }
                } else if (minorOriginalGap) { // there is already an unaligned fixed ending gap
                    minorGap = true;
                }
            }

            boolean fixedGap = aligned;

            if (!aligned) {
                if ((minorGap && !LayoutInterval.wantResize(parent)) || LayoutInterval.wantResize(addingInterval)) {
                    fixedGap = true;
                } else if (originalGap != null && LayoutInterval.canResize(originalGap)) {
                    // i.e. fixedGap kept false
                    if (originalGap.getMinimumSize() == 0 && neighbors[i] == null) {
                        noMinPadding = true;
                    }
                } else if (originalGap != null && !minorOriginalGap) {
                    if (!span[i^1]
                        || (neighbors[i] == null && !LayoutInterval.canResize(originalGap))
                        || (neighbors[i] != null
                            && (LayoutInterval.getEffectiveAlignment(neighbors[i], i^1) == (i^1)
                                || !tiedToParallelSnap(neighbors[i], i, otherPar)))) {
                        fixedGap = true;
                    }
//                } else if (LayoutInterval.wantResize(subseq ? iDesc1.parent : seq)) {
//                    fixedGap = true;
                } else if (LayoutInterval.wantResize(seq)) {
                    fixedGap = true;
                } else if (neighbors[i] != null) {
                    if (LayoutInterval.getEffectiveAlignment(neighbors[i], i^1) == (i^1)) {
                        fixedGap = true;
                    }
                } else if (otherPar != null) {
                    if (parent.isParentOf(otherPar)) {
                        if (LayoutInterval.getEffectiveAlignmentInParent(otherPar, parent, i^1) == i) {
                            fixedGap = true;
                        }
                    } else {
                        LayoutInterval p = LayoutInterval.getFirstParent(otherPar, PARALLEL);
                        if (p != null && LayoutInterval.getEffectiveAlignmentInParent(parent, p, i) == (i^1)) {
                            fixedGap = true;
                        }
                    }
                } else if (!span[i^1]) {
                    LayoutInterval alignParent = LayoutInterval.getCommonParent(outBounds[LEADING], outBounds[TRAILING]);
//                    int effAlign = i;
//                    if (outBounds[i] != alignParent) {
//                        effAlign = LayoutInterval.getEffectiveAlignmentInParent(outBounds[i], alignParent, outEdges[i]);
//                    }
//                    if (effAlign == (i^1)) {
//                        fixedGap = true;
//                    }
                    int[] effa = new int[2];
                    for (int e=LEADING; e <= TRAILING; e++) {
//                        IncludeDesc iDsc = (iDesc1.alignment < 0 || iDesc1.alignment == e) ? iDesc1 : iDesc2;
                        LayoutInterval b = outBounds[e];
//                        if (iDsc != null && iDsc.snappedParallel != null) {
//                            b = iDsc.snappedParallel;
//                        } else {
//                            b = outBounds[e];
//                        }
                        if (b == alignParent) {
                            effa[e] = e;
                        } else {
//                            boolean inParallel = b == parent || b.isParentOf(parent)
//                                        || LayoutInterval.getCommonParent(b, parent).isParallel();
                            int edge = (b == parent || b.isParentOf(parent)) ? e : e^1; // parent or neighbor
                            effa[e] = LayoutInterval.getEffectiveAlignmentInParent(b, alignParent, edge); //outEdges[e]);
                        }
                    }
                    if (effa[LEADING] == effa[TRAILING]) {
                        fixedGap = true;
                    }
                }
            }
            if (!aligned && (!fixedGap || minorGap)
                    && neighbors[i] == null && originalGap == null
                    && seq.getSubIntervalCount() == 0 && seq.getRawAlignment() == DEFAULT
                    && (otherPar == null || !parent.isParentOf(otherPar))) {
                // this new sequence should be aligned to opposite edge
                layoutModel.setIntervalAlignment(seq, i^1);
            }
            if (fixedGap && noMinPadding) { // minorPadding
                continue;
            }

            LayoutInterval gap = new LayoutInterval(SINGLE);
            if (!minorGap || !fixedGap) {
                if (iiDesc == null || iiDesc.snappedNextTo == null) {
                    // the gap possibly needs an explicit size
                    int distance;
//                    if (neighbors[i] == null// && boundaryPos[i] != LayoutRegion.UNKNOWN
//                            && (iiDesc == null || iiDesc.snappedParallel == null)) {
//                        distance = addingSpace.positions[dimension][i]
//                                - outBounds[i].getCurrentSpace().positions[dimension][outEdges[i]];
//                    } else {
                        LayoutRegion space = iiDesc != null && iiDesc.snappedParallel != null ?
                                             iiDesc.snappedParallel.getCurrentSpace() : addingSpace;
                        distance = neighbors[i] != null ?
                            LayoutRegion.distance(neighbors[i].getCurrentSpace(), space, dimension, i^1, i) :
                            LayoutRegion.distance(parent.getCurrentSpace(), space, dimension, i, i);
//                    }
                    if (i == TRAILING) {
                        distance *= -1;
                    }

                    if (distance > 0) {
                        int pad = neighbors[i] != null || outerNeighbor == null ?
//                                  || LayoutInterval.getNeighbor(parent, i, false, true, false) == null ?
//                            determineExpectingPadding(addingInterval, neighbors[i], seq, i) :
                            dragger.findPaddings(neighbors[i], addingInterval, PaddingType.RELATED, dimension, i)[0] :
                            Short.MIN_VALUE; // has no neighbor, but is not related to container border
                        if (distance > pad || (fixedGap && distance != pad)) {
                            gap.setPreferredSize(distance);
                            if (fixedGap) {
                                gap.setMinimumSize(USE_PREFERRED_SIZE);
                                gap.setMaximumSize(USE_PREFERRED_SIZE);
                            }
                        }
                    } else if (noMinPadding) {
                        gap.setPreferredSize(0);
                    }
                } else {
                    gap.setPaddingType(iiDesc.paddingType);
                }
            }
            if (!fixedGap) {
                if (noMinPadding) {
                    gap.setMinimumSize(0);
                }
                gap.setMaximumSize(Short.MAX_VALUE);
//                // resizing gap may close the open parent group on the other side
//                if (definite && (otherDesc == null || otherDesc.alignment < 0)) {
//                    if (!subseq && parent.getParent() != null && neighbors[i] != null
//                            && !isSignificantGroupEdge(seq, i^1)) {
//                        // the aligned edge needs to be anchored out of parent (independently)
//                        parent = separateSequence(seq, i^1);
////                        if (i == TRAILING) {
////                            edges++; // we need to revisit the LEADING gap
////                        }
//                    } else if (subseq && iDesc1.parent.getParent().getParent() != null
//                            && LayoutInterval.getDirectNeighbor(parent, i, true) != null
//                            && !isSignificantGroupEdge(iDesc1.parent, i^1)) {
//                        // the aligned edge needs to be anchored out of parent (independently)
//                        LayoutInterval p = separateSequence(iDesc1.parent, i^1);
//                        parent.getCurrentSpace().setPos(dimension, i^1,
//                                p.getCurrentSpace().positions[dimension][i^1]);
////                        if (i == TRAILING) {
////                            edges++; // we need to revisit the LEADING gap
////                        }
//                    }
//                }
            }
            gap.setAttribute(LayoutInterval.ATTR_FLEX_SIZEDEF);

            gaps[i] = gap;

            // if anchored towards open parent group edge, we may want to move the
            // sequence out to place it independently on the rest of the group
            if (definite && (otherDesc == null || otherDesc.alignment < 0)) {
                if (!subseq && parent.getParent() != null) {
                    // a) adding into main sequence directly
                    if (!fixedGap && neighbors[i] != null
                            && outBounds[i^1] != parent
                            && !parent.isParentOf(outBounds[i^1])) {
                        // should not close open group by making a resizing sequence
                        parent = separateSequence(seq, i^1);
                        if (i == TRAILING) {
                            edges++; // we need to revisit the LEADING gap
                        }
//                    } else if (addingOverGroupEdge(parent, i)
//                               && isSignificantButInvisibleGroupEdge(parent, i)) {
//                        // adding over group edge that is not apparent to the user
//                        parent = separateSequence(seq, i);
                    }
                } else if (subseq && iDesc1.parent.getParent().getParent() != null) {
                    // b) adding into sub-sequence (in parallel with part of main
                    // sequence), iDesc1.parent is the main sequence
                    if (!fixedGap
                            && LayoutInterval.getDirectNeighbor(parent, i, true) != null
                            && outBounds[i^1] != iDesc1.parent.getParent()
                            && !iDesc1.parent.getParent().isParentOf(outBounds[i^1])) {
                        // should not close open group by making a resizing sequence
                        LayoutInterval p = separateSequence(iDesc1.parent, i^1);
                        setCurrentPositionToParent(parent, p, dimension, i^1);
//                        parent.getCurrentSpace().setPos(dimension, i^1,
//                                p.getCurrentSpace().positions[dimension][i^1]);
                        if (i == TRAILING) {
                            edges++; // we need to revisit the LEADING gap
                        }
//                    } else if (addingOverGroupEdge(iDesc1.parent.getParent(), i)
//                               && isSignificantButInvisibleGroupEdge(iDesc1.parent.getParent(), i)) {
//                        // adding over group edge that is not apparent to the user
//                        LayoutInterval p = separateSequence(iDesc1.parent, i);
//                        parent.getCurrentSpace().setPos(dimension, i,
//                                p.getCurrentSpace().positions[dimension][i]);
                    }
                }
            }
        }

        // try to determine actual positions of the sequence ends
        for (int i = LEADING; i <= TRAILING; i++) {
            IncludeDesc iiDesc = iDesc1.alignment < 0 || iDesc1.alignment == i ?
                                 iDesc1 : iDesc2;
            if (iiDesc != null && neighbors[i] == null && !seq.getCurrentSpace().isSet(dimension, i)) {
                if (iiDesc.snappedParallel != null) {
                    seq.getCurrentSpace().setPos(dimension, i,
                            iiDesc.snappedParallel.getCurrentSpace().positions[dimension][i]);
                } else if (iiDesc.snappedNextTo != null) {
                    seq.getCurrentSpace().setPos(dimension, i,
                            parent.getCurrentSpace().positions[dimension][i]);
                }
            }
        }

        if (seq.getParent() == null) { // newly created sequence
            assert seq.getSubIntervalCount() == 0;
            if (gaps[LEADING] == null && gaps[TRAILING] == null) { // after all, the sequence is not needed
                layoutModel.setIntervalAlignment(addingInterval, seq.getAlignment());
                layoutModel.addInterval(addingInterval, parent, -1);
                return;
            } else {
                layoutModel.addInterval(seq, parent, -1);
            }
        }

        // aligning in parallel with interval in the same sequence was resolved
        // by substituting its position
        if (iDesc1.snappedParallel != null && seq.isParentOf(iDesc1.snappedParallel)) {
            iDesc1.snappedParallel = null; // set to null not to try alignInParallel later
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

        index += operations.addContent(addingInterval, seq, index);
        if (gaps[TRAILING] != null) {
            layoutModel.addInterval(gaps[TRAILING], seq, index);
        }
    }

    private int getExtractCloseAlign(IncludeDesc iDesc) {
        // aligned within the same sequence and without indent?
        return iDesc != null && iDesc.snappedParallel != null
               && iDesc.parent.isParentOf(iDesc.snappedParallel)
               && LayoutRegion.distance(addingSpace, iDesc.snappedParallel.getCurrentSpace(),
                                        dimension, iDesc.alignment, iDesc.alignment) == 0
       ? iDesc.alignment : -1;
    }

    private LayoutInterval extractParallelSequence(LayoutInterval seq,
                                                   LayoutRegion space,
                                                   int closeAlign1,
                                                   int closeAlign2,
                                                   int refPoint)
    {
        int count = seq.getSubIntervalCount();
        int startIndex = 0;
        int endIndex = count - 1;
        int startPos = seq.getCurrentSpace().positions[dimension][LEADING];
        int endPos = seq.getCurrentSpace().positions[dimension][TRAILING];
        boolean closeStart = closeAlign1 == LEADING || closeAlign2 == LEADING;
        boolean closeEnd = closeAlign1 == TRAILING || closeAlign2 == TRAILING;
        if (refPoint < 0) {
            refPoint = CENTER;
        }

        for (int i=0; i < count; i++) {
            LayoutInterval li = seq.getSubInterval(i);
            if (li.isEmptySpace())
                continue;

            LayoutRegion subSpace = li.getCurrentSpace();
            boolean forcedParallel = !solveOverlap && LayoutUtils.contentOverlap(space, li, dimension);
            if (!forcedParallel && LayoutUtils.contentOverlap(space, li, dimension^1)) { // orthogonal overlap
                // this interval makes a hard boundary
                if (getAddDirection(space, subSpace, dimension, refPoint) == LEADING) {
                    // given interval is positioned before this subinterval (trailing boundary)
                    endIndex = i - 1;
                    endPos = subSpace.positions[dimension][LEADING];
                    break;
                }
                else { // given interval points behind this one (leading boundary)
                    startIndex = i + 1;
                    startPos = subSpace.positions[dimension][TRAILING];
                }
            } else if (closeStart || closeEnd) { // go for smallest parallel part possible
                int[] detPos = space.positions[dimension];
                int[] subPos = subSpace.positions[dimension];
                if (closeStart) {
                    if (detPos[LEADING] >= subPos[TRAILING]) {
                        startIndex = i + 1;
                        startPos = subPos[TRAILING];
                    } else if (detPos[LEADING] >= subPos[LEADING]) {
                        startIndex = i;
                        startPos = subPos[LEADING];
                    }
                }
                if (closeEnd && detPos[TRAILING] <= subPos[TRAILING]) {
                    if (detPos[TRAILING] > subPos[LEADING]) {
                        endIndex = i;
                        endPos = subPos[TRAILING];
                        break;
                    } else { // detPos[TRAILING] <= subPos[LEADING]
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
        if (startIndex == endIndex) {
            LayoutInterval li = seq.getSubInterval(startIndex);
            if (li.isParallel()) {
                return li;
            }
        }

        if (seq.getParent() != null) {
            if (!LayoutRegion.isValidCoordinate(startPos) && startIndex == 0) {
                startPos = seq.getParent().getCurrentSpace().positions[dimension][LEADING];
            }
            if (!LayoutRegion.isValidCoordinate(endPos) && endIndex == count-1) {
                endPos = seq.getParent().getCurrentSpace().positions[dimension][TRAILING];
            }
        }

        LayoutInterval group = new LayoutInterval(PARALLEL);
//        int effAlign1 = getEffectiveAlignment(seq.getSubInterval(startIndex));
//        int effAlign2 = getEffectiveAlignment(seq.getSubInterval(endIndex));
//        int groupAlign = (effAlign1 == effAlign2 || effAlign2 < 0) ? effAlign1 : effAlign2;
//        if (alignment != DEFAULT) {
//            group.setGroupAlignment(/*groupAlign == LEADING || groupAlign == TRAILING ?
//                                groupAlign :*/ alignment);
//        }
        if (startIndex == 0 && LayoutInterval.getEffectiveAlignmentInParent(
                seq.getSubInterval(0), seq.getParent(), LEADING) == TRAILING) {
            group.setGroupAlignment(TRAILING);
        } // otherwise leave as LEADING
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

    private void alignWithResizingInSubgroup(LayoutInterval seqWithResizing, LayoutInterval group, IncludeDesc iDesc) {
        if (LayoutInterval.wantResize(addingInterval) || !iDesc.snapped()
                || !group.isParallel() || group.getSubIntervalCount() > 2) {
            return;
        }
        LayoutInterval toAlign = group.getSubInterval(0);
        if (seqWithResizing.getParent() == null) {
            if (group.getSubIntervalCount() != 1) {
                return;
            }
        } else { // existing sequence already in group
            if (group.getSubIntervalCount() != 2) {
                return;
            }
            if (toAlign == seqWithResizing) {
                toAlign = group.getSubInterval(1);
            }
        }
        int alignment = iDesc.alignment; // the resizing edge
        if (LayoutUtils.anythingAtGroupEdge(toAlign, null, dimension, alignment)
                && !LayoutUtils.anythingAtGroupEdge(toAlign, null, dimension, alignment^1)) {
            layoutModel.setGroupAlignment(group, alignment);
            if (toAlign.getAlignment() != alignment) {
                layoutModel.setIntervalAlignment(toAlign, DEFAULT);
            }
            if (seqWithResizing.getParent() == group && seqWithResizing.getAlignment() != alignment) {
                layoutModel.setIntervalAlignment(seqWithResizing, DEFAULT);
            }
        }
    }

    private static void setCurrentPositionToParent(LayoutInterval interval, LayoutInterval parent, int dimension, int alignment) {
        int parentPos = parent.getCurrentSpace().positions[dimension][alignment];
        if (!LayoutRegion.isValidCoordinate(parentPos)) {
            return;
        }
        for (LayoutInterval li = interval; li != parent; li = li.getParent()) {
            li.getCurrentSpace().setPos(dimension, alignment, parentPos);
        }
    }

//    private static LayoutInterval getPerceivedParentPosition(LayoutInterval interval, LayoutInterval parent,
//                                                  LayoutRegion space, int dimension, int alignment)
    private static LayoutInterval getPerceivedParentNeighbor(/*LayoutInterval interval,*/ LayoutInterval parent,
                                                             LayoutRegion space, boolean outOfGroup, int dimension, int alignment) {
        assert parent.isParallel();
        LayoutInterval interval = null;
        LayoutInterval neighbor = null;
//        int position = Integer.MIN_VALUE;
        boolean done = false;
        do {
//            if (parent.isSequential()) {
//                interval = parent;
//                parent = interval.getParent();
//            }

            neighbor = null;
            while (neighbor == null && parent.getParent() != null) {
                if (isSignificantGroupEdge(parent, alignment, outOfGroup)) {
                    break;
                }
//                if (interval != null && isSignificantGroupEdge(interval, alignment)) {
//                    break;
//                }
//                boolean significantEdge = interval.getParent() != null ?
//                            isSignificantGroupEdge(interval, alignment) :
//                            LayoutInterval.isClosedGroup(parent, alignment);
//                if (significantEdge)
//                    break;

                neighbor = LayoutInterval.getDirectNeighbor(parent, alignment, true);
                if (neighbor == null) {
                    interval = parent;
                    parent = interval.getParent();
                    if (parent.isSequential()) {
                        interval = parent;
                        parent = interval.getParent();
                    }
                }
            }

            if (neighbor == null) {
//                position = parent.getCurrentSpace().positions[dimension][alignment];
                done = true; // use the parent
            }
            else { // look for neighbor of the parent that has orthogonal overlap with the given space
                do {
                    if (LayoutUtils.contentOverlap(space, neighbor, dimension^1)) {
                        done = true;
                    } else { // the space can "go through" this neighbor
                        neighbor = LayoutInterval.getDirectNeighbor(neighbor, alignment, true);
                    }
                } while (!done && neighbor != null);
//                do {
//                    position = getPerceivedNeighborPosition(neighbor, space, dimension, alignment^1);
//                    if (position != Integer.MIN_VALUE)
//                        break;
//                    else // otherwise the space can "go through" this neighbor
//                        neighbor = LayoutInterval.getDirectNeighbor(neighbor, alignment, true);
//                }
//                while (neighbor != null);
                if (neighbor == null) {
                    interval = parent;
                    parent = interval.getParent();
                    if (parent.isSequential()) {
                        interval = parent;
                        parent = interval.getParent();
                    }
                }
            }
        } while (!done);
//        while (position == Integer.MIN_VALUE);
//        return position;
        return neighbor != null ? neighbor : parent;
    }

//    private static int getPerceivedParentNeighborPosition(LayoutInterval interval, LayoutInterval outBound,
//                                                          LayoutRegion space, int dimension, int alignment) {
//        if (LayoutInterval.getCommonParent(interval, outBound).isSequential()) {
//            return getPerceivedNeighborPosition(outBound, space, dimension, alignment);
//        } else {
//            return outBound.getCurrentSpace().positions[dimension][alignment];
//        }
////        if (outBound == interval || outBound.isParentOf(interval)
////                || LayoutInterval.getCommonParent(interval, outBound).isParallel()) {
////            return outBound.getCurrentSpace().positions[dimension][alignment];
////        } else {
////            return getPerceivedNeighborPosition(outBound, space, dimension, alignment^1);
//////            return outBound.getCurrentSpace().positions[dimension][alignment^1];
////        }
//    }

    private static boolean isSignificantGroupEdge(LayoutInterval interval, int alignment) {
        LayoutInterval group = interval.getParent();
        assert group.isParallel();
        if (interval.getAlignment() == alignment || LayoutInterval.wantResize(interval))
            return true;

        if (!LayoutInterval.isClosedGroup(group, alignment))
            return false;

        if (!LayoutInterval.isExplicitlyClosedGroup(group)) { // naturally closed group
            LayoutInterval neighborGap = LayoutInterval.getNeighbor(group, alignment, false, true, true);
            if (neighborGap != null && LayoutInterval.isDefaultPadding(neighborGap)) {
                // default padding means the group can be considered open at this edge
                return false;
            }
        }
        return true;
    }

    private static boolean shouldExpandOverGroupEdge(LayoutInterval group, LayoutInterval outBound, int alignment) {
//        return LayoutInterval.isClosedGroup(group, alignment) && !isVisibleGroupEdge(group, alignment);
        if (group != outBound && !group.isParentOf(outBound)
//                && !LayoutInterval.isExplicitlyClosedGroup(group)
//                && LayoutInterval.isClosedGroup(group, alignment) // [TODO perhaps should expand even if open but with fixed content to outBound]
                && !isSignificantGroupEdge(group, alignment, true)) {
            boolean open = !LayoutInterval.isClosedGroup(group, alignment);
            LayoutInterval li = group;
            boolean gapNotWorthExpanding = false;
            while ((li = LayoutInterval.getNeighbor(li, alignment, false, true, false)) != null) {
                if (li == outBound || li.isParentOf(outBound)
                        || LayoutInterval.getDirectNeighbor(li, alignment^1, false) == outBound) {
                    return false; // reached outBound, found nothing bigger than a gap
                }
                if (gapNotWorthExpanding) { // now already more than a gap
                    return true;
                }
                if (li.isEmptySpace() && (LayoutInterval.isDefaultPadding(li) // || open)) {
                        || (open && LayoutInterval.canResize(li)))) {
                    gapNotWorthExpanding = true;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSignificantGroupEdge(LayoutInterval group, int alignment, boolean placedOver) {
        assert group.isParallel();
        return LayoutInterval.isExplicitlyClosedGroup(group)
               || (!placedOver && LayoutUtils.edgeSubComponents(group, alignment, true).size() > 1);
    }

    private static int getPerceivedNeighborPosition(LayoutInterval firstNeighbor, LayoutRegion space, int dimension, int alignment) {
        LayoutInterval neighbor = firstNeighbor;
        do {
            int pos = getPerceivedNeighborPosition0(neighbor, space, dimension, alignment);
            if (pos != Integer.MIN_VALUE) {
                return pos;
            }
            neighbor = LayoutInterval.getDirectNeighbor(neighbor, alignment, true);
        } while (neighbor != null);
        return firstNeighbor.getCurrentSpace().positions[dimension][alignment^1];
    }

    private static int getPerceivedNeighborPosition0(LayoutInterval neighbor, LayoutRegion space, int dimension, int alignment) {
        assert !neighbor.isEmptySpace();

        int neighborPos = Integer.MIN_VALUE;
        if (neighbor.isComponent()) {
            if (LayoutRegion.overlap(space, neighbor.getCurrentSpace(), dimension^1, 0)) {
                neighborPos = neighbor.getCurrentSpace().positions[dimension][alignment^1];
            }
        } else {
            int n = neighbor.getSubIntervalCount();
            int i, d;
            if (neighbor.isParallel() || alignment == TRAILING) {
                d = 1;
                i = 0;
            } else {
                d = -1;
                i = n - 1;
            }
            while (i >=0 && i < n) {
                LayoutInterval sub = neighbor.getSubInterval(i);
                i += d;

                if (sub.isEmptySpace()) {
                    continue;
                }
//                if (sub.isEmptySpace()
//                    || (sub.isComponent()
//                        && !LayoutRegion.overlap(space, sub.getCurrentSpace(), dimension^1, 0)))
//                    continue;

                int pos = getPerceivedNeighborPosition0(sub, space, dimension, alignment);
                if (pos != Integer.MIN_VALUE) {
                    if (neighbor.isSequential()) {
                        neighborPos = pos;
                        break;
                    } else if (neighborPos == Integer.MIN_VALUE || pos*d < neighborPos*d) {
                        neighborPos = pos;
                        // continue, there can still be a closer position
                    }
                }
            }
        }
        return neighborPos;
    }

    private boolean endsWithNonZeroGap(LayoutInterval interval, int edge, LayoutInterval inParent) {
        assert edge == LEADING || edge == TRAILING;
        if (interval.isParallel() && inParent == null) {
            for (Iterator<LayoutInterval> it=interval.getSubIntervals(); it.hasNext(); ) {
                LayoutInterval li = it.next();
                if (endsWithNonZeroGap(li, edge, null)) {
                    return true;
                }
            }
            return false;
        }
        if (inParent != null) {
            LayoutInterval firstSeq = interval; // just for the case inParent is not parent of interval
            LayoutInterval parent = interval.getParent();
            while (parent != null && parent != inParent) {
                if (parent.isSequential()) {
                    if (!firstSeq.isSequential()) {
                        firstSeq = parent;
                    }
                    interval = parent;
                }
                parent = parent.getParent();
            }
            if (parent == null) {
                interval = firstSeq;
            }
        }
//        if (!interval.isSequential() && outParent) {
//            LayoutInterval parent = interval.getParent();
//            while (parent != null) {
//                if (parent.isSequential()) {
//                    interval = parent;
//                    break;
//                }
//                parent = parent.getParent();
//            }
//        }
        if (interval.isSequential() && interval.getSubIntervalCount() > 0) {
            int idx = (edge == LEADING) ? 0 : interval.getSubIntervalCount()-1;
            LayoutInterval li = interval.getSubInterval(idx);
            return li.isEmptySpace() && li.getMinimumSize() != 0;
        }
        return false;
    }

    private boolean followEndingPaddingFromNeighbors(LayoutInterval parent, LayoutInterval interval, int edge, boolean wantMinPadding) {
        boolean someAligned = false;
        for (Iterator<LayoutInterval> it=parent.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval li = it.next();
            if (li != interval && !li.isParentOf(interval)
//                    && (li.getAlignment() == (edge^1) || LayoutInterval.wantResize(li))
                    && wantMinPadding == endsWithNonZeroGap(li, edge^1, null)) {
                someAligned = true;
                if (wantMinPadding == endsWithNonZeroGap(li, edge, null)) {
                    return true; // there's an aligned neighbor with expected ending spaces
                }
            }
        }
        return !someAligned;
    }

    private LayoutInterval separateSequence(LayoutInterval seq, int alignment) {
        // [TODO repeatedly up to given parent]
        LayoutInterval parentPar = seq.getParent();
        assert parentPar.isParallel();
        while (!parentPar.getParent().isSequential()) {
            parentPar = parentPar.getParent();
        }
        LayoutInterval parentSeq = parentPar.getParent(); // sequential

        int d = alignment == LEADING ? -1 : 1;
        int n = parentSeq.getSubIntervalCount();
        int end = parentSeq.indexOf(parentPar) + d;
        while (end >= 0 && end < n) {
            LayoutInterval sub = parentSeq.getSubInterval(end);
            if (!sub.isEmptySpace()) {
//                LayoutRegion subSpace = sub.getCurrentSpace();
//                if (sub.isParallel()
//                    && subSpace.positions[dimension][alignment^1]*d > addingSpace.positions[dimension][alignment]*d
//                    && LayoutInterval.isClosedGroup(sub, alignment))
                if (LayoutUtils.contentOverlap(addingSpace, sub, dimension^1)) {
                    break;
                }
            }
            end += d;
        }

        int endPos = end >= 0 && end < n ?
                     parentSeq.getSubInterval(end).getCurrentSpace().positions[dimension][alignment^1] :
                     parentSeq.getParent().getCurrentSpace().positions[dimension][alignment];
        end -= d;
        operations.parallelizeWithParentSequence(seq, end, dimension);
        parentPar = seq.getParent();
        parentPar.getCurrentSpace().positions[dimension][alignment] = endPos;
        return parentPar;
    }

    /**
     * When an interval is added or resized out of current boundaries of its
     * parent, this method tries to accommodate the size increment in the parent
     * (and its parents). It acts according to the current visual position of
     * the interval - how it exceeds the current parent border. In the simplest
     * form the method tries to shorten the nearest gap in the parent sequence.
     */
    private void accommodateOutPosition(List<LayoutInterval> added) {
        LayoutInterval interval = added.get(0);
        LayoutInterval parent = interval.getParent();
        int alignment = DEFAULT;
        if (parent.isSequential()) {
            int align = parent.getAlignment();
            if (align == LEADING || align == TRAILING) {
                for (int i=0; i < 2; i++) {
                    align ^= 1;
                    LayoutInterval li = align == LEADING ? interval : added.get(added.size()-1);
                    if (LayoutInterval.getDirectNeighbor(li, align, true) == null) {
                        alignment = align;
                        interval = li;
                        break;
                    }
                }
            }
        } else {
            int align = interval.getAlignment();
            if (align == LEADING || align == TRAILING) {
                alignment = align ^ 1;
                int maxPos = LayoutRegion.UNKNOWN;
                for (LayoutInterval li : added) {
                    int pos = li.getCurrentSpace().positions[dimension][alignment];
                    if (maxPos == LayoutRegion.UNKNOWN
                            || (alignment == LEADING && pos < maxPos)
                            || (alignment == TRAILING && pos > maxPos)) {
                        maxPos = pos;
                        interval = li;
                    }
                }
            }
        }
        if (alignment == DEFAULT) {
            return;
        }

        int pos = interval.getCurrentSpace().positions[dimension][alignment];
        assert pos != LayoutRegion.UNKNOWN;
        int sizeIncrement = Integer.MIN_VALUE;
        int d = alignment == LEADING ? -1 : 1;
        int[] groupPos = null;
//        LayoutInterval parent = interval.getParent();
        LayoutInterval prev = null;

        do {
            if (parent.isSequential()) {
                if (sizeIncrement > 0) {
                    int accommodated = accommodateSizeInSequence(interval, prev, sizeIncrement, alignment);
                    sizeIncrement -= accommodated;
                    if (groupPos != null) {
                        groupPos[alignment] += accommodated * d;
                    }
                }
                LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
                if (neighbor != null && (!neighbor.isEmptySpace()/* || LayoutInterval.canResize(neighbor)*/)) {
                    // not a border interval in the sequence, can't go up
                    return;
                }
                prev = interval;
            } else {
                groupPos = parent.getCurrentSpace().positions[dimension];
                if (groupPos[alignment] != LayoutRegion.UNKNOWN) {
                    if (interval.isParallel() && prev != null
                        && interval.getCurrentSpace().positions[dimension][alignment] != groupPos[alignment]
                        && groupGrowingVisibly(interval, prev, alignment)) {
                        // par. group in par. group that is bigger, move the sticking out interval up
                        int align = prev.getAlignment();
                        layoutModel.removeInterval(prev);
                        layoutModel.addInterval(prev, parent, -1);
                        if (prev.getAlignment() != align) {
                            layoutModel.setIntervalAlignment(prev, align);
                        }
                    }
                    sizeIncrement = (pos - groupPos[alignment]) * d;
                    if (sizeIncrement > 0) {
                        int subPos[] = interval.getCurrentSpace().positions[dimension];
                        if (!interval.getCurrentSpace().isSet(dimension)
                            || subPos[alignment]*d < groupPos[alignment]*d)
                        {   // update space of subgroup according to parent (needed if subgroup is also parallel)
                            subPos[alignment] = groupPos[alignment];
                        }
                    }
                } else {
                    groupPos = null;
                }
                if (!interval.isSequential() || prev == null) {
                    prev = interval;
                }
            }
            interval = parent;
            parent = interval.getParent();
        }
        while ((sizeIncrement > 0 || sizeIncrement == Integer.MIN_VALUE)
               && parent != null
               && (!parent.isParallel() || interval.getAlignment() != alignment));
               // can't accommodate at the aligned side [but could probably turn to other side - update 'pos', etc]
    }

    private int accommodateSizeInSequence(LayoutInterval interval, LayoutInterval lower, int sizeIncrement, int alignment) {
        LayoutInterval parent = interval.getParent();
        assert parent.isSequential();
        LayoutRegion space = lower.getCurrentSpace();
        int increment = sizeIncrement;
        int pos = interval.getCurrentSpace().positions[dimension][alignment];
        int outPos = parent.getParent().getCurrentSpace().positions[dimension][alignment];

        boolean groupGrowingVisibly = groupGrowingVisibly(interval, lower, alignment);
        boolean parallel = false;
        boolean snapGap = false;
        int d = alignment == LEADING ? -1 : 1;
        int start = parent.indexOf(interval);
        int end = lower.isComponent() && !LayoutInterval.wantResize(lower) ? start : -1;
        int n = parent.getSubIntervalCount();

        for (int i=start+d; i >= 0 && i < n; i+=d) {
            LayoutInterval li = parent.getSubInterval(i);
            if (end != -1) { // consider parallel expansion of the sequence out
                             // of its parent (similar to what separateSequence does)
                int endPos = Integer.MIN_VALUE;
                if (!li.isEmptySpace()) {
                    if (LayoutUtils.contentOverlap(space, li, dimension^1)) {
                        // this stays in the way, can't parallelize over it
                        if (end != start) {
                            end = i - d;
                            endPos = li.getCurrentSpace().positions[dimension][alignment^1];
                        } else if (groupGrowingVisibly) {
                            endPos = space.positions[dimension][alignment];
                            if (!snapGap && LayoutInterval.wantResize(interval)) {
                                end = i - d;
                            }
                        } else { // there was only a gap before
                            end = -1;
                        }
                    } else { // no orthogonal overlap, count this in
                        end = i;
                        if (!parallel && LayoutUtils.contentOverlap(space, li, dimension)) {
                            parallel = true;
                        }
                    }
                } else {
                    snapGap = (i == start+d) && LayoutInterval.isFixedDefaultPadding(li);
                }
                if ((i == 0 || i+d == n) && endPos == Integer.MIN_VALUE && end != -1) {
                    // reached end, the last interval not in the way
                    if (end != start && (parallel || dimension == HORIZONTAL)) {
                        end = i;
                        endPos = outPos;
                    } else if (groupGrowingVisibly) {
                        endPos = space.positions[dimension][alignment];
                        if (!snapGap && LayoutInterval.wantResize(interval)) {
                            end = i;
                        }
                    } else {
                        end = -1; // only gap or not in parallel with anything (in vertical dimension)
                    }
                }
                if (endPos != Integer.MIN_VALUE) {
                    LayoutInterval toPar = lower.getParent().isSequential() ? lower.getParent() : lower;
                    if (end != start) {
                        LayoutInterval endGap = LayoutInterval.getDirectNeighbor(lower, alignment, false);
                        if (endGap == null && !LayoutInterval.isAlignedAtBorder(toPar, alignment)) {
                            endGap = new LayoutInterval(SINGLE);
                            if (!toPar.isSequential()) {
                                toPar = new LayoutInterval(SEQUENTIAL);
                                layoutModel.addInterval(toPar, lower.getParent(), layoutModel.removeInterval(lower));
                                layoutModel.setIntervalAlignment(toPar, lower.getRawAlignment());
                                layoutModel.setIntervalAlignment(lower, DEFAULT);
                                layoutModel.addInterval(lower, toPar, 0);
                            }
                            layoutModel.addInterval(endGap, toPar, alignment==LEADING ? 0 : -1);
                        } else if (toPar.isSequential() && endGap.getPreferredSize() == 0 && pos != outPos) {
                            layoutModel.setIntervalSize(endGap, NOT_EXPLICITLY_DEFINED, NOT_EXPLICITLY_DEFINED, endGap.getMaximumSize());
                        }
                    }
//                    else assert endGap == null || endGap.isEmptySpace();

                    operations.parallelizeWithParentSequence(toPar, end, dimension);

                    if (alignment == TRAILING) // adjust index
                        i -= n - parent.getSubIntervalCount();
                    n  = parent.getSubIntervalCount(); // adjust count
                    end = -1; // don't try anymore

                    increment -= Math.abs(endPos - pos);
                    if (increment < 0)
                        increment = 0;
                    continue;
                }
                else if (end == -1) { // no parallel expansion possible
                    i = start; // restart
//                    continue;
                }
            }
            // otherwise look for a gap to reduce
            else {
                if (li.isEmptySpace()
                        && (li.getPreferredSize() != NOT_EXPLICITLY_DEFINED
                        || li.getMaximumSize() == Short.MAX_VALUE)) {
                    int pad = determinePadding(interval, li.getPaddingType(), dimension, alignment);
                    int currentSize = LayoutInterval.getCurrentSize(li, dimension);

                    int size = currentSize - increment;
                    if (size <= pad) {
                        size = NOT_EXPLICITLY_DEFINED;
                        increment -= currentSize - pad;
                    }
                    else increment = 0;

                    operations.resizeInterval(li, size);
                    if (LayoutInterval.wantResize(li) && LayoutInterval.wantResize(interval)) {
                        // cancel gap resizing if the neighbor is also resizing
                        layoutModel.setIntervalSize(li, li.getMinimumSize(), li.getPreferredSize(), USE_PREFERRED_SIZE);
                    }
//                    break;
                }
//                else { interval = li; }
                break;
            }
        }
        return sizeIncrement - increment;
    }

    private boolean groupGrowingVisibly(LayoutInterval group, LayoutInterval interval, int edge) {
        // assuming it's already known that 'interval' sticks out of 'group' (added or resized)
        List<LayoutInterval> l = LayoutUtils.edgeSubComponents(interval, edge, false);
        if (l.size() == 1) {
            LayoutInterval comp = l.get(0);
            l = LayoutUtils.edgeSubComponents(group, edge, true);
            if (l.size() > 1 || (l.size() == 1 && l.get(0) != comp)) {
                return true;
            }
        }
        return false;
//        if (dragger.isResizing(dimension)) {
//            int max = interval.getMaximumSize();
//            interval.setMaximumSize(USE_PREFERRED_SIZE); // temporary
//            boolean groupWouldGrow = LayoutInterval.contentWantResize(group);
//            interval.setMaximumSize(max);
//            return groupWouldGrow;
//        }
//        return false;
    }

    /**
     * This method aligns an interval (just simply added to the layout - so it
     * is already placed correctly where it should appear) in parallel with
     * another interval.
     * @return parallel group with aligned intervals if some aligning changes happened,
     *         null if addingInterval has already been aligned or could not be aligned
     */
    private LayoutInterval alignInParallel(LayoutInterval interval, LayoutInterval toAlignWith, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;

        if (toAlignWith.isParentOf(interval) // already aligned to parent
            || interval.isParentOf(toAlignWith)) // can't align with own subinterval
        {   // contained intervals can't be aligned
            return null;
        }
        else {
            LayoutInterval commonParent = LayoutInterval.getCommonParent(interval, toAlignWith);
            if (commonParent == null || commonParent.isSequential()) // can't align with interval in the same sequence
                return null;
        }

        // if not in same parallel group try to substitute interval with parent
        boolean resizing = LayoutInterval.wantResize(interval);
        LayoutInterval aligning = interval; // may be substituted with parent
        LayoutInterval parParent = LayoutInterval.getFirstParent(interval, PARALLEL);
        while (!parParent.isParentOf(toAlignWith)) {
            if (LayoutInterval.isAlignedAtBorder(aligning, parParent, alignment)) { // substitute with parent
//                // make sure parent space is up-to-date
//                parParent.getCurrentSpace().positions[dimension][alignment]
//                        = aligning.getCurrentSpace().positions[dimension][alignment];
                // allow parent resizing if substituting for resizing interval
                if (resizing && !LayoutInterval.canResize(parParent))
                    operations.enableGroupResizing(parParent);
                aligning = parParent;
                parParent = LayoutInterval.getFirstParent(aligning, PARALLEL);
            }
            else parParent = null;
            if (parParent == null) // not parent of toAlignWith
                return null; // can't align with interval from different branch
        }

        // hack: remove aligning interval temporarily not to influence next analysis
        LayoutInterval tempRemoved = aligning;
        while (tempRemoved.getParent() != parParent)
            tempRemoved = tempRemoved.getParent();
        int removedIndex = parParent.remove(tempRemoved);

        // check if we shouldn't rather align with a whole group (parent of toAlignWith)
        boolean alignWithParent = false;
        LayoutInterval alignParent;
        do {
            alignParent = LayoutInterval.getFirstParent(toAlignWith, PARALLEL);
            if (alignParent == null) {
                parParent.add(tempRemoved, removedIndex); // add back temporarily removed
                return null; // aligning with parent (the interval must be already aligned)
            }
            if (canSubstAlignWithParent(toAlignWith, dimension, alignment, dragger.isResizing())) {
                // toAlignWith is at border so we can perhaps use the parent instead
                if (alignParent == parParent) {
                    if (LayoutInterval.getNeighbor(aligning, alignment, false, true, false) == null) {
                        alignWithParent = true;
                    }
                }
                else toAlignWith = alignParent;
            }
        }
        while (toAlignWith == alignParent);

        parParent.add(tempRemoved, removedIndex); // add back temporarily removed

        if (alignParent != parParent)
            return null; // can't align (toAlignWith is too deep)

        if (aligning != interval) {
            if (!LayoutInterval.isAlignedAtBorder(toAlignWith, alignment)) {
                // may have problems with S-layout
                int dst = LayoutRegion.distance(aligning.getCurrentSpace(),
                                                toAlignWith.getCurrentSpace(),
                                                dimension, alignment, alignment)
                          * (alignment == TRAILING ? -1 : 1);
                if (dst > 0) { // try to eliminate effect of avoiding S-layout
                    // need to exclude 'interval' - remove it temporarily
                    tempRemoved = interval;
                    while (tempRemoved.getParent() != aligning)
                        tempRemoved = tempRemoved.getParent();
                    removedIndex = aligning.remove(tempRemoved);

                    operations.cutStartingGap(aligning, dst, dimension, alignment);

                    aligning.add(tempRemoved, removedIndex); // add back temporarily removed
                }
            }
            optimizeStructure = true;
        }

        // check congruence of effective alignment
        int effAlign1 = LayoutInterval.getEffectiveAlignment(toAlignWith, alignment);
//        int effAlign2 = LayoutInterval.getEffectiveAlignment(aligning, alignment);
//        if (effAlign1 == (alignment^1) /*&& effAlign2 != effAlign1*/) {
//            LayoutInterval gap = LayoutInterval.getDirectNeighbor(aligning, alignment, false);
//            if (gap != null && gap.isEmptySpace()) {
//                layoutModel.setIntervalSize(gap, NOT_EXPLICITLY_DEFINED, gap.getPreferredSize(), Short.MAX_VALUE);
//            }
//            gap = LayoutInterval.getDirectNeighbor(aligning, alignment^1, false);
//            if (gap != null && gap.isEmptySpace() && LayoutInterval.getDirectNeighbor(gap, alignment^1, true) == null) {
//                layoutModel.setIntervalSize(gap, USE_PREFERRED_SIZE, gap.getPreferredSize(), USE_PREFERRED_SIZE);
//            }
//        }

        int indent = LayoutRegion.distance(toAlignWith.getCurrentSpace(), interval.getCurrentSpace(),
                                           dimension, alignment, alignment);
        boolean onPlace = aligning != interval
                && LayoutRegion.distance(aligning.getCurrentSpace(), interval.getCurrentSpace(),
                                         dimension, alignment, alignment) == 0;
        if (indent != 0 && onPlace) {
            // if there's a gap next to indent, its size needs to be reduced (ALT_Indent02Test)
            LayoutInterval indentNeighbor = LayoutInterval.getDirectNeighbor(aligning, alignment, false);
            if (indentNeighbor != null && indentNeighbor.isEmptySpace()
                    && indentNeighbor.getPreferredSize() > 0) {
                int size = indentNeighbor.getPreferredSize() - Math.abs(indent);
                if (size < 0) {
                    size = NOT_EXPLICITLY_DEFINED;
                }
                operations.resizeInterval(indentNeighbor, size);
            }
        }

        // separate content that is out of the emerging group
        List<LayoutInterval> alignedList = new ArrayList<LayoutInterval>(2);
        List<List> remainder = new ArrayList<List>(2);
        int originalCount = parParent.getSubIntervalCount();

        int extAlign1 = extract(toAlignWith, alignedList, remainder, alignment);
        extract(aligning, alignedList, remainder, alignment);

        assert !alignWithParent || remainder.isEmpty();

        // add indent if needed
        if (indent != 0) {
            LayoutInterval indentGap = new LayoutInterval(SINGLE);
            indentGap.setSize(Math.abs(indent));
            // [need to use default padding for indent gap]
            LayoutInterval indented = onPlace ? aligning : interval;
            LayoutInterval parent = indented.getParent();
            if (parent == null || !parent.isSequential()) {
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                if (parent != null) {
                    layoutModel.addInterval(seq, parent, layoutModel.removeInterval(indented));
                }
                layoutModel.setIntervalAlignment(indented, DEFAULT);
                layoutModel.addInterval(indented, seq, 0);
                parent = seq;
            }
            layoutModel.addInterval(indentGap, parent, alignment == LEADING ? 0 : -1);
            if (interval == aligning) {
                alignedList.set(alignedList.size()-1, parent);
            }
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
                if (effAlign1 == LEADING || effAlign1 == TRAILING) {
                    commonSeq.setAlignment(effAlign1);
                }
                layoutModel.addInterval(commonSeq, parParent, -1);
//                commonSeq.getCurrentSpace().set(dimension, parParent.getCurrentSpace());
            }
            else {
                commonSeq = null;
                if (effAlign1 == LEADING || effAlign1 == TRAILING) {
                    group.setAlignment(effAlign1);
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
        LayoutInterval aligning2 = alignedList.get(1);
        if (aligning2.getParent() != group) {
            if (aligning2.getParent() != null) {
                layoutModel.removeInterval(aligning2);
            }
            layoutModel.addInterval(aligning2, group, -1);
        }
        if (!LayoutInterval.isAlignedAtBorder(aligning2, alignment)) {
            layoutModel.setIntervalAlignment(aligning2, alignment);
        }

        LayoutInterval aligning1 = alignedList.get(0);
        if (aligning1.getParent() != group) {
            if (aligning1.getParent() != null) {
//                layoutModel.setIntervalAlignment(aligning1, extAlign1); //aligning1.getAlignment()); // remember explicit alignment
                layoutModel.removeInterval(aligning1);
            }
            layoutModel.addInterval(aligning1, group, -1);
            if (group == parParent && effAlign1 == alignment
                    && !LayoutInterval.isAlignedAtBorder(aligning1, group, effAlign1)) {
                layoutModel.setIntervalAlignment(aligning1, effAlign1); // not to lose original alignment in reused group
            }
        }

//        if (!dragger.isResizing(dimension) && group.getSubIntervalCount() == 2) {
//            layoutModel.setGroupAlignment(group, alignment);
//            layoutModel.setIntervalAlignment(aligning1, DEFAULT);
//            layoutModel.setIntervalAlignment(aligning2, DEFAULT);
//        } else if (!LayoutInterval.isAlignedAtBorder(aligning2, alignment)) {
//            layoutModel.setIntervalAlignment(aligning2, alignment);
//        }
        if ((!dragger.isResizing(dimension) || dragger.getResizingEdge(dimension) != alignment)
                && group.getSubIntervalCount() == 2) {
            if (!LayoutInterval.isAlignedAtBorder(aligning1, alignment)
                    && !LayoutInterval.isAlignedAtBorder(aligning2, alignment^1)) {
                layoutModel.setIntervalAlignment(aligning1, alignment);
            }
            if (LayoutInterval.isAlignedAtBorder(aligning1, group.getGroupAlignment())
                    && LayoutInterval.isAlignedAtBorder(aligning2, group.getGroupAlignment())) {
                layoutModel.setIntervalAlignment(aligning1, DEFAULT);
                layoutModel.setIntervalAlignment(aligning2, DEFAULT);
            } else if (LayoutInterval.isAlignedAtBorder(aligning1, alignment)
                    && LayoutInterval.isAlignedAtBorder(aligning2, alignment)) {
                layoutModel.setGroupAlignment(group, alignment);
                layoutModel.setIntervalAlignment(aligning1, DEFAULT);
                layoutModel.setIntervalAlignment(aligning2, DEFAULT);
            }
        }

        // create the remainder group next to the aligned group
        if (!remainder.isEmpty()) {
            int index = commonSeq.indexOf(group);
            if (alignment == TRAILING)
                index++;
            LayoutInterval sideGroup = operations.addGroupContent(
                    remainder, commonSeq, index, dimension, alignment/*, effAlign*/);
            if (sideGroup != null) {
                int pos1 = parParent.getCurrentSpace().positions[dimension][alignment];
                int pos2 = toAlignWith.getCurrentSpace().positions[dimension][alignment];
                sideGroup.getCurrentSpace().set(dimension,
                                                alignment == LEADING ? pos1 : pos2,
                                                alignment == LEADING ? pos2 : pos1);
                operations.optimizeGaps(sideGroup, dimension);
                operations.mergeParallelGroups(sideGroup);
            }
        }

        if (toAlignWith.isParallel()) { // try to reduce possible unnececssary nesting
            operations.dissolveRedundantGroup(toAlignWith);
        }

        return group;
    }

    private int extract(LayoutInterval interval, List<LayoutInterval> toAlign, List<List> toRemain, int alignment) {
        int effAlign = LayoutInterval.getEffectiveAlignment(interval, alignment);
        LayoutInterval parent = interval.getParent();
        if (parent.isSequential()) {
            int extractCount = operations.extract(interval, alignment, false,
                                                  alignment == LEADING ? toRemain : null,
                                                  alignment == LEADING ? null : toRemain);
            if (extractCount == 1) { // the parent won't be reused
//                if (effAlign == LEADING || effAlign == TRAILING)
//                    layoutModel.setIntervalAlignment(interval, effAlign);
                layoutModel.removeInterval(parent);
                toAlign.add(interval);
            }
            else { // we'll reuse the parent sequence in the new group
//                if (LayoutInterval.getEffectiveAlignment(parent, alignment) != alignment) {
//                    layoutModel.setIntervalAlignment(parent, alignment);
//                }
                toAlign.add(parent);
            }
        }
        else {
            toAlign.add(interval);
        }
        return effAlign;
    }

    /**
     * Detect when an adjusting resizing interval was created over a fixed area
     * that requires to suppress resizing of the parent group.
     */
    private void checkParallelResizing(List<LayoutInterval> added, IncludeDesc iDesc1, IncludeDesc iDesc2) {
        boolean one = added.size() == 1;
        LayoutInterval interval = added.get(0);
        LayoutInterval parallelInt;
        LayoutInterval group = interval.getParent();
        if (group.isSequential()) {
            parallelInt = group;
            group = group.getParent();
        } else  {
            parallelInt = interval;
        }

        // find resizing neighbor gap of added interval
        LayoutInterval neighborGap = null;
        if (interval != parallelInt) {
            assert parallelInt.isSequential();
            LayoutInterval gap = LayoutInterval.getDirectNeighbor(interval, LEADING, false); // added.get(0)
            if (gap != null && gap.isEmptySpace() && LayoutInterval.canResize(gap)) {
                neighborGap = gap;
            } else {
                gap = LayoutInterval.getDirectNeighbor(added.get(added.size()-1), TRAILING, false);
                if (gap != null && gap.isEmptySpace() && LayoutInterval.canResize(gap)) {
                    neighborGap = gap;
                }
            }
//            for (int i=LEADING; i <= TRAILING; i++) {
//                LayoutInterval gap = LayoutInterval.getDirectNeighbor(interval, i, false);
//                if (gap != null && gap.isEmptySpace() && LayoutInterval.canResize(gap)) {
//                    neighborGap = gap;
//                    break;
//                }
//            }
        }

        // one interval resized to resizing or a resizing neighbor gap created
        if (one && LayoutInterval.wantResize(interval)) {
            if (!dragger.isResizing(dimension)) {
                return;
            }
        } else if (neighborGap == null) {
            return;
        }
        // Now we know a resizing interval was created over the parallel group.
        // We may want to adjust resizing of the group (e.g. suppress it if the
        // content is otherwise fixed) or eliminate resizing gaps that are no
        // longer needed for size definition of the group.

        // do nothing in root and in parallel group tied closely to root on both edges
        int rootAlign = DEFAULT;
        if (group.getParent() == null) {
            rootAlign = LayoutRegion.ALL_POINTS;
        } else {
            if (iDesc1.snappedNextTo != null && iDesc1.snappedNextTo.getParent() == null) {
                rootAlign = iDesc1.alignment;
            }
            if (iDesc2 != null && iDesc2.snappedNextTo != null && iDesc2.snappedNextTo.getParent() == null) {
                rootAlign = rootAlign == DEFAULT ? iDesc2.alignment : LayoutRegion.ALL_POINTS;
            }
            if (rootAlign == LEADING || rootAlign == TRAILING) {
                // one edge snapped next to root - check the other one for full span
                int remIdx = group.remove(parallelInt); // temporarily
                LayoutInterval neighbor = LayoutInterval.getNeighbor(group, rootAlign^1, false, true, true);
                if ((neighbor != null
                     && neighbor.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                     && LayoutInterval.getEffectiveAlignmentInParent(group, LayoutInterval.getRoot(group), rootAlign^1) == (rootAlign^1))
//                     && LayoutInterval.isAlignedAtBorder(neighbor.getParent(), LayoutInterval.getRoot(neighbor), rootAlign^1))
                     ||
                    (neighbor == null
                     && LayoutInterval.isAlignedAtBorder(group, LayoutInterval.getRoot(group), rootAlign^1)))
                {   // the other group edge tied closely to root
                    rootAlign = LayoutRegion.ALL_POINTS;
                }
                group.add(parallelInt, remIdx);
            }
        }
//        if (rootAlign == LayoutRegion.ALL_POINTS)
//            return;

//        if (rootAlign != LayoutRegion.ALL_POINTS || LayoutInterval.getCount(group, LayoutRegion.ALL_POINTS, true) >= 2) {
//            if (neighborGap != null) { // resizing neighbor gap created
//                layoutModel.setIntervalSize(neighborGap, NOT_EXPLICITLY_DEFINED, NOT_EXPLICITLY_DEFINED, Short.MAX_VALUE);
//            } else if (interval.isComponent()) { // resizing component
//                java.awt.Dimension sizeLimit;
//                LayoutComponent lc = interval.getComponent();
//                sizeLimit = lc.isLayoutContainer() ?
//                    operations.getMapper().getComponentMinimumSize(lc.getId()) :
//                    operations.getMapper().getComponentPreferredSize(lc.getId());
//                int pref = dimension == HORIZONTAL ? sizeLimit.width : sizeLimit.height;
//                if (interval.getPreferredSize() < pref) {
//                    // if currently smaller than default, it needs 0 preferred size
//                    layoutModel.setIntervalSize(interval, NOT_EXPLICITLY_DEFINED, 0, interval.getMaximumSize());
//                } else {
//                    layoutModel.setIntervalSize(interval,
//                            interval.getMinimumSize() != USE_PREFERRED_SIZE ? interval.getMinimumSize() : NOT_EXPLICITLY_DEFINED,
//                            NOT_EXPLICITLY_DEFINED,
//                            interval.getMaximumSize());
//                }
//            }
//        }

        if (rootAlign != LayoutRegion.ALL_POINTS) {
            if (!LayoutInterval.canResize(group)
                && ((iDesc1.snappedNextTo != null && !group.isParentOf(iDesc1.snappedNextTo))
                     || (iDesc2 != null && iDesc2.snappedNextTo != null && !group.isParentOf(iDesc2.snappedNextTo))))
            {   // snapped out of the group - it might not want to be suppressed (will check right away)
                operations.enableGroupResizing(group);
            }

    //        if (LayoutInterval.canResize(group) && group.getParent() != null) {
            // suppress par. group resizing if it is otherwise fixed
            while (LayoutInterval.canResize(group) && group.getParent() != null) {
                boolean otherResizing = false;
                boolean samePosition = false;
                boolean onEdge = true;
                for (int i=0; i < group.getSubIntervalCount(); i++) {
                    LayoutInterval li = group.getSubInterval(i);
                    if (li != parallelInt) {
                        if (LayoutInterval.wantResize(li)) {
                            otherResizing = true;
                            break;
                        }
                        if (group.isParallel() && !samePosition) {
                            int align = li.getAlignment();
                            if (align == LEADING || align == TRAILING)
                                samePosition = getExpectedBorderPosition(parallelInt, dimension, align^1)
                                // [adding space instead of parallelInt's space does not work with indent]
                                               == getExpectedBorderPosition(li, dimension, align^1);
                        }
                    } else if (group.isSequential() && i != 0 && i+1 != group.getSubIntervalCount()) {
                        onEdge = false;
                        break;
                    }
                }
                if (otherResizing || !onEdge) {
                    break;
                }  else if (samePosition) {
                    operations.suppressGroupResizing(group);
                    break;
                }
                parallelInt = group;
                group = group.getParent();
            }
    //        }

            if (!LayoutInterval.canResize(group)) {
                // reset explicit size of interval or gap - subordinate to fixed content
                if (neighborGap != null) { // resizing neighbor gap created
                    layoutModel.setIntervalSize(neighborGap, NOT_EXPLICITLY_DEFINED, NOT_EXPLICITLY_DEFINED, Short.MAX_VALUE);
//                } else if (interval.isComponent()) { // resizing component
//                    java.awt.Dimension sizeLimit;
//                    LayoutComponent lc = interval.getComponent();
//                    sizeLimit = lc.isLayoutContainer() ?
//                        operations.getMapper().getComponentMinimumSize(lc.getId()) :
//                        operations.getMapper().getComponentPreferredSize(lc.getId());
//                    int pref = dimension == HORIZONTAL ? sizeLimit.width : sizeLimit.height;
//                    if (interval.getPreferredSize() < pref) {
//                        // if currently smaller than default, it needs 0 preferred size
//                        layoutModel.setIntervalSize(interval, NOT_EXPLICITLY_DEFINED, 0, interval.getMaximumSize());
//                    } else {
//                        layoutModel.setIntervalSize(interval,
//                                interval.getMinimumSize() != USE_PREFERRED_SIZE ? interval.getMinimumSize() : NOT_EXPLICITLY_DEFINED,
//                                NOT_EXPLICITLY_DEFINED,
//                                interval.getMaximumSize());
//                    }
                }
                if (unresizedOnRemove != null && unresizedOnRemove[dimension] != null) {
                    // resizing of some intervals may be restored (those made fixed
                    // when temporary removed the manipulated ones)
                    for (LayoutInterval li : unresizedOnRemove[dimension]) {
                        if (!LayoutInterval.canResize(li) && group.isParentOf(li)) {
                            boolean l = LayoutInterval.isPlacedAtBorder(li, group, dimension, LEADING);
                            boolean t = LayoutInterval.isPlacedAtBorder(li, group, dimension, TRAILING);
                            if ((li.getParent() == group && l && t)
                                    || (li.getParent() != group && (l || t))) {
                                if (li.isParallel()) {
                                    operations.enableGroupResizing(li);
                                } else if (li.isSingle()) {
                                    operations.setIntervalResizing(li, true);
                                }
                            }
                        }
                    }
                }
            }

            if (interval.isComponent() && neighborGap == null
                && (parallelInt == interval
                    || (parallelInt == interval.getParent()
                        && LayoutInterval.getCount(parallelInt, LayoutRegion.ALL_POINTS, true) == 1))) {
                // look for same sized components
                operations.setParallelSameSize(group, parallelInt, dimension);
            }
        }

//        eliminateParallelResizingGaps(interval);
        operations.completeGroupResizing(group, dimension);
    }

    private void eliminateParallelResizingGaps(LayoutInterval interval) {
        if (interval.getParent().isSequential()) {
            interval = interval.getParent();
        }
        LayoutInterval parent = interval.getParent();
        assert parent.isParallel() && LayoutInterval.wantResize(interval);

        for (Iterator<LayoutInterval> it = parent.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval sibling = it.next();
            if (sibling != interval && !sibling.isParentOf(interval) && sibling.isSequential()) {
                LayoutInterval resGap = null;
                int gapIndex = -1;
                int count = sibling.getSubIntervalCount();
                for (int i=0; i < count; i++) {
                    LayoutInterval li = sibling.getSubInterval(i);
                    if (LayoutInterval.wantResize(li)) {
                        if (/*(i == 0 || i == sibling.getSubIntervalCount()-1)
                                &&*/ li.isEmptySpace() && resGap == null) {
                            resGap = li;
                            gapIndex = i;
                        } else {
                            resGap = null;
                            break; // something else than just one ending gap is resizing
                        }
                    }
                }
                if (resGap != null) {
                    if (gapIndex == 0 || gapIndex == count-1) { // ending gap
                        if (resGap.getMinimumSize() != 0) { // change to fixed unaligned padding
                            layoutModel.setIntervalSize(resGap,
                                    NOT_EXPLICITLY_DEFINED, NOT_EXPLICITLY_DEFINED, USE_PREFERRED_SIZE);
                        } else {
                            layoutModel.removeInterval(resGap);
                        }
                        int align = (gapIndex==0) ? TRAILING : LEADING;
                        if (sibling.getAlignment() != align) {
                            layoutModel.setIntervalAlignment(sibling, align);
                        }
                    } else if (resGap.getMinimumSize() != USE_PREFERRED_SIZE) { // assuming NOT_EXPLICITLY_DEFINED
                        layoutModel.setIntervalSize(resGap,
                                resGap.getMinimumSize(), resGap.getMinimumSize(), resGap.getMaximumSize());
                    }
                }
            }
        }
    }

//    private void completeGroupResizing(LayoutInterval group, int dimension) {
//        assert group.isParallel() && LayoutInterval.contentWantResize(group);
////        int groupSize = LayoutInterval.getCurrentSize(group, dimension);
//        List<LayoutInterval> list = null;
//        for (Iterator<LayoutInterval> it=group.getSubIntervals(); it.hasNext(); ) {
//            LayoutInterval li = it.next();
//            if (!li.isEmptySpace() && !LayoutInterval.wantResize(li)
//                    && (li.getAlignment() == LEADING || li.getAlignment() == TRAILING)) {
//                if (list == null) {
//                    list = new LinkedList<LayoutInterval>();
//                }
//                list.add(li);
//            }
//        }
//        if (list != null) {
////            LayoutInterval[] neighborGaps = new LayoutInterval[2];
////            for (int e=LEADING; e <= TRAILING; e++) {
////                LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(group, e, false);
////                if (neighbor != null && neighbor.isEmptySpace()) {
////                    neighborGaps[e] = neighbor;
////                }
////            }
//            for (LayoutInterval li : list) {
////                if (li.getParent() != null) {
////                int size = groupSize - LayoutInterval.getCurrentSize(li, dimension);
////                if (size < 0) { // e.g. if current size of li is not set yet
////                    size = 0;
////                }
//                    LayoutInterval gap = new LayoutInterval(SINGLE);
//                    gap.setMinimumSize(0);
//                    gap.setPreferredSize(0);
//                    gap.setMaximumSize(Short.MAX_VALUE);
//                    gap.setAttribute(LayoutInterval.ATTR_FLEX_SIZEDEF);
//                    operations.insertGap(gap, li,
//                            li.getCurrentSpace().positions[dimension][li.getAlignment()^1],
//                            dimension, li.getAlignment()^1);
////                }
//            }
//        }
//    }

    private int getExpectedBorderPosition(LayoutInterval interval, int dimension, int alignment) {
        LayoutInterval comp = LayoutUtils.getOutermostComponent(interval, dimension, alignment);
        int pos = comp.getCurrentSpace().positions[dimension][alignment];
        LayoutInterval neighbor = LayoutInterval.getNeighbor(comp, alignment, false, true, false);
        if (neighbor != null && neighbor.isEmptySpace() && interval.isParentOf(neighbor)) {
            int diff = neighbor.getPreferredSize();
            if (diff == NOT_EXPLICITLY_DEFINED)
                diff = LayoutUtils.getSizeOfDefaultGap(neighbor, operations.getMapper());
            if (alignment == LEADING)
                diff *= -1;
            pos += diff;
        }
        return pos;
    }

    private int determinePadding(LayoutInterval interval, PaddingType paddingType,
                                 int dimension, int alignment)
    {
        LayoutInterval neighbor = LayoutInterval.getNeighbor(interval, alignment, true, true, false);
        if (paddingType == null) {
            paddingType = PaddingType.RELATED;
        }
        return dragger.findPaddings(neighbor, interval, paddingType, dimension, alignment)[0];
        // need to go through dragger as the component of 'interval' is not in model yet
    }

    // -----

    private void analyzeParallel(LayoutInterval group, List<IncludeDesc> inclusions) {
        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval sub = (LayoutInterval) it.next();
            if (sub.isEmptySpace())
                continue;

            LayoutRegion subSpace = sub.getCurrentSpace();

            if (sub.isParallel() && shouldEnterGroup(sub)) {
                // group space contains significant edge
                analyzeParallel(sub, inclusions);
            } else if (sub.isSequential()) {
                // always analyze sequence - it may be valid even if there is no
                // overlap (not required in vertical dimension)
                analyzeSequential(sub, inclusions);
            } else {
                boolean ortOverlap = orthogonalOverlap(sub);
                int margin = (dimension == VERTICAL && !ortOverlap ? 4 : 0);
                boolean dimOverlap = LayoutRegion.overlap(addingSpace, subSpace, dimension, margin);
                if (ortOverlap || (dimension == VERTICAL && !dimOverlap)) {
                    int ortDistance = 0;
                    if (dimOverlap) { // overlaps in both dimensions
                        if (!solveOverlap && LayoutUtils.contentOverlap(addingSpace, sub)) {
                            continue;
                        }
                        imposeSize = true;
                    } else if (!ortOverlap) {
                        int dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                         dimension^1, TRAILING, LEADING);
                        int dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                         dimension^1, TRAILING, LEADING);
                        ortDistance = dstL >= 0 ? dstL : dstT;
                    }

                    int distance = LayoutRegion.UNKNOWN;
                    if (aSnappedNextTo != null) {
                        // check if aSnappedNextTo is related to this position with 'sub' as neighbor
                        LayoutInterval neighbor;
                        if (sub == aSnappedNextTo
                            || sub.isParentOf(aSnappedNextTo)
                            || aSnappedNextTo.getParent() == null
                            || (neighbor = LayoutInterval.getNeighbor(sub, aEdge, true, true, false)) == aSnappedNextTo
                            || (neighbor != null && neighbor.isParentOf(aSnappedNextTo)))
                        {   // nextTo snap is relevant to this position
                            distance = -1; // IncludeDesc.snappedNextTo will be set if distance == -1
                        }
                    }
                    if (distance != -1) {
                        if (!dimOverlap) { // determine distance from 'sub'
                            int dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                             dimension, TRAILING, LEADING);
                            int dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                             dimension, TRAILING, LEADING);
                            distance = dstL >= 0 ? dstL : dstT;
                        }
                        else distance = 0; // overlapping
                    }

                    IncludeDesc iDesc = addInclusion(group, false, distance, ortDistance, inclusions);
                    if (iDesc != null) {
                        iDesc.neighbor = sub;
                        iDesc.index = getAddDirection(addingSpace, subSpace, dimension, getAddingPoint()) == LEADING ? 0 : 1;
                    }
                }
            }
        }

        if (inclusions.isEmpty()) { // no inclusion found yet
            if (group.getParent() == null
                && (aSnappedParallel == null || canAlignWith(aSnappedParallel, group, aEdge)))
            {   // this is the last (top) valid group
                int distance = aSnappedNextTo == group ? -1 : Integer.MAX_VALUE;
                addInclusion(group, false, distance, Integer.MAX_VALUE, inclusions);
            }
        }
    }

    private void analyzeSequential(LayoutInterval group, List<IncludeDesc> inclusions) {
        boolean inSequence = false;
        boolean parallelWithSequence = false;
        int startIndex = -1, endIndex = -1;
        int distance = Integer.MAX_VALUE;
        int ortDistance = Integer.MAX_VALUE;

        for (int i=0,n=group.getSubIntervalCount(); i < n; i++) {
            LayoutInterval sub = group.getSubInterval(i);
            if (sub.isEmptySpace()) {
                if (startIndex == i) {
                    startIndex++;
                }
                continue;
            }

            LayoutRegion subSpace = sub.getCurrentSpace();

            // first analyze the interval as a possible sub-group
            if (sub.isParallel() && shouldEnterGroup(sub)) {
                // group space contains significant edge
                int count = inclusions.size();
                analyzeParallel(sub, inclusions);
                if (inclusions.size() > count)
                    return;
            }

            // second analyze the interval as a single element for "next to" placement
            boolean ortOverlap = orthogonalOverlap(sub);
            int margin = (dimension == VERTICAL && !ortOverlap ? 4 : 0);
            boolean dimOverlap = LayoutRegion.overlap(addingSpace, subSpace, dimension, margin);
            // in vertical dimension always pretend orthogonal overlap if there
            // is no overlap in horizontal dimension (i.e. force inserting into sequence)
            if (ortOverlap || (dimension == VERTICAL && !dimOverlap && !parallelWithSequence)) {
                if (dimOverlap) { // overlaps in both dimensions
                    if (!solveOverlap && LayoutUtils.contentOverlap(addingSpace, sub)) { // don't want to solve the overlap in this sequence
                        parallelWithSequence = true;
                        continue;
                    }
                    if (ortOverlap) {
                        imposeSize = true;
                    }
                    inSequence = true;
                    distance = ortDistance = 0;
                } else { // determine distance from the interval
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
                if (getAddDirection(addingSpace, subSpace, dimension, getAddingPoint()) == LEADING) {
                    endIndex = i;
                    break; // this interval is already after the adding one, no need to continue
                } else { // intervals before this one are irrelevant
                    parallelWithSequence = false;
                    startIndex = i + 1;
                }
            }
            else { // no orthogonal overlap, moreover in vertical dimension located parallelly
                parallelWithSequence = true;
            }
        }

        if (inSequence || (dimension == VERTICAL && !parallelWithSequence)) {
            if (startIndex < 0) {
                startIndex = 0;
            }
            if (endIndex < 0) {
                endIndex = group.getSubIntervalCount();
            }
            if (forwardIntoSubParallel(group, startIndex, endIndex, inclusions)) {
                return;
            }
            // so it make sense to add the interval to this sequence
            if (aSnappedNextTo != null) {
                if (group.isParentOf(aSnappedNextTo) || aSnappedNextTo.getParent() == null) {
                    distance = -1; // preferred distance
                } else {
                    LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(group.getParent(), aEdge, true);
                    if (neighbor != null
                        && (neighbor == aSnappedNextTo
                            || LayoutInterval.isAlignedAtBorder(aSnappedNextTo, neighbor, aEdge^1))) {
                        distance = -1; // preferred distance
                    }
                }
            }
            IncludeDesc iDesc = addInclusion(group, parallelWithSequence, distance, ortDistance, inclusions);
            if (iDesc != null) {
                iDesc.index = (aEdge == LEADING) ? startIndex : endIndex; //index;
            }
        }
    }

    private int getAddingPoint() {
        if (aEdge < 0) {
            return CENTER;
        } else if ((aEdge == LEADING || aEdge == TRAILING)
                   && aSnappedNextTo == null && aSnappedParallel == null) {
            // secondary edge that does not snap
            LayoutDragger.PositionDef primaryPos = newPositions[dimension];
            return primaryPos != null && primaryPos.snapped && primaryPos.alignment == (aEdge^1)
                    ? primaryPos.alignment : CENTER;
        }
        return aEdge;
    }

    /**
     * Checks whether addingInterval overlaps given interval in the orthogonal
     * dimension (overlap in visual coordinates). In some situations the adding
     * operation in the other dimension may eliminate the overlap in the
     * resulting layout; in such case the overlap is not counted.
     * @return true if there is a significant overlap in orthogonal dimension
     *         that should be considered in this dimension
     */
    private boolean orthogonalOverlap(LayoutInterval interval) {
        boolean ortOverlap;
        if (solveOverlap || !LayoutUtils.isOverlapPreventedInOtherDimension(addingInterval, interval, dimension)) {
            ortOverlap = LayoutUtils.contentOverlap(addingSpace, interval, dimension^1);
            if (ortOverlap
                && dragger.isResizing(dimension) && !dragger.isResizing(dimension^1)
                && originalPosition != null) {
                // In case of resizing only in one dimension don't consider
                // overlap that was not cared of already before the resizing
                // started (i.e. the resizing interval was not in sequence with
                // the interval in question).
                IncludeDesc original = originalPosition.desc1;
                LayoutInterval parent = original.parent;
                if (parent.isParentOf(interval)) {
                    if (parent.isParallel()
                        && (original.neighbor == null
                            || (original.neighbor != interval && !original.neighbor.isParentOf(interval))))
                        ortOverlap = false;
                }
                else if (parent == interval) {
                    if (parent.isParallel() && original.neighbor == null)
                        ortOverlap = false;
                }
                else if (!interval.isParentOf(parent)) {
                    parent = LayoutInterval.getCommonParent(parent, interval);
                    if (parent != null && parent.isParallel())
                        ortOverlap = false;
                }
            } else if (ortOverlap && !dragger.isResizing()) {
                // The overlap may also be avoided in the other dimension when
                // adding into baseline or center position.
                LayoutDragger.PositionDef otherDimPos = newPositions[dimension^1];
                if (otherDimPos != null && otherDimPos.snapped
                        && (otherDimPos.alignment == CENTER || otherDimPos.alignment == BASELINE)) {
                    // anticipating addSimplyAligned will be used
                    LayoutInterval ortAligned = otherDimPos.interval;
                    if (!ortAligned.isParallel()) {
                        LayoutInterval li = LayoutInterval.getFirstParent(ortAligned, PARALLEL);
                        if (li.getGroupAlignment() == otherDimPos.alignment) {
                            ortAligned = li;
                        }
                    }
                    // first check if the center/baseline components from the
                    // ort. dimension are part of the interval
                    boolean intervalAligned = false;
                    Iterator<LayoutInterval> it = LayoutUtils.getComponentIterator(ortAligned);
                    while (it.hasNext()) {
                        LayoutInterval li = it.next().getComponent().getLayoutInterval(dimension);
                        if (interval == li || interval.isParentOf(li)) {
                            intervalAligned = true; // so there is ort. overlap
                            break;
                        }
                    }
                    if (!intervalAligned) {
                        LayoutInterval ortInterval = LayoutUtils.getComponentIterator(interval).next()
                                .getComponent().getLayoutInterval(dimension^1);
                        if (LayoutInterval.getCommonParent(ortAligned, ortInterval).isSequential()) {
                            ortOverlap = false;
                        }
                    }
                }
            }
        } else { // Here the overlap has already been prevented in the other
                 // dimension (interval already added there).
            ortOverlap = false;
        }
        return ortOverlap;
    }

    private IncludeDesc addInclusion(LayoutInterval parent,
                                     boolean subgroup,
                                     int distance,
                                     int ortDistance,
                                     List<IncludeDesc> inclusions)
    {
        if (!inclusions.isEmpty()) {
            int index = inclusions.size() - 1;
            IncludeDesc last = inclusions.get(index);
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
            iDesc.paddingType = aPaddingType;
            iDesc.fixedPosition = true;
        }
        iDesc.distance = distance;
        iDesc.ortDistance = ortDistance;
        inclusions.add(iDesc);

        return iDesc;
    }

    /**
     * Adds an inclusion for parallel aligning if none of found non-overlapping
     * inclusions is compatible with the required aligning.
     * Later mergeParallelInclusions may still unify the inclusions, but if not
     * then the inclusion created here is used - because requested parallel
     * aligning needs to be preserved even if overlapping can't be avoided.
     */
    private IncludeDesc addAligningInclusion(List<IncludeDesc> inclusions) {
        if (aSnappedParallel == null)
            return null;

        boolean compatibleFound = false;
        for (Iterator it=inclusions.iterator(); it.hasNext(); ) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (canAlignWith(aSnappedParallel, iDesc.parent, aEdge)) {
                compatibleFound = true;
                break;
            }
        }
        if (!compatibleFound) {
            IncludeDesc iDesc = new IncludeDesc();
            if (!aSnappedParallel.isParallel()) {
                // When the component to align with spans the whole parallel group,
                // it may be desirable to align with the whole group instead to:
                // - not influence its size (if there's anything resizable),
                // - preserve the group (not creating unnecessary subgroup in it).
                LayoutInterval parent = LayoutInterval.getFirstParent(aSnappedParallel, PARALLEL);
                if (parent != null && parent.getParent() != null
                        && LayoutInterval.isPlacedAtBorder(aSnappedParallel, parent, dimension, aEdge)
                        && ((LayoutInterval.contentWantResize(parent) && !LayoutInterval.wantResize(addingInterval))
                            || !LayoutInterval.isAlignedAtBorder(aSnappedParallel, parent, aEdge))) {
                    iDesc.snappedParallel = parent;
                    iDesc.parent = LayoutInterval.getFirstParent(parent, PARALLEL);
                }
            }
            if (iDesc.snappedParallel == null) {
                iDesc.snappedParallel = aSnappedParallel;
                iDesc.parent = LayoutInterval.getFirstParent(aSnappedParallel, PARALLEL);
            }
            if (iDesc.parent == null) {
                iDesc.parent = aSnappedParallel;
            }
            iDesc.alignment = aEdge;
            inclusions.add(0, iDesc);
            return iDesc;
        }
        else return null;
    }

    private boolean forwardIntoSubParallel(LayoutInterval seq, int startIndex, int endIndex, List<IncludeDesc> inclusions) {
        if (dragger.isResizing(dimension) || aSnappedParallel != null) {
            return false;
        }

        LayoutInterval[] neighbors = new LayoutInterval[2];
        boolean aimsToGroup[] = new boolean[2];
        for (int e=LEADING; e <= TRAILING; e++) {
            if (aSnappedNextTo != null && e == (aEdge^1)) {
                continue;
            }
            LayoutInterval neighbor = null;
            if (e == LEADING) {
                if (startIndex-1 >= 0) {
                    neighbor = seq.getSubInterval(startIndex-1);
                    if (neighbor.isEmptySpace() && startIndex-2 >= 0) {
                        neighbor = seq.getSubInterval(startIndex-2);
                    }
                }
            } else if (endIndex < seq.getSubIntervalCount()) {
                neighbor = seq.getSubInterval(endIndex);
            }
            if (neighbor != null && neighbor.isParallel()) {
                neighbors[e] = neighbor;
                aimsToGroup[e] = addingOverGroupEdge(neighbor, e^1);
            }
        }

        if (dimension == VERTICAL && !aimsToGroup[LEADING] && !aimsToGroup[TRAILING]) {
            return false;
        }

        int tryFirst;
        if (neighbors[LEADING] != null && neighbors[TRAILING] != null
                && aimsToGroup[LEADING] == aimsToGroup[TRAILING]) {
            int d1 = LayoutRegion.distance(neighbors[LEADING].getCurrentSpace(), addingSpace,
                                           dimension, TRAILING, LEADING);
            int d2 = LayoutRegion.distance(addingSpace, neighbors[TRAILING].getCurrentSpace(),
                                           dimension, TRAILING, LEADING);
            tryFirst = d1 <= d2 ? LEADING : TRAILING;
        } else if (aimsToGroup[LEADING]) {
            tryFirst = (!aimsToGroup[TRAILING] || aEdge != TRAILING) ? LEADING : TRAILING;
        } else {
            tryFirst = !aimsToGroup[TRAILING] ? LEADING : TRAILING;
        }
        for (int e=tryFirst, edgeCount=2; edgeCount > 0; edgeCount--, e^=1) {
            LayoutInterval neighbor = neighbors[e];
            if (neighbor != null && groupOpenToEnter(neighbor, e^1)) {
                int count = inclusions.size();
                analyzeParallel(neighbor, inclusions);
                if (inclusions.size() > count) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean addingOverGroupEdge(LayoutInterval group, int alignment) {
        int[] apos = addingSpace.positions[dimension];
        int[] gpos = group.getCurrentSpace().positions[dimension];
        return apos[LEADING] < gpos[alignment] && apos[TRAILING] > gpos[alignment];
    }

    private boolean stickingOutOfGroup(LayoutInterval group, int alignment) {
        int[] apos = addingSpace.positions[dimension];
        int[] gpos = group.getCurrentSpace().positions[dimension];
        return alignment == LEADING ? apos[LEADING] < gpos[LEADING] : apos[TRAILING] > gpos[TRAILING];
    }

    private boolean hasOpenRoomForAdding(LayoutInterval group, int alignment) {
        assert group.isParallel();
        LayoutRegion groupSpace = group.getCurrentSpace();
        for (LayoutInterval comp : LayoutUtils.edgeSubComponents(group, alignment, false)) {
            LayoutRegion compSpace = comp.getCurrentSpace();
            if (LayoutRegion.overlap(addingSpace, compSpace, dimension^1, 0)
                && compSpace.positions[dimension][alignment] == groupSpace.positions[dimension][alignment]) {
                return false;
            }
        }
        return true;
    }

    /**
     * The inclusion determined by analyzeParallel method is always the most open
     * possible at given location. However, at some specific conditions a closed
     * position can be preferred (i.e. placing the component into a parallel
     * group instead of independently with it). In such case this methods
     * modifies the given IncludeDesc object and returns true.
     */
    private boolean preferClosedPosition(IncludeDesc newDesc) {
        if (originalPosition == null) {
            return false;
        }

        // Check if something is moved within a closed group (typically moved
        // vertically within a column).
        IncludeDesc origDesc = originalPosition.desc1;
        if (origDesc != null && originalPosition.closedSpace != null) {
            LayoutInterval origParent = origDesc.parent;
            if (origParent.isSequential() && !origDesc.newSubGroup) {
                origParent = origParent.getParent();
            }
            if ((newDesc.parent == origParent || newDesc.parent.isParentOf(origParent))
                  && LayoutRegion.pointInside(addingSpace, LEADING, originalPosition.closedSpace, dimension)
                  && LayoutRegion.pointInside(addingSpace, TRAILING, originalPosition.closedSpace, dimension)
                  && newDesc.neighbor == origDesc.neighbor
                  && newDesc.snappedNextTo == null
                  && (newDesc.snappedParallel == null || newDesc.snappedParallel == origParent
                      || origParent.isParentOf(newDesc.snappedParallel))) {
                newDesc.parent = origParent;
                newDesc.index = origDesc.parent == origParent ? origDesc.index : -1;
                newDesc.newSubGroup = origDesc.newSubGroup;
                closedSpace = new LayoutRegion(originalPosition.closedSpace);
                closedSpace.set(dimension^1, addingSpace);
                return true;
            }
        }

        // Check if a resizing component is moved from a group where everything
        // is resizing to a similar group (typically the case of groups with
        // suppresed resizing).
        if (!dragger.isResizing()
                && originalPosition.wholeResizing
                && newDesc.snappedParallel != null && newDesc.neighbor == null) {
            LayoutInterval snapParent;
            if (newDesc.snappedParallel.isParallel()) {
                snapParent = newDesc.snappedParallel;
            } else {
                snapParent = LayoutInterval.getFirstParent(newDesc.snappedParallel, PARALLEL);
                if (!LayoutInterval.isAlignedAtBorder(newDesc.snappedParallel, snapParent, aEdge)) {
                    snapParent = null;
                }
            }
            if (snapParent != null
                    && newDesc.parent.isParentOf(snapParent)
                    && (!LayoutInterval.canResize(snapParent)
                        || (!originalPosition.suppressedResizing && LayoutInterval.wantResize(snapParent)))) {
                newDesc.parent = snapParent;
                newDesc.index = -1;
                newDesc.neighbor = null;
                newDesc.newSubGroup = false;
                closedSpace = new LayoutRegion(snapParent.getCurrentSpace());
                closedSpace.set(dimension^1, addingSpace);
                if (originalPosition.suppressedResizing && originalPosition.wholeResizing) {
                    int defSizeDef = LayoutInterval.getDefaultSizeDef(addingInterval);
                    if (addingInterval.getPreferredSize() != defSizeDef) {
                        operations.resizeInterval(addingInterval, defSizeDef);
                    }
                    int overSize = addingInterval.getDiffToDefaultSize();
                    if (overSize > 0) {
                        if (newDesc.alignment == TRAILING) {
                            addingInterval.getCurrentSpace().reshape(dimension, LEADING, overSize);
                        } else {
                            addingInterval.getCurrentSpace().reshape(dimension, TRAILING, -overSize);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @param preserveOriginal if true, original inclusion needs to be preserved,
     *        will be merged with new inclusion sequentially; if false, original
     *        inclusion is just consulted when choosing best inclusion
     */
    private void mergeParallelInclusions(List<IncludeDesc> inclusions, IncludeDesc original, boolean preserveOriginal) {
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
                else {
                    best = iDesc;
                    bestOriginal = original != null && !preserveOriginal && iDesc.parent == original.parent;
                }
            }
        }

        if (best == null) { // nothing compatible with original position
            assert preserveOriginal;
            inclusions.clear();
            inclusions.add(original);
            return;
        }

        LayoutInterval commonGroup = best.parent.isSequential() ? best.parent.getParent() : best.parent;

        // 2nd remove incompatible inclusions, move compatible ones to same level
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
                        if (group.getSubIntervalCount() == 1 && group.getParent() != null) {
                            LayoutInterval parent = group.getParent();
                            LayoutInterval last = layoutModel.removeInterval(group, 0);
                            operations.addContent(last, parent, layoutModel.removeInterval(group), dimension);
                            if (commonGroup == last && commonGroup.getParent() == null) // commonGroup dissolved in parent
                                commonGroup = parent;
                            updateReplacedOriginalGroup(commonGroup, null);
                        }
                        if (iDesc.parent == group)
                            iDesc.parent = commonGroup;
                    }
                }
            }
        }

        if (best.parent.isParallel() && best.snappedParallel != null && best.ortDistance != 0
            && inclusions.size() > 1)
        {   // forced inclusion by addAlignedInclusion
            inclusions.remove(best);
        }
        if (inclusions.size() == 1)
            return;

        // 3rd analyse inclusions requiring a subgroup (parallel with part of sequence)
        LayoutInterval subGroup = null;
        LayoutInterval nextTo = null;
        List<List> separatedLeading = new LinkedList<List>();
        List<List> separatedTrailing = new LinkedList<List>();

        for (Iterator it=inclusions.iterator(); it.hasNext(); ) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (iDesc.parent.isSequential() && iDesc.newSubGroup) {
                LayoutInterval parSeq = extractParallelSequence(iDesc.parent, addingSpace, -1, -1, iDesc.alignment);
                assert parSeq.isParallel(); // parallel group with part of the original sequence
                if (subGroup == null) {
                    subGroup = parSeq;
                }
                else {
                    LayoutInterval sub = layoutModel.removeInterval(parSeq, 0);
                    layoutModel.addInterval(sub, subGroup, -1);
                }
                // extract surroundings of the group in the sequence
                operations.extract(parSeq, DEFAULT, true, separatedLeading, separatedTrailing);
                layoutModel.removeInterval(parSeq);
                layoutModel.removeInterval(iDesc.parent);
            }
        }

        int extractAlign = DEFAULT;
        if (subGroup != null) {
            if (separatedLeading.isEmpty())
                extractAlign = TRAILING;
            if (separatedTrailing.isEmpty())
                extractAlign = LEADING;
        }

        // 4th collect surroundings of adding interval
        // (the intervals will go into a side group in step 5, or into subgroup
        //  of 'subGroup' next to the adding interval if in previous step some
        //  content was separated into a parallel subgroup of a sequence)
        LayoutInterval subsubGroup = null;
        for (Iterator it=inclusions.iterator(); it.hasNext(); ) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (iDesc.parent.isParallel() || !iDesc.newSubGroup) {
                addToGroup(iDesc, null, false);
                operations.extract(addingInterval, extractAlign, extractAlign == DEFAULT,
                                   separatedLeading, separatedTrailing);
                LayoutInterval parent = addingInterval.getParent();
                layoutModel.removeInterval(addingInterval);
                layoutModel.removeInterval(parent);
                if (extractAlign != DEFAULT && LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true) >= 1) {
                    if (subsubGroup == null) {
                        subsubGroup = new LayoutInterval(PARALLEL);
                        subsubGroup.setGroupAlignment(extractAlign);
                    }
                    operations.addContent(parent, subsubGroup, -1, dimension);
                }
            }
            if (iDesc.snappedNextTo != null)
                nextTo = iDesc.snappedNextTo;
            if (iDesc != best)
                it.remove();
        }

        // prepare the common group for merged content
        int[] borderPos = commonGroup.getCurrentSpace().positions[dimension];
        int[] neighborPos = (subGroup != null ? subGroup : addingInterval).getCurrentSpace().positions[dimension];
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
        updateReplacedOriginalGroup(commonGroup, commonSeq);

        // 5th create groups of merged content around the adding component
        LayoutInterval sideGroupLeading = null;
        LayoutInterval sideGroupTrailing = null;
        if (!separatedLeading.isEmpty()) {
            int checkCount = commonSeq.getSubIntervalCount(); // remember ...
            sideGroupLeading = operations.addGroupContent(
                    separatedLeading, commonSeq, index, dimension, LEADING); //, mainEffectiveAlign
            index += commonSeq.getSubIntervalCount() - checkCount;
        }
        if (!separatedTrailing.isEmpty()) {
            sideGroupTrailing = operations.addGroupContent(
                    separatedTrailing, commonSeq, index, dimension, TRAILING); //, mainEffectiveAlign
        }
        if (sideGroupLeading != null) {
            int checkCount = commonSeq.getSubIntervalCount(); // remember ...
            sideGroupLeading.getCurrentSpace().set(dimension, borderPos[LEADING], neighborPos[LEADING]);
            operations.optimizeGaps(sideGroupLeading, dimension);
            index += commonSeq.getSubIntervalCount() - checkCount;
        }
        if (sideGroupTrailing != null) {
            sideGroupTrailing.getCurrentSpace().set(dimension, neighborPos[TRAILING], borderPos[TRAILING]);
            operations.optimizeGaps(sideGroupTrailing, dimension);
        }

        // 6th adjust the final inclusion
        best.parent = commonSeq;
        best.newSubGroup = false;
        best.neighbor = null;

        LayoutInterval separatingGap;
        int gapIdx = index;
        if (gapIdx == commonSeq.getSubIntervalCount()) {
            gapIdx--;
            separatingGap = commonSeq.getSubInterval(gapIdx);
        }
        else {
            separatingGap = commonSeq.getSubInterval(gapIdx);
            if (!separatingGap.isEmptySpace()) {
                gapIdx--;
                if (gapIdx > 0)
                    separatingGap = commonSeq.getSubInterval(gapIdx);
            }
        }
        if (!separatingGap.isEmptySpace())
            separatingGap = null;
        else if (subGroup == null) {
            index = gapIdx;
            // eliminate the gap if caused by addToGroup called to separate adding
            // interval's surroundings to side groups; the gap will be created
            // again when addToGroup is called definitively (for merged inclusions)
            if (index == 0 && !LayoutInterval.isAlignedAtBorder(commonSeq, LEADING)) {
                layoutModel.removeInterval(separatingGap);
                separatingGap = null;
            }
            else if (index == commonSeq.getSubIntervalCount()-1 && !LayoutInterval.isAlignedAtBorder(commonSeq, TRAILING)) {
                layoutModel.removeInterval(separatingGap);
                separatingGap = null;
            }
        }

        best.snappedNextTo = nextTo;
        if (nextTo != null)
            best.fixedPosition = true;

        // 7th resolve subgroup
        if (subGroup != null) {
            if (separatingGap != null
                && (extractAlign == DEFAULT
                    || (extractAlign == LEADING && index > gapIdx)
                    || (extractAlign == TRAILING && index <= gapIdx)))
            {   // subGroup goes next to a separating gap - which is likely superflous
                // (the extracted parallel sequence in subGroup has its own gap)
                layoutModel.removeInterval(separatingGap);
                if (index >= gapIdx && index > 0)
                    index--;
            }
            int subIdx = index;
            if (subsubGroup != null && subsubGroup.getSubIntervalCount() > 0) {
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                seq.setAlignment(best.alignment);
                operations.addContent(subsubGroup, seq, 0, dimension);
                layoutModel.addInterval(seq, subGroup, -1);
                // [should run optimizeGaps on subsubGroup?]
                best.parent = seq;
                index = extractAlign == LEADING ? 0 : seq.getSubIntervalCount();
            }
            else {
                best.newSubGroup = true;
            }
            operations.addContent(subGroup, commonSeq, subIdx, dimension);

            updateMovedOriginalNeighbor();
        }

        best.index = index;
    }

    private static boolean compatibleInclusions(IncludeDesc iDesc1, IncludeDesc iDesc2, int dimension) {
        LayoutInterval group1 = iDesc1.parent.isSequential() ?
                                iDesc1.parent.getParent() : iDesc1.parent;
        LayoutInterval group2 = iDesc2.parent.isSequential() ?
                                iDesc2.parent.getParent() : iDesc2.parent;
        if (group1 == group2) {
            return true;
        }

        if (group1.isParentOf(group2)) {
            // swap so group2 is parent of group1 (iDesc1 the deeper inclusion)
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

        LayoutInterval neighbor; // to be moved into the deeper group (in parallel)
        if (iDesc2.parent.isSequential()) {
            if (iDesc2.parent.isParentOf(iDesc1.parent)) {
                // in the same sequence, can't combine in parallel
                return false;
            }
            neighbor = iDesc2.parent;
        } else {
            neighbor = iDesc2.neighbor;
        }
        if (neighbor == null) {
            return false;
        }
        LayoutRegion spaceToHold = new LayoutRegion(neighbor.getCurrentSpace());
        LayoutInterval lComp = LayoutUtils.getOutermostComponent(neighbor, dimension, 0);
        LayoutInterval tComp = LayoutUtils.getOutermostComponent(neighbor, dimension, 1);
        if (lComp != null && tComp != null) {
            spaceToHold.set(dimension,
                            lComp.getCurrentSpace().positions[dimension][LEADING],
                            tComp.getCurrentSpace().positions[dimension][TRAILING]);
        }
        LayoutRegion spaceAvailable = group1.getCurrentSpace();
        return LayoutRegion.pointInside(spaceToHold, LEADING, spaceAvailable, dimension)
               && LayoutRegion.pointInside(spaceToHold, TRAILING, spaceAvailable, dimension);
    }

    private void updateReplacedOriginalGroup(LayoutInterval newGroup, LayoutInterval newSeq) {
        updateReplacedOriginalGroup(originalPosition != null ? originalPosition.desc1 : null, newGroup, newSeq);
        updateReplacedOriginalGroup(originalPosition != null ? originalPosition.desc2 : null, newGroup, newSeq);
    }

    private static void updateReplacedOriginalGroup(IncludeDesc iDesc, LayoutInterval newGroup, LayoutInterval newSeq)
    {
        if (iDesc != null && LayoutInterval.getRoot(newGroup) != LayoutInterval.getRoot(iDesc.parent)) {
            if (iDesc.parent.isParallel())
                iDesc.parent = newGroup;
            else if (newSeq != null)
                iDesc.parent = newSeq;
        }
    }

    private void updateMovedOriginalNeighbor() {
        updateMovedOriginalNeighbor(originalPosition != null ? originalPosition.desc1 : null);
        updateMovedOriginalNeighbor(originalPosition != null ? originalPosition.desc2 : null);
    }

    private static void updateMovedOriginalNeighbor(IncludeDesc iDesc) {
        if (iDesc != null && iDesc.neighbor != null) {
            iDesc.parent = LayoutInterval.getFirstParent(iDesc.neighbor, PARALLEL);
            correctOriginalInclusion(iDesc, LayoutInterval.getRoot(iDesc.neighbor));
        }
    }

    private boolean mergeSequentialInclusions(IncludeDesc iDesc1, IncludeDesc iDesc2) {
        if (iDesc2 == null || !canCombine(iDesc1, iDesc2))
            return false;

        assert (iDesc1.alignment == LEADING || iDesc1.alignment == TRAILING)
                && (iDesc2.alignment == LEADING || iDesc2.alignment == TRAILING)
                && iDesc1.alignment == (iDesc2.alignment^1);

        if (iDesc1.parent == iDesc2.parent)
            return true;

        LayoutInterval commonGroup;
        boolean nextTo;
        if (iDesc1.parent.isParentOf(iDesc2.parent)) {
            commonGroup = iDesc1.parent;
            nextTo = iDesc1.neighbor != null || iDesc2.snappedNextTo != null || iDesc2.parent.isSequential();
        }
        else if (iDesc2.parent.isParentOf(iDesc1.parent)) {
            commonGroup = iDesc2.parent;
            nextTo = iDesc2.neighbor != null || iDesc1.snappedNextTo != null || iDesc1.parent.isSequential();
        }
        else {
            commonGroup = LayoutInterval.getFirstParent(iDesc1.parent, SEQUENTIAL);
            nextTo = false;
        }

        if (commonGroup.isSequential() || nextTo) {
            // inclusions in common sequence or the upper inclusion has the lower as neighbor
            if (iDesc1.alignment == TRAILING) {
                IncludeDesc temp = iDesc1;
                iDesc1 = iDesc2;
                iDesc2 = temp;
            } // so iDesc1 is leading and iDesc2 trailing
            mergeInclusionsInCommonSequence(iDesc1, iDesc2, commonGroup);
        } else { // common group is parallel - there is nothing in sequence, so nothing to extract
            assert iDesc1.parent.isParallel() && iDesc2.parent.isParallel()
                   && (commonGroup == iDesc1.parent || commonGroup == iDesc2.parent)
                   && iDesc1.neighbor == null && iDesc2.neighbor == null;

            if (iDesc1.snappedParallel != null && iDesc2.snappedParallel != null
                    && commonGroup.isParentOf(iDesc1.snappedParallel) && commonGroup.isParentOf(iDesc2.snappedParallel)) {
                // if aligning on both sides it could be placed in first common parent
                LayoutInterval parParent = iDesc1.snappedParallel;
                if (!parParent.isParentOf(iDesc2.snappedParallel)) {
                    parParent = LayoutInterval.getFirstParent(iDesc1.snappedParallel, PARALLEL);
                    if (!parParent.isParentOf(iDesc2.snappedParallel)) {
                        parParent = iDesc2.snappedParallel;
                        if (!parParent.isParentOf(iDesc1.snappedParallel)) {
                            parParent = LayoutInterval.getFirstParent(iDesc2.snappedParallel, PARALLEL);
                            if (!parParent.isParentOf(iDesc1.snappedParallel)) {
                                parParent = null;
                            }
                        }
                    }
                }
                if (parParent != null && commonGroup.isParentOf(parParent)
                        && canAlignWith(iDesc1.snappedParallel, parParent, iDesc1.alignment)
                        && canAlignWith(iDesc2.snappedParallel, parParent, iDesc2.alignment)) {
                    iDesc1.parent = parParent;
                    iDesc2.parent = parParent;
                    return true;
                }
            }

            if ((iDesc2.snappedNextTo == null && iDesc2.snappedParallel == null)
                || (iDesc2.snappedParallel != null && canAlignWith(iDesc2.snappedParallel, iDesc1.parent, iDesc2.alignment)))
            {   // iDesc2 can adapt to iDesc1
                iDesc2.parent = iDesc1.parent;
                return true;
            }

            if (iDesc2.parent == commonGroup) {
                IncludeDesc temp = iDesc1;
                iDesc1 = iDesc2;
                iDesc2 = temp;
            } // so iDesc1 is super-group and iDesc2 subgroup
            assert iDesc2.snappedNextTo == null;

            if (iDesc2.snappedParallel == iDesc2.parent) {
                iDesc2.parent = LayoutInterval.getFirstParent(iDesc2.parent, PARALLEL);
                if (iDesc2.parent == iDesc1.parent)
                    return true;
            }

            if (iDesc2.snappedParallel == null || canAlignWith(iDesc2.snappedParallel, iDesc1.parent, iDesc2.alignment)) {
                // subgroup is either not snapped at all, or can align also in parent group
                iDesc2.parent = iDesc1.parent;
                return true;
            }

            if (LayoutInterval.isAlignedAtBorder(iDesc2.parent, iDesc1.parent, iDesc1.alignment)) {
                iDesc1.parent = iDesc2.parent;
                return true; // subgroup is aligned to parent group edge
            }

            LayoutInterval seq = iDesc2.parent.getParent();
            if (seq.isSequential() && seq.getParent() == iDesc1.parent) {
                int index = seq.indexOf(iDesc2.parent) + (iDesc1.alignment == LEADING ? -1 : 1);
                LayoutInterval gap = (index == 0 || index == seq.getSubIntervalCount()-1) ?
                                     seq.getSubInterval(index) : null;
                if (gap != null
                    && LayoutInterval.isFixedDefaultPadding(gap)
                    && iDesc1.snappedNextTo == iDesc1.parent
                    && LayoutInterval.wantResize(seq))
                {   // subgroup is at preferred gap from parent - corresponds to parent's snappedNextTo
                    iDesc1.parent = iDesc2.parent;
                    iDesc1.snappedNextTo = null;
                    iDesc1.snappedParallel = iDesc2.parent;
                    return true;
                }

                if (gap != null && gap.isEmptySpace() && iDesc1.snappedParallel == iDesc1.parent) {
                    // need to make the subgroup aligned to parent group
                    int gapSize = LayoutInterval.getCurrentSize(gap, dimension);
                    copyGapInsideGroup(gap, gapSize, iDesc2.parent, iDesc1.alignment);
                    layoutModel.removeInterval(gap);
                    iDesc1.parent = iDesc2.parent;
                    return true;
                }
            }

            iDesc2.parent = iDesc1.parent; // prefer super-group otherwise
        }

        return true;
    }

    private void mergeInclusionsInCommonSequence(IncludeDesc iDesc1, IncludeDesc iDesc2, LayoutInterval commonGroup) {
        boolean more;
        do {
            more = false;
            int startIndex = 0;
            LayoutInterval ext1 = null;
            int depth1 = 0;
            boolean startGap = false;
            int endIndex = 0;
            LayoutInterval ext2 = null;
            int depth2 = 0;
            boolean endGap = false;

            if (commonGroup.isSequential()) {
                if (commonGroup.isParentOf(iDesc1.parent)) {
                    ext1 = (iDesc1.parent.isSequential() || iDesc1.parent.getParent() != commonGroup)
                            ? iDesc1.parent : iDesc1.neighbor;
                    depth1 = 1;
                    if (ext1 != null) {
                        while (ext1.getParent().getParent() != commonGroup) {
                            depth1++;
                            ext1 = ext1.getParent();
                        }
                        startIndex = commonGroup.indexOf(ext1.getParent());
                    } else { // nothing to extract, just find out the index
                        LayoutInterval inCommon = iDesc1.parent;
                        while (inCommon.getParent() != commonGroup) {
                            inCommon = inCommon.getParent();
                        }
                        startIndex = commonGroup.indexOf(inCommon);
                    }
                } else {
                    startIndex = iDesc1.index;
                    if (startIndex == commonGroup.getSubIntervalCount())
                        startIndex--;
                    startGap = commonGroup.getSubInterval(startIndex).isEmptySpace();
                }

                if (commonGroup.isParentOf(iDesc2.parent)) {
                    ext2 = (iDesc2.parent.isSequential() || iDesc2.parent.getParent() != commonGroup)
                            ? iDesc2.parent : iDesc2.neighbor;
                    depth2 = 1;
                    if (ext2 != null) {
                        while (ext2.getParent().getParent() != commonGroup) {
                            depth2++;
                            ext2 = ext2.getParent();
                        }
                        endIndex = commonGroup.indexOf(ext2.getParent());
                    } else {
                        LayoutInterval inCommon = iDesc2.parent;
                        while (inCommon.getParent() != commonGroup) {
                            inCommon = inCommon.getParent();
                        }
                        endIndex = commonGroup.indexOf(inCommon);
                    }
                } else {
                    endIndex = iDesc2.index;
                    if (iDesc2.snappedParallel == null || !commonGroup.isParentOf(iDesc2.snappedParallel)) {
                        endGap = commonGroup.getSubInterval(--endIndex).isEmptySpace();
                    } else if (endIndex == commonGroup.getSubIntervalCount()) {
                        endIndex--;
                    }
                }
            }

            boolean validSection = (endIndex > startIndex + 1 || (endIndex == startIndex+1
                    && (!startGap || iDesc1.snappedParallel != null) && (!endGap || iDesc2.snappedParallel != null)));
            if (validSection && (ext1 != null || ext2 != null)) {
                // there is a significant part of the common sequence to be parallelized
                LayoutInterval extSeq = new LayoutInterval(SEQUENTIAL);
                LayoutInterval startInt = commonGroup.getSubInterval(startIndex);
                LayoutInterval endInt = commonGroup.getSubInterval(endIndex);
                int posL = LayoutUtils.getVisualPosition(startInt, dimension, LEADING);
                int posT = LayoutUtils.getVisualPosition(endInt, dimension, TRAILING);

                // check visual overlap of the extracted intervals with the part of the sequence
                LayoutInterval parConnectingGap = null;
                LayoutInterval extConnectingGap = null;
                // temporarily remove ext1 and ext2 for analysis
                LayoutInterval parent1, parent2;
                int idx1, idx2;
                if (ext1 != null) {
                    parent1 = ext1.getParent();
                    idx1 = parent1.remove(ext1);
                } else {
                    parent1 = null; idx1 = -1;
                }
                if (ext2 != null) {
                    parent2 = ext2.getParent();
                    idx2 = parent2.remove(ext2);
                } else {
                    parent2 = null; idx2 = -1;
                }
                // Check if can extract ext1 and ext2 in parallel with whole sub-sequence
                // from startIndex to endIndex, or just with the first/last interval at
                // startIndex or endIndex (due to orthogonal overlap of ext1/ext2 with
                // something from the sequence).
                if (ext1 != null
                        && !LayoutInterval.isClosedGroup(startInt, TRAILING)
                        && LayoutUtils.contentOverlap(ext1, commonGroup, startIndex+1, endIndex, dimension^1)
                        && !LayoutUtils.contentOverlap(startInt, commonGroup, startIndex+1, endIndex, dimension^1)) {
                    while (startIndex < endIndex) {
                        layoutModel.addInterval(layoutModel.removeInterval(commonGroup, startIndex+1), extSeq, -1);
                        endIndex--;
                    }
                    if (extSeq.getSubIntervalCount() > 0) {
                        LayoutInterval li = extSeq.getSubInterval(extSeq.getSubIntervalCount()-1);
                        if (li.isEmptySpace()) { // cut everything after startInt, so at least clone the last gap
                            parConnectingGap = LayoutInterval.cloneInterval(li, null);
                        }
                        li = extSeq.getSubInterval(0);
                        if (li.isEmptySpace()) {
                            extConnectingGap = li;
                        }
                    }
                    if (ext2 != null) {
                        parent2.add(ext2, idx2);
                        ext2 = null; // don't extract ext2 in this round (its parent just moved to extSeq)
                        if (depth2 == 1) {
                            depth2 = 2; // don't adjust iDesc2 and do one more round
                        }
                    }
                } else if (ext2 != null
                           && !LayoutInterval.isClosedGroup(endInt, LEADING)
                           && LayoutUtils.contentOverlap(ext2, commonGroup, startIndex, endIndex-1, dimension^1)
                           && !LayoutUtils.contentOverlap(endInt, commonGroup, startIndex, endIndex-1, dimension^1)) {
                    while (startIndex < endIndex) {
                        layoutModel.addInterval(layoutModel.removeInterval(commonGroup, startIndex), extSeq, -1);
                        endIndex--;
                    }
                    if (extSeq.getSubIntervalCount() > 0) {
                        LayoutInterval li = extSeq.getSubInterval(0);
                        if (li.isEmptySpace()) { // cut everything before endInt, so at least clone the first gap
                            parConnectingGap = LayoutInterval.cloneInterval(li, null);
                        }
                        li = extSeq.getSubInterval(extSeq.getSubIntervalCount()-1);
                        if (li.isEmptySpace()) {
                            extConnectingGap = li;
                        }
                    }
                    if (ext1 != null) {
                        parent1.add(ext1, idx1);
                        ext1 = null; // don't extract ext1 in this round (its parent just moved to extSeq)
                        if (depth1 == 1) {
                            depth1 = 2; // don't adjust iDesc1 and do one more round
                        }
                    }
                }
                // return back ext1 and ext2
                if (ext1 != null) {
                    parent1.add(ext1, idx1);
                }
                if (ext2 != null) {
                    parent2.add(ext2, idx2);
                }

                LayoutInterval parGroup;
                if (startIndex == 0 && endIndex == commonGroup.getSubIntervalCount()-1) {
                    // parallel with whole sequence
                    parGroup = commonGroup.getParent();
                } else { // separate part of the original sequence
                    parGroup = new LayoutInterval(PARALLEL);
                    LayoutInterval parSeq = new LayoutInterval(SEQUENTIAL);
                    parGroup.add(parSeq, 0);
                    parGroup.getCurrentSpace().set(dimension, posL, posT);
                    while (startIndex <= endIndex) {
                        layoutModel.addInterval(layoutModel.removeInterval(commonGroup, startIndex), parSeq, -1);
                        endIndex--;
                    }
                    layoutModel.addInterval(parGroup, commonGroup, startIndex);
                }
                layoutModel.addInterval(extSeq, parGroup, -1);
                if (ext1 != null) {
                    LayoutInterval parent = ext1.getParent();
                    layoutModel.removeInterval(ext1);
                    if (parent.getSubIntervalCount() == 1) {
                        LayoutInterval last = layoutModel.removeInterval(parent, 0);
                        operations.addContent(last, parent.getParent(), layoutModel.removeInterval(parent), dimension);
                        if (parent == startInt) {
                            startInt = last;
                        }
                    }
                    int beforeCount = extSeq.getSubIntervalCount();
                    operations.addContent(ext1, extSeq, 0, dimension);
                    if (depth1 == 1 && !iDesc1.parent.isSequential()) {
                        iDesc1.index = extSeq.getSubIntervalCount() - beforeCount;
                    }
                    if (depth2 <= 1) {
                        if (ext2 == null || !iDesc2.parent.isSequential()) {
                            iDesc2.index = extSeq.getSubIntervalCount();
                        } else {
                            iDesc2.index += extSeq.getSubIntervalCount();
                        }
                    }
                    if (ext2 != null) {
                        LayoutInterval gap = new LayoutInterval(SINGLE);
                        int size = LayoutRegion.distance(ext1.getCurrentSpace(), ext2.getCurrentSpace(), dimension, LEADING, TRAILING);
                        gap.setSize(size);
                        layoutModel.addInterval(gap, extSeq, -1);
                    } else { // could have moved things next to startInt to extSeq
                        if (parConnectingGap != null) {
                            parent = startInt.getParent();
                            if (!parent.isSequential()) {
                                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                                layoutModel.addInterval(seq, parent, layoutModel.removeInterval(startInt));
                                layoutModel.addInterval(startInt, seq, 0);
                                parent = seq;
                            }
                            assert parent.indexOf(startInt) == 0;
                            layoutModel.addInterval(parConnectingGap, parent, 1);
                        }
                        if (extConnectingGap != null) {
                            operations.accommodateGap(extConnectingGap, dimension);
                            if (LayoutInterval.wantResize(startInt) && !LayoutInterval.wantResize(extSeq)) {
                                operations.setIntervalResizing(extConnectingGap, true);
                            }
                        }
                    }
                } else {
                    iDesc1.index = 0;
                    if (depth2 <= 1 && !iDesc2.parent.isSequential()) {
                        iDesc2.index = extSeq.getSubIntervalCount();
                    }
                }
                if (ext2 != null) {
                    LayoutInterval parent = ext2.getParent();
                    if (ext2.getAlignment() == TRAILING) {
                        extSeq.setAlignment(TRAILING);
                    }
                    layoutModel.removeInterval(ext2);
                    if (parent.getSubIntervalCount() == 1) {
                        LayoutInterval last = layoutModel.removeInterval(parent, 0);
                        operations.addContent(last, parent.getParent(), layoutModel.removeInterval(parent), dimension);
                        if (parent == endInt) {
                            endInt = last;
                        }
                    }
                    operations.addContent(ext2, extSeq, -1, dimension);
                    if (ext1 == null) { // could have moved things next to endInt to extSeq
                        if (parConnectingGap != null) {
                            parent = endInt.getParent();
                            if (!parent.isSequential()) {
                                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                                layoutModel.addInterval(seq, parent, layoutModel.removeInterval(endInt));
                                layoutModel.addInterval(endInt, seq, 0);
                                parent = seq;
                            }
                            assert parent.indexOf(endInt) == 0;
                            layoutModel.addInterval(parConnectingGap, parent, 0);
                        }
                        if (extConnectingGap != null) {
                            operations.accommodateGap(extConnectingGap, dimension);
                            if (LayoutInterval.wantResize(endInt) && !LayoutInterval.wantResize(extSeq)) {
                                operations.setIntervalResizing(extConnectingGap, true);
                            }
                        }
                    }
                }

                if (depth1 <= 1) {
                    iDesc1.parent = extSeq;
                    if (iDesc2.newSubGroup) {
                        iDesc1.newSubGroup = true;
                    }
                    iDesc1.neighbor = null;
                }
                if (depth2 <= 1) {
                    iDesc2.parent = extSeq;
                    if (iDesc1.newSubGroup) {
                        iDesc2.newSubGroup = true;
                    }
                    iDesc2.neighbor = null;
                }
                commonGroup = extSeq;
                more = depth1 > 1 || depth2 > 1;
                optimizeStructure = true;
            } else if (ext1 == null && ext2 == null && validSection) {
                // nothing to extract, but the resizing interval still to be in
                // parallel with part of the sequence
                if (commonGroup.isParentOf(iDesc1.parent)) {
                    iDesc1.index = startIndex;
                }
                if (commonGroup.isParentOf(iDesc2.parent)) {
                    iDesc2.index = endIndex;
                }
                iDesc1.parent = iDesc2.parent = commonGroup;
                iDesc1.newSubGroup = iDesc2.newSubGroup = true;
            } else { // end position, stay in subgroup
                if (iDesc2.parent.isParentOf(iDesc1.parent)) {
                    iDesc2.parent = iDesc1.parent;
                    iDesc2.index = iDesc1.index;
                    iDesc2.newSubGroup = iDesc1.newSubGroup;
                    iDesc2.neighbor = iDesc1.neighbor;
                    if (endGap) // there's an outer gap
                        iDesc2.fixedPosition = false;
                }
                else if (iDesc1.parent.isParentOf(iDesc2.parent)) {
                    iDesc1.parent = iDesc2.parent;
                    iDesc1.index = iDesc2.index;
                    iDesc1.newSubGroup = iDesc2.newSubGroup;
                    iDesc1.neighbor = iDesc2.neighbor;
                    if (startGap) // there's an outer gap
                        iDesc1.fixedPosition = false;
                } else break;
            }
        } while (more);
    }

    /**
     * Moves a gap next to a parallel group into the parallel group - i.e. each
     * interval in the group gets extended by the gap. Sort of opposite to
     * LayoutOperations.optimizeGaps.
     * @param alignment which side of the group is extended
     */
    private void copyGapInsideGroup(LayoutInterval gap, int gapSize, LayoutInterval group, int alignment) {
        assert gap.isEmptySpace() && (alignment == LEADING || alignment == TRAILING);

        if (alignment == LEADING)
            gapSize = -gapSize;

        group.getCurrentSpace().positions[dimension][alignment] += gapSize;

        List<LayoutInterval> originalGroup = new ArrayList<LayoutInterval>(group.getSubIntervalCount());
        for (Iterator<LayoutInterval> it=group.getSubIntervals(); it.hasNext(); ) {
            originalGroup.add(it.next());
        }

        for (LayoutInterval sub : originalGroup) {
            LayoutInterval gapClone = LayoutInterval.cloneInterval(gap, null);
            if (sub.isSequential()) {
                sub.getCurrentSpace().positions[dimension][alignment] += gapSize;
                int index = alignment == LEADING ? 0 : sub.getSubIntervalCount();
                operations.insertGapIntoSequence(gapClone, sub, index, dimension);
            }
            else {
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                seq.getCurrentSpace().set(dimension, sub.getCurrentSpace());
                seq.getCurrentSpace().positions[dimension][alignment] += gapSize;
                seq.setAlignment(sub.getRawAlignment());
                layoutModel.addInterval(seq, group, layoutModel.removeInterval(sub));
                layoutModel.setIntervalAlignment(sub, DEFAULT);
                layoutModel.addInterval(sub, seq, 0);
                layoutModel.addInterval(gapClone, seq, alignment == LEADING ? 0 : 1);
            }
        }
    }

    private boolean shouldEnterGroup(LayoutInterval group) {
        assert group.isParallel();
        if (positionToEnterGroup(group)) {
            if (aSnappedParallel != null || aSnappedNextTo != null || aEdge == DEFAULT) {
                return true;
            }
            // Otherwise secondary edge that does not snap. Makes sense if primary
            // edge snapped in sequence and secondary aims into neighbor parallel
            // group - should it enter or not?
            if (!stickingOutOfGroup(group, aEdge^1) || groupOpenToEnter(group, aEdge^1)) {
                return true;
            }
        }
        return false;
    }

    private boolean positionToEnterGroup(LayoutInterval group) {
        if (group.getGroupAlignment() == BASELINE && aSnappedParallel == null) {
            return false;
        }

        int alignment = aEdge != DEFAULT ? aEdge : CENTER;
        LayoutRegion groupSpace = group.getCurrentSpace();
        if (LayoutRegion.pointInside(addingSpace, alignment, groupSpace, dimension)) {
            if (aEdge == DEFAULT) {
                // for easier inserting between groups we consider 10 pixels
                // border as not yet in the group
                if (!LayoutRegion.pointInside(addingSpace, LEADING, groupSpace, dimension)
                        || !LayoutRegion.pointInside(addingSpace, TRAILING, groupSpace, dimension)) {
                    // not entirely within the group
                    int[] apos = addingSpace.positions[dimension];
                    int[] gpos = groupSpace.positions[dimension];
                    if (getAddDirection(addingSpace, groupSpace, dimension, CENTER) == LEADING) {
                        if (LayoutInterval.isClosedGroup(group, LEADING)) {
                            int dCL = apos[CENTER] - gpos[LEADING];
                            int dCC = gpos[CENTER] - apos[CENTER];
                            if (dCL < 10 && dCL < dCC) {
                                return false; // out of the group
                            }
                        }
                    } else {
                        if (LayoutInterval.isClosedGroup(group, TRAILING)) {
                            int dCT = gpos[TRAILING] - apos[CENTER];
                            int dCC = apos[CENTER] - gpos[CENTER];
                            if (dCT < 10 && dCT < dCC) {
                                return false; // out of the group
                            }
                        }
                    }
                }
                return true;
            }
            if (aSnappedParallel == null || group == aSnappedParallel
                    || group.isParentOf(aSnappedParallel)) {
                return true;
            }
            if (alignment == LEADING || alignment == TRAILING) {
                // Determine if within or under 'group' one might align in parallel
                // with required 'aSnappedParallel' interval that is out of the group.
                LayoutInterval interval = aSnappedParallel;
                LayoutInterval parent = LayoutInterval.getFirstParent(interval, PARALLEL);
                while (parent != null && LayoutInterval.isAlignedAtBorder(interval, parent, alignment)) {
                    if (parent.isParentOf(group) && LayoutInterval.isAlignedAtBorder(group, parent, alignment)) {
                        return true;
                    }
                    interval = parent;
                    parent = LayoutInterval.getFirstParent(interval, PARALLEL);
                }
                
            }
        }
        return false;
    }

    private boolean groupOpenToEnter(LayoutInterval group, int alignment) {
        if (group.getGroupAlignment() == BASELINE) {
            return false;
        }
        boolean placedOver = addingOverGroupEdge(group, alignment);
        if (!LayoutInterval.isClosedGroup(group, alignment)
            || (placedOver
                && hasOpenRoomForAdding(group, alignment)
                && !isSignificantGroupEdge(group, alignment, true))) {
            // also check if not ort. overlapping with everything
            boolean nextToEverything = true;
            for (Iterator<LayoutInterval> it=group.getSubIntervals(); it.hasNext(); ) {
                if (!orthogonalOverlap(it.next())) {
                    nextToEverything = false;
                    break;
                }
            }
            if (!nextToEverything) {
                if (placedOver) {
                    return true;
                } else {
                    LayoutDragger.PositionDef primaryPos = newPositions[dimension];
                    if (primaryPos == null || !primaryPos.snapped // there is no snap
                            || primaryPos.alignment == aEdge // or this is the primary position
                            || alignment != (primaryPos.alignment^1)) { // secondary position on the other side than primary
                        LayoutInterval significantNeighbor = LayoutInterval.getDirectNeighbor(group, alignment, true);
                        if (significantNeighbor == null
                                || !LayoutRegion.overlap(addingSpace, significantNeighbor.getCurrentSpace(), dimension, 0)) {
                            return true;
                        } // otherwise there already is something next to the group and we have
                          // no reason to try to enter it when our adding position is also next to
                    }
                }
            }
        }
        return false;
    }

    /**
     * @return whether being in 'group' (having it as first parallel parent)
     *         allows parallel align with 'interval'
     */
    private boolean canAlignWith(LayoutInterval interval, LayoutInterval group, int alignment) {
        if (group.isSequential())
            group = group.getParent();

        if (interval == group)
            return true; // can align to group border from inside

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
        if (!aligned
            && LayoutInterval.getDirectNeighbor(interval, alignment, false) == null
            && LayoutInterval.isPlacedAtBorder(interval, parent, dimension, alignment))
        {   // not aligned, but touching parallel group border
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

    private boolean isBorderInclusion(IncludeDesc iDesc) {
        if (iDesc.alignment != LEADING && iDesc.alignment != TRAILING)
            return false;

        if (iDesc.parent.isSequential()) {
            int startIndex = iDesc.alignment == LEADING ? iDesc.index : 0;
            int endIndex;
            if (iDesc.alignment == LEADING) {
                endIndex = iDesc.parent.getSubIntervalCount() - 1;
            } else {
                endIndex = iDesc.index - 1;
                if (endIndex >= iDesc.parent.getSubIntervalCount()) {
                    // if comming from original position the original index might be too high
                    endIndex = iDesc.parent.getSubIntervalCount() - 1;
                }
            }
            return startIndex > endIndex
                   || !LayoutUtils.contentOverlap(addingSpace, iDesc.parent, startIndex, endIndex, dimension^1);
        } else if (iDesc.snappedParallel != null) {
            return iDesc.snappedParallel == iDesc.parent
                   || !iDesc.parent.isParentOf(iDesc.snappedParallel)
                   || LayoutInterval.isPlacedAtBorder(iDesc.snappedParallel, iDesc.parent, dimension, iDesc.alignment);
        } else {
            return iDesc.neighbor == null
                   || (iDesc.alignment == LEADING && iDesc.index >= 1)
                   || (iDesc.alignment == TRAILING && iDesc.index == 0);
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
