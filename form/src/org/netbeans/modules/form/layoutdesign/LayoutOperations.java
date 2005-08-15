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
 * This class serves as a library of various useful and well-defined operations
 * on the layout model.
 *
 * @author Tomas Pavek
 */

class LayoutOperations implements LayoutConstants {

    private LayoutModel layoutModel;

    LayoutOperations(LayoutModel model) {
        layoutModel = model;
    }

    LayoutModel getModel() {
        return layoutModel;
    }

    // -----

    /**
     * Extracts surroundings of given interval (placed in a sequential group).
     * Extracted intervals are removed and go to the 'restLeading' and
     * 'restTrailing' lists.
     */
    int extract(LayoutInterval interval, int alignment, boolean closed,
                List restLeading, List restTrailing)
    {
        LayoutInterval seq = interval.getParent();
        assert seq.isSequential();

        int index = seq.indexOf(interval);
        int count = seq.getSubIntervalCount();
        int extractCount;
        if (closed || (alignment != LEADING && alignment != TRAILING)) {
            extractCount = 1;
        }
        else {
            extractCount = alignment == LEADING ? count - index : index + 1;
        }

        if (extractCount < seq.getSubIntervalCount()) {
            List toRemainL = null;
            List toRemainT = null;
            int startIndex = alignment == LEADING ? index : index - extractCount + 1;
            int endIndex = alignment == LEADING ? index + extractCount - 1 : index;
            Iterator it = seq.getSubIntervals();
            for (int idx=0; it.hasNext(); idx++) {
                LayoutInterval li = (LayoutInterval) it.next();
                if (idx < startIndex) {
                    if (toRemainL == null) {
                        toRemainL = new LinkedList();
                        toRemainL.add(new Integer(LayoutInterval.getEffectiveAlignment(li)));
                    }
                    toRemainL.add(li);
                }
                else if (idx > endIndex) {
                    if (toRemainT == null) {
                        toRemainT = new LinkedList();
                        toRemainT.add(new Integer(LayoutInterval.getEffectiveAlignment(li)));
                    }
                    toRemainT.add(li);
                }
            }
            if (toRemainL != null) {
                it = toRemainL.iterator();
                it.next();
                do {
                    layoutModel.removeInterval((LayoutInterval)it.next());
                }
                while (it.hasNext());
                restLeading.add(toRemainL);
            }
            if (toRemainT != null) {
                it = toRemainT.iterator();
                it.next();
                do {
                    layoutModel.removeInterval((LayoutInterval)it.next());
                }
                while (it.hasNext());
                restTrailing.add(toRemainT);
            }
        }

        return extractCount;
    }

