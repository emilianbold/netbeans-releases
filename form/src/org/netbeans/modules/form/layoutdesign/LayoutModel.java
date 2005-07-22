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
        idToComponents.remove(comp.getId());
    }

    Iterator getAllComponents() {
        return idToComponents.values().iterator();
    }
    
    public void addNewComponent(LayoutComponent component, LayoutComponent parent) {
        for (int i=0; i<DIM_COUNT; i++) {
            addInterval(component.getLayoutInterval(i), parent.getLayoutRoot(i), -1);
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

    void setIntervalSize(LayoutInterval interval, int min, int pref, int max) {
        int oldMin = interval.getMinimumSize();
        int oldPref = interval.getPreferredSize();
        int oldMax = interval.getMaximumSize();
        if (min == oldMin && pref == oldPref && max == oldMax) {
            return; // no change
        }
        interval.setSizes(min, pref, max);

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
            LayoutInterval targetSub = LayoutInterval.cloneInterval(sourceSub);
            if (sourceSub.isComponent()) {
                String compId = (String)sourceToTargetIds.get(sourceSub.getComponent().getId());
                LayoutComponent comp = getLayoutComponent(compId);
                targetSub.setComponent(comp);
            } else if (sourceSub.isGroup()) {
                copyInterval(sourceSub, targetSub, sourceToTargetIds);
            }
            addInterval(targetSub, targetInterval, -1);
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
        return new Integer(changeMark + redoMap.size());
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
            changeMark += redoMap.size(); // to ensure unique marks
            redoMap.clear();

            if (undoMap.size() == 0) {
                oldestMark = changeMark;
            }

            undoMap.put(new Integer(changeMark++), change);

            while (undoMap.size() > changeCountHardLimit) {
                undoMap.remove(new Integer(oldestMark++));
            }
        }
    }

    boolean undoToMark(Object mark) {
        assert !undoRedoInProgress;
        if (!undoMap.containsKey(mark)) {
            return false; // the mark is not present in the undo queue
        }

        int lastMark = ((Integer)mark).intValue();
        undoRedoInProgress = true;

        while (changeMark > lastMark) {
            Object key = new Integer(--changeMark);
            LayoutEvent change = (LayoutEvent) undoMap.remove(key);
            if (change != null) {
                change.undo();
                redoMap.put(key, change);
            }
        }

        undoRedoInProgress = false;
        return true;
    }

    boolean redoToMark(Object mark) {
        assert !undoRedoInProgress;
//        if (!redoMap.containsKey(mark)) {
//            return false; // the mark is not present in the redo queue
//        }

        int toMark = ((Integer)mark).intValue();
        undoRedoInProgress = true;

        while (changeMark < toMark) {
            Object key = new Integer(changeMark++);
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
            undoToMark(startMark);
        }

        public void redo() throws CannotRedoException {
            super.redo();
            redoToMark(endMark);
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

}
