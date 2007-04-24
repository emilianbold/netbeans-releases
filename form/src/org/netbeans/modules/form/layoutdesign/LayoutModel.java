/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutdesign;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.undo.*;


/**
 * This class manages layout data of a form. Specifically it:
 * - provides entry points for exploring the layout,
 * - allows to add/remove layout intervals and components,
 * - allows to listen on changes,
 * - manages an undo/redo queue for the layout, provides undo/redo marks,
 *   and allows to perform undo/redo to given mark.
 *
 * @author Tomas Pavek
 */

public class LayoutModel implements LayoutConstants {

    // map String component Id -> LayoutComponent instance
    private Map idToComponents = new HashMap();

    // list of listeners registered on LayoutModel
    private ArrayList listeners;

    // layout changes recording and undo/redo
    private boolean recordingChanges = true;
    private boolean undoRedoInProgress;
    private int changeMark;
    private int oldestMark;
    private int changeCountHardLimit = 10000;
    private Map undoMap = new HashMap(500);
    private Map redoMap = new HashMap(100);
    private LayoutUndoableEdit lastUndoableEdit;

    // remembers whether the model was corrected/upgraded during loading
    private boolean corrected;

    // -----

    /**
     * Basic mapping method. Returns LayoutComponent for given Id.
     * @return LayoutComponent of given Id, null if there is no such component
     *         registered in the model
     */
    public LayoutComponent getLayoutComponent(String compId) {
        return (LayoutComponent) idToComponents.get(compId);
    }

    public void addRootComponent(LayoutComponent comp) {
        addComponent(comp, null, -1);
    }

    public void removeComponent(String compId, boolean fromModel) {
        LayoutComponent comp = getLayoutComponent(compId);
        if (comp != null)
            removeComponentAndIntervals(comp, fromModel);
    }

    /**
     * @return false if the component does not exist in the layout model
     */
    public boolean changeComponentToContainer(String componentId) {
        LayoutComponent component = getLayoutComponent(componentId);
        if (component != null) {
            setLayoutContainer(component, true);
            return true;
        }
        return false;
    }

    /**
     * Changes a container to a component (that cannot contain sub-components).
     * All its current sub-components are removed. Those not being containers
     * are also removed from the model - containers remain in model.
     * @return false if the component does not exist in the layout model
     */
    public boolean changeContainerToComponent(String componentId) {
        LayoutComponent component = getLayoutComponent(componentId);
        if (component == null) {
            return false;
        }
        for (int i=component.getSubComponentCount()-1; i>=0; i--) {
            LayoutComponent sub = component.getSubComponent(i);
            removeComponent(sub, !sub.isLayoutContainer());
        }
        if (component.getParent() == null) { // non-container without a parent
            removeComponent(component, true);
        }
        setLayoutContainer(component, false); // this removes interval roots of the container
        return true;
    }

    // -----

    void registerComponent(LayoutComponent comp, boolean recursive) {
        registerComponentImpl(comp);
        if (recursive && comp.isLayoutContainer()) {
            for (LayoutComponent subComp : comp.getSubcomponents()) {
                registerComponent(subComp, recursive);
            }
        }
    }
    