    /**
     * Adds parallel content of a group specified in List to given sequence.
     * Used to create a remainder parallel group to a group of aligned intervals.
     * @param list the content of the group, output from 'extract' method
     * @param seq a sequential group where to add to
     * @param index the index in the sequence where to add
     * @param dimension
     * @param position the position of the remainder group relative to the main
     *        group (LEADING or TRAILING)
//     * @param mainAlignment effective alignment of the main group (LEADING or
//     *        TRAILING or something else meaning not aligned)
     * @return parallel group if it has been created, or null
     */
    LayoutInterval addGroupContent(List list, LayoutInterval seq,
                                   int index, int dimension, int position/*, int mainAlignment*/)
    {
        assert seq.isSequential() && (position == LEADING || position == TRAILING);
        boolean resizingFillGap = false;
        LayoutInterval commonGap = null;
        boolean onlyGaps = true;

        // Remove sequences just with one gap
        for (int i=list.size()-1; i >= 0; i--) {
            List subList = (List)list.get(i);
            assert subList.size() >= 2;
            if (subList.size() == 2) { // there is just one interval
                int alignment = ((Integer)subList.get(0)).intValue();
                LayoutInterval li = (LayoutInterval) subList.get(1);
                if (li.isEmptySpace()) {
                    if (commonGap == null || li.getPreferredSize() > commonGap.getPreferredSize())
                        commonGap = li;
                    if (LayoutInterval.canResize(li))
                        resizingFillGap = true;
                    list.remove(i);
                }
                else onlyGaps = false;
            }
            else onlyGaps = false;
        }

        if (onlyGaps) { // just one gap
            if (resizingFillGap && !LayoutInterval.canResize(commonGap))
                layoutModel.setIntervalSize(commonGap, NOT_EXPLICITLY_DEFINED,
                                                       commonGap.getPreferredSize(),
                                                       Short.MAX_VALUE);
            insertGapIntoSequence(commonGap, seq, index, dimension);
            return null;
        }

        if (list.size() == 1) { // just one sequence
            List subList = (List) list.get(0);
            for (int n=subList.size(),i=n-1; i > 0; i--) { // skip alignment at 0
                LayoutInterval li = (LayoutInterval) subList.get(i);
                if (resizingFillGap && li.isEmptySpace() && !LayoutInterval.canResize(li)
                    && ((i == 1 && position == TRAILING) || (i == n-1 && position == LEADING)))
                {   // make the end gap resizing
                    layoutModel.setIntervalSize(
                            li, NOT_EXPLICITLY_DEFINED, li.getPreferredSize(), Short.MAX_VALUE);
                }
                if (i == 1 && li.isEmptySpace()) // first gap
                    insertGapIntoSequence(li, seq, index, dimension);
                else
                    layoutModel.addInterval(li, seq, index);
            }
            return null;
        }

        // create parallel group for multiple intervals/sequences
        LayoutInterval group = new LayoutInterval(PARALLEL);
//        if (position == mainAlignment) {
//            // [but this should eliminate resizability only for gaps...]
//            group.setMinimumSize(USE_PREFERRED_SIZE);
//            group.setMaximumSize(USE_PREFERRED_SIZE);
//        }
////        group.setGroupAlignment(alignment);

        // fill the group
        for (Iterator it=list.iterator(); it.hasNext(); ) {
            List subList = (List) it.next();
            LayoutInterval interval;
            if (subList.size() == 2) { // there is just one interval - use it directly
                int alignment = ((Integer)subList.get(0)).intValue();
                interval = (LayoutInterval) subList.get(1);
                if (alignment == LEADING || alignment == TRAILING)
                    layoutModel.setIntervalAlignment(interval, alignment);
            }
            else { // there are more intervals - create sequence
                interval = new LayoutInterval(SEQUENTIAL);
                int alignment = ((Integer)subList.get(0)).intValue();
                if (alignment == LEADING || alignment == TRAILING)
                    interval.setAlignment(alignment);
                for (int i=1,n=subList.size(); i < n; i++) {
                    LayoutInterval li = (LayoutInterval) subList.get(i);
                    if (resizingFillGap && li.isEmptySpace() && !LayoutInterval.canResize(li)
                        && ((i == 1 && position == TRAILING) || (i == n-1 && position == LEADING)))
                    {   // make the end gap resizing
                        layoutModel.setIntervalSize(
                                li, NOT_EXPLICITLY_DEFINED, li.getPreferredSize(), Short.MAX_VALUE);
                    }
                    layoutModel.addInterval(li, interval, -1);
                }
            }
            layoutModel.addInterval(interval, group, -1);
        }

        layoutModel.addInterval(group, seq, index++);

        return group;
    }

    /**
     * Adds 'interval' to 'target'. In case of 'interval' is a group, it is
     * dismounted to individual intervals if needed (e.g. if adding sequence to
     * sequence), or if producing equal result with less nesting (e.g. when
     * adding parallel group to parallel group with same alignment).
     * Also redundant groups are canceled (containing just one interval).
     */
    boolean addContent(LayoutInterval interval, LayoutInterval target, int index) {
        if (interval.isGroup() && interval.getSubIntervalCount() == 1) {
            return addContent(layoutModel.removeInterval(interval, 0), target, index);
        }

        if (interval.isSequential() && target.isSequential()) {
            if (index < 0) {
                index = target.getSubIntervalCount();
            }
            while (interval.getSubIntervalCount() > 0) {
                LayoutInterval li = layoutModel.removeInterval(interval, 0);
                layoutModel.addInterval(li, target, index++);
            }
            return true;
        }
        else if (interval.isParallel() && target.isParallel()) {
            int align = interval.getAlignment();
            boolean sameAlign = true;
            Iterator it = interval.getSubIntervals();
            while (it.hasNext()) {
                LayoutInterval li = (LayoutInterval) it.next();
                if (LayoutInterval.wantResize(li)) { // will span over whole target group
                    sameAlign = true;
                    break;
                }
                if (align == DEFAULT) {
                    align = li.getAlignment();
                }
                else if (li.getAlignment() != align) {
                    sameAlign = false;
                }
            }

            if (sameAlign
                && (LayoutInterval.canResize(interval) || !LayoutInterval.canResize(target) || !LayoutInterval.wantResize(target)))
            {   // can dismantle the group
                assert interval.getParent() == null;
                while (interval.getSubIntervalCount() > 0) {
                    LayoutInterval li = interval.getSubInterval(0);
                    if (li.getRawAlignment() == DEFAULT
                        && interval.getGroupAlignment() != target.getGroupAlignment())
                    {   // force alignment explicitly
                        layoutModel.setIntervalAlignment(li, li.getAlignment());
                    }
                    layoutModel.removeInterval(li);
                    layoutModel.addInterval(li, target, index);
                    if (index >= 0)
                        index++;
                }
                if (!LayoutInterval.canResize(interval) && LayoutInterval.canResize(target)) {
                    suppressGroupResizing(target);
                }
                return true;
            }
            else { // need to add the group as a whole
                layoutModel.addInterval(interval, target, index);
            }
        }
        else {
            layoutModel.addInterval(interval, target, index);
        }
        return false;
    }

