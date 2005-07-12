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

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.*;

public class LayoutDesigner implements LayoutConstants {

    private LayoutModel layoutModel;

    private VisualMapper visualMapper;

    private LayoutDragger dragger;

//    private Object removeUndoMark;

    private Listener modelListener;

    private boolean dirty;

    // -----

    public LayoutDesigner(LayoutModel model, VisualMapper mapper) {
        layoutModel = model;
        visualMapper = mapper;
        modelListener = new Listener();
        modelListener.activate();
    }

    // -------
    // updates of the current visual state stored in the model

    public boolean updateCurrentState() {
        List updatedContainers = updatePositions(null);

        if (dirty) {
            // after everything is laid out (all positions known), we perform
            // gaps optimization over the whole layout (as the operations done
            // by designer produce unwanted gaps)
            // these additional changes require to build the layout once again
            // [need to make sure code generator acts afterwards,
            //  and some optimization is needed not to scan everything...]
            dirty = false;
            modelListener.deactivate();
            Iterator it = updatedContainers.iterator();
            while (it.hasNext()) {
                LayoutComponent cont = (LayoutComponent) it.next();
                for (int i=0; i < DIM_COUNT; i++) {
                    LayoutInterval root = cont.getLayoutRoot(i);
                    optimizeGaps(root, i, true);
                    findContainerResizingGap(root, i);
                }
            }
            modelListener.activate();

            // the model has changed - rebuild the layout (synchronously)
            it = updatedContainers.iterator();
            while (it.hasNext()) {
                LayoutComponent cont = (LayoutComponent) it.next();
                visualMapper.rebuildLayout(cont.getId());
            }

            updatePositions(updatedContainers);

            return true;
        }
        return false;
    }

    private List updatePositions(List updatedContainers) {
        List containers = updatedContainers != null ?
                          updatedContainers : new LinkedList();

        Iterator it = layoutModel.getAllComponents();
        while (it.hasNext()) {
            LayoutComponent comp = (LayoutComponent) it.next();
            if (comp.isLayoutContainer()) {
                Rectangle bounds = visualMapper.getContainerInterior(comp.getId());
                if (bounds != null) {
                    comp.setCurrentInterior(bounds);
                    if (updatedContainers == null) {
                        containers.add(comp);
                    }
                }
            }
            if (comp.getParent() != null) {
                Rectangle bounds = visualMapper.getComponentBounds(comp.getId());
                if (bounds != null) {
                    int baseline = visualMapper.getBaselinePosition(comp.getId());
                    comp.setCurrentBounds(bounds, baseline);
                }
            }
        }

        it = containers.iterator();
        while (it.hasNext()) {
            LayoutComponent comp = (LayoutComponent) it.next();
            for (int i=0; i < DIM_COUNT; i++) {
                LayoutInterval root = comp.getLayoutRoot(i);
                if (root.getSubIntervalCount() == 0) {
                    // [sort of hack - would be nice the filling gap is ensured somewhere else]
                    propEmptyContainer(root, i);
                }
                else {
                    updateLayoutStructure(root, i);
                }
            }
        }

        return containers;
    }

    // recursive
    /**
     * Updates current space (LayoutRegion) of all groups in the layout.
     * Also for all intervals having the preferred size explicitly defined the
     * pref size is updated according to the current state. Min and max sizes
     * as well if they are the same as pref.
     */
    void updateLayoutStructure(LayoutInterval interval, int dimension) {
        // [size definition of intervals should be changed via LayoutModel.setIntervalSize]
        LayoutRegion space = interval.getCurrentSpace();
        boolean baseline = interval.getGroupAlignment() == BASELINE;

        Iterator it = interval.getSubIntervals();
        boolean first = true;
        boolean firstResizingSpace = false;
        int leadingSpace = 0;
        while (it.hasNext()) {
            LayoutInterval sub = (LayoutInterval) it.next();
            if (dirty) {
                sub.unsetAttribute(LayoutInterval.DESIGN_ATTRS);
            }
            if (sub.isEmptySpace()) {
                if (interval.isSequential() && (first || !it.hasNext())) {
                    int min = sub.getMinimumSize(true);
                    int pref = sub.getPreferredSize(true);
                    int max = sub.getMaximumSize(true);
                    if ((min == pref || min == USE_PREFERRED_SIZE)
                        && (pref == max || max == USE_PREFERRED_SIZE)) {
                        // Fixed size padding
                        if (pref == NOT_EXPLICITLY_DEFINED) {
                            pref = LayoutDragger.getPreferredPadding(sub, dimension);
                        }
                        if (first) {
                            leadingSpace = pref;
                        } else {
                            space.reshape(dimension, TRAILING, pref);
                        }
                    } else {
                        int currentPref = LayoutRegion.UNKNOWN;
                        LayoutInterval sibling = LayoutInterval.getNeighbor(sub, SEQUENTIAL, first ? LEADING : TRAILING);
                        if (sibling == null) {
                            LayoutRegion rootSpace = LayoutInterval.getRoot(interval).getCurrentSpace();
                            if (first) {
                                firstResizingSpace = true;
                                leadingSpace = - rootSpace.positions[dimension][LEADING];
                            } else { // last
                                currentPref = rootSpace.positions[dimension][TRAILING] - space.positions[dimension][TRAILING];
                                space.reshape(dimension, TRAILING, currentPref);
                            }
                        } else if (sibling.isEmptySpace()) {
                            LayoutInterval parent = interval.getParent();
                            LayoutInterval alignedParent = parent;
                            int align = first ? LEADING : TRAILING;
                            while (parent != null && LayoutInterval.isAlignedAtBorder(interval, parent, align)) {
                                alignedParent = parent;
                                parent = parent.getParent();
                            }
                            LayoutInterval parComp = getOutermostComponent(alignedParent, dimension, align);
                            // [this assert is maybe too strong - it's ok if the component at least touches the border - but that's hard to determine here]
//                            assert LayoutInterval.isAlignedAtBorder(parComp, interval.getParent(), first ? LEADING : TRAILING);
                            LayoutRegion parSpace = parComp.getCurrentSpace();
                            if (first) {
                                firstResizingSpace = true;
                                leadingSpace = - parSpace.positions[dimension][LEADING];
                            }
                            else {
                                currentPref = parSpace.positions[dimension][TRAILING] - space.positions[dimension][TRAILING];
                                space.reshape(dimension, TRAILING, currentPref);
                            }
                        } else {
                            if (first) {
                                firstResizingSpace = true;
                                LayoutRegion sibSpace = sibling.getCurrentSpace();
                                leadingSpace = - sibSpace.positions[dimension][TRAILING];
                            } else {
                                if (!sibling.isComponent()) {
                                    sibling.getCurrentSpace().reset();
                                    updateLayoutStructure(sibling, dimension);
                                }
                                LayoutRegion sibSpace = sibling.getCurrentSpace();
                                currentPref = sibSpace.positions[dimension][LEADING] - space.positions[dimension][TRAILING];
                                space.reshape(dimension, TRAILING, currentPref);
                            }
                        }

                        if (currentPref != LayoutRegion.UNKNOWN) { // not first resizing gap
                            imposeCurrentGapSize(sub, currentPref, dimension);
                        }
                    }
                }
                first = false;
                continue;
            }

            LayoutRegion subSpace = sub.getCurrentSpace();

            if (sub.getSubIntervalCount() > 0) {
                if (!sub.isComponent()) {
                    subSpace.reset();
                    /*if (interval.getParent() == null) {
                        subSpace.set(space, dimension); // root space is known
                    }*/
                }
                updateLayoutStructure(sub, dimension);
            }
            else {
                // consistency check - there should be no empty groups
                assert sub.isComponent() || !sub.isGroup();
            }

            space.expand(subSpace);

            if (baseline && sub.isComponent()) {
                int baselinePos = subSpace.positions[dimension][BASELINE];
                if (baselinePos != LayoutRegion.UNKNOWN) {
                    space.positions[dimension][BASELINE] = baselinePos;
                    baseline = false;
                }
            }
            if (firstResizingSpace) {
                leadingSpace += space.positions[dimension][LEADING];
                firstResizingSpace = false;
                imposeCurrentGapSize(interval.getSubInterval(0), leadingSpace, dimension);
            }
            first = false;
        }
        if (leadingSpace != 0) {
            space.reshape(dimension, LEADING, -leadingSpace);
        }
    }

    // -----
    // adding, moving, resizing

    public void startAdding(LayoutComponent[] comps,
                            Rectangle[] bounds,
                            Point hotspot)
    {
//        removeUndoMark = null;
        prepareDragger(comps, bounds, hotspot, LayoutDragger.ALL_EDGES);
    }

    public void startMoving(String[] compIds, Rectangle[] bounds, Point hotspot) {
        LayoutComponent[] comps = new LayoutComponent[compIds.length];
        for (int i=0; i < compIds.length; i++) {
            comps[i] = layoutModel.getLayoutComponent(compIds[i]);
        }

//        removeUndoMark = layoutModel.getChangeMark();
//        layoutModel.removeComponents(compIds);

        prepareDragger(comps, bounds, hotspot, LayoutDragger.ALL_EDGES);
    }

    // [change to one component only?]
    public void startResizing(String[] compIds,
                              Rectangle[] bounds,
                              Point hotspot,
                              int[] resizeEdges)
    {
        LayoutComponent[] comps = new LayoutComponent[compIds.length];
        for (int i=0; i < compIds.length; i++) {
            comps[i] = layoutModel.getLayoutComponent(compIds[i]);
        }

        int[] edges = new int[DIM_COUNT];
        for (int i=0; i < DIM_COUNT; i++) {
            edges[i] = resizeEdges[i] == LEADING || resizeEdges[i] == TRAILING ?
                       resizeEdges[i] : LayoutRegion.NO_POINT;
        }

//        removeUndoMark = layoutModel.getChangeMark();
//        layoutModel.removeComponents(compIds);

        prepareDragger(comps, bounds, hotspot, edges);
        dragger.setTargetContainer(comps[0].getParent());
    }

    private void prepareDragger(LayoutComponent[] comps,
                                Rectangle[] bounds,
                                Point hotspot,
                                int[] edges)
    {
        if (comps.length != bounds.length)
            throw new IllegalArgumentException();

        LayoutRegion[] movingFormation = new LayoutRegion[bounds.length];
        for (int i=0; i < bounds.length; i++) {
            int baseline = visualMapper.getBaselinePosition(comps[i].getId());
            int baselinePos = baseline > 0 ? bounds[i].y + baseline : LayoutRegion.UNKNOWN;
            movingFormation[i] = new LayoutRegion();
            movingFormation[i].set(bounds[i], baselinePos);
        }

        dragger = new LayoutDragger(comps,
                                    movingFormation,
                                    new int[] { hotspot.x, hotspot.y },
                                    edges,
                                    visualMapper);
    }

    /**
     * @param p mouse cursor position in coordinates of the whole design area;
     *        it is adjusted if the position is changed (due to a snap effect)
     * @param containerId for container the cursor is currently moved over,
     *        can be null if e.g. a root container is resized
     * @param autoPositioning if true, searching for optimal position will be
     *        performed - a found position will be painted and the moving
     *        component snapped to it
     * @param lockDimension if true, one dimension is locked for this move
     *        (does not change); the dimension to lock must have aligned
     *        position found in previous move steps - if this is true for both
     *        dimensions then the one with smaller delta is chosen
     * @param bounds (output) bounds of moving components after the move
     */
    public void move(Point p,
                     String containerId,
                     boolean autoPositioning,
                     boolean lockDimension,
                     Rectangle[] bounds)
    {
        if (dirty) {
            return; // visual state of layout structure not updated yet (from last operation)
        }

        if (!dragger.isResizing() && (!lockDimension || dragger.getTargetContainer() == null)) {
            dragger.setTargetContainer(layoutModel.getLayoutComponent(containerId));
        }

        cursorPos[HORIZONTAL] = p.x;
        cursorPos[VERTICAL] = p.y;

        dragger.move(cursorPos, autoPositioning, lockDimension);

        p.x = cursorPos[HORIZONTAL];
        p.y = cursorPos[VERTICAL];

        if (bounds != null) {
            LayoutRegion[] current = dragger.getMovingBounds();
            for (int i=0; i < current.length; i++) {
                current[i].toRectangle(bounds[i]);
            }
        }
    }

    public void endMoving(boolean committed) {
        if (committed) {
            LayoutComponent[] components = dragger.getMovingComponents();
            LayoutComponent targetContainer = dragger.getTargetContainer();

            if (targetContainer != null) {
                boolean newComponent = components[0].getParent() == null;
                LayoutDragger.PositionDef[] positions = dragger.getPositions();
                LayoutDragger.PositionDef[] secondPositions = null;
                for (int dim=0; dim < DIM_COUNT; dim++) {
                    LayoutDragger.PositionDef pos = positions[dim];
                    if (pos == null) {
                        assert !newComponent;
                        positions[dim] = getCurrentPosition(components[0].getLayoutInterval(dim), true, dim, DEFAULT);
                        // [maybe dragger should rather provide positions for both dimensions;
                        //  current position for non-resized dimension is inaccurate]
                    }
                    else if (dragger.isResizing(dim)
                              && (pos.alignment == LEADING || pos.alignment == TRAILING))
                    {   // get current position, use the new one as secondary
                        assert !newComponent;
                        LayoutDragger.PositionDef currentPos =
                                getCurrentPosition(components[0].getLayoutInterval(dim), true, dim, pos.alignment^1);
                        if (currentPos != null) {
                            positions[dim] = currentPos;
                            if (secondPositions == null) {
                                secondPositions = new LayoutDragger.PositionDef[DIM_COUNT];
                            }
                            secondPositions[dim] = pos;
                        }
                    }
                }

                // Calculate moving and original space
                LayoutRegion movingSpace = dragger.getMovingSpace();
                LayoutRegion origSpace = new LayoutRegion();
                LayoutRegion[] movingBounds = dragger.getMovingBounds();
                for (int i=0; i<components.length; i++) {
                    origSpace.expand(components[i].getLayoutInterval(0).getCurrentSpace());
                }

                // Determine intervals that should be added
                LayoutInterval[] addingInts = new LayoutInterval[DIM_COUNT];
                LayoutInterval[] childs = new LayoutInterval[components.length];
                for (int dim=0; dim < DIM_COUNT; dim++) {
                    if (components.length > 1) {
                        for (int i=0; i<components.length; i++) {
                            childs[i] = components[i].getLayoutInterval(dim);
                        }
                        LayoutInterval parent = findCommonParent(childs);
                        // Restriction of the layout model of the common parent
                        // in the original layout
                        addingInts[dim] = restrictedCopy(parent, components, origSpace, dim, null);
                    } else {
                        addingInts[dim] = components[0].getLayoutInterval(dim);
                    }
                }

                // Remove components from original location
                for (int i=0; i<components.length; i++) {
                    if (components[i].getParent() != null) {
                        if (components.length == 1) {
                            layoutModel.removeComponents(components);
                        } else {
                            // Don't remove layout intervals of components from
                            // their parents - they are placed in the added
                            // intervals (groups) already
                            layoutModel.removeComponent(components[i]);
                        }
                    }
                }

                modelListener.deactivate(); // from now do not react on model changes

                int overlapDim = getDimensionSolvingOverlap(positions);

                for (int dim=0; dim < DIM_COUNT; dim++) {
                    LayoutInterval adding = addingInts[dim];
                    LayoutDragger.PositionDef pos1 = positions[dim];
                    LayoutDragger.PositionDef pos2 =
                            secondPositions != null ? secondPositions[dim] : null;

                    if (dragger.isResizing(dim)) {
                        checkResizing(adding, pos1, pos2, dim);
                    } else {
                        for (int i=0; i<components.length; i++) {
                            LayoutInterval compInt = components[i].getLayoutInterval(dim);
                            if (compInt.getPreferredSize() != NOT_EXPLICITLY_DEFINED) {
                                resizeInterval(compInt, movingBounds[i].size(dim));
                            }
                        }
                    }
                    adding.setCurrentSpace(movingSpace);

                    addInterval(adding, pos1, pos2, dim, dim == overlapDim);

                    if (pos2 == null && dragger.isResizing() && !dragger.isResizing(dim)) {
                        LayoutDragger.PositionDef pos = getCurrentPosition(adding, false, dim, DEFAULT);
                        if (pos != null) {
                            pos1 = pos;
                            // [maybe dragger should rather provide positions for both dimensions]
                        }
                    }
                    
                    if (components.length == 1) { // PENDING
                        checkResizing(adding, pos1, pos2, dim);
                    }
                }

                // Add components to the target container
                for (int i=0; i<components.length; i++) {
                    layoutModel.addComponent(components[i], targetContainer, -1);
                }

                if (dragger.isResizing() || (newComponent && components[0].isLayoutContainer())) {
                    imposeCurrentSize(components[0], dragger.getSizes());
                }

                dirty = true;
            }
            else { // resizing root container
                assert dragger.isResizing();
                modelListener.deactivate(); // do not react on model changes
                LayoutRegion space = dragger.getMovingBounds()[0];
                for (int dim=0; dim < DIM_COUNT; dim++) {
                    components[0].getLayoutInterval(dim).setCurrentSpace(space);
                }
                imposeCurrentSize(components[0], dragger.getSizes());
                // not changing structure, need not set "dirty"
            }

            modelListener.activate();
        }

        dragger = null;
    }