    void registerComponentImpl(LayoutComponent comp) {
        Object lc = idToComponents.put(comp.getId(), comp);

        if (lc != comp) {
            // record undo/redo and fire event
            LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.COMPONENT_REGISTERED);
            ev.setComponent(comp);
            addChange(ev);
            fireEvent(ev);
        } // else noop => don't need change event
    }

    void unregisterComponent(LayoutComponent comp, boolean recursive) {
        if (recursive && comp.isLayoutContainer()) {
            for (LayoutComponent subComp : comp.getSubcomponents()) {
                unregisterComponent(subComp, recursive);
            }
        }
        removeComponentFromLinkSizedGroup(comp, HORIZONTAL);
        removeComponentFromLinkSizedGroup(comp, VERTICAL);
        unregisterComponentImpl(comp);
    }

    void unregisterComponentImpl(LayoutComponent comp) {
        Object lc = idToComponents.remove(comp.getId());

        if (lc != null) {
            // record undo/redo and fire event
            LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.COMPONENT_UNREGISTERED);
            ev.setComponent(comp);
            addChange(ev);
            fireEvent(ev);
        } // else noop => don't need change event
    }

    void changeComponentId(LayoutComponent comp, String newId) {
        unregisterComponentImpl(comp);
        comp.setId(newId);
        registerComponentImpl(comp);
    }

    void replaceComponent(LayoutComponent comp, LayoutComponent substComp) {
        assert substComp.getParent() == null;
        for (int i=0; i<DIM_COUNT; i++) {
            LayoutInterval interval = comp.getLayoutInterval(i);
            LayoutInterval substInt = substComp.getLayoutInterval(i);
            assert substInt.getParent() == null;
            setIntervalAlignment(substInt, interval.getRawAlignment());
            setIntervalSize(substInt, interval.getMinimumSize(),
                    interval.getPreferredSize(), interval.getMaximumSize());
            LayoutInterval parentInt = interval.getParent();
            if (parentInt != null) {
                int index = removeInterval(interval);
                addInterval(substInt, parentInt, index);
            }
        }

        LayoutComponent parent = comp.getParent();
        if (parent != null) {
            int index = removeComponentImpl(comp);
            addComponentImpl(substComp, parent, index);
        }
        unregisterComponentImpl(comp);
        registerComponentImpl(substComp);
    }

    Iterator getAllComponents() {
        return idToComponents.values().iterator();
    }

    // Note this method does not care about adding the layout intervals of the
    // component, it must be done in advance.
    void addComponent(LayoutComponent component, LayoutComponent parent, int index) {
        addComponentImpl(component, parent, index);
        registerComponent(component, true);
    }

    void addComponentImpl(LayoutComponent component, LayoutComponent parent, int index) {
        assert component.getParent() == null;

        if (parent != null) {
            assert getLayoutComponent(parent.getId()) == parent;
            index = parent.addComponent(component, index);
        }
        else {
            assert component.isLayoutContainer();
        }

        // record undo/redo and fire event
        LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.COMPONENT_ADDED);
        ev.setComponent(component, parent, index);
        addChange(ev);
        fireEvent(ev);
    }

    // Low level removal - removes the component from parent, unregisters it,
    // records the change for undo/redo, and fires an event. Does nothing to
    // the layout intervals of the component.
    void removeComponent(LayoutComponent component, boolean fromModel) {
        removeComponentImpl(component);
        if (fromModel && (getLayoutComponent(component.getId()) != null)) {
            unregisterComponent(component, true);
        }
    }

    int removeComponentImpl(LayoutComponent component) {
        int index;
        LayoutComponent parent = component.getParent();
        if (parent != null) {
            index = parent.removeComponent(component);
        } else {
            return -1; // the removal operation is "noop"
        }

        // record undo/redo and fire event
        LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.COMPONENT_REMOVED);
        ev.setComponent(component, parent, index);
        addChange(ev);
        fireEvent(ev);

        return index;
    }

    void removeComponentAndIntervals(LayoutComponent comp, boolean fromModel) {
        if (comp.getParent() != null) {
            for (int i=0; i < DIM_COUNT; i++) {
                LayoutInterval interval = comp.getLayoutInterval(i);
                if (interval.getParent() != null) {
                    removeInterval(interval);
                }
            }
        }
        removeComponent(comp, fromModel);
    }

    LayoutInterval[] addNewLayoutRoots(LayoutComponent container) {
        LayoutInterval[] newRoots = container.addNewLayoutRoots();

        // record undo/redo (don't fire event)
        LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.LAYOUT_ROOTS_ADDED);
        ev.setLayoutRoots(container, newRoots, -1);
        addChange(ev);

        return newRoots;
    }

    LayoutInterval[] removeLayoutRoots(LayoutComponent container, LayoutInterval oneRoot) {
        LayoutInterval[] roots = container.getLayoutRoots(oneRoot);
        if (roots != null) {
            int index = container.removeLayoutRoots(roots);

            // record undo/redo (don't fire event)
            LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.LAYOUT_ROOTS_REMOVED);
            ev.setLayoutRoots(container, roots, index);
            addChange(ev);
        }
        return roots;
    }

    void addInterval(LayoutInterval interval, LayoutInterval parent, int index) {
        assert interval.getParent() == null;

        index = parent.add(interval, index);

        // record undo/redo and fire event
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_ADDED);
        ev.setInterval(interval, parent, index);
        addChange(ev);
        fireEvent(ev);
    }

    // Low level removal - removes the interval from parent, records the
    // change for undo/redo, and fires an event.
    int removeInterval(LayoutInterval interval) {
        LayoutInterval parent = interval.getParent();
        int index = parent.remove(interval);

        // record undo/redo and fire event
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_REMOVED);
        ev.setInterval(interval, parent, index);
        addChange(ev);
        fireEvent(ev);

        return index;
    }

    LayoutInterval removeInterval(LayoutInterval parent, int index) {
        LayoutInterval interval = parent.remove(index);

        // record undo/redo and fire event
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_REMOVED);
        ev.setInterval(interval, parent, index);
        addChange(ev);
        fireEvent(ev);

        return interval;
    }
    
    void changeIntervalAttribute(LayoutInterval interval, int attribute, boolean set) {
        int oldAttributes = interval.getAttributes();
        if (set) {
            interval.setAttribute(attribute);
        } else {
            interval.unsetAttribute(attribute);
        }
        int newAttributes = interval.getAttributes();
        
        // record undo/redo (don't fire event)
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_ATTRIBUTES_CHANGED);
        ev.setAttributes(interval, oldAttributes, newAttributes);
        addChange(ev);
    }

    void setIntervalAlignment(LayoutInterval interval, int alignment) {
        int oldAlignment = interval.getRawAlignment();
        interval.setAlignment(alignment);

        // record undo/redo (don't fire event)
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_ALIGNMENT_CHANGED);
        ev.setAlignment(interval, oldAlignment, alignment);
        addChange(ev);
    }

    void setGroupAlignment(LayoutInterval group, int alignment) {
        int oldAlignment = group.getGroupAlignment();
        if (alignment == oldAlignment) {
            return;
        }
        group.setGroupAlignment(alignment);

        // record undo/redo (don't fire event)
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.GROUP_ALIGNMENT_CHANGED);
        ev.setAlignment(group, oldAlignment, alignment);
        addChange(ev);
    }

    void setLayoutContainer(LayoutComponent component, boolean container) {
        boolean oldContainer = component.isLayoutContainer();
        if (oldContainer != container) {
            List<LayoutInterval[]> roots = component.getLayoutRoots();
            component.setLayoutContainer(container, null);

            // record undo/redo (don't fire event)
            LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.CONTAINER_ATTR_CHANGED);
            ev.setContainer(component, roots);
            addChange(ev);            
        }
    }

    public void setIntervalSize(LayoutInterval interval, int min, int pref, int max) {
        int oldMin = interval.getMinimumSize();
        int oldPref = interval.getPreferredSize();
        int oldMax = interval.getMaximumSize();
        if (min == oldMin && pref == oldPref && max == oldMax) {
            return; // no change
        }
        interval.setSizes(min, pref, max);
        if (interval.isComponent()) {
            LayoutComponent comp = interval.getComponent();
            boolean horizontal = (interval == comp.getLayoutInterval(HORIZONTAL));
            if (oldMin != min) {
                comp.firePropertyChange(horizontal ? PROP_HORIZONTAL_MIN_SIZE : PROP_VERTICAL_MIN_SIZE,
                    new Integer(oldMin), new Integer(min));
            }
            if (oldPref != pref) {
                comp.firePropertyChange(horizontal ? PROP_HORIZONTAL_PREF_SIZE : PROP_VERTICAL_PREF_SIZE,
                    new Integer(oldPref), new Integer(pref));
            }
            if (oldMax != max) {
                comp.firePropertyChange(horizontal ? PROP_HORIZONTAL_MAX_SIZE : PROP_VERTICAL_MAX_SIZE,
                    new Integer(oldMax), new Integer(max));
            }
        }

        // record undo/redo (don't fire event)
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_SIZE_CHANGED);
        ev.setSize(interval, oldMin, oldPref, oldMax, min, pref, max);
        addChange(ev);
    }

    public void setPaddingType(LayoutInterval interval, PaddingType paddingType) {
        PaddingType oldPadding = interval.getPaddingType();
        if (oldPadding != paddingType) {
            interval.setPaddingType(paddingType);

            // record undo/redo (don't fire event)
            LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_PADDING_TYPE_CHANGED);
            ev.setPaddingType(interval, oldPadding, paddingType);
            addChange(ev);
        }
    }

    /**
     * Does a non-recursive copy of components and layout of given source
     * container. Assuming the target container is empty (or does not exist yet).
     * LayoutComponent instances are created automatically as needed, using the
     * provided IDs.
     * @param sourceModel the source LayoutModel
     * @param sourceContainerId ID of the source container
     * @param sourceToTargetId mapping between the original and the copied
     *        components' IDs
     * @param targetContainerId ID of the target container
     */
    public void copyContainerLayout(LayoutModel sourceModel, String sourceContainerId,
                Map<String, String> sourceToTargetId, String targetContainerId) {
        LayoutComponent sourceContainer = sourceModel.getLayoutComponent(sourceContainerId);
        LayoutComponent targetContainer = getLayoutComponent(targetContainerId);
        if (targetContainer == null) {
            targetContainer = new LayoutComponent(targetContainerId, true);
            addRootComponent(targetContainer);
        } else if (!targetContainer.isLayoutContainer()) {
            changeComponentToContainer(targetContainerId);
        }
        copyContainerLayout(sourceContainer, sourceToTargetId, targetContainer);
    }

    void copyContainerLayout(LayoutComponent sourceContainer,
            Map<String, String> sourceToTargetId, LayoutComponent targetContainer) {
        // Create LayoutComponents
        for (LayoutComponent sourceComp : sourceContainer.getSubcomponents()) {
            String targetId = sourceToTargetId.get(sourceComp.getId());
            LayoutComponent targetComp = getLayoutComponent(targetId);
            if (targetComp == null) {
                targetComp = new LayoutComponent(targetId, sourceComp.isLayoutContainer());
            }
            if (targetComp.getParent() == null) {
                addComponent(targetComp, targetContainer, -1);
            }
        }
        // Copy LayoutIntervals
        int i = 0;
        for (LayoutInterval[] sourceRoots : sourceContainer.getLayoutRoots()) {
            if (i == targetContainer.getLayoutRootCount()) {
                addNewLayoutRoots(targetContainer);
            }
            for (int dim=0; dim<DIM_COUNT; dim++) {
                copySubIntervals(sourceRoots[dim], targetContainer.getLayoutRoot(i, dim), sourceToTargetId);
            }
            i++;
        }
    }

    private void copySubIntervals(LayoutInterval sourceInterval, LayoutInterval targetInterval, Map/*<String,String>*/ sourceToTargetIds) {
        Iterator iter = sourceInterval.getSubIntervals();
        while (iter.hasNext()) {
            LayoutInterval sourceSub = (LayoutInterval)iter.next();
            LayoutInterval clone = null;
            if (sourceSub.isComponent()) {
                String compId = (String)sourceToTargetIds.get(sourceSub.getComponent().getId());
                LayoutComponent comp = getLayoutComponent(compId);
                int dimension = (sourceSub == sourceSub.getComponent().getLayoutInterval(HORIZONTAL)) ? HORIZONTAL : VERTICAL;
                clone = comp.getLayoutInterval(dimension);
            }
            LayoutInterval targetSub = LayoutInterval.cloneInterval(sourceSub, clone);
            if (sourceSub.isGroup()) {
                copySubIntervals(sourceSub, targetSub, sourceToTargetIds);
            }
            addInterval(targetSub, targetInterval, -1);
        }
    }

    // assuming target container is empty
    void moveContainerLayout(LayoutComponent sourceContainer, LayoutComponent targetContainer) {
        if (!sourceContainer.isLayoutContainer() || !targetContainer.isLayoutContainer()
                || targetContainer.getSubComponentCount() > 0) {
            throw new IllegalArgumentException();
        }

        while (sourceContainer.getSubComponentCount() > 0) {
            LayoutComponent sub = sourceContainer.getSubComponent(0);
            removeComponent(sub, false);
            addComponent(sub, targetContainer, -1);
        }

        List<LayoutInterval[]> sourceRoots = sourceContainer.getLayoutRoots();

        sourceContainer.setLayoutRoots(null); // clear
        LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.LAYOUT_ROOTS_CHANGED);
        ev.setLayoutRoots(sourceContainer, sourceRoots, null);
        addChange(ev);

        targetContainer.setLayoutRoots(sourceRoots);
        ev = new LayoutEvent.Component(this, LayoutEvent.LAYOUT_ROOTS_CHANGED);
        ev.setLayoutRoots(targetContainer, null, sourceRoots);
        addChange(ev);
    }

    static LayoutInterval[] createIntervalsFromBounds(Map<LayoutComponent, Rectangle> compToBounds) {
        RegionInfo region = new RegionInfo(compToBounds);
        region.calculateIntervals();
        LayoutInterval[] result = new LayoutInterval[DIM_COUNT];
        for (int dim=0; dim<DIM_COUNT; dim++) {
            result[dim] = region.getInterval(dim);
        }
        return result;
    }

    private static class RegionInfo {
        private LayoutInterval horizontal = null;
        private LayoutInterval vertical = null;
        private Map<LayoutComponent, Rectangle> compToBounds;
        private int minx;
        private int maxx;
        private int miny;
        private int maxy;
        private int dimension;

        public RegionInfo(Map<LayoutComponent, Rectangle> compToBounds) {
            this.compToBounds = compToBounds;
            this.dimension = -1;
            minx = miny = 0;
            updateRegionBounds();
        }

        private RegionInfo(Map<LayoutComponent, Rectangle> compToBounds, int dimension) {
            this.compToBounds = compToBounds;
            this.dimension = dimension;
            minx = miny = Short.MAX_VALUE;
            updateRegionBounds();
        }

        private void updateRegionBounds() {
            maxy = maxx = Short.MIN_VALUE;
            for (Rectangle bounds : compToBounds.values()) {
                minx = Math.min(minx, bounds.x);
                miny = Math.min(miny, bounds.y);
                maxx = Math.max(maxx, bounds.x + bounds.width);
                maxy = Math.max(maxy, bounds.y + bounds.height);
            }
        }

        public void calculateIntervals() {
            if (compToBounds.size() == 1) {
                Map.Entry<LayoutComponent, Rectangle> e = compToBounds.entrySet().iterator().next();
                LayoutComponent comp = e.getKey();
                Rectangle bounds = e.getValue();
                horizontal = comp.getLayoutInterval(HORIZONTAL);
                horizontal = prefixByGap(horizontal, bounds.x - minx);
                vertical = comp.getLayoutInterval(VERTICAL);
                vertical = prefixByGap(vertical, bounds.y - miny);
                return;
            }
            int effDim = -1;
            List parts = null;
            Map<LayoutComponent, Rectangle> removedCompToBounds = null;
            do {                
                boolean remove = ((dimension == -1) && (effDim == HORIZONTAL))
                    || ((dimension != -1) && (effDim != -1));
                if (remove) {
                    effDim = -1;
                }
                if (dimension == -1) {
                    switch (effDim) {
                        case -1: effDim = VERTICAL; break;
                        case VERTICAL: effDim = HORIZONTAL; break;
                        case HORIZONTAL: remove = true;
                    }
                } else {
                    effDim = dimension;
                }
                if (remove) { // no cut found, remove some component
                    Map.Entry<LayoutComponent, Rectangle> e = compToBounds.entrySet().iterator().next();
                    LayoutComponent comp = e.getKey();
                    Rectangle bounds = e.getValue();
                    if (removedCompToBounds == null) {
                        removedCompToBounds = new HashMap<LayoutComponent, Rectangle>();
                    }
                    removedCompToBounds.put(comp, bounds);
                    compToBounds.remove(comp);
                }
                Set cutSet = createPossibleCuts(effDim);
                parts = cutIntoParts(cutSet, effDim);            
            } while (!compToBounds.isEmpty() && parts.isEmpty());
            dimension = effDim;
            List regions = new LinkedList();
            Iterator iter = parts.iterator();
            while (iter.hasNext()) {
                Map part = (Map)iter.next();
                RegionInfo region = new RegionInfo(part, (dimension == HORIZONTAL) ? VERTICAL : HORIZONTAL);
                region.calculateIntervals();
                regions.add(region);
            }
            mergeSubRegions(regions, dimension);
            if (removedCompToBounds != null) {
                for (int dim = HORIZONTAL; dim <= VERTICAL; dim++) {
                    LayoutInterval parent = (dim == HORIZONTAL) ? horizontal : vertical;
                    if (!parent.isParallel()) {
                        LayoutInterval parGroup = new LayoutInterval(PARALLEL);
                        parGroup.add(parent, -1);
                        if (dim == HORIZONTAL) {
                            horizontal = parGroup;
                        } else {
                            vertical = parGroup;
                        }
                        parent = parGroup;
                    }
                    for (Map.Entry<LayoutComponent, Rectangle> entry : removedCompToBounds.entrySet()) {
                        LayoutComponent comp = entry.getKey();
                        Rectangle bounds = entry.getValue();
                        LayoutInterval interval = comp.getLayoutInterval(dim);
                        int gap = (dim == HORIZONTAL) ? bounds.x - minx : bounds.y - miny;
                        interval = prefixByGap(interval, gap);
                        parent.add(interval, -1);
                    }
                }
            }
        }
        
        private SortedSet createPossibleCuts(int dimension) {
            SortedSet cutSet = new TreeSet();
            for (Rectangle bounds : compToBounds.values()) {
                // Leading lines are sufficient
                int leading = (dimension == HORIZONTAL) ? bounds.x : bounds.y;
                cutSet.add(new Integer(leading));
            }
            cutSet.add(new Integer((dimension == HORIZONTAL) ? maxx : maxy));
            return cutSet;
        }
        
        private List cutIntoParts(Set cutSet, int dimension) {
            List parts = new LinkedList();
            Iterator iter = cutSet.iterator();
            while (iter.hasNext()) {
                Integer cutInt = (Integer)iter.next();
                int cut = cutInt.intValue();
                boolean isCut = true;
                Map<LayoutComponent, Rectangle> preCompToBounds = new HashMap<LayoutComponent, Rectangle>();
                Map<LayoutComponent, Rectangle> postCompToBounds = new HashMap<LayoutComponent, Rectangle>();
                Iterator<Map.Entry<LayoutComponent, Rectangle>> it = compToBounds.entrySet().iterator();                
                while (isCut && it.hasNext()) {
                    Map.Entry<LayoutComponent, Rectangle> entry = it.next();
                    LayoutComponent comp = entry.getKey();
                    Rectangle bounds = entry.getValue();
                    int leading = (dimension == HORIZONTAL) ? bounds.x : bounds.y;
                    int trailing = leading + ((dimension == HORIZONTAL) ? bounds.width : bounds.height);
                    if (leading >= cut) {
                        postCompToBounds.put(comp, bounds);
                    } else if (trailing <= cut) {
                        preCompToBounds.put(comp, bounds);
                    } else {
                        isCut = false;
                    }
                }
                if (isCut && !preCompToBounds.isEmpty()
                    // the last cut candidate (end of the region) cannot be the first cut
                    && (!parts.isEmpty() || (preCompToBounds.size() != compToBounds.size()))) {
                    compToBounds.keySet().removeAll(preCompToBounds.keySet());
                    parts.add(preCompToBounds);
                }
            }
            return parts;
        }
        
        private void mergeSubRegions(List regions, int dimension) {
            if (regions.size() == 0) {
                horizontal = new LayoutInterval(PARALLEL);
                vertical = new LayoutInterval(PARALLEL);
                return;
            }
            LayoutInterval seqGroup = new LayoutInterval(SEQUENTIAL);
            LayoutInterval parGroup = new LayoutInterval(PARALLEL);
            int lastSeqTrailing = (dimension == HORIZONTAL) ? minx : miny;
            Iterator iter = regions.iterator();
            while (iter.hasNext()) {
                RegionInfo region = (RegionInfo)iter.next();
                LayoutInterval seqInterval;
                LayoutInterval parInterval;
                int seqGap;
                int parGap;
                if (dimension == HORIZONTAL) {
                    seqInterval = region.horizontal;
                    parInterval = region.vertical;
                    parGap = region.miny - miny;
                    seqGap = region.minx - lastSeqTrailing;
                    lastSeqTrailing = region.maxx;
                } else {
                    seqInterval = region.vertical;
                    parInterval = region.horizontal;
                    parGap = region.minx - minx;
                    seqGap = region.miny - lastSeqTrailing;
                    lastSeqTrailing = region.maxy;
                }
                // PENDING optimization of the resulting layout model
                if (seqGap > 0) {
                    LayoutInterval gap = new LayoutInterval(SINGLE);
                    gap.setSize(seqGap);
                    seqGroup.add(gap, -1);
                }
                seqGroup.add(seqInterval, -1);
                parInterval = prefixByGap(parInterval, parGap);
                parGroup.add(parInterval, -1);
            }
            if (dimension == HORIZONTAL) {
                horizontal = seqGroup;
                vertical = parGroup;
            } else {
                horizontal = parGroup;
                vertical = seqGroup;
            }
        }
        
        private LayoutInterval prefixByGap(LayoutInterval interval, int size) {
            if (size > 0) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setSize(size);
                if (interval.isSequential()) {
                    interval.add(gap, 0);
                } else {
                    LayoutInterval group = new LayoutInterval(SEQUENTIAL);
                    group.add(gap, -1);
                    group.add(interval, -1);
                    interval = group;
                }
            }
            return interval;
        }
        
        public LayoutInterval getInterval(int dimension) {
            return (dimension == HORIZONTAL) ? horizontal : vertical;
        }

    }

    // -----
    // listeners registration, firing methods (no synchronization)

    void addListener(Listener l) {
        if (listeners == null) {
            listeners = new ArrayList();
        }
        else {
            listeners.remove(l);
        }
        listeners.add(l);
    }

    void removeListener(Listener l) {
        if (listeners != null) {
            listeners.remove(l);
        }
    }

    private void fireEvent(LayoutEvent event) {
        if (listeners != null && listeners.size() > 0) {
            Iterator it = ((List)listeners.clone()).iterator();
            while (it.hasNext()) {
                ((Listener)it.next()).layoutChanged(event);
            }
        }
    }

    /**
     * Listener interface for changes in the layout model.
     */
    interface Listener {
        void layoutChanged(LayoutEvent ev);
    }

    // -----
    // changes recording and undo/redo

    public boolean isChangeRecording() {
        return recordingChanges;
    }

    public void setChangeRecording(boolean record) {
        recordingChanges = record;
    }

    boolean isUndoRedoInProgress() {
        return undoRedoInProgress;
    }

    public Object getChangeMark() {
        return new Integer(changeMark);
    }
    
    public void endUndoableEdit() {
        if (lastUndoableEdit != null) {
            lastUndoableEdit.endMark = getChangeMark();
            lastUndoableEdit = null;
        }
    }
    
    public boolean isUndoableEditInProgress() {
        return (lastUndoableEdit != null);
    }

    public UndoableEdit getUndoableEdit() {
        if (recordingChanges && !undoRedoInProgress) {
            LayoutUndoableEdit undoEdit = new LayoutUndoableEdit();
            undoEdit.startMark = getChangeMark();
            endUndoableEdit();
            lastUndoableEdit = undoEdit;
            return undoEdit;
        }
        return null;
    }

    private void addChange(LayoutEvent change) {
        if (recordingChanges && !undoRedoInProgress) {
            redoMap.clear();
            if (undoMap.size() == 0)
                oldestMark = changeMark;

            undoMap.put(new Integer(changeMark++), change);

            while (undoMap.size() > changeCountHardLimit) {
                undoMap.remove(new Integer(oldestMark++));
            }
        }
    }

    boolean undo(Object startMark, Object endMark) {
        assert !undoRedoInProgress;
        if (!undoMap.containsKey(startMark)) {
            return false; // the mark is not present in the undo queue
        }

        int start = ((Integer)startMark).intValue();
        int end = ((Integer)endMark).intValue();
        undoRedoInProgress = true;

        while (end > start) {
            Object key = new Integer(--end);
            LayoutEvent change = (LayoutEvent) undoMap.remove(key);
            if (change != null) {
                change.undo();
                redoMap.put(key, change);
            }
        }

        undoRedoInProgress = false;
        return true;
    }

    boolean redo(Object startMark, Object endMark) {
        assert !undoRedoInProgress;
        if (!redoMap.containsKey(startMark)) {
            return false; // the mark is not present in the redo queue
        }

        int start = ((Integer)startMark).intValue();
        int end = ((Integer)endMark).intValue();
        undoRedoInProgress = true;

        while (start < end) {
            Object key = new Integer(start++);
            LayoutEvent change = (LayoutEvent) redoMap.remove(key);
            if (change != null) {
                change.redo();
                undoMap.put(key, change);
            }
        }

        undoRedoInProgress = false;
        return true;
    }

    void releaseChanges(Object fromMark, Object toMark) {
        int m1 = ((Integer)fromMark).intValue();
        int m2 = ((Integer)toMark).intValue();

        while (m1 < m2) {
            Object m = new Integer(m1);
            undoMap.remove(m);
            redoMap.remove(m);
            m1++;
        }
    }

    /**
     * UndoableEdit implementation for series of changes in layout model.
     */
    private class LayoutUndoableEdit extends AbstractUndoableEdit {
        private Object startMark;
        private Object endMark;

        public void undo() throws CannotUndoException {
            super.undo();
            if (endMark == null) {
                assert lastUndoableEdit == this;
                endMark = getChangeMark();
                lastUndoableEdit = null;
            }
            LayoutModel.this.undo(startMark, endMark);
        }

        public void redo() throws CannotRedoException {
            super.redo();
            LayoutModel.this.redo(startMark, endMark);
        }

        public String getUndoPresentationName() {
            return ""; // NOI18N
        }
        public String getRedoPresentationName() {
            return ""; // NOI18N
        }

        public void die() {
            releaseChanges(startMark, endMark != null ? endMark : getChangeMark());
        }
    }

    /**
     * Returns dump of the layout model. For debugging and testing purposes only.
     *
     * @return dump of the layout model.
     */
    public String dump(final Map idToNameMap) {
        Set roots = new TreeSet(new Comparator() {
            // comparator to ensure stable order of dump; according to tree
            // hierarchy, order within container, name
            public int compare(Object o1, Object o2) {
                if (o1 == o2)
                    return 0;
                LayoutComponent lc1 = (LayoutComponent) o1;
                LayoutComponent lc2 = (LayoutComponent) o2;
                // parent always first
                if (lc1.isParentOf(lc2))
                    return -1;
                if (lc2.isParentOf(lc1))
                    return 1;
                // get the same level under common parent
                LayoutComponent parent = LayoutComponent.getCommonParent(lc1, lc2);
                while (lc1.getParent() != parent)
                    lc1 = lc1.getParent();
                while (lc2.getParent() != parent)
                    lc2 = lc2.getParent();
                if (parent != null) { // in the same tree
                    return parent.indexOf(lc1) < parent.indexOf(lc2) ? -1 : 1;
                }
                else { // in distinct trees
                    String id1 = lc1.getId();
                    String id2 = lc2.getId();
                    if (idToNameMap != null) {
                        id1 = (String) idToNameMap.get(id1);
                        id2 = (String) idToNameMap.get(id2);
                        if (id1 == null) {
                            return -1;
                        }
                        if (id2 == null) {
                            return 1;
                        }
                    }
                    return id1.compareTo(id2);
                }
            }
        });
        Iterator iter = idToComponents.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            LayoutComponent comp = (LayoutComponent)entry.getValue();
            if (comp.isLayoutContainer()) {
                roots.add(comp);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<LayoutModel>\n"); // NOI18N
        Iterator rootIter = roots.iterator();
        while (rootIter.hasNext()) {
            LayoutComponent root = (LayoutComponent)rootIter.next();
            String rootId = root.getId();
            if (idToNameMap != null) {
                rootId = (String) idToNameMap.get(rootId);
            }
            if (rootId != null)
                sb.append("  <Root id=\""+rootId+"\">\n"); // NOI18N
            else
                sb.append("  <Root>\n"); // NOI18N
            sb.append(saveContainerLayout(root, idToNameMap, 2, true));
            sb.append("  </Root>\n"); // NOI18N
        }
        sb.append("</LayoutModel>\n"); // NOI18N
        return sb.toString();
    }
    
    /**
     * Returns dump of the layout interval.
     *
     * @param interval interval whose dump should be returned.
     * @param dimension dimension in which the layout interval resides.
     */
    public String dump(LayoutInterval interval, int dimension) {
        return LayoutPersistenceManager.dumpInterval(this, interval, dimension, 2);
    }
    
    /**
     * Saves given layout container into a String.
     *
     * @param container the layout container to be saved
     * @param idToNameMap map for translating component Ids to names suitable
     *        for saving
     * @param indent determines size of indentation
     * @param humanReadable determines whether constants should be replaced
     * by human readable expressions
     * @return dump of the layout model of given container
     */
    public String saveContainerLayout(LayoutComponent container, Map idToNameMap, int indent, boolean humanReadable) {
        return LayoutPersistenceManager.saveContainer(this, container, idToNameMap, indent, humanReadable);
    }

    /**
     * Loads the layout of the given container.
     *
     * @param containerId ID of the layout container to be loaded
     * @param layoutNodeList XML data to load
     * @param nameToIdMap map from component names to component IDs
     */
    public void loadContainerLayout(String containerId, org.w3c.dom.NodeList layoutNodeList, Map nameToIdMap)
        throws java.io.IOException
    {
        LayoutPersistenceManager.loadContainer(this, containerId, layoutNodeList, nameToIdMap);
    }

    /**
     * Returns whether the model was repaired (because of some error found) or
     * upgraded automatically during loading. After loading, it might be a good
     * idea to save the corrected state, so to mark the loaded layout as modified.
     * @return whether the model was changed during loading or saving
     */
    public boolean wasCorrected() {
        return corrected;
    }

    void setCorrected() {
        corrected = true;
    }

    /* 
     * LINKSIZE 
     */
    
    // each object in the map is a List and contains list of components within the group
    private Map linkSizeGroupsH = new HashMap();
    private Map linkSizeGroupsV = new HashMap();
    
    private int maxLinkGroupId = 0;

    void addComponentToLinkSizedGroup(int groupId, String compId, int dimension) {
                
        if (NOT_EXPLICITLY_DEFINED == groupId) { // 
            return;
        }
        if (maxLinkGroupId < groupId) {
            maxLinkGroupId=groupId;
        }
        Integer groupIdInt = new Integer(groupId);
        Map linkSizeGroups = (dimension == HORIZONTAL) ? linkSizeGroupsH : linkSizeGroupsV;
        List l = (List)linkSizeGroups.get(groupIdInt);
        if ((l != null) && (l.contains(compId) || !sameContainer(compId, (String)l.get(0)))) {
            return;
        }
        addComponentToLinkSizedGroupImpl(groupId, compId, dimension);
    }

    void addComponentToLinkSizedGroupImpl(int groupId, String compId, int dimension) {
        LayoutComponent lc = getLayoutComponent(compId);
        Integer groupIdInt = new Integer(groupId);
        Map linkSizeGroups = (dimension == HORIZONTAL) ? linkSizeGroupsH : linkSizeGroupsV;
        List l = (List)linkSizeGroups.get(groupIdInt);
        if (l != null) {
            l.add(lc.getId());
        } else {
            l = new ArrayList();
            l.add(lc.getId());
            linkSizeGroups.put(groupIdInt, l);
        }

        int oldLinkSizeId = lc.getLinkSizeId(dimension);
        lc.setLinkSizeId(groupId, dimension);
        
        // record undo/redo and fire event
        LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.INTERVAL_LINKSIZE_CHANGED);
        ev.setLinkSizeGroup(lc, oldLinkSizeId, groupId, dimension);
        addChange(ev);
        fireEvent(ev);
    }

    private boolean sameContainer(String compId1, String compId2) {
        LayoutComponent lc1 = getLayoutComponent(compId1);
        LayoutComponent lc2 = getLayoutComponent(compId2);
        return lc1.getParent().equals(lc2.getParent());
    }
    
    void removeComponentFromLinkSizedGroup(LayoutComponent comp, int dimension) {

        if (comp == null) return;
        
        int linkId = comp.getLinkSizeId(dimension);
        if (linkId != NOT_EXPLICITLY_DEFINED) {

            Map map = (dimension == HORIZONTAL) ? linkSizeGroupsH : linkSizeGroupsV;
            Integer linkIdInt = new Integer(linkId);
            
            List l = null;
            l = (List)map.get(linkIdInt);
            l.remove(comp.getId());
            comp.setLinkSizeId(NOT_EXPLICITLY_DEFINED, dimension);
            
            if (l.size() == 1) {
                LayoutComponent lc = getLayoutComponent((String)l.get(0));
                int oldLinkSizeId = lc.getLinkSizeId(dimension);
                lc.setLinkSizeId(NOT_EXPLICITLY_DEFINED, dimension);
                map.remove(linkIdInt);
                // record undo/redo and fire event
                LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.INTERVAL_LINKSIZE_CHANGED);
                ev.setLinkSizeGroup(lc, oldLinkSizeId, NOT_EXPLICITLY_DEFINED, dimension);
                addChange(ev);
                fireEvent(ev);
            }
            
            if (l.size() == 0) {
                map.remove(linkIdInt);
            }

            // record undo/redo and fire event
            LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.INTERVAL_LINKSIZE_CHANGED);
            ev.setLinkSizeGroup(comp, linkId, NOT_EXPLICITLY_DEFINED, dimension);
            addChange(ev);
            fireEvent(ev);
        }
    }
    
    /**
     * @return returns FALSE if components are not linked, and if so, they are linked in the same group
     *         returns TRUE if all components are in the same linksize group
     *         returns INVALID if none of above is true
     */
    public int areComponentsLinkSized(List/*<String>*/ components, int dimension) {

        if (components.size() == 1) {
            String id = (String)components.get(0);
            boolean retVal = (getLayoutComponent(id).isLinkSized(dimension));
            return retVal ? TRUE : FALSE;
        }
        
        Iterator i = components.iterator();
        boolean someUnlinkedPresent = false;
        List idsFound = new ArrayList();

        while (i.hasNext()) {
            String cid = (String)i.next();
            LayoutComponent lc = getLayoutComponent(cid);
            Integer linkSizeId =  new Integer(lc.getLinkSizeId(dimension));
            if (!idsFound.contains(linkSizeId)) {
                idsFound.add(linkSizeId);
            }
            if (idsFound.size() > 2) { // components are from at least two different groups
                return INVALID;
            }
        }
        if (idsFound.size() == 1) {
            if (idsFound.contains(new Integer(NOT_EXPLICITLY_DEFINED))) {
                return FALSE;
            }
            return TRUE;
        }
        if (idsFound.contains(new Integer(NOT_EXPLICITLY_DEFINED))) { // == 2 elements
            return FALSE;
        } else {
            return INVALID;
        }
    }
        
    Map getLinkSizeGroups(int dimension) {
        if (HORIZONTAL == dimension) {
            return linkSizeGroupsH;
        } 
        if (VERTICAL == dimension) {
            return linkSizeGroupsV;
        }
        return null; // incorrect dimension passed
    }
    
    public void unsetSameSize(List/*<String>*/ components, int dimension) {
        Iterator i = components.iterator();
        while (i.hasNext()) {
            String cid = (String)i.next();
            LayoutComponent lc = getLayoutComponent(cid);
            removeComponentFromLinkSizedGroup(lc, dimension);            
        }
    }
    
    public void setSameSize(List/*<String>*/ components, int dimension) {
        Iterator i = components.iterator();
        int groupId = findGroupId(components, dimension);
        
        while (i.hasNext()) {
            String cid = (String)i.next();
            LayoutComponent lc = getLayoutComponent(cid);
            addComponentToLinkSizedGroup(groupId, lc.getId(), dimension); 
        }
    }
    
    private int findGroupId(List/*<String*/ components, int dimension) {
        Iterator i = components.iterator();
        while (i.hasNext()) {
            String cid = (String)i.next();
            LayoutComponent lc = getLayoutComponent(cid);
            if (lc.isLinkSized(dimension)) {
                return lc.getLinkSizeId(dimension);
            }
        }
        return ++maxLinkGroupId;
    }
}