    void resizeInterval(LayoutInterval interval, int size) {
        assert size >= 0 || size == NOT_EXPLICITLY_DEFINED;
        int min = interval.getMinimumSize() == interval.getPreferredSize()
                  && (interval.getMinimumSize() != NOT_EXPLICITLY_DEFINED
                      || interval.getMaximumSize() < Short.MAX_VALUE) ?
                  size : interval.getMinimumSize();
        int max = interval.getMaximumSize() == interval.getPreferredSize() ?
                  size : interval.getMaximumSize();
        layoutModel.setIntervalSize(interval, min, size, max);
    }

    void suppressGroupResizing(LayoutInterval group) {
        // don't for root group
        if (group.getParent() != null) {
            layoutModel.setIntervalSize(group, group.getMinimumSize(),
                                               group.getPreferredSize(),
                                               USE_PREFERRED_SIZE);
        }
    }

    void enableGroupResizing(LayoutInterval group) {
        layoutModel.setIntervalSize(group, group.getMinimumSize(),
                                           group.getPreferredSize(),
                                           NOT_EXPLICITLY_DEFINED);
    }

    void mergeParallelGroups(LayoutInterval group) {
        assert group.isParallel();
        if (!group.isParallel())
            return;

        for (int i=0; i < group.getSubIntervalCount(); i++) {
            LayoutInterval sub = group.getSubInterval(i);
            if (sub.isParallel()) {
                int align = sub.getAlignment();
                boolean sameAlign = true;
                Iterator it = sub.getSubIntervals();
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

                if (sameAlign && LayoutInterval.canResize(sub) == LayoutInterval.canResize(group)) {
                    // the sub-group can be dissolved into parent group
                    mergeParallelGroups(sub);
                    layoutModel.removeInterval(group, i--);
                    while (sub.getSubIntervalCount() > 0) {
                        LayoutInterval li = sub.getSubInterval(0);
                        if (li.getRawAlignment() == DEFAULT
                            && sub.getGroupAlignment() != group.getGroupAlignment())
                        {   // force alignment explicitly
                            layoutModel.setIntervalAlignment(li, li.getAlignment());
                        }
                        layoutModel.removeInterval(li);
                        layoutModel.addInterval(li, group, ++i);
                    }
                }
            }
        }
    }

    /**
     * This method goes through a sequential group and moves each interval next
     * to an open edge of a parallel group into the group.
     * @param parent sequential group to process
     * @param dimension
     */
    void moveInsideSequential(LayoutInterval parent, int dimension) {
        assert parent.isSequential();
        if (!parent.isSequential())
            return;

        int alignment = LEADING;
        do {
            LayoutInterval extend = findIntervalToExtend(parent, dimension, alignment);
            if (extend == null) {
                if (alignment == LEADING) {
                    alignment = TRAILING;
                    extend = findIntervalToExtend(parent, dimension, alignment);
                }
                if (extend == null)
                    break;
            }

            LayoutInterval inGroup = extend.getParent(); // group to infiltrate
            LayoutInterval outGroup = inGroup;
            while (outGroup.getParent() != parent) {
                outGroup = outGroup.getParent();
            }
            int index = parent.indexOf(outGroup);
            int d = alignment == LEADING ? -1 : 1;

            // will the group remain open at the opposite edge?
            boolean commonEndingGap = true;
            for (int i=index-d, n=parent.getSubIntervalCount(); i >= 0 && i < n; i-=d) {
                LayoutInterval li = parent.getSubInterval(i);
                if ((!li.isEmptySpace() || (i-d >= 0 && i-d < n)) // ignore last gap
                    && LayoutInterval.wantResize(li))
                {   // resizing interval will close the group
                    // possibly need to separate the rest of the group not to be influenced
                    LayoutInterval endGap = parent.getSubInterval(alignment == LEADING ? n-1 : 0);
                    if (endGap == null || endGap.getPreferredSize() != NOT_EXPLICITLY_DEFINED) {
                        commonEndingGap = false;
                        LayoutInterval closing = extend;
                        int borderPos = parent.getCurrentSpace().positions[dimension][alignment^1];
                        do {
                            LayoutInterval par = closing.getParent();
                            if (par.isParallel()) {
                                separateGroupContent(closing, borderPos, dimension, alignment^1);
                            }
                            closing = par;
                        }
                        while (closing != outGroup);
                    }
                    break;
                }
            }

            int extendPos = extend.getCurrentSpace().positions[dimension][alignment^1];
            if (!extend.isSequential()) {
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                seq.setAlignment(extend.getAlignment());
                layoutModel.addInterval(seq, inGroup, layoutModel.removeInterval(extend));
                layoutModel.setIntervalAlignment(extend, DEFAULT);
                layoutModel.addInterval(extend, seq, 0);
                extend = seq;
            }

            // move the intervals from outside inside the group, next to found interval (extend)
            LayoutInterval connectingGap = null;
            int idx, addIdx;
            if (alignment == LEADING) {
                idx = index + 1; // start behind the group
                addIdx = extend.getSubIntervalCount(); // add behind the interval
            }
            else {
                idx = index - 1; // start before the group
                addIdx = 0; // add before the interval
            }
            while (idx >= 0 && idx < parent.getSubIntervalCount()) {
                LayoutInterval li = parent.getSubInterval(idx);
                if (li.isEmptySpace()) {
                    if (connectingGap == null) { // first gap
                        if (extendPos != outGroup.getCurrentSpace().positions[dimension][alignment^1]) {
                            // need to extend the first gap (extended interval inside group is smaller than the group)
                            int neighborPos = parent.getSubInterval(idx-d).getCurrentSpace().positions[dimension][alignment];
                            int distance = d * (extendPos - neighborPos);
                            if (distance > 0)
                                resizeInterval(li, distance);
                        }
                        connectingGap = li;
                    }
                    else if ((idx == 0 || idx == parent.getSubIntervalCount()-1)
                             && commonEndingGap)
                    {   // keep the last gap out
                        break;
                    }
                }
                layoutModel.removeInterval(li);
                layoutModel.addInterval(li, extend, addIdx);
                if (alignment == LEADING)
                    addIdx++;
                else
                    idx--;
            }

            // check if the sequence was not whole moved into the group
            if (parent.getSubIntervalCount() == 1) { // only neighborGroup remained, eliminate the parent group
                assert outGroup == parent.getSubInterval(0);
                layoutModel.removeInterval(outGroup);
                LayoutInterval superParent = parent.getParent();
                addContent(outGroup, superParent, layoutModel.removeInterval(parent));
                break;
            }
        }
        while (true);
    }

