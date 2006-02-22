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
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.*;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

public class LayoutDesigner implements LayoutConstants {

    private LayoutModel layoutModel;

    private VisualMapper visualMapper;

    private LayoutDragger dragger;

    private LayoutOperations operations;

    private Listener modelListener;

    private boolean imposeSize = true;
    private boolean optimizeStructure = true;
    private boolean visualStateUpToDate;

    // -----

    public LayoutDesigner(LayoutModel model, VisualMapper mapper) {
        layoutModel = model;
        visualMapper = mapper;
        operations = new LayoutOperations(model, mapper);
        modelListener = new Listener();
        modelListener.activate();
    }

    // -------
    // updates of the current visual state stored in the model

    public boolean updateCurrentState() {
        if (logTestCode()) {
            testCode.add("// > UPDATE CURRENT STATE"); //NOI18N
	}
        Object changeMark = layoutModel.getChangeMark();
        boolean changeRequired = imposeSize || optimizeStructure;
        Set updatedContainers = changeRequired ? new HashSet() : null;
        try {
            if (changeRequired) {
                modelListener.deactivate(); // some changes may happen...
                destroyRedundantGroups(updatedContainers);
                operations.mergeAdjacentGaps(updatedContainers);
            }

            updatePositions(updatedContainers);
        } finally {
            if (changeRequired) {
                modelListener.activate();
            }
        }

        if (changeRequired) {
            imposeSize = optimizeStructure = false;

            if (updatedContainers != null) {
                Iterator it = updatedContainers.iterator();
                while (it.hasNext()) {
                    LayoutComponent cont = (LayoutComponent) it.next();
                    visualMapper.rebuildLayout(cont.getId());
                }
                updatePositions(null);
            }
        }

        if (logTestCode()) {
            testCode.add("ld.updateCurrentState();"); //NOI18N
            testCode.add("// < UPDATE CURRENT STATE"); //NOI18N
        }
        
        visualStateUpToDate = true;
        return !changeMark.equals(layoutModel.getChangeMark());
    }

    public void externalSizeChangeHappened() {
        imposeSize = true;
        visualStateUpToDate = false;
        if (logTestCode()) {
            testCode.add("ld.externalSizeChangeHappened();"); // NOI18N
        }
    }

    void requireStructureOptimization() {
        optimizeStructure = true;
        visualStateUpToDate = false;

        Iterator it = layoutModel.getAllComponents();
        while (it.hasNext()) {
            LayoutComponent comp = (LayoutComponent) it.next();
            if (comp.isLayoutContainer()) {
                for (int i=0; i < DIM_COUNT; i++) {
                    cleanDesignAttrs(comp.getLayoutRoot(i));
                }
            }
        }
    }

    private void updatePositions(Set updatedContainers) {
        Iterator it = layoutModel.getAllComponents();
        while (it.hasNext()) {
            LayoutComponent comp = (LayoutComponent) it.next();
            if (!comp.isLayoutContainer())
                continue;

            if (optimizeStructure || imposeSize) {
                // make sure the layout definition reflects the current size
                if (imposeCurrentContainerSize(comp, null, false)) {
                    updatedContainers.add(comp);
                    for (int i=0; i < DIM_COUNT; i++) {
                        LayoutInterval root = comp.getLayoutRoot(i);
                        if (optimizeStructure) {
                            optimizeGaps(root, i, true);
                            destroyRedundantGroups(root);
                        }
                        updateDesignModifications(root, i);
                    }
                }
            }
            else { // just update the current visual positions (LayoutRegion)
                Rectangle bounds = visualMapper.getContainerInterior(comp.getId());
                if (bounds != null) {
                    comp.setCurrentInterior(bounds);
                    Iterator it2 = comp.getSubcomponents();
                    while (it2.hasNext()) {
                        LayoutComponent subComp = (LayoutComponent) it2.next();
                        bounds = visualMapper.getComponentBounds(subComp.getId());
                        int baseline = visualMapper.getBaselinePosition(subComp.getId(), bounds.width, bounds.height);
                        subComp.setCurrentBounds(bounds, baseline);
                    }
                    for (int i=0; i < DIM_COUNT; i++) {
                        updateLayoutStructure(comp.getLayoutRoot(i), i, false);
                    }
                }
            }
        }
    }

