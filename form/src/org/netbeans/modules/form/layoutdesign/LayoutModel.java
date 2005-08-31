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

    // supposing none of removing components is a parent of another
    public void removeComponents(String[] componentIds) {
        for (int i=0; i < componentIds.length; i++) {
            removeComponentAndIntervals(componentIds[i], true);
        }
    }

    public void removeComponentAndIntervals(String compId, boolean fromModel) {
        LayoutComponent comp = getLayoutComponent(compId);
        if (comp != null) {
            boolean wasRoot = (comp.getParent() == null);
            removeComponent(comp, fromModel);
            if (!wasRoot && fromModel) {
                for (int j=0; j < DIM_COUNT; j++) {
                    removeInterval(comp.getLayoutInterval(j));
                }
            }
        }
    }

    void removeComponents(LayoutComponent[] components) {
        for (int i=0; i < components.length; i++) {
            LayoutComponent comp = components[i];
            removeComponent(comp);
            for (int j=0; j < DIM_COUNT; j++) {
                removeInterval(comp.getLayoutInterval(j));
            }
        }
    }

    /**
     * @return false if the component does not exist in the layout model
     */
    public boolean changeComponentToContainer(String componentId) {
        LayoutComponent component = getLayoutComponent(componentId);
        if (component != null) {
            component.setLayoutContainer(true);
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
        Iterator it = component.getSubcomponents();
        while (it.hasNext()) {
            LayoutComponent sub = (LayoutComponent) it.next();
            if (!sub.isLayoutContainer()) { // non-container without a parent is useless
                unregisterComponent(sub, false);
            }
        }
        component.setLayoutContainer(false); // this removes everything from the component
        if (component.getParent() == null) { // non-container without a parent
            unregisterComponent(component, false);
        }
        return true;
    }

    // -----

    void registerComponent(LayoutComponent comp, boolean recursive) {
        idToComponents.put(comp.getId(), comp);
        if (recursive) {
            for (Iterator it=comp.getSubcomponents(); it.hasNext(); ) {
                registerComponent((LayoutComponent)it.next(), recursive);
            }
        }
    }

    void unregisterComponent(LayoutComponent comp, boolean recursive) {
        if (recursive) {
            for (Iterator it=comp.getSubcomponents(); it.hasNext(); ) {
                unregisterComponent((LayoutComponent)it.next(), recursive);
            }
        }
        removeComponentFromLinkSizedGroup(comp, HORIZONTAL);
        removeComponentFromLinkSizedGroup(comp, VERTICAL);
        idToComponents.remove(comp.getId());
    }

    Iterator getAllComponents() {
        return idToComponents.values().iterator();
    }
    
    public void addNewComponent(LayoutComponent component, LayoutComponent parent, LayoutComponent prototype) {
        for (int i=0; i<DIM_COUNT; i++) {
            LayoutInterval interval = component.getLayoutInterval(i);
            addInterval(interval, parent.getLayoutRoot(i), -1);
            setIntervalAlignment(interval, DEFAULT);
            setIntervalSize(interval, USE_PREFERRED_SIZE, interval.getPreferredSize(), USE_PREFERRED_SIZE);
            if (prototype != null) {
                LayoutInterval pInt = prototype.getLayoutInterval(i);
                setIntervalSize(interval, interval.getMinimumSize(), pInt.getPreferredSize(), interval.getMaximumSize());
            }
        }
        addComponent(component, parent, -1);
    }

    // Note this method does not care about adding the layout intervals of the
    // component, it must be done in advance.
    void addComponent(LayoutComponent component, LayoutComponent parent, int index) {
        assert component.getParent() == null;

        if (parent != null) {
            assert getLayoutComponent(parent.getId()) == parent;
            index = parent.add(component, index);
        }
        else {
            assert component.isLayoutContainer();
        }
        registerComponent(component, true);

        // record undo/redo and fire event
        LayoutEvent ev = new LayoutEvent(this, LayoutEvent.COMPONENT_ADDED);
        ev.setComponent(component, parent, index);
        addChange(ev);
        fireEvent(ev);
    }

    void removeComponent(LayoutComponent component) {
        removeComponent(component, true);
    }
    
    // Low level removal - removes the component from parent, unregisters it,
    // records the change for undo/redo, and fires an event. Does nothing to
    // the layout intervals of the component.
    void removeComponent(LayoutComponent component, boolean fromModel) {
        int index;
        LayoutComponent parent = component.getParent();
        if (parent != null) {
            index = parent.remove(component);
        }
        else {
            index = -1;
        }
        if (fromModel) {
            unregisterComponent(component, true);
        }

        
        // record undo/redo and fire event
        LayoutEvent ev = new LayoutEvent(this, LayoutEvent.COMPONENT_REMOVED);
        ev.setComponent(component, parent, index);
        addChange(ev);
        fireEvent(ev);
    }
    
    void addInterval(LayoutInterval interval, LayoutInterval parent, int index) {
        assert interval.getParent() == null;

        index = parent.add(interval, index);

        // record undo/redo and fire event
        LayoutEvent ev = new LayoutEvent(this, LayoutEvent.INTERVAL_ADDED);
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
        LayoutEvent ev = new LayoutEvent(this, LayoutEvent.INTERVAL_REMOVED);
        ev.setInterval(interval, parent, index);
        addChange(ev);
        fireEvent(ev);

        return index;
    }

    LayoutInterval removeInterval(LayoutInterval parent, int index) {
        LayoutInterval interval = parent.remove(index);

        // record undo/redo and fire event
        LayoutEvent ev = new LayoutEvent(this, LayoutEvent.INTERVAL_REMOVED);
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
        LayoutEvent ev = new LayoutEvent(this, LayoutEvent.INTERVAL_ATTRIBUTES_CHANGED);
        ev.setAttributes(interval, oldAttributes, newAttributes);
        addChange(ev);
    }

    void setIntervalAlignment(LayoutInterval interval, int alignment) {
        int oldAlignment = interval.getRawAlignment();
        interval.setAlignment(alignment);

        // record undo/redo (don't fire event)
        LayoutEvent ev = new LayoutEvent(this, LayoutEvent.INTERVAL_ALIGNMENT_CHANGED);
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
        LayoutEvent ev = new LayoutEvent(this, LayoutEvent.GROUP_ALIGNMENT_CHANGED);
        ev.setAlignment(group, oldAlignment, alignment);
        addChange(ev);
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
        LayoutEvent ev = new LayoutEvent(this, LayoutEvent.INTERVAL_SIZE_CHANGED);
        ev.setSize(interval, oldMin, oldPref, oldMax, min, pref, max);
        addChange(ev);
    }
    
    public void copyModelFrom(LayoutModel sourceModel, Map/*<String,String>*/ sourceToTargetIds,
            String sourceContainerId, String targetContainerId) {
        LayoutComponent sourceContainer = sourceModel.getLayoutComponent(sourceContainerId);
        LayoutComponent targetContainer = getLayoutComponent(targetContainerId);
        if (targetContainer == null) {
            targetContainer = new LayoutComponent(targetContainerId, true);
            addRootComponent(targetContainer);
        } else if (!targetContainer.isLayoutContainer()) {
            changeComponentToContainer(targetContainerId);
        }
        // Create LayoutComponents
        Iterator iter = sourceToTargetIds.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String targetId = (String)entry.getValue();
            LayoutComponent targetLC = getLayoutComponent(targetId);
            if (targetLC == null) {
                String sourceId = (String)entry.getKey();
                LayoutComponent sourceLC = sourceModel.getLayoutComponent(sourceId);
                targetLC = new LayoutComponent(targetId, sourceLC.isLayoutContainer());
                addComponent(targetLC, targetContainer, -1);
            }
        }
        // Copy LayoutIntervals
        for (int dim=0; dim<DIM_COUNT; dim++) {
            LayoutInterval sourceInterval = sourceContainer.getLayoutRoot(dim);
            LayoutInterval targetInterval = targetContainer.getLayoutRoot(dim);
            copyInterval(sourceInterval, targetInterval, sourceToTargetIds);
        }
    }
    
    private void copyInterval(LayoutInterval sourceInterval, LayoutInterval targetInterval, Map/*<String,String>*/ sourceToTargetIds) {
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
                copyInterval(sourceSub, targetSub, sourceToTargetIds);
            }
            addInterval(targetSub, targetInterval, -1);
        }
    }
    
    /**
     * Creates layout model based on the current layout represented
     * by bounds of given components.
     *
     * @param idToComponent maps component Id to <code>Component</code>.
     */
    public void createModel(String containerId, Container cont, Map idToComponent) {
        if (idToComponent.isEmpty()) return;
        LayoutComponent lCont = getLayoutComponent(containerId);
        assert (lCont != null);
        Insets insets = new Insets(0, 0, 0, 0);
        if (cont instanceof javax.swing.JComponent) {
            javax.swing.border.Border border = ((javax.swing.JComponent)cont).getBorder();
            if (border != null) {
                insets = border.getBorderInsets(cont);
            }
        }
        Map idToBounds = new HashMap();
        Iterator iter = idToComponent.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String id = (String)entry.getKey();
            Component component = (Component)entry.getValue();
            LayoutComponent lComp = getLayoutComponent(id);
            if (lComp == null) {
                lComp = new LayoutComponent(id, false);
                addComponent(lComp, lCont, -1);
            }
            Rectangle bounds = component.getBounds();
            bounds = new Rectangle(bounds.x - insets.left, bounds.y - insets.top, bounds.width, bounds.height);
            idToBounds.put(id, bounds);
            Dimension dim = component.getPreferredSize();
            if (dim.width != bounds.width) {
                LayoutInterval interval = lComp.getLayoutInterval(HORIZONTAL);
                setIntervalSize(interval, interval.getMinimumSize(), bounds.width, interval.getMaximumSize());
            }
            if (dim.height != bounds.height) {
                LayoutInterval interval = lComp.getLayoutInterval(VERTICAL);
                setIntervalSize(interval, interval.getMinimumSize(), bounds.height, interval.getMaximumSize());
            }
        }
        RegionInfo region = new RegionInfo(idToBounds);
        region.calculateIntervals();
        // PENDING don't insert parallel group to parallel group
        addInterval(region.getInterval(HORIZONTAL), lCont.getLayoutRoot(HORIZONTAL), -1);
        addInterval(region.getInterval(VERTICAL), lCont.getLayoutRoot(VERTICAL), -1);
    }
    
    private class RegionInfo {
        private LayoutInterval horizontal = null;
        private LayoutInterval vertical = null;
        private Map idToBounds;
        private int minx;
        private int maxx;
        private int miny;
        private int maxy;
        private int dimension;

        public RegionInfo(Map idToBounds) {
            this.idToBounds = idToBounds;
            this.dimension = -1;
            minx = miny = 0;
            updateRegionBounds();
        }
        
        private RegionInfo(Map idToBounds, int dimension) {
            this.idToBounds = idToBounds;
            this.dimension = dimension;
            minx = miny = Short.MAX_VALUE;
            updateRegionBounds();
        }
        
        private void updateRegionBounds() {
            maxy = maxx = Short.MIN_VALUE;
            Iterator iter = idToBounds.values().iterator();
            while (iter.hasNext()) {
                Rectangle bounds = (Rectangle)iter.next();
                minx = Math.min(minx, bounds.x);
                miny = Math.min(miny, bounds.y);
                maxx = Math.max(maxx, bounds.x + bounds.width);
                maxy = Math.max(maxy, bounds.y + bounds.height);
            }
        }

        public void calculateIntervals() {
            if (idToBounds.size() == 1) {
                String id = (String)idToBounds.keySet().iterator().next();
                Rectangle bounds = (Rectangle)idToBounds.get(id);
                LayoutComponent comp = getLayoutComponent(id);
                horizontal = comp.getLayoutInterval(HORIZONTAL);
                horizontal = prefixByGap(horizontal, bounds.x - minx);
                vertical = comp.getLayoutInterval(VERTICAL);
                vertical = prefixByGap(vertical, bounds.y - miny);
                return;
            }
            int effDim = -1;
            List parts = null;
            Map removedIdToBounds = null;
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
                    String id = (String)idToBounds.keySet().iterator().next();
                    Rectangle bounds = (Rectangle)idToBounds.remove(id);
                    if (removedIdToBounds == null) {
                        removedIdToBounds = new HashMap();
                    }
                    removedIdToBounds.put(id, bounds);
                }
                Set cutSet = createPossibleCuts(effDim);
                parts = cutIntoParts(cutSet, effDim);            
            } while (!idToBounds.isEmpty() && parts.isEmpty());
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
            if (removedIdToBounds != null) {
                for (int dim = HORIZONTAL; dim <= VERTICAL; dim++) {
                    iter = removedIdToBounds.entrySet().iterator();
                    LayoutInterval parent = (dim == HORIZONTAL) ? horizontal : vertical;
                    if (!parent.isParallel()) {
                        LayoutInterval parGroup = new LayoutInterval(PARALLEL);
                        addInterval(parent, parGroup, -1);
                        if (dim == HORIZONTAL) {
                            horizontal = parGroup;
                        } else {
                            vertical = parGroup;
                        }
                        parent = parGroup;
                    }
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry)iter.next();
                        String id = (String)entry.getKey();
                        Rectangle bounds = (Rectangle)entry.getValue();
                        LayoutComponent comp = getLayoutComponent(id);
                        LayoutInterval interval = comp.getLayoutInterval(dim);
                        int gap = (dim == HORIZONTAL) ? bounds.x - minx : bounds.y - miny;
                        interval = prefixByGap(interval, gap);
                        addInterval(interval, parent, -1);
                    }
                }
            }
        }
        
        private SortedSet createPossibleCuts(int dimension) {
            SortedSet cutSet = new TreeSet();
            Iterator iter = idToBounds.keySet().iterator();
            while (iter.hasNext()) {
                String id = (String)iter.next();
                Rectangle bounds = (Rectangle)idToBounds.get(id);
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
                Map preIdToBounds = new HashMap();
                Map postIdToBounds = new HashMap();
                Iterator it = idToBounds.entrySet().iterator();                
                while (isCut && it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    String id = (String)entry.getKey();
                    Rectangle bounds = (Rectangle)entry.getValue();
                    int leading = (dimension == HORIZONTAL) ? bounds.x : bounds.y;
                    int trailing = leading + ((dimension == HORIZONTAL) ? bounds.width : bounds.height);
                    if (leading >= cut) {
                        postIdToBounds.put(id, bounds);
                    } else if (trailing <= cut) {
                        preIdToBounds.put(id, bounds);
                    } else {
                        isCut = false;
                    }
                }
                if (isCut && !preIdToBounds.isEmpty()
                    // the last cut candidate (end of the region) cannot be the first cut
                    && (!parts.isEmpty() || (preIdToBounds.size() != idToBounds.size()))) {
                    idToBounds.keySet().removeAll(preIdToBounds.keySet());
                    parts.add(preIdToBounds);
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
                    addInterval(gap, seqGroup, -1);
                }
                addInterval(seqInterval, seqGroup, -1);
                parInterval = prefixByGap(parInterval, parGap);
                addInterval(parInterval, parGroup, -1);
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
                LayoutInterval group = new LayoutInterval(SEQUENTIAL);
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setSize(size);
                addInterval(gap, group, -1);
                addInterval(interval, group, -1);
                interval = group;
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

    boolean isChangeRecording() {
        return recordingChanges;
    }

    void setChangeRecording(boolean record) {
        recordingChanges = record;
    }

    boolean isUndoRedoInProgress() {
        return undoRedoInProgress;
    }

    public Object getChangeMark() {
        return new Integer(changeMark);
    }

    public UndoableEdit getUndoableEdit() {
        if (recordingChanges && !undoRedoInProgress) {
            LayoutUndoableEdit undoEdit = new LayoutUndoableEdit();
            undoEdit.startMark = getChangeMark();
            if (lastUndoableEdit != null) {
                lastUndoableEdit.endMark = undoEdit.startMark;
            }
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
     * Returns dump of the layout model.
     *
     * @return dump of the layout model.
     */
    public String dump(Map idToNameMap) {
        Set roots = new HashSet();
        Iterator iter = idToComponents.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            LayoutComponent comp = (LayoutComponent)entry.getValue();
            if (comp.isLayoutContainer()) {
                roots.add(comp);
            }
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<LayoutModel>\n"); // NOI18N
        Iterator rootIter = roots.iterator();
        while (rootIter.hasNext()) {
            LayoutComponent root = (LayoutComponent)rootIter.next();
            sb.append("  <Root>\n"); // NOI18N
            sb.append(dumpLayout(2, root, idToNameMap, true));
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
        return new LayoutPersistenceManager(this).saveIntervalLayout(2, interval, dimension);
    }
    
    /**
     * Returns dump of the layout model.
     *
     * @param indent determines size of indentation.
     * @param root container layout model should be dumped.
     * @param humanReadable determines whether constants should be replaced
     * by human readable expressions.
     * @return dump of the layout model.
     */
    public String dumpLayout(int indent, LayoutComponent root, Map idToNameMap, boolean humanReadable) {
        return new LayoutPersistenceManager(this).saveLayout(indent, root, idToNameMap, humanReadable);
    }
    
    /**
     * Loads the layout of the given container.
     *
     * @param rootId ID of the layout root (the container whose layout should be loaded).
     * @param dimLayoutList nodes holding the information about the layout.
     * @param nameToIdMap map from component names to component IDs.
     */
    public void loadModel(String rootId, org.w3c.dom.NodeList dimLayoutList, Map nameToIdMap) {
        new LayoutPersistenceManager(this).loadModel(rootId, dimLayoutList, nameToIdMap);
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
        LayoutComponent lc = getLayoutComponent(compId);        
        Integer groupIdInt = new Integer(groupId);
        Map linkSizeGroups = (dimension == HORIZONTAL) ? linkSizeGroupsH : linkSizeGroupsV;
        List l = (List)linkSizeGroups.get(groupIdInt);
        if (l != null) {
            if ((!l.contains(compId)) && (sameContainer(compId, (String)l.get(0)))) {
                l.add(compId);
            } else {
                return;
            }
        } else {
            l = new ArrayList();
            l.add(compId);
            linkSizeGroups.put(groupIdInt, l);
        }

        int oldLinkSizeId = lc.getLinkSizeId(dimension);
        lc.setLinkSizeId(groupId, dimension);
        
        // record undo/redo and fire event
        LayoutEvent ev = new LayoutEvent(this, LayoutEvent.INTERVAL_LINKSIZE_CHANGED);
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
                LayoutEvent ev = new LayoutEvent(this, LayoutEvent.INTERVAL_LINKSIZE_CHANGED);
                ev.setLinkSizeGroup(lc, oldLinkSizeId, NOT_EXPLICITLY_DEFINED, dimension);
                addChange(ev);
                fireEvent(ev);
            }
            
            if (l.size() == 0) {
                map.remove(linkIdInt);
            }

            // record undo/redo and fire event
            LayoutEvent ev = new LayoutEvent(this, LayoutEvent.INTERVAL_LINKSIZE_CHANGED);
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
            int oldLinkSizeId = lc.getLinkSizeId(dimension);
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