    private LayoutInterval findIntervalToExtend(LayoutInterval parent, int dimension, int alignment) {
        int d = alignment == LEADING ? -1 : 1;
        int count = parent.getSubIntervalCount();
        int idx = alignment == LEADING ? count-1 : 0;
        boolean atBorder = true;
        boolean gap = false;

        while (idx >= 0 && idx < parent.getSubIntervalCount()) {
            LayoutInterval sub = parent.getSubInterval(idx);
            if (sub.isEmptySpace()) {
                gap = true;
            }
            else {
                if (!atBorder && gap && sub.isParallel()) {
                    LayoutRegion space = new LayoutRegion();
                    for (int i=idx-d-d; i >= 0 && i < count; i-=d) {
                        LayoutInterval li = parent.getSubInterval(i);
                        if (!li.isEmptySpace())
                            space.expand(li.getCurrentSpace());
                    }
                    LayoutInterval extend = prepareGroupExtension(sub, space, dimension, alignment^1);
                    if (extend != null)
                        return extend;
                }
                gap = false;
                atBorder = false;
            }
            idx += d;
        }
        return null;
    }

    private LayoutInterval prepareGroupExtension(LayoutInterval group, LayoutRegion space, int dimension, int alignment) {
        if (LayoutInterval.isClosedGroup(group, alignment)) {
            return null; // can't expand the group - it is not open
        }

        boolean allOverlapping = true;
        LayoutInterval singleOverlap = null;
        List overlapList = null;

        // looking for all intervals the given space is located next to
        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (!li.isEmptySpace()) {
                if (LayoutRegion.overlap(li.getCurrentSpace(), space, dimension^1, 0)) {
                    // interval overlaps orthogonally
                    if (singleOverlap == null) {
                        singleOverlap = li;
                    }
                    else {
                        if (overlapList == null) {
                            overlapList = new LinkedList();
                            overlapList.add(singleOverlap);
                        }
                        overlapList.add(li);
                    }
                }
                else allOverlapping = false;
            }
        }

        if (allOverlapping) // spans whole group
            return null;