    // recursive
    /**
     * Updates current space (LayoutRegion) of all groups in the layout.
     * Also for all intervals having the preferred size explicitly defined the
     * pref size is updated according to the current state. Min and max sizes
     * as well if they are the same as pref.
     */
    void updateLayoutStructure(LayoutInterval interval, int dimension, boolean imposeGaps) {
        LayoutRegion space = interval.getCurrentSpace();
        boolean baseline = interval.getGroupAlignment() == BASELINE;

        boolean first = true;
        boolean firstResizingSpace = false;
        int leadingSpace = 0;
        boolean skipNext = false;

        Iterator it = interval.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval sub = (LayoutInterval) it.next();
            if (sub.isEmptySpace()) {
                if (!interval.isSequential()) {
                    // filling gap in empty root group
                    assert interval.getParent() == null;
                    if (imposeGaps && space.isSet(dimension)) {
                        imposeCurrentGapSize(sub, space.size(dimension), dimension);
                    }
                }
                else if (first || !it.hasNext()) {
                    // first or last gap in sequence
                    int min = sub.getMinimumSize(true);
                    int pref = sub.getPreferredSize(true);
                    int max = sub.getMaximumSize(true);
                    if ((min == pref || min == USE_PREFERRED_SIZE)
                        && (pref == max || max == USE_PREFERRED_SIZE)
                        && pref != NOT_EXPLICITLY_DEFINED) {
                        // Fixed size gap
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
                            LayoutInterval parComp = LayoutUtils.getOutermostComponent(alignedParent, dimension, align);
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
                                LayoutRegion sibSpace = sibling.getCurrentSpace();
                                if (!sibling.isComponent()) {
                                    sibSpace.reset();
                                    updateLayoutStructure(sibling, dimension, imposeGaps);
                                    skipNext = true;
                                }
                                int sibPos = sibSpace.positions[dimension][LEADING];
                                if (sibPos != LayoutRegion.UNKNOWN) { // Issue 62320
                                    currentPref = sibPos - space.positions[dimension][TRAILING];
                                    space.reshape(dimension, TRAILING, currentPref);
                                }
                            }
                        }

                        if (imposeGaps && (currentPref != LayoutRegion.UNKNOWN)) { // last resizing gap
                            imposeCurrentGapSize(sub, currentPref, dimension);
                        }
                    }
                }
                else if (imposeGaps) {
                    LayoutInterval sibling = LayoutInterval.getDirectNeighbor(sub, TRAILING, false);
                    assert !sibling.isEmptySpace();
                    LayoutRegion sibSpace = sibling.getCurrentSpace();
                    if (!sibling.isComponent()) {
                        sibSpace.reset();
                        updateLayoutStructure(sibling, dimension, imposeGaps);
                        skipNext = true;
                    }
                    int currentSize = LayoutRegion.distance(space, sibSpace, dimension, TRAILING, LEADING);
                    imposeCurrentGapSize(sub, currentSize, dimension);
                }
                first = false;
                continue;
            }

            LayoutRegion subSpace = sub.getCurrentSpace();
            if (skipNext) { // this subgroup has been processed in advance
                skipNext = false;
            }
            else if (sub.isGroup()) {
                assert sub.getSubIntervalCount() > 0; // consistency check - there should be no empty groups
                subSpace.reset();
                    /*if (interval.getParent() == null) {
                        subSpace.set(space, dimension); // root space is known
                    }*/
                updateLayoutStructure(sub, dimension, imposeGaps);
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
                if (imposeGaps) {
                    imposeCurrentGapSize(interval.getSubInterval(0), leadingSpace, dimension);
                }
            }
            first = false;
        }
        if (leadingSpace != 0) {
            space.reshape(dimension, LEADING, -leadingSpace);
        }
    }

    public void dumpTestcode(DataObject form) {
        LayoutTestUtils.dumpTestcode(testCode, form, getModelCounter());
        testCode = new ArrayList();
        testCode0 = new ArrayList();
        beforeMove = new ArrayList();
        move1 = new ArrayList();
        move2 = new ArrayList();
        isMoving = false;
    }
    
    // -----
    // adding, moving, resizing

    public void startAdding(LayoutComponent[] comps,
                            Rectangle[] bounds,
                            Point hotspot,
                            String defaultContId)
    {
        if (logTestCode()) {
            testCode.add("// > START ADDING"); //NOI18N
	}
        prepareDragger(comps, bounds, hotspot, LayoutDragger.ALL_EDGES);
        if (logTestCode()) {
            testCode.add("{"); // NOI18N
	    // lc should be already filled in the MetaComponentCreator.getPrecreatedComponent
            LayoutTestUtils.writeLayoutComponentArray(testCode, "comps", "lc");				    //NOI18N
            LayoutTestUtils.writeRectangleArray(testCode, "bounds", bounds);				    //NOI18N
            LayoutTestUtils.writeString(testCode, "defaultContId", defaultContId);			    //NOI18N         
            testCode.add("Point hotspot = new Point(" + new Double(hotspot.getX()).intValue() + "," +	    //NOI18N
			    new Double(hotspot.getY()).intValue() + ");");				    //NOI18N
            testCode.add("ld.startAdding(comps, bounds, hotspot, defaultContId);");			    //NOI18N
            testCode.add("}");										    //NOI18N
        }
        if (defaultContId != null)
            dragger.setTargetContainer(layoutModel.getLayoutComponent(defaultContId));
        if (logTestCode()) {
            testCode.add("// < START ADDING"); //NOI18N
	}
    }
    
    public void startMoving(String[] compIds, Rectangle[] bounds, Point hotspot) {
        if (logTestCode()) {
            testCode.add("// > START MOVING"); //NOI18N
        }

        LayoutComponent[] comps = new LayoutComponent[compIds.length];
        for (int i=0; i < compIds.length; i++) {
            comps[i] = layoutModel.getLayoutComponent(compIds[i]);
        }
        prepareDragger(comps, bounds, hotspot, LayoutDragger.ALL_EDGES);

        if (logTestCode()) {
            testCode.add("{"); //NOI18N
            LayoutTestUtils.writeStringArray(testCode, "compIds", compIds); //NOI18N
            LayoutTestUtils.writeRectangleArray(testCode, "bounds", bounds); //NOI18N
            testCode.add("Point hotspot = new Point(" + new Double(hotspot.getX()).intValue() + "," +  //NOI18N
		    new Double(hotspot.getY()).intValue() + ");"); //NOI18N
            testCode.add("ld.startMoving(compIds, bounds, hotspot);"); //NOI18N
            testCode.add("}"); //NOI18N
        }
        
        dragger.setTargetContainer(comps[0].getParent());

        if (logTestCode()) {
            testCode.add("// < START MOVING"); //NOI18N
	}
    }

    // [change to one component only?]
    public void startResizing(String[] compIds,
                              Rectangle[] bounds,
                              Point hotspot,
                              int[] resizeEdges,
                              boolean inLayout)
    {
        if (logTestCode()) {
            testCode.add("// > START RESIZING"); //NOI18N
        }

        LayoutComponent[] comps = new LayoutComponent[compIds.length];
        for (int i=0; i < compIds.length; i++) {
            comps[i] = layoutModel.getLayoutComponent(compIds[i]);
        }
        
        int[] edges = new int[DIM_COUNT];
        for (int i=0; i < DIM_COUNT; i++) {
            edges[i] = resizeEdges[i] == LEADING || resizeEdges[i] == TRAILING ?
                       resizeEdges[i] : LayoutRegion.NO_POINT;
        }

        prepareDragger(comps, bounds, hotspot, edges);

        if (logTestCode()) {
            testCode.add("{"); //NOI18N
            LayoutTestUtils.writeStringArray(testCode, "compIds", compIds); //NOI18N
            LayoutTestUtils.writeRectangleArray(testCode, "bounds", bounds); //NOI18N
            testCode.add("Point hotspot = new Point(" + new Double(hotspot.getX()).intValue() + "," +  //NOI18N
		    new Double(hotspot.getY()).intValue() + ");"); //NOI18N
            LayoutTestUtils.writeIntArray(testCode, "resizeEdges", resizeEdges); //NOI18N
            testCode.add("boolean inLayout = " + inLayout + ";"); // NOI18N
            testCode.add("ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);"); //NOI18N
            testCode.add("}"); //NOI18N
        }

        dragger.setTargetContainer(inLayout ? comps[0].getParent() : null);

        if (logTestCode()) {
            testCode.add("// < START RESIZING"); //NOI18N
	}
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
            int baseline = visualMapper.getBaselinePosition(comps[i].getId(), bounds[i].width, bounds[i].height);
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
	
	int x = (p != null) ? p.x : 0;
	int y = (p != null) ? p.y : 0;

	if (logTestCode()) {
            // this terrible code here is to store only two last move() calls
            if (!isMoving) {
                isMoving = true;
                // backup all current entries and clear the testcode list
                beforeMove = new ArrayList();
                beforeMove.addAll(testCode);
                testCode = new ArrayList();
		lastMovePoint = new Point(0,0);
            }

	    if (!((x == lastMovePoint.x) && (y == lastMovePoint.y))) {
		lastMovePoint = new Point(x, y);
                move1 = move2;
		testCode0 = testCode;
	    }

            move2 = new ArrayList();
            move2.add("// > MOVE");
            testCode = new ArrayList();
        }
        if ((!visualStateUpToDate) || (dragger == null)) {
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

        if (logTestCode()) {
            move2.add("{"); //NOI18N
            move2.add("Point p = new Point(" + x + "," + y + ");"); //NOI18N
            LayoutTestUtils.writeString(move2, "containerId", containerId); //NOI18N
            move2.add("boolean autoPositioning = " + autoPositioning + ";"); //NOI18N
            move2.add("boolean lockDimension = " + lockDimension + ";"); //NOI18N
            LayoutTestUtils.writeRectangleArray(move2, "bounds", bounds); //NOI18N
            move2.add("ld.move(p, containerId, autoPositioning, lockDimension, bounds);"); //NOI18N
            move2.add("}"); //NOI18N
            move2.add("// < MOVE"); //NOI18N
        }
    }

    public void endMoving(boolean committed) {
        if (!committed && dragger == null)
            return; // redundant call

        if (logTestCode()) {
            if (committed) {
                beforeMove.addAll(testCode0);
                beforeMove.addAll(move1);
                beforeMove.addAll(testCode);
                beforeMove.addAll(move2);
                testCode = beforeMove;
            }
            testCode.add("// > END MOVING"); //NOI18N
            isMoving = false;
        }
        try {
            if (committed) {
                LayoutComponent[] components = dragger.getMovingComponents();
                LayoutComponent targetContainer = dragger.getTargetContainer();

                if (targetContainer != null) {
                    boolean newComponent = components[0].getParent() == null;
                    // Determine intervals that should be added
                    LayoutInterval[] addingInts = new LayoutInterval[DIM_COUNT];
                    LayoutRegion origSpace = null;
                    for (int dim=0; dim < DIM_COUNT; dim++) {
                        if (components.length > 1) {
                            if (newComponent) {
                                // Moving several components from old layout
                                for (int i=0; i<components.length; i++) {
                                    layoutModel.addComponent(components[i], targetContainer, -1);
                                }
                                addingInts = layoutModel.createIntervalsFromBounds(dragger.getMovingSpace(), components, dragger.getMovingBounds());
                                break;
                            } else {
                                if (origSpace == null) {
                                    origSpace = new LayoutRegion();
                                    // Calculate original space
                                    for (int i=0; i < components.length; i++) {
                                        origSpace.expand(components[i].getLayoutInterval(0).getCurrentSpace());
                                    }
                                }
                                LayoutInterval[] children = new LayoutInterval[components.length];
                                for (int i=0; i<components.length; i++) {
                                    children[i] = components[i].getLayoutInterval(dim);
                                }
                                LayoutInterval parent = LayoutInterval.getCommonParent(children);
                                // Restriction of the layout model of the common parent
                                // in the original layout (this also removes the original intervals)
                                addingInts[dim] = restrictedCopy(parent, components, origSpace, dim, null);
                            }
                        } else {
                            addingInts[dim] = components[0].getLayoutInterval(dim);
                            if (newComponent) { // Ensure correct size when the component comes from old layout
                                Dimension preferred = visualMapper.getComponentPreferredSize(components[0].getId());
                                int size = dragger.getMovingBounds()[0].size(dim);
                                if (size != ((dim == HORIZONTAL) ? preferred.width : preferred.height)) {
                                    LayoutInterval intr = addingInts[dim];
                                    layoutModel.setIntervalSize(intr, intr.getMinimumSize(), size, intr.getMaximumSize());
                                }
                            }
                        }
                    }

                    LayoutFeeder layoutFeeder = new LayoutFeeder(operations, dragger, addingInts);

                    // Remove components from original location
                    for (int i=0; i<components.length; i++) {
                        if (components[i].getParent() != null) {
                            LayoutComponent comp = components[i];
                            if (components[i].getParent() != null) {
                                if (dragger.isResizing(HORIZONTAL)) {
                                    layoutModel.removeComponentFromLinkSizedGroup(components[i], HORIZONTAL);
                                }
                                if (dragger.isResizing(VERTICAL)) {
                                    layoutModel.removeComponentFromLinkSizedGroup(components[i], VERTICAL);
                                }
                            }
                            if (components.length == 1) { // remove also the intervals
                                layoutModel.removeComponentAndIntervals(comp, false);
                            }
                            else { // Don't remove layout intervals of components if
                                // moving multiple components - the intervals are placed
                                // in the adding group already (by restrictedCopy)
                                layoutModel.removeComponent(comp, false);
                            }
                        }
                    }

                    modelListener.deactivate(); // from now do not react on model changes

                    // Add components to the target container
                    for (int i=0; i<components.length; i++) {
                        layoutModel.addComponent(components[i], targetContainer, -1);
                    }

                    // add the intervals
                    layoutFeeder.add();
                    if (layoutFeeder.imposeSize)
                        imposeSize = true;
                    if (layoutFeeder.optimizeStructure)
                        optimizeStructure = true;
                    // if an overlap occurred we can't calculate the correct sizes
                    // of resizing intervals, thus need to do real layout first (to
                    // get the right picture), then update the actual sizes, and then
                    // re-layout with design specific attributes (container resizing gap)

                    for (int dim=0; dim < DIM_COUNT; dim++) {
                        destroyGroupIfRedundant(addingInts[dim], addingInts[dim].getParent());
                    }

                    if (components[0].isLayoutContainer() && (dragger.isResizing() || newComponent)) {
                        // container size needs to be defined from inside in advance
                        imposeCurrentContainerSize(components[0], dragger.getSizes(), true);
                    }
                    else if (dragger.isResizing() && !components[0].isLayoutContainer()) {
                        // component might be resized to default size
                        for (int i=0; i < DIM_COUNT; i++) {
                            if (dragger.snappedToDefaultSize(i))
                               operations.resizeInterval(components[0].getLayoutInterval(i), NOT_EXPLICITLY_DEFINED);
                        }
                    }

                    updateDesignModifications(targetContainer);
                }
                else { // resizing root container
                    assert dragger.isResizing();

                    modelListener.deactivate(); // do not react on model changes

                    LayoutRegion space = dragger.getMovingBounds()[0];
                    for (int dim=0; dim < DIM_COUNT; dim++) {
                        components[0].getLayoutInterval(dim).setCurrentSpace(space);
                    }
                    if (components[0].isLayoutContainer()) {
                        imposeCurrentContainerSize(components[0], dragger.getSizes(), true);
                    }
                }

                if (dragger.isResizing() && components[0].isLayoutContainer())
                    updateDesignModifications(components[0]);

                visualStateUpToDate = false;
            }
        } finally {
            modelListener.activate();
            dragger = null;
            if (logTestCode()) {
                testCode.add("ld.endMoving(" + committed + ");"); //NOI18N
                testCode.add("// < END MOVING"); //NOI18N
            }
        }
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
                    // Consolidate the groups where the components has been added
                    boolean active = modelListener.isActive();
                    if (active) modelListener.deactivate();
                    iter = temp.iterator();
                    while (iter.hasNext()) {
                        iter.next(); // skip the component
                        LayoutInterval group = (LayoutInterval)iter.next();
                        iter.next(); iter.next(); // skip alignment and index
                        while (group.getSubIntervalCount() == 1) {
                            LayoutInterval sub = group.getSubInterval(0);
                            LayoutInterval parent = group.getParent();
                            layoutModel.removeInterval(sub);
                            int alignment = group.getAlignment();
                            int index = layoutModel.removeInterval(group);
                            layoutModel.setIntervalAlignment(sub, alignment);
                            layoutModel.addInterval(sub, parent, index);
                            group = sub;
                        }
                    }
                    compCount = 0;
                    if (active) modelListener.activate();
                }
                // consolidate copy
                if ((copy.getSubIntervalCount() == 1) && (compCount == 0)) {
                    boolean active = modelListener.isActive();
                    if (active) modelListener.deactivate();
                    LayoutInterval subCopy = copy.getSubInterval(0);
                    layoutModel.removeInterval(subCopy);
                    layoutModel.setIntervalAlignment(subCopy, copy.getAlignment());
                    if (copy.isSequential() && subCopy.isEmptySpace()) {
                        copy = null;
                    } else {
                        copy = subCopy;
                    }
                    if (active) modelListener.activate();
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
        while ((seqGroup.getSubIntervalCount() > boundary)
            && seqGroup.getSubInterval(seqGroup.getSubIntervalCount()-1).isEmptySpace()) {
            layoutModel.removeInterval(seqGroup.getSubInterval(seqGroup.getSubIntervalCount()-1));
        }
        if (size > 0) {
            LayoutInterval gap = new LayoutInterval(SINGLE);
            gap.setSize(size);
            layoutModel.addInterval(gap, seqGroup, -1);
        }
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

    /**
     * Removes currently dragged components from layout model. Called when
     * the components were dragged out of the form (or to a container not
     * managed by this layout model).
     */
    public void removeDraggedComponents() {
        if (dragger != null) {
            LayoutComponent[] components = dragger.getMovingComponents();
            for (int i=0; i < components.length; i++) {
                layoutModel.removeComponentAndIntervals(components[i], !components[i].isLayoutContainer());
            }
            endMoving(false);
        }
    }

    public void paintMoveFeedback(Graphics2D g) {
        if (dragger != null) { // Dragger might not be initialized yet
            dragger.paintMoveFeedback(g);
        }
    }
    
    /**
     * Paints layout information (alignment) for the selected component.
     *
     * @param g graphics object to use.
     * @param componentId ID of selected component.
     */
    public void paintSelection(Graphics2D g, String componentId) {
        LayoutComponent comp = layoutModel.getLayoutComponent(componentId);
        if ((comp != null) && (comp.getParent() != null)) {
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

    private void paintLinks(Graphics2D g, LayoutComponent component) {
        
        if ((component.isLinkSized(HORIZONTAL)) && (component.isLinkSized(VERTICAL))) {
            Map linkGroupsH = layoutModel.getLinkSizeGroups(HORIZONTAL);            
            Map linkGroupsV = layoutModel.getLinkSizeGroups(VERTICAL);
            Integer linkIdH = new Integer(component.getLinkSizeId(HORIZONTAL));
            Integer linkIdV = new Integer(component.getLinkSizeId(VERTICAL));
            
            List lH = (List)linkGroupsH.get(linkIdH);
            List lV = (List)linkGroupsV.get(linkIdV);

            Set merged = new HashSet(); 
            for (int i=0; i < lH.size(); i++) {
                merged.add(lH.get(i));
            }
            for (int i=0; i < lV.size(); i++) {
                merged.add(lV.get(i));
            }

            Iterator mergedIt = merged.iterator();
            while (mergedIt.hasNext()) {
                String id = (String)mergedIt.next();
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
    
    private Image linkBadgeBoth = null;
    private Image linkBadgeHorizontal = null;
    private Image linkBadgeVertical = null;
    
    private static final int BOTH_DIMENSIONS = 2;
            
    private Image getLinkBadge(int dimension) {
        if (dimension == (BOTH_DIMENSIONS)) {
            if (linkBadgeBoth == null) {
                linkBadgeBoth = Utilities.loadImage("org/netbeans/modules/form/resources/sameboth.png"); //NOI18N
            }
            return linkBadgeBoth;
        }
        if (dimension == HORIZONTAL) {
            if (linkBadgeHorizontal == null) {
                linkBadgeHorizontal = Utilities.loadImage("org/netbeans/modules/form/resources/samewidth.png"); //NOI18N
            }
            return linkBadgeHorizontal;
        }
        if (dimension == VERTICAL) {
            if (linkBadgeVertical == null) {
                linkBadgeVertical = Utilities.loadImage("org/netbeans/modules/form/resources/sameheight.png"); //NOI18N
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
    // LayoutModel.Listener implementation & related

    class Listener implements LayoutModel.Listener {
        private boolean active = false;
        
        public void layoutChanged(LayoutEvent ev) {
            if (!layoutModel.isUndoRedoInProgress()) {
                deactivate();
                LayoutDesigner.this.layoutChanged(ev);
                activate();
            }
        }
        void activate() {
            layoutModel.addListener(this);
            active = true;
        }
        void deactivate() {
            layoutModel.removeListener(this);
            active = false;
        }
        
        boolean isActive() {
            return active;
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
                                LayoutInterval.wantResize(ev.getInterval()),
                                dim);
                if (comp.getParent() != null) {
                    updateDesignModifications(comp.getParent().getLayoutRoot(dim), dim);
                    // [maybe set imposeGaps or optimizeStructure to true if can't
                    //  ensure that removing handles gaps properly]
                    visualStateUpToDate = false;
                }
            }
        }
    }

    // -----

    private boolean isComponentResizable(LayoutComponent comp, int dimension) {
        boolean[] res = comp.getResizability();
        if (res == null) {
            res = visualMapper.getComponentResizability(comp.getId(), new boolean[DIM_COUNT]);
            comp.setResizability(res);
        }
        return res[dimension];
    }

    /**
     * Changes global alignment of the layout component.
     *
     * @param comp component whose alignment should be changed.
     * @param dimension dimension the alignment should be applied in.
     * @param alignment desired alignment.
     */
    public void adjustComponentAlignment(LayoutComponent comp, int dimension, int alignment) {
        if (logTestCode()) {
            testCode.add("// > ADJUST COMPONENT ALIGNMENT"); //NOI18N
            testCode.add("{"); //NOI18N
            testCode.add("LayoutComponent comp = model.getLayoutComponent(\"" + comp.getId() + "\");"); //NOI18N
            testCode.add("int dimension = " + dimension);	    //NOI18N
            testCode.add("int alignment = " + alignment);          //NOI18N 
            testCode.add("ld.adjustComponentAlignment(comp, dimension, alignment);"); //NOI18N
            testCode.add("}"); //NOI18N
        }
        modelListener.deactivate();
        LayoutInterval interval = comp.getLayoutInterval(dimension);
        
        // Skip non-resizable groups
        LayoutInterval parent = interval.getParent();
        while (parent != null) {
            if (!LayoutInterval.canResize(parent)) {
                interval = parent;
            }
            parent = parent.getParent();
        }
        assert !LayoutInterval.wantResize(interval);
        
        boolean changed = false;
        parent = interval.getParent();
        while (parent != null) {
            if (parent.isParallel()) {
                if (LayoutInterval.wantResize(parent) && !LayoutInterval.wantResize(interval)) {
                    int alg = interval.getAlignment();
                    if (alg != alignment) {
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
                    } else if (LayoutInterval.wantResize(li)) {
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
                        setIntervalResizing(gap, true);
                        layoutModel.setIntervalSize(gap, 0, 0, gap.getMaximumSize());
                        layoutModel.addInterval(gap, parent, index);
                    }
                    changed = true;
                }
            }
            interval = parent;
            parent = parent.getParent();
        }
        updateDesignModifications(interval, dimension);
        modelListener.activate();
        visualStateUpToDate = false;
        if (logTestCode()) {
            testCode.add("// < ADJUST COMPONENT ALIGNMENT"); //NOI18N
	}
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
        
        if (LayoutInterval.wantResize(interval)) {
            leadingFixed = trailingFixed = leadingAdjustable = trailingAdjustable = false;
        }
        LayoutInterval parent = interval.getParent();
        while (parent != null) {
            if (!LayoutInterval.canResize(parent)) {
                leadingFixed = trailingFixed = leadingAdjustable = trailingAdjustable = true;
            } else if (parent.isParallel()) {
                if (LayoutInterval.wantResize(parent) && !LayoutInterval.wantResize(interval)) {
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
                    } else if (LayoutInterval.wantResize(li)) {
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
        return fill ? false : LayoutInterval.wantResizeInLayout(interval);
    }

    /**
     * Returns preferred size of the given interval (in pixels).
     *
     * @param interval interval whose preferred size should be determined.
     * @return preferred size of the given interval.
     */
    private int prefSizeOfInterval(LayoutInterval interval) {
        int dimension = -1;
        if (interval.isComponent()) {
            LayoutComponent comp = interval.getComponent();
            dimension = (interval == comp.getLayoutInterval(HORIZONTAL)) ? HORIZONTAL : VERTICAL;
            if (comp.isLinkSized(dimension)) {
                Collection linked = (Collection)layoutModel.getLinkSizeGroups(dimension).get(new Integer(comp.getLinkSizeId(dimension)));
                Iterator iter = linked.iterator();
                int prefSize = 0;
                while (iter.hasNext()) {
                    String compId = (String)iter.next();
                    LayoutComponent component = layoutModel.getLayoutComponent(compId);
                    LayoutInterval intr = component.getLayoutInterval(dimension);
                    int pref = intr.getPreferredSize();
                    if (pref == NOT_EXPLICITLY_DEFINED) {
                        Dimension prefDim = visualMapper.getComponentPreferredSize(compId);
                        pref = (dimension == HORIZONTAL) ? prefDim.width : prefDim.height;
                    }
                    prefSize = Math.max(pref, prefSize);
                }
                return prefSize;
            }
        }
        int prefSize = interval.getPreferredSize();
        if (prefSize == NOT_EXPLICITLY_DEFINED) {
            if (interval.isComponent()) {
                LayoutComponent comp = interval.getComponent();
                Dimension pref = visualMapper.getComponentPreferredSize(comp.getId());
                return (dimension == HORIZONTAL) ? pref.width : pref.height;
            } else if (interval.isEmptySpace()) {
                return sizeOfEmptySpace(interval);
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
     * Returns size of the empty space represented by the given layout interval.
     *
     * @param interval layout interval that represents padding.
     * @return size of the padding.
     */
    private int sizeOfEmptySpace(LayoutInterval interval) {
        return LayoutUtils.getSizeOfDefaultGap(interval, visualMapper);
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
        if (logTestCode()) {
            testCode.add("// > SET COMPONENT RESIZING"); //NOI18N
            testCode.add("{"); //NOI18N
            testCode.add("LayoutComponent comp = lm.getLayoutComponent(\"" + comp.getId() + "\");"); //NOI18N
            testCode.add("int dimension = " + dimension + ";"); //NOI18N
            testCode.add("boolean resizing = " + resizing + ";"); //NOI18N   
            testCode.add("ld.setComponentResizing(comp, dimension, resizing);"); //NOI18N
            testCode.add("}"); //NOI18N
        }
        modelListener.deactivate();
        LayoutInterval interval = comp.getLayoutInterval(dimension);
        
        // Unset the same-size if we are making the component resizable
        if (resizing && comp.isLinkSized(dimension)) {
            Collection linked = (Collection)layoutModel.getLinkSizeGroups(dimension).get(new Integer(comp.getLinkSizeId(dimension)));
            Collection toChange;
            if (linked.size() == 2) { // The second component will be unlinked, too.
                toChange = linked;
            } else {
                toChange = Collections.singletonList(comp.getId());
            }
            Iterator iter = toChange.iterator();
            while (iter.hasNext()) {
                String compId = (String)iter.next();
                LayoutComponent component = layoutModel.getLayoutComponent(compId);
                LayoutInterval intr = component.getLayoutInterval(dimension);
                Dimension prefDim = visualMapper.getComponentPreferredSize(compId);
                int prefSize = (dimension == HORIZONTAL) ? prefDim.width : prefDim.height;
                int currSize = intr.getCurrentSpace().size(dimension);
                if (currSize == prefSize) {
                    currSize = NOT_EXPLICITLY_DEFINED;
                }
                layoutModel.setIntervalSize(intr, intr.getMinimumSize(), currSize, intr.getMaximumSize());
            }
        }
        
        LayoutInterval parent = interval.getParent();
        boolean fill = interval.hasAttribute(LayoutInterval.ATTRIBUTE_FILL);
        boolean formerFill = interval.hasAttribute(LayoutInterval.ATTRIBUTE_FORMER_FILL);
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
        LayoutInterval intr = interval;
        LayoutInterval par = parent;
        while (par != null) {
            if (par.isParallel() && resizing) {
                int groupCurrSize = LayoutInterval.getIntervalCurrentSize(par, dimension);
                int currSize = LayoutInterval.getIntervalCurrentSize(intr, dimension);
                // PENDING currSize could change if groupPrefSize != groupCurrSize
                if (groupCurrSize != currSize) {
                    LayoutInterval seqGroup = intr;
                    LayoutInterval space = new LayoutInterval(SINGLE);
                    space.setSize(groupCurrSize - currSize);
                    int alignment = intr.getAlignment();
                    int index = (alignment == LEADING) ? -1 : 0;
                    if (intr.isSequential()) {
                        int spaceIndex = (alignment == LEADING) ? intr.getSubIntervalCount()-1 : 0;
                        LayoutInterval adjacentSpace = intr.getSubInterval(spaceIndex);
                        if (adjacentSpace.isEmptySpace()) {
                            int spaceSize = LayoutInterval.getIntervalCurrentSize(adjacentSpace, dimension);
                            layoutModel.removeInterval(adjacentSpace);
                            space.setSize(groupCurrSize - currSize + spaceSize);
                        }
                    } else {
                        seqGroup = new LayoutInterval(SEQUENTIAL);
                        layoutModel.setIntervalAlignment(intr, DEFAULT);
                        seqGroup.setAlignment(alignment);
                        int i = layoutModel.removeInterval(intr);
                        layoutModel.addInterval(intr, seqGroup, -1);
                        layoutModel.addInterval(seqGroup, par, i);
                    }
                    layoutModel.addInterval(space, seqGroup, index);
                    seqGroup.getCurrentSpace().set(dimension, par.getCurrentSpace());
                }
            } else if (par.isSequential()) {
                // Change resizability of gaps
                boolean parentSeq = (parent == par);
                List resizableList = new LinkedList();
                int alignment = parentSeq ? LayoutInterval.getEffectiveAlignment(interval) : 0;
                LayoutInterval leadingGap = null;
                LayoutInterval trailingGap = null;
                boolean afterDefining = false;
                Iterator iter = par.getSubIntervals();
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
                        } else if (parentSeq) {
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
                    delta = (LayoutInterval.getIntervalCurrentSize(par, dimension) - prefSizeOfInterval(par) + delta)/resizableList.size();
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
                if (parentSeq) {
                    if (!LayoutInterval.wantResize(par)) {
                        LayoutInterval gap = null;
                        if ((alignment == TRAILING) && (leadingGap != null)) {
                            gap = leadingGap;
                            setIntervalResizing(leadingGap, !resizing);
                            layoutModel.changeIntervalAttribute(leadingGap, LayoutInterval.ATTRIBUTE_FILL, true);
                        }
                        if ((alignment == LEADING) && (trailingGap != null)) {
                            gap = trailingGap;
                            setIntervalResizing(trailingGap, !resizing);
                            layoutModel.changeIntervalAttribute(trailingGap, LayoutInterval.ATTRIBUTE_FILL, true);
                        }
                        if ((gap != null) && (delta != 0) && (gap.getPreferredSize() != NOT_EXPLICITLY_DEFINED)) {
                            layoutModel.setIntervalSize(gap, gap.getMinimumSize(), 
                                Math.max(0, gap.getPreferredSize() - delta), gap.getMaximumSize());
                        }
                    }
                    parent = par.getParent(); // use parallel parent for group resizing check
                }
            }
            intr = par;
            par = par.getParent();
        }
        
        // Unset the same size once all changes in gap sizes are done
        if (resizing) {
            layoutModel.unsetSameSize(Collections.singletonList(comp.getId()), dimension);
        }
        modelListener.activate();

        if (resizing) {
            // cancel possible suppressed resizing
            while (parent != null) {
                if (!LayoutInterval.canResize(parent)) {
                    operations.enableGroupResizing(parent);
                }
                parent = parent.getParent();
            }
        } else {
            // check if we should suppress resizing
            while (parent != null) {
                if (fillResizable(parent)) {
                    operations.suppressGroupResizing(parent);
                    parent = parent.getParent();
                } else {
                    break;
                }
            }
        }

        updateDesignModifications(comp.getParent());
        visualStateUpToDate = false;
        if (logTestCode()) {
	    testCode.add("// < SET COMPONENT RESIZING"); //NOI18N
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
                if (LayoutInterval.wantResize(li) && !fillResizable(li)) {
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
        if (logTestCode()) {
            testCode.add("// > ALIGN"); //NOI18N
            testCode.add("{"); //NOI18N
	    LayoutTestUtils.writeCollection(testCode, "componentIds", componentIds); //NOI18N
            testCode.add("boolean closed = " + closed + ";"); //NOI18N
            testCode.add("int dimension = " + dimension + ";");        //NOI18N   
            testCode.add("int alignment = " + alignment + ";");         //NOI18N  
            testCode.add("ld.align(componentIds, closed, dimension, alignment);"); //NOI18N
            testCode.add("}"); //NOI18N
        }
        LayoutInterval[] intervals = new LayoutInterval[componentIds.size()];
        int counter = 0;
        Iterator iter = componentIds.iterator();        
        while (iter.hasNext()) {
            String id = (String)iter.next();
            LayoutComponent component = layoutModel.getLayoutComponent(id);
            intervals[counter++] = component.getLayoutInterval(dimension);            
        }
        modelListener.deactivate();
        try {
            new LayoutAligner(this, layoutModel, operations).alignIntervals(intervals, closed, dimension, alignment);
        } finally {
            modelListener.activate();
        }
        requireStructureOptimization();
        if (logTestCode()) {
	    testCode.add("// < ALIGN"); //NOI18N
	}
    }

    private void destroyRedundantGroups(Set updatedContainers) {
        Iterator it = layoutModel.getAllComponents();
        while (it.hasNext()) {
            LayoutComponent comp = (LayoutComponent) it.next();
            if (!comp.isLayoutContainer())
                continue;
            
            boolean updated = false;
            for (int dim=0; dim<DIM_COUNT; dim++) {
                LayoutInterval interval = comp.getLayoutRoot(dim);
                updated = updated || destroyRedundantGroups(interval);
            }
            if (updated) {
                updatedContainers.add(comp);
            }
        }
    }
    
    private boolean destroyRedundantGroups(LayoutInterval interval) {
        boolean updated = false;
        for (int i=interval.getSubIntervalCount()-1; i>=0; i--) {
            if (i >= interval.getSubIntervalCount()) continue;
            LayoutInterval subInterval = interval.getSubInterval(i);
            if (subInterval.isGroup()) {
                destroyRedundantGroups(subInterval);
                destroyGroupIfRedundant(subInterval, interval);
                updated |= (subInterval.getParent() == null);
            }
        }
        return updated;
    }

    /**
     * Destroys the given group if it is redundant in the layout model.
     *
     * @param group group whose necessity should be checked.
     * @param boundary parent of the group that limits the changes that
     * should be made e.g. no changes outside of this group even if it were
     * itself redundant. Can be <code>null</code> if there's no boundary.
     */
    void destroyGroupIfRedundant(LayoutInterval group, LayoutInterval boundary) {
        if ((group == null) || (!group.isGroup()) || (group == boundary)) return;
        LayoutInterval parent = group.getParent();
        // Don't destroy root intervals
        if (parent == null) return;

        // Remove empty groups
        if (group.getSubIntervalCount() == 0) {
            takeOutInterval(group, boundary);
            return;
        }

        if (operations.dissolveRedundantGroup(group)) {
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
    void takeOutInterval(LayoutInterval interval, LayoutInterval boundary) {
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
                    int alignment = LayoutInterval.getEffectiveAlignment(interval);
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

    // -----

    // [should change to operations.addGroupContent]
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
     * @param dimension dimension the remainder group is created in.
     */
    void createRemainderGroup(List list, LayoutInterval seq,
                                      int index, int position, int mainAlignment, int dimension)
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
            operations.insertGapIntoSequence(gap, seq, index, dimension);
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

    static boolean isFixedPadding(LayoutInterval interval) {
        return interval.isEmptySpace()
               && (interval.getMinimumSize() == NOT_EXPLICITLY_DEFINED || interval.getMinimumSize() == USE_PREFERRED_SIZE)
               && interval.getPreferredSize() == NOT_EXPLICITLY_DEFINED
               && (interval.getMaximumSize() == NOT_EXPLICITLY_DEFINED || interval.getMaximumSize() == USE_PREFERRED_SIZE);
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
                    // [removing container supporting gap]
                    for (int i=group.getSubIntervalCount()-1; i >= 0; i--) {
                        if (group.getSubInterval(i).isEmptySpace()) {
                            layoutModel.removeInterval(group, i);
                            break;
                        }
                    }
                }
                else if (group.getSubIntervalCount() == 0) {
                    // [sort of hack - would be nice the filling gap is ensured somewhere else]
                    propEmptyContainer(group, dimension);
                }
            } else { // [dissolving one-member group should not be here]
                assert (nonEmptyCount == 1);
                assert (group.getSubIntervalCount() == 1);
                LayoutInterval interval = group.getSubInterval(0);
                //int alignment = interval.getAlignment();
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
/*                if (offsetGap != null) {
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
                } */
            }
            return -1;
        }

        return operations.optimizeGaps(group, dimension);
    }

    // -----

    /**
     * Sets interval resizability. Makes it resizing or fixed.
     *
     * @param interval layout interval whose resizability should be set.
     * @param resizable determines whether the passed interval should be made resizable.
     */
    void setIntervalResizing(LayoutInterval interval, boolean resizable) {
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

    // -----

    public void setDefaultSize(String compId) {
        if (logTestCode()) {
            testCode.add("// > SET DEFAULT SIZE"); //NOI18N
            testCode.add("{"); //NOI18N
            testCode.add("String compId = \"${" + compId + "}\";"); //NOI18N
            testCode.add("ld.setDefaultSize(compId);"); //NOI18N
            testCode.add("}"); //NOI18N
        }        
        LayoutComponent component = layoutModel.getLayoutComponent(compId);
        if (component != null)
            setDefaultSize(component);
        if (logTestCode()) {
            testCode.add("// < SET DEFAULT SIZE"); //NOI18N
	}
    }

    private void setDefaultSize(LayoutComponent component) {
        imposeSize = true;
        if (component.isLayoutContainer()) {
            for (Iterator it=component.getSubcomponents(); it.hasNext(); ) {
                LayoutComponent comp = (LayoutComponent) it.next();
                if (comp.isLayoutContainer())
                    setDefaultSize(comp);
            }
            setDefaultSizeInContainer(component.getLayoutRoot(HORIZONTAL));
            setDefaultSizeInContainer(component.getLayoutRoot(VERTICAL));
            updateDesignModifications(component);
        }
        else {
            operations.resizeInterval(component.getLayoutInterval(HORIZONTAL), NOT_EXPLICITLY_DEFINED);
            operations.resizeInterval(component.getLayoutInterval(VERTICAL), NOT_EXPLICITLY_DEFINED);
        }
    }

    private void setDefaultSizeInContainer(LayoutInterval interval) {
        if (!interval.isGroup()) {
            if (LayoutInterval.canResize(interval))
                operations.resizeInterval(interval,
                        interval.getMinimumSize() != USE_PREFERRED_SIZE ? interval.getMinimumSize() : NOT_EXPLICITLY_DEFINED);
        }
        else {
            for (Iterator it=interval.getSubIntervals(); it.hasNext(); ) {
                setDefaultSizeInContainer((LayoutInterval)it.next());
            }
        }
    }

    private void updateDesignModifications(LayoutComponent container) {
        if (imposeSize || optimizeStructure) {
            // additional update (after layout) is going to happen - so now just
            // clean the design attrs, container resizing gap will be found later
            cleanDesignAttrs(container.getLayoutRoot(HORIZONTAL));
            cleanDesignAttrs(container.getLayoutRoot(VERTICAL));
        }
        else {
            updateDesignModifications(container.getLayoutRoot(HORIZONTAL), HORIZONTAL);
            updateDesignModifications(container.getLayoutRoot(VERTICAL), VERTICAL);
        }
    }

    private void updateDesignModifications(LayoutInterval root, int dimension) {
        cleanDesignAttrs(root);
        findContainerResizingGap(root, dimension);
    }

    private static void cleanDesignAttrs(LayoutInterval group) {
        group.unsetAttribute(LayoutInterval.DESIGN_ATTRS);
        for (int i=0,n=group.getSubIntervalCount(); i < n; i++) {
            LayoutInterval li = group.getSubInterval(i);
            if (li.isGroup())
                cleanDesignAttrs(li);
            else
                li.unsetAttribute(LayoutInterval.DESIGN_ATTRS);
        }
    }

    private void findContainerResizingGap(LayoutInterval rootInterval, int dimension) {
        if (!LayoutInterval.wantResize(rootInterval) && // See issue 66849
            (LayoutInterval.getIntervalCurrentSize(rootInterval, dimension) != prefSizeOfInterval(rootInterval))) {
            // Resizing gap would change the layout
            return;
        }
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

    private static LayoutInterval findContainerResizingGap(LayoutInterval group, int dimension, int alignment) {
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
                // [check for the same resizability of the ending gaps]
                LayoutInterval li = seq.getSubInterval(alignment == LEADING ? 0 : n-1);
                LayoutInterval gap;
                if (canBeContainerResizingGap(li)
                    && (LayoutInterval.wantResize(seq)
                        || LayoutInterval.getEffectiveAlignment(li) == (alignment^1)))
                {   // making this gap resizing won't change the visual appearance of the sequence
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
                int p2 = group.getCurrentSpace().positions[dimension][alignment];
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
                    if (canBeContainerResizingGap(li) && LayoutInterval.canResize(li)) {
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

    /**
     * @param resizingDef if provided the size change is caused "internally"
     *        (mouse operation driven by LayoutDragger)
     */
    private boolean imposeCurrentContainerSize(LayoutComponent component, LayoutDragger.SizeDef[] resizingDef, boolean recursive) {
        assert component.isLayoutContainer();

        Rectangle interior = visualMapper.getContainerInterior(component.getId());
        if (interior == null)
            return false; // this container is not built
        component.setCurrentInterior(interior);

        if (component.getParent() != null) {
            Rectangle bounds = visualMapper.getComponentBounds(component.getId());
            component.setCurrentBounds(bounds,
                visualMapper.getBaselinePosition(component.getId(), bounds.width, bounds.height));
        }
        for (Iterator it=component.getSubcomponents(); it.hasNext(); ) {
            LayoutComponent subComp = (LayoutComponent) it.next();
            Rectangle bounds = visualMapper.getComponentBounds(subComp.getId());
            subComp.setCurrentBounds(bounds,
                                     visualMapper.getBaselinePosition(subComp.getId(), bounds.width, bounds.height));
            if (subComp.isLayoutContainer()) {
                if (recursive)
                    imposeCurrentContainerSize(subComp, null, true);
            }
            else imposeCurrentComponentSize(subComp);
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
            else { // not empty, there can be a resizing gap inside the container
                if (resizingDef != null && resizingDef[i] != null) {
                    LayoutInterval resGap = resizingDef[i].getResizingGap();
                    if (resGap != null) { // this is it (special gap for design time resizing)
                        int size = resizingDef[i].getResizingGapSize(currentSize);
                        if (size == 0) { // remove the gap
                            LayoutInterval gapParent = resGap.getParent();
                            assert gapParent.isSequential();
                            int index = layoutModel.removeInterval(resGap);
                            assert index == 0 || index == gapParent.getSubIntervalCount();
                            if (gapParent.getSubIntervalCount() == 1) {
                                LayoutInterval last = layoutModel.removeInterval(gapParent, 0);
                                operations.addContent(last, gapParent.getParent(), layoutModel.removeInterval(gapParent));
                            }
                            else if (LayoutInterval.canResize(resGap) && !LayoutInterval.wantResize(root)) {
                                // don't lose resizability of the layout
                                index = index == 0 ? gapParent.getSubIntervalCount()-1 : 0;
                                LayoutInterval otherGap = gapParent.getSubInterval(index);
                                if (otherGap.isEmptySpace()) { // the gap should be resizing
                                    layoutModel.setIntervalSize(otherGap,
                                        NOT_EXPLICITLY_DEFINED, otherGap.getPreferredSize(), Short.MAX_VALUE);
                                }
                            }
                        }
                        else { // set gap size
                            operations.resizeInterval(resGap, size);
                            if (size == NOT_EXPLICITLY_DEFINED && LayoutInterval.canResize(resGap)) {
                                // hack: eliminate unnecessary resizing of default padding gap
                                resGap.setMaximumSize(USE_PREFERRED_SIZE);
                                boolean layoutResizing = LayoutInterval.wantResize(root);
                                resGap.setMaximumSize(Short.MAX_VALUE);
                                if (layoutResizing) { // the gap should be fixed
                                    layoutModel.setIntervalSize(resGap,
                                        NOT_EXPLICITLY_DEFINED, NOT_EXPLICITLY_DEFINED, USE_PREFERRED_SIZE);
                                }
                            }
                        }
                    }
                    else if (!LayoutInterval.wantResize(root)) {
                        // no resizing gap in fixed layout of resizing container
                        int minLayoutSize = computeMinimumDesignSize(root);
                        int growth = root.getCurrentSpace().size(i) - minLayoutSize;
                        if (growth > 0) { // add new resizing gap at the end to hold the new extra space
                            LayoutInterval endGap = new LayoutInterval(SINGLE);
                            endGap.setSizes(NOT_EXPLICITLY_DEFINED, growth, Short.MAX_VALUE);
                            operations.insertGap(endGap, root, minLayoutSize, i, TRAILING);
                        }
                    }
                }
                updateLayoutStructure(root, i, true);
            }

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
                    || (currentSize > min && !LayoutInterval.wantResize(root));
                operations.resizeInterval(outer, externalSize ? currentSize : NOT_EXPLICITLY_DEFINED);
            }
        }

        return true;
    }

    private void imposeCurrentComponentSize(LayoutComponent component) {
        Dimension preferred = visualMapper.getComponentPreferredSize(component.getId());
        for (int i=0; i < DIM_COUNT; i++) {
            LayoutInterval li = component.getLayoutInterval(i);
            int defPref = li.getPreferredSize();
            if (LayoutInterval.wantResizeInLayout(li) && defPref != 0) { // == 0 subordinate component (filling)
                // resizing component with size-defining role in parent
                int current = li.getCurrentSpace().size(i);
                int pref = i == HORIZONTAL ? preferred.width : preferred.height;
                if (defPref == NOT_EXPLICITLY_DEFINED)
                    defPref = pref;
                if (defPref != current)
                    operations.resizeInterval(li, current != pref ? current : NOT_EXPLICITLY_DEFINED);
            }
        }
    }

    private void imposeCurrentGapSize(LayoutInterval gap, int currentSize, int dimension) {
        int pad = -1;
        int min = gap.getMinimumSize();
        int pref = gap.getPreferredSize();
        if (pref == NOT_EXPLICITLY_DEFINED) {
            if (!LayoutInterval.wantResizeInLayout(gap))
                return; // don't change default gap if not resizing
            pad = LayoutUtils.getSizeOfDefaultGap(gap, visualMapper);
            pref = pad;
        }
        if (currentSize != pref) { // [check for canResize?]
            if (min == NOT_EXPLICITLY_DEFINED) {
                if (pad < 0) {
                    pad = LayoutUtils.getSizeOfDefaultGap(gap, visualMapper);
                }
                min = pad;
            }
            else if (min == USE_PREFERRED_SIZE) {
                min = pref;
            }
            if (currentSize < min) {
                currentSize = min;
            }
            operations.resizeInterval(gap, currentSize == pad ? NOT_EXPLICITLY_DEFINED : currentSize);
        }
    }

    private void propEmptyContainer(LayoutInterval root, int dimension) {
        assert root.getParent() == null && root.getSubIntervalCount() == 0;
        LayoutInterval gap = new LayoutInterval(SINGLE);
        gap.setSizes(0, root.getCurrentSpace().size(dimension), Short.MAX_VALUE);
        layoutModel.addInterval(gap, root, 0);
    }

    private int computeMinimumDesignSize(LayoutInterval interval) {
        int size = 0;
        if (interval.isSingle()) {
            int min = interval.getMinimumSize(true);
            size = min == USE_PREFERRED_SIZE ? interval.getPreferredSize(true) : min;
            if (size == NOT_EXPLICITLY_DEFINED) {
                if (interval.isComponent()) {
                    LayoutComponent comp = interval.getComponent();
                    Dimension dim = min == USE_PREFERRED_SIZE ?
                                    visualMapper.getComponentPreferredSize(comp.getId()) :
                                    visualMapper.getComponentMinimumSize(comp.getId());
                    size = interval==comp.getLayoutInterval(HORIZONTAL) ? dim.width : dim.height;
                }
                else { // gap
                    size = LayoutUtils.getSizeOfDefaultGap(interval, visualMapper);
                }
            }
        }
        else if (interval.isSequential()) {
            for (int i=0, n=interval.getSubIntervalCount(); i < n; i++) {
                size += computeMinimumDesignSize(interval.getSubInterval(i));
            }
        }
        else { // parallel group
            for (int i=0, n=interval.getSubIntervalCount(); i < n; i++) {
                size = Math.max(size, computeMinimumDesignSize(interval.getSubInterval(i)));
            }
        }
        return size;
    }

    // -----

    // recursive
    private void intervalRemoved(LayoutInterval parent, int index, boolean primary, boolean wasResizing, int dimension) {
        if (parent.isSequential()) {
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

            if (!wasResizing
                && ((leadingGap != null && LayoutInterval.canResize(leadingGap))
                    || (trailingGap != null && LayoutInterval.canResize(trailingGap))))
                wasResizing = true;

            LayoutInterval superParent = parent.getParent();

            // [check for last interval (count==1), if parallel superParent try to re-add the interval]
            if (parent.getSubIntervalCount() == 0) { // nothing remained
                int idx = layoutModel.removeInterval(parent);
                if (superParent.getParent() != null) {
                    intervalRemoved(superParent, idx, false, wasResizing, dimension);
                }
                else if (superParent.getSubIntervalCount() == 0) { // empty root group - add a filling gap
                    propEmptyContainer(superParent, dimension);
                }
            }
            else { // the sequence remains
                boolean restResizing = LayoutInterval.contentWantResize(parent);
                if (wasResizing && !restResizing) {
                    if (leadingNeighbor == null && parent.getAlignment() == LEADING) {
                        layoutModel.setIntervalAlignment(parent, TRAILING);
                    }
                    if (trailingNeighbor == null && parent.getAlignment() == TRAILING) {
                        layoutModel.setIntervalAlignment(parent, LEADING);
                    }
                }

                int cutSize = LayoutRegion.distance(
                        (leadingNeighbor != null ? leadingNeighbor : parent).getCurrentSpace(),
                        (trailingNeighbor != null ? trailingNeighbor : parent).getCurrentSpace(),
                        dimension,
                        leadingNeighbor != null ? TRAILING : LEADING,
                        trailingNeighbor != null ? LEADING : TRAILING);

                if ((leadingNeighbor != null && trailingNeighbor != null) // inside a sequence
                    || superParent.getParent() == null // in root parallel group
                    || (leadingNeighbor != null && LayoutInterval.getEffectiveAlignment(leadingNeighbor, TRAILING) == TRAILING)
                    || (trailingNeighbor != null && LayoutInterval.getEffectiveAlignment(trailingNeighbor, LEADING) == LEADING))
                {   // create a placeholder gap
                    int min, max;
                    if (wasResizing && !restResizing) { // the gap should be resizing
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
                        layoutModel.setIntervalAlignment(last, parent.getRawAlignment());
                    }
                    else { // adjust current space of the parent sequence
                           // (border interval at the open end removed)
                        int l = (trailingNeighbor != null && leadingNeighbor == null ?
                            trailingNeighbor : parent).getCurrentSpace().positions[dimension][LEADING];
                        int t = (leadingNeighbor != null && trailingNeighbor == null ?
                            leadingNeighbor : parent).getCurrentSpace().positions[dimension][TRAILING];
                        parent.getCurrentSpace().set(dimension, l, t);
                    }
                    maintainSize(superParent, wasResizing || restResizing, dimension,
                                 parent, parent.getCurrentSpace().size(dimension) - cutSize);
                }

                if (wasResizing && !restResizing) {
                    operations.enableGroupResizing(superParent); // in case it was disabled
                }
            }
        }
        else {
            if (parent.getParent() == null) return; // Component placed directly in the root interval
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
                if (LayoutInterval.wantResize(remaining) && !LayoutInterval.canResize(parent)) {
                    // resizing interval in fixed group - make it fixed
                    if (remaining.isGroup())
                        operations.suppressGroupResizing(remaining);
                    else
                        layoutModel.setIntervalSize(remaining,
                                USE_PREFERRED_SIZE, remaining.getPreferredSize(), USE_PREFERRED_SIZE);
                }
                LayoutInterval superParent = parent.getParent();
                int i = layoutModel.removeInterval(parent);
                operations.addContent(remaining, superParent, i);
                if (remaining.isSequential() && superParent.isSequential()) {
                    // eliminate possible directly consecutive gaps
                    // [this could be done by the addContent method directly]
                    eliminateConsecutiveGaps(superParent, i, dimension);
                }
                // [TODO if parallel superParent try to re-add the interval]
            }
            else if (wasResizing && !LayoutInterval.contentWantResize(parent)) {
                operations.enableGroupResizing(parent);
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
                operations.eatGap(current, next, LayoutRegion.distance(lr, tr, dimension, la, ta));
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

        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval li = (LayoutInterval) it.next();
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
                l = LayoutUtils.getOutermostComponent(li, dimension, LEADING)
                        .getCurrentSpace().positions[dimension][LEADING];
                t = LayoutUtils.getOutermostComponent(li, dimension, TRAILING)
                        .getCurrentSpace().positions[dimension][TRAILING];
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

        if (leadAlign && trailAlign) {
            optimizeGaps(group, dimension, false);
        }
        else { // one open edge to compensate
            if (!LayoutInterval.canResize(group)) { // resizing disabled on the group
                wasResizing = false;
            }
            boolean resizing = LayoutInterval.wantResize(group);
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

                operations.insertGap(gap, group, leadAlign ? trailCompPos : leadCompPos,
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

    // -----
    // auxiliary fields holding temporary objects used frequently

    // converted cursor position used during moving/resizing
    private int[] cursorPos = { 0, 0 };

    // resizability of a component in the designer
    private boolean[][] resizability = { { true, true }, { true, true } };

    // -----
    // test generation support

    static final String TEST_SWITCH = "netbeans.form.layout_test"; // NOI18N

    /* stores test code lines */
    public List testCode = new ArrayList();

    // these below are used for removing unwanted move entries, otherwise the code can exceed 10000 lines in a few seconds of form editor work ;O)
    private List testCode0 = new ArrayList();
    private List beforeMove = new ArrayList();
    private List move1 = new ArrayList();
    private List move2 = new ArrayList();
    private boolean isMoving = false;
    
    private int modelCounter = -1;
    
    private Point lastMovePoint = new Point(0, 0);

    public int getModelCounter() {
        return modelCounter;
    }

    public void setModelCounter(int modelCounter) {
        this.modelCounter = modelCounter;
    }

    public static boolean testMode() {
        return Boolean.getBoolean(TEST_SWITCH);
    }

    public boolean logTestCode() {
        return modelCounter > -1 && Boolean.getBoolean(TEST_SWITCH);
    }
}