    /**
     * Creates copy of the given interval restricted to specified components
     * and region (<code>space</code>).
     *
     * @param interval interval whose restricted copy should be created.
     * @param components components that determine the restriction.
     * @param space region (current space) that determine the restriction.
     * @param dimension dimension that restricted layout interval belongs to.
     * @param temp internal helper parameter for recursive invocation.
     * Pass <code>null</code> when you invoke this method.
     */
    private LayoutInterval restrictedCopy(LayoutInterval interval,
        LayoutComponent[] components, LayoutRegion space, int dimension, List temp) {
        boolean processTemp = (temp == null);
        if (temp == null) {
            temp = new LinkedList();
        }
        if (interval.isGroup()) {
            boolean parallel = interval.isParallel();
            LayoutInterval copy = new LayoutInterval(parallel ? PARALLEL : SEQUENTIAL);
            copy.setAlignment(interval.getAlignment());
            copy.setAttributes(interval.getAttributes());
            copy.setSizes(interval.getMinimumSize(), interval.getPreferredSize(), interval.getMaximumSize());
            if (parallel) {                
                copy.setGroupAlignment(interval.getGroupAlignment());
            }
            Iterator iter = interval.getSubIntervals();
            int compCount = 0; // Number of components already copied to the group
            boolean includeGap = false; // Helper variables that allow us to insert gaps
            int firstGapToInclude = 0;  // instead of components that has been filtered out.
            int gapStart = interval.getCurrentSpace().positions[dimension][LEADING];
            while (iter.hasNext()) {
                LayoutInterval sub = (LayoutInterval)iter.next();
                LayoutInterval subCopy = restrictedCopy(sub, components, space, dimension, temp);
                if (subCopy != null) {
                    if (!sub.isEmptySpace()) {
                        if (includeGap) {
                            gapStart = Math.max(space.positions[dimension][LEADING], gapStart);
                            int size = sub.getCurrentSpace().positions[dimension][LEADING] - gapStart;
                            integrateGap(copy, size, firstGapToInclude);
                            includeGap = false;
                        }
                        gapStart = sub.getCurrentSpace().positions[dimension][TRAILING];
                        firstGapToInclude = copy.getSubIntervalCount();
                    }                    
                    if (sub.isComponent()) {
                        // Remember where to add component intervals - they cannot
                        // be moved immediately because the model listener would
                        // destroy the adjacent intervals before we would be able
                        // to copy them.
                        temp.add(subCopy);
                        temp.add(copy);
                        temp.add(new Integer(subCopy.getRawAlignment()));
                        temp.add(new Integer(copy.getSubIntervalCount() + compCount));
                        compCount++;
                    } else {
                        layoutModel.addInterval(subCopy, copy, -1);
                    }
                } else {
                    if (!parallel) {
                        includeGap = true;
                    }
                }
            }
            if (includeGap) {
                gapStart = Math.max(space.positions[dimension][LEADING], gapStart);
                int gapEnd = Math.min(space.positions[dimension][TRAILING], interval.getCurrentSpace().positions[dimension][TRAILING]);
                integrateGap(copy, gapEnd - gapStart, firstGapToInclude);
            }
            if (copy.getSubIntervalCount() + compCount > 0) {
                if (processTemp) {
                    // Insert component intervals
                    iter = temp.iterator();
                    while (iter.hasNext()) {
                        LayoutInterval comp = (LayoutInterval)iter.next();
                        LayoutInterval parent = (LayoutInterval)iter.next();
                        int alignment = ((Integer)iter.next()).intValue();
                        int index = ((Integer)iter.next()).intValue();
                        layoutModel.removeInterval(comp);
                        layoutModel.setIntervalAlignment(comp, alignment);
                        layoutModel.addInterval(comp, parent, index);
                    }
                    compCount = 0;
                }
                // consolidate copy
                if ((copy.getSubIntervalCount() == 1) && (compCount == 0)) {
                    LayoutInterval subCopy = copy.getSubInterval(0);
                    layoutModel.removeInterval(subCopy);
                    layoutModel.setIntervalAlignment(subCopy, copy.getAlignment());
                    copy = subCopy;
                }
                return copy;
            } else {
                return null;
            }
        } else if (interval.isComponent()) {
            LayoutComponent comp = interval.getComponent();
            if (Arrays.asList(components).contains(comp)) {
                return interval; // Don't copy layout component's intervals
            }
            return null;
        } else {
            assert interval.isEmptySpace();
            int[] bounds = emptySpaceBounds(interval, dimension);
            int rangeStart = space.positions[dimension][LEADING];
            int rangeEnd = space.positions[dimension][TRAILING];
            if ((bounds[0] < rangeEnd) && (bounds[1] > rangeStart)) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setAttributes(interval.getAttributes());
                if ((bounds[0] < rangeStart) || (bounds[1] > rangeEnd)) {
                    // Partial overlap with the provides space
                    int min = interval.getMinimumSize();
                    if (min >= 0) min = USE_PREFERRED_SIZE;
                    int pref = Math.min(bounds[1], rangeEnd) - Math.max(bounds[0], rangeStart);
                    int max = interval.getMaximumSize();
                    if (max >= 0) max = USE_PREFERRED_SIZE;
                    gap.setSizes(min, pref, max);
                } else {
                    gap.setSizes(interval.getMinimumSize(), interval.getPreferredSize(), interval.getMaximumSize());
                }
                return gap;
            } else {
                // Outside the provided space
                return null;
            }
        }        
    }

    /**
     * Helper method used by <code>restrictedCopy()</code> method.
     * Replaces empty spaces at the end of the sequential group
     * by an empty space of the specified size. Only empty spaces
     * with index >= boundary are replaced.
     *
     * @param seqGroup sequential group.
     * @param size size of the empty space that should be added.
     * @param boundary index in the sequential group that limits
     * the replacement of the empty spaces.
     */
    private void integrateGap(LayoutInterval seqGroup, int size, int boundary) {
        modelListener.deactivate();
        while ((seqGroup.getSubIntervalCount() > boundary)
            && seqGroup.getSubInterval(seqGroup.getSubIntervalCount()-1).isEmptySpace()) {
            layoutModel.removeInterval(seqGroup.getSubInterval(seqGroup.getSubIntervalCount()-1));
        }
        if (size > 0) {
            LayoutInterval gap = new LayoutInterval(SINGLE);
            gap.setSize(size);
            layoutModel.addInterval(gap, seqGroup, -1);
        }
        modelListener.activate();
    }
    
    /**
     * Returns bounds (e.g. current space) of the empty space in the given dimension.
     *
     * @param emptySpace empty space.
     * @param dimenion dimension.
     * @return array whose the first item is the leading bound of the empty
     * space and the second item is the trailing bound.
     */
    private int[] emptySpaceBounds(LayoutInterval emptySpace, int dimension) {
        assert emptySpace.isEmptySpace();
        int leading, trailing;
        LayoutInterval parent = emptySpace.getParent();
        int index = parent.indexOf(emptySpace);
        if (index == 0) {
            leading = parent.getCurrentSpace().positions[dimension][LEADING];            
        } else {
            leading = parent.getSubInterval(index - 1).getCurrentSpace().positions[dimension][TRAILING];
        }
        if (index+1 == parent.getSubIntervalCount()) {
            trailing = parent.getCurrentSpace().positions[dimension][TRAILING];
        } else {
            trailing = parent.getSubInterval(index + 1).getCurrentSpace().positions[dimension][LEADING];
        }
        return new int[] {leading, trailing};
    }

    public void removeDraggedComponents() {
        if (dragger != null) {
            LayoutComponent[] components = dragger.getMovingComponents();
            if (components[0].getParent() != null) { // remove from original location
                layoutModel.removeComponents(components);
            }
            endMoving(false);
        }
    }

    public void paintMoveFeedback(Graphics2D g) {
        dragger.paintMoveFeedback(g);
    }
    
    /**
     * Paints layout information (alignment) for the selected component.
     *
     * @param g graphics object to use.
     * @param componentId ID of selected component.
     */
    public void paintSelection(Graphics2D g, String componentId) {
        LayoutComponent comp = layoutModel.getLayoutComponent(componentId);
        if (comp != null) {
            paintSelection(g, comp, HORIZONTAL);
            paintSelection(g, comp, VERTICAL);
        }
    }
    
    /**
     * Paints layout information (alignment) for the selected component
     * and specified dimension.
     *
     * @param g graphics object to use.
     * @param component selected layout component.
     * @param dimension dimension whose layout should be visualized.
     */
    private void paintSelection(Graphics2D g, LayoutComponent component, int dimension) {
        LayoutInterval interval = component.getLayoutInterval(dimension);
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
                int alignment = getEffectiveAlignment(interval);
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
                        paintAlignment(g, candidate, dimension, getEffectiveAlignment(candidate));
                    }
                }
            } else {
                int alignment = interval.getAlignment();
                if (!LayoutInterval.wantResize(interval, true)) {
                    lastAlignment = alignment;
                }
                paintAlignment(g, interval, dimension, lastAlignment);
            }
            interval = interval.getParent();
        }
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
                    x2 = x2int.getCurrentSpace().positions[dimension][LEADING];
                }
                yb = visualIntervalPosition(x2int, opposite, LEADING);
                x2group = x2int.isGroup();
            }
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
        if ((x1 != LayoutRegion.UNKNOWN) && (x2 != LayoutRegion.UNKNOWN) && (x2 - x1 > 1)) {
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
    // LayoutModel.Listener implementation & related

    class Listener implements LayoutModel.Listener {
        public void layoutChanged(LayoutEvent ev) {
            if (!layoutModel.isUndoRedoInProgress()) {
                deactivate();
                LayoutDesigner.this.layoutChanged(ev);
                activate();
            }
        }
        void activate() {
            layoutModel.addListener(this);
        }
        void deactivate() {
            layoutModel.removeListener(this);
        }
    };

    private void layoutChanged(LayoutEvent ev) {
        if (ev.getType() == LayoutEvent.INTERVAL_REMOVED) {
            // component interval was removed - need to clear neighbor gaps etc.
            LayoutInterval interval = ev.getInterval();
            LayoutComponent comp = interval.getComponent();
            if (comp != null) {
                int dim = -1;
                for (int i=0; i < DIM_COUNT; i++) {
                    if (comp.getLayoutInterval(i) == interval) {
                        dim = i;
                        break;
                    }
                }
                assert dim > -1;
                intervalRemoved(ev.getParentInterval(),
                                ev.getIndex(),
                                true,
                                LayoutInterval.wantResize(ev.getInterval(), false),
                                dim);
            }
        }
    }

    // -----

    // [the right way to do this still TBD]
    private int getDimensionSolvingOverlap(LayoutDragger.PositionDef[] positions) {
        if ((dragger.isResizing(VERTICAL) && !dragger.isResizing(HORIZONTAL))
            || (positions[HORIZONTAL].snapped && !positions[VERTICAL].snapped)
            || (!positions[VERTICAL].nextTo && positions[VERTICAL].snapped
                && (positions[VERTICAL].interval.getParent() == null)
                && !existsComponentPlacedAtBorder(positions[VERTICAL].interval, VERTICAL, positions[VERTICAL].alignment))) {
            return VERTICAL;
        }
        if (positions[VERTICAL].nextTo && positions[VERTICAL].snapped
            && (positions[VERTICAL].interval.getParent() == null)) {
            int alignment = positions[VERTICAL].alignment;
            int[][] overlapSides = overlappingGapSides(positions);
            if (((alignment == LEADING) || (alignment == TRAILING))
                && (overlapSides[VERTICAL][1-alignment] != 0)
                && (overlapSides[VERTICAL][alignment] == 0)) {
                return VERTICAL;
            }
        }
        if (!positions[HORIZONTAL].snapped && !positions[VERTICAL].snapped) {
            boolean[] overlapDim = overlappingGapDimensions(positions);
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
    private boolean existsComponentPlacedAtBorder(LayoutInterval interval, int dimension, int alignment) {
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
    private void fillOverlappingComponents(List overlaps, LayoutInterval group, LayoutRegion region) {
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
    private boolean[] overlappingGapDimensions(LayoutDragger.PositionDef[] positions) {
        boolean[] result = new boolean[2];
        int[][] overlapSides = overlappingGapSides(positions);
        for (int i=0; i<DIM_COUNT; i++) {
            result[i] = (overlapSides[i][0] == 1) && (overlapSides[i][1] == 1);
        }
        return result;        
    }
    
    // Helper method for getDimensionSolvingOverlap() method
    private int[][] overlappingGapSides(LayoutDragger.PositionDef[] positions) {
        int[][] overlapSides = new int[][] {{0,0},{0,0}};
        List overlaps = new LinkedList();
        LayoutInterval layoutRoot = LayoutInterval.getRoot(positions[HORIZONTAL].interval);
        LayoutRegion region = dragger.getMovingSpace();
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
    private int[] overlappingSides(LayoutRegion compRegion, LayoutRegion region, int dimension) {
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

    // to be called after adding/moving/resizing - assumes LayoutDragger still exists
    private void checkResizing(LayoutInterval interval,
                               LayoutDragger.PositionDef pos1,
                               LayoutDragger.PositionDef pos2,
                               int dimension)
    {
        assert interval.isComponent();

        boolean resizing;
        if (pos2 != null) {
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
        }

        if (!resizing) {
            layoutModel.setIntervalSize(interval,
                    USE_PREFERRED_SIZE, interval.getPreferredSize(), USE_PREFERRED_SIZE);
        }
        else if (dragger.isResizing(dimension)) {
            // [TODO act according to potential resizability of the component,
            //  not only on resizing operation - the condition should be:
            //  isComponentResizable(interval.getComponent(), dimension)  ]
            layoutModel.setIntervalSize(interval,
                    NOT_EXPLICITLY_DEFINED, interval.getPreferredSize(), Short.MAX_VALUE);
        }
    }

    private boolean isComponentResizable(LayoutComponent comp, int dimension) {
        boolean[] res = comp.getResizability();
        if (res == null) {
            res = visualMapper.getComponentResizability(comp.getId(), new boolean[DIM_COUNT]);
            comp.setResizability(res);
        }
        return res[dimension];
    }

    /**
     * Adds given interval to the model. The interval was moved around via
     * mouse - its last position/size is stored in its "current state", the
     * suggested logical position from dragger comes with the 'pos' parameter.
     */
    private void addInterval(LayoutInterval interval,
                             LayoutDragger.PositionDef pos1,
                             LayoutDragger.PositionDef pos2,
                             int dimension,
                             boolean dealWithOverlap)
    {
        int alignment1 = pos1.alignment;
        int alignment2 = pos2 != null ? pos2.alignment : DEFAULT;

        if (alignment1 == CENTER || alignment1 == BASELINE) {
            // create (or add to) a closed group
            addSimplyAligned(interval, pos1.interval, alignment1);
            // (necessary for multi-component move of baseline aligned components)
            destroyGroupIfRedundant(interval, interval.getParent());
            return;
            // [need to do full space analysis like for LEADING or TRAILING - for the whole group]
        }

        // analyze how the adding interval affects the existing layout - there
        // can be multiple groups potentially affected (need not be "compatible")
        List inclusions = new LinkedList();
        analyzeAddingParallel(LayoutInterval.getRoot(pos1.interval),
                              interval.getCurrentSpace(),
                              inclusions,
                              dimension,
                              alignment1, alignment2,
                              dealWithOverlap);

        // find the best set of compatible inclusions
        LayoutInterval commonParent = findBestInclusions(inclusions, pos1, dimension);

        boolean multipleImpact = inclusions.size() > 1;
        int mainEffectiveAlign = alignment1;
        boolean needAlign = !pos1.nextTo || (pos2 != null && !pos2.nextTo);

        // in case of multiple inclusions we'll need to split the groups the interval
        // goes through and group their content into separate groups around the interval
        List separatedLeading;
        List separatedTrailing;
        if (multipleImpact) {
            separatedLeading = new LinkedList();
            separatedTrailing = new LinkedList();
        }
        else {
            separatedLeading = separatedTrailing = null;
        }

        Iterator it = inclusions.iterator();
        while (it.hasNext()) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            boolean subAligned = (!pos1.nextTo && iDesc.group != pos1.interval.getParent() && iDesc.interval != pos1.interval)
                                 || (pos2 != null && !pos2.nextTo);
            if (!subAligned) {
                needAlign = false;
            }

            addToGroup(interval, iDesc, subAligned, dimension, alignment1, alignment2);

            if (multipleImpact) { // adding was done only to have the split point
                if (!subAligned) {
                    mainEffectiveAlign = getEffectiveAlignment(interval);
                }
                // [assuming adding was done to a sequence, probably ok assumption...]
                extract(interval, alignment1, true, separatedLeading, separatedTrailing);
                layoutModel.removeInterval(interval);
                layoutModel.removeInterval(iDesc.group);
            }
        }

        if (!multipleImpact) { // if this was single adding...
            accommodateOutPosition(interval, dimension, alignment1^1,  pos2!= null && pos2.snapped); // adapt size of parent/neighbor
            if (dimension == HORIZONTAL && !needAlign && interval.getParent().isSequential()) {
                // check whether the added interval could not be rather placed
                // in a neighbor parallel group
                if (!moveInside(interval, dimension, alignment1)) {
                    moveInside(interval, dimension, alignment1^1);
                }
            }
        }

        // in case of multiple groups affected, the interval has not been added
        // yet, just the surroundings extracted - now add the interval and form
        // the remainder groups around it
        if (multipleImpact) {
            LayoutInterval commonSeq;
            int index;
            if (commonParent.getSubIntervalCount() == 0 && commonParent.getParent() != null) {
                // the common group got empty - eliminate it to avoid unncessary nesting
                LayoutInterval parent = commonParent.getParent();
                index = layoutModel.removeInterval(commonParent);
                if (parent.isSequential()) {
                    commonSeq = parent;
                    commonParent = parent.getParent();
                }
                else { // parallel parent
                    commonSeq = new LayoutInterval(SEQUENTIAL);
                    commonSeq.setAlignment(commonParent.getAlignment());
                    layoutModel.addInterval(commonSeq, parent, index);
                    commonParent = parent;
                    index = 0;
                }
            }
            else {
                commonSeq = new LayoutInterval(SEQUENTIAL);
                layoutModel.addInterval(commonSeq, commonParent, -1);
                index = 0;
            }

            layoutModel.addInterval(interval, commonSeq, index);

            if (!separatedLeading.isEmpty()) {
                createRemainderGroup(separatedLeading,
                                     commonSeq, commonSeq.indexOf(interval),
                                     LEADING, mainEffectiveAlign);
            }
            if (!separatedTrailing.isEmpty()) {
                createRemainderGroup(separatedTrailing,
                                     commonSeq, commonSeq.indexOf(interval),
                                     TRAILING, mainEffectiveAlign);
            }
        }

        if (needAlign) { // align the added interval as required
            if (pos2 != null && !pos2.nextTo) {
                alignWith(interval, pos2.interval, dimension, alignment2);
            }
            if (!pos1.nextTo) {
                alignWith(interval, pos1.interval, dimension, alignment1);
            }
        }

        // check of the redundancy of the inserted group
        destroyGroupIfRedundant(interval, interval.getParent());
    }

    private boolean analyzeAddingParallel(LayoutInterval group,
                                          LayoutRegion addingSpace,
                                          List inclusions,
                                          int dimension,
                                          int alignment1,
                                          int alignment2,
                                          boolean dealWithOverlap)
    {
        boolean thisSignificant = false;
        boolean subSignificant = false;

        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li.isEmptySpace())
                continue;

            LayoutRegion subSpace = li.getCurrentSpace();

            // first analyze the interval as a possible sub-group
            if (li.isParallel()
                && LayoutRegion.pointInside(addingSpace, alignment1, subSpace, dimension)
                && compatibleGroupAlignment(li.getGroupAlignment(), alignment1))
            {   // adding space overlaps with this parallel group (at least partially)
                if (analyzeAddingParallel(li, addingSpace, inclusions,
                                          dimension, alignment1, alignment2,
                                          dealWithOverlap))
                {   // sub-group found as a reasonable place for adding
                    subSignificant = true;
                    // [should we go on, or not?]
                }
            }
            else if (li.isSequential()) {
                // always analyze sequence - it may be valid even if there is no
                // overlap (not required in vertical dimension)
                if (analyzeAddingSequential(li, addingSpace, inclusions,
                                            dimension, alignment1, alignment2,
                                            dealWithOverlap))
                {   // sub-sequence found as a reasonable place for adding
                    subSignificant = true;
                }
                continue;
            }

            // second analyze the interval as a single element for "next to" placement
            boolean ortOverlap = contentOverlap(addingSpace, li, dimension^1);
            int margin = (dimension == VERTICAL && !ortOverlap ? 4 : 0);
            boolean dimOverlap = LayoutRegion.overlap(addingSpace, subSpace,
                                                      dimension, margin);
            // in vertical dimension always pretend orthogonal overlap if
            // there is no direct overlap (i.e. force "next to" positioning)
            if (ortOverlap || (dimension == VERTICAL && !dimOverlap)) {
                if (dimOverlap && !dealWithOverlap) {
                    continue; // don't want to deal with the overlap here
                }
                IncludeDesc iDesc = new IncludeDesc();
                if (dimOverlap) { // full overlap
                    iDesc.overlap = true;
                    iDesc.distance = 0;
                    iDesc.ortDistance = 0;
                }
                else { // determine distance from the interval
                    int dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                     dimension, TRAILING, LEADING);
                    int dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                     dimension, TRAILING, LEADING);
                    iDesc.distance = dstL >= 0 ? dstL : dstT;
                    if (ortOverlap) {
                        iDesc.ortDistance = 0;
                    }
                    else { // remember also the orthogonal distance
                        dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                     dimension^1, TRAILING, LEADING);
                        dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                     dimension^1, TRAILING, LEADING);
                        iDesc.ortDistance = dstL > 0 ? dstL : dstT;
                    }
                }
                iDesc.parent = group;
                // iDesc.group left null - a new sequence is to be created
                iDesc.interval = li;
                inclusions.add(iDesc);
                thisSignificant = true;
            }
        }

        // always create inclusion for the whole parallel group
        // - if there is no subinterval affected to have at least something
        // - if there is a valid subinterval it may turn out it is incompatible
        //   with required aligning
        IncludeDesc iDesc = new IncludeDesc();
        if (group.getParent() != null
            && LayoutRegion.distance(addingSpace, group.getCurrentSpace(),
                                     dimension, alignment1, alignment1) == 0)
        {   // aligned with the group border - can be added to the group directly
            iDesc.parent = group.getParent();
            iDesc.group = group;
        }
        else { // group left null - a new sequence will be created
            iDesc.parent = group;
        }
        iDesc.complemental = true;
//        if (LayoutRegion.overlap(addingSpace, groupSpace, dimension^1, 0)) {
//            iDesc.overlap = true; // [should not be used only for something inside?]
//            iDesc.complemental = affected.size() > affectedCount;
//        }
//        else {
//            iDesc.noOrtOverlap = true;
//            iDesc.distance = Math.abs(LayoutRegion.nonOverlapDistance(
//                                      addingSpace, groupSpace, dimension^1));
//            iDesc.complemental = true;
//        }
        inclusions.add(iDesc);

        return thisSignificant || subSignificant;
    }

    private boolean analyzeAddingSequential(LayoutInterval group,
                                            LayoutRegion addingSpace,
                                            List inclusions,
                                            int dimension,
                                            int alignment1,
                                            int alignment2,
                                            boolean dealWithOverlap)
    {
        boolean thisSignificant = false;
        boolean subSignificant = false;

        boolean fullOverlap = false;
        boolean inSequence = false;
        boolean parallelWithSequence = false;
        int distance = Integer.MAX_VALUE;
        int ortDistance = Integer.MAX_VALUE;

        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li.isEmptySpace())
                continue;

            LayoutRegion subSpace = li.getCurrentSpace();

            // first analyze the interval as a possible sub-group
            if (li.isParallel()
                && LayoutRegion.pointInside(addingSpace, alignment1, subSpace, dimension)
                && compatibleGroupAlignment(li.getGroupAlignment(), alignment1))
            {   // adding space overlaps with this parallel group (at least partially)
                if (analyzeAddingParallel(li, addingSpace, inclusions,
                                          dimension, alignment1, alignment2,
                                          dealWithOverlap))
                {   // sub-group found as a reasonable place for adding
                    subSignificant = true;
                }
            }
            else assert !li.isSequential();

            // second analyze the interval as a single element for "next to" placement
            boolean ortOverlap = contentOverlap(addingSpace, li, dimension^1);
            int margin = (dimension == VERTICAL && !ortOverlap ? 4 : 0);
            boolean dimOverlap = LayoutRegion.overlap(addingSpace, subSpace,
                                                      dimension, margin);
            // in vertical dimension always pretend orthogonal overlap if
            // there is no direct overlap (i.e. force inserting into sequence)
            if (ortOverlap || (dimension == VERTICAL && !dimOverlap && !parallelWithSequence)) {
                if (dimOverlap) { // overlaps in both dimensions
                    if (!dealWithOverlap) {
                        return false; // don't want to solve the overlap in this sequence
                    }
                    fullOverlap = true;
                    inSequence = true;
                    distance = ortDistance = 0;
                }
                else { // determine distance from the interval
                    int dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                     dimension, TRAILING, LEADING);
                    int dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                     dimension, TRAILING, LEADING);
                    if (dstL >= 0 && dstL < distance) {
                        distance = dstL;
                    }
                    if (dstT >= 0 && dstT < distance) {
                        distance = dstT;
                    }
                    if (ortOverlap) {
                        ortDistance = 0;
                        inSequence = true;
                    }
                    else { // remember also the orthogonal distance
                        dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                     dimension^1, TRAILING, LEADING);
                        dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                     dimension^1, TRAILING, LEADING);
                        if (dstL > 0 && dstL < ortDistance) {
                            ortDistance = dstL;
                        }
                        if (dstT > 0 && dstT < ortDistance) {
                            ortDistance = dstT;
                        }
                    }
                }
                if (getAddDirection(addingSpace, subSpace, dimension, alignment1, alignment2) == LEADING) {
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

        if (inSequence || (dimension == VERTICAL && !parallelWithSequence)) {
            // so it make sense to add the interval to this sequence
            IncludeDesc iDesc = new IncludeDesc();
            if (parallelWithSequence) {
                iDesc.parent = group;
                // not specifying iDesc.group means a new group must be created
            }
            else {
                iDesc.parent = group.getParent();
                iDesc.group = group;
            }
            iDesc.distance = distance;
            iDesc.ortDistance = ortDistance;
            iDesc.overlap = fullOverlap;
            iDesc.complemental = subSignificant;
            thisSignificant = true;
            inclusions.add(iDesc);
        }

        return thisSignificant || subSignificant;
    }

    private static int getAddDirection(LayoutRegion adding,
                                       LayoutRegion existing,
                                       int dimension,
                                       int alignment1,
                                       int alignment2) // need not be provided
    {
        assert alignment1 == LEADING || alignment1 == TRAILING;

        int pos1 = LayoutRegion.distance(adding, existing, dimension, alignment1, CENTER) > 0 ?
                   LEADING : TRAILING;
        int pos2;
        if (alignment2 == LEADING || alignment2 == TRAILING) {
            pos2 = LayoutRegion.distance(adding, existing, dimension, alignment2, CENTER) > 0 ?
                   LEADING : TRAILING;
        }
        else pos2 = pos1;

        if (pos1 == pos2) {
            return pos1;
        }

        // if a component is resized out of a parallel group to the parent sequence,
        // then the secondary alignment is the right one
        // in such case the primary position points inside the interval which
        // normally does not happen in resizing
        return LayoutRegion.pointInside(adding, alignment1, existing, dimension) ?
               pos2 : pos1;
    }

    /**
     * Goes through a list of IncludeDesc objects and finds the best set of
     * compatible inclusions that should be used for processing the addition
     * of the new interval. The other inclusions are removed from the list.
     * @return common parent of the best set of inclusions
     */
    private LayoutInterval findBestInclusions(List inclusions,
                                              LayoutDragger.PositionDef pos,
                                              int dimension)
    {
        // first find a matching representative
        IncludeDesc best = null;
        Iterator it = inclusions.iterator();
        while (it.hasNext()) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (best == null) {
                best = iDesc;
            }
            else {
                int c = IncludeDesc.compare(iDesc, best, pos);
                if (c > 0 || (c == 0 && iDesc.isComplemental())) {
                    best = iDesc;
                }
            }
        }

        // remove incompatible inclusions
        it = inclusions.iterator();
        while (it.hasNext()) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (iDesc != best
                && (iDesc.isComplemental() // redundant now
                    || (!IncludeDesc.compatible(iDesc, best, pos)
                        && !adaptInclusion(iDesc, best, dimension))))
            {   // unusable inclusion
                it.remove();
            }
        }

        // choose a non-complemental inclusion compatible with the matching
        if (best.isComplemental() && inclusions.size() > 1) {
            inclusions.remove(best);
            best = (IncludeDesc) inclusions.get(0);
        }

        return best.parent;
    }

    private boolean adaptInclusion(IncludeDesc iDesc1, IncludeDesc iDesc2, int dimension) {
        // [merge groups to the same level, TBD]
        return false;
    }

    /**
     * Adds aligned with an interval to existing group, or creates new.
     * (Now used only to a limited extent for closed groups only.)
     */
    private void addSimplyAligned(LayoutInterval interval,
                                  LayoutInterval toAlignWith,
                                  int alignment)
    {
        assert alignment != LEADING && alignment != TRAILING;

        LayoutInterval parent = toAlignWith.getParent();
        if (parent.isParallel() && parent.getGroupAlignment() == alignment) {
            layoutModel.setIntervalAlignment(interval, alignment);
            layoutModel.addInterval(interval, parent, -1);
            return;
        }

        int alignIndex = layoutModel.removeInterval(toAlignWith);

        LayoutInterval subGroup = new LayoutInterval(PARALLEL);
        subGroup.setGroupAlignment(alignment);
        if (parent.isParallel()) {
            subGroup.setAlignment(toAlignWith.getAlignment());
        }

        layoutModel.setIntervalAlignment(toAlignWith, alignment);
        layoutModel.setIntervalAlignment(interval, alignment);

        layoutModel.addInterval(toAlignWith, subGroup, -1);
        layoutModel.addInterval(interval, subGroup, -1);
        layoutModel.addInterval(subGroup, parent, alignIndex);
    }

    /**
     * Adds an interval to a group according to IncludeDesc.
     */
    private void addToGroup(LayoutInterval adding,
                            IncludeDesc iDesc,
                            boolean alignedSeq,
                            int dimension,
                            int alignment1,
                            int alignment2)
    {
        if (iDesc.group == null) { // a new group for adding must be created first
            int index;
            if (iDesc.parent.isSequential()) { // adding parallely with part of a sequence
                assert alignment1 == LEADING || alignment1 == TRAILING;
                LayoutRegion space = adding.getCurrentSpace();
                if (dimension == VERTICAL) { // count in a margin in vertical direction
                    // [because analyzeAdding uses it - maybe we should get rid of it completely]
                    space = new LayoutRegion(space);
                    space.reshape(VERTICAL, LEADING, -4);
                    space.reshape(VERTICAL, TRAILING, 4);
                }
                iDesc.group = extractParallelSequence(
                                  iDesc.parent, space, false, // dimension == VERTICAL
                                  dimension, alignment1, alignment2);
                iDesc.parent = iDesc.group;
                iDesc.group = new LayoutInterval(SEQUENTIAL);
                index = -1;
            }
            else { // parallel parent
                iDesc.group = new LayoutInterval(SEQUENTIAL);
                if (iDesc.interval != null) { // adding next to a lone interval in parallel group
                    index = layoutModel.removeInterval(iDesc.interval);
                    iDesc.group.setAlignment(iDesc.interval.getAlignment());
                    layoutModel.setIntervalAlignment(iDesc.interval, DEFAULT);
                    layoutModel.addInterval(iDesc.interval, iDesc.group, -1);
                }
                else { // adding inside a parallel group (not aligned to border)
                    index = -1;
                    iDesc.group.setAlignment(alignment1);
                }
            }
            layoutModel.addInterval(iDesc.group, iDesc.parent, index);
        }

        if (iDesc.group.isSequential()) {
            addToSequence(adding, iDesc.group, alignedSeq, dimension, alignment1, alignment2);
        }
        else { // adding to parallel group
            layoutModel.setIntervalAlignment(adding, alignment1); // != DEFAULT ? alignment1 : alignment2);
            layoutModel.addInterval(adding, iDesc.group, -1);
        }
    }

    /**
     * Adds and interval to a sequential group - based on the current visual
     * position and size.
     */
    private void addToSequence(LayoutInterval adding,
                               LayoutInterval sequence,
                               boolean alignedSeq, // [is it actually needed?]
                               int dimension,
                               int alignment1,
                               int alignment2)
    {
        assert alignment1 == LEADING || alignment1 == TRAILING;

        // find the neighbors for the new interval
        LayoutRegion addingSpace = adding.getCurrentSpace();
        LayoutInterval[] neighbors = new LayoutInterval[2]; // LEADING, TRAILING
        LayoutInterval originalGap = null;

        Iterator it = sequence.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li.isEmptySpace()) {
                originalGap = li;
            }
            else {
                LayoutRegion subSpace = li.getCurrentSpace();
                int dir = getAddDirection(addingSpace, subSpace,
                                          dimension, alignment1, alignment2);
                if (dir == TRAILING) { // adding after this
                    neighbors[LEADING] = li;
                    originalGap = null;
                }
                else { // LEADING - adding before
                    neighbors[TRAILING] = li;
                    break;
                }
            }
        }

        // compute leading and trailing gaps
        LayoutInterval[] gaps = new LayoutInterval[2]; // LEADING, TRAILING

        for (int i = LEADING; i <= TRAILING; i++) {
            int distance;
            boolean meaningfulDistance;
            if (neighbors[i] != null) {
                distance = LayoutRegion.distance(neighbors[i].getCurrentSpace(),
                                                 addingSpace,
                                                 dimension, i ^ 1, i);
                if (i == TRAILING) {
                    distance = -distance;
                }
                meaningfulDistance = true;
            }
            else { // no neighbor
                distance = LayoutRegion.distance(sequence.getParent().getCurrentSpace(),
                                                 addingSpace,
                                                 dimension, i, i);
                if (i == TRAILING) {
                    distance = -distance;
                }
                meaningfulDistance = distance > 0;
            }

            if (meaningfulDistance
                && (neighbors[i] != null || !alignedSeq || alignment1 != i || originalGap != null)) //align
            {
                int pad = determineExpectingPadding(adding, neighbors[i], sequence, dimension, i);

                gaps[i] = new LayoutInterval(SINGLE);
                if (distance >= 0 && distance != pad
                    && (alignment1 == i
                        || (distance > pad
                            && (originalGap == null || originalGap.getPreferredSize() > 0))))
                {
                    gaps[i].setPreferredSize(distance);
                }
                if (alignment1 != i
                    && !LayoutInterval.wantResize(adding, false)
                    && ((originalGap != null && originalGap.getMaximumSize() == Short.MAX_VALUE)
                        || (originalGap == null && distance > 0 && !LayoutInterval.wantResize(sequence, false)
                            && (neighbors[i] != null
                                || sequence.getParent().getSubIntervalCount() == 1
                                || LayoutInterval.contentWantResize(sequence.getParent())))))
                {   // should be resizing gap
                    gaps[i].setMaximumSize(Short.MAX_VALUE);
                    // [need to detect "fill" in a non-resizable group]
                }
                else if (gaps[i].getPreferredSize() != NOT_EXPLICITLY_DEFINED) {
                    gaps[i].setMinimumSize(USE_PREFERRED_SIZE);
                    gaps[i].setMaximumSize(USE_PREFERRED_SIZE);
                }
            }
        }

        // try to avoid resizing default padding
        for (int i = LEADING; i <= TRAILING; i++) {
            LayoutInterval gap = gaps[i];
            if (gap == null)
                continue;
            LayoutInterval otherGap = gaps[i^1];
            if (gap.getMaximumSize() == Short.MAX_VALUE
                && gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                && otherGap != null)
            {   // resizing padding
                gap.setMaximumSize(USE_PREFERRED_SIZE);
                if (otherGap.getPreferredSize() >= 0) {
                    otherGap.setMaximumSize(Short.MAX_VALUE);
                }
                break;
            }
        }

        // remove original gap, add new gaps and the interval
        int index;
        if (originalGap != null) {
            index = layoutModel.removeInterval(originalGap);
        }
        else if (neighbors[TRAILING] != null) {
            index = sequence.indexOf(neighbors[TRAILING]);
        }
        else if (neighbors[LEADING] != null) {
            index = sequence.getSubIntervalCount();
        }
        else index = 0;

        if (gaps[LEADING] != null) {
            layoutModel.addInterval(gaps[LEADING], sequence, index++);
        }
        layoutModel.setIntervalAlignment(adding, DEFAULT);
        layoutModel.addInterval(adding, sequence, index++);
        if (gaps[TRAILING] != null) {
            layoutModel.addInterval(gaps[TRAILING], sequence, index);
        }
    }