        if (overlapList != null) { // overlaps multiple intervals
            LayoutInterval subGroup = new LayoutInterval(PARALLEL);
            subGroup.setGroupAlignment(alignment^1);
            subGroup.setAlignment(alignment^1);
            int index = -1;
            do {
                LayoutInterval li = (LayoutInterval) overlapList.remove(0);
                int idx = layoutModel.removeInterval(li);
                if (index < 0) {
                    index = idx;
                }
                layoutModel.addInterval(li, subGroup, -1);
                subGroup.getCurrentSpace().expand(li.getCurrentSpace());
            }
            while (overlapList.size() > 0);

            layoutModel.addInterval(subGroup, group, index);
            singleOverlap = subGroup;
        }
        else {
            LayoutInterval subParallel;
            if (singleOverlap.isSequential()) {
                subParallel = singleOverlap.getSubInterval(
                              alignment == LEADING ? 0 : singleOverlap.getSubIntervalCount()-1);
                if (!subParallel.isParallel())
                    subParallel = null;
            }
            else if (singleOverlap.isParallel()) {
                subParallel = singleOverlap;
            }
            else subParallel = null;

            LayoutInterval subOverlap = subParallel != null ?
                prepareGroupExtension(subParallel, space, dimension, alignment) : null;
            if (subOverlap != null)
                singleOverlap = subOverlap;
        }

        return singleOverlap;
    }

    private void separateGroupContent(LayoutInterval separate, int outPos, int dimension, int alignment) {
        LayoutInterval group = separate.getParent();
        assert group.isParallel();
        LayoutInterval remainder = null;
        LayoutInterval remainderGroup = null;
        LayoutRegion remainderSpace = null;

        for (int i=0; i < group.getSubIntervalCount(); ) {
            LayoutInterval li = group.getSubInterval(i);
            if (li != separate) {
                assert li.getAlignment() == (alignment^1);
                layoutModel.removeInterval(li);
                if (remainder == null) {
                    remainder = li;
                }
                else {
                    if (remainderGroup == null) {
                        remainderGroup = new LayoutInterval(PARALLEL);
                        remainderGroup.setAlignment(alignment^1);
                        remainderGroup.setGroupAlignment(alignment^1);
                        layoutModel.addInterval(remainder, remainderGroup, 0);
                        remainder = remainderGroup;
                    }
                    layoutModel.addInterval(li, remainderGroup, -1);
                }
                if (!li.isEmptySpace()) {
                    if (remainderSpace == null) {
                        remainderSpace = new LayoutRegion();
                    }
                    remainderSpace.expand(li.getCurrentSpace());
                }
            }
            else i++;
        }
        remainder.setCurrentSpace(remainderSpace);

        LayoutInterval seq;
        if (remainder.isSequential()) {
            seq = remainder;
        }
        else {
             seq = new LayoutInterval(SEQUENTIAL);
             if (remainderGroup == null) {
                 layoutModel.setIntervalAlignment(remainder, DEFAULT);
             }
             layoutModel.addInterval(remainder, seq, 0);
        }
        layoutModel.addInterval(seq, group, -1);

        int remainderPos = remainderSpace.positions[dimension][alignment];
        int gapSize = alignment == LEADING ? remainderPos - outPos : outPos - remainderPos;
        LayoutInterval gap = new LayoutInterval(SINGLE);
        gap.setSizes(NOT_EXPLICITLY_DEFINED, gapSize, Short.MAX_VALUE);
        layoutModel.addInterval(gap, seq, -1);
    }

    int optimizeGaps(LayoutInterval group, int dimension) {
        boolean anyAlignedLeading = false; // if false the group is open at leading edge
        boolean anyAlignedTrailing = false; // if false the group is open at trailing edge
        boolean anyAlignedBoth = false;
        boolean anyGapLeading = false; // if true there is some gap at the leading edge
        boolean anyGapTrailing = false; // if true there is some gap at the trailing edge
        boolean sameMinGapLeading = true; // if true all intervals are aligned with the same gap at leading edge
        boolean sameMinGapTrailing = true; // if true all intervals are aligned with the same gap at trailing edge
        LayoutInterval commonGapLeading = null;
        LayoutInterval commonGapTrailing = null;

        // first analyze the group
        for (int i=0; i < group.getSubIntervalCount(); i++) {
            LayoutInterval li = group.getSubInterval(i);
            if (li.isEmptySpace()) { // remove container supporting gap
                if (group.getSubIntervalCount() > 1) {
                    layoutModel.removeInterval(group, i);
                    i--;
                    continue;
                }
            }

            boolean leadingAlign;
            boolean trailingAlign;
            LayoutInterval leadingGap = null;
            LayoutInterval trailingGap = null;
            if (li.isSequential()) {
                LayoutInterval sub = li.getSubInterval(0);
                leadingAlign = LayoutInterval.wantResize(sub) ?
                               !sub.isEmptySpace() :
                               LayoutInterval.getEffectiveAlignment(sub) == LEADING;
                if (sub.isEmptySpace())
                    leadingGap = sub;

                sub = li.getSubInterval(li.getSubIntervalCount()-1);
                trailingAlign = LayoutInterval.wantResize(sub) ?
                                !sub.isEmptySpace() :
                                LayoutInterval.getEffectiveAlignment(sub) == TRAILING;
                if (sub.isEmptySpace())
                    trailingGap = sub;
            }
            else if (!LayoutInterval.wantResize(li)) {
                int alignment = li.getAlignment();
                leadingAlign = alignment == LEADING;
                trailingAlign = alignment == TRAILING;
            }
            else leadingAlign = trailingAlign = true;

            if (leadingAlign) {
                anyAlignedLeading = true;
                if (trailingAlign)
                    anyAlignedBoth = true;
            }
            if (trailingAlign) {
                anyAlignedTrailing = true;
            }

            if (leadingGap != null) {
                anyGapLeading = true;
                if (sameMinGapLeading) {
                    if (commonGapLeading != null) {
                        int min1 = leadingGap.getMinimumSize() == USE_PREFERRED_SIZE ?
                            leadingGap.getPreferredSize() : leadingGap.getMinimumSize();
                        int min2 = commonGapLeading.getMinimumSize() == USE_PREFERRED_SIZE ?
                            commonGapLeading.getPreferredSize() : commonGapLeading.getMinimumSize();
                        if (min1 != min2) {
                            sameMinGapLeading = false;
                        }
                    }
                    else commonGapLeading = leadingGap;
                }
            }
            else sameMinGapLeading = false;

            if (trailingGap != null) {
                anyGapTrailing = true;
                if (sameMinGapTrailing) {
                    if (commonGapTrailing != null) {
                        int min1 = trailingGap.getMinimumSize() == USE_PREFERRED_SIZE ?
                            trailingGap.getPreferredSize() : trailingGap.getMinimumSize();
                        int min2 = commonGapTrailing.getMinimumSize() == USE_PREFERRED_SIZE ?
                            commonGapTrailing.getPreferredSize() : commonGapTrailing.getMinimumSize();
                        if (min1 != min2) {
                            sameMinGapTrailing = false;
                        }
                    }
                    else commonGapTrailing = trailingGap;
                }
            }
            else sameMinGapTrailing = false;
        }

        if (group.getSubIntervalCount() <= 1 || (!anyGapLeading && !anyGapTrailing)) {
            return -1;
        }

        if (!anyAlignedBoth) {
            // can't reduce common minimum gap if anything aligned to opposite egde
            if (anyAlignedTrailing)
                sameMinGapLeading = false;
            if (anyAlignedLeading)
                sameMinGapTrailing = false;
        }

        int[] groupOuterPos = group.getCurrentSpace().positions[dimension];
        assert groupOuterPos[LEADING] > Short.MIN_VALUE && groupOuterPos[TRAILING] > Short.MIN_VALUE;
        int groupInnerPosLeading = LayoutUtils.getOutermostComponent(group, dimension, LEADING)
                                       .getCurrentSpace().positions[dimension][LEADING];
        int groupInnerPosTrailing = LayoutUtils.getOutermostComponent(group, dimension, TRAILING)
                                        .getCurrentSpace().positions[dimension][TRAILING];

        boolean paddingLeading = false; // if true, there is a default padding at leading side
        boolean paddingTrailing = false; // if true, there is a default padding at traling side
        boolean defaultPaddingLeading = false; // if true, the leading padding has default preferred size
        boolean defaultPaddingTrailing = false; // if true, the trailing padding has default preferred size
        boolean resizingGapLeading = false;
        boolean resizingGapTrailing = false;

        // remove gaps where needed
        for (int i=0; i < group.getSubIntervalCount(); i++) {
            LayoutInterval li = group.getSubInterval(i);
            if (li.isSequential()) {
                if (anyGapLeading && (!anyAlignedLeading || sameMinGapLeading)) {
                    int idx = 0;
                    LayoutInterval gap = li.getSubInterval(idx);
                    if (gap.isEmptySpace()) {
                        layoutModel.removeInterval(li, idx);
                        if (LayoutInterval.isDefaultPadding(gap)) {
                            paddingLeading = true;
                            if (gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                                && li.getSubInterval(idx).getCurrentSpace().positions[dimension][LEADING]
                                     == groupInnerPosLeading) // [would be better to check if the current space corresponds to the default gap]
                            {   // outer-most gap has default preferred size
                                defaultPaddingLeading = true;
                            }
                        }
                        if (gap.getMaximumSize() >= Short.MAX_VALUE) {
                            if (li.getAlignment() == LEADING) // need to change alignment as we removed resizing gap
                                layoutModel.setIntervalAlignment(li, TRAILING);
                            if (!anyAlignedLeading) // resizability goes out of the group
                                resizingGapLeading = true;
                        }
                    }
                }

                if (anyGapTrailing && (!anyAlignedTrailing || sameMinGapTrailing)) {
                    int idx = li.getSubIntervalCount() - 1;
                    LayoutInterval gap = li.getSubInterval(idx);
                    if (gap.isEmptySpace()) {
                        layoutModel.removeInterval(li, idx--);
                        if (LayoutInterval.isDefaultPadding(gap)) {
                            paddingTrailing = true;
                            if (gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                                && li.getSubInterval(idx).getCurrentSpace().positions[dimension][TRAILING]
                                     == groupInnerPosTrailing) // [would be better to check if the current space corresponds to the default gap]
                            {   // outer-most gap has default preferred size
                                defaultPaddingTrailing = true;
                            }
                        }
                        if (gap.getMaximumSize() >= Short.MAX_VALUE) {
                            if (li.getAlignment() == TRAILING) // need to change alignment as we removed resizing gap
                                layoutModel.setIntervalAlignment(li, LEADING);
                            if (!anyAlignedTrailing) // resizability goes out of the group
                                resizingGapTrailing = true;
                        }
                    }
                }

                if (li.getSubIntervalCount() == 1) {
                    // only one interval remained in sequence - cancel the sequence
                    layoutModel.removeInterval(group, i); // removes li from group
                    LayoutInterval sub = layoutModel.removeInterval(li, 0); // removes last interval from li
                    layoutModel.setIntervalAlignment(sub, li.getRawAlignment());
                    layoutModel.addInterval(sub, group, i);
                    li = sub;
                }
            }
        }

        LayoutInterval leadingGap = null;
        LayoutInterval trailingGap = null;

        if (anyGapLeading) {
            if (!anyAlignedLeading) { // group is open at leading edge
                int size = groupInnerPosLeading - groupOuterPos[LEADING];
                if (size > 0 || defaultPaddingLeading) {
                    leadingGap = new LayoutInterval(SINGLE);
                    if (!paddingLeading) {
                        leadingGap.setMinimumSize(USE_PREFERRED_SIZE);
                    }
                    if (!defaultPaddingLeading) {
                        leadingGap.setPreferredSize(size);
                    }
                    if (resizingGapLeading) {
                        leadingGap.setMaximumSize(Short.MAX_VALUE);
                    }
                }
            }
            else if (sameMinGapLeading) {
                leadingGap = new LayoutInterval(SINGLE);
                int size = commonGapLeading.getMinimumSize();
                if (size == USE_PREFERRED_SIZE)
                    size = commonGapLeading.getPreferredSize();
                leadingGap.setSizes(size, size, USE_PREFERRED_SIZE);
            }
        }
        if (anyGapTrailing) {
            if (!anyAlignedTrailing) { // group is open at trailing edge
                int size = groupOuterPos[TRAILING] - groupInnerPosTrailing;
                if (size > 0 || defaultPaddingTrailing) {
                    trailingGap = new LayoutInterval(SINGLE);
                    if (!paddingTrailing) {
                        trailingGap.setMinimumSize(USE_PREFERRED_SIZE);
                    }
                    if (!defaultPaddingTrailing) {
                        trailingGap.setPreferredSize(size);
                    }
                    if (resizingGapTrailing) {
                        trailingGap.setMaximumSize(Short.MAX_VALUE);
                    }
                }
            }
            else if (sameMinGapTrailing) {
                trailingGap = new LayoutInterval(SINGLE);
                int size = commonGapTrailing.getMinimumSize();
                if (size == USE_PREFERRED_SIZE)
                    size = commonGapTrailing.getPreferredSize();
                trailingGap.setSizes(size, size, USE_PREFERRED_SIZE);
            }
        }

        if (leadingGap != null || trailingGap != null) {
            groupOuterPos[LEADING] = groupInnerPosLeading;
            groupOuterPos[TRAILING] = groupInnerPosTrailing;
            groupOuterPos[CENTER] = (groupInnerPosLeading + groupInnerPosTrailing) / 2;
            if (leadingGap != null) {
                group = insertGap(leadingGap, group, groupInnerPosLeading, dimension, LEADING);
            }
            if (trailingGap != null) {
                group = insertGap(trailingGap, group, groupInnerPosTrailing, dimension, TRAILING);
            }
            LayoutInterval parent = group.getParent();
            return parent != null ? parent.indexOf(group) : -1;//idx;
        }
        return -1;
    }

    /**
     * Inserts a gap before or after specified interval. If in a sequence, the
     * method takes care about merging gaps if there is already some as neighbor.
     * Expects the actual positions of the sequence are up-to-date.
     * @param gap the gap to be inserted
     * @param interval the interval before or after which the gap is added
     * @param pos expected real position of the end of the interval where the gap
     *        is added (need not correspond to that stored in the interval)
     * @param dimension
     * @param alignment at which side of the interval the gap is added (LEADING or TRAILING)
     */
    LayoutInterval insertGap(LayoutInterval gap, LayoutInterval interval, int pos, int dimension, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;
        assert !interval.isSequential();
        assert gap.isEmptySpace();

        LayoutInterval parent = interval.getParent();
        if (parent == null) {
            assert interval.isParallel();
            parent = interval;
            if (parent.getSubIntervalCount() > 1) {
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                layoutModel.addInterval(seq, parent, -1);
                interval = new LayoutInterval(PARALLEL);
                layoutModel.addInterval(interval, seq, 0);
                while (parent.getSubIntervalCount() > 1) {
                    layoutModel.addInterval(layoutModel.removeInterval(parent, 0), interval, -1);
                }
                parent = seq;
            }
            else {
                interval = parent.getSubInterval(0);
                if (interval.isSequential()) {
                    parent = interval;
                    interval = parent.getSubInterval(alignment == LEADING ?
                                                     0 : parent.getSubIntervalCount()-1);
                }
                else {
                    LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                    layoutModel.addInterval(seq, parent, -1);
                    layoutModel.removeInterval(interval);
                    layoutModel.addInterval(interval, seq, -1);
                    parent = seq;
                }
            }
        }
        if (parent.isSequential()) {
            // we can't use insertGapIntoSequence here because 'pos' can be special
            LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
            if (neighbor != null && neighbor.isEmptySpace()) {
                LayoutInterval next = LayoutInterval.getDirectNeighbor(neighbor, alignment, false);
                int otherPos = next != null ? next.getCurrentSpace().positions[dimension][alignment^1] :
                                              parent.getCurrentSpace().positions[dimension][alignment];
                int mergedSize = (pos - otherPos) * (alignment == LEADING ? 1 : -1);
                eatGap(neighbor, gap, mergedSize);
            }
            else {
                int idx = parent.indexOf(interval) + (alignment == LEADING ? 0 : 1);
                layoutModel.addInterval(gap, parent, idx);
            }
        }
        else { // parallel parent
            LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
            int idx = layoutModel.removeInterval(interval);
            seq.setAlignment(interval.getAlignment());
            layoutModel.addInterval(seq, parent, idx);
            layoutModel.addInterval(interval, seq, 0);
            layoutModel.addInterval(gap, seq, alignment == LEADING ? 0 : 1);
        }

        return interval;
    }

    int insertGapIntoSequence(LayoutInterval gap, LayoutInterval seq, int index, int dimension) {
        assert gap.isEmptySpace();
        LayoutInterval otherGap = null;
        int alignment = DEFAULT;
        if (index >= 0 && index < seq.getSubIntervalCount()) {
            otherGap = seq.getSubInterval(index);
            if (otherGap.isEmptySpace())
                alignment = TRAILING;
        }
        if (alignment == DEFAULT && index > 0) {
            otherGap = seq.getSubInterval(index-1);
            if (otherGap.isEmptySpace())
                alignment = LEADING;
        }
        if (alignment == DEFAULT) {
            layoutModel.addInterval(gap, seq, index);
                return index; // gap was added normally
        }

        int pos1, pos2;
        LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(otherGap, alignment, true);
        pos1 = neighbor != null ?
               neighbor.getCurrentSpace().positions[dimension][alignment^1] :
               seq.getCurrentSpace().positions[dimension][alignment];
        neighbor = LayoutInterval.getDirectNeighbor(otherGap, alignment^1, true);
        pos2 = neighbor != null ?
               neighbor.getCurrentSpace().positions[dimension][alignment] :
               seq.getCurrentSpace().positions[dimension][alignment^1];

        eatGap(otherGap, gap, Math.abs(pos2 - pos1));
        return alignment == LEADING ? index-1 : index; // gap was eaten
    }

    void eatGap(LayoutInterval main, LayoutInterval eaten, int currentMergedSize) {
        int min;
        if (eaten.getMinimumSize() == 0 || LayoutInterval.canResize(main)) {
            min = main.getMinimumSize();
        }
        else if (main.getMinimumSize() == 0 && eaten.getMinimumSize() != eaten.getPreferredSize()) {
            min = eaten.getMinimumSize();
        }
        else {
            min = USE_PREFERRED_SIZE;
        }

        int pref;
        if (eaten.getPreferredSize() == 0) {
            pref = main.getPreferredSize();
        }
        else if (main.getPreferredSize() == 0) {
            pref = eaten.getPreferredSize();
        }
        else if (main.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                || eaten.getPreferredSize() == NOT_EXPLICITLY_DEFINED) {
            pref = currentMergedSize;
        }
        else {
            pref = main.getPreferredSize() + eaten.getPreferredSize();
        }

        int max = main.getMaximumSize() >= Short.MAX_VALUE || eaten.getMaximumSize() >= Short.MAX_VALUE ?
                  Short.MAX_VALUE : USE_PREFERRED_SIZE;

        layoutModel.setIntervalSize(main, min, pref, max);
        if (eaten.getParent() != null) {
            layoutModel.removeInterval(eaten);
        }
    }
}