/*    private int moveAside(LayoutInterval interval, int distance, int dimension, int alignment) {
        // [need to consider also shrinking resizable components]
        LayoutInterval parent = interval.getParent();
        assert parent.isSequential() && (alignment == LEADING || alignment == TRAILING);

        LayoutInterval prev = interval;
        LayoutInterval next = null;

        int orient = alignment == LEADING ? -1 : 1;
        int count = parent.getSubIntervalCount();
        int i = parent.indexOf(interval) + orient;
        while (i >= 0 && i < count && distance > 0) {
            LayoutInterval li = parent.getSubInterval(i);
            i += orient;

            if (li.isEmptySpace()) {
                if (i >= 0 && i < count) {
                    next = parent.getSubInterval(i);
                    assert !next.isEmptySpace();
                }
                else next = null;

                int pad = findInternalPadding(next, parent, prev, dimension, alignment);
                int pref = li.getPreferredSize();
                int delta = pref - pad;
                if (delta > 0) {
                    LayoutInterval gap = new LayoutInterval(SINGLE);
                    if (delta > distance) {
                        delta = distance;
                        if (pref == li.getMinimumSize()) {
                            gap.setMinimumSize(pref - delta);
                        }
                        gap.setPreferredSize(pref - delta);
                        if (pref == li.getMaximumSize()) {
                            gap.setMaximumSize(pref - delta);
                        }
                    }
                    else if (li.getMinimumSize() < pad) {
                        gap.setMinimumSize(li.getMinimumSize());
                    }
                    if (li.getMaximumSize() == Short.MAX_VALUE) {
                        gap.setMaximumSize(Short.MAX_VALUE);
                    }

                    int idx = layoutModel.removeInterval(li);
                    layoutModel.addInterval(gap, parent, idx);

                    distance -= delta;
                }
            }
            else {
                prev = li;
            }
        }

        return distance;
    } */

    /**
     * When an interval is added or resized out of current boundaries of its
     * parent, this method tries to accommodate the size increment in the parent
     * (and its parents). It acts according to the current visual position of
     * the interval - how it exceeds the current parent border. In the simplest
     * form the method tries to shorten the nearest gap in the parent sequence.
     * [practically this method is now needed when an interval is resized out of its parent and snapped]
     */
    private void accommodateOutPosition(LayoutInterval interval, int dimension, int alignment, boolean snapped) {
        if (alignment == CENTER || alignment == BASELINE) {
            return; // [but should consider these too...]
        }

        int pos = interval.getCurrentSpace().positions[dimension][alignment];
        assert pos != LayoutRegion.UNKNOWN;
        int sizeIncrement = Integer.MIN_VALUE;
        LayoutInterval parent = interval.getParent();

        do {
            if (parent.isSequential()) {
                if (sizeIncrement > 0) {
                    sizeIncrement = accommodateSizeInSequence(interval, sizeIncrement, dimension, alignment, snapped);
                }
                if (parent.getSubInterval(alignment == LEADING ? 0 : parent.getSubIntervalCount()-1) != interval) {
                    return; // not a border interval in the sequence, can't go up
                }
            }
            else {
                int groupPos = parent.getCurrentSpace().positions[dimension][alignment];
                if (groupPos != LayoutRegion.UNKNOWN) {
                    sizeIncrement = alignment == LEADING ? groupPos - pos : pos - groupPos;
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

    private int accommodateSizeInSequence(LayoutInterval interval, int sizeIncrement, int dimension, int alignment, boolean snapped) {
        LayoutInterval parent = interval.getParent();
        assert parent.isSequential();
        int d = alignment == LEADING ? -1 : 1;
        int i = parent.indexOf(interval) + d;
        int n = parent.getSubIntervalCount();
        while (i >= 0 && i < n) {
            LayoutInterval li = parent.getSubInterval(i);
            if (li.isEmptySpace() && li.getPreferredSize() != NOT_EXPLICITLY_DEFINED) {
                int pad = determinePadding(interval, dimension, alignment);
                int currentSize = LayoutInterval.getIntervalCurrentSize(li, dimension);

                int size = currentSize - sizeIncrement;
                if (size <= pad) {
                    if (size > 0 || !snapped || (i+d >= 0 && i+d < n)) {
                        size = NOT_EXPLICITLY_DEFINED;
                        sizeIncrement -= currentSize - pad;
                    }
                    else { // remove gap
                        layoutModel.removeInterval(parent, i);
                        break;
                    }
                }
                else sizeIncrement = 0;

                resizeInterval(li, size);
                if (LayoutInterval.wantResize(li, false) && LayoutInterval.wantResize(interval, false)) {
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
        return sizeIncrement;
    }

    /**
     * Assuming given interval is in sequence, it tries to place it inside a
     * nearby open parallel group.
     */
    private boolean moveInside(LayoutInterval interval, int dimension, int alignment) {
        LayoutInterval gap = null;
        LayoutInterval neighborGroup = null;

        LayoutInterval parent = interval.getParent();
        assert parent.isSequential() && (alignment == LEADING || alignment == TRAILING);

        int d = alignment == LEADING ? -1 : 1;
        int index = parent.indexOf(interval) + d;
        if (index >= 0 && index < parent.getSubIntervalCount()) {
            LayoutInterval li = parent.getSubInterval(index);
            if (li.isEmptySpace()) {
                gap = li;
                index += d;
                if (index >= 0 && index < parent.getSubIntervalCount()) {
                    li = parent.getSubInterval(index);
                    if (li.isParallel()) {
                        neighborGroup = li;
                    }
                }
            }
        }
        if (gap == null || neighborGroup == null) {
            return false;
        }

        LayoutInterval extend = findIntervalToExtend(
                neighborGroup, interval.getCurrentSpace(), dimension, alignment^1);
        if (extend == null) {
            return false;
        }

        int distance = LayoutInterval.isPlacedAtBorder(extend, dimension, alignment^1) ?
            0 : d * LayoutRegion.distance(interval.getCurrentSpace(),
                                          extend.getCurrentSpace(),
                                          dimension, alignment, alignment^1);
        assert distance >= 0; // shouldn't be here if the distance is negative

        if (extend.isSequential()) {
            int idx = alignment == LEADING ? extend.getSubIntervalCount()-1 : 0;
            LayoutInterval endGap = extend.getSubInterval(idx);
            if (endGap.isEmptySpace()) {
                distance = d * LayoutRegion.distance(
                                   interval.getCurrentSpace(),
                                   extend.getSubInterval(idx+d).getCurrentSpace(),
                                   dimension, alignment, alignment^1);
                eatGap(gap, endGap, distance);
                distance = 0; // the gap is now set
            }
        }
        else {
            LayoutInterval extParent = extend.getParent();
            LayoutInterval extSeq = new LayoutInterval(SEQUENTIAL);
            extSeq.setAlignment(extend.getAlignment());
            layoutModel.addInterval(extSeq, extParent, layoutModel.removeInterval(extend));
            layoutModel.setIntervalAlignment(extend, DEFAULT);
            layoutModel.addInterval(extend, extSeq, 0);
            extend = extSeq;
        }
        layoutModel.removeInterval(interval); // from 'parent'
        layoutModel.removeInterval(gap); // from 'parent'
        if (parent.getSubIntervalCount() == 1) { // only neighborGroup remained, eliminate the parent group
            layoutModel.removeInterval(neighborGroup);
            LayoutInterval superParent = parent.getParent();
            int idx = layoutModel.removeInterval(parent);
            addContent(neighborGroup, superParent, idx);
        }
        if (distance > 0) {
            resizeInterval(gap, distance);
        }
        layoutModel.addInterval(gap, extend, alignment == LEADING ? -1 : 0);
        layoutModel.addInterval(interval, extend, alignment == LEADING ? -1 : 0);

        return true;
    }

    /**
     * In given parallel group recursively looks for a sub-interval that could
     * be extended by adding an interval corresponding to 'space'.
     */
    private static LayoutInterval findIntervalToExtend(LayoutInterval group, LayoutRegion space, int dimension, int alignment) {
        if (LayoutInterval.isClosedGroup(group, alignment)) {
            return null; // can't expand the group - the alignment edge is not open
        }
        LayoutInterval overlapping = null;
        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li.isEmptySpace()) {
                continue;
            }
            if (LayoutRegion.overlap(li.getCurrentSpace(), space, dimension^1, 0)) {
                // overlaps orthogonally
                if (overlapping != null) {
                    // more overlapping intervals - a new group would have to be created
                    return null;
                }
                if (li.isParallel()) {
                    li = findIntervalToExtend(li, space, dimension, alignment);
                    if (li == null) {
                        return null;
                    }
                }
                overlapping = li;
            }
        }
        return overlapping;
    }

    /**
     * Simpler version of the general 'align' method. This method aligns an
     * interval to given interval, but does not try to set the same alignment
     * to both intervals. The method expects the interval is already placed
     * correctly where it should appear, just not aligned in parallel group.
     */
    private boolean alignWith(LayoutInterval interval, LayoutInterval toAlignWith, int dimension, int alignment) {
        if (toAlignWith.getParent() == null) {
            return false; // aligning with root - nothing to do
        }
        // find common parallel group for the intervals
        LayoutInterval commonGroup = LayoutInterval.getFirstParent(toAlignWith, PARALLEL);
        LayoutInterval parParent = LayoutInterval.getFirstParent(interval, PARALLEL);
        if (parParent != commonGroup) {
            if (parParent.isParentOf(commonGroup)) {
                toAlignWith = getAlignSubstitute(toAlignWith, parParent, alignment);
                if (toAlignWith == null) {
                    return false; // cannot align
                }
                commonGroup = parParent;
            }
// [commented out; for the aligning interval this is wrong - need to bring the interval to the commonGroup level]
//            else if (commonGroup.isParentOf(parParent)) {
//                interval = getAlignSubstitute(interval, commonGroup, alignment);
//                if (interval == null) {
//                    return false; // cannot align
//                }
//            }
            else return false; // cannot align with interval from different branch
        }

        // prepare separation to groups
        List aligned = new ArrayList(2);
        List remainder = new ArrayList(2);
        LayoutInterval offsetGap = eliminateOffsetGap(commonGroup);
        int originalCount = commonGroup.getSubIntervalCount();

        int effAlign1 = extract(toAlignWith, aligned, remainder, alignment);
        int effAlign2 = extract(interval, aligned, remainder, alignment);

        assert aligned.size() == 2
               && (!LayoutInterval.isPlacedAtBorder(toAlignWith, commonGroup, dimension, alignment)
                   || remainder.isEmpty());

        // add indent if needed
        int indent = LayoutRegion.distance(toAlignWith.getCurrentSpace(), interval.getCurrentSpace(),
                                           dimension, alignment, alignment);
        assert indent == 0 || alignment == LEADING; // currently support indent only at the LEADING side
        if (indent != 0) {
            LayoutInterval indentGap = new LayoutInterval(SINGLE);
            indentGap.setSize(Math.abs(indent));
            // [would be good to have a default gap for preferred indent - similar to padding]
            int index = indent > 0 ? 1 : 0;
            LayoutInterval li = (LayoutInterval) aligned.get(index);
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
                
                aligned.set(index, seq);
                li = seq;
            }
            layoutModel.addInterval(indentGap, li, alignment == LEADING ? 0 : -1);
        }

        // prepare the group where the aligned intervals will be placed
        LayoutInterval group;
        LayoutInterval commonSeq;
        if ((originalCount == 2 && commonGroup.getParent() != null)
            || LayoutInterval.isPlacedAtBorder(toAlignWith, commonGroup, dimension, alignment))
        {   // reuse the original group - avoid unnecessary nesting
            group = commonGroup;
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
                    layoutModel.setIntervalAlignment(group, DEFAULT);
                    layoutModel.addInterval(group, commonSeq, -1);
                }
            }
            else commonSeq = null;
        }
        else { // need to create a new group
            group = new LayoutInterval(PARALLEL);
            if (!remainder.isEmpty()) { // need a new sequence for the remainder group
                commonSeq = new LayoutInterval(SEQUENTIAL);
                commonSeq.add(group, 0);
                layoutModel.addInterval(commonSeq, commonGroup, -1);
            }
            else {
                commonSeq = null;
                if (effAlign1 == LEADING || effAlign1 == TRAILING) {
                    group.setAlignment(effAlign1);
                }
                layoutModel.addInterval(group, commonGroup, -1);
            }
        }

        // add the intervals and their neighbors to the main aligned group
        toAlignWith = (LayoutInterval) aligned.get(0);
        boolean resizing1 = LayoutInterval.wantResize(toAlignWith, false); // [should rather check all group content except interval]
        if (toAlignWith.getParent() != group) {
            if (toAlignWith.getParent() != null) {
                layoutModel.removeInterval(toAlignWith);
            }
            if (effAlign1 == LEADING || effAlign1 == TRAILING) {
                layoutModel.setIntervalAlignment(toAlignWith, effAlign1); // keeps its alignment
            }
            addContent(toAlignWith, group, -1);
        }
        interval = (LayoutInterval) aligned.get(1);
        boolean resizing2 = LayoutInterval.wantResize(interval, false);
        if (interval.getParent() != group) {
            if (interval.getParent() != null) {
                layoutModel.removeInterval(interval);
            }
            layoutModel.setIntervalAlignment(interval, alignment);
            addContent(interval, group, -1);
        }
        else {
            layoutModel.setIntervalAlignment(interval, alignment);
        }
        if (resizing2 && !resizing1 && indent == 0) {
            suppressGroupResizing(group);
            layoutModel.changeIntervalAttribute(interval, LayoutInterval.ATTRIBUTE_FILL, true);
        }

        // create the remainder group next to the aligned group
        if (!remainder.isEmpty()) {
            createRemainderGroup(remainder, commonSeq, commonSeq.indexOf(group), alignment, effAlign1);
        }

        if (offsetGap != null) {
            layoutModel.addInterval(offsetGap, commonGroup, -1);
        }

        return true;
    }

    private int extract(LayoutInterval interval, List toAlign, List toRemain, int alignment) {
        int effAlign = getEffectiveAlignment(interval);
        LayoutInterval parent = interval.getParent();
        if (parent.isSequential()) {
            int extractCount = extract(interval, alignment, false,
                                       alignment == LEADING ? toRemain : null,
                                       alignment == LEADING ? null : toRemain);
            if (extractCount == 1) { // the parent won't be reused
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

    /**
     * Changes global alignment of the layout component.
     *
     * @param comp component whose alignment should be changed.
     * @param dimension dimension the alignment should be applied in.
     * @param alignment desired alignment.
     */
    public void adjustComponentAlignment(LayoutComponent comp, int dimension, int alignment) {
        modelListener.deactivate();
        LayoutInterval interval = comp.getLayoutInterval(dimension);
        assert !LayoutInterval.wantResize(interval, false);
        
        // Skip non-resizable groups
        LayoutInterval parent = interval.getParent();
        while (parent != null) {
            if (!LayoutInterval.canResize(parent)) {
                interval = parent;
            }
            parent = parent.getParent();
        }
        
        boolean changed = false;
        parent = interval.getParent();
        while (parent != null) {
            if (parent.isParallel()) {
                if (LayoutInterval.wantResize(parent, false) && !LayoutInterval.wantResize(interval, false)) {
                    int alg = interval.getAlignment();
                    if (alg != alignment) {
                        LayoutInterval offsetGap = eliminateOffsetGap(parent);
                        
                        // Add fixed gap and change alignment
                        int size = LayoutInterval.getIntervalCurrentSize(parent, dimension)
                            - LayoutInterval.getIntervalCurrentSize(interval, dimension);
                        if (size > 0) {
                            if (!interval.isSequential()) {
                                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                                layoutModel.setIntervalAlignment(interval, DEFAULT);
                                int i = layoutModel.removeInterval(interval);
                                layoutModel.addInterval(interval, seq, -1);
                                layoutModel.addInterval(seq, parent, i);
                                interval = seq;
                            }
                            int index = (alg == LEADING) ? -1 : 0;
                            LayoutInterval gap = new LayoutInterval(SINGLE);
                            gap.setSize(size);
                            layoutModel.addInterval(gap, interval, index);
                        }
                        layoutModel.setIntervalAlignment(interval, alignment);
                        
                        if (offsetGap != null) {
                            layoutModel.addInterval(offsetGap, parent, -1);
                        }
                    }
                    changed = true;
                }
            } else {
                boolean before = true;
                boolean seqChanged = false;
                for (int i=0; i<parent.getSubIntervalCount(); i++) {
                    LayoutInterval li = parent.getSubInterval(i);
                    if (li == interval) {
                        before = false;
                    } else if (LayoutInterval.wantResize(li, false)) {
                        if ((before && (alignment == LEADING)) || (!before && (alignment == TRAILING))) {
                            assert li.isEmptySpace();
                            setIntervalResizing(li, false);
                            if (li.getPreferredSize() == 0) {
                                layoutModel.removeInterval(li);
                                i--;
                            }
                            seqChanged = true;
                        }
                    }
                }
                if (!changed && seqChanged) {
                    boolean insertGap = false;
                    int index = parent.indexOf(interval);
                    if (alignment == LEADING) {
                        if (parent.getSubIntervalCount() <= index+1) {
                            insertGap = true;
                            index = -1;
                        } else {
                            index++;
                            LayoutInterval candidate = parent.getSubInterval(index);
                            if (candidate.isEmptySpace()) {
                                setIntervalResizing(candidate, true);
                            } else {
                                insertGap = true;
                            }
                        }
                    } else {
                        assert (alignment == TRAILING);
                        if (index == 0) {
                            insertGap = true;
                        } else {                            
                            LayoutInterval candidate = parent.getSubInterval(index-1);
                            if (candidate.isEmptySpace()) {
                                setIntervalResizing(candidate, true);
                            } else {
                                insertGap = true;
                            }
                        }
                    }
                    if (insertGap) {
                        LayoutInterval gap = new LayoutInterval(SINGLE);
                        gap.setSize(0);
                        setIntervalResizing(gap, true);
                        layoutModel.addInterval(gap, parent, index);
                    }
                    changed = true;
                }
            }
            interval = parent;
            parent = parent.getParent();
        }
        modelListener.activate();
        //dirty = true;
    }

    /**
     * Returns alignment of the component as the first item of the array.
     * The second item of the array indicates whether the alignment can
     * be changed to leading or trailing (e.g. if the current value is not
     * enforced by other resizable components). The returned alignment is
     * global e.g. it shows which edge of the container the component will track.
     *
     * @param comp component whose alignment should be determined.
     * @param dimension dimension in which the alignment should be determined.
     * @return alignment (or -1 if the component doesn't have a global alignment)
     * as the first item of the array and (canBeChangedToLeading ? 1 : 0) +
     * (canBeChangedToTrailing ? 2 : 0) as the second item.
     */
    public int[] getAdjustableComponentAlignment(LayoutComponent comp, int dimension) {
        LayoutInterval interval = comp.getLayoutInterval(dimension);
        boolean leadingFixed = true;
        boolean trailingFixed = true;
        boolean leadingAdjustable = true;
        boolean trailingAdjustable = true;
        
        if (LayoutInterval.wantResize(interval, false)) {
            leadingFixed = trailingFixed = leadingAdjustable = trailingAdjustable = false;
        }
        LayoutInterval parent = interval.getParent();
        while (parent != null) {
            if (!LayoutInterval.canResize(parent)) {
                leadingFixed = trailingFixed = leadingAdjustable = trailingAdjustable = true;
            } else if (parent.isParallel()) {
                if (LayoutInterval.wantResize(parent, false) && !LayoutInterval.wantResize(interval, false)) {
                    int alignment = interval.getAlignment();
                    if (alignment == LEADING) {
                        trailingFixed = false;
                    } else if (alignment == TRAILING) {
                        leadingFixed = false;
                    }
                }
            } else {
                boolean before = true;
                Iterator iter = parent.getSubIntervals();
                while (iter.hasNext()) {
                    LayoutInterval li = (LayoutInterval)iter.next();
                    if (li == interval) {
                        before = false;
                    } else if (LayoutInterval.wantResize(li, false)) {
                        boolean space = li.isEmptySpace();
                        if (before) {
                            leadingFixed = false;
                            if (!space) {
                                leadingAdjustable = false;
                            }
                        } else {
                            trailingFixed = false;
                            if (!space) {
                                trailingAdjustable = false;
                            }
                        }
                    }
                }
            }
            interval = parent;
            parent = parent.getParent();
        }
        int adjustable = (leadingAdjustable ? 1 << LEADING : 0) + (trailingAdjustable ? 1 << TRAILING : 0);
        if (leadingFixed && trailingFixed) {
            // As if top level group wantResize()
            if (LEADING == interval.getGroupAlignment()) {
                trailingFixed = false;
            } else {
                leadingFixed = false;
            }
        }
        int alignment;
        if (leadingFixed) {
            // !trailingFixed
            alignment = LEADING;
        } else {
            if (trailingFixed) {
                alignment = TRAILING;
            } else {
                alignment = -1;
            }
        }
        return new int[] {alignment, adjustable};
    }

    /**
     * Determines whether the component is resizing in the given direction.
     *
     * @param comp component whose resizability should be determined.
     * @param dimension dimension in which the resizability should be determined.
     * @return <code>true</code> if the component is resizing, returns
     * <code>false</code> otherwise.
     */
    public boolean isComponentResizing(LayoutComponent comp, int dimension) {
        LayoutInterval interval = comp.getLayoutInterval(dimension);
        boolean fill = interval.hasAttribute(LayoutInterval.ATTRIBUTE_FILL);
        return fill ? false : LayoutInterval.wantResize(interval, true);
    }
    
    /**
     * Returns size of the padding represented by the given layout interval.
     *
     * @param interval layout interval that represents padding.
     * @return size of the padding.
     */
    private int sizeOfPadding(LayoutInterval interval) {
        // PENDING should use the same algorithm as layout manager
        boolean first = true;
        boolean last = true;
        LayoutInterval parent = interval.getParent();
        while ((first || last) && (parent != null)) {
            if (parent.isSequential()) {
                int index = parent.indexOf(interval);
                first = first && (index == 0);
                last = last && (index == parent.getSubIntervalCount() - 1);
            }
            interval = parent;
            parent = parent.getParent();
        }
        return (first || last) ? 12 : 6;
    }
    
    /**
     * Returns preferred size of the given interval (in pixels).
     *
     * @param interval interval whose preferred size should be determined.
     * @return preferred size of the given interval.
     */
    private int prefSizeOfInterval(LayoutInterval interval) {
        int prefSize = interval.getPreferredSize();
        if (prefSize == NOT_EXPLICITLY_DEFINED) {
            if (interval.isComponent()) {
                LayoutComponent comp = interval.getComponent();
                Dimension pref = visualMapper.getComponentPreferredSize(comp.getId());
                return (interval == comp.getLayoutInterval(HORIZONTAL)) ? pref.width : pref.height;
            } else if (interval.isEmptySpace()) {
                return sizeOfPadding(interval);
            } else {
                assert interval.isGroup();
                prefSize = 0;
                Iterator iter = interval.getSubIntervals();
                if (interval.isSequential()) {
                    while (iter.hasNext()) {
                        LayoutInterval subInterval = (LayoutInterval)iter.next();
                        prefSize += prefSizeOfInterval(subInterval);
                    }
                } else {
                    while (iter.hasNext()) {
                        LayoutInterval subInterval = (LayoutInterval)iter.next();
                        prefSize = Math.max(prefSize, prefSizeOfInterval(subInterval));
                    }                    
                }
            }
        }
        return prefSize;    
    }
    
    /**
     * Sets component resizability. Makes the component resizing or fixed.
     *
     * @param comp component whose resizability should be set.
     * @param dimension dimension in which the resizability should be changed.
     * @param resizable determines whether the component should be made
     * resizable in the given dimension.
     */
    public void setComponentResizing(LayoutComponent comp, int dimension, boolean resizing) {
        modelListener.deactivate();
        LayoutInterval interval = comp.getLayoutInterval(dimension);
        LayoutInterval parent = interval.getParent();
        boolean fill = interval.hasAttribute(LayoutInterval.ATTRIBUTE_FILL);
        boolean formerFill = interval.hasAttribute(LayoutInterval.ATTRIBUTE_FORMER_FILL);
        boolean parentContentResizable = resizing && LayoutInterval.contentWantResize(parent);
        if (fill || formerFill) {
            switchFillAttribute(interval, resizing);
        } else {
            setIntervalResizing(interval, resizing);
        }
        int delta = 0;
        if (!resizing) {
            int currSize = LayoutInterval.getIntervalCurrentSize(interval, dimension);
            int prefSize = prefSizeOfInterval(interval);
            delta = currSize - prefSize;
            if (delta != 0) {
                layoutModel.setIntervalSize(interval, interval.getMinimumSize(), currSize, interval.getMaximumSize());
            }
        }
        if (parent.isParallel()) {
            if (resizing) {
                int groupCurrSize = LayoutInterval.getIntervalCurrentSize(parent, dimension);
                int currSize = LayoutInterval.getIntervalCurrentSize(interval, dimension);
                // PENDING currSize could change if groupPrefSize != groupCurrSize
                if (groupCurrSize != currSize) {
                    LayoutInterval seqGroup = new LayoutInterval(SEQUENTIAL);
                    int alignment = interval.getAlignment();
                    layoutModel.setIntervalAlignment(interval, DEFAULT);
                    seqGroup.setAlignment(alignment);
                    int i = layoutModel.removeInterval(interval);
                    layoutModel.addInterval(interval, seqGroup, -1);
                    LayoutInterval space = new LayoutInterval(SINGLE);
                    space.setSize(groupCurrSize - currSize);
                    layoutModel.addInterval(space, seqGroup, (alignment == LEADING) ? -1 : 0);
                    layoutModel.addInterval(seqGroup, parent, i);
                }
            }
        } else {
            if (resizing && !parentContentResizable) {
                // PENDING currSize could change if groupPrefSize != groupCurrSize
                int seqCurrSize = LayoutInterval.getIntervalCurrentSize(parent, dimension);
                int parCurrSize = LayoutInterval.getIntervalCurrentSize(parent.getParent(), dimension);
                if (parCurrSize != seqCurrSize) {
                    LayoutInterval space = new LayoutInterval(SINGLE);
                    space.setSize(parCurrSize - seqCurrSize);
                    layoutModel.addInterval(space, parent, (parent.getAlignment() == LEADING) ? -1 : 0);
                }
            } else {
                // Change resizability of gaps
                List resizableList = new LinkedList();
                int alignment = getEffectiveAlignment(interval);
                LayoutInterval leadingGap = null;
                LayoutInterval trailingGap = null;
                boolean afterDefining = false;
                Iterator iter = parent.getSubIntervals();
                while (iter.hasNext()) {
                    LayoutInterval candidate = (LayoutInterval)iter.next();
                    if (candidate == interval) {
                        afterDefining = true;
                    }
                    if (candidate.isEmptySpace()) {
                        if (resizing) {
                            setIntervalResizing(candidate, false);
                            int currSize = LayoutInterval.getIntervalCurrentSize(candidate, dimension);
                            int prefSize = prefSizeOfInterval(candidate);
                            if (currSize != prefSize) {
                                layoutModel.setIntervalSize(candidate, candidate.getMinimumSize(),
                                    currSize, candidate.getMaximumSize());
                                delta += currSize - prefSize;
                            }
                        } else {
                            boolean wasFill = candidate.hasAttribute(LayoutInterval.ATTRIBUTE_FORMER_FILL);
                            boolean glue = (candidate.getPreferredSize() != NOT_EXPLICITLY_DEFINED);
                            if (wasFill) {
                                trailingGap = candidate;
                            } else if ((trailingGap == null) || (!trailingGap.hasAttribute(LayoutInterval.ATTRIBUTE_FORMER_FILL))) {
                                if (glue) {
                                    trailingGap = candidate;
                                } else {
                                    if (afterDefining && ((trailingGap == null) || (trailingGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED))) {
                                        trailingGap = candidate;
                                    }
                                }
                            }
                            if ((leadingGap == null) && !afterDefining) {
                                leadingGap = candidate;
                            } else {
                                if ((wasFill && ((leadingGap == null) || (!leadingGap.hasAttribute(LayoutInterval.ATTRIBUTE_FORMER_FILL))))
                                    || glue && ((leadingGap == null) || (!leadingGap.hasAttribute(LayoutInterval.ATTRIBUTE_FORMER_FILL)
                                    && (leadingGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED)))) {
                                    leadingGap = candidate;
                                }
                            }
                        }
                    } else {
                        if (candidate.getMaximumSize() == Short.MAX_VALUE) {
                            resizableList.add(candidate);
                        }
                    }
                }
                if (resizableList.size() > 0) {
                    iter = resizableList.iterator();
                    delta = (LayoutInterval.getIntervalCurrentSize(parent, dimension) - prefSizeOfInterval(parent) + delta)/resizableList.size();
                    while (iter.hasNext()) {
                        LayoutInterval candidate = (LayoutInterval)iter.next();
                        if (candidate.isGroup()) {
                            // PENDING currSize could change - we can't modify prefSize of group directly
                        } else {
                            if (candidate == interval) {
                                if (delta != 0) {
                                    int prefSize = prefSizeOfInterval(candidate);
                                    layoutModel.setIntervalSize(candidate, candidate.getMinimumSize(),
                                        Math.max(0, prefSize - delta), candidate.getMaximumSize());
                                }
                            } else {
                                int currSize = LayoutInterval.getIntervalCurrentSize(candidate, dimension);
                                layoutModel.setIntervalSize(candidate, candidate.getMinimumSize(),
                                    Math.max(0, currSize - delta), candidate.getMaximumSize());                            
                            }
                        }
                    }
                }
                if (!LayoutInterval.wantResize(parent, false)) {
                    LayoutInterval gap = null;
                    if ((alignment == TRAILING) && (leadingGap != null)) {
                        gap = leadingGap;
                        setIntervalResizing(leadingGap, !resizing);
                        layoutModel.changeIntervalAttribute(leadingGap, LayoutInterval.ATTRIBUTE_FILL, true);
                    }
                    if ((alignment == LEADING) && (trailingGap != null)) {
                        gap = trailingGap;
                        setIntervalResizing(trailingGap, !resizing);
                        layoutModel.changeIntervalAttribute(leadingGap, LayoutInterval.ATTRIBUTE_FILL, true);
                    }
                    if ((gap != null) && (delta != 0) && (gap.getPreferredSize() != NOT_EXPLICITLY_DEFINED)) {
                        layoutModel.setIntervalSize(gap, gap.getMinimumSize(), 
                            Math.max(0, gap.getPreferredSize() - delta), gap.getMaximumSize());
                    }
                }
            }
            parent = parent.getParent(); // use parallel parent for group resizing check
        }
        modelListener.activate();

        if (resizing) {
            // cancel possible suppressed resizing
            while (parent != null) {
                if (!LayoutInterval.canResize(parent)) {
                    enableGroupResizing(parent);
                }
                parent = parent.getParent();
            }
        } else {
            // check if we should suppress resizing
            while (parent != null) {
                if (fillResizable(parent)) {
                    suppressGroupResizing(parent);
                    parent = parent.getParent();
                } else {
                    break;
                }
            }
        }        
    }
    
    private boolean fillResizable(LayoutInterval interval) {
        if (!LayoutInterval.canResize(interval)) {
            return false;
        }
        if (interval.isGroup()) {
            boolean subres = true;
            Iterator it = interval.getSubIntervals();
            while (it.hasNext()) {
                LayoutInterval li = (LayoutInterval)it.next();
                if (LayoutInterval.wantResize(li, false) && !fillResizable(li)) {
                    subres = false;
                    break;
                }
            }
            return subres;
        } else {
            return interval.hasAttribute(LayoutInterval.ATTRIBUTE_FILL);
        }
    }

    /**
     * Aligns given components in the specified direction.
     *
     * @param componentIds IDs of components that should be aligned.
     * @param closed determines if closed group should be created.
     * @param dimension dimension to align in.
     * @param alignment requested alignment.
     */
    public void align(Collection componentIds, boolean closed, int dimension, int alignment) {
        LayoutInterval[] intervals = new LayoutInterval[componentIds.size()];
        int counter = 0;
        Iterator iter = componentIds.iterator();        
        while (iter.hasNext()) {
            String id = (String)iter.next();
            LayoutComponent component = layoutModel.getLayoutComponent(id);
            intervals[counter++] = component.getLayoutInterval(dimension);            
        }
        modelListener.deactivate();
        alignIntervals(intervals, closed, dimension, alignment);
        modelListener.activate();
        dirty = true;
    }

    /**
     * Aligns given components in the specified direction.
     *
     * @param componentIds IDs of components that should be aligned.
     * @param closed determines if closed group should be created.
     * @param dimension dimension to align in.
     * @param alignment requested alignment.
     */
    private void alignIntervals(LayoutInterval[] intervals, boolean closed, int dimension, int alignment) {
        // Find nearest common (parallel) parent
        LayoutInterval parParent = findCommonParent(intervals);
        LayoutInterval seqParent = null;
        if (parParent.isSequential()) {
            seqParent = parParent;
            while (parParent.isSequential()) {
                parParent = parParent.getParent();
            }
        }
        
        // Calculate the range of the sequential parent that will be changed
        int maxSeqIndex = -1;
        int minSeqIndex = Short.MAX_VALUE;
        if (seqParent != null) {
            // Calculate maxSeq and minSeq indices            
            for (int i=0; i<intervals.length; i++) {
                LayoutInterval interval = intervals[i];
                while (interval.getParent() != seqParent) {
                    interval = interval.getParent();
                }
                int index = seqParent.indexOf(interval);
                maxSeqIndex = Math.max(index, maxSeqIndex);
                minSeqIndex = Math.min(index, minSeqIndex);
            }
            
            // Include also empty space before the first layout interval
            int preGap = 0;
            if ((minSeqIndex > 0) && (seqParent.getSubInterval(minSeqIndex-1).isEmptySpace())) {
                minSeqIndex--;
                preGap = LayoutInterval.getIntervalCurrentSize(seqParent.getSubInterval(minSeqIndex), dimension);
            }
            
            // Include also empty space after the last layout interval
            int postGap = 0;
            if ((maxSeqIndex + 1 < seqParent.getSubIntervalCount()) &&
                (seqParent.getSubInterval(maxSeqIndex+1).isEmptySpace())) {
                maxSeqIndex++;
                postGap = LayoutInterval.getIntervalCurrentSize(seqParent.getSubInterval(maxSeqIndex), dimension);
            }
            
            if ((minSeqIndex > 0) || (maxSeqIndex + 1 < seqParent.getSubIntervalCount())) {
                // We have to create a new parallel parent for the subsequence
                LayoutInterval newSequence = new LayoutInterval(SEQUENTIAL);
                LayoutRegion region = newSequence.getCurrentSpace();
                for (int i=maxSeqIndex; i>=minSeqIndex; i--) {
                    LayoutInterval interval = seqParent.getSubInterval(i);
                    layoutModel.removeInterval(interval);
                    layoutModel.addInterval(interval, newSequence, 0);
                    if (!interval.isEmptySpace()) {
                        region.expand(interval.getCurrentSpace());
                    }
                }
                
                // Add pre/post gaps to current size
                region.positions[dimension][LEADING] -= preGap;
                region.positions[dimension][TRAILING] += postGap;

                parParent = new LayoutInterval(PARALLEL);
                layoutModel.addInterval(newSequence, parParent, -1);
                layoutModel.addInterval(parParent, seqParent, minSeqIndex);
                region = parParent.getCurrentSpace();
                region.expand(newSequence.getCurrentSpace());
            }
        }
        
        // Transfer the intervals into common parallel parent
        List gapsToResize = transferToParallelParent(intervals, parParent, alignment);
        
        if (alignment != CENTER) {
            // Calculate leading and trailing intervals
            Map leadingMap = new HashMap();
            Map trailingMap = new HashMap();
            for (int i=0; i<intervals.length; i++) {
                LayoutInterval interval = intervals[i];
                LayoutInterval parent = interval.getParent();
                // Special handling for standalone component intervals in parParent
                if (parent == parParent) parent = interval;
                LayoutInterval leading = (LayoutInterval)leadingMap.get(parent);
                LayoutInterval trailing = (LayoutInterval)trailingMap.get(parent);
                if ((leading == null) || (parent.indexOf(leading) > parent.indexOf(interval))) {
                    leadingMap.put(parent, interval);
                }
                if ((trailing == null) || (parent.indexOf(trailing) < parent.indexOf(interval))) {
                    trailingMap.put(parent, interval);
                }
            }        
            // Create arrays of leading/trailing intervals
            LayoutInterval[] leadingIntervals = new LayoutInterval[leadingMap.size()];
            LayoutInterval[] trailingIntervals = new LayoutInterval[trailingMap.size()];
            Iterator iter = leadingMap.values().iterator();
            int counter = 0;
            while (iter.hasNext()) {
                LayoutInterval interval = (LayoutInterval)iter.next();
                leadingIntervals[counter++] = interval;
            }
            iter = trailingMap.values().iterator();
            counter = 0;
            while (iter.hasNext()) {
                LayoutInterval interval = (LayoutInterval)iter.next();
                trailingIntervals[counter++] = interval;
            }

            // Perform alignment of the intervals transfered into common parallel parent
            if (closed) {
                if (alignment == LEADING) {
                    align(trailingIntervals, false, dimension, TRAILING);
                    align(leadingIntervals, false, dimension, LEADING);
                } else {
                    align(leadingIntervals, false, dimension, LEADING);
                    align(trailingIntervals, false, dimension, TRAILING);
                }
            } else {
                LayoutInterval[] itervalsToAlign = (alignment == LEADING) ? leadingIntervals : trailingIntervals;
                align(itervalsToAlign, closed, dimension, alignment);
            }
            
            // Must be done after align() to keep original eff. alignment inside align() method
            iter = gapsToResize.iterator();
            while (iter.hasNext()) {
                LayoutInterval gap = (LayoutInterval)iter.next();
                setIntervalResizing(gap, true);
            }
        }
        
        // Do some clean up
        destroyGroupIfRedundant(parParent, null);
    }

    /**
     * Finds common parent of the given intervals.
     *
     * @param intervals intervals whose parent should be found.
     * @return common parent of the given intervals.
     */
    private LayoutInterval findCommonParent(LayoutInterval[] intervals) {
        assert (intervals != null) && (intervals.length > 0);
        LayoutInterval parent = intervals[0].getParent();
        for (int i=1; i<intervals.length; i++) {
            parent = findCommonParent(parent, intervals[i]);
        }
        return parent;
    }

    /**
     * Finds common parent of two given intervals.
     *
     * @param interval1 interval whose parent should be found.
     * @param interval2 interval whose parent should be found.
     * @return common parent of two given intervals.
     */
    private LayoutInterval findCommonParent(LayoutInterval interval1, LayoutInterval interval2) {
        // Find all parents of given intervals
        Iterator parents1 = parentsOfInterval(interval1).iterator();
        Iterator parents2 = parentsOfInterval(interval2).iterator();
        LayoutInterval parent1 = (LayoutInterval)parents1.next();
        LayoutInterval parent2 = (LayoutInterval)parents2.next();
        assert (parent1 == parent2);
        
        // Candidate for the common parent
        LayoutInterval parent = null;
        while (parent1 == parent2) {
            parent = parent1;
            if (parents1.hasNext()) {
                parent1 = (LayoutInterval)parents1.next();
            } else {
                break;
            }
            if (parents2.hasNext()) {
                parent2 = (LayoutInterval)parents2.next();
            } else {
                break;
            }
        }
        return parent;
    }

    /**
     * Calculates all parents of the given interval.
     *
     * @param interval interval whose parents should be found.
     * @return <code>List</code> of <code>LayoutInterval</code> objects that
     * are parents of the given interval. The immediate parent of the interval
     * is at the end of the list.
     */
    private List parentsOfInterval(LayoutInterval interval) {
        List parents = new LinkedList();
        while (interval != null) {
            parents.add(0, interval);
            interval = interval.getParent();
        }
        return parents;
    }
    
    /**
     * Destroys the given group if it is redundant in the layout model.
     *
     * @param group group whose necessity should be checked.
     * @param boundary parent of the group that limits the changes that
     * should be made e.g. no changes outside of this group even if it were
     * itself redundant. Can be <code>null</code> if there's no boundary.
     */
    private void destroyGroupIfRedundant(LayoutInterval group, LayoutInterval boundary) {
        if ((group == null) || (!group.isGroup()) || (group == boundary)) return;
        LayoutInterval parent = group.getParent();
        // Don't destroy root intervals
        if (parent == null) return;

        // Remove empty groups
        if (group.getSubIntervalCount() == 0) {
            takeOutInterval(group, boundary);
            return;
        }

        // Destroy parallel groups with one sub-interval
        if (group.isParallel() && (group.getSubIntervalCount() == 1)) {
            LayoutInterval interval = group.getSubInterval(0);
            int index = parent.indexOf(group);
            layoutModel.removeInterval(interval);
            layoutModel.removeInterval(group);
            layoutModel.setIntervalAlignment(interval, parent.getAlignment());
            layoutModel.addInterval(interval, parent, index);
            if (interval.isGroup()) {
                destroyGroupIfRedundant(interval, boundary);
            }
        }
        
        // Sequential group can be dissolved in sequential parent
        boolean dissolve = parent.isSequential() && group.isSequential();
        
        // Parallel groups can be sometimes dissolved in parallel parent
        if (parent.isParallel() && group.isParallel()
            && (parent.getGroupAlignment() == group.getGroupAlignment())) {
            dissolve = true;
            Iterator iter = group.getSubIntervals();
            while (iter.hasNext()) {
                LayoutInterval subInterval = (LayoutInterval)iter.next();
                if (subInterval.getAlignment() != group.getGroupAlignment()) {
                    dissolve = false;
                    break;
                }
            }
        }
        if (dissolve) {
            int index = layoutModel.removeInterval(group);
            for (int i=group.getSubIntervalCount()-1; i>=0; i--) {
                LayoutInterval subInterval = group.getSubInterval(i);
                layoutModel.removeInterval(subInterval);
                layoutModel.addInterval(subInterval, parent, index);
            }
            destroyGroupIfRedundant(parent, boundary);
        }
    }

    /**
     * Removes the given interval from the layout model. Consolidates
     * parent groups if necessary.
     *
     * @param interval interval that should be removed.
     * @param boundary parent of the group that limits the changes that
     * should be made e.g. no changes outside of this group even if it were
     * itself redundant. Can be <code>null</code> if there's no boundary.
     */
    private void takeOutInterval(LayoutInterval interval, LayoutInterval boundary) {
        LayoutInterval parent = interval.getParent();
        int index = parent.indexOf(interval);
        List toRemove = new LinkedList();
        toRemove.add(interval);
        if (parent.isSequential()) {
            // Remove leading gap
            if (index > 0) {
                LayoutInterval li = parent.getSubInterval(index-1);
                if (li.isEmptySpace()) {
                    toRemove.add(li);
                }
            }
            // Remove trailing gap
            if (index+1 < parent.getSubIntervalCount()) {
                LayoutInterval li = parent.getSubInterval(index+1);
                if (li.isEmptySpace()) {
                    toRemove.add(li);
                }
            }
            // Add dummy gap if necessary
            if ((toRemove.size() == 3) && (parent.getSubIntervalCount() > 3)) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                if (interval.isComponent() && (interval.getComponent().getLayoutInterval(VERTICAL) == interval)) {
                    int alignment = getEffectiveAlignment(interval);
                    int size = 0;
                    for (int i=0; i<3; i++) {
                        size += LayoutInterval.getIntervalCurrentSize((LayoutInterval)toRemove.get(i), VERTICAL);
                    }
                    gap.setSizes(NOT_EXPLICITLY_DEFINED, size, (alignment == TRAILING) ? Short.MAX_VALUE : USE_PREFERRED_SIZE);
                }
                layoutModel.addInterval(gap, parent, index);
            }
        }
        Iterator iter = toRemove.iterator();
        while (iter.hasNext()) {
            LayoutInterval remove = (LayoutInterval)iter.next();
            layoutModel.removeInterval(remove);
        }
        // Consolidate parent
        destroyGroupIfRedundant(parent, boundary);
    }
    
    /**
     * Ensures that the nearest parallel parent of the given intervals is the passed one.
     *
     * @param intervals intervals that should be transfered into the given parallel parent.
     * @param parParent parallel group that is already parent (but maybe not the nearest
     * parallel parent) of the given intervals.
     * @param requested alignment.
     * @return <code>List</code> of intervals (gaps) that should become resizable.
     */
    private List transferToParallelParent(LayoutInterval[] intervals, LayoutInterval parParent, int alignment) {
        // Determine dimension used to align components
        LayoutComponent temp = intervals[0].getComponent();
        int dimension = (temp.getLayoutInterval(HORIZONTAL) == intervals[0]) ? HORIZONTAL : VERTICAL;

        // Calculate extreme coordinates
        int leadingPosition = Short.MAX_VALUE;
        int trailingPosition = 0;
        int targetEffAlignment = LayoutConstants.DEFAULT;
        for (int i=0; i<intervals.length; i++) {
            LayoutInterval interval = intervals[i];
            
            // This method should be called only for components
            assert interval.isComponent();
            
            LayoutRegion region = interval.getCurrentSpace();
            int leading = region.positions[dimension][LEADING];
            int trailing = region.positions[dimension][TRAILING];
            leadingPosition = Math.min(leading, leadingPosition);
            trailingPosition = Math.max(trailing, trailingPosition);
            
            int effAlignment = getEffectiveAlignment(interval);
            if (((effAlignment == LEADING) || (effAlignment == TRAILING))
                && ((targetEffAlignment == DEFAULT) || (effAlignment == alignment))) {
                targetEffAlignment = effAlignment;
            }
        }
        
        boolean resizable = false;
        boolean sequenceResizable;
        boolean leadingGaps = true;
        boolean trailingGaps = true;
        List gapsToResize = new LinkedList();
        List sequenceGapsToResize;
        
        // List of new sequence groups for individual intervals
        List intervalList = Arrays.asList(intervals);
        List newSequences = new LinkedList();
        Map gapSizes = new HashMap();
        for (int i=0; i<intervals.length; i++) {
            LayoutInterval interval = intervals[i];
                
            // Find intervals that should be in the same sequence with the transfered interval
            List transferedComponents = transferedComponents(interval, parParent);
            if (alignment == CENTER) {
                for (int j = transferedComponents.size()-1; j>=0; j--) {
                    LayoutInterval trInterval = (LayoutInterval)transferedComponents.get(j);
                    if (!intervalList.contains(trInterval)) {
                        transferedComponents.remove(trInterval);
                    }
                }
            }
            
            // List of LayoutIntervals in the new sequence group
            List newSequenceList = new LinkedList();
            newSequences.add(newSequenceList);
            sequenceResizable = false;
            sequenceGapsToResize = new LinkedList();
            Iterator iter = transferedComponents.iterator();
            
            // Determine leading gap of the sequence
            LayoutRegion parentRegion = parParent.getCurrentSpace();
            LayoutInterval leadingInterval = (LayoutInterval)iter.next();
            LayoutRegion leadingRegion = leadingInterval.getCurrentSpace();
            if (alignment != CENTER) {
                int preGap = leadingRegion.positions[dimension][LEADING]
                    - parentRegion.positions[dimension][LEADING];
                LayoutInterval gapInterval = LayoutInterval.getNeighbor(leadingInterval, SEQUENTIAL, LEADING);
                leadingGaps = leadingGaps && (preGap != 0);
                if ((gapInterval != null) && gapInterval.isEmptySpace() && parParent.isParentOf(gapInterval)
                    && (LayoutInterval.getIntervalCurrentSize(gapInterval, dimension) == preGap)) {
                    LayoutInterval gap = cloneGap(gapInterval);
                    newSequenceList.add(gap);
                    gapSizes.put(gap, new Integer(preGap));
                    if (alignment == TRAILING) {
                        sequenceResizable = sequenceResizable || LayoutInterval.canResize(gap);
                    }
                } else {
                    maybeAddGap(newSequenceList, preGap, true);
                    if ((preGap != 0) && (alignment == TRAILING) && (leadingInterval == interval)) {
                        LayoutInterval gap = (LayoutInterval)newSequenceList.get(newSequenceList.size() - 1);
                        if (getEffectiveAlignment(leadingInterval) == TRAILING) {
                            layoutModel.setIntervalSize(gap, USE_PREFERRED_SIZE, preGap, USE_PREFERRED_SIZE);
                            sequenceGapsToResize.add(gap);
                        }
                    }
                }
            }
            
            // Determine content of the sequence
            boolean afterDefiningInterval = false;
            newSequenceList.add(leadingInterval);
            while (iter.hasNext()) {
                if (leadingInterval == interval) {
                    afterDefiningInterval = true;
                }
                LayoutInterval trailingInterval = (LayoutInterval)iter.next();
                if (((alignment == TRAILING) && (!afterDefiningInterval || (leadingInterval == interval)))
                    || ((alignment == LEADING) && afterDefiningInterval)) {
                    sequenceResizable = sequenceResizable || LayoutInterval.canResize(leadingInterval);
                }
                
                // Determine gap between before the processed interval
                LayoutRegion trailingRegion = trailingInterval.getCurrentSpace();
                LayoutInterval gapInterval = LayoutInterval.getNeighbor(leadingInterval, SEQUENTIAL, TRAILING);
                int gapSize = trailingRegion.positions[dimension][LEADING]
                    - leadingRegion.positions[dimension][TRAILING];
                boolean gapFound = false;
                if (gapInterval.isEmptySpace()) {
                    LayoutInterval neighbor = LayoutInterval.getNeighbor(gapInterval, SEQUENTIAL, TRAILING);
                    if (neighbor == trailingInterval) {
                        gapFound = true;
                        LayoutInterval gap = cloneGap(gapInterval);
                        newSequenceList.add(gap);
                        gapSizes.put(gap, new Integer(gapSize));
                        if (((alignment == TRAILING) && !afterDefiningInterval)
                            || ((alignment == LEADING) && afterDefiningInterval)) {
                            sequenceResizable = sequenceResizable || LayoutInterval.canResize(gap);
                        }
                    }
                }
                if (!gapFound) {
                    maybeAddGap(newSequenceList, gapSize, (alignment == CENTER));
                }
                if (((leadingInterval == interval) && (alignment == LEADING)
                      && (getEffectiveAlignment(trailingInterval) == TRAILING))
                    || ((trailingInterval == interval) && (alignment == TRAILING))
                      && (getEffectiveAlignment(leadingInterval) == LEADING)) {
                    LayoutInterval gap = (LayoutInterval)newSequenceList.get(newSequenceList.size() - 1);
                    if (!LayoutInterval.canResize(gap)) {
                        sequenceGapsToResize.add(gap);
                    }
                }
                
                newSequenceList.add(trailingInterval);
                leadingInterval = trailingInterval;
                leadingRegion = trailingRegion;
            }
            
            // Determine trailing gap of the sequence
            if ((alignment == LEADING) || ((alignment == TRAILING) && (leadingInterval == interval))) {
                sequenceResizable = sequenceResizable || LayoutInterval.canResize(leadingInterval);
            }
            if (alignment != CENTER) {
                int postGap = parentRegion.positions[dimension][TRAILING]
                    - leadingRegion.positions[dimension][TRAILING];
                trailingGaps = trailingGaps && (postGap != 0);
                LayoutInterval gapInterval = LayoutInterval.getNeighbor(leadingInterval, SEQUENTIAL, TRAILING);
                if ((gapInterval != null) && gapInterval.isEmptySpace() && parParent.isParentOf(gapInterval)
                    && (LayoutInterval.getIntervalCurrentSize(gapInterval, dimension) == postGap)) {
                    LayoutInterval gap = cloneGap(gapInterval);
                    newSequenceList.add(gap);
                    gapSizes.put(gap, new Integer(postGap));
                    if (alignment == LEADING) {
                        sequenceResizable = sequenceResizable || LayoutInterval.canResize(gap);
                    }
                } else {
                    maybeAddGap(newSequenceList, postGap, true);
                }
            }
            resizable = resizable || sequenceResizable;
            if (!sequenceResizable) {
                gapsToResize.addAll(sequenceGapsToResize);
            }
        }
        
        // Modify transfered gaps adjacent to aligned components
        if (alignment != CENTER) {
            Iterator listIter = newSequences.iterator();
            for (int i=0; i<intervals.length; i++) {
                List newSequenceList = (List)listIter.next();
                Iterator iter = newSequenceList.iterator();
                LayoutInterval gapCandidate = null;
                while (iter.hasNext()) {
                    LayoutInterval interval = (LayoutInterval)iter.next();
                    if (interval == intervals[i]) {
                        LayoutRegion region = interval.getCurrentSpace();
                        int diff = 0;
                        if (alignment == TRAILING) {
                            if (iter.hasNext()) {
                                gapCandidate = (LayoutInterval)iter.next();
                                diff = trailingPosition - region.positions[dimension][TRAILING];
                            } else {
                                break;
                            }
                        } else {
                            diff = region.positions[dimension][LEADING] - leadingPosition;
                        }
                        if ((gapCandidate != null) && (gapCandidate.isEmptySpace())) {
                            if ((!leadingGaps && (alignment == LEADING) && (newSequenceList.indexOf(gapCandidate) == 0))
                                || (!trailingGaps && (alignment == TRAILING) && !iter.hasNext())) {
                                newSequenceList.remove(gapCandidate);
                            } else {
                                Integer size = (Integer)gapSizes.get(gapCandidate);
                                int minSize = gapCandidate.getMinimumSize();
                                int prefSize = gapCandidate.getPreferredSize();
                                int maxSize = gapCandidate.getMaximumSize();
                                if (diff > 0) {
                                    if (size != null) {
                                        int actualSize = size.intValue();
                                        diff += prefSize - actualSize;
                                    }
                                    if (minSize >= 0) {
                                        minSize = (minSize - diff > 0) ? minSize - diff : NOT_EXPLICITLY_DEFINED;
                                    }
                                    if (prefSize >= 0) {
                                        prefSize = (prefSize - diff > 0) ? prefSize - diff : NOT_EXPLICITLY_DEFINED;
                                    }
                                    if ((maxSize >= 0) && (maxSize != Short.MAX_VALUE)) {
                                        maxSize = (maxSize - diff > 0) ? maxSize - diff : USE_PREFERRED_SIZE;
                                    }                            
                                }
                                if ((targetEffAlignment == alignment) && (maxSize == Short.MAX_VALUE)) {
                                    maxSize = USE_PREFERRED_SIZE;
                                }
                                layoutModel.setIntervalSize(gapCandidate, minSize, prefSize, maxSize);
                            }
                        }
                        break;
                    }
                    gapCandidate = interval;
                }
            }
        }

        // The content of all new sequence groups is known.
        // We can update the layout model.
        Iterator listIter = newSequences.iterator();
        while (listIter.hasNext()) {
            List newSequenceList = (List)listIter.next();
            LayoutInterval newSequence = new LayoutInterval(SEQUENTIAL);
            if (alignment == CENTER) {
                newSequence.setAlignment(CENTER);
            }
            Iterator iter = newSequenceList.iterator();
            int sequenceAlignment = DEFAULT;
            while (iter.hasNext()) {
                LayoutInterval compInterval = (LayoutInterval)iter.next();
                if (compInterval.isComponent()) { // e.g. compInterval.getParent() != null
                    if (sequenceAlignment == DEFAULT) {
                        sequenceAlignment = getEffectiveAlignment(compInterval);
                    }
                    takeOutInterval(compInterval, parParent);
                    layoutModel.setIntervalAlignment(compInterval, DEFAULT);
                }
                layoutModel.addInterval(compInterval, newSequence, -1);
            }
            if ((alignment != CENTER) && !LayoutInterval.wantResize(newSequence, false)) {
                newSequence.setAlignment(sequenceAlignment);
            }
            if (newSequenceList.size() == 1) {
                LayoutInterval compInterval = (LayoutInterval)newSequenceList.get(0);
                layoutModel.removeInterval(compInterval);
                if (newSequence.getAlignment() != DEFAULT) {
                    layoutModel.setIntervalAlignment(compInterval, newSequence.getAlignment());
                }
                newSequence = compInterval;
            }
            layoutModel.addInterval(newSequence, parParent, -1);
        }
        
        // Check resizability
        if ((gapsToResize.size() > 0) && !resizable && (alignment != CENTER)) {
            suppressGroupResizing(parParent);
            Iterator iter = gapsToResize.iterator();
            while (iter.hasNext()) {
                LayoutInterval gap = (LayoutInterval)iter.next();
                layoutModel.changeIntervalAttribute(gap, LayoutInterval.ATTRIBUTE_FORMER_FILL, false);
                layoutModel.changeIntervalAttribute(gap, LayoutInterval.ATTRIBUTE_FILL, true);
            }
        }
        return gapsToResize;
    }

    /**
     * Determines layout components that will be transfered to the specified
     * parallel parent together with the given layout component.
     *
     * @param interval layout component to transfer to the parallel parent.
     * @param parParent parallel parent to transfer the component to.
     * @return <code>List</code> of <code>LayoutInterval</code> objects.
     */
    private List transferedComponents(LayoutInterval interval, LayoutInterval parParent) {
        LayoutInterval oppInterval = oppositeComponentInterval(interval);
        LayoutInterval oppParentInterval = LayoutInterval.getFirstParent(oppInterval, PARALLEL);
        List transferedComponents = new LinkedList();
        List components = new LinkedList();
        componentsInGroup(oppParentInterval, components);
        Iterator iter = components.iterator();
        while (iter.hasNext()) {
            LayoutInterval oppCandidate = (LayoutInterval)iter.next();
            if (alignedIntervals(oppInterval, oppCandidate, BASELINE)
                || alignedIntervals(oppInterval, oppCandidate, LEADING)
                || alignedIntervals(oppInterval, oppCandidate, TRAILING)
                || alignedIntervals(oppInterval, oppCandidate, CENTER)) {
                LayoutInterval candidate = oppositeComponentInterval(oppCandidate);
                if (parParent.isParentOf(candidate)) {
                    transferedComponents.add(candidate);
                }
            }
        }
        if (!transferedComponents.contains(interval)) {
            transferedComponents.add(interval);
        }

        // Sort layout components according to their current bounds
        Collections.sort(transferedComponents, new Comparator() {
            public int compare(Object o1, Object o2) {
                LayoutInterval interval1 = (LayoutInterval)o1;
                LayoutInterval interval2 = (LayoutInterval)o2;
                LayoutComponent comp = interval1.getComponent();
                int dimension = (comp.getLayoutInterval(VERTICAL) == interval1)
                    ? VERTICAL : HORIZONTAL;
                LayoutRegion region1 = interval1.getCurrentSpace();
                LayoutRegion region2 = interval2.getCurrentSpace();
                int value1 = region1.positions[dimension][LEADING];
                int value2 = region2.positions[dimension][LEADING];
                return (value1 - value2);
            }
        });
        return transferedComponents;
    }
    
    private void componentsInGroup(LayoutInterval group, Collection components) {
        Iterator iter = group.getSubIntervals();
        while (iter.hasNext()) {
            LayoutInterval interval = (LayoutInterval)iter.next();
            if (interval.isGroup()) {
                componentsInGroup(interval, components);
            } else if (interval.isComponent()) {
                components.add(interval);
            }
        }
    }
    
    /**
     * Returns layout interval for the opposite dimension for the given
     * layout interval of a layout component.
     *
     * @param interval layout interval of some layout component.
     * @return layout interval for the opposite dimension for the given
     * layout interval of a layout component.
     */
    private LayoutInterval oppositeComponentInterval(LayoutInterval interval) {
        assert interval.isComponent();
        LayoutComponent component = interval.getComponent();
        int oppDimension = (component.getLayoutInterval(HORIZONTAL) == interval)
            ? VERTICAL : HORIZONTAL;
        return component.getLayoutInterval(oppDimension);
    }
    
    /**
     * Clones given layout interval (empty space).
     *
     * @return clone of the given layout interval (empty space).
     */
    private LayoutInterval cloneGap(LayoutInterval interval) {
        assert interval.isEmptySpace();
        LayoutInterval gap = new LayoutInterval(SINGLE);
        gap.setMinimumSize(interval.getMinimumSize());
        gap.setPreferredSize(interval.getPreferredSize());
        gap.setMaximumSize(interval.getMaximumSize());
        return gap;
    }
    
    /**
     * Helper method that adds layout interval (empty space) to the given
     * list (when the specified size is positive).
     *
     * @param list list the gap should be added to.
     * @param size size of the original space.
     */
    private void maybeAddGap(List list, int size, boolean forceSize) {
        if (size > 0) {
            LayoutInterval gapInterval = new LayoutInterval(SINGLE);
            if (forceSize) {
                layoutModel.setIntervalSize(gapInterval, size, size, size);
            }
            list.add(gapInterval);
        }
    }

    // -----

    /**
     * Aligns given intervals to a parallel group. The intervals are supposed
     * to have the same first parallel parent.
     */
    private boolean align(LayoutInterval[] intervals, boolean closed, int dimension, int alignment) {
        // find common parallel group for aligned intervals
        LayoutInterval commonGroup = null;
        for (int i=0; i < intervals.length; i++) {
            LayoutInterval interval = intervals[i];
            LayoutInterval parent = interval.getParent();
            if (!parent.isParallel()) {
                parent = parent.getParent();
                assert parent.isParallel();
            }
            if (commonGroup == null || (parent != commonGroup && parent.isParentOf(commonGroup))) {
                commonGroup = parent;
            }
            else {
                assert parent == commonGroup || commonGroup.isParentOf(parent);
            }
        }

        // prepare separation to groups
        List aligned = new LinkedList();
        List restLeading = new LinkedList();
        List restTrailing = new LinkedList();
        int mainEffectiveAlign = -1;
        eliminateOffsetGap(commonGroup);
        int originalCount = commonGroup.getSubIntervalCount();

        for (int i=0; i < intervals.length; i++) {
            LayoutInterval interval = intervals[i];
            LayoutInterval parent = interval.getParent();
            LayoutInterval parParent = parent.isParallel() ? parent : parent.getParent();
            if (parParent != commonGroup) {
                interval = getAlignSubstitute(interval, commonGroup, alignment);
                if (interval == null) {
                    return false; // cannot align
                }
                parent = interval.getParent();
            }

            if (parent.isSequential()) {
                mainEffectiveAlign = getEffectiveAlignment(interval); // [need better way to collect - here it takes the last one...]

                // extract the interval surroundings
                int extractCount = extract(interval, alignment, closed,
                                           restLeading, restTrailing);
                if (extractCount == 1) { // the parent won't be reused
                    layoutModel.removeInterval(parent);
                    aligned.add(interval);
                }
                else { // we'll reuse the parent sequence in the new group
                    aligned.add(parent);
                }
            }
            else {
                aligned.add(interval);
            }
        }

        // prepare the group where the aligned intervals will be placed
        LayoutInterval group;
        LayoutInterval commonSeq;
        boolean remainder = !restLeading.isEmpty() || !restTrailing.isEmpty();

        if ((!remainder && mainEffectiveAlign == alignment)
            || (aligned.size() == originalCount
                && commonGroup.getParent() != null))
        {   // reuse the original group - avoid unnecessary nesting
            group = commonGroup;
            if (remainder) { // need a sequence for the remainder groups
                LayoutInterval groupParent = group.getParent();
                if (groupParent.isSequential()) {
                    commonSeq = groupParent;
                }
                else { // insert a new one
                    int index = layoutModel.removeInterval(group);
                    commonSeq = new LayoutInterval(SEQUENTIAL);
                    commonSeq.setAlignment(group.getAlignment());
                    layoutModel.addInterval(commonSeq, groupParent, index);
                    layoutModel.setIntervalAlignment(group, DEFAULT);
                    layoutModel.addInterval(group, commonSeq, -1);
                }
            }
            else commonSeq = null;
        }
        else { // need to create a new group
            group = new LayoutInterval(PARALLEL);
            if (remainder) { // need a new sequence for the remainder groups
                commonSeq = new LayoutInterval(SEQUENTIAL);
                commonSeq.add(group, -1);
                layoutModel.addInterval(commonSeq, commonGroup, -1);
            }
            else {
                commonSeq = null;
                layoutModel.addInterval(group, commonGroup, -1);
            }
            layoutModel.setGroupAlignment(group, alignment);
        }

        // add the intervals and their neighbors to the main aligned group
        // [need to fix the resizability (fill) and compute effective alignment]
        for (Iterator it=aligned.iterator(); it.hasNext(); ) {
            LayoutInterval interval = (LayoutInterval) it.next();
            if (interval.getParent() != group) {
                layoutModel.removeInterval(interval);
                addContent(interval, group, -1);
            }
            layoutModel.setIntervalAlignment(interval, alignment);
        }

        // create the remainder groups around the main one
        if (!restLeading.isEmpty()) {
            createRemainderGroup(restLeading, commonSeq, commonSeq.indexOf(group), LEADING, mainEffectiveAlign);
        }
        if (!restTrailing.isEmpty()) {
            createRemainderGroup(restTrailing, commonSeq, commonSeq.indexOf(group), TRAILING, mainEffectiveAlign);
        }

        return true;
    }
    // [would be nice to have a test checking the model is not changed if
    //  the required align is already in there]

    /**
     * Extracts surroundings of given interval (placed in a sequential group).
     * Extracted intervals are removed and go to the 'restLeading' and
     * 'restTrailing' lists.
     */
    private int extract(LayoutInterval interval,
                        int alignment, boolean closed,
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
                        toRemainL.add(new Integer(getEffectiveAlignment(li)));
                    }
                    toRemainL.add(li);
                }
                else if (idx > endIndex) {
                    if (toRemainT == null) {
                        toRemainT = new LinkedList();
                        toRemainT.add(new Integer(getEffectiveAlignment(li)));
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
     * Creates a remainder parallel group (remainder to a main group of
     * aligned intervals).
     * @param list the content of the group, output from 'extract' method
     * @param seq a sequential group where to add to
     * @param index the index of the main group in the sequence
     * @param position the position of the remainder group relative to the main
     *        group (LEADING or TRAILING)
     * @param mainAlignment effective alignment of the main group (LEADING or
     *        TRAILING or something else meaning not aligned)
     */
    private void createRemainderGroup(List list, LayoutInterval seq,
                                      int index, int position, int mainAlignment)
    {
        assert seq.isSequential() && (position == LEADING || position == TRAILING);
        if (position == TRAILING) {
            index++;
        }
        // [revisit the way how spaces are handled - in accordance to optimizeGaps]
        
        LayoutInterval gap = null;
        LayoutInterval leadingGap = null;
        LayoutInterval trailingGap = null;
        boolean onlyGaps = true;
        boolean gapLeads = true;
        boolean gapTrails = true;

        // Remove sequences just with one gap
        for (int i = list.size()-1; i>=0; i--) {
            List subList = (List)list.get(i);
            if (subList.size() == 2) { // there is just one interval
                int alignment = ((Integer)subList.get(0)).intValue();
                LayoutInterval li = (LayoutInterval) subList.get(1);
                if (li.isEmptySpace()) {
                    if (gap == null || li.getMaximumSize() > gap.getMaximumSize()) {
                        gap = li;
                    }
                    if (isFixedPadding(li)) {
                        if (alignment == LEADING) {
                            leadingGap = li;
                            gapTrails = false;
                        }
                        else if (alignment == TRAILING) {
                            trailingGap = li;
                            gapLeads = false;
                        }
                    }
                    else {
                        gapLeads = false;
                        gapTrails = false;
                    }
                    list.remove(i);
                }
                else {
                    onlyGaps = false;
                }
            }
        }

        if (list.size() == 1) { // just one sequence, need not a group
            List subList = (List) list.get(0);
            Iterator itr = subList.iterator();
            itr.next(); // skip alignment
            do {
                LayoutInterval li = (LayoutInterval) itr.next();
                layoutModel.addInterval(li, seq, index++);
            }
            while (itr.hasNext());
            return;
        }

        // find common ending gaps, possibility to eliminate some...
        for (Iterator it=list.iterator(); it.hasNext(); ) {
            List subList = (List) it.next();
            if (subList.size() != 2) { // there are more intervals (will form a sequential group)
                onlyGaps = false;

                boolean first = true;
                Iterator itr = subList.iterator();
                itr.next(); // skip seq. alignment
                do {
                    LayoutInterval li = (LayoutInterval) itr.next();
                    if (first) {
                        first = false;
                        if (isFixedPadding(li))
                            leadingGap = li;
                        else
                            gapLeads = false;
                    }
                    else if (!itr.hasNext()) {
                        if (isFixedPadding(li))
                            trailingGap = li;
                        else
                            gapTrails = false;
                    }
                }
                while (itr.hasNext());
            }
        }

        if (onlyGaps) {
            layoutModel.addInterval(gap, seq, index);
            assertSingleGap(gap);
            return;
        }

        // create group
        LayoutInterval group = new LayoutInterval(PARALLEL);
        if (position == mainAlignment) {
            // [but this should eliminate resizability only for gaps...]
            group.setMinimumSize(USE_PREFERRED_SIZE);
            group.setMaximumSize(USE_PREFERRED_SIZE);
        }
//        group.setGroupAlignment(alignment);

        // fill the group
        for (Iterator it=list.iterator(); it.hasNext(); ) {
            List subList = (List) it.next();

            if (gapLeads) {
                subList.remove(1);
            }
            if (gapTrails) {
                subList.remove(subList.size()-1);
            }

            LayoutInterval interval;
            if (subList.size() == 2) { // there is just one interval - use it directly
                int alignment = ((Integer)subList.get(0)).intValue();
                interval = (LayoutInterval) subList.get(1);
                if (alignment == LEADING || alignment == TRAILING) {
                    layoutModel.setIntervalAlignment(interval, alignment);
                }
            }
            else { // there are more intervals - group them in a sequence
                interval = new LayoutInterval(SEQUENTIAL);
                Iterator itr = subList.iterator();
                int alignment = ((Integer)itr.next()).intValue();
                if (alignment == LEADING || alignment == TRAILING) {
                    interval.setAlignment(alignment);
                }
                do {
                    LayoutInterval li = (LayoutInterval) itr.next();
                    layoutModel.addInterval(li, interval, -1);
                }
                while (itr.hasNext());
            }
            layoutModel.addInterval(interval, group, -1);
        }

        // add the group to the sequence
        if (gapLeads) {
            layoutModel.addInterval(leadingGap, seq, index++);
        }
        layoutModel.addInterval(group, seq, index++);
        if (gapTrails) {
            layoutModel.addInterval(trailingGap, seq, index);
        }
    }

    private static boolean isFixedPadding(LayoutInterval interval) {
        return interval.isEmptySpace()
               && (interval.getMinimumSize() == NOT_EXPLICITLY_DEFINED || interval.getMinimumSize() == USE_PREFERRED_SIZE)
               && interval.getPreferredSize() == NOT_EXPLICITLY_DEFINED
               && (interval.getMaximumSize() == NOT_EXPLICITLY_DEFINED || interval.getMaximumSize() == USE_PREFERRED_SIZE);
    }

    private LayoutInterval extractParallelSequence(LayoutInterval seq,
                                                   LayoutRegion space,
                                                   boolean close,
                                                   int dimension,
                                                   int alignment1,
                                                   int alignment2)
    {
        int count = seq.getSubIntervalCount();
        int startIndex = 0;
        int endIndex = count - 1;
        int startPos = seq.getCurrentSpace().positions[dimension][LEADING];
        int endPos = seq.getCurrentSpace().positions[dimension][TRAILING];

        for (int i=0; i < count; i++) {
            LayoutInterval li = seq.getSubInterval(i);
            if (li.isEmptySpace())
                continue;

            LayoutRegion subSpace = li.getCurrentSpace();

            if (contentOverlap(space, li, dimension^1)) { // orthogonal overlap
                // this interval makes a hard boundary
                if (getAddDirection(space, subSpace, dimension, alignment1, alignment2) == LEADING) {
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
        group.setGroupAlignment(/*groupAlign == LEADING || groupAlign == TRAILING ?
                                groupAlign :*/ alignment1);
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

    private static int determinePadding(LayoutInterval interval, int dimension, int alignment) {
        LayoutInterval neighbor = LayoutInterval.getNeighbor(interval, alignment, true, true, false);
        return LayoutDragger.findPadding(neighbor, interval.getComponent(), dimension, alignment);
        // [TODO interval may not be component]
    }

    /**
     * Finds padding for an interval (yet to be added) in relation to a base
     * interval or a parent interval border (base interval null)
     * @param alignment LEADING or TRAILING point of addingInt
     */ 
    private static int determineExpectingPadding(LayoutInterval addingInt,
                                                 LayoutInterval baseInt,
                                                 LayoutInterval baseParent,
                                                 int dimension, int alignment)
    {
        // PENDING rewrite to work correctly for general intervals
        // assert addingInt.isComponent();
        if (baseInt == null) {
            baseInt = LayoutInterval.getNeighbor(baseParent, SEQUENTIAL, alignment);
        }
        return LayoutDragger.findPadding(baseInt, addingInt.getComponent(), dimension, alignment);
    }

    /**
     * A description of inclusion of a new interval. This gives information
     * about what group gets affected, if a sub-group needs to be created, and
     * allows for comparing with other inclusion (to choose the best one).
     */
    private static class IncludeDesc {
        LayoutInterval parent;
        LayoutInterval group;
        LayoutInterval interval;
        int distance = Integer.MAX_VALUE;
        int ortDistance = Integer.MAX_VALUE;
        boolean overlap;
        boolean complemental;

        static int compare(IncludeDesc d1, IncludeDesc d2, LayoutDragger.PositionDef pos) {
            boolean b1 = d1.isMatching(pos);
            boolean b2 = d2.isMatching(pos);
            if (b1 != b2) {
                return b1 ? 1 : -1;
            }

            b1 = d1.isComplemental();
            b2 = d2.isComplemental();
            if (b1 != b2) {
                return b1 ? -1 : 1;
            }

            return 0;
        }

        static boolean compatible(IncludeDesc d1, IncludeDesc d2, LayoutDragger.PositionDef pos) {
            LayoutInterval par1 = d1.group != null && d1.group.isParallel() ?
                                  d1.group : d1.parent;
            LayoutInterval par2 = d2.group != null && d2.group.isParallel() ?
                                  d2.group : d2.parent;
            if (par1 == par2) {
                return true;
            }
            if (!pos.nextTo && (pos.alignment == LEADING || pos.alignment == TRAILING)) {
                if (d1.isComplemental() && par2.isParentOf(par1)) { // [shouldn't d2.isComplemental() be here?? the complemental is likely the parent]
                    return getAlignSubstitute(pos.interval, par2.isSequential() ? par2.getParent() : par2, pos.alignment) != null;
                }
                else if (d2.isComplemental() && par1.isParentOf(par2)) { // [and d1 here??]
                    return getAlignSubstitute(pos.interval, par1.isSequential() ? par1.getParent() : par1, pos.alignment) != null;
                }
            }
            return false;
        }

        static boolean same(IncludeDesc d1, IncludeDesc d2) {
            return d1.parent == d2.parent && d1.group == d2.group && d1.interval == d2.interval;
        }

        boolean isComplemental() {
            return complemental;
//            return ((parent.isParallel() && group == null)
//                       || (group != null && group.isParallel()))
//                   && interval == null;
//            // [noOrtOverlap?]
        }

        boolean isMatching(LayoutDragger.PositionDef pos) {
            LayoutInterval posParent = pos.interval.getParent();
            if (posParent == null) {
                return true;
//                       parent == pos.interval
//                       || (group == null && parent.isSequential())
//                       || (group != null && group.isSequential());
//                 // parent == pos.interval && (group == null || group.isSequential());
            }
            if (pos.nextTo) {
                return group == posParent
                       || (parent == posParent && (group == null || parent.isSequential()))
                       || (posParent.isSequential() && posParent.isParentOf(parent));
            }
            else { // align with
                if (posParent.isSequential() && (group != null || parent.isParallel())) {
                    posParent = posParent.getParent();
                }
                return group == posParent || parent == posParent;
            }
        }
    }

    // -----

    private static LayoutDragger.PositionDef getCurrentPosition(
                                                 LayoutInterval li,
                                                 boolean toBeRemoved,
                                                 int dimension,
                                                 int alignment)
    {
        assert alignment == DEFAULT || alignment == LEADING || alignment == TRAILING;

        LayoutInterval parent = li.getParent();
        LayoutInterval neighbor = null;
        LayoutInterval interval = null;
        boolean padding = false;
        int align;
        boolean specificAlignment = alignment != DEFAULT;

        if (parent.isSequential()) {
            if (!specificAlignment) {
                align = getEffectiveAlignment(li);
                if (align != TRAILING) {
                    align = LEADING;
                }
            }
            else {
                align = alignment;
            }
            neighbor = getCloseNeighbor(li, align);
            if (neighbor == null) {
                if (!specificAlignment) {
                    neighbor = getCloseNeighbor(li, align^1);
                    if (neighbor != null)
                        align ^= 1;
                }
                if (neighbor == null) {
                    neighbor = LayoutInterval.getNeighbor(li, align, true, true, false);
                }
            }
            LayoutInterval gap = LayoutInterval.getNeighbor(li, align, false, true, true);
            if (gap != null && gap.isDefaultPadding() && !LayoutInterval.canResize(gap)) {
                padding = true;
            }
        }
        else { // parallel parent
            if (!specificAlignment) {
                align = li.getAlignment();
            }
            else {
                align = alignment;
                if (!LayoutInterval.isAlignedAtBorder(li, align)) {
                    return null;
                }
            }
            // try a padded neighbor first (as dragger also prefers "next to" position)
//            if (toBeRemoved && (align == LEADING || align == TRAILING)) {
//                LayoutInterval gap = LayoutInterval.getNeighbor(parent, align, false, true, true);
//                if (gap != null && gap.isDefaultPadding() && !LayoutInterval.canResize(gap)) {
//                    neighbor = LayoutInterval.getDirectNeighbor(gap, align, true); // [perhaps should go to parents as well?]
//                    if (neighbor != null) {
//                        padding = true;
//                    }
//                }
//            }
            if (neighbor == null) { // second try to find an aligned component
                LayoutInterval sibling = getAlignedComponent(li, dimension, align);
                if (sibling != null) {
                    interval = sibling;
                }
                else if (!toBeRemoved
                         || (LayoutInterval.getCount(parent, align, true) > 1
                             && LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true) > 2))
                {   // the group will stay even after the component is removed
                    interval = parent;
                }
                else { // aligned not found - try anything next to (default padding not required now)
                    if (align != TRAILING) {
                        align = LEADING;
                    }
                    neighbor = LayoutInterval.getNeighbor(parent, align, true, true, true);
                    if (neighbor != null) {
                        LayoutInterval gap = LayoutInterval.getNeighbor(parent, align, false, true, true);
                        if (gap != null && gap.isDefaultPadding() && !LayoutInterval.canResize(gap)) {
                            padding = true;
                        }
                    }
                }
            }
        }

        LayoutDragger.PositionDef pos = new LayoutDragger.PositionDef();
        if (interval != null) {
            pos.interval = interval;
            pos.alignment = align;
            pos.nextTo = false;
            pos.snapped = true;
        }
        else if (neighbor != null) {
            pos.interval = neighbor;
            pos.alignment = align;
            pos.nextTo = true;
            pos.snapped = padding;
        }
        else { // no neighbor, relate to root
            pos.interval = LayoutInterval.getRoot(li);
            pos.alignment = align;
            pos.nextTo = li.getCurrentSpace().positions[dimension][align]
                         != pos.interval.getCurrentSpace().positions[dimension][align];
            pos.snapped = !pos.nextTo || padding;
        }
        return pos;
    }

    /**
     * Looks for a non-empty neighbor in the same sequence, being separated by
     * a fixed gap (or no gap). If behind a resizing gap, it is not considered
     * "close".
     */
    private static LayoutInterval getCloseNeighbor(LayoutInterval interval, int alignment) {
        LayoutInterval parent = interval.getParent();
        int index = parent.indexOf(interval) + (alignment == LEADING ? -1 : 1);
        while (index >= 0 && index < parent.getSubIntervalCount()) {
            LayoutInterval neighbor = parent.getSubInterval(index);
            if (!neighbor.isEmptySpace()) {
                return neighbor;
            }
            if (LayoutInterval.canResize(neighbor)) {
                return null; // resizing gap - no close neighbor
            }
            index += alignment == LEADING ? -1 : 1;
        }
        return null;
    }

    private static LayoutInterval getAlignedComponent(LayoutInterval interval,
                                                      int dimension,
                                                      int alignment)
    {
        LayoutInterval parent = interval.getParent();
        assert parent.isParallel();
        Iterator it = parent.getSubIntervals();
        do {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li != interval && li.getAlignment() == alignment) {
                if (li.isGroup()) {
                    LayoutInterval comp = getOutermostComponent(li, dimension, alignment);
                    if (comp != null && LayoutInterval.isAlignedAtBorder(comp, li, alignment)) {
                        li = comp;
                    }
                }
                if (li.isComponent()) {
                    return li;
                }
            }
        }
        while (it.hasNext());

        // aligned component not found, try just visually matching
        it = parent.getSubIntervals();
        do {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li != interval) {
                if (li.isGroup()) {
                    LayoutInterval comp = getOutermostComponent(li, dimension, alignment);
                    if (comp != null) {
                        li = comp;
                    }
                }
                if (li.isComponent()
                    && LayoutRegion.distance(li.getCurrentSpace(), interval.getCurrentSpace(),
                                             dimension, alignment, alignment) == 0)
                {   // this component is placed at the group border though it is aligned to the other side
                    return li;
                }
            }
        }
        while (it.hasNext());

        return null;
    }

    // -----

    // requires the layout image up-to-date (all positions known)
    // requires the group contains some component (at least indirectly)
    private int optimizeGaps(LayoutInterval group, int dimension, boolean recursive) {
        assert group.isParallel();

        // sub-groups first (not using iterator, intervals may change)
        if (recursive) {
            for (int i=0; i < group.getSubIntervalCount(); i++) {
                LayoutInterval li = group.getSubInterval(i);
                if (li.isParallel()) {
                    optimizeGaps(li, dimension, recursive);
                }
                else if (li.isSequential()) {
                    for (int ii=0; ii < li.getSubIntervalCount(); ii++) {
                        LayoutInterval llii = li.getSubInterval(ii);
                        if (llii.isParallel()) {
                            int idx = optimizeGaps(llii, dimension, recursive);
                            if (idx >= 0) // position in sequence changed (a gap inserted)
                                ii = idx;
                        }
                    }
                }
            }
        }

        if (group.getGroupAlignment() == CENTER || group.getGroupAlignment() == BASELINE) {
            return -1;
        }
        int nonEmptyCount = LayoutInterval.getCount(group, LayoutRegion.ALL_POINTS, true);
        if (nonEmptyCount <= 1) {
            if (group.getParent() == null) {
                if (group.getSubIntervalCount() > 1) {
                    eliminateOffsetGap(group);
                }
                else if (group.getSubIntervalCount() == 0) {
                    // [sort of hack - would be nice the filling gap is ensured somewhere else]
                    propEmptyContainer(group, dimension);
                }
            } else {
                assert (nonEmptyCount == 1);
                LayoutInterval offsetGap = eliminateOffsetGap(group);
                assert (group.getSubIntervalCount() == 1);
                LayoutInterval interval = group.getSubInterval(0);
                int alignment = interval.getAlignment();
                layoutModel.removeInterval(interval);
                layoutModel.setIntervalAlignment(interval, group.getAlignment());
                LayoutInterval parent = group.getParent();
                int index = layoutModel.removeInterval(group);
                if (parent.isSequential() && interval.isSequential()) {
                    // dissolve the sequential group in its parent                    
                    for (int i=interval.getSubIntervalCount()-1; i>=0; i--) {
                        LayoutInterval subInterval = interval.getSubInterval(i);
                        layoutModel.removeInterval(subInterval);
                        layoutModel.addInterval(subInterval, parent, index);
                    }
                    eliminateConsecutiveGaps(parent, 0, dimension);
                } else {
                    layoutModel.addInterval(interval, parent, index);
                }
                if (offsetGap != null) {
                    // Reinsert/simulate behaviour of the removed offset gap.
                    int size;
                    if (parent.isSequential()) {
                        size = LayoutInterval.getIntervalCurrentSize(group, dimension)
                            - LayoutInterval.getIntervalCurrentSize(interval, dimension);
                        if (alignment == LEADING) index++;
                    } else {
                        size = LayoutInterval.getIntervalCurrentSize(parent, dimension);
                        index = -1;
                    }
                    layoutModel.setIntervalSize(offsetGap, offsetGap.getMinimumSize(), size, offsetGap.getMaximumSize());
                    layoutModel.addInterval(offsetGap, parent, index);
                    eliminateConsecutiveGaps(parent, 0, dimension);
                }
            }
            return -1;
        }

        // go through the group and check if there is anything to do
        boolean anyLeadingGap = false;
        boolean anyTrailingGap = false;
        for (int i=0, n=group.getSubIntervalCount(); i < n; i++) {
            LayoutInterval li = group.getSubInterval(i);
            if (li.isSequential()) {
                if (li.getSubInterval(0).isEmptySpace()) {
                    anyLeadingGap = true;
                    break;
                }
                if (li.getSubInterval(li.getSubIntervalCount()-1).isEmptySpace()) {
                    anyTrailingGap = true;
                    break;
                }
            }
            else if (li.isEmptySpace()) { // offset gap - might be not needed anymore
                anyLeadingGap = anyTrailingGap = true;
                break;
            }
        }

        if (!anyLeadingGap && !anyTrailingGap) {
            return -1; // nothing to eliminate
        }

        // collect data about visual arrangement of the sub-intervals in the group
        boolean leadingAlign = false;
        boolean trailingAlign = false;
        boolean resizingContent = false;
        boolean leadingGapResizing = false;
        boolean trailingGapResizing = false;
        boolean leadingPadding = false;
        boolean trailingPadding = false;
        boolean defaultLeadingGap = false;
        boolean defaultTrailingGap = false;
        boolean selfSized = false; // [!!need to consider also resizability!!]
        int[] groupOuterPos = group.getCurrentSpace().positions[dimension];
        assert groupOuterPos[LEADING] > Short.MIN_VALUE && groupOuterPos[TRAILING] > Short.MIN_VALUE;
        int leadingPos = getOutermostComponent(group, dimension, LEADING).getCurrentSpace().positions[dimension][LEADING];
        int trailingPos = getOutermostComponent(group, dimension, TRAILING).getCurrentSpace().positions[dimension][TRAILING];
        LayoutInterval offsetGap = null;

        for (int i=0; i < group.getSubIntervalCount(); i++) {
            LayoutInterval li = group.getSubInterval(i);
            if (li.isSequential()) { // sequences may have ending gaps
                int align = li.getAlignment();
                assert align == LEADING || align == TRAILING;
                int d = align == LEADING ? 1 : -1;
                int alIdx = align == LEADING ? 0 : li.getSubIntervalCount()-1;
                int opIdx = align == LEADING ? li.getSubIntervalCount()-1 : 0;

                // gap at the aligned end of the sequence
                LayoutInterval alignedGap = li.getSubInterval(alIdx);
                if (!alignedGap.isEmptySpace()) {
                    alignedGap = null;
                }
                // gap at the opposite end of the sequence ("open" end)
                LayoutInterval oppositeGap = li.getSubInterval(opIdx);
                if (!oppositeGap.isEmptySpace()) {
                    oppositeGap = null;
                }

                // calculate end positions without gaps
                int alPos = li.getSubInterval(alignedGap != null ? alIdx+d : alIdx)
                                   .getCurrentSpace().positions[dimension][align];
                int opPos = li.getSubInterval(oppositeGap != null ? opIdx-d : opIdx)
                                   .getCurrentSpace().positions[dimension][align^1];
                assert alPos > Short.MIN_VALUE && opPos > Short.MIN_VALUE;

                int seqLeadPos = align == LEADING ? alPos : opPos;
                int seqTrailPos = align == LEADING ? opPos : alPos;

                if (alignedGap != null && alignedGap.getMaximumSize() >= Short.MAX_VALUE) {
                    // resizing aligned gap is superfluous - revert the alignment instead
                    assert oppositeGap == null || oppositeGap.getMaximumSize() < Short.MAX_VALUE;
                    // [what if both end gaps are resizing? should solve as center alignment?]
                    align ^= 1;
                    layoutModel.setIntervalAlignment(li, align);
                    LayoutInterval temp = alignedGap;
                    alignedGap = oppositeGap;
                    oppositeGap = temp;
                }

                if (alignedGap != null && nonEmptyCount > 1
                    && ((align == LEADING && leadingPos > groupOuterPos[LEADING])
                        || (align == TRAILING && trailingPos < groupOuterPos[TRAILING])))
                {   // group is going to shrink - shorten the gap
                    int reducedSize = align == LEADING ?
                                      seqLeadPos - leadingPos : trailingPos - seqTrailPos;
                    if (reducedSize > 0) {
                        resizeInterval(alignedGap, reducedSize);
                    }
                    else {
                        layoutModel.removeInterval(alignedGap);
                        if (alignedGap.isDefaultPadding()) {
                            if (align == LEADING) {
                                leadingPadding = true;
                                if (alignedGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED)
                                    defaultLeadingGap = true;
                            }
                            else {
                                trailingPadding = true;
                                if (alignedGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED)
                                    defaultTrailingGap = true;
                            }
                        }
                    }
                }

                if (oppositeGap != null && nonEmptyCount > 1) {
                    if (!LayoutInterval.wantResize(oppositeGap, false) && LayoutInterval.wantResize(li, false)) { // same as aligned
                        if ((align == TRAILING && leadingPos > groupOuterPos[LEADING])
                            || (align == LEADING && trailingPos < groupOuterPos[TRAILING]))
                        {   // group is going to shrink - shorten the gap
                            int reducedSize = align == TRAILING ?
                                              seqLeadPos - leadingPos : trailingPos - seqTrailPos;
                            if (reducedSize > 0) {
                                resizeInterval(oppositeGap, reducedSize);
                            }
                            else {
                                layoutModel.removeInterval(oppositeGap);
                                if (oppositeGap.isDefaultPadding()) {
                                    if (align == TRAILING) {
                                        leadingPadding = true;
                                        if (oppositeGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED)
                                            defaultLeadingGap = true;
                                    }
                                    else {
                                        trailingPadding = true;
                                        if (oppositeGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED)
                                            defaultTrailingGap = true;
                                    }
                                }
                            }
                        }
                    }
                    else { // gap at the open end is not needed (the sequence is aligned to the other side)
                        if (align == LEADING) {
                            if (oppositeGap.isDefaultPadding()) {
                                trailingPadding = true;
                                if (seqTrailPos == trailingPos && oppositeGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED)
                                    defaultTrailingGap = true;
                                // [maybe we should also check that seq outer pos == group outer pos]
                            }
                            if (oppositeGap.getMaximumSize() >= Short.MAX_VALUE)
                                trailingGapResizing = true;
                        }
                        else { // TRAILING
                            if (oppositeGap.isDefaultPadding()) {
                                leadingPadding = true;
                                if (seqLeadPos == leadingPos && oppositeGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED)
                                    defaultLeadingGap = true;
                                // [maybe we should also check that seq outer pos == group outer pos]
                            }
                            if (oppositeGap.getMaximumSize() >= Short.MAX_VALUE)
                                leadingGapResizing = true;
                        }
                        layoutModel.removeInterval(oppositeGap);
                    }
                }

                assert li.getSubIntervalCount() > 0;
                if (li.getSubIntervalCount() == 1) {
                    // only one interval remained in sequence - cancel the sequence
                    layoutModel.removeInterval(group, i); // removes li from group
                    LayoutInterval sub = layoutModel.removeInterval(li, 0); // removes last interval from li
                    layoutModel.setIntervalAlignment(sub, align);
                    layoutModel.addInterval(sub, group, i);
                    i--;
                    continue;
                }

                // check whether the sequence spans the whole group width
                if ((align == LEADING && seqTrailPos >= trailingPos)
                        || (align == TRAILING && seqLeadPos <= leadingPos)) {
                    selfSized = true;
                }
            }
            else if (li.isEmptySpace()) { // likely an offset gap (holding the min size of the group)
                if (group.getSubIntervalCount() > 1) {
                    offsetGap = li;
                    layoutModel.removeInterval(li); // if needed, will be added again
                    i--;
                    li = null;
                }
                else offsetGap = null;
            }
            else { // not sequence nor gap
                if (li.isParallel()) {
                    layoutModel.removeInterval(group, i);
                    if (addContent(li, group, i)) { // content of sub-group merged
                        i--;
                        continue;
                    }
                }
                // check whether it spans the group width
                int currentSize = li.getCurrentSpace().size(dimension);
                assert currentSize > Short.MIN_VALUE && currentSize < Short.MAX_VALUE;
                if (currentSize >= trailingPos - leadingPos) {
                    // this interval is end-to-end, no ending gaps needed
                    selfSized = true;
                }
            }

            if (li != null) {
                if (!resizingContent && LayoutInterval.wantResize(li, false)) {
                    resizingContent = true;
                }
                if (li.getAlignment() == LEADING) {
                    leadingAlign = true;
                }
                else { // TRAILING
                    trailingAlign = true;
                }
            }
        }

        assert group.getSubIntervalCount() > 0;
//        if (group.getSubIntervalCount() == 1) {
//            // this may happen if there was an offset gap forgotten from previous state
//            LayoutInterval parent = group.getParent();
//            int idx = layoutModel.removeInterval(group);
//            addContent(group.getSubInterval(0), parent, idx);
//            return; // whole group was eliminated
//            // [this should happen only if a group containing just gaps was passed in]
//        }

        if (!resizingContent // nothing resizing inside the group
            && (!selfSized // no sub-interval has the size of the group
                || (leadingAlign && trailingAlign && (leadingGapResizing || trailingGapResizing)))) // there was a resizing gap affecting the group
        {   // need to set up an offset gap (strut)
            if (offsetGap == null) {
                offsetGap = new LayoutInterval(SINGLE);
            }
            int min = 0;
            int pref = trailingPos - leadingPos;
            int max;
            if (leadingGapResizing || trailingGapResizing || offsetGap.getMaximumSize() >= Short.MAX_VALUE) {
                max = Short.MAX_VALUE;
                resizingContent = true;
            }
            else {
                max = USE_PREFERRED_SIZE;
            }
            layoutModel.setIntervalSize(offsetGap, min, pref, max);
            layoutModel.addInterval(offsetGap, group, -1);
        }

        if (!resizingContent) {
            enableGroupResizing(group);
        }

        LayoutInterval leadingGap = null;
        LayoutInterval trailingGap = null;
        if (leadingPos > groupOuterPos[LEADING]) {
            int size = leadingPos - groupOuterPos[LEADING];
            leadingGap = new LayoutInterval(SINGLE);
            if (!defaultLeadingGap) {
                int pad = determinePadding(group, dimension, LEADING);
                if (size == pad) {
                    defaultLeadingGap = leadingPadding = true;
                }
            }
            if (!leadingPadding) {
                leadingGap.setMinimumSize(USE_PREFERRED_SIZE);
            }
            if (!defaultLeadingGap) {
                leadingGap.setPreferredSize(size);
            }
            else assert leadingPadding;
            if (leadingGapResizing && !resizingContent && !leadingAlign) {
                leadingGap.setMaximumSize(Short.MAX_VALUE);
            }
        }
        if (groupOuterPos[TRAILING] > trailingPos) {
            int size = groupOuterPos[TRAILING] - trailingPos;
            trailingGap = new LayoutInterval(SINGLE);
            if (!defaultTrailingGap) {
                int pad = determinePadding(group, dimension, TRAILING);
                if (size == pad) {
                    defaultTrailingGap = trailingPadding = true;
                }
            }
            if (!trailingPadding) {
                trailingGap.setMinimumSize(USE_PREFERRED_SIZE);
            }
            if (!defaultTrailingGap) {
                trailingGap.setPreferredSize(size);
            }
            else assert trailingPadding;
            if (trailingGapResizing && !resizingContent && !trailingAlign) {
                trailingGap.setMaximumSize(Short.MAX_VALUE);
            }
        }
        if (leadingGap != null || trailingGap != null) {
            groupOuterPos[LEADING] = leadingPos;
            groupOuterPos[TRAILING] = trailingPos;
            groupOuterPos[CENTER] = (leadingPos + trailingPos) / 2;
            if (leadingGap != null) {
                group = insertGap(leadingGap, group, leadingPos, dimension, LEADING);
            }
            // [should check where we are adding - not to create two gaps consecutively]
            if (trailingGap != null) {
                group = insertGap(trailingGap, group, trailingPos, dimension, TRAILING);
            }
            LayoutInterval parent = group.getParent();
            return parent != null ? parent.indexOf(group) : -1;//idx;
        }
        return -1;
    }

    private LayoutInterval eliminateOffsetGap(LayoutInterval group) {
        assert group.isParallel();
        for (int i=group.getSubIntervalCount()-1; i >= 0; i--) {
            if (group.getSubInterval(i).isEmptySpace()) {
                return layoutModel.removeInterval(group, i);
            }
        }
        return null;
    }

    private static LayoutInterval getOutermostComponent(LayoutInterval interval, int dimension, int alignment) {
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

    // -----

    private void resizeInterval(LayoutInterval interval, int size) { //LayoutRegion space, int dimension
        assert size >= 0 || size == NOT_EXPLICITLY_DEFINED;
//        int size = space.size(dimension);
//        int currentSize = interval.getCurrentSpace().size(dimension);
//        if (size != currentSize) {
            int min = interval.getMinimumSize() == interval.getPreferredSize()
                      && (interval.getMinimumSize() != NOT_EXPLICITLY_DEFINED
                          || interval.getMaximumSize() < Short.MAX_VALUE) ?
                      size : interval.getMinimumSize();
            int max = interval.getMaximumSize() == interval.getPreferredSize() ?
                      size : interval.getMaximumSize();
            layoutModel.setIntervalSize(interval, min, size, max);
//        }
    }

    /**
     * Sets interval resizability. Makes it resizing or fixed.
     *
     * @param interval layout interval whose resizability should be set.
     * @param resizable determines whether the passed interval should be made resizable.
     */
    private void setIntervalResizing(LayoutInterval interval, boolean resizable) {
        switchFillAttribute(interval, resizable);
        layoutModel.setIntervalSize(interval,
            resizable ? NOT_EXPLICITLY_DEFINED : USE_PREFERRED_SIZE,
            interval.getPreferredSize(),
            resizable ? Short.MAX_VALUE : USE_PREFERRED_SIZE);
    }

    // Changes fill attributes when interval becomes (non-)resizable.
    private void switchFillAttribute(LayoutInterval interval, boolean resizable) {
        if (resizable) {
            if (interval.hasAttribute(LayoutInterval.ATTRIBUTE_FILL)) {
                layoutModel.changeIntervalAttribute(interval, LayoutInterval.ATTRIBUTE_FORMER_FILL, true);
                layoutModel.changeIntervalAttribute(interval, LayoutInterval.ATTRIBUTE_FILL, false);
            }            
        } else {
            if (interval.hasAttribute(LayoutInterval.ATTRIBUTE_FORMER_FILL)) {
                layoutModel.changeIntervalAttribute(interval, LayoutInterval.ATTRIBUTE_FORMER_FILL, false);
                layoutModel.changeIntervalAttribute(interval, LayoutInterval.ATTRIBUTE_FILL, true);
            }
        }
    }

    private void suppressGroupResizing(LayoutInterval group) {
        // Don't suppress resizing of root group
        if (group.getParent() != null) {
            layoutModel.setIntervalSize(group, group.getMinimumSize(),
                                               group.getPreferredSize(),
                                               USE_PREFERRED_SIZE);
        }
    }

    private void enableGroupResizing(LayoutInterval group) {
        layoutModel.setIntervalSize(group, group.getMinimumSize(),
                                           group.getPreferredSize(),
                                           NOT_EXPLICITLY_DEFINED);
    }

    private void findContainerResizingGap(LayoutInterval rootInterval, int dimension) {
        // find gap for container resizing
        int gapPosition = TRAILING;
        LayoutInterval resGap = findContainerResizingGap(rootInterval, dimension, gapPosition);
        if (resGap == null) {
            gapPosition = LEADING;
            resGap = findContainerResizingGap(rootInterval, dimension, gapPosition);
            if (resGap == null) {
                gapPosition = -1;
                resGap = findContainerResizingGap(rootInterval, dimension, gapPosition);
                if (resGap == null) {
                    return;
                }
            }
        }
        else if (!LayoutInterval.canResize(resGap)) { // we prefer resizing gaps
            LayoutInterval gap = findContainerResizingGap(rootInterval, dimension, LEADING);
            if (gap != null && LayoutInterval.canResize(gap)) {
                resGap = gap;
                gapPosition = LEADING;
            }
            else {
                gap = findContainerResizingGap(rootInterval, dimension, -1);
                if (gap != null && LayoutInterval.canResize(gap)) {
                    resGap = gap;
                    gapPosition = -1;
                }
            }
        }

        // mark the gap and all surrounding intervals
        resGap.setAttribute(LayoutInterval.ATTR_DESIGN_CONTAINER_GAP
                            | LayoutInterval.ATTR_DESIGN_RESIZING);

        LayoutInterval sub = resGap;
        LayoutInterval parent = resGap.getParent();
        do {
            if (parent.isSequential()) {
                for (Iterator it=parent.getSubIntervals(); it.hasNext(); ) {
                    LayoutInterval li = (LayoutInterval) it.next();
                    if (li != sub) {
                        li.setAttribute(LayoutInterval.ATTR_DESIGN_SUPPRESSED_RESIZING);
                    }
                }
            }
            else { // parallel parent
                for (Iterator it=parent.getSubIntervals(); it.hasNext(); ) {
                    LayoutInterval interval = (LayoutInterval) it.next();
                    if (interval != sub) {
                        assert interval.isSequential();
                        if (interval.isSequential()) {
                            for (int i=0, n=interval.getSubIntervalCount(); i < n; i++) {
                                LayoutInterval li = interval.getSubInterval(i);
                                if (((i == 0 && gapPosition == LEADING)
                                     || (i+1 == n && gapPosition == TRAILING))
                                    && canBeContainerResizingGap(li))
                                {   // parallel resizing gap
                                    li.setAttribute(LayoutInterval.ATTR_DESIGN_RESIZING);
                                }
                                else li.setAttribute(LayoutInterval.ATTR_DESIGN_SUPPRESSED_RESIZING);
                            }
                        }
                        else {
                            interval.setAttribute(LayoutInterval.ATTR_DESIGN_SUPPRESSED_RESIZING);
                        }
                    }
                }
            }
            sub = parent;
            parent = sub.getParent();
        }
        while (parent != null);
    }

    private LayoutInterval findContainerResizingGap(LayoutInterval group, int dimension, int alignment) {
        assert group.isParallel();

        LayoutInterval theGap = null;
        int gapSize = Integer.MAX_VALUE;

        for (Iterator it=group.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval seq = (LayoutInterval) it.next();
            if (!seq.isSequential()) {
                return null;
            }

            int n = seq.getSubIntervalCount();
            if (alignment == LEADING || alignment == TRAILING) {
                LayoutInterval li = seq.getSubInterval(alignment == LEADING ? 0 : n-1);
                LayoutInterval gap;
                if (canBeContainerResizingGap(li)) {
                    gap = li;
                }
                else if (li.isParallel()) {
                    gap = findContainerResizingGap(li, dimension, alignment);
                }
                else gap = null; // not an ending gap

                if (gap == null) {
                    return null;
                }

                LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(gap, alignment^1, false);
                int p1 = neighbor.getCurrentSpace().positions[dimension][alignment];
                int p2 = group.getCurrentSpace().positions[dimension][alignment^1];
                int size = Math.abs(p2-p1);

                if (theGap == null || size < gapSize) {
                    theGap = gap;
                    gapSize = size;
                }
//                if (LayoutInterval.canResize(gap) != LayoutInterval.canResize(theGap)) {
//                    return null;
//                }
            }
            else { // somewhere in the middle - can be just one
                for (int i=n-2; i > 0; i--) {
                    LayoutInterval li = seq.getSubInterval(i);
                    if (canBeContainerResizingGap(li)) {
                        return group.getSubIntervalCount() == 1 ? li : null;
                    }
                }
                return null;
            }
        }

        return theGap;
    }

    private static boolean canBeContainerResizingGap(LayoutInterval li) {
        return li.isEmptySpace()
               && (li.getPreferredSize() != NOT_EXPLICITLY_DEFINED || li.getMaximumSize() >= Short.MAX_VALUE);
    }

    private void imposeCurrentSize(LayoutComponent component, LayoutDragger.SizeDef[] resizingDef) {
        if (component.isLayoutContainer()) {
            component.setCurrentInterior(visualMapper.getContainerInterior(component.getId()));
            for (Iterator it=component.getSubcomponents(); it.hasNext(); ) {
                LayoutComponent subComp = (LayoutComponent) it.next();
                subComp.setCurrentBounds(visualMapper.getComponentBounds(subComp.getId()), -1);
                imposeCurrentSize(subComp, null);
            }
            Dimension minimum = null;
            Dimension preferred = null;
            for (int i=0; i < DIM_COUNT; i++) {
                LayoutInterval outer = component.getLayoutInterval(i);
                int currentSize = outer.getCurrentSpace().size(i);
                LayoutInterval root = component.getLayoutRoot(i);
                if (root.getSubIntervalCount() == 0) { // empty root group - add a filling gap
                    propEmptyContainer(root, i);
                }
                else {
                    LayoutInterval resGap = resizingDef != null && resizingDef[i] != null ?
                                            resizingDef[i].getResizingGap() : null;
                    if (resGap != null) { // special gap for design time resizing
                        resizeInterval(resGap, resizingDef[i].getResizingGapSize(currentSize));
                        if (resGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED && LayoutInterval.canResize(resGap)) {
                            layoutModel.setIntervalSize(resGap, NOT_EXPLICITLY_DEFINED, NOT_EXPLICITLY_DEFINED, USE_PREFERRED_SIZE);
                            if (!LayoutInterval.wantResize(root, false)) {
                                layoutModel.setIntervalSize(resGap, NOT_EXPLICITLY_DEFINED, NOT_EXPLICITLY_DEFINED, Short.MAX_VALUE);
                            }
                        }
                    }
                }
                updateLayoutStructure(root, i);
                imposeGapsSize(root, i);

                if (component.getParent() != null) {
                    if (minimum == null) {
                        minimum = visualMapper.getComponentMinimumSize(component.getId());
                        preferred = visualMapper.getComponentPreferredSize(component.getId());
                    }
                    int min = i == HORIZONTAL ? minimum.width : minimum.height;
                    boolean externalSize =
                        (visualMapper.hasExplicitPreferredSize(component.getId())
                         && currentSize != (i==HORIZONTAL ? preferred.width:preferred.height))
                        || currentSize < min
                        || (currentSize > min && !LayoutInterval.wantResize(root, false));
                    resizeInterval(outer, externalSize ? currentSize : NOT_EXPLICITLY_DEFINED);
                }
            }
        }
        else {
            Dimension preferred = visualMapper.getComponentPreferredSize(component.getId());
            for (int i=0; i < DIM_COUNT; i++) {
                LayoutInterval li = component.getLayoutInterval(i);
                int curr = li.getCurrentSpace().size(i);
                int pref = i == HORIZONTAL ? preferred.width : preferred.height;
                resizeInterval(li, curr != pref ? curr : NOT_EXPLICITLY_DEFINED);
            }
        }
    }

    // requires current positions up-to-date
    private void imposeGapsSize(LayoutInterval group, int dimension) {
        for (Iterator it=group.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li.isEmptySpace()) {
                int pref = li.getPreferredSize();
                int min = li.getMinimumSize();
                if (min == USE_PREFERRED_SIZE) {
                    min = pref;
                }
                int max = li.getMaximumSize();
                if (max == USE_PREFERRED_SIZE) {
                    max = pref;
                }
                if (min != pref || max != pref) { // this gap may have different than default size
                    int pad = LayoutDragger.getPreferredPadding(li, dimension);
                    int size = LayoutInterval.getIntervalCurrentSize(li, dimension);
                    if (size > (max != NOT_EXPLICITLY_DEFINED ? max : pad)) {
                        size = max;
                    }
                    else if (size < (min != NOT_EXPLICITLY_DEFINED ? min : pad)) {
                        size = min;
                    }
                    if (size != pref) {
                        resizeInterval(li, size != pad ? size : NOT_EXPLICITLY_DEFINED);
                    }
                }
            }
            else if (li.isGroup()) {
                imposeGapsSize(li, dimension);
            }
        }
    }

    private void imposeCurrentGapSize(LayoutInterval gap, int currentSize, int dimension) {
        int pad = -1;
        int min = gap.getPreferredSize();
        int pref = gap.getPreferredSize();
        if (pref == NOT_EXPLICITLY_DEFINED) {
            pad = LayoutDragger.getPreferredPadding(gap, dimension);
            pref = pad;
        }
        if (currentSize != pref) {
            if (min == NOT_EXPLICITLY_DEFINED) {
                if (pad < 0) {
                    pad = LayoutDragger.getPreferredPadding(gap, dimension);
                }
                min = pad;
            }
            else if (min == USE_PREFERRED_SIZE) {
                min = pref;
            }
            if (currentSize < min) {
                currentSize = min;
            }
            resizeInterval(gap, currentSize == pad ? NOT_EXPLICITLY_DEFINED : currentSize);
        }
    }

    private void propEmptyContainer(LayoutInterval root, int dimension) {
        assert root.getParent() == null && root.getSubIntervalCount() == 0;
        LayoutInterval gap = new LayoutInterval(SINGLE);
        gap.setSizes(0, root.getCurrentSpace().size(dimension), Short.MAX_VALUE);
        layoutModel.addInterval(gap, root, 0);
    }

    // -----

    private boolean addContent(LayoutInterval interval, LayoutInterval target, int index) {
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
                if (LayoutInterval.wantResize(li, false)) { // will span over whole target group
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
                && (LayoutInterval.canResize(interval) || !LayoutInterval.canResize(target) || !LayoutInterval.wantResize(target, false)))
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
     * Computes effective alignment of an interval in its parent. In case of
     * a sequential parent, the effective interval alignment depends on other
     * intervals and their resizability. E.g. if a preceding interval is
     * resizing then the interval is effectivelly "pushed" to the trailing end.
     * If there are no other intervals resizing then the parent alignment is
     * returned. If there are resizing intervals on both sides, or the interval
     * itself is resizing, then the there is no (positive) effective alignment.
     */
    private static int getEffectiveAlignment(LayoutInterval interval) {
        LayoutInterval parent = interval.getParent();
        if (parent.isParallel()) {
            return interval.getAlignment();
        }
        if (LayoutInterval.wantResize(interval, false)) {
            return LayoutRegion.NO_POINT;
        }

        boolean before = true;
        boolean leadingFixed = true;
        boolean trailingFixed = true;
        Iterator it = parent.getSubIntervals();
        do {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li == interval) {
                before = false;
            }
            else if (LayoutInterval.wantResize(li, false)) {
                if (before) {
                    leadingFixed = false;
                }
                else {
                    trailingFixed = false;
                }
            }
        }
        while (it.hasNext());

        if (leadingFixed && !trailingFixed) {
            return LEADING;
        }
        if (!leadingFixed && trailingFixed) {
            return TRAILING;
        }
        if (leadingFixed && trailingFixed) {
            return parent.getAlignment();
        }
        return LayoutRegion.NO_POINT; // !leadingFixed && !trailingFixed
    }

    private static boolean compatibleGroupAlignment(int groupAlign, int align) {
        return groupAlign == align
               || ((groupAlign == LEADING || groupAlign == TRAILING)
                   && (align == LEADING || align == TRAILING));
    }

    static boolean alignedIntervals(LayoutInterval interval1,
                                    LayoutInterval interval2,
                                    int alignment)
    {
        LayoutInterval commonParent;
        LayoutInterval otherInterval;
        if (interval1.isParentOf(interval2)) {
            commonParent = interval1;
            otherInterval = interval2;
        }
        else if (interval2.isParentOf(interval1)) {
            commonParent = interval2;
            otherInterval = interval1;
        }
        else {
            commonParent = interval1.getParent();
            while (commonParent != null) {
                if (!hasAlignmentInParent(interval1, alignment)) {
                    return false;
                }
                if (commonParent.isParentOf(interval2)) {
                    break;
                }
                interval1 = commonParent;
                commonParent = interval1.getParent();
            }
            if (commonParent == null) {
                return false;
            }
            otherInterval = interval2;
        }

        do {
            if (!hasAlignmentInParent(otherInterval, alignment)) {
                return false;
            }
            otherInterval = otherInterval.getParent();
        }
        while (otherInterval != commonParent);
        return true;
    }

    private static boolean hasAlignmentInParent(LayoutInterval interval, int alignment) {
        LayoutInterval parent = interval.getParent();
        if (parent.isSequential()) {
            if (alignment == LEADING) {
                return parent.getSubInterval(0) == interval;
            }
            if (alignment == TRAILING) {
                return parent.getSubInterval(parent.getSubIntervalCount()-1) == interval;
            }
            return false;
        }
        else { // parallel group
            assert interval.getAlignment() != alignment || compatibleGroupAlignment(parent.getGroupAlignment(), alignment);
            return interval.getAlignment() == alignment
                   || LayoutInterval.wantResize(interval, false);
        }
    }

    private static LayoutInterval getAlignSubstitute(LayoutInterval toAlignWith, LayoutInterval commonParParent, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;

        while (toAlignWith != null && LayoutInterval.getFirstParent(toAlignWith, PARALLEL) != commonParParent) {
            if (LayoutInterval.isAlignedAtBorder(toAlignWith, alignment)) {
                toAlignWith = toAlignWith.getParent();
            }
            else return null;
        }
        return toAlignWith;
    }

    // -----

    // recursive
    private void intervalRemoved(LayoutInterval parent, int index, boolean primary, boolean wasResizing, int dimension) {
        dirty = true;
        if (parent.isSequential()) {
            boolean restResizing = LayoutInterval.contentWantResize(parent);

            LayoutInterval leadingGap;
            LayoutInterval leadingNeighbor;
            if (index > 0) {
                LayoutInterval li = parent.getSubInterval(index-1);
                if (li.isEmptySpace()) {
                    leadingGap = li;
                    layoutModel.removeInterval(li);
                    index--;
                    leadingNeighbor = index > 0 ? parent.getSubInterval(index-1) : null;
                }
                else {
                    leadingGap = null;
                    leadingNeighbor = li;
                }
            }
            else {
                leadingGap = null;
                leadingNeighbor = null;
            }

            LayoutInterval trailingGap;
            LayoutInterval trailingNeighbor;
            if (index < parent.getSubIntervalCount()) {
                LayoutInterval li = parent.getSubInterval(index);
                if (li.isEmptySpace()) {
                    trailingGap = li;
                    layoutModel.removeInterval(li);
                    trailingNeighbor = index < parent.getSubIntervalCount() ?
                                       parent.getSubInterval(index) : null;
                }
                else {
                    trailingGap = null;
                    trailingNeighbor = li;
                }
            }
            else {
                trailingGap = null;
                trailingNeighbor = null;
            }

            // [check for last interval (count==1), if parallel superParent try to re-add the interval]
            if (parent.getSubIntervalCount() == 0) { // nothing remained
                LayoutInterval superParent = parent.getParent();
                int idx = layoutModel.removeInterval(parent);
                if (superParent.getParent() != null) {
                    boolean res = LayoutInterval.canResize(parent) && (wasResizing || restResizing);
                    intervalRemoved(superParent, idx, false, res, dimension);
                }
                else if (superParent.getSubIntervalCount() == 0) { // empty root group - add a filling gap
                    propEmptyContainer(superParent, dimension);
                }
            }
            else { // the sequence remains
                if (wasResizing && !restResizing) {
                    if (leadingNeighbor == null && parent.getAlignment() == LEADING) {
                        layoutModel.setIntervalAlignment(parent, TRAILING);
                    }
                    if (trailingNeighbor == null && parent.getAlignment() == TRAILING) {
                        layoutModel.setIntervalAlignment(parent, LEADING);
                    }
                }
                // [what about not to compensate the removed space if there is a resizing interval in the sequence?
                //  i.e. taking into account the effective alignment of the removed interval]
                LayoutInterval superParent = parent.getParent();
                int cutSize = LayoutRegion.distance(
                        (leadingNeighbor != null ? leadingNeighbor : parent).getCurrentSpace(),
                        (trailingNeighbor != null ? trailingNeighbor : parent).getCurrentSpace(),
                        dimension,
                        leadingNeighbor != null ? TRAILING : LEADING,
                        trailingNeighbor != null ? LEADING : TRAILING);
                if ((leadingNeighbor != null && trailingNeighbor != null) // inside a sequence
                    || superParent.getParent() == null // in root parallel group
                    || (leadingNeighbor != null && LayoutInterval.getCount(superParent, TRAILING, true) > 0)
                    || (trailingNeighbor != null && LayoutInterval.getCount(superParent, LEADING, true) > 0))
                {   // create a placeholder gap
                    int min, max;
                    if (wasResizing
                        || (leadingGap != null && leadingGap.getMaximumSize() >= Short.MAX_VALUE)
                        || (trailingGap != null && trailingGap.getMaximumSize() >= Short.MAX_VALUE))
                    {   // the gap should be resizing
                        min = NOT_EXPLICITLY_DEFINED;
                        max = Short.MAX_VALUE;
                    }
                    else {
                        min = max = USE_PREFERRED_SIZE;
                    }
                    LayoutInterval gap = new LayoutInterval(SINGLE);
                    gap.setSizes(min, cutSize, max);
                    layoutModel.addInterval(gap, parent, index);
                }
                else { // this is an "open" end - compensate the size in the parent
                    if (parent.getSubIntervalCount() == 1) {
                        LayoutInterval last = layoutModel.removeInterval(parent, 0);
                        layoutModel.addInterval(last, superParent, layoutModel.removeInterval(parent));
                    }
                    maintainSize(superParent, wasResizing || restResizing, dimension,
                                 parent, parent.getCurrentSpace().size(dimension) - cutSize);
                }

                if (wasResizing && !restResizing) {
                    enableGroupResizing(parent); // in case it was disabled
                }
            }
        }
        else {
            assert parent.isParallel() && parent.getSubIntervalCount() > 0;

            int groupAlign = parent.getGroupAlignment();
            if (primary && (groupAlign == LEADING || groupAlign == TRAILING)) {
                maintainSize(parent, wasResizing, dimension, null, 0);
            }

            if (parent.getSubIntervalCount() == 1 && parent.getParent() != null) { // last interval in parallel group
                // cancel the group and move the interval up
                LayoutInterval remaining = parent.getSubInterval(0);
                layoutModel.removeInterval(remaining);
                layoutModel.setIntervalAlignment(remaining, parent.getAlignment());
                LayoutInterval superParent = parent.getParent();
                int i = layoutModel.removeInterval(parent);
                addContent(remaining, superParent, i);
                if (remaining.isSequential() && superParent.isSequential()) {
                    // eliminate possible directly consecutive gaps
                    // [this could be done by the addContent method directly]
                    eliminateConsecutiveGaps(superParent, i, dimension);
                }
                // [TODO if parallel superParent try to re-add the interval]
            }
            else if (wasResizing && !LayoutInterval.contentWantResize(parent)) {
                enableGroupResizing(parent);
            }
        }
    }
    
    private void eliminateConsecutiveGaps(LayoutInterval group, int index, int dimension) {
        assert group.isSequential();
        if (index > 0)
            index--;
        while (index < group.getSubIntervalCount()-1) {
            LayoutInterval current = group.getSubInterval(index);
            LayoutInterval next = group.getSubInterval(index+1);
            if (current.isEmptySpace() && next.isEmptySpace()) {
                int la;
                LayoutRegion lr;
                if (index > 0) {
                    la = TRAILING;
                    lr = group.getSubInterval(index-1).getCurrentSpace();
                }
                else {
                    la = LEADING;
                    lr = group.getCurrentSpace();
                }
                int ta;
                LayoutRegion tr;
                if (index+2 < group.getSubIntervalCount()) {
                    ta = LEADING;
                    tr = group.getSubInterval(index+2).getCurrentSpace();
                }
                else {
                    ta = TRAILING;
                    tr = group.getCurrentSpace();
                }
                eatGap(current, next, LayoutRegion.distance(lr, tr, dimension, la, ta));
            }
            else index++;
        }
    }


    private void maintainSize(LayoutInterval group, boolean wasResizing, int dimension,
                              LayoutInterval excluded, int excludedSize)
    {
        assert group.isParallel(); // [also not used for center or baseline groups]

        int groupSize = group.getCurrentSpace().size(dimension);
        int[] groupPos = group.getCurrentSpace().positions[dimension];

        boolean leadAlign = false;
        boolean trailAlign = false;
        int leadCompPos = Integer.MAX_VALUE;
        int trailCompPos = Integer.MIN_VALUE;
        int subSize = Integer.MIN_VALUE;
        LayoutInterval offsetGap = null;

        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li.isEmptySpace()) {
                offsetGap = li;
            }
            else  {
                int align = li.getAlignment();
                int l, t; // leading and trailing position of first and last component
                if (li != excluded) {
                    int size = li.getCurrentSpace().size(dimension);
                    if (size >= groupSize) {
                        return;
                    }
                    if (size > subSize) {
                        subSize = size;
                    }
                    l = getOutermostComponent(li, dimension, LEADING).getCurrentSpace().positions[dimension][LEADING];
                    t = getOutermostComponent(li, dimension, TRAILING).getCurrentSpace().positions[dimension][TRAILING];
                }
                else {
                    if (excludedSize > subSize) {
                        subSize = excludedSize;
                    }
                    if (align == LEADING) {
                        l = groupPos[LEADING];
                        t = groupPos[LEADING] + excludedSize;
                    }
                    else {// TRAILING
                        l = groupPos[TRAILING] - excludedSize;
                        t = groupPos[TRAILING];
                    }
                }
                if (l < leadCompPos)
                    leadCompPos = l;
                if (t > trailCompPos)
                    trailCompPos = t;

                if (align == LEADING)
                    leadAlign = true;
                else // TRAILING
                    trailAlign = true;
            }
        }

        if (leadAlign && trailAlign) {
            if (leadCompPos <= groupPos[LEADING] && trailCompPos >= groupPos[TRAILING]) {
                // both edges backed by components, we can use an offset gap
                if (offsetGap == null) {
                    offsetGap = new LayoutInterval(SINGLE);
                    offsetGap.setMinimumSize(0);
                    offsetGap.setPreferredSize(groupPos[TRAILING] - groupPos[LEADING]);
                    layoutModel.addInterval(offsetGap, group, -1);
                }
            }
            else { // there are some ending gaps
                optimizeGaps(group, dimension, false);
            }
        }
        else { // one open edge to compensate
            if (offsetGap != null) { // offset gap not needed anymore
                layoutModel.removeInterval(offsetGap);
                if (LayoutInterval.wantResize(offsetGap, false)) {
                    wasResizing = true;
                }
            }
            if (!LayoutInterval.canResize(group)) { // resizing disabled on the group
                wasResizing = false;
            }
            boolean resizing = LayoutInterval.wantResize(group, false);
            LayoutInterval parent = group.getParent();
            if (parent != null && parent.isParallel()
                && group.getAlignment() == (leadAlign ? LEADING : TRAILING))
            {   // the group can shrink and the parent compensate
                maintainSize(parent, wasResizing && !resizing, dimension, group, subSize);
                if (leadAlign) {
                    groupPos[TRAILING] = trailCompPos;
                }
                if (trailAlign) {
                    groupPos[LEADING] = leadCompPos;
                }
                groupPos[CENTER] = (groupPos[LEADING] + groupPos[LEADING]) / 2;
            }
            else {
                int increment = groupSize - subSize;
                assert increment > 0;
                LayoutInterval gap = new LayoutInterval(SINGLE);
                int min, max;
                if (!resizing && (wasResizing || parent == null)) {
                    min = NOT_EXPLICITLY_DEFINED;
                    max = Short.MAX_VALUE;
                }
                else {
                    min = max = USE_PREFERRED_SIZE;
                }
                gap.setSizes(min, increment, max);

                insertGap(gap, group, leadAlign ? trailCompPos : leadCompPos,
                          dimension, leadAlign ? TRAILING : LEADING);

                if (leadAlign) {
                    groupPos[TRAILING] = trailCompPos;
                }
                if (trailAlign) {
                    groupPos[LEADING] = leadCompPos;
                }
                groupPos[CENTER] = (groupPos[LEADING] + groupPos[LEADING]) / 2;

                if (parent != null) {
                    if (parent.isSequential()) {
                        parent = parent.getParent();
                    }
                    optimizeGaps(parent, dimension, false);
                }
            }
        }
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
    private LayoutInterval insertGap(LayoutInterval gap, LayoutInterval interval, int pos, int dimension, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;
        assert !interval.isSequential();

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

    private void eatGap(LayoutInterval main, LayoutInterval eaten, int currentMergedSize) {
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

    private static void assertSingleGap(LayoutInterval gap) {
        if (gap != null && gap.isEmptySpace() && gap.getParent() != null && gap.getParent().isSequential()) {
            LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(gap, LEADING, false);
            assert neighbor == null || !neighbor.isEmptySpace();
            neighbor = LayoutInterval.getDirectNeighbor(gap, TRAILING, false);
            assert neighbor == null || !neighbor.isEmptySpace();
        }
    }

    // -----
    // auxiliary fields holding temporary objects used frequently

    // converted cursor position used during moving/resizing
    private int[] cursorPos = { 0, 0 };

    // resizability of a component in the designer
    private boolean[][] resizability = { { true, true }, { true, true } };
}
